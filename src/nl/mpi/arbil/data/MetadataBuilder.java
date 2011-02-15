package nl.mpi.arbil.data;

import nl.mpi.arbil.data.metadatafile.MetadataReader;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.naming.OperationNotSupportedException;
import javax.xml.parsers.ParserConfigurationException;
import nl.mpi.arbil.ui.GuiHelper;
import nl.mpi.arbil.templates.ArbilFavourites;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.ArbilMetadataException;
import nl.mpi.arbil.clarin.profiles.CmdiProfileReader;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *  Document   : MetadataBuilder
 *  Created on : Jun 9, 2010, 4:03:07 PM
 *  Author     : Peter Withers
 */
public class MetadataBuilder {

    /**
     * Requests to add a new node of given type to root
     * @param nodeType Name of node type
     * @param nodeTypeDisplayName Name to display as node type
     */
    public void requestRootAddNode(String nodeType, String nodeTypeDisplayName) {
        ArbilNodeObject imdiTreeObject = new ArbilNodeObject(ArbilSessionStorage.getSingleInstance().getNewImdiFileName(ArbilSessionStorage.getSingleInstance().getSaveLocation(""), nodeType));
        requestAddNode(imdiTreeObject, nodeType, nodeTypeDisplayName);
    }

    /**
     * Requests to add a new node of given type to given destination node
     * @param destinationNode Node to add new node to
     * @param nodeType Name of node type
     * @param nodeTypeDisplayName Name to display as node type
     */
    public void requestAddNode(final ArbilNodeObject destinationNode, final String nodeType, final String nodeTypeDisplayName) {
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
                        ArbilWindowManager.getSingleInstance().addMessageDialogToQueue(exception.getLocalizedMessage(), "Insert node error");
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
    public void requestAddNode(final ArbilNodeObject destinationNode, final String nodeTypeDisplayNameLocal, final ArbilNodeObject addableNode) {
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
    private Thread creatAddAddableNodeThread(final ArbilNodeObject destinationNode, final String nodeTypeDisplayNameLocal, final ArbilNodeObject addableNode) {
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
                    ArbilWindowManager.getSingleInstance().addMessageDialogToQueue(exception.getLocalizedMessage(), "Insert node error");
                } catch (UnsupportedOperationException exception) {
                    ArbilWindowManager.getSingleInstance().addMessageDialogToQueue(exception.getLocalizedMessage(), "Insert node error");
                }
            }

            private void addNonMetaDataNode() throws ArbilMetadataException {
                String nodeTypeDisplayName = nodeTypeDisplayNameLocal;
                ArbilNodeObject[] sourceImdiNodeArray;
                if (addableNode.isEmptyMetaNode()) {
                    sourceImdiNodeArray = addableNode.getChildArray();
                } else {
                    sourceImdiNodeArray = new ArbilNodeObject[]{addableNode};
                }
                for (ArbilNodeObject currentImdiNode : sourceImdiNodeArray) {
                    if (destinationNode.isCmdiMetaDataNode()) {
                        new ArbilComponentBuilder().insertResourceProxy(destinationNode, addableNode);
                        destinationNode.getParentDomNode().loadImdiDom();
                    } else {
                        String nodeType;
                        String favouriteUrlString = null;
                        URI resourceUrl = null;
                        String mimeType = null;
                        if (currentImdiNode.isArchivableFile() && !currentImdiNode.isMetaDataNode()) {
                            nodeType = MetadataReader.getSingleInstance().getNodeTypeFromMimeType(currentImdiNode.mpiMimeType);
                            resourceUrl = currentImdiNode.getURI();
                            mimeType = currentImdiNode.mpiMimeType;
                            nodeTypeDisplayName = "Resource";
                        } else {
                            nodeType = ArbilFavourites.getSingleInstance().getNodeType(currentImdiNode, destinationNode);
                            favouriteUrlString = currentImdiNode.getUrlString();
                        }
                        if (nodeType != null) {
                            String targetXmlPath = destinationNode.getURI().getFragment();
                            if (nodeType == null) {
                                ArbilWindowManager.getSingleInstance().addMessageDialogToQueue("Cannot add this type of node", null);
                            } else {
                                System.out.println("requestAddNode: " + nodeType + " : " + nodeTypeDisplayName + " : " + favouriteUrlString + " : " + resourceUrl);
                                processAddNodes(destinationNode, nodeType, targetXmlPath, nodeTypeDisplayName, favouriteUrlString, mimeType, resourceUrl);
                                destinationNode.getParentDomNode().loadImdiDom();
                            }
                        }
                    }
                }
            }

            private void addMetaDataNode() throws ArbilMetadataException {
                URI addedNodeUri;
                if (addableNode.getURI().getFragment() == null) {
                    addedNodeUri = ArbilSessionStorage.getSingleInstance().getNewImdiFileName(destinationNode.getSubDirectory(), addableNode.getURI().getPath());
                    ArbilNodeObject.getMetadataUtils(addableNode.getURI().toString()).copyMetadataFile(addableNode.getURI(), new File(addedNodeUri), null, true);
                    ArbilNodeObject addedNode = ImdiLoader.getSingleInstance().getImdiObjectWithoutLoading(addedNodeUri);
                    new ArbilComponentBuilder().removeArchiveHandles(addedNode);
                    destinationNode.metadataUtils.addCorpusLink(destinationNode.getURI(), new URI[]{addedNodeUri});
                    addedNode.loadImdiDom();
                    addedNode.scrollToRequested = true;
                } else {
                    ArbilComponentBuilder componentBuilder = new ArbilComponentBuilder();
                    addedNodeUri = componentBuilder.insertFavouriteComponent(destinationNode, addableNode);
                    new ArbilComponentBuilder().removeArchiveHandles(destinationNode);
                }
                destinationNode.getParentDomNode().loadImdiDom();
                String newTableTitleString = "new " + addableNode + " in " + destinationNode;
                ArbilWindowManager.getSingleInstance().openFloatingTableOnce(new URI[]{addedNodeUri}, newTableTitleString);
            }
        };
    }

    private void processAddNodes(ArbilNodeObject currentImdiObject, String nodeType, String targetXmlPath, String nodeTypeDisplayName, String favouriteUrlString, String mimeType, URI resourceUri) throws ArbilMetadataException {

        // make title for imdi table
        String newTableTitleString = "new " + nodeTypeDisplayName;
        if (currentImdiObject.isMetaDataNode() && currentImdiObject.getFile().exists()) {
            newTableTitleString = newTableTitleString + " in " + currentImdiObject.toString();
        }

        System.out.println("addQueue:-\nnodeType: " + nodeType + "\ntargetXmlPath: " + targetXmlPath + "\nnodeTypeDisplayName: " + nodeTypeDisplayName + "\nfavouriteUrlString: " + favouriteUrlString + "\nresourceUrl: " + resourceUri + "\nmimeType: " + mimeType);
        URI addedNodeUri = addChildNode(currentImdiObject, nodeType, targetXmlPath, resourceUri, mimeType);
        ArbilNodeObject addedImdiObject = ImdiLoader.getSingleInstance().getImdiObjectWithoutLoading(addedNodeUri);
        if (addedImdiObject != null) {
            if (favouriteUrlString != null) {
            }
            if (currentImdiObject.getFile().exists()) { // if this is a root node request then the target node will not have a file to reload
                currentImdiObject.getParentDomNode().loadImdiDom();
            }
            if (currentImdiObject.getParentDomNode() != addedImdiObject.getParentDomNode()) {
                addedImdiObject.getParentDomNode().loadImdiDom();
            }
            addedImdiObject.scrollToRequested = true;
            addedImdiObject.getParentDomNode().clearIcon();
            addedImdiObject.getParentDomNode().clearChildIcons();
        }
        ArbilWindowManager.getSingleInstance().openFloatingTableOnce(new URI[]{addedNodeUri}, newTableTitleString);
    }

    /**
     * Add a new node based on a template and optionally attach a resource
     * @return String path to the added node
     */
    public URI addChildNode(ArbilNodeObject destinationNode, String nodeType, String targetXmlPath, URI resourceUri, String mimeType) throws ArbilMetadataException {
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
                            ArbilWindowManager.getSingleInstance().addMessageDialogToQueue("The metadata file could not be opened", "Add Node");
                        } else {
                            addedNodePath = MetadataReader.getSingleInstance().insertFromTemplate(destinationNode.getNodeTemplate(), destinationNode.getURI(), destinationNode.getSubDirectory(), nodeType, targetXmlPath, nodDom, resourceUri, mimeType);
                            destinationNode.bumpHistory();
                            ArbilComponentBuilder.savePrettyFormatting(nodDom, destinationNode.getFile());
                        }
                    } catch (ParserConfigurationException ex) {
                        GuiHelper.linorgBugCatcher.logError(ex);
                    } catch (SAXException ex) {
                        GuiHelper.linorgBugCatcher.logError(ex);
                    } catch (IOException ex) {
                        GuiHelper.linorgBugCatcher.logError(ex);
                    }
//            needsSaveToDisk = true;
                } else {
                    System.out.println("adding new node");
                    URI targetFileURI = ArbilSessionStorage.getSingleInstance().getNewImdiFileName(destinationNode.getSubDirectory(), nodeType);
                    if (CmdiProfileReader.pathIsProfile(nodeType)) {
                        ArbilComponentBuilder componentBuilder = new ArbilComponentBuilder();
                        try {
                            addedNodePath = componentBuilder.createComponentFile(targetFileURI, new URI(nodeType), false);
                            // TODO: some sort of warning like: "Could not add node of type: " + nodeType; would be useful here or downstream
//                    if (addedNodePath == null) {
//                      LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Could not add node of type: " + nodeType, "Error inserting node");
//                    }
                        } catch (URISyntaxException ex) {
                            GuiHelper.linorgBugCatcher.logError(ex);
                            return null;
                        }
                    } else {
                        addedNodePath = MetadataReader.getSingleInstance().addFromTemplate(new File(targetFileURI), nodeType);
                    }
                    if (destinationNode.getFile().exists()) {
                        destinationNode.metadataUtils.addCorpusLink(destinationNode.getURI(), new URI[]{addedNodePath});
                        destinationNode.getParentDomNode().loadImdiDom();
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
