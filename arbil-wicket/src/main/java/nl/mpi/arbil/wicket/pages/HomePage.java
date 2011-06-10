package nl.mpi.arbil.wicket.pages;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.wicket.ArbilWicketSession;
import nl.mpi.arbil.wicket.components.ArbilWicketTree;
import nl.mpi.arbil.wicket.components.ArbilWicketTablePanel;
import nl.mpi.arbil.wicket.model.ArbilWicketTableModel;
import nl.mpi.arbil.wicket.model.ArbilWicketTreeModel;
import nl.mpi.arbil.wicket.model.ArbilWicketTreeNode;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;

/**
 * Homepage (test page)
 */
public class HomePage extends WebPage {

    private static final long serialVersionUID = 1L;
    private ArbilWicketTree tree;
    private WebMarkupContainer tablePanel;

    public HomePage(final PageParameters parameters) {
	super(parameters);

	ArbilWicketSession.get().getTreeHelper().applyRootLocations();

	final WebMarkupContainer container = new WebMarkupContainer("mainContainer");
	container.setOutputMarkupId(true);

	// Empty placeholder for table panel until a node is selected
	tablePanel = new WebMarkupContainer("tablePanel");
	container.add(tablePanel);

	TreeModel treeModel = ArbilWicketSession.get().getTreeHelper().getRemoteCorpusTreeModel();
	tree = new ArbilWicketTree("tree", new ArbilWicketTreeModel.DetachableArbilWicketTreeModel(treeModel)) {

	    @Override
	    protected void onNodeLinkClicked(AjaxRequestTarget target, TreeNode treeNode) {
		if (treeNode instanceof ArbilWicketTreeNode) {
		    ArbilWicketTreeNode node = (ArbilWicketTreeNode) treeNode;
		    if (tree.getTreeState().isNodeSelected(node)) {
			ArbilDataNode dataNode = node.getDataNode();
			ArbilWicketTableModel model = new ArbilWicketTableModel();
			if (dataNode.isEmptyMetaNode()) {
			    model.addArbilDataNodes(dataNode.getAllChildren());
			} else {
			    model.addSingleArbilDataNode(dataNode);
			}
			model.setShowIcons(true);

			tablePanel = new ArbilWicketTablePanel("tablePanel", model);
			container.addOrReplace(tablePanel);
			if (target != null) {
			    target.addComponent(container);
			}
		    }
		}
	    }
	};
	tree.getTreeState().expandNode(treeModel.getRoot());
	add(tree);
	add(container);
    }
}
