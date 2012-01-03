package nl.mpi.arbil;

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
public class MockWindowManager implements WindowManager {

    public JFrame getMainFrame() {
	return null;
    }

    public void closeAllWindows() {
    }

    public void openFloatingTable(ArbilDataNode[] rowNodesArray, String frameTitle) {
    }

    public void openFloatingTableOnce(URI[] rowNodesArray, String frameTitle) {
    }

    public void openFloatingTableOnce(ArbilDataNode[] rowNodesArray, String frameTitle) {
    }

    public void openSearchTable(ArbilNode[] selectedNodes, String frameTitle) {
    }

    public void openFloatingSubnodesWindows(ArbilDataNode[] arbilDataNodes) {
    }

    public JEditorPane openUrlWindowOnce(String frameTitle, URL locationUrl) {
	return null;
    }

    public ProgressMonitor newProgressMonitor(Object message, String note, int min, int max) {
	return null;
    }

    public AbstractArbilTableModel openFloatingTableOnceGetModel(URI[] rowNodesArray, String frameTitle) {
	return null;
    }

    public AbstractArbilTableModel openAllChildNodesInFloatingTableOnce(URI[] rowNodesArray, String frameTitle) {
	return null;
    }

    public void saveWindowStates() {
    }

    public boolean openFileInExternalApplication(URI targetUri) {
	return true;
    }

    public void openImdiXmlWindow(Object userObject, boolean formatXml, boolean launchInBrowser) {
    }
    
}
