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
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JApplet;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
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
import nl.mpi.arbil.util.WindowManager;
import nl.mpi.arbilcommons.journal.ArbilJournal;
import nl.mpi.arbilcommons.ui.LocalisationSelector;
import nl.mpi.pluginloader.PluginService;
import nl.mpi.pluginloader.ui.PluginMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;

/**
 * ArbilMenuBar.java Created on Jul 9, 2009, 12:01:02 PM
 *
 * @author Peter.Withers@mpi.nl
 */
public class ArbilMenuBar extends JMenuBar {

    public static enum HostOS {

	MACOS,
	OTHER
    }
    private final static Logger logger = LoggerFactory.getLogger(ArbilMenuBar.class);
    private static final ResourceBundle menus = ResourceBundle.getBundle("nl/mpi/arbil/localisation/Menus");
    private static final ResourceBundle services = java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Services");
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
    private final PreviewSplitPanel previewSplitPanel;
    private final JApplet containerApplet;
    private final HostOS hostOS;

    public ArbilMenuBar(ArbilConfiguration appConfiguration, SessionStorage sessionStorage, MessageDialogHandler dialogHandler, ArbilWindowManager windowManager, ArbilTreeHelper treeHelper, DataNodeLoader dataNodeLoader, MimeHashQueue mimeHashQueue, ApplicationVersionManager versionManager, ArbilLogConfigurer logConfigurer, JApplet containerApplet, PreviewSplitPanel previewSplitPanel, HostOS hostOS) {
	this.containerApplet = containerApplet;
	this.previewSplitPanel = previewSplitPanel;
	this.hostOS = hostOS;
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
	initViewMenu();
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
	saveFileMenuItem.setEnabled(false);
	saveFileMenuItem.setAction(saveAction);
	saveFileMenuItem.setText(menus.getString("SAVE CHANGES"));

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
	undoMenuItem.setEnabled(false);
	undoMenuItem.setAction(undoAction);
	undoMenuItem.setText(menus.getString("UNDO"));

	redoMenuItem.setEnabled(false);
	redoMenuItem.setAction(redoAction);
	redoMenuItem.setText(menus.getString("REDO"));

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
	editMenu.add(redoMenuItem);
	this.add(editMenu);
    }

    private void initViewMenu() {
	showSelectionPreviewCheckBoxMenuItem.setSelected(sessionStorage.loadBoolean("showSelectionPreview", true));
	previewSplitPanel.setPreviewPanel(showSelectionPreviewCheckBoxMenuItem.getState());
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

	showStatusBarMenuItem.setText(menus.getString("SHOW STATUS BAR"));
	showStatusBarMenuItem.setState(sessionStorage.loadBoolean("showStatusBar", false));
	showStatusBarMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		windowManager.setStatusBarVisible(showStatusBarMenuItem.getState());
		sessionStorage.saveBoolean("showStatusBar", showStatusBarMenuItem.getState());
	    }
	});
	// Status bar not fully functional ATM, so disabled until completion (https://trac.mpi.nl/ticket/920)
	showStatusBarMenuItem.setVisible(false);
	showStatusBarMenuItem.setEnabled(false);

	zoomInMenuItem.setAction(zoomInAction);
	zoomOutMenuItem.setAction(zoomOutAction);
	zoomResetMenuItem.setAction(zoomResetAction);

	viewMenu.add(showSelectionPreviewCheckBoxMenuItem);
	viewMenu.add(showStatusBarMenuItem);

	viewMenu.add(new JSeparator());

	viewMenu.add(zoomInMenuItem);
	viewMenu.add(zoomOutMenuItem);
	viewMenu.add(zoomResetMenuItem);

	viewMenu.setText(menus.getString("MENU_VIEW"));
	this.add(viewMenu);
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
		    dataNodeLoader.saveNodesNeedingSave(false);
		    configurationManager.write(applicationConfiguration);
		    ArbilTemplateManager.getSingleInstance().unloadCmdiTemplates();
		    dataNodeLoader.requestReloadAllMetadataNodes();
		}
		verbatimXmlStructureMenuItem.setSelected(applicationConfiguration.isVerbatimXmlTreeStructure());
	    }
	});
	optionsMenu.add(verbatimXmlStructureMenuItem);

	optionsMenu.add(new JSeparator());

	columnViewMenu.setText(menus.getString("COLUMN VIEW FOR NEW TABLES"));
	columnViewMenu.addMenuListener(new MenuListener() {
	    public void menuCanceled(MenuEvent evt) {
	    }

	    public void menuDeselected(MenuEvent evt) {
	    }

	    public void menuSelected(MenuEvent evt) {
		colomnViewMenuMenuSelected(evt);
	    }
	});
	optionsMenu.add(columnViewMenu);

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
	setupWizardMenuItem.setText(menus.getString("RUN SETUP WIZARD"));
	setupWizardMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		sessionStorage.saveString(SessionStorage.PARAM_WIZARD_RUN, menus.getString("YES"));
		ArbilWizard wizard = new ArbilSetupWizard(windowManager.getMainFrame());
		wizard.showModalDialog();
	    }
	});
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

	helpMenu.setText(menus.getString("HELP"));
	helpMenu.add(aboutMenuItem);
	helpMenu.add(helpMenuItem);
	helpMenu.add(setupWizardMenuItem);
	helpMenu.add(shortCutKeysjMenuItem);
	helpMenu.add(new JSeparator());
	helpMenu.add(arbilForumMenuItem);
	helpMenu.add(checkForUpdatesMenuItem);
	helpMenu.add(new JSeparator());
	helpMenu.add(viewErrorLogMenuItem);
	helpMenu.add(logConsoleMenuItem);
	helpMenu.add(new JSeparator());
	helpMenu.add(selectLanguageMenuItem);
	this.add(helpMenu);

	helpMenu.addMenuListener(new MenuListener() {
	    public void menuCanceled(MenuEvent evt) {
	    }

	    public void menuDeselected(MenuEvent evt) {
	    }

	    public void menuSelected(MenuEvent evt) {
		viewErrorLogMenuItem.setEnabled(logConfigurer.getLogFile(sessionStorage).exists());
	    }
	});
    }

    private void setUpHotKeys() {
	// The jdesktop seems to be consuming the key strokes used for save, undo and redo so to make this available over the whole 
	// interface it has been added here.

	// Command key (meta) is default modifier for MacOS, CTRL for other OS's
	final int modifier = (hostOS == HostOS.MACOS) ? KeyEvent.META_DOWN_MASK : KeyEvent.CTRL_DOWN_MASK;

	saveFileMenuItem.setMnemonic(java.awt.event.KeyEvent.VK_S);
	final KeyStroke saveKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_S, modifier);
	saveFileMenuItem.setAccelerator(saveKeyStroke);
	getActionMap().put("save", saveAction);
	getInputMap(WHEN_IN_FOCUSED_WINDOW).put(saveKeyStroke, "save");

	final KeyStroke undoKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Z, modifier);
	undoMenuItem.setAccelerator(undoKeyStroke);
	getActionMap().put("undo", undoAction);
	getInputMap(WHEN_IN_FOCUSED_WINDOW).put(undoKeyStroke, "undo");

	final KeyStroke redoKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.SHIFT_DOWN_MASK | modifier);
	redoMenuItem.setAccelerator(redoKeyStroke);
	getActionMap().put("redo", redoAction);
	getInputMap(WHEN_IN_FOCUSED_WINDOW).put(redoKeyStroke, "redo");
	getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, modifier), "redo");

	searchReplaceMenuItem.setMnemonic(KeyEvent.VK_F);
	searchReplaceMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, modifier));
	showStatusBarMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, modifier));
	showSelectionPreviewCheckBoxMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, modifier));
	helpMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));

	// Zoom in shortcuts
	zoomInMenuItem.setMnemonic(KeyEvent.VK_PLUS);
	zoomInMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, modifier));
	getActionMap().put("zoomIn", zoomInAction);
	getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, modifier), "zoomIn");
	getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, modifier), "zoomIn");
	getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, modifier), "zoomIn");
	getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, modifier | KeyEvent.SHIFT_DOWN_MASK), "zoomIn");

	// Zoom out shortcuts
	zoomOutMenuItem.setMnemonic(KeyEvent.VK_MINUS);
	zoomOutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, modifier));
	getActionMap().put("zoomOut", zoomOutAction);
	getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, modifier), "zoomOut");
	getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, modifier), "zoomOut");

	// Zoom reset shortcuts
	zoomResetMenuItem.setMnemonic(KeyEvent.VK_0);
	zoomResetMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, modifier));
	getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_0, modifier), "zoomReset");

    }

    private void colomnViewMenuMenuSelected(MenuEvent evt) {
	initColumnViewMenu(columnViewMenu);
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
	    final Runnable exitRunner = new Runnable() {
		public void run() {
		    new HashQueueTerminator(mimeHashQueue, windowManager).terminateHashQueue();
		    System.exit(0);
		}
	    };

	    new Thread(exitRunner).start();
	    return true;
	} else {
	    return false;
	}
    }

    private void initColumnViewMenu(javax.swing.JMenu menu) {
	menu.removeAll();
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
	    menu.add(viewLabelRadioButtonMenuItem);
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
    private final Action zoomInAction = new AbstractAction(menus.getString("MENU_INCREASE_FONT_SIZE")) {
	public void actionPerformed(ActionEvent ae) {
	    windowManager.changeFontScale(ArbilWindowManager.FONT_SCALE_STEP);
	}
    };
    private final Action zoomOutAction = new AbstractAction(menus.getString("MENU_DECREASE_FONT_SIZE")) { //TODO: i18n
	public void actionPerformed(ActionEvent ae) {
	    windowManager.changeFontScale(-1 * ArbilWindowManager.FONT_SCALE_STEP);
	}
    };
    private final Action zoomResetAction = new AbstractAction(menus.getString("MENU_RESET_FONT_SIZE")) { //TODO: i18n
	public void actionPerformed(ActionEvent ae) {
	    windowManager.resetFontScale();
	}
    };
    private final Action saveAction = new AbstractAction() {
	public void actionPerformed(ActionEvent ae) {
	    try {
		windowManager.stopEditingInCurrentWindow();
		dataNodeLoader.saveNodesNeedingSave(true);
	    } catch (Exception ex) {
		BugCatcherManager.getBugCatcher().logError(ex);
	    }
	}
    };
    private final Action redoAction = new AbstractAction() {
	public void actionPerformed(ActionEvent ae) {
	    try {
		windowManager.stopEditingInCurrentWindow();
		ArbilJournal.getSingleInstance().redoFromFieldChangeHistory();
	    } catch (Exception ex) {
		BugCatcherManager.getBugCatcher().logError(ex);
	    }
	}
    };
    private final Action undoAction = new AbstractAction() {
	public void actionPerformed(ActionEvent ae) {
	    try {
		windowManager.stopEditingInCurrentWindow();
		ArbilJournal.getSingleInstance().undoFromFieldChangeHistory();
	    } catch (Exception ex) {
		BugCatcherManager.getBugCatcher().logError(ex);
	    }
	}
    };
    private final JMenu windowMenu = new JMenu();
    private final JMenuItem saveFileMenuItem = new JMenuItem();
    private final JMenuItem showChangedNodesMenuItem = new JMenuItem();
    private final JCheckBoxMenuItem saveWindowsCheckBoxMenuItem = new JCheckBoxMenuItem();
    private final JMenuItem shortCutKeysjMenuItem = new JMenuItem();
    private final JMenuItem selectLanguageMenuItem = new JMenuItem();
    private final JMenuItem arbilForumMenuItem = new JMenuItem();
    private final JMenuItem checkForUpdatesMenuItem = new JMenuItem();
    private final JMenuItem viewErrorLogMenuItem = new JMenuItem();
    private final JCheckBoxMenuItem showSelectionPreviewCheckBoxMenuItem = new JCheckBoxMenuItem();
    private final JMenuItem templatesMenu = new JMenuItem();
    private final JCheckBoxMenuItem trackTableSelectionCheckBoxMenuItem = new JCheckBoxMenuItem();
    private final JCheckBoxMenuItem useLanguageIdInColumnNameCheckBoxMenuItem = new JCheckBoxMenuItem();
    private final JMenuItem verbatimXmlStructureMenuItem = new JCheckBoxMenuItem();
    private final JMenuItem undoMenuItem = new JMenuItem();
    private final JMenu setCacheDirectoryMenu = new JMenu();
    private final JMenu columnViewMenu = new JMenu();
    private final JMenuItem resetWindowsMenuItem = new JMenuItem();
    private final JMenuItem closeWindowsMenuItem = new JMenuItem();
    private final JMenu optionsMenu = new JMenu();
    private final JMenuItem pasteMenuItem = new JMenuItem();
    private final JMenuItem redoMenuItem = new JMenuItem();
    private final JCheckBoxMenuItem checkNewVersionAtStartCheckBoxMenuItem = new JCheckBoxMenuItem();
    private final JMenuItem copyMenuItem = new JMenuItem();
    private final JCheckBoxMenuItem copyNewResourcesCheckBoxMenuItem = new JCheckBoxMenuItem();
    private final JCheckBoxMenuItem checkResourcePermissionsCheckBoxMenuItem = new JCheckBoxMenuItem();
    private final JCheckBoxMenuItem schemaCheckLocalFiles = new JCheckBoxMenuItem();
    private final JMenuItem editPreferredLanguagesMenuItem = new JMenuItem();
    private final JMenuItem editFieldViewsMenuItem = new JMenuItem();
    private final JMenuItem logConsoleMenuItem = new JMenuItem();
    private final JMenuItem updateAllLoadedVocabulariesMenuItem = new JMenuItem();
    private final JMenu editMenu = new JMenu();
    private final JMenu fileMenu = new JMenu();
    private final JMenu helpMenu = new JMenu();
    private final JMenuItem helpMenuItem = new JMenuItem();
    private final JMenuItem setupWizardMenuItem = new JMenuItem();
    private final JMenuItem importMenuItem = new JMenuItem();
    private final JCheckBoxMenuItem showStatusBarMenuItem = new JCheckBoxMenuItem();
    private final JMenu viewMenu = new JMenu();
    private final JMenuItem zoomInMenuItem = new JMenuItem();
    private final JMenuItem zoomOutMenuItem = new JMenuItem();
    private final JMenuItem zoomResetMenuItem = new JMenuItem();
    private final JMenuItem exitMenuItem = new JMenuItem() {
	@Override
	public boolean isVisible() {
	    return hostOS != HostOS.MACOS;
	}
    };
    private JMenuItem aboutMenuItem = new JMenuItem() {
	@Override
	public boolean isVisible() {
	    return hostOS != HostOS.MACOS;
	}
    };
    private final JMenuItem searchReplaceMenuItem = new JMenuItem() {
	@Override
	public boolean isEnabled() {
	    if (windowManager != null) {
		return (windowManager.getCurrentFrameComponent() instanceof ArbilSplitPanel);
	    } else {
		return false;
	    }
	}
    };

    public static class HashQueueTerminator {

	private final static int HASH_QUEUE_WAIT_TIME = 500;
	private final Object terminationLockObject = new Object();
	private final MimeHashQueue mimeHashQueue;
	private final WindowManager windowManager;
	private boolean terminationComplete = false;
	private JDialog messageDialogue;

	public HashQueueTerminator(MimeHashQueue mimeHashQueue, WindowManager windowManager) {
	    this.mimeHashQueue = mimeHashQueue;
	    this.windowManager = windowManager;
	}

	public void terminateHashQueue() {
	    startTerminatorThread();
	    waitForTerminatorThread();
	}

	private void startTerminatorThread() {
	    final Runnable terminator = new Runnable() {
		public void run() {
		    synchronized (terminationLockObject) {
			terminationComplete = false;
		    }

		    mimeHashQueue.terminateQueue();

		    synchronized (terminationLockObject) {
			terminationComplete = true;
			terminationLockObject.notifyAll();
		    }
		}
	    };
	    final Thread terminatorThread = new Thread(terminator);
	    terminatorThread.start();
	}

	private void waitForTerminatorThread() {
	    // Wait for termination to complete, if it takes too long show a popup
	    boolean messageDialogueShown = false;
	    while (!terminationComplete) {
		synchronized (terminationLockObject) {
		    try {
			logger.debug("Waiting for mime hash queue termination to finish");
			terminationLockObject.wait(HASH_QUEUE_WAIT_TIME);
			if (!terminationComplete && !messageDialogueShown) {
			    logger.info("Mime hash queue is busy while trying to exit application");
			    SwingUtilities.invokeLater(new Runnable() {
				public void run() {
				    messageDialogue = new JDialog(windowManager.getMainFrame(), services.getString("TYPECHECKING_IN_PROGRESS"));
				    final JLabel label = new JLabel(services.getString("FINISHING_TYPECHECKING"));
				    label.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
				    messageDialogue.getContentPane().add(label);
				    messageDialogue.setLocationRelativeTo(windowManager.getMainFrame());
				    messageDialogue.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
				    messageDialogue.pack();
				    messageDialogue.setVisible(true);
				}
			    });
			    messageDialogueShown = true;
			}
		    } catch (InterruptedException ex) {
			if (messageDialogue != null) {
			    messageDialogue.dispose();
			}
		    }
		}
	    }

	    // Termination complete, dispose dialogue if any
	    SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    if (messageDialogue != null) {
			messageDialogue.dispose();
		    }
		}
	    });
	}
    }
}
