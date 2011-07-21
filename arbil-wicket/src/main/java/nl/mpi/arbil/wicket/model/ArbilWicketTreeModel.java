package nl.mpi.arbil.wicket.model;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeContainer;
import nl.mpi.arbil.data.ArbilNode;
import org.apache.wicket.markup.html.tree.ITreeStateListener;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilWicketTreeModel extends DefaultTreeModel implements ArbilDataNodeContainer, IDetachable, Serializable, ITreeStateListener {

    private final static Logger logger = LoggerFactory.getLogger(ArbilWicketTreeModel.class);
    private DetachableArbilDataNodeCollection rootNoteChildren;

    private List<ArbilDataNode> getRootNodeChildrenList() {
	return rootNoteChildren.getDataNodes();
    }

    public ArbilWicketTreeModel(TreeNode root) {
	super(root, true);
    }

    private void waitTillLoaded(ArbilDataNode dataNode) {
	if (!dataNode.isDataLoaded()) {
	    synchronized (dataNode.getParentDomNode()) {
		dataNode.waitTillLoaded();
	    }
	}
    }

    private void doNodeChanged(TreeNode treeNode) {
	try {
	    nodeChanged(root);
	} catch (NullPointerException ex) {
	    logger.warn("Could not update node " + treeNode.toString(), ex);
	}
    }

    private void sortDescendentNodes(DefaultMutableTreeNode currentNode/*, TreePath[] selectedPaths*/) {
	boolean isExpanded = true;
	ArbilDataNode[] childDataNodeArray = getRootNodeChildren();
	if (currentNode instanceof DefaultMutableTreeNode) {
	    if (currentNode.getUserObject() instanceof ArbilDataNode) {
		ArbilDataNode curentDataNode = (ArbilDataNode) currentNode.getUserObject();
		waitTillLoaded(curentDataNode);
		if (curentDataNode != null) {
		    childDataNodeArray = curentDataNode.getChildArray();
		    isExpanded = isNodeExpanded(currentNode);
		}
	    }
	}
	Arrays.sort(childDataNodeArray);

	DefaultTreeModel treeModel = this;

	if (!isExpanded) {
	    currentNode.setAllowsChildren(childDataNodeArray.length > 0);
	    // Don't signal node changed as this wicket tree will choke
	    doNodeChanged(currentNode);
	} else {
	    if (childDataNodeArray.length > 0) {
		// never disable allows children when there are child nodes!
		// but allows children must be set before nodes can be added (what on earth were they thinking!)
		currentNode.setAllowsChildren(true);
		doNodeChanged(currentNode);
	    }
	    for (int childIndex = 0; childIndex < childDataNodeArray.length; childIndex++) {
		// search for an existing node and move it if required
		for (int modelChildIndex = 0; modelChildIndex < ((DefaultTreeModel) treeModel).getChildCount(currentNode); modelChildIndex++) {
		    if (((DefaultMutableTreeNode) ((DefaultTreeModel) treeModel).getChild(currentNode, modelChildIndex)).getUserObject().equals(childDataNodeArray[childIndex])) {
			if (childIndex != modelChildIndex) {
			    DefaultMutableTreeNode shiftedNode = (DefaultMutableTreeNode) ((DefaultTreeModel) treeModel).getChild(currentNode, modelChildIndex);
			    ((DefaultTreeModel) treeModel).removeNodeFromParent(shiftedNode);
			    ((DefaultTreeModel) treeModel).insertNodeInto(shiftedNode, currentNode, childIndex);
			} else {
			    doNodeChanged((DefaultMutableTreeNode) ((DefaultTreeModel) treeModel).getChild(currentNode, modelChildIndex));
			}
			break;
		    }
		}
		// check if using an existing node failed and if so then add a new node
		if (childIndex >= ((DefaultTreeModel) treeModel).getChildCount(currentNode) || !((DefaultMutableTreeNode) ((DefaultTreeModel) treeModel).getChild(currentNode, childIndex)).getUserObject().equals(childDataNodeArray[childIndex])) {
		    childDataNodeArray[childIndex].registerContainer(this);
		    final ArbilDataNode dataNode = childDataNodeArray[childIndex];
		    waitTillLoaded(dataNode);
		    DefaultMutableTreeNode addableNode = new ArbilWicketTreeNode(dataNode);
		    ((DefaultTreeModel) treeModel).insertNodeInto(addableNode, currentNode, childIndex);
		}
	    }
	    // remove any extraneous nodes from the end
	    for (int childIndex = ((DefaultTreeModel) treeModel).getChildCount(currentNode) - 1; childIndex >= childDataNodeArray.length; childIndex--) {
		((DefaultTreeModel) treeModel).removeNodeFromParent(((DefaultMutableTreeNode) ((DefaultTreeModel) treeModel).getChild(currentNode, childIndex)));
	    }
	    for (int childIndex = 0; childIndex < ((DefaultTreeModel) treeModel).getChildCount(currentNode); childIndex++) {
		//for (Enumeration<DefaultMutableTreeNode> childTreeNodeEnum = currentNode.children(); childTreeNodeEnum.hasMoreElements();) {
		sortDescendentNodes((DefaultMutableTreeNode) ((DefaultTreeModel) treeModel).getChild(currentNode, childIndex));
	    }
	}
    }

    public void requestResort() {
	requestResort((DefaultMutableTreeNode) getRoot());
    }

    public void requestResort(DefaultMutableTreeNode node) {
	// This used to be done in a separate thread, like so:
	//	      private transient ArbilActionBuffer sortRunner = new ArbilActionBuffer("ArbilTree sort thread", 50, 150) {
	//
	//		@Override
	//		protected void executeAction() {
	//		    sortDescendentNodes((DefaultMutableTreeNode) getRoot());
	//		}
	//      };
	//	sortRunner.requestActionAndNotify();
	// 
	// but that will break Wicket. So do it synchronous sorting (and hope that it scales)
	sortDescendentNodes(node);
    }

    /**
     * Data node is to be removed from this tree
     * @param dataNode Data node that should be removed
     */
    @Override
    public void dataNodeRemoved(ArbilNode dataNode) {
	requestResort();
    }

    /**
     * Data node is clearing its icon
     * @param dataNode Data node that is clearing its icon
     */
    @Override
    public void dataNodeIconCleared(ArbilNode dataNode) {
	//requestResort();
    }

    /**
     * @return the rootNodeChildren
     */
    public ArbilDataNode[] getRootNodeChildren() {
	return getRootNodeChildrenList().toArray(new ArbilDataNode[]{});
    }

    /**
     * @param rootNodeChildren the rootNodeChildren to set
     */
    public void setRootNodeChildren(ArbilDataNode[] rootNodeChildren) {
	this.rootNoteChildren = new DetachableArbilDataNodeCollection(rootNodeChildren);
    }

    @Override
    public void detach() {
	if (rootNoteChildren != null) {
	    rootNoteChildren.detach();
	}
	detachChildren((DefaultMutableTreeNode) getRoot());
    }

    private void detachChildren(TreeNode node) {
	Enumeration children = node.children();
	while (children.hasMoreElements()) {
	    Object child = children.nextElement();
	    if (child instanceof TreeNode) {
		detachChildren((TreeNode) child);
	    }
	    if (child instanceof ArbilWicketTreeNode) {
		((ArbilWicketTreeNode) child).detach();
	    }
	}
    }
    private DetachableArbilDataNodeCollection expandedNodes = new DetachableArbilDataNodeCollection(new ArrayList<URI>());

    private synchronized boolean isNodeExpanded(DefaultMutableTreeNode node) {
	if (node.getUserObject() instanceof ArbilDataNode) {
	    return expandedNodes.contains((ArbilDataNode) node.getUserObject());
	} else {
	    return false;
	}
    }

    @Override
    public synchronized void allNodesCollapsed() {
	expandedNodes.clear();
    }

    @Override
    public synchronized void allNodesExpanded() {
	// TODO: What to do?
    }

    @Override
    public synchronized void nodeCollapsed(Object node) {
	if (node instanceof DefaultMutableTreeNode) {
	    Object userObject = (((DefaultMutableTreeNode) node).getUserObject());
	    if (userObject instanceof ArbilDataNode) {
		expandedNodes.remove(((ArbilDataNode) userObject));
	    }
	    if (node instanceof ArbilWicketTreeNode) {
		((ArbilWicketTreeNode) node).detach();
	    }
	}
    }

    @Override
    public synchronized void nodeExpanded(Object node) {
	if (node instanceof DefaultMutableTreeNode) {
	    Object userObject = (((DefaultMutableTreeNode) node).getUserObject());
	    if (userObject instanceof ArbilDataNode) {
		expandedNodes.add(((ArbilDataNode) userObject));
	    }
	    requestResort(((DefaultMutableTreeNode) node));
	} else {
	    requestResort(((DefaultMutableTreeNode) getRoot()));
	}
    }

    @Override
    public void nodeSelected(Object node) {
    }

    @Override
    public void nodeUnselected(Object node) {
    }

    public static class DetachableArbilWicketTreeModel implements IModel<TreeModel>, IDetachable {

	private TreeModel treeModel;

	public DetachableArbilWicketTreeModel(TreeModel object) {
	    treeModel = object;
	}

	public DetachableArbilWicketTreeModel(TreeNode root) {
	    treeModel = new ArbilWicketTreeModel(root);
	}

	@Override
	public TreeModel getObject() {
	    return treeModel;
	}

	@Override
	public void setObject(TreeModel object) {
	    treeModel = object;
	}

	@Override
	public void detach() {
	    if (treeModel != null && treeModel instanceof ArbilWicketTreeModel) {
		((ArbilWicketTreeModel) treeModel).detach();
	    }
	}
    }
}
