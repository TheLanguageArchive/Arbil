package nl.mpi.arbil.data;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nl.mpi.arbil.ArbilConstants;
import nl.mpi.arbil.templates.ArbilTemplateManager;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.model.MetadataContainer;
import nl.mpi.metadata.api.model.MetadataDocument;
import nl.mpi.metadata.api.model.MetadataElement;
import nl.mpi.metadata.api.model.MetadataField;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.api.model.ReferencingMetadataElement;
import nl.mpi.metadata.api.type.ContainedMetadataElementType;
import nl.mpi.metadata.api.type.MetadataElementType;

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
	    // Set the string name to unknown, it will be updated in the tostring function
	    dataNode.nodeText = "unknown";
	    // Get metadatadocument from API and set template on root
	    final MetadataDocument metadataDocument = metadataAPI.getMetadataDocument(dataNode.getURI().toURL());
	    dataNode.setMetadataElement(metadataDocument);
	    dataNode.nodeTemplate = ArbilTemplateManager.getSingleInstance().getCmdiTemplate(metadataDocument.getType().getSchemaLocation().toString());
	    //start loading into parentChildTree
	    Map<ArbilDataNode, Set<ArbilDataNode>> parentChildTree = new HashMap<ArbilDataNode, Set<ArbilDataNode>>();
	    loadRootNodeChildNodes(dataNode, parentChildTree);
	    updateChildNodes(parentChildTree);
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

    private void loadRootNodeChildNodes(ArbilDataNode parentNode, Map<ArbilDataNode, Set<ArbilDataNode>> parentChildTree) {
	if (!parentChildTree.containsKey(parentNode)) {
	    parentChildTree.put(parentNode, new HashSet<ArbilDataNode>());
	}
	final MetadataElement metadataElement = parentNode.getMetadataElement();
	if (metadataElement instanceof MetadataContainer) {
	    final MetadataContainer<MetadataElement> container = (MetadataContainer) metadataElement;
	    iterateChildNodes(parentNode, container, parentChildTree);
	}
	// TODO: Add unreferenced resource proxies
    }

    private void iterateChildNodes(ArbilDataNode parentNode, final MetadataContainer<MetadataElement> container, Map<ArbilDataNode, Set<ArbilDataNode>> parentChildTree) {
	// Add internal child nodes
	addMetadataChildNodes(container, parentNode, parentChildTree);
	// Add references (resources + linked metadata)
	if (container instanceof ReferencingMetadataElement) {
	    addReferencedChildNodes(parentNode, (ReferencingMetadataElement<Reference>) container, parentChildTree);
	}
    }

    private void addMetadataChildNodes(final MetadataContainer<MetadataElement> container, ArbilDataNode parentNode, Map<ArbilDataNode, Set<ArbilDataNode>> parentChildTree) {
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
			final int index = (metaNode == null) ? 1 : parentChildTree.get(metaNode).size() + 1;
			final ArbilDataNode subNode = createChildNode(nodeURIStringBuilder, child, index);

			if (metaNode == null) {
			    parentChildTree.get(parentNode).add(subNode);
			} else {
			    parentChildTree.get(metaNode).add(subNode);
			}

			loadRootNodeChildNodes(subNode, parentChildTree);
		    } catch (URISyntaxException usEx) {
			BugCatcherManager.getBugCatcher().logError("URISyntaxException while loading child nodes", usEx);
		    }
		} else {
		    // Don't create child ArbilDataNode, iterate children of metadata element
		    iterateChildNodes(parentNode, (MetadataContainer) child, parentChildTree);
		}
	    } else if (child instanceof MetadataField) {
		// Add field
		addField(fieldOrder++, parentNode, (MetadataField) child);
	    }
	}
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
    private ArbilDataNode getMetaNode(ArbilDataNode parentNode, MetadataElement child, Map<ArbilDataNode, Set<ArbilDataNode>> parentChildTree, StringBuilder nodeURIStringBuilder) throws URISyntaxException {
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

    private void addReferencedChildNodes(ArbilDataNode parentNode, ReferencingMetadataElement<Reference> container, Map<ArbilDataNode, Set<ArbilDataNode>> parentChildTree) {
	for (Reference reference : container.getReferences()) {
	    ArbilDataNode referenceNode = dataNodeLoader.getArbilDataNodeWithoutLoading(parentNode.getURI().resolve(reference.getURI()));
	    parentChildTree.get(parentNode).add(referenceNode);
	}
    }

    private void updateChildNodes(Map<ArbilDataNode, Set<ArbilDataNode>> parentChildTree) {
	for (Map.Entry<ArbilDataNode, Set<ArbilDataNode>> entry : parentChildTree.entrySet()) {
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

    private boolean isSingleton(MetadataElement element) {
	final MetadataElementType type = element.getType();
	if (type instanceof ContainedMetadataElementType) {
	    return ((ContainedMetadataElementType) type).getMaxOccurences() == 1;
	}
	return true;
    }
}
