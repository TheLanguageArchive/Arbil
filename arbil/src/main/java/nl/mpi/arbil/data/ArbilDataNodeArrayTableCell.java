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
    private static DataNodeLoader dataNodeLoader;

    public static void setDataNodeLoader(DataNodeLoader dataNodeLoaderInstance) {
	dataNodeLoader = dataNodeLoaderInstance;
    }

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
	if (dataNodes == null) {
	    contentUris = null;
	} else {
	    contentUris = new ArrayList<URI>(dataNodes.length);
	    for (ArbilDataNode node : dataNodes) {
		contentUris.add(node != null ? node.getURI() : null);
	    }
	}
    }

    private void loadNodes() {
	if (contentUris == null) {
	    dataNodes = null;
	} else {
	    dataNodes = new ArbilDataNode[contentUris.size()];
	    for (int i = 0; i < contentUris.size(); i++) {
		dataNodes[i] = dataNodeLoader.getArbilDataNode(null, contentUris.get(i));
	    }
	}
    }

    @Override
    public String toString() {
	StringBuilder cellText = new StringBuilder();
	Arrays.sort(getContent(), new Comparator() {

	    public int compare(Object o1, Object o2) {
		String value1 = o1.toString();
		String value2 = o2.toString();
		return value1.compareToIgnoreCase(value2);
	    }
	});
	boolean hasAddedValues = false;
	for (ArbilDataNode currentArbilDataNode : getContent()) {
	    if (hasAddedValues) {
		cellText.append(','); // before each non-first value
	    }
	    cellText.append('[');
	    cellText.append(currentArbilDataNode.toString());
	    cellText.append(']');
	    hasAddedValues = true;
	}
	return cellText.toString();
    }
}
