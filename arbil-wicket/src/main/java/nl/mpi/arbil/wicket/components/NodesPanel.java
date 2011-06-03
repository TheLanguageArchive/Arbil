package nl.mpi.arbil.wicket.components;

import nl.mpi.arbil.data.ArbilDataNode;
import org.apache.wicket.markup.html.panel.Panel;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class NodesPanel extends Panel {

    public NodesPanel(String id, ArbilDataNode[] dataNodes) {
	super(id);
	add(new ArbilTable("datatable", dataNodes));
    }
}
