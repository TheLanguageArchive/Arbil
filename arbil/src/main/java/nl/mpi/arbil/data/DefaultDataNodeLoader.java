/**
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package nl.mpi.arbil.data;

import java.net.URI;
import java.util.Hashtable;
import java.util.Vector;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.plugin.PluginArbilDataNode;

/**
 * Document : ArbilDataNodeLoader formerly known as ImdiLoader <br>Created on : Dec
 * 30, 2008, 3:04:39 PM
 *
 * @author Peter.Withers@mpi.nl
 */
public class DefaultDataNodeLoader implements DataNodeLoader {

    private Hashtable<String, ArbilDataNode> arbilHashTable = new Hashtable<String, ArbilDataNode>();
    private Vector<ArbilDataNode> nodesNeedingSave = new Vector<ArbilDataNode>();
    private static SessionStorage sessionStorage;
    private DataNodeLoaderThreadManager threadManager;

    public static void setSessionStorage(SessionStorage sessionStorageInstance) {
        sessionStorage = sessionStorageInstance;
    }

    public DefaultDataNodeLoader(DataNodeLoaderThreadManager loaderThreadManager) {
        System.out.println("ArbilDataNodeLoader init");
        threadManager = loaderThreadManager;
        threadManager.setSchemaCheckLocalFiles(sessionStorage.loadBoolean("schemaCheckLocalFiles", threadManager.isSchemaCheckLocalFiles()));
    }

//    public ImdiTreeObject isImdiObjectLoaded(String localUrlString) {
//        localUrlString = ImdiTreeObject.conformStringToUrl(localUrlString).toString();
//        return imdiHashTable.get(localUrlString);
//    }
    @Override
    public ArbilDataNode getArbilDataNodeWithoutLoading(URI localUri) {
        ArbilDataNode currentDataNode = null;
        if (localUri != null) {
            localUri = ArbilDataNode.normaliseURI(localUri);
            // correct any variations in the url string
//            localUri = ImdiTreeObject.conformStringToUrl(localUri).toString();
            currentDataNode = arbilHashTable.get(localUri.toString());
            if (currentDataNode == null) {
                currentDataNode = new ArbilDataNode(localUri);
                arbilHashTable.put(localUri.toString(), currentDataNode);
            }
        }
        return currentDataNode;
    }

    /**
     * this is a transitional method and will be replaced when the time comes
     *
     * @return the ArbilDataNode that was obtained via getArbilDataNode and cast
     * to PluginArbilDataNode
     */
    public PluginArbilDataNode getPluginArbilDataNode(Object registeringObject, URI localUri) {
        return (PluginArbilDataNode) getArbilDataNode(registeringObject, localUri);
    }

    @Override
    public ArbilDataNode getArbilDataNode(Object registeringObject, URI localUri) {// throws Exception {
//        if (localNodeText == null && localUrlString.contains("WrittenResource")) {
//            System.out.println("getImdiObject: " + localNodeText + " : " + localUrlString);
//        }
//        if (registeringObject == null) {
//            throw (new Exception("no container object provided"));
//        }
//       todo if (localUrlString == null) {
//            System.out.println("getImdiObject: " + localNodeText + " : " + localUrlString);
//       end todo }
        ArbilDataNode currentDataNode = null;
        if (localUri != null && localUri.toString().length() > 0) {
            currentDataNode = getArbilDataNodeWithoutLoading(localUri);
            if (!currentDataNode.getParentDomNode().isDataLoaded() && !currentDataNode.isLoading()) {
                if (ArbilDataNode.isStringChildNode(currentDataNode.getUrlString())) {
                    // cause the parent node to be loaded
                    currentDataNode.getParentDomNode();
                } else if (MetadataFormat.isPathMetadata(currentDataNode.getUrlString()) || ArbilDataNode.isPathHistoryFile(currentDataNode.getUrlString())) {
                    threadManager.addNodeToQueue(currentDataNode);
                } else if (!MetadataFormat.isPathMetadata(currentDataNode.getUrlString())) {
//                    currentImdiObject.clearIcon(); // do not do this
                }
            }
            if (registeringObject != null && registeringObject instanceof ArbilDataNodeContainer) {
                currentDataNode.registerContainer((ArbilDataNodeContainer) registeringObject);
            }
        }
        return currentDataNode;
    }

    // return the node only if it has already been loaded otherwise return null
    @Override
    public ArbilDataNode getArbilDataNodeOnlyIfLoaded(URI arbilUri) {
//        String localUrlString = ImdiTreeObject.conformStringToUrl(imdiUrl).toString();
        arbilUri = ArbilDataNode.normaliseURI(arbilUri);
        return arbilHashTable.get(arbilUri.toString());
    }

    // reload the node only if it has already been loaded otherwise ignore
    @Override
    public void requestReloadOnlyIfLoaded(URI arbilUri) {
//        String localUrlString = ImdiTreeObject.conformStringToUrl(imdiUrl).toString();
        arbilUri = ArbilDataNode.normaliseURI(arbilUri);
        ArbilDataNode currentDataNode = arbilHashTable.get(arbilUri.toString());
        if (currentDataNode != null) {
            requestReload(currentDataNode);
        }
    }

    // reload the node or if it is an imdichild node then reload its parent
    @Override
    public void requestReload(ArbilDataNode currentDataNode) {
        if (currentDataNode.isChildNode()) {
            currentDataNode = currentDataNode.getParentDomNode();
        }
        removeNodesNeedingSave(currentDataNode);
//        if (ImdiTreeObject.isStringImdi(currentImdiObject.getUrlString()) || ImdiTreeObject.isStringImdiHistoryFile(currentImdiObject.getUrlString())) {
        threadManager.addNodeToQueue(currentDataNode);
//        }
    }

    @Override
    public void requestReloadAllNodes() {
        for (ArbilDataNode currentDataNode : arbilHashTable.values()) { // Should use copy of collection (e.g. toArray) to prevent concurrency exceptions?
            requestReload(currentDataNode);
        }
    }

    @Override
    public void startLoaderThreads() {
        threadManager.startLoaderThreads();
    }

    public void stopLoaderThreads() {
        threadManager.stopLoaderThreads();
    }

    @Override
    protected void finalize() throws Throwable {
        // stop the thread
        threadManager.setContinueThread(false);
        super.finalize();
    }

    @Override
    public void addNodeNeedingSave(ArbilDataNode nodeToSave) {
        nodeToSave = nodeToSave.getParentDomNode();
        if (!nodesNeedingSave.contains(nodeToSave)) {
            nodesNeedingSave.add(nodeToSave);
        }
    }

    @Override
    public void removeNodesNeedingSave(ArbilDataNode savedNode) {
        nodesNeedingSave.remove(savedNode);
    }

    @Override
    public ArbilDataNode[] getNodesNeedSave() {
        return nodesNeedingSave.toArray(new ArbilDataNode[]{});
    }

    @Override
    public boolean nodesNeedSave() {
        return nodesNeedingSave.size() > 0;
    }

    @Override
    public synchronized void saveNodesNeedingSave(boolean updateIcons) {
        // this is syncronised to avoid issues from the key repeat on linux which fails to destinguish between key up events and key repeat events
        while (nodesNeedingSave.size() > 0) {
            // remove the node from the save list not in the save function because otherwise if the save fails the application will lock up
            ArbilDataNode currentNode = nodesNeedingSave.remove(0);
            if (currentNode != null) {
                currentNode.saveChangesToCache(updateIcons); // saving removes the node from the nodesNeedingSave vector via removeNodesNeedingSave
                if (updateIcons) {
                    requestReload(currentNode);
                }
            }
        }
    }

    /**
     * @return the schemaCheckLocalFiles
     */
    @Override
    public boolean isSchemaCheckLocalFiles() {
        return threadManager.isSchemaCheckLocalFiles();
    }

    /**
     * @param schemaCheckLocalFiles the schemaCheckLocalFiles to set
     */
    @Override
    public void setSchemaCheckLocalFiles(boolean schemaCheckLocalFiles) {
        threadManager.setSchemaCheckLocalFiles(schemaCheckLocalFiles);
    }
}
