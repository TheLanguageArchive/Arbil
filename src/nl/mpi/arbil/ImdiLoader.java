package nl.mpi.arbil;

import java.awt.Component;
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

    public ImdiLoader() {
        System.out.println("ImdiLoader init");
        continueThread = true;
        // start three remote imdi loader threads
        for (int threadCounter = 0; threadCounter < 3; threadCounter++) {
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
                            }
                            currentImdiObject = getNodeFromQueue(imdiRemoteNodesToInit);
                        }
                    }
                }
            }.start();
            // start the local imdi thread
            new Thread() {

                @Override
                public void run() {
                    setPriority(Thread.NORM_PRIORITY - 1);
                    while (continueThread) {
                        try {
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
                                    String nodeType, targetXmlPath, nodeTypeDisplayName, favouriteUrlString, resourceUrl, mimeType;
                                    {
                                        String[] addRequestArrayString = currentImdiObject.addQueue.remove(0);
                                        nodeType = addRequestArrayString[0];
                                        targetXmlPath = addRequestArrayString[1];
                                        nodeTypeDisplayName = addRequestArrayString[2];
                                        favouriteUrlString = addRequestArrayString[3];
                                        resourceUrl = addRequestArrayString[4];
                                        mimeType = addRequestArrayString[5];
                                    }
                                    String newTableTitleString = "new " + nodeTypeDisplayName;
                                    if (currentImdiObject.isImdi() && !currentImdiObject.fileNotFound) {
                                        newTableTitleString = newTableTitleString + " in " + currentImdiObject.toString();
                                    }
                                    System.out.println("addQueue:-\nnodeType: " + nodeType + "\ntargetXmlPath: " + targetXmlPath + "\nnodeTypeDisplayName: " + nodeTypeDisplayName + "\nfavouriteUrlString: " + favouriteUrlString + "\nresourceUrl: " + resourceUrl + "\nmimeType: " + mimeType);
//                                    ImdiTreeObject addedImdiObject = TreeHelper.getSingleInstance().addImdiChildNode(currentImdiObject, nodeType, nodeTypeDisplayName, resourceUrl, mimeType);
                                ImdiTreeObject addedImdiObject = getImdiObjectWithoutLoading(currentImdiObject.addChildNode(nodeType, targetXmlPath, resourceUrl, mimeType));
                                if (addedImdiObject != null) {
                                    Vector<ImdiTreeObject> allAddedNodes = new Vector<ImdiTreeObject>();
//                                    imdiTableModel.addImdiObjects(new ImdiTreeObject[]{addedImdiObject});
                                    //ImdiTableModel imdiTableModel = LinorgWindowManager.getSingleInstance().openFloatingTableOnce(new ImdiTreeObject[]{addedImdiObject}, newTableTitleString);
                                    allAddedNodes.add(addedImdiObject);
                                    addedImdiObject.loadImdiDom();
                                    if (favouriteUrlString != null) {
                                        progressMonitor.setNote("Adding Child Nodes");
                                        ArrayList<ImdiTreeObject[]> nodesToMerge = new ArrayList<ImdiTreeObject[]>();
                                        //getImdiObject/* this should not be used here because it will cause another thread to work on the node */
                                        // TODO: should this favourite node be loaded here? if so it must be done without the queue
                                        ImdiTreeObject favouriteImdiNode = getImdiObjectWithoutLoading(favouriteUrlString);
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
                            currentImdiObject = getNodeFromQueue(imdiLocalNodesToInit);
                        }
                        } catch (Exception ie) {
                            // anything that throws in this loop will prevent any further loading of local imdi files
                            GuiHelper.linorgBugCatcher.logError(ie);
                        }
                    }
                }
            }.start();
        }
    }

    public void duplicateChildNodeStructure(ImdiTreeObject favouriteImdiNode, ImdiTreeObject addedImdiObject, ArrayList<ImdiTreeObject[]> nodesToMerge, ProgressMonitor progressMonitor, Vector<ImdiTreeObject> allAddedNodes) {
        ImdiTreeObject[] currentFavChildren = favouriteImdiNode.getChildArray();
        for (ImdiTreeObject currentFavChild : currentFavChildren) {
            System.out.println("childNode: " + currentFavChild.getUrlString());
            if (currentFavChild.isMetaNode()) {
                System.out.println("omitting: " + currentFavChild);
                duplicateChildNodeStructure(currentFavChild, addedImdiObject, nodesToMerge, progressMonitor, allAddedNodes);
            } else {
//                                                    ImdiTreeObject addedChildImdiObjects = TreeHelper.getSingleInstance().addImdiChildNode(addedImdiObject, LinorgFavourites.getSingleInstance().getNodeType(currentFavChild, currentImdiObject), nodeTypeDisplayName, resourceUrl, mimeType);
                String addedChildImdiObjectPath = addedImdiObject.addChildNode(LinorgFavourites.getSingleInstance().getNodeType(currentFavChild, addedImdiObject), addedImdiObject.getURL().getRef(), null, null);
                //GuiHelper.imdiLoader.getImdiObject/* this should not be used here because it will cause another thread to work on the node */
                ImdiTreeObject addedChildImdiObject = getImdiObjectWithoutLoading(addedChildImdiObjectPath);
                allAddedNodes.add(addedChildImdiObject);
//                imdiTableModel.addImdiObjects(new ImdiTreeObject[]{addedChildImdiObject});
                nodesToMerge.add(new ImdiTreeObject[]{addedChildImdiObject, currentFavChild});
                System.out.println("nodesToMerge: " + addedChildImdiObject + addedChildImdiObject.getParentDomNode() + "\n" + addedChildImdiObject.getUrlString());
//                                                    if (!addedChildImdiObject.getUrlString().contains("#")) {
//                                                        System.out.println("oops A: " + addedChildImdiObject + addedChildImdiObject.getParentDomNode() + "\n" + addedChildImdiObject.getUrlString());
//                                                        System.out.println("oops A: " + addedChildImdiObject + addedChildImdiObject.getParentDomNode() + "\n" + addedChildImdiObject.getUrlString());
//                                                    }
                progressMonitor.setProgress(nodesToMerge.size());
                duplicateChildNodeStructure(currentFavChild, addedChildImdiObject, nodesToMerge, progressMonitor, allAddedNodes);
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
    public ImdiTreeObject getImdiObjectWithoutLoading(String localUrlString) {
        ImdiTreeObject currentImdiObject = null;
        if (localUrlString != null) {
            // correct any variations in the url string
            localUrlString = ImdiTreeObject.conformStringToUrl(localUrlString).toString();
            currentImdiObject = imdiHashTable.get(localUrlString);
            if (currentImdiObject == null) {
//                System.out.println("ImdiObject not in list so requesting: " + localNodeText + " : " + localUrlString);
                currentImdiObject = new ImdiTreeObject(localUrlString);
                imdiHashTable.put(localUrlString, currentImdiObject);
            }
        }
        return currentImdiObject;
    }

    public ImdiTreeObject getImdiObject(Component registeringObject, String localUrlString) {// throws Exception {
        ImdiTreeObject currentImdiObject = null;
        if (localUrlString != null && localUrlString.length() > 0) {
            currentImdiObject = getImdiObjectWithoutLoading(localUrlString);
            currentImdiObject.registerContainer(registeringObject);
//            System.out.println(currentImdiObject.isImdiChild() + ", " + currentImdiObject.getParentDomNode().imdiDataLoaded + ", " + currentImdiObject.isLoading());
            if (!currentImdiObject.getParentDomNode().imdiDataLoaded && !currentImdiObject.isLoading()) {
//                System.out.println("created new ImdiObject: " + currentImdiObject.getUrlString());
                if (ImdiTreeObject.isStringImdiChild(currentImdiObject.getUrlString())) {
//                    System.out.println("cause the parent node to be loaded");
                    // cause the parent node to be loaded
                    currentImdiObject.getParentDomNode();
                } else if (ImdiTreeObject.isStringImdi(currentImdiObject.getUrlString()) || ImdiTreeObject.isStringImdiHistoryFile(currentImdiObject.getUrlString())) {
                    currentImdiObject.updateLoadingState(+1);
                    if (ImdiTreeObject.isStringLocal(currentImdiObject.getUrlString())) {
                        imdiLocalNodesToInit.add(currentImdiObject);
                    } else {
                        imdiRemoteNodesToInit.add(currentImdiObject);
                    }
                } else if (!ImdiTreeObject.isStringImdi(currentImdiObject.getUrlString())) {
//                    currentImdiObject.clearIcon(); // do not do this
                }
            }
        }
//        System.out.println("currentImdiObject: " + currentImdiObject);
        return currentImdiObject;
    }

    public void releaseImdiObject(String imdiUrlString) {
    }

    // return the node only if it has already been loaded otherwise return null
    public ImdiTreeObject getImdiObjectOnlyIfLoaded(String imdiUrl) {
        String localUrlString = ImdiTreeObject.conformStringToUrl(imdiUrl).toString();
        return imdiHashTable.get(localUrlString);
    }

    // reload the node only if it has already been loaded otherwise ignore
    public void requestReloadOnlyIfLoaded(String imdiUrl) {
        String localUrlString = ImdiTreeObject.conformStringToUrl(imdiUrl).toString();
        ImdiTreeObject currentImdiObject = imdiHashTable.get(localUrlString);
        if (currentImdiObject != null) {
            requestReload(currentImdiObject);
        }
    }

    // reload the node or if it is an imdichild node then reload its parent
    public void requestReload(ImdiTreeObject currentImdiObject) {
        if (currentImdiObject.isImdiChild()) {
            currentImdiObject = currentImdiObject.getParentDomNode();
        }
        if (ImdiTreeObject.isStringImdi(currentImdiObject.getUrlString()) || ImdiTreeObject.isStringImdiHistoryFile(currentImdiObject.getUrlString())) {
            if (!imdiLocalNodesToInit.contains(currentImdiObject)) {
                System.out.println("requestReload: " + currentImdiObject.getUrlString());
                currentImdiObject.updateLoadingState(+1);
                imdiLocalNodesToInit.add(currentImdiObject);
            }
        }
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
            }
        }
    }
}
