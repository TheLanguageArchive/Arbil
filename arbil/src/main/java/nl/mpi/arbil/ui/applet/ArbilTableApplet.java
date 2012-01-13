package nl.mpi.arbil.ui.applet;

import java.net.URI;
import nl.mpi.arbil.ArbilDesktopInjector;
import nl.mpi.arbil.ui.ArbilTable;
import nl.mpi.arbil.ui.ArbilTableModel;
import nl.mpi.arbil.ui.ArbilSplitPanel;
import nl.mpi.arbil.data.DataNodeLoader;
import nl.mpi.arbil.util.BugCatcher;

/*
 * ArbilTableApplet.java
 * Created on 28 September 2009, 13:10
 * @author Peter.Withers@mpi.nl
 */
public class ArbilTableApplet extends javax.swing.JApplet {

    private BugCatcher bugCatcher;
    private DataNodeLoader dataNodeLoader;

    @Override
    public void init() {
	final ArbilDesktopInjector injector = new ArbilDesktopInjector();
	// TODO: test if this suffices
	injector.injectHandlers();
	bugCatcher = injector.getBugCatcher();
	dataNodeLoader = injector.getDataNodeLoader();
	try {
	    java.awt.EventQueue.invokeAndWait(new Runnable() {

		public void run() {
		    initComponents();
		    addNodesToTable(getParameter("ImdiFileList"));
		    addShowOnlyColumnsToTable(getParameter("ShowOnlyColumns"));
		    addChildNodesToTable(getParameter("ChildNodeColumns"));
		    addHighlightToTable(getParameter("HighlightText"));
		}
	    });
	} catch (Exception ex) {
	    injector.getBugCatcher().logError(ex);
	}
    }

    private void addNodesToTable(String nodeURLsString) {
	if (nodeURLsString != null) {
	    for (String currentUrlString : nodeURLsString.split(",")) {
		try {
		    arbilTableModel.addSingleArbilDataNode(dataNodeLoader.getArbilDataNode(rootPane, new URI(currentUrlString)));
		} catch (Exception ex) {
		    bugCatcher.logError(ex);
		}
	    }
	}
    }

    private void addShowOnlyColumnsToTable(String showColumnsString) {
	if (showColumnsString != null && showColumnsString.trim().length() > 0) {
	    for (String currentshowColumns : showColumnsString.split(",")) {
		arbilTableModel.getFieldView().addShowOnlyColumn(currentshowColumns.trim());
	    }
	}
    }

    private void addChildNodesToTable(String childNodesString) {
	if (childNodesString != null && childNodesString.trim().length() > 0) {
	    for (String currentChildNode : childNodesString.split(",")) {
		arbilTableModel.addChildTypeToDisplay(currentChildNode.trim());
	    }
	}
    }

    private void addHighlightToTable(String highlightableTextString) {
	if (highlightableTextString != null && highlightableTextString.length() > 0) {
	    for (String highlightText : highlightableTextString.split(",")) {
		arbilTableModel.highlightMatchingText(highlightText);
	    }
	}
    }

    private void initComponents() {
	arbilTableModel = new ArbilTableModel();
	ArbilTable arbilTable = new ArbilTable(arbilTableModel, tableTitle);
	ArbilSplitPanel arbilSplitPanel = new ArbilSplitPanel(arbilTable);
	arbilTableModel.hideContextMenuAndStatusBar = true;
	arbilSplitPanel.setSplitDisplay();
	getContentPane().add(arbilSplitPanel, java.awt.BorderLayout.CENTER);
    }
    private String tableTitle = "Arbil Table Demo";
    private ArbilTableModel arbilTableModel;
}
