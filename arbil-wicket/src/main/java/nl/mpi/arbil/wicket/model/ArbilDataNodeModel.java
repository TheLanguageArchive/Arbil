package nl.mpi.arbil.wicket.model;

import java.io.Serializable;
import java.net.URI;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.wicket.ArbilWicketSession;
import nl.mpi.arbil.data.ArbilNode;
import org.apache.wicket.model.LoadableDetachableModel;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilDataNodeModel extends LoadableDetachableModel<ArbilNode> {

    private URI uri;
    private ArbilNode serializableNode;

    public ArbilDataNodeModel(ArbilNode dataNode) {
	super(dataNode);
	if (dataNode instanceof Serializable) {
	    serializableNode = dataNode;
	} else if (dataNode instanceof ArbilDataNode) {
	    this.uri = ((ArbilDataNode) dataNode).getURI();
	}
    }

    public ArbilDataNodeModel(URI uri) {
	super();
	this.uri = uri;
    }

    @Override
    protected ArbilNode load() {
	if (serializableNode != null) {
	    return serializableNode;
	} else if (uri != null) {
	    return ArbilWicketSession.get().getDataNodeLoader().getArbilDataNode(null, uri);
	} else {
	    return null;
	}
    }

    public boolean waitTillLoaded() {
	if (getObject() instanceof ArbilDataNode) {
	    return ((ArbilDataNode) getObject()).waitTillLoaded();
	}
	return getObject() != null;
    }
}
