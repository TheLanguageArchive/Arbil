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
package nl.mpi.arbil.ui.applet;

import javax.swing.JApplet;
import nl.mpi.arbil.ArbilDesktopInjector;
import nl.mpi.arbil.ArbilVersion;
import nl.mpi.arbil.data.DataNodeLoader;
import nl.mpi.arbil.ui.ArbilTreePanels;
import nl.mpi.arbil.ui.PreviewSplitPanel;
import nl.mpi.arbil.ui.menu.ArbilMenuBar;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.ArbilLogConfigurer;
import nl.mpi.arbil.util.ArbilMimeHashQueue;
import nl.mpi.arbil.util.TreeHelper;

/*
 * ArbilApplet2.java
 * Created on 1 September 2010, 17:23
 * @author Peter.Withers@mpi.nl
 */
public class ArbilApplet2 extends JApplet {

    private javax.swing.JSplitPane mainSplitPane;
    private ArbilMenuBar arbilMenuBar;
    private DataNodeLoader dataNodeLoader;
    private final ArbilVersion arbilVersion = new ArbilVersion();
    private final ApplicationVersionManager versionManager = new ApplicationVersionManager(arbilVersion);
    private final ArbilLogConfigurer logManager = new ArbilLogConfigurer(arbilVersion, "arbil-applet-log");

    private void initComponents(ArbilDesktopInjector injector) {
	dataNodeLoader = injector.getDataNodeLoader();
	mainSplitPane = new javax.swing.JSplitPane();
	mainSplitPane.setName("mainSplitPane");
	PreviewSplitPanel previewSplitPanel = new PreviewSplitPanel(injector.getWindowManager(), injector.getTableController());
	ArbilTreePanels arbilTreePanels = new ArbilTreePanels(injector.getTreeHelper(), injector.getTreeController(), previewSplitPanel, null);
	mainSplitPane.setLeftComponent(arbilTreePanels);
	mainSplitPane.setRightComponent(previewSplitPanel);
	arbilMenuBar = new ArbilMenuBar(previewSplitPanel, this, logManager);
	ArbilMimeHashQueue.setAllowCookies(true);
	add(mainSplitPane, java.awt.BorderLayout.CENTER);
	setJMenuBar(arbilMenuBar);

//        mainSplitPane.setDividerLocation(100);
	mainSplitPane.setDividerSize(5);
	mainSplitPane.setDividerLocation(0.25);

//        LinorgWindowManager.getSingleInstance().loadGuiState(this);
//        setTitle(new LinorgVersion().applicationTitle + " " + new LinorgVersion().compileDate);
//        setIconImage(ImdiIcons.getSingleInstance().linorgIcon.getImage());
	// load the templates and populate the templates menu
	setVisible(true);
	injector.getWindowManager().openIntroductionPage();

//        if (arbilMenuBar.checkNewVersionAtStartCheckBoxMenuItem.isSelected()) {
//            new LinorgVersionChecker().checkForUpdate();
//        }
    }

    private void initApplication(TreeHelper treeHelper, ArbilMimeHashQueue hashQueue) {
	treeHelper.init();
	hashQueue.init();
    }

    /**
     * Initialization method that will be called after the applet is loaded
     * into the browser.
     */
    public void init() {
	// TODO start asynchronous download of heavy resources
	try {
	    javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
		public void run() {
		    ArbilDesktopInjector injector = new ArbilDesktopInjector();
		    injector.injectHandlers(new ArbilSessionStorage(), versionManager, logManager);
		    System.setProperty("sun.swing.enableImprovedDragGesture", "true");
		    System.setProperty("apple.awt.graphics.UseQuartz", "true");
		    System.setProperty("apple.laf.useScreenMenuBar", "true");
		    initApplication(injector.getTreeHelper(), injector.getMimeHashQueue());
		    initComponents(injector);
		}
	    });
	} catch (Exception e) {
	    System.err.println("init didn't successfully complete");
	}
    }
    // TODO overwrite start(), stop() and destroy() methods

    public void start() {
	try {
	    javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
		public void run() {
		    dataNodeLoader.startLoaderThreads();
		}
	    });
	} catch (Exception e) {
	    System.err.println("start didn't successfully complete");
	}
    }

    public void stop() {
	// it would seem that any dialogue box on applet stop will kill the web browser in a very bad way
	//arbilMenuBar.performCleanExit();
	//arbilMenuBar.saveApplicationState();
    }
}
