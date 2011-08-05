package nl.mpi.arbil.wicket.pages;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.search.ArbilNodeSearchTerm;
import nl.mpi.arbil.wicket.components.ArbilWicketSearchForm;
import nl.mpi.arbil.wicket.components.ArbilWicketTablePanel;
import nl.mpi.arbil.wicket.model.ArbilDataNodeModel;
import nl.mpi.arbil.wicket.model.ArbilWicketNodeSearchTerm;
import nl.mpi.arbil.wicket.model.ArbilWicketSearch;
import nl.mpi.arbil.wicket.model.ArbilWicketSearchModel;
import nl.mpi.arbil.wicket.model.ArbilWicketTableModel;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class NodeSearchPage extends WebPage {

    private static Logger logger = LoggerFactory.getLogger(NodeSearchPage.class);
    private WebMarkupContainer tableContainer;
    private WebMarkupContainer tablePanel;
    private ArbilWicketSearchForm searchForm;
    private ArbilDataNodeModel selectedNodeModel = null;

    public NodeSearchPage(PageParameters parameters) {
	super(parameters);
	createTable();
	createForm(new ArbilWicketSearch(parameters, newNodeSearchTerm()));
	if (parameters.containsKey("nodeURI")) {
	    String nodeURI = parameters.getString("nodeURI");
	    try {
		selectedNodeModel = new ArbilDataNodeModel(new URI(nodeURI));
		if (selectedNodeModel.getObject() != null) {
		    selectedNodeModel.getObject().waitTillLoaded();
		} else {
		    logger.error("nodeURI with URI " + nodeURI + "could not be loaded");
		    selectedNodeModel = null;
		}
	    } catch (URISyntaxException ex) {
		logger.error("Invalid nodeURI supplied: " + nodeURI, ex);
		selectedNodeModel = null;
	    }
	}
	// TODO: add optional parameter to execute search immediately, and (optionally) hide search form so only results get shown
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
		if (selectedNodeModel != null) {
		    return Collections.singleton((ArbilNode) selectedNodeModel.getObject());
		} else {
		    return Collections.emptyList();
		}
	    }

	    @Override
	    protected boolean isNodesSelected() {
		return selectedNodeModel != null;
	    }

	    @Override
	    protected boolean isRemote() {
		return selectedNodeModel != null && !selectedNodeModel.getObject().isLocal();
	    }

	    @Override
	    protected ArbilWicketNodeSearchTerm newNodeSearchTerm() {
		return NodeSearchPage.this.newNodeSearchTerm();
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
