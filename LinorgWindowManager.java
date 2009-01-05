/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import java.awt.AWTEvent;
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
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
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
    int nextWindowX = 0;
    int nextWindowY = 0;
    int nextWindowWidth = 800;
    int nextWindowHeight = 600;

    public void setComponents(JMenu jMenu, JFrame linorgFrameLocal, JDesktopPane jDesktopPane) {
        windowMenu = jMenu;
        linorgFrame = linorgFrameLocal;
        desktopPane = jDesktopPane;
        //linorgFrame.getLayeredPane().add(desktopPane);

        try {
            // load the saved states
            windowStatesHashtable = (Hashtable) GuiHelper.linorgSessionStorage.loadObject("windowStates");
            // set the main window position and size
            Object linorgFrameBounds = windowStatesHashtable.get("linorgFrameBounds");
            if (linorgFrameBounds instanceof Rectangle) {
                linorgFrame.setBounds((Rectangle) linorgFrameBounds);
//                Rectangle screenSize = Toolkit.getScreenSize();
//                if (screenSize.getHeight() > linorgFrame.getHeight())
//                    linorgFrame.setSize(((Rectangle) linorgFrameBounds).getWidth(), screenSize.getHeight());
            }
            // set the split pane positions
            loadSplitPlanes(linorgFrame.getContentPane().getComponent(0));
        } catch (Exception ex) {
            windowStatesHashtable = new Hashtable();
            System.out.println("load windowStates exception: " + ex.getMessage());
        }
    }

    public void openIntroductionPage() {
        // open the introduction page
        // TODO: always get this page from the server if available, but also save it for off line use
        URL introductionUrl = this.getClass().getResource("/mpi/linorg/resources/html/Introduction.html");
        openUrlWindow("Introduction", introductionUrl);
        
        try {
            // load the saved windows
            Hashtable windowListHashtable = (Hashtable) GuiHelper.linorgSessionStorage.loadObject("openWindows");
            for (Enumeration windowNamesEnum = windowListHashtable.keys(); windowNamesEnum.hasMoreElements();) {
                String currentWindowName = windowNamesEnum.nextElement().toString();
                System.out.println("currentWindowName: " + currentWindowName);
                Vector imdiURLs = (Vector)windowListHashtable.get(currentWindowName);
//                System.out.println("imdiEnumeration: " + imdiEnumeration);
                Vector imdiObjectsVector = new Vector();
                for (Enumeration imdiURLsEnum = imdiURLs.elements(); imdiURLsEnum.hasMoreElements();){
                    // TODO: move all loading of imdi objects into a single class that makes sure only one instence of each URL is ever loaded
                    imdiObjectsVector.add(GuiHelper.imdiLoader.getImdiObject("", imdiURLsEnum.nextElement().toString()));
                }
                openFloatingTable(imdiObjectsVector.elements(), currentWindowName);
                //openFloatingTable(null, currentWindowName);
            }
            System.out.println("done loading windowStates");
        } catch (Exception ex) {
            windowStatesHashtable = new Hashtable();
            System.out.println("load windowStates exception: " + ex.getMessage());
        }
        
        startKeyListener();
    }

    private void loadSplitPlanes(Component targetComponent) {
        //System.out.println("loadSplitPlanes: " + targetComponent);
        if (targetComponent instanceof JSplitPane) {
            System.out.println("loadSplitPlanes: " + targetComponent.getName());
            Object linorgSplitPosition = windowStatesHashtable.get(targetComponent.getName());
            if (linorgSplitPosition instanceof Integer) {
                System.out.println(targetComponent.getName() + ": " + linorgSplitPosition);
                ((JSplitPane) targetComponent).setDividerLocation((Integer) linorgSplitPosition);
            }
            for (Component childComponent : ((JSplitPane) targetComponent).getComponents()) {
                loadSplitPlanes(childComponent);
            }
        }
    }

    private void saveSplitPlanes(Component targetComponent) {
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
            windowStatesHashtable.put("linorgFrameBounds", linorgFrame.getBounds());
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
                        if (currentComponent != null && currentComponent instanceof LinorgSplitPanel){
//                System.out.println("windowObject: " + windowObject);
//                System.out.println("getContentPane: " + ((JInternalFrame) windowObject).getContentPane());
//                System.out.println("getComponent: " + ((JInternalFrame) windowObject).getComponent(0));
//                System.out.println("LinorgSplitPanel: " + ((LinorgSplitPanel)((JInternalFrame) windowObject).getContentPane()));
//                System.out.println("getContentPane: " + ((JInternalFrame) windowObject).getContentPane().getComponent(0));                                           
                            Enumeration windowImdiNodes = ((ImdiTableModel) ((LinorgSplitPanel)currentComponent).imdiTable.getModel()).getImdiNodesURLs();
                            Vector currentNodesVector = new Vector();
                            while (windowImdiNodes.hasMoreElements()){
                                currentNodesVector.add(windowImdiNodes.nextElement().toString());
                            }
                            windowListHashtable.put(currentWindowName, currentNodesVector);
                            System.out.println("saved");
                        }
                    }
                } catch (Exception ex) {
                    System.out.println("Exception: " + ex.getMessage());
                }
            }
            // save the windows
            GuiHelper.linorgSessionStorage.saveObject(windowListHashtable, "openWindows");

            System.out.println("saved windowStates");
        } catch (Exception ex) {
            System.out.println("save windowStates exception: " + ex.getMessage());
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

    private void focusWindow(String windowName) {
        Object windowObject = ((Component[]) windowList.get(windowName))[0];
        try {
            if (windowObject != null) {
                ((JInternalFrame) windowObject).setSelected(true);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
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
                                        JInternalFrame topMostWindow = desktopPane.getAllFrames()[0];
                                        if (topMostWindow != null) {
                                            System.out.println("topMostWindow: " + topMostWindow);
                                            topMostWindow.setSelected(true);
                                        }
                                    } catch (Exception ex) {
                                        System.out.println(ex.getMessage());
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
                                    allWindows[targetLayerInt].setSelected(true);
                                } catch (Exception ex) {
                                    System.out.println(ex.getMessage());
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
        //        GuiHelper.imdiDragDrop.addTransferHandler(currentInternalFrame);
        currentInternalFrame.add(contentsComponent);
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
            // prevent the frame focus process onsuming mouse events that should be recieved by the jtable etc.
            currentInternalFrame.setSelected(true);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return currentInternalFrame;
    }

    public void openUrlWindow(String frameTitle, URL locationUrl) {
        JEditorPane htmlDisplay = new JEditorPane();
        htmlDisplay.setEditable(false);
        htmlDisplay.setContentType("text/html");
        try {
            htmlDisplay.setPage(locationUrl);
            htmlDisplay.addHyperlinkListener(new LinorgHyperlinkListener());

        //gridViewInternalFrame.setMaximum(true);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        JScrollPane jScrollPane6;
        jScrollPane6 = new javax.swing.JScrollPane();
        jScrollPane6.setViewportView(htmlDisplay);
        createWindow(frameTitle, jScrollPane6);
    }

    public void openFloatingTable(Enumeration rowNodesEnum, String frameTitle) {
        ImdiTable imdiTable = new ImdiTable(new ImdiTableModel(), rowNodesEnum, frameTitle);
        LinorgSplitPanel imdiSplitPanel = new LinorgSplitPanel(imdiTable);
        this.createWindow(frameTitle, imdiSplitPanel);
        imdiSplitPanel.setSplitDisplay();
    }
}
