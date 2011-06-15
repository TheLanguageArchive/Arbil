package nl.mpi.arbil.data;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * ArbilTableCell for arrays of ArbilDataNodes. Serializable due to transient ArbilDataNode field.
 * Re-attaches using node URI
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilDataNodeArrayTableCell implements ArbilTableCell<ArbilDataNode[]> {

    private transient ArbilDataNode[] dataNodes;
    private ArrayList<URI> contentUris;

    public ArbilDataNodeArrayTableCell(ArbilDataNode[] dataNode) {
	setContent(dataNode);
	setURIs();
    }

    /**
     * @return the content
     */
    @Override
    public ArbilDataNode[] getContent() {
	if (dataNodes == null && contentUris != null) {
	    loadNodes();
	}
	return dataNodes;
    }

    /**
     * @param content the content to set
     */
    @Override
    public final void setContent(ArbilDataNode[] content) {
	this.dataNodes = content;
	setURIs();
    }

    private void setURIs() {
	assert dataNodes != null;
	contentUris = new ArrayList<URI>(dataNodes.length);
	for (ArbilDataNode node : dataNodes) {
	    contentUris.add(node != null ? node.getURI() : null);
	}
    }

    private void loadNodes() {
	assert contentUris != null;
	dataNodes = new ArbilDataNode[contentUris.size()];
	for (int i = 0; i < contentUris.size(); i++) {
	    dataNodes[i] = ArbilDataNodeLoader.getSingleInstance().getArbilDataNode(null, contentUris.get(i));
	}
    }

    @Override
    public String toString() {
	String cellText = "";
	Arrays.sort(getContent(), new Comparator() {

	    public int compare(Object o1, Object o2) {
		String value1 = o1.toString();
		String value2 = o2.toString();
		return value1.compareToIgnoreCase(value2);
	    }
	});
	boolean hasAddedValues = false;
	for (ArbilDataNode currentArbilDataNode : getContent()) {
	    cellText = cellText + "[" + currentArbilDataNode.toString() + "],";
	    hasAddedValues = true;
	}
	if (hasAddedValues) {
	    cellText = cellText.substring(0, cellText.length() - 1);
	}
	return (cellText);
    }
}
