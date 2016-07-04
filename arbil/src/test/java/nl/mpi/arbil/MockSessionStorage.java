/**
 * Copyright (C) 2016 The Language Archive, Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.arbil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.mpi.arbil.data.importexport.ShibbolethNegotiator;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.DownloadAbortFlag;
import nl.mpi.arbil.util.ProgressListener;
import org.slf4j.LoggerFactory;

public class MockSessionStorage implements SessionStorage {
    
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(MockSessionStorage.class);
    private static final Logger log = Logger.getLogger(MockSessionStorage.class.toString());
    private File localCacheDirectory = null;
    
    public void changeCacheDirectory(File preferedCacheDirectory, boolean moveFiles) {
	log.log(Level.INFO, "changeCacheDirectory({0},{1})", new Object[]{preferedCacheDirectory, moveFiles});
    }
    
    public File getProjectDirectory() {
	return getProjectWorkingDirectory().getParentFile();
    }

    /**
     * Tests that the cache directory exists and creates it if it does not.
     *
     * @return Boolean
     */
    public File getProjectWorkingDirectory() {
	if (localCacheDirectory == null) {
	    // load from the text based properties file
	    String localCacheDirectoryPathString = loadString("cacheDirectory");
	    if (localCacheDirectoryPathString != null) {
		localCacheDirectory = new File(localCacheDirectoryPathString);
	    } else {
		// otherwise load from the to be replaced binary based storage file
//		try {
//		    File localWorkingDirectory = (File) loadObject("cacheDirectory");
//		    localCacheDirectory = localWorkingDirectory;
//		} catch (Exception exception) {
		if (new File(getApplicationSettingsDirectory(), "imdicache").exists()) {
		    localCacheDirectory = new File(getApplicationSettingsDirectory(), "imdicache");
		} else {
		    localCacheDirectory = new File(getApplicationSettingsDirectory(), "ArbilWorkingFiles");
		}
//		}
		saveString("cacheDirectory", localCacheDirectory.getAbsolutePath());
	    }
	    boolean cacheDirExists = localCacheDirectory.exists();
	    if (!cacheDirExists) {
		if (!localCacheDirectory.mkdirs()) {
		    log.severe("Could not create cache directory");
		    return null;
		}
	    }
	}
	return localCacheDirectory;
    }
    
    public File getExportPath(String pathString, String destinationDirectory) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Checks for the existance of the favourites directory exists and creates
     * it if it does not.
     *
     * @return File pointing to the favourites directory
     */
    public File getFavouritesDir() {
	File favDirectory = new File(getProjectDirectory(), "favourites"); // storageDirectory already has the file separator appended
	boolean favDirExists = favDirectory.exists();
	if (!favDirExists) {
	    if (!favDirectory.mkdir()) {
		log.severe("Could not create favourites directory");
		return null;
	    }
	}
	return favDirectory;
    }
    
    public String[] getLocationOptions() {
	throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public URI getNewArbilFileName(File parentDirectory, String nodeType) {
	throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public URI getOriginatingUri(URI locationInCacheURI) {
	URI returnUri = null;
	String uriPath = locationInCacheURI.getPath();
//        logger.debug("pathIsInsideCache" + storageDirectory + " : " + fullTestFile);
	logger.debug("uriPath: {}", uriPath);
	int foundPos = uriPath.indexOf("imdicache");
	if (foundPos == -1) {
	    foundPos = uriPath.indexOf("ArbilWorkingFiles");
	    if (foundPos == -1) {
		return null;
	    }
	}
	uriPath = uriPath.substring(foundPos);
	String[] uriParts = uriPath.split("/", 4);
	try {
	    if (uriParts[1].toLowerCase().equals("http")) {
		returnUri = new URI(uriParts[1], uriParts[2], "/" + uriParts[3], null); // [0] will be "imdicache"
		logger.debug("returnUri: {}", returnUri);
	    }
	} catch (URISyntaxException urise) {
	    log.severe(urise.toString());
	}
	return returnUri;
    }

    /**
     * Converts a String path from the remote location to the respective
     * location in the cache. Then tests for and creates the directory structure
     * in the cache if requred.
     *
     * @param pathString Path of the remote file.
     * @return The path in the cache for the file.
     */
    public File getSaveLocation(String pathString) {
	try {
	    pathString = URLDecoder.decode(pathString, "UTF-8");
	} catch (UnsupportedEncodingException uee) {
	    log.log(Level.SEVERE, null, uee);
	}
	pathString = pathString.replace("//", "/");
	for (String searchString : new String[]{getProjectWorkingDirectory().toString()}) {
	    if (pathString.indexOf(searchString) > -1) {
		log.log(Level.SEVERE, "Recursive path error (about to be corrected) in: {0}", pathString);
		pathString = pathString.substring(pathString.lastIndexOf(searchString) + searchString.length());
	    }
	}
	String cachePath = pathString.replace(":/", "/").replace("//", "/");
	while (cachePath.contains(":")) { // todo: this may not be the only char that is bad on file systems and this will cause issues reconstructing the url later
	    cachePath = cachePath.replace(":", "_");
	}
	// make the xsd path tidy for viewing in an editor durring testing
	cachePath = cachePath.replaceAll("/xsd$", ".xsd");
	if (cachePath.matches(".*/[^.]*$")) {
	    // rest paths will create files and then require directories of the same name and this must be avoided
	    cachePath = cachePath + ".dat";
	}
	File returnFile = new File(getProjectWorkingDirectory(), cachePath);
	if (!returnFile.getParentFile().exists()) {
	    returnFile.getParentFile().mkdirs();
	}
	return returnFile;
    }
    private HashMap<String, Object> saveMap = new HashMap<String, Object>();
    
    public boolean loadBoolean(String filename, boolean defaultValue) {
	try {
	    Object object = loadObject(filename);
	    if (object != null) {
		if (object instanceof Boolean) {
		    return (Boolean) object;
		}
	    }
	} catch (Exception ex) {
	    log.log(Level.SEVERE, null, ex);
	}
	return defaultValue;
    }
    
    public Object loadObject(String filename) throws Exception {
	return saveMap.get(filename);
    }
    
    public String loadString(String filename) {
	try {
	    Object object = loadObject(filename);
	    if (object != null) {
		if (object instanceof String) {
		    return (String) object;
		}
	    }
	    
	} catch (Exception ex) {
	    log.log(Level.SEVERE, null, ex);
	}
	return null;
    }
    
    public String[] loadStringArray(String filename) throws IOException {
	try {
	    Object object = loadObject(filename);
	    if (object != null) {
		if (object instanceof String[]) {
		    return (String[]) object;
		}
	    }
	    
	} catch (Exception ex) {
	    log.log(Level.SEVERE, null, ex);
	}
	return null;
    }

    /**
     * Tests if the a string points to a file that is in the favourites
     * directory.
     *
     * @return Boolean
     */
    public boolean pathIsInFavourites(File fullTestFile) { //todo: test me
	String favouritesString = "favourites";
	int foundPos = fullTestFile.getPath().indexOf(favouritesString) + favouritesString.length();
	if (foundPos == -1) {
	    return false;
	}
	if (foundPos > fullTestFile.getPath().length()) {
	    return false;
	}
	File testFile = new File(fullTestFile.getPath().substring(0, foundPos));
	return testFile.equals(getFavouritesDir());
    }

    /**
     * Tests if the a string points to a flie that is in the cache directory.
     *
     * @return Boolean
     */
    public boolean pathIsInsideCache(File fullTestFile) {
	File cacheDirectory = getProjectWorkingDirectory();
	File testFile = fullTestFile;
	while (testFile != null) {
	    if (testFile.equals(cacheDirectory)) {
		return true;
	    }
	    testFile = testFile.getParentFile();
	}
	return false;
    }
    
    public boolean replaceCacheCopy(String pathString) {
	throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void saveBoolean(String filename, boolean storableValue) {
	saveMap.put(filename, storableValue);
    }
    
    public void saveObject(Serializable object, String filename) throws IOException {
	saveMap.put(filename, object);
    }
    
    public boolean saveRemoteResource(URL targetUrl, File destinationFile, ShibbolethNegotiator shibbolethNegotiator, boolean expireCacheCopy, boolean followRedirect, DownloadAbortFlag abortFlag, ProgressListener progressLabel) {
	boolean downloadSucceeded = false;
//        String targetUrlString = getFullResourceURI();
//        String destinationPath = GuiHelper.linorgSessionStorage.getSaveLocation(targetUrlString);
//        logger.debug("saveRemoteResource: " + targetUrlString);
//        logger.debug("destinationPath: " + destinationPath);
//        File destinationFile = new File(destinationPath);
	if (destinationFile.length() == 0) {
	    // todo: check the file size on the server and maybe its date also
	    // if the file is zero length then is presumably should either be replaced or the version in the jar used.
	    destinationFile.delete();
	}
	String fileName = destinationFile.getName();
	if (destinationFile.exists() && !expireCacheCopy && destinationFile.length() > 0) {
	    logger.debug("this resource is already in the cache");
	} else {
	    try {
		URLConnection urlConnection = targetUrl.openConnection();
		HttpURLConnection httpConnection = null;
		if (urlConnection instanceof HttpURLConnection) {
		    httpConnection = (HttpURLConnection) urlConnection;
//                    httpConnection.setFollowRedirects(false); // this is done when this class is created because it is a static call
		    if (shibbolethNegotiator != null) {
			httpConnection = shibbolethNegotiator.getShibbolethConnection((HttpURLConnection) urlConnection);
//                        if (httpConnection.getResponseCode() != 200 && targetUrl.getProtocol().equals("http")) {
//                            // work around for resources being https when under shiboleth
//                            // try https after http failed
//                            logger.debug("Code: " + httpConnection.getResponseCode() + ", Message: " + httpConnection.getResponseMessage());
//                            logger.debug("trying https");
//                            targetUrl = new URL(targetUrl.toString().replace("http://", "https://"));
//                            urlConnection = targetUrl.openConnection();
//                            httpConnection = shibbolethNegotiator.getShibbolethConnection((HttpURLConnection) urlConnection);
//                        }
		    }
		    //h.setFollowRedirects(false);
		    logger.debug("Code: " + httpConnection.getResponseCode() + ", Message: " + httpConnection.getResponseMessage());
		}
		if (httpConnection != null && httpConnection.getResponseCode() != 200) { // if the url points to a file on disk then the httpconnection will be null, hence the response code is only relevant if the connection is not null
		    if (httpConnection == null) {
			logger.debug("httpConnection is null, hence this is a local file and we should not have been testing the response code");
		    } else {
			logger.debug("non 200 response, skipping file");
		    }
		} else {
		    File tempFile = File.createTempFile(destinationFile.getName(), "tmp", destinationFile.getParentFile());
		    tempFile.deleteOnExit();
		    int bufferLength = 1024 * 3;
		    FileOutputStream outFile = new FileOutputStream(tempFile); //targetUrlString
		    logger.debug("getting file");
		    InputStream stream = urlConnection.getInputStream();
		    byte[] buffer = new byte[bufferLength]; // make this 1024*4 or something and read chunks not the whole file
		    int bytesread = 0;
		    int totalRead = 0;
		    while (bytesread >= 0 && !abortFlag.abortDownload) {
			bytesread = stream.read(buffer);
			totalRead += bytesread;
//                        logger.debug("bytesread: " + bytesread);
//                        logger.debug("Mbs totalRead: " + totalRead / 1048576);
			if (bytesread == -1) {
			    break;
			}
			outFile.write(buffer, 0, bytesread);
			if (progressLabel != null) {
			    progressLabel.setProgressText(fileName + " : " + totalRead / 1024 + " Kb");
			}
		    }
		    outFile.close();
		    if (tempFile.length() > 0 && !abortFlag.abortDownload) { // TODO: this should check the file size on the server
			if (destinationFile.exists()) {
			    destinationFile.delete();
			}
			tempFile.renameTo(destinationFile);
			downloadSucceeded = true;
		    }
		    logger.debug("Downloaded: " + totalRead / (1024 * 1024) + " Mb");
		}
	    } catch (Exception ex) {
		log.log(Level.SEVERE, null, ex);
//                logger.debug(ex.getMessage());
	    }
	}
	return downloadSucceeded;
    }
    
    public void saveString(String filename, String storableValue) {
	saveMap.put(filename, storableValue);
    }
    
    public void saveStringArray(String filename, String[] storableValue) throws IOException {
	saveMap.put(filename, storableValue);
    }

    /**
     * Fetch the file from the remote URL and save into the cache. Currently
     * this does not expire the objects in the cache, however that will be
     * required in the future.
     *
     * @param pathString Path of the remote file.
     * @param expireCacheDays Number of days old that a file can be before it is
     * replaced.
     * @return The path of the file in the cache.
     */
    public synchronized File updateCache(String pathString, int expireCacheDays, boolean followRedirect) { // update if older than the date - x
	File targetFile = getSaveLocation(pathString);
	boolean fileNeedsUpdate = !targetFile.exists();
	if (!fileNeedsUpdate) {
	    Date lastModified = new Date(targetFile.lastModified());
	    Date expireDate = new Date(System.currentTimeMillis());
	    logger.debug("updateCache: " + expireDate + " : " + lastModified + " : " + targetFile.getAbsolutePath());
	    
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTime(expireDate);
	    calendar.add(Calendar.DATE, -expireCacheDays);
	    expireDate.setTime(calendar.getTime().getTime());
	    
	    logger.debug("updateCache: " + expireDate + " : " + lastModified + " : " + targetFile.getAbsolutePath());
	    
	    fileNeedsUpdate = expireDate.after(lastModified);
	    logger.debug("fileNeedsUpdate: " + fileNeedsUpdate);
	}
	logger.debug("fileNeedsUpdate: " + fileNeedsUpdate);
	return updateCache(pathString, null, fileNeedsUpdate, followRedirect, new DownloadAbortFlag(), null);
    }
    
    public File getFromCache(String pathString, boolean followRedirect) {
	return updateCache(pathString, Integer.MAX_VALUE, followRedirect);
    }

    /**
     * Fetch the file from the remote URL and save into the cache. Currently
     * this does not expire the objects in the cache, however that will be
     * required in the future.
     *
     * @param pathString Path of the remote file.
     * @return The path of the file in the cache.
     */
    public File updateCache(String pathString, ShibbolethNegotiator shibbolethNegotiator, boolean expireCacheCopy, boolean followRedirect, DownloadAbortFlag abortFlag, ProgressListener progressLabel) {
	
	if (pathString.equals("http://www.w3.org/2001/xml.xsd")) {
	    try {
		return new File(getClass().getResource("/nl/mpi/arbil/data/xml.xsd").toURI());
	    } catch (URISyntaxException ex) {
		log.warning("Could not get local resource for " + pathString);
	    }
	}

	// to expire the files in the cache set the expireCacheCopy flag.
	File cachePath = getSaveLocation(pathString);
	try {
	    saveRemoteResource(new URL(pathString), cachePath, shibbolethNegotiator, expireCacheCopy, followRedirect, abortFlag, progressLabel);
	} catch (MalformedURLException mul) {
	    log.log(Level.SEVERE, null, new Exception(pathString, mul));
	}
	return cachePath;
    }
    private File tempDir;
    
    public synchronized File getApplicationSettingsDirectory() {
	if (tempDir == null) {
	    try {
		tempDir = File.createTempFile("arbil", Long.toString(System.nanoTime()), new File(System.getProperty("java.io.tmpdir")));
		if (tempDir.exists()) {
		    if (!tempDir.delete()) {
			throw new RuntimeException("Cannot create temp dir!");
		    }
		}
		if (tempDir.mkdir()) {
		    tempDir.deleteOnExit();
		} else {
		    throw new RuntimeException("Cannot create temp dir!");
		}
		log.log(Level.INFO, "Created temp dir {0}", tempDir.getAbsolutePath());
	    } catch (IOException ex) {
		log.log(Level.SEVERE, null, ex);
	    }
	}
	return tempDir;
    }
    
    public File getTypeCheckerConfig() {
	return null;
    }
    
    public void cleanUp() {
	if (tempDir != null) {
	    deleteRecursively(tempDir);
	}
    }
    
    private boolean deleteRecursively(File file) {
	if (!file.exists()) {
	    return false;
	} else {
	    if (file.isDirectory()) {
		for (File dirChild : file.listFiles()) {
		    if (!deleteRecursively(dirChild)) {
			return false;
		    }
		}
	    }
	    return file.delete();
	}
    }
}
