package nl.mpi.arbil;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 * Document   : TreeHelper
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
public class TreeHelper {

    public DefaultTreeModel localCorpusTreeModel;
    public DefaultTreeModel remoteCorpusTreeModel;
    public DefaultTreeModel localDirectoryTreeModel;
    public DefaultTreeModel favouritesTreeModel;
    private DefaultMutableTreeNode localCorpusRootNode;
    private DefaultMutableTreeNode remoteCorpusRootNode;
    private DefaultMutableTreeNode localDirectoryRootNode;
    private DefaultMutableTreeNode favouritesRootNode;
    public ArbilTreePanels arbilTreePanel;
    private Vector<String> locationsList; // this is the list of locations seen in the tree and the location settings
    static private TreeHelper singleInstance = null;
    static public boolean trackTableSelection = false;
    Vector<DefaultMutableTreeNode> treeNodeSortQueue = new Vector<DefaultMutableTreeNode>(); // used in the tree node sort thread
    boolean treeNodeSortQueueRunning = false; // used in the tree node sort thread
    public boolean showHiddenFilesInTree = false;

    static synchronized public TreeHelper getSingleInstance() {
//        System.out.println("TreeHelper getSingleInstance");
        if (singleInstance == null) {
            singleInstance = new TreeHelper();
        }
        return singleInstance;
    }

    private TreeHelper() {
        localCorpusRootNode = new DefaultMutableTreeNode();
        remoteCorpusRootNode = new DefaultMutableTreeNode();
        localDirectoryRootNode = new DefaultMutableTreeNode();
        favouritesRootNode = new DefaultMutableTreeNode();

        localCorpusTreeModel = new DefaultTreeModel(localCorpusRootNode, true);
        remoteCorpusTreeModel = new DefaultTreeModel(remoteCorpusRootNode, true);
        localDirectoryTreeModel = new DefaultTreeModel(localDirectoryRootNode, true);
        favouritesTreeModel = new DefaultTreeModel(favouritesRootNode, true);
        loadLocationsList();
    }

    public ImdiTree getTreeForNode(DefaultMutableTreeNode nodeToTest) {
        if (nodeToTest.getRoot().equals(remoteCorpusRootNode)) {
            return arbilTreePanel.remoteCorpusTree;
        }
        if (nodeToTest.getRoot().equals(localCorpusRootNode)) {
            return arbilTreePanel.localCorpusTree;
        }
        if (nodeToTest.getRoot().equals(localDirectoryRootNode)) {
            return arbilTreePanel.localDirectoryTree;
        }
        return arbilTreePanel.favouritesTree;
    }

    public DefaultTreeModel getModelForNode(DefaultMutableTreeNode nodeToTest) {
        if (nodeToTest.getRoot().equals(remoteCorpusRootNode)) {
            return remoteCorpusTreeModel;
        }
        if (nodeToTest.getRoot().equals(localCorpusRootNode)) {
            return localCorpusTreeModel;
        }
        if (nodeToTest.getRoot().equals(localDirectoryRootNode)) {
            return localDirectoryTreeModel;
        }
        return favouritesTreeModel;
    }

    public boolean componentIsTheLocalCorpusTree(Component componentToTest) {
        return componentToTest.equals(arbilTreePanel.localCorpusTree);
        //return localCorpusTree.getName().equals(componentToTest.getName());
    }

    public boolean componentIsTheFavouritesTree(Component componentToTest) {
        return componentToTest.equals(arbilTreePanel.favouritesTree);
    }

    public void setTrees(ArbilTreePanels arbilTreePanelLocal) {
        arbilTreePanel = arbilTreePanelLocal;
//            ImdiTree tempRemoteCorpusTree, ImdiTree tempLocalCorpusTree, ImdiTree tempLocalDirectoryTree) {
        remoteCorpusRootNode.setUserObject(new JLabel("Remote Corpus", ImdiIcons.getSingleInstance().serverIcon, JLabel.LEFT));
        localCorpusRootNode.setUserObject(new JLabel("Local Corpus", ImdiIcons.getSingleInstance().directoryIcon, JLabel.LEFT));
        localDirectoryRootNode.setUserObject(new JLabel("Working Directories", ImdiIcons.getSingleInstance().computerIcon, JLabel.LEFT));
        favouritesRootNode.setUserObject(new JLabel("Favourites", ImdiIcons.getSingleInstance().favouriteIcon, JLabel.LEFT));

        arbilTreePanel.remoteCorpusTree.setName("RemoteCorpusTree");
        arbilTreePanel.localCorpusTree.setName("LocalCorpusTree");
        arbilTreePanel.localDirectoryTree.setName("LocalDirectoryTree");
        arbilTreePanel.favouritesTree.setName("FavouritesTree");

        applyRootLocations();
    }

    public int addDefaultCorpusLocations() {
        int addedCount = 0;
        if (addLocation("http://corpus1.mpi.nl/IMDI/metadata/IMDI.imdi")) {
            addedCount++;
        }
        if (addLocation("http://corpus1.mpi.nl/qfs1/media-archive/Corpusstructure/MPI.imdi")) {
            addedCount++;
        }
//        if (addLocation("http://corpus1.mpi.nl/qfs1/media-archive/dobes_data/ChintangPuma/Chintang/Conversation/Metadata/phidang_talk.imdi")) {
//            addedCount++;
//        }
//        if (addLocation("http://corpus1.mpi.nl/qfs1/media-archive/silang_data/Corpusstructure/1-03.imdi")) {
//            addedCount++;
//        }
//        if (addLocation("http://corpus1.mpi.nl/qfs1/media-archive/dobes_data/ECLING/Corpusstructure/ECLING.imdi")) {
//            addedCount++;
//        }
//        if (addLocation("http://corpus1.mpi.nl/qfs1/media-archive/dobes_data/Center/Corpusstructure/center.imdi")) {
//            addedCount++;
//        }
//        if (addLocation("http://corpus1.mpi.nl/qfs1/media-archive/dobes_data/Teop/Corpusstructure/1.imdi")) {
//            addedCount++;
//        }
//        if (addLocation("http://corpus1.mpi.nl/qfs1/media-archive/dobes_data/Waimaa/Corpusstructure/1.imdi")) {
//            addedCount++;
//        }
//        if (addLocation("http://corpus1.mpi.nl/qfs1/media-archive/dobes_data/Beaver/Corpusstructure/Beaver.imdi")) {
//            addedCount++;
//        }
        return addedCount;
    }

    public void saveLocations() {
        try {
            LinorgSessionStorage.getSingleInstance().saveObject(locationsList, "locationsList");
            System.out.println("saved locationsList");
        } catch (Exception ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
//            System.out.println("save locationsList exception: " + ex.getMessage());
        }
    }

    public void loadLocationsList() {
        try {
            System.out.println("loading locationsList");
            locationsList = (Vector<String>) LinorgSessionStorage.getSingleInstance().loadObject("locationsList");
        } catch (Exception ex) {
            System.out.println("load locationsList failed: " + ex.getMessage());
            locationsList = new Vector<String>();
//            locationsList.add("http://corpus1.mpi.nl/IMDI/metadata/IMDI.imdi");
//            locationsList.add("http://corpus1.mpi.nl/qfs1/media-archive/Corpusstructure/MPI.imdi");
//            //locationsList.add("file:///data1/media-archive-copy/Corpusstructure/MPI.imdi");
//            locationsList.add("file:///data1/media-archive-copy/TestWorkingDirectory/");
//            //locationsList.add("http://lux16.mpi.nl/corpora/ac-ESF/Info/ladfc2.txt");
//            //locationsList.add("file:///data1/media-archive-copy/Corpusstructure/MPI.imdi");
//            locationsList.add("http://corpus1.mpi.nl/qfs1/media-archive/Comprehension/Elizabeth_Johnson/Corpusstructure/1.imdi");
//            //locationsList.add("file:///data1/media-archive-copy/TestWorkingDirectory/");
            addDefaultCorpusLocations();
            System.out.println("created new locationsList");
        }
        showHiddenFilesInTree = LinorgSessionStorage.getSingleInstance().loadBoolean("showHiddenFilesInTree", showHiddenFilesInTree);
    }

    public void setShowHiddenFilesInTree(boolean showState) {
        showHiddenFilesInTree = showState;
        clearIconsInTree(localDirectoryRootNode);
        try {
            LinorgSessionStorage.getSingleInstance().saveObject(showHiddenFilesInTree, "showHiddenFilesInTree");
        } catch (Exception ex) {
            System.out.println("save showHiddenFilesInTree failed");
        }
    }

    public void addLocationGui(String addableLocation) {
        if (!addLocation(addableLocation)) {
            // alert the user when the node already exists and cannot be added again
            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("The location already exists and cannot be added again", "Add location");
        }
        applyRootLocations();
    //locationSettingsTable.setModel(guiHelper.getLocationsTableModel());
    }

    public boolean addLocation(String addedLocation) {
        System.out.println("addLocation" + addedLocation.toString());
        // make sure the added location url matches that of the imdi node format
        addedLocation = GuiHelper.imdiLoader.getImdiObject(null, addedLocation).getUrlString();
        if (addedLocation != null) {
            if (!locationsList.contains(addedLocation)) {
                locationsList.add(addedLocation);
                return true;
            }
        }
        return false;
    }

    public void removeLocation(Object removeObject) {
        if (ImdiTreeObject.isImdiNode(removeObject)) {
            removeLocation(((ImdiTreeObject) removeObject).getUrlString()); //.replace("file://", "")
        }
    }

    public void removeLocation(String removeLocation) {
        System.out.println("removeLocation: " + removeLocation);
        locationsList.remove(removeLocation);
    }

    private void clearIconsInTree(DefaultMutableTreeNode parentTreeNode) {
        // this will reload all nodes in a tree but not create any new child nodes
        for (Enumeration<DefaultMutableTreeNode> childNodesEnum = parentTreeNode.children(); childNodesEnum.hasMoreElements();) {
            clearIconsInTree(childNodesEnum.nextElement());
        }
        if (parentTreeNode.getUserObject() instanceof ImdiTreeObject) {
            // this will also update the child nodes in the tree without adding any new branches
            ((ImdiTreeObject) parentTreeNode.getUserObject()).clearIcon();
        }
    }

    // this will load all imdi child nodes into the tree, and in the case of a session it will load all the imdi childnodes, the while the other updateTreeNodeChildren methods will not
    public void updateTreeNodeChildren(ImdiTreeObject parentImdiNode) {
//        System.out.println("updateTreeNodeChildren ImdiTreeObject: " + parentImdiNode);
        for (Object currentContainer : parentImdiNode.getRegisteredContainers()) {
            if (currentContainer instanceof DefaultMutableTreeNode) {
//                System.out.println("updateTreeNodeChildren currentContainer: " + parentImdiNode + " : " + currentContainer.hashCode());
                updateTreeNodeChildren((DefaultMutableTreeNode) currentContainer);
            }
        }
        for (Enumeration<ImdiTreeObject> childNodesEnum = parentImdiNode.getChildEnum(); childNodesEnum.hasMoreElements();) {
            // recursively load the tree nodes of the child nodes
            ImdiTreeObject currentChild = childNodesEnum.nextElement();
            if (currentChild.isImdiChild()) {
                updateTreeNodeChildren(currentChild);
            }
        }
    }

    public void loadTreeNodeChildren(DefaultMutableTreeNode parentTreeNode) {
        Object parentObject = parentTreeNode.getUserObject();
        if (parentObject instanceof ImdiTreeObject) {
            ((ImdiTreeObject) parentObject).loadChildNodes();
        }
        updateTreeNodeChildren(parentTreeNode);
    }

    public void updateTreeNodeChildren(DefaultMutableTreeNode parentTreeNode) {
//        System.out.println("updateTreeNodeChildren DefaultMutableTreeNode: " + parentTreeNode.toString());
        addToSortQueue(parentTreeNode);
    }
    // check that all child nodes are attached and sorted, removing any extranious nodes found

    private void updateTreeNodeChildren(DefaultMutableTreeNode parentNode, Vector<String> childUrls, Vector<DefaultMutableTreeNode> scrollToRequests) {
//        System.out.println("updateTreeNodeChildren");
        DefaultTreeModel treeModel = getModelForNode(parentNode);
        if (parentNode.getUserObject() instanceof ImdiTreeObject && parentNode.getChildCount() > 0) {
            // leave any realoading nodes alone if they already have child nodes in the tree
            if (((ImdiTreeObject) parentNode.getUserObject()).getParentDomNode().isLoading()) {
                treeModel.nodeChanged(parentNode);
                // since we have ingnored the loading node we must put it back on the list so that it gets sorted when it has loaded
//                addToSortQueue(parentNode);
                return;
            }
        }
        Vector<DefaultMutableTreeNode> nodesToRemove = new Vector<DefaultMutableTreeNode>();
//        Vector<ImdiTreeObject> imdiNodesToInsert = new Vector<ImdiTreeObject>();
//      Do the sorting in a threaded queue, this has been tested with the Beaver archive which has lots of sessions in single branches
        ArrayList<DefaultMutableTreeNode> sortedChildren = Collections.list(parentNode.children());
//        if (childUrls.size() == 0) {
//            System.out.println("no children");
////            if (sortedChildren.size() > 0) {
//            System.out.println("removed all children");
//            treeModel.nodeStructureChanged(parentNode);
//            parentNode.removeAllChildren();
////            }
//            return;
//        }
        for (DefaultMutableTreeNode currentChildNode : sortedChildren.toArray(new DefaultMutableTreeNode[]{})) {
//            DefaultMutableTreeNode currentChildNode = childrenEnum.nextElement();
            ImdiTreeObject childImdiObject = (ImdiTreeObject) currentChildNode.getUserObject();
            if (!childUrls.remove(childImdiObject.getUrlString())) {
                nodesToRemove.add(currentChildNode);
                sortedChildren.remove(currentChildNode);
            }
        }
        boolean parentCanHaveChildren = false;
        if (parentNode.getUserObject() instanceof ImdiTreeObject) {
            parentCanHaveChildren = ((ImdiTreeObject) parentNode.getUserObject()).canHaveChildren();
        }
        parentNode.setAllowsChildren(childUrls.size() > 0 || sortedChildren.size() > 0 || parentNode.getChildCount() > 0 || parentCanHaveChildren);
        while (childUrls.size() > 0) {
            // add any missing child nodes
            ImdiTreeObject missingImdiNode = GuiHelper.imdiLoader.getImdiObject(null, childUrls.remove(0));
            DefaultMutableTreeNode missingTreeNode = new DefaultMutableTreeNode(missingImdiNode);
            missingImdiNode.registerContainer(missingTreeNode);
            sortedChildren.add(missingTreeNode);
        }
        while (!nodesToRemove.isEmpty()) {
//            TODO: are the nodes being removed when an actor is deleted ???
            DefaultMutableTreeNode currentChildNode = nodesToRemove.remove(0);
            System.out.println("nodesToRemove: " + currentChildNode);
            System.out.println("nodesToRemove: " + currentChildNode.getUserObject());
//            if (currentChildNode.getParent() != null) {
//                treeModel.removeNodeFromParent(currentChildNode);
//            }
            removeAndDetatchDescendantNodes(currentChildNode);
        }
        sortChildNodes(parentNode, sortedChildren, scrollToRequests);
    }

    private void getRootNodeArrays(Vector<String> remoteCorpusRootUrls, Vector<String> localCorpusRootUrls, Vector<String> localDirectoryRootUrls) {
        // this also removes all locations and replaces them with normalised paths
        Vector locationImdiNodes = new Vector();
        for (Enumeration locationEnum = locationsList.elements(); locationEnum.hasMoreElements();) {
            locationImdiNodes.add(GuiHelper.imdiLoader.getImdiObject(null, locationEnum.nextElement().toString()));
        }
        // remove all locations from the list so they can be replaced in a format that matches the imdi url format
//        locationsList.removeAllElements();
        Collections.sort(locationImdiNodes);
        for (Enumeration<ImdiTreeObject> locationNodesEnum = locationImdiNodes.elements(); locationNodesEnum.hasMoreElements();) {
            ImdiTreeObject currentImdiObject = locationNodesEnum.nextElement();
//            System.out.println("root location: " + currentImdiObject.getUrlString());
            // add the locations back to the list so they matches the imdi url format
//            locationsList.add(currentImdiObject.getUrlString());
            if (!currentImdiObject.isLocal()) {
                remoteCorpusRootUrls.add(currentImdiObject.getUrlString());
            } else if (LinorgSessionStorage.getSingleInstance().pathIsInsideCache(currentImdiObject.getFile())) {
                localCorpusRootUrls.add(currentImdiObject.getUrlString());
            } else {
                localDirectoryRootUrls.add(currentImdiObject.getUrlString());
            }
        }
    }

    synchronized private void addToSortQueue(DefaultMutableTreeNode currentTreeNode) {
        if (!treeNodeSortQueue.contains(currentTreeNode)) {
//            System.out.println("requestSort: " + currentTreeNode.getUserObject().toString());
            treeNodeSortQueue.add(currentTreeNode);
        }
        if (!treeNodeSortQueueRunning) {
            treeNodeSortQueueRunning = true;
            new Thread() {

                @Override
                public void run() {
                    Vector<DefaultMutableTreeNode> scrollToRequests = new Vector<DefaultMutableTreeNode>();
//                    setPriority(Thread.NORM_PRIORITY - 1);
                    while (treeNodeSortQueue.size() > 0) {
                        DefaultMutableTreeNode currentTreeNode = treeNodeSortQueue.remove(0);
                        if (currentTreeNode != null) {
                            Vector<String> childUrls = new Vector();
                            //DefaultMutableTreeNode parentTreeNode = (DefaultMutableTreeNode) itemNode.getParent();
                            Object parentObject = currentTreeNode.getUserObject();
                            if (parentObject instanceof ImdiTreeObject) {
                                ImdiTreeObject parentImdiObject = (ImdiTreeObject) parentObject;
                                // make the list of child urls
                                for (Enumeration<ImdiTreeObject> childEnum = parentImdiObject.getChildEnum(); childEnum.hasMoreElements();) {
                                    ImdiTreeObject childImdiObject = childEnum.nextElement();
                                    boolean showChild = true;
                                    if (!showHiddenFilesInTree && childImdiObject.isLocal() && childImdiObject.getFile().isHidden()) {
                                        showChild = false;
                                    }
                                    if (showChild) {
                                        childUrls.add(childImdiObject.getUrlString());
                                    }
//                                    System.out.println("adding child to update list: " + childImdiObject.getUrlString());
                                }
                                updateTreeNodeChildren(currentTreeNode, childUrls, scrollToRequests);
                            } else {
                                // assume that this is a root node so update the root nodes                              
                                Vector<String> remoteCorpusRootUrls = new Vector();
                                Vector<String> localCorpusRootUrls = new Vector();
                                Vector<String> localDirectoryRootUrls = new Vector();
                                getRootNodeArrays(remoteCorpusRootUrls, localCorpusRootUrls, localDirectoryRootUrls);
                                updateTreeNodeChildren(remoteCorpusRootNode, remoteCorpusRootUrls, scrollToRequests);
                                updateTreeNodeChildren(localCorpusRootNode, localCorpusRootUrls, scrollToRequests);
                                updateTreeNodeChildren(localDirectoryRootNode, localDirectoryRootUrls, scrollToRequests);
                                updateTreeNodeChildren(favouritesRootNode, LinorgFavourites.getSingleInstance().getFavouritesAsUrls(), scrollToRequests);
                            }
//                                sortChildNodes(currentTreeNode);
                        }
                    }
                    if (scrollToRequests.size() > 0) {
                        // clear the tree selection
                        arbilTreePanel.remoteCorpusTree.clearSelection();
                        arbilTreePanel.localCorpusTree.clearSelection();
                        arbilTreePanel.localDirectoryTree.clearSelection();
                        arbilTreePanel.favouritesTree.clearSelection();
                    }
                    for (DefaultMutableTreeNode currentScrollToNode : scrollToRequests) {
                        TreePath targetTreePath = new TreePath((currentScrollToNode).getPath());
                        if (targetTreePath != null) {
                            System.out.println("scrollToNode targetTreePath: " + targetTreePath);
                            ImdiTree imdiTree = getTreeForNode(currentScrollToNode);
                            imdiTree.expandPath(targetTreePath.getParentPath());
                            imdiTree.addSelectionPath(targetTreePath);
                            imdiTree.scrollPathToVisible(targetTreePath);
                        }
                    }
                    treeNodeSortQueueRunning = false;
                }
            }.start();
        }
    }

    private void sortChildNodes(DefaultMutableTreeNode parentNode, ArrayList<DefaultMutableTreeNode> currentChildren, Vector<DefaultMutableTreeNode> scrollToRequests) {
//        System.out.println("sortChildNodes: " + parentNode.getUserObject().toString());
        // resort the branch since the node name may have changed
        //boolean childNodesOrderChanged = false;
        DefaultTreeModel treeModel = getModelForNode(parentNode);
//        ArrayList<DefaultMutableTreeNode> sortedChildren = Collections.list(parentNode.children());
        Collections.sort(currentChildren, new ImdiTreeNodeSorter());
        // loop the child nodes comparing with the sorted array and move nodes only if required
        for (int childCounter = 0; childCounter < currentChildren.size(); childCounter++) {
//            System.out.println("sortChildNodes comparing: " + sortedChildren.get(childCounter));
//            System.out.println("sortChildNodes to: " + parentNode.getChildAt(childCounter));
            try {
                String leftUrlString = ((ImdiTreeObject) currentChildren.get(childCounter).getUserObject()).getUrlString();
                String rightUrlString = "";
                if (parentNode.getChildCount() > childCounter) {
                    rightUrlString = ((ImdiTreeObject) ((DefaultMutableTreeNode) parentNode.getChildAt(childCounter)).getUserObject()).getUrlString();
                }
//                System.out.println("leftUrlString: " + leftUrlString);
//                System.out.println("rightUrlString: " + rightUrlString);
                if (!leftUrlString.equals(rightUrlString)) {
//                    System.out.println("sortChildNodes moving: " + sortedChildren.get(childCounter) + " to " + childCounter);
//                    try {
//                        if (currentChildren.get(childCounter).getParent() != null) {
//                            System.out.println("removing");
//                            currentChildren.get(childCounter).removeFromParent();
//                            if (!currentChildren.contains(currentChildren.get(childCounter))) {
//                                treeModel.nodeStructureChanged(currentChildren.get(childCounter));
//                            }
//                        }
//                    } catch (Exception e) {
//                        GuiHelper.linorgBugCatcher.logError(e);
////                        System.out.println("sortChildNodes failed to move: " + sortedChildren.get(childCounter));
//                    }
//                    if (!((ImdiTreeObject) currentChildren.get(childCounter).getUserObject()).isLoading()) {
//                    System.out.println("inserting: " + currentChildren.get(childCounter).getUserObject() + " into: " + parentNode.getUserObject());
                    ImdiTreeObject currentImdiObject = ((ImdiTreeObject) currentChildren.get(childCounter).getUserObject());
                    currentImdiObject.removeContainer(currentChildren.get(childCounter));
                    if (currentChildren.get(childCounter).getParent() != null) {
                        treeModel.removeNodeFromParent(currentChildren.get(childCounter));
                    }
                    DefaultMutableTreeNode resortedTreeNode = new DefaultMutableTreeNode(currentImdiObject);
                    currentImdiObject.registerContainer(resortedTreeNode);
                    treeModel.insertNodeInto(resortedTreeNode, parentNode, childCounter);
//                    parentNode.insert(currentChildren.get(childCounter), childCounter);
                    //   childNodesOrderChanged = true;
//                    } else {
//                        parentNode.add(currentChildren.get(childCounter));
//                    }
//                treeModel.nodeStructureChanged(currentChildren.get(childCounter));
//                            treeModel.nodeChanged(itemNode);
//            treeModel.nodeChanged(missingTreeNode);
                } else {
                    treeModel.nodeChanged(currentChildren.get(childCounter));
                }
            } catch (Exception ex) {
                GuiHelper.linorgBugCatcher.logError(ex);
            }
        }
        ArrayList<DefaultMutableTreeNode> updatedChildren = Collections.list(parentNode.children());
        for (DefaultMutableTreeNode currentUpdatedChildNode : updatedChildren.toArray(new DefaultMutableTreeNode[]{})) {
            // update the string and icon etc for each node
            boolean childCanHaveChildren = ((ImdiTreeObject) currentUpdatedChildNode.getUserObject()).canHaveChildren();
            currentUpdatedChildNode.setAllowsChildren(childCanHaveChildren || currentUpdatedChildNode.getChildCount() > 0);
//            treeModel.nodeChanged(currentChildren.get(childCounter));
            if (((ImdiTreeObject) currentUpdatedChildNode.getUserObject()).scrollToRequested && !((ImdiTreeObject) currentUpdatedChildNode.getUserObject()).isLoading()) {
                scrollToRequests.add(currentUpdatedChildNode);
                ((ImdiTreeObject) currentUpdatedChildNode.getUserObject()).scrollToRequested = false;
            }
        }
        if (parentNode.getUserObject() instanceof ImdiTreeObject && ((ImdiTreeObject) parentNode.getUserObject()).scrollToRequested) {
            scrollToRequests.add(parentNode);
            ((ImdiTreeObject) parentNode.getUserObject()).scrollToRequested = false;
        }
//        if (childNodesOrderChanged) {
//            // refresh the child nodes
//            treeModel.nodeStructureChanged(parentNode);
//        }
        // update the string and icon etc for the parent node
        treeModel.nodeChanged(parentNode);

//        parentNode.removeAllChildren();
//        // add the child nodes in order
//        for (int childCounter = 0; childCounter < currentChildren.size(); childCounter++) {
//            parentNode.add(currentChildren.get(childCounter));
//            treeModel.nodeStructureChanged(parentNode);
//        }
    }

//    public void refreshDescendantNodes(DefaultMutableTreeNode itemNode) {
//        removeAndDetatchDescendantNodes(itemNode);
//        loadDescendantNodes(itemNode);
//    }
    private void removeAndDetatchDescendantNodes(DefaultMutableTreeNode itemNode) {
        System.out.println("removeDescendantNodes: " + itemNode);
        for (Enumeration<DefaultMutableTreeNode> childNodesEnum = itemNode.children(); childNodesEnum.hasMoreElements();) {
            removeAndDetatchDescendantNodes(childNodesEnum.nextElement());
        }
        Object childUserObject = itemNode.getUserObject();
        // get the imdi node
        if (childUserObject instanceof ImdiTreeObject) {
            //deregister the tree node in the imdinode
            ((ImdiTreeObject) childUserObject).removeContainer(itemNode);
        }
        DefaultTreeModel treeModel = getModelForNode(itemNode);
        if (itemNode.getParent() != null) {
            treeModel.removeNodeFromParent(itemNode);
        } else {
            treeModel.nodeStructureChanged(itemNode);
        }
    }

//    public void loadAndRefreshDescendantNodes(DefaultMutableTreeNode itemNode) {
//        System.out.println("refreshChildNodes: " + itemNode);
//        updateTreeNodeChildren(itemNode);
//        for (Enumeration<DefaultMutableTreeNode> childrenEnum = itemNode.children(); childrenEnum.hasMoreElements();) {
//            DefaultMutableTreeNode currentChildNode = childrenEnum.nextElement();
//            loadAndRefreshDescendantNodes(currentChildNode);
//        }
//    }
    public boolean locationsHaveBeenAdded() {
        boolean returnValue = false;
        System.out.println("locationsList.size: " + locationsList.size());
        for (String currentLocation : locationsList.toArray(new String[]{})) {
            if (ImdiTreeObject.isStringLocal(currentLocation)) {
                returnValue = true;
                break;
            }
        }
        return returnValue;
    }

    public void applyRootLocations() {
        System.out.println("applyRootLocations");
        addToSortQueue(remoteCorpusRootNode);
        addToSortQueue(localCorpusRootNode);
        addToSortQueue(localDirectoryRootNode);
        addToSortQueue(favouritesRootNode);
    }

    public void redrawTrees() {
        for (ImdiTree currentTree : arbilTreePanel.getTreeArray()) {
            currentTree.invalidate();
            currentTree.repaint();
        }
    }

//    public void reloadLocalCorpusTree(DefaultMutableTreeNode targetNode) {
//        // TODO: anything that calls this is adding to the tree in the wrong way (maybe with the exception of updating icons)
//        // TODO: replace with updateTreeNodeChildren()
//        localCorpusTreeModel.nodeStructureChanged(targetNode);
////         TODO: make sure the refreshed node is expanded 
////        localCorpusTree.expandPath(localCorpusTree.gettargetNode); 
//    }
//    public void reloadLocalCorpusTree() {
//        javax.swing.tree.TreePath currentSelection = localCorpusTree.getSelectionPath();
//        ((DefaultTreeModel) localCorpusTree.getModel()).reload();
////        localCorpusTree.expandPath(currentSelection); // this may be what is causing the tree draw issues
//    }
    public DefaultMutableTreeNode getLocalCorpusTreeSingleSelection() {
        System.out.println("localCorpusTree: " + arbilTreePanel.localCorpusTree);
        return (DefaultMutableTreeNode) arbilTreePanel.localCorpusTree.getSelectionPath().getLastPathComponent();
    }

    public void showLocationsDialog() {
        // TODO: it would be preferable to move all dialog creation and management into the linorgwindowmanager
        JDialog settingsjDialog = new JDialog(JOptionPane.getFrameForComponent(LinorgWindowManager.getSingleInstance().linorgFrame));
        settingsjDialog.setLocationRelativeTo(LinorgWindowManager.getSingleInstance().linorgFrame);
        JTable locationSettingsTable = new JTable(getLocationsTableModel()) {

            public TableCellRenderer getCellRenderer(int row, int column) {
                if (column == 0) {
                    ImdiTreeObject imdiObject = (ImdiTreeObject) getModel().getValueAt(row, column);
                    DefaultTableCellRenderer iconLabelRenderer = new DefaultTableCellRenderer();
                    iconLabelRenderer.setIcon(imdiObject.getIcon());
                    iconLabelRenderer.setText(imdiObject.toString());
                    return iconLabelRenderer;
                }
                return super.getCellRenderer(row, column);
            }
        };
        // set the icon column width
        locationSettingsTable.getColumnModel().getColumn(0).setPreferredWidth(1);
        locationSettingsTable.getColumnModel().getColumn(1).setPreferredWidth(1000);
        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(locationSettingsTable);
        settingsjDialog.add(scrollPane);
        settingsjDialog.setSize(400, 300);
        settingsjDialog.setVisible(true);
    }

    public javax.swing.table.DefaultTableModel getLocationsTableModel() {
        Object[][] tableObjectAray = new Object[locationsList.size()][2];
        Collections.sort(locationsList);
        Enumeration locationEnum = locationsList.elements();
        int rowCounter = 0;
        while (locationEnum.hasMoreElements()) {
            tableObjectAray[rowCounter][1] = locationEnum.nextElement();
            tableObjectAray[rowCounter][0] = GuiHelper.imdiLoader.getImdiObject(null, tableObjectAray[rowCounter][1].toString());
            rowCounter++;
        }
        return new javax.swing.table.DefaultTableModel(tableObjectAray, new String[]{"", "Location"}) {

            Class[] types = new Class[]{
                ImdiTreeObject.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        };
    }

//    public ImdiTreeObject addImdiChildNode(ImdiTreeObject imdiTreeObject, String nodeType, String targetXmlPath, String nodeTypeDisplayName, String resourcePath, String mimeType) {
//        ImdiTreeObject addedImdi = null;
//        String addedNodeUrl = null;
//        if (nodeType != null) {
////            String targetXmlPath = imdiTreeObject.getURL().getRef();
//            if (imdiTreeObject.isImdi() && !imdiTreeObject.fileNotFound) {// if url is null (not an imdi) then the node is unattached
//                if (imdiTreeObject.isImdiChild()) {
//                    imdiTreeObject = imdiTreeObject.getParentDomNode();
//                }
//                addedNodeUrl = imdiTreeObject.addChildNode(nodeType, resourcePath, mimeType);
//            } else {
//                addedNodeUrl = new ImdiTreeObject("temp root node", LinorgSessionStorage.getSingleInstance().getSaveLocation("unattachedcorpus")).addChildNode(nodeType, null, null);
//                addLocation(addedNodeUrl);
//                applyRootLocations();
//            }
//        }
//        if (addedNodeUrl != null) {
//            System.out.println("addedNodeUrl: " + addedNodeUrl);
//            addedImdi = GuiHelper.imdiLoader.getImdiObject(null, imdiTreeObject.addChildNode(nodeType, resourcePath, mimeType));
//        }
//        return addedImdi;
//    }

//    public void getImdiChildNodes(DefaultMutableTreeNode itemNode) {
//        System.out.println("getImdiChildNodes:" + itemNode.getUserObject());
////        DefaultTreeModel treeModel = getModelForNode(itemNode);
//        updateTreeNodeChildren(itemNode);
//        // TODO check that child nodes are always shown correctly and correclty synced with the imdinodes
//        if (itemNode.getChildCount() == 0) {
//            // add "loading" node
//            itemNode.setAllowsChildren(true);
//            itemNode.add(new DefaultMutableTreeNode(new JLabel("adding...", ImdiIcons.loadingIcon, JLabel.CENTER), false));
////        }
//            if (ImdiTreeObject.isImdiNode(itemNode.getUserObject())) {
//                ImdiTreeObject imdiTreeObject = (ImdiTreeObject) itemNode.getUserObject();
//                if (!imdiTreeObject.isImdi() && !imdiTreeObject.isDirectory()) {
//                    System.out.println("file to be opened");
//                } else {
//                    //ImdiHelper.ImdiTreeObject[] childNodes = imdiTreeObject.getChildren(imdiFieldViews, imdiFieldViews.getCurrentFieldArray());
//                    ImdiTreeObject[] childNodes = imdiTreeObject.loadChildNodes();
//                    Arrays.sort(childNodes);
//                    // remove the loading node
//                    removeChildNodes(itemNode);
////                    for (int childCount = 0; childCount < childNodes.length; childCount++) {
////                        System.out.println("Adding tree node: " + childNodes[childCount]);
////                        DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(childNodes[childCount]);
////                        treeNode.setAllowsChildren(childNodes[childCount].canHaveChildren() || childNodes[childCount].isDirectory());
////                        childNodes[childCount].registerContainer(treeNode);
////                        itemNode.add(treeNode);
////                    }
//                }
//            }
//        }
//    }
    public void removeSelectedLocation(DefaultMutableTreeNode selectedTreeNode) {
        if (selectedTreeNode == null) {
            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("No node selected", "Remove Link");
        } else {
            if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(LinorgWindowManager.getSingleInstance().linorgFrame, "Remove '" + selectedTreeNode + "' from list?", "Remove Link", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE)) {
                removeLocation(selectedTreeNode.getUserObject());
                applyRootLocations();
            }
        }
    }

    public void deleteNode(Object sourceObject) {
        System.out.println("deleteNode: " + sourceObject);
        DefaultMutableTreeNode selectedTreeNode = null;
        DefaultMutableTreeNode parentTreeNode = null;
        if (sourceObject == arbilTreePanel.localCorpusTree) {
            javax.swing.tree.TreePath currentNodePaths[] = ((ImdiTree) sourceObject).getSelectionPaths();
            if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(LinorgWindowManager.getSingleInstance().linorgFrame, "Delete " + currentNodePaths.length + " nodes?", "Delete", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE)) {
                Vector<ImdiTreeObject> imdiNodesToRemove = new Vector<ImdiTreeObject>();
                Hashtable<ImdiTreeObject, Vector> imdiNodesDeleteList = new Hashtable<ImdiTreeObject, Vector>();
                Hashtable<ImdiTreeObject, Vector> imdiChildNodeDeleteList = new Hashtable<ImdiTreeObject, Vector>();
                for (javax.swing.tree.TreePath currentNodePath : currentNodePaths) {
                    if (currentNodePath != null) {
                        selectedTreeNode = (DefaultMutableTreeNode) currentNodePath.getLastPathComponent();
                        Object userObject = selectedTreeNode.getUserObject();
                        System.out.println("trying to delete: " + userObject);
                        if (currentNodePath.getPath().length == 2) {
                            System.out.println("removing by location");
                            removeSelectedLocation(selectedTreeNode);
                        } else {
                            System.out.println("deleting from parent");
                            parentTreeNode = (DefaultMutableTreeNode) selectedTreeNode.getParent();
                            if (parentTreeNode != null) {
                                System.out.println("found parent to remove from");
                                ImdiTreeObject parentImdiNode = (ImdiTreeObject) parentTreeNode.getUserObject();
                                ImdiTreeObject childImdiNode = (ImdiTreeObject) selectedTreeNode.getUserObject();
                                if (childImdiNode.isImdiChild()) {
                                    // there is a risk of the later deleted nodes being outof sync with the xml, so we add them all to a list and delete all at once before the node is reloaded
                                    if (!imdiChildNodeDeleteList.containsKey(childImdiNode.getParentDomNode())) {
                                        imdiChildNodeDeleteList.put(childImdiNode.getParentDomNode(), new Vector());
                                    }
                                    imdiChildNodeDeleteList.get(childImdiNode.getParentDomNode()).add(childImdiNode.xmlNodeId);
                                } else {
                                    // add the parent and the child node to the deletelist
                                    if (!imdiNodesDeleteList.containsKey(parentImdiNode)) {
                                        imdiNodesDeleteList.put(parentImdiNode, new Vector());
                                    }
                                    imdiNodesDeleteList.get(parentImdiNode).add(childImdiNode);
                                }
                                // remove the deleted node from the favourites list if it is an imdichild node
//                            if (userObject instanceof ImdiTreeObject) {
//                                if (((ImdiTreeObject) userObject).isImdiChild()){
//                                LinorgTemplates.getSingleInstance().removeFromFavourites(((ImdiTreeObject) userObject).getUrlString());
//                                }
//                            }
                            }
                        }
                        // make a list of all child nodes so that they can be removed from any tables etc
                        imdiNodesToRemove.add((ImdiTreeObject) userObject);
                        ((ImdiTreeObject) userObject).getAllChildren(imdiNodesToRemove);
                    }
                }
                for (ImdiTreeObject currentParent : imdiChildNodeDeleteList.keySet()) {
                    System.out.println("deleting by child xml id link");
                    // TODO: There is an issue when deleting child nodes that the remaining nodes xml path (x) will be incorrect as will the xmlnode id hence the node in a table may be incorrect after a delete
                    currentParent.deleteFromDomViaId(((Vector<String>) imdiChildNodeDeleteList.get(currentParent)).toArray(new String[]{}));
                }
                for (ImdiTreeObject currentParent : imdiNodesDeleteList.keySet()) {
                    System.out.println("deleting by corpus link");
                    currentParent.deleteCorpusLink(((Vector<ImdiTreeObject>) imdiNodesDeleteList.get(currentParent)).toArray(new ImdiTreeObject[]{}));
                }
                for (Enumeration<ImdiTreeObject> deletedNodesEnum = imdiNodesToRemove.elements(); deletedNodesEnum.hasMoreElements();) {
                    // remove the deleted node from all tables
                    ImdiTreeObject currentDeletedNode = deletedNodesEnum.nextElement();
                    Vector tempVector = new Vector();
                    tempVector.add(currentDeletedNode);
                    for (Object currentContainer : currentDeletedNode.getRegisteredContainers()) {
                        if (currentContainer instanceof ImdiTableModel) {
                            ((ImdiTableModel) currentContainer).removeImdiObjects(tempVector.elements());
                        }
                    }
                }
            }
        } else {
            System.out.println("cannot delete from this tree");
        }
    }

    public void jumpToSelectionInTree(boolean silent, ImdiTreeObject cellImdiNode) {
        System.out.println("jumpToSelectionInTree: " + cellImdiNode);
        if (cellImdiNode != null) {
            cellImdiNode.scrollToRequested = true;
//            cellImdiNode.clearIcon();
            updateTreeNodeChildren(cellImdiNode);
        } else {
            if (!silent) {
                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("The selected cell has no value or is not associated with a node in the tree", "Jump to in Tree");
            }
        }
    }
}
