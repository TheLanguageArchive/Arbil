/**
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
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
import nl.mpi.arbil.MacAdapter.MacAdapterException;
import nl.mpi.arbil.data.ArbilTreeHelper;
import nl.mpi.arbil.ui.ArbilTaskStatusBar;
import nl.mpi.arbil.ui.ArbilTreeController;
import nl.mpi.arbil.ui.ArbilTreePanels;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.ui.PreviewSplitPanel;
import nl.mpi.arbil.ui.menu.ArbilMenuBar;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.ArbilLogConfigurer;
import nl.mpi.arbil.util.ArbilMimeHashQueue;
import nl.mpi.arbil.util.AuthenticatorStub;
import nl.mpi.arbil.util.BugCatcherManager;
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

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
	final ArbilVersion arbilVersion = new ArbilVersion();
	final ArbilLogConfigurer logConfigurer = new ArbilLogConfigurer(arbilVersion);
	// See if a logging configuration has been specified manually
	if (System.getProperty("java.util.logging.config.file") == null) {
	    // No logging configured, use built in initial logging properties
	    logConfigurer.configureLoggingFromResource(ArbilMain.class, "/logging-initial.properties");
	}

	logger.info("Starting Arbil");

	System.setProperty("sun.swing.enableImprovedDragGesture", "true");
	System.setProperty("apple.awt.graphics.UseQuartz", "true");
	System.setProperty("apple.laf.useScreenMenuBar", "true");
	java.awt.EventQueue.invokeLater(new Runnable() {
	    public void run() {
		final ApplicationVersionManager versionManager = new ApplicationVersionManager(arbilVersion);
		try {
		    new ArbilMain(versionManager, logConfigurer).run();
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
    }

    public ArbilMain(ApplicationVersionManager versionManager, ArbilLogConfigurer logConfigurer) {
	this.versionManager = versionManager;
	this.logConfigurer = logConfigurer;

	final ArbilDesktopInjector injector = new ArbilDesktopInjector();
	injector.injectHandlers(versionManager, logConfigurer);

	this.treeHelper = injector.getTreeHelper();
	this.treeController = injector.getTreeController();
	this.windowManager = injector.getWindowManager();
	this.mimeHashQueue = injector.getMimeHashQueue();
    }

    public void run() {
	initApplication();
	initUI();
	checkFirstRun();
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

    private void initUI() {
	this.addWindowListener(new WindowAdapter() {
	    @Override
	    public void windowClosing(WindowEvent e) {
		arbilMenuBar.performCleanExit();
		//super.windowClosing(e);
	    }
	});

	initComponents();
	windowManager.addTaskListener(statusBar);
	PreviewSplitPanel previewSplitPanel = PreviewSplitPanel.getInstance();
	mainSplitPane.setRightComponent(previewSplitPanel);

	ArbilTreePanels arbilTreePanels = new ArbilTreePanels(treeHelper, treeController);
	mainSplitPane.setLeftComponent(arbilTreePanels);
	arbilMenuBar = new ArbilMenuBar(previewSplitPanel, null, logConfigurer);
	setJMenuBar(arbilMenuBar);

	mainSplitPane.setDividerLocation(0.25);

	windowManager.loadGuiState(this, statusBar);
	setTitle(versionManager.getApplicationVersion().applicationTitle + " " + versionManager.getApplicationVersion().compileDate);
	setIconImage(ArbilIcons.getSingleInstance().linorgIcon.getImage());
	// load the templates and populate the templates menu
	setVisible(true);

	if (arbilMenuBar.checkNewVersionAtStartCheckBoxMenuItem.isSelected()) {
	    versionManager.checkForUpdate();
	}

	initMacHandlers();
    }

    private void initMacHandlers() {
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
		// Successfully set handlers, now remove redundant options from menu bar
		arbilMenuBar.setMacOsMenu(true);
	    }
	} catch (MacAdapterException exception) {
	    System.err.println("Could not configure MacOS application handlers");
	    if (exception != null) {
		System.err.println(exception);
	    }

	}
    }

    private void checkFirstRun() {
	windowManager.showSetupWizardIfFirstRun();
	windowManager.openIntroductionPage();
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
