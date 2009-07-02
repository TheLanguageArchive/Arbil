package mpi.linorg;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

/**
 * Document   : TreeHelper
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
public class TreeHelper {

    public DefaultTreeModel localCorpusTreeModel;
    public DefaultTreeModel remoteCorpusTreeModel;
    public DefaultTreeModel localDirectoryTreeModel;
    private DefaultMutableTreeNode localCorpusRootNode;
    private DefaultMutableTreeNode remoteCorpusRootNode;
    private DefaultMutableTreeNode localDirectoryRootNode;
    public ImdiTree localCorpusTree;
    public ImdiTree localDirectoryTree;
    public ImdiTree remoteCorpusTree;
    private Vector<String> locationsList; // this is the list of locations seen in the tree and the location settings
    static private TreeHelper singleInstance = null;
    static public boolean trackTableSelection = false;
    Vector<DefaultMutableTreeNode> treeNodeSortQueue = new Vector<DefaultMutableTreeNode>(); // used in the tree node sort thread
    boolean treeNodeSortQueueRunning = false; // used in the tree node sort thread

    static synchronized public TreeHelper getSingleInstance() {
        System.out.println("TreeHelper getSingleInstance");
        if (singleInstance == null) {
            singleInstance = new TreeHelper();
        }
        return singleInstance;
    }

    private TreeHelper() {
        localCorpusRootNode = new DefaultMutableTreeNode();
        remoteCorpusRootNode = new DefaultMutableTreeNode();
        localDirectoryRootNode = new DefaultMutableTreeNode();

        localCorpusTreeModel = new DefaultTreeModel(localCorpusRootNode, true);
        remoteCorpusTreeModel = new DefaultTreeModel(remoteCorpusRootNode, true);
        localDirectoryTreeModel = new DefaultTreeModel(localDirectoryRootNode, true);
        loadLocationsList();
    }

    public DefaultTreeModel getModelForNode(DefaultMutableTreeNode nodeToTest) {
        if (nodeToTest.getRoot().equals(remoteCorpusRootNode)) {
            return remoteCorpusTreeModel;
        }
        if (nodeToTest.getRoot().equals(localCorpusRootNode)) {
            return localCorpusTreeModel;
        }
        return localDirectoryTreeModel;
    }

    public boolean componentIsTheLocalCorpusTree(Component componentToTest) {
        return componentToTest.equals(localCorpusTree);
    //return localCorpusTree.getName().equals(componentToTest.getName());
    }

    public void setTrees(ImdiTree tempRemoteCorpusTree, ImdiTree tempLocalCorpusTree, ImdiTree tempLocalDirectoryTree) {
        remoteCorpusRootNode.setUserObject(new JLabel("Remote Corpus", ImdiIcons.getSingleInstance().serverIcon, JLabel.LEFT));
        localCorpusRootNode.setUserObject(new JLabel("Local Corpus", ImdiIcons.getSingleInstance().directoryIcon, JLabel.LEFT));
        localDirectoryRootNode.setUserObject(new JLabel("Working Directories", ImdiIcons.getSingleInstance().computerIcon, JLabel.LEFT));

        remoteCorpusTree = tempRemoteCorpusTree;
        localCorpusTree = tempLocalCorpusTree;
        localDirectoryTree = tempLocalDirectoryTree;

//        remoteCorpusTree.setLargeModel(true); // the huge tree node issue has been seen while setLargeModel is set to true
//        localCorpusTree.setLargeModel(true);
//        localDirectoryTree.setLargeModel(true);

        remoteCorpusTree.setName("RemoteCorpusTree");
        localCorpusTree.setName("LocalCorpusTree");
        localDirectoryTree.setName("LocalDirectoryTree");

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
            GuiHelper.linorgSessionStorage.saveObject(locationsList, "locationsList");
            System.out.println("saved locationsList");
        } catch (Exception ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
//            System.out.println("save locationsList exception: " + ex.getMessage());
        }
    }

    public void loadLocationsList() {
        try {
            System.out.println("loading locationsList");
            locationsList = (Vector<String>) GuiHelper.linorgSessionStorage.loadObject("locationsList");
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

    // this will load all imdi child nodes into the tree, and in the case of a session it will load all the imdi childnodes, the while the other updateTreeNodeChildren methods will not
    public void updateTreeNodeChildren(ImdiTreeObject parentImdiNode) {
//        System.out.println("updateTreeNodeChildren ImdiTreeObject: " + parentImdiNode);
        for (Object currentContainer : parentImdiNode.getRegisteredContainers()) {
            if (currentContainer instanceof DefaultMutableTreeNode) {
//                System.out.println("updateTreeNodeChildren currentContainer: " + parentImdiNode + " : " + currentContainer.hashCode());
                updateTreeNodeChildren((DefaultMutableTreeNode) currentContainer);
            }
        }
        if (parentImdiNode.isSession() || parentImdiNode.isImdiChild()) {
            // recursively load the tree nodes of the children
            for (Enumeration<ImdiTreeObject> childNodesEnum = parentImdiNode.getChildEnum(); childNodesEnum.hasMoreElements();) {
                updateTreeNodeChildren(childNodesEnum.nextElement());
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

    private void updateTreeNodeChildren(DefaultMutableTreeNode parentNode, Vector<String> childUrls) {
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
        sortChildNodes(parentNode, sortedChildren);
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
            System.out.println("root location: " + currentImdiObject.getUrlString());
            // add the locations back to the list so they matches the imdi url format
//            locationsList.add(currentImdiObject.getUrlString());
            if (!currentImdiObject.isLocal()) {
                remoteCorpusRootUrls.add(currentImdiObject.getUrlString());
            } else if (GuiHelper.linorgSessionStorage.pathIsInsideCache(currentImdiObject.getFile())) {
                localCorpusRootUrls.add(currentImdiObject.getUrlString());
            } else {
                localDirectoryRootUrls.add(currentImdiObject.getUrlString());
            }
        }
    }

    synchronized private void addToSortQueue(DefaultMutableTreeNode currentTreeNode) {
        if (!treeNodeSortQueue.contains(currentTreeNode)) {
            System.out.println("requestSort: " + currentTreeNode.getUserObject().toString());
            treeNodeSortQueue.add(currentTreeNode);
        }
        if (!treeNodeSortQueueRunning) {
            treeNodeSortQueueRunning = true;
            new Thread() {

                @Override
                public void run() {
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
                                    childUrls.add(childImdiObject.getUrlString());
//                                    System.out.println("adding child to update list: " + childImdiObject.getUrlString());
                                }
                                updateTreeNodeChildren(currentTreeNode, childUrls);
                            } else {
                                // assume that this is a root node so update the root nodes                              
                                Vector<String> remoteCorpusRootUrls = new Vector();
                                Vector<String> localCorpusRootUrls = new Vector();
                                Vector<String> localDirectoryRootUrls = new Vector();
                                getRootNodeArrays(remoteCorpusRootUrls, localCorpusRootUrls, localDirectoryRootUrls);
                                updateTreeNodeChildren(remoteCorpusRootNode, remoteCorpusRootUrls);
                                updateTreeNodeChildren(localCorpusRootNode, localCorpusRootUrls);
                                updateTreeNodeChildren(localDirectoryRootNode, localDirectoryRootUrls);
                            }
//                                sortChildNodes(currentTreeNode);
                        }
                    }
                    treeNodeSortQueueRunning = false;
                }
            }.start();
        }
    }

    private void sortChildNodes(DefaultMutableTreeNode parentNode, ArrayList<DefaultMutableTreeNode> currentChildren) {
//        System.out.println("sortChildNodes: " + parentNode.getUserObject().toString());
        // resort the branch since the node name may have changed
        DefaultTreeModel treeModel = getModelForNode(parentNode);
//        ArrayList<DefaultMutableTreeNode> sortedChildren = Collections.list(parentNode.children());
        Collections.sort(currentChildren, new Comparator() {

            public int compare(Object object1, Object object2) {
                if (!(object1 instanceof DefaultMutableTreeNode && object2 instanceof DefaultMutableTreeNode)) {
                    throw new IllegalArgumentException("not a DefaultMutableTreeNode object");
                }
                Object userObject1 = ((DefaultMutableTreeNode) object1).getUserObject();
                Object userObject2 = ((DefaultMutableTreeNode) object2).getUserObject();
                if (userObject1 instanceof ImdiTreeObject && userObject2 instanceof ImdiTreeObject) {
                    return ((ImdiTreeObject) userObject1).compareTo(userObject2);
                } else {
                    return userObject1.toString().compareToIgnoreCase(object2.toString());
                }
            }
        });
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
                    try {
                        if (currentChildren.get(childCounter).getParent() != null) {
                            System.out.println("removing");
                            treeModel.removeNodeFromParent(currentChildren.get(childCounter));
//                            if (!currentChildren.contains(currentChildren.get(childCounter))) {
//                                treeModel.nodeStructureChanged(currentChildren.get(childCounter));
//                            }
                        }
                    } catch (Exception e) {
                        GuiHelper.linorgBugCatcher.logError(e);
//                        System.out.println("sortChildNodes failed to move: " + sortedChildren.get(childCounter));
                    }
                    System.out.println("inserting");
                    treeModel.insertNodeInto(currentChildren.get(childCounter), parentNode, childCounter);
//                treeModel.nodeStructureChanged(parentNode);
//                            treeModel.nodeChanged(itemNode);
//            treeModel.nodeChanged(missingTreeNode);
                }
            } catch (Exception ex) {
                GuiHelper.linorgBugCatcher.logError(ex);
            }

            // update the string and icon etc for each node
            boolean childCanHaveChildren = ((ImdiTreeObject) ((DefaultMutableTreeNode) currentChildren.get(childCounter)).getUserObject()).canHaveChildren();
            currentChildren.get(childCounter).setAllowsChildren(childCanHaveChildren || currentChildren.get(childCounter).getChildCount() > 0);
            treeModel.nodeChanged(currentChildren.get(childCounter));
        }
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
    }

    public void redrawTrees() {
        localCorpusTree.invalidate();
        localDirectoryTree.invalidate();
        remoteCorpusTree.invalidate();
        localCorpusTree.repaint();
        localDirectoryTree.repaint();
        remoteCorpusTree.repaint();
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
        System.out.println("localCorpusTree: " + localCorpusTree);
        return (DefaultMutableTreeNode) localCorpusTree.getSelectionPath().getLastPathComponent();
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

    public ImdiTreeObject addImdiChildNode(ImdiTreeObject imdiTreeObject, String nodeType, String nodeTypeDisplayName, String resourcePath, String mimeType) {
        ImdiTreeObject addedImdi = null;
        String addedNodeUrl = null;
        if (nodeType != null) {
            if (imdiTreeObject.isImdi() && !imdiTreeObject.fileNotFound) {// if url is null (not an imdi) then the node is unattached
                if (imdiTreeObject.isImdiChild()) {
                    imdiTreeObject = imdiTreeObject.getParentDomNode();
                }
                addedNodeUrl = imdiTreeObject.addChildNode(nodeType, resourcePath, mimeType);
            } else {
                addedNodeUrl = new ImdiTreeObject("temp root node", GuiHelper.linorgSessionStorage.getSaveLocation("unattachedcorpus")).addChildNode(nodeType, null, null);
                addLocation(addedNodeUrl);
                applyRootLocations();
            }
        }
        if (addedNodeUrl != null) {
            System.out.println("addedNodeUrl: " + addedNodeUrl);
            addedImdi = GuiHelper.imdiLoader.getImdiObject(null, addedNodeUrl);
        }
        return addedImdi;
    }

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
        if (sourceObject == localCorpusTree) {
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
                    currentParent.deleteFromParentDom(((Vector<String>) imdiChildNodeDeleteList.get(currentParent)).toArray(new String[]{}));
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
            TreeHelper.getSingleInstance().remoteCorpusTree.clearSelection();
            TreeHelper.getSingleInstance().localCorpusTree.clearSelection();
            TreeHelper.getSingleInstance().localDirectoryTree.clearSelection();
            boolean foundInRemoteCorpus = TreeHelper.getSingleInstance().remoteCorpusTree.scrollToNode(cellImdiNode);
            boolean foundInLocalCorpus = TreeHelper.getSingleInstance().localCorpusTree.scrollToNode(cellImdiNode);
            boolean foundInLocalDirectory = TreeHelper.getSingleInstance().localDirectoryTree.scrollToNode(cellImdiNode);
            if (!foundInRemoteCorpus && !foundInLocalCorpus && !foundInLocalDirectory) {
                if (!silent) {
                    LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("The selected node has not been loaded in the tree.\nUntil a search is provided for this you will need to browse the tree to load the required node", "Jump to in Tree");
                }
            }
        } else {
            if (!silent) {
                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("The selected cell has not value or is not associated with a node in the tree", "Jump to in Tree");
            }
        }
    }
}
