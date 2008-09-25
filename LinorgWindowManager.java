/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import java.awt.Component;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.JDesktopPane;
import javax.swing.JEditorPane;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

/**
 *
 * @author petwit
 */
public class LinorgWindowManager {

    Hashtable windowList = new Hashtable();
    JMenu windowMenu;
    public JDesktopPane desktopPane; //TODO: this is public for the dialog boxes to use, but will change when the strings are loaded from the resources
    int nextWindowX = 0;
    int nextWindowY = 0;
    int nextWindowWidth = 800;
    int nextWindowHeight = 600;

    public LinorgWindowManager(JMenu jMenu, JDesktopPane jDesktopPane) {
        windowMenu = jMenu;
        desktopPane = jDesktopPane;
        // open the introduction page
        // always get this page from the server if available, but also save it for off line use
        openUrlWindow("Introduction", "file:///data1/repos/LocalCopy/Linorg-SingleFrameApplication/src/mpi/linorg/resources/html/Introduction.html");
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

    public void createWindow(String windowTitle, Component contentsComponent) {
        JInternalFrame currentInternalFrame = new javax.swing.JInternalFrame();
        currentInternalFrame.add(contentsComponent);
        windowTitle = addWindowToList(windowTitle, currentInternalFrame);

        currentInternalFrame.setSize(nextWindowWidth, nextWindowHeight);
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
        if (nextWindowX + nextWindowWidth > desktopPane.getWidth()) {
            nextWindowX = 0;
        }
        if (nextWindowY + nextWindowHeight > desktopPane.getHeight()) {
            nextWindowY = 0;
        }
        desktopPane.add(currentInternalFrame, 0);
    }

    public void openUrlWindow(String frameTitle, String locationUrl) {
        JEditorPane htmlDisplay = new JEditorPane();
        htmlDisplay.setEditable(false);
        htmlDisplay.setContentType("text/html");
        try {
            htmlDisplay.setPage(locationUrl);
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
        javax.swing.JTable jTable1;
        javax.swing.JScrollPane jScrollPane6;
        jScrollPane6 = new javax.swing.JScrollPane();
        ImdiTable imdiTable = new ImdiTable(GuiHelper.imdiFieldViews, GuiHelper.imdiHelper.getImdiTableModel(), rowNodesEnum, frameTitle);

        jScrollPane6.setViewportView(imdiTable);
        this.createWindow(frameTitle, jScrollPane6);
    }
}
