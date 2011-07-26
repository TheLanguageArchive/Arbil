package nl.mpi.arbil.wicket.pages;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import nl.mpi.arbil.wicket.ArbilWicketSession;
import nl.mpi.arbil.wicket.components.ArbilWicketTree;
import org.apache.wicket.IPageMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class TreePage extends WebPage {

    public TreePage(IPageMap pageMap, PageParameters parameters) {
	super(pageMap, parameters);
    }

    public TreePage(PageParameters parameters) {
	super(parameters);
    }

    public TreePage(IPageMap pageMap, IModel<?> model) {
	super(pageMap, model);
    }

    public TreePage(IPageMap pageMap) {
	super(pageMap);
    }

    public TreePage(IModel<?> model) {
	super(model);
    }

    public TreePage() {
    }

    protected ArbilWicketTree createRemoteTree() {
	// Create remote tree
	IModel<TreeModel> remoteTreeModel = new LoadableDetachableModel<TreeModel>() {

	    @Override
	    protected TreeModel load() {
		return ArbilWicketSession.get().getTreeHelper().getRemoteCorpusTreeModel();
	    }
	};
	ArbilWicketTree remoteTree = new ArbilWicketTree("remoteTree", remoteTreeModel) {

	    @Override
	    protected void onNodeLinkClicked(AjaxRequestTarget target, TreeNode treeNode) {
		onTreeNodeClicked(this, treeNode, target);
	    }
	};
	remoteTree.getTreeState().expandNode(remoteTreeModel.getObject().getRoot());
	
	return remoteTree;
    }

    protected ArbilWicketTree createLocalTree() {
	// Create local tree
	IModel<TreeModel> localTreeModel = new LoadableDetachableModel<TreeModel>() {

	    @Override
	    protected TreeModel load() {
		return ArbilWicketSession.get().getTreeHelper().getLocalCorpusTreeModel();
	    }
	};
	ArbilWicketTree localTree = new ArbilWicketTree("localTree", localTreeModel) {

	    @Override
	    protected void onNodeLinkClicked(AjaxRequestTarget target, TreeNode treeNode) {
		onTreeNodeClicked(this, treeNode, target);
	    }
	};
	localTree.getTreeState().expandNode(localTreeModel.getObject().getRoot());
	return localTree;
    }

    protected abstract void onTreeNodeClicked(ArbilWicketTree tree, TreeNode treeNode, AjaxRequestTarget target);
}
