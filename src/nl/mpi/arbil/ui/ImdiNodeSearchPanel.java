package nl.mpi.arbil.ui;

import nl.mpi.arbil.data.ImdiTreeObject;
import java.awt.Component;
import java.net.URI;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import nl.mpi.arbil.data.ImdiLoader;

/**
 * Document   : ImdiNodeSearchPanel
 * Created on : Feb 17, 2009, 4:42:59 PM
 * @author Peter.Withers@mpi.nl 
 */
public class ImdiNodeSearchPanel extends javax.swing.JPanel {

    ImdiNodeSearchPanel thisPanel = this;
    JInternalFrame parentFrame;
    ImdiTableModel resultsTableModel;
    private ImdiTreeObject[] selectedNodes;
    private javax.swing.JButton addButton;
    public javax.swing.JPanel searchTermsPanel;
    private javax.swing.JPanel inputNodePanel;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JButton searchButton;
    private javax.swing.JProgressBar searchProgressBar;
    private javax.swing.JButton stopButton;
    private boolean stopSearch = false;
    private boolean threadRunning = false;
    int totalNodesToSearch = -1;
    private RemoteServerSearchTerm remoteServerSearchTerm = null;

    public ImdiNodeSearchPanel(JInternalFrame parentFrameLocal, ImdiTableModel resultsTableModelLocal, ImdiTreeObject[] localSelectedNodes) {
        parentFrame = parentFrameLocal;
        resultsTableModel = resultsTableModelLocal;
        selectedNodes = localSelectedNodes;
        searchTermsPanel = new javax.swing.JPanel();
        inputNodePanel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        addButton = new javax.swing.JButton();
        stopButton = new javax.swing.JButton();
        searchProgressBar = new javax.swing.JProgressBar();
        searchButton = new javax.swing.JButton();

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.PAGE_AXIS));
        inputNodePanel.setLayout(new java.awt.GridLayout());
        add(inputNodePanel);
        for (ImdiTreeObject currentNode : selectedNodes) {
            JLabel currentLabel = new JLabel(currentNode.toString(), currentNode.getIcon(), JLabel.CENTER);
            inputNodePanel.add(currentLabel);
        }

        searchTermsPanel.setLayout(new javax.swing.BoxLayout(searchTermsPanel, javax.swing.BoxLayout.PAGE_AXIS));
        // check if this search includes remote nodes
        boolean remoteSearch = false;
        for (ImdiTreeObject imdiTreeObject : localSelectedNodes) {
            if (!imdiTreeObject.isLocal()) {
                remoteSearch = true;
                break;
            }
        }
        if (remoteSearch) {
            remoteServerSearchTerm = new RemoteServerSearchTerm(this);
            this.add(remoteServerSearchTerm);
        }

        searchTermsPanel.add(new ImdiNodeSearchTerm(this));
        add(searchTermsPanel);

        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.LINE_AXIS));

        addButton.setText("+");
        addButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    System.out.println("adding new term");
                    stopSearch();
                    searchTermsPanel.add(new ImdiNodeSearchTerm(thisPanel));
                    hideFirstBooleanOption();
//                searchTermsPanel.revalidate();
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        jPanel2.add(addButton);

        searchProgressBar.setString("");
        searchProgressBar.setStringPainted(true);
        jPanel2.add(searchProgressBar);

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
        jPanel2.add(stopButton);

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
        jPanel2.add(searchButton);

        add(jPanel2);
        hideFirstBooleanOption();
        parentFrameLocal.pack();
    }

    private void hideFirstBooleanOption() {
        boolean firstTerm = true;
        for (Component currentTermComp : searchTermsPanel.getComponents()) {
            ((ImdiNodeSearchTerm) currentTermComp).setBooleanVisible(!firstTerm);
            firstTerm = false;
        }
        searchTermsPanel.revalidate();
    }

    public void stopSearch() {
        System.out.println("stop search");
        hideFirstBooleanOption();
        stopSearch = true;
    }

    public void startSearch() {
        System.out.println("start search");
        stopSearch = false;
        searchButton.setEnabled(false);
        stopButton.setEnabled(true);
        resultsTableModel.removeAllImdiRows();
        performSearch();
    }

    private void performSearch() {
        new Thread("performSearch") {

            @Override
            public void run() {
                setPriority(Thread.NORM_PRIORITY - 1);
                threadRunning = true;
                Vector<ImdiTreeObject> foundNodes = new Vector();
                try {
//                    if (totalNodesToSearch == -1) {
//                        searchProgressBar.setIndeterminate(true);
//                    } else {
                    searchProgressBar.setIndeterminate(false);
                    searchProgressBar.setMinimum(0);
                    searchProgressBar.setMaximum(totalNodesToSearch);
                    searchProgressBar.setValue(0);
//                    }
                    for (Component currentTermComp : searchTermsPanel.getComponents()) {
                        ((ImdiNodeSearchTerm) currentTermComp).populateSearchTerm();
                    }
                    int totalSearched = 0;
                    Vector<ImdiTreeObject> localSearchNodes = new Vector<ImdiTreeObject>();
                    Vector<ImdiTreeObject> remoteSearchNodes = new Vector<ImdiTreeObject>();
                    for (ImdiTreeObject imdiTreeObject : selectedNodes) {
                        if (imdiTreeObject.isLocal()) {
                            localSearchNodes.add(imdiTreeObject);
                        } else {
                            remoteSearchNodes.add(imdiTreeObject);
                        }
                    }
                    if (remoteServerSearchTerm != null) {
                        searchProgressBar.setIndeterminate(true);
                        searchProgressBar.setString("connecting to server");
                        for (URI serverFoundUrl : remoteServerSearchTerm.getServerSearchResults(remoteSearchNodes.toArray(new ImdiTreeObject[]{}))) {
                            System.out.println("remote node found: " + serverFoundUrl);
                            localSearchNodes.add(ImdiLoader.getSingleInstance().getImdiObject(null, serverFoundUrl));
                        }
                        searchProgressBar.setString("");
                        searchProgressBar.setIndeterminate(false);
                    }
                    while (localSearchNodes.size() > 0 && !stopSearch) {
                        System.out.println("parentFrame: " + parentFrame.isVisible());
                        Object currentElement = localSearchNodes.remove(0);
                        if (currentElement instanceof ImdiTreeObject) {
                            ImdiTreeObject currentImdiNode = (ImdiTreeObject) currentElement;
                            if (currentImdiNode.isLoading()) { // todo: not all nodes get searched first time, this may be due to them still loading at the time
                                System.out.println("searching: " + currentImdiNode.getUrlString());
                                System.out.println("still loading so putting back into the list: " + currentImdiNode);
                                if (!currentImdiNode.fileNotFound) {
                                    localSearchNodes.add(currentImdiNode);
                                }
                            } else {
                                // perform the search
                                System.out.println("searching: " + currentImdiNode);
                                // add the child nodes
                                if (currentImdiNode.isLocal() || !currentImdiNode.isCorpus()) {
                                    // don't search remote corpus
                                    for (ImdiTreeObject currentChildNode : currentImdiNode.getChildArray()) {
                                        System.out.println("adding to search list: " + currentChildNode);
                                        currentChildNode.registerContainer(this); // this causes the node to be loaded
                                        localSearchNodes.add(currentChildNode);
                                    }
                                }
                                boolean nodePassedFilter = true;
                                for (Component currentTermComponent : searchTermsPanel.getComponents()) {
                                    ImdiNodeSearchTerm currentTermPanel = (ImdiNodeSearchTerm) currentTermComponent;
                                    boolean termPassedFilter = true;
                                    // filter by the node type if entered
                                    if (currentTermPanel.nodeType.equals("Corpus")) {
                                        termPassedFilter = currentImdiNode.isCorpus();
                                    } else if (currentTermPanel.nodeType.equals("Session")) {
                                        termPassedFilter = currentImdiNode.isSession();
                                    } else if (currentTermPanel.nodeType.equals("Catalogue")) {
                                        termPassedFilter = currentImdiNode.isCatalogue();
                                    } else if (!currentTermPanel.nodeType.equals("All")) {
                                        termPassedFilter = currentImdiNode.getUrlString().matches(".*" + currentTermPanel.nodeType + "\\(\\d*?\\)$");
                                    }
                                    if (currentTermPanel.searchFieldName.length() > 0) {// filter by the feild name and search string if entered
                                        termPassedFilter = termPassedFilter && (currentImdiNode.containsFieldValue(currentTermPanel.searchFieldName, currentTermPanel.searchString));
                                    } else if (currentTermPanel.searchString.length() > 0) { // filter by the search string if entered
                                        termPassedFilter = termPassedFilter && (currentImdiNode.containsFieldValue(currentTermPanel.searchString));
                                    }
                                    // invert based on the == / != selection
                                    termPassedFilter = currentTermPanel.notEqual != termPassedFilter;
                                    // apply the and or booleans against the other search terms
                                    if (!currentTermPanel.booleanAnd && nodePassedFilter) {
                                        // we have moved into an OR block so if we already have a positive result then exit the term checking loop
                                        break;
                                    }
                                    if (currentTermPanel.booleanAnd) {
                                        nodePassedFilter = (nodePassedFilter && termPassedFilter);
                                    } else {
                                        nodePassedFilter = (nodePassedFilter || termPassedFilter);
                                    }
                                }
                                totalSearched++;
                                // if the node has no fields it should still be added since it will only pass a search if for instance the search is for actors and in that case it should be shown even if blank
                                if (nodePassedFilter) {
                                    foundNodes.add(currentImdiNode);
                                    resultsTableModel.addSingleImdiObject(currentImdiNode);
                                } else {
                                    currentImdiNode.removeContainer(this);
                                }
                                if (totalNodesToSearch < totalSearched + localSearchNodes.size()) {
                                    totalNodesToSearch = totalSearched + localSearchNodes.size();
                                }
                                searchProgressBar.setMaximum(totalNodesToSearch);
                                searchProgressBar.setValue(totalSearched);
                                // todo: indicate how many metadata files searched rather than sub nodes
                                searchProgressBar.setString("searched: " + totalSearched + " found: " + foundNodes.size());
                            }
                        }
                        if (!parentFrame.isVisible()) {
                            // in the case that the user has closed the search window we want to stop the thread
                            stopSearch = true;
                        }
                    }

                    if (stopSearch) {
                        searchProgressBar.setString("search canceled");
                    } else {
                        // collect the max nodes found only if the search completed
                        totalNodesToSearch = totalSearched;
                    }
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
                searchProgressBar.setIndeterminate(false);
                searchProgressBar.setValue(0);
                searchProgressBar.setMaximum(1000);
                searchButton.setEnabled(true);
                stopButton.setEnabled(false);
                threadRunning = false;
                // add the results to the table
                resultsTableModel.addImdiObjects(foundNodes.elements());
                foundNodes.removeAllElements();
            }
        }.start();
    }
}
