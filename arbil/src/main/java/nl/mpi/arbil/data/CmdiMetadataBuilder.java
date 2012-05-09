package nl.mpi.arbil.data;

import java.io.File;
import java.net.URI;
import nl.mpi.arbil.ArbilMetadataException;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.TreeHelper;
import nl.mpi.arbil.util.WindowManager;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 * @author Peter Withers
 */
public class CmdiMetadataBuilder implements MetadataBuilder {

    private final MessageDialogHandler messageDialogHandler;
    private final WindowManager windowManager;
    private final SessionStorage sessionStorage;
    private final TreeHelper treeHelper;
    private final DataNodeLoader dataNodeLoader;
    private final ApplicationVersionManager versionManager;
    private final ArbilComponentBuilder arbilComponentBuilder = new ArbilComponentBuilder();

    public CmdiMetadataBuilder(MessageDialogHandler messageDialogHandler, WindowManager windowManager, SessionStorage sessionStorage, TreeHelper treeHelper, DataNodeLoader dataNodeLoader, ApplicationVersionManager versionManager) {
	this.messageDialogHandler = messageDialogHandler;
	this.windowManager = windowManager;
	this.sessionStorage = sessionStorage;
	this.treeHelper = treeHelper;
	this.dataNodeLoader = dataNodeLoader;
	this.versionManager = versionManager;
    }

    /**
     * Requests to add a node on basis of a given existing node to the local corpus
     *
     * @param nodeType Name of node type
     * @param addableNode Node to base new node on
     */
    @Override
    public void requestAddRootNode(final ArbilDataNode addableNode, final String nodeTypeDisplayNameLocal) {
	// Start new thread to add the node to its destination
	creatAddAddableNodeThread(null, nodeTypeDisplayNameLocal, addableNode).start();
    }

    /**
     * Checks whether the destinationNode in its current state supports adding a node of the specified type
     *
     * @param destinationNode Proposed destination node
     * @param nodeType Full type name of the node to add
     * @return Whether the node can be added
     */
    @Override
    public boolean canAddChildNode(final ArbilDataNode destinationNode, final String nodeType) {
	final String targetXmlPath = destinationNode.getURI().getFragment();

	synchronized (destinationNode.getParentDomLockObject()) {
	    if (nodeType.startsWith(".")) {
		// Check whether clarin sub node can be added
		// TODO: Use metadata API
		return arbilComponentBuilder.canInsertChildComponent(destinationNode, targetXmlPath, nodeType);
	    } else {
		// Other cases not handled
		return true;
	    }
	}
    }

    /**
     * Requests to add a new node of given type to given destination node
     *
     * @param destinationNode Node to add new node to
     * @param nodeType Name of node type
     * @param nodeTypeDisplayName Name to display as node type
     */
    @Override
    public void requestAddNode(final ArbilDataNode destinationNode, final String nodeType, final String nodeTypeDisplayName) {
	if (destinationNode.getNeedsSaveToDisk(false)) {
	    destinationNode.saveChangesToCache(true);
	}
	new Thread("requestAddNode") {

	    @Override
	    public void run() {
		ArbilNode addedNode = null;
		destinationNode.updateLoadingState(1);
		synchronized (destinationNode.getParentDomLockObject()) {
		    try {
			System.out.println("requestAddNode: " + nodeType + " : " + nodeTypeDisplayName);
			addedNode = processAddNodes(destinationNode, nodeType, destinationNode.getURI().getFragment(), nodeTypeDisplayName, null, null, null);

			// CODE REMOVED: previously, imdiLoaders was requested to reload destinationNode
		    } catch (ArbilMetadataException exception) {
			messageDialogHandler.addMessageDialogToQueue(exception.getLocalizedMessage(), "Insert node error");
		    }
		}
		destinationNode.updateLoadingState(-1);
		if (addedNode != null) {
		    destinationNode.triggerNodeAdded(addedNode);
		}
	    }
	}.start();
    }

    /**
     * Requests to add a node on basis of a given existing node to the given destination node
     *
     * @param destinationNode Node to add new node to
     * @param nodeType Name of node type
     * @param addableNode Node to base new node on
     */
    @Override
    public void requestAddNode(final ArbilDataNode destinationNode, final String nodeTypeDisplayNameLocal, final ArbilDataNode addableNode) {
	if (destinationNode.getNeedsSaveToDisk(false)) {
	    destinationNode.saveChangesToCache(true);
	}
	// Start new thread to add the node to its destination
	creatAddAddableNodeThread(destinationNode, nodeTypeDisplayNameLocal, addableNode).start();
    }

    /**
     * Creates a thread to be triggered by requestAddNode for addableNode
     *
     * @param destinationNode Node to add new node to
     * @param nodeType Name of node type
     * @param addableNode Node to base new node on
     * @return New thread that adds the addable node
     */
    private Thread creatAddAddableNodeThread(final ArbilDataNode destinationNode, final String nodeTypeDisplayNameLocal, final ArbilDataNode addableNode) {
	return new Thread("requestAddNode") {

	    @Override
	    public void run() {
		try {
		    if (destinationNode != null) {
			destinationNode.updateLoadingState(1);
			addNode(destinationNode, nodeTypeDisplayNameLocal, addableNode);
		    } else {
			addNodeToRoot(nodeTypeDisplayNameLocal, addableNode);
		    }
		} catch (ArbilMetadataException exception) {
		    messageDialogHandler.addMessageDialogToQueue(exception.getLocalizedMessage(), "Insert node error");
		} catch (UnsupportedOperationException exception) {
		    messageDialogHandler.addMessageDialogToQueue(exception.getLocalizedMessage(), "Insert node error");
		} finally {
		    if (destinationNode != null) {
			destinationNode.updateLoadingState(-1);
		    }
		}
	    }
	};
    }

    @Override
    public void addNode(final ArbilDataNode destinationNode, final String nodeTypeDisplayNameLocal, final ArbilDataNode addableNode) throws ArbilMetadataException {
	synchronized (destinationNode.getParentDomLockObject()) {
	    if (addableNode.isMetaDataNode()) {
		addMetaDataNode(destinationNode, nodeTypeDisplayNameLocal, addableNode);
	    } else {
		addNonMetaDataNode(destinationNode, nodeTypeDisplayNameLocal, addableNode);
	    }
	}
    }

    private void addNodeToRoot(final String nodeTypeDisplayNameLocal, final ArbilDataNode addableNode) throws ArbilMetadataException {
	if (addableNode.isMetaDataNode()) {
	    addMetaDataNode(null, nodeTypeDisplayNameLocal, addableNode);
	} else {
	    addNonMetaDataNode(null, nodeTypeDisplayNameLocal, addableNode);
	}

    }

    private void addNonMetaDataNode(final ArbilDataNode destinationNode, final String nodeTypeDisplayNameLocal, final ArbilDataNode addableNode) throws ArbilMetadataException {
	ArbilDataNode[] sourceArbilNodeArray;
	if (addableNode.isContainerNode()) {
	    sourceArbilNodeArray = addableNode.getChildArray();
	} else {
	    sourceArbilNodeArray = new ArbilDataNode[]{addableNode};
	}

	//TODO: does the loop make sense for CMDI?
	for (ArbilDataNode currentArbilNode : sourceArbilNodeArray) {
	    new ArbilComponentBuilder().insertResourceProxy(destinationNode, addableNode);
	    destinationNode.getParentDomNode().loadArbilDom();
	}
    }

    private void addMetaDataNode(final ArbilDataNode destinationNode, final String nodeTypeDisplayNameLocal, final ArbilDataNode addableNode) throws ArbilMetadataException {
	URI addedNodeUri;
	if (addableNode.getURI().getFragment() == null) {
	    if (destinationNode != null) {
		addedNodeUri = sessionStorage.getNewArbilFileName(destinationNode.getSubDirectory(), addableNode.getURI().getPath());
	    } else {
		addedNodeUri = sessionStorage.getNewArbilFileName(sessionStorage.getSaveLocation(""), addableNode.getURI().getPath());
	    }
	    ArbilDataNode.getMetadataUtils(addableNode.getURI().toString()).copyMetadataFile(addableNode.getURI(), new File(addedNodeUri), null, true);
	    ArbilDataNode addedNode = dataNodeLoader.getArbilDataNodeWithoutLoading(addedNodeUri);
	    new ArbilComponentBuilder().removeArchiveHandles(addedNode);
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
	    //TODO: Use metadata API
	    addedNodeUri = arbilComponentBuilder.insertFavouriteComponent(destinationNode, addableNode);
	    new ArbilComponentBuilder().removeArchiveHandles(destinationNode);
	}
	if (destinationNode != null) {
	    destinationNode.getParentDomNode().loadArbilDom();
	}
	String newTableTitleString = "new " + addableNode + (destinationNode == null ? "" : (" in " + destinationNode));
	windowManager.openFloatingTableOnce(new URI[]{addedNodeUri}, newTableTitleString);
    }

    private ArbilDataNode processAddNodes(ArbilDataNode currentArbilNode, String nodeType, String targetXmlPath, String nodeTypeDisplayName, String favouriteUrlString, String mimeType, URI resourceUri) throws ArbilMetadataException {
	// make title for imdi table
	String newTableTitleString = "new " + nodeTypeDisplayName;
	if (currentArbilNode.isMetaDataNode() && currentArbilNode.getFile().exists()) {
	    newTableTitleString = newTableTitleString + " in " + currentArbilNode.toString();
	}

	System.out.println("addQueue:-\nnodeType: " + nodeType + "\ntargetXmlPath: " + targetXmlPath + "\nnodeTypeDisplayName: " + nodeTypeDisplayName + "\nfavouriteUrlString: " + favouriteUrlString + "\nresourceUrl: " + resourceUri + "\nmimeType: " + mimeType);
	// Create child node
	URI addedNodeUri = addChildNode(currentArbilNode, nodeType, targetXmlPath, resourceUri, mimeType);
	// Get the newly created data node
	ArbilDataNode addedArbilNode = dataNodeLoader.getArbilDataNodeWithoutLoading(addedNodeUri);
	if (addedArbilNode != null) {
	    addedArbilNode.getParentDomNode().updateLoadingState(+1);
	    try {
		addedArbilNode.scrollToRequested = true;
		if (currentArbilNode.getFile().exists()) { // if this is a root node request then the target node will not have a file to reload
		    currentArbilNode.getParentDomNode().loadArbilDom();
		}
		if (currentArbilNode.getParentDomNode() != addedArbilNode.getParentDomNode()) {
		    addedArbilNode.getParentDomNode().loadArbilDom();
		}
	    } finally {
		addedArbilNode.getParentDomNode().updateLoadingState(-1);
	    }
	}
	windowManager.openFloatingTableOnce(new URI[]{addedNodeUri}, newTableTitleString);
	return addedArbilNode;
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
		    addedNodePath = arbilComponentBuilder.insertChildComponent(destinationNode, targetXmlPath, nodeType);
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
}
