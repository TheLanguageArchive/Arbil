package nl.mpi.arbil.wicket.components;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.search.ArbilNodeSearchTerm;
import nl.mpi.arbil.search.ArbilSearch;
import nl.mpi.arbil.wicket.model.ArbilWicketRemoteSearchTerm;
import nl.mpi.arbil.wicket.model.ArbilWicketTableModel;
import nl.mpi.arbil.wicket.model.ArbilWicketSearch;
import org.apache.wicket.Application;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
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
    private ListView<ArbilNodeSearchTerm> nodeSearchTerms;
    private AjaxButton stopButton;
    private ProgressBar progressbar;
    private String progressMessage = null;
    private int progress = 0;

    public ArbilWicketSearchForm(String id, IModel<ArbilWicketSearch> model) {
	super(id);
	setModel(new CompoundPropertyModel<ArbilWicketSearch>(model));

	addSearchNodesLabel();
	addRemoteSearchTermField();
	addNodeSearchTerms();
	addSearchButton();
	addStopButton();
	addProgressBar();
    }

    private void addSearchNodesLabel() {
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
    }

    private void addRemoteSearchTermField() {

	WebMarkupContainer remoteSearchTermContainer = new WebMarkupContainer("remoteSearchTermContainer") {

	    @Override
	    public boolean isVisible() {
		return isRemote();
	    }
	};

	// Remote search term (only for remote searches)
	remoteSearchTermContainer.add(new TextField("remoteSearchTerm"));
	add(remoteSearchTermContainer);

    }

    private void addNodeSearchTerms() {
	final WebMarkupContainer nodeSearchTermsContainer = new WebMarkupContainer("nodeSearchTermsContainer");

	// Collection of search terms
	nodeSearchTermsContainer.add(nodeSearchTerms = new PropertyListView<ArbilNodeSearchTerm>("nodeSearchTerms", getModelObject().getNodeSearchTerms()) {

	    @Override
	    protected void populateItem(final ListItem<ArbilNodeSearchTerm> item) {

		addBooleanAnd("booleanAnd", item);
		addNotEquals("notEqual", item);

		item.add(new DropDownChoice<String>("nodeType", ArbilNodeSearchTerm.NODE_TYPES));

		item.add(new TextField("searchFieldName"));
		item.add(new TextField("searchString"));

		addRemoveButton("removeNodeSearchTerm", item);
	    }

	    private void addBooleanAnd(String id, final ListItem<ArbilNodeSearchTerm> item) {
		item.add(new DropDownChoice<Boolean>(id, Arrays.asList(new Boolean[]{true, false}), new IChoiceRenderer<Boolean>() {

		    public Object getDisplayValue(Boolean object) {
			return object ? ArbilNodeSearchTerm.BOOLEAN_AND : ArbilNodeSearchTerm.BOOLEAN_OR;
		    }

		    public String getIdValue(Boolean object, int index) {
			return Boolean.toString(object);
		    }
		}) {

		    @Override
		    public boolean isVisible() {
			// Don't show for first item
			return ArbilWicketSearchForm.this.getModelObject().getNodeSearchTerms().indexOf(item.getModelObject()) > 0;
		    }
		});
	    }

	    private void addNotEquals(String id, final ListItem<ArbilNodeSearchTerm> item) {

		item.add(new DropDownChoice<Boolean>(id, Arrays.asList(new Boolean[]{true, false}), new IChoiceRenderer<Boolean>() {

		    public Object getDisplayValue(Boolean object) {
			return object ? "!=" : "==";
		    }

		    public String getIdValue(Boolean object, int index) {
			return Boolean.toString(object);
		    }
		}));
	    }

	    private void addRemoveButton(String id, final ListItem<ArbilNodeSearchTerm> item) {

		item.add(new AjaxFallbackButton(id, ArbilWicketSearchForm.this) {

		    @Override
		    protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
			List<ArbilNodeSearchTerm> searchTerms = ArbilWicketSearchForm.this.getModelObject().getNodeSearchTerms();
			searchTerms.remove(item.getModelObject());

			// When leaving only one, make sure it has boolean AND
			if (searchTerms.size() == 1) {
			    searchTerms.get(0).setBooleanAnd(true);
			}

			nodeSearchTermsContainer.addOrReplace(nodeSearchTerms);
			if (target != null) {
			    target.addComponent(nodeSearchTermsContainer);
			}
		    }

		    @Override
		    public boolean isVisible() {
			// Only show when there are multiple items
			return ArbilWicketSearchForm.this.getModelObject().getNodeSearchTerms().size() > 1;
		    }
		});
	    }
	});

	nodeSearchTermsContainer.add(new AjaxFallbackButton("addNodeSearchTerm", this) {

	    @Override
	    protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
		ArbilWicketSearchForm.this.getModelObject().getNodeSearchTerms().add(newNodeSearchTerm());
		nodeSearchTermsContainer.addOrReplace(nodeSearchTerms);
		if (target != null) {
		    target.addComponent(nodeSearchTermsContainer);
		}
	    }
	});

	nodeSearchTermsContainer.setOutputMarkupId(true);
	add(nodeSearchTermsContainer);
    }

    private void addSearchButton() {
	// 'Search' button
	add(new AjaxFallbackButton("searchSubmit", this) {

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

    private void addStopButton() {
	// Stop button. Will only show in Ajax context (i.e. when progress bar shows)
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
    }

    private void addProgressBar() {

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

    public void performSearch(AjaxRequestTarget target) {
	performSearch(getModelObject(), target);
    }

    private void performSearch(final ArbilWicketSearch searchTerm, AjaxRequestTarget target) {

	if (null != searchTerm && isNodesSelected() && (!isRemote() || null != searchTerm.getRemoteSearchTerm())) {
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
				    progress = (searchService.getTotalNodesToSearch() == 0) ? 0 : (100 * searchService.getTotalSearched()) / searchService.getTotalNodesToSearch();
				    progressMessage = "Searched: " + searchService.getTotalSearched() + "/" + searchService.getTotalNodesToSearch() + ". Found: " + searchService.getFoundNodes().size();
				}
				searchLock.wait(10);
			    } catch (InterruptedException ex) {
			    }
			}
		    }
		});
	    }

	    if (target != null) {
		stopButton.setVisible(true);
		progress = 0;
		progressMessage = "Searching...";
		progressbar.setVisible(true);
		progressbar.start(target);
	    }

	    if (target == null) {
		// Non-ajax. Search until complete, then call callback
		searchNodes();
		onSearchComplete(resultsModel, target);
	    } else {
		final Application app = getApplication();
		final Session sess = getSession();
		// Ajax, search in separate thread so that ajax target can be sent
		new Thread() {

		    @Override
		    public void run() {
			// Set application and session in this thread, so session specific services can be resolved by data loader
			Application.set(app);
			Session.set(sess);
			searchNodes();
			Application.unset();
			Session.unset();
		    }
		}.start();
	    }
	}
    }

    private void searchNodes() {
	searchService.splitLocalRemote();
	if (isRemote()) {
	    searchService.fetchRemoteSearchResults();
	}
	searchService.searchLocalNodes();
	//resultsModel.resumeReload();

	synchronized (searchLock) {
	    searchService = null;
	}
    }

    protected abstract ArbilNodeSearchTerm newNodeSearchTerm();

    protected abstract void onSearchComplete(ArbilWicketTableModel resultsTableModel, AjaxRequestTarget target);

    protected abstract Collection<ArbilNode> getSelectedNodes();

    protected abstract boolean isRemote();

    protected abstract boolean isNodesSelected();
}
