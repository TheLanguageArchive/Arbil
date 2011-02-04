package nl.mpi.arbil.data;

import nl.mpi.arbil.MetadataFile.MetadataReader;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.naming.OperationNotSupportedException;
import javax.xml.parsers.ParserConfigurationException;
import nl.mpi.arbil.GuiHelper;
import nl.mpi.arbil.ImdiTableModel;
import nl.mpi.arbil.LinorgFavourites;
import nl.mpi.arbil.LinorgSessionStorage;
import nl.mpi.arbil.LinorgWindowManager;
import nl.mpi.arbil.TreeHelper;
import nl.mpi.arbil.clarin.ArbilMetadataException;
import nl.mpi.arbil.clarin.CmdiComponentBuilder;
import nl.mpi.arbil.clarin.CmdiProfileReader;
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
        ImdiTreeObject imdiTreeObject = new ImdiTreeObject(LinorgSessionStorage.getSingleInstance().getNewImdiFileName(LinorgSessionStorage.getSingleInstance().getSaveLocation(""), nodeType));
        requestAddNode(imdiTreeObject, nodeType, nodeTypeDisplayName);
    }

    /**
     * Requests to add a new node of given type to given destination node
     * @param destinationNode Node to add new node to
     * @param nodeType Name of node type
     * @param nodeTypeDisplayName Name to display as node type
     */
    public void requestAddNode(final ImdiTreeObject destinationNode, final String nodeType, final String nodeTypeDisplayName) {
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
                        LinorgWindowManager.getSingleInstance().addMessageDialogToQueue(exception.getLocalizedMessage(), "Insert node error");
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
    public void requestAddNode(final ImdiTreeObject destinationNode, final String nodeTypeDisplayNameLocal, final ImdiTreeObject addableNode) {
        if (destinationNode.getNeedsSaveToDisk(false)) {
            destinationNode.saveChangesToCache(true);
        }
        // Start new thread to add the node to its destination
        creatAddAddableNodeThread(destinationNode, addableNode, nodeTypeDisplayNameLocal).start();
    }

    private Thread creatAddAddableNodeThread(final ImdiTreeObject destinationNode, final ImdiTreeObject addableNode, final String nodeTypeDisplayNameLocal) {
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
                    LinorgWindowManager.getSingleInstance().addMessageDialogToQueue(exception.getLocalizedMessage(), "Insert node error");
                } catch (UnsupportedOperationException exception) {
                    LinorgWindowManager.getSingleInstance().addMessageDialogToQueue(exception.getLocalizedMessage(), "Insert node error");
                }
            }

            private void addNonMetaDataNode() throws ArbilMetadataException {
                String nodeTypeDisplayName = nodeTypeDisplayNameLocal;
                ImdiTreeObject[] sourceImdiNodeArray;
                if (addableNode.isEmptyMetaNode()) {
                    sourceImdiNodeArray = addableNode.getChildArray();
                } else {
                    sourceImdiNodeArray = new ImdiTreeObject[]{addableNode};
                }
                for (ImdiTreeObject currentImdiNode : sourceImdiNodeArray) {
                    if (destinationNode.isCmdiMetaDataNode()) {
                        new CmdiComponentBuilder().insertResourceProxy(destinationNode, addableNode);
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
                            nodeType = LinorgFavourites.getSingleInstance().getNodeType(currentImdiNode, destinationNode);
                            favouriteUrlString = currentImdiNode.getUrlString();
                        }
                        if (nodeType != null) {
                            String targetXmlPath = destinationNode.getURI().getFragment();
                            if (nodeType == null) {
                                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Cannot add this type of node", null);
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
                    addedNodeUri = LinorgSessionStorage.getSingleInstance().getNewImdiFileName(destinationNode.getSubDirectory(), addableNode.getURI().getPath());
                    ImdiTreeObject.getMetadataUtils(addableNode.getURI().toString()).copyMetadataFile(addableNode.getURI(), new File(addedNodeUri), null, true);
                    ImdiTreeObject addedNode = ImdiLoader.getSingleInstance().getImdiObjectWithoutLoading(addedNodeUri);
                    new CmdiComponentBuilder().removeArchiveHandles(addedNode);
                    destinationNode.metadataUtils.addCorpusLink(destinationNode.getURI(), new URI[]{addedNodeUri});
                    addedNode.loadImdiDom();
                    addedNode.scrollToRequested = true;
                } else {
                    CmdiComponentBuilder componentBuilder = new CmdiComponentBuilder();
                    addedNodeUri = componentBuilder.insertFavouriteComponent(destinationNode, addableNode);
                    new CmdiComponentBuilder().removeArchiveHandles(destinationNode);
                }
                destinationNode.getParentDomNode().loadImdiDom();
                String newTableTitleString = "new " + addableNode + " in " + destinationNode;
                LinorgWindowManager.getSingleInstance().openFloatingTableOnce(new URI[]{addedNodeUri}, newTableTitleString);
            }
        };
    }

    private void processAddNodes(ImdiTreeObject currentImdiObject, String nodeType, String targetXmlPath, String nodeTypeDisplayName, String favouriteUrlString, String mimeType, URI resourceUri) throws ArbilMetadataException {

        // make title for imdi table
        String newTableTitleString = "new " + nodeTypeDisplayName;
        if (currentImdiObject.isMetaDataNode() && currentImdiObject.getFile().exists()) {
            newTableTitleString = newTableTitleString + " in " + currentImdiObject.toString();
        }

        System.out.println("addQueue:-\nnodeType: " + nodeType + "\ntargetXmlPath: " + targetXmlPath + "\nnodeTypeDisplayName: " + nodeTypeDisplayName + "\nfavouriteUrlString: " + favouriteUrlString + "\nresourceUrl: " + resourceUri + "\nmimeType: " + mimeType);
        URI addedNodeUri = addChildNode(currentImdiObject, nodeType, targetXmlPath, resourceUri, mimeType);
        ImdiTreeObject addedImdiObject = ImdiLoader.getSingleInstance().getImdiObjectWithoutLoading(addedNodeUri);
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
        LinorgWindowManager.getSingleInstance().openFloatingTableOnce(new URI[]{addedNodeUri}, newTableTitleString);
    }

    /**
     * Add a new node based on a template and optionally attach a resource
     * @return String path to the added node
     */
    public URI addChildNode(ImdiTreeObject destinationNode, String nodeType, String targetXmlPath, URI resourceUri, String mimeType) throws ArbilMetadataException {
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
                CmdiComponentBuilder componentBuilder = new CmdiComponentBuilder();
                addedNodePath = componentBuilder.insertChildComponent(destinationNode, targetXmlPath, nodeType);
            } else {
                if (destinationNode.getNodeTemplate().isImdiChildType(nodeType) || (resourceUri != null && destinationNode.isSession())) {
                    System.out.println("adding to current node");
                    try {
                        Document nodDom = nodDom = new CmdiComponentBuilder().getDocument(destinationNode.getURI());
                        if (nodDom == null) {
                            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("The metadata file could not be opened", "Add Node");
                        } else {
                            addedNodePath = MetadataReader.getSingleInstance().insertFromTemplate(destinationNode.getNodeTemplate(), destinationNode.getURI(), destinationNode.getSubDirectory(), nodeType, targetXmlPath, nodDom, resourceUri, mimeType);
                            destinationNode.bumpHistory();
                            new CmdiComponentBuilder().savePrettyFormatting(nodDom, destinationNode.getFile());
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
                    URI targetFileURI = LinorgSessionStorage.getSingleInstance().getNewImdiFileName(destinationNode.getSubDirectory(), nodeType);
                    if (CmdiProfileReader.pathIsProfile(nodeType)) {
                        CmdiComponentBuilder componentBuilder = new CmdiComponentBuilder();
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
