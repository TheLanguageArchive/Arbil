package nl.mpi.arbil.data;

import nl.mpi.arbil.data.metadatafile.MetadataReader;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.parsers.ParserConfigurationException;
import nl.mpi.arbil.templates.ArbilFavourites;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.ArbilMetadataException;
import nl.mpi.arbil.clarin.profiles.CmdiProfileReader;
import nl.mpi.arbil.util.BugCatcher;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.WindowManager;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *  Document   : MetadataBuilder
 *  Created on : Jun 9, 2010, 4:03:07 PM
 *  Author     : Peter Withers
 */
public class MetadataBuilder {

    private static MessageDialogHandler messageDialogHandler;

    public static void setMessageDialogHandler(MessageDialogHandler handler) {
        messageDialogHandler = handler;
    }
    private static BugCatcher bugCatcher;

    public static void setBugCatcher(BugCatcher bugCatcherInstance) {
        bugCatcher = bugCatcherInstance;
    }
    private static WindowManager windowManager;

    public static void setWindowManager(WindowManager windowManagerInstance) {
        windowManager = windowManagerInstance;
    }

    /**
     * Requests to add a new node of given type to root
     * @param nodeType Name of node type
     * @param nodeTypeDisplayName Name to display as node type
     */
    public void requestRootAddNode(String nodeType, String nodeTypeDisplayName) {
        ArbilDataNode arbilDataNode = new ArbilDataNode(ArbilSessionStorage.getSingleInstance().getNewArbilFileName(ArbilSessionStorage.getSingleInstance().getSaveLocation(""), nodeType));
        requestAddNode(arbilDataNode, nodeType, nodeTypeDisplayName);
    }

    /**
     * Checks whether the destinationNode in its current state supports adding a node of the specified type <em>FROM IMDI TEMPLATE</em>
     *
     * <strong>NOTE:</strong> For other node types, specifically CMDI NODES, this method will <strong>always return true</strong>
     * 
     * @param destinationNode Proposed destination node
     * @param nodeType Full type name of the node to add
     * @return Whether the node can be added - if it is an IMDI template. Otherwise, always returns TRUE
     */
    public boolean canAddChildNode(final ArbilDataNode destinationNode, final String nodeType) {
        final String targetXmlPath = destinationNode.getURI().getFragment();

        synchronized (destinationNode.getParentDomLockObject()) {
            // Ignore CMDI metadata
            if (!(nodeType.startsWith(".") && destinationNode.isCmdiMetaDataNode())) {
                // Ignore non-child nodes
                if (destinationNode.getNodeTemplate().isArbilChildNode(nodeType)) {
                    // Do a quick pre-check whether there is a finite number of occurrences
                    if (destinationNode.getNodeTemplate().getMaxOccursForTemplate(nodeType) >= 0) {
                        System.out.println("adding to current node");
                        try {
                            Document nodDom = nodDom = ArbilComponentBuilder.getDocument(destinationNode.getURI());
                            if (nodDom == null) {
                                messageDialogHandler.addMessageDialogToQueue("The metadata file could not be opened", "Add Node");
                            } else {
                                return MetadataReader.getSingleInstance().canInsertFromTemplate(destinationNode.getNodeTemplate(), nodeType, targetXmlPath, nodDom);
                            }
                        } catch (ParserConfigurationException ex) {
                            bugCatcher.logError(ex);
                        } catch (SAXException ex) {
                            bugCatcher.logError(ex);
                        } catch (IOException ex) {
                            bugCatcher.logError(ex);
                        } catch (ArbilMetadataException ex) {
                            bugCatcher.logError(ex);
                        }
                    }
                }
            }
        }
        // Other cases not handled
        return true;
    }

    /**
     * Requests to add a new node of given type to given destination node
     * @param destinationNode Node to add new node to
     * @param nodeType Name of node type
     * @param nodeTypeDisplayName Name to display as node type
     */
    public void requestAddNode(final ArbilDataNode destinationNode, final String nodeType, final String nodeTypeDisplayName) {
        if (destinationNode.getNeedsSaveToDisk(false)) {
            destinationNode.saveChangesToCache(true);
        }
        new Thread("requestAddNode") {

            @Override
            public void run() {
                destinationNode.updateLoadingState(1);
                synchronized (destinationNode.getParentDomLockObject()) {
                    try {
                        System.out.println("requestAddNode: " + nodeType + " : " + nodeTypeDisplayName);
                        processAddNodes(destinationNode, nodeType, destinationNode.getURI().getFragment(), nodeTypeDisplayName, null, null, null);

                        // CODE REMOVED: previously, imdiLoaders was requested to reload destinationNode
                    } catch (ArbilMetadataException exception) {
                        messageDialogHandler.addMessageDialogToQueue(exception.getLocalizedMessage(), "Insert node error");
                    }
                }
                destinationNode.updateLoadingState(-1);
            }
        }.start();
    }

    /**
     * Requests to add a node on basis of a given existing node to the given destination node
     * @param destinationNode Node to add new node to
     * @param nodeType Name of node type
     * @param addableNode Node to base new node on
     */
    public void requestAddNode(final ArbilDataNode destinationNode, final String nodeTypeDisplayNameLocal, final ArbilDataNode addableNode) {
        if (destinationNode.getNeedsSaveToDisk(false)) {
            destinationNode.saveChangesToCache(true);
        }
        // Start new thread to add the node to its destination
        creatAddAddableNodeThread(destinationNode, nodeTypeDisplayNameLocal, addableNode).start();
    }

    /**
     * Creates a thread to be triggered by requestAddNode for addableNode
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
                    destinationNode.updateLoadingState(1);
                    synchronized (destinationNode.getParentDomLockObject()) {
                        if (addableNode.isMetaDataNode()) {
                            addMetaDataNode();
                        } else {
                            addNonMetaDataNode();
                        }
                    }
                    destinationNode.updateLoadingState(-1);
                } catch (ArbilMetadataException exception) {
                    messageDialogHandler.addMessageDialogToQueue(exception.getLocalizedMessage(), "Insert node error");
                } catch (UnsupportedOperationException exception) {
                    messageDialogHandler.addMessageDialogToQueue(exception.getLocalizedMessage(), "Insert node error");
                }
            }

            private void addNonMetaDataNode() throws ArbilMetadataException {
                String nodeTypeDisplayName = nodeTypeDisplayNameLocal;
                ArbilDataNode[] sourceArbilNodeArray;
                if (addableNode.isEmptyMetaNode()) {
                    sourceArbilNodeArray = addableNode.getChildArray();
                } else {
                    sourceArbilNodeArray = new ArbilDataNode[]{addableNode};
                }
                for (ArbilDataNode currentArbilNode : sourceArbilNodeArray) {
                    if (destinationNode.isCmdiMetaDataNode()) {
                        new ArbilComponentBuilder().insertResourceProxy(destinationNode, addableNode);
                        destinationNode.getParentDomNode().loadArbilDom();
                    } else {
                        String nodeType;
                        String favouriteUrlString = null;
                        URI resourceUrl = null;
                        String mimeType = null;
                        if (currentArbilNode.isArchivableFile() && !currentArbilNode.isMetaDataNode()) {
                            nodeType = MetadataReader.getSingleInstance().getNodeTypeFromMimeType(currentArbilNode.mpiMimeType);
                            resourceUrl = currentArbilNode.getURI();
                            mimeType = currentArbilNode.mpiMimeType;
                            nodeTypeDisplayName = "Resource";
                        } else {
                            nodeType = ArbilFavourites.getSingleInstance().getNodeType(currentArbilNode, destinationNode);
                            favouriteUrlString = currentArbilNode.getUrlString();
                        }
                        if (nodeType != null) {
                            String targetXmlPath = destinationNode.getURI().getFragment();
                            if (nodeType == null) {
                                messageDialogHandler.addMessageDialogToQueue("Cannot add this type of node", null);
                            } else {
                                System.out.println("requestAddNode: " + nodeType + " : " + nodeTypeDisplayName + " : " + favouriteUrlString + " : " + resourceUrl);
                                processAddNodes(destinationNode, nodeType, targetXmlPath, nodeTypeDisplayName, favouriteUrlString, mimeType, resourceUrl);
                                destinationNode.getParentDomNode().loadArbilDom();
                            }
                        }
                    }
                }
            }

            private void addMetaDataNode() throws ArbilMetadataException {
                URI addedNodeUri;
                if (addableNode.getURI().getFragment() == null) {
                    addedNodeUri = ArbilSessionStorage.getSingleInstance().getNewArbilFileName(destinationNode.getSubDirectory(), addableNode.getURI().getPath());
                    ArbilDataNode.getMetadataUtils(addableNode.getURI().toString()).copyMetadataFile(addableNode.getURI(), new File(addedNodeUri), null, true);
                    ArbilDataNode addedNode = ArbilDataNodeLoader.getSingleInstance().getArbilDataNodeWithoutLoading(addedNodeUri);
                    new ArbilComponentBuilder().removeArchiveHandles(addedNode);
                    destinationNode.metadataUtils.addCorpusLink(destinationNode.getURI(), new URI[]{addedNodeUri});
                    addedNode.loadArbilDom();
                    addedNode.scrollToRequested = true;
                } else {
                    ArbilComponentBuilder componentBuilder = new ArbilComponentBuilder();
                    addedNodeUri = componentBuilder.insertFavouriteComponent(destinationNode, addableNode);
                    new ArbilComponentBuilder().removeArchiveHandles(destinationNode);
                }
                destinationNode.getParentDomNode().loadArbilDom();
                String newTableTitleString = "new " + addableNode + " in " + destinationNode;
                windowManager.openFloatingTableOnce(new URI[]{addedNodeUri}, newTableTitleString);
            }
        };
    }

    private void processAddNodes(ArbilDataNode currentArbilNode, String nodeType, String targetXmlPath, String nodeTypeDisplayName, String favouriteUrlString, String mimeType, URI resourceUri) throws ArbilMetadataException {

        // make title for imdi table
        String newTableTitleString = "new " + nodeTypeDisplayName;
        if (currentArbilNode.isMetaDataNode() && currentArbilNode.getFile().exists()) {
            newTableTitleString = newTableTitleString + " in " + currentArbilNode.toString();
        }

        System.out.println("addQueue:-\nnodeType: " + nodeType + "\ntargetXmlPath: " + targetXmlPath + "\nnodeTypeDisplayName: " + nodeTypeDisplayName + "\nfavouriteUrlString: " + favouriteUrlString + "\nresourceUrl: " + resourceUri + "\nmimeType: " + mimeType);
        // Create child node
        URI addedNodeUri = addChildNode(currentArbilNode, nodeType, targetXmlPath, resourceUri, mimeType);
        // Get the newly created data node
        ArbilDataNode addedArbilNode = ArbilDataNodeLoader.getSingleInstance().getArbilDataNodeWithoutLoading(addedNodeUri);
        if (addedArbilNode != null) {
            if (favouriteUrlString != null) {
            }
            if (currentArbilNode.getFile().exists()) { // if this is a root node request then the target node will not have a file to reload
                currentArbilNode.getParentDomNode().loadArbilDom();
            }
            if (currentArbilNode.getParentDomNode() != addedArbilNode.getParentDomNode()) {
                addedArbilNode.getParentDomNode().loadArbilDom();
            }
            addedArbilNode.scrollToRequested = true;
            addedArbilNode.getParentDomNode().clearIcon();
            addedArbilNode.getParentDomNode().clearChildIcons();
        }
        windowManager.openFloatingTableOnce(new URI[]{addedNodeUri}, newTableTitleString);
    }

    /**
     * Add a new node based on a template and optionally attach a resource
     * @return String path to the added node
     */
    public URI addChildNode(ArbilDataNode destinationNode, String nodeType, String targetXmlPath, URI resourceUri, String mimeType) throws ArbilMetadataException {
        System.out.println("addChildNode:: " + nodeType + " : " + resourceUri);
        System.out.println("targetXmlPath:: " + targetXmlPath);
        if (destinationNode.getNeedsSaveToDisk(false)) {
            destinationNode.saveChangesToCache(true);
        }
        URI addedNodePath = null;
        destinationNode.updateLoadingState(1);
        synchronized (destinationNode.getParentDomLockObject()) {
            if (destinationNode.getNeedsSaveToDisk(false)) {
                destinationNode.saveChangesToCache(false);
            }
            if (nodeType.startsWith(".") && destinationNode.isCmdiMetaDataNode()) {
                // Add clarin sub nodes
                ArbilComponentBuilder componentBuilder = new ArbilComponentBuilder();
                addedNodePath = componentBuilder.insertChildComponent(destinationNode, targetXmlPath, nodeType);
            } else {
                if (destinationNode.getNodeTemplate().isArbilChildNode(nodeType) || (resourceUri != null && destinationNode.isSession())) {
                    System.out.println("adding to current node");
                    try {
                        Document nodDom = nodDom = ArbilComponentBuilder.getDocument(destinationNode.getURI());
                        if (nodDom == null) {
                            messageDialogHandler.addMessageDialogToQueue("The metadata file could not be opened", "Add Node");
                        } else {
                            addedNodePath = MetadataReader.getSingleInstance().insertFromTemplate(destinationNode.getNodeTemplate(), destinationNode.getURI(), destinationNode.getSubDirectory(), nodeType, targetXmlPath, nodDom, resourceUri, mimeType);
                            destinationNode.bumpHistory();
                            ArbilComponentBuilder.savePrettyFormatting(nodDom, destinationNode.getFile());
                        }
                    } catch (ParserConfigurationException ex) {
                        bugCatcher.logError(ex);
                    } catch (SAXException ex) {
                        bugCatcher.logError(ex);
                    } catch (IOException ex) {
                        bugCatcher.logError(ex);
                    }
//            needsSaveToDisk = true;
                } else {
                    System.out.println("adding new node");
                    URI targetFileURI = ArbilSessionStorage.getSingleInstance().getNewArbilFileName(destinationNode.getSubDirectory(), nodeType);
                    if (CmdiProfileReader.pathIsProfile(nodeType)) {
                        // Is CMDI profile
                        ArbilComponentBuilder componentBuilder = new ArbilComponentBuilder();
                        try {
                            addedNodePath = componentBuilder.createComponentFile(targetFileURI, new URI(nodeType), false);
                            // TODO: some sort of warning like: "Could not add node of type: " + nodeType; would be useful here or downstream
//                    if (addedNodePath == null) {
//                      LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Could not add node of type: " + nodeType, "Error inserting node");
//                    }
                        } catch (URISyntaxException ex) {
                            bugCatcher.logError(ex);
                            return null;
                        }
                    } else {
                        addedNodePath = MetadataReader.getSingleInstance().addFromTemplate(new File(targetFileURI), nodeType);
                    }
                    if (destinationNode.getFile().exists()) {
                        destinationNode.metadataUtils.addCorpusLink(destinationNode.getURI(), new URI[]{addedNodePath});
                        destinationNode.getParentDomNode().loadArbilDom();
                    } else {
                        TreeHelper.getSingleInstance().addLocation(addedNodePath);
                        TreeHelper.getSingleInstance().applyRootLocations();
                    }
                }
                // CODE REMOVED: load then save the dom via the api to make sure there are id fields to each node then reload this imdi object
            }
        }
        destinationNode.updateLoadingState(-1);
        return addedNodePath;
    }
}
