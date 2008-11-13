/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import java.awt.BorderLayout; 
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JEditorPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
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

    public void setComponents(JMenu jMenu, JDesktopPane jDesktopPane) {
        windowMenu = jMenu;
        desktopPane = jDesktopPane;

        // open the introduction page
        // always get this page from the server if available, but also save it for off line use
        URL url = this.getClass().getResource("/mpi/linorg/resources/html/Introduction.html");
        openUrlWindow("Introduction", url.toString());
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
        ImdiTable imdiTable = new ImdiTable(new ImdiTableModel(), rowNodesEnum, frameTitle);
        ImdiSplitPanel imdiSplitPanel = new ImdiSplitPanel(imdiTable);
        this.createWindow(frameTitle, imdiSplitPanel);
        imdiSplitPanel.setSplitDisplay();
    }

    public class ImdiSplitPanel extends JPanel {

        private JList fileList;
        public ImdiTable imdiTable;
        private JScrollPane tableScrollPane;
        private JScrollPane listScroller;
        private JSplitPane splitPane;

        public ImdiSplitPanel(ImdiTable localImdiTable) {
//            setBackground(new Color(0xFF00FF));
            this.setLayout(new BorderLayout());

            imdiTable = localImdiTable;
            splitPane = new JSplitPane();

            fileList = new JList(((ImdiTableModel) localImdiTable.getModel()).getListModel(this));
            fileList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
            fileList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
            fileList.setVisibleRowCount(-1);

            listScroller = new JScrollPane(fileList);
            listScroller.setPreferredSize(new Dimension(250, 80));

            ImageBoxRenderer renderer = new ImageBoxRenderer();
            fileList.setCellRenderer(renderer);
            splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
            splitPane.setDividerSize(5);
            tableScrollPane = new JScrollPane(imdiTable);
        }

        public void setSplitDisplay() {
            this.removeAll();
            if (fileList.getModel().getSize() == 0) {
                this.add(tableScrollPane);
            } else {
                splitPane.setTopComponent(tableScrollPane);
                splitPane.setTopComponent(tableScrollPane);
                splitPane.setBottomComponent(listScroller);
                GuiHelper.imdiDragDrop.addDrag(fileList);
                GuiHelper.imdiDragDrop.addTransferHandler(tableScrollPane);
                GuiHelper.imdiDragDrop.addTransferHandler(this);
                this.add(splitPane);
                this.doLayout();
                splitPane.setDividerLocation(0.5);
            }
            GuiHelper.imdiDragDrop.addDrag(imdiTable);
            this.doLayout();
        }
    }

    class ImageBoxRenderer extends JLabel implements ListCellRenderer {

        int outputWidth = 200;
        int outputHeight = 130;

        public ImageBoxRenderer() {
            setOpaque(true);
            setHorizontalAlignment(CENTER);
            setVerticalAlignment(CENTER);
            setVerticalTextPosition(JLabel.BOTTOM);
            setHorizontalTextPosition(JLabel.CENTER);
            setPreferredSize(new Dimension(outputWidth + 10, outputHeight + 50));
        }

        /*
         * This method finds the image and text corresponding
         * to the selected value and returns the label, set up
         * to display the text and image.
         */
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            //Set the icon and text.  If icon was null, say so.
            if (value instanceof ImdiHelper.ImdiTreeObject) {
                ImdiHelper.ImdiTreeObject imdiObject = (ImdiHelper.ImdiTreeObject) value;
                setText(imdiObject.toString());
                String targetFile = "";
                if (imdiObject.hasResource()) {
                    targetFile = imdiObject.getResource();
                } else if (imdiObject.isArchivableFile()) {
                    targetFile = imdiObject.getUrl();
                }

                ImageIcon icon = new ImageIcon(targetFile.replace("file://", ""));
                if (icon != null) {
//                        int outputWidth = 32;
//                        int outputHeight = 32;
//                        int outputWidth = getPreferredSize().width;
//                        int outputHeight = getPreferredSize().height - 100;
                    BufferedImage resizedImg = new BufferedImage(outputWidth, outputHeight, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g2 = resizedImg.createGraphics();
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2.drawImage(icon.getImage(), 0, 0, outputWidth, outputHeight, null);
                    g2.dispose();
                    ImageIcon thumbnailIcon = new ImageIcon(resizedImg);
                    setIcon(thumbnailIcon);
                }

                setFont(list.getFont());
            } else {
                setText(value.toString() + " (no image available)");
            }
            return this;
        }
    }
}
