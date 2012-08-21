package nl.mpi.arbil.ui.menu;

import java.awt.event.ActionEvent;
import nl.mpi.arbil.data.ArbilJournal;
import nl.mpi.arbil.data.ArbilVocabularies;
import nl.mpi.arbil.util.ApplicationVersionManager;
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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.swing.ButtonGroup;
import javax.swing.JApplet;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.table.TableCellEditor;
import nl.mpi.arbil.data.metadatafile.MetadataReader;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.DataNodeLoader;
import nl.mpi.arbil.ui.ArbilFieldViews;
import nl.mpi.arbil.ui.TemplateDialogue;
import nl.mpi.arbil.ui.ArbilHelp;
import nl.mpi.arbil.ui.ArbilTable;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.ui.LanguageListDialogue;
import nl.mpi.arbil.ui.PreviewSplitPanel;
import nl.mpi.arbil.ui.wizard.ArbilWizard;
import nl.mpi.arbil.ui.wizard.setup.ArbilSetupWizard;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ApplicationVersion;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.MimeHashQueue;
import nl.mpi.arbil.util.TreeHelper;
import nl.mpi.arbil.util.WindowManager;
import org.xml.sax.SAXException;

/**
 * ArbilMenuBar.java
 * Created on Jul 9, 2009, 12:01:02 PM
 *
 * @author Peter.Withers@mpi.nl
 */
public class ArbilMenuBar extends JMenuBar {

    private static SessionStorage sessionStorage;

    public static void setSessionStorage(SessionStorage sessionStorageInstance) {
	sessionStorage = sessionStorageInstance;
    }
    private static MimeHashQueue mimeHashQueue;

    public static void setMimeHashQueue(MimeHashQueue mimeHashQueueInstance) {
	mimeHashQueue = mimeHashQueueInstance;
    }
    private static ArbilTreeHelper treeHelper;

    public static void setTreeHelper(TreeHelper treeHelperInstance) {
	if (treeHelperInstance instanceof ArbilTreeHelper) {
	    treeHelper = (ArbilTreeHelper) treeHelperInstance;
	} else {
	    throw new RuntimeException("ArbilMenuBar requires ArbilTreeHelper. Found " + treeHelperInstance.getClass());
	}
    }
    private static ArbilWindowManager windowManager;

    public static void setWindowManager(WindowManager windowManagerInstance) {
	if (windowManagerInstance instanceof ArbilWindowManager) {
	    windowManager = (ArbilWindowManager) windowManagerInstance;
	} else {
	    throw new RuntimeException("ArbilMenuBar requires ArbilWindowManager. Found " + windowManagerInstance.getClass());
	}
    }
    private static MessageDialogHandler dialogHandler;

    public static void setMessageDialogHandler(MessageDialogHandler dialogHandlerInstance) {
	dialogHandler = dialogHandlerInstance;
    }
    private static DataNodeLoader dataNodeLoader;

    public static void setDataNodeLoader(DataNodeLoader dataNodeLoaderInstance) {
	dataNodeLoader = dataNodeLoaderInstance;
    }
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
    private JMenuItem setupWizardMenuItem = new JMenuItem();
    private JMenuItem importMenuItem = new JMenuItem();
    private JCheckBoxMenuItem showStatusBarMenuItem = new JCheckBoxMenuItem();
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
    private static ApplicationVersionManager versionManager;

    public static void setVersionManager(ApplicationVersionManager versionManagerInstance) {
	versionManager = versionManagerInstance;
    }

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

    private ArbilHelp getArbilHelp() {
	try {
	    return ArbilHelp.getArbilHelpInstance();
	} catch (IOException ioEx) {
	    dialogHandler.addMessageDialogToQueue("I/O error while trying to read help system! See error log for details.", "Error");
	    BugCatcherManager.getBugCatcher().logError(ioEx);
	} catch (SAXException saxEx) {
	    dialogHandler.addMessageDialogToQueue("Parser error while trying to read help system! See error log for details.", "Error");
	    BugCatcherManager.getBugCatcher().logError(saxEx);
	}
	return null;
    }

    private void initFileMenu() {
	fileMenu.setText("File");
	fileMenu.addMenuListener(new MenuListener() {
	    public void menuCanceled(MenuEvent evt) {
	    }

	    public void menuDeselected(MenuEvent evt) {
	    }

	    public void menuSelected(MenuEvent evt) {
		saveFileMenuItem.setEnabled(dataNodeLoader.nodesNeedSave());
		showChangedNodesMenuItem.setEnabled(dataNodeLoader.nodesNeedSave());
	    }
	});
	saveFileMenuItem.setText("Save Changes");
	saveFileMenuItem.setEnabled(false);
	saveFileMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    windowManager.stopEditingInCurrentWindow();
		    dataNodeLoader.saveNodesNeedingSave(true);
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	fileMenu.add(saveFileMenuItem);
	showChangedNodesMenuItem.setText("Show Modified Nodes");
	showChangedNodesMenuItem.setEnabled(false);
	showChangedNodesMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    ArrayList<ArbilDataNode> individualChangedNodes = new ArrayList<ArbilDataNode>();
		    for (ArbilDataNode currentTestable : dataNodeLoader.getNodesNeedSave()) {
			if (currentTestable.hasChangedFields()) {
			    individualChangedNodes.add(currentTestable);
			}
			for (ArbilDataNode currentChildSaveable : currentTestable.getAllChildren()) {
			    if (currentChildSaveable.hasChangedFields()) {
				individualChangedNodes.add(currentChildSaveable);
			    }
			}
		    }
		    windowManager.openFloatingTable(individualChangedNodes.toArray(new ArbilDataNode[]{}), "Modified Nodes");
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
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
		    BugCatcherManager.getBugCatcher().logError(ex);
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
		    BugCatcherManager.getBugCatcher().logError(ex);
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
			    //LinorgWindowManager.getArbilHelpInstance().openUrlWindowOnce("Log out", new URL(logoutUrl));
			    containerApplet.getAppletContext().showDocument(new URL(logoutUrl));
			}
		    } catch (MalformedURLException ex) {
			dialogHandler.addMessageDialogToQueue("Invalid logout url:\n" + logoutUrl, "Logout Error");
			BugCatcherManager.getBugCatcher().logError(ex);
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
	//                        LinorgWindowManager.getArbilHelpInstance().addMessageDialogToQueue("No node selected", "Copy");
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
		    BugCatcherManager.getBugCatcher().logError(ex);
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
		    BugCatcherManager.getBugCatcher().logError(ex);
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
//                    BugCatcherManager.getBugCatcher().logError(ex);
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
		    BugCatcherManager.getBugCatcher().logError(ex);
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
//                    BugCatcherManager.getBugCatcher().logError(ex);
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
		cacheDirectoryMenuItem.setText(sessionStorage.getCacheDirectory().getAbsolutePath());
		cacheDirectoryMenuItem.setEnabled(false);
		JMenuItem changeCacheDirectoryMenuItem = new JMenuItem();
		changeCacheDirectoryMenuItem.setText("Move Local Corpus Storage Directory...");
		changeCacheDirectoryMenuItem.addActionListener(new java.awt.event.ActionListener() {
		    public void actionPerformed(java.awt.event.ActionEvent evt) {
			try {
			    dialogHandler.offerUserToSaveChanges();
			    File[] selectedFiles = dialogHandler.showDirectorySelectBox("Move Local Corpus Storage Directory", false);
			    if (selectedFiles != null && selectedFiles.length > 0) {
				//fileChooser.setCurrentDirectory(LinorgSessionStorage.getArbilHelpInstance().getCacheDirectory());
				sessionStorage.changeCacheDirectory(selectedFiles[0], true);
			    }
			} catch (Exception ex) {
			    BugCatcherManager.getBugCatcher().logError(ex);
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

	editPreferredLanguagesMenuItem.setText("Edit IMDI Language List...");
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

	saveWindowsCheckBoxMenuItem.setSelected(sessionStorage.loadBoolean("saveWindows", true));
	saveWindowsCheckBoxMenuItem.setText("Save Windows on Exit");
	optionsMenu.add(saveWindowsCheckBoxMenuItem);

	showSelectionPreviewCheckBoxMenuItem.setSelected(sessionStorage.loadBoolean("showSelectionPreview", true));
	previewSplitPanel.setPreviewPanel(showSelectionPreviewCheckBoxMenuItem.getState());
	showSelectionPreviewCheckBoxMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
//        showSelectionPreviewCheckBoxMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.META_MASK));
	showSelectionPreviewCheckBoxMenuItem.setText("Show Selection Preview");
	showSelectionPreviewCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    previewSplitPanel.setPreviewPanel(showSelectionPreviewCheckBoxMenuItem.getState());
		    sessionStorage.saveBoolean("showSelectionPreview", showSelectionPreviewCheckBoxMenuItem.isSelected());
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	optionsMenu.add(showSelectionPreviewCheckBoxMenuItem);

	showStatusBarMenuItem.setText("Show Status Bar");
	showStatusBarMenuItem.setState(sessionStorage.loadBoolean("showStatusBar", false));
	showStatusBarMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_B, java.awt.event.InputEvent.CTRL_MASK));
	showStatusBarMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		windowManager.setStatusBarVisible(showStatusBarMenuItem.getState());
		sessionStorage.saveBoolean("showStatusBar", showStatusBarMenuItem.getState());
	    }
	});
	optionsMenu.add(showStatusBarMenuItem);

	checkNewVersionAtStartCheckBoxMenuItem.setSelected(sessionStorage.loadBoolean("checkNewVersionAtStart", true));
	checkNewVersionAtStartCheckBoxMenuItem.setText("Check for new version on start");
	optionsMenu.add(checkNewVersionAtStartCheckBoxMenuItem);

	copyNewResourcesCheckBoxMenuItem.setSelected(MetadataReader.getSingleInstance().copyNewResourcesToCache);
	copyNewResourcesCheckBoxMenuItem.setText("Copy new resources into cache");
	copyNewResourcesCheckBoxMenuItem.setToolTipText("When adding a new resource to a session this options will copy the file into the local cache rather than linking to its current location. This option can make a considerable difference to disk use if you are handling large files.");
	copyNewResourcesCheckBoxMenuItem.addItemListener(new java.awt.event.ItemListener() {
	    public void itemStateChanged(java.awt.event.ItemEvent evt) {
		MetadataReader.getSingleInstance().copyNewResourcesToCache = copyNewResourcesCheckBoxMenuItem.isSelected();
		sessionStorage.saveBoolean("copyNewResources", copyNewResourcesCheckBoxMenuItem.isSelected());
	    }
	});
	optionsMenu.add(copyNewResourcesCheckBoxMenuItem);

	checkResourcePermissionsCheckBoxMenuItem.setSelected(mimeHashQueue.isCheckResourcePermissions());
	checkResourcePermissionsCheckBoxMenuItem.setText("Check permissions for remote resources");
	checkResourcePermissionsCheckBoxMenuItem.setToolTipText("This option checks the server permissions for remote resources and shows icons accordingly.");
	checkResourcePermissionsCheckBoxMenuItem.addItemListener(new java.awt.event.ItemListener() {
	    public void itemStateChanged(java.awt.event.ItemEvent evt) {
		mimeHashQueue.setCheckResourcePermissions(checkResourcePermissionsCheckBoxMenuItem.isSelected());
		sessionStorage.saveBoolean("checkResourcePermissions", checkResourcePermissionsCheckBoxMenuItem.isSelected());
		dialogHandler.addMessageDialogToQueue("The setting change will be effective when Arbil is restarted.", "Check permissions for remote resources");
	    }
	});
	optionsMenu.add(checkResourcePermissionsCheckBoxMenuItem);

	schemaCheckLocalFiles.setText("Always check local metadata files for XML conformance");
	schemaCheckLocalFiles.setSelected(dataNodeLoader.isSchemaCheckLocalFiles());
	schemaCheckLocalFiles.setToolTipText("This option checks all local metadata files for XML conformance every time they are loaded. If the metadata file does not validate against the schema it will be highlighted red in the tree.");
	schemaCheckLocalFiles.addItemListener(new java.awt.event.ItemListener() {
	    public void itemStateChanged(java.awt.event.ItemEvent evt) {
		dataNodeLoader.setSchemaCheckLocalFiles(schemaCheckLocalFiles.isSelected());
		sessionStorage.saveBoolean("schemaCheckLocalFiles", schemaCheckLocalFiles.isSelected());
	    }
	});
	optionsMenu.add(schemaCheckLocalFiles);

	trackTableSelectionCheckBoxMenuItem.setSelected(sessionStorage.loadBoolean("trackTableSelection", false));
	trackTableSelectionCheckBoxMenuItem.setText("Track Table Selection in Tree");
	trackTableSelectionCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    sessionStorage.saveBoolean("useLanguageIdInColumnName", trackTableSelectionCheckBoxMenuItem.getState());
		    sessionStorage.saveBoolean("trackTableSelection", trackTableSelectionCheckBoxMenuItem.isSelected());
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	trackTableSelectionCheckBoxMenuItem.setEnabled(false);
	//TODO: Implement trackTableSelectionCheckBoxMenuItem and re-enable
//        optionsMenu.add(trackTableSelectionCheckBoxMenuItem);

	useLanguageIdInColumnNameCheckBoxMenuItem.setSelected(sessionStorage.loadBoolean("useLanguageIdInColumnName", false));
	useLanguageIdInColumnNameCheckBoxMenuItem.setText("Show Language in Column Name");
	useLanguageIdInColumnNameCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    dialogHandler.offerUserToSaveChanges();
		    sessionStorage.saveBoolean("useLanguageIdInColumnName", useLanguageIdInColumnNameCheckBoxMenuItem.isSelected());
		    dataNodeLoader.requestReloadAllNodes();
		} catch (Exception ex) {
		    useLanguageIdInColumnNameCheckBoxMenuItem.setSelected(sessionStorage.loadBoolean("useLanguageIdInColumnName", false));
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

	//TODO: Implement editFieldViewsMenuItem and re-enable
//        optionsMenu.add(editFieldViewsMenuItem);
    }

    private void initWindowMenu() {
	windowMenu.setText("Window");

	resetWindowsMenuItem.setText("Reset window locations");
	resetWindowsMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		if (dialogHandler.showConfirmDialogBox("Reset all windows to default size and location?", "Reset windows")) {
		    windowManager.resetWindows();
		}
	    }
	});

	closeWindowsMenuItem.setText("Close all windows");
	closeWindowsMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		if (dialogHandler.showConfirmDialogBox("Close all windows?", "Close windows")) {
		    windowManager.closeAllWindows();
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
		viewErrorLogMenuItem.setEnabled(ArbilBugCatcher.getLogFile(sessionStorage, versionManager.getApplicationVersion()).exists());
	    }
	});
	aboutMenuItem.setText("About");
	aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    aboutMenuItemActionPerformed(evt);
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
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
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	helpMenu.add(helpMenuItem);
	setupWizardMenuItem.setText("Run setup wizard");
	setupWizardMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		sessionStorage.saveString(SessionStorage.PARAM_WIZARD_RUN, "yes");
		ArbilWizard wizard = new ArbilSetupWizard(windowManager.getMainFrame());
		wizard.showModalDialog();
	    }
	});
	helpMenu.add(setupWizardMenuItem);
	arbilForumMenuItem.setText("Arbil Forum (Website)");
	arbilForumMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    windowManager.openFileInExternalApplication(new URI("http://www.lat-mpi.eu/tools/arbil/Arbil-forum/"));
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	helpMenu.add(arbilForumMenuItem);
	viewErrorLogMenuItem.setText("View Error Log");
	viewErrorLogMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    windowManager.openFileInExternalApplication(ArbilBugCatcher.getLogFile(sessionStorage, versionManager.getApplicationVersion()).toURI());
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	helpMenu.add(viewErrorLogMenuItem);
	checkForUpdatesMenuItem.setText("Check for Updates");
	checkForUpdatesMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    if (!versionManager.forceUpdateCheck()) {
			ApplicationVersion appVersion = versionManager.getApplicationVersion();
			String versionString = appVersion.currentMajor + "." + appVersion.currentMinor + "." + appVersion.currentRevision;
			dialogHandler.addMessageDialogToQueue("No updates found, current version is " + versionString, "Check for Updates");
		    }
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
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
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	helpMenu.add(shortCutKeysjMenuItem);
//	printHelpMenuItem.setText("Print Help File");
//	printHelpMenuItem.addActionListener(new java.awt.event.ActionListener() {
//
//	    public void actionPerformed(java.awt.event.ActionEvent evt) {
//		try {
//		    printHelpMenuItemActionPerformed(evt);
//		} catch (Exception ex) {
//		    BugCatcherManager.getBugCatcher().logError(ex);
//		}
//	    }
//	});
//	printHelpMenuItem.setVisible(false);
//	helpMenu.add(printHelpMenuItem);
	this.add(helpMenu);
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
			windowManager.stopEditingInCurrentWindow();
			dataNodeLoader.saveNodesNeedingSave(true);
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
	initViewMenu(viewMenu);
    }

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
	performCleanExit();
    }

    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
	windowManager.openAboutPage();
    }

    private void shortCutKeysjMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
	final ArbilHelp helpComponent = getArbilHelp();
	if (helpComponent != null) {
	    if (null == windowManager.focusWindow(ArbilHelp.helpWindowTitle)) {
		windowManager.createWindow(ArbilHelp.helpWindowTitle, helpComponent);
	    }
	    helpComponent.setCurrentPage(ArbilHelp.SHOTCUT_KEYS_PAGE);
	}
    }

    private void helpMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
	final ArbilHelp arbilHelp = getArbilHelp();
	if (arbilHelp != null) {
	    if (null == windowManager.focusWindow(ArbilHelp.helpWindowTitle)) {
		// forcus existing or create a new help window
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			JInternalFrame helpWindow = windowManager.createWindow(ArbilHelp.helpWindowTitle, getArbilHelp());
			helpWindow.setSize(800, 600);
		    }
		});
	    }
	}
    }

    private void importMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
	try {
	    ImportExportDialog importExportDialog = new ImportExportDialog(treeHelper.getArbilTreePanel().remoteCorpusTree);
	    importExportDialog.importArbilBranch();
	} catch (Exception e) {
	    BugCatcherManager.getBugCatcher().logError(e);
	    System.out.println(e.getMessage());
	}
    }
//
//    private void printHelpMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
//	final ArbilHelp arbilHelp = getArbilHelpInstance();
//	if (arbilHelp != null) {
//	    if (null == windowManager.focusWindow(ArbilHelp.helpWindowTitle)) {
//		// forcus existing or create a new help window
//		windowManager.createWindow(ArbilHelp.helpWindowTitle, arbilHelp);
//	    }
//	    arbilHelp.printAsOneFile();
//	}
//    }

//    private void populateStorageLocationMenu(JMenu storageMenu) {
//        storageMenu.removeAll();
//        ButtonGroup storageMenuButtonGroup = new ButtonGroup();
//        String[] storageLocaations = LinorgSessionStorage.getArbilHelpInstance().getLocationOptions();
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
//                            LinorgSessionStorage.getArbilHelpInstance().changeStorageDirectory(evt.getActionCommand());
//                            // LinorgWindowManager.getArbilHelpInstance().addMessageDialogToQueue("This action is not yet available.", "Storage Directory");
//                        } catch (Exception e) {
//                            BugCatcherManager.getBugCatcher().logError(e);
//                        }
//                    }
//                });
//                templateMenuItem.setSelected(LinorgSessionStorage.getArbilHelpInstance().storageDirectory.equals(new File(currentTemplateName)));
//                storageMenuButtonGroup.add(templateMenuItem);
//                storageMenu.add(templateMenuItem);
//            }
//        }
//        // TODO: add other cache directory and update changeStorageDirectory to cope with the additional variables
//    }
    private boolean saveApplicationState() {
	if (dataNodeLoader.nodesNeedSave()) {
	    // TODO: why is LinorgWindowManager.getArbilHelpInstance().offerUserToSaveChanges(); not used?
	    switch (JOptionPane.showConfirmDialog(this, "Save changes before exiting?", "Arbil", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)) {
		case JOptionPane.NO_OPTION:
		    break;
		case JOptionPane.YES_OPTION:
		    dataNodeLoader.saveNodesNeedingSave(false);
		    break;
		default:
		    return false;
	    }
	}
	mimeHashQueue.terminateQueue();

	saveState(saveWindowsCheckBoxMenuItem.isSelected());
	sessionStorage.saveBoolean("saveWindows", saveWindowsCheckBoxMenuItem.isSelected());
	sessionStorage.saveBoolean("checkNewVersionAtStart", checkNewVersionAtStartCheckBoxMenuItem.isSelected());
	return true;
    }

    private void saveState(boolean saveWindows) {
	ArbilFieldViews.getSingleInstance().saveViewsToFile();
	// linorgFavourites.saveSelectedFavourites(); // no need to do here because the list is saved when favourites are changed
	// TreeHelper.getArbilHelpInstance().saveLocations(null, null); no need to do this here but it must be done when ever a change is made
	if (saveWindows) {
	    windowManager.saveWindowStates();
	}
    }

    public boolean performCleanExit() { // TODO: this should be moved into a utility class
	windowManager.stopEditingInCurrentWindow();
	if (saveApplicationState()) {
//                viewChangesMenuItem.setEnabled(false);
//        screenCapture.stopCapture();
	    System.exit(0);
	    return true;
	}
	return false;
    }

    private void initViewMenu(javax.swing.JMenu viewMenu) {
	viewMenu.removeAll();
	ButtonGroup viewMenuButtonGroup = new javax.swing.ButtonGroup();
	//String[] viewLabels = guiHelper.imdiFieldViews.getSavedFieldViewLables();
	for (Enumeration menuItemName = ArbilFieldViews.getSingleInstance().getSavedFieldViewLables(); menuItemName.hasMoreElements();) {
	    String currentMenuName = menuItemName.nextElement().toString();
	    javax.swing.JRadioButtonMenuItem viewLabelRadioButtonMenuItem;
	    viewLabelRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
	    viewMenuButtonGroup.add(viewLabelRadioButtonMenuItem);
	    viewLabelRadioButtonMenuItem.setSelected(ArbilFieldViews.getSingleInstance().getCurrentGlobalViewName().equals(currentMenuName));
	    viewLabelRadioButtonMenuItem.setText(currentMenuName);
	    viewLabelRadioButtonMenuItem.setName(currentMenuName);
	    viewLabelRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent evt) {
		    try {
			ArbilFieldViews.getSingleInstance().setCurrentGlobalViewName(((Component) evt.getSource()).getName());
		    } catch (Exception ex) {
			BugCatcherManager.getBugCatcher().logError(ex);
		    }
		}
	    });
	    viewMenu.add(viewLabelRadioButtonMenuItem);
	}
    }
//    private void addTemplateMenuItem(JMenu templateMenu, ButtonGroup templatesMenuButtonGroup, String templateName, String selectedTemplate) {
//        JRadioButtonMenuItem templateMenuItem = new JRadioButtonMenuItem();
//        templateMenuItem.setText(templateName);
//        templateMenuItem.setName(templateName);
//        templateMenuItem.setActionCommand(templateName);
//        templateMenuItem.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                try {
//                    LinorgWindowManager.getArbilHelpInstance().addMessageDialogToQueue("This action is not yet available.", "Templates");
    //GuiHelper.linorgWindowManager.openUrlWindow(evt.getActionCommand() + templateList.get(evt.getActionCommand()).toString(), new File(templateList.get(evt.getActionCommand()).toString()).toURL());
//                    System.out.println("setting template: " + evt.getActionCommand());
//                    ArbilTemplateManager.getArbilHelpInstance().setCurrentTemplate(evt.getActionCommand());
//                } catch (Exception e) {
//                    BugCatcherManager.getBugCatcher().logError(e);
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
//                    String newDirectoryName = JOptionPane.showInputDialog(LinorgWindowManager.getArbilHelpInstance().linorgFrame, "Enter the name for the new template", LinorgWindowManager.getArbilHelpInstance().linorgFrame.getTitle(), JOptionPane.PLAIN_MESSAGE, null, null, null).toString();
//                    // if the user cancels the directory string will be a empty string.
//                    if (ArbilTemplateManager.getArbilHelpInstance().getTemplateFile(newDirectoryName).exists()) {
//                        LinorgWindowManager.getArbilHelpInstance().addMessageDialogToQueue("The template \"" + newDirectoryName + "\" already exists.", "Templates");
//                    }
//                    File freshTemplateFile = ArbilTemplateManager.getArbilHelpInstance().createTemplate(newDirectoryName);
//                    if (freshTemplateFile != null) {
//                        GuiHelper.getArbilHelpInstance().openFileInExternalApplication(freshTemplateFile.toURI());
//                        GuiHelper.getArbilHelpInstance().openFileInExternalApplication(freshTemplateFile.getParentFile().toURI());
//                    } else {
//                        LinorgWindowManager.getArbilHelpInstance().addMessageDialogToQueue("The template \"" + newDirectoryName + "\" could not be created.", "Templates");
//                    }
////                    LinorgWindowManager.getArbilHelpInstance().addMessageDialogToQueue("This action is not yet available.", "Templates");
//                    //GuiHelper.linorgWindowManager.openUrlWindow(evt.getActionCommand() + templateList.get(evt.getActionCommand()).toString(), new File(templateList.get(evt.getActionCommand()).toString()).toURL());
////                    System.out.println("setting template: " + evt.getActionCommand());
////                    ArbilTemplateManager.getArbilHelpInstance().setCurrentTemplate(evt.getActionCommand());
//                } catch (Exception e) {
//                    BugCatcherManager.getBugCatcher().logError(e);
//                }
//            }
//        });
//        templateMenu.add(templateMenuItem);
//    }
//    public void populateTemplatesMenu(JMenu templateMenu) {
//        templateMenu.removeAll();
//        ButtonGroup templatesMenuButtonGroup = new javax.swing.ButtonGroup();
//        int templateCount = 0;
//        addTemplateMenuItem(templateMenu, templatesMenuButtonGroup, "", "Default", ArbilTemplateManager.getArbilHelpInstance().getCurrentTemplate());
//        for (String currentTemplateName : ArbilTemplateManager.getArbilHelpInstance().getAvailableTemplates()) {
//            String templatePath = templatesDir.getPath() + File.separatorChar + currentTemplateName;
//            if (new File(templatePath).isDirectory()) {
//            addTemplateMenuItem(templateMenu, templatesMenuButtonGroup, currentTemplateName, ArbilTemplateManager.getArbilHelpInstance().getCurrentTemplateName());
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
//            addTemplateMenuItem(templateMenu, templatesMenuButtonGroup, currentCmdiProfile.name, ArbilTemplateManager.getArbilHelpInstance().getCurrentTemplateName());
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
