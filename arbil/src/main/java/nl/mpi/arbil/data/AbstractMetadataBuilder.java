package nl.mpi.arbil.data;

import java.net.URI;
import nl.mpi.arbil.ArbilMetadataException;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.WindowManager;

/**
 * Base class with shared logic for IMDI and CMDI/MetadataAPI implementations
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class AbstractMetadataBuilder implements MetadataBuilder {

    private final MessageDialogHandler messageDialogHandler;
    private final WindowManager windowManager;
    private final DataNodeLoader dataNodeLoader;

    public AbstractMetadataBuilder(MessageDialogHandler messageDialogHandler, WindowManager windowManager, DataNodeLoader dataNodeLoader) {
	this.messageDialogHandler = messageDialogHandler;
	this.windowManager = windowManager;
	this.dataNodeLoader = dataNodeLoader;
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

    protected final ArbilDataNode processAddNodes(ArbilDataNode currentArbilNode, String nodeType, String targetXmlPath, String nodeTypeDisplayName, String favouriteUrlString, String mimeType, URI resourceUri) throws ArbilMetadataException {
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

    protected abstract void addNonMetaDataNode(final ArbilDataNode destinationNode, final String nodeTypeDisplayNameLocal, final ArbilDataNode addableNode) throws ArbilMetadataException;

    protected abstract void addMetaDataNode(final ArbilDataNode destinationNode, final String nodeTypeDisplayNameLocal, final ArbilDataNode addableNode) throws ArbilMetadataException;
}
