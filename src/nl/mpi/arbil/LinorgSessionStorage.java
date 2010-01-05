package nl.mpi.arbil;

import nl.mpi.arbil.data.ImdiTreeObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;
import javax.swing.JOptionPane;
import nl.mpi.arbil.importexport.ShibbolethNegotiator;

/**
 * Document   : LinorgSessionStorage
 * use to save and load objects from disk and to manage items in the local cache
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
public class LinorgSessionStorage {

    public String storageDirectory = null; // TODO: change this to a File
    public String cacheDirectory; // TODO: change this to a File
    static private LinorgSessionStorage singleInstance = null;
//    JDialog settingsjDialog;

    static synchronized public LinorgSessionStorage getSingleInstance() {
        if (singleInstance == null) {
            singleInstance = new LinorgSessionStorage();
        }
        return singleInstance;
    }

    private LinorgSessionStorage() {
        String storageDirectoryArray[] = getLocationOptions();

        // look for an existing storage directory
        for (String currentStorageDirectory : storageDirectoryArray) {
            File storageFile = new File(currentStorageDirectory);
            if (storageFile.exists()) {
                System.out.println("existing storage directory found: " + currentStorageDirectory);
                storageDirectory = currentStorageDirectory;
                break;
            }
        }

        String testedStorageDirectories = "";
        if (storageDirectory == null) {
            for (String currentStorageDirectory : storageDirectoryArray) {
                if (!currentStorageDirectory.startsWith("null")) {
                    File storageFile = new File(currentStorageDirectory);
                    if (!storageFile.exists()) {
                        storageFile.mkdir();
                        if (!storageFile.exists()) {
                            testedStorageDirectories = testedStorageDirectories + currentStorageDirectory + "\n";
                            System.out.println("failed to create: " + currentStorageDirectory);
                        } else {
                            System.out.println("created new storage directory: " + currentStorageDirectory);
                            storageDirectory = currentStorageDirectory;
                            break;
                        }
                    }
                }
            }
        }
        if (storageDirectory == null) {
            //LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Could not create a working directory.\n" + testedStorageDirectories + "There may be issues creating, editing and saving.", null);
            JOptionPane.showMessageDialog(LinorgWindowManager.getSingleInstance().linorgFrame, "Could not create a working directory in any of the potential location:\n" + testedStorageDirectories + "Please check that you have write permissions in at least one of these locations.\nThe application will now exit.", "Arbil Critical Error", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
        System.out.println("storageDirectory: " + storageDirectory);
        System.out.println("cacheDirExists: " + cacheDirExists());
    }

    public void changeStorageDirectory(String preferedDirectory) {
        boolean success = new File(storageDirectory).renameTo(new File(preferedDirectory));
        if (!success) {
            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Could not move the storage directory.\n", null);
        } else {
            try {
                Vector<String> locationsList = new Vector<String>();
                for (ImdiTreeObject[] currentTreeArray : new ImdiTreeObject[][]{TreeHelper.getSingleInstance().remoteCorpusNodes, TreeHelper.getSingleInstance().localCorpusNodes, TreeHelper.getSingleInstance().localFileNodes, TreeHelper.getSingleInstance().favouriteNodes}) {
                    for (ImdiTreeObject currentLocation : currentTreeArray) {
                        locationsList.add(currentLocation.getUrlString().replace(storageDirectory, preferedDirectory));
                    }
                }
                storageDirectory = preferedDirectory;
                LinorgSessionStorage.getSingleInstance().saveObject(locationsList, "locationsList");
                System.out.println("updated locationsList");
            } catch (Exception ex) {
                GuiHelper.linorgBugCatcher.logError(ex);
//            System.out.println("save locationsList exception: " + ex.getMessage());
            }
            TreeHelper.getSingleInstance().loadLocationsList();
        }
    }

    public String[] getLocationOptions() {
        return new String[]{
                    // System.getProperty("user.dir") is unreliable in the case of Vista and possibly others
                    //http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6519127
                    System.getenv("APPDATA") + File.separatorChar + ".arbil" + File.separatorChar,
                    System.getProperty("user.home") + File.separatorChar + ".arbil" + File.separatorChar,
                    System.getenv("USERPROFILE") + File.separatorChar + ".arbil" + File.separatorChar,
                    System.getProperty("user.dir") + File.separatorChar + ".arbil" + File.separatorChar,
                    // keep checking for linorg for users with old data
                    System.getenv("APPDATA") + File.separatorChar + ".linorg" + File.separatorChar,
                    System.getProperty("user.home") + File.separatorChar + ".linorg" + File.separatorChar,
                    System.getenv("USERPROFILE") + File.separatorChar + ".linorg" + File.separatorChar,
                    System.getProperty("user.dir") + File.separatorChar + ".linorg" + File.separatorChar
                };
    }

//    public void showDirectorySelectionDialogue() {
//        settingsjDialog = new JDialog(JOptionPane.getFrameForComponent(LinorgWindowManager.getSingleInstance().linorgFrame));
//        settingsjDialog.setLocationRelativeTo(LinorgWindowManager.getSingleInstance().linorgFrame);
//        JPanel optionsPanel = new JPanel();
//        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.PAGE_AXIS));
//        ButtonGroup group = new ButtonGroup();
//        for (String currentLocation : getLocationOptions()) {
//            if (!currentLocation.startsWith("null")) {
//                JRadioButton locationButton = new JRadioButton(currentLocation);
//                locationButton.setActionCommand(currentLocation);
//                locationButton.setSelected(storageDirectory.equals(currentLocation));
//                group.add(locationButton);
//                optionsPanel.add(locationButton);
////            birdButton.addActionListener(this);
//            }
//        }
//        JPanel buttonsPanel = new JPanel();
//        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 30));
//        JButton moveButton = new JButton("Move");
//        moveButton.addActionListener(new java.awt.event.ActionListener() {
//
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//
//                settingsjDialog.dispose();
//                settingsjDialog = null;
//            }
//        });
//        JButton cancelButton = new JButton("Cancel");
//        cancelButton.addActionListener(new java.awt.event.ActionListener() {
//
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                settingsjDialog.dispose();
//                settingsjDialog = null;
//            }
//        });
//        moveButton.setEnabled(false);
//        buttonsPanel.add(moveButton);
//        buttonsPanel.add(cancelButton);
//        optionsPanel.add(buttonsPanel);
//        settingsjDialog.add(optionsPanel);
////        optionsPanel.setBackground(Color.BLUE);
////        buttonsPanel.setBackground(Color.GREEN);
//        settingsjDialog.setTitle("Storage Directory Location");
////        settingsjDialog.setMinimumSize(new Dimension(400, 300));
//        settingsjDialog.pack();
//        settingsjDialog.setVisible(true);
//    }
    /**
     * Tests if the a string points to a file that is in the favourites directory.
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
        return getFavouritesDir().equals(testFile);
    }

    public URI getOriginatingUri(URI locationInCacheURI) {
        URI returnUri = null;
        String uriPath = locationInCacheURI.getPath();
//        System.out.println("pathIsInsideCache" + storageDirectory + " : " + fullTestFile);
        System.out.println("uriPath: " + uriPath);
        int foundPos = uriPath.indexOf("imdicache");
        if (foundPos == -1) {
            return null;
        }
        uriPath = uriPath.substring(foundPos);
        String[] uriParts = uriPath.split("/", 4);
        try {
            if (uriParts[1].toLowerCase().equals("http")) {
                returnUri = new URI(uriParts[1], uriParts[2], "/" + uriParts[3], null); // [0] will be "imdicache"
                System.out.println("returnUri: " + returnUri);
            }
        } catch (URISyntaxException urise) {
            GuiHelper.linorgBugCatcher.logError(urise);
        }
        return returnUri;
    }

    /**
     * Tests if the a string points to a flie that is in the cache directory.
     * @return Boolean
     */
    public boolean pathIsInsideCache(File fullTestFile) {
//        System.out.println("pathIsInsideCache" + storageDirectory + " : " + fullTestFile);    
        int foundPos = fullTestFile.getPath().indexOf("imdicache");
        if (foundPos == -1) {
            return false;
        }
        File testFile = new File(fullTestFile.getPath().substring(0, foundPos));
//                split("imdicache")[0]); // there is an issue using split because it parses the input string as a regex
        File storageFile = new File(storageDirectory);
//        System.out.println("fileIsInsideCache" + storageFile + " : " + testFile);
        return storageFile.equals(testFile);
    }

    /**
     * Checks for the existance of the favourites directory exists and creates it if it does not.
     * @return File pointing to the favourites directory
     */
    public File getFavouritesDir() {
        String favDirectory = storageDirectory + "favourites" + File.separatorChar; // storageDirectory already has the file separator appended
        File destinationFile = new File(favDirectory);
        boolean favDirExists = destinationFile.exists();
        if (!favDirExists) {
            favDirExists = destinationFile.mkdir();
        }
        return destinationFile;
    }

    /**
     * Tests that the cache directory exists and creates it if it does not.
     * @return Boolean
     */
    public boolean cacheDirExists() {
        cacheDirectory = storageDirectory + "imdicache" + File.separatorChar; // storageDirectory already has the file separator appended
        File destinationFile = new File(cacheDirectory);
        boolean cacheDirExists = destinationFile.exists();
        if (!cacheDirExists) {
            cacheDirExists = destinationFile.mkdir();
        }
        return cacheDirExists;
    }

    /**
     * Serialises the passed object to a file in the linorg storage directory so that it can be retrieved on application restart.
     * @param object The object to be serialised
     * @param filename The name of the file the object is to be serialised into
     * @throws java.io.IOException
     */
    public void saveObject(Serializable object, String filename) throws IOException {
        System.out.println("saveObject: " + filename);
        ObjectOutputStream objstream = new ObjectOutputStream(new FileOutputStream(storageDirectory + filename));
        objstream.writeObject(object);
        objstream.close();
    }

    /**
     * Deserialises the file from the linorg storage directory into an object. Use to recreate program state from last save.
     * @param filename The name of the file containing the serialised object
     * @return The deserialised object
     * @throws java.lang.Exception
     */
    public Object loadObject(String filename) throws Exception {
        System.out.println("loadObject: " + filename);
        Object object = null;
//        if (new File(storageDirectory + filename).exists()) { // this must be allowed to throw so don't do checks here
        ObjectInputStream objstream = new ObjectInputStream(new FileInputStream(storageDirectory + filename));
        object = objstream.readObject();
        objstream.close();
        if (object == null) {
            throw (new Exception("Loaded object is null"));
        }
//        }
        return object;
    }

    public boolean loadBoolean(String filename, boolean defaultValue) {
        boolean resultValue = false;
        try {
            resultValue = (Boolean) loadObject(filename);
        } catch (Exception ex) {
            System.out.println("load " + filename + " failed: " + ex.getMessage());
            resultValue = defaultValue;
        }
        return resultValue;
    }

    /**
     * Fetch the file from the remote URL and save into the cache.
     * Currently this does not expire the objects in the cache, however that will be required in the future.
     * @param pathString Path of the remote file.
     * @param expireCacheDays Number of days old that a file can be before it is replaced.
     * @return The path of the file in the cache.
     */
    public File updateCache(String pathString, int expireCacheDays) { // update if older than the date - x
        File targetFile = getSaveLocation(pathString);
        boolean fileNeedsUpdate = !targetFile.exists();
        if (!fileNeedsUpdate) {
            Date lastModified = new Date(targetFile.lastModified());
            Date expireDate = new Date(System.currentTimeMillis());
            System.out.println("updateCache: " + expireDate + " : " + lastModified + " : " + targetFile.getAbsolutePath());

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(expireDate);
            calendar.add(Calendar.DATE, -expireCacheDays);
            expireDate.setTime(calendar.getTime().getTime());

            System.out.println("updateCache: " + expireDate + " : " + lastModified + " : " + targetFile.getAbsolutePath());

            fileNeedsUpdate = expireDate.after(lastModified);
            System.out.println("fileNeedsUpdate: " + fileNeedsUpdate);
        }
        System.out.println("fileNeedsUpdate: " + fileNeedsUpdate);
        return updateCache(pathString, fileNeedsUpdate, new DownloadAbortFlag());
    }

    /**
     * Fetch the file from the remote URL and save into the cache.
     * Currently this does not expire the objects in the cache, however that will be required in the future.
     * @param pathString Path of the remote file.
     * @return The path of the file in the cache.
     */
    public File updateCache(String pathString, boolean expireCacheCopy, DownloadAbortFlag abortFlag) {
        //TODO: There will need to be a way to expire the files in the cache.
        File cachePath = getSaveLocation(pathString);
        try {
            saveRemoteResource(new URL(pathString), cachePath, null, expireCacheCopy, abortFlag);
        } catch (MalformedURLException mul) {
            GuiHelper.linorgBugCatcher.logError(mul);
        }
        return cachePath;
    }

    /**
     * Removes the cache path component from a path string and appends it to the destination directory.
     * Then tests for and creates the directory structure in the destination directory if requred.
     * @param pathString Path of a file within the cache.
     * @param destinationDirectory Path of the destination directory.
     * @return The path of the file in the destination directory.
     */
    public File getExportPath(String pathString, String destinationDirectory) {
        String cachePath = destinationDirectory + /*File.separatorChar +*/ pathString.substring(pathString.indexOf("imdicache") + 9); // this path must be inside the cache for this to work correctly
        File returnFile = new File(cachePath);
        if (!returnFile.getParentFile().exists()) {
            returnFile.getParentFile().mkdirs();
        }
        return returnFile;
    }

    public URI getNewImdiFileName(File parentDirectory) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        int fileCounter = 0;
        File returnFile = new File(parentDirectory, formatter.format(new Date()) + ".imdi");
        while (returnFile.exists()) {
            returnFile = new File(parentDirectory, formatter.format(new Date()) + (fileCounter++) + ".imdi");
        }
        return returnFile.toURI();
    }

    /**
     * Converts a String path from the remote location to the respective location in the cache.
     * Then tests for and creates the directory structure in the cache if requred.
     * @param pathString Path of the remote file.
     * @return The path in the cache for the file.
     */
    public File getSaveLocation(String pathString) {
        String searchString = ".linorg/imdicache";
        if (cacheDirectory.endsWith(".arbil/imdicache/")){
            searchString = ".arbil/imdicache";
        }
        try {
            pathString = URLDecoder.decode(pathString, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            GuiHelper.linorgBugCatcher.logError(uee);
        }
        pathString = pathString.replace("//", "/");
        if (pathString.indexOf(searchString) > -1) {
            GuiHelper.linorgBugCatcher.logError(new Exception("Recursive path error (about to be corrected) in: " + pathString));
            pathString = pathString.substring(pathString.lastIndexOf(searchString) + searchString.length());
        }
        String cachePath = cacheDirectory + pathString.replace(":/", "/").replace("//", "/");
        File returnFile = new File(cachePath);
        if (!returnFile.getParentFile().exists()) {
            returnFile.getParentFile().mkdirs();
        }
        return returnFile;
    }

    /**
     * Copies a remote file over http and saves it into the cache.
     * @param targetUrlString The URL of the remote file as a string
     * @param destinationPath The local path where the file should be saved
     */
    public void saveRemoteResource(URL targetUrl, File destinationFile, ShibbolethNegotiator shibbolethNegotiator, boolean expireCacheCopy, DownloadAbortFlag abortFlag) {
//        String targetUrlString = getFullResourceURI();
//        String destinationPath = GuiHelper.linorgSessionStorage.getSaveLocation(targetUrlString);
//        System.out.println("saveRemoteResource: " + targetUrlString);
//        System.out.println("destinationPath: " + destinationPath);
//        File destinationFile = new File(destinationPath);
        if (destinationFile.length() == 0) {
            // if the file is zero length then is presumably should either be replaced or the version in the jar used.
            destinationFile.delete();
        }
        if (destinationFile.exists() && !expireCacheCopy && destinationFile.length() > 0) {
            System.out.println("this resource is already in the cache");
        } else {
            try {
                URLConnection urlConnection = targetUrl.openConnection();
                HttpURLConnection httpConnection = null;
                if (urlConnection instanceof HttpURLConnection) {
                    httpConnection = (HttpURLConnection) urlConnection;
                    if (shibbolethNegotiator != null) {
                        httpConnection = shibbolethNegotiator.getShibbolethConnection((HttpURLConnection) urlConnection);
                    }
                    //h.setFollowRedirects(false);
                    System.out.println("Code: " + httpConnection.getResponseCode() + ", Message: " + httpConnection.getResponseMessage());
                }
                if (httpConnection != null && httpConnection.getResponseCode() != 200) {
                    System.out.println("non 200 response, skipping file");
                } else {
                    File tempFile = File.createTempFile(destinationFile.getName(), "tmp", destinationFile.getParentFile());
                    tempFile.deleteOnExit();
                    int bufferLength = 1024 * 3;
                    FileOutputStream outFile = new FileOutputStream(tempFile); //targetUrlString
                    System.out.println("getting file");
                    InputStream stream = urlConnection.getInputStream();
                    byte[] buffer = new byte[bufferLength]; // make htis 1024*4 or something and read chunks not the whole file
                    int bytesread = 0;
                    int totalRead = 0;
                    while (bytesread >= 0 && !abortFlag.abortDownload) {
                        bytesread = stream.read(buffer);
                        totalRead += bytesread;
//                        System.out.println("bytesread: " + bytesread);
//                        System.out.println("Mbs totalRead: " + totalRead / 1048576);
                        if (bytesread == -1) {
                            break;
                        }
                        outFile.write(buffer, 0, bytesread);
                    }
                    outFile.close();
                    if (tempFile.length() > 0 && !abortFlag.abortDownload) {
                        if (destinationFile.exists()) {
                            destinationFile.delete();
                        }
                        tempFile.renameTo(destinationFile);
                    }
                    System.out.println("Downloaded: " + totalRead / 1048576 + " Mb");
                }
            } catch (Exception ex) {
                GuiHelper.linorgBugCatcher.logError(ex);
//                System.out.println(ex.getMessage());
            }
        }
    }
}
