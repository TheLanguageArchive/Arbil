/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
public class SearchDialog {

    private LinorgWindowManager linorgWindowManager;
    private JDialog searchDialog;
    private JProgressBar progressBar;
    private JButton searchButton,  cancelButton,  showResultsButton;
    private JTextArea taskOutput;
    private JTextField searchLabel;
    private Vector selectedNodes;
    private Hashtable foundNodes = new Hashtable();
    private boolean stopSearch = false;

//    public Hashtable getFoundNodes() {
//        return foundNodes;
//    }
    public SearchDialog(LinorgWindowManager localLinorgWindowManager, Vector localSelectedNodes, String searchString) {
        linorgWindowManager = localLinorgWindowManager;
        selectedNodes = localSelectedNodes;
        searchDialog = new JDialog(JOptionPane.getFrameForComponent(linorgWindowManager.desktopPane), true);
        //searchDialog.setUndecorated(true);
        searchDialog.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                stopSearch = true;
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
        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.add(new JLabel("Search String:"));
        inputPanel.add(searchLabel);
        searchDialog.getContentPane().add(inputPanel, BorderLayout.PAGE_START);

        taskOutput = new JTextArea(5, 20);
        taskOutput.setMargin(new Insets(5, 5, 5, 5));
        taskOutput.setEditable(false);

        searchDialog.getContentPane().add(new JScrollPane(taskOutput), BorderLayout.CENTER);

        searchButton = new JButton("Search");
        cancelButton = new JButton("Cancel");
        showResultsButton = new JButton("Show Results");

        cancelButton.setEnabled(false);
        showResultsButton.setEnabled(false);

        //searchDialog.getContentPane().add(searchButton);

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        //searchDialog.getContentPane().add(progressBar);
        JPanel panel = new JPanel();
        panel.add(searchButton);
        panel.add(progressBar);
        panel.add(cancelButton);
        panel.add(showResultsButton);

        searchDialog.getContentPane().add(panel, BorderLayout.PAGE_END);

        searchDialog.pack();

//        super(new BorderLayout());
//
//        
//
//        add(panel, BorderLayout.PAGE_START);
//        add(new JScrollPane(taskOutput), BorderLayout.CENTER);

        showResultsButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                showResults();
            }
        });


        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                stopSearch = true;
                cancelButton.setEnabled(false);
            }
        });
        // set up search action
        searchButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                performSearch();
            }
        });
        if (searchString != null) {
            searchLabel.setText(searchString);
            performSearch();
        }
        searchDialog.setVisible(true);
    }
    
    private void appendToTaskOutput(String lineOfText){
        taskOutput.append(lineOfText + "\n");
        taskOutput.setCaretPosition(taskOutput.getText().length() - 1);
    }

    private void performSearch() {
        searchButton.setEnabled(false);
        cancelButton.setEnabled(true);
        showResultsButton.setEnabled(false);
        searchLabel.setEnabled(false);
        //searchLabel.setEditable(false);
        if (searchLabel.getText().length() == 0) {
            JOptionPane.showConfirmDialog(searchDialog, "A search term is required");
            searchButton.setEnabled(true);
            return;
        }

        searchLabel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        taskOutput.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        searchDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new Thread() {

            public void run() {
                try {
                    Enumeration selectedNodesEnum = selectedNodes.elements();
                    progressBar.setIndeterminate(true);
                    int totalLoaded = 0;
                    while (selectedNodesEnum.hasMoreElements() && !stopSearch) {
                        Object currentElement = selectedNodesEnum.nextElement();
                        int currentLoaded = 0;
                        if (currentElement instanceof ImdiHelper.ImdiTreeObject) {
                            boolean moreToLoad = true;
                            while (moreToLoad && !stopSearch) {
                                int[] tempChildCountArray = ((ImdiHelper.ImdiTreeObject) currentElement).getChildCount();
                                appendToTaskOutput("total loaded: " + (totalLoaded + tempChildCountArray[1]) + " (" + currentElement.toString() + " loaded: " + tempChildCountArray[1] + " unknown: " + tempChildCountArray[0] + ")");
                                moreToLoad = (tempChildCountArray[0] != 0);
                                if (moreToLoad) {
                                    ((ImdiHelper.ImdiTreeObject) currentElement).loadNextLevelOfChildren(System.currentTimeMillis() + 100 * 5);
                                }
                                currentLoaded = tempChildCountArray[1];
                            }
                        }
                        totalLoaded += currentLoaded;
                        // perform the search        
                        appendToTaskOutput("searching");
                        ((ImdiHelper.ImdiTreeObject) currentElement).searchNodes(foundNodes, searchLabel.getText());
                        appendToTaskOutput("total found: " + foundNodes.size() + ")");
                    }

                    if (stopSearch) {
                        appendToTaskOutput("search canceled");
                        System.out.println("search canceled");
                    }
//                            Random random = new Random();
//                            int progress = 0;
//                            //Initialize progress property.
//                            progressBar.setValue(0);
//                            while (progress < 100) {
//                                //Sleep for up to one second.
//                                try {
//                                    Thread.sleep(random.nextInt(1000));
//                                } catch (InterruptedException ignore) {
//                                }
//                                //Make random progress.
//                                progress += random.nextInt(10);
//                                progressBar.setValue(Math.min(progress, 100));
//                                appendToTaskOutput(String.format("Completed %d%% of task.", progress));
//                            }
                    Toolkit.getDefaultToolkit().beep();
                    searchLabel.setCursor(null);
                    taskOutput.setCursor(null);
                    searchDialog.setCursor(null); //turn off the wait cursor
                    appendToTaskOutput("Done!");
                    progressBar.setIndeterminate(false);
                    searchButton.setEnabled(true);
                    cancelButton.setEnabled(false);
                    searchLabel.setEnabled(true);
                    stopSearch = false;

                    if (foundNodes.size() > 0) {
                        showResultsButton.setEnabled(true);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.start();
    }

    private void showResults() {
        if (foundNodes.size() > 0) {
            String frameTitle;
            if (selectedNodes.size() == 1) {
                frameTitle = "Found: " + searchLabel.getText() + " x " + foundNodes.size() + " in " + selectedNodes.get(0).toString();
            } else {
                frameTitle = "Found: " + searchLabel.getText() + " x " + foundNodes.size() + " in " + selectedNodes.size() + " nodes";
            }
        linorgWindowManager.openFloatingTable(foundNodes.elements(), frameTitle);
        } else {
            JOptionPane.showMessageDialog(searchDialog, "\"" + searchLabel.getText() + "\" not found", "Search", 0);
        }
    }
}
