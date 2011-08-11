package nl.mpi.arbil.ui;

import java.io.Serializable;
import java.net.URI;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.DataNodeLoader;
import nl.mpi.arbil.data.ArbilFieldsNode;

/**
 * Used as blank in horizontal tables (grid view) in cells that represent
 * fields that are not present in the node represented by their row, i.e. the
 * 'grey fields'.
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilFieldPlaceHolder implements Serializable {

    private String fieldName;
    private transient ArbilFieldsNode arbilFieldsNode;
    private ArbilFieldsNode serializableNode;
    private URI arbilDataNodeURI = null;
    private static DataNodeLoader dataNodeLoader;

    public static void setDataNodeLoader(DataNodeLoader dataNodeLoaderInstance) {
	dataNodeLoader = dataNodeLoaderInstance;
    }

    public ArbilFieldPlaceHolder(String fieldName, ArbilFieldsNode dataNode) {
	this.fieldName = fieldName;
	this.arbilFieldsNode = dataNode;
	if (dataNode instanceof Serializable) {
	    serializableNode = dataNode;
	} else {
	    if (dataNode instanceof ArbilDataNode && dataNode != null) {
		this.arbilDataNodeURI = ((ArbilDataNode) arbilFieldsNode).getURI();
	    }
	}
    }

    public String getFieldName() {
	return fieldName;
    }

    public ArbilFieldsNode getArbilDataNode() {
	if (arbilFieldsNode == null) {
	    if (serializableNode != null) {
		arbilFieldsNode = serializableNode;
	    } else if (arbilDataNodeURI != null) {
		arbilFieldsNode = dataNodeLoader.getArbilDataNode(null, arbilDataNodeURI);
	    }
	}
	return arbilFieldsNode;
    }

    @Override
    public String toString() {
	return "";
    }
}
