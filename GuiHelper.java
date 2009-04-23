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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.ButtonGroup;
import javax.swing.JMenuItem;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author petwit
 */
public class GuiHelper {

    static TreeHelper treeHelper = new TreeHelper();
    static LinorgSessionStorage linorgSessionStorage = new LinorgSessionStorage();
    static ImdiDragDrop imdiDragDrop = new ImdiDragDrop();
    static LinorgJournal linorgJournal = new LinorgJournal();
    static ImdiSchema imdiSchema = new ImdiSchema();
    static LinorgBugCatcher linorgBugCatcher = new LinorgBugCatcher();
    static ImdiLoader imdiLoader = new ImdiLoader();
    private Hashtable selectedFilesList = new Hashtable(); // this is a list of the files currently displayed in the files window
//    private JPanel selectedFilesPanel;
    //static LinorgWindowManager linorgWindowManager = new LinorgWindowManager();
    // create a clip board owner for copy and paste actions
    static ClipboardOwner clipboardOwner = new ClipboardOwner() {

        public void lostOwnership(Clipboard clipboard, Transferable contents) {
            System.out.println("lost clipboard ownership");
        //throw new UnsupportedOperationException("Not supported yet.");
        }
    };

    public GuiHelper() {
        //imdiHelper = new ImdiIcons();
        treeHelper.loadLocationsList();
    }

    public void saveState(boolean saveWindows) {
        ImdiFieldViews.getSingleInstance().saveViewsToFile();
        // linorgTemplates.saveSelectedTemplates(); // no need to do here because the list is saved when templates are changed
        treeHelper.saveLocations();
        if (saveWindows) {
            LinorgWindowManager.getSingleInstance().saveWindowStates();
        }
    }

    public void initAddMenu(javax.swing.JMenu addMenu, Object targetNodeUserObject) {
        addMenu.removeAll();
//        System.out.println("initAddMenu: " + targetNodeUserObject);
        for (Enumeration menuItemName = imdiSchema.listTypesFor(targetNodeUserObject); menuItemName.hasMoreElements();) {
            String[] currentField = (String[]) menuItemName.nextElement();
//            System.out.println("MenuText: " + currentField[0]);
//            System.out.println("ActionCommand: " + currentField[1]);

            JMenuItem addMenuItem;
            addMenuItem = new javax.swing.JMenuItem();
            addMenuItem.setText(currentField[0]);
            addMenuItem.setName(currentField[0]);
            addMenuItem.setActionCommand(currentField[1]);
            addMenuItem.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    DefaultMutableTreeNode targetNode = treeHelper.getLocalCorpusTreeSingleSelection();
                    ImdiTreeObject imdiTreeObject;
                    if (ImdiTreeObject.isImdiNode(targetNode.getUserObject())) {
                        imdiTreeObject = (ImdiTreeObject) targetNode.getUserObject();
                    } else {
                        imdiTreeObject = new ImdiTreeObject("temp root node", GuiHelper.linorgSessionStorage.getSaveLocation("unattachedcorpus.imdi"));
                    }
                    imdiTreeObject.requestAddNode(evt.getActionCommand(), ((JMenuItem) evt.getSource()).getText(), null, null, null);
                }
            });
            addMenu.add(addMenuItem);
        }
//                String emptyMenuName = "cannot add here";
//                addMenuItem.setText(emptyMenuName);
//                addMenuItem.setName(emptyMenuName);
//                addMenuItem.setEnabled(false);
//                addMenu.add(addMenuItem);
    }

    public void initAddFromFavouritesMenu(javax.swing.JMenu addFromFavouritesMenu, Object targetNodeUserObject) {
        addFromFavouritesMenu.removeAll();
        for (Enumeration menuItemName = LinorgFavourites.getSingleInstance().listFavouritesFor(targetNodeUserObject); menuItemName.hasMoreElements();) {
            String[] currentField = (String[]) menuItemName.nextElement();
//            System.out.println("MenuText: " + currentField[0]);
//            System.out.println("ActionCommand: " + currentField[1]);

            JMenuItem addMenuItem;
            addMenuItem = new javax.swing.JMenuItem();
            addMenuItem.setText(currentField[0]);
            addMenuItem.setName(currentField[0]);
            addMenuItem.setActionCommand(currentField[1]);
            addMenuItem.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    String imdiFavouriteUrlString = evt.getActionCommand();
                    DefaultMutableTreeNode targetNode = treeHelper.getLocalCorpusTreeSingleSelection();
                    ImdiTreeObject imdiTreeObject;
                    if (ImdiTreeObject.isImdiNode(targetNode.getUserObject())) {
                        imdiTreeObject = (ImdiTreeObject) targetNode.getUserObject();
//                        imdiTreeObject.requestMerge(imdiLoader.getImdiObject("", imdiTemplateUrlString));
                        imdiTreeObject.requestAddNode(LinorgFavourites.getSingleInstance().getNodeType(imdiFavouriteUrlString), ((JMenuItem) evt.getSource()).getText(), imdiFavouriteUrlString, null, null);
                    }
//                    treeHelper.getImdiChildNodes(targetNode);
//                    String addedNodeUrlString = treeHelper.addImdiChildNode(targetNode, linorgTemplates.getNodeType(imdiTemplateUrlString), ((JMenuItem) evt.getSource()).getText());
//                    imdiLoader.getImdiObject("", addedNodeUrlString).requestMerge(imdiLoader.getImdiObject("", imdiTemplateUrlString));
//                    loop child nodes and insert them into the new node
//                    ImdiTreeObject templateImdiObject = GuiHelper.imdiLoader.getImdiObject("", imdiTemplateUrlString);
//                    ImdiTreeObject targetImdiObject = GuiHelper.imdiLoader.getImdiObject("", addedNodeUrl);
//                    
//                    for (Enumeration<ImdiTreeObject> childTemplateEnum = templateImdiObject.getChildEnum(); childTemplateEnum.hasMoreElements();) {
//                        ImdiTreeObject currentTemplateChild = childTemplateEnum.nextElement();
//                        String addedNodeUrl = treeHelper.addImdiChildNode(targetNode, linorgTemplates.getNodeType(currentTemplateChild.getUrlString()), currentTemplateChild.toString());
//                        linorgTemplates.mergeFromTemplate(addedNodeUrl, imdiTemplateUrlString, true);
//                    }
//                    treeHelper.reloadLocalCorpusTree(targetNode);
                }
            });
            addFromFavouritesMenu.add(addMenuItem);
        }
    }

    public void initViewMenu(javax.swing.JMenu viewMenu) {
        viewMenu.removeAll();
        ButtonGroup viewMenuButtonGroup = new javax.swing.ButtonGroup();
        //String[] viewLabels = guiHelper.imdiFieldViews.getSavedFieldViewLables();
        for (Enumeration menuItemName = ImdiFieldViews.getSingleInstance().getSavedFieldViewLables(); menuItemName.hasMoreElements();) {
            String currentMenuName = menuItemName.nextElement().toString();
            javax.swing.JRadioButtonMenuItem viewLabelRadioButtonMenuItem;
            viewLabelRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
            viewMenuButtonGroup.add(viewLabelRadioButtonMenuItem);
            viewLabelRadioButtonMenuItem.setSelected(ImdiFieldViews.getSingleInstance().getCurrentGlobalViewName().equals(currentMenuName));
            viewLabelRadioButtonMenuItem.setText(currentMenuName);
            viewLabelRadioButtonMenuItem.setName(currentMenuName);
            viewLabelRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener () {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                    ImdiFieldViews.getSingleInstance().setCurrentGlobalViewName(((Component) evt.getSource()).getName());
                }
            });
            viewMenu.add(viewLabelRadioButtonMenuItem);
        }
    }
//// date filter code
//    public void updateDateSlider(JSlider dateSlider) {
//        if (imdiHelper.minNodeDate == null) {
//            System.out.println("global node date is null");
//            return;
//        }
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(imdiHelper.minNodeDate);
//        int startYear = calendar.get(Calendar.YEAR);
//        dateSlider.setMinimum(startYear);
//        calendar.setTime(imdiHelper.maxNodeDate);
//        int endYear = calendar.get(Calendar.YEAR);
//        dateSlider.setMaximum(endYear);
//    }
//
//    public void filterByDate(DefaultMutableTreeNode itemNode, int sliderValue) {
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.YEAR, sliderValue);
//        Enumeration childNodes = itemNode.children();
//        while (childNodes.hasMoreElements()) {
//            Object tempNodeObject = ((DefaultMutableTreeNode) childNodes.nextElement()).getUserObject();
//            System.out.println("filterByDate: " + tempNodeObject.toString());
//            if (imdiHelper.isImdiNode(tempNodeObject)) {
//                ((ImdiHelper.ImdiTreeObject) tempNodeObject).setMinDate(calendar.getTime());
//            } else {
//                System.out.println("not an imdi node: " + tempNodeObject.toString());
//            }
//        }
//    }
//// end date filter code

    public void openImdiXmlWindow(Object userObject, boolean formatXml) {
        if (userObject instanceof ImdiTreeObject) {
            File nodeFile = ((ImdiTreeObject) (userObject)).getFile();
            System.out.println("openImdiXmlWindow: " + nodeFile);
            String nodeName = ((ImdiTreeObject) (userObject)).toString();
            if (formatXml) {
                try {
                    // 1. Instantiate a TransformerFactory.
                    javax.xml.transform.TransformerFactory tFactory = javax.xml.transform.TransformerFactory.newInstance();
                    // 2. Use the TransformerFactory to process the stylesheet Source and generate a Transformer.
                    javax.xml.transform.Transformer transformer = tFactory.newTransformer(new javax.xml.transform.stream.StreamSource(this.getClass().getResource("/mpi/linorg/resources/xsl/IMDI_3_0_TO_WEB.xsl").toString()));
                    // 3. Use the Transformer to transform an XML Source and send the output to a Result object.
                    transformer.transform(new javax.xml.transform.stream.StreamSource(nodeFile), new javax.xml.transform.stream.StreamResult(new java.io.FileOutputStream(nodeFile.getCanonicalPath() + ".html")));
                    LinorgWindowManager.getSingleInstance().openUrlWindowOnce(nodeName + "-transformed", new File(nodeFile.getCanonicalPath() + ".html").toURL());
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                //System.out.println(ex.getMessage());
                //linorgWindowManager.openUrlWindow(nodeName, nodeUrl);
                }
            } else {
                try {
                    LinorgWindowManager.getSingleInstance().openUrlWindowOnce(nodeName + "-xml", nodeFile.toURL());
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                //System.out.println(ex.getMessage());
                //linorgWindowManager.openUrlWindow(nodeName, nodeUrl);
                }
            }
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
        if (ImdiTreeObject.isImdiNode(itemNode)) {
            ImdiTreeObject itemImdiTreeObject = (ImdiTreeObject) itemNode;
            String hashKey = itemImdiTreeObject.getUrlString();
            System.out.println("hashkey: " + hashKey);
            if (itemImdiTreeObject.isImdi()) {
                ((ImdiTableModel) tableModel).addSingleImdiObject(itemImdiTreeObject);
            }
//            if (!itemImdiTreeObject.isImdi() || itemImdiTreeObject.getResource() != null) {
//                // TODO: display non imdi file
//                // TODO: move the display of resources and files into a separate class
//                // TODO: replace selectedFilesList but using the name propetry of the added component for the purpose of removing it later
//                System.out.println("display non imdi file: " + itemImdiTreeObject.getUrlString());
//                String imageFileName;
//                if (itemImdiTreeObject.getResource() != null) {
//                    imageFileName = itemImdiTreeObject.getResource();
//                } else {
//                    imageFileName = itemImdiTreeObject.getUrlString();
//                }
////                if (selectedFilesPanel == null) {
////                    selectedFilesPanel = new JPanel();
////                    selectedFilesPanel.setLayout(new java.awt.GridLayout(6, 6));
////                    linorgWindowManager.createWindow("Selected Files", selectedFilesPanel);
////                }
//                //imageFileName = imageFileName.replace("file:", "");
//                try {
//                System.out.println("imageFileName: " + new URL(imageFileName).getFile());
//                    //ImageIcon nodeImage = new ImageIcon(new URL(imageFileName).getFile());
//                ImageIcon nodeImage = ImdiIcons.directoryIcon;
//                    JLabel imageLabel = new JLabel(itemImdiTreeObject.toString(), nodeImage, JLabel.CENTER);
//                    //Set the position of the text, relative to the icon:
//                    imageLabel.setVerticalTextPosition(JLabel.BOTTOM);
//                    imageLabel.setHorizontalTextPosition(JLabel.CENTER);
////                selectedFilesPanel.add(imageLabel);
////                selectedFilesList.put(hashKey, imageLabel);
////                selectedFilesPanel.revalidate();
////                selectedFilesPanel.repaint();
//                    if (MapView.isGisFile(hashKey)) {
//                        if (mapView == null) {
//                            mapView = new MapView();
//                            linorgWindowManager.createWindow("GIS Viewer", mapView);
//                        }
//                        mapView.addLayer(hashKey, imageFileName);
//                        mapView.setVisible(true);
//                    }
//                } catch (Exception ex) {
//                    GuiHelper.linorgBugCatcher.logError(ex);
//                }
//            }
        }
    }

    public void removeAllFromGridData(TableModel tableModel) {
        System.out.println("removing all images");
//        if (selectedFilesPanel != null) {
//            selectedFilesPanel.removeAll();
//            selectedFilesPanel.revalidate();
//            selectedFilesPanel.repaint();
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
            if (ImdiTreeObject.isImdiNode(currentObject)) {
                String hashKey = ((ImdiTreeObject) currentObject).getUrlString();
                if (selectedFilesList.containsKey(hashKey)) {
                    // remove any image nodes from the image window                
                    //System.out.println("removing from images");
//                    selectedFilesPanel.remove((Component) selectedFilesList.remove(hashKey));
//                    selectedFilesPanel.revalidate();
//                    selectedFilesPanel.repaint();
                }
            }
        }
    }
}


