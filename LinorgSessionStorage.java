/*
 * LinorgSessionStorage 
 * use to save and load objects from disk and to manage items in the local cache
 */
package mpi.linorg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author petwit
 */
public class LinorgSessionStorage {

    public String storageDirectory = "";
    public String destinationDirectory;

    public LinorgSessionStorage() {
        storageDirectory = System.getProperty("user.home") + File.separatorChar + ".linorg" + File.separatorChar;

        File storageFile = new File(storageDirectory);
        if (!storageFile.exists()) {
            storageFile.mkdir();
            if (!storageFile.exists()) {
                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Could not create working files in\n" + storageDirectory + "\nplease check user permissions and disk space");
                System.out.println("failed to create: " + storageDirectory);
            }
        }
        System.out.println("storageDirectory: " + storageDirectory);
        System.out.println("cacheDirExists: " + cacheDirExists());
    }

    /**
     * Tests if the a string points to a flie that is in the cache directory.
     * @return Boolean
     */
    public boolean pathIsInsideCache(File fullTestFile) {
//        System.out.println("pathIsInsideCache" + storageDirectory + " : " + fullTestFile);    
        File testFile = new File(fullTestFile.getPath().split("imdicache")[0]);
        File storageFile = new File(storageDirectory);
//        System.out.println("fileIsInsideCache" + storageFile + " : " + testFile);
        return storageFile.equals(testFile);
    }

    /**
     * Tests that the cache directory exists and creates it if it does not.
     * @return Boolean
     */
    public boolean cacheDirExists() {
        destinationDirectory = storageDirectory + "imdicache" + File.separatorChar; // storageDirectory already has the file separator appended
        File destinationFile = new File(destinationDirectory);
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
            resultValue = (Boolean) GuiHelper.linorgSessionStorage.loadObject(filename);
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
    public String updateCache(String pathString, int expireCacheDays) { // update if older than the date - x
        String cachePath = getSaveLocation(pathString);
        File targetFile = new File(cachePath);
        boolean fileNeedsUpdate = !targetFile.exists();
        if (!fileNeedsUpdate) {
            Date lastModified = new Date(targetFile.lastModified());
            Date expireDate = new Date(System.currentTimeMillis());
            System.out.println("updateCache: " + expireDate + " : " + lastModified + " : " + cachePath);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(expireDate);
            calendar.add(Calendar.DATE, -expireCacheDays);
            expireDate.setTime(calendar.getTime().getTime());

            System.out.println("updateCache: " + expireDate + " : " + lastModified + " : " + cachePath);

            fileNeedsUpdate = expireDate.after(lastModified);
            System.out.println("fileNeedsUpdate: " + fileNeedsUpdate);
        }
        System.out.println("fileNeedsUpdate: " + fileNeedsUpdate);
        return updateCache(pathString, fileNeedsUpdate);
    }

    /**
     * Fetch the file from the remote URL and save into the cache.
     * Currently this does not expire the objects in the cache, however that will be required in the future.
     * @param pathString Path of the remote file.
     * @return The path of the file in the cache.
     */
    public String updateCache(String pathString, boolean expireCacheCopy) {
        //TODO: There will need to be a way to expire the files in the cache.
        String cachePath = getSaveLocation(pathString);
        if (!new File(cachePath).exists() || expireCacheCopy) {
            GuiHelper.linorgSessionStorage.saveRemoteResource(pathString, cachePath, expireCacheCopy);
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
    public String getExportPath(String pathString, String destinationDirectory) {
        String cachePath = destinationDirectory + pathString.split(".linorg" + File.separatorChar + "imdicache")[1];
        File tempFile = new File(cachePath);
        if (!tempFile.getParentFile().exists()) {
            tempFile.getParentFile().mkdirs();
        }
        return cachePath;
    }

    /**
     * Converts a String path from the remote location to the respective location in the cache.
     * Then tests for and creates the directory structure in the cache if requred.
     * @param pathString Path of the remote file.
     * @return The path in the cache for the file.
     */
    public String getSaveLocation(String pathString) {
        String cachePath = GuiHelper.linorgSessionStorage.destinationDirectory + pathString.replace("://", "/");
        File tempFile = new File(cachePath);
        if (!tempFile.getParentFile().exists()) {
            tempFile.getParentFile().mkdirs();
        }
        return cachePath;
    }

    /**
     * Copies a remote file over http and saves it into the cache.
     * @param targetUrlString The URL of the remote file as a string
     * @param destinationPath The local path where the file should be saved
     */
    public void saveRemoteResource(String targetUrlString, String destinationPath, boolean expireCacheCopy) {
//        String targetUrlString = getFullResourcePath();
//        String destinationPath = GuiHelper.linorgSessionStorage.getSaveLocation(targetUrlString);
        System.out.println("saveRemoteResource: " + targetUrlString);
        System.out.println("destinationPath: " + destinationPath);
        File tempFile = new File(destinationPath);
        if (tempFile.exists() && !expireCacheCopy) {
            System.out.println("this resource is already in the cache");
        } else {
            try {
                URL u = new URL(targetUrlString);
                URLConnection yc = u.openConnection();
                HttpURLConnection h = (HttpURLConnection) yc;
                //h.setFollowRedirects(false);

                System.out.println("Code: " + h.getResponseCode() + ", Message: " + h.getResponseMessage());
                if (h.getResponseCode() != 200) {
                    System.out.println("non 200 response, skipping file");
                } else {
                    int bufferLength = 1024 * 4;
                    FileOutputStream fout = new FileOutputStream(destinationPath); //targetUrlString
                    System.out.println("getting file");
                    InputStream stream = yc.getInputStream();
                    byte[] buffer = new byte[bufferLength]; // make htis 1024*4 or something and read chunks not the whole file
                    int bytesread = 0;
                    int totalRead = 0;
                    while (bytesread >= 0) {
                        bytesread = stream.read(buffer);
                        totalRead += bytesread;
//                        System.out.println("bytesread: " + bytesread);
//                        System.out.println("Mbs totalRead: " + totalRead / 1048576);
                        if (bytesread == -1) {
                            break;
                        }
                        fout.write(buffer, 0, bytesread);
                    }
                    System.out.println("Downloaded: " + totalRead / 1048576 + " Mbs");
                }
            } catch (Exception ex) {
                GuiHelper.linorgBugCatcher.logError(ex);
//                System.out.println(ex.getMessage());
            }
        }
    }
}
