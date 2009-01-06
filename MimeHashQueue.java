/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author petwit
 */
public class MimeHashQueue {

    private Hashtable imdiObjectQueue = new Hashtable();
    private Hashtable processedImdiObjects = new Hashtable();
    private Hashtable knownMimeTypes;
    private Hashtable md5SumToDuplicates;
    private Hashtable pathToMd5Sums;
    private boolean continueThread = false;
    private static mpi.bcarchive.typecheck.FileType fileType = new mpi.bcarchive.typecheck.FileType(); //  used to check the file type
    private static mpi.bcarchive.typecheck.DeepFileType deepFileType = new mpi.bcarchive.typecheck.DeepFileType();

    public MimeHashQueue() {
        System.out.println("MimeHashQueue init");
        continueThread = true;
        new Thread() {

            public void run() {
                // load from disk
                loadMd5sumIndex();
                while (continueThread) {
                    try {
                        Thread.currentThread().sleep(500);//sleep for 100 ms
                    } catch (InterruptedException ie) {
                        System.err.println("run MimeHashQueue: " + ie.getMessage());
                    }
                    for (Enumeration nodesToCheck = imdiObjectQueue.keys(); nodesToCheck.hasMoreElements();) {
                        String currentNodeURL = nodesToCheck.nextElement().toString();
                        System.out.println("run MimeHashQueue processing: " + currentNodeURL);
                        ImdiTreeObject currentImdiObject = ((ImdiTreeObject) imdiObjectQueue.get(currentNodeURL));
                        // check that the file has not been done already
                        // TODO: chang this to use an additional hastable of mtimes for each file and if the mtime does not match then rescan the file
                        if (!knownMimeTypes.containsKey(currentNodeURL)) {
                            // this couldbe optimised by not mime checking imdi files, but for easy reading it is done
                            if (/*currentNodeURL.endsWith(".imdi") ||*/getMimeType(currentNodeURL)) {
                                getHash(currentNodeURL);
                            }
                            currentImdiObject.clearIcon();
                        }
                        processedImdiObjects.put(currentNodeURL, currentImdiObject);
                        imdiObjectQueue.remove(currentNodeURL);
                    }
                // TODO: add check for url in list with different hash which would indicate a modified file and require a red x on the icon
                // TODO: add check for mtime change and update accordingly
                }
            }
        }.start();
    }

    @Override
    protected void finalize() throws Throwable {
        // stop the thread
        continueThread = false;
//        // save to disk
//        saveMd5sumIndex(); // this is called by guihelper
        super.finalize();
    }

    private void loadMd5sumIndex() {
        try {
            knownMimeTypes = (Hashtable) GuiHelper.linorgSessionStorage.loadObject("knownMimeTypes");
            md5SumToDuplicates = (Hashtable) GuiHelper.linorgSessionStorage.loadObject("knownMd5Sums");
            pathToMd5Sums = (Hashtable) GuiHelper.linorgSessionStorage.loadObject("pathToMd5Sums");
            System.out.println("loaded md5 and mime from disk");
        } catch (Exception ex) {
            knownMimeTypes = new Hashtable();
            md5SumToDuplicates = new Hashtable();
            pathToMd5Sums = new Hashtable();
            System.out.println("loadMap exception: " + ex.getMessage());
        }
    }

    public void saveMd5sumIndex() {
        // this is called by guihelper
        try {
            GuiHelper.linorgSessionStorage.saveObject(knownMimeTypes, "knownMimeTypes");
            GuiHelper.linorgSessionStorage.saveObject(md5SumToDuplicates, "knownMd5Sums");
            GuiHelper.linorgSessionStorage.saveObject(pathToMd5Sums, "pathToMd5Sums");
            System.out.println("saveMd5sumIndex");
        } catch (IOException ex) {
            System.out.println("saveMap exception: " + ex.getMessage());
        }
    }

    public boolean getMimeType(String filePath) {
        System.out.println("getMimeType: " + filePath);
        String mpiMimeType;
        // here we also want to check the magic number but the mpi api has a function similar to that so we
        // use the mpi.api to get the mime type of the file, if the mime type is not a valid archive format the api will return null
        // because the api uses null to indicate non archivable we cant return other strings
        mpiMimeType = null;//"unreadable";
        boolean deep = true;

        URL url = null;
        try {
            // Create a file object
            File file = new File(filePath.replace("file://", ""));
            // convert to a url object
            url = file.toURL();
        } catch (MalformedURLException e) {
            System.out.println(e.getMessage());
        }
        if (url == null) {
            System.out.println("Invalid URL: " + filePath);
        //System.exit(1);
        } else {
            try {
                // this will choke on strings that look url encoded but are not. because it erroneously decodes them
                InputStream inputStream = url.openStream();
                if (inputStream != null) {
                    if (deep) {
                        mpiMimeType = deepFileType.checkStream(inputStream, filePath);
                    } else {
                        mpiMimeType = fileType.checkStream(inputStream, filePath);
                    }
                }
                mpiMimeType = mpi.bcarchive.typecheck.FileType.resultToMPIType(mpiMimeType);
            } catch (Exception ioe) {
                System.out.println("Cannot read file at URL: " + url + " ioe: " + ioe.getMessage());
            }
            System.out.println(mpiMimeType);
        }
        // if non null then it is an archivable file type
        if (mpiMimeType != null) {
            knownMimeTypes.put(filePath, mpiMimeType);
        } else {
            // because the api uses null to indicate non archivable we cant return other strings
            //knownMimeTypes.put(filePath, "nonarchivable");
        }
        return (mpiMimeType != null);
    }

    public void getHash(String filePath) {
        System.out.println("getHash: " + filePath);
        File targetFile = new File(filePath.replace("file://", ""));
        String hashString = null;
        // TODO: add hashes for session links 
        // TODO: organise a way to get the md5 sum of files on the server
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            StringBuffer hexString = new StringBuffer();
            FileInputStream is = new FileInputStream(targetFile);
            byte[] buff = new byte[1024];
            byte[] md5sum;
            int i = 0;
            while ((i = is.read(buff)) > 0) {
                digest.update(buff, 0, i);
            }
            md5sum = digest.digest();
            for (i = 0; i < md5sum.length; ++i) {
                hexString.append(Integer.toHexString(0x0100 + (md5sum[i] & 0x00FF)).substring(1));
            }
            hashString = hexString.toString();
//                    debugOut("file: " + this.getFile().getAbsolutePath());
//                    debugOut("location: " + getUrl());
//                    debugOut("digest: " + digest.toString());                    
        } catch (Exception ex) {
            System.out.println("failed to created hash: " + ex.getMessage());
        }
        // store the url to node mapping. Note that; in the case of a resource line the session node is mapped against the resource url not the imdichildnode for the file
//                urlToNodeHashtable.put(nodeLocation, this);

        if (hashString != null) {
            pathToMd5Sums.put(filePath, hashString);
            Object matchingNodes = md5SumToDuplicates.get(hashString);
            if (matchingNodes != null) {
//                        debugOut("checking vector for: " + hashString);
                if (!((Vector) matchingNodes).contains(filePath)) {
//                            debugOut("adding to vector: " + hashString);
                    Enumeration otherNodesEnum = ((Vector) matchingNodes).elements();
                    while (otherNodesEnum.hasMoreElements()) {
                        Object currentElement = otherNodesEnum.nextElement();
                        Object currentNode = processedImdiObjects.get(currentElement);
                        if (currentNode instanceof ImdiTreeObject) {
                            //debugOut("updating icon for: " + ((ImdiTreeObject) currentNode).getUrl());
                            // clear the icon of the other copies so that they will be updated to indicate the commonality
                            System.out.println("Clearing icon for other node: " + currentNode.toString());
                            ((ImdiTreeObject) currentNode).clearIcon();
                        }
                    }
                    ((Vector) matchingNodes).add(filePath);
                }
            } else {
                System.out.println("creating new vector for: " + hashString);
                Vector nodeVector = new Vector();
                nodeVector.add(filePath);
                md5SumToDuplicates.put(hashString, nodeVector);
            }
        }
//            }
//            debugOut("hashString: " + hashString);
//            return hashString;
    }

    private String getFilePath(ImdiTreeObject imdiObject) {
        if (imdiObject.hasResource()) {
            return imdiObject.getFullResourcePath();
        } else {
            return imdiObject.getUrlString();
        }
    }

    public void addToQueue(ImdiTreeObject imdiObject) {
        if (!imdiObject.isDirectory() && imdiObject.isLocal() && (!imdiObject.isImdiChild() || imdiObject.hasResource())) {
            System.out.println("addToQueue: " + getFilePath(imdiObject));
            // here also check that the destination file exists and is readable
            imdiObjectQueue.put(getFilePath(imdiObject), imdiObject);
        }
    }

    public String getMimeResult(ImdiTreeObject imdiObject) {
        Object returnObject = knownMimeTypes.get(getFilePath(imdiObject));
        if (returnObject != null) {
            return returnObject.toString();
        } else {
            return null;
        }
    }

    public String getHashResult(ImdiTreeObject imdiObject) {
        Object returnObject = null;
        if (pathToMd5Sums != null) {
            returnObject = pathToMd5Sums.get(getFilePath(imdiObject));
        }
        if (returnObject != null) {
            return returnObject.toString();
        } else {
            return null;
        }
    }

    public Enumeration getDuplicateList(String hashString) {
        Object matchingNodes = md5SumToDuplicates.get(hashString);
        return ((Vector) matchingNodes).elements();
    }
}
