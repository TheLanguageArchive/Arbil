/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author petwit
 */
public class ThreadedDialog {

    private JDialog searchDialog;
    private JProgressBar progressBar;
    private JButton searchButton,  stopButton,  showResultsButton;
    JPanel searchPanel;
    private JTextArea taskOutput;
    // variables used but the search thread
    private JTextField searchLabel;
    private Hashtable foundNodes = new Hashtable();
    // variables used by the copy thread
    String destinationDirectory;
    // variables used by all threads
    private boolean stopSearch = false;
    private boolean threadARunning = false;
    private boolean threadBRunning = false;
    private Vector selectedNodes;

//    public Hashtable getFoundNodes() {
//        return foundNodes;
//    }
    public void searchNodes(Vector localSelectedNodes, String searchString) {
        selectedNodes = localSelectedNodes;
        searchDialog.setTitle("Search");
        if (!selectedNodesContainImdi()) {
            JOptionPane.showMessageDialog(GuiHelper.linorgWindowManager.desktopPane, "No relevant nodes are selected", searchDialog.getTitle(), 0);
            return;
        }
        if (searchString != null) {
            searchLabel.setText(searchString);
            performSearch();
        }
        searchDialog.setVisible(true);
    }

    public void copyToCache(Vector localSelectedNodes) {
        selectedNodes = localSelectedNodes;
        searchDialog.setTitle("Copy Brach");
        if (!selectedNodesContainImdi()) {
            JOptionPane.showMessageDialog(GuiHelper.linorgWindowManager.desktopPane, "No relevant nodes are selected", searchDialog.getTitle(), 0);
            return;
        }
        //String mirrorNameString = JOptionPane.showInputDialog(destinationComp, "Enter a tile for the local mirror");
        destinationDirectory = GuiHelper.linorgSessionStorage.storageDirectory + File.separatorChar + "imdicache";
        File destinationFile = new File(destinationDirectory);
        boolean cacheDirExists = destinationFile.exists();
        if (!cacheDirExists) {
            cacheDirExists = destinationFile.mkdir();
        }
        appendToTaskOutput("destination directory:" + destinationDirectory);
        //destinationDirectory = destinationDirectory + File.separator + mirrorNameString;
        //boolean brachDirCreated = (new File(destinationDirectory)).mkdir();
        // TODO: remove the branch directory and replace it with a named node in the locations settings or just a named imdinode
        if (cacheDirExists) {
            destinationDirectory = destinationDirectory + File.separatorChar;
            performCopy();
            searchDialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(GuiHelper.linorgWindowManager.desktopPane, "Could not create the local directory", searchDialog.getTitle(), 0);
        }
    }

    private boolean selectedNodesContainImdi() {
        Enumeration selectedNodesEnum = selectedNodes.elements();
        while (selectedNodesEnum.hasMoreElements()) {
            if (selectedNodesEnum.nextElement() instanceof ImdiHelper.ImdiTreeObject) {
                return true;
            }
        }
        return false;
    }

    public ThreadedDialog(Component targetComponent) {
        searchDialog = new JDialog(JOptionPane.getFrameForComponent(GuiHelper.linorgWindowManager.desktopPane), true);
        //searchDialog.setUndecorated(true);
        searchDialog.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                stopSearch = true;
                while (threadARunning || threadBRunning) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignore) {
                    }
                }
            }
        });

        //searchDialog.getContentPane().setLayout(new FlowLayout());
        searchDialog.getContentPane().setLayout(new BorderLayout());

        searchLabel = new JTextField(25);
        //searchLabel.setMargin(new Insets(5, 5, 5, 5));
//        searchLabel.setMaximumSize(new Dimension(300, 50));
//        searchLabel.set

//        JPanel inputPanel = new JPanel(new BorderLayout());
//        inputPanel.add(new JLabel("Search String:"), BorderLayout.LINE_START);
//        inputPanel.add(searchLabel, BorderLayout.LINE_END);
        searchPanel = new JPanel(new FlowLayout());
        searchPanel.add(new JLabel("Search String:"));
        searchPanel.add(searchLabel);
        searchDialog.getContentPane().add(searchPanel, BorderLayout.PAGE_START);

        taskOutput = new JTextArea(5, 20);
        taskOutput.setMargin(new Insets(5, 5, 5, 5));
        taskOutput.setEditable(false);

        searchDialog.getContentPane().add(new JScrollPane(taskOutput), BorderLayout.CENTER);

        searchButton = new JButton("Search");
        showResultsButton = new JButton("Show Results");
        stopButton = new JButton("Stop");

        stopButton.setEnabled(false);
        showResultsButton.setEnabled(false);

        //searchDialog.getContentPane().add(searchButton);

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setString("");
        //searchDialog.getContentPane().add(progressBar);
        JPanel panel = new JPanel();
        searchPanel.add(searchButton);
        panel.add(progressBar);
        panel.add(stopButton);
        panel.add(showResultsButton);

        searchDialog.getContentPane().add(panel, BorderLayout.PAGE_END);

        searchDialog.pack();
        
        searchDialog.setLocationRelativeTo(targetComponent);

        showResultsButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                showResults();
            }
        });

        stopButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                stopSearch = true;
                stopButton.setEnabled(false);
            }
        });
        // set up search action
        searchButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                performSearch();
            }
        });
    }

    private void appendToTaskOutput(String lineOfText) {
        taskOutput.append(lineOfText + "\n");
        taskOutput.setCaretPosition(taskOutput.getText().length() - 1);
    }

    private void setUItoRunningState() {
        searchButton.setEnabled(false);
        stopButton.setEnabled(true);
        showResultsButton.setEnabled(false);
        searchLabel.setEnabled(false);
        searchLabel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        taskOutput.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        searchDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    private void setUItoStoppedState() {
        Toolkit.getDefaultToolkit().beep();
        searchLabel.setCursor(null);
        taskOutput.setCursor(null);
        searchDialog.setCursor(null); //turn off the wait cursor
        //appendToTaskOutput("Done!");
        progressBar.setIndeterminate(false);
        searchButton.setEnabled(true);
        stopButton.setEnabled(false);
        searchLabel.setEnabled(true);
        stopSearch = false;
    }
    /////////////////////////////////////
    // functions called by the threads //
    /////////////////////////////////////
    private void waitTillVisible() {
        // this is to prevent deadlocks between the thread starting before the dialog is showing which causes the JTextArea to appear without the frame
        while (!searchDialog.isVisible()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignore) {
            }
        }
    }
    // count childeren
    private int[] countChildern() {
        int childrenToLoad = 0, loadedChildren = 0;
        Enumeration selectedNodesEnum = selectedNodes.elements();
        while (selectedNodesEnum.hasMoreElements()) {
            Object currentElement = selectedNodesEnum.nextElement();
            if (currentElement instanceof ImdiHelper.ImdiTreeObject) {
                int[] tempChildCountArray = ((ImdiHelper.ImdiTreeObject) currentElement).getChildCount();
                childrenToLoad += tempChildCountArray[0];
                loadedChildren += tempChildCountArray[1];
            }
        }
        return (new int[]{childrenToLoad, loadedChildren});
    }
    // load all childeren
    private int loadSomeChildren(Object currentElement, int totalLoaded) {
        int currentLoaded = 0;
        appendToTaskOutput("loading sub corpus");
        boolean moreToLoad = true;
        while (moreToLoad && !stopSearch) {
            int[] tempChildCountArray = ((ImdiHelper.ImdiTreeObject) currentElement).getChildCount();
            appendToTaskOutput("total loaded: " + (totalLoaded + tempChildCountArray[1]) + " (" + currentElement.toString() + " loaded: " + tempChildCountArray[1] + " unknown: " + tempChildCountArray[0] + ")");
            progressBar.setString("" + (totalLoaded + tempChildCountArray[1]));
            moreToLoad = (tempChildCountArray[0] != 0);
            if (moreToLoad) {
                ((ImdiHelper.ImdiTreeObject) currentElement).loadNextLevelOfChildren(System.currentTimeMillis() + 100 * 5);
            }
            currentLoaded = tempChildCountArray[1];
//            progressBar.setNote(currentLoaded);
        }
        return currentLoaded;
    }
    /////////////////////////////////////////
    // end functions called by the threads //
    /////////////////////////////////////////

    ///////////////////////////////////////
    // functions that create the threads //
    ///////////////////////////////////////
    private void performCopy() {
        appendToTaskOutput("performCopy");
        setUItoRunningState();
        searchPanel.setVisible(false);
        showResultsButton.setVisible(false);
        threadARunning = true;
        new Thread() {

            public void run() {
                try {
                    waitTillVisible();
                    appendToTaskOutput("Copying");
                    progressBar.setIndeterminate(true);
                    int[] childCount = countChildern();
                    appendToTaskOutput("corpus to load: " + childCount[0] + "corpus loaded: " + childCount[1]);
                    Enumeration selectedNodesEnum = selectedNodes.elements();
                    int totalLoaded = 0;
                    while (selectedNodesEnum.hasMoreElements() && !stopSearch) {
                        Object currentElement = selectedNodesEnum.nextElement();
                        if (currentElement instanceof ImdiHelper.ImdiTreeObject) {
                            String newNodeLocation = ((ImdiHelper.ImdiTreeObject) currentElement).getSaveLocation(destinationDirectory);
                            if (newNodeLocation != null) {
//                                if (!new File(newNodeLocation).exists()) {// this would allow incomplete copies to be added
                                    totalLoaded += loadSomeChildren(currentElement, totalLoaded);
                                    if (!stopSearch) {
                                        // perform the copy
                                        appendToTaskOutput("Saving to: " + newNodeLocation);
                                        newNodeLocation = ((ImdiHelper.ImdiTreeObject) currentElement).saveBrachToLocal(destinationDirectory);
                                    }
    //                             } else {
    //                                 appendToTaskOutput("Using existing cached copy: " + newNodeLocation);
    //                             }
                            } // else appendToTaskOutput("Unable to process: " + currentElement);                            
                            if (newNodeLocation != null && !stopSearch) { // make sure we dont add an incomplete location
                                //appendToTaskOutput("would save location when done: " + newNodeLocation);
                                //guiHelper.addLocation("file://" + newNodeLocation);
                                // TODO: create an imdinode to contain the name and point to the location
                                if (!GuiHelper.treeHelper.addLocation("file://" + newNodeLocation)) {
                                    // alert the user when the node already exists and cannot be added again
                                    progressBar.setIndeterminate(false);
                                    JOptionPane.showMessageDialog(GuiHelper.linorgWindowManager.desktopPane, "The location already exists and cannot be added again", searchDialog.getTitle(), 0);
                                }
                            }
                        }
                    }
                    if (stopSearch) {
                        appendToTaskOutput("copy canceled");
                        System.out.println("copy canceled");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                threadARunning = false;
                if (!stopSearch) {
                    searchDialog.setVisible(false);
                } else {
                    setUItoStoppedState();
                }
            }
        }.start();
    }

    private void performSearch() {
        //searchLabel.setEditable(false);
        if (searchLabel.getText().length() == 0) {
            JOptionPane.showConfirmDialog(searchDialog, "A search term is required");
            searchButton.setEnabled(true);
            return;
        }
        setUItoRunningState();
        threadBRunning = true;
        new Thread() {

            public void run() {
                try {
                    waitTillVisible();
                    progressBar.setIndeterminate(true);
                    int[] childCount = countChildern();
                    appendToTaskOutput("corpus to load: " + childCount[0] + "corpus loaded: " + childCount[1]);
                    Enumeration selectedNodesEnum = selectedNodes.elements();
                    int totalLoaded = 0;
                    while (selectedNodesEnum.hasMoreElements() && !stopSearch) {
                        Object currentElement = selectedNodesEnum.nextElement();
                        if (currentElement instanceof ImdiHelper.ImdiTreeObject) {
                            totalLoaded += loadSomeChildren(currentElement, totalLoaded);
                            // perform the search        
                            appendToTaskOutput("searching");
                            ((ImdiHelper.ImdiTreeObject) currentElement).searchNodes(foundNodes, searchLabel.getText());
                            appendToTaskOutput("total found: " + foundNodes.size() + ")");
                        }
                    }

                    if (stopSearch) {
                        appendToTaskOutput("search canceled");
                    //System.out.println("search canceled");
                    }
                    setUItoStoppedState();

                    if (foundNodes.size() > 0) {
                        showResultsButton.setEnabled(true);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                threadBRunning = false;
            }
        }.start();
    }
    ///////////////////////////////////////////
    // end functions that create the threads //
    ///////////////////////////////////////////
    private void showResults() {
        if (foundNodes.size() > 0) {
            String frameTitle;
            if (selectedNodes.size() == 1) {
                frameTitle = "Found: " + searchLabel.getText() + " x " + foundNodes.size() + " in " + selectedNodes.get(0).toString();
            } else {
                frameTitle = "Found: " + searchLabel.getText() + " x " + foundNodes.size() + " in " + selectedNodes.size() + " nodes";
            }
            GuiHelper.linorgWindowManager.openFloatingTable(foundNodes.elements(), frameTitle);
        } else {
            JOptionPane.showMessageDialog(searchDialog, "\"" + searchLabel.getText() + "\" not found", "Search", 0);
        }
    }
}
