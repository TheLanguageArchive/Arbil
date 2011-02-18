package nl.mpi.arbil.data;

import nl.mpi.arbil.ui.XsdChecker;
import nl.mpi.arbil.ui.GuiHelper;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import java.net.URI;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Document   : ArbilDataNodeLoader formerly known as ImdiLoader
 * Created on : Dec 30, 2008, 3:04:39 PM
 * @author Peter.Withers@mpi.nl 
 */
public class ArbilDataNodeLoader {

    private boolean continueThread = true;
    public boolean schemaCheckLocalFiles = false;
    private Vector<ArbilDataNode> arbilRemoteNodesToInit = new Vector<ArbilDataNode>();
    private Vector<ArbilDataNode> arbilLocalNodesToInit = new Vector<ArbilDataNode>();
    private Hashtable<String, ArbilDataNode> arbilHashTable = new Hashtable<String, ArbilDataNode>();
    private Vector<ArbilDataNode> nodesNeedingSave = new Vector<ArbilDataNode>();
    private int arbilFilesLoaded = 0;
    private int remoteArbilFilesLoaded = 0;
    private int threadStartCounter = 0;
    private ThreadGroup remoteLoaderThreadGroup;
    private ThreadGroup localLoaderThreadGroup;
    static private ArbilDataNodeLoader singleInstance = null;

    static synchronized public ArbilDataNodeLoader getSingleInstance() {
//        System.out.println("ImdiLoader getSingleInstance");
        if (singleInstance == null) {
            singleInstance = new ArbilDataNodeLoader();
        }
        return singleInstance;
    }

    private ArbilDataNodeLoader() {
        System.out.println("ArbilDataNodeLoader init");
        schemaCheckLocalFiles = ArbilSessionStorage.getSingleInstance().loadBoolean("schemaCheckLocalFiles", schemaCheckLocalFiles);
        continueThread = true;
        remoteLoaderThreadGroup = new ThreadGroup("RemoteLoaderThreads");
        localLoaderThreadGroup = new ThreadGroup("LocalLoaderThreads");
    }

    synchronized public void startLoaderThreads() {
        // start the remote imdi loader threads
        while (continueThread && remoteLoaderThreadGroup.activeCount() < 6) { //TG: Why 6? (2011/2/2)
            String threadName = "ArbilDataNodeLoader-remote-" + threadStartCounter++;
            //createRemoteLoadThread(threadName).start();
            Thread thread = new Thread(remoteLoaderThreadGroup, new RemoteLoader(), threadName);
            thread.setPriority(Thread.NORM_PRIORITY - 1);
            thread.start();
        }
        // due to an apparent deadlock in the imdi api only one thread is used for local files. the deadlock appears to be in the look up host area
        // start the local imdi threads
        while (continueThread && localLoaderThreadGroup.activeCount() < 6) { //TG: Why 6? (2011/2/2)
            String threadName = "ArbilDataNodeLoader-local-" + threadStartCounter++;
            Thread thread = new Thread(localLoaderThreadGroup, new LocalLoader(), threadName);
            thread.setPriority(Thread.NORM_PRIORITY - 1);
            thread.start();
        }
    }

    synchronized private void addNodeToQueue(ArbilDataNode nodeToAdd) {
        startLoaderThreads();
        if (ArbilDataNode.isStringLocal(nodeToAdd.getUrlString())) {
            if (!arbilLocalNodesToInit.contains(nodeToAdd)) {
                arbilLocalNodesToInit.addElement(nodeToAdd);
            }
        } else {
            if (!arbilRemoteNodesToInit.contains(nodeToAdd)) {
                arbilRemoteNodesToInit.addElement(nodeToAdd);
            }
        }
    }

    synchronized private ArbilDataNode getNodeFromQueue(Vector<ArbilDataNode> dataNodesQueue) {
        // TODO: put this size test and remove into a syncronised function
        if (dataNodesQueue.size() > 0) {
            ArbilDataNode tempDataNode = dataNodesQueue.remove(0);
            if (tempDataNode.lockedByLoadingThread) {
                dataNodesQueue.add(tempDataNode);
                return null;
            } else {
                tempDataNode.lockedByLoadingThread = true;
                return tempDataNode;
            }
        } else {
            return null;
        }
    }

//    public ImdiTreeObject isImdiObjectLoaded(String localUrlString) {
//        localUrlString = ImdiTreeObject.conformStringToUrl(localUrlString).toString();
//        return imdiHashTable.get(localUrlString);
//    }
    public ArbilDataNode getArbilDataNodeWithoutLoading(URI localUri) {
        ArbilDataNode currentDataNode = null;
        if (localUri != null) {
            localUri = ArbilDataNode.normaliseURI(localUri);
            // correct any variations in the url string
//            localUri = ImdiTreeObject.conformStringToUrl(localUri).toString();
            currentDataNode = arbilHashTable.get(localUri.toString());
            if (currentDataNode == null) {
//                System.out.println("ImdiObject not in list so requesting: " + localNodeText + " : " + localUrlString);
                currentDataNode = new ArbilDataNode(localUri);
                arbilHashTable.put(localUri.toString(), currentDataNode);
            }
        }
        return currentDataNode;
    }

    public ArbilDataNode getArbilDataNode(Object registeringObject, URI localUri) {// throws Exception {
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
        ArbilDataNode currentDataNode = null;
        if (localUri != null && localUri.toString().length() > 0) {
            currentDataNode = getArbilDataNodeWithoutLoading(localUri);
//            System.out.println(currentImdiObject.isImdiChild() + ", " + currentImdiObject.getParentDomNode().imdiDataLoaded + ", " + currentImdiObject.isLoading());
            if (!currentDataNode.getParentDomNode().dataLoaded && !currentDataNode.isLoading()) {
//                System.out.println("created new ImdiObject: " + currentImdiObject.getUrlString());
                if (ArbilDataNode.isStringChildNode(currentDataNode.getUrlString())) {
//                    System.out.println("cause the parent node to be loaded");
                    // cause the parent node to be loaded
                    currentDataNode.getParentDomNode();
                } else if (ArbilDataNode.isPathMetadata(currentDataNode.getUrlString()) || ArbilDataNode.isPathHistoryFile(currentDataNode.getUrlString())) {
                    currentDataNode.updateLoadingState(+1);
                    addNodeToQueue(currentDataNode);
//                    System.out.println("+imdiHashTable.size: " + arbilHashTable.size());
                } else if (!ArbilDataNode.isPathMetadata(currentDataNode.getUrlString())) {
//                    currentImdiObject.clearIcon(); // do not do this
                }
            }
            currentDataNode.registerContainer(registeringObject);
        }
//        System.out.println("currentImdiObject: " + currentImdiObject);
        return currentDataNode;
    }

    public void releaseArbilDataNode(URI localUri) {
//        imdiHashTable.remove(imdiUrlString); // TODO: implement this so that imdi files are not held in memory for ever
        localUri = ArbilDataNode.normaliseURI(localUri);
        System.out.println("-imdiHashTable.size: " + arbilHashTable.size());
    }

    // return the node only if it has already been loaded otherwise return null
    public ArbilDataNode getArbilDataNodeOnlyIfLoaded(URI arbilUri) {
//        String localUrlString = ImdiTreeObject.conformStringToUrl(imdiUrl).toString();
        arbilUri = ArbilDataNode.normaliseURI(arbilUri);
        return arbilHashTable.get(arbilUri.toString());
    }

    // reload the node only if it has already been loaded otherwise ignore
    public void requestReloadOnlyIfLoaded(URI arbilUri) {
//        String localUrlString = ImdiTreeObject.conformStringToUrl(imdiUrl).toString();
        arbilUri = ArbilDataNode.normaliseURI(arbilUri);
        ArbilDataNode currentDataNode = arbilHashTable.get(arbilUri.toString());
        if (currentDataNode != null) {
            requestReload(currentDataNode);
        }
    }

    // reload the node or if it is an imdichild node then reload its parent
    public void requestReload(ArbilDataNode currentDataNode) {
        if (currentDataNode.isChildNode()) {
            currentDataNode = currentDataNode.getParentDomNode();
        }
        removeNodesNeedingSave(currentDataNode);
//        if (ImdiTreeObject.isStringImdi(currentImdiObject.getUrlString()) || ImdiTreeObject.isStringImdiHistoryFile(currentImdiObject.getUrlString())) {
        addNodeToQueue(currentDataNode);
//        }
    }

    public void requestReloadAllNodes() {
        for (ArbilDataNode currentDataNode : arbilHashTable.values()) {
            requestReload(currentDataNode);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        // stop the thread
        continueThread = false;
        super.finalize();
    }

    public void addNodeNeedingSave(ArbilDataNode nodeToSave) {
        nodeToSave = nodeToSave.getParentDomNode();
        if (!nodesNeedingSave.contains(nodeToSave)) {
            System.out.println("addNodeNeedingSave: " + nodeToSave);
            nodesNeedingSave.add(nodeToSave);
        }
    }

    public void removeNodesNeedingSave(ArbilDataNode savedNode) {
        System.out.println("removeNodesNeedingSave: " + savedNode);
        nodesNeedingSave.remove(savedNode);
    }

    public ArbilDataNode[] getNodesNeedSave() {
        return nodesNeedingSave.toArray(new ArbilDataNode[]{});
    }

    public boolean nodesNeedSave() {
        return nodesNeedingSave.size() > 0;
    }

    public synchronized void saveNodesNeedingSave(boolean updateIcons) {
        // this is syncronised to avoid issues from the key repeat on linux which fails to destinguish between key up events and key repeat events
        while (nodesNeedingSave.size() > 0) {
            // remove the node from the save list not in the save function because otherwise if the save fails the application will lock up
            ArbilDataNode currentNode = nodesNeedingSave.remove(0);
            if (currentNode != null) {
                currentNode.saveChangesToCache(updateIcons); // saving removes the node from the nodesNeedingSave vector via removeNodesNeedingSave
                if (updateIcons) {
                    requestReload(currentNode);
                }
            }
        }
    }

    /***
     * Runnable that gets a node from the remote queue and loads it
     */
    private class RemoteLoader implements Runnable {

        @Override
        @SuppressWarnings("SleepWhileHoldingLock")
        public void run() {
            while (continueThread && !Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    GuiHelper.linorgBugCatcher.logError(ie);
                }
                ArbilDataNode currentArbilDataNode = getNodeFromQueue(arbilRemoteNodesToInit);
                while (currentArbilDataNode != null) {
                    // this has been separated in to two separate threads to prevent long delays when there is no server connection
                    // each node is loaded one at a time and must time out before the next is started
                    // the local corpus nodes are the fastest so they are now loaded in a separate thread
                    // alternatively a thread pool may be an option
                    System.out.println("run RemoteArbilLoader processing: " + currentArbilDataNode.getUrlString());
                    currentArbilDataNode.loadArbilDom();
                    currentArbilDataNode.updateLoadingState(-1);
                    currentArbilDataNode.clearIcon();
                    currentArbilDataNode.clearChildIcons();
                    remoteArbilFilesLoaded++;
                    currentArbilDataNode.notifyLoaded();
                    currentArbilDataNode.lockedByLoadingThread = false;
                    currentArbilDataNode = getNodeFromQueue(arbilRemoteNodesToInit);
                }
            }
        }
    }

    /**
     * Runnable that gets a node from the local queue and loads it
     */
    private class LocalLoader implements Runnable {

        @Override
        @SuppressWarnings("SleepWhileHoldingLock")
        public void run() {
            while (continueThread && !Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    GuiHelper.linorgBugCatcher.logError(ie);
                }
                ArbilDataNode currentArbilDataNode = getNodeFromQueue(arbilLocalNodesToInit);
                while (currentArbilDataNode != null) {
                    System.out.println("run LocalArbilLoader processing: " + currentArbilDataNode.getUrlString());
                    if (currentArbilDataNode.getNeedsSaveToDisk(false)) {
                        currentArbilDataNode.saveChangesToCache(false);
                    }
                    currentArbilDataNode.loadArbilDom();
                    if (schemaCheckLocalFiles) {
                        if (currentArbilDataNode.isMetaDataNode()) {
                            XsdChecker xsdChecker = new XsdChecker();
                            String checkerResult;
                            checkerResult = xsdChecker.simpleCheck(currentArbilDataNode.getFile(), currentArbilDataNode.getURI());
                            currentArbilDataNode.hasSchemaError = (checkerResult != null);
                        }
                    } else {
                        currentArbilDataNode.hasSchemaError = false;
                    }
                    currentArbilDataNode.updateLoadingState(-1);
                    currentArbilDataNode.clearIcon();
                    currentArbilDataNode.clearChildIcons();
                    arbilFilesLoaded++;
                    System.out.println("remoteArbilFilesLoaded: " + remoteArbilFilesLoaded + " arbilFilesLoaded: " + arbilFilesLoaded);
                    currentArbilDataNode.lockedByLoadingThread = false;
                    currentArbilDataNode.notifyLoaded();
                    currentArbilDataNode = getNodeFromQueue(arbilLocalNodesToInit);
                }
            }
        }
    }
}
