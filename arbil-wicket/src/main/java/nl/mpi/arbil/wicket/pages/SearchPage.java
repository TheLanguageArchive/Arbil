package nl.mpi.arbil.wicket.pages;

import java.util.Arrays;
import java.util.Collections;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
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
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class SearchPage extends WebPage {

    private static final long serialVersionUID = 1L;
    private static final String SELECT_NODES_STRING = "Select a node and enter search terms";
    private ArbilWicketTree remoteTree;
    private ArbilWicketTree localTree;
    private WebMarkupContainer tableContainer;
    private WebMarkupContainer tablePanel;
    private Form<SimpleNodeSearchTerm> searchForm;
    private ArbilWicketTree selectedTree = null;
    private ArbilSearch searchService;

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

	searchForm.add(new DropDownChoice<String>("nodeType", Arrays.asList(ArbilNodeSearchTerm.NODE_TYPES)));
	searchForm.add(new TextField("remoteSearchString") {

	    @Override
	    public boolean isVisible() {
		return selectedTree == remoteTree;
	    }
	});
	searchForm.add(new TextField("searchFieldName"));
	searchForm.add(new TextField("searchString"));

	// Label that shows either the nodes selected for searchService or a string informing the user they should select one
	searchForm.add(new Label("searchNodes", new Model<String>() {

	    @Override
	    public String getObject() {
		if (selectedTree != null) {
		    return "Searching in " + Arrays.toString(selectedTree.getTreeState().getSelectedNodes().toArray());
		} else {
		    return SELECT_NODES_STRING;
		}
	    }
	}));

	// 'Search' button
	searchForm.add(new AjaxButton("searchSubmit", searchForm) {

	    @Override
	    protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
		performSearch(searchForm.getModelObject(), target);
	    }

	    @Override
	    public boolean isEnabled() {
		// Nodes should be selected for this to be enabled
		return selectedTree != null;
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

	// Empty placeholder for table panel until searchService is performed
	tablePanel = new WebMarkupContainer("tablePanel");
	tableContainer.add(tablePanel);
    }

    private void createTrees() {
	// Create remote tree
	TreeModel remoteTreeModel = ArbilWicketSession.get().getTreeHelper().getRemoteCorpusTreeModel();
	remoteTree = new ArbilWicketTree("remoteTree", new ArbilWicketTreeModel.DetachableArbilWicketTreeModel(remoteTreeModel)) {

	    @Override
	    protected void onNodeLinkClicked(AjaxRequestTarget target, TreeNode treeNode) {
		onTreeNodeClicked(this, target);
	    }
	};
	remoteTree.getTreeState().expandNode(remoteTreeModel.getRoot());
	add(remoteTree);

	// Create local tree
	TreeModel localTreeModel = ArbilWicketSession.get().getTreeHelper().getLocalCorpusTreeModel();
	localTree = new ArbilWicketTree("localTree", new ArbilWicketTreeModel.DetachableArbilWicketTreeModel(localTreeModel)) {

	    @Override
	    protected void onNodeLinkClicked(AjaxRequestTarget target, TreeNode treeNode) {
		onTreeNodeClicked(this, target);
	    }
	};
	localTree.getTreeState().expandNode(localTreeModel.getRoot());
	add(localTree);
    }

    private void onTreeNodeClicked(ArbilWicketTree tree, AjaxRequestTarget target) {
	selectedTree = tree.getTreeState().getSelectedNodes().size() > 0 ? tree : null;
	// refresh table container
	target.addComponent(searchForm);
    }

    private void performSearch(SimpleNodeSearchTerm searchTerm, AjaxRequestTarget target) {
	if (null != searchTerm && null != selectedTree) {
	    ArbilWicketTableModel model = new ArbilWicketTableModel();
	    searchService = new ArbilSearch(selectedTree.getSelectedNodes(), Collections.singleton(searchTerm), searchTerm.getRemoteServerSearchTerm(),
		    model, // as ArbilTableModel
		    model, // as DataNodeContainer
		    null); // no listener (for now)
	    
	    searchService.splitLocalRemote();
	    if (selectedTree == remoteTree) {
		searchService.fetchRemoteSearchResults();
	    }
	    searchService.searchLocalNodes();
	    // Done. Remove searchService to indicate search is not active
	    searchService = null;

	    if (model.getRowCount() > 0) {
		tablePanel = new ArbilWicketTablePanel("tablePanel", model);
	    } else {
		// No results, empty placeholder for table panel
		tablePanel = new WebMarkupContainer("tablePanel");
	    }
	    tableContainer.addOrReplace(tablePanel);
	    
	    if (target != null) {
		target.addComponent(tableContainer);
	    }
	}
    }
}
