package nl.mpi.arbil.data;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * ArbilTableCell for arrays of ArbilDataNodes. Serializable due to transient ArbilDataNode field.
 * Re-attaches using node URI
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilDataNodeArrayTableCell implements ArbilTableCell<ArbilNode[]> {

    private transient ArbilNode[] dataNodes;
    private ArrayList<ArbilNode> serializableNodes;
    private ArrayList<URI> contentUris;
    private static DataNodeLoader dataNodeLoader;

    public static void setDataNodeLoader(DataNodeLoader dataNodeLoaderInstance) {
	dataNodeLoader = dataNodeLoaderInstance;
    }

    public ArbilDataNodeArrayTableCell(ArbilNode[] dataNode) {
	setContent(dataNode);
	setURIs();
    }

    /**
     * @return the content
     */
    @Override
    public ArbilNode[] getContent() {
	if (dataNodes == null && contentUris != null) {
	    loadNodes();
	}
	return dataNodes;
    }

    /**
     * @param content the content to set
     */
    @Override
    public final void setContent(ArbilNode[] content) {
	this.dataNodes = content;
	setURIs();
    }

    private void setURIs() {
	if (dataNodes == null) {
	    contentUris = null;
	} else {
	    contentUris = new ArrayList<URI>(dataNodes.length);
	    serializableNodes = new ArrayList<ArbilNode>(dataNodes.length);
	    int index = 0;
	    for (ArbilNode node : dataNodes) {
		if (node instanceof Serializable) {
		    contentUris.add(index, null);
		    serializableNodes.add(index++, node);
		} else if (node instanceof ArbilDataNode) {
		    serializableNodes.add(index, null);
		    contentUris.add(node != null ? ((ArbilDataNode) node).getURI() : null);
		}
	    }
	}
    }

    private void loadNodes() {
	if (contentUris == null) {
	    dataNodes = null;
	} else {
	    dataNodes = new ArbilDataNode[contentUris.size()];
	    for (int i = 0; i < contentUris.size(); i++) {
		if (serializableNodes.get(i) != null) {
		    dataNodes[i] = serializableNodes.get(i);
		} else if (contentUris.get(i) != null) {
		    dataNodes[i] = dataNodeLoader.getArbilDataNode(null, contentUris.get(i));
		} else {
		    dataNodes[i] = null;
		}
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
	for (ArbilNode currentArbilDataNode : getContent()) {
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
