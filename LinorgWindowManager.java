/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.JDesktopPane;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

/**
 *
 * @author petwit
 */
public class LinorgWindowManager {

    Hashtable windowList = new Hashtable();
    Hashtable windowStatesHashtable;
    JMenu windowMenu;
    private JDesktopPane desktopPane; //TODO: this is public for the dialog boxes to use, but will change when the strings are loaded from the resources
    public JFrame linorgFrame;
    int nextWindowX = 50;
    int nextWindowY = 50;
    int nextWindowWidth = 800;
    int nextWindowHeight = 600;
    private Vector<String> messageDialogQueue = new Vector();
    private boolean messagesCanBeShown = false;

    public void setComponents(JMenu jMenu, JFrame linorgFrameLocal, JDesktopPane jDesktopPane) {
        windowMenu = jMenu;
        linorgFrame = linorgFrameLocal;
        desktopPane = jDesktopPane;
        try {
            // load the saved states
            windowStatesHashtable = (Hashtable) GuiHelper.linorgSessionStorage.loadObject("windowStates");
            // set the main window position and size
            linorgFrame.setExtendedState((Integer) windowStatesHashtable.get("linorgFrameExtendedState"));
            if (linorgFrame.getExtendedState() == JFrame.ICONIFIED) {
                // start up iconified is just too confusing to the user
                linorgFrame.setExtendedState(JFrame.NORMAL);
            }
            // if the application was maximised when it was last closed then these values will not be set and this will through setting the size in the catch
            Object linorgFrameBounds = windowStatesHashtable.get("linorgFrameBounds");
            linorgFrame.setBounds((Rectangle) linorgFrameBounds);
        } catch (Exception ex) {
            System.out.println("load windowStates failed: " + ex.getMessage());
            System.out.println("setting default windowStates");
            windowStatesHashtable = new Hashtable();
            linorgFrame.setBounds(0, 0, 800, 600);
            linorgFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
        // set the split pane positions
        loadSplitPlanes(linorgFrame.getContentPane().getComponent(0));
        GuiHelper.imdiDragDrop.addTransferHandler(desktopPane);
    }

    public void openAboutPage() {
        LinorgVersion linorgVersion = new LinorgVersion();
        String messageString = "Linguistic Organiser\n" +
                "A local tool for organising linguistic data.\n" +
                "Max Planck Institute for Psycholinguistics\n" +
                "Version: " + linorgVersion.currentMajor + "." + linorgVersion.currentMinor + "." + linorgVersion.currentRevision + "\n" +
                linorgVersion.lastCommitDate + "\n" +
                "Compile Date: " + linorgVersion.compileDate + "\n";
        JOptionPane.showMessageDialog(linorgFrame, messageString, "About Arbil", JOptionPane.PLAIN_MESSAGE);
    }

    public void addMessageDialogToQueue(String messageString) {
        messageDialogQueue.add(messageString);
        showMessageDialogQueue();
    }

    private void showMessageDialogQueue() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                if (messagesCanBeShown) {
                    while (messageDialogQueue.size() > 0) {
                        String messageString = messageDialogQueue.remove(0);
                        if (messageString != null) {
                            JOptionPane.showMessageDialog(GuiHelper.linorgWindowManager.linorgFrame, messageString);
                        }
                    }

                }
            }
        });
    }

    public void openIntroductionPage() {
        // open the introduction page
        // TODO: always get this page from the server if available, but also save it for off line use
//        URL introductionUrl = this.getClass().getResource("/mpi/linorg/resources/html/Introduction.html");
//        openUrlWindowOnce("Introduction", introductionUrl);
//        get remote file to local disk
//        if local file exists then open that
//        else open the one in the jar file
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//  The features html file has been limited to the version in the jar (not the server), so that it is specific to the version of linorg in the jar. //
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//        String remoteUrl = "http://www.mpi.nl/tg/j2se/jnlp/linorg/Features.html";
//        String cachePath = GuiHelper.linorgSessionStorage.updateCache(remoteUrl, true);
//        System.out.println("cachePath: " + cachePath);
//        URL destinationUrl = null;
//        try {
//            if (new File(cachePath).exists()) {
//                destinationUrl = new File(cachePath).toURL();
//            }
//        } catch (Exception ex) {
//        }
//        if (destinationUrl == null) {
//        destinationUrl = this.getClass().getResource("/mpi/linorg/resources/html/Features.html");
////        }
//        System.out.println("destinationUrl: " + destinationUrl);
//        openUrlWindowOnce("Features/Known Bugs", destinationUrl);

        try {
            // load the saved windows
            Hashtable windowListHashtable = (Hashtable) GuiHelper.linorgSessionStorage.loadObject("openWindows");
            for (Enumeration windowNamesEnum = windowListHashtable.keys(); windowNamesEnum.hasMoreElements();) {
                String currentWindowName = windowNamesEnum.nextElement().toString();
                System.out.println("currentWindowName: " + currentWindowName);
                Vector imdiURLs = (Vector) windowListHashtable.get(currentWindowName);
//                System.out.println("imdiEnumeration: " + imdiEnumeration);
                Vector imdiObjectsVector = new Vector();
                for (Enumeration imdiURLsEnum = imdiURLs.elements(); imdiURLsEnum.hasMoreElements();) {
                    // TODO: move all loading of imdi objects into a single class that makes sure only one instence of each URL is ever loaded
                    imdiObjectsVector.add(GuiHelper.imdiLoader.getImdiObject("", imdiURLsEnum.nextElement().toString()));
                }
                openFloatingTable(imdiObjectsVector.elements(), currentWindowName);
            //openFloatingTable(null, currentWindowName);
            }
            System.out.println("done loading windowStates");
        } catch (Exception ex) {
            windowStatesHashtable = new Hashtable();
            System.out.println("load windowStates failed: " + ex.getMessage());
        }

        if (GuiHelper.treeHelper.localCorpusTreeModel.getChildCount(GuiHelper.treeHelper.localCorpusTreeModel.getRoot()) == 0) {
            LinorgHelp helpComponent = LinorgHelp.getSingleInstance();
            if (!focusWindow("Help")) {
                createWindow("Help", helpComponent);
            }
            helpComponent.setCurrentPage(LinorgHelp.IntroductionPage);
        }
        startKeyListener();
        messagesCanBeShown = true;
        showMessageDialogQueue();
    }

    public void loadSplitPlanes(Component targetComponent) {
        //System.out.println("loadSplitPlanes: " + targetComponent);
        if (targetComponent instanceof JSplitPane) {
            System.out.println("loadSplitPlanes: " + targetComponent.getName());
            Object linorgSplitPosition = windowStatesHashtable.get(targetComponent.getName());
            if (linorgSplitPosition instanceof Integer) {
                System.out.println(targetComponent.getName() + ": " + linorgSplitPosition);
                ((JSplitPane) targetComponent).setDividerLocation((Integer) linorgSplitPosition);
            } else {
                if (targetComponent.getName().equals("rightSplitPane")) {
                    ((JSplitPane) targetComponent).setDividerLocation(150);
                } else {
                    //leftSplitPane  leftLocalSplitPane rightSplitPane)
                    ((JSplitPane) targetComponent).setDividerLocation(200);
                }
            }
            for (Component childComponent : ((JSplitPane) targetComponent).getComponents()) {
                loadSplitPlanes(childComponent);
            }
        }
    }

    public void saveSplitPlanes(Component targetComponent) {
        //System.out.println("saveSplitPlanes: " + targetComponent);
        if (targetComponent instanceof JSplitPane) {
            System.out.println("saveSplitPlanes: " + targetComponent.getName());
            windowStatesHashtable.put(targetComponent.getName(), ((JSplitPane) targetComponent).getDividerLocation());
            for (Component childComponent : ((JSplitPane) targetComponent).getComponents()) {
                saveSplitPlanes(childComponent);
            }
        }
    }

    public void saveWindowStates() {
        // loop windowList and make a hashtable of window names with a vector of the imdinodes displayed, then save the hashtable
        try {
            // collect the main window size and position for saving             
            if (linorgFrame.getExtendedState() != JFrame.MAXIMIZED_BOTH) {
                windowStatesHashtable.put("linorgFrameBounds", linorgFrame.getBounds());
            }
            windowStatesHashtable.put("linorgFrameExtendedState", linorgFrame.getExtendedState());
            // collect the split pane positions for saving
            saveSplitPlanes(linorgFrame.getContentPane().getComponent(0));
            // save the collected states
            GuiHelper.linorgSessionStorage.saveObject(windowStatesHashtable, "windowStates");
            // save the windows
            Hashtable windowListHashtable = new Hashtable();
            //(Hashtable) windowList.clone();
            for (Enumeration windowNamesEnum = windowList.keys(); windowNamesEnum.hasMoreElements();) {
                String currentWindowName = windowNamesEnum.nextElement().toString();
                System.out.println("currentWindowName: " + currentWindowName);
                // set the value of the windowListHashtable to be the imdi urls rather than the windows
                Object windowObject = ((Component[]) windowList.get(currentWindowName))[0];
                try {
                    if (windowObject != null) {
                        Object currentComponent = ((JInternalFrame) windowObject).getContentPane().getComponent(0);
                        if (currentComponent != null && currentComponent instanceof LinorgSplitPanel) {
//                System.out.println("windowObject: " + windowObject);
//                System.out.println("getContentPane: " + ((JInternalFrame) windowObject).getContentPane());
//                System.out.println("getComponent: " + ((JInternalFrame) windowObject).getComponent(0));
//                System.out.println("LinorgSplitPanel: " + ((LinorgSplitPanel)((JInternalFrame) windowObject).getContentPane()));
//                System.out.println("getContentPane: " + ((JInternalFrame) windowObject).getContentPane().getComponent(0));                                           
                            Enumeration windowImdiNodes = ((ImdiTableModel) ((LinorgSplitPanel) currentComponent).imdiTable.getModel()).getImdiNodesURLs();
                            Vector currentNodesVector = new Vector();
                            while (windowImdiNodes.hasMoreElements()) {
                                currentNodesVector.add(windowImdiNodes.nextElement().toString());
                            }
                            windowListHashtable.put(currentWindowName, currentNodesVector);
                            System.out.println("saved");
                        }
                    }
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
//                    System.out.println("Exception: " + ex.getMessage());
                }
            }
            // save the windows
            GuiHelper.linorgSessionStorage.saveObject(windowListHashtable, "openWindows");

            System.out.println("saved windowStates");
        } catch (Exception ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
//            System.out.println("save windowStates exception: " + ex.getMessage());
        }
    }

    private String addWindowToList(String windowName, JInternalFrame windowFrame) {
        int instanceCount = 0;
        String currentWindowName = windowName;
        while (windowList.containsKey(currentWindowName)) {
            currentWindowName = windowName + "(" + ++instanceCount + ")";
        }
        JMenuItem windowMenuItem = new JMenuItem();
        windowMenuItem.setText(currentWindowName);
        windowMenuItem.setName(currentWindowName);
        windowFrame.setName(currentWindowName);
        windowMenuItem.setActionCommand(currentWindowName);
        windowMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                focusWindow(evt.getActionCommand());
            }
        });
        windowFrame.addInternalFrameListener(new InternalFrameAdapter() {

            @Override
            public void internalFrameClosed(InternalFrameEvent e) {
                String windowName = e.getInternalFrame().getName();
                Component[] windowAndMenu = (Component[]) windowList.get(windowName);
                windowMenu.remove(windowAndMenu[1]);
                windowList.remove(windowName);
                super.internalFrameClosed(e);
            }
        });
        windowList.put(currentWindowName, new Component[]{windowFrame, windowMenuItem});
        windowMenu.add(windowMenuItem);
        return currentWindowName;
    }

    public boolean focusWindow(String windowName) {
        if (windowList.containsKey(windowName)) {
            Object windowObject = ((Component[]) windowList.get(windowName))[0];
            try {
                if (windowObject != null) {
                    ((JInternalFrame) windowObject).setIcon(false);
                    ((JInternalFrame) windowObject).setSelected(true);
                    return true;
                }
            } catch (Exception ex) {
                GuiHelper.linorgBugCatcher.logError(ex);
//            System.out.println(ex.getMessage());
            }
        }
        return false;
    }

    private void startKeyListener() {

//        desktopPane.addKeyListener(new KeyAdapter() {
//
//            @Override
//            public void keyPressed(KeyEvent e) {
//                System.out.println("keyPressed");
//                if (e.VK_W == e.getKeyCode()){
//                    System.out.println("VK_W");
//                }
//                super.keyPressed(e);
//            }
//        
//        });

        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {

            public void eventDispatched(AWTEvent e) {
                boolean isKeybordRepeat = false;
                if (e instanceof KeyEvent) {
                    // only consider key release events
                    if (e.getID() == KeyEvent.KEY_RELEASED) {
                        // work around for jvm in linux
                        // due to the bug in the jvm for linux the keyboard repeats are shown as real key events, so we attempt to prevent ludicrous key events being used here
                        KeyEvent nextPress = (KeyEvent) Toolkit.getDefaultToolkit().getSystemEventQueue().peekEvent(KeyEvent.KEY_PRESSED);
                        if (nextPress != null) {
                            // the next key event is at the same time as this event
                            if ((nextPress.getWhen() == ((KeyEvent) e).getWhen())) {
                                // the next key code is the same as this event                                
                                if (((nextPress.getKeyCode() == ((KeyEvent) e).getKeyCode()))) {
                                    isKeybordRepeat = true;
                                }
                            }
                        }
                        // end work around for jvm in linux
                        if (!isKeybordRepeat) {
//                            System.out.println("KeyEvent.paramString: " + ((KeyEvent) e).paramString());
//                            System.out.println("KeyEvent.getWhen: " + ((KeyEvent) e).getWhen());
                            if (((KeyEvent) e).isControlDown() && ((KeyEvent) e).getKeyCode() == KeyEvent.VK_W) {
                                JInternalFrame focusedWindow = desktopPane.getSelectedFrame();
                                if (focusedWindow != null) {
                                    String windowName = focusedWindow.getName();
                                    Component[] windowAndMenu = (Component[]) windowList.get(windowName);
                                    if (windowAndMenu != null) {
                                        windowMenu.remove(windowAndMenu[1]);
                                    }
                                    windowList.remove(windowName);
                                    desktopPane.remove(focusedWindow);
                                    try {
                                        JInternalFrame[] allWindows = desktopPane.getAllFrames();
                                        if (allWindows.length > 0) {
                                            JInternalFrame topMostWindow = allWindows[0];
                                            if (topMostWindow != null) {
                                                System.out.println("topMostWindow: " + topMostWindow);
                                                topMostWindow.setIcon(false);
                                                topMostWindow.setSelected(true);
                                            }
                                        }
                                    } catch (Exception ex) {
                                        GuiHelper.linorgBugCatcher.logError(ex);
//                                        System.out.println(ex.getMessage());
                                    }
                                    desktopPane.repaint();
                                }
                            }
                            if (((KeyEvent) e).getKeyCode() == KeyEvent.VK_TAB && ((KeyEvent) e).isControlDown()) {
                                try {
                                    JInternalFrame[] allWindows = desktopPane.getAllFrames();
                                    int targetLayerInt;
                                    if (((KeyEvent) e).isShiftDown()) {
                                        allWindows[0].moveToBack();
                                        targetLayerInt = 1;
                                    } else {
                                        targetLayerInt = allWindows.length - 1;
                                    }
                                    allWindows[targetLayerInt].setIcon(false);
                                    allWindows[targetLayerInt].setSelected(true);
                                } catch (Exception ex) {
                                    GuiHelper.linorgBugCatcher.logError(ex);
//                                    System.out.println(ex.getMessage());
                                }
                            }
                        }
                    }
                }
            }
        }, AWTEvent.KEY_EVENT_MASK);
    }

    public JInternalFrame createWindow(String windowTitle, Component contentsComponent) {
        JInternalFrame currentInternalFrame = new javax.swing.JInternalFrame();
        currentInternalFrame.setLayout(new BorderLayout());
        //        GuiHelper.imdiDragDrop.addTransferHandler(currentInternalFrame);
        currentInternalFrame.add(contentsComponent, BorderLayout.CENTER);
        windowTitle = addWindowToList(windowTitle, currentInternalFrame);

        // set the new window size to be fully visible
        int tempWindowWidth, tempWindowHeight;
        if (desktopPane.getWidth() > nextWindowWidth) {
            tempWindowWidth = nextWindowWidth;
        } else {
            tempWindowWidth = desktopPane.getWidth() - 50;
        }
        if (desktopPane.getHeight() > nextWindowHeight) {
            tempWindowHeight = nextWindowHeight;
        } else {
            tempWindowHeight = desktopPane.getHeight() - 50;
        }
        currentInternalFrame.setSize(tempWindowWidth, tempWindowHeight);

        currentInternalFrame.setClosable(true);
        currentInternalFrame.setIconifiable(true);
        currentInternalFrame.setMaximizable(true);
        currentInternalFrame.setResizable(true);
        currentInternalFrame.setTitle(windowTitle);
        currentInternalFrame.setToolTipText(windowTitle);
        currentInternalFrame.setName(windowTitle);
        currentInternalFrame.setVisible(true);

//        selectedFilesFrame.setSize(destinationComp.getWidth(), 300);
//        selectedFilesFrame.setRequestFocusEnabled(false);
//        selectedFilesFrame.getContentPane().add(selectedFilesPanel, java.awt.BorderLayout.CENTER);
//        selectedFilesFrame.setBounds(0, 0, 641, 256);
//        destinationComp.add(selectedFilesFrame, javax.swing.JLayeredPane.DEFAULT_LAYER);
        // set the window position so that they are cascaded
        currentInternalFrame.setLocation(nextWindowX, nextWindowY);
        nextWindowX = nextWindowX + 10;
        nextWindowY = nextWindowY + 10;
        // TODO: it would be nice to use the JInternalFrame's title bar height to increment the position
        if (nextWindowX + tempWindowWidth > desktopPane.getWidth()) {
            nextWindowX = 0;
        }
        if (nextWindowY + tempWindowHeight > desktopPane.getHeight()) {
            nextWindowY = 0;
        }
        desktopPane.add(currentInternalFrame, 0);
        try {
            // prevent the frame focus process consuming mouse events that should be recieved by the jtable etc.
            currentInternalFrame.setSelected(true);
        } catch (Exception ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
//            System.out.println(ex.getMessage());
        }
        return currentInternalFrame;
    }

    public JEditorPane openUrlWindowOnce(String frameTitle, URL locationUrl) {
        // TODO: this is not a good way to do this as it does not check the URL only the title
        if (!focusWindow(frameTitle)) {
            return openUrlWindow(frameTitle, locationUrl);
        }
        return null;
    }

    public JEditorPane openUrlWindow(String frameTitle, URL locationUrl) {
        JEditorPane htmlDisplay = new JEditorPane();
        htmlDisplay.setEditable(false);
        htmlDisplay.setContentType("text/html");
        try {
            htmlDisplay.setPage(locationUrl);
            htmlDisplay.addHyperlinkListener(new LinorgHyperlinkListener());

        //gridViewInternalFrame.setMaximum(true);
        } catch (Exception ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
//            System.out.println(ex.getMessage());
        }
        JScrollPane jScrollPane6;
        jScrollPane6 = new javax.swing.JScrollPane();
        jScrollPane6.setViewportView(htmlDisplay);
        createWindow(frameTitle, jScrollPane6);
        return htmlDisplay;
    }

    public void openSearchTable(Vector selectedNodes, String frameTitle) {
        ImdiTableModel resultsTableModel = new ImdiTableModel();
        ImdiTable imdiTable = new ImdiTable(resultsTableModel, null, frameTitle);
        LinorgSplitPanel imdiSplitPanel = new LinorgSplitPanel(imdiTable);
        JInternalFrame searchFrame = this.createWindow(frameTitle, imdiSplitPanel);
        searchFrame.add(new ImdiNodeSearchPanel(searchFrame, resultsTableModel, selectedNodes), BorderLayout.NORTH);
        imdiSplitPanel.setSplitDisplay();
    }

    public void openFloatingTable(Enumeration rowNodesEnum, String frameTitle) {
        ImdiTable imdiTable = new ImdiTable(new ImdiTableModel(), rowNodesEnum, frameTitle);
        LinorgSplitPanel imdiSplitPanel = new LinorgSplitPanel(imdiTable);
        this.createWindow(frameTitle, imdiSplitPanel);
        imdiSplitPanel.setSplitDisplay();
    }
}
