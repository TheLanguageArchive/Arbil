package nl.mpi.arbil.data;

import java.net.URI;

/**
 * ArbilTableCell for ArbilDataNodes. Serializable due to transient ArbilDataNode field.
 * Re-attaches using node URI
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilDataNodeTableCell implements ArbilTableCell<ArbilDataNode> {

    private transient ArbilDataNode dataNode;
    private URI contentUri;

    public ArbilDataNodeTableCell(ArbilDataNode dataNode) {
	setContent(dataNode);
	if (dataNode != null) {
	    contentUri = dataNode.getURI();
	}
    }

    /**
     * @return the content
     */
    @Override
    public ArbilDataNode getContent() {
	if (dataNode == null && contentUri != null) {
	    dataNode = ArbilDataNodeLoader.getSingleInstance().getArbilDataNode(null, contentUri);
	}
	return dataNode;
    }

    /**
     * @param content the content to set
     */
    @Override
    public final void setContent(ArbilDataNode content) {
	this.dataNode = content;
	contentUri = content != null ? content.getURI() : null;
    }
}
