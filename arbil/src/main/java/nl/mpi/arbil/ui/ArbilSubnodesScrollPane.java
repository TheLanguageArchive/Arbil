/**
 * Copyright (C) 2016 The Language Archive, Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
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

    public ArbilSubnodesScrollPane(ArbilDataNode dataNode, TableController tableController, ImageBoxRenderer imageBoxRenderer) {
        this(new ArbilSubnodesPanel(dataNode, tableController, imageBoxRenderer));
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
