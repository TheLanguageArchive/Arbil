package nl.mpi.arbil.data;

import java.net.URI;
import java.net.URISyntaxException;
import nl.mpi.arbil.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.ProgressMonitor;

/**
 * Document   : ImdiLoader
 * Created on : Dec 30, 2008, 3:04:39 PM
 * @author Peter.Withers@mpi.nl 
 */
public class ImdiLoader {

    private boolean continueThread = true;
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
        continueThread = true;
        // start three remote imdi loader threads
        for (int threadCounter = 0; threadCounter < 6; threadCounter++) {
            new Thread() {

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
                            currentImdiObject = getNodeFromQueue(imdiRemoteNodesToInit);
                        }
                    }
                }
            }.start();
        }
        // due to an apparent deadlock in the imdi api only one thread is used for local files. the deadlock appears to be in the look up host area
        // start the local imdi thread
        new Thread() {

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
                        ProgressMonitor progressMonitor = new ProgressMonitor(LinorgWindowManager.getSingleInstance().desktopPane, null, "Adding", 0, 100);
                        if (currentImdiObject.getNeedsSaveToDisk()) {
                            currentImdiObject.saveChangesToCache(false);
                        }
                        currentImdiObject.loadImdiDom();
                        if (currentImdiObject.addQueue.size() > 0) { // add any child nodes requested
                            String nodeType, targetXmlPath, nodeTypeDisplayName, favouriteUrlString, mimeType;
                            URI resourceUri = null;
                            {
                                String[] addRequestArrayString = currentImdiObject.addQueue.remove(0);
                                nodeType = addRequestArrayString[0];
                                targetXmlPath = addRequestArrayString[1];
                                nodeTypeDisplayName = addRequestArrayString[2];
                                favouriteUrlString = addRequestArrayString[3];
                                if (addRequestArrayString[4] != null) {
                                    try {
                                        resourceUri = new URI(addRequestArrayString[4]);
                                    } catch (URISyntaxException urise) {
                                        GuiHelper.linorgBugCatcher.logError(urise);
                                    }
                                }
                                mimeType = addRequestArrayString[5];
                            }
//                                    Vector<ImdiTreeObject> allChildren = new Vector();
//                                    allChildren.add(favouriteImdiNode);
//                                    favouriteImdiNode.getAllChildren(allChildren);
                            // sub node loop
//                                    for (ImdiTreeObject currentFavChild : allChildren.toArray(new ImdiTreeObject[]{})) 

                            String newTableTitleString = "new " + nodeTypeDisplayName;
                            if (currentImdiObject.isImdi() && !currentImdiObject.fileNotFound) {
                                newTableTitleString = newTableTitleString + " in " + currentImdiObject.toString();
                            }
                            System.out.println("addQueue:-\nnodeType: " + nodeType + "\ntargetXmlPath: " + targetXmlPath + "\nnodeTypeDisplayName: " + nodeTypeDisplayName + "\nfavouriteUrlString: " + favouriteUrlString + "\nresourceUrl: " + resourceUri + "\nmimeType: " + mimeType);
//                                    ImdiTreeObject addedImdiObject = TreeHelper.getSingleInstance().addImdiChildNode(currentImdiObject, nodeType, nodeTypeDisplayName, resourceUrl, mimeType);
                            ImdiTreeObject addedImdiObject = getImdiObjectWithoutLoading(currentImdiObject.addChildNode(nodeType, targetXmlPath, resourceUri, mimeType));
                            if (addedImdiObject != null) {
                                Vector<ImdiTreeObject> allAddedNodes = new Vector<ImdiTreeObject>();
//                                    imdiTableModel.addImdiObjects(new ImdiTreeObject[]{addedImdiObject});
                                //ImdiTableModel imdiTableModel = LinorgWindowManager.getSingleInstance().openAllChildNodesInFloatingTableOnce(new ImdiTreeObject[]{addedImdiObject}, newTableTitleString);
                                allAddedNodes.add(addedImdiObject);
                                addedImdiObject.loadImdiDom();
                                if (favouriteUrlString != null) {
                                    mergeWithFavourite(addedImdiObject, favouriteUrlString, allAddedNodes, progressMonitor);
                                } else {
                                    addedImdiObject.updateImdiFileNodeIds();
                                }
//                                    addedImdiObject.loadChildNodes();
                                addedImdiObject.clearIcon();
                                addedImdiObject.clearChildIcons();
                                addedImdiObject.scrollToRequested = true;
                                TreeHelper.getSingleInstance().updateTreeNodeChildren(currentImdiObject.getParentDomNode());
                                if (currentImdiObject.getParentDomNode() != addedImdiObject.getParentDomNode()) {
                                    TreeHelper.getSingleInstance().updateTreeNodeChildren(addedImdiObject.getParentDomNode());
                                }
                                ImdiTableModel imdiTableModel = LinorgWindowManager.getSingleInstance().openFloatingTableOnce(allAddedNodes.toArray(new ImdiTreeObject[]{}), newTableTitleString);
                            }
                        } else {
//                                if (currentImdiObject.autoLoadChildNodes) {
//                                    currentImdiObject.loadChildNodes();
//                                }
                            TreeHelper.getSingleInstance().updateTreeNodeChildren(currentImdiObject);
                        }
                        currentImdiObject.updateLoadingState(-1);
                        currentImdiObject.clearIcon();
                        currentImdiObject.clearChildIcons();
                        imdiFilesLoaded++;
                        System.out.println("remoteImdiFilesLoaded: " + remoteImdiFilesLoaded + " imdiFilesLoaded: " + imdiFilesLoaded);
                        // TODO: implement a cancel action for the progress bar
                        progressMonitor.close();
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

    private void mergeWithFavourite(ImdiTreeObject addedImdiObject, String favouriteUrlString, Vector<ImdiTreeObject> allAddedNodes, ProgressMonitor progressMonitor) {
        progressMonitor.setNote("Adding Child Nodes");
        ArrayList<ImdiTreeObject[]> nodesToMerge = new ArrayList<ImdiTreeObject[]>();
        //getImdiObject/* this should not be used here because it will cause another thread to work on the node */
        // TODO: should this favourite node be loaded here? if so it must be done without the queue
        try {
            ImdiTreeObject favouriteImdiNode = getImdiObjectWithoutLoading(new URI(favouriteUrlString));
            nodesToMerge.add(new ImdiTreeObject[]{addedImdiObject, favouriteImdiNode});
            // add all the child node templates
            progressMonitor.setMaximum(addedImdiObject.getAllChildren().length * 3);

            duplicateChildNodeStructure(favouriteImdiNode, addedImdiObject, nodesToMerge, progressMonitor, allAddedNodes);
            int progressCounter = nodesToMerge.size();
            progressMonitor.setNote("Copying Data");
            progressMonitor.setMaximum(progressCounter + nodesToMerge.size() * 2);
            for (ImdiTreeObject[] currentMergeArray : nodesToMerge.toArray(new ImdiTreeObject[][]{})) {
                if (currentMergeArray[0] != null && currentMergeArray[1] != null) {
                    System.out.println("about to merge:\n" + currentMergeArray[0].getUrlString() + "\n" + currentMergeArray[1].getUrlString());
                }
                progressMonitor.setProgress(progressCounter++);
            }
            addedImdiObject.updateImdiFileNodeIds();
            for (ImdiTreeObject[] currentMergeArray : nodesToMerge.toArray(new ImdiTreeObject[][]{})) {
                if (currentMergeArray[0] != null && currentMergeArray[1] != null) {
                    System.out.println("merging:\n" + currentMergeArray[0].getUrlString() + "\n" + currentMergeArray[1].getUrlString());
//                                                    if (!currentMergeArray[0].getUrlString().contains("#")) {
//                                                        System.out.println("oops: " + currentMergeArray[0] + currentMergeArray[0].getParentDomNode() + "\n" + currentMergeArray[0].getUrlString());
//                                                        System.out.println("oops: " + currentMergeArray[0] + currentMergeArray[0].getParentDomNode() + "\n" + currentMergeArray[0].getUrlString());
//                                                    }
                    LinorgFavourites.getSingleInstance().mergeFromFavourite(currentMergeArray[0], currentMergeArray[1], true);
                }
                progressMonitor.setProgress(progressCounter++);
            }
//                                        addedImdiObject.saveChangesToCache(true);
        } catch (URISyntaxException ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
        }
    }

    private void duplicateChildNodeStructure(ImdiTreeObject favouriteImdiNode, ImdiTreeObject addedImdiObject, ArrayList<ImdiTreeObject[]> nodesToMerge, ProgressMonitor progressMonitor, Vector<ImdiTreeObject> allAddedNodes) {
        //if (addedImdiObject.getURI().getFragment() == null && addedImdiObject.isImdiChild()) {
        //    System.out.println("Found a node to check");
        //}
        ImdiTreeObject[] currentFavChildren = favouriteImdiNode.getChildArray();
        for (ImdiTreeObject currentFavChild : currentFavChildren) {
            System.out.println("childNode: " + currentFavChild.getUrlString());
            if (currentFavChild.isMetaNode()) {
                System.out.println("omitting: " + currentFavChild);
//                System.out.println("currentFavChild.getFragment():" + currentFavChild.getURI().getFragment().toString());
//                System.out.println("addedImdiObject.getFragment():" + addedImdiObject.getURI().getFragment().toString());
                duplicateChildNodeStructure(currentFavChild, addedImdiObject, nodesToMerge, progressMonitor, allAddedNodes);
            } else if (currentFavChild.isImdi()) {
//                                                    ImdiTreeObject addedChildImdiObjects = TreeHelper.getSingleInstance().addImdiChildNode(addedImdiObject, LinorgFavourites.getSingleInstance().getNodeType(currentFavChild, currentImdiObject), nodeTypeDisplayName, resourceUrl, mimeType);
                String nodeType = addedImdiObject.getURI().getFragment();
//                if (nodeType == null) {
//                    nodeType = ""; // imdi parent nodes have no fragment but must not pass null as the nodeType
//                }
                URI addedChildImdiObjectURI = addedImdiObject.addChildNode(LinorgFavourites.getSingleInstance().getNodeType(currentFavChild, addedImdiObject), nodeType, null, null);
                //GuiHelper.imdiLoader.getImdiObject/* this should not be used here because it will cause another thread to work on the node */
                ImdiTreeObject addedChildImdiObject = getImdiObjectWithoutLoading(addedChildImdiObjectURI);
                allAddedNodes.add(addedChildImdiObject);
//                imdiTableModel.addImdiObjects(new ImdiTreeObject[]{addedChildImdiObject});
                nodesToMerge.add(new ImdiTreeObject[]{addedChildImdiObject, currentFavChild});
                System.out.println("nodesToMerge: " + addedChildImdiObject + addedChildImdiObject.getParentDomNode() + "\n" + addedChildImdiObject.getUrlString());
//                                                    if (!addedChildImdiObject.getUrlString().contains("#")) {
//                                                        System.out.println("oops A: " + addedChildImdiObject + addedChildImdiObject.getParentDomNode() + "\n" + addedChildImdiObject.getUrlString());
//                                                        System.out.println("oops A: " + addedChildImdiObject + addedChildImdiObject.getParentDomNode() + "\n" + addedChildImdiObject.getUrlString());
//                                                    }
                progressMonitor.setProgress(nodesToMerge.size());
                //System.out.println("addedImdiObject.getFragment():" + addedImdiObject.getURI().getFragment());
                //System.out.println("addedChildImdiObject.getFragment():" + addedChildImdiObject.getURI().getFragment());
                duplicateChildNodeStructure(currentFavChild, addedChildImdiObject, nodesToMerge, progressMonitor, allAddedNodes);
            } else {
                System.out.println("omitting due to not being an imdi: " + currentFavChild);
            }
//        Arrays.sort(allChildNodes, new Comparator() {
//
//            public int compare(Object firstColumn, Object secondColumn) {
//                try {
//                    String leftString = ((ImdiTreeObject) firstColumn).getUrlString();
//                    String rightString = ((ImdiTreeObject) secondColumn).getUrlString();
//                    int leftPathCount = leftString.split("\\.").length;
//                    int rightPathCount = rightString.split("\\.").length;
//                    if (leftPathCount == rightPathCount) {
//                        return leftString.compareTo(rightString);
//                    } else {
//                        return leftPathCount - rightPathCount;
//                    }
//                } catch (Exception ex) {
//                    GuiHelper.linorgBugCatcher.logError(ex);
//                    return 1;
//                }
//            }
//        });
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
        localUri = ImdiTreeObject.normaliseURI(localUri);
        ImdiTreeObject currentImdiObject = null;
        if (localUri != null) {
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
                } else if (ImdiTreeObject.isStringImdi(currentImdiObject.getUrlString()) || ImdiTreeObject.isStringImdiHistoryFile(currentImdiObject.getUrlString())) {
                    currentImdiObject.updateLoadingState(+1);
                    addNodeToQueue(currentImdiObject);
                    System.out.println("+imdiHashTable.size: " + imdiHashTable.size());
                } else if (!ImdiTreeObject.isStringImdi(currentImdiObject.getUrlString())) {
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

    @Override
    protected void finalize() throws Throwable {
        // stop the thread
        continueThread = false;
        super.finalize();
    }

    public void addNodeNeedingSave(ImdiTreeObject nodeToSave) {
        if (!nodesNeedingSave.contains(nodeToSave)) {
            System.out.println("addNodeNeedingSave: " + nodeToSave);
            nodesNeedingSave.add(nodeToSave.getParentDomNode());
        }
    }

    public void removeNodesNeedingSave(ImdiTreeObject savedNode) {
        System.out.println("removeNodesNeedingSave: " + savedNode);
        nodesNeedingSave.remove(savedNode.getParentDomNode());
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
