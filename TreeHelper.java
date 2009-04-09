/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
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
 *
 * @author petwit
 */
public class TreeHelper {

    public DefaultTreeModel localCorpusTreeModel;
    public DefaultTreeModel remoteCorpusTreeModel;
    public DefaultTreeModel localDirectoryTreeModel;
    private DefaultMutableTreeNode localCorpusRootNode;
    private DefaultMutableTreeNode remoteCorpusRootNode;
    private DefaultMutableTreeNode localDirectoryRootNode;
    public ImdiTree localCorpusTree;
    private ImdiTree localDirectoryTree;
    private ImdiTree remoteCorpusTree;
    private Vector<String> locationsList; // this is the list of locations seen in the tree and the location settings

    public TreeHelper() {
        localCorpusRootNode = new DefaultMutableTreeNode();
        remoteCorpusRootNode = new DefaultMutableTreeNode();
        localDirectoryRootNode = new DefaultMutableTreeNode();

        localCorpusTreeModel = new DefaultTreeModel(localCorpusRootNode, true);
        remoteCorpusTreeModel = new DefaultTreeModel(remoteCorpusRootNode, true);
        localDirectoryTreeModel = new DefaultTreeModel(localDirectoryRootNode, true);
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

    public void updateTreeNodeChildren(ImdiTreeObject parentImdiNode) {
        System.out.println("updateTreeNodeChildren ImdiTreeObject: " + parentImdiNode);
        for (Enumeration nodeContainersEnum = parentImdiNode.getRegisteredContainers(); nodeContainersEnum.hasMoreElements();) {
            Object currentContainer = nodeContainersEnum.nextElement();
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
        System.out.println("updateTreeNodeChildren DefaultMutableTreeNode: " + parentTreeNode.toString());
//        System.out.println("updateTreeNodeChildren DefaultMutableTreeNode: " + parentTreeNode.hashCode());
        Vector<String> childUrls = new Vector();
        //DefaultMutableTreeNode parentTreeNode = (DefaultMutableTreeNode) itemNode.getParent();
        Object parentObject = parentTreeNode.getUserObject();
        if (parentObject instanceof ImdiTreeObject) {
            ImdiTreeObject parentImdiObject = (ImdiTreeObject) parentObject;
            // make the list of child urls
            for (Enumeration<ImdiTreeObject> childEnum = parentImdiObject.getChildEnum(); childEnum.hasMoreElements();) {
                ImdiTreeObject childImdiObject = childEnum.nextElement();
                childUrls.add(childImdiObject.getUrlString());
                System.out.println("adding child to update list: " + childImdiObject.getUrlString());
            }
            updateTreeNodeChildren(parentTreeNode, childUrls);
        }
    }
    // check that all child nodes are attached and sorted, removing any extranious nodes found
    public void updateTreeNodeChildren(DefaultMutableTreeNode itemNode, Vector<String> childUrls) {
        //TODO: find out why this leaves duplicate meta nodes when adding a imdi child node
        Vector<DefaultMutableTreeNode> nodesToRemove = new Vector();
        DefaultTreeModel treeModel = getModelForNode(itemNode);
//        boolean childrenChanged = false;
        // this could make sure the order is that of the supplied url list, er no it could not
        for (Enumeration<DefaultMutableTreeNode> childrenEnum = itemNode.children(); childrenEnum.hasMoreElements();) {
            DefaultMutableTreeNode currentChildNode = childrenEnum.nextElement();
            ImdiTreeObject childImdiObject = (ImdiTreeObject) currentChildNode.getUserObject();
//            System.out.println("updateTreeNodeChildren[]: checking: " + childImdiObject.toString());
            if (!childUrls.remove(childImdiObject.getUrlString())) {
                System.out.println("updateTreeNodeChildren[]: removing extraneous node: " + childImdiObject.getUrlString());
                // remove any extraneous nodes
                nodesToRemove.add(currentChildNode);
                removeAndDetatchDescendantNodes(currentChildNode);
//                treeModel.removeNodeFromParent(currentChildNode);
//                treeModel.nodeStructureChanged(itemNode);
            }
        }
        while (childUrls.size() > 0) {
            // add any missing child nodes
            ImdiTreeObject missingImdiNode = GuiHelper.imdiLoader.getImdiObject(null, childUrls.remove(0));
            System.out.println("updateTreeNodeChildren[]: add missing node: " + missingImdiNode.getUrlString());
            DefaultMutableTreeNode missingTreeNode = new DefaultMutableTreeNode(missingImdiNode);
            //itemNode.add(missingTreeNode);
            itemNode.setAllowsChildren(true);
            missingTreeNode.setAllowsChildren(missingImdiNode.canHaveChildren());
            treeModel.insertNodeInto(missingTreeNode, itemNode, itemNode.getChildCount());
            missingImdiNode.registerContainer(missingTreeNode);
            treeModel.nodeStructureChanged(itemNode);
        }
        sortChildNodes(itemNode);
        while (!nodesToRemove.isEmpty()) {
            treeModel.removeNodeFromParent(nodesToRemove.remove(0));
        }
    }

    public void sortChildNodes(DefaultMutableTreeNode parentNode) {
        System.out.println("sortChildNodes: " + parentNode.getUserObject().toString());
        // resort the branch since the node name may have changed
        DefaultTreeModel treeModel = getModelForNode(parentNode);
        ArrayList<DefaultMutableTreeNode> sortedChildren = Collections.list(parentNode.children());
        Collections.sort(sortedChildren, new Comparator() {

            public int compare(Object object1, Object object2) {
                if (!(object1 instanceof DefaultMutableTreeNode && object2 instanceof DefaultMutableTreeNode)) {
                    throw new IllegalArgumentException("not a DefaultMutableTreeNode object");
                }
                String string1 = ((DefaultMutableTreeNode) object1).getUserObject().toString();
                String string2 = ((DefaultMutableTreeNode) object2).getUserObject().toString();
                return string1.compareToIgnoreCase(string2);
            }
        });
        // loop the child nodes comparing with the sorted array and move nodes only if required
        for (int childCounter = 0; childCounter < sortedChildren.size(); childCounter++) {
//            System.out.println("sortChildNodes comparing: " + sortedChildren.get(childCounter));
//            System.out.println("sortChildNodes to: " + parentNode.getChildAt(childCounter));
            if (!sortedChildren.get(childCounter).equals(parentNode.getChildAt(childCounter))) {
                System.out.println("sortChildNodes moving: " + sortedChildren.get(childCounter) + " to " + childCounter);
                try {
                    treeModel.removeNodeFromParent(sortedChildren.get(childCounter));
                } catch (Exception e) {
                    System.out.println("sortChildNodes failed to move: " + sortedChildren.get(childCounter));
                }
                treeModel.insertNodeInto(sortedChildren.get(childCounter), parentNode, childCounter);
                treeModel.nodeStructureChanged(parentNode);
            }
        }
    }

//    public void refreshDescendantNodes(DefaultMutableTreeNode itemNode) {
//        removeAndDetatchDescendantNodes(itemNode);
//        loadDescendantNodes(itemNode);
//    }
    public void removeAndDetatchDescendantNodes(DefaultMutableTreeNode itemNode) {
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
    }

//    public void loadAndRefreshDescendantNodes(DefaultMutableTreeNode itemNode) {
//        System.out.println("refreshChildNodes: " + itemNode);
//        updateTreeNodeChildren(itemNode);
//        for (Enumeration<DefaultMutableTreeNode> childrenEnum = itemNode.children(); childrenEnum.hasMoreElements();) {
//            DefaultMutableTreeNode currentChildNode = childrenEnum.nextElement();
//            loadAndRefreshDescendantNodes(currentChildNode);
//        }
//    }

    public void applyRootLocations() {
        System.out.println("applyRootLocations");
        Vector<String> remoteCorpusRootUrls = new Vector();
        Vector<String> localCorpusRootUrls = new Vector();
        Vector<String> localDirectoryRootUrls = new Vector();

        Vector locationImdiNodes = new Vector();
        for (Enumeration locationEnum = locationsList.elements(); locationEnum.hasMoreElements();) {
            locationImdiNodes.add(GuiHelper.imdiLoader.getImdiObject(null, locationEnum.nextElement().toString()));
        }
        // remove all locations from the list so they can be replaced in a format that matches the imdi url format
        locationsList.removeAllElements();
        Collections.sort(locationImdiNodes);
        for (Enumeration<ImdiTreeObject> locationNodesEnum = locationImdiNodes.elements(); locationNodesEnum.hasMoreElements();) {
            ImdiTreeObject currentImdiObject = locationNodesEnum.nextElement();
            System.out.println("root location: " + currentImdiObject.getUrlString());
            // add the locations back to the list so they matches the imdi url format
            locationsList.add(currentImdiObject.getUrlString());
            if (!currentImdiObject.isLocal()) {
                remoteCorpusRootUrls.add(currentImdiObject.getUrlString());
            } else if (currentImdiObject.isImdi()) {
                localCorpusRootUrls.add(currentImdiObject.getUrlString());
            } else {
                localDirectoryRootUrls.add(currentImdiObject.getUrlString());
            }
        }
        updateTreeNodeChildren(remoteCorpusRootNode, remoteCorpusRootUrls);
        updateTreeNodeChildren(localCorpusRootNode, localCorpusRootUrls);
        updateTreeNodeChildren(localDirectoryRootNode, localDirectoryRootUrls);

//        localDirectoryTreeModel.reload();
//        localCorpusTreeModel.reload();
//        remoteCorpusTreeModel.reload();
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

    public void reloadLocalCorpusTree() {
        javax.swing.tree.TreePath currentSelection = localCorpusTree.getSelectionPath();
        ((DefaultTreeModel) localCorpusTree.getModel()).reload();
//        localCorpusTree.expandPath(currentSelection); // this may be what is causing the tree draw issues
    }

    public DefaultMutableTreeNode getLocalCorpusTreeSingleSelection() {
        System.out.println("localCorpusTree: " + localCorpusTree);
        return (DefaultMutableTreeNode) localCorpusTree.getSelectionPath().getLastPathComponent();
    }

    public void showLocationsDialog() {
        // TODO: it would be preferable to move all dialog creation and management into the linorgwindowmanager
        JDialog settingsjDialog = new JDialog(JOptionPane.getFrameForComponent(GuiHelper.linorgWindowManager.linorgFrame));
        settingsjDialog.setLocationRelativeTo(GuiHelper.linorgWindowManager.linorgFrame);
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
//        System.out.println("adding a new node to: " + itemNode);
        System.out.println("adding nodeType: " + nodeType);
        System.out.println("adding nodeTypeDisplayName: " + nodeTypeDisplayName);
        if (imdiTreeObject.isImdi() && !imdiTreeObject.fileNotFound) {// if url is null (not an imdi) then the node is unattached
            if (imdiTreeObject.isImdiChild()) {
                imdiTreeObject = imdiTreeObject.getParentDomNode();
            }
            System.out.println("adding to imdi node");
            String addedNodeUrl = imdiTreeObject.addChildNode(nodeType, resourcePath, mimeType);
//            updateTreeNodeChildren(imdiTreeObject);
            if (addedNodeUrl != null) {
                Vector tempVector = new Vector();
                addedImdi = GuiHelper.imdiLoader.getImdiObject(null, addedNodeUrl);
                System.out.println("addedNodeUrl: " + addedNodeUrl);
                System.out.println("addedImdi: " + addedImdi);
                tempVector.add(addedImdi);
                GuiHelper.linorgWindowManager.openFloatingTable(tempVector.elements(), "new " + nodeTypeDisplayName + " in " + imdiTreeObject.toString());
            // this will only happen on the local corpus tree so we can just address that here
//                localCorpusTree.scrollToNode(addedImdi);
            }
        } else {
            System.out.println("adding root imdi node");
            String addedNodeUrl = new ImdiTreeObject("temp root node", GuiHelper.linorgSessionStorage.getSaveLocation("unattachedcorpus")).addChildNode(nodeType, null, null);
            addLocation(addedNodeUrl);
            applyRootLocations();
            //refreshChildNodes(itemNode);
            Vector tempVector = new Vector();
            addedImdi = GuiHelper.imdiLoader.getImdiObject(null, addedNodeUrl);
            tempVector.add(addedImdi);
            GuiHelper.linorgWindowManager.openFloatingTable(tempVector.elements(), "new " + nodeTypeDisplayName);
        // this will only happen on the local corpus tree so we can just address that here
//            localCorpusTree.scrollToNode(addedImdi); //TODO: this is failing because at this point the new node is probably not laoded. This must be done in the loading thread after load
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

    public Object getSingleSelectedNode(Object sourceObject) {
//        System.out.println("getSingleSelectedNode: " + sourceObject);

        DefaultMutableTreeNode selectedTreeNode = null;
        Object returnObject = null;
        if (sourceObject instanceof ImdiTree) {
            javax.swing.tree.TreePath currentNodePath = ((ImdiTree) sourceObject).getSelectionPath();
            if (currentNodePath != null) {
                selectedTreeNode = (DefaultMutableTreeNode) currentNodePath.getLastPathComponent();
            }
            if (selectedTreeNode != null) {
                returnObject = selectedTreeNode.getUserObject();
            }
        }
        return returnObject;
    }

    public void removeSelectedLocation(DefaultMutableTreeNode selectedTreeNode) {
        if (selectedTreeNode == null) {
            JOptionPane.showMessageDialog(GuiHelper.linorgWindowManager.linorgFrame, "No node selected", "", 0);
        } else {
            if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(GuiHelper.linorgWindowManager.linorgFrame, "Remove link to '" + selectedTreeNode + "?", "Remove", JOptionPane.YES_NO_OPTION)) {
                GuiHelper.treeHelper.removeLocation(selectedTreeNode.getUserObject());
                GuiHelper.treeHelper.applyRootLocations();
            }
        }
    }

    public void deleteNode(Object sourceObject) {
        System.out.println("deleteNode: " + sourceObject);

        DefaultMutableTreeNode selectedTreeNode = null;
        DefaultMutableTreeNode parentTreeNode = null;
        if (sourceObject == localCorpusTree) {
            int selectedRow = ((ImdiTree) sourceObject).getMinSelectionRow();
            javax.swing.tree.TreePath currentNodePath = ((ImdiTree) sourceObject).getSelectionPath();
            if (currentNodePath != null) {
                selectedTreeNode = (DefaultMutableTreeNode) currentNodePath.getLastPathComponent();
                parentTreeNode = (DefaultMutableTreeNode) selectedTreeNode.getParent();
                ImdiTreeObject parentImdiNode = (ImdiTreeObject) parentTreeNode.getUserObject();
                ImdiTreeObject childImdiNode = (ImdiTreeObject) selectedTreeNode.getUserObject();
                if (childImdiNode.isImdiChild()) {
                    System.out.println("cannot delete imdi child nodes yet");
                } else if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(GuiHelper.linorgWindowManager.linorgFrame, "Delete '" + childImdiNode + "' from '" + parentImdiNode + "'?", "Delete", JOptionPane.YES_NO_OPTION)) {
                    parentImdiNode.deleteCorpusLink(childImdiNode);
                    parentTreeNode.remove(selectedTreeNode);
                    localCorpusTreeModel.nodeStructureChanged(parentTreeNode);
                }
                ((ImdiTree) sourceObject).setSelectionRow(selectedRow);
            }
        } else {
            System.out.println("cannot delete from this tree");
        }
    }
}
