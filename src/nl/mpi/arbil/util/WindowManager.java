package nl.mpi.arbil.util;

import java.net.URI;
import java.net.URL;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.ProgressMonitor;
import nl.mpi.arbil.data.ArbilDataNode;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface WindowManager {

    JFrame getMainFrame();

    void closeAllWindows();
    
    void openFloatingTable(ArbilDataNode[] rowNodesArray, String frameTitle);

    void openFloatingTableOnce(URI[] rowNodesArray, String frameTitle);

    void openFloatingTableOnce(ArbilDataNode[] rowNodesArray, String frameTitle);

    void openSearchTable(ArbilDataNode[] selectedNodes, String frameTitle);
    
    void openFloatingSubnodesWindows(ArbilDataNode[] arbilDataNodes);

    JEditorPane openUrlWindowOnce(String frameTitle, URL locationUrl);

    ProgressMonitor newProgressMonitor(Object message, String note, int min, int max);

    /* Methods below are implemented by ArbilWindowManager and may be relevant
     * but currently are not used through the interface so they are not included
     * so as not to put any additional burden on potential other implementers
     */

    //JInternalFrame createWindow(String windowTitle, Component contentsComponent);

    //JInternalFrame focusWindow(String windowName);

    //ArbilTableModel openAllChildNodesInFloatingTableOnce(URI[] rowNodesArray, String frameTitle);

    //ArbilTableModel openFloatingTable(ArbilDataNode[] rowNodesArray, String frameTitle);

    //void saveWindowStates();

    //void stopEditingInCurrentWindow();

}
