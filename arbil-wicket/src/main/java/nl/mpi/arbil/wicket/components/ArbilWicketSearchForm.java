package nl.mpi.arbil.wicket.components;

import java.util.Arrays;
import java.util.Collection;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.search.ArbilNodeSearchTerm;
import nl.mpi.arbil.search.ArbilSearch;
import nl.mpi.arbil.wicket.model.ArbilWicketNodeSearchTerm;
import nl.mpi.arbil.wicket.model.ArbilWicketRemoteSearchTerm;
import nl.mpi.arbil.wicket.model.ArbilWicketTableModel;
import nl.mpi.arbil.wicket.model.ArbilWicketSearch;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PropertyListView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.wicketstuff.progressbar.ProgressBar;
import org.wicketstuff.progressbar.Progression;
import org.wicketstuff.progressbar.ProgressionModel;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class ArbilWicketSearchForm extends Form<ArbilWicketSearch> {

    private final String searchLock = new String();
    private static final String SELECT_NODES_STRING = "Select a node and enter search terms";
    private transient ArbilSearch searchService;
    private ArbilWicketTableModel resultsModel;
    private AjaxButton stopButton;
    private ProgressBar progressbar;
    private String progressMessage = null;
    private int progress = 0;

    public ArbilWicketSearchForm(String id, IModel<ArbilWicketSearch> model) {
	super(id);
	setModel(new CompoundPropertyModel<ArbilWicketSearch>(model));

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

	// Remote search term (only for remote searches)
	add(new TextField("remoteSearchTerm") {

	    @Override
	    public boolean isVisible() {
		return isRemote();
	    }
	});


	// Collection of search terms
	add(new PropertyListView<ArbilWicketNodeSearchTerm>("nodeSearchTerms",getModelObject().getNodeSearchTerms()) {

	    @Override
	    protected void populateItem(ListItem<ArbilWicketNodeSearchTerm> item) {
		item.add(new DropDownChoice<String>("nodeType", Arrays.asList(ArbilNodeSearchTerm.NODE_TYPES)));

		item.add(new TextField("searchFieldName"));
		item.add(new TextField("searchString"));
	    }
	});

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

	// Stop button
	add(stopButton = new AjaxButton("searchStop") {

	    @Override
	    protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
		synchronized (searchLock) {
		    if (searchService != null) {
			searchService.stopSearch();
		    }
		}
	    }
	});
	stopButton.setOutputMarkupId(true);
	stopButton.setVisible(false);

	// Progress bar
	add(progressbar = new ProgressBar("progress", new ProgressionModel() {

	    @Override
	    protected Progression getProgression() {
		return new Progression(0) {

		    @Override
		    public int getProgress() {
			synchronized (searchLock) {
			    return progress;
			}
		    }

		    @Override
		    public String getProgressMessage() {
			synchronized (searchLock) {
			    return progressMessage;
			}
		    }

		    @Override
		    public boolean isDone() {
			synchronized (searchLock) {
			    return searchService == null;
			}
		    }
		};
	    }
	}) {

	    @Override
	    protected void onFinished(AjaxRequestTarget target) {
		setVisible(false);
		stopButton.setVisible(false);
		onSearchComplete(resultsModel, target);
	    }
	});
	progressbar.setVisible(false);
    }

    private void performSearch(final ArbilWicketSearch searchTerm, AjaxRequestTarget target) {

	if (null != searchTerm && isNodesSelected()) {
	    if (resultsModel != null) {
		resultsModel.removeAllArbilDataNodeRows();
	    }
	    resultsModel = new ArbilWicketTableModel();

	    synchronized (searchLock) {
		searchService = new ArbilSearch(getSelectedNodes(), searchTerm.getNodeSearchTerms(), new ArbilWicketRemoteSearchTerm(searchTerm.getRemoteSearchTerm()),
			resultsModel, // as table model
			resultsModel, // as DataNodeContainer
			new ArbilSearch.ArbilSearchListener() {

		    public void searchProgress(Object currentElement) {
			synchronized (searchLock) {
			    try {
				if (searchService.isSearchStopped()) {
				    progressMessage = "Search stopped. Found: " + searchService.getFoundNodes().size();
				} else {
				    progress = (100 * searchService.getTotalSearched()) / searchService.getTotalNodesToSearch();
				    progressMessage = "Searched: " + searchService.getTotalSearched() + "/" + searchService.getTotalNodesToSearch() + ". Found: " + searchService.getFoundNodes().size();
				}
				searchLock.wait(10);
			    } catch (InterruptedException ex) {
			    }
			}
		    }
		});
	    }

	    stopButton.setVisible(true);

	    progress = 0;
	    progressMessage = "Searching...";
	    progressbar.setVisible(true);
	    progressbar.start(target);

	    // Search in separate thread so that ajax target can be sent
	    new Thread() {

		@Override
		public void run() {
		    resultsModel.suspendReload();

		    searchService.splitLocalRemote();
		    if (isRemote()) {
			searchService.fetchRemoteSearchResults();
		    }
		    searchService.searchLocalNodes();
		    synchronized (searchLock) {
			searchService = null;
		    }

		    resultsModel.resumeReload();
		}
	    }.start();
	}
    }

    protected abstract void onSearchComplete(ArbilWicketTableModel resultsTableModel, AjaxRequestTarget target);

    protected abstract Collection<ArbilDataNode> getSelectedNodes();

    protected abstract boolean isRemote();

    protected abstract boolean isNodesSelected();
}
