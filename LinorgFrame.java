package mpi.linorg;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;

/*
 * LinorgView.java
 * This version uses only a JFrame and does not require additional dependencies
 * Created on 23 September 2008, 17:23
 * @author Peter.Withers@mpi.nl
 */
import javax.swing.tree.ExpandVetoException;

public class LinorgFrame extends javax.swing.JFrame {

    private ImdiTable previewTable;
    private JScrollPane rightScrollPane;
    private JLabel previewHiddenColumnLabel;
    private JPanel previewPanel;
    private JDesktopPane jDesktopPane1;

    public LinorgFrame() {
        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                performCleanExit();
            //super.windowClosing(e);
            }
        });

        initComponents();
        TreeHelper.getSingleInstance().setTrees((ImdiTree) remoteCorpusTree, (ImdiTree) localCorpusTree, (ImdiTree) localDirectoryTree);

        //Enable tool tips.
        ToolTipManager.sharedInstance().registerComponent(localDirectoryTree);
        ToolTipManager.sharedInstance().registerComponent(localCorpusTree);
        ToolTipManager.sharedInstance().registerComponent(remoteCorpusTree);
        // Enable the tree icons
        localCorpusTree.setCellRenderer(new ImdiTreeRenderer());
        remoteCorpusTree.setCellRenderer(new ImdiTreeRenderer());
        localDirectoryTree.setCellRenderer(new ImdiTreeRenderer());

//        imdiDragDrop.addDrop(localCorpusTree);

        GuiHelper.imdiDragDrop.addDrag(remoteCorpusTree);
        GuiHelper.imdiDragDrop.addDrag(localDirectoryTree);
        GuiHelper.imdiDragDrop.addDrag(localCorpusTree);

        // set the default window dimensions
        // TODO: move this to the sessionstorage and load / save on exit

        jDesktopPane1 = new JDesktopPane();
        jDesktopPane1.setBackground(new java.awt.Color(204, 204, 204));
        previewHiddenColumnLabel = new javax.swing.JLabel(" ");
        previewTable = new ImdiTable(new ImdiTableModel(), "Preview");
        ((ImdiTableModel) previewTable.getModel()).setHiddenColumnsLabel(previewHiddenColumnLabel);
        rightScrollPane = new JScrollPane(previewTable);
        previewPanel = new JPanel(new java.awt.BorderLayout());
        previewPanel.add(rightScrollPane, BorderLayout.CENTER);
        previewPanel.add(previewHiddenColumnLabel, BorderLayout.SOUTH);
        mainSplitPane.setDividerLocation(0.25);
        leftSplitPane.setDividerLocation(0.15);
        leftLocalSplitPane.setDividerLocation(0.2); 
        showSelectionPreviewCheckBoxMenuItem.setSelected(GuiHelper.linorgSessionStorage.loadBoolean("showSelectionPreview", true));
        trackTableSelectionCheckBoxMenuItem.setSelected(GuiHelper.linorgSessionStorage.loadBoolean("trackTableSelection", false));
        TreeHelper.trackTableSelection = trackTableSelectionCheckBoxMenuItem.getState();
        checkNewVersionAtStartCheckBoxMenuItem.setSelected(GuiHelper.linorgSessionStorage.loadBoolean("checkNewVersionAtStart", true));
        copyNewResourcesCheckBoxMenuItem.setSelected(GuiHelper.linorgSessionStorage.loadBoolean("copyNewResources", true));
        GuiHelper.imdiSchema.copyNewResourcesToCache = copyNewResourcesCheckBoxMenuItem.isSelected();
        saveWindowsCheckBoxMenuItem.setSelected(GuiHelper.linorgSessionStorage.loadBoolean("saveWindows", true));
        LinorgWindowManager.getSingleInstance().setComponents(windowMenu, this, jDesktopPane1);
        showSelectionPreviewCheckBoxMenuItemActionPerformed(null); // this is to set the preview table visible or not       
        setTitle("Arbil (Testing version) " + new LinorgVersion().compileDate);
        setIconImage(ImdiIcons.getSingleInstance().linorgTestingIcon.getImage());
        // load the templates and populate the templates menu
        GuiHelper.imdiSchema.populateTemplatesMenu(templatesMenu);
        printHelpMenuItem.setVisible(false);
        setVisible(true);
        LinorgWindowManager.getSingleInstance().openIntroductionPage();
        if (checkNewVersionAtStartCheckBoxMenuItem.isSelected()) {
            new LinorgVersionChecker().checkForUpdate(this);
        }
    }

    private void performCleanExit() {
        if (GuiHelper.imdiLoader.nodesNeedSave()) {
            switch (JOptionPane.showConfirmDialog(this, "Save changes before exiting?", "Arbil", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)) {
                case JOptionPane.NO_OPTION:
                    break;
                case JOptionPane.YES_OPTION:
                    GuiHelper.imdiLoader.saveNodesNeedingSave(false);
                    break;
                default:
                    return;
            }
        }
        GuiHelper.getSingleInstance().saveState(saveWindowsCheckBoxMenuItem.isSelected());
        try {
            GuiHelper.linorgSessionStorage.saveObject(showSelectionPreviewCheckBoxMenuItem.isSelected(), "showSelectionPreview");
            GuiHelper.linorgSessionStorage.saveObject(trackTableSelectionCheckBoxMenuItem.isSelected(), "trackTableSelection");
            GuiHelper.linorgSessionStorage.saveObject(checkNewVersionAtStartCheckBoxMenuItem.isSelected(), "checkNewVersionAtStart");
            GuiHelper.linorgSessionStorage.saveObject(copyNewResourcesCheckBoxMenuItem.isSelected(), "copyNewResources");
            GuiHelper.linorgSessionStorage.saveObject(saveWindowsCheckBoxMenuItem.isSelected(), "saveWindows");
        } catch (Exception ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
        }
        System.exit(0);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainSplitPane = new javax.swing.JSplitPane();
        leftSplitPane = new javax.swing.JSplitPane();
        leftLocalSplitPane = new javax.swing.JSplitPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        localDirectoryTree = new ImdiTree();
        jScrollPane4 = new javax.swing.JScrollPane();
        localCorpusTree = new ImdiTree();
        jScrollPane3 = new javax.swing.JScrollPane();
        remoteCorpusTree = new ImdiTree();
        rightSplitPane = new javax.swing.JSplitPane();
        jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        saveFileMenuItem = new javax.swing.JMenuItem();
        importMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        copyMenuItem = new javax.swing.JMenuItem();
        pasteMenuItem = new javax.swing.JMenuItem();
        undoMenuItem = new javax.swing.JMenuItem();
        redoMenuItem = new javax.swing.JMenuItem();
        optionsMenu = new javax.swing.JMenu();
        editLocationsMenuItem = new javax.swing.JMenuItem();
        templatesMenu = new javax.swing.JMenu();
        viewFavouritesMenuItem = new javax.swing.JMenuItem();
        editFieldViewsMenuItem = new javax.swing.JMenuItem();
        saveWindowsCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        showSelectionPreviewCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        checkNewVersionAtStartCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        copyNewResourcesCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        trackTableSelectionCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        viewMenu = new javax.swing.JMenu();
        windowMenu = new javax.swing.JMenu();
        helpMenu = new javax.swing.JMenu();
        aboutMenuItem = new javax.swing.JMenuItem();
        helpMenuItem = new javax.swing.JMenuItem();
        shortCutKeysjMenuItem = new javax.swing.JMenuItem();
        printHelpMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Arbil");

        mainSplitPane.setDividerLocation(100);
        mainSplitPane.setDividerSize(5);
        mainSplitPane.setName("mainSplitPane"); // NOI18N

        leftSplitPane.setDividerSize(5);
        leftSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        leftSplitPane.setName("leftSplitPane"); // NOI18N

        leftLocalSplitPane.setDividerSize(5);
        leftLocalSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        leftLocalSplitPane.setName("leftLocalSplitPane"); // NOI18N

        localDirectoryTree.setModel(TreeHelper.getSingleInstance().localDirectoryTreeModel);
        localDirectoryTree.addTreeWillExpandListener(new javax.swing.event.TreeWillExpandListener() {
            public void treeWillCollapse(javax.swing.event.TreeExpansionEvent evt)throws javax.swing.tree.ExpandVetoException {
                imdiTreeTreeWillCollapse(evt);
            }
            public void treeWillExpand(javax.swing.event.TreeExpansionEvent evt)throws javax.swing.tree.ExpandVetoException {
                remoteCorpusTreeTreeWillExpand(evt);
            }
        });
        localDirectoryTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                jTreeValueChanged(evt);
            }
        });
        localDirectoryTree.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                treeMouseDragged(evt);
            }
        });
        jScrollPane2.setViewportView(localDirectoryTree);

        leftLocalSplitPane.setBottomComponent(jScrollPane2);

        localCorpusTree.setModel(TreeHelper.getSingleInstance().localCorpusTreeModel);
        localCorpusTree.addTreeWillExpandListener(new javax.swing.event.TreeWillExpandListener() {
            public void treeWillCollapse(javax.swing.event.TreeExpansionEvent evt)throws javax.swing.tree.ExpandVetoException {
                imdiTreeTreeWillCollapse(evt);
            }
            public void treeWillExpand(javax.swing.event.TreeExpansionEvent evt)throws javax.swing.tree.ExpandVetoException {
                remoteCorpusTreeTreeWillExpand(evt);
            }
        });
        localCorpusTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                jTreeValueChanged(evt);
            }
        });
        localCorpusTree.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                treeMouseDragged(evt);
            }
        });
        jScrollPane4.setViewportView(localCorpusTree);

        leftLocalSplitPane.setLeftComponent(jScrollPane4);

        leftSplitPane.setBottomComponent(leftLocalSplitPane);

        remoteCorpusTree.setModel(TreeHelper.getSingleInstance().remoteCorpusTreeModel);
        remoteCorpusTree.addTreeWillExpandListener(new javax.swing.event.TreeWillExpandListener() {
            public void treeWillCollapse(javax.swing.event.TreeExpansionEvent evt)throws javax.swing.tree.ExpandVetoException {
                imdiTreeTreeWillCollapse(evt);
            }
            public void treeWillExpand(javax.swing.event.TreeExpansionEvent evt)throws javax.swing.tree.ExpandVetoException {
                remoteCorpusTreeTreeWillExpand(evt);
            }
        });
        remoteCorpusTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                jTreeValueChanged(evt);
            }
        });
        remoteCorpusTree.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                treeMouseDragged(evt);
            }
        });
        jScrollPane3.setViewportView(remoteCorpusTree);

        leftSplitPane.setLeftComponent(jScrollPane3);

        mainSplitPane.setLeftComponent(leftSplitPane);

        rightSplitPane.setDividerSize(5);
        rightSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        rightSplitPane.setName("rightSplitPane"); // NOI18N
        mainSplitPane.setRightComponent(rightSplitPane);

        getContentPane().add(mainSplitPane, java.awt.BorderLayout.CENTER);

        fileMenu.setText("File");
        fileMenu.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                fileMenuMenuSelected(evt);
            }
        });

        saveFileMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        saveFileMenuItem.setText("Save Changes");
        saveFileMenuItem.setEnabled(false);
        saveFileMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveFileMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveFileMenuItem);

        importMenuItem.setText("Import");
        importMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(importMenuItem);

        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        jMenuBar1.add(fileMenu);

        editMenu.setText("Edit");

        copyMenuItem.setText("Copy");
        copyMenuItem.setEnabled(false);
        editMenu.add(copyMenuItem);

        pasteMenuItem.setText("Paste");
        pasteMenuItem.setEnabled(false);
        editMenu.add(pasteMenuItem);

        undoMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
        undoMenuItem.setText("Undo");
        undoMenuItem.setEnabled(false);
        editMenu.add(undoMenuItem);

        redoMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        redoMenuItem.setText("Redo");
        redoMenuItem.setEnabled(false);
        editMenu.add(redoMenuItem);

        jMenuBar1.add(editMenu);

        optionsMenu.setText("Options");

        editLocationsMenuItem.setText("Locations");
        editLocationsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editLocationsMenuItemActionPerformed(evt);
            }
        });
        optionsMenu.add(editLocationsMenuItem);

        templatesMenu.setText("Templates");
        templatesMenu.setEnabled(false);
        optionsMenu.add(templatesMenu);

        viewFavouritesMenuItem.setText("View Favourites");
        viewFavouritesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewFavouritesMenuItemActionPerformed(evt);
            }
        });
        optionsMenu.add(viewFavouritesMenuItem);

        editFieldViewsMenuItem.setText("Field Views");
        editFieldViewsMenuItem.setEnabled(false);
        editFieldViewsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editFieldViewsMenuItemActionPerformed(evt);
            }
        });
        optionsMenu.add(editFieldViewsMenuItem);

        saveWindowsCheckBoxMenuItem.setSelected(true);
        saveWindowsCheckBoxMenuItem.setText("Save Windows on Exit");
        optionsMenu.add(saveWindowsCheckBoxMenuItem);

        showSelectionPreviewCheckBoxMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
        showSelectionPreviewCheckBoxMenuItem.setSelected(true);
        showSelectionPreviewCheckBoxMenuItem.setText("Show Selection Preview");
        showSelectionPreviewCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showSelectionPreviewCheckBoxMenuItemActionPerformed(evt);
            }
        });
        optionsMenu.add(showSelectionPreviewCheckBoxMenuItem);

        checkNewVersionAtStartCheckBoxMenuItem.setSelected(true);
        checkNewVersionAtStartCheckBoxMenuItem.setText("Check for new version on start");
        optionsMenu.add(checkNewVersionAtStartCheckBoxMenuItem);

        copyNewResourcesCheckBoxMenuItem.setSelected(true);
        copyNewResourcesCheckBoxMenuItem.setText("Copy new resources into cache");
        copyNewResourcesCheckBoxMenuItem.setToolTipText("When adding a new resource to a session copy the file into the local cache.");
        copyNewResourcesCheckBoxMenuItem.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                copyNewResourcesCheckBoxMenuItemItemStateChanged(evt);
            }
        });
        optionsMenu.add(copyNewResourcesCheckBoxMenuItem);

        trackTableSelectionCheckBoxMenuItem.setText("Track Table Selection in Tree");
        trackTableSelectionCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trackTableSelectionCheckBoxMenuItemActionPerformed(evt);
            }
        });
        optionsMenu.add(trackTableSelectionCheckBoxMenuItem);

        jMenuBar1.add(optionsMenu);

        viewMenu.setText("Column Views");
        viewMenu.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                viewMenuMenuSelected(evt);
            }
        });
        jMenuBar1.add(viewMenu);

        windowMenu.setText("Window");
        jMenuBar1.add(windowMenu);

        helpMenu.setText("Help");

        aboutMenuItem.setText("About");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(aboutMenuItem);

        helpMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        helpMenuItem.setText("Help");
        helpMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(helpMenuItem);

        shortCutKeysjMenuItem.setText("Short Cut Keys");
        shortCutKeysjMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                shortCutKeysjMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(shortCutKeysjMenuItem);

        printHelpMenuItem.setText("Print Help File");
        printHelpMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printHelpMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(printHelpMenuItem);

        jMenuBar1.add(helpMenu);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void editFieldViewsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editFieldViewsMenuItemActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_editFieldViewsMenuItemActionPerformed

private void remoteCorpusTreeTreeWillExpand(javax.swing.event.TreeExpansionEvent evt)throws javax.swing.tree.ExpandVetoException {//GEN-FIRST:event_remoteCorpusTreeTreeWillExpand
// TODO add your handling code here:
    DefaultMutableTreeNode parentNode = null;
    if (evt.getPath() == null) {
        //There is no selection.
    } else {
        parentNode = (DefaultMutableTreeNode) (evt.getPath().getLastPathComponent());
        // load imdi data if not already loaded
        TreeHelper.getSingleInstance().loadTreeNodeChildren(parentNode);
    }
//remoteCorpusTree.scrollPathToVisible(evt.getPath());
}//GEN-LAST:event_remoteCorpusTreeTreeWillExpand

private void jTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_jTreeValueChanged
// TODO add your handling code here:
    if (showSelectionPreviewCheckBoxMenuItem.getState()) {
        // count the total number of selected nodes across all trees
//        int selectedNodesCount = remoteCorpusTree.getSelectionCount();
//        selectedNodesCount += localCorpusTree.getSelectionCount();
//        selectedNodesCount += localDirectoryTree.getSelectionCount();
//
//        // if there are no nodes selected then clear the grid
//        if (0 == selectedNodesCount) {
//            guiHelper.removeAllFromGridData(previewTable.getModel());
//        } else {
//            Vector nodesToRemove = new Vector();
//            Vector nodesToAdd = new Vector();
//            // Make a list of nodes to be removed and a separate list of nodes to ba added
//            // this may not be the quickest way to do this but it will reduce redraws and make the other calls simpler
//            for (int selectedCount = 0; selectedCount < evt.getPaths().length; selectedCount++) {
//                DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) evt.getPaths()[selectedCount].getLastPathComponent();
//                // only preview imdi nodes
//                if (parentNode.getUserObject() instanceof ImdiHelper.ImdiTreeObject) {
//                    if (((ImdiHelper.ImdiTreeObject) parentNode.getUserObject()).isImdi()) {
//                        if (evt.isAddedPath(selectedCount)) {
//                            System.out.println("adding: " + parentNode.getPath());
//                            nodesToAdd.add(parentNode.getUserObject());
//                        } else {
//                            System.out.println("removing: " + parentNode.getPath());
//                            nodesToRemove.add(parentNode.getUserObject());
//                        }
//                    }
//                }
//            }
//            guiHelper.removeFromGridData(previewTable.getModel(), nodesToRemove);
//            guiHelper.addToGridData(previewTable.getModel(), nodesToAdd);  
//        }
        GuiHelper.getSingleInstance().removeAllFromGridData(previewTable.getModel());
        GuiHelper.getSingleInstance().addToGridData(previewTable.getModel(), ((ImdiTree) evt.getSource()).getSingleSelectedNode());
    }
}//GEN-LAST:event_jTreeValueChanged

private void showSelectionPreviewCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showSelectionPreviewCheckBoxMenuItemActionPerformed
// TODO add your handling code here:
    LinorgWindowManager.getSingleInstance().saveSplitPlanes(this.getContentPane().getComponent(0));
    if (!showSelectionPreviewCheckBoxMenuItem.getState()) {//GEN-LAST:event_showSelectionPreviewCheckBoxMenuItemActionPerformed
            // remove the right split split and show only the jdesktoppane
            mainSplitPane.remove(rightSplitPane);
            mainSplitPane.setRightComponent(jDesktopPane1);
            // clear the grid to keep things tidy
            GuiHelper.getSingleInstance().removeAllFromGridData(previewTable.getModel());
        } else {
            // put the jdesktoppane and the preview grid back into the right split pane
            mainSplitPane.remove(jDesktopPane1);
            mainSplitPane.setRightComponent(rightSplitPane);
            rightSplitPane.setTopComponent(previewPanel);
            rightSplitPane.setBottomComponent(jDesktopPane1);
            rightSplitPane.setDividerLocation(0.1);
            // update the preview data grid
            GuiHelper.getSingleInstance().removeAllFromGridData(previewTable.getModel());
//            guiHelper.addToGridData(previewTable.getModel(), getSelectedNodes(new JTree[]{remoteCorpusTree, localCorpusTree, localDirectoryTree}));
        }
        LinorgWindowManager.getSingleInstance().loadSplitPlanes(this.getContentPane().getComponent(0));
    }

private void editLocationsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editLocationsMenuItemActionPerformed
// TODO add your handling code here:
    TreeHelper.getSingleInstance().showLocationsDialog();
}//GEN-LAST:event_editLocationsMenuItemActionPerformed

private void viewMenuMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_viewMenuMenuSelected
// TODO add your handling code here:
    GuiHelper.getSingleInstance().initViewMenu(viewMenu);
}//GEN-LAST:event_viewMenuMenuSelected

private void treeMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_treeMouseDragged
// TODO add your handling code here:
    System.out.println("jTree1MouseDragged");
    JComponent c = (JComponent) evt.getSource();
    TransferHandler th = c.getTransferHandler();
    th.exportAsDrag(c, evt, TransferHandler.COPY);
}//GEN-LAST:event_treeMouseDragged

private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
// TODO add your handling code here:
    performCleanExit();
}//GEN-LAST:event_exitMenuItemActionPerformed

private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
// TODO add your handling code here:
    LinorgWindowManager.getSingleInstance().openAboutPage();
}//GEN-LAST:event_aboutMenuItemActionPerformed

private void saveFileMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveFileMenuItemActionPerformed
// TODO add your handling code here:
    GuiHelper.imdiLoader.saveNodesNeedingSave(true);
}//GEN-LAST:event_saveFileMenuItemActionPerformed

private void fileMenuMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_fileMenuMenuSelected
// TODO add your handling code here:
    saveFileMenuItem.setEnabled(GuiHelper.imdiLoader.nodesNeedSave());
}//GEN-LAST:event_fileMenuMenuSelected

private void shortCutKeysjMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_shortCutKeysjMenuItemActionPerformed
// TODO add your handling code here:
    LinorgHelp helpComponent = LinorgHelp.getSingleInstance();
    if (!LinorgWindowManager.getSingleInstance().focusWindow(LinorgHelp.helpWindowTitle)) {
        LinorgWindowManager.getSingleInstance().createWindow(LinorgHelp.helpWindowTitle, helpComponent);
    }
    helpComponent.setCurrentPage(LinorgHelp.ShorCutKeysPage);
}//GEN-LAST:event_shortCutKeysjMenuItemActionPerformed

private void helpMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpMenuItemActionPerformed
// TODO add your handling code here:
    if (!LinorgWindowManager.getSingleInstance().focusWindow(LinorgHelp.helpWindowTitle)) {
        // forcus existing or create a new help window
        LinorgWindowManager.getSingleInstance().createWindow(LinorgHelp.helpWindowTitle, LinorgHelp.getSingleInstance());
    }
}//GEN-LAST:event_helpMenuItemActionPerformed

private void copyNewResourcesCheckBoxMenuItemItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_copyNewResourcesCheckBoxMenuItemItemStateChanged
// TODO add your handling code here:
    GuiHelper.imdiSchema.copyNewResourcesToCache = copyNewResourcesCheckBoxMenuItem.isSelected();
}//GEN-LAST:event_copyNewResourcesCheckBoxMenuItemItemStateChanged

private void importMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importMenuItemActionPerformed
// TODO add your handling code here:
    ImportExportDialog importExportDialog = new ImportExportDialog(remoteCorpusTree);
    importExportDialog.importImdiBranch();
}//GEN-LAST:event_importMenuItemActionPerformed

private void viewFavouritesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewFavouritesMenuItemActionPerformed
// TODO add your handling code here:
    LinorgWindowManager.getSingleInstance().openFloatingTable(LinorgFavourites.getSingleInstance().listAllFavourites(), "Favourites");
}//GEN-LAST:event_viewFavouritesMenuItemActionPerformed

private void printHelpMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printHelpMenuItemActionPerformed
// TODO add your handling code here:
    if (!LinorgWindowManager.getSingleInstance().focusWindow(LinorgHelp.helpWindowTitle)) {
        // forcus existing or create a new help window
        LinorgWindowManager.getSingleInstance().createWindow(LinorgHelp.helpWindowTitle, LinorgHelp.getSingleInstance());
    }
    LinorgHelp.getSingleInstance().printAsOneFile();
}//GEN-LAST:event_printHelpMenuItemActionPerformed

private void trackTableSelectionCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trackTableSelectionCheckBoxMenuItemActionPerformed
// TODO add your handling code here:
    TreeHelper.trackTableSelection = trackTableSelectionCheckBoxMenuItem.getState();
}//GEN-LAST:event_trackTableSelectionCheckBoxMenuItemActionPerformed

private void imdiTreeTreeWillCollapse(javax.swing.event.TreeExpansionEvent evt)throws javax.swing.tree.ExpandVetoException {//GEN-FIRST:event_imdiTreeTreeWillCollapse
// TODO add your handling code here:
    if (evt.getPath().getPathCount() == 1) {
        System.out.println("root node cannot be collapsed");
        throw new ExpandVetoException(evt, "root node cannot be collapsed");
    }
}//GEN-LAST:event_imdiTreeTreeWillCollapse

/**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new LinorgFrame();
                } catch (Exception ex) {
                    new LinorgBugCatcher().logError(ex);
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JCheckBoxMenuItem checkNewVersionAtStartCheckBoxMenuItem;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JCheckBoxMenuItem copyNewResourcesCheckBoxMenuItem;
    private javax.swing.JMenuItem editFieldViewsMenuItem;
    private javax.swing.JMenuItem editLocationsMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenuItem helpMenuItem;
    private javax.swing.JMenuItem importMenuItem;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSplitPane leftLocalSplitPane;
    private javax.swing.JSplitPane leftSplitPane;
    private javax.swing.JTree localCorpusTree;
    private javax.swing.JTree localDirectoryTree;
    private javax.swing.JSplitPane mainSplitPane;
    private javax.swing.JMenu optionsMenu;
    private javax.swing.JMenuItem pasteMenuItem;
    private javax.swing.JMenuItem printHelpMenuItem;
    private javax.swing.JMenuItem redoMenuItem;
    private javax.swing.JTree remoteCorpusTree;
    private javax.swing.JSplitPane rightSplitPane;
    private javax.swing.JMenuItem saveFileMenuItem;
    private javax.swing.JCheckBoxMenuItem saveWindowsCheckBoxMenuItem;
    private javax.swing.JMenuItem shortCutKeysjMenuItem;
    private javax.swing.JCheckBoxMenuItem showSelectionPreviewCheckBoxMenuItem;
    private javax.swing.JMenu templatesMenu;
    private javax.swing.JCheckBoxMenuItem trackTableSelectionCheckBoxMenuItem;
    private javax.swing.JMenuItem undoMenuItem;
    private javax.swing.JMenuItem viewFavouritesMenuItem;
    private javax.swing.JMenu viewMenu;
    private javax.swing.JMenu windowMenu;
    // End of variables declaration//GEN-END:variables

}
