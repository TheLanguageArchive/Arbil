package nl.mpi.arbil.wicket.components;

import nl.mpi.arbil.data.ArbilNode;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class NodePanel extends Panel {

    public NodePanel(String id, final IModel<ArbilNode> model) {
	super(id, model);
	add(new NodeIcon("icon", model.getObject().getIcon().getImage()));
	add(new Label("name", new Model<String>() {

	    @Override
	    public String getObject() {
		return model.getObject().toString();
	    }
	}));
    }
}
