package nl.mpi.arbil.data;

import java.awt.Component;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import nl.mpi.arbil.ui.ArbilTreePanels;
import nl.mpi.arbil.ArbilIcons;
import nl.mpi.arbil.ui.ArbilTree;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.util.BugCatcher;
import nl.mpi.arbil.util.MessageDialogHandler;

/**
 * Document   : TreeHelper
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
public class TreeHelper {

    private static MessageDialogHandler messageDialogHandler;

    public static void setMessageDialogHandler(MessageDialogHandler handler) {
        messageDialogHandler = handler;
    }

    private static BugCatcher bugCatcher;

    public static void setBugCatcher(BugCatcher bugCatcherInstance){
        bugCatcher = bugCatcherInstance;
    }

    public DefaultTreeModel localCorpusTreeModel;
    public DefaultTreeModel remoteCorpusTreeModel;
    public DefaultTreeModel localDirectoryTreeModel;
    public DefaultTreeModel favouritesTreeModel;
    private DefaultMutableTreeNode localCorpusRootNode;
    private DefaultMutableTreeNode remoteCorpusRootNode;
    private DefaultMutableTreeNode localDirectoryRootNode;
    private DefaultMutableTreeNode favouritesRootNode;
    public ArbilTreePanels arbilTreePanel;
    public ArbilDataNode[] remoteCorpusNodes = new ArbilDataNode[]{};
    public ArbilDataNode[] localCorpusNodes = new ArbilDataNode[]{};
    public ArbilDataNode[] localFileNodes = new ArbilDataNode[]{};
    public ArbilDataNode[] favouriteNodes = new ArbilDataNode[]{};
    static private TreeHelper singleInstance = null;
    Vector<DefaultMutableTreeNode> treeNodeSortQueue = new Vector<DefaultMutableTreeNode>(); // used in the tree node sort thread
    boolean treeNodeSortQueueRunning = false; // used in the tree node sort thread
    public boolean showHiddenFilesInTree = false;

    static synchronized public TreeHelper getSingleInstance() {
//        System.out.println("TreeHelper getSingleInstance");
        if (singleInstance == null) {
            singleInstance = new TreeHelper();
        }
        return singleInstance;
    }

    private TreeHelper() {
        localCorpusRootNode = new DefaultMutableTreeNode();
        remoteCorpusRootNode = new DefaultMutableTreeNode();
        localDirectoryRootNode = new DefaultMutableTreeNode();
        favouritesRootNode = new DefaultMutableTreeNode();

        localCorpusTreeModel = new DefaultTreeModel(localCorpusRootNode, true);
        remoteCorpusTreeModel = new DefaultTreeModel(remoteCorpusRootNode, true);
        localDirectoryTreeModel = new DefaultTreeModel(localDirectoryRootNode, true);
        favouritesTreeModel = new DefaultTreeModel(favouritesRootNode, true);
        // load any locations from the previous file formats
        //LinorgFavourites.getSingleInstance().convertOldFormatLocationLists();

        loadLocationsList();
    }

    public ArbilTree getTreeForNode(DefaultMutableTreeNode nodeToTest) {
        if (nodeToTest.getRoot().equals(remoteCorpusRootNode)) {
            return arbilTreePanel.remoteCorpusTree;
        }
        if (nodeToTest.getRoot().equals(localCorpusRootNode)) {
            return arbilTreePanel.localCorpusTree;
        }
        if (nodeToTest.getRoot().equals(localDirectoryRootNode)) {
            return arbilTreePanel.localDirectoryTree;
        }
        return arbilTreePanel.favouritesTree;
    }

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

    public boolean componentIsTheLocalCorpusTree(Component componentToTest) {
        return componentToTest.equals(arbilTreePanel.localCorpusTree);
        //return localCorpusTree.getName().equals(componentToTest.getName());
    }

    public boolean componentIsTheFavouritesTree(Component componentToTest) {
        return componentToTest.equals(arbilTreePanel.favouritesTree);
    }

    public void setTrees(ArbilTreePanels arbilTreePanelLocal) {
        arbilTreePanel = arbilTreePanelLocal;
//            ImdiTree tempRemoteCorpusTree, ImdiTree tempLocalCorpusTree, ImdiTree tempLocalDirectoryTree) {
        remoteCorpusRootNode.setUserObject(new JLabel("Remote Corpus", ArbilIcons.getSingleInstance().serverIcon, JLabel.LEFT));
        localCorpusRootNode.setUserObject(new JLabel("Local Corpus", ArbilIcons.getSingleInstance().directoryIcon, JLabel.LEFT));
        localDirectoryRootNode.setUserObject(new JLabel("Working Directories", ArbilIcons.getSingleInstance().computerIcon, JLabel.LEFT));
        favouritesRootNode.setUserObject(new JLabel("Favourites", ArbilIcons.getSingleInstance().favouriteIcon, JLabel.LEFT));

        arbilTreePanel.remoteCorpusTree.setName("RemoteCorpusTree");
        arbilTreePanel.localCorpusTree.setName("LocalCorpusTree");
        arbilTreePanel.localDirectoryTree.setName("LocalDirectoryTree");
        arbilTreePanel.favouritesTree.setName("FavouritesTree");

        applyRootLocations();
    }

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
                remoteCorpusNodesSet.add(ArbilDataNodeLoader.getSingleInstance().getArbilDataNode(null, new URI(currentUrlString)));
            } catch (URISyntaxException ex) {
                bugCatcher.logError(ex);
            }
        }
        remoteCorpusNodes = remoteCorpusNodesSet.toArray(new ArbilDataNode[]{});
        return remoteCorpusNodesSet.size();
    }

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
            Vector<String> locationsList = new Vector<String>(); // this vector is kept for backwards compatability
            for (String currentLocation : locationsSet) {
                locationsList.add(URLDecoder.decode(currentLocation, "UTF-8"));
            }
            //LinorgSessionStorage.getSingleInstance().saveObject(locationsList, "locationsList");
            ArbilSessionStorage.getSingleInstance().saveStringArray("locationsList", locationsList.toArray(new String[]{}));
            System.out.println("saved locationsList");
        } catch (Exception ex) {
            bugCatcher.logError(ex);
//            System.out.println("save locationsList exception: " + ex.getMessage());
        }
    }

    public void loadLocationsList() {
        try {
            System.out.println("loading locationsList");
            String[] locationsArray = ArbilSessionStorage.getSingleInstance().loadStringArray("locationsList");
            if (locationsArray == null) {
                addDefaultCorpusLocations();
            } else {
                Vector<ArbilDataNode> remoteCorpusNodesVector = new Vector<ArbilDataNode>();
                Vector<ArbilDataNode> localCorpusNodesVector = new Vector<ArbilDataNode>();
                Vector<ArbilDataNode> localFileNodesVector = new Vector<ArbilDataNode>();
                Vector<ArbilDataNode> favouriteNodesVector = new Vector<ArbilDataNode>();

                // this also removes all locations and replaces them with normalised paths
                for (String currentLocationString : locationsArray) {
                    URI currentLocation = ArbilDataNode.conformStringToUrl(currentLocationString);
                    ArbilDataNode currentTreeObject = ArbilDataNodeLoader.getSingleInstance().getArbilDataNode(null, currentLocation);
                    if (currentTreeObject.isLocal()) {
                        if (currentTreeObject.isFavorite()) {
                            favouriteNodesVector.add(currentTreeObject);
                        } else if (ArbilSessionStorage.getSingleInstance().pathIsInsideCache(currentTreeObject.getFile())) {
                            if (currentTreeObject.isMetaDataNode() && !currentTreeObject.isChildNode()) {
                                localCorpusNodesVector.add(currentTreeObject);
                            }
                        } else {
                            localFileNodesVector.add(currentTreeObject);
                        }
                    } else {
                        remoteCorpusNodesVector.add(currentTreeObject);
                    }
                }
                remoteCorpusNodes = remoteCorpusNodesVector.toArray(new ArbilDataNode[]{});
                localCorpusNodes = localCorpusNodesVector.toArray(new ArbilDataNode[]{});
                localFileNodes = localFileNodesVector.toArray(new ArbilDataNode[]{});
                favouriteNodes = favouriteNodesVector.toArray(new ArbilDataNode[]{});
            }
        } catch (Exception ex) {
//            System.out.println("load locationsList failed: " + ex.getMessage());
            bugCatcher.logError(ex);
//            locationsList.add("http://corpus1.mpi.nl/IMDI/metadata/IMDI.imdi");
//            locationsList.add("http://corpus1.mpi.nl/qfs1/media-archive/Corpusstructure/MPI.imdi");
//            //locationsList.add("file:///data1/media-archive-copy/Corpusstructure/MPI.imdi");
//            locationsList.add("file:///data1/media-archive-copy/TestWorkingDirectory/");
//            //locationsList.add("http://lux16.mpi.nl/corpora/ac-ESF/Info/ladfc2.txt");
//            //locationsList.add("file:///data1/media-archive-copy/Corpusstructure/MPI.imdi");
//            locationsList.add("http://corpus1.mpi.nl/qfs1/media-archive/Comprehension/Elizabeth_Johnson/Corpusstructure/1.imdi");
//            //locationsList.add("file:///data1/media-archive-copy/TestWorkingDirectory/");

//            System.out.println("created new locationsList");
        }
        showHiddenFilesInTree = ArbilSessionStorage.getSingleInstance().loadBoolean("showHiddenFilesInTree", showHiddenFilesInTree);
    }

    public void setShowHiddenFilesInTree(boolean showState) {
        showHiddenFilesInTree = showState;
        reloadNodesInTree(localDirectoryRootNode);
        try {
            ArbilSessionStorage.getSingleInstance().saveBoolean("showHiddenFilesInTree", showHiddenFilesInTree);
        } catch (Exception ex) {
            System.out.println("save showHiddenFilesInTree failed");
        }
    }

    public void addLocationGui(URI addableLocation) {
        if (!addLocation(addableLocation)) {
            // alert the user when the node already exists and cannot be added again
            messageDialogHandler.addMessageDialogToQueue("The location already exists and cannot be added again", "Add location");
        }
        applyRootLocations();
        //locationSettingsTable.setModel(guiHelper.getLocationsTableModel());
    }

    public boolean addLocation(URI addedLocation) {
        System.out.println("addLocation: " + addedLocation.toString());
        // make sure the added location url matches that of the imdi node format
        ArbilDataNode addedLocationObject = ArbilDataNodeLoader.getSingleInstance().getArbilDataNode(null, addedLocation);
        if (addedLocationObject != null) {
            saveLocations(new ArbilDataNode[]{addedLocationObject}, null);
            loadLocationsList();
            return true;
        }
        return false;
    }

    public void removeLocation(ArbilDataNode removeObject) {
        if (removeObject != null) {
            saveLocations(null, new ArbilDataNode[]{removeObject});
            removeObject.removeFromAllContainers();
            loadLocationsList();
        }
    }

    public void removeLocation(URI removeLocation) {
        System.out.println("removeLocation: " + removeLocation);
        removeLocation(ArbilDataNodeLoader.getSingleInstance().getArbilDataNode(null, removeLocation));
    }

    private void reloadNodesInTree(DefaultMutableTreeNode parentTreeNode) {
        // this will reload all nodes in a tree but not create any new child nodes
        for (Enumeration<DefaultMutableTreeNode> childNodesEnum = parentTreeNode.children(); childNodesEnum.hasMoreElements();) {
            reloadNodesInTree(childNodesEnum.nextElement());
        }
        if (parentTreeNode.getUserObject() instanceof ArbilDataNode) {
            if (((ArbilDataNode) parentTreeNode.getUserObject()).dataLoaded) {
                ((ArbilDataNode) parentTreeNode.getUserObject()).reloadNode();
            }
        }
    }

    public boolean locationsHaveBeenAdded() {
        return localCorpusNodes.length > 0;
    }

    public void applyRootLocations() {
        System.out.println("applyRootLocations");
        arbilTreePanel.localCorpusTree.rootNodeChildren = localCorpusNodes;
        arbilTreePanel.remoteCorpusTree.rootNodeChildren = remoteCorpusNodes;
        arbilTreePanel.localDirectoryTree.rootNodeChildren = localFileNodes;
        arbilTreePanel.favouritesTree.rootNodeChildren = favouriteNodes;
        arbilTreePanel.localCorpusTree.requestResort();
        arbilTreePanel.remoteCorpusTree.requestResort();
        arbilTreePanel.localDirectoryTree.requestResort();
        arbilTreePanel.favouritesTree.requestResort();
    }

    public DefaultMutableTreeNode getLocalCorpusTreeSingleSelection() {
        System.out.println("localCorpusTree: " + arbilTreePanel.localCorpusTree);
        return (DefaultMutableTreeNode) arbilTreePanel.localCorpusTree.getSelectionPath().getLastPathComponent();
    }

    public void deleteNode(Object sourceObject) {
        System.out.println("deleteNode: " + sourceObject);
        DefaultMutableTreeNode selectedTreeNode = null;
        DefaultMutableTreeNode parentTreeNode = null;
        if (sourceObject == arbilTreePanel.localCorpusTree) {
            TreePath currentNodePaths[] = ((ArbilTree) sourceObject).getSelectionPaths();
            int toDeleteCount = 0;
            // count the number of nodes to delete
            for (TreePath currentNodePath : currentNodePaths) {
                if (currentNodePath != null) {
                    selectedTreeNode = (DefaultMutableTreeNode) currentNodePath.getLastPathComponent();
                    Object userObject = selectedTreeNode.getUserObject();
                    if (userObject instanceof ArbilDataNode) {
                        if (((ArbilDataNode) userObject).isEmptyMetaNode()) {
                            toDeleteCount = toDeleteCount + ((ArbilDataNode) userObject).getChildCount();
                        } else {
                            toDeleteCount++;
                        }
                    }
                }
            }
            if(JOptionPane.OK_OPTION == messageDialogHandler.showConfirmDialog("Delete " + toDeleteCount + " nodes?", "Delete", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE)) {
                Vector<ArbilDataNode> dataNodesToRemove = new Vector<ArbilDataNode>();
                Hashtable<ArbilDataNode, Vector<ArbilDataNode>> dataNodesDeleteList = new Hashtable<ArbilDataNode, Vector<ArbilDataNode>>();
                Hashtable<ArbilDataNode, Vector<String>> childNodeDeleteList = new Hashtable<ArbilDataNode, Vector<String>>();
                for (TreePath currentNodePath : currentNodePaths) {
                    if (currentNodePath != null) {
                        selectedTreeNode = (DefaultMutableTreeNode) currentNodePath.getLastPathComponent();
                        Object userObject = selectedTreeNode.getUserObject();
                        System.out.println("trying to delete: " + userObject);
                        if (currentNodePath.getPath().length == 2) {
                            System.out.println("removing by location");
                            removeLocation((ArbilDataNode) selectedTreeNode.getUserObject());
                            applyRootLocations();
                        } else {
                            System.out.println("deleting from parent");
                            parentTreeNode = (DefaultMutableTreeNode) selectedTreeNode.getParent();
                            if (parentTreeNode != null) {
                                System.out.println("found parent to remove from");
                                ArbilDataNode parentDataNode = (ArbilDataNode) parentTreeNode.getUserObject();
                                ArbilDataNode childDataNode = (ArbilDataNode) selectedTreeNode.getUserObject();
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
                                } else {
                                    // add the parent and the child node to the deletelist
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
                        }
// todo: this fixes some of the nodes left after a delete EXCEPT; for example, the "actors" node when all the actors are deleted
//                        TreeHelper.getSingleInstance().removeAndDetatchDescendantNodes(selectedTreeNode);
                        // make a list of all child nodes so that they can be removed from any tables etc
                        dataNodesToRemove.add((ArbilDataNode) userObject);
                        ((ArbilDataNode) userObject).getAllChildren(dataNodesToRemove);
                    }
                }
                for (ArbilDataNode currentParent : childNodeDeleteList.keySet()) {
                    System.out.println("deleting by child xml id link");
                    // TODO: There is an issue when deleting child nodes that the remaining nodes xml path (x) will be incorrect as will the xmlnode id hence the node in a table may be incorrect after a delete
                    //currentParent.deleteFromDomViaId(((Vector<String>) imdiChildNodeDeleteList.get(currentParent)).toArray(new String[]{}));
                    ArbilComponentBuilder componentBuilder = new ArbilComponentBuilder();
                    boolean result = componentBuilder.removeChildNodes(currentParent, (childNodeDeleteList.get(currentParent)).toArray(new String[]{}));
                    if (result) {
                        currentParent.reloadNode();
                    } else {
                        messageDialogHandler.addMessageDialogToQueue("Error deleting node, check the log file via the help menu for more information.", "Delete Node");
                    }
                    //bugCatcher.logError(new Exception("deleteFromDomViaId"));
                }
                for (ArbilDataNode currentParent : dataNodesDeleteList.keySet()) {
                    System.out.println("deleting by corpus link");
                    currentParent.deleteCorpusLink(((Vector<ArbilDataNode>) dataNodesDeleteList.get(currentParent)).toArray(new ArbilDataNode[]{}));
                }
//                // todo: this could probably be removed
//                for (Enumeration<ImdiTreeObject> deletedNodesEnum = imdiNodesToRemove.elements(); deletedNodesEnum.hasMoreElements();) {
//                    // remove the deleted node from all tables
//                    // todo: this is also done in deleteCorpusLink and need not be here
//                    ImdiTreeObject currentDeletedNode = deletedNodesEnum.nextElement();
//                    for (Object currentContainer : currentDeletedNode.getRegisteredContainers()) {
//                        // this is required here even though it is now also done in the reloading process
//                        if (currentContainer instanceof ImdiTableModel) {
//                            ((ImdiTableModel) currentContainer).removeImdiObjects(new ImdiTreeObject[]{currentDeletedNode});
//                        }
//                    }
//                }
            }
        } else {
            System.out.println("cannot delete from this tree");
        }
    }

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
}
