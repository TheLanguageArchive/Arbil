package nl.mpi.arbil.data;

import java.io.Serializable;
import java.net.URI;

/**
 * ArbilTableCell for ArbilDataNodes. Serializable due to transient ArbilDataNode field.
 * Re-attaches using node URI
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilDataNodeTableCell implements ArbilTableCell<ArbilFieldsNode> {

    private transient ArbilFieldsNode dataNode;
    private ArbilFieldsNode serializableDataNode = null;
    private URI contentUri;
    
    private static DataNodeLoader dataNodeLoader;
    public static void setDataNodeLoader(DataNodeLoader dataNodeLoaderInstance){
	dataNodeLoader = dataNodeLoaderInstance;
    }

    public ArbilDataNodeTableCell(ArbilFieldsNode dataNode) {
	setContent(dataNode);
    }

    /**
     * @return the content
     */
    @Override
    public ArbilFieldsNode getContent() {
	if (dataNode == null) {
	    if (serializableDataNode != null) {
		dataNode = serializableDataNode;
	    } else {
		if (contentUri != null) {
		    dataNode = dataNodeLoader.getArbilDataNode(null, contentUri);
		}
	    }
	}
	return dataNode;
    }

    /**
     * @param content the content to set
     */
    @Override
    public final void setContent(ArbilFieldsNode content) {
	this.dataNode = content;
	if (content instanceof Serializable) {
	    serializableDataNode = content;
	} else if (content instanceof ArbilDataNode) {
	    contentUri = (content != null) ? ((ArbilDataNode) content).getURI() : null;
	}
    }

    @Override
    public String toString() {
	return dataNode == null ? null : dataNode.toString();
    }
}
