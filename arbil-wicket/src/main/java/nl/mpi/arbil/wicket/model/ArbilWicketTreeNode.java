package nl.mpi.arbil.wicket.model;

import java.io.Serializable;
import java.net.URI;
import javax.swing.tree.DefaultMutableTreeNode;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.wicket.ArbilWicketSession;
import org.apache.wicket.model.IDetachable;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilWicketTreeNode extends DefaultMutableTreeNode implements IDetachable, Serializable {

    private URI uri;
    private ArbilNode serializableNode;
    private transient ArbilNode dataNode;

    public ArbilWicketTreeNode(ArbilNode dataNode) {
	this.dataNode = dataNode;
	if (dataNode instanceof Serializable) {
	    serializableNode = dataNode;
	} else {
	    if (dataNode instanceof ArbilDataNode) {
		this.uri = ((ArbilDataNode) dataNode).getURI();
	    }
	}
	setUserObject(dataNode);
    }

    public ArbilWicketTreeNode(URI uri) {
	this.uri = uri;
    }

    public synchronized ArbilNode getDataNode() {
	loadDataNode();
	return dataNode;
    }

    private synchronized void loadDataNode() {
	if (dataNode == null) {
	    if (serializableNode != null) {
		dataNode = serializableNode;
	    } else {
		if (uri != null) {
		    dataNode = ArbilWicketSession.get().getDataNodeLoader().getArbilDataNode(null, uri);
		}
	    }
	    setUserObject(dataNode);
	}
    }

    @Override
    public Object getUserObject() {
	return getDataNode();
    }

    @Override
    public boolean isLeaf() {
	return getDataNode().getChildCount() <= 0;
    }

    public void detach() {
	dataNode = null;
	userObject = null;
    }

    @Override
    public String toString() {
	return getDataNode().toString();
    }
}
