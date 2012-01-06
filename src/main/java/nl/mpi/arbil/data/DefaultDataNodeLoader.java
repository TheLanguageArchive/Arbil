package nl.mpi.arbil.data;

import java.net.URI;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Document   : ArbilDataNodeLoader formerly known as ImdiLoader
 * Created on : Dec 30, 2008, 3:04:39 PM
 * @author Peter.Withers@mpi.nl
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class DefaultDataNodeLoader implements DataNodeLoader {

    private Hashtable<String, ArbilDataNode> arbilHashTable = new Hashtable<String, ArbilDataNode>();
    private Vector<ArbilDataNode> nodesNeedingSave = new Vector<ArbilDataNode>();
    private DataNodeLoaderThreadManager threadManager;
    private ArbilDataNodeService dataNodeService;

    public DefaultDataNodeLoader(DataNodeLoaderThreadManager loaderThreadManager) {
	System.out.println("ArbilDataNodeLoader init");
	threadManager = loaderThreadManager;
    }
    
    protected final void setDataNodeService(ArbilDataNodeService dataNodeService){
	this.dataNodeService = dataNodeService;
    }

//    public ImdiTreeObject isImdiObjectLoaded(String localUrlString) {
//        localUrlString = ImdiTreeObject.conformStringToUrl(localUrlString).toString();
//        return imdiHashTable.get(localUrlString);
//    }
    @Override
    public ArbilDataNode getArbilDataNodeWithoutLoading(URI localUri) {
	ArbilDataNode currentDataNode = null;
	if (localUri != null) {
	    localUri = ArbilDataNodeService.normaliseURI(localUri);
	    // correct any variations in the url string
//            localUri = ImdiTreeObject.conformStringToUrl(localUri).toString();
	    currentDataNode = arbilHashTable.get(localUri.toString());
	    if (currentDataNode == null) {
		currentDataNode = new ArbilDataNode(dataNodeService, localUri);
		arbilHashTable.put(localUri.toString(), currentDataNode);
	    }
	}
	return currentDataNode;
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
		    currentDataNode.updateLoadingState(+1);
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
	arbilUri = ArbilDataNodeService.normaliseURI(arbilUri);
	return arbilHashTable.get(arbilUri.toString());
    }

    // reload the node only if it has already been loaded otherwise ignore
    @Override
    public void requestReloadOnlyIfLoaded(URI arbilUri) {
//        String localUrlString = ImdiTreeObject.conformStringToUrl(imdiUrl).toString();
	arbilUri = ArbilDataNodeService.normaliseURI(arbilUri);
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

    public ArbilDataNode createNewDataNode(URI uri) {
	return new ArbilDataNode(dataNodeService, uri);
    }

    /**
     * @return the threadManager
     */
    protected DataNodeLoaderThreadManager getThreadManager() {
	return threadManager;
    }
}
