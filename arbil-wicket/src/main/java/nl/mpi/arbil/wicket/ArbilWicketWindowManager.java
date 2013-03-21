package nl.mpi.arbil.wicket;

import java.net.URI;
import java.net.URL;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.ProgressMonitor;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.ui.AbstractArbilTableModel;
import nl.mpi.arbil.util.WindowManager;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilWicketWindowManager implements WindowManager {

    public JFrame getMainFrame() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public void closeAllWindows() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public void openFloatingTable(ArbilDataNode[] rowNodesArray, String frameTitle) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public void openFloatingTableOnce(URI[] rowNodesArray, String frameTitle) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public void openFloatingTableOnce(ArbilDataNode[] rowNodesArray, String frameTitle) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public void openSearchTable(ArbilNode[] selectedNodes, String frameTitle) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public void openFloatingSubnodesWindows(ArbilDataNode[] arbilDataNodes) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public void openUrlWindowOnce(String frameTitle, URL locationUrl) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public ProgressMonitor newProgressMonitor(Object message, String note, int min, int max) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public AbstractArbilTableModel openFloatingTableOnceGetModel(URI[] rowNodesArray, String frameTitle) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public AbstractArbilTableModel openAllChildNodesInFloatingTableOnce(URI[] rowNodesArray, String frameTitle) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public void saveWindowStates() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean openFileInExternalApplication(URI targetUri) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public void openImdiXmlWindow(Object userObject, boolean formatXml, boolean launchInBrowser) {
	throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
