package mpi.linorg;

import javax.swing.JOptionPane;

/**
 * ArbilMenuBar.java
 * Created on Jul 9, 2009, 12:01:02 PM
 * @author Peter.Withers@mpi.nl
 */
public class ArbilMenuBar extends javax.swing.JMenuBar {

    private javax.swing.JMenuItem saveFileMenuItem;
    private javax.swing.JCheckBoxMenuItem saveWindowsCheckBoxMenuItem;
    private javax.swing.JMenuItem shortCutKeysjMenuItem;
    private javax.swing.JCheckBoxMenuItem showSelectionPreviewCheckBoxMenuItem;
    private javax.swing.JMenu templatesMenu;
    private javax.swing.JCheckBoxMenuItem trackTableSelectionCheckBoxMenuItem;
    private javax.swing.JMenuItem undoMenuItem;
    private javax.swing.JMenuItem viewFavouritesMenuItem;
    private javax.swing.JMenu viewMenu;
    static public javax.swing.JMenu windowMenu;
    private javax.swing.JMenu optionsMenu;
    private javax.swing.JMenuItem pasteMenuItem;
    private javax.swing.JMenuItem printHelpMenuItem;
    private javax.swing.JMenuItem redoMenuItem;
    private javax.swing.JMenuItem aboutMenuItem;
    public javax.swing.JCheckBoxMenuItem checkNewVersionAtStartCheckBoxMenuItem;
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
    private PreviewSplitPanel previewSplitPanel;

    public ArbilMenuBar(PreviewSplitPanel previewSplitPanelLocal) {
        previewSplitPanel = previewSplitPanelLocal;
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

        this.add(fileMenu);

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

        this.add(editMenu);

        optionsMenu.setText("Options");

        editLocationsMenuItem.setText("Locations");
        editLocationsMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editLocationsMenuItemActionPerformed(evt);
            }
        });
        optionsMenu.add(editLocationsMenuItem);

        templatesMenu.setText("Templates");
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
//        editFieldViewsMenuItem.addActionListener(new java.awt.event.ActionListener() {
//
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                editFieldViewsMenuItemActionPerformed(evt);
//            }
//        });
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

        this.add(optionsMenu);

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
        this.add(viewMenu);

        windowMenu.setText("Window");
        this.add(windowMenu);

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
        this.add(helpMenu);

        showSelectionPreviewCheckBoxMenuItem.setSelected(GuiHelper.linorgSessionStorage.loadBoolean("showSelectionPreview", true));
        showSelectionPreviewCheckBoxMenuItemActionPerformed(null);
        trackTableSelectionCheckBoxMenuItem.setSelected(GuiHelper.linorgSessionStorage.loadBoolean("trackTableSelection", false));
        TreeHelper.trackTableSelection = trackTableSelectionCheckBoxMenuItem.getState();
        checkNewVersionAtStartCheckBoxMenuItem.setSelected(GuiHelper.linorgSessionStorage.loadBoolean("checkNewVersionAtStart", true));
        copyNewResourcesCheckBoxMenuItem.setSelected(GuiHelper.linorgSessionStorage.loadBoolean("copyNewResources", true));
        GuiHelper.imdiSchema.copyNewResourcesToCache = copyNewResourcesCheckBoxMenuItem.isSelected();
        saveWindowsCheckBoxMenuItem.setSelected(GuiHelper.linorgSessionStorage.loadBoolean("saveWindows", true));
        showSelectionPreviewCheckBoxMenuItemActionPerformed(null); // this is to set the preview table visible or not
        GuiHelper.imdiSchema.populateTemplatesMenu(templatesMenu);
        printHelpMenuItem.setVisible(false);
    }

    private void editLocationsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
// TODO add your handling code here:
        TreeHelper.getSingleInstance().showLocationsDialog();
    }

    private void viewMenuMenuSelected(javax.swing.event.MenuEvent evt) {
// TODO add your handling code here:
        GuiHelper.getSingleInstance().initViewMenu(viewMenu);
    }

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
// TODO add your handling code here:
        performCleanExit();
    }

    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
// TODO add your handling code here:
        LinorgWindowManager.getSingleInstance().openAboutPage();
    }

    private void saveFileMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
// TODO add your handling code here:
        GuiHelper.imdiLoader.saveNodesNeedingSave(true);
    }

    private void fileMenuMenuSelected(javax.swing.event.MenuEvent evt) {
// TODO add your handling code here:
        saveFileMenuItem.setEnabled(GuiHelper.imdiLoader.nodesNeedSave());
    }

    private void shortCutKeysjMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
// TODO add your handling code here:
        LinorgHelp helpComponent = LinorgHelp.getSingleInstance();
        if (null == LinorgWindowManager.getSingleInstance().focusWindow(LinorgHelp.helpWindowTitle)) {
            LinorgWindowManager.getSingleInstance().createWindow(LinorgHelp.helpWindowTitle, helpComponent);
        }
        helpComponent.setCurrentPage(LinorgHelp.ShorCutKeysPage);
    }

    private void helpMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
// TODO add your handling code here:
        if (null == LinorgWindowManager.getSingleInstance().focusWindow(LinorgHelp.helpWindowTitle)) {
            // forcus existing or create a new help window
            LinorgWindowManager.getSingleInstance().createWindow(LinorgHelp.helpWindowTitle, LinorgHelp.getSingleInstance());
        }
    }

    private void copyNewResourcesCheckBoxMenuItemItemStateChanged(java.awt.event.ItemEvent evt) {
// TODO add your handling code here:
        GuiHelper.imdiSchema.copyNewResourcesToCache = copyNewResourcesCheckBoxMenuItem.isSelected();
    }

    private void importMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
// TODO add your handling code here:
        try {
            ImportExportDialog importExportDialog = new ImportExportDialog(TreeHelper.getSingleInstance().remoteCorpusTree);
            importExportDialog.importImdiBranch();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void viewFavouritesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        LinorgWindowManager.getSingleInstance().openFloatingTableOnce(LinorgFavourites.getSingleInstance().listAllFavourites(), "Favourites");
    }

    private void printHelpMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        if (null == LinorgWindowManager.getSingleInstance().focusWindow(LinorgHelp.helpWindowTitle)) {
            // forcus existing or create a new help window
            LinorgWindowManager.getSingleInstance().createWindow(LinorgHelp.helpWindowTitle, LinorgHelp.getSingleInstance());
        }
        LinorgHelp.getSingleInstance().printAsOneFile();
    }

    private void trackTableSelectionCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        TreeHelper.trackTableSelection = trackTableSelectionCheckBoxMenuItem.getState();
    }

    private void showSelectionPreviewCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        previewSplitPanel.setPreviewPanel(showSelectionPreviewCheckBoxMenuItem.getState());
    }

    public void performCleanExit() { // TODO: this should be moved into a utility class
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
//                viewChangesMenuItem.setEnabled(false);
//        screenCapture.stopCapture();
        System.exit(0);
    }
}
