/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.arbil.data;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import nl.mpi.arbil.ArbilConstants;
import nl.mpi.arbil.ArbilMetadataException;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.model.MetadataContainer;
import nl.mpi.metadata.api.model.MetadataElement;
import nl.mpi.metadata.api.model.MetadataField;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.api.model.ReferencingMetadataElement;
import nl.mpi.metadata.api.type.ContainedMetadataElementType;
import nl.mpi.metadata.api.type.MetadataElementType;
import org.xml.sax.SAXException;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class CmdiDomLoader implements MetadataDomLoader {

    private final DataNodeLoader dataNodeLoader;
    private final MetadataAPI metadataAPI;

    public CmdiDomLoader(DataNodeLoader dataNodeLoader, MetadataAPI metadataAPI) {
	this.dataNodeLoader = dataNodeLoader;
	this.metadataAPI = metadataAPI;
    }

    public void loadMetadataDom(ArbilDataNode dataNode) {
	try {
	    //set the string name to unknown, it will be updated in the tostring function
	    dataNode.nodeText = "unknown";
	    if (!dataNode.isChildNode()) {
		dataNode.setMetadataElement(metadataAPI.getMetadataDocument(dataNode.getURI().toURL()));
	    } else if (dataNode.getMetadataElement() == null) {
		throw new AssertionError("Child without element encountered");
	    }
	    updateMetadataChildNodes(dataNode);
	} catch (Exception mue) {
	    BugCatcherManager.getBugCatcher().logError(dataNode.getUrlString(), mue);
	    //            System.out.println("Invalid input URL: " + mue);
	    File nodeFile = dataNode.getFile();
	    if (nodeFile != null && nodeFile.exists()) {
		dataNode.nodeText = "Could not load data";
	    } else {
		dataNode.nodeText = "File not found";
		dataNode.fileNotFound = true;
	    }
	}
    }

    private void updateMetadataChildNodes(ArbilDataNode dataNode) throws ParserConfigurationException, SAXException, IOException, TransformerException, ArbilMetadataException {
	HashMap<ArbilDataNode, HashSet<ArbilDataNode>> parentChildTree = new HashMap<ArbilDataNode, HashSet<ArbilDataNode>>();
	loadMetadataChildNodes(dataNode, parentChildTree);
	// TODO: Add unreferenced resourece proxies
	updateChildNodes(parentChildTree);
    }

    private void loadMetadataChildNodes(ArbilDataNode parentNode, HashMap<ArbilDataNode, HashSet<ArbilDataNode>> parentChildTree) {
	if (!parentChildTree.containsKey(parentNode)) {
	    parentChildTree.put(parentNode, new HashSet<ArbilDataNode>());
	}
	final MetadataElement metadataElement = parentNode.getMetadataElement();
	if (metadataElement instanceof MetadataContainer) {
	    final MetadataContainer<MetadataElement> container = (MetadataContainer) metadataElement;
	    iterateChildNodes(container, parentNode, parentChildTree);
	}
    }

    private void iterateChildNodes(final MetadataContainer<MetadataElement> container, ArbilDataNode parentNode, HashMap<ArbilDataNode, HashSet<ArbilDataNode>> parentChildTree) {
	// Add internal child nodes
	addMetadataChildNodes(container, parentNode, parentChildTree);
	// Add references (resources + linked metadata)
	if (container instanceof ReferencingMetadataElement) {
	    addReferencedChildNodes(parentNode, (ReferencingMetadataElement<Reference>) container, parentChildTree);
	}
    }

    private void addMetadataChildNodes(final MetadataContainer<MetadataElement> container, ArbilDataNode parentNode, HashMap<ArbilDataNode, HashSet<ArbilDataNode>> parentChildTree) {
	// Index for fields
	int fieldOrder = 0;
	// Iterate metadata children
	for (MetadataElement child : container.getChildren()) {
	    if (child instanceof MetadataContainer) {
		if (isChildNode(child)) {
		    try {
			// Create a new child ArbilDataNode
			final StringBuilder nodeURIStringBuilder = getNodeUriBase(parentNode);
			final ArbilDataNode metaNode = getMetaNode(parentNode, child, parentChildTree, nodeURIStringBuilder);
			final int index = metaNode == null ? 1 : parentChildTree.get(metaNode).size() + 1;
			final ArbilDataNode subNode = createChildNode(nodeURIStringBuilder, child, index);

			if (metaNode == null) {
			    parentChildTree.get(parentNode).add(subNode);
			} else {
			    parentChildTree.get(metaNode).add(subNode);
			}

			loadMetadataChildNodes(subNode, parentChildTree);
		    } catch (URISyntaxException usEx) {
			BugCatcherManager.getBugCatcher().logError("URISyntaxException while loading child nodes", usEx);
		    }
		} else {
		    // Don't create child ArbilDataNode, iterate children of metadata element
		    iterateChildNodes((MetadataContainer) child, parentNode, parentChildTree);
		}
	    } else if (child instanceof MetadataField) {
		// Add field
		addField(fieldOrder++, parentNode, (MetadataField) child);
	    }
	}
    }

    private boolean isChildNode(MetadataElement element) {
	final MetadataElementType type = element.getType();
	if (type instanceof ContainedMetadataElementType) {
	    final ContainedMetadataElementType containedType = (ContainedMetadataElementType) type;
	    final int minOccurences = containedType.getMinOccurences();
	    final int maxOccurences = containedType.getMaxOccurences();
	    return minOccurences != maxOccurences || minOccurences > 1;
	}
	return true;
    }

    /**
     *
     * @param parentNode
     * @return URI base for child node of specified parent node
     */
    private StringBuilder getNodeUriBase(ArbilDataNode parentNode) {
	final StringBuilder nodeURIStringBuilder = new StringBuilder(4).append(parentNode.getURI().toString());
	if (!parentNode.getUrlString().contains("#")) {
	    nodeURIStringBuilder.append("#");
	}
	nodeURIStringBuilder.append(ArbilConstants.imdiPathSeparator);
	return nodeURIStringBuilder;
    }

    /**
     * Will create/retrieve meta node for child node if required, and append the appropriate bits to the node URI stringß
     *
     * @param parentNode
     * @param child
     * @param parentChildTree
     * @param nodeURIStringBuilder
     * @return meta node for child node, null if not required
     * @throws URISyntaxException
     */
    private ArbilDataNode getMetaNode(ArbilDataNode parentNode, MetadataElement child, HashMap<ArbilDataNode, HashSet<ArbilDataNode>> parentChildTree, StringBuilder nodeURIStringBuilder) throws URISyntaxException {
	ArbilDataNode metaNode = null;
	if (!isSingleton(child)) {
	    // Make URI for metanode and create node
	    nodeURIStringBuilder.append(child.getType().getName());
	    metaNode = dataNodeLoader.getArbilDataNodeWithoutLoading(new URI(nodeURIStringBuilder.toString()));
	    if (!parentChildTree.containsKey(metaNode)) {
		// new metaNode, initialize and add to data structures
		metaNode.setNodeText(child.getType().getName());
		metaNode.setContainerNode(true);
		parentChildTree.get(parentNode).add(metaNode);
		parentChildTree.put(metaNode, new HashSet<ArbilDataNode>());
	    }
	    // Add separator for child node URI
	    nodeURIStringBuilder.append(ArbilConstants.imdiPathSeparator);
	}
	return metaNode;
    }

    private ArbilDataNode createChildNode(StringBuilder nodeURIStringBuilder, MetadataElement child, int index) throws URISyntaxException {
	nodeURIStringBuilder.append(child.getType().getName());
	nodeURIStringBuilder.append("(").append(index).append(")"); // TODO: make functional
	ArbilDataNode subNode = dataNodeLoader.getArbilDataNodeWithoutLoading(new URI(nodeURIStringBuilder.toString()));
	subNode.setMetadataElement(child);
	return subNode;
    }

    private boolean isSingleton(MetadataElement element) {
	final MetadataElementType type = element.getType();
	if (type instanceof ContainedMetadataElementType) {
	    return ((ContainedMetadataElementType) type).getMaxOccurences() == 1;
	}
	return true;
    }

    private void addField(int fieldOrder, ArbilDataNode parentNode, final MetadataField metadataField) {
	String parentPath = parentNode.getMetadataElement().getType().getPathString();
	String fieldPath = metadataField.getType().getPathString();
	if (fieldPath.startsWith(parentPath)) {
	    fieldPath = fieldPath.substring(parentPath.length());
	}

	ArbilField field = new ArbilField(fieldOrder, parentNode, fieldPath.replaceAll("/:", "."), metadataField.getValue().toString(), 0, false);
	field.setMetadataField(metadataField);
	parentNode.addField(field);
    }

    private void addReferencedChildNodes(ArbilDataNode parentNode, ReferencingMetadataElement<Reference> container, HashMap<ArbilDataNode, HashSet<ArbilDataNode>> parentChildTree) {
	for (Reference reference : container.getReferences()) {
	    ArbilDataNode referenceNode = dataNodeLoader.getArbilDataNodeWithoutLoading(reference.getURI());
	    parentChildTree.get(parentNode).add(referenceNode);
	}
    }

    private void updateChildNodes(HashMap<ArbilDataNode, HashSet<ArbilDataNode>> parentChildTree) {
	for (Map.Entry<ArbilDataNode, HashSet<ArbilDataNode>> entry : parentChildTree.entrySet()) {
	    ArbilDataNode currentNode = entry.getKey();
	    // save the old child array
	    ArbilDataNode[] oldChildArray = currentNode.childArray;
	    // set the new child array
	    currentNode.childArray = parentChildTree.get(currentNode).toArray(new ArbilDataNode[]{});
	    // check the old child array and for each that is no longer in the child array make sure they are removed from any containers (tables or trees)
	    List currentChildList = Arrays.asList(currentNode.childArray);
	    for (ArbilDataNode currentOldChild : oldChildArray) {
		if (currentChildList.indexOf(currentOldChild) == -1) {
		    // remove from any containers that its found in
		    for (ArbilDataNodeContainer currentContainer : currentOldChild.getRegisteredContainers()) {
			currentContainer.dataNodeRemoved(currentOldChild);
		    }
		}
	    }
	}
    }
}
