package nl.mpi.arbil.data;

import java.awt.Component;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import nl.mpi.arbil.ui.ArbilTree;
import nl.mpi.arbil.ui.ArbilTreePanels;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.MessageDialogHandler;

/**
 * Singleton instance of TreeHelper, for use with Arbil desktop application
 * Document   : ArbilTreeHelper
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
public class ArbilTreeHelper extends AbstractTreeHelper {

    static private ArbilTreeHelper singleInstance = null;

    static synchronized public ArbilTreeHelper getSingleInstance() {
	if (singleInstance == null) {
	    singleInstance = new ArbilTreeHelper();
	}
	return singleInstance;
    }
    private static MessageDialogHandler messageDialogHandler;

    public static void setMessageDialogHandler(MessageDialogHandler handler) {
	messageDialogHandler = handler;
    }
    private ArbilTreePanels arbilTreePanel;

    protected ArbilTreeHelper() {
	super();
	initTrees();
	// load any locations from the previous file formats
	//LinorgFavourites.getSingleInstance().convertOldFormatLocationLists();
	loadLocationsList();
    }

    @Override
    protected SessionStorage getSessionStorage() {
	// Hardwired to work with ArbilSessionStorage (as is the other way around)
	return ArbilSessionStorage.getSingleInstance();
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
	    if (JOptionPane.OK_OPTION == messageDialogHandler.showDialogBox(
		    "Delete " + (toDeleteCount == 1 ? "the node \"" + nameOfFirst + "\"?" : toDeleteCount + " nodes?")
		    + " This will also save any pending changes to disk.", "Delete",
		    JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE)) {
		// make lists of nodes to delete
		Hashtable<ArbilDataNode, Vector<ArbilDataNode>> dataNodesDeleteList = new Hashtable<ArbilDataNode, Vector<ArbilDataNode>>();
		Hashtable<ArbilDataNode, Vector<String>> childNodeDeleteList = new Hashtable<ArbilDataNode, Vector<String>>();
		determineNodesToDelete(currentNodePaths, childNodeDeleteList, dataNodesDeleteList);
		// delete child nodes
		deleteNodesByChidXmlIdLink(childNodeDeleteList);
		// delete parent nodes
		deleteNodesByCorpusLink(dataNodesDeleteList);
	    }
	} else {
	    System.out.println("cannot delete from this tree");
	}
    }

    public ArbilTreePanels getArbilTreePanel() {
	return arbilTreePanel;
    }
}
