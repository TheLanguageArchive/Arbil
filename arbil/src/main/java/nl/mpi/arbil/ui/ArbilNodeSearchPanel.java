package nl.mpi.arbil.ui;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import nl.mpi.arbil.data.ArbilDataNodeContainer;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.search.ArbilNodeSearchTerm;
import nl.mpi.arbil.search.ArbilSearch;

/**
 * Document   : ArbilNodeSearchPanel
 * Created on : Feb 17, 2009, 4:42:59 PM
 * @author Peter.Withers@mpi.nl 
 */
public class ArbilNodeSearchPanel extends JPanel implements ArbilDataNodeContainer {

    private ArbilNodeSearchPanel thisPanel = this;
    private JInternalFrame parentFrame;
    private ArbilTableModel resultsTableModel;
    private ArbilNode[] selectedNodes;
    private JPanel searchTermsPanel;
    private JPanel inputNodePanel;
    private JProgressBar searchProgressBar;
    private JButton searchButton;
    private JButton stopButton;
    private RemoteServerSearchTermPanel remoteServerSearchTerm = null;
    private ArbilSearch searchService;

    public ArbilNodeSearchPanel(JInternalFrame parentFrameLocal, ArbilTableModel resultsTableModelLocal, ArbilNode[] localSelectedNodes) {
	parentFrame = parentFrameLocal;
	resultsTableModel = resultsTableModelLocal;
	selectedNodes = localSelectedNodes.clone();
	searchTermsPanel = new JPanel();

	setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

	initNodePanel();
	add(inputNodePanel);

	initSearchTermsPanel();
	add(searchTermsPanel);

	JPanel buttonsProgressPanel = createButtonsProgressPanel();
	add(buttonsProgressPanel);

	hideFirstBooleanOption();
	parentFrame.pack();
    }

    private JPanel createButtonsProgressPanel() {
	JPanel buttonsProgressPanel = new JPanel();
	buttonsProgressPanel.setLayout(new BoxLayout(buttonsProgressPanel, BoxLayout.LINE_AXIS));

	JButton addButton = new JButton();
	addButton.setText("+");
	addButton.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    System.out.println("adding new term");
		    stopSearch();
		    getSearchTermsPanel().add(new ArbilNodeSearchTermPanel(thisPanel));
		    hideFirstBooleanOption();
//                searchTermsPanel.revalidate();
		} catch (Exception ex) {
		    GuiHelper.linorgBugCatcher.logError(ex);
		}
	    }
	});
	buttonsProgressPanel.add(addButton);

	searchProgressBar = new JProgressBar();
	searchProgressBar.setString("");
	searchProgressBar.setStringPainted(true);
	buttonsProgressPanel.add(searchProgressBar);

	stopButton = new JButton();
	stopButton.setText("stop");
	stopButton.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    stopSearch();
		} catch (Exception ex) {
		    GuiHelper.linorgBugCatcher.logError(ex);
		}
	    }
	});
	stopButton.setEnabled(false);
	buttonsProgressPanel.add(stopButton);

	searchButton = new JButton();
	searchButton.setText("search");
	searchButton.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    startSearch();
		} catch (Exception ex) {
		    GuiHelper.linorgBugCatcher.logError(ex);
		}
	    }
	});
	buttonsProgressPanel.add(searchButton);
	return buttonsProgressPanel;
    }

    private void initSearchTermsPanel() {
	searchTermsPanel.setLayout(new BoxLayout(searchTermsPanel, BoxLayout.PAGE_AXIS));
	// check if this search includes remote nodes
	boolean remoteSearch = false;
	for (ArbilNode arbilNode : selectedNodes) {
	    if (!arbilNode.isLocal()) {
		remoteSearch = true;
		break;
	    }
	}
	if (remoteSearch) {
	    remoteServerSearchTerm = new RemoteServerSearchTermPanel(this);
	    this.add(remoteServerSearchTerm);
	}
	searchTermsPanel.add(new ArbilNodeSearchTermPanel(this));
    }

    private void initNodePanel() {
	inputNodePanel = new JPanel();
	inputNodePanel.setLayout(new java.awt.GridLayout());
	for (ArbilNode currentNode : selectedNodes) {
	    JLabel currentLabel = new JLabel(currentNode.toString(), currentNode.getIcon(), JLabel.CENTER);
	    inputNodePanel.add(currentLabel);
	}
    }

    private void hideFirstBooleanOption() {
	boolean firstTerm = true;
	for (Component currentTermComp : searchTermsPanel.getComponents()) {
	    ((ArbilNodeSearchTermPanel) currentTermComp).setBooleanVisible(!firstTerm);
	    firstTerm = false;
	}
	searchTermsPanel.revalidate();
    }

    public void stopSearch() {
	System.out.println("stop search");
	hideFirstBooleanOption();
	if (searchService != null) {
	    searchService.stopSearch();
	}
    }

    public void startSearch() {
	System.out.println("start search");
	searchButton.setEnabled(false);
	stopButton.setEnabled(true);
	resultsTableModel.removeAllArbilDataNodeRows();
	performSearch();
    }

    private void performSearch() {
	Thread thread = new Thread(new SearchThread(), "performSearch");
	thread.setPriority(Thread.NORM_PRIORITY - 1);
	thread.start();
    }

    /**
     * @return the searchTermsPanel
     */
    public JPanel getSearchTermsPanel() {
	return searchTermsPanel;
    }

    private class SearchThread implements Runnable, ArbilSearch.ArbilSearchListener {

	@Override
	public void run() {
	    try {
		initSearchService();
		prepareUI();
		populateSearchTerms();
		executeSearch();
	    } catch (Exception ex) {
		GuiHelper.linorgBugCatcher.logError(ex);
	    }
	    finishUI();
	    // add the results to the table
	    resultsTableModel.addArbilDataNodes(Collections.enumeration(searchService.getFoundNodes()));
	    searchService.clearResults();
	}

	private void initSearchService() {
	    ArrayList searchTerms = new ArrayList<ArbilNodeSearchTerm>(getComponentCount());
	    for (Component component : searchTermsPanel.getComponents()) {
		if (component instanceof ArbilNodeSearchTermPanel) {
		    searchTerms.add((ArbilNodeSearchTermPanel) component);
		}
	    }
	    searchService = new ArbilSearch(Arrays.asList(selectedNodes), searchTerms, remoteServerSearchTerm, resultsTableModel, ArbilNodeSearchPanel.this, this);
	}

	private void populateSearchTerms() {
	    for (Component currentTermComp : searchTermsPanel.getComponents()) {
		((ArbilNodeSearchTermPanel) currentTermComp).populateSearchTerm();
	    }
	}

	private void executeSearch() {
	    searchService.splitLocalRemote();

	    if (remoteServerSearchTerm != null) {
		searchProgressBar.setIndeterminate(true);
		searchProgressBar.setString("connecting to server");

		searchService.fetchRemoteSearchResults();

		searchProgressBar.setString("");
		searchProgressBar.setIndeterminate(false);
	    }

	    searchService.searchLocalNodes();

	    if (searchService.isSearchStopped()) {
		searchProgressBar.setString("search canceled");
	    } else {
		// collect the max nodes found only if the search completed
		searchService.setTotalNodesToSearch(searchService.getTotalSearched());
	    }
	}

	private void finishUI() {
	    searchProgressBar.setIndeterminate(false);
	    searchProgressBar.setValue(0);
	    searchProgressBar.setMaximum(1000);
	    searchButton.setEnabled(true);
	    stopButton.setEnabled(false);
	}

	private void prepareUI() {
	    searchProgressBar.setIndeterminate(false);
	    searchProgressBar.setMinimum(0);
	    searchProgressBar.setMaximum(searchService.getTotalNodesToSearch());
	    searchProgressBar.setValue(0);
	}

	/**
	 * Implements ArbilSearchListener method, called for each element that gets searched
	 * @param currentElement 
	 */
	public void searchProgress(Object currentElement) {
	    if (currentElement instanceof ArbilNode) {
		searchProgressBar.setMaximum(searchService.getTotalNodesToSearch());
		searchProgressBar.setValue(searchService.getTotalSearched());
		// todo: indicate how many metadata files searched rather than sub nodes
		searchProgressBar.setString("searched: " + searchService.getTotalSearched() + "/" + searchService.getTotalNodesToSearch() + " found: " + searchService.getFoundNodes().size());
	    }
	    if (!parentFrame.isVisible() && searchService != null) {
		// in the case that the user has closed the search window we want to stop the thread
		searchService.stopSearch();
	    }
	}
    }

    /**
     * Data node is to be removed from the container
     * @param dataNode Data node that should be removed
     */
    public void dataNodeRemoved(ArbilNode node) {
	// Nothing to do, but this is implements  ArbilDataNodeContainer
    }

    /**
     * Data node is clearing its icon
     * @param node Data node that is clearing its icon
     */
    public void dataNodeIconCleared(ArbilNode node) {
	// Nothing to do
    }
}
