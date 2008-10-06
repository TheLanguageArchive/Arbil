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
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author petwit
 */
public class GuiHelper {

    static ImdiHelper imdiHelper;
    static ImdiFieldViews imdiFieldViews;
    static TreeHelper treeHelper = new TreeHelper();
    static LinorgSessionStorage linorgSessionStorage;
    static ImdiDragDrop imdiDragDrop = new ImdiDragDrop();
    private Hashtable selectedFilesList = new Hashtable(); // this is a list of the files currently displayed in the files window
    //private MapView mapView;
    private JPanel selectedFilesPanel;
    static LinorgWindowManager linorgWindowManager;
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
        treeHelper.loadLocationsList();
    }

    public void setWindowManager(LinorgWindowManager localLinorgWindowManager) {
        linorgWindowManager = localLinorgWindowManager;
    }

    public void saveState() {
        imdiHelper.saveMd5sumIndex();
        imdiFieldViews.saveViewsToFile();
        treeHelper.saveLocations();
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

    public void searchSelectedNodes(Vector selectedNodes, String searchString, JPopupMenu jPopupMenu) {
        //int[] childCountArray = new int[]{0, 0};
        int messageIconIndex = 0;
        if (selectedNodes.size() == 0) {
            JOptionPane.showMessageDialog(linorgWindowManager.desktopPane, "No nodes are selected", "Search", messageIconIndex);
            return;
        } else {
            ThreadedDialog threadedDialog = new ThreadedDialog();
            threadedDialog.searchNodes(selectedNodes, searchString);
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
        ImdiTableModel searchTableModel = new ImdiTableModel();
        searchTableModel.setShowIcons(true);
        searchTableModel.addImdiObjects(rowNodes.elements());
        //Enumeration rowNodeEnum = rowNodes.elements();
        //while (rowNodeEnum.hasMoreElements()) {
        //searchTableModel.addImdiObject((ImdiHelper.ImdiTreeObject) rowNodeEnum.nextElement());
        //}
        return searchTableModel;
    }

    public AbstractTableModel getImdiTableModel() {
        ImdiTableModel tempModel = new ImdiTableModel();
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
                ((ImdiTableModel) tableModel).addSingleImdiObject(itemImdiTreeObject);
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
        ((ImdiTableModel) tableModel).removeAllImdiRows();
    }

    public void removeFromGridData(TableModel tableModel, Vector nodesToRemove) {
        // remove the supplied nodes from the grid
        ((ImdiTableModel) tableModel).removeImdiObjects(nodesToRemove.elements());
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

    public void copyNodeUrlToClipboard(DefaultMutableTreeNode selectedNode) {
        if (imdiHelper.isImdiNode(selectedNode.getUserObject())) {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            StringSelection stringSelection = new StringSelection(((ImdiHelper.ImdiTreeObject) selectedNode.getUserObject()).getUrl());
            clipboard.setContents(stringSelection, clipboardOwner);
        }
    }
}


