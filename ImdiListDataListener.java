package mpi.linorg;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * Document   : 
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */

// Updates the split panel when there are resources or loose files in the list to display
class ImdiListDataListener implements ListDataListener {

    private LinorgSplitPanel imdiSplitPanel;

    public ImdiListDataListener(LinorgSplitPanel localImdiSplitPanel) {
        imdiSplitPanel = localImdiSplitPanel;
    }

    public void contentsChanged(ListDataEvent e) {
        if (imdiSplitPanel != null) {
            imdiSplitPanel.setSplitDisplay();
        }
    }

    public void intervalAdded(ListDataEvent e) {
        if (imdiSplitPanel != null) {
            imdiSplitPanel.setSplitDisplay();
        }
    }

    public void intervalRemoved(ListDataEvent e) {
        if (imdiSplitPanel != null) {
            imdiSplitPanel.setSplitDisplay();
        }
    }
}
