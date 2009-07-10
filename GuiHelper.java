package mpi.linorg;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.ButtonGroup;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Document   : GuiHelper
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
public class GuiHelper {

    static LinorgSessionStorage linorgSessionStorage = new LinorgSessionStorage();
    static ImdiDragDrop imdiDragDrop = new ImdiDragDrop();
    static LinorgJournal linorgJournal = new LinorgJournal();
    static ImdiSchema imdiSchema = new ImdiSchema();
    static LinorgBugCatcher linorgBugCatcher = new LinorgBugCatcher();
    static ImdiLoader imdiLoader = new ImdiLoader();
//    private JPanel selectedFilesPanel;
    //static LinorgWindowManager linorgWindowManager = new LinorgWindowManager();
    // create a clip board owner for copy and paste actions
    static ClipboardOwner clipboardOwner = new ClipboardOwner() {

        public void lostOwnership(Clipboard clipboard, Transferable contents) {
            System.out.println("lost clipboard ownership");
        //throw new UnsupportedOperationException("Not supported yet.");
        }
    };
    
    static private GuiHelper singleInstance = null;

    static synchronized public GuiHelper getSingleInstance() {
        System.out.println("GuiHelper getSingleInstance");
        if (singleInstance == null) {
            singleInstance = new GuiHelper();
        }
        return singleInstance;
    }

    private GuiHelper() {
        LinorgFavourites.getSingleInstance(); // cause the favourites imdi nodes to be loaded        
    }

    public void saveState(boolean saveWindows) {
        ImdiFieldViews.getSingleInstance().saveViewsToFile();
        // linorgFavourites.saveSelectedFavourites(); // no need to do here because the list is saved when favourites are changed
        TreeHelper.getSingleInstance().saveLocations();
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
                    DefaultMutableTreeNode targetNode = TreeHelper.getSingleInstance().getLocalCorpusTreeSingleSelection();
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
                    ImdiTreeObject templateImdiObject = GuiHelper.imdiLoader.getImdiObject(null, imdiFavouriteUrlString);
                    DefaultMutableTreeNode targetNode = TreeHelper.getSingleInstance().getLocalCorpusTreeSingleSelection();
                    ImdiTreeObject imdiTreeObject;
                    if (ImdiTreeObject.isImdiNode(targetNode.getUserObject())) {
                        imdiTreeObject = (ImdiTreeObject) targetNode.getUserObject();
//                        imdiTreeObject.requestMerge(imdiLoader.getImdiObject("", imdiTemplateUrlString));
                        imdiTreeObject.requestAddNode(LinorgFavourites.getSingleInstance().getNodeType(templateImdiObject, imdiTreeObject), ((JMenuItem) evt.getSource()).getText(), imdiFavouriteUrlString, null, null);
                    }
//                    treeHelper.getImdiChildNodes(targetNode);
//                    String addedNodeUrlString = treeHelper.addImdiChildNode(targetNode, linorgFavourites.getNodeType(imdiTemplateUrlString), ((JMenuItem) evt.getSource()).getText());
//                    imdiLoader.getImdiObject("", addedNodeUrlString).requestMerge(imdiLoader.getImdiObject("", imdiTemplateUrlString));
//                    loop child nodes and insert them into the new node
//                    ImdiTreeObject templateImdiObject = GuiHelper.imdiLoader.getImdiObject("", imdiTemplateUrlString);
//                    ImdiTreeObject targetImdiObject = GuiHelper.imdiLoader.getImdiObject("", addedNodeUrl);
//                    
//                    for (Enumeration<ImdiTreeObject> childTemplateEnum = templateImdiObject.getChildEnum(); childTemplateEnum.hasMoreElements();) {
//                        ImdiTreeObject currentTemplateChild = childTemplateEnum.nextElement();
//                        String addedNodeUrl = treeHelper.addImdiChildNode(targetNode, linorgFavourites.getNodeType(currentTemplateChild.getUrlString()), currentTemplateChild.toString());
//                        linorgFavourites.mergeFromFavourite(addedNodeUrl, imdiTemplateUrlString, true);
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
            viewLabelRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {

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

    public void openImdiXmlWindow(Object userObject, boolean formatXml, boolean launchInBrowser) {
        if (userObject instanceof ImdiTreeObject) {
            if (((ImdiTreeObject) (userObject)).imdiNeedsSaveToDisk) {
                if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(LinorgWindowManager.getSingleInstance().linorgFrame, "The node must be saved first.\nSave now?", "View IMDI XML", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)) {
                    ((ImdiTreeObject) (userObject)).saveChangesToCache(true);
                } else {
                    return;
                }
            }
            File nodeFile = ((ImdiTreeObject) (userObject)).getFile();
            System.out.println("openImdiXmlWindow: " + nodeFile);
            String nodeName = ((ImdiTreeObject) (userObject)).toString();
            if (formatXml) {
                try {
                    javax.xml.transform.TransformerFactory tFactory = javax.xml.transform.TransformerFactory.newInstance();
                    // 2. Use the TransformerFactory to process the stylesheet Source and generate a Transformer.
                    URL xslUrl = this.getClass().getResource("/mpi/linorg/resources/xsl/IMDI_3_0_TO_WEB.xsl");
                    File tempHtmlFile;
                    File xslFile = null;
                    if (imdiSchema.selectedTemplateDirectory != null) {
                        xslFile = new File(imdiSchema.selectedTemplateDirectory.toString() + File.separatorChar + "format.xsl");
                    }
                    if (xslFile != null && xslFile.exists()) {
                        xslUrl = xslFile.toURL();
                        tempHtmlFile = File.createTempFile(nodeFile.getName(), ".html", xslFile.getParentFile());
                        tempHtmlFile.deleteOnExit();
                    } else {
                        // copy any dependent files from the jar
                        String[] dependentFiles = {"imdi-viewer-open.gif", "imdi-viewer-closed.gif", "imdi-viewer.js", "additTooltip.js", "additPopup.js", "imdi-viewer.css", "additTooltip.css"};
                        tempHtmlFile = File.createTempFile(nodeFile.getName(), ".html");
                    tempHtmlFile.deleteOnExit();
                    for (String dependantFileString : dependentFiles) {
                        File tempDependantFile = new File(tempHtmlFile.getParent() + File.separatorChar + dependantFileString);
                        tempDependantFile.deleteOnExit();
//                        File tempDependantFile = File.createTempFile(dependantFileString, "");
                        FileOutputStream outFile = new FileOutputStream(tempDependantFile);
                        //InputStream inputStream = this.getClass().getResourceAsStream("html/imdi-viewer/" + dependantFileString);
                        InputStream inputStream = this.getClass().getResourceAsStream("/mpi/linorg/resources/xsl/" + dependantFileString);
                        int bufferLength = 1024 * 4;
                        byte[] buffer = new byte[bufferLength]; // make htis 1024*4 or something and read chunks not the whole file
                        int bytesread = 0;
                        while (bytesread >= 0) {
                            bytesread = inputStream.read(buffer);
                            if (bytesread == -1) {
                                break;
                            }
                            outFile.write(buffer, 0, bytesread);
                        }
                        outFile.close();
                    }
                    }
                    javax.xml.transform.Transformer transformer = tFactory.newTransformer(new javax.xml.transform.stream.StreamSource(xslUrl.toString()));
                    // 3. Use the Transformer to transform an XML Source and send the output to a Result object.
                    transformer.transform(new javax.xml.transform.stream.StreamSource(nodeFile), new javax.xml.transform.stream.StreamResult(tempHtmlFile));
                    if (!launchInBrowser) {
                        LinorgWindowManager.getSingleInstance().openUrlWindowOnce(nodeName + "-transformed", tempHtmlFile.toURL());
                    } else {
                        openFileInExternalApplication(tempHtmlFile.toURI());
                    }
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                //System.out.println(ex.getMessage());
                //LinorgWindowManager.getSingleInstance().openUrlWindow(nodeName, nodeUrl);
                }
            } else {
                try {
                    LinorgWindowManager.getSingleInstance().openUrlWindowOnce(nodeName + "-xml", nodeFile.toURL());
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                //System.out.println(ex.getMessage());
                //LinorgWindowManager.getSingleInstance().openUrlWindow(nodeName, nodeUrl);
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
            if (!itemImdiTreeObject.isDirectory()) {
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

    public boolean openFileInExternalApplication(URI targetUri) {
        boolean result = false;
        boolean awtDesktopFound = false;
        try {
            Class.forName("java.awt.Desktop");
            awtDesktopFound = true;
        } catch (ClassNotFoundException cnfE) {
            awtDesktopFound = false;
            System.out.println("java.awt.Desktop class not found");
        }
        if (awtDesktopFound) {
            try {
                Desktop.getDesktop().browse(targetUri);
                result = true;
            } catch (MalformedURLException muE) {
                muE.printStackTrace();
            } catch (IOException ioE) {
                ioE.printStackTrace();
            }
        } else {
            try {
                String osNameString = System.getProperty("os.name").toLowerCase();
                String openCommand = "";
                if (osNameString.indexOf("windows") != -1 || osNameString.indexOf("nt") != -1) {
                    openCommand = "cmd /c start ";
                }
                if (osNameString.equals("windows 95") || osNameString.equals("windows 98")) {
                    openCommand = "command.com /C start ";
                }
                if (osNameString.indexOf("mac") != -1) {
                    openCommand = "open ";
                }
                if (osNameString.indexOf("linux") != -1) {
                    openCommand = "gnome-open ";
                }
                String execString = openCommand + targetUri;
                System.out.println(execString);
                Process launchedProcess = Runtime.getRuntime().exec(execString);
                BufferedReader errorStreamReader = new BufferedReader(new InputStreamReader(launchedProcess.getErrorStream()));
                String line;
                while ((line = errorStreamReader.readLine()) != null) {
                    System.out.println("Launched process error stream: \"" + line + "\"");
                }
                result = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public void removeFromGridData(TableModel tableModel, Vector nodesToRemove) {
        // remove the supplied nodes from the grid
        ((ImdiTableModel) tableModel).removeImdiObjects(nodesToRemove.elements());
        for (Enumeration nodesToRemoveEnum = nodesToRemove.elements(); nodesToRemoveEnum.hasMoreElements();) {
            // iterate over the supplied nodes
            Object currentObject = nodesToRemoveEnum.nextElement();
            if (ImdiTreeObject.isImdiNode(currentObject)) {
                String hashKey = ((ImdiTreeObject) currentObject).getUrlString();
            }
        }
    }
}


