package nl.mpi.arbil.data;

import java.net.URI;
import java.util.Vector;
import nl.mpi.arbil.ImdiTableModel;
import nl.mpi.arbil.LinorgWindowManager;
import nl.mpi.arbil.TreeHelper;

/**
 *  Document   : MetadataBuilder
 *  Created on : Jun 9, 2010, 4:03:07 PM
 *  Author     : Peter Withers
 */
public class MetadataBuilder {

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
        ImdiTreeObject addedImdiObject = getImdiObjectWithoutLoading(addedNodeUri);
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
