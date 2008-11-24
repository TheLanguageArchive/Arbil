/*
 * LinorgSessionStorage 
 * use to save and load objects from disk
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
                System.out.println("failed to create: " + storageDirectory);
            }
        }
        System.out.println("storageDirectory: " + storageDirectory);
        System.out.println("cacheDirExists: " + cacheDirExists());
//        ObjectToSerialize o = new ObjectToSerialize("Object", 42);
//        System.out.println(o);
//        try {
//            Hashtable hashtable = new Hashtable();
//            hashtable.put("first key", "first value");
//            hashtable.put("second key", "second value");
//            hashtable.put("third key", "third value");
//            
//            saveObject(hashtable, "hashtable.ser");
//            saveObject(o, "object.ser");
//            ObjectToSerialize object_loaded = (ObjectToSerialize) loadObject("object.ser");
//            System.out.println(object_loaded);
//        } catch (Exception e) {
//        }
    }

    public boolean cacheDirExists() {
        destinationDirectory = storageDirectory + "imdicache" + File.separatorChar; // storageDirectory already has the file separator appended
        File destinationFile = new File(destinationDirectory);
        boolean cacheDirExists = destinationFile.exists();
        if (!cacheDirExists) {
            cacheDirExists = destinationFile.mkdir();
        }
        return cacheDirExists;
    }

    public void saveObject(Serializable object, String filename) throws IOException {
        System.out.println("saveObject: " + filename);
        ObjectOutputStream objstream = new ObjectOutputStream(new FileOutputStream(storageDirectory + filename));
        objstream.writeObject(object);
        objstream.close();
    }

    public Object loadObject(String filename) throws Exception {
        System.out.println("loadObject: " + filename);
        ObjectInputStream objstream = new ObjectInputStream(new FileInputStream(storageDirectory + filename));
        Object object = objstream.readObject();
        objstream.close();
        return object;
    }

    public String updateCache(String pathString) {
        //TODO: There will need to be a way to expire the files in the cache.
        String cachePath = getSaveLocation(pathString);
        if (!new File(cachePath).exists()) {
            GuiHelper.linorgSessionStorage.saveRemoteResource(pathString, cachePath);
        }
        return cachePath;
    }
    // converts a String path to the cache path
    public String getSaveLocation(String pathString) {
        String cachePath = GuiHelper.linorgSessionStorage.destinationDirectory + pathString.replace("://", "/");
        File tempFile = new File(cachePath);
        if (!tempFile.getParentFile().exists()) {
            tempFile.getParentFile().mkdirs();
        }
        return cachePath;
    }

    public void saveRemoteResource(String targetUrlString, String destinationPath) {
//        String targetUrlString = getFullResourcePath();
//        String destinationPath = GuiHelper.linorgSessionStorage.getSaveLocation(targetUrlString);
        System.out.println("saveRemoteResource: " + targetUrlString);
        File tempFile = new File(destinationPath);
        if (tempFile.exists()) {
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
                System.out.println(ex.getMessage());
            }
        }
    }//    class ObjectToSerialize implements Serializable {
//
//        static private final long serialVersionUID = 42L;
//
//        public ObjectToSerialize(String firstAttribute, int secondAttribute) {
//            this.firstAttribute = firstAttribute;
//            this.secondAttribute = secondAttribute;
//        }
//
//        @Override
//        public String toString() {
//            return firstAttribute + ", " + secondAttribute;
//        }
//        private String firstAttribute;
//        private int secondAttribute;
//    }
}
