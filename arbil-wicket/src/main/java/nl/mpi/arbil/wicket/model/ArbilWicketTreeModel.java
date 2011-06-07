package nl.mpi.arbil.wicket.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeContainer;
import org.apache.wicket.markup.html.tree.ITreeStateListener;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilWicketTreeModel extends DefaultTreeModel implements ArbilDataNodeContainer, IDetachable, Serializable, ITreeStateListener {

    private DetachableArbilDataNodeCollector rootNoteChildren;

    private List<ArbilDataNode> getRootNodeChildrenList() {
	return rootNoteChildren.getDataNodes();
    }

    public ArbilWicketTreeModel(TreeNode root) {
	super(root, true);
    }

    private void sortDescendentNodes(DefaultMutableTreeNode currentNode/*, TreePath[] selectedPaths*/) {
	boolean isExpanded = true;
	ArbilDataNode[] childDataNodeArray = getRootNodeChildren();
	if (currentNode instanceof DefaultMutableTreeNode) {
	    if (currentNode.getUserObject() instanceof ArbilDataNode) {
		ArbilDataNode curentDataNode = (ArbilDataNode) currentNode.getUserObject();
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
	    ((DefaultTreeModel) treeModel).nodeChanged(currentNode);
	} else {
	    if (childDataNodeArray.length > 0) {
		// never disable allows children when there are child nodes!
		// but allows children must be set before nodes can be added (what on earth were they thinking!)
		currentNode.setAllowsChildren(true);
		((DefaultTreeModel) treeModel).nodeChanged(currentNode);
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
			    ((DefaultTreeModel) treeModel).nodeChanged((DefaultMutableTreeNode) ((DefaultTreeModel) treeModel).getChild(currentNode, modelChildIndex));
			}
			break;
		    }
		}
		// check if using an existing node failed and if so then add a new node
		if (childIndex >= ((DefaultTreeModel) treeModel).getChildCount(currentNode) || !((DefaultMutableTreeNode) ((DefaultTreeModel) treeModel).getChild(currentNode, childIndex)).getUserObject().equals(childDataNodeArray[childIndex])) {
		    childDataNodeArray[childIndex].registerContainer(this);
		    final ArbilDataNode dataNode = childDataNodeArray[childIndex];
		    DefaultMutableTreeNode addableNode = new DefaultMutableTreeNode(dataNode){

			@Override
			public boolean isLeaf() {
			    return dataNode.getChildCount() <= 0;
			}
			
		    };
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
	sortDescendentNodes((DefaultMutableTreeNode) getRoot());
    }

    /**
     * Data node is to be removed from this tree
     * @param dataNode Data node that should be removed
     */
    public void dataNodeRemoved(ArbilDataNode dataNode) {
	requestResort();
    }

    /**
     * Data node is clearing its icon
     * @param dataNode Data node that is clearing its icon
     */
    public void dataNodeIconCleared(ArbilDataNode dataNode) {
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
	this.rootNoteChildren = new DetachableArbilDataNodeCollector(rootNodeChildren);
    }

    public void detach() {
	if (rootNoteChildren != null) {
	    rootNoteChildren.detach();
	}
    }
    private List<TreeNode> expanded = new ArrayList<TreeNode>();

    private synchronized boolean isNodeExpanded(DefaultMutableTreeNode node) {
	return expanded.contains(node);
    }

    public synchronized void allNodesCollapsed() {
	expanded.clear();
    }

    public synchronized void allNodesExpanded() {
	// TODO: What to do?
    }

    public synchronized void nodeCollapsed(Object node) {
	if (node instanceof TreeNode && expanded.contains((TreeNode) node)) {
	    expanded.remove((TreeNode) node);
	}
    }

    public synchronized void nodeExpanded(Object node) {
	if (node instanceof TreeNode) {
	    expanded.add((TreeNode) node);
	}
	requestResort();
    }

    public void nodeSelected(Object node) {
    }

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

	public TreeModel getObject() {
	    return treeModel;
	}

	public void setObject(TreeModel object) {
	    treeModel = object;
	}

	public void detach() {
	    if (treeModel != null && treeModel instanceof ArbilWicketTreeModel) {
		((ArbilWicketTreeModel) treeModel).detach();
	    }
	}
    }
}
