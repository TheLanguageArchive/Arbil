package nl.mpi.arbil;

import nl.mpi.arbil.data.ImdiTreeObject;
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
import nl.mpi.arbil.clarin.CmdiComponentBuilder;
import nl.mpi.arbil.data.ImdiLoader;

/**
 * Document   : TreeHelper
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
public class TreeHelper {

    public DefaultTreeModel localCorpusTreeModel;
    public DefaultTreeModel remoteCorpusTreeModel;
    public DefaultTreeModel localDirectoryTreeModel;
    public DefaultTreeModel favouritesTreeModel;
    private DefaultMutableTreeNode localCorpusRootNode;
    private DefaultMutableTreeNode remoteCorpusRootNode;
    private DefaultMutableTreeNode localDirectoryRootNode;
    private DefaultMutableTreeNode favouritesRootNode;
    public ArbilTreePanels arbilTreePanel;
    public ImdiTreeObject[] remoteCorpusNodes = new ImdiTreeObject[]{};
    public ImdiTreeObject[] localCorpusNodes = new ImdiTreeObject[]{};
    public ImdiTreeObject[] localFileNodes = new ImdiTreeObject[]{};
    public ImdiTreeObject[] favouriteNodes = new ImdiTreeObject[]{};
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

    public ImdiTree getTreeForNode(DefaultMutableTreeNode nodeToTest) {
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
        remoteCorpusRootNode.setUserObject(new JLabel("Remote Corpus", ImdiIcons.getSingleInstance().serverIcon, JLabel.LEFT));
        localCorpusRootNode.setUserObject(new JLabel("Local Corpus", ImdiIcons.getSingleInstance().directoryIcon, JLabel.LEFT));
        localDirectoryRootNode.setUserObject(new JLabel("Working Directories", ImdiIcons.getSingleInstance().computerIcon, JLabel.LEFT));
        favouritesRootNode.setUserObject(new JLabel("Favourites", ImdiIcons.getSingleInstance().favouriteIcon, JLabel.LEFT));

        arbilTreePanel.remoteCorpusTree.setName("RemoteCorpusTree");
        arbilTreePanel.localCorpusTree.setName("LocalCorpusTree");
        arbilTreePanel.localDirectoryTree.setName("LocalDirectoryTree");
        arbilTreePanel.favouritesTree.setName("FavouritesTree");

        applyRootLocations();
    }

    public int addDefaultCorpusLocations() {
        HashSet<ImdiTreeObject> remoteCorpusNodesSet = new HashSet<ImdiTreeObject>();
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
                remoteCorpusNodesSet.add(ImdiLoader.getSingleInstance().getImdiObject(null, new URI(currentUrlString)));
            } catch (URISyntaxException ex) {
                GuiHelper.linorgBugCatcher.logError(ex);
            }
        }
        remoteCorpusNodes = remoteCorpusNodesSet.toArray(new ImdiTreeObject[]{});
        return remoteCorpusNodesSet.size();
    }

    public void saveLocations(ImdiTreeObject[] nodesToAdd, ImdiTreeObject[] nodesToRemove) {
        try {
            HashSet<String> locationsSet = new HashSet<String>();
            for (ImdiTreeObject[] currentTreeArray : new ImdiTreeObject[][]{remoteCorpusNodes, localCorpusNodes, localFileNodes, favouriteNodes}) {
                for (ImdiTreeObject currentLocation : currentTreeArray) {
                    locationsSet.add(currentLocation.getUrlString());
                }
            }
            if (nodesToAdd != null) {
                for (ImdiTreeObject currentAddable : nodesToAdd) {
                    locationsSet.add(currentAddable.getUrlString());
                }
            }
            if (nodesToRemove != null) {
                for (ImdiTreeObject currentRemoveable : nodesToRemove) {
                    locationsSet.remove(currentRemoveable.getUrlString());
                }
            }
            Vector<String> locationsList = new Vector<String>(); // this vector is kept for backwards compatability
            for (String currentLocation : locationsSet) {
                locationsList.add(URLDecoder.decode(currentLocation, "UTF-8"));
            }
            //LinorgSessionStorage.getSingleInstance().saveObject(locationsList, "locationsList");
            LinorgSessionStorage.getSingleInstance().saveStringArray("locationsList", locationsList.toArray(new String[]{}));
            System.out.println("saved locationsList");
        } catch (Exception ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
//            System.out.println("save locationsList exception: " + ex.getMessage());
        }
    }

    public void loadLocationsList() {
        try {
            System.out.println("loading locationsList");
            String[] locationsArray = LinorgSessionStorage.getSingleInstance().loadStringArray("locationsList");
            if (locationsArray == null) {
                addDefaultCorpusLocations();
            } else {
                Vector<ImdiTreeObject> remoteCorpusNodesVector = new Vector<ImdiTreeObject>();
                Vector<ImdiTreeObject> localCorpusNodesVector = new Vector<ImdiTreeObject>();
                Vector<ImdiTreeObject> localFileNodesVector = new Vector<ImdiTreeObject>();
                Vector<ImdiTreeObject> favouriteNodesVector = new Vector<ImdiTreeObject>();

                // this also removes all locations and replaces them with normalised paths
                for (String currentLocationString : locationsArray) {
                    URI currentLocation = ImdiTreeObject.conformStringToUrl(currentLocationString);
                    ImdiTreeObject currentTreeObject = ImdiLoader.getSingleInstance().getImdiObject(null, currentLocation);
                    if (currentTreeObject.isLocal()) {
                        if (currentTreeObject.isFavorite()) {
                            favouriteNodesVector.add(currentTreeObject);
                        } else if (LinorgSessionStorage.getSingleInstance().pathIsInsideCache(currentTreeObject.getFile())) {
                            if (currentTreeObject.isMetaDataNode() && !currentTreeObject.isImdiChild()) {
                                localCorpusNodesVector.add(currentTreeObject);
                            }
                        } else {
                            localFileNodesVector.add(currentTreeObject);
                        }
                    } else {
                        remoteCorpusNodesVector.add(currentTreeObject);
                    }
                }
                remoteCorpusNodes = remoteCorpusNodesVector.toArray(new ImdiTreeObject[]{});
                localCorpusNodes = localCorpusNodesVector.toArray(new ImdiTreeObject[]{});
                localFileNodes = localFileNodesVector.toArray(new ImdiTreeObject[]{});
                favouriteNodes = favouriteNodesVector.toArray(new ImdiTreeObject[]{});
            }
        } catch (Exception ex) {
//            System.out.println("load locationsList failed: " + ex.getMessage());
            GuiHelper.linorgBugCatcher.logError(ex);
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
        showHiddenFilesInTree = LinorgSessionStorage.getSingleInstance().loadBoolean("showHiddenFilesInTree", showHiddenFilesInTree);
    }

    public void setShowHiddenFilesInTree(boolean showState) {
        showHiddenFilesInTree = showState;
        reloadNodesInTree(localDirectoryRootNode);
        try {
            LinorgSessionStorage.getSingleInstance().saveBoolean("showHiddenFilesInTree", showHiddenFilesInTree);
        } catch (Exception ex) {
            System.out.println("save showHiddenFilesInTree failed");
        }
    }

    public void addLocationGui(URI addableLocation) {
        if (!addLocation(addableLocation)) {
            // alert the user when the node already exists and cannot be added again
            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("The location already exists and cannot be added again", "Add location");
        }
        applyRootLocations();
        //locationSettingsTable.setModel(guiHelper.getLocationsTableModel());
    }

    public boolean addLocation(URI addedLocation) {
        System.out.println("addLocation: " + addedLocation.toString());
        // make sure the added location url matches that of the imdi node format
        ImdiTreeObject addedLocationObject = ImdiLoader.getSingleInstance().getImdiObject(null, addedLocation);
        if (addedLocationObject != null) {
            saveLocations(new ImdiTreeObject[]{addedLocationObject}, null);
            loadLocationsList();
            return true;
        }
        return false;
    }

    public void removeLocation(ImdiTreeObject removeObject) {
        if (removeObject != null) {
            saveLocations(null, new ImdiTreeObject[]{removeObject});
            removeObject.removeFromAllContainers();
            loadLocationsList();
        }
    }

    public void removeLocation(URI removeLocation) {
        System.out.println("removeLocation: " + removeLocation);
        removeLocation(ImdiLoader.getSingleInstance().getImdiObject(null, removeLocation));
    }

    private void reloadNodesInTree(DefaultMutableTreeNode parentTreeNode) {
        // this will reload all nodes in a tree but not create any new child nodes
        for (Enumeration<DefaultMutableTreeNode> childNodesEnum = parentTreeNode.children(); childNodesEnum.hasMoreElements();) {
            reloadNodesInTree(childNodesEnum.nextElement());
        }
        if (parentTreeNode.getUserObject() instanceof ImdiTreeObject) {
            if (((ImdiTreeObject) parentTreeNode.getUserObject()).imdiDataLoaded) {
                ((ImdiTreeObject) parentTreeNode.getUserObject()).reloadNode();
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
            TreePath currentNodePaths[] = ((ImdiTree) sourceObject).getSelectionPaths();
            int toDeleteCount = 0;
            // count the number of nodes to delete
            for (TreePath currentNodePath : currentNodePaths) {
                if (currentNodePath != null) {
                    selectedTreeNode = (DefaultMutableTreeNode) currentNodePath.getLastPathComponent();
                    Object userObject = selectedTreeNode.getUserObject();
                    if (userObject instanceof ImdiTreeObject) {
                        if (((ImdiTreeObject) userObject).isEmptyMetaNode()) {
                            toDeleteCount = toDeleteCount + ((ImdiTreeObject) userObject).getChildCount();
                        } else {
                            toDeleteCount++;
                        }
                    }
                }
            }
            if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(LinorgWindowManager.getSingleInstance().linorgFrame, "Delete " + toDeleteCount + " nodes?", "Delete", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE)) {
                Vector<ImdiTreeObject> imdiNodesToRemove = new Vector<ImdiTreeObject>();
                Hashtable<ImdiTreeObject, Vector<ImdiTreeObject>> imdiNodesDeleteList = new Hashtable<ImdiTreeObject, Vector<ImdiTreeObject>>();
                Hashtable<ImdiTreeObject, Vector<String>> imdiChildNodeDeleteList = new Hashtable<ImdiTreeObject, Vector<String>>();
                for (TreePath currentNodePath : currentNodePaths) {
                    if (currentNodePath != null) {
                        selectedTreeNode = (DefaultMutableTreeNode) currentNodePath.getLastPathComponent();
                        Object userObject = selectedTreeNode.getUserObject();
                        System.out.println("trying to delete: " + userObject);
                        if (currentNodePath.getPath().length == 2) {
                            System.out.println("removing by location");
                            removeLocation((ImdiTreeObject) selectedTreeNode.getUserObject());
                            applyRootLocations();
                        } else {
                            System.out.println("deleting from parent");
                            parentTreeNode = (DefaultMutableTreeNode) selectedTreeNode.getParent();
                            if (parentTreeNode != null) {
                                System.out.println("found parent to remove from");
                                ImdiTreeObject parentImdiNode = (ImdiTreeObject) parentTreeNode.getUserObject();
                                ImdiTreeObject childImdiNode = (ImdiTreeObject) selectedTreeNode.getUserObject();
                                if (childImdiNode.isImdiChild()) {
                                    // there is a risk of the later deleted nodes being outof sync with the xml, so we add them all to a list and delete all at once before the node is reloaded
                                    if (!imdiChildNodeDeleteList.containsKey(childImdiNode.getParentDomNode())) {
                                        imdiChildNodeDeleteList.put(childImdiNode.getParentDomNode(), new Vector());
                                    }
                                    if (childImdiNode.isEmptyMetaNode()) {
                                        for (ImdiTreeObject metaChildNode : childImdiNode.getChildArray()) {
                                            imdiChildNodeDeleteList.get(childImdiNode.getParentDomNode()).add(metaChildNode.getURI().getFragment());
                                        }
                                    }
                                    imdiChildNodeDeleteList.get(childImdiNode.getParentDomNode()).add(childImdiNode.getURI().getFragment());
                                } else {
                                    // add the parent and the child node to the deletelist
                                    if (!imdiNodesDeleteList.containsKey(parentImdiNode)) {
                                        imdiNodesDeleteList.put(parentImdiNode, new Vector());
                                    }
                                    imdiNodesDeleteList.get(parentImdiNode).add(childImdiNode);
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
                        imdiNodesToRemove.add((ImdiTreeObject) userObject);
                        ((ImdiTreeObject) userObject).getAllChildren(imdiNodesToRemove);
                    }
                }
                for (ImdiTreeObject currentParent : imdiChildNodeDeleteList.keySet()) {
                    System.out.println("deleting by child xml id link");
                    // TODO: There is an issue when deleting child nodes that the remaining nodes xml path (x) will be incorrect as will the xmlnode id hence the node in a table may be incorrect after a delete
                    //currentParent.deleteFromDomViaId(((Vector<String>) imdiChildNodeDeleteList.get(currentParent)).toArray(new String[]{}));
                    CmdiComponentBuilder componentBuilder = new CmdiComponentBuilder();
                    boolean result = componentBuilder.removeChildNodes(currentParent, (imdiChildNodeDeleteList.get(currentParent)).toArray(new String[]{}));
                    if (result) {
                        currentParent.reloadNode();
                    } else {
                        LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Error deleting node, check the log file via the help menu for more information.", "Delete Node");
                    }
                    //GuiHelper.linorgBugCatcher.logError(new Exception("deleteFromDomViaId"));
                }
                for (ImdiTreeObject currentParent : imdiNodesDeleteList.keySet()) {
                    System.out.println("deleting by corpus link");
                    currentParent.deleteCorpusLink(((Vector<ImdiTreeObject>) imdiNodesDeleteList.get(currentParent)).toArray(new ImdiTreeObject[]{}));
                }
                for (Enumeration<ImdiTreeObject> deletedNodesEnum = imdiNodesToRemove.elements(); deletedNodesEnum.hasMoreElements();) {
                    // remove the deleted node from all tables
                    // todo: this is also done in deleteCorpusLink and need not be here
                    ImdiTreeObject currentDeletedNode = deletedNodesEnum.nextElement();
                    for (Object currentContainer : currentDeletedNode.getRegisteredContainers()) {
                        // this is required here even though it is now also done in the reloading process
                        if (currentContainer instanceof ImdiTableModel) {
                            ((ImdiTableModel) currentContainer).removeImdiObjects(new ImdiTreeObject[]{currentDeletedNode});
                        }
                    }
                }
            }
        } else {
            System.out.println("cannot delete from this tree");
        }
    }

    public void jumpToSelectionInTree(boolean silent, ImdiTreeObject cellImdiNode) {
        System.out.println("jumpToSelectionInTree: " + cellImdiNode);
        if (cellImdiNode != null) {
            cellImdiNode.scrollToRequested = true;
            cellImdiNode.clearIcon();
        } else {
            if (!silent) {
                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("The selected cell has no value or is not associated with a node in the tree", "Jump to in Tree");
            }
        }
    }
}
