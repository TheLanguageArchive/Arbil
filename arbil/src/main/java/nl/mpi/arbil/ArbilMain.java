/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.arbil;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ResourceBundle;
import nl.mpi.arbil.MacAdapter.MacAdapterException;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.data.ArbilTreeHelper;
import nl.mpi.arbil.data.metadatafile.MetadataReader;
import nl.mpi.arbil.templates.ArbilTemplateManager;
import nl.mpi.arbil.ui.ArbilDragDrop;
import nl.mpi.arbil.ui.ArbilTableController;
import nl.mpi.arbil.ui.ArbilTaskStatusBar;
import nl.mpi.arbil.ui.ArbilTreeController;
import nl.mpi.arbil.ui.ArbilTreePanels;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.ui.PreviewSplitPanel;
import nl.mpi.arbil.ui.menu.ArbilMenuBar;
import nl.mpi.arbil.ui.menu.ArbilMenuBar.HostOS;
import nl.mpi.arbil.userstorage.ArbilConfiguration;
import nl.mpi.arbil.userstorage.ArbilConfigurationManager;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.ArbilLogConfigurer;
import nl.mpi.arbil.util.ArbilMimeHashQueue;
import nl.mpi.arbil.util.AuthenticatorStub;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbilcommons.ui.LocalisationSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * ArbilMain.java
 * This version uses only a JFrame and does not require additional dependencies
 * Created on 23 September 2008, 17:23
 * @author Peter.Withers@mpi.nl
 * @author Twan.Goosen@mpi.nl
 */
public class ArbilMain extends javax.swing.JFrame {

    private final static Logger logger = LoggerFactory.getLogger(ArbilMain.class);
    private javax.swing.JSplitPane mainSplitPane;
    private ArbilMenuBar arbilMenuBar;
    private ArbilTaskStatusBar statusBar;
    private final ArbilTreeHelper treeHelper;
    private final ArbilTreeController treeController;
    private final ArbilWindowManager windowManager;
    private final ApplicationVersionManager versionManager;
    private final ArbilMimeHashQueue mimeHashQueue;
    private final ArbilLogConfigurer logConfigurer;
    private final ArbilTableController tableController;
    private final ArbilSessionStorage sessionStorage;
    private final ArbilDataNodeLoader dataNodeLoader;

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
	final ArbilVersion arbilVersion = new ArbilVersion();
	final ArbilLogConfigurer logConfigurer = new ArbilLogConfigurer(arbilVersion, "arbil-log-");
	// See if a logging configuration has been specified manually
	if (System.getProperty("java.util.logging.config.file") == null) {
	    // No logging configured, use built in initial logging properties
	    logConfigurer.configureLoggingFromResource(ArbilMain.class, "/logging-initial.properties");
	}

	logger.info("Starting Arbil");
	final ApplicationVersionManager versionManager = new ApplicationVersionManager(arbilVersion);
	final ArbilSessionStorage sessionStorage = new ArbilSessionStorage();

	System.setProperty("sun.swing.enableImprovedDragGesture", "true");
	System.setProperty("apple.awt.graphics.UseQuartz", "true");
	System.setProperty("apple.laf.useScreenMenuBar", "true");
	java.awt.EventQueue.invokeLater(new Runnable() {
	    public void run() {
		// setting version manager on icon because it may be needed for localisation popup
		ArbilIcons.setVersionManager(versionManager);
		// set localisation from stored setting as early as possible because some initialisations may be locale sensitive
		checkLocalisation(sessionStorage);

		try {
		    new ArbilMain(sessionStorage, versionManager, logConfigurer).run();
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
    }

    public ArbilMain(ArbilSessionStorage sessionStorage, ApplicationVersionManager versionManager, ArbilLogConfigurer logConfigurer) {
	this.versionManager = versionManager;
	this.logConfigurer = logConfigurer;
	this.sessionStorage = sessionStorage;

	final ArbilDesktopInjector injector = new ArbilDesktopInjector();
	injector.injectHandlers(sessionStorage, versionManager, logConfigurer);

	this.treeHelper = injector.getTreeHelper();
	this.treeController = injector.getTreeController();
	this.windowManager = injector.getWindowManager();
	this.mimeHashQueue = injector.getMimeHashQueue();
	this.tableController = injector.getTableController();
	this.dataNodeLoader = injector.getDataNodeLoader();
    }

    public void run() {
	final ArbilConfiguration config = readConfig();
	initApplication();
	initUI(config);
	checkFirstRun();
    }

    private ArbilConfiguration readConfig() {
	ArbilConfigurationManager configManager = new ArbilConfigurationManager(sessionStorage);
	ArbilConfiguration appConfig = configManager.read();

	// Set shared config on using objects
	ArbilTemplateManager.getSingleInstance().setApplicationConfiguration(appConfig);
	MetadataReader.getSingleInstance().setApplicationConfiguration(appConfig);
	return appConfig;
    }

    private void initApplication() {

	try {
	    java.net.Authenticator.setDefault(new AuthenticatorStub(windowManager));
	} catch (SecurityException sEx) {
	    BugCatcherManager.getBugCatcher().logError("Failed to set custom Authenticator. Default authentication dialogs may appear.", sEx);
	}

	treeHelper.init();
	mimeHashQueue.init();
    }

    private void initUI(ArbilConfiguration appConfig) {
	this.addWindowListener(new WindowAdapter() {
	    @Override
	    public void windowClosing(WindowEvent e) {
		arbilMenuBar.performCleanExit();
		//super.windowClosing(e);
	    }
	});

	final boolean macHandlersInitialized = initMacHandlers();

	initComponents();
	windowManager.addTaskListener(statusBar);
	PreviewSplitPanel previewSplitPanel = new PreviewSplitPanel(windowManager, tableController);
	mainSplitPane.setRightComponent(previewSplitPanel);

	final ArbilDragDrop dragDrop = new ArbilDragDrop(sessionStorage, treeHelper, windowManager, windowManager, tableController);
	windowManager.setDragDrop(dragDrop);

	final ArbilTreePanels arbilTreePanels = new ArbilTreePanels(treeHelper, treeController, previewSplitPanel, dragDrop);
	mainSplitPane.setLeftComponent(arbilTreePanels);

	final HostOS hostOs = macHandlersInitialized ? HostOS.MACOS : HostOS.OTHER;
	arbilMenuBar = new ArbilMenuBar(appConfig, sessionStorage, windowManager, windowManager, treeHelper, dataNodeLoader, mimeHashQueue, versionManager, logConfigurer, null, previewSplitPanel, hostOs);
	setJMenuBar(arbilMenuBar);

	mainSplitPane.setDividerLocation(0.25);

	windowManager.loadGuiState(this, statusBar);
	setTitle(versionManager.getApplicationVersion().applicationTitle + " " + versionManager.getApplicationVersion().compileDate);
	setIconImage(ArbilIcons.getSingleInstance().linorgIcon.getImage());
	// load the templates and populate the templates menu
	setVisible(true);
        
	if (sessionStorage.loadBoolean("checkNewVersionAtStart", true)) {
	    versionManager.checkForUpdate();
	}


    }

    private boolean initMacHandlers() {
	final MacAdapter macAdapter = new MacAdapter() {
	    @Override
	    protected boolean performApplicationExit() {
		return arbilMenuBar.performCleanExit();
	    }

	    @Override
	    protected void performApplicationAbout() {
		windowManager.openAboutPage();
	    }
	};

	try {
	    if (macAdapter.initMacApplicationHandlers()) {
		logger.debug("Mac handlers succesfully congifured");
		return true;
	    }
	} catch (MacAdapterException exception) {
	    logger.error("Could not configure MacOS application handlers", exception);
	}
	return false;
    }

    private void checkFirstRun() {
	windowManager.showSetupWizardIfFirstRun();
	windowManager.openIntroductionPage();
    }

    private static void checkLocalisation(SessionStorage sessionStorage) {
	final String availableLanguages = ResourceBundle.getBundle("nl/mpi/arbil/localisation/AvailableLanguages").getString("LANGUAGE CODES");
	final LocalisationSelector localisationSelector = new LocalisationSelector(sessionStorage, availableLanguages.split(","));
	if (!localisationSelector.hasSavedLocal()) {
	    final String please_select_your_preferred_language = ResourceBundle.getBundle("nl/mpi/arbil/localisation/Menus").getString("PLEASE SELECT YOUR PREFERRED LANGUAGE");
	    final String language_Selection = ResourceBundle.getBundle("nl/mpi/arbil/localisation/Menus").getString("LANGUAGE SELECTION");
	    final String system_Default = ResourceBundle.getBundle("nl/mpi/arbil/localisation/Menus").getString("SYSTEM DEFAULT");
	    localisationSelector.askUser(null, ArbilIcons.getSingleInstance().linorgIcon, please_select_your_preferred_language, language_Selection, system_Default);
	}
	localisationSelector.setLanguageFromSaved();
    }

    private void initComponents() {

	mainSplitPane = new javax.swing.JSplitPane();
	statusBar = new ArbilTaskStatusBar();

	setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
	setTitle("Arbil");

	mainSplitPane.setDividerLocation(100);
	mainSplitPane.setDividerSize(5);
	mainSplitPane.setName("mainSplitPane");
	getContentPane().setLayout(new BorderLayout());
	getContentPane().add(mainSplitPane, java.awt.BorderLayout.CENTER);
	getContentPane().add(statusBar, BorderLayout.SOUTH);

	pack();
    }
}
