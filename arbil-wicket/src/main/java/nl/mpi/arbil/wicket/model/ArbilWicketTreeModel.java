package nl.mpi.arbil.wicket.model;

import java.util.Arrays;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeContainer;
import nl.mpi.arbil.util.ArbilActionBuffer;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilWicketTreeModel extends DefaultTreeModel implements ArbilDataNodeContainer {
    public ArbilWicketTreeModel(TreeNode root){
	super(root, true);
    }
    
    private ArbilDataNode[] rootNodeChildren;
    
    private void sortDescendentNodes(DefaultMutableTreeNode currentNode/*, TreePath[] selectedPaths*/) {
	// todo: consider returning a list of tree paths for the nodes that are opened and have no open children
//        if (currentNode.getUserObject() instanceof JLabel) {
//            System.out.println("currentNode: " + ((JLabel) currentNode.getUserObject()).getText());
//        } else {
//            System.out.println("currentNode: " + currentNode);
//        }
	boolean isExpanded = true;
	ArbilDataNode[] childDataNodeArray = rootNodeChildren;
	if (currentNode instanceof DefaultMutableTreeNode) {
	    if (currentNode.getUserObject() instanceof ArbilDataNode) {
		ArbilDataNode curentDataNode = (ArbilDataNode) currentNode.getUserObject();
		if (curentDataNode != null) {
		    childDataNodeArray = curentDataNode.getChildArray();
		    //isExpanded = this.isExpanded(new TreePath((currentNode).getPath()));
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
		    DefaultMutableTreeNode addableNode = new DefaultMutableTreeNode(childDataNodeArray[childIndex]);
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
	sortRunner.requestActionAndNotify();
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
	requestResort();
    }
    ArbilActionBuffer sortRunner = new ArbilActionBuffer("ArbilTree sort thread", 50, 150) {

	@Override
	protected void executeAction() {
	    sortDescendentNodes((DefaultMutableTreeNode)getRoot());
	}
    };

    /**
     * @return the rootNodeChildren
     */
    public ArbilDataNode[] getRootNodeChildren() {
	return rootNodeChildren;
    }

    /**
     * @param rootNodeChildren the rootNodeChildren to set
     */
    public void setRootNodeChildren(ArbilDataNode[] rootNodeChildren) {
	this.rootNodeChildren = rootNodeChildren;
    }
    
    
}
