package nl.mpi.arbil.ui.menu;

import java.awt.event.ActionEvent;
import nl.mpi.arbil.data.ArbilJournal;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.data.ArbilVocabularies;
import nl.mpi.arbil.util.ArbilVersionChecker;
import nl.mpi.arbil.util.ArbilBugCatcher;
import nl.mpi.arbil.data.ArbilTreeHelper;
import nl.mpi.arbil.ui.ImportExportDialog;
import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.JApplet;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.table.TableCellEditor;
import nl.mpi.arbil.ArbilVersion;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.data.metadatafile.MetadataReader;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilFieldsNode;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.ui.TemplateDialogue;
import nl.mpi.arbil.ui.ArbilHelp;
import nl.mpi.arbil.ui.ArbilTable;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.ui.GuiHelper;
import nl.mpi.arbil.ui.LanguageListDialogue;
import nl.mpi.arbil.ui.PreviewSplitPanel;
import nl.mpi.arbil.util.ArbilMimeHashQueue;

/**
 * ArbilMenuBar.java
 * Created on Jul 9, 2009, 12:01:02 PM
 * @author Peter.Withers@mpi.nl
 */
public class ArbilMenuBar extends JMenuBar {

    final static public JMenu windowMenu = new JMenu();
    private boolean macOsMenu = false;
    private JMenuItem saveFileMenuItem = new JMenuItem();
    private JMenuItem showChangedNodesMenuItem = new JMenuItem();
    private JCheckBoxMenuItem saveWindowsCheckBoxMenuItem = new JCheckBoxMenuItem();
    private JMenuItem shortCutKeysjMenuItem = new JMenuItem();
    private JMenuItem arbilForumMenuItem = new JMenuItem();
    private JMenuItem checkForUpdatesMenuItem = new JMenuItem();
    private JMenuItem viewErrorLogMenuItem = new JMenuItem();
    private JCheckBoxMenuItem showSelectionPreviewCheckBoxMenuItem = new JCheckBoxMenuItem();
    private JMenuItem templatesMenu = new JMenuItem();
    private JCheckBoxMenuItem trackTableSelectionCheckBoxMenuItem = new JCheckBoxMenuItem();
    private JCheckBoxMenuItem useLanguageIdInColumnNameCheckBoxMenuItem = new JCheckBoxMenuItem();
    private JMenuItem undoMenuItem = new JMenuItem();
//    private JMenuItem viewFavouritesMenuItem;
//    private JMenu setStorageDirectoryMenu;
    private JMenu setCacheDirectoryMenu = new JMenu();
    private JMenu viewMenu = new JMenu();
    private JMenuItem resetWindowsMenuItem = new JMenuItem();
    private JMenuItem closeWindowsMenuItem = new JMenuItem();
    private JMenu optionsMenu = new JMenu();
    private JMenuItem pasteMenuItem = new JMenuItem();
    private JMenuItem printHelpMenuItem = new JMenuItem();
    private JMenuItem redoMenuItem = new JMenuItem();
    public JCheckBoxMenuItem checkNewVersionAtStartCheckBoxMenuItem = new JCheckBoxMenuItem();
    private JMenuItem copyMenuItem = new JMenuItem();
    private JCheckBoxMenuItem copyNewResourcesCheckBoxMenuItem = new JCheckBoxMenuItem();
    private JCheckBoxMenuItem checkResourcePermissionsCheckBoxMenuItem = new JCheckBoxMenuItem();
    private JCheckBoxMenuItem schemaCheckLocalFiles = new JCheckBoxMenuItem();
    private JMenuItem editPreferredLanguagesMenuItem = new JMenuItem();
    private JMenuItem editFieldViewsMenuItem = new JMenuItem();
//    private JMenuItem editLocationsMenuItem;
    private JMenuItem updateAllLoadedVocabulariesMenuItem = new JMenuItem();
    private JMenu editMenu = new JMenu();
    private JMenu fileMenu = new JMenu();
    private JMenu helpMenu = new JMenu();
    private JMenuItem helpMenuItem = new JMenuItem();
    private JMenuItem importMenuItem = new JMenuItem();
    private PreviewSplitPanel previewSplitPanel;
    private JApplet containerApplet = null;
    private JMenuItem exitMenuItem = new JMenuItem() {

	@Override
	public boolean isVisible() {
	    return !isMacOsMenu();
	}
    };
    private JMenuItem aboutMenuItem = new JMenuItem() {

	@Override
	public boolean isVisible() {
	    return !isMacOsMenu();
	}
    };

    public ArbilMenuBar(PreviewSplitPanel previewSplitPanelLocal, JApplet containerAppletLocal) {
	containerApplet = containerAppletLocal;
	previewSplitPanel = previewSplitPanelLocal;

	initFileMenu();
	initEditMenu();
	initOptionsMenu();
	initWindowMenu();
	initHelpMenu();

	setUpHotKeys();
    }

    private void initFileMenu() {
	fileMenu.setText("File");
	fileMenu.addMenuListener(new MenuListener() {

	    public void menuCanceled(MenuEvent evt) {
	    }

	    public void menuDeselected(MenuEvent evt) {
	    }

	    public void menuSelected(MenuEvent evt) {
		saveFileMenuItem.setEnabled(ArbilDataNodeLoader.getSingleInstance().nodesNeedSave());
		showChangedNodesMenuItem.setEnabled(ArbilDataNodeLoader.getSingleInstance().nodesNeedSave());
	    }
	});
	saveFileMenuItem.setText("Save Changes");
	saveFileMenuItem.setEnabled(false);
	saveFileMenuItem.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    ArbilWindowManager.getSingleInstance().stopEditingInCurrentWindow();
		    ArbilDataNodeLoader.getSingleInstance().saveNodesNeedingSave(true);
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
                    ArrayList<ArbilFieldsNode> individualChangedNodes = new ArrayList<ArbilFieldsNode>();
                    for (ArbilDataNode currentTestable : ArbilDataNodeLoader.getSingleInstance().getNodesNeedSave()) {
                        if (currentTestable.hasChangedFields()) {
                            individualChangedNodes.add(currentTestable);
                        }
                        for (ArbilNode currentChildSaveable : currentTestable.getAllChildren()) {
                            if ((currentChildSaveable instanceof ArbilFieldsNode) && ((ArbilFieldsNode)currentChildSaveable).hasChangedFields()) {
                                individualChangedNodes.add(((ArbilFieldsNode)currentChildSaveable));
                            }
                        }
                    }
                    ArbilWindowManager.getSingleInstance().openFloatingTable(individualChangedNodes.toArray(new ArbilDataNode[]{}), "Modified Nodes");
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        fileMenu.add(showChangedNodesMenuItem);
        importMenuItem.setText("Import...");
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
	if (containerApplet == null) {
	    fileMenu.add(exitMenuItem);
	} else {
	    // create the applet shibboleth menu items
	    String loggedInUserName = containerApplet.getParameter("UserName");
	    // in php this could be: $_SERVER["REMOTE_USER"] or $_SERVER["eduPersonPrincipalName"] or $_SERVER["HTTP_EDUPERSONPRINCIPALNAME"]
	    if (loggedInUserName == null) {
		loggedInUserName = "unknown user";
	    }
	    JMenuItem logoutButton = new JMenuItem("Log Out (" + loggedInUserName + ")");
	    logoutButton.addActionListener(new java.awt.event.ActionListener() {

		public void actionPerformed(java.awt.event.ActionEvent evt) {
		    String logoutUrl = containerApplet.getParameter("LogoutUrl");
		    try {
			//			String userName = containerApplet.getParameter("UserName");
			if (containerApplet != null) {
			    //LinorgWindowManager.getSingleInstance().openUrlWindowOnce("Log out", new URL(logoutUrl));
			    containerApplet.getAppletContext().showDocument(new URL(logoutUrl));
			}
		    } catch (MalformedURLException ex) {
			ArbilWindowManager.getSingleInstance().addMessageDialogToQueue("Invalid logout url:\n" + logoutUrl, "Logout Error");
			GuiHelper.linorgBugCatcher.logError(ex);
		    }
		}
	    });
	    fileMenu.add(logoutButton);
	}
	this.add(fileMenu);
    }

    private void initEditMenu() {
	editMenu.setText("Edit");
	editMenu.addMenuListener(new javax.swing.event.MenuListener() {

	    public void menuCanceled(javax.swing.event.MenuEvent evt) {
	    }

	    public void menuDeselected(javax.swing.event.MenuEvent evt) {
	    }

	    public void menuSelected(javax.swing.event.MenuEvent evt) {
		undoMenuItem.setEnabled(ArbilJournal.getSingleInstance().canUndo());
		redoMenuItem.setEnabled(ArbilJournal.getSingleInstance().canRedo());
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
		    ArbilJournal.getSingleInstance().undoFromFieldChangeHistory();
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
		    ArbilJournal.getSingleInstance().redoFromFieldChangeHistory();
		} catch (Exception ex) {
		    GuiHelper.linorgBugCatcher.logError(ex);
		}
	    }
	});
	editMenu.add(redoMenuItem);
	this.add(editMenu);
    }

    private void initOptionsMenu() {

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

	templatesMenu.setText("Templates & Profiles...");
	templatesMenu.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    TemplateDialogue.showTemplatesDialogue();
		} catch (Exception ex) {
		    GuiHelper.linorgBugCatcher.logError(ex);
		}
	    }
	});
	optionsMenu.add(templatesMenu);
//        .addMenuListener(new javax.swing.event.MenuListener() {
//
//            public void menuCanceled(javax.swing.event.MenuEvent evt) {
//            }
//
//            public void menuDeselected(javax.swing.event.MenuEvent evt) {
//            }
//
//            public void menuSelected(javax.swing.event.MenuEvent evt) {
//                populateTemplatesMenu(templatesMenu);
//            }
//        });

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

	setCacheDirectoryMenu.setText("Local Corpus Storage Directory");
	setCacheDirectoryMenu.addMenuListener(new javax.swing.event.MenuListener() {

	    public void menuCanceled(javax.swing.event.MenuEvent evt) {
	    }

	    public void menuDeselected(javax.swing.event.MenuEvent evt) {
	    }

	    public void menuSelected(javax.swing.event.MenuEvent evt) {
		setCacheDirectoryMenu.removeAll();
		JMenuItem cacheDirectoryMenuItem = new JMenuItem();
		cacheDirectoryMenuItem.setText(ArbilSessionStorage.getSingleInstance().getCacheDirectory().getAbsolutePath());
		cacheDirectoryMenuItem.setEnabled(false);
		JMenuItem changeCacheDirectoryMenuItem = new JMenuItem();
		changeCacheDirectoryMenuItem.setText("Move Local Corpus Storage Directory...");
		changeCacheDirectoryMenuItem.addActionListener(new java.awt.event.ActionListener() {

		    public void actionPerformed(java.awt.event.ActionEvent evt) {
			try {
			    ArbilWindowManager.getSingleInstance().offerUserToSaveChanges();
			    File[] selectedFiles = ArbilWindowManager.getSingleInstance().showFileSelectBox("Move Local Corpus Storage Directory", true, false, false);
			    if (selectedFiles != null && selectedFiles.length > 0) {
				//fileChooser.setCurrentDirectory(LinorgSessionStorage.getSingleInstance().getCacheDirectory());
				ArbilSessionStorage.getSingleInstance().changeCacheDirectory(selectedFiles[0], true);
			    }
			} catch (Exception ex) {
			    GuiHelper.linorgBugCatcher.logError(ex);
			}
		    }
		});
		setCacheDirectoryMenu.add(cacheDirectoryMenuItem);
		setCacheDirectoryMenu.add(changeCacheDirectoryMenuItem);
	    }
	});
//        setStorageDirectoryMenu.setText("Configuration Files Directory");
//        setStorageDirectoryMenu.addMenuListener(new javax.swing.event.MenuListener() {
//
//            public void menuCanceled(javax.swing.event.MenuEvent evt) {
//            }
//
//            public void menuDeselected(javax.swing.event.MenuEvent evt) {
//            }
//
//            public void menuSelected(javax.swing.event.MenuEvent evt) {
//                populateStorageLocationMenu(setStorageDirectoryMenu);
//
//            }
//        });
//        JMenu localStorageDirectoriesMenu = new JMenu("Local Storage Directories");
//        localStorageDirectoriesMenu.add(setStorageDirectoryMenu);
//        localStorageDirectoriesMenu.add(setCacheDirectoryMenu);
//        optionsMenu.add(localStorageDirectoriesMenu);
	optionsMenu.add(setCacheDirectoryMenu);

	editPreferredLanguagesMenuItem.setText("Edit Language List...");
	editPreferredLanguagesMenuItem.setEnabled(true);
	editPreferredLanguagesMenuItem.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		LanguageListDialogue.showLanguageDialogue();
	    }
	});
	optionsMenu.add(editPreferredLanguagesMenuItem);

	updateAllLoadedVocabulariesMenuItem.setText("Re-download Current Vocabularies");
	updateAllLoadedVocabulariesMenuItem.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		ArbilVocabularies.getSingleInstance().redownloadCurrentlyLoadedVocabularies();
	    }
	});
	optionsMenu.add(updateAllLoadedVocabulariesMenuItem);

	saveWindowsCheckBoxMenuItem.setSelected(ArbilSessionStorage.getSingleInstance().loadBoolean("saveWindows", true));
	saveWindowsCheckBoxMenuItem.setText("Save Windows on Exit");
	optionsMenu.add(saveWindowsCheckBoxMenuItem);

	showSelectionPreviewCheckBoxMenuItem.setSelected(ArbilSessionStorage.getSingleInstance().loadBoolean("showSelectionPreview", true));
	previewSplitPanel.setPreviewPanel(showSelectionPreviewCheckBoxMenuItem.getState());
	showSelectionPreviewCheckBoxMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
//        showSelectionPreviewCheckBoxMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.META_MASK));
	showSelectionPreviewCheckBoxMenuItem.setText("Show Selection Preview");
	showSelectionPreviewCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    previewSplitPanel.setPreviewPanel(showSelectionPreviewCheckBoxMenuItem.getState());
		    ArbilSessionStorage.getSingleInstance().saveBoolean("showSelectionPreview", showSelectionPreviewCheckBoxMenuItem.isSelected());
		} catch (Exception ex) {
		    GuiHelper.linorgBugCatcher.logError(ex);
		}
	    }
	});
	optionsMenu.add(showSelectionPreviewCheckBoxMenuItem);

	checkNewVersionAtStartCheckBoxMenuItem.setSelected(ArbilSessionStorage.getSingleInstance().loadBoolean("checkNewVersionAtStart", true));
	checkNewVersionAtStartCheckBoxMenuItem.setText("Check for new version on start");
	optionsMenu.add(checkNewVersionAtStartCheckBoxMenuItem);

	copyNewResourcesCheckBoxMenuItem.setSelected(MetadataReader.getSingleInstance().copyNewResourcesToCache);
	copyNewResourcesCheckBoxMenuItem.setText("Copy new resources into cache");
	copyNewResourcesCheckBoxMenuItem.setToolTipText("When adding a new resource to a session this options will copy the file into the local cache rather than linking to its current location. This option can make a considerable difference to disk use if you are handling large files.");
	copyNewResourcesCheckBoxMenuItem.addItemListener(new java.awt.event.ItemListener() {

	    public void itemStateChanged(java.awt.event.ItemEvent evt) {
		MetadataReader.getSingleInstance().copyNewResourcesToCache = copyNewResourcesCheckBoxMenuItem.isSelected();
		ArbilSessionStorage.getSingleInstance().saveBoolean("copyNewResources", copyNewResourcesCheckBoxMenuItem.isSelected());
	    }
	});
	optionsMenu.add(copyNewResourcesCheckBoxMenuItem);

	checkResourcePermissionsCheckBoxMenuItem.setSelected(ArbilMimeHashQueue.getSingleInstance().isCheckResourcePermissions());
	checkResourcePermissionsCheckBoxMenuItem.setText("Check permissions for remote resources");
	checkResourcePermissionsCheckBoxMenuItem.setToolTipText("This option checks the server permissions for remote resources and shows icons accordingly.");
	checkResourcePermissionsCheckBoxMenuItem.addItemListener(new java.awt.event.ItemListener() {

	    public void itemStateChanged(java.awt.event.ItemEvent evt) {
		ArbilMimeHashQueue.getSingleInstance().setCheckResourcePermissions(checkResourcePermissionsCheckBoxMenuItem.isSelected());
		ArbilSessionStorage.getSingleInstance().saveBoolean("checkResourcePermissions", checkResourcePermissionsCheckBoxMenuItem.isSelected());
		ArbilWindowManager.getSingleInstance().addMessageDialogToQueue("The setting change will be effective when Arbil is restarted.", "Check permissions for remote resources");
	    }
	});
	optionsMenu.add(checkResourcePermissionsCheckBoxMenuItem);

	schemaCheckLocalFiles.setText("Always check local metadata files for XML conformance");
	schemaCheckLocalFiles.setSelected(ArbilDataNodeLoader.getSingleInstance().isSchemaCheckLocalFiles());
	schemaCheckLocalFiles.setToolTipText("This option checks all local metadata files for XML conformance every time they are loaded. If the metadata file does not validate against the schema it will be highlighted red in the tree.");
	schemaCheckLocalFiles.addItemListener(new java.awt.event.ItemListener() {

	    public void itemStateChanged(java.awt.event.ItemEvent evt) {
		ArbilDataNodeLoader.getSingleInstance().setSchemaCheckLocalFiles(schemaCheckLocalFiles.isSelected());
		ArbilSessionStorage.getSingleInstance().saveBoolean("schemaCheckLocalFiles", schemaCheckLocalFiles.isSelected());
	    }
	});
	optionsMenu.add(schemaCheckLocalFiles);

	trackTableSelectionCheckBoxMenuItem.setSelected(ArbilSessionStorage.getSingleInstance().isTrackTableSelection());
	trackTableSelectionCheckBoxMenuItem.setText("Track Table Selection in Tree");
	trackTableSelectionCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    ArbilSessionStorage.getSingleInstance().setTrackTableSelection(trackTableSelectionCheckBoxMenuItem.getState());
		    ArbilSessionStorage.getSingleInstance().saveBoolean("trackTableSelection", trackTableSelectionCheckBoxMenuItem.isSelected());
		} catch (Exception ex) {
		    GuiHelper.linorgBugCatcher.logError(ex);
		}
	    }
	});
	trackTableSelectionCheckBoxMenuItem.setEnabled(false);
	optionsMenu.add(trackTableSelectionCheckBoxMenuItem);

	useLanguageIdInColumnNameCheckBoxMenuItem.setSelected(ArbilSessionStorage.getSingleInstance().isUseLanguageIdInColumnName());
	useLanguageIdInColumnNameCheckBoxMenuItem.setText("Show Language in Column Name");
	useLanguageIdInColumnNameCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    ArbilWindowManager.getSingleInstance().offerUserToSaveChanges();
		    ArbilSessionStorage.getSingleInstance().setUseLanguageIdInColumnName(useLanguageIdInColumnNameCheckBoxMenuItem.getState());
		    ArbilSessionStorage.getSingleInstance().saveBoolean("useLanguageIdInColumnName", useLanguageIdInColumnNameCheckBoxMenuItem.isSelected());
		    ArbilDataNodeLoader.getSingleInstance().requestReloadAllNodes();
		} catch (Exception ex) {
		    useLanguageIdInColumnNameCheckBoxMenuItem.setSelected(ArbilSessionStorage.getSingleInstance().isUseLanguageIdInColumnName());
		}
	    }
	});
	optionsMenu.add(useLanguageIdInColumnNameCheckBoxMenuItem);

	this.add(optionsMenu);

	optionsMenu.add(new JSeparator());

	viewMenu.setText("Column View for new tables");
	viewMenu.addMenuListener(new MenuListener() {

	    public void menuCanceled(MenuEvent evt) {
	    }

	    public void menuDeselected(MenuEvent evt) {
	    }

	    public void menuSelected(MenuEvent evt) {
		viewMenuMenuSelected(evt);
	    }
	});
	optionsMenu.add(viewMenu);

	editFieldViewsMenuItem.setText("Edit Column Views");
	editFieldViewsMenuItem.setEnabled(false);
//        editFieldViewsMenuItem.addActionListener(new java.awt.event.ActionListener() {
//
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                editFieldViewsMenuItemActionPerformed(evt);
//            }
//        });
	optionsMenu.add(editFieldViewsMenuItem);
    }

    private void initWindowMenu() {
	windowMenu.setText("Window");

	resetWindowsMenuItem.setText("Reset windows");
	resetWindowsMenuItem.addActionListener(new ActionListener() {

	    public void actionPerformed(ActionEvent e) {
		if (ArbilWindowManager.getSingleInstance().showConfirmDialogBox("Reset all windows to default size and location?", "Reset windows")) {
		    ArbilWindowManager.getSingleInstance().resetWindows();
		}
	    }
	});

	closeWindowsMenuItem.setText("Close all windows");
	closeWindowsMenuItem.addActionListener(new ActionListener() {

	    public void actionPerformed(ActionEvent e) {
		if (ArbilWindowManager.getSingleInstance().showConfirmDialogBox("Close all windows?", "Close windows")) {
		    ArbilWindowManager.getSingleInstance().closeAllWindows();
		}
	    }
	});

	windowMenu.add(closeWindowsMenuItem);
	windowMenu.add(resetWindowsMenuItem);
	windowMenu.add(new JSeparator());

	this.add(windowMenu);
    }

    private void initHelpMenu() {
	helpMenu.setText("Help");
	helpMenu.addMenuListener(new MenuListener() {

	    public void menuCanceled(MenuEvent evt) {
	    }

	    public void menuDeselected(MenuEvent evt) {
	    }

	    public void menuSelected(MenuEvent evt) {
		viewErrorLogMenuItem.setEnabled(new ArbilBugCatcher().getLogFile().exists());
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
		    GuiHelper.getSingleInstance().openFileInExternalApplication(new ArbilBugCatcher().getLogFile().toURI());
		} catch (Exception ex) {
		    GuiHelper.linorgBugCatcher.logError(ex);
		}
	    }
	});
	helpMenu.add(viewErrorLogMenuItem);
	checkForUpdatesMenuItem.setText("Check for Updates");
	checkForUpdatesMenuItem.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    if (!new ArbilVersionChecker().forceUpdateCheck()) {
			ArbilVersion linorgVersion = new ArbilVersion();
			String versionString = linorgVersion.currentMajor + "." + linorgVersion.currentMinor + "." + linorgVersion.currentRevision;
			ArbilWindowManager.getSingleInstance().addMessageDialogToQueue("No updates found, current version is " + versionString, "Check for Updates");
		    }
		} catch (Exception ex) {
		    GuiHelper.linorgBugCatcher.logError(ex);
		}
	    }
	});
	helpMenu.add(checkForUpdatesMenuItem);
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
	printHelpMenuItem.setVisible(false);
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
			if (compFocusOwner instanceof ArbilTable) {
			    TableCellEditor currentEditor = ((ArbilTable) compFocusOwner).getCellEditor();
			    if (currentEditor != null) {
				currentEditor.stopCellEditing();
				break;
			    }
			}
			compFocusOwner = compFocusOwner.getParent();
		    }
		    if (((KeyEvent) event).getKeyCode() == KeyEvent.VK_S) {
			ArbilWindowManager.getSingleInstance().stopEditingInCurrentWindow();
			ArbilDataNodeLoader.getSingleInstance().saveNodesNeedingSave(true);
		    }
		    if (((KeyEvent) event).getKeyCode() == java.awt.event.KeyEvent.VK_Z) {
			if (((KeyEvent) event).isShiftDown()) {
			    ArbilJournal.getSingleInstance().redoFromFieldChangeHistory();
			} else {
			    ArbilJournal.getSingleInstance().undoFromFieldChangeHistory();
			}
		    }
		    if (((KeyEvent) event).getKeyCode() == java.awt.event.KeyEvent.VK_Y) {
			ArbilJournal.getSingleInstance().redoFromFieldChangeHistory();
		    }
		}
	    }
	}, AWTEvent.KEY_EVENT_MASK);
    }

    private void viewMenuMenuSelected(MenuEvent evt) {
	GuiHelper.getSingleInstance().initViewMenu(viewMenu);
    }

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
	performCleanExit();
    }

    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
	ArbilWindowManager.getSingleInstance().openAboutPage();
    }

    private void shortCutKeysjMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
	ArbilHelp helpComponent = ArbilHelp.getSingleInstance();
	if (null == ArbilWindowManager.getSingleInstance().focusWindow(ArbilHelp.helpWindowTitle)) {
	    ArbilWindowManager.getSingleInstance().createWindow(ArbilHelp.helpWindowTitle, helpComponent);
	}
	helpComponent.setCurrentPage(ArbilHelp.SHOTCUT_KEYS_PAGE);
    }

    private void helpMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
	if (null == ArbilWindowManager.getSingleInstance().focusWindow(ArbilHelp.helpWindowTitle)) {
	    // forcus existing or create a new help window
	    ArbilWindowManager.getSingleInstance().createWindow(ArbilHelp.helpWindowTitle, ArbilHelp.getSingleInstance());
	}
    }

    private void importMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
	try {
	    ImportExportDialog importExportDialog = new ImportExportDialog(ArbilTreeHelper.getSingleInstance().getArbilTreePanel().remoteCorpusTree);
	    importExportDialog.importArbilBranch();
	} catch (Exception e) {
	    System.out.println(e.getMessage());
	}
    }

    private void printHelpMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
	if (null == ArbilWindowManager.getSingleInstance().focusWindow(ArbilHelp.helpWindowTitle)) {
	    // forcus existing or create a new help window
	    ArbilWindowManager.getSingleInstance().createWindow(ArbilHelp.helpWindowTitle, ArbilHelp.getSingleInstance());
	}
	ArbilHelp.getSingleInstance().printAsOneFile();
    }

//    private void populateStorageLocationMenu(JMenu storageMenu) {
//        storageMenu.removeAll();
//        ButtonGroup storageMenuButtonGroup = new ButtonGroup();
//        String[] storageLocaations = LinorgSessionStorage.getSingleInstance().getLocationOptions();
//        Arrays.sort(storageLocaations);
//        ArrayList<String> addedPaths = new ArrayList<String>();
//        for (String currentTemplateName : storageLocaations) {
//            if (!currentTemplateName.startsWith("null") && !addedPaths.contains(currentTemplateName)) {
//                addedPaths.add(currentTemplateName);
//                JRadioButtonMenuItem templateMenuItem = new JRadioButtonMenuItem();
//                templateMenuItem.setText(currentTemplateName);
//                templateMenuItem.setName(currentTemplateName);
//                templateMenuItem.setActionCommand(currentTemplateName);
////                templateMenuItem.setEnabled(false);
//                templateMenuItem.addActionListener(new java.awt.event.ActionListener() {
//
//                    public void actionPerformed(java.awt.event.ActionEvent evt) {
//                        try {
//                            saveApplicationState();
//                            LinorgSessionStorage.getSingleInstance().changeStorageDirectory(evt.getActionCommand());
//                            // LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("This action is not yet available.", "Storage Directory");
//                        } catch (Exception e) {
//                            GuiHelper.linorgBugCatcher.logError(e);
//                        }
//                    }
//                });
//                templateMenuItem.setSelected(LinorgSessionStorage.getSingleInstance().storageDirectory.equals(new File(currentTemplateName)));
//                storageMenuButtonGroup.add(templateMenuItem);
//                storageMenu.add(templateMenuItem);
//            }
//        }
//        // TODO: add other cache directory and update changeStorageDirectory to cope with the additional variables
//    }
    private boolean saveApplicationState() {
	if (ArbilDataNodeLoader.getSingleInstance().nodesNeedSave()) {
	    // TODO: why is LinorgWindowManager.getSingleInstance().offerUserToSaveChanges(); not used?
	    switch (JOptionPane.showConfirmDialog(this, "Save changes before exiting?", "Arbil", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)) {
		case JOptionPane.NO_OPTION:
		    break;
		case JOptionPane.YES_OPTION:
		    ArbilDataNodeLoader.getSingleInstance().saveNodesNeedingSave(false);
		    break;
		default:
		    return false;
	    }
	}
	GuiHelper.getSingleInstance().saveState(saveWindowsCheckBoxMenuItem.isSelected());
	ArbilSessionStorage.getSingleInstance().saveBoolean("saveWindows", saveWindowsCheckBoxMenuItem.isSelected());
	ArbilSessionStorage.getSingleInstance().saveBoolean("checkNewVersionAtStart", checkNewVersionAtStartCheckBoxMenuItem.isSelected());
	return true;
    }

    public boolean performCleanExit() { // TODO: this should be moved into a utility class
	if (saveApplicationState()) {
//                viewChangesMenuItem.setEnabled(false);
//        screenCapture.stopCapture();
	    System.exit(0);
	    return true;
	}
	return false;
    }
//    private void addTemplateMenuItem(JMenu templateMenu, ButtonGroup templatesMenuButtonGroup, String templateName, String selectedTemplate) {
//        JRadioButtonMenuItem templateMenuItem = new JRadioButtonMenuItem();
//        templateMenuItem.setText(templateName);
//        templateMenuItem.setName(templateName);
//        templateMenuItem.setActionCommand(templateName);
//        templateMenuItem.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                try {
//                    LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("This action is not yet available.", "Templates");
    //GuiHelper.linorgWindowManager.openUrlWindow(evt.getActionCommand() + templateList.get(evt.getActionCommand()).toString(), new File(templateList.get(evt.getActionCommand()).toString()).toURL());
//                    System.out.println("setting template: " + evt.getActionCommand());
//                    ArbilTemplateManager.getSingleInstance().setCurrentTemplate(evt.getActionCommand());
//                } catch (Exception e) {
//                    GuiHelper.linorgBugCatcher.logError(e);
//                }
//            }
//        });
//        templatesMenuButtonGroup.add(templateMenuItem);
//        templateMenu.add(templateMenuItem);
//        if (selectedTemplate.equals(templateName)) {
//            templateMenuItem.setSelected(true);
//        }
//    }
//    private void addTemplateAddNewMenuItem(JMenu templateMenu) {
//        JMenuItem templateMenuItem = new JMenuItem();
//        templateMenuItem.setText("<add new>");
//        templateMenuItem.setName("<add new>");
//        templateMenuItem.setActionCommand("<add new>");
//        templateMenuItem.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                try {
//                    String newDirectoryName = JOptionPane.showInputDialog(LinorgWindowManager.getSingleInstance().linorgFrame, "Enter the name for the new template", LinorgWindowManager.getSingleInstance().linorgFrame.getTitle(), JOptionPane.PLAIN_MESSAGE, null, null, null).toString();
//                    // if the user cancels the directory string will be a empty string.
//                    if (ArbilTemplateManager.getSingleInstance().getTemplateFile(newDirectoryName).exists()) {
//                        LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("The template \"" + newDirectoryName + "\" already exists.", "Templates");
//                    }
//                    File freshTemplateFile = ArbilTemplateManager.getSingleInstance().createTemplate(newDirectoryName);
//                    if (freshTemplateFile != null) {
//                        GuiHelper.getSingleInstance().openFileInExternalApplication(freshTemplateFile.toURI());
//                        GuiHelper.getSingleInstance().openFileInExternalApplication(freshTemplateFile.getParentFile().toURI());
//                    } else {
//                        LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("The template \"" + newDirectoryName + "\" could not be created.", "Templates");
//                    }
////                    LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("This action is not yet available.", "Templates");
//                    //GuiHelper.linorgWindowManager.openUrlWindow(evt.getActionCommand() + templateList.get(evt.getActionCommand()).toString(), new File(templateList.get(evt.getActionCommand()).toString()).toURL());
////                    System.out.println("setting template: " + evt.getActionCommand());
////                    ArbilTemplateManager.getSingleInstance().setCurrentTemplate(evt.getActionCommand());
//                } catch (Exception e) {
//                    GuiHelper.linorgBugCatcher.logError(e);
//                }
//            }
//        });
//        templateMenu.add(templateMenuItem);
//    }
//    public void populateTemplatesMenu(JMenu templateMenu) {
//        templateMenu.removeAll();
//        ButtonGroup templatesMenuButtonGroup = new javax.swing.ButtonGroup();
//        int templateCount = 0;
//        addTemplateMenuItem(templateMenu, templatesMenuButtonGroup, "", "Default", ArbilTemplateManager.getSingleInstance().getCurrentTemplate());
//        for (String currentTemplateName : ArbilTemplateManager.getSingleInstance().getAvailableTemplates()) {
//            String templatePath = templatesDir.getPath() + File.separatorChar + currentTemplateName;
//            if (new File(templatePath).isDirectory()) {
//            addTemplateMenuItem(templateMenu, templatesMenuButtonGroup, currentTemplateName, ArbilTemplateManager.getSingleInstance().getCurrentTemplateName());
//            templateCount++;
//            }
//        }
//        if (templateCount == 0) {
//            JMenuItem noneMenuItem = new JMenuItem("<none installed>");
//            noneMenuItem.setEnabled(false);
//            templateMenu.add(noneMenuItem);
//        }
//        addTemplateAddNewMenuItem(templateMenu);
//        CmdiProfileReader cmdiProfileReader = new CmdiProfileReader();
//        if (templateCount > 0 && cmdiProfileReader.cmdiProfileArray.size() > 0) {
//            templateMenu.add(new JSeparator());
//        }
//        for (CmdiProfileReader.CmdiProfile currentCmdiProfile : cmdiProfileReader.cmdiProfileArray) {
//            addTemplateMenuItem(templateMenu, templatesMenuButtonGroup, currentCmdiProfile.name, ArbilTemplateManager.getSingleInstance().getCurrentTemplateName());
//            templateCount++;
//        }
//    }

    /**
     * @return the macOsMenu
     */
    public boolean isMacOsMenu() {
	return macOsMenu;
    }

    /**
     * @param macOsMenu the macOsMenu to set
     */
    public void setMacOsMenu(boolean macOsMenu) {
	this.macOsMenu = macOsMenu;
    }
}
