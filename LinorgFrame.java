/*
 * LinorgView.java
 * This version uses only a JFrame and does not require additional dependencies
 * Created on 23 September 2008, 17:23
 */
package mpi.linorg;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Vector;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 *
 * @author  petwit
 */
public class LinorgFrame extends javax.swing.JFrame {

    private DefaultTreeModel localCorpusTreeModel;
    private DefaultTreeModel remoteCorpusTreeModel;
    private DefaultTreeModel localDirectoryTreeModel;
    private DefaultMutableTreeNode localCorpusRootNode;
    private DefaultMutableTreeNode remoteCorpusRootNode;
    private DefaultMutableTreeNode localDirectoryRootNode;
    private LinorgSessionStorage linorgSessionStorage = new LinorgSessionStorage();
    private GuiHelper guiHelper = new GuiHelper(linorgSessionStorage);
    private ImdiDragDrop imdiDragDrop = new ImdiDragDrop();
    private LinorgWindowManager linorgWindowManager;

    public LinorgFrame() {
        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                guiHelper.saveState();
                super.windowClosing(e);
            }
        });
        localCorpusRootNode = new DefaultMutableTreeNode("Local Corpus Cache");
        remoteCorpusRootNode = new DefaultMutableTreeNode("Remote Corpus");
        localDirectoryRootNode = new DefaultMutableTreeNode("Local File System");
        guiHelper.applyRootLocations(localDirectoryRootNode, localCorpusRootNode, remoteCorpusRootNode);

        localCorpusTreeModel = new DefaultTreeModel(localCorpusRootNode, true);
        remoteCorpusTreeModel = new DefaultTreeModel(remoteCorpusRootNode, true);
        localDirectoryTreeModel = new DefaultTreeModel(localDirectoryRootNode, true);

        initComponents();

        //Enable tool tips.
        ToolTipManager.sharedInstance().registerComponent(localDirectoryTree);
        ToolTipManager.sharedInstance().registerComponent(localCorpusTree);
        ToolTipManager.sharedInstance().registerComponent(remoteCorpusTree);
        // Enable the tree icons
        localCorpusTree.setCellRenderer(guiHelper.getImdiTreeRenderer());
        remoteCorpusTree.setCellRenderer(guiHelper.getImdiTreeRenderer());
        localDirectoryTree.setCellRenderer(guiHelper.getImdiTreeRenderer());

        imdiDragDrop.addDrop(localCorpusTree);

        imdiDragDrop.addDrag(remoteCorpusTree);
        imdiDragDrop.addDrag(localDirectoryTree);
        imdiDragDrop.addDrag(localCorpusTree);

        // set the default window dimensions
        // TODO: move this to the sessionstorage and load / save on exit

        mainSplitPane.setDividerLocation(0.25);
        // also set in showSelectionPreviewCheckBoxMenuItemActionPerformed
        rightSplitPane.setDividerLocation(0.1);
        leftSplitPane.setDividerLocation(0.15);
        leftLocalSplitPane.setDividerLocation(0.2);

        setSize(800, 600);
        //this.setExtendedState(Frame.MAXIMIZED_BOTH);
        
        setVisible(true);
        
        linorgWindowManager = new LinorgWindowManager(windowMenu, jDesktopPane1);
        guiHelper.setWindowManager(linorgWindowManager);
        guiHelper.initViewMenu(viewMenu);
    }

    private void addLocation(String addableLocation) {
        if (!guiHelper.addLocation(addableLocation)) {
            // alert the user when the node already exists and cannot be added again
            JOptionPane.showMessageDialog(this, "The location already exists and cannot be added again", "Add location", JOptionPane.INFORMATION_MESSAGE);
        }
        guiHelper.applyRootLocations(localDirectoryRootNode, localCorpusRootNode, remoteCorpusRootNode);
        //locationSettingsTable.setModel(guiHelper.getLocationsTableModel());
        localDirectoryTreeModel.reload();
        localCorpusTreeModel.reload();
        remoteCorpusTreeModel.reload();
    }

    private void removeSelectedLocation(DefaultMutableTreeNode selectedTreeNode) {
        if (selectedTreeNode == null) {
            JOptionPane.showMessageDialog(jDesktopPane1, "No node selected", "", 0);
        } else {
            guiHelper.removeLocation(selectedTreeNode.getUserObject());
            guiHelper.applyRootLocations(localDirectoryRootNode, localCorpusRootNode, remoteCorpusRootNode);
            localDirectoryTreeModel.reload();
            localCorpusTreeModel.reload();
            remoteCorpusTreeModel.reload();
        }
    }

    private Vector getSelectedNodes() {
        Vector selectedNodes = new Vector();
        JTree[] treesToSearch = new JTree[]{remoteCorpusTree, localCorpusTree, localDirectoryTree};
        // iterate over allthe selected nodes in the available trees
        for (int treeCount = 0; treeCount < treesToSearch.length; treeCount++) {
            for (int selectedCount = 0; selectedCount < treesToSearch[treeCount].getSelectionCount(); selectedCount++) {
                DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) treesToSearch[treeCount].getSelectionPaths()[selectedCount].getLastPathComponent();
                selectedNodes.add(parentNode.getUserObject());
            }
        }
        return selectedNodes;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        treePopupMenu = new javax.swing.JPopupMenu();
        viewSelectedNodesMenuItem = new javax.swing.JMenuItem();
        copyBranchMenuItem = new javax.swing.JMenuItem();
        actorsToGridMenuItem = new javax.swing.JMenuItem();
        searchSubnodesMenuItem = new javax.swing.JMenuItem();
        reloadSubnodesMenuItem = new javax.swing.JMenuItem();
        treePopupMenuSeparator1 = new javax.swing.JSeparator();
        copyImdiUrlMenuItem = new javax.swing.JMenuItem();
        viewXmlMenuItem = new javax.swing.JMenuItem();
        treePopupMenuSeparator2 = new javax.swing.JSeparator();
        addRemoteCorpusMenuItem = new javax.swing.JMenuItem();
        addDefaultLocationsMenuItem = new javax.swing.JMenuItem();
        removeRemoteCorpusMenuItem = new javax.swing.JMenuItem();
        removeCachedCopyMenuItem = new javax.swing.JMenuItem();
        addLocalDirectoryMenuItem = new javax.swing.JMenuItem();
        removeLocalDirectoryMenuItem = new javax.swing.JMenuItem();
        mainSplitPane = new javax.swing.JSplitPane();
        leftSplitPane = new javax.swing.JSplitPane();
        leftLocalSplitPane = new javax.swing.JSplitPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        localDirectoryTree = new javax.swing.JTree();
        jScrollPane4 = new javax.swing.JScrollPane();
        localCorpusTree = new javax.swing.JTree();
        jScrollPane3 = new javax.swing.JScrollPane();
        remoteCorpusTree = new javax.swing.JTree();
        rightSplitPane = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        previewTable = new javax.swing.JTable();
        jDesktopPane1 = new javax.swing.JDesktopPane();
        jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        exitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        optionsMenu = new javax.swing.JMenu();
        editLocationsMenuItem = new javax.swing.JMenuItem();
        editFieldViewsMenuItem = new javax.swing.JMenuItem();
        saveWindowsCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        showSelectionPreviewCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        viewMenu = new javax.swing.JMenu();
        windowMenu = new javax.swing.JMenu();
        helpMenu = new javax.swing.JMenu();
        aboutMenuItem = new javax.swing.JMenuItem();

        viewSelectedNodesMenuItem.setText("View Selected");
        viewSelectedNodesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewSelectedNodesMenuItemActionPerformed(evt);
            }
        });
        treePopupMenu.add(viewSelectedNodesMenuItem);

        copyBranchMenuItem.setText("Copy Branch to Offline Cache");
        copyBranchMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyBranchMenuItemActionPerformed(evt);
            }
        });
        treePopupMenu.add(copyBranchMenuItem);

        actorsToGridMenuItem.setText("Search for Actors");
        actorsToGridMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actorsToGridMenuItemActionPerformed(evt);
            }
        });
        treePopupMenu.add(actorsToGridMenuItem);

        searchSubnodesMenuItem.setText("Search");
        searchSubnodesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchSubnodesMenuItemActionPerformed(evt);
            }
        });
        treePopupMenu.add(searchSubnodesMenuItem);

        reloadSubnodesMenuItem.setText("Reload");
        treePopupMenu.add(reloadSubnodesMenuItem);
        treePopupMenu.add(treePopupMenuSeparator1);

        copyImdiUrlMenuItem.setText("Copy Location to Clipboard");
        copyImdiUrlMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyImdiUrlMenuItemActionPerformed(evt);
            }
        });
        treePopupMenu.add(copyImdiUrlMenuItem);

        viewXmlMenuItem.setText("View IMDI XML");
        viewXmlMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewXmlMenuItemActionPerformed(evt);
            }
        });
        treePopupMenu.add(viewXmlMenuItem);
        treePopupMenu.add(treePopupMenuSeparator2);

        addRemoteCorpusMenuItem.setText("Add Remote Location");
        addRemoteCorpusMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addRemoteCorpusMenuItemActionPerformed(evt);
            }
        });
        treePopupMenu.add(addRemoteCorpusMenuItem);

        addDefaultLocationsMenuItem.setText("Add Default Remote Locations");
        addDefaultLocationsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addDefaultLocationsMenuItemActionPerformed(evt);
            }
        });
        treePopupMenu.add(addDefaultLocationsMenuItem);

        removeRemoteCorpusMenuItem.setText("Remove Remote Location");
        removeRemoteCorpusMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeRemoteCorpusMenuItemActionPerformed(evt);
            }
        });
        treePopupMenu.add(removeRemoteCorpusMenuItem);

        removeCachedCopyMenuItem.setText("Remove Cache Link");
        removeCachedCopyMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeCachedCopyMenuItemActionPerformed(evt);
            }
        });
        treePopupMenu.add(removeCachedCopyMenuItem);

        addLocalDirectoryMenuItem.setText("Add Local Directory");
        addLocalDirectoryMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addLocalDirectoryMenuItemActionPerformed(evt);
            }
        });
        treePopupMenu.add(addLocalDirectoryMenuItem);

        removeLocalDirectoryMenuItem.setText("Remove Local Directory");
        removeLocalDirectoryMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeLocalDirectoryMenuItemActionPerformed(evt);
            }
        });
        treePopupMenu.add(removeLocalDirectoryMenuItem);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Linorg");

        mainSplitPane.setDividerSize(5);

        leftSplitPane.setDividerSize(5);
        leftSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        leftLocalSplitPane.setDividerSize(5);
        leftLocalSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        localDirectoryTree.setModel(localDirectoryTreeModel);
        localDirectoryTree.addTreeWillExpandListener(new javax.swing.event.TreeWillExpandListener() {
            public void treeWillCollapse(javax.swing.event.TreeExpansionEvent evt)throws javax.swing.tree.ExpandVetoException {
            }
            public void treeWillExpand(javax.swing.event.TreeExpansionEvent evt)throws javax.swing.tree.ExpandVetoException {
                remoteCorpusTreeTreeWillExpand(evt);
            }
        });
        localDirectoryTree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                treeMousePressed(evt);
            }
        });
        localDirectoryTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                jTreeValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(localDirectoryTree);

        leftLocalSplitPane.setBottomComponent(jScrollPane2);

        localCorpusTree.setModel(localCorpusTreeModel);
        localCorpusTree.addTreeWillExpandListener(new javax.swing.event.TreeWillExpandListener() {
            public void treeWillCollapse(javax.swing.event.TreeExpansionEvent evt)throws javax.swing.tree.ExpandVetoException {
            }
            public void treeWillExpand(javax.swing.event.TreeExpansionEvent evt)throws javax.swing.tree.ExpandVetoException {
                remoteCorpusTreeTreeWillExpand(evt);
            }
        });
        localCorpusTree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                treeMousePressed(evt);
            }
        });
        localCorpusTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                jTreeValueChanged(evt);
            }
        });
        jScrollPane4.setViewportView(localCorpusTree);

        leftLocalSplitPane.setLeftComponent(jScrollPane4);

        leftSplitPane.setBottomComponent(leftLocalSplitPane);

        remoteCorpusTree.setModel(remoteCorpusTreeModel);
        remoteCorpusTree.addTreeWillExpandListener(new javax.swing.event.TreeWillExpandListener() {
            public void treeWillCollapse(javax.swing.event.TreeExpansionEvent evt)throws javax.swing.tree.ExpandVetoException {
            }
            public void treeWillExpand(javax.swing.event.TreeExpansionEvent evt)throws javax.swing.tree.ExpandVetoException {
                remoteCorpusTreeTreeWillExpand(evt);
            }
        });
        remoteCorpusTree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                treeMousePressed(evt);
            }
        });
        remoteCorpusTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                jTreeValueChanged(evt);
            }
        });
        jScrollPane3.setViewportView(remoteCorpusTree);

        leftSplitPane.setLeftComponent(jScrollPane3);

        mainSplitPane.setLeftComponent(leftSplitPane);

        rightSplitPane.setDividerSize(5);
        rightSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        previewTable.setModel(guiHelper.getImdiTableModel());
        jScrollPane1.setViewportView(previewTable);

        rightSplitPane.setLeftComponent(jScrollPane1);
        rightSplitPane.setBottomComponent(jDesktopPane1);

        mainSplitPane.setRightComponent(rightSplitPane);

        getContentPane().add(mainSplitPane, java.awt.BorderLayout.CENTER);

        fileMenu.setText("File");

        exitMenuItem.setText("Exit");
        fileMenu.add(exitMenuItem);

        jMenuBar1.add(fileMenu);

        editMenu.setText("Edit");
        jMenuBar1.add(editMenu);

        optionsMenu.setText("Options");

        editLocationsMenuItem.setText("Locations");
        optionsMenu.add(editLocationsMenuItem);

        editFieldViewsMenuItem.setText("Field Views");
        editFieldViewsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editFieldViewsMenuItemActionPerformed(evt);
            }
        });
        optionsMenu.add(editFieldViewsMenuItem);

        saveWindowsCheckBoxMenuItem.setSelected(true);
        saveWindowsCheckBoxMenuItem.setText("Save Windows on Exit");
        optionsMenu.add(saveWindowsCheckBoxMenuItem);

        showSelectionPreviewCheckBoxMenuItem.setSelected(true);
        showSelectionPreviewCheckBoxMenuItem.setText("Show Selection Preview");
        showSelectionPreviewCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showSelectionPreviewCheckBoxMenuItemActionPerformed(evt);
            }
        });
        optionsMenu.add(showSelectionPreviewCheckBoxMenuItem);

        jMenuBar1.add(optionsMenu);

        viewMenu.setText("View");
        jMenuBar1.add(viewMenu);

        windowMenu.setText("Window");
        jMenuBar1.add(windowMenu);

        helpMenu.setText("Help");

        aboutMenuItem.setText("About");
        helpMenu.add(aboutMenuItem);

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
    }
    // load imdi data if not already loaded
    guiHelper.getImdiChildNodes(parentNode);
//remoteCorpusTree.scrollPathToVisible(evt.getPath());
}//GEN-LAST:event_remoteCorpusTreeTreeWillExpand

private void treeMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_treeMousePressed
// TODO add your handling code here:
    // test if click was over a selected node
    javax.swing.tree.TreePath clickedNodePath = ((javax.swing.JTree) evt.getSource()).getPathForLocation(evt.getX(), evt.getY());
    boolean clickedPathIsSelected = (((javax.swing.JTree) evt.getSource()).isPathSelected(clickedNodePath));

    if ((!evt.isControlDown() && evt.getButton() == 1 && !evt.isShiftDown()) || (evt.getButton() == 3 && !clickedPathIsSelected)) {
        if (evt.getSource() != remoteCorpusTree) {
            remoteCorpusTree.clearSelection();
        }
        if (evt.getSource() != localCorpusTree) {
            localCorpusTree.clearSelection();
        }
        if (evt.getSource() != localDirectoryTree) {
            localDirectoryTree.clearSelection();
        }
        ((javax.swing.JTree) evt.getSource()).setSelectionPath(((javax.swing.JTree) evt.getSource()).getPathForLocation(evt.getX(), evt.getY()));
    } else {
        if (clickedPathIsSelected) {
            ((javax.swing.JTree) evt.getSource()).addSelectionPath(clickedNodePath);
        } else {
            ((javax.swing.JTree) evt.getSource()).removeSelectionPath(clickedNodePath);
        }
    }
    if (evt.getButton() == 3) {
        boolean showContextMenu = true;
        int selectionCount = ((javax.swing.JTree) evt.getSource()).getSelectionCount();
        int nodeLevel = -1;
        if (selectionCount > 0) {
            nodeLevel = ((javax.swing.JTree) evt.getSource()).getSelectionPath().getPathCount();
        }
        boolean showRemoveLocationsTasks = selectionCount == 1 && nodeLevel == 2;
        boolean showAddLocationsTasks = selectionCount == 1 && nodeLevel == 1;
        //System.out.println("path count: " + ((JTree) evt.getSource()).getSelectionPath().getPathCount());
        // set up the contect menu
        removeCachedCopyMenuItem.setVisible(false);
        removeLocalDirectoryMenuItem.setVisible(false);
        addLocalDirectoryMenuItem.setVisible(false);
        removeRemoteCorpusMenuItem.setVisible(false);
        addRemoteCorpusMenuItem.setVisible(false);
        copyBranchMenuItem.setVisible(false);
        copyImdiUrlMenuItem.setVisible(false);
        viewXmlMenuItem.setVisible(false);
        searchSubnodesMenuItem.setVisible(false);
        reloadSubnodesMenuItem.setVisible(false);
        actorsToGridMenuItem.setVisible(false);
        addDefaultLocationsMenuItem.setVisible(false);

        if (evt.getSource() == remoteCorpusTree) {
            removeRemoteCorpusMenuItem.setVisible(showRemoveLocationsTasks);
            addRemoteCorpusMenuItem.setVisible(showAddLocationsTasks);
            copyBranchMenuItem.setVisible(selectionCount > 0 && nodeLevel > 1);
            addDefaultLocationsMenuItem.setVisible(showAddLocationsTasks);
        }
        if (evt.getSource() == localCorpusTree) {
            removeCachedCopyMenuItem.setVisible(showRemoveLocationsTasks);
            searchSubnodesMenuItem.setVisible(selectionCount > 0 && nodeLevel > 1);
            actorsToGridMenuItem.setVisible(selectionCount > 0 && nodeLevel > 1);
            showContextMenu = nodeLevel != 1;
        }
        if (evt.getSource() == localDirectoryTree) {
            removeLocalDirectoryMenuItem.setVisible(showRemoveLocationsTasks);
            addLocalDirectoryMenuItem.setVisible(showAddLocationsTasks);
        } else {
            copyImdiUrlMenuItem.setVisible(selectionCount > 0 && nodeLevel > 1);
            viewXmlMenuItem.setVisible(selectionCount > 0 && nodeLevel > 1);
            reloadSubnodesMenuItem.setVisible(selectionCount > 0 && nodeLevel > 1);
        }
        // hide show the separators
        treePopupMenuSeparator2.setVisible(nodeLevel != 1 && showRemoveLocationsTasks && evt.getSource() != localDirectoryTree);
        treePopupMenuSeparator1.setVisible(nodeLevel != 1 && evt.getSource() != localDirectoryTree);

        // show the context menu
        if (showContextMenu) {
            treePopupMenu.show((java.awt.Component) evt.getSource(), evt.getX(), evt.getY());
        }
    }
    }//GEN-LAST:event_treeMousePressed

private void actorsToGridMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actorsToGridMenuItemActionPerformed
// TODO add your handling code here:
    String searchString = "Actor";
    Vector selectedNodes = getSelectedNodes();

    if (selectedNodes.size() == 0) {
        JOptionPane.showMessageDialog(linorgWindowManager.desktopPane, "No nodes are selected", "Search", 0);
    } else {
        SearchDialog searchDialog = new SearchDialog(linorgWindowManager, selectedNodes, searchString);
    }
    }//GEN-LAST:event_actorsToGridMenuItemActionPerformed

private void copyBranchMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyBranchMenuItemActionPerformed
// TODO add your handling code here:
    if (remoteCorpusTree.getLeadSelectionPath() == null) {
        JOptionPane.showMessageDialog(jDesktopPane1, "No node selected", "", 0);
    } else {
        guiHelper.copyBranchToCashe(jDesktopPane1, ((DefaultMutableTreeNode) remoteCorpusTree.getLeadSelectionPath().getLastPathComponent()).getUserObject());
        guiHelper.applyRootLocations(localDirectoryRootNode, localCorpusRootNode, remoteCorpusRootNode);
        localDirectoryTreeModel.reload();
        localCorpusTreeModel.reload();
        remoteCorpusTreeModel.reload();
    }
}//GEN-LAST:event_copyBranchMenuItemActionPerformed

private void addLocalDirectoryMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addLocalDirectoryMenuItemActionPerformed
// TODO add your handling code here:
    JFileChooser fc = new JFileChooser();
    //fc.setDialogTitle(getResourceMap().getString(name + ".dialogTitle"));
    //String textFilesDesc = getResourceMap().getString("txtFileExtensionDescription");
    fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    int option = fc.showOpenDialog(this);
    if (JFileChooser.APPROVE_OPTION == option) {
        try {
            addLocation(fc.getSelectedFile().getCanonicalPath());
        } catch (IOException ex) {
            System.out.println("Error adding location: " + ex.getMessage());
        }
    }
}//GEN-LAST:event_addLocalDirectoryMenuItemActionPerformed

private void viewXmlMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewXmlMenuItemActionPerformed
// TODO add your handling code here:
    DefaultMutableTreeNode selectedTreeNode = null;
    if (remoteCorpusTree.getLeadSelectionPath() == null) {
        if (localCorpusTree.getLeadSelectionPath() != null) {
            System.out.println("copying local directory location");
            selectedTreeNode = (DefaultMutableTreeNode) localCorpusTree.getLeadSelectionPath().getLastPathComponent();
        }
    } else {
        //System.out.println("copying remote url");
        selectedTreeNode = (DefaultMutableTreeNode) remoteCorpusTree.getLeadSelectionPath().getLastPathComponent();
    }
    if (selectedTreeNode != null) {
        guiHelper.openImdiXmlWindow(selectedTreeNode.getUserObject());
    }
}//GEN-LAST:event_viewXmlMenuItemActionPerformed

private void copyImdiUrlMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyImdiUrlMenuItemActionPerformed
// TODO add your handling code here:
    DefaultMutableTreeNode selectedTreeNode = null;
    if (remoteCorpusTree.getLeadSelectionPath() == null) {
        if (localDirectoryTree.getLeadSelectionPath() != null) {
            System.out.println("copying local directory location");
            selectedTreeNode = (DefaultMutableTreeNode) localDirectoryTree.getLeadSelectionPath().getLastPathComponent();
        }
    } else {
        System.out.println("copying remote url");
        selectedTreeNode = (DefaultMutableTreeNode) remoteCorpusTree.getLeadSelectionPath().getLastPathComponent();
    }
    if (selectedTreeNode == null) {
        if (localCorpusTree.getLeadSelectionPath() != null) {
            JOptionPane.showMessageDialog(jDesktopPane1, "Cannot copy from the cache", "", 0);
        } else {
            JOptionPane.showMessageDialog(jDesktopPane1, "No node selected", "", 0);
        }
    } else {
        guiHelper.copyNodeUrlToClipboard(selectedTreeNode);
    }
}//GEN-LAST:event_copyImdiUrlMenuItemActionPerformed

private void addRemoteCorpusMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addRemoteCorpusMenuItemActionPerformed
// TODO add your handling code here:
    String addableLocation = (String) JOptionPane.showInputDialog(this, "Enter the URL");
    if ((addableLocation != null) && (addableLocation.length() > 0)) {
        addLocation(addableLocation);
    }
}//GEN-LAST:event_addRemoteCorpusMenuItemActionPerformed

private void addDefaultLocationsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addDefaultLocationsMenuItemActionPerformed
// TODO add your handling code here:
    if (0 < guiHelper.addDefaultCorpusLocations()) {
        guiHelper.applyRootLocations(localDirectoryRootNode, localCorpusRootNode, remoteCorpusRootNode);
        localDirectoryTreeModel.reload();
        localCorpusTreeModel.reload();
        remoteCorpusTreeModel.reload();
    } else {
        // alert the user when the node already exists and cannot be added again
        JOptionPane.showMessageDialog(this, "The defalut locations already exists and cannot be added again", "Add default locations", JOptionPane.INFORMATION_MESSAGE);
    }
}//GEN-LAST:event_addDefaultLocationsMenuItemActionPerformed

private void removeRemoteCorpusMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeRemoteCorpusMenuItemActionPerformed
// TODO add your handling code here:
    DefaultMutableTreeNode selectedTreeNode = null;
    if (remoteCorpusTree.getLeadSelectionPath() != null) {
        selectedTreeNode = (DefaultMutableTreeNode) remoteCorpusTree.getLeadSelectionPath().getLastPathComponent();
    }
    removeSelectedLocation(selectedTreeNode);
}//GEN-LAST:event_removeRemoteCorpusMenuItemActionPerformed

private void removeCachedCopyMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeCachedCopyMenuItemActionPerformed
// TODO add your handling code here:
    DefaultMutableTreeNode selectedTreeNode = null;
    if (localCorpusTree.getLeadSelectionPath() != null) {
        selectedTreeNode = (DefaultMutableTreeNode) localCorpusTree.getLeadSelectionPath().getLastPathComponent();
    }
    removeSelectedLocation(selectedTreeNode);
}//GEN-LAST:event_removeCachedCopyMenuItemActionPerformed

private void removeLocalDirectoryMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeLocalDirectoryMenuItemActionPerformed
// TODO add your handling code here:
    DefaultMutableTreeNode selectedTreeNode = null;
    if (localDirectoryTree.getLeadSelectionPath() != null) {
        selectedTreeNode = (DefaultMutableTreeNode) localDirectoryTree.getLeadSelectionPath().getLastPathComponent();
    }
    removeSelectedLocation(selectedTreeNode);
}//GEN-LAST:event_removeLocalDirectoryMenuItemActionPerformed

private void searchSubnodesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchSubnodesMenuItemActionPerformed
// TODO add your handling code here:
    Vector selectedNodes = getSelectedNodes();
    if (selectedNodes.size() == 0) {
        JOptionPane.showMessageDialog(linorgWindowManager.desktopPane, "No nodes are selected", "Search", 0);
    } else {
        SearchDialog searchDialog = new SearchDialog(linorgWindowManager, selectedNodes, null);
    }
}//GEN-LAST:event_searchSubnodesMenuItemActionPerformed

private void jTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_jTreeValueChanged
// TODO add your handling code here:
    if (showSelectionPreviewCheckBoxMenuItem.getState()) {
        // count the total number of selected nodes across all trees
        int selectedNodesCount = remoteCorpusTree.getSelectionCount();
        selectedNodesCount += localCorpusTree.getSelectionCount();
        selectedNodesCount += localDirectoryTree.getSelectionCount();

        // if there are no nodes selected then clear the grid
        if (0 == selectedNodesCount) {
            guiHelper.removeAllFromGridData(previewTable.getModel());
        } else {
            Vector nodesToRemove = new Vector();
            Vector nodesToAdd = new Vector();
            // Make a list of nodes to be removed and a separate list of nodes to ba added
            // this may not be the quickest way to do this but it will reduce redraws and make the other calls simpler
            for (int selectedCount = 0; selectedCount < evt.getPaths().length; selectedCount++) {
                DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) evt.getPaths()[selectedCount].getLastPathComponent();
                if (evt.isAddedPath(selectedCount)) {
                    System.out.println("adding: " + parentNode.getPath());
                    nodesToAdd.add(parentNode.getUserObject());
                } else {
                    System.out.println("removing: " + parentNode.getPath());
                    nodesToRemove.add(parentNode.getUserObject());
                }
            }
            guiHelper.removeFromGridData(previewTable.getModel(), nodesToRemove);
            guiHelper.addToGridData(previewTable.getModel(), nodesToAdd);
        }
    }
}//GEN-LAST:event_jTreeValueChanged

private void showSelectionPreviewCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showSelectionPreviewCheckBoxMenuItemActionPerformed
// TODO add your handling code here:
    if (!showSelectionPreviewCheckBoxMenuItem.getState()) {//GEN-LAST:event_showSelectionPreviewCheckBoxMenuItemActionPerformed
            // remove the right split split and show only the jdesktoppane
            mainSplitPane.remove(rightSplitPane);
            mainSplitPane.setRightComponent(jDesktopPane1);
            // clear the grid to keep things tidy
            guiHelper.removeAllFromGridData(previewTable.getModel());
        } else {
            // put the jdesktoppane and the preview grid back into the right split pane
            mainSplitPane.remove(jDesktopPane1);
            mainSplitPane.setRightComponent(rightSplitPane);
            rightSplitPane.setTopComponent(jScrollPane1);
            rightSplitPane.setBottomComponent(jDesktopPane1);
            rightSplitPane.setDividerLocation(0.1);
            // update the preview data grid
            guiHelper.removeAllFromGridData(previewTable.getModel());
            guiHelper.addToGridData(previewTable.getModel(), getSelectedNodes());
        }
    }

private void viewSelectedNodesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewSelectedNodesMenuItemActionPerformed
// TODO add your handling code here:
    linorgWindowManager.openFloatingTable(getSelectedNodes().elements(), "Selection");
}//GEN-LAST:event_viewSelectedNodesMenuItemActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new LinorgFrame();
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem actorsToGridMenuItem;
    private javax.swing.JMenuItem addDefaultLocationsMenuItem;
    private javax.swing.JMenuItem addLocalDirectoryMenuItem;
    private javax.swing.JMenuItem addRemoteCorpusMenuItem;
    private javax.swing.JMenuItem copyBranchMenuItem;
    private javax.swing.JMenuItem copyImdiUrlMenuItem;
    private javax.swing.JMenuItem editFieldViewsMenuItem;
    private javax.swing.JMenuItem editLocationsMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JDesktopPane jDesktopPane1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSplitPane leftLocalSplitPane;
    private javax.swing.JSplitPane leftSplitPane;
    private javax.swing.JTree localCorpusTree;
    private javax.swing.JTree localDirectoryTree;
    private javax.swing.JSplitPane mainSplitPane;
    private javax.swing.JMenu optionsMenu;
    private javax.swing.JTable previewTable;
    private javax.swing.JMenuItem reloadSubnodesMenuItem;
    private javax.swing.JTree remoteCorpusTree;
    private javax.swing.JMenuItem removeCachedCopyMenuItem;
    private javax.swing.JMenuItem removeLocalDirectoryMenuItem;
    private javax.swing.JMenuItem removeRemoteCorpusMenuItem;
    private javax.swing.JSplitPane rightSplitPane;
    private javax.swing.JCheckBoxMenuItem saveWindowsCheckBoxMenuItem;
    private javax.swing.JMenuItem searchSubnodesMenuItem;
    private javax.swing.JCheckBoxMenuItem showSelectionPreviewCheckBoxMenuItem;
    private javax.swing.JPopupMenu treePopupMenu;
    private javax.swing.JSeparator treePopupMenuSeparator1;
    private javax.swing.JSeparator treePopupMenuSeparator2;
    private javax.swing.JMenu viewMenu;
    private javax.swing.JMenuItem viewSelectedNodesMenuItem;
    private javax.swing.JMenuItem viewXmlMenuItem;
    private javax.swing.JMenu windowMenu;
    // End of variables declaration//GEN-END:variables

}
