/**
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.arbil.ui.menu;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;
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
import nl.mpi.arbil.ArbilIcons;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilTreeHelper;
import nl.mpi.arbil.data.DataNodeLoader;
import nl.mpi.arbil.data.IMDIVocabularies;
import nl.mpi.arbil.plugins.ArbilPluginManager;
import nl.mpi.arbil.templates.ArbilTemplateManager;
import nl.mpi.arbil.ui.ArbilFieldViews;
import nl.mpi.arbil.ui.ArbilHelp;
import nl.mpi.arbil.ui.ArbilLogConsole;
import nl.mpi.arbil.ui.ArbilSplitPanel;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.ui.ImportExportDialog;
import nl.mpi.arbil.ui.LanguageListDialogue;
import nl.mpi.arbil.ui.PreviewSplitPanel;
import nl.mpi.arbil.ui.TemplateDialogue;
import nl.mpi.arbil.ui.wizard.ArbilWizard;
import nl.mpi.arbil.ui.wizard.setup.ArbilSetupWizard;
import nl.mpi.arbil.userstorage.ArbilConfiguration;
import nl.mpi.arbil.userstorage.ArbilConfigurationManager;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ApplicationVersion;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.ArbilLogConfigurer;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.MimeHashQueue;
import nl.mpi.arbil.util.WebstartHelper;
import nl.mpi.arbilcommons.journal.ArbilJournal;
import nl.mpi.arbilcommons.ui.LocalisationSelector;
import nl.mpi.pluginloader.PluginService;
import nl.mpi.pluginloader.ui.PluginMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * ArbilMenuBar.java Created on Jul 9, 2009, 12:01:02 PM
 *
 * @author Peter.Withers@mpi.nl
 */
public class ArbilMenuBar extends JMenuBar {

    private final static Logger logger = LoggerFactory.getLogger(ArbilMenuBar.class);
    private static final ResourceBundle menus = ResourceBundle.getBundle("nl/mpi/arbil/localisation/Menus");
    public static final String FORUM_URL = "http://tla.mpi.nl/forums/software/arbil/";
    private final WebstartHelper webstartHelper = new WebstartHelper();
    private final SessionStorage sessionStorage;
    private final MimeHashQueue mimeHashQueue;
    private final ArbilTreeHelper treeHelper;
    private final ArbilWindowManager windowManager;
    private final MessageDialogHandler dialogHandler;
    private final DataNodeLoader dataNodeLoader;
    private final ArbilLogConfigurer logConfigurer;
    private final ApplicationVersionManager versionManager;
    private final ArbilConfiguration applicationConfiguration;
    private final ArbilConfigurationManager configurationManager;
    private JMenu windowMenu = new JMenu();
    private boolean macOsMenu = false;
    private JMenuItem saveFileMenuItem = new JMenuItem();
    private JMenuItem showChangedNodesMenuItem = new JMenuItem();
    private JCheckBoxMenuItem saveWindowsCheckBoxMenuItem = new JCheckBoxMenuItem();
    private JMenuItem shortCutKeysjMenuItem = new JMenuItem();
    private JMenuItem selectLanguageMenuItem = new JMenuItem();
    private JMenuItem arbilForumMenuItem = new JMenuItem();
    private JMenuItem checkForUpdatesMenuItem = new JMenuItem();
    private JMenuItem viewErrorLogMenuItem = new JMenuItem();
    private JCheckBoxMenuItem showSelectionPreviewCheckBoxMenuItem = new JCheckBoxMenuItem();
    private JMenuItem templatesMenu = new JMenuItem();
    private JCheckBoxMenuItem trackTableSelectionCheckBoxMenuItem = new JCheckBoxMenuItem();
    private JCheckBoxMenuItem useLanguageIdInColumnNameCheckBoxMenuItem = new JCheckBoxMenuItem();
    private JMenuItem verbatimXmlStructureMenuItem = new JCheckBoxMenuItem();
    private JMenuItem undoMenuItem = new JMenuItem();
//    private JMenuItem viewFavouritesMenuItem;
//    private JMenu setStorageDirectoryMenu;
    private JMenu setCacheDirectoryMenu = new JMenu();
    private JMenu viewMenu = new JMenu();
    private JMenuItem resetWindowsMenuItem = new JMenuItem();
    private JMenuItem closeWindowsMenuItem = new JMenuItem();
    private JMenu optionsMenu = new JMenu();
    private JMenuItem searchReplaceMenuItem;
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
    private JMenuItem logConsoleMenuItem = new JMenuItem();
//    private JMenuItem editLocationsMenuItem;
    private JMenuItem updateAllLoadedVocabulariesMenuItem = new JMenuItem();
    private JMenu editMenu = new JMenu();
    private JMenu fileMenu = new JMenu();
    private JMenu helpMenu = new JMenu();
    private JMenuItem helpMenuItem = new JMenuItem();
    private JMenuItem setupWizardMenuItem = new JMenuItem();
    private JMenuItem importMenuItem = new JMenuItem();
    private JCheckBoxMenuItem showStatusBarMenuItem = new JCheckBoxMenuItem();
    private final PreviewSplitPanel previewSplitPanel;
    private final JApplet containerApplet;
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

    public ArbilMenuBar(ArbilConfiguration appConfiguration, SessionStorage sessionStorage, MessageDialogHandler dialogHandler, ArbilWindowManager windowManager, ArbilTreeHelper treeHelper, DataNodeLoader dataNodeLoader, MimeHashQueue mimeHashQueue, ApplicationVersionManager versionManager, ArbilLogConfigurer logConfigurer, JApplet containerApplet, PreviewSplitPanel previewSplitPanel) {
	this.containerApplet = containerApplet;
	this.previewSplitPanel = previewSplitPanel;
	this.versionManager = versionManager;
	this.logConfigurer = logConfigurer;
	this.applicationConfiguration = appConfiguration;
	this.treeHelper = treeHelper;
	this.windowManager = windowManager;
	this.dialogHandler = dialogHandler;
	this.dataNodeLoader = dataNodeLoader;
	this.sessionStorage = sessionStorage;
	this.mimeHashQueue = mimeHashQueue;
	this.configurationManager = new ArbilConfigurationManager(sessionStorage);

	initFileMenu();
	initEditMenu();
	initOptionsMenu();
	initPluginMenu();
	initWindowMenu();
	initHelpMenu();

	setUpHotKeys();
    }

    private ArbilHelp getArbilHelp() {
	try {
	    return ArbilHelp.getArbilHelpInstance();
	} catch (IOException ioEx) {
	    dialogHandler.addMessageDialogToQueue(menus.getString("I/O ERROR WHILE TRYING TO READ HELP SYSTEM! SEE ERROR LOG FOR DETAILS."), menus.getString("ERROR"));
	    BugCatcherManager.getBugCatcher().logError(ioEx);
	} catch (SAXException saxEx) {
	    dialogHandler.addMessageDialogToQueue(menus.getString("PARSER ERROR WHILE TRYING TO READ HELP SYSTEM! SEE ERROR LOG FOR DETAILS."), menus.getString("ERROR"));
	    BugCatcherManager.getBugCatcher().logError(saxEx);
	}
	return null;
    }

    private void initFileMenu() {
	fileMenu.setText(menus.getString("FILE"));
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
	saveFileMenuItem.setText(menus.getString("SAVE CHANGES"));
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
	showChangedNodesMenuItem.setText(menus.getString("SHOW MODIFIED NODES"));
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
		    windowManager.openFloatingTable(individualChangedNodes.toArray(new ArbilDataNode[]{}), menus.getString("MODIFIED NODES"));
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	fileMenu.add(showChangedNodesMenuItem);
	importMenuItem.setText(menus.getString("IMPORT..."));
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
	exitMenuItem.setText(menus.getString("EXIT"));
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
	    String loggedInUserName = containerApplet.getParameter(menus.getString("USERNAME"));
	    // in php this could be: $_SERVER["REMOTE_USER"] or $_SERVER["eduPersonPrincipalName"] or $_SERVER["HTTP_EDUPERSONPRINCIPALNAME"]
	    if (loggedInUserName == null) {
		loggedInUserName = menus.getString("UNKNOWN USER");
	    }
	    JMenuItem logoutButton = new JMenuItem(java.text.MessageFormat.format(menus.getString("LOG OUT ({0})"), new Object[]{loggedInUserName}));
	    logoutButton.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent evt) {
		    String logoutUrl = containerApplet.getParameter(menus.getString("LOGOUTURL"));
		    try {
			//			String userName = containerApplet.getParameter("UserName");
			if (containerApplet != null) {
			    //LinorgWindowManager.getArbilHelpInstance().openUrlWindowOnce("Log out", new URL(logoutUrl));
			    containerApplet.getAppletContext().showDocument(new URL(logoutUrl));
			}
		    } catch (MalformedURLException ex) {
			dialogHandler.addMessageDialogToQueue(java.text.MessageFormat.format(menus.getString("INVALID LOGOUT URL: {0}"),
				new Object[]{logoutUrl}), menus.getString("LOGOUT ERROR"));
			BugCatcherManager.getBugCatcher().logError(ex);
		    }
		}
	    });
	    fileMenu.add(logoutButton);
	}
	this.add(fileMenu);
    }

    private void initEditMenu() {
	editMenu.setText(menus.getString("EDIT"));
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
	copyMenuItem.setText(menus.getString("COPY"));
	copyMenuItem.setEnabled(false);
	//        by the time the menu action has occured the focus has moved to the root pane of the application, this further supports the concept that a global copy paste for a multi focus UI is a flawed concept
	//        copyMenuItem.addActionListener(new java.awt.event.ActionListener() {
	//
	//            public void actionPerformed(java.awt.event.ActionEvent evt) {
	//                Component currentFocusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
	//                logger.debug("currentFocusOwner: " + currentFocusOwner);
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
	pasteMenuItem.setText(menus.getString("PASTE"));
	pasteMenuItem.setEnabled(false);
	//        pasteMenuItem.addActionListener(new java.awt.event.ActionListener() {
	//
	//            public void actionPerformed(java.awt.event.ActionEvent evt) {
	//                Component currentFocusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
	//            }
	//        });
	//        editMenu.add(pasteMenuItem);
	undoMenuItem.setText(menus.getString("UNDO"));
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

	searchReplaceMenuItem = new JMenuItem() {
	    @Override
	    public boolean isEnabled() {
		return (windowManager.getCurrentFrameComponent() instanceof ArbilSplitPanel);
	    }
	};
	searchReplaceMenuItem.setText(menus.getString("FIND/REPLACE"));
	searchReplaceMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		Component selectedComponent = windowManager.getCurrentFrameComponent();
		if (selectedComponent instanceof ArbilSplitPanel) {
		    ((ArbilSplitPanel) selectedComponent).showSearchPane();
		}
	    }
	});
	editMenu.add(searchReplaceMenuItem);

	editMenu.add(undoMenuItem);
	redoMenuItem.setText(menus.getString("REDO"));
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

	optionsMenu.setText(menus.getString("OPTIONS"));

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

	templatesMenu.setText(menus.getString("TEMPLATES & PROFILES..."));
	templatesMenu.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    TemplateDialogue.showTemplatesDialogue(windowManager, dialogHandler);
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

	setCacheDirectoryMenu.setText(menus.getString("LOCAL CORPUS STORAGE DIRECTORY"));
	setCacheDirectoryMenu.addMenuListener(new javax.swing.event.MenuListener() {
	    public void menuCanceled(javax.swing.event.MenuEvent evt) {
	    }

	    public void menuDeselected(javax.swing.event.MenuEvent evt) {
	    }

	    public void menuSelected(javax.swing.event.MenuEvent evt) {
		setCacheDirectoryMenu.removeAll();
		JMenuItem cacheDirectoryMenuItem = new JMenuItem();
		cacheDirectoryMenuItem.setText(sessionStorage.getProjectWorkingDirectory().getAbsolutePath());
		cacheDirectoryMenuItem.setEnabled(false);
		JMenuItem changeCacheDirectoryMenuItem = new JMenuItem();
		changeCacheDirectoryMenuItem.setText(menus.getString("MOVE LOCAL CORPUS STORAGE DIRECTORY..."));
		changeCacheDirectoryMenuItem.addActionListener(new java.awt.event.ActionListener() {
		    public void actionPerformed(java.awt.event.ActionEvent evt) {
			try {
			    dialogHandler.offerUserToSaveChanges();
			    File[] selectedFiles = dialogHandler.showDirectorySelectBox(menus.getString("MOVE LOCAL CORPUS STORAGE DIRECTORY"), false);
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

	editPreferredLanguagesMenuItem.setText(menus.getString("EDIT IMDI LANGUAGE LIST..."));
	editPreferredLanguagesMenuItem.setEnabled(true);
	editPreferredLanguagesMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		LanguageListDialogue.showLanguageDialogue(windowManager, dialogHandler);
	    }
	});
	optionsMenu.add(editPreferredLanguagesMenuItem);

	updateAllLoadedVocabulariesMenuItem.setText(menus.getString("RE-DOWNLOAD CURRENT VOCABULARIES"));
	updateAllLoadedVocabulariesMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		IMDIVocabularies.getSingleInstance().redownloadCurrentlyLoadedVocabularies();
	    }
	});
	optionsMenu.add(updateAllLoadedVocabulariesMenuItem);

	saveWindowsCheckBoxMenuItem.setSelected(sessionStorage.loadBoolean("saveWindows", true));
	saveWindowsCheckBoxMenuItem.setText(menus.getString("SAVE WINDOWS ON EXIT"));
	optionsMenu.add(saveWindowsCheckBoxMenuItem);

	showSelectionPreviewCheckBoxMenuItem.setSelected(sessionStorage.loadBoolean("showSelectionPreview", true));
	previewSplitPanel.setPreviewPanel(showSelectionPreviewCheckBoxMenuItem.getState());
	showSelectionPreviewCheckBoxMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
//        showSelectionPreviewCheckBoxMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.META_MASK));
	showSelectionPreviewCheckBoxMenuItem.setText(menus.getString("SHOW SELECTION PREVIEW"));
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

	showStatusBarMenuItem.setText(menus.getString("SHOW STATUS BAR"));
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
	checkNewVersionAtStartCheckBoxMenuItem.setText(menus.getString("CHECK FOR NEW VERSION ON START"));
	optionsMenu.add(checkNewVersionAtStartCheckBoxMenuItem);

	copyNewResourcesCheckBoxMenuItem.setSelected(applicationConfiguration.isCopyNewResourcesToCache());
	copyNewResourcesCheckBoxMenuItem.setText(menus.getString("COPY NEW RESOURCES INTO CACHE"));
	copyNewResourcesCheckBoxMenuItem.setToolTipText(menus.getString("WHEN ADDING A NEW RESOURCE TO A SESSION THIS OPTIONS WILL COPY THE FILE INTO THE LOCAL CACHE RATHER THAN LINKING TO ITS CURRENT LOCATION. THIS OPTION CAN MAKE A CONSIDERABLE DIFFERENCE TO DISK USE IF YOU ARE HANDLING LARGE FILES."));
	copyNewResourcesCheckBoxMenuItem.addItemListener(new java.awt.event.ItemListener() {
	    public void itemStateChanged(java.awt.event.ItemEvent evt) {
		applicationConfiguration.setCopyNewResourcesToCache(copyNewResourcesCheckBoxMenuItem.isSelected());
		configurationManager.write(applicationConfiguration);
	    }
	});
	optionsMenu.add(copyNewResourcesCheckBoxMenuItem);

	checkResourcePermissionsCheckBoxMenuItem.setSelected(mimeHashQueue.isCheckResourcePermissions());
	checkResourcePermissionsCheckBoxMenuItem.setText(menus.getString("CHECK PERMISSIONS FOR REMOTE RESOURCES"));
	checkResourcePermissionsCheckBoxMenuItem.setToolTipText(menus.getString("THIS OPTION CHECKS THE SERVER PERMISSIONS FOR REMOTE RESOURCES AND SHOWS ICONS ACCORDINGLY."));
	checkResourcePermissionsCheckBoxMenuItem.addItemListener(new java.awt.event.ItemListener() {
	    public void itemStateChanged(java.awt.event.ItemEvent evt) {
		mimeHashQueue.setCheckResourcePermissions(checkResourcePermissionsCheckBoxMenuItem.isSelected());
		sessionStorage.saveBoolean("checkResourcePermissions", checkResourcePermissionsCheckBoxMenuItem.isSelected());
		dialogHandler.addMessageDialogToQueue(menus.getString("THE SETTING CHANGE WILL BE EFFECTIVE WHEN ARBIL IS RESTARTED."), menus.getString("CHECK PERMISSIONS FOR REMOTE RESOURCES"));
	    }
	});
	optionsMenu.add(checkResourcePermissionsCheckBoxMenuItem);

	schemaCheckLocalFiles.setText(menus.getString("ALWAYS CHECK LOCAL METADATA FILES FOR XML CONFORMANCE"));
	schemaCheckLocalFiles.setSelected(dataNodeLoader.isSchemaCheckLocalFiles());
	schemaCheckLocalFiles.setToolTipText(menus.getString("THIS OPTION CHECKS ALL LOCAL METADATA FILES FOR XML CONFORMANCE EVERY TIME THEY ARE LOADED. IF THE METADATA FILE DOES NOT VALIDATE AGAINST THE SCHEMA IT WILL BE HIGHLIGHTED RED IN THE TREE."));
	schemaCheckLocalFiles.addItemListener(new java.awt.event.ItemListener() {
	    public void itemStateChanged(java.awt.event.ItemEvent evt) {
		dataNodeLoader.setSchemaCheckLocalFiles(schemaCheckLocalFiles.isSelected());
		sessionStorage.saveBoolean("schemaCheckLocalFiles", schemaCheckLocalFiles.isSelected());
	    }
	});
	optionsMenu.add(schemaCheckLocalFiles);

	trackTableSelectionCheckBoxMenuItem.setSelected(sessionStorage.loadBoolean("trackTableSelection", false));
	trackTableSelectionCheckBoxMenuItem.setText(menus.getString("TRACK TABLE SELECTION IN TREE"));
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
	trackTableSelectionCheckBoxMenuItem.setEnabled(true);
	optionsMenu.add(trackTableSelectionCheckBoxMenuItem);

	useLanguageIdInColumnNameCheckBoxMenuItem.setSelected(sessionStorage.loadBoolean("useLanguageIdInColumnName", false));
	useLanguageIdInColumnNameCheckBoxMenuItem.setText(menus.getString("SHOW LANGUAGE IN COLUMN NAME"));
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

	verbatimXmlStructureMenuItem.setSelected(applicationConfiguration.isVerbatimXmlTreeStructure());
	verbatimXmlStructureMenuItem.setText(menus.getString("MENU OPTION VERBATIM XML STRUCTURE IN TREE"));
	verbatimXmlStructureMenuItem.setToolTipText(menus.getString("MENU OPTION VERBATIM XML STRUCTURE TOOLTIP"));
	verbatimXmlStructureMenuItem.setEnabled(true);
	verbatimXmlStructureMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent ae) {
		if (dialogHandler.showConfirmDialogBox(
			menus.getString("MENU SWITCH XML VERBATIM STRUCTURE CONFIRM"),
			menus.getString("MENU OPTION VERBATIM XML STRUCTURE IN TREE"))) {
		    applicationConfiguration.setVerbatimXmlTreeStructure(verbatimXmlStructureMenuItem.isSelected());
		    configurationManager.write(applicationConfiguration);
		    ArbilTemplateManager.getSingleInstance().unloadCmdiTemplates();
		    dataNodeLoader.requestReloadAllNodes();
		}
		verbatimXmlStructureMenuItem.setSelected(applicationConfiguration.isVerbatimXmlTreeStructure());
	    }
	});
	optionsMenu.add(verbatimXmlStructureMenuItem);

	optionsMenu.add(new JSeparator());

	viewMenu.setText(menus.getString("COLUMN VIEW FOR NEW TABLES"));
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

	editFieldViewsMenuItem.setText(menus.getString("EDIT COLUMN VIEWS"));
	editFieldViewsMenuItem.setEnabled(false);
//        editFieldViewsMenuItem.addActionListener(new java.awt.event.ActionListener() {
//
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                editFieldViewsMenuItemActionPerformed(evt);
//            }
//        });

	//TODO: Implement editFieldViewsMenuItem and re-enable
//        optionsMenu.add(editFieldViewsMenuItem);
	this.add(optionsMenu);
    }

    private void initPluginMenu() {
	final ArbilPluginManager pluginManager = new ArbilPluginManager(sessionStorage, windowManager, dataNodeLoader, BugCatcherManager.getBugCatcher());
	final List<URL> pluginUlrs = pluginManager.getPluginsFromDirectoriesAndPluginsList();
	final String javaVersion = System.getProperty("java.version");
	if (!(javaVersion.startsWith("1.4.") || javaVersion.startsWith("1.5."))) {
	    try {
		this.add(new PluginMenu(new PluginService(pluginUlrs.toArray(new URL[]{})), pluginManager, true));
	    } catch (NoClassDefFoundError error) {
		logger.error("Failed to initialize plugin system. Probably JRE version issue despite check.", error);
	    }
	} else {
	    logger.warn("Plugins are NOT enabled due to unsupported java version {}", javaVersion);
	}
    }

    private void initWindowMenu() {
	windowManager.setWindowMenu(windowMenu);

	windowMenu.setText(menus.getString("WINDOW"));

	resetWindowsMenuItem.setText(menus.getString("RESET WINDOW LOCATIONS"));
	resetWindowsMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		if (dialogHandler.showConfirmDialogBox(menus.getString("RESET ALL WINDOWS TO DEFAULT SIZE AND LOCATION?"), menus.getString("RESET WINDOWS"))) {
		    windowManager.resetWindows();
		}
	    }
	});

	closeWindowsMenuItem.setText(menus.getString("CLOSE ALL WINDOWS"));
	closeWindowsMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		if (dialogHandler.showConfirmDialogBox(menus.getString("CLOSE ALL WINDOWS?"), menus.getString("CLOSE WINDOWS"))) {
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
	helpMenu.setText(menus.getString("HELP"));
	helpMenu.addMenuListener(new MenuListener() {
	    public void menuCanceled(MenuEvent evt) {
	    }

	    public void menuDeselected(MenuEvent evt) {
	    }

	    public void menuSelected(MenuEvent evt) {
		viewErrorLogMenuItem.setEnabled(logConfigurer.getLogFile(sessionStorage).exists());
	    }
	});
	aboutMenuItem.setText(menus.getString("ABOUT"));
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
	helpMenuItem.setText(menus.getString("HELP"));
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
	setupWizardMenuItem.setText(menus.getString("RUN SETUP WIZARD"));
	setupWizardMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		sessionStorage.saveString(SessionStorage.PARAM_WIZARD_RUN, menus.getString("YES"));
		ArbilWizard wizard = new ArbilSetupWizard(windowManager.getMainFrame());
		wizard.showModalDialog();
	    }
	});
	helpMenu.add(setupWizardMenuItem);
	arbilForumMenuItem.setText(menus.getString("ARBIL FORUM (WEBSITE)"));
	arbilForumMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    windowManager.openFileInExternalApplication(new URI(FORUM_URL));
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	helpMenu.add(arbilForumMenuItem);

	logConsoleMenuItem.setText(menus.getString("SHOW LOG CONSOLE"));
	logConsoleMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		new ArbilLogConsole(windowManager.getMainFrame()).show();
	    }
	});
	if (webstartHelper.isWebStart()) {
	    logConsoleMenuItem.setEnabled(false);
	    logConsoleMenuItem.setToolTipText(menus.getString("LOG CONSOLE NOT AVAILABLE WEB START"));
	}
	helpMenu.add(logConsoleMenuItem);

	viewErrorLogMenuItem.setText(menus.getString("VIEW ERROR LOG"));
	viewErrorLogMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    windowManager.openFileInExternalApplication(logConfigurer.getLogFile(sessionStorage).toURI());
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	helpMenu.add(viewErrorLogMenuItem);
	checkForUpdatesMenuItem.setText(menus.getString("CHECK FOR UPDATES"));
	checkForUpdatesMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    if (!versionManager.forceUpdateCheck()) {
			ApplicationVersion appVersion = versionManager.getApplicationVersion();
			String versionString = appVersion.currentMajor + "." + appVersion.currentMinor + "." + appVersion.currentRevision;
			dialogHandler.addMessageDialogToQueue(java.text.MessageFormat.format(menus.getString("NO UPDATES FOUND, CURRENT VERSION IS {0}"),
				new Object[]{versionString}), menus.getString("CHECK FOR UPDATES"));
		    }
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	helpMenu.add(checkForUpdatesMenuItem);

	shortCutKeysjMenuItem.setText(menus.getString("SHORT CUT KEYS"));
	shortCutKeysjMenuItem.addActionListener(
		new java.awt.event.ActionListener() {
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

	selectLanguageMenuItem.setText(menus.getString("SELECT LANGUAGE"));
	selectLanguageMenuItem.addActionListener(
		new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    final String availableLanguages = ResourceBundle.getBundle("nl/mpi/arbil/localisation/AvailableLanguages").getString("LANGUAGE CODES");
		    final LocalisationSelector localisationSelector = new LocalisationSelector(sessionStorage, availableLanguages.split(","));
		    final String please_select_your_preferred_language = menus.getString("PLEASE SELECT YOUR PREFERRED LANGUAGE");
		    final String language_Selection = menus.getString("LANGUAGE SELECTION");
		    final String system_Default = menus.getString("SYSTEM DEFAULT");
		    localisationSelector.askUser(windowManager.getMainFrame(), ArbilIcons.getSingleInstance().linorgIcon, please_select_your_preferred_language, language_Selection, system_Default);
		    localisationSelector.setLanguageFromSaved();
		    dialogHandler.addMessageDialogToQueue(menus.getString("PLEASE RESTART THE APPLICATION FOR THE LANGUAGE SELECTION TO BECOME VISIBLE"), menus.getString("SELECT LANGUAGE"));
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	helpMenu.add(selectLanguageMenuItem);
	this.add(helpMenu);
    }

    private void setUpHotKeys() {
	// The jdesktop seems to be consuming the key strokes used for save, undo and redo so to make this available over the whole 
	// interface it has been added here.
	//
	// These shortcuts are also configured in ArbilWindowManager through an AWT event listener to make sure they are globablly available
	// even before the menu has been accessed.

	saveFileMenuItem.setMnemonic(java.awt.event.KeyEvent.VK_S);
	saveFileMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
	undoMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
	redoMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
	searchReplaceMenuItem.setMnemonic(java.awt.event.KeyEvent.VK_F);
	searchReplaceMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
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
	    //TODO: Decide whether to show IMDI or CMDI help (possibly based on previous state)
	    helpComponent.setCurrentPage(ArbilHelp.IMDI_HELPSET, ArbilHelp.SHOTCUT_KEYS_PAGE);
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
	    logger.debug(e.getMessage());
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
	    switch (JOptionPane.showConfirmDialog(this, menus.getString("SAVE CHANGES BEFORE EXITING?"), menus.getString("ARBIL"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)) {
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
//                    logger.debug("setting template: " + evt.getActionCommand());
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
////                    logger.debug("setting template: " + evt.getActionCommand());
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
//        for (CmdiProfile currentCmdiProfile : cmdiProfileReader.cmdiProfileArray) {
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
