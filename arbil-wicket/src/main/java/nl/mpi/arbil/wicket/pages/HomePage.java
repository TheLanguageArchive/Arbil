package nl.mpi.arbil.wicket.pages;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import nl.mpi.arbil.wicket.ArbilWicketSession;
import nl.mpi.arbil.wicket.components.ArbilWicketTree;
import nl.mpi.arbil.wicket.components.ArbilWicketTablePanel;
import nl.mpi.arbil.wicket.model.ArbilWicketTableModel;
import nl.mpi.arbil.wicket.model.ArbilWicketTreeModel;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tree.Tree;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;

/**
 * Homepage (test page)
 */
public class HomePage extends WebPage {

    private static final long serialVersionUID = 1L;
    private ArbilWicketTree remoteTree;
    private ArbilWicketTree localTree;
    WebMarkupContainer tableContainer;
    private WebMarkupContainer tablePanel;

    public HomePage(final PageParameters parameters) {
	super(parameters);

	ArbilWicketSession.get().getTreeHelper().applyRootLocations();

	tableContainer = new WebMarkupContainer("tableContainer");
	tableContainer.setOutputMarkupId(true);
	tableContainer.setMarkupId("tableContainer");
	add(tableContainer);

	// Empty placeholder for table panel until a node is selected
	tablePanel = new WebMarkupContainer("tablePanel");
	tableContainer.add(tablePanel);

	// Create remote tree
	TreeModel remoteTreeModel = ArbilWicketSession.get().getTreeHelper().getRemoteCorpusTreeModel();
	TreeModel localTreeModel = ArbilWicketSession.get().getTreeHelper().getLocalCorpusTreeModel();
	remoteTree = new ArbilWicketTree("remoteTree", new ArbilWicketTreeModel.DetachableArbilWicketTreeModel(remoteTreeModel)) {

	    @Override
	    protected void onNodeLinkClicked(AjaxRequestTarget target, TreeNode treeNode) {
		onTreeNodeClicked(this, target);
	    }
	};
	remoteTree.getTreeState().expandNode(remoteTreeModel.getRoot());
	add(remoteTree);

	// Create local tree
	localTree = new ArbilWicketTree("localTree", new ArbilWicketTreeModel.DetachableArbilWicketTreeModel(localTreeModel)) {

	    @Override
	    protected void onNodeLinkClicked(AjaxRequestTarget target, TreeNode treeNode) {
		onTreeNodeClicked(this, target);
	    }
	};
	localTree.getTreeState().expandNode(localTreeModel.getRoot());
	add(localTree);
    }

    private void onTreeNodeClicked(Tree tree, AjaxRequestTarget target) {
	ArbilWicketTableModel model = new ArbilWicketTableModel();
	model.setShowIcons(true);
	model.addSelectedNodesToModel(tree.getTreeState());

	tablePanel = new ArbilWicketTablePanel("tablePanel", model);
	tableContainer.addOrReplace(tablePanel);
	if (target != null) {
	    target.addComponent(tableContainer);
	}
    }
}
