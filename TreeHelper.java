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
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 *
 * @author petwit
 */
public class TreeHelper {

    private Vector locationsList; // this is the list of locations seen in the tree and the location settings
    private Hashtable locationTreeNodes = new Hashtable(); // this is used to find the location tree node when it is to be removed via the ulr

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

    public void applyRootLocations(DefaultMutableTreeNode localDirectoryNode, DefaultMutableTreeNode localCorpusNode, DefaultMutableTreeNode remoteCorpusNode) {
        Enumeration locationEnum = locationsList.elements();
        while (locationEnum.hasMoreElements()) {
            String currentLocation = locationEnum.nextElement().toString();
            System.out.println("currentLocation: " + currentLocation);
            if (GuiHelper.imdiHelper.isStringLocal(currentLocation)) {
                // is local
                if (GuiHelper.imdiHelper.isStringImdi(currentLocation)) {
                    // is an imdi
                    addNodeOnce(localCorpusNode, currentLocation);
                } else {
                    // not an imdi
                    addNodeOnce(localDirectoryNode, currentLocation);
                }
            } else {
                // is a remote file or imdi
                addNodeOnce(remoteCorpusNode, currentLocation);
            }
        }
        removeExtraneousNodes();
    }

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
            if (GuiHelper.imdiHelper.isImdiNode(node.getUserObject())) {
                ImdiHelper.ImdiTreeObject imdiTreeObject = (ImdiHelper.ImdiTreeObject) node.getUserObject();

                setIcon(imdiTreeObject.getIcon());
                setToolTipText(imdiTreeObject.toString());
                setEnabled(imdiTreeObject.getNodeEnabled());
            //setVisible(imdiTreeObject.getNodeEnabled());
            }
            return this;
        }
    }
}
