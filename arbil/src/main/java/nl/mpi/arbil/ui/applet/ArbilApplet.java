package nl.mpi.arbil.ui.applet;

import javax.swing.SwingUtilities;
import nl.mpi.arbil.ArbilDesktopInjector;
import nl.mpi.arbil.ui.menu.ArbilMenuBar;
import nl.mpi.arbil.ui.ArbilTreePanels;
import nl.mpi.arbil.ui.PreviewSplitPanel;
import nl.mpi.arbil.util.BugCatcherManager;

/*
 * ArbilApplet.java
 * Created on 8 July 2009, 14:03
 * @author Peter.Withers@mpi.nl
 */
public class ArbilApplet extends javax.swing.JApplet {

    @Override
    public void init() {
	final ArbilDesktopInjector injector = new ArbilDesktopInjector();
	// TODO: test if this suffices
	injector.injectHandlers();
	//System.setProperty("sun.swing.enableImprovedDragGesture", "true");
	try {
	    SwingUtilities.invokeAndWait(new Runnable() {

		public void run() {
		    //injector.injectHandlers();
		    mainSplitPane = new javax.swing.JSplitPane();
		    getContentPane().add(mainSplitPane, java.awt.BorderLayout.CENTER);
		    previewSplitPanel = PreviewSplitPanel.getInstance();
		    mainSplitPane.setRightComponent(previewSplitPanel);
		    arbilTreePanels = new ArbilTreePanels(injector.getTreeHelper());
		    mainSplitPane.setLeftComponent(arbilTreePanels);
		    previewSplitPanel.setPreviewPanel(true);
		    ArbilMenuBar arbilMenuBar = new ArbilMenuBar(previewSplitPanel, ArbilApplet.this);
		    setJMenuBar(arbilMenuBar);
//                  LinorgWindowManager.getSingleInstance().setComponents(this);
		    injector.getWindowManager().openIntroductionPage();
		    arbilTreePanels.setDefaultTreePaneSize();
		    previewSplitPanel.setDividerLocation(0.3);
		}
	    });
	} catch (Exception ex) {
	    BugCatcherManager.getBugCatcher().logError(ex);
	}
    }

    public void start() {
    }
    private javax.swing.JSplitPane mainSplitPane;
    private nl.mpi.arbil.ui.ArbilTreePanels arbilTreePanels;
    private nl.mpi.arbil.ui.PreviewSplitPanel previewSplitPanel;
}
