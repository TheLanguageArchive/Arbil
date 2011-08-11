package nl.mpi.arbil.wicket.pages;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.search.ArbilNodeSearchTerm;
import nl.mpi.arbil.search.ArbilSimpleNodeSearchTerm;
import nl.mpi.arbil.wicket.components.ArbilWicketSearchForm;
import nl.mpi.arbil.wicket.components.ArbilWicketTablePanel;
import nl.mpi.arbil.wicket.model.ArbilDataNodeModel;
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
    public final static String PARAM_NODEURI = "nodeURI";
    /**
     * Include doSearch parameter to immediately initiate search. This will by default hide the form
     */
    public final static String PARAM_DO_SEARCH = "doSearch";
    /**
     * To show the form when having doSearch parameter, include showForm parameter
     */
    public static final String PARAM_SHOW_FORM = "showForm";

    private static Logger logger = LoggerFactory.getLogger(NodeSearchPage.class);
    private WebMarkupContainer tableContainer;
    private WebMarkupContainer tablePanel;
    private ArbilWicketSearchForm searchForm;
    private ArbilDataNodeModel selectedNodeModel = null;
    
    public NodeSearchPage(PageParameters parameters) {
	super(parameters);
	createTable();
	createForm(new ArbilWicketSearch(parameters, newNodeSearchTerm()));
	if (parameters.containsKey(PARAM_NODEURI)) {
	    String nodeURI = parameters.getString(PARAM_NODEURI);
	    try {
		selectedNodeModel = new ArbilDataNodeModel(new URI(nodeURI));
		if (selectedNodeModel.getObject() != null) {
		    selectedNodeModel.waitTillLoaded();
		} else {
		    logger.error("nodeURI with URI " + nodeURI + "could not be loaded");
		    selectedNodeModel = null;
		}
	    } catch (URISyntaxException ex) {
		logger.error("Invalid nodeURI supplied: " + nodeURI, ex);
		selectedNodeModel = null;
	    }
	}

	if(parameters.containsKey(PARAM_DO_SEARCH)){
	    initSearch(parameters.containsKey(PARAM_SHOW_FORM));
	}
    }

    /**
     * Initiates search without showing search form
     */
    private void initSearch(boolean showForm) {
	searchForm.setVisible(showForm);
	searchForm.performSearch(null);
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
	    protected ArbilNodeSearchTerm newNodeSearchTerm() {
		return NodeSearchPage.this.newNodeSearchTerm();
	    }
	};

	searchForm.setOutputMarkupId(true);
	tableContainer.add(searchForm);
    }

    private ArbilSimpleNodeSearchTerm newNodeSearchTerm() {
	ArbilSimpleNodeSearchTerm term = new ArbilSimpleNodeSearchTerm();
	term.setNodeType(ArbilNodeSearchTerm.NODE_TYPE_ALL);
	term.setBooleanAnd(true);
	term.setNotEqual(false);
	return term;
    }
}
