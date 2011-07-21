package nl.mpi.arbil.wicket.components;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.locks.ReentrantLock;
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
import org.wicketstuff.progressbar.ProgressBar;
import org.wicketstuff.progressbar.Progression;
import org.wicketstuff.progressbar.ProgressionModel;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class ArbilWicketSearchForm extends Form<ArbilWicketNodeSearchTerm> {

    private static final String SELECT_NODES_STRING = "Select a node and enter search terms";
    private transient ArbilSearch searchService;
    private ProgressBar progressbar;
    private String progessMessage = null;
    private int progress = 0;
    private ArbilWicketTableModel resultsModel;
    private final String progressLock = new String();

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

	// Progress bar
	add(progressbar = new ProgressBar("progress", new ProgressionModel() {

	    @Override
	    protected Progression getProgression() {
		return new Progression(0) {

		    @Override
		    public int getProgress() {
			synchronized (progressLock) {
			    return progress;
			}
		    }

		    @Override
		    public String getProgressMessage() {
			synchronized (progressLock) {
			    return progessMessage;
			}
		    }

		    @Override
		    public boolean isDone() {
			synchronized (progressLock) {
			    return searchService == null;
			}
		    }
		};
	    }
	}) {

	    @Override
	    protected void onFinished(AjaxRequestTarget target) {
		setVisible(false);
		onSearchComplete(resultsModel, target);
	    }
	});
	progressbar.setVisible(false);
    }

    private void performSearch(final ArbilWicketNodeSearchTerm searchTerm, AjaxRequestTarget target) {
	if (null != searchTerm && isNodesSelected()) {

	    synchronized (progressLock) {
		searchService = new ArbilSearch(getSelectedNodes(), Collections.singleton(searchTerm), searchTerm.getRemoteServerSearchTerm(),
			resultsModel, // as table model
			resultsModel, // as DataNodeContainer
			new ArbilSearch.ArbilSearchListener() {

		    public void searchProgress(Object currentElement) {
			synchronized (progressLock) {
			    try {
				progress = (100 * searchService.getTotalSearched()) / searchService.getTotalNodesToSearch();
				progessMessage = "searched: " + searchService.getTotalSearched() + "/" + searchService.getTotalNodesToSearch() + " found: " + searchService.getFoundNodes().size();
				progressLock.wait(10);
			    } catch (InterruptedException ex) {
			    }
			}
		    }
		});
	    }

	    progress = 0;
	    progressbar.setVisible(true);
	    progressbar.start(target);

	    // Search in separate thread so that ajax target can be sent
	    new Thread() {

		@Override
		public void run() {
		    resultsModel = new ArbilWicketTableModel();

		    searchService.splitLocalRemote();
		    if (isRemote()) {
			searchService.fetchRemoteSearchResults();
		    }
		    searchService.searchLocalNodes();
		    synchronized (progressLock) {
			searchService = null;
		    }
		}
	    }.start();
	}
    }

    protected abstract void onSearchComplete(ArbilWicketTableModel resultsTableModel, AjaxRequestTarget target);

    protected abstract Collection<ArbilDataNode> getSelectedNodes();

    protected abstract boolean isRemote();

    protected abstract boolean isNodesSelected();
}
