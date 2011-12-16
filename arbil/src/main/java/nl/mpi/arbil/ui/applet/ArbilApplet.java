package nl.mpi.arbil.ui.applet;

import javax.swing.SwingUtilities;
import nl.mpi.arbil.ArbilDesktopInjector;
import nl.mpi.arbil.ui.menu.ArbilMenuBar;
import nl.mpi.arbil.ui.ArbilTreePanels;
import nl.mpi.arbil.util.ArbilBugCatcher;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.ui.PreviewSplitPanel;
import nl.mpi.arbil.util.BugCatcher;

/*
 * ArbilApplet.java
 * Created on 8 July 2009, 14:03
 * @author Peter.Withers@mpi.nl
 */
public class ArbilApplet extends javax.swing.JApplet {

    private BugCatcher bugCatcher;

    @Override
    public void init() {
	// TODO: test if this suffices
	new ArbilDesktopInjector().injectHandlers();
	bugCatcher = new ArbilBugCatcher();
	//System.setProperty("sun.swing.enableImprovedDragGesture", "true");
	try {
	    SwingUtilities.invokeAndWait(new Runnable() {

		public void run() {
		    ArbilDesktopInjector injector = new ArbilDesktopInjector();
		    injector.injectHandlers();
		    mainSplitPane = new javax.swing.JSplitPane();
		    getContentPane().add(mainSplitPane, java.awt.BorderLayout.CENTER);
		    previewSplitPanel = PreviewSplitPanel.getInstance();
		    mainSplitPane.setRightComponent(previewSplitPanel);
		    arbilTreePanels = new ArbilTreePanels(injector.getTreeHelper());
		    mainSplitPane.setLeftComponent(arbilTreePanels);
		    previewSplitPanel.setPreviewPanel(true);
		    ArbilMenuBar arbilMenuBar = new ArbilMenuBar(previewSplitPanel, ArbilApplet.this, injector.getTreeHelper());
		    setJMenuBar(arbilMenuBar);
//                  LinorgWindowManager.getSingleInstance().setComponents(this);
		    ArbilWindowManager.getSingleInstance().openIntroductionPage();
		    arbilTreePanels.setDefaultTreePaneSize();
		    previewSplitPanel.setDividerLocation(0.3);
		}
	    });
	} catch (Exception ex) {
	    new ArbilBugCatcher().logError(ex);
	}
    }

    public void start() {
    }
    private javax.swing.JSplitPane mainSplitPane;
    private nl.mpi.arbil.ui.ArbilTreePanels arbilTreePanels;
    private nl.mpi.arbil.ui.PreviewSplitPanel previewSplitPanel;
}
