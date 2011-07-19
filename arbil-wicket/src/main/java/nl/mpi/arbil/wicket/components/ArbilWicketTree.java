package nl.mpi.arbil.wicket.components;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.ui.AbstractArbilTableModel;
import nl.mpi.arbil.wicket.model.ArbilWicketTreeModel;
import nl.mpi.arbil.wicket.model.ArbilWicketTreeNode;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.extensions.markup.html.tree.Tree;
import org.apache.wicket.markup.html.tree.ITreeState;
import org.apache.wicket.model.IModel;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilWicketTree extends Tree {

    public ArbilWicketTree(String id, IModel<TreeModel> model) {
	super(id, model);
    }
    private ITreeState state;

    @Override
    protected ITreeState newTreeState() {
	ITreeState treeState = super.newTreeState();
	//treeState.setAllowSelectMultiple(true);
	return treeState;
    }

    /**
     * Returns the TreeState of this tree.
     * 
     * @return Tree state instance
     */
    @Override
    public ITreeState getTreeState() {
	if (state == null) {
	    state = newTreeState();

	    // add this object as listener of the state
	    state.addTreeStateListener(this);
	    // FIXME: Where should we remove the listener?
	    if (getModel().getObject() instanceof ArbilWicketTreeModel) {
		state.addTreeStateListener((ArbilWicketTreeModel) this.getModel().getObject());
	    }
	}
	return state;
    }

    @Override
    protected Component newNodeIcon(MarkupContainer parent, String id, final TreeNode node) {
	Object object = ((DefaultMutableTreeNode) node).getUserObject();
	if (object instanceof ArbilNode) {
	    return new NodeIcon(id, ((ArbilNode) object).getIcon().getImage());
	} else {
	    return super.newNodeIcon(parent, id, node);
	}
    }

    /**
     * Adds selected nodes from the tree's TreeState to a table model
     * @param AbstractArbilTableModel Table model to add selected nodes to
     * @return Number of nodes added to the model 
     */
    public int addSelectedNodesToModel(AbstractArbilTableModel tableModel) {
	int rowCount = tableModel.getRowCount();
	Collection<Object> selectedTreeNodes = getTreeState().getSelectedNodes();
	if (selectedTreeNodes.size() == 1) {
	    // Single selection
	    Object node = selectedTreeNodes.iterator().next();
	    if (node instanceof ArbilWicketTreeNode) {
		tableModel.addSingleArbilDataNode(((ArbilWicketTreeNode) node).getDataNode());
	    } else {
		return 0;
	    }
	} else {
	    // Multiselect
	    List displayNodes = getSelectedArbilDataNodes(selectedTreeNodes);
	    if (displayNodes.isEmpty()) {
		return 0;
	    } else {
		tableModel.addArbilDataNodes(Collections.enumeration(displayNodes));
	    }
	}
	return tableModel.getRowCount() - rowCount;
    }

    public List<ArbilDataNode> getSelectedNodes() {
	Collection<Object> selected = getTreeState().getSelectedNodes();
	return getSelectedArbilDataNodes(selected);
    }

    private List<ArbilDataNode> getSelectedArbilDataNodes(final Collection<Object> selectedTreeNodes) {
	List displayNodes = new LinkedList<ArbilDataNode>();
	for (Object node : selectedTreeNodes) {
	    if (node instanceof ArbilWicketTreeNode) {
		displayNodes.add(((ArbilWicketTreeNode) node).getDataNode());
	    }
	}
	return displayNodes;
    }
}
