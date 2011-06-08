package nl.mpi.arbil.wicket.pages;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.wicket.ArbilWicketSession;
import nl.mpi.arbil.wicket.components.ArbilWicketTree;
import nl.mpi.arbil.wicket.components.NodesPanel;
import nl.mpi.arbil.wicket.model.ArbilWicketTreeModel;
import nl.mpi.arbil.wicket.model.ArbilWicketTreeNode;
import nl.mpi.arbil.wicket.model.DataNodeDataProvider;
import nl.mpi.arbil.wicket.model.DetachableArbilDataNodeCollection;
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
			container.remove(tablePanel);
			ArbilDataNode dataNode = node.getDataNode();
			DataNodeDataProvider provider;
			if (dataNode.isEmptyMetaNode()) {
			    provider = new DataNodeDataProvider(DetachableArbilDataNodeCollection.URIsFromNodes(Arrays.asList(dataNode.getAllChildren())));
			} else {
			    provider = new DataNodeDataProvider(Collections.singletonList(dataNode.getURI()));
			}
			tablePanel = new NodesPanel("tablePanel", provider);
			container.add(tablePanel);
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
