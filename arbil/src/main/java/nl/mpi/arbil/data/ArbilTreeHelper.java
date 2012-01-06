package nl.mpi.arbil.data;

import java.awt.Component;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import nl.mpi.arbil.ui.ArbilTree;
import nl.mpi.arbil.ui.ArbilTreePanels;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcher;
import nl.mpi.arbil.util.MessageDialogHandler;

/**
 * Singleton instance of TreeHelper, for use with Arbil desktop application
 * Document   : ArbilTreeHelper
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
public class ArbilTreeHelper extends AbstractTreeHelper {

    private SessionStorage sessionStorage;

    private ArbilTreePanels arbilTreePanel;

    public ArbilTreeHelper(SessionStorage sessionStorage, MessageDialogHandler messageDialogHandler, BugCatcher bugCatcher) {
	super(messageDialogHandler, bugCatcher);
	this.sessionStorage = sessionStorage;
    }

    public void init() {
	initTrees();
	// load any locations from the previous file formats
	//LinorgFavourites.getSingleInstance().convertOldFormatLocationLists();
	loadLocationsList();
    }

    @Override
    protected SessionStorage getSessionStorage() {
	return sessionStorage;
    }

    public void setTrees(ArbilTreePanels arbilTreePanelLocal) {
	arbilTreePanel = arbilTreePanelLocal;
	arbilTreePanel.remoteCorpusTree.setName("RemoteCorpusTree");
	arbilTreePanel.localCorpusTree.setName("LocalCorpusTree");
	arbilTreePanel.localDirectoryTree.setName("LocalDirectoryTree");
	arbilTreePanel.favouritesTree.setName("FavouritesTree");

	applyRootLocations();
    }

    public boolean componentIsTheLocalCorpusTree(Component componentToTest) {
	return componentToTest.equals(arbilTreePanel.localCorpusTree);
    }

    public boolean componentIsTheFavouritesTree(Component componentToTest) {
	return componentToTest.equals(arbilTreePanel.favouritesTree);
    }

    @Override
    public void applyRootLocations() {
	System.out.println("applyRootLocations");
	arbilTreePanel.localCorpusTree.rootNodeChildren = getLocalCorpusNodes();
	arbilTreePanel.remoteCorpusTree.rootNodeChildren = getRemoteCorpusNodes();
	arbilTreePanel.localDirectoryTree.rootNodeChildren = getLocalFileNodes();
	arbilTreePanel.favouritesTree.rootNodeChildren = getFavouriteNodes();
	arbilTreePanel.localCorpusTree.requestResort();
	arbilTreePanel.remoteCorpusTree.requestResort();
	arbilTreePanel.localDirectoryTree.requestResort();
	arbilTreePanel.favouritesTree.requestResort();
    }

    public DefaultMutableTreeNode getLocalCorpusTreeSingleSelection() {
	System.out.println("localCorpusTree: " + arbilTreePanel.localCorpusTree);
	return (DefaultMutableTreeNode) arbilTreePanel.localCorpusTree.getSelectionPath().getLastPathComponent();
    }

    public void deleteNodes(Object sourceObject) {
	System.out.println("deleteNode: " + sourceObject);
	if (sourceObject == arbilTreePanel.localCorpusTree || sourceObject == arbilTreePanel.favouritesTree) {
	    TreePath currentNodePaths[] = ((ArbilTree) sourceObject).getSelectionPaths();
	    int toDeleteCount = 0;
	    // count the number of nodes to delete
	    String nameOfFirst = null;
	    for (TreePath currentNodePath : currentNodePaths) {
		if (currentNodePath != null) {
		    DefaultMutableTreeNode selectedTreeNode = (DefaultMutableTreeNode) currentNodePath.getLastPathComponent();
		    Object userObject = selectedTreeNode.getUserObject();
		    if (userObject instanceof ArbilDataNode) {
			if (((ArbilDataNode) userObject).fileNotFound) {
			    toDeleteCount++;
			} else if (((ArbilDataNode) userObject).isEmptyMetaNode()) {
			    toDeleteCount += ((ArbilDataNode) userObject).getChildCount();
			} else {
			    toDeleteCount++;
			}
			if (nameOfFirst == null) {
			    nameOfFirst = ((ArbilDataNode) userObject).toString();
			}
		    }
		}
	    }
	    if (JOptionPane.OK_OPTION == getMessageDialogHandler().showDialogBox(
		    "Delete " + (toDeleteCount == 1 ? "the node \"" + nameOfFirst + "\"?" : toDeleteCount + " nodes?")
		    + " This will also save any pending changes to disk.", "Delete",
		    JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE)) {
		// make lists of nodes to delete
		Map<ArbilDataNode, List<ArbilDataNode>> dataNodesDeleteList = new HashMap<ArbilDataNode, List<ArbilDataNode>>();
		Map<ArbilDataNode, List<String>> childNodeDeleteList = new HashMap<ArbilDataNode, List<String>>();
		Map<ArbilDataNode, List<ArbilDataNode>> cmdiLinksDeleteList = new HashMap<ArbilDataNode, List<ArbilDataNode>>();
		determineNodesToDelete(currentNodePaths, childNodeDeleteList, dataNodesDeleteList, cmdiLinksDeleteList);
		// delete child nodes
		deleteNodesByChidXmlIdLink(childNodeDeleteList);
		// delete parent nodes
		deleteNodesByCorpusLink(dataNodesDeleteList);
		// delete CMDI link nodes
		deleteCmdiLinks(cmdiLinksDeleteList);
	    }
	} else {
	    System.out.println("cannot delete from this tree");
	}
    }

    @Override
    public boolean addLocationInteractive(URI addableLocation) {
	boolean added = addLocation(addableLocation);
	if (!added) {
	    // alert the user when the node already exists and cannot be added again
	    getMessageDialogHandler().addMessageDialogToQueue("The location already exists and cannot be added again", "Add location");
	}
	applyRootLocations();
	return added;
    }

    public ArbilTreePanels getArbilTreePanel() {
	return arbilTreePanel;
    }
}
