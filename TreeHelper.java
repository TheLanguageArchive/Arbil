/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import java.awt.Component;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
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

    public TreeHelper() {
        localCorpusRootNode = new DefaultMutableTreeNode();
        remoteCorpusRootNode = new DefaultMutableTreeNode();
        localDirectoryRootNode = new DefaultMutableTreeNode();

        localCorpusTreeModel = new DefaultTreeModel(localCorpusRootNode, true);
        remoteCorpusTreeModel = new DefaultTreeModel(remoteCorpusRootNode, true);
        localDirectoryTreeModel = new DefaultTreeModel(localDirectoryRootNode, true);
    }

    public boolean componentIsTheLocalCorpusTree(Component componentToTest) {
        return componentToTest.equals(localCorpusTree);
    //return localCorpusTree.getName().equals(componentToTest.getName());
    }

    public void setTrees(JTree tempRemoteCorpusTree, JTree tempLocalCorpusTree, JTree tempLocalDirectoryTree) {
        remoteCorpusRootNode.setUserObject(new JLabel("Remote Corpus", ImdiHelper.serverIcon, JLabel.LEFT));
        localCorpusRootNode.setUserObject(new JLabel("Local Corpus", ImdiHelper.directoryIcon, JLabel.LEFT));
        localDirectoryRootNode.setUserObject(new JLabel("Working Directories", UIManager.getIcon("FileView.computerIcon"), JLabel.LEFT));

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
        if (addLocation("http://corpus1.mpi.nl/qfs1/media-archive/dobes_data/ChintangPuma/Chintang/Conversation/Metadata/phidang_talk.imdi")) {
            addedCount++;
        }
        if (addLocation("http://corpus1.mpi.nl/qfs1/media-archive/silang_data/Corpusstructure/1-03.imdi")) {
            addedCount++;
        }
        if (addLocation("http://corpus1.mpi.nl/qfs1/media-archive/dobes_data/ECLING/Corpusstructure/ECLING.imdi")) {
            addedCount++;
        }
        if (addLocation("http://corpus1.mpi.nl/qfs1/media-archive/dobes_data/Center/Corpusstructure/center.imdi")) {
            addedCount++;
        }
        if (addLocation("http://corpus1.mpi.nl/qfs1/media-archive/dobes_data/Teop/Corpusstructure/1.imdi")) {
            addedCount++;
        }
        if (addLocation("http://corpus1.mpi.nl/qfs1/media-archive/dobes_data/Waimaa/Corpusstructure/1.imdi")) {
            addedCount++;
        }
        if (addLocation("http://corpus1.mpi.nl/qfs1/media-archive/dobes_data/Beaver/Corpusstructure/Beaver.imdi")) {
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

    public void refreshChildNodes(DefaultMutableTreeNode itemNode) {
        System.out.println("refreshChildNodes: " + itemNode);
        removeChildNodes(itemNode);
        getImdiChildNodes(itemNode);
    }

    private void removeChildNodes(DefaultMutableTreeNode parentNode) {
        // this function replaces the use of removeAllChildren et. al. from the tree nodes
        // its purpose is to deregister the tree node from the imdinode before removing the node from the tree
        // loop child nodes
        for (Enumeration childNodesEnum = parentNode.children(); childNodesEnum.hasMoreElements();) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) childNodesEnum.nextElement();
            // remove recursively
            removeChildNodes(childNode);
            System.out.println("removeChildNode: " + childNode);
            Object childUserObject = childNode.getUserObject();
            // get the imdi node
            if (childUserObject instanceof ImdiHelper.ImdiTreeObject) {
                //deregister the tree node in the imdinode
                ((ImdiHelper.ImdiTreeObject) childUserObject).removeContainer(childUserObject);
            }
        }
        //remove the node from the parent               
        parentNode.removeAllChildren();
    }

    public void applyRootLocations() {
        // TODO: remove the containers registered with the imdi objects via: currentImdiObject.registerContainer(currentTreeNode);
        removeChildNodes(remoteCorpusRootNode);
        removeChildNodes(localCorpusRootNode);
        removeChildNodes(localDirectoryRootNode);
        Vector locationImdiNodes = new Vector();
        for (Enumeration locationEnum = locationsList.elements(); locationEnum.hasMoreElements();) {
            locationImdiNodes.add(GuiHelper.imdiHelper.getTreeNodeObject(locationEnum.nextElement().toString()));
        }
        Collections.sort(locationImdiNodes);
        for (Enumeration<ImdiHelper.ImdiTreeObject> locationNodesEnum = locationImdiNodes.elements(); locationNodesEnum.hasMoreElements();) {
            ImdiHelper.ImdiTreeObject currentImdiObject = locationNodesEnum.nextElement();
            DefaultMutableTreeNode currentTreeNode = new DefaultMutableTreeNode(currentImdiObject);
            currentImdiObject.registerContainer(currentTreeNode);
            if (!currentImdiObject.isLocal()) {
                remoteCorpusRootNode.add(currentTreeNode);
            } else if (currentImdiObject.isImdi()) {
                localCorpusRootNode.add(currentTreeNode);
            } else {
                localDirectoryRootNode.add(currentTreeNode);
            }
        }
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

    public void showLocationsDialog() {
        // TODO: it would be preferable to move all dialog creation and management into the linorgwindowmanager
        JDialog settingsjDialog = new JDialog(JOptionPane.getFrameForComponent(GuiHelper.linorgWindowManager.desktopPane));
        settingsjDialog.setLocationRelativeTo(GuiHelper.linorgWindowManager.desktopPane);
        JTable locationSettingsTable = new JTable(getLocationsTableModel()) {

            public TableCellRenderer getCellRenderer(int row, int column) {
                if (column == 0) {
                    ImdiHelper.ImdiTreeObject imdiObject = (ImdiHelper.ImdiTreeObject) getModel().getValueAt(row, column);
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
            tableObjectAray[rowCounter][0] = GuiHelper.imdiHelper.getTreeNodeObject(tableObjectAray[rowCounter][1].toString());
            rowCounter++;
        }
        return new javax.swing.table.DefaultTableModel(tableObjectAray, new String[]{"", "Location"}) {

            Class[] types = new Class[]{
                ImdiHelper.ImdiTreeObject.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        };
    }

    public void addImdiChildNode(DefaultMutableTreeNode itemNode, String nodeType) {
        System.out.println("adding a new node to: " + itemNode);
        if (GuiHelper.imdiHelper.isImdiNode(itemNode.getUserObject())) {
            ImdiHelper.ImdiTreeObject imdiTreeObject = (ImdiHelper.ImdiTreeObject) itemNode.getUserObject();
            if (imdiTreeObject.isImdi()) {
                System.out.println("its an imdi so start adding");
                Vector tempVector = imdiTreeObject.addChildNode(nodeType, null);
                refreshChildNodes(itemNode);
                GuiHelper.linorgWindowManager.openFloatingTable(tempVector.elements(), "new " + nodeType + " in " + itemNode);
            }
        } else {
            // TODO: implement adding to the root node
            System.out.println("TODO: implement adding to the root node");
        }
    }

    public void getImdiChildNodes(DefaultMutableTreeNode itemNode) {
        if (itemNode.getChildCount() == 0) {
            // add "loading" node
            itemNode.add(new DefaultMutableTreeNode(new JLabel("loading...", ImdiHelper.fileUnknown, JLabel.CENTER)));
            if (GuiHelper.imdiHelper.isImdiNode(itemNode.getUserObject())) {
                ImdiHelper.ImdiTreeObject imdiTreeObject = (ImdiHelper.ImdiTreeObject) itemNode.getUserObject();
                if (!imdiTreeObject.isImdi() && !imdiTreeObject.isDirectory()) {
                    System.out.println("file to be opened");
                } else {
                    //ImdiHelper.ImdiTreeObject[] childNodes = imdiTreeObject.getChildren(imdiFieldViews, imdiFieldViews.getCurrentFieldArray());
                    ImdiHelper.ImdiTreeObject[] childNodes = imdiTreeObject.loadChildNodes(false);
                    Arrays.sort(childNodes);
                    // remove the loading node
                    removeChildNodes(itemNode);
                    for (int childCount = 0; childCount < childNodes.length; childCount++) {
                        DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(childNodes[childCount]);
                        treeNode.setAllowsChildren(childNodes[childCount].isImdi() || childNodes[childCount].isDirectory());
                        childNodes[childCount].registerContainer(treeNode);
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
                setIcon(((JLabel) node.getUserObject()).getIcon());
                setText(((JLabel) node.getUserObject()).getText());
            }
            return this;
        }
    }
}
