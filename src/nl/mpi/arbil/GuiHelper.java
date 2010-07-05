package nl.mpi.arbil;

import nl.mpi.arbil.data.ImdiTreeObject;
import nl.mpi.arbil.MetadataFile.MetadataReader;
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
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

/**
 * Document   : GuiHelper
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
public class GuiHelper {

//    static ArbilDragDrop arbilDragDrop = new ArbilDragDrop();
    public static LinorgBugCatcher linorgBugCatcher = new LinorgBugCatcher();
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
        // TreeHelper.getSingleInstance().saveLocations(null, null); no need to do this here but it must be done when ever a change is made
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
            URI nodeUri = ((ImdiTreeObject) (userObject)).getURI();
            System.out.println("openImdiXmlWindow: " + nodeUri);
            String nodeName = ((ImdiTreeObject) (userObject)).toString();
            if (formatXml) {
                try {
                    File tempHtmlFile = new ImdiToHtmlConverter().convertToHtml((ImdiTreeObject) userObject);
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
                    LinorgWindowManager.getSingleInstance().openUrlWindowOnce(nodeName + "-xml", nodeUri.toURL());
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
                // this method is failing on some windows installations so we will just use browse instead
                // TODO: verify that removing this helps and that it does not cause issues on other OSs
                // removing this breaks launching directories on mac
                if (targetUri.getScheme().toLowerCase().equals("file")) {
                    File targetFile = new File(targetUri);
                    // a path with white space will fail as a uri and as a file so it must be url decoded first.
                    targetFile = new File(URLDecoder.decode(targetFile.getAbsolutePath(), "UTF-8"));
                    Desktop.getDesktop().open(targetFile);
                } else {
                    Desktop.getDesktop().browse(targetUri);
                }
                result = true;
            } catch (MalformedURLException muE) {
                GuiHelper.linorgBugCatcher.logError("awtDesktopFound", muE);
                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Failed to open the file: " + muE.getMessage(), "Open In External Application");
            } catch (IOException ioE) {
                GuiHelper.linorgBugCatcher.logError("awtDesktopFound", ioE);
                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Failed to find the file: " + ioE.getMessage(), "Open In External Application");
            }
        } else {
            String osNameString = null;
            try {
                osNameString = System.getProperty("os.name").toLowerCase();
//                String openCommand = "";
                String fileString;
                if (ImdiTreeObject.isStringLocal(targetUri.getScheme())) {
                    fileString = new File(targetUri).getAbsolutePath();
                } else {
                    fileString = targetUri.toString();
                }
                Process launchedProcess = null;

                if (osNameString.indexOf("windows") != -1 || osNameString.indexOf("nt") != -1) {
//                    openCommand = "cmd /c start ";
                    launchedProcess = Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", fileString});
                }
                if (osNameString.equals("windows 95") || osNameString.equals("windows 98")) {
//                    openCommand = "command.com /C start ";
                    launchedProcess = Runtime.getRuntime().exec(new String[]{"command.com", "/C", "start", fileString});
                }
                if (osNameString.indexOf("mac") != -1) {
//                    openCommand = "open ";
                    launchedProcess = Runtime.getRuntime().exec(new String[]{"open", fileString});
                }
                if (osNameString.indexOf("linux") != -1) {
//                    openCommand = "gnome-open ";
                    launchedProcess = Runtime.getRuntime().exec(new String[]{"gnome-open", fileString});
                }
//                String execString = openCommand + targetUri.getPath();
//                System.out.println(execString);
//                Process launchedProcess = Runtime.getRuntime().exec(new String[]{openCommand, targetUri.getPath()});
                if (launchedProcess != null) {
                    BufferedReader errorStreamReader = new BufferedReader(new InputStreamReader(launchedProcess.getErrorStream()));
                    String line;
                    while ((line = errorStreamReader.readLine()) != null) {
                        LinorgWindowManager.getSingleInstance().addMessageDialogToQueue(line, "Open In External Application");
                        System.out.println("Launched process error stream: \"" + line + "\"");
                    }
                    result = true;
                }
            } catch (Exception e) {
                GuiHelper.linorgBugCatcher.logError(osNameString, e);
            }
        }
        return result;
    }
}


