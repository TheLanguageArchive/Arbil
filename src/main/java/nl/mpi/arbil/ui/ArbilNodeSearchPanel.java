package nl.mpi.arbil.ui;

import nl.mpi.arbil.data.ArbilDataNode;
import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
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
    private Thread searchThread = null;

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
	for (ArbilNode arbilDataNode : selectedNodes) {
	    if (!arbilDataNode.isLocal()) {
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

    public void waitForSearch() {
	synchronized (searchThreadLock) {
	    while (searchThread != null && searchThread.isAlive()) {
		try {
		    searchThreadLock.wait(1000);
		} catch (InterruptedException ie) {
		}
	    }
	}
    }

    public synchronized void startSearch() {
	if (searchThread != null && searchThread.isAlive()) {
	    stopSearch();
	    waitForSearch();
	}
	System.out.println("start search");
	searchButton.setEnabled(false);
	stopButton.setEnabled(true);
	resultsTableModel.removeAllArbilDataNodeRows();
	performSearch();
    }

    private void performSearch() {
	searchThread = new Thread(new SearchThread(), "performSearch");
	searchThread.setPriority(Thread.NORM_PRIORITY - 1);
	searchThread.start();
    }

    /**
     * @return the searchTermsPanel
     */
    public JPanel getSearchTermsPanel() {
	return searchTermsPanel;
    }
    private final Object searchThreadLock = new Object();

    private class SearchThread implements Runnable, ArbilSearch.ArbilSearchListener {

	@Override
	public void run() {
	    synchronized (searchThreadLock) {
		try {
		    initSearchService();
		    prepareUI();
		    populateSearchTerms();
		    saveColumnOptions();
		    executeSearch();
		} catch (Exception ex) {
		    GuiHelper.linorgBugCatcher.logError(ex);
		}
		finishUI();
		// add the results to the table
		resultsTableModel.addArbilDataNodes(Collections.enumeration(searchService.getFoundNodes()));
		searchService.clearResults();
		searchThreadLock.notifyAll();
	    }
	}

	private void initSearchService() {
	    ArrayList<ArbilNodeSearchTerm> searchTerms = new ArrayList<ArbilNodeSearchTerm>(getComponentCount());
	    for (Component component : searchTermsPanel.getComponents()) {
		if (component instanceof ArbilNodeSearchTermPanel) {
		    searchTerms.add((ArbilNodeSearchTermPanel) component);
		}
	    }
	    searchService = new ArbilSearch(Arrays.asList(selectedNodes), searchTerms, remoteServerSearchTerm, resultsTableModel, ArbilNodeSearchPanel.this, this);
	}

	private void populateSearchTerms() {
	    try {
		SwingUtilities.invokeAndWait(new Runnable() {

		    public void run() {
			for (Component currentTermComp : searchTermsPanel.getComponents()) {
			    ((ArbilNodeSearchTermPanel) currentTermComp).populateSearchTerm();
			}
		    }
		});
	    } catch (InterruptedException ex) {
	    } catch (InvocationTargetException ex) {
	    }
	}

	private void saveColumnOptions() {
	    ArrayList<String> columns = new ArrayList<String>(searchTermsPanel.getComponentCount());
	    for (Component currentTermComp : searchTermsPanel.getComponents()) {
		((ArbilNodeSearchTermPanel) currentTermComp).addCurrentSearchColumnOption();
		columns.add(((ArbilNodeSearchTermPanel) currentTermComp).searchFieldName);
	    }
	    ArbilNodeSearchColumnComboBox.addOptions(columns);
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
	    try {
		SwingUtilities.invokeAndWait(new Runnable() {

		    public void run() {
			searchProgressBar.setIndeterminate(false);
			searchProgressBar.setValue(0);
			searchProgressBar.setMaximum(1000);
			searchButton.setEnabled(true);
			stopButton.setEnabled(false);
		    }
		});
	    } catch (InterruptedException ex) {
	    } catch (InvocationTargetException ex) {
	    }
	}

	private void prepareUI() {
	    try {
		SwingUtilities.invokeAndWait(new Runnable() {

		    public void run() {
			searchProgressBar.setIndeterminate(true);
			searchProgressBar.setMinimum(0);
			searchProgressBar.setMaximum(searchService.getTotalNodesToSearch());
		    }
		});
	    } catch (InterruptedException ex) {
	    } catch (InvocationTargetException ex) {
	    }
	}

	/**
	 * Implements ArbilSearchListener method, called for each element that gets searched
	 * @param currentElement 
	 */
	public void searchProgress(Object currentElement) {
	    if (currentElement instanceof ArbilNode) {
		// todo: indicate how many metadata files searched rather than sub nodes
		searchProgressBar.setString("searched: " + searchService.getTotalSearched() + "/" + searchService.getTotalNodesToSearch() + " found: " + searchService.getFoundNodes().size());
	    }
	    if (!parentFrame.isVisible() && searchService != null) {
		// in the case that the user has closed the search window we want to stop the searchThread
		searchService.stopSearch();
	    }
	}
    }

    /**
     * Data node is to be removed from the container
     * @param dataNode Data node that should be removed
     */
    public void dataNodeRemoved(ArbilNode dataNode) {
	// Nothing to do, but this is implements  ArbilDataNodeContainer
    }

    /**
     * Data node is clearing its icon
     * @param dataNode Data node that is clearing its icon
     */
    public void dataNodeIconCleared(ArbilNode dataNode) {
	// Nothing to do
    }

    /**
     * A new child node has been added to the destination node
     * @param destination Node to which a node has been added
     * @param newNode The newly added node
     */
    public void dataNodeChildAdded(ArbilNode destination, ArbilNode newNode) {
	// Nothing to do
    }
}
