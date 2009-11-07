package nl.mpi.arbil;

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

    static ArbilDragDrop arbilDragDrop = new ArbilDragDrop();
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
        TreeHelper.getSingleInstance().saveLocations(null, null);
        if (saveWindows) {
            LinorgWindowManager.getSingleInstance().saveWindowStates();
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
                    try {
                        ImdiFieldViews.getSingleInstance().setCurrentGlobalViewName(((Component) evt.getSource()).getName());
                    } catch (Exception ex) {
                        GuiHelper.linorgBugCatcher.logError(ex);
                    }
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
            if (((ImdiTreeObject) (userObject)).getNeedsSaveToDisk()) {
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
                    // 1. Instantiate a TransformerFactory.
                    javax.xml.transform.TransformerFactory tFactory = javax.xml.transform.TransformerFactory.newInstance();
                    // 2. Use the TransformerFactory to process the stylesheet Source and generate a Transformer.
                    URL xslUrl = this.getClass().getResource("/nl/mpi/arbil/resources/xsl/imdi-viewer.xsl");
                    File tempHtmlFile;
                    File xslFile = null;
                    if (imdiSchema.selectedTemplateDirectory != null) {
                        xslFile = new File(imdiSchema.selectedTemplateDirectory.toString() + File.separatorChar + "format.xsl");
                    }
                    if (xslFile != null && xslFile.exists()) {
                        xslUrl = xslFile.toURL();
                        tempHtmlFile = File.createTempFile("tmp", ".html", xslFile.getParentFile());
                        tempHtmlFile.deleteOnExit();
                    } else {
                        // copy any dependent files from the jar
                        String[] dependentFiles = {"imdi-viewer-open.gif", "imdi-viewer-closed.gif", "imdi-viewer.js", "additTooltip.js", "additPopup.js", "imdi-viewer.css", "additTooltip.css"};
                        tempHtmlFile = File.createTempFile("tmp", ".html");
                        tempHtmlFile.deleteOnExit();
                        for (String dependantFileString : dependentFiles) {
                            File tempDependantFile = new File(tempHtmlFile.getParent() + File.separatorChar + dependantFileString);
                            tempDependantFile.deleteOnExit();
//                        File tempDependantFile = File.createTempFile(dependantFileString, "");
                            FileOutputStream outFile = new FileOutputStream(tempDependantFile);
                            //InputStream inputStream = this.getClass().getResourceAsStream("html/imdi-viewer/" + dependantFileString);
                            InputStream inputStream = this.getClass().getResourceAsStream("/nl/mpi/arbil/resources/xsl/" + dependantFileString);
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
                    transformer.transform(new javax.xml.transform.stream.StreamSource(nodeFile), new javax.xml.transform.stream.StreamResult(new java.io.FileOutputStream(tempHtmlFile.getCanonicalPath())));
                    if (!launchInBrowser) {
                        LinorgWindowManager.getSingleInstance().openUrlWindowOnce(nodeName + " formatted", tempHtmlFile.toURL());
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
                    LinorgWindowManager.getSingleInstance().addMessageDialogToQueue(line, "Open In External Application");
                    System.out.println("Launched process error stream: \"" + line + "\"");
                }
                result = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}


