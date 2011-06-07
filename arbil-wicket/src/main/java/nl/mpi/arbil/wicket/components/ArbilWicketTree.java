package nl.mpi.arbil.wicket.components;

import javax.swing.tree.TreeModel;
import nl.mpi.arbil.wicket.model.ArbilWicketTreeModel;
import org.apache.wicket.extensions.markup.html.tree.Tree;
import org.apache.wicket.markup.html.tree.DefaultTreeState;
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
	return new DefaultTreeState() {
	};
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
}
