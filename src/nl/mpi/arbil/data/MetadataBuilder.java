package nl.mpi.arbil.data;

import java.net.URI;
import java.util.Vector;
import nl.mpi.arbil.ImdiTableModel;
import nl.mpi.arbil.LinorgFavourites;
import nl.mpi.arbil.LinorgWindowManager;
import nl.mpi.arbil.TreeHelper;
import nl.mpi.arbil.clarin.CmdiComponentBuilder;

/**
 *  Document   : MetadataBuilder
 *  Created on : Jun 9, 2010, 4:03:07 PM
 *  Author     : Peter Withers
 */
public class MetadataBuilder {

    public boolean requestAddNode(ImdiTreeObject destinationNode, String nodeTypeDisplayName, ImdiTreeObject addableNode) {
        // todo: update this when functional
        if (destinationNode.isCmdiMetaDataNode()) {
            CmdiComponentBuilder componentBuilder = new CmdiComponentBuilder();
//            todo handle this outside the gui thread
            URI addedNodePath = componentBuilder.insertResourceProxy(destinationNode, addableNode);
            destinationNode.reloadNode();
            return true;
        }

        boolean returnValue = true;
        ImdiTreeObject[] sourceImdiNodeArray;
        if (addableNode.isEmptyMetaNode()) {
            sourceImdiNodeArray = addableNode.getChildArray();
        } else {
            sourceImdiNodeArray = new ImdiTreeObject[]{addableNode};
        }

        for (ImdiTreeObject currentImdiNode : sourceImdiNodeArray) {
            String nodeType;
            String favouriteUrlString = null;
            String resourceUrl = null;
            String mimeType = null;
            if (currentImdiNode.isArchivableFile() && !currentImdiNode.isMetaDataNode()) {
                nodeType = ImdiSchema.getSingleInstance().getNodeTypeFromMimeType(currentImdiNode.mpiMimeType);
                resourceUrl = currentImdiNode.getUrlString();
                mimeType = currentImdiNode.mpiMimeType;
                nodeTypeDisplayName = "Resource";
            } else {
                nodeType = LinorgFavourites.getSingleInstance().getNodeType(currentImdiNode, destinationNode);
                favouriteUrlString = currentImdiNode.getUrlString();
            }
            if (nodeType == null) {
                returnValue = false;
            }
            String targetXmlPath = destinationNode.getURI().getFragment();
            if (nodeType == null) { // targetXmlPath hass been  added at this point to preserve the sub node (N) which otherwise had been lost for the (x) and this is required to add to a sub node correctly
                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Cannot add this type of node", null);
            } else {
//            if (this.isImdiChild()) {
//                System.out.println("requestAddNodeChild: " + this.getUrlString());
//                this.domParentImdi.requestAddNode(nodeType, this.nodeUrl.getRef(), nodeTypeDisplayName, favouriteUrlString, resourceUrl, mimeType);
//            } else {
                System.out.println("requestAddNode: " + nodeType + " : " + nodeTypeDisplayName + " : " + favouriteUrlString + " : " + resourceUrl);
                destinationNode.getParentDomNode().addQueue.add(new String[]{nodeType, targetXmlPath, nodeTypeDisplayName, favouriteUrlString, resourceUrl, mimeType});
                ImdiLoader.getSingleInstance().requestReload(destinationNode);
            }
        }
//            }
        return returnValue;
    }

    public void requestAddNode(ImdiTreeObject destinationNode, String nodeType, String nodeTypeDisplayName) {
        System.out.println("requestAddNode: " + nodeType + " : " + nodeTypeDisplayName);
        destinationNode.getParentDomNode().addQueue.add(new String[]{nodeType, destinationNode.getURI().getFragment(), nodeTypeDisplayName, null, null, null});
        ImdiLoader.getSingleInstance().requestReload(destinationNode);
    }

    public void processAddNodes(ImdiTreeObject currentImdiObject, String nodeType, String targetXmlPath, String nodeTypeDisplayName, String favouriteUrlString, String mimeType, URI resourceUri) {
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
        if (currentImdiObject.isMetaDataNode() && !currentImdiObject.fileNotFound) {
            newTableTitleString = newTableTitleString + " in " + currentImdiObject.toString();
        }
        System.out.println("addQueue:-\nnodeType: " + nodeType + "\ntargetXmlPath: " + targetXmlPath + "\nnodeTypeDisplayName: " + nodeTypeDisplayName + "\nfavouriteUrlString: " + favouriteUrlString + "\nresourceUrl: " + resourceUri + "\nmimeType: " + mimeType);
//                                    ImdiTreeObject addedImdiObject = TreeHelper.getSingleInstance().addImdiChildNode(currentImdiObject, nodeType, nodeTypeDisplayName, resourceUrl, mimeType);
        URI addedNodeUri = currentImdiObject.addChildNode(nodeType, targetXmlPath, resourceUri, mimeType);
        ImdiTreeObject addedImdiObject = ImdiLoader.getSingleInstance().getImdiObjectWithoutLoading(addedNodeUri);
//                                if (addedImdiObject == null) {
//                                    LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Could not add node of type: " + nodeType, "Error inserting node");
//                                } else {
        if (addedImdiObject != null) {
            Vector<ImdiTreeObject> allAddedNodes = new Vector<ImdiTreeObject>();
//                                    imdiTableModel.addImdiObjects(new ImdiTreeObject[]{addedImdiObject});
            //ImdiTableModel imdiTableModel = LinorgWindowManager.getSingleInstance().openAllChildNodesInFloatingTableOnce(new ImdiTreeObject[]{addedImdiObject}, newTableTitleString);
            allAddedNodes.add(addedImdiObject);
            addedImdiObject.loadImdiDom();
            if (favouriteUrlString != null) {
                mergeWithFavourite(addedImdiObject, favouriteUrlString, allAddedNodes, progressMonitor);
            }
//                                    addedImdiObject.loadChildNodes();
            addedImdiObject.clearIcon();
            addedImdiObject.clearChildIcons();
            addedImdiObject.scrollToRequested = true;
            TreeHelper.getSingleInstance().updateTreeNodeChildren(currentImdiObject.getParentDomNode());
            if (currentImdiObject.getParentDomNode() != addedImdiObject.getParentDomNode()) {
                TreeHelper.getSingleInstance().updateTreeNodeChildren(addedImdiObject.getParentDomNode());
            }
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
}
