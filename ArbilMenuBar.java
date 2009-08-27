package mpi.linorg;

import java.util.Arrays;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

/**
 * ArbilMenuBar.java
 * Created on Jul 9, 2009, 12:01:02 PM
 * @author Peter.Withers@mpi.nl
 */
public class ArbilMenuBar extends JMenuBar {

    private JMenuItem saveFileMenuItem;
    private JCheckBoxMenuItem saveWindowsCheckBoxMenuItem;
    private JMenuItem shortCutKeysjMenuItem;
    private JCheckBoxMenuItem showSelectionPreviewCheckBoxMenuItem;
    private JMenu templatesMenu;
    private JCheckBoxMenuItem trackTableSelectionCheckBoxMenuItem;
    private JMenuItem undoMenuItem;
    private JMenuItem viewFavouritesMenuItem;
    private JMenu setStorageDirectoryMenu;
    private JMenu viewMenu;
    static public JMenu windowMenu;
    private JMenu optionsMenu;
    private JMenuItem pasteMenuItem;
    private JMenuItem printHelpMenuItem;
    private JMenuItem redoMenuItem;
    private JMenuItem aboutMenuItem;
    public JCheckBoxMenuItem checkNewVersionAtStartCheckBoxMenuItem;
    private JMenuItem copyMenuItem;
    private JCheckBoxMenuItem copyNewResourcesCheckBoxMenuItem;
    private JMenuItem editFieldViewsMenuItem;
    private JMenuItem editLocationsMenuItem;
    private JMenu editMenu;
    private JMenuItem exitMenuItem;
    private JMenu fileMenu;
    private JMenu helpMenu;
    private JMenuItem helpMenuItem;
    private JMenuItem importMenuItem;
    private PreviewSplitPanel previewSplitPanel;

    public ArbilMenuBar(PreviewSplitPanel previewSplitPanelLocal) {
        previewSplitPanel = previewSplitPanelLocal;
        fileMenu = new JMenu();
        saveFileMenuItem = new JMenuItem();
        importMenuItem = new JMenuItem();
        exitMenuItem = new JMenuItem();
        editMenu = new JMenu();
        copyMenuItem = new JMenuItem();
        pasteMenuItem = new JMenuItem();
        undoMenuItem = new JMenuItem();
        redoMenuItem = new JMenuItem();
        optionsMenu = new JMenu();
        editLocationsMenuItem = new JMenuItem();
        templatesMenu = new JMenu();
        viewFavouritesMenuItem = new JMenuItem();
        setStorageDirectoryMenu = new JMenu();
        editFieldViewsMenuItem = new JMenuItem();
        saveWindowsCheckBoxMenuItem = new JCheckBoxMenuItem();
        showSelectionPreviewCheckBoxMenuItem = new JCheckBoxMenuItem();
        checkNewVersionAtStartCheckBoxMenuItem = new JCheckBoxMenuItem();
        copyNewResourcesCheckBoxMenuItem = new JCheckBoxMenuItem();
        trackTableSelectionCheckBoxMenuItem = new JCheckBoxMenuItem();
        viewMenu = new JMenu();
        windowMenu = new JMenu();
        helpMenu = new JMenu();
        aboutMenuItem = new JMenuItem();
        helpMenuItem = new JMenuItem();
        shortCutKeysjMenuItem = new JMenuItem();
        printHelpMenuItem = new JMenuItem();
        fileMenu.setText("File");
        fileMenu.addMenuListener(new MenuListener() {

            public void menuCanceled(MenuEvent evt) {
            }

            public void menuDeselected(MenuEvent evt) {
            }

            public void menuSelected(MenuEvent evt) {
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

        // TODO: when the undo / redo is done this can be readded, node that copy paste from this menu may not be a good idea due to the many simultainous selections possible
        //this.add(editMenu);

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
        templatesMenu.addMenuListener(new javax.swing.event.MenuListener() {

            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }

            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }

            public void menuSelected(javax.swing.event.MenuEvent evt) {
                populateTemplatesMenu(templatesMenu);
            }
        });

        viewFavouritesMenuItem.setText("View Favourites");
        viewFavouritesMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewFavouritesMenuItemActionPerformed(evt);
            }
        });
        optionsMenu.add(viewFavouritesMenuItem);

        setStorageDirectoryMenu.setText("Storage Directory");
        setStorageDirectoryMenu.addMenuListener(new javax.swing.event.MenuListener() {

            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }

            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }

            public void menuSelected(javax.swing.event.MenuEvent evt) {
                populateStorageLocationMenu(setStorageDirectoryMenu);

            }
        });
        optionsMenu.add(setStorageDirectoryMenu);
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
        viewMenu.addMenuListener(new MenuListener() {

            public void menuCanceled(MenuEvent evt) {
            }

            public void menuDeselected(MenuEvent evt) {
            }

            public void menuSelected(MenuEvent evt) {
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

        showSelectionPreviewCheckBoxMenuItem.setSelected(LinorgSessionStorage.getSingleInstance().loadBoolean("showSelectionPreview", true));
        showSelectionPreviewCheckBoxMenuItemActionPerformed(null);
        trackTableSelectionCheckBoxMenuItem.setSelected(LinorgSessionStorage.getSingleInstance().loadBoolean("trackTableSelection", false));
        TreeHelper.trackTableSelection = trackTableSelectionCheckBoxMenuItem.getState();
        checkNewVersionAtStartCheckBoxMenuItem.setSelected(LinorgSessionStorage.getSingleInstance().loadBoolean("checkNewVersionAtStart", true));
        copyNewResourcesCheckBoxMenuItem.setSelected(LinorgSessionStorage.getSingleInstance().loadBoolean("copyNewResources", true));
        GuiHelper.imdiSchema.copyNewResourcesToCache = copyNewResourcesCheckBoxMenuItem.isSelected();
        saveWindowsCheckBoxMenuItem.setSelected(LinorgSessionStorage.getSingleInstance().loadBoolean("saveWindows", true));
        showSelectionPreviewCheckBoxMenuItemActionPerformed(null); // this is to set the preview table visible or not
        printHelpMenuItem.setVisible(false);
    }

    private void editLocationsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
// TODO add your handling code here:
        TreeHelper.getSingleInstance().showLocationsDialog();
    }

    private void viewMenuMenuSelected(MenuEvent evt) {
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

    private void fileMenuMenuSelected(MenuEvent evt) {
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
            ImportExportDialog importExportDialog = new ImportExportDialog(TreeHelper.getSingleInstance().arbilTreePanel.remoteCorpusTree);
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

    private void populateStorageLocationMenu(JMenu storageMenu) {
        storageMenu.removeAll();
        ButtonGroup storageMenuButtonGroup = new ButtonGroup();
        String[] storageLocaations = LinorgSessionStorage.getSingleInstance().getLocationOptions();
        Arrays.sort(storageLocaations);
        for (String currentTemplateName : storageLocaations) {
            if (!currentTemplateName.startsWith("null")) {
                JRadioButtonMenuItem templateMenuItem = new JRadioButtonMenuItem();
                templateMenuItem.setText(currentTemplateName);
                templateMenuItem.setName(currentTemplateName);
                templateMenuItem.setActionCommand(currentTemplateName);
                templateMenuItem.addActionListener(new java.awt.event.ActionListener() {

                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        try {
                            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("This action is not yet available.", "Storage Directory");
                        } catch (Exception e) {
                            GuiHelper.linorgBugCatcher.logError(e);
                        }
                    }
                });
                templateMenuItem.setSelected(LinorgSessionStorage.getSingleInstance().storageDirectory.equals(currentTemplateName));
                storageMenuButtonGroup.add(templateMenuItem);
                storageMenu.add(templateMenuItem);
            }
        }
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
            LinorgSessionStorage.getSingleInstance().saveObject(showSelectionPreviewCheckBoxMenuItem.isSelected(), "showSelectionPreview");
            LinorgSessionStorage.getSingleInstance().saveObject(trackTableSelectionCheckBoxMenuItem.isSelected(), "trackTableSelection");
            LinorgSessionStorage.getSingleInstance().saveObject(checkNewVersionAtStartCheckBoxMenuItem.isSelected(), "checkNewVersionAtStart");
            LinorgSessionStorage.getSingleInstance().saveObject(copyNewResourcesCheckBoxMenuItem.isSelected(), "copyNewResources");
            LinorgSessionStorage.getSingleInstance().saveObject(saveWindowsCheckBoxMenuItem.isSelected(), "saveWindows");
        } catch (Exception ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
        }
//                viewChangesMenuItem.setEnabled(false);
//        screenCapture.stopCapture();
        System.exit(0);
    }

    private void addTemplateMenuItem(JMenu templateMenu, ButtonGroup templatesMenuButtonGroup, String templateName, String selectedTemplate) {
        // TODO: move this into the menu bar class
        JRadioButtonMenuItem templateMenuItem = new JRadioButtonMenuItem();
        templateMenuItem.setText(templateName);
        templateMenuItem.setName(templateName);
        templateMenuItem.setActionCommand(templateName);
        templateMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
//                    LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("This action is not yet available.", "Templates");
                    //GuiHelper.linorgWindowManager.openUrlWindow(evt.getActionCommand() + templateList.get(evt.getActionCommand()).toString(), new File(templateList.get(evt.getActionCommand()).toString()).toURL());
                    System.out.println("setting template: " + evt.getActionCommand());
                    ArbilTemplateManager.getSingleInstance().setCurrentTemplate(evt.getActionCommand());
                } catch (Exception e) {
                    GuiHelper.linorgBugCatcher.logError(e);
                }
            }
        });
        templatesMenuButtonGroup.add(templateMenuItem);
        templateMenu.add(templateMenuItem);
        if (selectedTemplate.equals(templateName)) {
            templateMenuItem.setSelected(true);
        }
    }

    public void populateTemplatesMenu(JMenu templateMenu) {
        templateMenu.removeAll();
        ButtonGroup templatesMenuButtonGroup = new javax.swing.ButtonGroup();

        int templateCount = 0;
//        addTemplateMenuItem(templateMenu, templatesMenuButtonGroup, "", "Default", ArbilTemplateManager.getSingleInstance().getCurrentTemplate());
        for (String currentTemplateName : ArbilTemplateManager.getSingleInstance().getAvailableTemplates()) {
//            String templatePath = templatesDir.getPath() + File.separatorChar + currentTemplateName;
//            if (new File(templatePath).isDirectory()) {
            addTemplateMenuItem(templateMenu, templatesMenuButtonGroup, currentTemplateName, ArbilTemplateManager.getSingleInstance().getCurrentTemplate());
            templateCount++;
//            }
        }
        if (templateCount == 0) {
            JMenuItem noneMenuItem = new JMenuItem("<none installed>");
            noneMenuItem.setEnabled(false);
            templateMenu.add(noneMenuItem);
        }
    }
}
