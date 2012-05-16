package nl.mpi.arbil.data;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import nl.mpi.arbil.ArbilMetadataException;
import nl.mpi.arbil.templates.ArbilTemplate;
import nl.mpi.arbil.templates.MetadataAPITemplate;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.TreeHelper;
import nl.mpi.arbil.util.WindowManager;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.MetadataContainer;
import nl.mpi.metadata.api.model.MetadataElement;
import nl.mpi.metadata.api.model.ReferencingMetadataElement;
import nl.mpi.metadata.api.type.ContainedMetadataElementType;
import nl.mpi.metadata.api.type.MetadataElementType;
import nl.mpi.metadata.cmdi.api.model.CMDIDocument;
import nl.mpi.metadata.cmdi.api.model.ResourceProxy;

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

    public CmdiMetadataBuilder(MessageDialogHandler messageDialogHandler, WindowManager windowManager, SessionStorage sessionStorage, TreeHelper treeHelper, DataNodeLoader dataNodeLoader, ApplicationVersionManager versionManager) {
	super(messageDialogHandler, windowManager, dataNodeLoader);
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

	//TODO: does the loop make sense for CMDI?
	for (ArbilDataNode currentArbilNode : sourceArbilNodeArray) {
	    insertResourceProxy(destinationNode, addableNode);
	    destinationNode.getParentDomNode().loadArbilDom();
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
	    ArbilDataNode.getMetadataUtils(addableNode.getURI().toString()).copyMetadataFile(addableNode.getURI(), new File(addedNodeUri), null, true);
	    ArbilDataNode addedNode = dataNodeLoader.getArbilDataNodeWithoutLoading(addedNodeUri);
	    if (destinationNode == null) {
		// Destination node null means add to tree root
		treeHelper.addLocation(addedNodeUri);
		treeHelper.applyRootLocations();
	    } else {
		destinationNode.metadataUtils.addCorpusLink(destinationNode.getURI(), new URI[]{addedNodeUri});
	    }
	    addedNode.loadArbilDom();
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
	    destinationNode.getParentDomNode().loadArbilDom();
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
	    destinationNode.saveChangesToCache(true);
	}
	URI addedNodePath = null;
	destinationNode.updateLoadingState(1);
	try {
	    synchronized (destinationNode.getParentDomLockObject()) {
		if (destinationNode.getNeedsSaveToDisk(false)) {
		    destinationNode.saveChangesToCache(false);
		}
		if (nodeType.startsWith(".")) {
		    // Add clarin sub nodes
		    // TODO: use metadata API
		    //addedNodePath = arbilComponentBuilder.insertChildComponent(destinationNode, targetXmlPath, nodeType);
		    addedNodePath = null;
		}
	    }
	} finally {
	    destinationNode.updateLoadingState(-1);
	}
	return addedNodePath;
    }

    @Override
    public URI addFromTemplate(File destinationFile, String templateType) {
	//TODO: Use MetadataAPI
	return null;
    }

    public boolean removeChildNodes(ArbilDataNode arbilDataNode, String[] nodePaths) {
	// TODO: Use MetadataAPI
	return false;
    }

    public boolean removeResourceProxyReferences(ArbilDataNode parent, Collection<String> resourceProxyReferences) {
	//TODO: Use MetadataAPI
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
		final ResourceProxy documentResourceProxy = document.getDocumentResourceProxy(resourceProxyReference);
		if (documentResourceProxy != null) {
		    try {
			element.removeReference(documentResourceProxy);
		    } catch (MetadataException mdEx) {
			BugCatcherManager.getBugCatcher().logError("Error while trying to remove reference to resource proxy " + resourceProxyReference, mdEx);
			success = false;
		    }
		}
	    }
	    return success;
	}
    }

    public URI insertResourceProxy(ArbilDataNode arbilDataNode, ArbilDataNode resourceNode) {
	// there is no need to save the node at this point because metadatabuilder has already done so
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
