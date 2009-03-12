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
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import mpi.imdi.api.*;
import mpi.util.OurURL;
import org.w3c.dom.Document;

/**
 *
 * @author petwit
 */
public class ThreadedDialog {

    private JDialog searchDialog;
    private JProgressBar progressBar;
    private JButton stopButton;
    JPanel searchPanel;
    private JTextArea taskOutput;
    // variables used but the search thread
    // variables used by the copy thread
    // variables used by all threads
    private boolean stopSearch = false;
    private boolean threadARunning = false;
    private boolean threadBRunning = false;
    private Vector selectedNodes;

    public void copyToCache(Vector localSelectedNodes) {
        selectedNodes = localSelectedNodes;
        searchDialog.setTitle("Copy Branch");
        if (!selectedNodesContainImdi()) {
            JOptionPane.showMessageDialog(GuiHelper.linorgWindowManager.linorgFrame, "No relevant nodes are selected", searchDialog.getTitle(), 0);
            return;
        }
        //String mirrorNameString = JOptionPane.showInputDialog(destinationComp, "Enter a tile for the local mirror");

        //destinationDirectory = destinationDirectory + File.separator + mirrorNameString;
        //boolean branchDirCreated = (new File(destinationDirectory)).mkdir();
        // TODO: remove the branch directory and replace it with a named node in the locations settings or just a named imdinode
        if (GuiHelper.linorgSessionStorage.cacheDirExists()) {
            performCopy();
            searchDialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(GuiHelper.linorgWindowManager.linorgFrame, "Could not create the local directory", searchDialog.getTitle(), 0);
        }
    }

    private boolean selectedNodesContainImdi() {
        Enumeration selectedNodesEnum = selectedNodes.elements();
        while (selectedNodesEnum.hasMoreElements()) {
            if (selectedNodesEnum.nextElement() instanceof ImdiTreeObject) {
                return true;
            }
        }
        return false;
    }

    public ThreadedDialog(Component targetComponent) {
        searchDialog = new JDialog(JOptionPane.getFrameForComponent(GuiHelper.linorgWindowManager.linorgFrame), true);
        //searchDialog.setUndecorated(true);
        searchDialog.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                stopSearch = true;
                while (threadARunning || threadBRunning) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignore) {
                        GuiHelper.linorgBugCatcher.logError(ignore);
                    }
                }
            }
        });

        //searchDialog.getContentPane().setLayout(new FlowLayout());
        searchDialog.getContentPane().setLayout(new BorderLayout());
        searchPanel = new JPanel(new FlowLayout());
        searchPanel.add(new JLabel("Search String:"));
        searchDialog.getContentPane().add(searchPanel, BorderLayout.PAGE_START);
        taskOutput = new JTextArea(5, 20);
        taskOutput.setMargin(new Insets(5, 5, 5, 5));
        taskOutput.setEditable(false);
        searchDialog.getContentPane().add(new JScrollPane(taskOutput), BorderLayout.CENTER);
        stopButton = new JButton("Stop");
        stopButton.setEnabled(false);
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setString("");
        JPanel panel = new JPanel();
        panel.add(progressBar);
        panel.add(stopButton);
        searchDialog.getContentPane().add(panel, BorderLayout.PAGE_END);
        searchDialog.pack();
        searchDialog.setLocationRelativeTo(targetComponent);
        stopButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                stopSearch = true;
                stopButton.setEnabled(false);
            }
        });
    }

    private void appendToTaskOutput(String lineOfText) {
        taskOutput.append(lineOfText + "\n");
        taskOutput.setCaretPosition(taskOutput.getText().length() - 1);
    }

    private void setUItoRunningState() {
        stopButton.setEnabled(true);
        taskOutput.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        searchDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    private void setUItoStoppedState() {
        Toolkit.getDefaultToolkit().beep();
        taskOutput.setCursor(null);
        searchDialog.setCursor(null); //turn off the wait cursor
        //appendToTaskOutput("Done!");
        progressBar.setIndeterminate(false);
        stopButton.setEnabled(false);
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
                GuiHelper.linorgBugCatcher.logError(ignore);
            }
        }
    }
    // count childeren
    private int[] countChildern() {
        int childrenToLoad = 0, loadedChildren = 0;
        Enumeration selectedNodesEnum = selectedNodes.elements();
        while (selectedNodesEnum.hasMoreElements()) {
            Object currentElement = selectedNodesEnum.nextElement();
            if (currentElement instanceof ImdiTreeObject) {
                int[] tempChildCountArray = ((ImdiTreeObject) currentElement).getRecursiveChildCount();
                childrenToLoad += tempChildCountArray[0];
                loadedChildren += tempChildCountArray[1];
            }
        }
        return (new int[]{childrenToLoad, loadedChildren});
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
        threadARunning = true;
        new Thread() {

            public void run() {
                try {
//                    boolean saveToCache = true;
                    waitTillVisible();
                    appendToTaskOutput("Copying");
                    progressBar.setIndeterminate(true);
                    int[] childCount = countChildern();
                    appendToTaskOutput("corpus to load: " + childCount[0] + "corpus loaded: " + childCount[1]);
                    Enumeration selectedNodesEnum = selectedNodes.elements();
                    int totalLoaded = 0;
                    int totalErrors = 0;
                    while (selectedNodesEnum.hasMoreElements() && !stopSearch) {
                        Object currentElement = selectedNodesEnum.nextElement();
                        if (currentElement instanceof ImdiTreeObject) {
                            Vector getList = new Vector();
                            getList.add(((ImdiTreeObject) currentElement).getUrlString());
                            while (!stopSearch && getList.size() > 0) {
                                String currentTarget = (String) getList.remove(0);
                                appendToTaskOutput(currentTarget);
                                try {
                                    appendToTaskOutput("connecting...");
                                    OurURL inUrlLocal = new OurURL(currentTarget);
                                    Document nodDom = ImdiTreeObject.api.loadIMDIDocument(inUrlLocal, false);
                                    appendToTaskOutput("getting links...");
                                    IMDILink[] links = ImdiTreeObject.api.getIMDILinks(nodDom, inUrlLocal, WSNodeType.CORPUS);
                                    if (links != null) {
                                        for (int linkCount = 0; linkCount < links.length; linkCount++) {
                                            getList.add(links[linkCount].getRawURL().toString());
                                        }
                                    }
                                    appendToTaskOutput("saving to disk...");
                                    String destinationPath = GuiHelper.linorgSessionStorage.getSaveLocation(currentTarget);
                                    File tempFile = new File(destinationPath);
                                    if (tempFile.exists()) {
                                        appendToTaskOutput("this imdi is already in the cache");
                                    } else {
                                        // this function of the imdi.api will modify the imdi file as it saves it "(will be normalized and possibly de-domId-ed)"
                                        // this will make it dificult to determin if changes are from this function of by the user deliberatly making a chage
                                        ImdiTreeObject.api.writeDOM(nodDom, new File(destinationPath), false);
                                        // at this point the file should exist and not have been modified by the user
                                        // create hash index with server url but basedon the saved file
                                        // note that if the imdi.api has changed this file then it will not be detected
                                        // TODO: it will be best to change this to use the server api get mb5 sum when it is written
                                        // TODO: there needs to be some mechanism to check for changes on the server and update the local copy
                                        //getHash(tempFile, this.getUrl());
                                        appendToTaskOutput("saved in cache");
                                    }
                                } catch (Exception ex) {
                                    GuiHelper.linorgBugCatcher.logError(ex);
                                    totalErrors++;
                                    appendToTaskOutput("Exception: " + ex.getMessage());
                                }
                                totalLoaded++;
                                progressBar.setString(totalLoaded + "/" + (getList.size() + totalLoaded) + " (" + totalErrors + " errors)");
                            }
                            String newNodeLocation = GuiHelper.linorgSessionStorage.getSaveLocation(((ImdiTreeObject) currentElement).getUrlString());
//                            String newNodeLocation = ((ImdiTreeObject) currentElement).loadImdiDom(); // save the first node which will not be saved by loadSomeChildren
                            if (newNodeLocation != null && !stopSearch) { // make sure we dont add an incomplete location
                                //appendToTaskOutput("would save location when done: " + newNodeLocation);
                                //guiHelper.addLocation("file://" + newNodeLocation);
                                // TODO: create an imdinode to contain the name and point to the location
                                if (!GuiHelper.treeHelper.addLocation("file://" + newNodeLocation)) {
                                    // alert the user when the node already exists and cannot be added again
                                    progressBar.setIndeterminate(false);
                                    JOptionPane.showMessageDialog(GuiHelper.linorgWindowManager.linorgFrame, "The location:\n" + newNodeLocation + "\nalready exists and cannot be added again", searchDialog.getTitle(), 0);
                                } else {
                                    //GuiHelper.linorgWindowManager.localCorpusTreeModel.reload();
                                    GuiHelper.treeHelper.reloadLocalCorpusTree();
                                }
                                if (totalErrors != 0) {
                                    JOptionPane.showMessageDialog(GuiHelper.linorgWindowManager.linorgFrame, "There were " + totalErrors + " errors, some files may not be in the cache.", searchDialog.getTitle(), 0);
                                }
                            }
                        }
                    }
                    if (stopSearch) {
                        appendToTaskOutput("copy canceled");
                        System.out.println("copy canceled");
                    }
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
//                    ex.printStackTrace();
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
///////////////////////////////////////////
// end functions that create the threads //
///////////////////////////////////////////
}
