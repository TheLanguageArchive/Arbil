package nl.mpi.arbil;

import nl.mpi.arbil.importexport.ImportExportDialog;
import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.util.ArrayList;
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
import javax.swing.table.TableCellEditor;
import nl.mpi.arbil.data.ImdiLoader;
import nl.mpi.arbil.data.ImdiSchema;
import nl.mpi.arbil.data.ImdiTreeObject;

/**
 * ArbilMenuBar.java
 * Created on Jul 9, 2009, 12:01:02 PM
 * @author Peter.Withers@mpi.nl
 */
public class ArbilMenuBar extends JMenuBar {

    private JMenuItem saveFileMenuItem;
    private JMenuItem showChangedNodesMenuItem;
    private JCheckBoxMenuItem saveWindowsCheckBoxMenuItem;
    private JMenuItem shortCutKeysjMenuItem;
    private JMenuItem arbilForumMenuItem;
    private JMenuItem viewErrorLogMenuItem;
    private JCheckBoxMenuItem showSelectionPreviewCheckBoxMenuItem;
    private JMenu templatesMenu;
    private JCheckBoxMenuItem trackTableSelectionCheckBoxMenuItem;
    private JMenuItem undoMenuItem;
//    private JMenuItem viewFavouritesMenuItem;
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
    private JCheckBoxMenuItem checkResourcePermissionsCheckBoxMenuItem;
    private JMenuItem editFieldViewsMenuItem;
//    private JMenuItem editLocationsMenuItem;
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
        showChangedNodesMenuItem = new JMenuItem();
        importMenuItem = new JMenuItem();
        exitMenuItem = new JMenuItem();
        editMenu = new JMenu();
        copyMenuItem = new JMenuItem();
        pasteMenuItem = new JMenuItem();
        undoMenuItem = new JMenuItem();
        redoMenuItem = new JMenuItem();
        optionsMenu = new JMenu();
//        editLocationsMenuItem = new JMenuItem();
        templatesMenu = new JMenu();
//        viewFavouritesMenuItem = new JMenuItem();
        setStorageDirectoryMenu = new JMenu();
        editFieldViewsMenuItem = new JMenuItem();
        saveWindowsCheckBoxMenuItem = new JCheckBoxMenuItem();
        showSelectionPreviewCheckBoxMenuItem = new JCheckBoxMenuItem();
        checkNewVersionAtStartCheckBoxMenuItem = new JCheckBoxMenuItem();
        copyNewResourcesCheckBoxMenuItem = new JCheckBoxMenuItem();
        checkResourcePermissionsCheckBoxMenuItem = new JCheckBoxMenuItem();
        trackTableSelectionCheckBoxMenuItem = new JCheckBoxMenuItem();
        viewMenu = new JMenu();
        windowMenu = new JMenu();
        helpMenu = new JMenu();
        aboutMenuItem = new JMenuItem();
        helpMenuItem = new JMenuItem();
        shortCutKeysjMenuItem = new JMenuItem();
        arbilForumMenuItem = new JMenuItem();
        viewErrorLogMenuItem = new JMenuItem();
        printHelpMenuItem = new JMenuItem();
        fileMenu.setText("File");
        fileMenu.addMenuListener(new MenuListener() {

            public void menuCanceled(MenuEvent evt) {
            }

            public void menuDeselected(MenuEvent evt) {
            }

            public void menuSelected(MenuEvent evt) {
                saveFileMenuItem.setEnabled(ImdiLoader.getSingleInstance().nodesNeedSave());
                showChangedNodesMenuItem.setEnabled(ImdiLoader.getSingleInstance().nodesNeedSave());
            }
        });

        saveFileMenuItem.setText("Save Changes");
        saveFileMenuItem.setEnabled(false);
        saveFileMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    ImdiLoader.getSingleInstance().saveNodesNeedingSave(true);
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        fileMenu.add(saveFileMenuItem);

        showChangedNodesMenuItem.setText("Show Modified Nodes");
        showChangedNodesMenuItem.setEnabled(false);
        showChangedNodesMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    ArrayList<ImdiTreeObject> individualChangedNodes = new ArrayList<ImdiTreeObject>();
                    for (ImdiTreeObject currentTestable : ImdiLoader.getSingleInstance().getNodesNeedSave()) {
                        if (currentTestable.hasChangedFields()) {
                            individualChangedNodes.add(currentTestable);
                        }
                        for (ImdiTreeObject currentChildSaveable : currentTestable.getAllChildren()) {
                            if (currentChildSaveable.hasChangedFields()) {
                                individualChangedNodes.add(currentChildSaveable);
                            }
                        }
                    }
                    LinorgWindowManager.getSingleInstance().openFloatingTable(individualChangedNodes.toArray(new ImdiTreeObject[]{}), "Modified Nodes");
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        fileMenu.add(showChangedNodesMenuItem);

        importMenuItem.setText("Import");
        importMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    importMenuItemActionPerformed(evt);
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        fileMenu.add(importMenuItem);

        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    exitMenuItemActionPerformed(evt);
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        fileMenu.add(exitMenuItem);

        this.add(fileMenu);

        editMenu.setText("Edit");
        editMenu.addMenuListener(new javax.swing.event.MenuListener() {

            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }

            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }

            public void menuSelected(javax.swing.event.MenuEvent evt) {
                undoMenuItem.setEnabled(LinorgJournal.getSingleInstance().canUndo());
                redoMenuItem.setEnabled(LinorgJournal.getSingleInstance().canRedo());
//                Component currentFocusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
//                copyMenuItem.setEnabled(currentFocusOwner instanceof ImdiTree);
            }
        });

        copyMenuItem.setText("Copy");
        copyMenuItem.setEnabled(false);
//        by the time the menu action has occured the focus has moved to the root pane of the application, this further supports the concept that a global copy paste for a multi focus UI is a flawed concept
//        copyMenuItem.addActionListener(new java.awt.event.ActionListener() {
//
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                Component currentFocusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
//                System.out.println("currentFocusOwner: " + currentFocusOwner);
//                if (currentFocusOwner instanceof ImdiTree) {
//                    ImdiTree sourceTree = (ImdiTree) currentFocusOwner;
//                    ImdiTreeObject[] selectedImdiNodes = sourceTree.getSelectedNodes();
//                    if (selectedImdiNodes == null) {
//                        LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("No node selected", "Copy");
//                    } else {
//                        sourceTree.copyNodeUrlToClipboard(selectedImdiNodes);
//                    }
//                }
//            }
//        });
//        editMenu.add(copyMenuItem);

        pasteMenuItem.setText("Paste");
        pasteMenuItem.setEnabled(false);
//        pasteMenuItem.addActionListener(new java.awt.event.ActionListener() {
//
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                Component currentFocusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
//            }
//        });
//        editMenu.add(pasteMenuItem);

        undoMenuItem.setText("Undo");
        undoMenuItem.setEnabled(false);
        undoMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    LinorgJournal.getSingleInstance().undoFromFieldChangeHistory();
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        editMenu.add(undoMenuItem);
        redoMenuItem.setText("Redo");
        redoMenuItem.setEnabled(false);
        redoMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    LinorgJournal.getSingleInstance().redoFromFieldChangeHistory();
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        editMenu.add(redoMenuItem);

        this.add(editMenu);

        optionsMenu.setText("Options");

//        editLocationsMenuItem.setText("Locations");
//        editLocationsMenuItem.addActionListener(new java.awt.event.ActionListener() {
//
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                try {
//                    editLocationsMenuItemActionPerformed(evt);
//                } catch (Exception ex) {
//                    GuiHelper.linorgBugCatcher.logError(ex);
//                }
//            }
//        });
//        optionsMenu.add(editLocationsMenuItem);

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

//        viewFavouritesMenuItem.setText("View Favourites");
//        viewFavouritesMenuItem.addActionListener(new java.awt.event.ActionListener() {
//
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                try {
//                    viewFavouritesMenuItemActionPerformed(evt);
//                } catch (Exception ex) {
//                    GuiHelper.linorgBugCatcher.logError(ex);
//                }
//            }
//        });
//        optionsMenu.add(viewFavouritesMenuItem);

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
        showSelectionPreviewCheckBoxMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.META_MASK));
        showSelectionPreviewCheckBoxMenuItem.setSelected(true);
        showSelectionPreviewCheckBoxMenuItem.setText("Show Selection Preview");
        showSelectionPreviewCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    showSelectionPreviewCheckBoxMenuItemActionPerformed(evt);
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
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
                ImdiSchema.getSingleInstance().copyNewResourcesToCache = copyNewResourcesCheckBoxMenuItem.isSelected();
            }
        });
        optionsMenu.add(copyNewResourcesCheckBoxMenuItem);

        checkResourcePermissionsCheckBoxMenuItem.setSelected(true);
        checkResourcePermissionsCheckBoxMenuItem.setText("Check permissions for remote resources");
        checkResourcePermissionsCheckBoxMenuItem.setToolTipText("This option checks the server permissions for remote resources and shows icons accordingly.");
        checkResourcePermissionsCheckBoxMenuItem.addItemListener(new java.awt.event.ItemListener() {

            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                MimeHashQueue.getSingleInstance().checkResourcePermissions = checkResourcePermissionsCheckBoxMenuItem.isSelected();
            }
        });
        optionsMenu.add(checkResourcePermissionsCheckBoxMenuItem);

        trackTableSelectionCheckBoxMenuItem.setText("Track Table Selection in Tree");
        trackTableSelectionCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    trackTableSelectionCheckBoxMenuItemActionPerformed(evt);
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
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
        helpMenu.addMenuListener(new MenuListener() {

            public void menuCanceled(MenuEvent evt) {
            }

            public void menuDeselected(MenuEvent evt) {
            }

            public void menuSelected(MenuEvent evt) {
                viewErrorLogMenuItem.setEnabled(new LinorgBugCatcher().getLogFile().exists());
            }
        });

        aboutMenuItem.setText("About");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    aboutMenuItemActionPerformed(evt);
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        helpMenu.add(aboutMenuItem);

        helpMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        helpMenuItem.setText("Help");
        helpMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    helpMenuItemActionPerformed(evt);
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        helpMenu.add(helpMenuItem);

        arbilForumMenuItem.setText("Arbil Forum (Website)");
        arbilForumMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    GuiHelper.getSingleInstance().openFileInExternalApplication(new URI("http://www.lat-mpi.eu/tools/arbil/Arbil-forum/"));
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        helpMenu.add(arbilForumMenuItem);

        viewErrorLogMenuItem.setText("View Error Log");
        viewErrorLogMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    GuiHelper.getSingleInstance().openFileInExternalApplication(new LinorgBugCatcher().getLogFile().toURI());
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        helpMenu.add(viewErrorLogMenuItem);

        shortCutKeysjMenuItem.setText("Short Cut Keys");
        shortCutKeysjMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    shortCutKeysjMenuItemActionPerformed(evt);
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        helpMenu.add(shortCutKeysjMenuItem);

        printHelpMenuItem.setText("Print Help File");
        printHelpMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    printHelpMenuItemActionPerformed(evt);
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
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
        ImdiSchema.getSingleInstance().copyNewResourcesToCache = copyNewResourcesCheckBoxMenuItem.isSelected();
        checkResourcePermissionsCheckBoxMenuItem.setSelected(LinorgSessionStorage.getSingleInstance().loadBoolean("checkResourcePermissions", true));
        MimeHashQueue.getSingleInstance().checkResourcePermissions = checkResourcePermissionsCheckBoxMenuItem.isSelected();
        saveWindowsCheckBoxMenuItem.setSelected(LinorgSessionStorage.getSingleInstance().loadBoolean("saveWindows", true));
        showSelectionPreviewCheckBoxMenuItemActionPerformed(null); // this is to set the preview table visible or not
        printHelpMenuItem.setVisible(false);
        setUpHotKeys();
    }

    private void setUpHotKeys() {
        // the jdesktop seems to be consuming the key strokes used for save, undo and redo so to make this available over the whole interface it has been added here
        saveFileMenuItem.setMnemonic(java.awt.event.KeyEvent.VK_S);
        saveFileMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        undoMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
        redoMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));

        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {

            public void eventDispatched(AWTEvent event) {
//                System.out.println("KeyEvent.paramString: " + ((KeyEvent) event).paramString());
                if ((((KeyEvent) event).isMetaDown() || ((KeyEvent) event).isControlDown()) && event.getID() == KeyEvent.KEY_RELEASED) {
                    // KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent();
                    Component compFocusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                    while (compFocusOwner != null) {
                        // stop any table cell edits
                        if (compFocusOwner instanceof ImdiTable) {
                            TableCellEditor currentEditor = ((ImdiTable) compFocusOwner).getCellEditor();
                            if (currentEditor != null) {
                                currentEditor.stopCellEditing();
                                break;
                            }
                        }
                        compFocusOwner = compFocusOwner.getParent();
                    }
                    if (((KeyEvent) event).getKeyCode() == KeyEvent.VK_S) {
                        ImdiLoader.getSingleInstance().saveNodesNeedingSave(true);
                    }
                    if (((KeyEvent) event).getKeyCode() == java.awt.event.KeyEvent.VK_Z) {
                        if (((KeyEvent) event).isShiftDown()) {
                            LinorgJournal.getSingleInstance().redoFromFieldChangeHistory();
                        } else {
                            LinorgJournal.getSingleInstance().undoFromFieldChangeHistory();
                        }
                    }
                    if (((KeyEvent) event).getKeyCode() == java.awt.event.KeyEvent.VK_Y) {
                        LinorgJournal.getSingleInstance().redoFromFieldChangeHistory();
                    }
                }
            }
        }, AWTEvent.KEY_EVENT_MASK);
    }

//    private void editLocationsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
//// TODO add your handling code here:
//        TreeHelper.getSingleInstance().showLocationsDialog();
//    }
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

    private void importMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
// TODO add your handling code here:
        try {
            ImportExportDialog importExportDialog = new ImportExportDialog(TreeHelper.getSingleInstance().arbilTreePanel.remoteCorpusTree);
            importExportDialog.importImdiBranch();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

//    private void viewFavouritesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
//        LinorgWindowManager.getSingleInstance().openFloatingTableOnce(LinorgFavourites.getSingleInstance().listAllFavourites(), "Favourites");
//    }
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
        ArrayList<String> addedPaths = new ArrayList<String>();
        for (String currentTemplateName : storageLocaations) {
            if (!currentTemplateName.startsWith("null") && !addedPaths.contains(currentTemplateName)) {
                addedPaths.add(currentTemplateName);
                JRadioButtonMenuItem templateMenuItem = new JRadioButtonMenuItem();
                templateMenuItem.setText(currentTemplateName);
                templateMenuItem.setName(currentTemplateName);
                templateMenuItem.setActionCommand(currentTemplateName);
//                templateMenuItem.setEnabled(false);
                templateMenuItem.addActionListener(new java.awt.event.ActionListener() {

                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        try {
                            saveApplicationState();
                            LinorgSessionStorage.getSingleInstance().changeStorageDirectory(evt.getActionCommand());
                            // LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("This action is not yet available.", "Storage Directory");
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
        // TODO: add other cache directory and update changeStorageDirectory to cope with the additional variables 
    }

    private boolean saveApplicationState() {
        if (ImdiLoader.getSingleInstance().nodesNeedSave()) {
            switch (JOptionPane.showConfirmDialog(this, "Save changes before exiting?", "Arbil", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)) {
                case JOptionPane.NO_OPTION:
                    break;
                case JOptionPane.YES_OPTION:
                    ImdiLoader.getSingleInstance().saveNodesNeedingSave(false);
                    break;
                default:
                    return false;
            }
        }
        GuiHelper.getSingleInstance().saveState(saveWindowsCheckBoxMenuItem.isSelected());
        try {
            LinorgSessionStorage.getSingleInstance().saveObject(showSelectionPreviewCheckBoxMenuItem.isSelected(), "showSelectionPreview");
            LinorgSessionStorage.getSingleInstance().saveObject(trackTableSelectionCheckBoxMenuItem.isSelected(), "trackTableSelection");
            LinorgSessionStorage.getSingleInstance().saveObject(checkNewVersionAtStartCheckBoxMenuItem.isSelected(), "checkNewVersionAtStart");
            LinorgSessionStorage.getSingleInstance().saveObject(copyNewResourcesCheckBoxMenuItem.isSelected(), "copyNewResources");
            LinorgSessionStorage.getSingleInstance().saveObject(checkResourcePermissionsCheckBoxMenuItem.isSelected(), "checkResourcePermissions");
            LinorgSessionStorage.getSingleInstance().saveObject(saveWindowsCheckBoxMenuItem.isSelected(), "saveWindows");
        } catch (Exception ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
        }
        return true;
    }

    public void performCleanExit() { // TODO: this should be moved into a utility class
        if (saveApplicationState()) {
//                viewChangesMenuItem.setEnabled(false);
//        screenCapture.stopCapture();
            System.exit(0);
        }
    }

    private void addTemplateMenuItem(JMenu templateMenu, ButtonGroup templatesMenuButtonGroup, String templateName, String selectedTemplate) {
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

    private void addTemplateAddNewMenuItem(JMenu templateMenu) {
        JMenuItem templateMenuItem = new JMenuItem();
        templateMenuItem.setText("<add new>");
        templateMenuItem.setName("<add new>");
        templateMenuItem.setActionCommand("<add new>");
        templateMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    String newDirectoryName = JOptionPane.showInputDialog(LinorgWindowManager.getSingleInstance().linorgFrame, "Enter the name for the new template", LinorgWindowManager.getSingleInstance().linorgFrame.getTitle(), JOptionPane.PLAIN_MESSAGE, null, null, null).toString();
                    // if the user cancels the directory string will be a empty string.
                    if (ArbilTemplateManager.getSingleInstance().getTemplateFile(newDirectoryName).exists()) {
                        LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("The template \"" + newDirectoryName + "\" already exists.", "Templates");
                    }
                    if (ArbilTemplateManager.getSingleInstance().createTemplate(newDirectoryName)) {
                    } else {
                        LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("The template \"" + newDirectoryName + "\" could not be created.", "Templates");
                    }
//                    LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("This action is not yet available.", "Templates");
                    //GuiHelper.linorgWindowManager.openUrlWindow(evt.getActionCommand() + templateList.get(evt.getActionCommand()).toString(), new File(templateList.get(evt.getActionCommand()).toString()).toURL());
//                    System.out.println("setting template: " + evt.getActionCommand());
//                    ArbilTemplateManager.getSingleInstance().setCurrentTemplate(evt.getActionCommand());
                } catch (Exception e) {
                    GuiHelper.linorgBugCatcher.logError(e);
                }
            }
        });
        templateMenu.add(templateMenuItem);
    }

    public void populateTemplatesMenu(JMenu templateMenu) {
        templateMenu.removeAll();
        ButtonGroup templatesMenuButtonGroup = new javax.swing.ButtonGroup();

        int templateCount = 0;
//        addTemplateMenuItem(templateMenu, templatesMenuButtonGroup, "", "Default", ArbilTemplateManager.getSingleInstance().getCurrentTemplate());
        for (String currentTemplateName : ArbilTemplateManager.getSingleInstance().getAvailableTemplates()) {
//            String templatePath = templatesDir.getPath() + File.separatorChar + currentTemplateName;
//            if (new File(templatePath).isDirectory()) {
            addTemplateMenuItem(templateMenu, templatesMenuButtonGroup, currentTemplateName, ArbilTemplateManager.getSingleInstance().getCurrentTemplateName());
            templateCount++;
//            }
        }
        if (templateCount == 0) {
            JMenuItem noneMenuItem = new JMenuItem("<none installed>");
            noneMenuItem.setEnabled(false);
            templateMenu.add(noneMenuItem);
        }
        addTemplateAddNewMenuItem(templateMenu);
    }
}
