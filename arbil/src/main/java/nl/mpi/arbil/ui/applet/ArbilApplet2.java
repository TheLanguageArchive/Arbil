package nl.mpi.arbil.ui.applet;

import javax.swing.JApplet;
import nl.mpi.arbil.ArbilDesktopInjector;
import nl.mpi.arbil.ArbilVersion;
import nl.mpi.arbil.ui.menu.ArbilMenuBar;
import nl.mpi.arbil.ui.ArbilTreePanels;
import nl.mpi.arbil.ui.PreviewSplitPanel;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.data.DataNodeLoader;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.ArbilMimeHashQueue;

/*
 * ArbilApplet2.java
 * Created on 1 September 2010, 17:23
 * @author Peter.Withers@mpi.nl
 */
public class ArbilApplet2 extends JApplet {

    private javax.swing.JSplitPane mainSplitPane;
    private ArbilMenuBar arbilMenuBar;
    private DataNodeLoader dataNodeLoader;

    private void initComponents() {

	ArbilDesktopInjector injector = new ArbilDesktopInjector();
	injector.injectHandlers();
	dataNodeLoader = injector.getDataNodeLoader();
	mainSplitPane = new javax.swing.JSplitPane();
	mainSplitPane.setName("mainSplitPane");
	PreviewSplitPanel previewSplitPanel = PreviewSplitPanel.getInstance();
	ArbilTreePanels arbilTreePanels = new ArbilTreePanels(injector.getTreeHelper());
	mainSplitPane.setLeftComponent(arbilTreePanels);
	mainSplitPane.setRightComponent(previewSplitPanel);
	arbilMenuBar = new ArbilMenuBar(previewSplitPanel, this);
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

    /**
     * Initialization method that will be called after the applet is loaded
     * into the browser.
     */
    public void init() {
	// TODO start asynchronous download of heavy resources
	try {
	    javax.swing.SwingUtilities.invokeAndWait(new Runnable() {

		public void run() {
		    final ApplicationVersionManager versionManager = new ApplicationVersionManager(new ArbilVersion());
		    new ArbilDesktopInjector().injectHandlers();
		    System.setProperty("sun.swing.enableImprovedDragGesture", "true");
		    System.setProperty("apple.awt.graphics.UseQuartz", "true");
		    System.setProperty("apple.laf.useScreenMenuBar", "true");
		    initComponents();
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
