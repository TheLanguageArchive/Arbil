/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 *
 * @author petwit
 */
public class GuiHelper {

    static ImdiHelper imdiHelper;
    static ImdiFieldViews imdiFieldViews;
    private LinorgSessionStorage linorgSessionStorage;
    private Hashtable selectedFilesList = new Hashtable(); // this is a list of the files currently displayed in the files window
    private Vector locationsList; // this is the list of locations seen in the tree and the location settings
    private Hashtable locationTreeNodes = new Hashtable(); // this is used to find the location tree node when it is to be removed via the ulr
//    MapView mapView;
    private JPanel selectedFilesPanel;
    private LinorgWindowManager linorgWindowManager;
    // create a clip board owner for copy and paste actions
    static ClipboardOwner clipboardOwner = new ClipboardOwner() {

        public void lostOwnership(Clipboard clipboard, Transferable contents) {
            System.out.println("lost clipboard ownership");
        //throw new UnsupportedOperationException("Not supported yet.");
        }
    };

    public GuiHelper(LinorgSessionStorage tempLinorgSessionStorage) {
        linorgSessionStorage = tempLinorgSessionStorage;
        imdiHelper = new ImdiHelper(linorgSessionStorage);
        imdiFieldViews = new ImdiFieldViews(linorgSessionStorage);
        loadLocationsList();
    }

    public void setWindowManager(LinorgWindowManager localLinorgWindowManager) {
        linorgWindowManager = localLinorgWindowManager;
    }

    public void saveState() {
        imdiHelper.saveMd5sumIndex();
        imdiFieldViews.saveViewsToFile();
        try {
            linorgSessionStorage.saveObject(locationsList, "locationsList");
            System.out.println("saved locationsList");
        } catch (Exception ex) {
            System.out.println("save locationsList exception: " + ex.getMessage());
        }
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

    private void loadLocationsList() {
        try {
            locationsList = (Vector) linorgSessionStorage.loadObject("locationsList");
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

    public void initViewMenu(javax.swing.JMenu viewMenu) {
        ButtonGroup viewMenuButtonGroup = new javax.swing.ButtonGroup();
        //String[] viewLabels = guiHelper.imdiFieldViews.getSavedFieldViewLables();
        for (Enumeration menuItemName = imdiFieldViews.getSavedFieldViewLables(); menuItemName.hasMoreElements();) {
            String currentMenuName = menuItemName.nextElement().toString();
            javax.swing.JRadioButtonMenuItem viewLabelRadioButtonMenuItem;
            viewLabelRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
            viewMenuButtonGroup.add(viewLabelRadioButtonMenuItem);
            viewLabelRadioButtonMenuItem.setSelected(imdiFieldViews.getCurrentGlobalViewName().equals(currentMenuName));
            viewLabelRadioButtonMenuItem.setText(currentMenuName);
            viewLabelRadioButtonMenuItem.setName(currentMenuName);
            viewLabelRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    imdiFieldViews.setCurrentGlobalViewName(((Component) evt.getSource()).getName());
                }
            });
            viewMenu.add(viewLabelRadioButtonMenuItem);
        }
    }

    public DefaultMutableTreeNode getImdiTreeNode(String urlString) {
        DefaultMutableTreeNode treeNode;
        ImdiHelper.ImdiTreeObject imdiTreeObject = imdiHelper.getTreeNodeObject(urlString);
        treeNode = new DefaultMutableTreeNode(imdiTreeObject);
        treeNode.setAllowsChildren(imdiTreeObject.isImdi() || imdiTreeObject.isDirectory());
        return treeNode;
    }
// date filter code
    public void updateDateSlider(JSlider dateSlider) {
        if (imdiHelper.minNodeDate == null) {
            System.out.println("global node date is null");
            return;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(imdiHelper.minNodeDate);
        int startYear = calendar.get(Calendar.YEAR);
        dateSlider.setMinimum(startYear);
        calendar.setTime(imdiHelper.maxNodeDate);
        int endYear = calendar.get(Calendar.YEAR);
        dateSlider.setMaximum(endYear);
    }

    public void filterByDate(DefaultMutableTreeNode itemNode, int sliderValue) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, sliderValue);
        Enumeration childNodes = itemNode.children();
        while (childNodes.hasMoreElements()) {
            Object tempNodeObject = ((DefaultMutableTreeNode) childNodes.nextElement()).getUserObject();
            System.out.println("filterByDate: " + tempNodeObject.toString());
            if (imdiHelper.isImdiNode(tempNodeObject)) {
                ((ImdiHelper.ImdiTreeObject) tempNodeObject).setMinDate(calendar.getTime());
            } else {
                System.out.println("not an imdi node: " + tempNodeObject.toString());
            }
        }
    }
// end date filter code
    public void getImdiChildNodes(DefaultMutableTreeNode itemNode) {
        if (itemNode.getChildCount() == 0) {
            if (imdiHelper.isImdiNode(itemNode.getUserObject())) {
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

    public void copyBranchToCashe(JDesktopPane destinationComp, Object selectedNodeUserObject) {
        String dialogTitle = "Copy Brach";
        if (imdiHelper.isImdiNode(selectedNodeUserObject)) {
            boolean moreToLoad = true;
            while (moreToLoad) {
                int[] tempChildCountArray = ((ImdiHelper.ImdiTreeObject) selectedNodeUserObject).getChildCount();
                System.out.println("children not loaded: " + tempChildCountArray[0] + " loaded:" + tempChildCountArray[1]);
                moreToLoad = (tempChildCountArray[0] != 0);
                if (moreToLoad) {
                    if (0 != JOptionPane.showConfirmDialog(destinationComp, tempChildCountArray[0] + " out of " + (tempChildCountArray[0] + tempChildCountArray[1]) + "nodes are not loaded\ndo you want to continue?", "Loading Children", 0)) {
                        return;
                    }
                    ((ImdiHelper.ImdiTreeObject) selectedNodeUserObject).loadNextLevelOfChildren(System.currentTimeMillis() + 100 * 5);
                }
            }
            //String mirrorNameString = JOptionPane.showInputDialog(destinationComp, "Enter a tile for the local mirror");
            String destinationDirectory = linorgSessionStorage.storageDirectory + File.separatorChar + "imdicache";
            File destinationFile = new File(destinationDirectory);
            boolean cacheDirExists = destinationFile.exists();
            if (!cacheDirExists) {
                cacheDirExists = destinationFile.mkdir();
            }
            //destinationDirectory = destinationDirectory + File.separator + mirrorNameString;
            //boolean brachDirCreated = (new File(destinationDirectory)).mkdir();
            // TODO: remove the branch directory and replace it with a named node in the locations settings or just a named imdinode
            if (cacheDirExists) {
                destinationDirectory = destinationDirectory + File.separatorChar;
                JOptionPane.showMessageDialog(destinationComp, "Saving to: " + destinationDirectory, dialogTitle, 0);
                String newNodeLocation = ((ImdiHelper.ImdiTreeObject) selectedNodeUserObject).saveBrachToLocal(destinationDirectory);
                if (newNodeLocation != null) {
                    addLocation("file://" + newNodeLocation);
                // TODO: create an imdinode to contain the name and point to the location
                }
            } else {
                JOptionPane.showMessageDialog(destinationComp, "Could not create the local directory", dialogTitle, 0);
            }
        }
    }

    public void searchSelectedNodes(Vector selectedNodes, String searchString, JPopupMenu jPopupMenu) {
        int[] childCountArray = new int[]{0, 0};
        int messageIconIndex = 0;
        if (selectedNodes.size() == 0) {
            JOptionPane.showMessageDialog(linorgWindowManager.desktopPane, "No nodes are selected", "Search", messageIconIndex);
            return;
        } else {
            SearchDialog searchDialog = new SearchDialog(linorgWindowManager, selectedNodes, searchString);
        //Hashtable foundNodes = searchDialog.getFoundNodes();
//            if (foundNodes.size() > 0) {
//                String frameTitle;
//                if (selectedNodes.size() == 1) {
//                    frameTitle = "Found: " + searchString + " x " + foundNodes.size() + " in " + selectedNodes.get(0).toString();
//                } else {
//                    frameTitle = "Found: " + searchString + " x " + foundNodes.size() + " in " + selectedNodes.size() + " nodes";
//                }
//                openFloatingTable(foundNodes.elements(), frameTitle, jPopupMenu);
//            } else {
//                JOptionPane.showMessageDialog(linorgWindowManager.desktopPane, "\"" + searchString + "\" not found", "Search", messageIconIndex);
//            }
        }

    // count selected nodes and then their child node indicating unopened nodes
    // iterate over allthe selected nodes in the localCorpusTree
//        Enumeration selectedNodesEnum = selectedNodes.elements();
//        while (selectedNodesEnum.hasMoreElements()) {
//            Object currentElement = selectedNodesEnum.nextElement();
//            if (imdiHelper.isImdiNode(currentElement)) {
//                int[] tempChildCountArray = ((ImdiHelper.ImdiTreeObject) currentElement).getChildCount();
//                childCountArray[0] += tempChildCountArray[0];
//                childCountArray[1] += tempChildCountArray[1];
//                System.out.println("children not loaded: " + childCountArray[0] + " loaded:" + childCountArray[1]);
//            }
//        }

//        if (childCountArray[0] > 0 || childCountArray[1] == 0) {
//            if (selectedNodes.size() == 0) {
//                JOptionPane.showMessageDialog(linorgWindowManager.desktopPane, "No nodes are selected", "Search", messageIconIndex);
//                return;
//            }
//            if (childCountArray[1] == 0) {
//                JOptionPane.showMessageDialog(linorgWindowManager.desktopPane, "Of the selected nodes none have been loaded", "Search", messageIconIndex);
//                return;
//            }
//            if (childCountArray[0] > 0) {
//                if (0 != JOptionPane.showConfirmDialog(linorgWindowManager.desktopPane, childCountArray[0] + " out of " + (childCountArray[0] + childCountArray[1]) + "nodes are not loaded\ndo you want to continue?", "Search", messageIconIndex)) {
//                    return;
//                }
//            }
//        }

//        if (searchString == null) {
//            searchString = JOptionPane.showInputDialog(linorgWindowManager.desktopPane, "Enter search term");
//        }

//        // iterate over all the selected nodes in the localCorpusTree
//        Hashtable foundNodes = new Hashtable();
//        selectedNodesEnum = selectedNodes.elements();
//        // show a progress dialog
////        int lengthOfTask = selectedNodes.size();
////        int progressInTask = 0;
////        ProgressMonitor progressMonitor = new ProgressMonitor(destinationComp, "Searching selected nodes and (loaded)subnodes", "", 0, lengthOfTask);
//        while (selectedNodesEnum.hasMoreElements()) {
//            Object currentElement = selectedNodesEnum.nextElement();
//            if (imdiHelper.isImdiNode(currentElement)) {
//                System.out.println("parentNode: " + currentElement);
//                ((ImdiHelper.ImdiTreeObject) currentElement).searchNodes(foundNodes, searchString);
//            // update the progress dialog
////                String message = String.format("done " + progressInTask + " of " + lengthOfTask);//"Completed %d%%.\n", progressInTask);
////                progressMonitor.setNote(message);
////                progressMonitor.setProgress(progressInTask);
////                progressInTask++;
////                if (progressMonitor.isCanceled()) {
////                    progressMonitor.close();
////                    break;
////                }
////            JOptionPane.showMessageDialog(destinationComp, "done " + progressInTask + " of " + lengthOfTask);
//            }
//        }
//        //progressMonitor.close();

//        System.out.println("done");
    }

    public void openImdiXmlWindow(Object userObject) {
        if (imdiHelper.isImdiNode(userObject)) {
            String nodeUrl = ((ImdiHelper.ImdiTreeObject) (userObject)).getUrl();
            String nodeName = ((ImdiHelper.ImdiTreeObject) (userObject)).toString();
            linorgWindowManager.openUrlWindow(nodeName, nodeUrl);
        }
    }

    // TODO: this could be merged witht the add row function
    public AbstractTableModel getImdiTableModel(Hashtable rowNodes) {
        ImdiHelper.ImdiTableModel searchTableModel = imdiHelper.getImdiTableModel();
        searchTableModel.setShowIcons(true);
        searchTableModel.addImdiObjects(rowNodes.elements());
        //Enumeration rowNodeEnum = rowNodes.elements();
        //while (rowNodeEnum.hasMoreElements()) {
        //searchTableModel.addImdiObject((ImdiHelper.ImdiTreeObject) rowNodeEnum.nextElement());
        //}
        return searchTableModel;
    }

    public AbstractTableModel getImdiTableModel() {
        ImdiHelper.ImdiTableModel tempModel = imdiHelper.getImdiTableModel();
        tempModel.setShowIcons(true);
        return tempModel;
    }

    public void addToGridData(TableModel tableModel, Vector nodesToAdd) {
        for (Enumeration nodesToAddEnum = nodesToAdd.elements(); nodesToAddEnum.hasMoreElements();) {
            // iterate over the and add supplied nodes
            addToGridData(tableModel, nodesToAddEnum.nextElement());
//            Object currentObject = nodesToAddEnum.nextElement();
//            if (imdiHelper.isImdiNode(currentObject)) {
//                String hashKey = ((ImdiHelper.ImdiTreeObject) currentObject).getUrl();
//                if (selectedFilesList.containsKey(hashKey)) {
//                    // remove any image nodes from the image window                
//                    //System.out.println("removing from images");
//                    selectedFilesPanel.remove((Component) selectedFilesList.remove(hashKey));
//                    selectedFilesPanel.revalidate();
//                    selectedFilesPanel.repaint();
//                    // remove any map layers
//                    if (mapView.isGisFile(hashKey)) {
//                        mapView.removeLayer(hashKey);
//                    }
//                }
//            }
        }
    }

    public void addToGridData(TableModel tableModel, Object itemNode) {
        // there is no point loading the child nodes to display the parent node in a grid, however if the child nodes are requested for display then at that point they will need to be loaded but not at this point
        //getImdiChildNodes(itemNode); // load the child nodes and the fields for each
        if (imdiHelper.isImdiNode(itemNode)) {
            ImdiHelper.ImdiTreeObject itemImdiTreeObject = (ImdiHelper.ImdiTreeObject) itemNode;
            String hashKey = itemImdiTreeObject.getUrl();
            System.out.println("hashkey: " + hashKey);
            if (itemImdiTreeObject.isImdi()) {
                ((ImdiHelper.ImdiTableModel) tableModel).addSingleImdiObject(itemImdiTreeObject);
            }
            if (!itemImdiTreeObject.isImdi() || itemImdiTreeObject.getResource() != null) {
                // TODO: display non imdi file
                // TODO: move the display of resources and files into a separate class
                // TODO: replace selectedFilesList but using the name propetry of the added component for the purpose of removing it later
                System.out.println("display non imdi file: " + itemImdiTreeObject.getUrl());
                String imageFileName;
                if (itemImdiTreeObject.getResource() != null) {
                    imageFileName = itemImdiTreeObject.getResource();
                } else {
                    imageFileName = itemImdiTreeObject.getUrl();
                }
                if (selectedFilesPanel == null) {
                    selectedFilesPanel = new JPanel();
                    selectedFilesPanel.setLayout(new java.awt.GridLayout(6, 6));
                    linorgWindowManager.createWindow("Selected Files", selectedFilesPanel);
                }
                imageFileName = imageFileName.replace("file://", "");
                ImageIcon nodeImage = new ImageIcon(imageFileName);
                JLabel imageLabel = new JLabel(itemImdiTreeObject.toString(), nodeImage, JLabel.CENTER);
                //Set the position of the text, relative to the icon:
                imageLabel.setVerticalTextPosition(JLabel.BOTTOM);
                imageLabel.setHorizontalTextPosition(JLabel.CENTER);
                selectedFilesPanel.add(imageLabel);
                selectedFilesList.put(hashKey, imageLabel);
                selectedFilesPanel.revalidate();
                selectedFilesPanel.repaint();
//                if (mapView == null) {
//                    mapView = new MapView();
//                    linorgWindowManager.createWindow("GIS Viewer", mapView);
//                }
//                if (mapView.isGisFile(hashKey)) {
//                    mapView.addLayer(hashKey, imageFileName);
//                    mapView.setVisible(true);
//                }
            }
        }
    }

    public void removeAllFromGridData(TableModel tableModel) {
        System.out.println("removing all images");
        if (selectedFilesPanel != null) {
            selectedFilesPanel.removeAll();
            selectedFilesPanel.revalidate();
            selectedFilesPanel.repaint();
        }
//        if (mapView != null) {
//            mapView.removeAll();
//        }
        selectedFilesList.clear();
        ((ImdiHelper.ImdiTableModel) tableModel).removeAllImdiRows();
    }

    public void removeFromGridData(TableModel tableModel, Vector nodesToRemove) {
        // remove the supplied nodes from the grid
        ((ImdiHelper.ImdiTableModel) tableModel).removeImdiObjects(nodesToRemove.elements());
        for (Enumeration nodesToRemoveEnum = nodesToRemove.elements(); nodesToRemoveEnum.hasMoreElements();) {
            // iterate over the supplied nodes
            Object currentObject = nodesToRemoveEnum.nextElement();
            if (imdiHelper.isImdiNode(currentObject)) {
                String hashKey = ((ImdiHelper.ImdiTreeObject) currentObject).getUrl();
                if (selectedFilesList.containsKey(hashKey)) {
                    // remove any image nodes from the image window                
                    //System.out.println("removing from images");
                    selectedFilesPanel.remove((Component) selectedFilesList.remove(hashKey));
                    selectedFilesPanel.revalidate();
                    selectedFilesPanel.repaint();
                    // remove any map layers
//                    if (mapView.isGisFile(hashKey)) {
//                        mapView.removeLayer(hashKey);
//                    }
                }
            }
        }
    }

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

    public void copyNodeUrlToClipboard(DefaultMutableTreeNode selectedNode) {
        if (imdiHelper.isImdiNode(selectedNode.getUserObject())) {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            StringSelection stringSelection = new StringSelection(((ImdiHelper.ImdiTreeObject) selectedNode.getUserObject()).getUrl());
            clipboard.setContents(stringSelection, clipboardOwner);
        }
    }

    public void applyRootLocations(DefaultMutableTreeNode localDirectoryNode, DefaultMutableTreeNode localCorpusNode, DefaultMutableTreeNode remoteCorpusNode) {
        Enumeration locationEnum = locationsList.elements();
        while (locationEnum.hasMoreElements()) {
            String currentLocation = locationEnum.nextElement().toString();
            System.out.println("currentLocation: " + currentLocation);
            if (imdiHelper.isStringLocal(currentLocation)) {
                // is local
                if (imdiHelper.isStringImdi(currentLocation)) {
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

    public javax.swing.table.DefaultTableModel getLocationsTableModel() {
        Object[][] tableObjectAray = new Object[locationsList.size()][2];
        Enumeration locationEnum = locationsList.elements();
        int rowCounter = 0;
        while (locationEnum.hasMoreElements()) {
            tableObjectAray[rowCounter][1] = locationEnum.nextElement();
            if (imdiHelper.isStringImdi(tableObjectAray[rowCounter][1].toString())) {
                // is an imdi
                if (imdiHelper.isStringLocal(tableObjectAray[rowCounter][1].toString())) {
                    tableObjectAray[rowCounter][0] = (Object) ImdiHelper.corpuslocalicon;
                } else {
                    tableObjectAray[rowCounter][0] = ImdiHelper.corpusservericon;
                }
            } else {
                // is not an imdi
                if (imdiHelper.isStringLocal(tableObjectAray[rowCounter][1].toString())) {
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

    public boolean addLocation(String addedLocation) {
        System.out.println("addLocation" + addedLocation);
        if (!locationsList.contains(addedLocation)) {
            locationsList.add(addedLocation);
            return true;
        }
        return false;
    }

    public void removeLocation(Object removeObject) {
        if (imdiHelper.isImdiNode(removeObject)) {
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
            if (imdiHelper.isImdiNode(node.getUserObject())) {
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


