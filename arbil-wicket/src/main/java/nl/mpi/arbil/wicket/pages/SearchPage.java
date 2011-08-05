package nl.mpi.arbil.wicket.pages;

import java.util.Collection;
import java.util.Collections;
import javax.swing.tree.TreeNode;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.search.ArbilNodeSearchTerm;
import nl.mpi.arbil.wicket.ArbilWicketSession;
import nl.mpi.arbil.wicket.components.ArbilWicketSearchForm;
import nl.mpi.arbil.wicket.components.ArbilWicketTablePanel;
import nl.mpi.arbil.wicket.components.ArbilWicketTree;
import nl.mpi.arbil.wicket.model.ArbilWicketTableModel;
import nl.mpi.arbil.wicket.model.ArbilWicketNodeSearchTerm;
import nl.mpi.arbil.wicket.model.ArbilWicketSearch;
import nl.mpi.arbil.wicket.model.ArbilWicketSearchModel;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class SearchPage extends TreePage {

    private static final long serialVersionUID = 1L;
    private ArbilWicketTree remoteTree;
    private WebMarkupContainer tableContainer;
    private WebMarkupContainer tablePanel;
    private ArbilWicketSearchForm searchForm;
    private ArbilWicketTree selectedTree = null;

    public SearchPage(PageParameters parameters) {
	super(parameters);

	ArbilWicketSession.get().getTreeHelper().applyRootLocations();
	createTable();
	createTrees();
	createForm(new ArbilWicketSearch(parameters, newNodeSearchTerm()));
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
	add(remoteTree = createRemoteTree());
	add(createLocalTree());
    }

    @Override
    protected void onTreeNodeClicked(ArbilWicketTree tree, TreeNode treeNode, AjaxRequestTarget target) {
	selectedTree = tree.getTreeState().getSelectedNodes().size() > 0 ? tree : null;
	// refresh table container
	target.addComponent(searchForm);
    }

    private void createForm(ArbilWicketSearch search) {
	searchForm = new ArbilWicketSearchForm("searchForm", new ArbilWicketSearchModel(search)) {

	    @Override
	    protected void onSearchComplete(ArbilWicketTableModel model, AjaxRequestTarget target) {

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

	    @Override
	    protected Collection<ArbilNode> getSelectedNodes() {
		if (selectedTree != null) {
		    return selectedTree.getSelectedNodes();
		} else {
		    return Collections.emptyList();
		}
	    }

	    @Override
	    protected boolean isNodesSelected() {
		return selectedTree != null;
	    }

	    @Override
	    protected boolean isRemote() {
		return selectedTree == remoteTree;
	    }

	    @Override
	    protected ArbilWicketNodeSearchTerm newNodeSearchTerm() {
		return SearchPage.this.newNodeSearchTerm();
	    }
	};

	searchForm.setOutputMarkupId(true);
	tableContainer.add(searchForm);
    }

    private ArbilWicketNodeSearchTerm newNodeSearchTerm() {
	ArbilWicketNodeSearchTerm term = new ArbilWicketNodeSearchTerm();
	term.setNodeType(ArbilNodeSearchTerm.NODE_TYPE_ALL);
	term.setBooleanAnd(true);
	term.setNotEqual(false);
	return term;
    }
}
