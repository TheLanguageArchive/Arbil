package nl.mpi.arbil.ui;

import javax.swing.JScrollPane;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import nl.mpi.arbil.data.ArbilDataNode;

/**
 * Scroll pane wrapper for ArbilSubnodesPanel. To be used as top level
 * container in a subnodes window
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilSubnodesScrollPane extends JScrollPane implements ArbilWindowComponent {

    private ArbilSubnodesPanel panel;

    public ArbilSubnodesScrollPane(ArbilDataNode dataNode) {
        this(new ArbilSubnodesPanel(dataNode));
    }

    public ArbilSubnodesScrollPane(ArbilSubnodesPanel panel) {
        super(panel);
        this.panel = panel;
    }

    public ArbilDataNode getDataNode() {
        return panel.getDataNode();
    }

    public void arbilWindowClosed() {
        panel.clear();
    }

    /**
     *
     * @return InternalFrameListener for the frame that contains this scroll pane.
     * Stops all editing when frame is deactivated.
     */
    public InternalFrameListener getInternalFrameListener() {
        return new InternalFrameAdapter() {

            @Override
            public void internalFrameDeactivated(InternalFrameEvent e) {
                panel.stopAllEditing();
            }
        };
    }
}
