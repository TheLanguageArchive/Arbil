/*
 * LinorgSessionStorage 
 * use to save and load objects from disk
 */
package mpi.linorg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

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
    
//    class ObjectToSerialize implements Serializable {
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
