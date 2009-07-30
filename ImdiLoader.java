package mpi.linorg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Vector;

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
                            sleep(100);
                        } catch (InterruptedException ie) {
                            GuiHelper.linorgBugCatcher.logError(ie);
                        }
                        ImdiTreeObject currentImdiObject = getNodeFromQueue(imdiLocalNodesToInit);
                        while (currentImdiObject != null) {
                            if (currentImdiObject != null) {
                                System.out.println("run LocalImdiLoader processing: " + currentImdiObject.getUrlString());
                                if (currentImdiObject.imdiNeedsSaveToDisk) {
                                    currentImdiObject.saveChangesToCache(false);
                                }
                                currentImdiObject.loadImdiDom();
                                if (currentImdiObject.addQueue.size() > 0) { // add any child nodes requested
                                    String nodeType, nodeTypeDisplayName, favouriteUrlString, resourceUrl, mimeType;
                                    {
                                        String[] addRequestArrayString = currentImdiObject.addQueue.remove(0);
                                        nodeType = addRequestArrayString[0];
                                        nodeTypeDisplayName = addRequestArrayString[1];
                                        favouriteUrlString = addRequestArrayString[2];
                                        resourceUrl = addRequestArrayString[3];
                                        mimeType = addRequestArrayString[4];
                                    }
                                    String newTableTitleString = "new " + nodeTypeDisplayName;
                                    if (currentImdiObject.isImdi() && !currentImdiObject.fileNotFound) {
                                        newTableTitleString = newTableTitleString + " in " + currentImdiObject.toString();
                                    }
                                    System.out.println("addQueue:-\nnodeType: " + nodeType + "\nnodeTypeDisplayName: " + nodeTypeDisplayName + "\nfavouriteUrlString: " + favouriteUrlString + "\nresourceUrl: " + resourceUrl + "\nmimeType: " + mimeType);
                                    ImdiTreeObject addedImdiObject = TreeHelper.getSingleInstance().addImdiChildNode(currentImdiObject, nodeType, nodeTypeDisplayName, resourceUrl, mimeType);
//                                    imdiTableModel.addImdiObjects(new ImdiTreeObject[]{addedImdiObject});
                                    ImdiTableModel imdiTableModel = LinorgWindowManager.getSingleInstance().openFloatingTableOnce(new ImdiTreeObject[]{addedImdiObject}, newTableTitleString);

                                    currentImdiObject.loadImdiDom();
                                    if (favouriteUrlString != null) {
                                        ArrayList<ImdiTreeObject[]> nodesToMerge = new ArrayList();
                                        ImdiTreeObject favouriteImdiNode = getImdiObject(null, favouriteUrlString);
                                        nodesToMerge.add(new ImdiTreeObject[]{addedImdiObject, favouriteImdiNode});
                                        ImdiTreeObject[] allChildNodes = favouriteImdiNode.getAllChildren();
                                        Arrays.sort(allChildNodes, new Comparator() {

                                            public int compare(Object firstColumn, Object secondColumn) {
                                                try {
                                                    String leftString = ((ImdiTreeObject) firstColumn).getUrlString();
                                                    String rightString = ((ImdiTreeObject) secondColumn).getUrlString();
                                                    int leftPathCount = leftString.split("\\.").length;
                                                    int rightPathCount = rightString.split("\\.").length;
                                                    if (leftPathCount == rightPathCount) {
                                                        return leftString.compareTo(rightString);
                                                    } else {
                                                        return leftPathCount - rightPathCount;
                                                    }
                                                } catch (Exception ex) {
                                                    GuiHelper.linorgBugCatcher.logError(ex);
                                                    return 1;
                                                }
                                            }
                                        });
//                                        for (ImdiTreeObject currentFavChild : allChildNodes) {
//                                            System.out.println("sort output: " + currentFavChild.getUrlString());
//                                        }
                                        for (ImdiTreeObject currentFavChild : allChildNodes) {
                                            System.out.println("currentFavChild: " + currentFavChild.getUrlString());
                                            if (currentFavChild.getFields().size() > 0) {
                                                ImdiTreeObject addedChildImdiObject = TreeHelper.getSingleInstance().addImdiChildNode(addedImdiObject, LinorgFavourites.getSingleInstance().getNodeType(currentFavChild, currentImdiObject), nodeTypeDisplayName, resourceUrl, mimeType);
                                                imdiTableModel.addImdiObjects(new ImdiTreeObject[]{addedChildImdiObject});
                                                nodesToMerge.add(new ImdiTreeObject[]{addedChildImdiObject, currentFavChild});
                                            } else {
                                                System.out.println("omitting: " + currentFavChild);
                                            }
                                        }
                                        currentImdiObject.loadImdiDom();
                                        for (ImdiTreeObject[] currentMergeArray : nodesToMerge.toArray(new ImdiTreeObject[][]{})) {
                                            if (currentMergeArray[0] != null && currentMergeArray[1] != null) {
                                                System.out.println("about to merge:\n" + currentMergeArray[0].getUrlString() + "\n" + currentMergeArray[1].getUrlString());
                                            }
                                        }
                                        for (ImdiTreeObject[] currentMergeArray : nodesToMerge.toArray(new ImdiTreeObject[][]{})) {
                                            if (currentMergeArray[0] != null && currentMergeArray[1] != null) {
                                                System.out.println("merging:\n" + currentMergeArray[0].getUrlString() + "\n" + currentMergeArray[1].getUrlString());
                                                LinorgFavourites.getSingleInstance().mergeFromFavourite(currentMergeArray[0], currentMergeArray[1], true);
                                            }
                                        }
//                                        currentImdiObject.saveChangesToCache(true);
                                    }

                                    currentImdiObject.loadChildNodes();
                                    addedImdiObject.clearIcon();
                                    addedImdiObject.scrollToRequested = true;
                                    TreeHelper.getSingleInstance().updateTreeNodeChildren(currentImdiObject.getParentDomNode());
                                } else {
                                    if (currentImdiObject.autoLoadChildNodes) {
                                        currentImdiObject.loadChildNodes();
                                    }
                                    TreeHelper.getSingleInstance().updateTreeNodeChildren(currentImdiObject);
                                }
                                currentImdiObject.updateLoadingState(-1);
                                currentImdiObject.clearIcon();
                                imdiFilesLoaded++;
                                System.out.println("remoteImdiFilesLoaded: " + remoteImdiFilesLoaded + " imdiFilesLoaded: " + imdiFilesLoaded);
                            }
                            currentImdiObject.lockedByLoadingThread = false;
                            currentImdiObject = getNodeFromQueue(imdiLocalNodesToInit);
                        }
                    }
                }
            }.start();
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
    public ImdiTreeObject getImdiObject(String localNodeText, String localUrlString) {
//        System.out.println("getImdiObject: " + localNodeText + " : " + localUrlString);
        ImdiTreeObject currentImdiObject = null;
        if (localUrlString.length() > 0) {
            // correct any variations in the url string
            localUrlString = ImdiTreeObject.conformStringToUrl(localUrlString).toString();
            currentImdiObject = imdiHashTable.get(localUrlString);
            if (currentImdiObject == null) {
                System.out.println("ImdiObject not in list so requesting: " + localNodeText + " : " + localUrlString);
                currentImdiObject = new ImdiTreeObject(localNodeText, localUrlString);
                System.out.println("created new ImdiObject: " + currentImdiObject.getUrlString());
                imdiHashTable.put(localUrlString, currentImdiObject);
                if (ImdiTreeObject.isStringImdiChild(currentImdiObject.getUrlString())) {
                    System.out.println("cause the parent node to be loaded");
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
                    currentImdiObject.clearIcon();
                }
            } else if (localNodeText != null) {
                // update the note text if it has been provided (will only change if not already set)
                currentImdiObject.setNodeText(localNodeText);
            }
        }
        return currentImdiObject;
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
            nodesNeedingSave.add(nodeToSave);
        }
    }

    public void removeNodesNeedingSave(ImdiTreeObject savedNode) {
        System.out.println("removeNodesNeedingSave: " + savedNode);
        nodesNeedingSave.remove(savedNode);
    }

    public boolean nodesNeedSave() {
        return nodesNeedingSave.size() > 0;
    }

    public void saveNodesNeedingSave(boolean updateIcons) {
        while (nodesNeedingSave.size() > 0) {
            // remove the node from the save list not in the save function because otherwise if the save fails the application will lock up
            ImdiTreeObject currentNode = nodesNeedingSave.remove(0);
            currentNode.saveChangesToCache(updateIcons); // saving removes the node from the nodesNeedingSave vector via removeNodesNeedingSave
        }
    }
}
