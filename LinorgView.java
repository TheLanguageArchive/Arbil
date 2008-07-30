/*
 * LinorgView.java
 */
package mpi.linorg;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * The application's main frame.
 */
public class LinorgView extends JFrame {

    private DefaultTreeModel localCorpusTreeModel;
    private DefaultTreeModel remoteCorpusTreeModel;
    private DefaultTreeModel localDirectoryTreeModel;
    private DefaultMutableTreeNode localCorpusRootNode;
    private DefaultMutableTreeNode remoteCorpusRootNode;
    private DefaultMutableTreeNode localDirectoryRootNode;
    private DefaultTableModel tableModel = new javax.swing.table.DefaultTableModel();
    private GuiHelper imdiHelper = new GuiHelper();

    public LinorgView() {
        localCorpusRootNode = new DefaultMutableTreeNode("Local Corpus");
        localCorpusRootNode.add(imdiHelper.getImdiTreeNode("file://data1/media-archive-copy/Corpusstructure/MPI.imdi"));
        remoteCorpusRootNode = new DefaultMutableTreeNode("Remote Corpus");
        remoteCorpusRootNode.add(imdiHelper.getImdiTreeNode("http://corpus1.mpi.nl/IMDI/metadata/IMDI.imdi"));
        localDirectoryRootNode = new DefaultMutableTreeNode("Local File System");
        localDirectoryRootNode.add(imdiHelper.getImdiTreeNode("file://data1/media-archive-copy/TestWorkingDirectory/"));
        localCorpusTreeModel = new DefaultTreeModel(localCorpusRootNode, true);
        remoteCorpusTreeModel = new DefaultTreeModel(remoteCorpusRootNode, true);
        localDirectoryTreeModel = new DefaultTreeModel(localDirectoryRootNode, true);

        initComponents();
        initViewMenu();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);

    // status bar initialization - message timeout, idle icon and busy animation, etc
    //ResourceMap resourceMap = getResourceMap();
    //int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
    //messageTimer = new Timer(messageTimeout, new ActionListener() {
//
//            public void actionPerformed(ActionEvent e) {
//                statusMessageLabel.setText("");
//            }
//        });
//        messageTimer.setRepeats(false);
//        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
//        for (int i = 0; i < busyIcons.length; i++) {
//            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
//        }
//        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
//
//            public void actionPerformed(ActionEvent e) {
//                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
//                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
//            }
//        });
//        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
//        statusAnimationLabel.setIcon(idleIcon);
//        progressBar.setVisible(false);
//
//        // connecting action tasks to status bar via TaskMonitor
//        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
//        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
//
//            public void propertyChange(java.beans.PropertyChangeEvent evt) {
//                String propertyName = evt.getPropertyName();
//                if ("started".equals(propertyName)) {
//                    if (!busyIconTimer.isRunning()) {
//                        statusAnimationLabel.setIcon(busyIcons[0]);
//                        busyIconIndex = 0;
//                        busyIconTimer.start();
//                    }
//                    progressBar.setVisible(true);
//                    progressBar.setIndeterminate(true);
//                } else if ("done".equals(propertyName)) {
//                    busyIconTimer.stop();
//                    statusAnimationLabel.setIcon(idleIcon);
//                    progressBar.setVisible(false);
//                    progressBar.setValue(0);
//                } else if ("message".equals(propertyName)) {
//                    String text = (String) (evt.getNewValue());
//                    statusMessageLabel.setText((text == null) ? "" : text);
//                    messageTimer.restart();
//                } else if ("progress".equals(propertyName)) {
//                    int value = (Integer) (evt.getNewValue());
//                    progressBar.setVisible(true);
//                    progressBar.setIndeterminate(false);
//                    progressBar.setValue(value);
//                }
//            }
//        });
    }

    private void initViewMenu() {
        String[] viewLabels = imdiHelper.getFieldListLables();
        for (int menuItemCount = 0; menuItemCount < viewLabels.length; menuItemCount++) {
            javax.swing.JRadioButtonMenuItem viewLabelRadioButtonMenuItem;
            viewLabelRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
            viewMenuButtonGroup.add(viewLabelRadioButtonMenuItem);
            viewLabelRadioButtonMenuItem.setSelected(imdiHelper.getCurrentFieldListIndex() == menuItemCount);
            viewLabelRadioButtonMenuItem.setText(viewLabels[menuItemCount]);
            viewLabelRadioButtonMenuItem.setName("viewLabelRadioButtonMenuItem" + menuItemCount);
            viewLabelRadioButtonMenuItem.addChangeListener(new javax.swing.event.ChangeListener() {

                public void stateChanged(javax.swing.event.ChangeEvent evt) {
                    for (int checkboxCounter = 0; checkboxCounter < viewMenu.getItemCount(); checkboxCounter++) {
                        if (viewMenu.getItem(checkboxCounter).isSelected()) {
                            imdiHelper.setCurrentFieldListIndex(checkboxCounter);
                        }
                    }
                }
            });
            viewMenu.add(viewLabelRadioButtonMenuItem);
        }
    }

    public void showSettingsDialog() {
        if (settingsjDialog == null) {
            settingsjDialog = new LinorgAboutBox(this);
            settingsjDialog.setLocationRelativeTo(this);
        }
        settingsjDialog.show();
    }

    public void showAboutBox() {
        if (aboutBox == null) {
            aboutBox = new LinorgAboutBox(this);
            aboutBox.setLocationRelativeTo(this);
        }
        aboutBox.show();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        mainSplitPane = new javax.swing.JSplitPane();
        jSplitPane2 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        remoteCorpusTree = new javax.swing.JTree();
        jSplitPane4 = new javax.swing.JSplitPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        localCorpusTree = new javax.swing.JTree();
        jScrollPane3 = new javax.swing.JScrollPane();
        localDirectoryTree = new javax.swing.JTree();
        rightSplitPane = new javax.swing.JSplitPane();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jScrollPane6 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        viewMenu = new javax.swing.JMenu();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTree2 = new javax.swing.JTree();
        settingsjDialog = new javax.swing.JDialog();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane7 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jScrollPane8 = new javax.swing.JScrollPane();
        jTable3 = new javax.swing.JTable();
        viewMenuButtonGroup = new javax.swing.ButtonGroup();

        mainPanel.setName("mainPanel"); // NOI18N

        mainSplitPane.setName("mainSplitPane"); // NOI18N

        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane2.setName("jSplitPane2"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        remoteCorpusTree.setModel(remoteCorpusTreeModel);
        remoteCorpusTree.setName("remoteCorpusTree"); // NOI18N
        remoteCorpusTree.addTreeWillExpandListener(new javax.swing.event.TreeWillExpandListener() {

            public void treeWillCollapse(javax.swing.event.TreeExpansionEvent evt) throws javax.swing.tree.ExpandVetoException {
            }

            public void treeWillExpand(javax.swing.event.TreeExpansionEvent evt) throws javax.swing.tree.ExpandVetoException {
                jTreeTreeWillExpand(evt);
            }
        });
        remoteCorpusTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {

            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                jTreeValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(remoteCorpusTree);

        jSplitPane2.setTopComponent(jScrollPane1);

        jSplitPane4.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane4.setName("jSplitPane4"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        localCorpusTree.setModel(localCorpusTreeModel);
        localCorpusTree.setName("localCorpusTree"); // NOI18N
        localCorpusTree.addTreeWillExpandListener(new javax.swing.event.TreeWillExpandListener() {

            public void treeWillCollapse(javax.swing.event.TreeExpansionEvent evt) throws javax.swing.tree.ExpandVetoException {
            }

            public void treeWillExpand(javax.swing.event.TreeExpansionEvent evt) throws javax.swing.tree.ExpandVetoException {
                jTreeTreeWillExpand(evt);
            }
        });
        localCorpusTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {

            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                jTreeValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(localCorpusTree);

        jSplitPane4.setTopComponent(jScrollPane2);

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        localDirectoryTree.setModel(localDirectoryTreeModel);
        localDirectoryTree.setName("localDirectoryTree"); // NOI18N
        localDirectoryTree.addTreeWillExpandListener(new javax.swing.event.TreeWillExpandListener() {

            public void treeWillCollapse(javax.swing.event.TreeExpansionEvent evt) throws javax.swing.tree.ExpandVetoException {
            }

            public void treeWillExpand(javax.swing.event.TreeExpansionEvent evt) throws javax.swing.tree.ExpandVetoException {
                jTreeTreeWillExpand(evt);
            }
        });
        localDirectoryTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {

            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                jTreeValueChanged(evt);
            }
        });
        jScrollPane3.setViewportView(localDirectoryTree);

        jSplitPane4.setRightComponent(jScrollPane3);

        jSplitPane2.setRightComponent(jSplitPane4);

        mainSplitPane.setLeftComponent(jSplitPane2);

        rightSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        rightSplitPane.setName("rightSplitPane"); // NOI18N

        jScrollPane5.setName("jScrollPane5"); // NOI18N

        jTextPane1.setName("jTextPane1"); // NOI18N
        jScrollPane5.setViewportView(jTextPane1);

        rightSplitPane.setBottomComponent(jScrollPane5);

        jScrollPane6.setName("jScrollPane6"); // NOI18N

        jTable1.setModel(tableModel);
        jTable1.setName("jTable1"); // NOI18N
        jScrollPane6.setViewportView(jTable1);

        rightSplitPane.setLeftComponent(jScrollPane6);

        mainSplitPane.setRightComponent(rightSplitPane);

//        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
//        mainPanel.setLayout(mainPanelLayout);
//        mainPanelLayout.setHorizontalGroup(
//            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        this.getContentPane().add(mainSplitPane, BorderLayout.CENTER);
//        );
//        mainPanelLayout.setVerticalGroup(
//            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//            .addComponent(mainSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 519, Short.MAX_VALUE)
//        );

        menuBar.setName("menuBar"); // NOI18N

//        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(mpi.linorg.Linorg.class).getContext().getResourceMap(LinorgView.class);
//        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setText("File");
        fileMenu.setName("fileMenu"); // NOI18N

        //javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(mpi.linorg.Linorg.class).getContext().getActionMap(LinorgView.class, this);
        //jMenuItem1.setAction(actionMap.get("showSettingsDialog")); // NOI18N
        //jMenuItem1.setText(resourceMap.getString("jMenuItem1.text")); // NOI18N
        jMenuItem1.setAction(new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                showSettingsDialog();
            }
        });
        jMenuItem1.setText("Settings");
        //jMenuItem1.setToolTipText(resourceMap.getString("jMenuItem1.toolTipText")); // NOI18N
        jMenuItem1.setName("jMenuItem1"); // NOI18N
        fileMenu.add(jMenuItem1);
//        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setAction(new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
            }
        });
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        exitMenuItem.setText("Exit");
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        viewMenu.setText("View");
        viewMenu.setName("viewMenu"); // NOI18N
        menuBar.add(viewMenu);
//        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setText("Help");
        helpMenu.setName("helpMenu"); // NOI18N

//        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setAction(
                new AbstractAction() {

                    public void actionPerformed(ActionEvent e) {
                        showAboutBox();
                    }
                });
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        aboutMenuItem.setText("About");
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

//        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
//        statusPanel.setLayout(statusPanelLayout);
//        statusPanelLayout.setHorizontalGroup(
//            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 741, Short.MAX_VALUE)
//            .addGroup(statusPanelLayout.createSequentialGroup()
//                .addContainerGap()
//                .addComponent(statusMessageLabel)
//                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 557, Short.MAX_VALUE)
//                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
//                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
//                .addComponent(statusAnimationLabel)
//                .addContainerGap())
//        );
//        statusPanelLayout.setVerticalGroup(
//            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//            .addGroup(statusPanelLayout.createSequentialGroup()
//                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
//                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
//                    .addComponent(statusMessageLabel)
//                    .addComponent(statusAnimationLabel)
//                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
//                .addGap(3, 3, 3))
//        );

        jScrollPane4.setName("jScrollPane4"); // NOI18N

        jTree2.setName("jTree2"); // NOI18N
        jScrollPane4.setViewportView(jTree2);

        //this.getContentPane().add(mainPanel);
        menuBar.setPreferredSize(new Dimension(200, 20));
        this.setJMenuBar(menuBar);
    //this.getContentPane().add(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void jTreeTreeWillExpand(javax.swing.event.TreeExpansionEvent evt) throws javax.swing.tree.ExpandVetoException {//GEN-FIRST:event_jTreeTreeWillExpand
// TODO add your handling code here:
        statusMessageLabel.setText("Loading");
        System.out.println("jTree1TreeWillExpand");
        DefaultMutableTreeNode parentNode = null;
        if (evt.getPath() == null) {
            //There is no selection.
            statusMessageLabel.setText("No node selected");
        } else {
            parentNode = (DefaultMutableTreeNode) (evt.getPath().getLastPathComponent());
        }
        // check for imdi data
        imdiHelper.getImdiChildNodes(parentNode);
        remoteCorpusTree.scrollPathToVisible(evt.getPath());
        statusMessageLabel.setText("");
    }//GEN-LAST:event_jTreeTreeWillExpand

    private void jTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_jTreeValueChanged
// TODO add your handling code here:

        int selectedNoedsCount = remoteCorpusTree.getSelectionCount();
        selectedNoedsCount += localCorpusTree.getSelectionCount();
        selectedNoedsCount += localDirectoryTree.getSelectionCount();

        statusMessageLabel.setText("loading details");
//    System.out.println("jTree1ValueChanged");
//    System.out.println("getSelectionCount: " + selectedNoedsCount);

        // if there are no nodes selected then clear the grid or if there is only one node selected then clear the tree then add the node to trigger the single mode view
        if (1 >= selectedNoedsCount) {
            imdiHelper.removeAllFromGridData(tableModel);
            // there is only one tree node selected but we don't know on which tree so check each one and display the selected node
            if (0 < remoteCorpusTree.getSelectionCount()) {
                imdiHelper.addToGridData(tableModel, (DefaultMutableTreeNode) remoteCorpusTree.getSelectionPath().getLastPathComponent(), jTextPane1);
            }
            if (0 < localCorpusTree.getSelectionCount()) {
                imdiHelper.addToGridData(tableModel, (DefaultMutableTreeNode) localCorpusTree.getSelectionPath().getLastPathComponent(), jTextPane1);
            }
            if (0 < localDirectoryTree.getSelectionCount()) {
                imdiHelper.addToGridData(tableModel, (DefaultMutableTreeNode) localDirectoryTree.getSelectionPath().getLastPathComponent(), jTextPane1);
            }
        } else {
            // if there is more than one selected node then remove any deselected nodes before adding new ones
            for (int selectedCount = 0; selectedCount < evt.getPaths().length; selectedCount++) {
                if (!evt.isAddedPath(selectedCount)) {
                    DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) evt.getPaths()[selectedCount].getLastPathComponent();
                    imdiHelper.removeFromGridData(tableModel, parentNode);
                }
            }

            // add any newly selected nodes
            for (int selectedCount = 0; selectedCount < evt.getPaths().length; selectedCount++) {
//        System.out.println("added: " + selectedCount + ":" + evt.isAddedPath(selectedCount) + " path: " + evt.getPaths()[selectedCount]);
                if (evt.isAddedPath(selectedCount)) {
                    DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) evt.getPaths()[selectedCount].getLastPathComponent();
                    imdiHelper.addToGridData(tableModel, parentNode, jTextPane1);
                //imdiHelper.addToGridData(tableModel, 0, (Document)((DefaultMutableTreeNode)evt.getPaths()[selectedCount].getLastPathComponent()).getUserObject());
                }
            }
        }
        statusMessageLabel.setText("");
        System.out.println(evt.isAddedPath());
    }//GEN-LAST:event_jTreeValueChanged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JSplitPane jSplitPane4;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTable jTable3;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JTree jTree2;
    private javax.swing.JTree localCorpusTree;
    private javax.swing.JTree localDirectoryTree;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JSplitPane mainSplitPane;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JTree remoteCorpusTree;
    private javax.swing.JSplitPane rightSplitPane;
    private javax.swing.JDialog settingsjDialog;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JMenu viewMenu;
    private javax.swing.ButtonGroup viewMenuButtonGroup;
    // End of variables declaration//GEN-END:variables
//    private final Timer messageTimer;
//    private final Timer busyIconTimer;
//    private final Icon idleIcon;
//    private final Icon[] busyIcons = new Icon[15];
//    private int busyIconIndex = 0;
    private JDialog aboutBox;
}
