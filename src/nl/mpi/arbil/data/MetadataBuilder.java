package nl.mpi.arbil.data;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import nl.mpi.arbil.GuiHelper;
import nl.mpi.arbil.ImdiTableModel;
import nl.mpi.arbil.LinorgFavourites;
import nl.mpi.arbil.LinorgSessionStorage;
import nl.mpi.arbil.LinorgWindowManager;
import nl.mpi.arbil.TreeHelper;
import nl.mpi.arbil.clarin.CmdiComponentBuilder;
import nl.mpi.arbil.clarin.CmdiProfileReader;
import org.w3c.dom.Document;

/**
 *  Document   : MetadataBuilder
 *  Created on : Jun 9, 2010, 4:03:07 PM
 *  Author     : Peter Withers
 */
public class MetadataBuilder {

    public void requestRootAddNode(String nodeType, String nodeTypeDisplayName) {
        ImdiTreeObject imdiTreeObject;
        imdiTreeObject = new ImdiTreeObject(LinorgSessionStorage.getSingleInstance().getNewImdiFileName(LinorgSessionStorage.getSingleInstance().getSaveLocation(""), nodeType));
        requestAddNode(imdiTreeObject, nodeType, nodeTypeDisplayName);
    }

    public void requestAddNode(final ImdiTreeObject destinationNode, final String nodeTypeDisplayNameLocal, final ImdiTreeObject addableNode) {
        // todo: ask user to save
        new Thread("requestAddNode") {

            @Override
            public void run() {
                destinationNode.updateLoadingState(1);
                synchronized (destinationNode.domLockObject) {
                    if (addableNode.isMetaDataNode()) {
                        destinationNode.saveChangesToCache(true);
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
                        TreeHelper.getSingleInstance().updateTreeNodeChildren(destinationNode.getParentDomNode());
                        String newTableTitleString = "new " + addableNode + " in " + destinationNode;
                        ImdiTableModel imdiTableModel = LinorgWindowManager.getSingleInstance().openFloatingTableOnce(new URI[]{addedNodeUri}, newTableTitleString);
                    } else {
                        String nodeTypeDisplayName = nodeTypeDisplayNameLocal;
                        ImdiTreeObject[] sourceImdiNodeArray;
                        if (addableNode.isEmptyMetaNode()) {
                            sourceImdiNodeArray = addableNode.getChildArray();
                        } else {
                            sourceImdiNodeArray = new ImdiTreeObject[]{addableNode};
                        }

                        for (ImdiTreeObject currentImdiNode : sourceImdiNodeArray) {
                            // todo: update this when functional
                            if (destinationNode.isCmdiMetaDataNode()) {
                                CmdiComponentBuilder componentBuilder = new CmdiComponentBuilder();
//            todo handle this outside the gui thread
                                URI addedNodePath = componentBuilder.insertResourceProxy(destinationNode, addableNode);
//                            destinationNode.reloadNode();
                                destinationNode.getParentDomNode().loadImdiDom();
//                            destinationNode.getParentDomNode().clearIcon();
//                            destinationNode.getParentDomNode().clearChildIcons();
//                        destinationNode.getParentDomNode().reloadNode();
//                        destinationNode.getParentDomNode().clearChildIcons();
//                        destinationNode.clearIcon();
//                        ImdiLoader.getSingleInstance().requestReload(destinationNode.getParentDomNode());
//                        destinationNode.getParentDomNode().waitTillLoaded();
//                        TreeHelper.getSingleInstance().updateTreeNodeChildren(destinationNode.getParentDomNode());
                            } else {

                                String nodeType;
                                String favouriteUrlString = null;
                                URI resourceUrl = null;
                                String mimeType = null;
                                if (currentImdiNode.isArchivableFile() && !currentImdiNode.isMetaDataNode()) {
                                    nodeType = ImdiSchema.getSingleInstance().getNodeTypeFromMimeType(currentImdiNode.mpiMimeType);
                                    resourceUrl = currentImdiNode.getURI();
                                    mimeType = currentImdiNode.mpiMimeType;
                                    nodeTypeDisplayName = "Resource";
                                } else {
                                    nodeType = LinorgFavourites.getSingleInstance().getNodeType(currentImdiNode, destinationNode);
                                    favouriteUrlString = currentImdiNode.getUrlString();
                                }
                                if (nodeType != null) {
                                    String targetXmlPath = destinationNode.getURI().getFragment();
                                    if (nodeType == null) { // targetXmlPath hass been  added at this point to preserve the sub node (N) which otherwise had been lost for the (x) and this is required to add to a sub node correctly
                                        LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Cannot add this type of node", null);
                                    } else {
//            if (this.isImdiChild()) {
//                System.out.println("requestAddNodeChild: " + this.getUrlString());
//                this.domParentImdi.requestAddNode(nodeType, this.nodeUrl.getRef(), nodeTypeDisplayName, favouriteUrlString, resourceUrl, mimeType);
//            } else {
                                        System.out.println("requestAddNode: " + nodeType + " : " + nodeTypeDisplayName + " : " + favouriteUrlString + " : " + resourceUrl);
                                        processAddNodes(destinationNode, nodeType, targetXmlPath, nodeTypeDisplayName, favouriteUrlString, mimeType, resourceUrl);
//                                    ImdiLoader.getSingleInstance().requestReload(destinationNode);
                                        destinationNode.getParentDomNode().loadImdiDom();
//                                    destinationNode.getParentDomNode().clearIcon();
//                                    destinationNode.getParentDomNode().clearChildIcons();
                                    }
                                }
                            }
                        }
                    }
                }
                destinationNode.updateLoadingState(-1);
            }
        }.start();
    }

    public void requestAddNode(final ImdiTreeObject destinationNode, final String nodeType, final String nodeTypeDisplayName) {
        // todo: ask user to save
        new Thread("requestAddNode") {

            @Override
            public void run() {
                destinationNode.updateLoadingState(1);
                synchronized (destinationNode.domLockObject) {
                    System.out.println("requestAddNode: " + nodeType + " : " + nodeTypeDisplayName);
                    processAddNodes(destinationNode, nodeType, destinationNode.getURI().getFragment(), nodeTypeDisplayName, null, null, null);
//                    ImdiLoader.getSingleInstance().requestReload(destinationNode);
//                    if (destinationNode.getFile().exists()) {
//                        destinationNode.getParentDomNode().loadImdiDom();
//                    }
//                    destinationNode.getParentDomNode().clearIcon();
//                    destinationNode.getParentDomNode().clearChildIcons();
                }
                destinationNode.updateLoadingState(-1);
            }
        }.start();
    }

    private void processAddNodes(ImdiTreeObject currentImdiObject, String nodeType, String targetXmlPath, String nodeTypeDisplayName, String favouriteUrlString, String mimeType, URI resourceUri) {
//         if (currentImdiObject.addQueue.size() > 0) { // add any child nodes requested
//                                ;
//                                URI resourceUri = null;
//                                {
//                                    String[] addRequestArrayString = currentImdiObject.addQueue.remove(0);
//                                    nodeType = addRequestArrayString[0];
//                                    targetXmlPath = addRequestArrayString[1];
//                                    nodeTypeDisplayName = addRequestArrayString[2];
//                                    favouriteUrlString = addRequestArrayString[3];
//                                    if (addRequestArrayString[4] != null) {
//                                        try {
//                                            resourceUri = new URI(addRequestArrayString[4]);
//                                        } catch (URISyntaxException urise) {
//                                            GuiHelper.linorgBugCatcher.logError(urise);
//                                        }
//                                    }
//                                    mimeType = addRequestArrayString[5];
//                                }
//                                    Vector<ImdiTreeObject> allChildren = new Vector();
//                                    allChildren.add(favouriteImdiNode);
//                                    favouriteImdiNode.getAllChildren(allChildren);
        // sub node loop
//                                    for (ImdiTreeObject currentFavChild : allChildren.toArray(new ImdiTreeObject[]{}))

        String newTableTitleString = "new " + nodeTypeDisplayName;
        if (currentImdiObject.isMetaDataNode() && currentImdiObject.getFile().exists()) {
            newTableTitleString = newTableTitleString + " in " + currentImdiObject.toString();
        }
        System.out.println("addQueue:-\nnodeType: " + nodeType + "\ntargetXmlPath: " + targetXmlPath + "\nnodeTypeDisplayName: " + nodeTypeDisplayName + "\nfavouriteUrlString: " + favouriteUrlString + "\nresourceUrl: " + resourceUri + "\nmimeType: " + mimeType);
//                                    ImdiTreeObject addedImdiObject = TreeHelper.getSingleInstance().addImdiChildNode(currentImdiObject, nodeType, nodeTypeDisplayName, resourceUrl, mimeType);
        URI addedNodeUri = addChildNode(currentImdiObject, nodeType, targetXmlPath, resourceUri, mimeType);
        ImdiTreeObject addedImdiObject = ImdiLoader.getSingleInstance().getImdiObjectWithoutLoading(addedNodeUri);
//                                if (addedImdiObject == null) {
//                                    LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Could not add node of type: " + nodeType, "Error inserting node");
//                                } else {
        if (addedImdiObject != null) {
//            Vector<ImdiTreeObject> allAddedNodes = new Vector<ImdiTreeObject>();
//                                    imdiTableModel.addImdiObjects(new ImdiTreeObject[]{addedImdiObject});
            //ImdiTableModel imdiTableModel = LinorgWindowManager.getSingleInstance().openAllChildNodesInFloatingTableOnce(new ImdiTreeObject[]{addedImdiObject}, newTableTitleString);
//            allAddedNodes.add(addedImdiObject);
//            addedImdiObject.loadImdiDom();
            if (favouriteUrlString != null) {
//                mergeWithFavourite(addedImdiObject, favouriteUrlString, allAddedNodes, progressMonitor);
            }
//                                    addedImdiObject.loadChildNodes();
//            addedImdiObject.clearIcon();
//            addedImdiObject.getParentDomNode().clearChildIcons();
//            ImdiLoader.getSingleInstance().requestReload(addedImdiObject);
            if (currentImdiObject.getFile().exists()) { // if this is a root node request then the target node will not have a file to reload
                currentImdiObject.getParentDomNode().loadImdiDom();
//                currentImdiObject.getParentDomNode().clearIcon();
//                currentImdiObject.getParentDomNode().clearChildIcons();
            }
            if (currentImdiObject.getParentDomNode() != addedImdiObject.getParentDomNode()) {
                addedImdiObject.getParentDomNode().loadImdiDom();
//                addedImdiObject.getParentDomNode().clearIcon();
//                addedImdiObject.getParentDomNode().clearChildIcons();
                TreeHelper.getSingleInstance().updateTreeNodeChildren(currentImdiObject.getParentDomNode());
            }
            TreeHelper.getSingleInstance().updateTreeNodeChildren(addedImdiObject.getParentDomNode());
            addedImdiObject.scrollToRequested = true;
            TreeHelper.getSingleInstance().updateTreeNodeChildren(addedImdiObject);
            //ImdiTableModel imdiTableModel = LinorgWindowManager.getSingleInstance().openFloatingTableOnce(allAddedNodes.toArray(new ImdiTreeObject[]{}), newTableTitleString);
        }
        ImdiTableModel imdiTableModel = LinorgWindowManager.getSingleInstance().openFloatingTableOnce(new URI[]{addedNodeUri}, newTableTitleString);
//                            } else {
////                                if (currentImdiObject.autoLoadChildNodes) {
////                                    currentImdiObject.loadChildNodes();
////                                }
//                                TreeHelper.getSingleInstance().updateTreeNodeChildren(currentImdiObject);
//                            }
    }

    /**
     * Add a new node based on a template and optionally attach a resource
     * @return String path to the added node
     */
    public URI addChildNode(ImdiTreeObject destinationNode, String nodeType, String targetXmlPath, URI resourceUri, String mimeType) {
        System.out.println("addChildNode:: " + nodeType + " : " + resourceUri);
        System.out.println("targetXmlPath:: " + targetXmlPath);
        // todo: ask user to save
        URI addedNodePath = null;
        destinationNode.updateLoadingState(1);
        synchronized (destinationNode.domLockObject) {
            if (destinationNode.getNeedsSaveToDisk()) {
                destinationNode.saveChangesToCache(false);
            }
            if (nodeType.startsWith(".") && destinationNode.isCmdiMetaDataNode()) {
                // add clarin sub nodes
                CmdiComponentBuilder componentBuilder = new CmdiComponentBuilder();
                addedNodePath = componentBuilder.insertChildComponent(destinationNode, targetXmlPath, nodeType);
            } else if (destinationNode.getNodeTemplate().isImdiChildType(nodeType) || (resourceUri != null && destinationNode.isSession())) {
                System.out.println("adding to current node");
                try {
                    Document nodDom = nodDom = new CmdiComponentBuilder().getDocument(destinationNode.getURI());
                    if (nodDom == null) {
                        LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("The metadata file could not be opened", "Add Node");
                    } else {
                        addedNodePath = ImdiSchema.getSingleInstance().insertFromTemplate(destinationNode.getNodeTemplate(), destinationNode.getURI(), destinationNode.getSubDirectory(), nodeType, targetXmlPath, nodDom, resourceUri, mimeType);
                        destinationNode.bumpHistory();
                        new CmdiComponentBuilder().savePrettyFormatting(nodDom, destinationNode.getFile());
                    }
                } catch (Exception ex) {
//                System.out.println("addChildNode: " + ex.getMessage());
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
//            needsSaveToDisk = true;
            } else {
                System.out.println("adding new node");
                URI targetFileURI = LinorgSessionStorage.getSingleInstance().getNewImdiFileName(destinationNode.getSubDirectory(), nodeType);
                if (CmdiProfileReader.pathIsProfile(nodeType)) {
                    CmdiComponentBuilder componentBuilder = new CmdiComponentBuilder();
                    try {
                        addedNodePath = componentBuilder.createComponentFile(targetFileURI, new URI(nodeType));
                        // TODO: some sort of warning like: "Could not add node of type: " + nodeType; would be useful here or downstream
//                    if (addedNodePath == null) {
//                      LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Could not add node of type: " + nodeType, "Error inserting node");
//                    }
                    } catch (URISyntaxException ex) {
                        GuiHelper.linorgBugCatcher.logError(ex);
                        return null;
                    }
                } else {
                    addedNodePath = ImdiSchema.getSingleInstance().addFromTemplate(new File(targetFileURI), nodeType);
                }
//                ImdiTreeObject addedNode = ImdiLoader.getSingleInstance().getImdiObject(null, targetFileURI);
                if (destinationNode.getFile().exists()) {
                    destinationNode.metadataUtils.addCorpusLink(destinationNode.getURI(), new URI[]{addedNodePath});
                    destinationNode.getParentDomNode().loadImdiDom();
//                    destinationNode.getParentDomNode().clearIcon();
//                    destinationNode.getParentDomNode().clearChildIcons();
                } else {
                    TreeHelper.getSingleInstance().addLocation(addedNodePath);
                    TreeHelper.getSingleInstance().applyRootLocations();
                }
//            destinationNode.saveChangesToCache();
//            destinationNode.needsSaveToDisk = true;
            }
//        //load then save the dom via the api to make sure there are id fields to each node then reload this imdi object
            //        destinationNode.updateImdiFileNodeIds();

            // begin temp test
            //            ImdiField fieldToAdd1 = new ImdiField("test.field", "unset");
            //            fieldToAdd1.translateFieldName("test.field.translated");
            //            addableImdiChild.addField(fieldToAdd1, 0);
            // end temp test
            //for (Enumeration fieldsToAdd = GuiHelper.imdiFieldViews.getCurrentGlobalView().getAlwaysShowColumns(); fieldsToAdd.hasMoreElements();) {
            //        for (Enumeration fieldsToAdd = GuiHelper.imdiSchema.listFieldsFor(this, nodeType, getNextImdiChildIdentifier(), resourcePath); fieldsToAdd.hasMoreElements();) {
            //            String[] currentField = (String[]) fieldsToAdd.nextElement();
            //            System.out.println("fieldToAdd: " + currentField[0]);
            //            System.out.println("valueToAdd: " + currentField[1]);
            //            ImdiField fieldToAdd = new ImdiField(destinationNode, currentField[0], currentField[1]);
            //            //fieldToAdd.translateFieldName(nodePath + siblingSpacer);
            //            fieldToAdd.translateFieldName(currentField[0]);
            //            if (GuiHelper.linorgJournal.saveJournalEntry(fieldToAdd.parentImdi.getUrlString(), fieldToAdd.xmlPath, null, fieldToAdd.fieldValue)) {
            //                destinationNode.addField(fieldToAdd, 0, addedImdiNodes, false);
            //            }
            //        }
            //        if (destinationNode != this) {
            ////            System.out.println("adding to list of child nodes 1: " + destinationNode);
            //            childrenHashtable.put(destinationNode.getUrlString(), destinationNode);
            //        }
        }
        destinationNode.updateLoadingState(-1);
        return addedNodePath;
    }
}
