/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for
 * Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.arbil.data;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;
import nl.mpi.arbil.ArbilMetadataException;
import nl.mpi.arbil.clarin.profiles.CmdiProfileReader;
import nl.mpi.arbil.data.metadatafile.ImdiUtils;
import nl.mpi.arbil.data.metadatafile.MetadataReader;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.TreeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Document : MetadataBuilder Created on : Jun 9, 2010, 4:03:07 PM Author :
 * Peter Withers
 */
public class MetadataBuilder {

    private final static Logger logger = LoggerFactory.getLogger(MetadataBuilder.class);
    private static final ResourceBundle services = ResourceBundle.getBundle("nl/mpi/arbil/localisation/Services");
    private static MessageDialogHandler messageDialogHandler;

    public static void setMessageDialogHandler(MessageDialogHandler handler) {
        messageDialogHandler = handler;
    }
    private static SessionStorage sessionStorage;

    public static void setSessionStorage(SessionStorage sessionStorageInstance) {
        sessionStorage = sessionStorageInstance;
    }
    private static TreeHelper treeHelper;

    public static void setTreeHelper(TreeHelper treeHelperInstance) {
        treeHelper = treeHelperInstance;
    }
    private static DataNodeLoader dataNodeLoader;

    public static void setDataNodeLoader(DataNodeLoader dataNodeLoaderInstance) {
        dataNodeLoader = dataNodeLoaderInstance;
    }
    private ArbilComponentBuilder arbilComponentBuilder = new ArbilComponentBuilder();

    /**
     * Checks whether the destinationNode in its current state supports adding a
     * node of the specified type
     *
     * @param destinationNode Proposed destination node
     * @param nodeType Full type name of the node to add
     * @return Whether the node can be added
     */
    public boolean canAddChildNode(final ArbilDataNode destinationNode, final String nodeType) {
        final String targetXmlPath = destinationNode.getURIFragment();

        synchronized (destinationNode.getParentDomLockObject()) {
            // Ignore CMDI metadata
            if (nodeType.startsWith(".") && destinationNode.isCmdiMetaDataNode()) {
                // Check whether clarin sub node can be added
                return arbilComponentBuilder.canInsertChildComponent(destinationNode, targetXmlPath, nodeType);
            } else {                // Ignore non-child nodes
                if (destinationNode.getNodeTemplate().isArbilChildNode(nodeType)) {
                    // Do a quick pre-check whether there is a finite number of occurrences
                    if (destinationNode.getNodeTemplate().getMaxOccursForTemplate(nodeType) >= 0) {
                        logger.debug("adding to current node");
                        try {
                            Document nodDom = ArbilComponentBuilder.getDocument(destinationNode.getUri());
                            if (nodDom == null) {
                                messageDialogHandler.addMessageDialogToQueue(services.getString("THE METADATA FILE COULD NOT BE OPENED"), services.getString("ADD NODE"));
                            } else {
                                return MetadataReader.getSingleInstance().canInsertFromTemplate(destinationNode.getNodeTemplate(), nodeType, targetXmlPath, nodDom);
                            }
                        } catch (ParserConfigurationException ex) {
                            BugCatcherManager.getBugCatcher().logError(ex);
                        } catch (SAXException ex) {
                            BugCatcherManager.getBugCatcher().logError(ex);
                        } catch (IOException ex) {
                            BugCatcherManager.getBugCatcher().logError(ex);
                        } catch (ArbilMetadataException ex) {
                            BugCatcherManager.getBugCatcher().logError(ex);
                        }
                    }
                }
                // Other cases not handled
                return true;
            }
        }
    }

    /**
     * Requests to add a metadata node for each resource file or one metadata
     * file for all the resources by making a copy the given existing node to
     * the given destination node
     *
     * @param destinationNode Node to add new node to
     * @param nodeTypeDisplayNameLocal the title of the table that will be
     * opened with the results when complete
     * @param nodeType Name of node type
     * @param addableNode Node to base new node on
     * @param resourceFiles An array of resource files or directories
     * @param copyDirectoryStructure if true then the directory structure should
     * be replicated by generating a corpus structure
     * @param metadataFilePerResource if true then each resource will be given a
     * separate metadata file, otherwise only one metadata file will be created
     * per directory
     */
    public void requestAddNodeAndResources(final ArbilNode destinationNode, final String nodeTypeDisplayNameLocal, final ArbilDataNode addableNode, final File[] resourceFiles, final boolean copyDirectoryStructure, final boolean metadataFilePerResource) {
        if (destinationNode instanceof ArbilDataNode && ((ArbilDataNode) destinationNode).getNeedsSaveToDisk(false)) {
            ((ArbilDataNode) destinationNode).saveChangesToCache(true);
        }
        // Start new thread to add the node to its destination
        new Thread(creatAddNodeAndResources(destinationNode, nodeTypeDisplayNameLocal, addableNode, resourceFiles, copyDirectoryStructure, metadataFilePerResource), "AddNodeAndResources").start();
    }

    private Runnable creatAddNodeAndResources(final ArbilNode destinationNode, final String nodeTypeDisplayNameLocal, final ArbilDataNode addableNode, final File[] resourceFiles, final boolean copyDirectoryStructure, final boolean metadataFilePerResource) {
        return new Runnable() {
            public void run() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }

    /**
     *
     * @param currentArbilNode
     * @return Manual nodetype, if set. Otherwise null
     */
    private String handleUnknownMimetype(ArbilDataNode currentArbilNode) {
        if (JOptionPane.YES_OPTION == messageDialogHandler.showDialogBox(
                MessageFormat.format(services.getString("THERE IS NO CONTROLLED VOCABULARY FOR EITHER WRITTEN RESOURCE OR MEDIA FILE THAT MATCH \"{0}\"."), currentArbilNode.mpiMimeType) + "\n"
                + services.getString("THIS PROBABLY MEANS THAT THE FILE IS NOT ARCHIVABLE. HOWEVER, YOU CAN PROCEED BY MANUALLY SELECTING THE RESOURCE TYPE.") + "\n\n"
                + services.getString("DO YOU WANT TO PROCEED?") + "\n\n"
                + services.getString("WARNING: DOING THIS WILL NOT GUARANTEE THAT YOUR DATA WILL BE UPLOADABLE TO THE CORPUS SERVER!"),
                services.getString("ADD RESOURCE"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE)) {
            String originalMime = currentArbilNode.mpiMimeType;
            currentArbilNode.mpiMimeType = null;
            if (new ImdiUtils().overrideTypecheckerDecision(new ArbilDataNode[]{currentArbilNode})) {
                // Try again
                return MetadataReader.getSingleInstance().getNodeTypeFromMimeType(currentArbilNode.mpiMimeType);
            } else {
                currentArbilNode.mpiMimeType = originalMime;
            }
        }
        return null;
    }

    public URI addChildNode(ArbilDataNode destinationNode, String nodeType, String targetXmlPath, URI resourceUri, String mimeType) throws ArbilMetadataException {
        return addChildNode(destinationNode, nodeType, targetXmlPath, resourceUri, mimeType, null);
    }

    /**
     * Add a new node based on a template and optionally attach a resource
     *
     * @param creationCallback option callback; if not null, its {@link NodeCreationCallback#nodeCreated(nl.mpi.arbil.data.ArbilDataNode, java.net.URI)
     * } method gets called as soon as the new node has been created and loaded.
     * @return String path to the added node
     */
    public URI addChildNode(ArbilDataNode destinationNode, String nodeType, String targetXmlPath, URI resourceUri, String mimeType, final NodeCreationCallback creationCallback) throws ArbilMetadataException {
        logger.debug("addChildNode:: {} : {}", nodeType, resourceUri);
        logger.debug("targetXmlPath:: {}", targetXmlPath);
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
                if (nodeType.startsWith(".") && destinationNode.isCmdiMetaDataNode()) {
                    // Add clarin sub nodes
                    addedNodePath = arbilComponentBuilder.insertChildComponent(destinationNode, targetXmlPath, nodeType);
                } else {
                    if (destinationNode.getNodeTemplate().isArbilChildNode(nodeType) || (resourceUri != null && destinationNode.isSession())) {
                        logger.debug("adding to current node");
                        try {
                            Document nodDom = ArbilComponentBuilder.getDocument(destinationNode.getUri());
                            if (nodDom == null) {
                                messageDialogHandler.addMessageDialogToQueue(services.getString("THE METADATA FILE COULD NOT BE OPENED"), services.getString("ADD NODE"));
                            } else {
                                addedNodePath = MetadataReader.getSingleInstance().insertFromTemplate(destinationNode.getNodeTemplate(), destinationNode.getUri(), destinationNode.getSubDirectory(), nodeType, targetXmlPath, nodDom, resourceUri, mimeType);
                                destinationNode.bumpHistory();
                                ArbilComponentBuilder.savePrettyFormatting(nodDom, destinationNode.getFile());

                                if (creationCallback == null) {
                                    // reload without callback
                                    dataNodeLoader.requestReload(destinationNode);
                                } else {
                                    // reload with new reload callback that wraps the creation callback
                                    final URI newNodePath = addedNodePath;
                                    dataNodeLoader.requestReload(destinationNode, new ArbilDataNodeLoaderCallBack() {
                                        public void dataNodeLoaded(ArbilDataNode reloadedDestinationNode) {
                                            creationCallback.nodeCreated(reloadedDestinationNode, newNodePath);
                                        }
                                    });
                                }
                            }
                        } catch (ParserConfigurationException ex) {
                            BugCatcherManager.getBugCatcher().logError(ex);
                        } catch (SAXException ex) {
                            BugCatcherManager.getBugCatcher().logError(ex);
                        } catch (IOException ex) {
                            BugCatcherManager.getBugCatcher().logError(ex);
                        }
//            needsSaveToDisk = true;
                    } else {
                        logger.debug("adding new metadata node of type {} as reference to {}", nodeType, destinationNode);
                        if (!destinationNode.equals(destinationNode.getParentDomNode())) {
                            logger.error("Node type '{}' not a child node according to template. Destination node '{}' not a parent dom node.", nodeType, destinationNode);
                            throw new ArbilMetadataException("Cannot create reference on child node");
                        }
                        URI targetFileURI = sessionStorage.getNewArbilFileName(destinationNode.getSubDirectory(), nodeType);
                        if (CmdiProfileReader.pathIsProfile(nodeType)) {
                            // Is CMDI profile
                            try {
                                addedNodePath = arbilComponentBuilder.createComponentFile(targetFileURI, new URI(nodeType), false);
                                // TODO: some sort of warning like: "Could not add node of type: " + nodeType; would be useful here or downstream
//                    if (addedNodePath == null) {
//                      LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Could not add node of type: " + nodeType, "Error inserting node");
//                    }
                            } catch (URISyntaxException ex) {
                                throw new ArbilMetadataException(String.format("Error while adding node to metadata hierarchy. Node type %s not a valid URI", nodeType), ex);
                            }
                        } else {
                            addedNodePath = MetadataReader.getSingleInstance().addFromTemplate(new File(targetFileURI), nodeType);
                        }
                        if (addedNodePath == null) {
                            throw new ArbilMetadataException("A node of the requested could not be created. See previous messages for details.");
                        } else {
                            if (destinationNode.getFile().exists()) {
                                destinationNode.getParentDomNode().getMetadataUtils().addCorpusLink(destinationNode.getUri(), new URI[]{addedNodePath});
                                destinationNode.getParentDomNode().loadArbilDom();
                                if (creationCallback != null) {
                                    creationCallback.nodeCreated(destinationNode, addedNodePath);
                                }
                            } else {
                                treeHelper.addLocation(addedNodePath);
                                treeHelper.applyRootLocations();
                            }
                        }
                    }
                    // CODE REMOVED: load then save the dom via the api to make sure there are id fields to each node then reload this imdi object
                }
            }
        } finally {
            destinationNode.updateLoadingState(-1);
        }
        return addedNodePath;
    }
}
