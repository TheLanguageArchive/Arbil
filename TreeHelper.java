/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import java.awt.Component;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
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
    private javax.swing.JTree localCorpusTree;
    private javax.swing.JTree localDirectoryTree;
    private javax.swing.JTree remoteCorpusTree;
    private Vector locationsList; // this is the list of locations seen in the tree and the location settings
    private Hashtable locationTreeNodes = new Hashtable(); // this is used to find the location tree node when it is to be removed via the ulr

    public TreeHelper() {
        localCorpusRootNode = new DefaultMutableTreeNode();
        remoteCorpusRootNode = new DefaultMutableTreeNode();
        localDirectoryRootNode = new DefaultMutableTreeNode();

        localCorpusTreeModel = new DefaultTreeModel(localCorpusRootNode, true);
        remoteCorpusTreeModel = new DefaultTreeModel(remoteCorpusRootNode, true);
        localDirectoryTreeModel = new DefaultTreeModel(localDirectoryRootNode, true);
    }

    public void setTrees(JTree tempRemoteCorpusTree, JTree tempLocalCorpusTree, JTree tempLocalDirectoryTree) {
        remoteCorpusRootNode.setUserObject(new JLabel("Remote Corpus", ImdiHelper.serverIcon, JLabel.LEFT));
        localCorpusRootNode.setUserObject(new JLabel("Local Corpus Cache", ImdiHelper.directoryIcon, JLabel.LEFT));
        localDirectoryRootNode.setUserObject(new JLabel("Local File System", UIManager.getIcon("FileView.computerIcon"), JLabel.LEFT));

        remoteCorpusTree = tempRemoteCorpusTree;
        localCorpusTree = tempLocalCorpusTree;
        localDirectoryTree = tempLocalDirectoryTree;
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
        return addedCount;
    }

    public void saveLocations() {
        try {
            GuiHelper.linorgSessionStorage.saveObject(locationsList, "locationsList");
            System.out.println("saved locationsList");
        } catch (Exception ex) {
            System.out.println("save locationsList exception: " + ex.getMessage());
        }
    }

    public void loadLocationsList() {
        try {
            locationsList = (Vector) GuiHelper.linorgSessionStorage.loadObject("locationsList");
        } catch (Exception ex) {
            System.out.println("load locationsList exception: " + ex.getMessage());
        }
        if (locationsList == null) {
            locationsList = new Vector();
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
        System.out.println("addLocation" + addedLocation);
        if (addedLocation != null) {
            if (!locationsList.contains(addedLocation)) {
                locationsList.add(addedLocation);
                return true;
            }
        }
        return false;
    }

    public void removeLocation(Object removeObject) {
        if (GuiHelper.imdiHelper.isImdiNode(removeObject)) {
            removeLocation(((ImdiHelper.ImdiTreeObject) removeObject).getUrl()); //.replace("file://", "")
        }
    }

    public void removeLocation(String removeLocation) {
        System.out.println("removeLocation: " + removeLocation);
        locationsList.remove(removeLocation);
    }
//    public void updateLocationsFromModel(javax.swing.table.DefaultTableModel changedTableModel) {
//        Vector updatedLocations = new Vector();
//        for (int rowCounter = 0; rowCounter < changedTableModel.getRowCount(); rowCounter++) {
//            updatedLocations.add(changedTableModel.getValueAt(rowCounter, 1));
//        }
//        locationsList = updatedLocations;
//    }
    private void addNodeOnce(DefaultMutableTreeNode localDirectoryNode, String currentLocation) {
        boolean nodeExists = false;
        Enumeration localCorpusChildren = localDirectoryNode.children();
        while (localCorpusChildren.hasMoreElements()) {
            if (currentLocation.equals(((ImdiHelper.ImdiTreeObject) ((DefaultMutableTreeNode) localCorpusChildren.nextElement()).getUserObject()).getUrl())) {
                nodeExists = true;
            }
        }
        if (nodeExists) {
            //localDirectoryNode.add(getImdiTreeNode("duplicate"));
        } else {
            DefaultMutableTreeNode currentTreeNode = getImdiTreeNode(currentLocation);
            locationTreeNodes.put(currentLocation, currentTreeNode);
            localDirectoryNode.add(currentTreeNode);
        }
    }

    private void removeExtraneousNodes() {
        Enumeration locationNodesEnum = locationTreeNodes.keys();
        while (locationNodesEnum.hasMoreElements()) {
            String currentLocation = (String) locationNodesEnum.nextElement();
            if (!locationsList.contains(currentLocation)) {
                System.out.println("removing location: " + currentLocation);
                ((DefaultMutableTreeNode) locationTreeNodes.get(currentLocation)).removeFromParent();
                locationTreeNodes.remove(currentLocation);
            }
        }
    }

    public void applyRootLocations() {
        Enumeration locationEnum = locationsList.elements();
        while (locationEnum.hasMoreElements()) {
            String currentLocation = locationEnum.nextElement().toString();
            System.out.println("currentLocation: " + currentLocation);
            if (GuiHelper.imdiHelper.isStringLocal(currentLocation)) {
                // is local
                if (GuiHelper.imdiHelper.isStringImdi(currentLocation)) {
                    // is an imdi
                    addNodeOnce(localCorpusRootNode, currentLocation);
                } else {
                    // not an imdi
                    addNodeOnce(localDirectoryRootNode, currentLocation);
                }
            } else {
                // is a remote file or imdi
                addNodeOnce(remoteCorpusRootNode, currentLocation);
            }
        }
        removeExtraneousNodes();
        localDirectoryTreeModel.reload();
        localCorpusTreeModel.reload();
        remoteCorpusTreeModel.reload();
    }

    public void reloadLocalCorpusTree() {
        javax.swing.tree.TreePath currentSelection = localCorpusTree.getSelectionPath();
        ((DefaultTreeModel) localCorpusTree.getModel()).reload();
        localCorpusTree.expandPath(currentSelection);
    }

    public DefaultMutableTreeNode getLeadLocalCorpusTreeSelection() {
        System.out.println("localCorpusTree: " + localCorpusTree);
        return (DefaultMutableTreeNode) localCorpusTree.getLeadSelectionPath().getLastPathComponent();
    }
//    public void addChild(String childType) {
//        // TODO: use the xsd at http://www.mpi.nl/IMDI/Schema/IMDI_3.0.xsd or aa later version to create the new xml document
////        if (remoteCorpusTree.getLeadSelectionPath() == null) {
//            if (localCorpusTree.getLeadSelectionPath() != null) {
//                DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) localCorpusTree.getLeadSelectionPath().getLastPathComponent();
//                GuiHelper.treeHelper.getImdiChildNodes(targetNode);
//                DefaultMutableTreeNode addedNode = GuiHelper.treeHelper.addImdiChildNode(targetNode);
//                localCorpusTreeModel.reload();
//                localCorpusTree.expandPath(currentSelection);
//            //localCorpusTree.scrollPathToVisible(addedNode);
//            //localCorpusTree.setSelectionPaths(currentSelection);
//            }
//  //      }
//    }
    public void showLocationsDialog() {
        // TODO: it would be preferable to move all dialog creation and management into the linorgwindowmanager
        JDialog settingsjDialog = new JDialog(JOptionPane.getFrameForComponent(GuiHelper.linorgWindowManager.desktopPane));
        settingsjDialog.setLocationRelativeTo(GuiHelper.linorgWindowManager.desktopPane);
        JTable locationSettingsTable = new JTable(GuiHelper.treeHelper.getLocationsTableModel());
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
        Enumeration locationEnum = locationsList.elements();
        int rowCounter = 0;
        while (locationEnum.hasMoreElements()) {
            tableObjectAray[rowCounter][1] = locationEnum.nextElement();
            if (GuiHelper.imdiHelper.isStringImdi(tableObjectAray[rowCounter][1].toString())) {
                // is an imdi
                if (GuiHelper.imdiHelper.isStringLocal(tableObjectAray[rowCounter][1].toString())) {
                    tableObjectAray[rowCounter][0] = (Object) ImdiHelper.corpuslocalicon;
                } else {
                    tableObjectAray[rowCounter][0] = ImdiHelper.corpusservericon;
                }
            } else {
                // is not an imdi
                if (GuiHelper.imdiHelper.isStringLocal(tableObjectAray[rowCounter][1].toString())) {
                    tableObjectAray[rowCounter][0] = ImdiHelper.directoryIcon;
                } else {
                    tableObjectAray[rowCounter][0] = ImdiHelper.stopicon;
                }
            }
            rowCounter++;
        }
        return new javax.swing.table.DefaultTableModel(tableObjectAray, new String[]{"", "Location"}) {

            Class[] types = new Class[]{
                javax.swing.Icon.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        };
    }

    public DefaultMutableTreeNode getImdiTreeNode(String urlString) {
        DefaultMutableTreeNode treeNode;
        ImdiHelper.ImdiTreeObject imdiTreeObject = GuiHelper.imdiHelper.getTreeNodeObject(urlString);
        treeNode = new DefaultMutableTreeNode(imdiTreeObject);
        treeNode.setAllowsChildren(imdiTreeObject.isImdi() || imdiTreeObject.isDirectory());
        return treeNode;
    }

    public void addImdiChildNode(DefaultMutableTreeNode itemNode, String nodeType) {
        System.out.println("adding a new node to: " + itemNode);
        if (GuiHelper.imdiHelper.isImdiNode(itemNode.getUserObject())) {
            ImdiHelper.ImdiTreeObject imdiTreeObject = (ImdiHelper.ImdiTreeObject) itemNode.getUserObject();
            if (imdiTreeObject.isImdi()) {
                System.out.println("its an imdi so start adding");
                ImdiHelper.ImdiTreeObject childImdiNode = imdiTreeObject.addChildNode(nodeType);
                DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(childImdiNode);
                itemNode.add(treeNode);
                Vector tempVector = new Vector();
                tempVector.add(childImdiNode);
                GuiHelper.linorgWindowManager.openFloatingTable(tempVector.elements(), "new node");
            }
        }
    }

    public void getImdiChildNodes(DefaultMutableTreeNode itemNode) {
        if (itemNode.getChildCount() == 0) {
            if (GuiHelper.imdiHelper.isImdiNode(itemNode.getUserObject())) {
                ImdiHelper.ImdiTreeObject imdiTreeObject = (ImdiHelper.ImdiTreeObject) itemNode.getUserObject();
                if (!imdiTreeObject.isImdi() && !imdiTreeObject.isDirectory()) {
                    System.out.println("file to be opened");
                } else {
                    //ImdiHelper.ImdiTreeObject[] childNodes = imdiTreeObject.getChildren(imdiFieldViews, imdiFieldViews.getCurrentFieldArray());
                    ImdiHelper.ImdiTreeObject[] childNodes = imdiTreeObject.loadChildNodes();
                    for (int childCount = 0; childCount < childNodes.length; childCount++) {
                        DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(childNodes[childCount]);
                        treeNode.setAllowsChildren(childNodes[childCount].isImdi() || childNodes[childCount].isDirectory());
                        itemNode.add(treeNode);
                    }
                }
            }
        }
    }

    public Object getSingleSelectedNode() {
        // TODO: this is not preferable because two nodes could be selected and be confusing to the user
        DefaultMutableTreeNode selectedTreeNode = null;
        Object returnObject = null;
        
        if (remoteCorpusTree.getSelectionCount() > 0 && localCorpusTree.getSelectionCount() > 0) {
            // if two trees have nodes selected then fail rather than confusing the user with an unexpected node
            return null;
        }
        
        if (remoteCorpusTree.getLeadSelectionPath() == null) {
            if (localCorpusTree.getLeadSelectionPath() != null) {
                selectedTreeNode = (DefaultMutableTreeNode) localCorpusTree.getLeadSelectionPath().getLastPathComponent();
            }
        } else {
            selectedTreeNode = (DefaultMutableTreeNode) remoteCorpusTree.getLeadSelectionPath().getLastPathComponent();
        }
        if (selectedTreeNode != null) {
            returnObject = selectedTreeNode.getUserObject();
        }
        return returnObject;
    }

    public ImdiTreeRenderer getImdiTreeRenderer() {
        return new ImdiTreeRenderer();
    }

    public class ImdiTreeRenderer extends DefaultTreeCellRenderer {

        public ImdiTreeRenderer() {
        }

        public Component getTreeCellRendererComponent(
                JTree tree,
                Object value,
                boolean sel,
                boolean expanded,
                boolean leaf,
                int row,
                boolean hasFocus) {

            super.getTreeCellRendererComponent(
                    tree, value, sel,
                    expanded, leaf, row,
                    hasFocus);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            if (node.getUserObject() instanceof ImdiHelper.ImdiTreeObject) {
                ImdiHelper.ImdiTreeObject imdiTreeObject = (ImdiHelper.ImdiTreeObject) node.getUserObject();

                setIcon(imdiTreeObject.getIcon());
                setToolTipText(imdiTreeObject.toString());
                setEnabled(imdiTreeObject.getNodeEnabled());
            //setVisible(imdiTreeObject.getNodeEnabled());
            } else if (node.getUserObject() instanceof JLabel) {
                setIcon(((JLabel)node.getUserObject()).getIcon());
                setText(((JLabel)node.getUserObject()).getText());
            }
            return this;
        }
    }
}
