package nl.mpi.arbil;

import nl.mpi.arbil.data.ImdiTreeObject;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import nl.mpi.arbil.data.ImdiLoader;

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
    public ImdiTreeObject[] remoteCorpusNodes = new ImdiTreeObject[]{};
    public ImdiTreeObject[] localCorpusNodes = new ImdiTreeObject[]{};
    public ImdiTreeObject[] localFileNodes = new ImdiTreeObject[]{};
    public ImdiTreeObject[] favouriteNodes = new ImdiTreeObject[]{};
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
        // load any favourites from the previous file format
        LinorgFavourites.getSingleInstance().loadOldFormatFavourites();

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
        HashSet<ImdiTreeObject> remoteCorpusNodesSet = new HashSet<ImdiTreeObject>();
        remoteCorpusNodesSet.addAll(Arrays.asList(remoteCorpusNodes));
        remoteCorpusNodesSet.add(ImdiLoader.getSingleInstance().getImdiObject(null, "http://corpus1.mpi.nl/IMDI/metadata/IMDI.imdi"));
        remoteCorpusNodesSet.add(ImdiLoader.getSingleInstance().getImdiObject(null, "http://corpus1.mpi.nl/qfs1/media-archive/Corpusstructure/MPI.imdi"));
//        remoteCorpusNodesSet.add(ImdiLoader.getSingleInstance().getImdiObject(null, "http://corpus1.mpi.nl/qfs1/media-archive/dobes_data/ChintangPuma/Chintang/Conversation/Metadata/phidang_talk.imdi"));
//        remoteCorpusNodesSet.add(ImdiLoader.getSingleInstance().getImdiObject(null, "http://corpus1.mpi.nl/qfs1/media-archive/silang_data/Corpusstructure/1-03.imdi"));
//        remoteCorpusNodesSet.add(ImdiLoader.getSingleInstance().getImdiObject(null, "http://corpus1.mpi.nl/qfs1/media-archive/dobes_data/ECLING/Corpusstructure/ECLING.imdi"));
//        remoteCorpusNodesSet.add(ImdiLoader.getSingleInstance().getImdiObject(null, "http://corpus1.mpi.nl/qfs1/media-archive/dobes_data/Center/Corpusstructure/center.imdi"));
//        remoteCorpusNodesSet.add(ImdiLoader.getSingleInstance().getImdiObject(null, "http://corpus1.mpi.nl/qfs1/media-archive/dobes_data/Teop/Corpusstructure/1.imdi"));
//        remoteCorpusNodesSet.add(ImdiLoader.getSingleInstance().getImdiObject(null, "http://corpus1.mpi.nl/qfs1/media-archive/dobes_data/Waimaa/Corpusstructure/1.imdi"));
//        remoteCorpusNodesSet.add(ImdiLoader.getSingleInstance().getImdiObject(null, "http://corpus1.mpi.nl/qfs1/media-archive/dobes_data/Beaver/Corpusstructure/Beaver.imdi"));
        remoteCorpusNodes = remoteCorpusNodesSet.toArray(new ImdiTreeObject[]{});
        return remoteCorpusNodesSet.size();
    }

    public void saveLocations(ImdiTreeObject[] nodesToAdd, ImdiTreeObject[] nodesToRemove) {
        try {
            Vector<String> locationsList = new Vector<String>();
            for (ImdiTreeObject[] currentTreeArray : new ImdiTreeObject[][]{remoteCorpusNodes, localCorpusNodes, localFileNodes, favouriteNodes}) {
                for (ImdiTreeObject currentLocation : currentTreeArray) {
                    locationsList.add(currentLocation.getUrlString());
                }
            }
            if (nodesToAdd != null) {
                for (ImdiTreeObject currentAddable : nodesToAdd) {
                    locationsList.add(currentAddable.getUrlString());
                }
            }

            if (nodesToRemove != null) {
                for (ImdiTreeObject currentRemoveable : nodesToRemove) {
                    locationsList.removeElement(currentRemoveable.getUrlString());
                }
            }
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
            Vector<String> locationsList = (Vector<String>) LinorgSessionStorage.getSingleInstance().loadObject("locationsList");
//            System.out.println("loaded locationsList.size: " + locationsList.size());
            // remoteCorpusNodes, localCorpusNodes, localFileNodes, favouriteNodes

            Vector<ImdiTreeObject> remoteCorpusNodesVector = new Vector<ImdiTreeObject>();
            Vector<ImdiTreeObject> localCorpusNodesVector = new Vector<ImdiTreeObject>();
            Vector<ImdiTreeObject> localFileNodesVector = new Vector<ImdiTreeObject>();
            Vector<ImdiTreeObject> favouriteNodesVector = new Vector<ImdiTreeObject>();

            // this also removes all locations and replaces them with normalised paths
            for (Enumeration<String> locationEnum = locationsList.elements(); locationEnum.hasMoreElements();) {

                String currentLocation = locationEnum.nextElement();
                ImdiTreeObject currentTreeObject = ImdiLoader.getSingleInstance().getImdiObject(null, currentLocation);
                if (currentTreeObject.isLocal()) {
                    if (currentTreeObject.isFavorite()) {
                        favouriteNodesVector.add(currentTreeObject);
                    } else if (LinorgSessionStorage.getSingleInstance().pathIsInsideCache(currentTreeObject.getFile())) {
                        localCorpusNodesVector.add(currentTreeObject);
                    } else {
                        localFileNodesVector.add(currentTreeObject);
                    }
                } else {
                    remoteCorpusNodesVector.add(currentTreeObject);
                }
            }
            remoteCorpusNodes = remoteCorpusNodesVector.toArray(new ImdiTreeObject[]{});
            localCorpusNodes = localCorpusNodesVector.toArray(new ImdiTreeObject[]{});
            localFileNodes = localFileNodesVector.toArray(new ImdiTreeObject[]{});
            favouriteNodes = favouriteNodesVector.toArray(new ImdiTreeObject[]{});
        } catch (Exception ex) {
            System.out.println("load locationsList failed: " + ex.getMessage());
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
        reloadNodesInTree(localDirectoryRootNode);
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
        ImdiTreeObject addedLocationObject = ImdiLoader.getSingleInstance().getImdiObject(null, addedLocation);
        if (addedLocationObject != null) {
            saveLocations(new ImdiTreeObject[]{addedLocationObject}, null);
            loadLocationsList();
            return true;
        }
        return false;
    }

    public void removeLocation(ImdiTreeObject removeObject) {
        if (removeObject != null) {
            saveLocations(null, new ImdiTreeObject[]{removeObject});
            loadLocationsList();
        }
    }

    public void removeLocation(String removeLocation) {
        System.out.println("removeLocation: " + removeLocation);
        removeLocation(ImdiLoader.getSingleInstance().getImdiObject(null, removeLocation));
    }

    private void reloadNodesInTree(DefaultMutableTreeNode parentTreeNode) {
        // this will reload all nodes in a tree but not create any new child nodes
        for (Enumeration<DefaultMutableTreeNode> childNodesEnum = parentTreeNode.children(); childNodesEnum.hasMoreElements();) {
            reloadNodesInTree(childNodesEnum.nextElement());
        }
        if (parentTreeNode.getUserObject() instanceof ImdiTreeObject) {
            if (((ImdiTreeObject) parentTreeNode.getUserObject()).imdiDataLoaded) {
                ((ImdiTreeObject) parentTreeNode.getUserObject()).reloadNode();
            }
        }
    }

    // this will load all imdi child nodes into the tree, and in the case of a session it will load all the imdi childnodes, the while the other updateTreeNodeChildren methods will not
    public void updateTreeNodeChildren(ImdiTreeObject parentImdiNode) {
//        System.out.println("updateTreeNodeChildren ImdiTreeObject: " + parentImdiNode);
        for (Object currentContainer : parentImdiNode.getRegisteredContainers()) {
            if (currentContainer instanceof DefaultMutableTreeNode) {
//                System.out.println("updateTreeNodeChildren currentContainer: " + parentImdiNode + " : " + currentContainer.hashCode());
                addToSortQueue((DefaultMutableTreeNode) currentContainer);
            }
        }
        for (ImdiTreeObject currentChild : parentImdiNode.getChildArray()) {
            // recursively load the tree nodes of the child nodes            
            if (currentChild.isImdiChild()) {
                updateTreeNodeChildren(currentChild);
            }
        }
    }

    // check that all child nodes are attached and sorted, removing any extranious nodes found
    private void updateTreeNodeChildren(DefaultMutableTreeNode parentNode, ImdiTreeObject[] childNodes, Vector<DefaultMutableTreeNode> scrollToRequests) {
//        System.out.println("updateTreeNodeChildren");
        ImdiTree currentTree = getTreeForNode(parentNode);
        DefaultTreeModel treeModel = getModelForNode(parentNode);
//        if (parentNode.getUserObject() instanceof ImdiTreeObject && parentNode.getChildCount() > 0) {
//            // leave any realoading nodes alone if they already have child nodes in the tree
//            if (((ImdiTreeObject) parentNode.getUserObject()).getParentDomNode().isLoading()) {
//                treeModel.nodeChanged(parentNode);
//                // since we have ingnored the loading node we must put it back on the list so that it gets sorted when it has loaded
//                addToSortQueue(parentNode);
//                return;
//            }
//        }
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
            boolean nodeUrlFoundInchildArray = false;
            for (ImdiTreeObject childArrayObject : childNodes) {
                if (childArrayObject.getUrlString().equals(childImdiObject.getUrlString())) {
                    nodeUrlFoundInchildArray = true;
                }
            }
            // binary search does not work correctly in this case
            //if (Arrays.binarySearch(childNodes, childImdiObject) < 0) {
            if (!nodeUrlFoundInchildArray) {
                nodesToRemove.add(currentChildNode);
                System.out.println("setting for removal from tree: " + childImdiObject.getUrlString());
//                sortedChildren.remove(currentChildNode);
            }
        }
        while (!nodesToRemove.isEmpty()) {
//            TODO: are the nodes being removed when an actor is deleted ???
            DefaultMutableTreeNode currentChildNode = nodesToRemove.remove(0);
            //System.out.println("nodesToRemove: " + currentChildNode);
            //System.out.println("nodesToRemove: " + currentChildNode.getUserObject());
//            if (currentChildNode.getParent() != null) {
//                treeModel.removeNodeFromParent(currentChildNode);
//            }
            removeAndDetatchDescendantNodes(currentChildNode);
        }
        Arrays.sort(childNodes);
        parentNode.setAllowsChildren(childNodes.length > 0);
//        if (treeModel.parentNode.)
//        if (!parentNode.isLeaf()) {
        boolean containsSubNodes = false;
        if (parentNode.getUserObject() instanceof ImdiTreeObject) {
            ImdiTreeObject childImdiObject = (ImdiTreeObject) parentNode.getUserObject();
            if (childImdiObject.isImdiChild()) {
                containsSubNodes = true;
            } else if (childNodes.length > 0) {
                containsSubNodes = childNodes[0].isImdiChild();
            }
        }
        boolean parentExpanded = false;
//        if (parentNode.getParent() != null) {
//            if (currentTree.isExpanded(new TreePath((((DefaultMutableTreeNode) parentNode.getParent())).getPath()))) {
//                parentExpanded = true;
//            }
        if (currentTree.isExpanded(
                new TreePath((parentNode).getPath()))) {
            parentExpanded = true;
        }
        if (containsSubNodes || parentExpanded || parentNode.getChildCount() > 0) {
            sortChildNodes(parentNode, childNodes, scrollToRequests);
        }
//        System.out.println("setAllowsChildren: " + parentNode.getAllowsChildren() + ", " + parentNode.getChildCount() + ", " + parentNode.toString());
//        if (parentNode.getAllowsChildren() && 0 == parentNode.getChildCount()) {
//            System.out.println("oops");
//        }
    }

    synchronized public void addToSortQueue(DefaultMutableTreeNode addToQueueTreeNode) {
        if (!treeNodeSortQueue.contains(addToQueueTreeNode)) {
//            System.out.println("requestSort: " + currentTreeNode.getUserObject().toString());
            treeNodeSortQueue.add(addToQueueTreeNode);
        }
        if (!treeNodeSortQueueRunning) {
            treeNodeSortQueueRunning = true;
            new Thread() {

                @Override
                public void run() {
                    Vector<DefaultMutableTreeNode> scrollToRequests = new Vector<DefaultMutableTreeNode>();
//                    setPriority(Thread.NORM_PRIORITY - 1);
                    while (treeNodeSortQueue.size() > 0) {
                        DefaultMutableTreeNode currentTreeNode = treeNodeSortQueue.remove(treeNodeSortQueue.size() - 1);
                        if (currentTreeNode != null) {
//                            Vector<String> childUrls = new Vector();
                            //DefaultMutableTreeNode parentTreeNode = (DefaultMutableTreeNode) itemNode.getParent();
                            Object parentObject = currentTreeNode.getUserObject();
                            if (parentObject instanceof ImdiTreeObject) {
                                ImdiTreeObject parentImdiObject = (ImdiTreeObject) parentObject;
                                // make the list of child urls
//                                for (ImdiTreeObject childImdiObject : parentImdiObject.getChildArray()) {
//                                    boolean showChild = true;
//                                    if (!showHiddenFilesInTree && childImdiObject.isLocal() && childImdiObject.getFile().isHidden()) {
//                                        showChild = false;
//                                    }
//                                    if (showChild) {
//                                        childUrls.add(childImdiObject.getUrlString());
//                                    }
////                                    System.out.println("adding child to update list: " + childImdiObject.getUrlString());
//                                }
                                if (parentImdiObject.isLoading()) {
                                    // since we have ignored the loading node we must put it back on the list so that it gets sorted when it has loaded
                                    treeNodeSortQueue.add(currentTreeNode);
                                } else {
                                    updateTreeNodeChildren(currentTreeNode, parentImdiObject.getChildArray(), scrollToRequests);
                                }
                            } else {
                                if (currentTreeNode == remoteCorpusRootNode) {
                                    updateTreeNodeChildren(remoteCorpusRootNode, remoteCorpusNodes, scrollToRequests);
                                }
                                if (currentTreeNode == localCorpusRootNode) {
                                    updateTreeNodeChildren(localCorpusRootNode, localCorpusNodes, scrollToRequests);
                                }
                                if (currentTreeNode == localDirectoryRootNode) {
                                    updateTreeNodeChildren(localDirectoryRootNode, localFileNodes, scrollToRequests);
                                }
                                if (currentTreeNode == favouritesRootNode) {
                                    updateTreeNodeChildren(favouritesRootNode, favouriteNodes, scrollToRequests);
                                }
                            }
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

    private void sortChildNodes(DefaultMutableTreeNode parentNode, ImdiTreeObject[] sortedChildren, Vector<DefaultMutableTreeNode> scrollToRequests) {
//        System.out.println("sortChildNodes: " + parentNode.getUserObject().toString());
        // resort the branch since the node name may have changed
        //boolean childNodesOrderChanged = false;
        DefaultTreeModel treeModel = getModelForNode(parentNode);
//        ArrayList<DefaultMutableTreeNode> sortedChildren = Collections.list(parentNode.children());
//        Collections.sort(currentChildren, new ImdiTreeNodeSorter());
        // loop the child nodes comparing with the sorted array and move nodes only if required
        for (int childCounter = 0; childCounter < sortedChildren.length; childCounter++) {
//            System.out.println("sortChildNodes comparing: " + sortedChildren.get(childCounter));
//            System.out.println("sortChildNodes to: " + parentNode.getChildAt(childCounter));
//            try {
            String leftUrlString = sortedChildren[childCounter].getUrlString();
                String rightUrlString = "";
            DefaultMutableTreeNode currentTreeNode = null;
                if (parentNode.getChildCount() > childCounter) {
                currentTreeNode = (DefaultMutableTreeNode) parentNode.getChildAt(childCounter);
                rightUrlString = ((ImdiTreeObject) currentTreeNode.getUserObject()).getUrlString();
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
//                ImdiTreeObject currentImdiObject = ((ImdiTreeObject) currentChildren.get(childCounter).getUserObject());
                sortedChildren[childCounter].removeContainer(currentTreeNode);
                if (childCounter < parentNode.getChildCount() && parentNode.getChildAt(childCounter).getParent() != null) {
                    treeModel.removeNodeFromParent(currentTreeNode);
                    }
                DefaultMutableTreeNode resortedTreeNode = new DefaultMutableTreeNode(sortedChildren[childCounter]);
                sortedChildren[childCounter].registerContainer(resortedTreeNode);
                if (parentNode.getChildCount() > childCounter) {
                    treeModel.insertNodeInto(resortedTreeNode, parentNode, childCounter);
                } else {
                    treeModel.insertNodeInto(resortedTreeNode, parentNode, parentNode.getChildCount());
                }
//                    parentNode.insert(currentChildren.get(childCounter), childCounter);
                //   childNodesOrderChanged = true;
//                    } else {
//                        parentNode.add(currentChildren.get(childCounter));
//                    }
//                treeModel.nodeStructureChanged(currentChildren.get(childCounter));
//                            treeModel.nodeChanged(itemNode);
//            treeModel.nodeChanged(missingTreeNode);
                } else {
                treeModel.nodeChanged(currentTreeNode);
                }
//            } catch (Exception ex) {
//                GuiHelper.linorgBugCatcher.logError(ex);
            }
        while (parentNode.getChildCount() > sortedChildren.length) {
            DefaultMutableTreeNode excessTreeNode = (DefaultMutableTreeNode) parentNode.getChildAt(parentNode.getChildCount() - 1);
            ((ImdiTreeObject) excessTreeNode.getUserObject()).removeContainer(excessTreeNode);
            treeModel.removeNodeFromParent(excessTreeNode);
        }
        ArrayList<DefaultMutableTreeNode> updatedChildren = Collections.list(parentNode.children());
        for (DefaultMutableTreeNode currentUpdatedChildNode : updatedChildren.toArray(new DefaultMutableTreeNode[]{})) {
            // update the string and icon etc for each node
            boolean childCanHaveChildren = ((ImdiTreeObject) currentUpdatedChildNode.getUserObject()).canHaveChildren();
            currentUpdatedChildNode.setAllowsChildren(childCanHaveChildren);
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
//        if (parentNode.getUserObject() instanceof ImdiTreeObject) {
//            boolean childCanHaveChildren = ((ImdiTreeObject) parentNode.getUserObject()).canHaveChildren();
//            parentNode.setAllowsChildren(childCanHaveChildren || parentNode.getChildCount() > 0);
//            System.out.println("setAllowsChildren: " + parentNode.getAllowsChildren() + ", " + parentNode.getChildCount() + ", " + parentNode.toString());
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
//        boolean returnValue = false;
//        System.out.println("locationsList.size: " + locationsList.size());
//        for (String currentLocation : locationsList.toArray(new String[]{})) {
//            if (ImdiTreeObject.isStringLocal(currentLocation)) {
//                returnValue = true;
//                break;
//            }
//        }
//        return returnValue;
        return localCorpusNodes.length > 0;
    }

    public void applyRootLocations() {
        System.out.println("applyRootLocations");
        addToSortQueue(remoteCorpusRootNode);
        addToSortQueue(localCorpusRootNode);
        addToSortQueue(localDirectoryRootNode);
        addToSortQueue(favouritesRootNode);
    }

//    public void redrawTrees() {
//        for (ImdiTree currentTree : arbilTreePanel.getTreeArray()) {
//            currentTree.invalidate();
//            currentTree.repaint();
//        }
//    }

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

//    public void showLocationsDialog() {
//        // TODO: it would be preferable to move all dialog creation and management into the linorgwindowmanager
//        JDialog settingsjDialog = new JDialog(JOptionPane.getFrameForComponent(LinorgWindowManager.getSingleInstance().linorgFrame));
//        settingsjDialog.setLocationRelativeTo(LinorgWindowManager.getSingleInstance().linorgFrame);
//        JTable locationSettingsTable = new JTable(getLocationsTableModel()) {
//
//            public TableCellRenderer getCellRenderer(int row, int column) {
//                if (column == 0) {
//                    ImdiTreeObject imdiObject = (ImdiTreeObject) getModel().getValueAt(row, column);
//                    DefaultTableCellRenderer iconLabelRenderer = new DefaultTableCellRenderer();
//                    iconLabelRenderer.setIcon(imdiObject.getIcon());
//                    iconLabelRenderer.setText(imdiObject.toString());
//                    return iconLabelRenderer;
//                }
//                return super.getCellRenderer(row, column);
//            }
//        };
//        // set the icon column width
//        locationSettingsTable.getColumnModel().getColumn(0).setPreferredWidth(1);
//        locationSettingsTable.getColumnModel().getColumn(1).setPreferredWidth(1000);
//        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(locationSettingsTable);
//        settingsjDialog.add(scrollPane);
//        settingsjDialog.setSize(400, 300);
//        settingsjDialog.setVisible(true);
//    }
//    public javax.swing.table.DefaultTableModel getLocationsTableModel() {
//        Object[][] tableObjectAray = new Object[locationsList.size()][2];
//        Collections.sort(locationsList);
//        Enumeration locationEnum = locationsList.elements();
//        int rowCounter = 0;
//        while (locationEnum.hasMoreElements()) {
//            tableObjectAray[rowCounter][1] = locationEnum.nextElement();
//            tableObjectAray[rowCounter][0] = GuiHelper.imdiLoader.getImdiObject(null, tableObjectAray[rowCounter][1].toString());
//            rowCounter++;
//        }
//        return new javax.swing.table.DefaultTableModel(tableObjectAray, new String[]{"", "Location"}) {
//
//            Class[] types = new Class[]{
//                ImdiTreeObject.class, java.lang.String.class
//            };
//
//            public Class getColumnClass(int columnIndex) {
//                return types[columnIndex];
//            }
//        };
//    }
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
                            removeLocation((ImdiTreeObject) selectedTreeNode.getUserObject());
                            applyRootLocations();
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
//                for (Enumeration<ImdiTreeObject> deletedNodesEnum = imdiNodesToRemove.elements(); deletedNodesEnum.hasMoreElements();) {
//                    // remove the deleted node from all tables
//                    ImdiTreeObject currentDeletedNode = deletedNodesEnum.nextElement();
//                    for (Object currentContainer : currentDeletedNode.getRegisteredContainers()) {
//                        // TODO: this can probably be removed since it is now done in the reloading process
//                        if (currentContainer instanceof ImdiTableModel) {
//                            ((ImdiTableModel) currentContainer).removeImdiObjects(new ImdiTreeObject[]{currentDeletedNode});
//                        }
//                    }
//                }
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
