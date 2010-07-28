package nl.mpi.arbil.data;

import java.net.URI;
import nl.mpi.arbil.*;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Document   : ImdiLoader
 * Created on : Dec 30, 2008, 3:04:39 PM
 * @author Peter.Withers@mpi.nl 
 */
public class ImdiLoader {

    private boolean continueThread = true;
    public boolean schemaCheckLocalFiles = false;
    private Vector<ImdiTreeObject> imdiRemoteNodesToInit = new Vector<ImdiTreeObject>();
    private Vector<ImdiTreeObject> imdiLocalNodesToInit = new Vector<ImdiTreeObject>();
    private Hashtable<String, ImdiTreeObject> imdiHashTable = new Hashtable<String, ImdiTreeObject>();
    private Vector<ImdiTreeObject> nodesNeedingSave = new Vector<ImdiTreeObject>();
    int imdiFilesLoaded = 0;
    int remoteImdiFilesLoaded = 0;
    static private ImdiLoader singleInstance = null;

    static synchronized public ImdiLoader getSingleInstance() {
//        System.out.println("ImdiLoader getSingleInstance");
        if (singleInstance == null) {
            singleInstance = new ImdiLoader();
        }
        return singleInstance;
    }

    private ImdiLoader() {
        System.out.println("ImdiLoader init");
        schemaCheckLocalFiles = LinorgSessionStorage.getSingleInstance().loadBoolean("schemaCheckLocalFiles", schemaCheckLocalFiles);
        continueThread = true;
        // start three remote imdi loader threads
        for (int threadCounter = 0; threadCounter < 6; threadCounter++) {
            new Thread("ImdiLoader-remote-" + threadCounter) {

                @Override
                public void run() {
                    setPriority(Thread.NORM_PRIORITY - 1);
                    while (continueThread) {
                        try {
                            sleep(500);
                        } catch (InterruptedException ie) {
                            GuiHelper.linorgBugCatcher.logError(ie);
                        }
                        ImdiTreeObject currentImdiObject = getNodeFromQueue(imdiRemoteNodesToInit);
                        while (currentImdiObject != null) {
                            // this has been separated in to two separate threads to prevent long delays when there is no server connection
                            // each node is loaded one at a time and must time out before the next is started
                            // the local corpus nodes are the fastest so they are now loaded in a separate thread
                            // alternatively a thread pool may be an option
                            if (currentImdiObject != null) {
                                System.out.println("run RemoteImdiLoader processing: " + currentImdiObject.getUrlString());
                                currentImdiObject.loadImdiDom();
                                currentImdiObject.updateLoadingState(-1);
                                currentImdiObject.clearIcon();
                                currentImdiObject.clearChildIcons();
                                remoteImdiFilesLoaded++;
                                currentImdiObject.notifyLoaded();
                            }
                            currentImdiObject.lockedByLoadingThread = false;
                            currentImdiObject = getNodeFromQueue(imdiRemoteNodesToInit);
                        }
                    }
                }
            }.start();
        }
        // due to an apparent deadlock in the imdi api only one thread is used for local files. the deadlock appears to be in the look up host area
        // start the local imdi threads
        for (int threadCounter = 0; threadCounter < 6; threadCounter++) {
            new Thread("ImdiLoader-local-" + threadCounter) {

                @Override
                public void run() {
                    setPriority(Thread.NORM_PRIORITY - 1);
                    while (continueThread) {
//                        try {
                        try {
                            sleep(100);
                        } catch (InterruptedException ie) {
                            GuiHelper.linorgBugCatcher.logError(ie);
                        }
                        ImdiTreeObject currentImdiObject = getNodeFromQueue(imdiLocalNodesToInit);
                        while (currentImdiObject != null) {
                            System.out.println("run LocalImdiLoader processing: " + currentImdiObject.getUrlString());
                            if (currentImdiObject.getNeedsSaveToDisk()) {
                                currentImdiObject.saveChangesToCache(false);
                            }
                            currentImdiObject.loadImdiDom();
                            if (schemaCheckLocalFiles) {
                                if (currentImdiObject.isMetaDataNode()) {
                                    XsdChecker xsdChecker = new XsdChecker();
                                    String checkerResult;
                                    checkerResult = xsdChecker.simpleCheck(currentImdiObject.getFile(), currentImdiObject.getURI());
                                    currentImdiObject.hasSchemaError = (checkerResult != null);
                                }
                            } else {
                                currentImdiObject.hasSchemaError = false;
                            }
                            currentImdiObject.updateLoadingState(-1);
                            currentImdiObject.clearIcon();
                            currentImdiObject.clearChildIcons();
                            imdiFilesLoaded++;
                            System.out.println("remoteImdiFilesLoaded: " + remoteImdiFilesLoaded + " imdiFilesLoaded: " + imdiFilesLoaded);
                            currentImdiObject.lockedByLoadingThread = false;
                            currentImdiObject.notifyLoaded();
                            currentImdiObject = getNodeFromQueue(imdiLocalNodesToInit);
                        }
//                        } catch (Exception ie) {
//                            // anything that throws in this loop will prevent any further loading of local imdi files
//                            GuiHelper.linorgBugCatcher.logError(ie);
//                        }
                    }
                }
            }.start();
        }
    }

    synchronized private void addNodeToQueue(ImdiTreeObject nodeToAdd) {
        if (ImdiTreeObject.isStringLocal(nodeToAdd.getUrlString())) {
            if (!imdiLocalNodesToInit.contains(nodeToAdd)) {
                imdiLocalNodesToInit.addElement(nodeToAdd);
            }
        } else {
            if (!imdiRemoteNodesToInit.contains(nodeToAdd)) {
                imdiRemoteNodesToInit.addElement(nodeToAdd);
            }
        }
    }

    synchronized private ImdiTreeObject getNodeFromQueue(Vector<ImdiTreeObject> imdiNodesQueue) {
        // TODO: put this size test and remove into a syncronised function
        if (imdiNodesQueue.size() > 0) {
            ImdiTreeObject tempImdiObject = imdiNodesQueue.remove(0);
            if (tempImdiObject.lockedByLoadingThread) {
                imdiNodesQueue.add(tempImdiObject);
                return null;
            } else {
                tempImdiObject.lockedByLoadingThread = true;
                return tempImdiObject;
            }
        } else {
            return null;
        }
    }

//    public ImdiTreeObject isImdiObjectLoaded(String localUrlString) {
//        localUrlString = ImdiTreeObject.conformStringToUrl(localUrlString).toString();
//        return imdiHashTable.get(localUrlString);
//    }
    public ImdiTreeObject getImdiObjectWithoutLoading(URI localUri) {
        ImdiTreeObject currentImdiObject = null;
        if (localUri != null) {
            localUri = ImdiTreeObject.normaliseURI(localUri);
            // correct any variations in the url string
//            localUri = ImdiTreeObject.conformStringToUrl(localUri).toString();
            currentImdiObject = imdiHashTable.get(localUri.toString());
            if (currentImdiObject == null) {
//                System.out.println("ImdiObject not in list so requesting: " + localNodeText + " : " + localUrlString);
                currentImdiObject = new ImdiTreeObject(localUri);
                imdiHashTable.put(localUri.toString(), currentImdiObject);
            }
        }
        return currentImdiObject;
    }

    public ImdiTreeObject getImdiObject(Object registeringObject, URI localUri) {// throws Exception {
//        if (localNodeText == null && localUrlString.contains("WrittenResource")) {
//            System.out.println("getImdiObject: " + localNodeText + " : " + localUrlString);
//        }
//        if (registeringObject == null) {
//            throw (new Exception("no container object provided"));
//        }
//       todo if (localUrlString == null) {
//            System.out.println("getImdiObject: " + localNodeText + " : " + localUrlString);
//       end todo }
//        System.out.println("getImdiObject: " + localNodeText + " : " + localUrlString);
        ImdiTreeObject currentImdiObject = null;
        if (localUri != null && localUri.toString().length() > 0) {
            currentImdiObject = getImdiObjectWithoutLoading(localUri);
//            System.out.println(currentImdiObject.isImdiChild() + ", " + currentImdiObject.getParentDomNode().imdiDataLoaded + ", " + currentImdiObject.isLoading());
            if (!currentImdiObject.getParentDomNode().imdiDataLoaded && !currentImdiObject.isLoading()) {
//                System.out.println("created new ImdiObject: " + currentImdiObject.getUrlString());
                if (ImdiTreeObject.isStringImdiChild(currentImdiObject.getUrlString())) {
//                    System.out.println("cause the parent node to be loaded");
                    // cause the parent node to be loaded
                    currentImdiObject.getParentDomNode();
                } else if (ImdiTreeObject.isPathMetadata(currentImdiObject.getUrlString()) || ImdiTreeObject.isPathHistoryFile(currentImdiObject.getUrlString())) {
                    currentImdiObject.updateLoadingState(+1);
                    addNodeToQueue(currentImdiObject);
                    System.out.println("+imdiHashTable.size: " + imdiHashTable.size());
                } else if (!ImdiTreeObject.isPathMetadata(currentImdiObject.getUrlString())) {
//                    currentImdiObject.clearIcon(); // do not do this
                }
            }
            currentImdiObject.registerContainer(registeringObject);
        }
//        System.out.println("currentImdiObject: " + currentImdiObject);
        return currentImdiObject;
    }

    public void releaseImdiObject(URI localUri) {
//        imdiHashTable.remove(imdiUrlString); // TODO: implement this so that imdi files are not held in memory for ever
        localUri = ImdiTreeObject.normaliseURI(localUri);
        System.out.println("-imdiHashTable.size: " + imdiHashTable.size());
    }

    // return the node only if it has already been loaded otherwise return null
    public ImdiTreeObject getImdiObjectOnlyIfLoaded(URI imdiUri) {
//        String localUrlString = ImdiTreeObject.conformStringToUrl(imdiUrl).toString();
        imdiUri = ImdiTreeObject.normaliseURI(imdiUri);
        return imdiHashTable.get(imdiUri.toString());
    }

    // reload the node only if it has already been loaded otherwise ignore
    public void requestReloadOnlyIfLoaded(URI imdiUri) {
//        String localUrlString = ImdiTreeObject.conformStringToUrl(imdiUrl).toString();
        imdiUri = ImdiTreeObject.normaliseURI(imdiUri);
        ImdiTreeObject currentImdiObject = imdiHashTable.get(imdiUri.toString());
        if (currentImdiObject != null) {
            requestReload(currentImdiObject);
        }
    }

    // reload the node or if it is an imdichild node then reload its parent
    public void requestReload(ImdiTreeObject currentImdiObject) {
        if (currentImdiObject.isImdiChild()) {
            currentImdiObject = currentImdiObject.getParentDomNode();
        }
        removeNodesNeedingSave(currentImdiObject);
//        if (ImdiTreeObject.isStringImdi(currentImdiObject.getUrlString()) || ImdiTreeObject.isStringImdiHistoryFile(currentImdiObject.getUrlString())) {
        addNodeToQueue(currentImdiObject);
//        }
    }

    public void requestReloadAllNodes() {
        for (ImdiTreeObject currentImdiObject : imdiHashTable.values()) {
            requestReload(currentImdiObject);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        // stop the thread
        continueThread = false;
        super.finalize();
    }

    public void addNodeNeedingSave(ImdiTreeObject nodeToSave) {
        nodeToSave = nodeToSave.getParentDomNode();
        if (!nodesNeedingSave.contains(nodeToSave)) {
            System.out.println("addNodeNeedingSave: " + nodeToSave);
            nodesNeedingSave.add(nodeToSave);
        }
    }

    public void removeNodesNeedingSave(ImdiTreeObject savedNode) {
        System.out.println("removeNodesNeedingSave: " + savedNode);
        nodesNeedingSave.remove(savedNode);
    }

    public ImdiTreeObject[] getNodesNeedSave() {
        return nodesNeedingSave.toArray(new ImdiTreeObject[]{});
    }

    public boolean nodesNeedSave() {
        return nodesNeedingSave.size() > 0;
    }

    public synchronized void saveNodesNeedingSave(boolean updateIcons) {
        // this is syncronised to avoid issues from the key repeat on linux which fails to destinguish between key up events and key repeat events
        while (nodesNeedingSave.size() > 0) {
            // remove the node from the save list not in the save function because otherwise if the save fails the application will lock up
            ImdiTreeObject currentNode = nodesNeedingSave.remove(0);
            if (currentNode != null) {
                currentNode.saveChangesToCache(updateIcons); // saving removes the node from the nodesNeedingSave vector via removeNodesNeedingSave
                if (updateIcons) {
                    requestReload(currentNode);
                }
            }
        }
    }
}
