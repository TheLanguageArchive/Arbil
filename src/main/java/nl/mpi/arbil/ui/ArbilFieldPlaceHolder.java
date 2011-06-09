package nl.mpi.arbil.ui;

import java.io.Serializable;
import java.net.URI;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeLoader;

/**
 * Used as blank in horizontal tables (grid view) in cells that represent
 * fields that are not present in the node represented by their row, i.e. the
 * 'grey fields'.
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilFieldPlaceHolder implements Serializable {

    private String fieldName;
    private transient ArbilDataNode arbilDataNode;
    private URI arbilDataNodeURI = null;

    public ArbilFieldPlaceHolder(String fieldName, ArbilDataNode dataNode) {
	this.fieldName = fieldName;
	this.arbilDataNode = dataNode;
	if (dataNode != null) {
	    this.arbilDataNodeURI = arbilDataNode.getURI();
	}
    }

    public String getFieldName() {
	return fieldName;
    }

    public ArbilDataNode getArbilDataNode() {
	if (arbilDataNode == null && arbilDataNodeURI != null) {
	    arbilDataNode = ArbilDataNodeLoader.getSingleInstance().getArbilDataNode(null, arbilDataNodeURI);
	}
	return arbilDataNode;
    }

    @Override
    public String toString() {
	return "";
    }
}
