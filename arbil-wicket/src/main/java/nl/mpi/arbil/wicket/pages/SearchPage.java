package nl.mpi.arbil.wicket.pages;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.search.ArbilNodeSearchTerm;
import nl.mpi.arbil.search.ArbilSearch;
import nl.mpi.arbil.wicket.ArbilWicketSession;
import nl.mpi.arbil.wicket.components.ArbilWicketTablePanel;
import nl.mpi.arbil.wicket.components.ArbilWicketTree;
import nl.mpi.arbil.wicket.model.ArbilWicketTableModel;
import nl.mpi.arbil.wicket.model.ArbilWicketTreeModel;
import nl.mpi.arbil.wicket.model.SimpleNodeSearchTerm;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class SearchPage extends WebPage {

    private static final long serialVersionUID = 1L;
    private ArbilWicketTree remoteTree;
    private ArbilWicketTree localTree;
    private WebMarkupContainer tableContainer;
    private WebMarkupContainer tablePanel;
    private Form<SimpleNodeSearchTerm> searchForm;

    public SearchPage(PageParameters parameters) {
	super(parameters);

	ArbilWicketSession.get().getTreeHelper().applyRootLocations();
	createTable();
	createTrees();
	createForm();
    }

    private void createForm() {
	ArbilNodeSearchTerm term = new SimpleNodeSearchTerm();
	term.setNodeType(ArbilNodeSearchTerm.NODE_TYPE_ALL);
	term.setBooleanAnd(true);
	term.setNotEqual(false);

	searchForm = new Form<SimpleNodeSearchTerm>("searchForm", new CompoundPropertyModel<SimpleNodeSearchTerm>(term));

	searchForm.add(new TextField("searchFieldName"));
	searchForm.add(new TextField("searchString"));
	searchForm.add(new DropDownChoice<String>("nodeType", Arrays.asList(ArbilNodeSearchTerm.NODE_TYPES)));

	searchForm.add(new AjaxButton("searchSubmit", searchForm) {

	    @Override
	    protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
		performSearch(searchForm.getModelObject(), target);
	    }

	    @Override
	    public boolean isEnabled() {
		return localTree.getTreeState().getSelectedNodes().size() > 0
			|| remoteTree.getTreeState().getSelectedNodes().size() > 0;
	    }
	});

	searchForm.setOutputMarkupId(true);
	tableContainer.add(searchForm);
    }

    private void createTable() {
	tableContainer = new WebMarkupContainer("tableContainer");
	tableContainer.setOutputMarkupId(true);
	tableContainer.setMarkupId("tableContainer");
	add(tableContainer);

	// Empty placeholder for table panel until a node is selected
	tablePanel = new WebMarkupContainer("tablePanel");
	tableContainer.add(tablePanel);
    }

    private void createTrees() {
	// Create remote tree
	TreeModel remoteTreeModel = ArbilWicketSession.get().getTreeHelper().getRemoteCorpusTreeModel();
	remoteTree = new ArbilWicketTree("remoteTree", new ArbilWicketTreeModel.DetachableArbilWicketTreeModel(remoteTreeModel)) {

	    @Override
	    protected void onNodeLinkClicked(AjaxRequestTarget target, TreeNode treeNode) {
		onTreeNodeClicked(target);
	    }
	};
	remoteTree.getTreeState().expandNode(remoteTreeModel.getRoot());
	add(remoteTree);

	// Create local tree
	TreeModel localTreeModel = ArbilWicketSession.get().getTreeHelper().getLocalCorpusTreeModel();
	localTree = new ArbilWicketTree("localTree", new ArbilWicketTreeModel.DetachableArbilWicketTreeModel(localTreeModel)) {

	    @Override
	    protected void onNodeLinkClicked(AjaxRequestTarget target, TreeNode treeNode) {
		onTreeNodeClicked(target);
	    }
	};
	localTree.getTreeState().expandNode(localTreeModel.getRoot());
	add(localTree);
    }

    private void onTreeNodeClicked(AjaxRequestTarget target) {
	// refresh table container
	target.addComponent(searchForm);
    }

    private void performSearch(ArbilNodeSearchTerm searchTerm, AjaxRequestTarget target) {
	Collection<ArbilDataNode> selectedNodes = localTree.getSelectedNodes();
	selectedNodes.addAll(remoteTree.getSelectedNodes());

	if (null != searchTerm && selectedNodes.size() > 0) {
	    ArbilWicketTableModel model = new ArbilWicketTableModel();
	    ArbilSearch search = new ArbilSearch(selectedNodes, Collections.singleton(searchTerm), null,
		    model, // as ArbilTableModel
		    model, // as DataNodeContainer
		    null); // no listener (for now)
	    search.splitLocalRemote();
	    search.fetchRemoteSearchResults();
	    search.searchLocalNodes();

	    tablePanel = new ArbilWicketTablePanel("tablePanel", model);
	    tableContainer.addOrReplace(tablePanel);

	    if (target != null) {
		target.addComponent(tableContainer);
	    }
	}
    }
}
