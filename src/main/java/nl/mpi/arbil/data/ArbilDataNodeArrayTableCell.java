package nl.mpi.arbil.data;

import java.net.URI;
import java.util.ArrayList;

/**
 * ArbilTableCell for arrays of ArbilDataNodes. Serializable due to transient ArbilDataNode field.
 * Re-attaches using node URI
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilDataNodeArrayTableCell implements ArbilTableCell<ArbilDataNode[]> {

    private transient ArbilDataNode[] dataNode;
    private ArrayList<URI> contentUri;

    public ArbilDataNodeArrayTableCell(ArbilDataNode[] dataNode) {
	setContent(dataNode);
	setURIs();
    }

    /**
     * @return the content
     */
    @Override
    public ArbilDataNode[] getContent() {
	if (dataNode == null && contentUri != null) {
	    loadNodes();
	}
	return dataNode;
    }

    /**
     * @param content the content to set
     */
    @Override
    public final void setContent(ArbilDataNode[] content) {
	this.dataNode = content;
	setURIs();
    }

    private void setURIs() {
	assert dataNode != null;
	contentUri = new ArrayList<URI>(dataNode.length);
	for (ArbilDataNode node : dataNode) {
	    contentUri.add(node != null ? node.getURI() : null);
	}
    }

    private void loadNodes() {
	assert contentUri != null;
	dataNode = new ArbilDataNode[contentUri.size()];
	for (int i = 0; i < contentUri.size(); i++) {
	    dataNode[i] = ArbilDataNodeLoader.getSingleInstance().getArbilDataNode(null, contentUri.get(i));
	}
    }
}
