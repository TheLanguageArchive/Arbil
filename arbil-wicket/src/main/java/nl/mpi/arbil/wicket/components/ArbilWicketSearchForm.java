package nl.mpi.arbil.wicket.components;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.search.ArbilNodeSearchTerm;
import nl.mpi.arbil.search.ArbilSearch;
import nl.mpi.arbil.wicket.model.ArbilWicketTableModel;
import nl.mpi.arbil.wicket.model.ArbilWicketNodeSearchTerm;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class ArbilWicketSearchForm extends Form<ArbilWicketNodeSearchTerm> {

    private static final String SELECT_NODES_STRING = "Select a node and enter search terms";
    private ArbilSearch searchService;

    public ArbilWicketSearchForm(String id, IModel<ArbilWicketNodeSearchTerm> model) {
	super(id, model);

	add(new DropDownChoice<String>("nodeType", Arrays.asList(ArbilNodeSearchTerm.NODE_TYPES)));
	add(new TextField("remoteSearchString") {

	    @Override
	    public boolean isVisible() {
		return isRemote();
	    }
	});
	add(new TextField("searchFieldName"));
	add(new TextField("searchString"));

	// Label that shows either the nodes selected for searchService or a string informing the user they should select one
	add(new Label("searchNodes", new Model<String>() {

	    @Override
	    public String getObject() {
		if (isNodesSelected()) {
		    return "Searching in " + Arrays.toString(getSelectedNodes().toArray());
		} else {
		    return SELECT_NODES_STRING;
		}
	    }
	}));

	// 'Search' button
	add(new AjaxButton("searchSubmit", this) {

	    @Override
	    protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
		performSearch(ArbilWicketSearchForm.this.getModelObject(), target);
	    }

	    @Override
	    public boolean isEnabled() {
		// Nodes should be selected for this to be enabled
		return isNodesSelected();
	    }
	});
    }

    private void performSearch(ArbilWicketNodeSearchTerm searchTerm, AjaxRequestTarget target) {
	Collection<ArbilDataNode> selectedNodes = getSelectedNodes();
	if (null != searchTerm && isNodesSelected()) {
	    ArbilWicketTableModel model = new ArbilWicketTableModel();
	    searchService = new ArbilSearch(selectedNodes, Collections.singleton(searchTerm), searchTerm.getRemoteServerSearchTerm(),
		    model, // as table model
		    model, // as DataNodeContainer
		    null); // no listener (for now)

	    searchService.splitLocalRemote();
	    if (isRemote()) {
		searchService.fetchRemoteSearchResults();
	    }
	    searchService.searchLocalNodes();
	    // Done. Remove searchService to indicate search is not active
	    searchService = null;

	    onSearchComplete(model, target);
	}
    }

    protected abstract void onSearchComplete(ArbilWicketTableModel resultsTableModel, AjaxRequestTarget target);

    protected abstract Collection<ArbilDataNode> getSelectedNodes();

    protected abstract boolean isRemote();

    protected abstract boolean isNodesSelected();
}
