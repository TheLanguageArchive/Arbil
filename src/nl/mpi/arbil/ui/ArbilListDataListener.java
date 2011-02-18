package nl.mpi.arbil.ui;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * Document   : ArbilListDataListener
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */

// Updates the split panel when there are resources or loose files in the list to display
public class ArbilListDataListener implements ListDataListener {

    private ArbilSplitPanel arbilSplitPanel;

    public ArbilListDataListener(ArbilSplitPanel localArbilSplitPanel) {
        arbilSplitPanel = localArbilSplitPanel;
    }

    public void contentsChanged(ListDataEvent e) {
        if (arbilSplitPanel != null) {
            arbilSplitPanel.setSplitDisplay();
        }
    }

    public void intervalAdded(ListDataEvent e) {
        if (arbilSplitPanel != null) {
            arbilSplitPanel.setSplitDisplay();
        }
    }

    public void intervalRemoved(ListDataEvent e) {
        if (arbilSplitPanel != null) {
            arbilSplitPanel.setSplitDisplay();
        }
    }
}
