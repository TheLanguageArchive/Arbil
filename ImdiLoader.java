/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import java.util.Hashtable;
import java.util.Vector;

/**
 * Document   : ImdiLoader
 * Created on : Dec 30, 2008, 3:04:39 PM
 * @author petwit
 */
public class ImdiLoader {

    private boolean continueThread = true;
    private Vector<ImdiTreeObject> imdiRemoteNodesToInit = new Vector();
    private Vector<ImdiTreeObject> imdiLocalNodesToInit = new Vector();
    private Hashtable<String, ImdiTreeObject> imdiHashTable = new Hashtable();
    private Vector<ImdiTreeObject> nodesNeedingSave = new Vector();
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
                                currentImdiObject.isLoadingCount--;
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
                                    currentImdiObject.saveChangesToCache();
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
                                    System.out.println("addQueue:-\nnodeType: " + nodeType + "\nnodeTypeDisplayName: " + nodeTypeDisplayName + "\nfavouriteUrlString: " + favouriteUrlString + "\nresourceUrl: " + resourceUrl + "\nmimeType: " + mimeType);
                                    ImdiTreeObject addedImdiObject = GuiHelper.treeHelper.addImdiChildNode(currentImdiObject, nodeType, nodeTypeDisplayName, resourceUrl, mimeType);
                                    currentImdiObject.loadImdiDom();
                                    if (favouriteUrlString != null) {
                                        // TODO: do this for all the descendants of the template
                                        LinorgFavourites.getSingleInstance().mergeFromFavourite(addedImdiObject, getImdiObject("", favouriteUrlString), true);
                                    }
                                    currentImdiObject.loadChildNodes();
                                    addedImdiObject.clearIcon();
                                    GuiHelper.treeHelper.updateTreeNodeChildren(currentImdiObject);
//                                    addedImdiObject.autoLoadChildNodes = true;
//                                    addedImdiObject.loadChildNodes();
                                    GuiHelper.treeHelper.localCorpusTree.scrollToNode(addedImdiObject);
                                } else {
                                    if (currentImdiObject.autoLoadChildNodes) {
                                        currentImdiObject.loadChildNodes();
                                    }
                                    GuiHelper.treeHelper.updateTreeNodeChildren(currentImdiObject);
                                }
                                currentImdiObject.isLoadingCount--;
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
                    currentImdiObject.isLoadingCount++;
                    if (ImdiTreeObject.isStringLocal(currentImdiObject.getUrlString())) {
                        imdiLocalNodesToInit.add(currentImdiObject);
                    } else {
                        imdiRemoteNodesToInit.add(currentImdiObject);
                    }
                }
            }
        }
        return currentImdiObject;
    }

    // reload the node or if it is an imdichild node then reload its parent
    public void requestReload(ImdiTreeObject currentImdiObject) {
        if (currentImdiObject.isImdiChild()) {
            currentImdiObject = currentImdiObject.getParentDomNode();
        }
        if (ImdiTreeObject.isStringImdi(currentImdiObject.getUrlString()) || ImdiTreeObject.isStringImdiHistoryFile(currentImdiObject.getUrlString())) {
            if (!imdiLocalNodesToInit.contains(currentImdiObject)) {
                System.out.println("requestReload: " + currentImdiObject.getUrlString());
                currentImdiObject.isLoadingCount++;
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

    public void saveNodesNeedingSave() {
        while (nodesNeedingSave.size() > 0) {
            nodesNeedingSave.get(0).saveChangesToCache(); // saving removes the node from the nodesNeedingSave vector via removeNodesNeedingSave
        }
    }
}
