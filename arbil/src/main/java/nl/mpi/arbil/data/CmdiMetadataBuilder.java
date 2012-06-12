package nl.mpi.arbil.data;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import nl.mpi.arbil.ArbilMetadataException;
import nl.mpi.arbil.templates.ArbilTemplate;
import nl.mpi.arbil.templates.MetadataAPITemplate;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.TreeHelper;
import nl.mpi.arbil.util.WindowManager;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataElementException;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.ContainedMetadataElement;
import nl.mpi.metadata.api.model.MetadataContainer;
import nl.mpi.metadata.api.model.MetadataDocument;
import nl.mpi.metadata.api.model.MetadataElement;
import nl.mpi.metadata.api.model.Reference;
import nl.mpi.metadata.api.model.ReferencingMetadataDocument;
import nl.mpi.metadata.api.model.ReferencingMetadataElement;
import nl.mpi.metadata.api.type.ContainedMetadataElementType;
import nl.mpi.metadata.api.type.MetadataDocumentType;
import nl.mpi.metadata.api.type.MetadataElementType;
import nl.mpi.metadata.cmdi.api.model.CMDIDocument;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 * @author Peter Withers
 */
public class CmdiMetadataBuilder extends AbstractMetadataBuilder {

    private final WindowManager windowManager;
    private final SessionStorage sessionStorage;
    private final TreeHelper treeHelper;
    private final DataNodeLoader dataNodeLoader;
    private final MetadataAPI metadataAPI;
    private final CmdiDataNodeService dataNodeService;

    public CmdiMetadataBuilder(MetadataAPI metadataAPI, CmdiDataNodeService dataNodeService, MessageDialogHandler messageDialogHandler, WindowManager windowManager, SessionStorage sessionStorage, TreeHelper treeHelper, DataNodeLoader dataNodeLoader, ApplicationVersionManager versionManager) {
	super(dataNodeService, messageDialogHandler, windowManager, dataNodeLoader);
	this.metadataAPI = metadataAPI;
	this.dataNodeService = dataNodeService;
	this.windowManager = windowManager;
	this.sessionStorage = sessionStorage;
	this.treeHelper = treeHelper;
	this.dataNodeLoader = dataNodeLoader;
    }

    /**
     * Checks whether the destinationNode in its current state supports adding a node of the specified type
     *
     * @param destinationNode Proposed destination node
     * @param nodeTypeString Full type name of the node to add
     * @return Whether the node can be added
     */
    @Override
    public boolean canAddChildNode(ArbilDataNode destinationNode, final String nodeTypeString) {
	final MetadataElement metadataElement = destinationNode.getMetadataElement();
	if (!(metadataElement instanceof MetadataContainer)) {
	    // Target cannot contain children
	    return false;
	}

	// Get type from type string
	final MetadataElementType childType = getMetadataElementType(destinationNode, nodeTypeString);
	if (!(childType instanceof ContainedMetadataElementType)) {
	    // Type has to be containable
	    return false;
	}

	// Check if target or one of its singleton children can contain the specified type
	return canAddChildNode((MetadataContainer) metadataElement, (ContainedMetadataElementType) childType);
    }

    private boolean canAddChildNode(MetadataContainer<MetadataElement> container, ContainedMetadataElementType childType) {
	if (container.canAddInstanceOfType(childType)) {
	    return true;
	} else {
	    // Check 1:0..1 children
	    for (MetadataElement child : container.getChildren()) {
		if (child instanceof MetadataContainer) {
		    if (((ContainedMetadataElementType) child.getType()).getMaxOccurences() == 1) {
			if (canAddChildNode((MetadataContainer) child, childType)) {
			    return true;
			}
		    }
		}
	    }
	    return false;
	}
    }

    private MetadataElementType getMetadataElementType(ArbilDataNode destinationNode, final String nodeTypeString) throws RuntimeException {
	final ArbilTemplate nodeTemplate = destinationNode.getNodeTemplate();
	if (!(nodeTemplate instanceof MetadataAPITemplate)) {
	    throw new RuntimeException("Encountered CMDI node without MetadataAPITemplate");
	}
	final MetadataElementType childType = ((MetadataAPITemplate) nodeTemplate).getMetadataElement(nodeTypeString);
	return childType;
    }

    @Override
    protected final void addNonMetaDataNode(final ArbilDataNode destinationNode, final String nodeTypeDisplayNameLocal, final ArbilDataNode addableNode) throws ArbilMetadataException {
	ArbilDataNode[] sourceArbilNodeArray;
	if (addableNode.isContainerNode()) {
	    sourceArbilNodeArray = addableNode.getChildArray();
	} else {
	    sourceArbilNodeArray = new ArbilDataNode[]{addableNode};
	}

	for (ArbilDataNode currentArbilNode : sourceArbilNodeArray) {
	    insertResourceProxy(destinationNode, currentArbilNode);
	    destinationNode.getDataNodeService().saveChangesToCache(destinationNode);
	    dataNodeService.loadArbilDom(destinationNode.getParentDomNode());
	}
    }

    @Override
    protected final void addMetaDataNode(final ArbilDataNode destinationNode, final String nodeTypeDisplayNameLocal, final ArbilDataNode addableNode) throws ArbilMetadataException {
	URI addedNodeUri;
	if (addableNode.getURI().getFragment() == null) {
	    if (destinationNode != null) {
		addedNodeUri = sessionStorage.getNewArbilFileName(destinationNode.getSubDirectory(), addableNode.getURI().getPath());
	    } else {
		addedNodeUri = sessionStorage.getNewArbilFileName(sessionStorage.getSaveLocation(""), addableNode.getURI().getPath());
	    }
	    addableNode.getDataNodeService().copyMetadataFile(addableNode.getURI(), new File(addedNodeUri), null, true);
	    ArbilDataNode addedNode = dataNodeLoader.getArbilDataNodeWithoutLoading(addedNodeUri);
	    if (destinationNode == null) {
		// Destination node null means add to tree root
		treeHelper.addLocation(addedNodeUri);
		treeHelper.applyRootLocations();
	    } else {
		destinationNode.getDataNodeService().addCorpusLink(destinationNode.getURI(), new URI[]{addedNodeUri});
	    }
	    dataNodeService.loadArbilDom(addedNode);
	    addedNode.scrollToRequested = true;
	} else {
	    if (destinationNode == null) {
		// Cannot add subnode to local corpus tree root
		BugCatcherManager.getBugCatcher().logError(new Exception("Attempt to add child node to local corpus root"));
		return;
	    }
	    //TODO: Use metadata API(?)
	    addedNodeUri = new ArbilComponentBuilder().insertFavouriteComponent(destinationNode, addableNode);
	}
	if (destinationNode != null) {
	    dataNodeService.loadArbilDom(destinationNode.getParentDomNode());
	}
	String newTableTitleString = "new " + addableNode + (destinationNode == null ? "" : (" in " + destinationNode));
	windowManager.openFloatingTableOnce(new URI[]{addedNodeUri}, newTableTitleString);
    }

    /**
     * Add a new node based on a template and optionally attach a resource
     *
     * @return String path to the added node
     */
    @Override
    public URI addChildNode(ArbilDataNode destinationNode, String nodeType, String targetXmlPath, URI resourceUri, String mimeType) throws ArbilMetadataException {
	System.out.println("addChildNode:: " + nodeType + " : " + resourceUri);
	System.out.println("targetXmlPath:: " + targetXmlPath);
	if (destinationNode.getNeedsSaveToDisk(false)) {
	    dataNodeService.saveChangesToCache(destinationNode);
	}
	destinationNode.updateLoadingState(1);
	try {
	    synchronized (destinationNode.getParentDomLockObject()) {
		if (destinationNode.getNeedsSaveToDisk(false)) {
		    dataNodeService.saveChangesToCache(destinationNode);
		}
		if (destinationNode.getNodeTemplate().isArbilChildNode(nodeType) || (resourceUri != null && destinationNode.isSession())) {
		    return addMetadataChildNode(nodeType, destinationNode);
		} else {
		    final URI targetFileURI = sessionStorage.getNewArbilFileName(destinationNode.getSubDirectory(), nodeType);
		    final URI addedNodePath = addFromTemplate(new File(targetFileURI), nodeType);
		    if (destinationNode.getFile().exists()) {
			//TODO: Add as link to the existing document (see ImdiMetadataBuilder)
		    } else {
			// Add to tree root
			treeHelper.addLocation(addedNodePath);
			treeHelper.applyRootLocations();
		    }
		    return addedNodePath;
		}
	    }
	} finally {
	    destinationNode.updateLoadingState(-1);
	}
    }

    private URI addMetadataChildNode(String nodeType, ArbilDataNode destinationNode) throws RuntimeException {
	if (nodeType.startsWith(".")) {
	    final MetadataElementType metadataElementType = getMetadataElementType(destinationNode, nodeType);
	    final MetadataElement destinationElement = destinationNode.getMetadataElement();
	    if (!(destinationElement instanceof MetadataContainer)) {
		throw new RuntimeException("Cannot add child node to non-container MetadataElement");
	    }
	    MetadataElement addedChildNode = addChildNode((MetadataContainer) destinationElement, (ContainedMetadataElementType) metadataElementType);
	    if (addedChildNode != null) {
		try {
		    URI addedNodePath = getNodePathForMetadataElement(destinationNode, addedChildNode);
		    if (!bumpHistoryAndSaveToDisk(destinationNode)) {
			return null;
		    }
		    dataNodeLoader.requestReload(destinationNode);
		    return addedNodePath;
		} catch (URISyntaxException uriSEx) {
		    throw new RuntimeException("Invalid URI from metadata path" + addedChildNode.getPathString(), uriSEx);
		}
	    }
	}
	return null;
    }

    private URI getNodePathForMetadataElement(ArbilDataNode destinationNode, MetadataElement addedChildNode) throws URISyntaxException {
	StringBuilder nodeURIStringBuilder = new StringBuilder(destinationNode.getFile().toURI().toString());
	nodeURIStringBuilder.append("#");
	nodeURIStringBuilder.append(addedChildNode.getPathString().replaceAll("/:", ".").replaceAll("\\[(\\d+)\\]", "($1)"));
	URI addedNodePath = new URI(nodeURIStringBuilder.toString());
	return addedNodePath;
    }

    private MetadataElement addChildNode(MetadataContainer<MetadataElement> container, ContainedMetadataElementType childType) {
	if (container.canAddInstanceOfType(childType)) {
	    try {
		return metadataAPI.insertMetadataElement(container, childType);
	    } catch (MetadataException mdEx) {
		BugCatcherManager.getBugCatcher().logError("MetadataException while adding child node", mdEx);
		return null;
	    }
	} else {
	    // Check 1:0..1 children
	    for (MetadataElement child : container.getChildren()) {
		if (child instanceof MetadataContainer) {
		    if (((ContainedMetadataElementType) child.getType()).getMaxOccurences() == 1) {
			MetadataElement addedChildNode = addChildNode((MetadataContainer) child, childType);
			if (addedChildNode != null) {
			    return addedChildNode;
			}
		    }
		}
	    }
	}
	return null;
    }

    @Override
    public URI addFromTemplate(File destinationFile, String templateType) {
	try {
	    MetadataDocumentType metadataDocumentType = metadataAPI.getMetadataDocumentType(new URI(templateType));
	    MetadataDocument newDocument = metadataAPI.createMetadataDocument(metadataDocumentType);
	    metadataAPI.writeMetadataDocument(newDocument, new StreamResult(destinationFile));
	    return destinationFile.toURI();
	} catch (MetadataException mdEx) {
	    BugCatcherManager.getBugCatcher().logError(mdEx);
	} catch (IOException ioEx) {
	    BugCatcherManager.getBugCatcher().logError(ioEx);
	} catch (TransformerException tEx) {
	    BugCatcherManager.getBugCatcher().logError(tEx);
	} catch (URISyntaxException uriEx) {
	    BugCatcherManager.getBugCatcher().logError(uriEx);
	}
	return null;
    }

    public boolean removeChildNodes(ArbilDataNode arbilDataNode, String[] nodePaths) {
	final List<ContainedMetadataElement> elementsToRemove = getElementsToRemove(arbilDataNode, Arrays.asList(nodePaths));
	if (elementsToRemove != null) {
	    if (removeElements(elementsToRemove)) {
		return bumpHistoryAndSaveToDisk(arbilDataNode);
	    }
	}
	return false;
    }

    /**
     *
     * @param arbilDataNode Node to remove from
     * @param nodePaths paths of nodes to remove
     * @return List of elements based on nodePaths. Null if an error was encountered.
     */
    private List<ContainedMetadataElement> getElementsToRemove(ArbilDataNode arbilDataNode, Collection<String> nodePaths) {
	final MetadataDocument metadataDocument = arbilDataNode.getMetadataElement().getMetadataDocument();
	final List<ContainedMetadataElement> elements = new ArrayList<ContainedMetadataElement>(nodePaths.size());
	for (String nodePath : nodePaths) {
	    final String nodeXPath = nodePath.replaceAll("\\.", "/:").replaceAll("\\((\\d+)\\)", "[$1]");
	    final MetadataElement childElement = metadataDocument.getChildElement(nodeXPath);
	    if (childElement instanceof ContainedMetadataElement) {
		elements.add((ContainedMetadataElement) childElement);
	    } else {
		BugCatcherManager.getBugCatcher().logError("Child to remove not found or not ContainedMetadataElement for path " + nodeXPath, null);
		return null;
	    }
	}
	return elements;
    }

    private boolean removeElements(final List<ContainedMetadataElement> elements) {
	for (ContainedMetadataElement element : elements) {
	    try {
		element.getParent().removeChildElement(element);
	    } catch (MetadataElementException ex) {
		BugCatcherManager.getBugCatcher().logError("Exception while trying to remove element " + element.getPathString(), ex);
		return false;
	    }
	}
	return true;
    }

    private boolean bumpHistoryAndSaveToDisk(ArbilDataNode destinationNode) {
	try {
	    dataNodeService.bumpHistory(destinationNode);
	    return dataNodeService.saveToDisk(destinationNode);
	} catch (IOException ioEx) {
	    BugCatcherManager.getBugCatcher().logError(ioEx);
	}
	return false;
    }

    /**
     *
     * @param parent node to remove from
     * @param resourceProxyReferences resource proxies to remove from parent
     * @return true <em>iff</em> all resources have been removed
     */
    public boolean removeResourceProxyReferences(ArbilDataNode parent, Collection<String> resourceProxyReferences) {
	synchronized (parent.getParentDomLockObject()) {
	    boolean success = true;
	    if (!(parent.getMetadataElement() instanceof ReferencingMetadataElement)) {
		throw new UnsupportedOperationException("Can only add resource proxy to CMDI");
	    }
	    final ReferencingMetadataElement element = (ReferencingMetadataElement) parent.getMetadataElement();
	    if (!(element.getMetadataDocument() instanceof CMDIDocument)) {
		throw new UnsupportedOperationException("Can only remove resource proxy from CMDIDocument");
	    }
	    final CMDIDocument document = (CMDIDocument) element.getMetadataDocument();
	    for (String resourceProxyReference : resourceProxyReferences) {
		try {
		    final URI referenceURI = new URI(resourceProxyReference);
		    if (!removeReference(element, referenceURI, document)) {
			// One or more failures -> make false (and keep)
			success = false;
		    }
		} catch (URISyntaxException uriSEx) {
		    // Resource proxy reference is not a correct URI
		    BugCatcherManager.getBugCatcher().logError(uriSEx);
		    success = false;
		}
	    }
	    if (success) {
		success = bumpHistoryAndSaveToDisk(parent);
	    }
	    return success;
	}
    }

    private boolean removeReference(ReferencingMetadataElement element, URI referenceURI, ReferencingMetadataDocument document) {
	// Look for reference on document by URI
	final Reference reference = document.getDocumentReferenceByURI(referenceURI);

	if (reference != null) {
	    try {
		// Remove from element
		Reference removedReference = element.removeReference(reference);
		if (removedReference == null && element.equals(document)) {
		    // Not referenced from element, but document reference
		    removedReference = reference;
		}
		if (removedReference != null) {
		    // Check if there are any remaining references
		    if (document instanceof CMDIDocument
			    && ((CMDIDocument) document).getResourceProxyReferences(removedReference).isEmpty()) {
			// If not, remove from document
			document.removeDocumentReference(removedReference);
		    }
		    return true;
		}
	    } catch (MetadataException mdEx) {
		BugCatcherManager.getBugCatcher().logError("Error while trying to remove reference to resource proxy " + referenceURI, mdEx);
	    }
	}
	return false;
    }

    public URI insertResourceProxy(ArbilDataNode arbilDataNode, ArbilDataNode resourceNode) {
	synchronized (arbilDataNode.getParentDomLockObject()) {
	    if (!(arbilDataNode.getMetadataElement() instanceof ReferencingMetadataElement)) {
		throw new UnsupportedOperationException("Can only add resource proxy to CMDI");
	    }
	    final ReferencingMetadataElement element = (ReferencingMetadataElement) arbilDataNode.getMetadataElement();
	    try {
		element.createMetadataReference(resourceNode.getURI(), resourceNode.getAnyMimeType());
		return arbilDataNode.getURI();
	    } catch (MetadataException mdEx) {
		BugCatcherManager.getBugCatcher().logError("Error while trying to insert resource proxy reference", mdEx);
		return null;
	    }
	}
    }
}
