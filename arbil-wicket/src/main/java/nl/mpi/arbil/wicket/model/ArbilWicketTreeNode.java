package nl.mpi.arbil.wicket.model;

import java.io.Serializable;
import java.net.URI;
import javax.swing.tree.DefaultMutableTreeNode;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.data.ArbilNode;
import org.apache.wicket.model.IDetachable;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilWicketTreeNode extends DefaultMutableTreeNode implements IDetachable, Serializable {

    private URI uri;
    private transient ArbilDataNode dataNode;
    
    public ArbilWicketTreeNode(ArbilDataNode dataNode) {
    this.dataNode = dataNode;
	this.uri = dataNode.getURI();
	setUserObject(dataNode);
    }

    public ArbilWicketTreeNode(URI uri) {
	this.uri = uri;
    }

    public synchronized ArbilNode getDataNode() {
	loadDataNode();
	return dataNode;
    }

    private void loadDataNode() {
	if (dataNode == null) {
	    assert (uri != null);
	    dataNode = ArbilDataNodeLoader.getSingleInstance().getArbilDataNode(null, uri);
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
