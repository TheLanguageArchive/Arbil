package nl.mpi.arbil.data;

import nl.mpi.arbil.util.TreeHelper;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import nl.mpi.arbil.ArbilIcons;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcher;
import nl.mpi.arbil.util.MessageDialogHandler;

/**
 * Document   : ArbilTreeHelper
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
public abstract class AbstractTreeHelper implements TreeHelper {

    private DefaultTreeModel localCorpusTreeModel;
    private DefaultTreeModel remoteCorpusTreeModel;
    private DefaultTreeModel localDirectoryTreeModel;
    private DefaultTreeModel favouritesTreeModel;
    protected DefaultMutableTreeNode localCorpusRootNode;
    protected DefaultMutableTreeNode remoteCorpusRootNode;
    protected DefaultMutableTreeNode localDirectoryRootNode;
    protected DefaultMutableTreeNode favouritesRootNode;
    private ArbilDataNode[] remoteCorpusNodes = new ArbilDataNode[]{};
    private ArbilDataNode[] localCorpusNodes = new ArbilDataNode[]{};
    private ArbilDataNode[] localFileNodes = new ArbilDataNode[]{};
    private ArbilDataNode[] favouriteNodes = new ArbilDataNode[]{};
    private boolean showHiddenFilesInTree = false;
    private static MessageDialogHandler messageDialogHandler;

    public static void setMessageDialogHandler(MessageDialogHandler handler) {
	messageDialogHandler = handler;
    }
    private static BugCatcher bugCatcher;

    public static void setBugCatcher(BugCatcher bugCatcherInstance) {
	bugCatcher = bugCatcherInstance;
    }

    protected abstract SessionStorage getSessionStorage();
    private static DataNodeLoader dataNodeLoader;

    public static void setDataNodeLoader(DataNodeLoader dataNodeLoaderInstance) {
	dataNodeLoader = dataNodeLoaderInstance;
    }

    protected final void initTrees() {
	initRootNodes();
	initTreeModels();
    }

    protected void initTreeModels() {
	localCorpusTreeModel = new DefaultTreeModel(localCorpusRootNode, true);
	remoteCorpusTreeModel = new DefaultTreeModel(remoteCorpusRootNode, true);
	localDirectoryTreeModel = new DefaultTreeModel(localDirectoryRootNode, true);
	favouritesTreeModel = new DefaultTreeModel(favouritesRootNode, true);
    }

    private void initRootNodes() {
	localCorpusRootNode = new DefaultMutableTreeNode(localCorpusRootNodeObject);
	remoteCorpusRootNode = new DefaultMutableTreeNode(remoteCorpusRootNodeObject);
	localDirectoryRootNode = new DefaultMutableTreeNode(localDirectoryRootNodeObject);
	favouritesRootNode = new DefaultMutableTreeNode(favouritesRootNodeObject);
    }

    @Override
    public DefaultTreeModel getModelForNode(DefaultMutableTreeNode nodeToTest) {
	if (nodeToTest.getRoot().equals(remoteCorpusRootNode)) {
	    return remoteCorpusTreeModel;
	}
	if (nodeToTest.getRoot().equals(localCorpusRootNode)) {
	    return localCorpusTreeModel;
	}
	if (nodeToTest.getRoot().equals(localDirectoryRootNode)) {
	    return localDirectoryTreeModel;
	}
	return favouritesTreeModel;
    }

    @Override
    public int addDefaultCorpusLocations() {
	HashSet<ArbilDataNode> remoteCorpusNodesSet = new HashSet<ArbilDataNode>();
	remoteCorpusNodesSet.addAll(Arrays.asList(remoteCorpusNodes));
	for (String currentUrlString : new String[]{
		    "http://corpus1.mpi.nl/IMDI/metadata/IMDI.imdi",
		    "http://corpus1.mpi.nl/qfs1/media-archive/Corpusstructure/MPI.imdi",
		    //                    "http://corpus1.mpi.nl/qfs1/media-archive/silang_data/Corpusstructure/1.imdi",
		    "http://corpus1.mpi.nl/qfs1/media-archive/Corpusstructure/sign_language.imdi"
//                    "http://corpus1.mpi.nl/qfs1/media-archive/dobes_data/ChintangPuma/Chintang/Conversation/Metadata/phidang_talk.imdi",
//                    "http://corpus1.mpi.nl/qfs1/media-archive/silang_data/Corpusstructure/1-03.imdi",
//                    "http://corpus1.mpi.nl/qfs1/media-archive/dobes_data/ECLING/Corpusstructure/ECLING.imdi",
//                    "http://corpus1.mpi.nl/qfs1/media-archive/dobes_data/Center/Corpusstructure/center.imdi",
//                    "http://corpus1.mpi.nl/qfs1/media-archive/dobes_data/Teop/Corpusstructure/1.imdi",
//                    "http://corpus1.mpi.nl/qfs1/media-archive/dobes_data/Waimaa/Corpusstructure/1.imdi",
//                    "http://corpus1.mpi.nl/qfs1/media-archive/dobes_data/Beaver/Corpusstructure/Beaver.imdi"
		}) {
	    try {
		remoteCorpusNodesSet.add(dataNodeLoader.getArbilDataNode(null, new URI(currentUrlString)));
	    } catch (URISyntaxException ex) {
		bugCatcher.logError(ex);
	    }
	}
	remoteCorpusNodes = remoteCorpusNodesSet.toArray(new ArbilDataNode[]{});
	return remoteCorpusNodesSet.size();
    }

    @Override
    public void saveLocations(ArbilDataNode[] nodesToAdd, ArbilDataNode[] nodesToRemove) {
	try {
	    HashSet<String> locationsSet = new HashSet<String>();
	    for (ArbilDataNode[] currentTreeArray : new ArbilDataNode[][]{remoteCorpusNodes, localCorpusNodes, localFileNodes, favouriteNodes}) {
		for (ArbilDataNode currentLocation : currentTreeArray) {
		    locationsSet.add(currentLocation.getUrlString());
		}
	    }
	    if (nodesToAdd != null) {
		for (ArbilDataNode currentAddable : nodesToAdd) {
		    locationsSet.add(currentAddable.getUrlString());
		}
	    }
	    if (nodesToRemove != null) {
		for (ArbilDataNode currentRemoveable : nodesToRemove) {
		    locationsSet.remove(currentRemoveable.getUrlString());
		}
	    }
	    ArrayList<String> locationsList = new ArrayList<String>(); // this vector is kept for backwards compatability
	    for (String currentLocation : locationsSet) {
		locationsList.add(URLDecoder.decode(currentLocation, "UTF-8"));
	    }
	    //LinorgSessionStorage.getSingleInstance().saveObject(locationsList, "locationsList");
	    getSessionStorage().saveStringArray("locationsList", locationsList.toArray(new String[]{}));
	    System.out.println("saved locationsList");
	} catch (Exception ex) {
	    bugCatcher.logError(ex);
//            System.out.println("save locationsList exception: " + ex.getMessage());
	}
    }

    @Override
    public final void loadLocationsList() {
	System.out.println("loading locationsList");
	String[] locationsArray = null;
	try {
	    locationsArray = getSessionStorage().loadStringArray("locationsList");
	} catch (IOException ex) {
	    bugCatcher.logError(ex);
	    messageDialogHandler.addMessageDialogToQueue("Could not find or load locations. Adding default locations.", "Error");
	}
	if (locationsArray == null) {
	    addDefaultCorpusLocations();
	} else {
	    ArrayList<ArbilDataNode> remoteCorpusNodesList = new ArrayList<ArbilDataNode>();
	    ArrayList<ArbilDataNode> localCorpusNodesList = new ArrayList<ArbilDataNode>();
	    ArrayList<ArbilDataNode> localFileNodesList = new ArrayList<ArbilDataNode>();
	    ArrayList<ArbilDataNode> favouriteNodesList = new ArrayList<ArbilDataNode>();

	    int failedLoads = 0;
	    // this also removes all locations and replaces them with normalised paths
	    for (String currentLocationString : locationsArray) {
		URI currentLocation = ArbilDataNode.conformStringToUrl(currentLocationString);
		if (currentLocation == null) {
		    bugCatcher.logError("Could conform string to url: " + currentLocationString, null);
		    failedLoads++;
		} else {
		    try {
			ArbilDataNode currentTreeObject = dataNodeLoader.getArbilDataNode(null, currentLocation);
			if (currentTreeObject.isLocal()) {
			    if (currentTreeObject.isFavorite()) {
				favouriteNodesList.add(currentTreeObject);
			    } else if (getSessionStorage().pathIsInsideCache(currentTreeObject.getFile())) {
				if (currentTreeObject.isMetaDataNode() && !currentTreeObject.isChildNode()) {
				    localCorpusNodesList.add(currentTreeObject);
				}
			    } else {
				localFileNodesList.add(currentTreeObject);
			    }
			} else {
			    remoteCorpusNodesList.add(currentTreeObject);
			}
		    } catch (Exception ex) {
			bugCatcher.logError("Failure in trying to load " + currentLocationString, ex);
			failedLoads++;
		    }
		}
	    }

	    if (failedLoads > 0) {
		messageDialogHandler.addMessageDialogToQueue("Failed to load " + failedLoads + " locations. See error log for details.", "Warning");
	    }

	    remoteCorpusNodes = remoteCorpusNodesList.toArray(new ArbilDataNode[]{});
	    localCorpusNodes = localCorpusNodesList.toArray(new ArbilDataNode[]{});
	    localFileNodes = localFileNodesList.toArray(new ArbilDataNode[]{});
	    favouriteNodes = favouriteNodesList.toArray(new ArbilDataNode[]{});
	}
	showHiddenFilesInTree = getSessionStorage().loadBoolean("showHiddenFilesInTree", showHiddenFilesInTree);
    }

    @Override
    public void setShowHiddenFilesInTree(boolean showState) {
	showHiddenFilesInTree = showState;
	reloadNodesInTree(localDirectoryRootNode);
	try {
	    getSessionStorage().saveBoolean("showHiddenFilesInTree", showHiddenFilesInTree);
	} catch (Exception ex) {
	    System.out.println("save showHiddenFilesInTree failed");
	}
    }

    @Override
    public boolean addLocation(URI addedLocation) {
	System.out.println("addLocation: " + addedLocation.toString());
	// make sure the added location url matches that of the imdi node format
	ArbilDataNode addedLocationObject = dataNodeLoader.getArbilDataNode(null, addedLocation);
	if (addedLocationObject != null) {
	    saveLocations(new ArbilDataNode[]{addedLocationObject}, null);
	    loadLocationsList();
	    return true;
	}
	return false;
    }

    @Override
    public void removeLocation(ArbilDataNode removeObject) {
	if (removeObject != null) {
	    saveLocations(null, new ArbilDataNode[]{removeObject});
	    removeObject.removeFromAllContainers();
	    loadLocationsList();
	}
    }

    @Override
    public void removeLocation(URI removeLocation) {
	System.out.println("removeLocation: " + removeLocation);
	removeLocation(dataNodeLoader.getArbilDataNode(null, removeLocation));
    }

    private void reloadNodesInTree(DefaultMutableTreeNode parentTreeNode) {
	// this will reload all nodes in a tree but not create any new child nodes
	for (Enumeration<DefaultMutableTreeNode> childNodesEnum = parentTreeNode.children(); childNodesEnum.hasMoreElements();) {
	    reloadNodesInTree(childNodesEnum.nextElement());
	}
	if (parentTreeNode.getUserObject() instanceof ArbilDataNode) {
	    if (((ArbilDataNode) parentTreeNode.getUserObject()).isDataLoaded()) {
		((ArbilDataNode) parentTreeNode.getUserObject()).reloadNode();
	    }
	}
    }

    @Override
    public boolean locationsHaveBeenAdded() {
	return localCorpusNodes.length > 0;
    }

    @Override
    public abstract void applyRootLocations();

    @Override
    public abstract void deleteNodes(Object sourceObject);

    public void deleteChildNodes(ArbilDataNode parent, Collection<ArbilDataNode> children) {
	Map<ArbilDataNode, List<ArbilDataNode>> dataNodesDeleteList = new HashMap<ArbilDataNode, List<ArbilDataNode>>();
	Map<ArbilDataNode, List<String>> childNodeDeleteList = new HashMap<ArbilDataNode, List<String>>();
	Map<ArbilDataNode, List<ArbilDataNode>> cmdiLinksDeleteList = new HashMap<ArbilDataNode, List<ArbilDataNode>>();
	for (ArbilDataNode child : children) {
	    determineDeleteFromParent(child, parent, childNodeDeleteList, dataNodesDeleteList, cmdiLinksDeleteList);
	}
	// delete child nodes
	deleteNodesByChidXmlIdLink(childNodeDeleteList);
	// delete parent nodes
	deleteNodesByCorpusLink(dataNodesDeleteList);
	// delete cmdi links
	deleteCmdiLinks(cmdiLinksDeleteList);
    }

    protected void determineNodesToDelete(TreePath[] nodePaths, Map<ArbilDataNode, List<String>> childNodeDeleteList, Map<ArbilDataNode, List<ArbilDataNode>> dataNodesDeleteList, Map<ArbilDataNode, List<ArbilDataNode>> cmdiLinksDeleteList) {
	Vector<ArbilDataNode> dataNodesToRemove = new Vector<ArbilDataNode>();
	for (TreePath currentNodePath : nodePaths) {
	    if (currentNodePath != null) {
		DefaultMutableTreeNode selectedTreeNode = (DefaultMutableTreeNode) currentNodePath.getLastPathComponent();
		Object userObject = selectedTreeNode.getUserObject();
		System.out.println("trying to delete: " + userObject);
		if (currentNodePath.getPath().length == 2) {
		    // In locations list (i.e. child of root node)
		    System.out.println("removing by location");
		    removeLocation((ArbilDataNode) selectedTreeNode.getUserObject());
		    applyRootLocations();
		} else {
		    System.out.println("deleting from parent");
		    DefaultMutableTreeNode parentTreeNode = (DefaultMutableTreeNode) selectedTreeNode.getParent();
		    if (parentTreeNode != null) {
			System.out.println("found parent to remove from");
			ArbilDataNode parentDataNode = (ArbilDataNode) parentTreeNode.getUserObject();
			ArbilDataNode childDataNode = (ArbilDataNode) selectedTreeNode.getUserObject();
			determineDeleteFromParent(childDataNode, parentDataNode, childNodeDeleteList, dataNodesDeleteList, cmdiLinksDeleteList);
		    }
		}
		// todo: this fixes some of the nodes left after a delete EXCEPT; for example, the "actors" node when all the actors are deleted
		//                        ArbilTreeHelper.getSingleInstance().removeAndDetatchDescendantNodes(selectedTreeNode);
		// make a list of all child nodes so that they can be removed from any tables etc
		dataNodesToRemove.add((ArbilDataNode) userObject);
		((ArbilDataNode) userObject).getAllChildren(dataNodesToRemove);
	    }
	}
    }

    private void determineDeleteFromParent(ArbilDataNode childDataNode, ArbilDataNode parentDataNode, Map<ArbilDataNode, List<String>> childNodeDeleteList, Map<ArbilDataNode, List<ArbilDataNode>> dataNodesDeleteList, Map<ArbilDataNode, List<ArbilDataNode>> cmdiLinksDeleteList) {
	if (childDataNode.isChildNode()) {
	    // there is a risk of the later deleted nodes being outof sync with the xml, so we add them all to a list and delete all at once before the node is reloaded
	    if (!childNodeDeleteList.containsKey(childDataNode.getParentDomNode())) {
		childNodeDeleteList.put(childDataNode.getParentDomNode(), new Vector());
	    }
	    if (childDataNode.isEmptyMetaNode()) {
		for (ArbilDataNode metaChildNode : childDataNode.getChildArray()) {
		    childNodeDeleteList.get(childDataNode.getParentDomNode()).add(metaChildNode.getURI().getFragment());
		}
	    }
	    childNodeDeleteList.get(childDataNode.getParentDomNode()).add(childDataNode.getURI().getFragment());
	    childDataNode.removeFromAllContainers();
	} else if (parentDataNode.isCmdiMetaDataNode()) {
	    // CMDI link
	    if (!cmdiLinksDeleteList.containsKey(parentDataNode)) {
		cmdiLinksDeleteList.put(parentDataNode, new ArrayList<ArbilDataNode>());
	    }
	    cmdiLinksDeleteList.get(parentDataNode).add(childDataNode);
	} else {
	    // Not a child node or CMDI resource, ergo corpus child
	    // Add the parent and the child node to the deletelist
	    if (!dataNodesDeleteList.containsKey(parentDataNode)) {
		dataNodesDeleteList.put(parentDataNode, new Vector());
	    }
	    dataNodesDeleteList.get(parentDataNode).add(childDataNode);
	}
	// remove the deleted node from the favourites list if it is an imdichild node
	//                            if (userObject instanceof ImdiTreeObject) {
	//                                if (((ImdiTreeObject) userObject).isImdiChild()){
	//                                LinorgTemplates.getSingleInstance().removeFromFavourites(((ImdiTreeObject) userObject).getUrlString());
	//                                }
	//                            }
    }

    protected void deleteNodesByChidXmlIdLink(Map<ArbilDataNode, List<String>> childNodeDeleteList) {
	for (Entry<ArbilDataNode, List<String>> deleteEntry : childNodeDeleteList.entrySet()) {
	    ArbilDataNode currentParent = deleteEntry.getKey();
	    System.out.println("deleting by child xml id link");
	    // TODO: There is an issue when deleting child nodes that the remaining nodes xml path (x) will be incorrect as will the xmlnode id hence the node in a table may be incorrect after a delete
	    //currentParent.deleteFromDomViaId(((Vector<String>) imdiChildNodeDeleteList.get(currentParent)).toArray(new String[]{}));
	    ArbilComponentBuilder componentBuilder = new ArbilComponentBuilder();
	    boolean result = componentBuilder.removeChildNodes(currentParent, deleteEntry.getValue().toArray(new String[]{}));
	    if (result) {
		// Invalidate all thumbnails for the parent node. If MediaFiles are deleted, this prevents the thumbnails to get 'shifted' 
		// i.e. stick on the wrong note. This could perhaps be done a bit more sophisticated, it is not actually needed 
		// unless MediaFiles are deleted
		currentParent.invalidateThumbnails();
		currentParent.reloadNode();
	    } else {
		messageDialogHandler.addMessageDialogToQueue("Error deleting node, check the log file via the help menu for more information.", "Delete Node");
	    }
	    //bugCatcher.logError(new Exception("deleteFromDomViaId"));
	}
    }

    protected void deleteNodesByCorpusLink(Map<ArbilDataNode, List<ArbilDataNode>> dataNodesDeleteList) {
	for (Entry<ArbilDataNode, List<ArbilDataNode>> deleteEntry : dataNodesDeleteList.entrySet()) {
	    System.out.println("deleting by corpus link");
	    deleteEntry.getKey().deleteCorpusLink(deleteEntry.getValue().toArray(new ArbilDataNode[]{}));
	}
    }

    protected void deleteCmdiLinks(Map<ArbilDataNode, List<ArbilDataNode>> cmdiLinks) {
	ArbilComponentBuilder componentBuilder = new ArbilComponentBuilder();
	for (Entry<ArbilDataNode, List<ArbilDataNode>> deleteEntry : cmdiLinks.entrySet()) {
	    ArrayList<String> references = new ArrayList<String>(deleteEntry.getValue().size());
	    for (ArbilDataNode node : deleteEntry.getValue()) {
		references.add(node.getUrlString());
	    }
	    if (componentBuilder.removeResourceProxyReferences(deleteEntry.getKey(), references)) {
		deleteEntry.getKey().reloadNode();
	    } else {
		messageDialogHandler.addMessageDialogToQueue("Error deleting node, check the log file via the help menu for more information.", "Delete Node");
	    }
	}
    }

    @Override
    public void jumpToSelectionInTree(boolean silent, ArbilDataNode cellDataNode) {
	System.out.println("jumpToSelectionInTree: " + cellDataNode);
	if (cellDataNode != null) {
	    cellDataNode.scrollToRequested = true;
	    cellDataNode.clearIcon();
	} else {
	    if (!silent) {
		messageDialogHandler.addMessageDialogToQueue("The selected cell has no value or is not associated with a node in the tree", "Jump to in Tree");
	    }
	}
    }

    @Override
    public boolean isInFavouritesNodes(ArbilDataNode dataNode) {
	return Arrays.asList(favouriteNodes).contains(dataNode);
    }
    private ArbilRootNode localCorpusRootNodeObject = new ArbilRootNode("Local corpus", ArbilIcons.getSingleInstance().directoryIcon, true) {

	public ArbilDataNode[] getChildArray() {
	    return getLocalCorpusNodes();
	}
    };
    private ArbilRootNode remoteCorpusRootNodeObject = new ArbilRootNode("Remote corpus", ArbilIcons.getSingleInstance().serverIcon, false) {

	public ArbilDataNode[] getChildArray() {
	    return getRemoteCorpusNodes();
	}
    };
    private ArbilRootNode localDirectoryRootNodeObject = new ArbilRootNode("Working Directories", ArbilIcons.getSingleInstance().computerIcon, true) {

	public ArbilDataNode[] getChildArray() {
	    return getLocalFileNodes();
	}
    };
    private ArbilRootNode favouritesRootNodeObject = new ArbilRootNode("Favourites", ArbilIcons.getSingleInstance().favouriteIcon, true) {

	public ArbilDataNode[] getChildArray() {
	    return getFavouriteNodes();
	}
    };

    /**
     * @return the localCorpusTreeModel
     */
    @Override
    public DefaultTreeModel getLocalCorpusTreeModel() {
	return localCorpusTreeModel;
    }

    /**
     * @return the remoteCorpusTreeModel
     */
    @Override
    public DefaultTreeModel getRemoteCorpusTreeModel() {
	return remoteCorpusTreeModel;
    }

    /**
     * @return the localDirectoryTreeModel
     */
    @Override
    public DefaultTreeModel getLocalDirectoryTreeModel() {
	return localDirectoryTreeModel;
    }

    /**
     * @return the favouritesTreeModel
     */
    @Override
    public DefaultTreeModel getFavouritesTreeModel() {
	return favouritesTreeModel;
    }

    /**
     * @return the remoteCorpusNodes
     */
    @Override
    public ArbilDataNode[] getRemoteCorpusNodes() {
	return remoteCorpusNodes;
    }

    /**
     * @return the localCorpusNodes
     */
    @Override
    public ArbilDataNode[] getLocalCorpusNodes() {
	return localCorpusNodes;
    }

    /**
     * @return the localFileNodes
     */
    @Override
    public ArbilDataNode[] getLocalFileNodes() {
	return localFileNodes;
    }

    /**
     * @return the favouriteNodes
     */
    @Override
    public ArbilDataNode[] getFavouriteNodes() {
	return favouriteNodes;
    }

    /**
     * @return the showHiddenFilesInTree
     */
    @Override
    public boolean isShowHiddenFilesInTree() {
	return showHiddenFilesInTree;
    }
}
