package nl.mpi.arbil.wicket.components;

import org.apache.wicket.Resource;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class NodeIcon extends Panel {

    public NodeIcon(String id, Resource iconResource) {
	super(id);
	add(new Image("nodeIcon", iconResource));
    }
}
