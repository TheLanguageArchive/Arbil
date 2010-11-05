package nl.mpi.arbil.importexport;

import nl.mpi.arbil.*;
import nl.mpi.arbil.data.ImdiTreeObject;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.tree.DefaultMutableTreeNode;
import nl.mpi.arbil.MetadataFile.MetadataUtils;
import nl.mpi.arbil.clarin.ArbilMetadataException;
import nl.mpi.arbil.data.ImdiLoader;

/**
 * Document   : ImportExportDialog
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
public class ImportExportDialog {

    private JDialog searchDialog;
    private JPanel searchPanel;
    private JPanel inputNodePanel;
    private JPanel outputNodePanel;
    private JCheckBox copyFilesCheckBox;
    private JCheckBox renameFileToNodeName;
    private JCheckBox renameFileToLamusFriendlyName;
    private JCheckBox detailsCheckBox;
    private JCheckBox overwriteCheckBox;
    private JCheckBox shibbolethCheckBox;
    private JPanel shibbolethPanel;
//    private JProgressBar resourceProgressBar;
    private JLabel resourceProgressLabel;
    private JProgressBar progressBar;
    private JLabel diskSpaceLabel;
    JPanel detailsPanel;
    JPanel bottomPanel;
    private JLabel progressFoundLabel;
    private JLabel progressProcessedLabel;
    private JLabel progressAlreadyInCacheLabel;
    private JLabel progressFailedLabel;
    private JLabel progressXmlErrorsLabel;
    private JLabel resourceCopyErrorsLabel;
    private JButton showInTableButton;
    String progressFoundLabelText = "Total Metadata Files Found: ";
    String progressProcessedLabelText = "Total Metadata Files Processed: ";
    String progressAlreadyInCacheLabelText = "Metadata Files already in Local Corpus: ";
    String progressFailedLabelText = "Metadata File Copy Errors: ";
    String progressXmlErrorsLabelText = "Metadata File Validation Errors: ";
    String resourceCopyErrorsLabelText = "Resource File Copy Errors: ";
    String diskFreeLabelText = "Total Disk Free: ";
    private JButton stopButton;
    private JButton startButton;
    private JTabbedPane detailsTabPane;
    private JTextArea taskOutput;
    private JTextArea xmlOutput;
    private JTextArea resourceCopyOutput;
    // variables used but the search thread
    // variables used by the copy thread
    // variables used by all threads
    private boolean stopSearch = false;
    private Vector selectedNodes;
    ImdiTreeObject destinationNode = null;
    File exportDestinationDirectory = null;
    DownloadAbortFlag downloadAbortFlag = new DownloadAbortFlag();
    ShibbolethNegotiator shibbolethNegotiator = null;
    Vector<URI> validationErrors = new Vector<URI>();
    Vector<URI> metaDataCopyErrors = new Vector<URI>();
    Vector<URI> fileCopyErrors = new Vector<URI>();

    private void setNodesPanel(ImdiTreeObject selectedNode, JPanel nodePanel) {
        JLabel currentLabel = new JLabel(selectedNode.toString(), selectedNode.getIcon(), JLabel.CENTER);
        nodePanel.add(currentLabel);
    }

    private void setNodesPanel(Vector selectedNodes, JPanel nodePanel) {
//            setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.PAGE_AXIS));
//        nodePanel.setLayout(new java.awt.GridLayout());
//        add(nodePanel);
        for (Enumeration<ImdiTreeObject> selectedNodesEnum = selectedNodes.elements(); selectedNodesEnum.hasMoreElements();) {
            ImdiTreeObject currentNode = selectedNodesEnum.nextElement();
            JLabel currentLabel = new JLabel(currentNode.toString(), currentNode.getIcon(), JLabel.CENTER);
            nodePanel.add(currentLabel);
        }
    }

    private void setLocalCacheToNodesPanel(JPanel nodePanel) {
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) TreeHelper.getSingleInstance().localCorpusTreeModel.getRoot();
        JLabel rootNodeLabel = (JLabel) rootNode.getUserObject();
        JLabel currentLabel = new JLabel(rootNodeLabel.getText(), rootNodeLabel.getIcon(), JLabel.CENTER);
        nodePanel.add(currentLabel);
    }

    private void setLocalFileToNodesPanel(JPanel nodePanel, File destinationDirectory) {
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) TreeHelper.getSingleInstance().localDirectoryTreeModel.getRoot();
        JLabel rootNodeLabel = (JLabel) rootNode.getUserObject();
        JLabel currentLabel = new JLabel(destinationDirectory.getPath(), rootNodeLabel.getIcon(), JLabel.CENTER);
        nodePanel.add(currentLabel);
    }

    public void importImdiBranch() {
        File[] selectedFiles = LinorgWindowManager.getSingleInstance().showFileSelectBox("Import", false, true, true);
        if (selectedFiles != null) {
            Vector importNodeVector = new Vector();
            for (File currentFile : selectedFiles) {
                ImdiTreeObject imdiToImport = ImdiLoader.getSingleInstance().getImdiObject(null, currentFile.toURI());
                importNodeVector.add(imdiToImport);
            }
            copyToCache(importNodeVector);
        }
    }

    public void selectExportDirectoryAndExport(ImdiTreeObject[] localCorpusSelectedNodes) {
        // make sure the chosen directory is empty
        // export the tree, maybe adjusting resource links so that resource files do not need to be copied
        searchDialog.setTitle("Export Branch");
        File destinationDirectory = LinorgWindowManager.getSingleInstance().showEmptyExportDirectoryDialogue(searchDialog.getTitle());
        if (destinationDirectory != null) {
            exportFromCache(new Vector(Arrays.asList(localCorpusSelectedNodes)), destinationDirectory);
        }
    }

    private void exportFromCache(Vector localSelectedNodes, File destinationDirectory) {
        selectedNodes = localSelectedNodes;
//        searchDialog.setTitle("Export Branch");
        if (!selectedNodesContainImdi()) {
            JOptionPane.showMessageDialog(LinorgWindowManager.getSingleInstance().linorgFrame, "No relevant nodes are selected", searchDialog.getTitle(), JOptionPane.PLAIN_MESSAGE);
            return;
        }
        setNodesPanel(selectedNodes, inputNodePanel);
        setLocalFileToNodesPanel(outputNodePanel, destinationDirectory);
        //String mirrorNameString = JOptionPane.showInputDialog(destinationComp, "Enter a tile for the local mirror");

        exportDestinationDirectory = destinationDirectory;
        searchDialog.setVisible(true);
    }

    public void copyToCache(ImdiTreeObject[] localSelectedNodes) {
        copyToCache(new Vector(Arrays.asList(localSelectedNodes)));
    }

    // sets the destination branch for the imported nodes
    public void setDestinationNode(ImdiTreeObject localDestinationNode) {
        destinationNode = localDestinationNode;
        setNodesPanel(destinationNode, outputNodePanel);
    }

    public void copyToCache(Vector localSelectedNodes) {
        selectedNodes = localSelectedNodes;
        searchDialog.setTitle("Import Branch");
        if (!selectedNodesContainImdi()) {
            JOptionPane.showMessageDialog(LinorgWindowManager.getSingleInstance().linorgFrame, "No relevant nodes are selected", searchDialog.getTitle(), JOptionPane.PLAIN_MESSAGE);
            return;
        }
        setNodesPanel(selectedNodes, inputNodePanel);
        if (destinationNode == null) {
            setLocalCacheToNodesPanel(outputNodePanel);
        }
        searchDialog.setVisible(true);
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

    private void showDetails(boolean showFlag) {
        // showFlag is false the first time this is called when the dialog is initialised so we need to make sure that pack gets called in this case
        // otherwise try to prevent chenging the window size when not required
        if (!showFlag || detailsTabPane.isVisible() != showFlag) {
            detailsTabPane.setVisible(showFlag);
            bottomPanel.setVisible(showFlag);
            copyFilesCheckBox.setVisible(showFlag);
            renameFileToNodeName.setVisible(showFlag && exportDestinationDirectory != null);
            renameFileToLamusFriendlyName.setVisible(showFlag && exportDestinationDirectory != null);
            overwriteCheckBox.setVisible(showFlag && exportDestinationDirectory == null);
            //shibbolethCheckBox.setVisible(showFlag && exportDestinationDirectory == null);
            shibbolethPanel.setVisible(showFlag/* && shibbolethCheckBox.isSelected()*/);
            outputNodePanel.setVisible(false);
            inputNodePanel.setVisible(false);
//            searchDialog.pack();
            outputNodePanel.setVisible(true);
            inputNodePanel.setVisible(true);
        }
    }

    // the targetComponent is used to place the import dialog
    public ImportExportDialog(Component targetComponent) throws Exception {
        LinorgWindowManager.getSingleInstance().offerUserToSaveChanges();
        searchDialog = new JDialog(JOptionPane.getFrameForComponent(LinorgWindowManager.getSingleInstance().linorgFrame), true);
        searchDialog.addWindowStateListener(new WindowAdapter() {

            @Override
            public void windowStateChanged(WindowEvent e) {
                if ((e.getNewState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
                    detailsCheckBox.setSelected(true);
                    showDetails(true);
                } else {
                    searchDialog.pack();
                }
            }
        });
        //searchDialog.setUndecorated(true);
        searchDialog.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                stopSearch = true;
                downloadAbortFlag.abortDownload = true;
//                while (threadARunning || threadBRunning) {
//                    try {
//                        Thread.sleep(100);
//                    } catch (InterruptedException ignore) {
//                        GuiHelper.linorgBugCatcher.logError(ignore);
//                    }
//                }
//                GuiHelper.linorgWindowManager.linorgFrame.requestFocusInWindow();
            }
        });
        searchPanel = new JPanel();
        searchPanel.setLayout(new BorderLayout());
        //searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.PAGE_AXIS));
        searchDialog.getContentPane().setLayout(new BorderLayout());
        searchDialog.add(searchPanel, BorderLayout.CENTER);

        JPanel inOutNodePanel = new JPanel();
        inOutNodePanel.setLayout(new BoxLayout(inOutNodePanel, BoxLayout.PAGE_AXIS));

        JPanel inputNodeLabelPanel = new JPanel();
        inputNodeLabelPanel.setLayout(new BorderLayout());
        inputNodePanel = new JPanel();
        inputNodePanel.setLayout(new java.awt.GridLayout());
        inputNodeLabelPanel.add(new JLabel("From: "), BorderLayout.LINE_START);
        inputNodeLabelPanel.add(inputNodePanel, BorderLayout.CENTER);
        inOutNodePanel.add(inputNodeLabelPanel);

        JPanel outputNodeLabelPanel = new JPanel();
        outputNodeLabelPanel.setLayout(new BorderLayout());
        outputNodePanel = new JPanel();
        outputNodePanel.setLayout(new java.awt.GridLayout());
        outputNodeLabelPanel.add(new JLabel("To: "), BorderLayout.LINE_START);
        outputNodeLabelPanel.add(outputNodePanel, BorderLayout.CENTER);
        inOutNodePanel.add(outputNodeLabelPanel);

        detailsCheckBox = new JCheckBox("Show Details", false);
        detailsCheckBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    showDetails(detailsCheckBox.isSelected());
                    searchDialog.pack();
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        JPanel detailsCheckBoxPanel = new JPanel();
        detailsCheckBoxPanel.setLayout(new java.awt.GridLayout());
        detailsCheckBoxPanel.add(detailsCheckBox);
        inOutNodePanel.add(detailsCheckBoxPanel);

        searchPanel.add(inOutNodePanel, BorderLayout.NORTH);

        detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.PAGE_AXIS));

        copyFilesCheckBox = new JCheckBox("Copy Resource Files (if available)", false);
        renameFileToNodeName = new JCheckBox("Rename Metadata Files (to match local corpus tree names)", true);
        renameFileToLamusFriendlyName = new JCheckBox("Limit Characters in File Names (LAMUS friendly format)", true);
        overwriteCheckBox = new JCheckBox("Overwrite Local Changes", false);
        shibbolethCheckBox = new JCheckBox("Shibboleth authentication via the SURFnet method", false);
        shibbolethPanel = new JPanel();
        shibbolethCheckBox.setVisible(false);
        shibbolethPanel.setVisible(false);

        shibbolethCheckBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (shibbolethCheckBox.isSelected()) {
                    if (shibbolethNegotiator == null) {
                        shibbolethNegotiator = new ShibbolethNegotiator();
                    }
                    shibbolethPanel.add(shibbolethNegotiator.getControlls());
                } else {
                    shibbolethPanel.removeAll();
                    shibbolethNegotiator = null;
                }
                searchDialog.pack();
            }
        });
        copyFilesCheckBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                shibbolethCheckBox.setVisible(copyFilesCheckBox.isSelected());
                shibbolethPanel.setVisible(copyFilesCheckBox.isSelected());
                searchDialog.pack();
            }
        });

//        JPanel copyFilesCheckBoxPanel = new JPanel();
//        copyFilesCheckBoxPanel.setLayout(new BoxLayout(copyFilesCheckBoxPanel, BoxLayout.X_AXIS));
//        copyFilesCheckBoxPanel.add(copyFilesCheckBox);
//        copyFilesCheckBoxPanel.add(new JPanel());
//        detailsPanel.add(copyFilesCheckBoxPanel, BorderLayout.NORTH);
        detailsPanel.add(renameFileToNodeName, BorderLayout.NORTH);
        detailsPanel.add(renameFileToLamusFriendlyName, BorderLayout.NORTH);
        detailsPanel.add(overwriteCheckBox, BorderLayout.NORTH);
        detailsPanel.add(copyFilesCheckBox, BorderLayout.NORTH);
        detailsPanel.add(shibbolethCheckBox, BorderLayout.NORTH);
        detailsPanel.add(shibbolethPanel, BorderLayout.NORTH);

//        detailsPanel.add(new JPanel());

        detailsTabPane = new JTabbedPane();

        taskOutput = new JTextArea(5, 20);
        taskOutput.setMargin(new Insets(5, 5, 5, 5));
        taskOutput.setEditable(false);
        detailsTabPane.add("Process Details", new JScrollPane(taskOutput));

        xmlOutput = new JTextArea(5, 20);
        xmlOutput.setMargin(new Insets(5, 5, 5, 5));
        xmlOutput.setEditable(false);
        detailsTabPane.add("Validation Errors", new JScrollPane(xmlOutput));

        resourceCopyOutput = new JTextArea(5, 20);
        resourceCopyOutput.setMargin(new Insets(5, 5, 5, 5));
        resourceCopyOutput.setEditable(false);
        detailsTabPane.add("Resource Copy Errors", new JScrollPane(resourceCopyOutput));

        detailsPanel.add(detailsTabPane, BorderLayout.CENTER);

        bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.PAGE_AXIS));

        progressFoundLabel = new JLabel(progressFoundLabelText);
        progressProcessedLabel = new JLabel(progressProcessedLabelText);
        progressAlreadyInCacheLabel = new JLabel(progressAlreadyInCacheLabelText);
        progressFailedLabel = new JLabel(progressFailedLabelText);
        progressXmlErrorsLabel = new JLabel(progressXmlErrorsLabelText);
        resourceCopyErrorsLabel = new JLabel(resourceCopyErrorsLabelText);
        showInTableButton = new JButton("Show errors in table");
        diskSpaceLabel = new JLabel(diskFreeLabelText);

        progressAlreadyInCacheLabel.setForeground(Color.darkGray);
        progressFailedLabel.setForeground(Color.red);
        progressXmlErrorsLabel.setForeground(Color.red);
        resourceCopyErrorsLabel.setForeground(Color.red);

        bottomPanel.add(progressFoundLabel);
        bottomPanel.add(progressProcessedLabel);
        bottomPanel.add(progressAlreadyInCacheLabel);
        bottomPanel.add(progressFailedLabel);
        bottomPanel.add(progressXmlErrorsLabel);
        bottomPanel.add(resourceCopyErrorsLabel);
        bottomPanel.add(showInTableButton);
        bottomPanel.add(diskSpaceLabel);

        resourceProgressLabel = new JLabel(" ");
        bottomPanel.add(resourceProgressLabel);

//        bottomPanel = new JPanel();
//        bottomPanel.setLayout(new java.awt.GridLayout());
//        bottomPanel.add(bottomInnerPanel);
//        detailsPanel.add(bottomPanel, BorderLayout.SOUTH);
        detailsPanel.add(bottomPanel, BorderLayout.SOUTH);

        searchPanel.add(detailsPanel, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout());

        stopButton = new JButton("Stop");
        startButton = new JButton("Start");
        stopButton.setEnabled(false);
        buttonsPanel.add(stopButton);

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setString("");
        buttonsPanel.add(progressBar);

//        resourceProgressBar = new JProgressBar(0, 100);
//        resourceProgressBar.setValue(0);
//        resourceProgressBar.setStringPainted(true);
//        resourceProgressBar.setString("");
//        buttonsPanel.add(resourceProgressBar);

        buttonsPanel.add(startButton);

        searchPanel.add(buttonsPanel, BorderLayout.SOUTH);

        searchDialog.setLocationRelativeTo(targetComponent);

        showInTableButton.setEnabled(false);
        showInTableButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    if (metaDataCopyErrors.size() > 0) {
                        LinorgWindowManager.getSingleInstance().openFloatingTableOnce(metaDataCopyErrors.toArray(new URI[]{}), progressFailedLabelText);
                    }
                    if (validationErrors.size() > 0) {
                        LinorgWindowManager.getSingleInstance().openAllChildNodesInFloatingTableOnce(validationErrors.toArray(new URI[]{}), progressXmlErrorsLabelText);
                    }
                    if (fileCopyErrors.size() > 0) {
                        ImdiTableModel resourceFileErrorsTable = LinorgWindowManager.getSingleInstance().openFloatingTableOnce(fileCopyErrors.toArray(new URI[]{}), resourceCopyErrorsLabelText);
                        //resourceFileErrorsTable.getFieldView().
                        resourceFileErrorsTable.addChildTypeToDisplay("MediaFiles");
                        resourceFileErrorsTable.addChildTypeToDisplay("WrittenResources");
                    }
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        startButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    performCopy();
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        stopButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    stopSearch = true;
                    downloadAbortFlag.abortDownload = true;
                    stopButton.setEnabled(false);
                    startButton.setEnabled(false);
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        taskOutput.append("The details of the import / export process will be displayed here.\n");
        xmlOutput.append("When the metadata files are imported or exported they will be validated (for XML schema conformance) and any errors will be reported here.\n");
        resourceCopyOutput.append("If copying of resource files is selected, any file copy errors will be reported here.\n");

        //searchDialog.pack();
        showDetails(detailsCheckBox.isSelected()); // showDetails no longer calls pack()
        searchDialog.pack();
    }

    private void appendToTaskOutput(String lineOfText) {
        taskOutput.append(lineOfText + "\n");
        taskOutput.setCaretPosition(taskOutput.getText().length());
    }

    private void setUItoRunningState() {
        stopButton.setEnabled(true);
        startButton.setEnabled(false);
        showInTableButton.setEnabled(false);
        taskOutput.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        searchDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    private void setUItoStoppedState() {
        Toolkit.getDefaultToolkit().beep();
        taskOutput.setCursor(null);
        searchDialog.setCursor(null); //turn off the wait cursor
        //appendToTaskOutput("Done!");
        progressBar.setIndeterminate(false);
//        resourceProgressBar.setIndeterminate(false);
        resourceProgressLabel.setText(" ");
//        progressLabel.setText("");
        stopButton.setEnabled(false);
        startButton.setEnabled(selectedNodes.size() > 0);
        showInTableButton.setEnabled(validationErrors.size() > 0 || metaDataCopyErrors.size() > 0 || fileCopyErrors.size() > 0);
        // TODO: add a close button?
        stopSearch = false;
        downloadAbortFlag.abortDownload = false;
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

    private void removeEmptyDirectoryPaths(File currentDirectory, File[] destinationFile) {
        File[] childDirectories = currentDirectory.listFiles();
        if (childDirectories != null && childDirectories.length == 1) {
            removeEmptyDirectoryPaths(childDirectories[0], destinationFile);
            if (childDirectories[0].isDirectory()) {
                childDirectories[0].delete();
            }
        } else {
            try {
                File tempFile = destinationFile[0] = File.createTempFile("tmp-" + currentDirectory.getName(), "", exportDestinationDirectory);
                destinationFile[1] = new File(exportDestinationDirectory, currentDirectory.getName());
                tempFile.delete();
                if (!currentDirectory.renameTo(tempFile)) {
//                    appendToTaskOutput("failed to tidy directory stucture");
                } else {
//                    appendToTaskOutput("tidy directory stucture done");
                }
            } catch (Exception ex) {
                GuiHelper.linorgBugCatcher.logError(ex);
            }
        }
    }
//        private String getShallowestDirectory() {
//        int childrenToLoad = 0, loadedChildren = 0;
//        Enumeration selectedNodesEnum = selectedNodes.elements();
//        while (selectedNodesEnum.hasMoreElements()) {
//            Object currentElement = selectedNodesEnum.nextElement();
//            if (currentElement instanceof ImdiTreeObject) {
//                int[] tempChildCountArray = ((ImdiTreeObject) currentElement).getRecursiveChildCount();
//                childrenToLoad += tempChildCountArray[0];
//                loadedChildren += tempChildCountArray[1];
//            }
//        }
//        return (new int[]{childrenToLoad, loadedChildren});
//    }
    /////////////////////////////////////////
    // end functions called by the threads //
    /////////////////////////////////////////

    ///////////////////////////////////////
    // functions that create the threads //
    ///////////////////////////////////////
    private void performCopy() {
//        appendToTaskOutput("performCopy");
        setUItoRunningState();
//        // Set the connection timeout
//        System.getProperties().setProperty("sun.net.client.defaultConnectTimeout", "2000");
//        System.getProperties().setProperty("sun.net.client.defaultReadTimeout", "2000");
//        searchPanel.setVisible(false);
        new Thread("performCopy") {

            public void run() {
//                setPriority(Thread.NORM_PRIORITY - 1);
                boolean testFreeSpace = true;
                String javaVersionString = System.getProperty("java.version");
                if (javaVersionString.startsWith("1.4.") || javaVersionString.startsWith("1.5.")) {
                    testFreeSpace = false;
                }
                int freeGbWarningPoint = 3;
                int xsdErrors = 0;
                int totalLoaded = 0;
                int totalErrors = 0;
                int totalExisting = 0;
                int resourceCopyErrors = 0;
                String finalMessageString = "";
                File directoryForSizeTest;
                if (exportDestinationDirectory != null) {
                    directoryForSizeTest = exportDestinationDirectory;
                } else {
                    directoryForSizeTest = LinorgSessionStorage.getSingleInstance().getCacheDirectory();
                }
                if (copyFilesCheckBox.isSelected()) {
                    resourceCopyOutput.append("'Copy Resource Files' is selected: Resource files will be downloaded where appropriate permission are granted." + "\n");
                } else {
                    resourceCopyOutput.append("'Copy Resource Files' is not selected: No resource files will be downloaded, however they will be still accessible via the web server." + "\n");
                }

                try {
//                    boolean saveToCache = true;
//                    File tempFileForValidator = File.createTempFile("linorg", ".imdi");
//                    tempFileForValidator.deleteOnExit();
                    XsdChecker xsdChecker = new XsdChecker();
                    waitTillVisible();
//                    appendToTaskOutput("copying: ");
                    progressBar.setIndeterminate(true);
//                    resourceProgressBar.setIndeterminate(true);
//                    int[] childCount = countChildern();
//                    appendToTaskOutput("corpus to load: " + childCount[0] + "corpus loaded: " + childCount[1]);
                    class RetrievableFile {

                        public RetrievableFile(URI sourceURILocal, File destinationDirectoryLocal) {
                            sourceURI = sourceURILocal;
                            destinationDirectory = destinationDirectoryLocal;
                        }

                        private String makeFileNameLamusFriendly(String fileNameString) {
                            // as requested by Eric: x = x.replaceAll("[^A-Za-z0-9._-]", "_"); // keep only "nice" chars
                            return fileNameString.replaceAll("[^A-Za-z0-9-]", "_"); // this will only be passed the file name without suffix so "." should not be allowed, also there is no point replacing "_" with "_".
                        }

                        public void calculateUriFileName() {
                            if (destinationDirectory != null) {
                                destinationFile = LinorgSessionStorage.getSingleInstance().getExportPath(sourceURI.toString(), destinationDirectory.getPath());
                            } else {
                                destinationFile = LinorgSessionStorage.getSingleInstance().getSaveLocation(sourceURI.toString());
                            }
                            childDestinationDirectory = destinationDirectory;
                        }

                        public void calculateTreeFileName(boolean lamusFriendly) {
                            fileSuffix = sourceURI.toString().substring(sourceURI.toString().lastIndexOf("."));
                            ImdiTreeObject currentNode = ImdiLoader.getSingleInstance().getImdiObject(null, sourceURI); // rather than use ImdiLoader this metadata could be loaded directly and then garbaged to save memory
                            currentNode.waitTillLoaded();
                            String fileNameString;
                            if (currentNode.isMetaDataNode()) {
                                fileNameString = currentNode.toString();
                            } else {
                                String urlString = sourceURI.toString();
                                try {
                                    urlString = URLDecoder.decode(urlString, "UTF-8");
                                } catch (Exception ex) {
                                    GuiHelper.linorgBugCatcher.logError(urlString, ex);
                                    appendToTaskOutput("unable to decode the file name for: " + urlString);
                                    System.out.println("unable to decode the file name for: " + urlString);
                                }
                                fileNameString = urlString.substring(urlString.lastIndexOf("/") + 1, urlString.lastIndexOf("."));
                            }
                            fileNameString = fileNameString.replace("\\", "_");
                            fileNameString = fileNameString.replace("/", "_");
                            // other potential problem chars can be removed with the lamus friendly option, if it was done always then all non asc11 languages will be destroyed and non english like languages would be unreadable
                            if (lamusFriendly) {
                                fileNameString = makeFileNameLamusFriendly(fileNameString);
                            }
                            if (fileNameString.length() < 1) {
                                fileNameString = "unnamed";
                            }
                            destinationFile = new File(destinationDirectory, fileNameString + fileSuffix);
                            childDestinationDirectory = new File(destinationDirectory, fileNameString);
                            int fileCounter = 1;
                            while (destinationFile.exists()) {
                                if (lamusFriendly) {
                                    destinationFile = new File(destinationDirectory, fileNameString + "_" + fileCounter + fileSuffix);
                                    childDestinationDirectory = new File(destinationDirectory, fileNameString + "_" + fileCounter);
                                } else {
                                    destinationFile = new File(destinationDirectory, fileNameString + "(" + fileCounter + ")" + fileSuffix);
                                    childDestinationDirectory = new File(destinationDirectory, fileNameString + "(" + fileCounter + ")");
                                }
                                fileCounter++;
                            }
                        }
                        URI sourceURI;
                        File destinationDirectory; // if null then getSaveLocation in LinorgSessionStorage will be used
                        File childDestinationDirectory;
                        File destinationFile;
                        String fileSuffix;
                    }
                    Enumeration selectedNodesEnum = selectedNodes.elements();
                    ArrayList<ImdiTreeObject> finishedTopNodes = new ArrayList<ImdiTreeObject>();
                    Hashtable<URI, RetrievableFile> seenFiles = new Hashtable<URI, RetrievableFile>();
                    ArrayList<URI> getList = new ArrayList<URI>(); // TODO: make this global so files do not get redone
                    ArrayList<URI> doneList = new ArrayList<URI>();
                    while (selectedNodesEnum.hasMoreElements() && !stopSearch) {
                        // todo: test for export keeping file names
                        // todo: test for export when there are nodes imported from the local file system
                        // todo: test all the options for a unusal sotrage directory
                        // todo: and test on windows
                        Object currentElement = selectedNodesEnum.nextElement();
                        if (currentElement instanceof ImdiTreeObject) {
                            URI currentGettableUri = ((ImdiTreeObject) currentElement).getParentDomNode().getURI();
                            getList.add(currentGettableUri);
                            if (!seenFiles.containsKey(currentGettableUri)) {
                                seenFiles.put(currentGettableUri, new RetrievableFile(((ImdiTreeObject) currentElement).getParentDomNode().getURI(), exportDestinationDirectory));
                            }
                            while (!stopSearch && getList.size() > 0) {
                                RetrievableFile currentRetrievableFile = seenFiles.get(getList.remove(0));
//                                appendToTaskOutput(currentTarget);
                                try {
                                    if (!doneList.contains(currentRetrievableFile.sourceURI)) {
//                                        File destinationFile;
                                        //                                    appendToTaskOutput("connecting...");
                                        //OurURL inUrlLocal = new OurURL(currentTarget.toURL());
                                        //                                    String destinationPath;
//                                        File destinationFile;// = new File(destinationPath);
                                        String journalActionString;
                                        if (exportDestinationDirectory == null) {
                                            currentRetrievableFile.calculateUriFileName();
                                            journalActionString = "import";
                                        } else {
                                            if (renameFileToNodeName.isSelected() && exportDestinationDirectory != null) {
                                                currentRetrievableFile.calculateTreeFileName(renameFileToLamusFriendlyName.isSelected());
                                            } else {
                                                currentRetrievableFile.calculateUriFileName();
                                            }
                                            journalActionString = "export";
                                        }
                                        MetadataUtils currentMetdataUtil = ImdiTreeObject.getMetadataUtils(currentRetrievableFile.sourceURI.toString());
                                        ArrayList<URI[]> uncopiedLinks = new ArrayList<URI[]>();
                                        URI[] linksUriArray = currentMetdataUtil.getCorpusLinks(currentRetrievableFile.sourceURI);

//                                    appendToTaskOutput("destination path: " + destinationFile.getAbsolutePath());
//                                    appendToTaskOutput("getting links...");

//                                        links = ImdiTreeObject.api.getIMDILinks(nodDom, inUrlLocal, WSNodeType.CORPUS);
                                        if (linksUriArray != null) {
                                            for (int linkCount = 0; linkCount < linksUriArray.length && !stopSearch; linkCount++) {
                                                System.out.println("Link: " + linksUriArray[linkCount].toString());
                                                String currentLink = linksUriArray[linkCount].toString();
//                                                URI gettableLinkUri = ImdiTreeObject.conformStringToUrl(currentLink);
                                                URI gettableLinkUri = linksUriArray[linkCount].normalize();
                                                if (!seenFiles.containsKey(gettableLinkUri)) {
                                                    seenFiles.put(gettableLinkUri, new RetrievableFile(gettableLinkUri, currentRetrievableFile.childDestinationDirectory));
                                                }
                                                RetrievableFile retrievableLink = seenFiles.get(gettableLinkUri);
                                                if (ImdiTreeObject.isPathMetadata(currentLink)) {
                                                    getList.add(gettableLinkUri);
                                                    if (renameFileToNodeName.isSelected() && exportDestinationDirectory != null) {
                                                        retrievableLink.calculateTreeFileName(renameFileToLamusFriendlyName.isSelected());
                                                    } else {
                                                        retrievableLink.calculateUriFileName();
                                                    }
                                                    uncopiedLinks.add(new URI[]{linksUriArray[linkCount], retrievableLink.destinationFile.toURI()});
                                                } else /*if (links[linkCount].getType() != null) this null also exists when a resource is local *//* filter out non resources */ {
                                                    if (!copyFilesCheckBox.isSelected()) {
//                                                        retrievableLink.setFileNotCopied();
                                                        uncopiedLinks.add(new URI[]{linksUriArray[linkCount], linksUriArray[linkCount]});
                                                    } else {
//                                                    appendToTaskOutput("getting resource file: " + links[linkCount].getType());
//                                                    resourceCopyOutput.append("Type: " + links[linkCount].getType() + "\n");
//                                                    resourceCopyOutput.append(currentLink + "\n");
                                                        File downloadFileLocation;
                                                        // todo: warning! this appears to beable to create a directory called "file:"
                                                        if (exportDestinationDirectory == null) {
                                                            downloadFileLocation = LinorgSessionStorage.getSingleInstance().updateCache(currentLink, shibbolethNegotiator, false, downloadAbortFlag, resourceProgressLabel);
                                                        } else {
                                                            if (renameFileToNodeName.isSelected() && exportDestinationDirectory != null) {
                                                                retrievableLink.calculateTreeFileName(renameFileToLamusFriendlyName.isSelected());
                                                            } else {
                                                                retrievableLink.calculateUriFileName();
                                                            }
                                                            if (!retrievableLink.destinationFile.getParentFile().exists()) {
                                                                retrievableLink.destinationFile.getParentFile().mkdirs();
                                                            }
                                                            downloadFileLocation = retrievableLink.destinationFile;
//                                                        System.out.println("downloadLocation: " + downloadLocation);
//                                                            resourceProgressBar.setIndeterminate(false);
                                                            resourceProgressLabel.setText(" ");
                                                            LinorgSessionStorage.getSingleInstance().saveRemoteResource(new URL(currentLink), downloadFileLocation, shibbolethNegotiator, true, downloadAbortFlag, resourceProgressLabel);
//                                                            resourceProgressBar.setIndeterminate(true);
                                                            resourceProgressLabel.setText(" ");
                                                        }
                                                        //resourceCopyOutput.append(downloadFileLocation + "\n");
                                                        if (downloadFileLocation.exists()) {
                                                            appendToTaskOutput("Downloaded resource: " + downloadFileLocation.getAbsolutePath());
                                                            //resourceCopyOutput.append("Copied " + downloadFileLocation.length() + "b\n");
                                                            uncopiedLinks.add(new URI[]{linksUriArray[linkCount], downloadFileLocation.toURI()});
                                                        } else {
                                                            resourceCopyOutput.append("Download failed: " + currentLink + " \n");
                                                            //resourceCopyOutput.append("path: " + destinationFile.getAbsolutePath());
                                                            //resourceCopyOutput.append("Failed" + "\n");
                                                            fileCopyErrors.add(currentRetrievableFile.sourceURI);
                                                            uncopiedLinks.add(new URI[]{linksUriArray[linkCount], linksUriArray[linkCount]});
                                                            resourceCopyErrors++;
                                                        }
                                                        resourceCopyOutput.setCaretPosition(resourceCopyOutput.getText().length() - 1);
                                                    }
//                                                if (!resourceFileCopied) {
//                                                    currentMetdataUtil.updateSingleLink(currentTarget, curr)
////                                                    ImdiTreeObject.api.changeIMDILink(nodDom, destinationUrl, links[linkCount]);
//                                                }
                                                }
//                                            System.out.println("getIMDILinks.getRawURL: " + links[linkCount].getRawURL().toString());
//                                            SystecurrentTree.m.out.println("getIMDILinks.getURL: " + links[linkCount].getURL().toString());
                                            }
                                        }
                                        boolean replacingExitingFile = currentRetrievableFile.destinationFile.exists() && overwriteCheckBox.isSelected();
                                        if (currentRetrievableFile.destinationFile.exists()) {
                                            totalExisting++;
                                        }
                                        if (currentRetrievableFile.destinationFile.exists() && !overwriteCheckBox.isSelected()) {
                                            appendToTaskOutput(currentRetrievableFile.sourceURI.toString());
                                            appendToTaskOutput("Destination already exists, skipping file: " + currentRetrievableFile.destinationFile.getAbsolutePath());
//                                        appendToTaskOutput("this destination file already exists, skipping file");
                                        } else {
                                            if (replacingExitingFile) {
                                                //appendToTaskOutput(currentTarget);
                                                appendToTaskOutput("Replaced: " + currentRetrievableFile.destinationFile.getAbsolutePath());
                                                //appendToTaskOutput("replacing existing file...");
                                            } else {
//                                            appendToTaskOutput("saving to disk...");
                                            }
                                            // this function of the imdi.api will modify the imdi file as it saves it "(will be normalized and possibly de-domId-ed)"
                                            // this will make it dificult to determin if changes are from this function of by the user deliberatly making a chage
//                                        boolean removeIdAttributes = exportDestinationDirectory != null;

                                            ImdiTreeObject destinationNode = ImdiLoader.getSingleInstance().getImdiObjectWithoutLoading(currentRetrievableFile.destinationFile.toURI());
                                            if (destinationNode.getNeedsSaveToDisk(false)) {
                                                destinationNode.saveChangesToCache(true);
                                            }
                                            if (destinationNode.hasHistory()) {
                                                destinationNode.bumpHistory();
                                            }
//                                        todo: this has been observed to download a corpus branch that links to the sub nodes on the server instead of to the disk
//                                        todo: this appears to be adding too many ../../../../../../../ and must be checked
//                                        todo: the ../../../../../ issue is caused by the imdi api, but also there are issues with the way the imdi api 'corrects' links and the use of that method must be replaced
                                            if (!currentRetrievableFile.destinationFile.getParentFile().exists()) {
                                                currentRetrievableFile.destinationFile.getParentFile().mkdir();
                                            }
                                            currentMetdataUtil.copyMetadataFile(currentRetrievableFile.sourceURI, currentRetrievableFile.destinationFile, uncopiedLinks.toArray(new URI[][]{}), true);

//                                        ImdiTreeObject.api.writeDOM(nodDom, destinationFile, removeIdAttributes);
                                            LinorgJournal.getSingleInstance().saveJournalEntry(currentRetrievableFile.destinationFile.getAbsolutePath(), "", currentRetrievableFile.sourceURI.toString(), "", journalActionString);
                                            // validate the imdi file
//                                        appendToTaskOutput("validating");
                                            String checkerResult;
                                            checkerResult = xsdChecker.simpleCheck(currentRetrievableFile.destinationFile, currentRetrievableFile.sourceURI);
                                            if (checkerResult != null) {
                                                xmlOutput.append(currentRetrievableFile.sourceURI.toString() + "\n");
                                                xmlOutput.append("destination path: " + currentRetrievableFile.destinationFile.getAbsolutePath());
                                                System.out.println("checkerResult: " + checkerResult);
                                                xmlOutput.append(checkerResult + "\n");
                                                xmlOutput.setCaretPosition(xmlOutput.getText().length() - 1);
//                                            appendToTaskOutput(checkerResult);
                                                validationErrors.add(currentRetrievableFile.sourceURI);
                                                xsdErrors++;
                                            }
                                            // at this point the file should exist and not have been modified by the user
                                            // create hash index with server url but basedon the saved file
                                            // note that if the imdi.api has changed this file then it will not be detected
                                            // TODO: it will be best to change this to use the server api get mb5 sum when it is written
                                            // TODO: there needs to be some mechanism to check for changes on the server and update the local copy
                                            //getHash(tempFile, this.getUrl());
                                            if (replacingExitingFile) {
//                                            appendToTaskOutput("reloading existing data");
                                                ImdiLoader.getSingleInstance().requestReloadOnlyIfLoaded(currentRetrievableFile.destinationFile.toURI());
                                            }
//                                        appendToTaskOutput("done");
                                        }
                                    }
                                } catch (ArbilMetadataException ex) {
                                    GuiHelper.linorgBugCatcher.logError(currentRetrievableFile.sourceURI.toString(), ex);
                                    totalErrors++;
                                    metaDataCopyErrors.add(currentRetrievableFile.sourceURI);
                                    appendToTaskOutput("Unable to process the file: " + currentRetrievableFile.sourceURI + " (" + ex.getMessage() + ")");
//                                    System.out.println("Error getting links from: " + currentRetrievableFile.sourceURI);
                                } catch (MalformedURLException ex) {
                                    GuiHelper.linorgBugCatcher.logError(currentRetrievableFile.sourceURI.toString(), ex);
                                    totalErrors++;
                                    metaDataCopyErrors.add(currentRetrievableFile.sourceURI);
                                    appendToTaskOutput("Unable to process the file: " + currentRetrievableFile.sourceURI);
                                    System.out.println("Error getting links from: " + currentRetrievableFile.sourceURI);
                                }
                                totalLoaded++;

                                progressFoundLabel.setText(progressFoundLabelText + (getList.size() + totalLoaded));
                                progressProcessedLabel.setText(progressProcessedLabelText + totalLoaded);
                                progressAlreadyInCacheLabel.setText(progressAlreadyInCacheLabelText + totalExisting);
                                progressFailedLabel.setText(progressFailedLabelText + totalErrors);
                                progressXmlErrorsLabel.setText(progressXmlErrorsLabelText + xsdErrors);
                                resourceCopyErrorsLabel.setText(resourceCopyErrorsLabelText + resourceCopyErrors);
                                progressBar.setString(totalLoaded + "/" + (getList.size() + totalLoaded) + " (" + (totalErrors + xsdErrors + resourceCopyErrors) + " errors)");
                                if (testFreeSpace) {
                                    try {
                                        int diskFreePercent = (int) (directoryForSizeTest.getFreeSpace() / directoryForSizeTest.getTotalSpace() * 100);
                                        int freeGBytes = (int) (directoryForSizeTest.getFreeSpace() / 1073741824);
                                        //diskSpaceLabel.setText("Total Disk Use: " + diskFreePercent + "%");
                                        diskSpaceLabel.setText(diskFreeLabelText + freeGBytes + "GB");
                                        if (freeGbWarningPoint > freeGBytes) {
                                            progressBar.setIndeterminate(false);
                                            if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(LinorgWindowManager.getSingleInstance().linorgFrame,
                                                    "There is only " + freeGBytes + "GB free space left on the disk.\nTo you still want to continue?", searchDialog.getTitle(),
                                                    JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE)) {
                                                freeGbWarningPoint = freeGBytes - 1;
                                            } else {
                                                stopSearch = true;
                                            }
                                            progressBar.setIndeterminate(true);
                                        }
                                    } catch (Exception ex) {
                                        diskSpaceLabel.setText(diskFreeLabelText + "N/A");
                                        testFreeSpace = false;
                                    }
                                }
//                                System.out.println("progressFound"+ (getList.size() + totalLoaded));
//                                System.out.println("progressProcessed"+ totalLoaded);
//                                System.out.println("progressAlreadyInCache" + totalExisting);
//                                System.out.println("progressFailed"+totalErrors);
//                                System.out.println("progressXmlErrors" + xsdErrors);
//                                System.out.println("resourceCopyErrors" + resourceCopyErrors);
                            }
                            if (exportDestinationDirectory == null) {
                                // add the completed node to the done list
                                File newNodeLocation = LinorgSessionStorage.getSingleInstance().getSaveLocation(((ImdiTreeObject) currentElement).getParentDomNode().getUrlString());
                                finishedTopNodes.add(ImdiLoader.getSingleInstance().getImdiObjectWithoutLoading(newNodeLocation.toURI()));
                            }
                        }
                    }

                    finalMessageString = finalMessageString + "Processed " + totalLoaded + " Metadata Files.\n";
                    if (exportDestinationDirectory == null) {
//                        String newNodeLocation = GuiHelper.linorgSessionStorage.getSaveLocation(((ImdiTreeObject) currentElement).getUrlString());
//                            String newNodeLocation = ((ImdiTreeObject) currentElement).loadImdiDom(); // save the first node which will not be saved by loadSomeChildren
                        if (!stopSearch) { // make sure we dont add an incomplete location
                            //appendToTaskOutput("would save location when done: " + newNodeLocation);
                            //guiHelper.addLocation("file://" + newNodeLocation);
                            // TODO: create an imdinode to contain the name and point to the location
                            for (ImdiTreeObject currentFinishedNode : finishedTopNodes) {
                                if (destinationNode != null) {
                                    if (!destinationNode.getURI().equals(currentFinishedNode.getURI())) { // do not try to link a node to itself (itself is passed as the lead selection node when reimporting from the context menu)
                                        // add the nodes to their parent here
                                        destinationNode.addCorpusLink(currentFinishedNode);
                                    }
                                } else {
                                    // add the nodes to the local corpus root node here
                                    if (!TreeHelper.getSingleInstance().addLocation(currentFinishedNode.getURI())) {
                                        // alert the user when the node already exists and cannot be added again                                        
                                        finalMessageString = finalMessageString + "The location:\n" + currentFinishedNode + "\nalready exists and need not be added again\n";
                                    }
                                }
                                // make sure that any changes are reflected in the tree
                                currentFinishedNode.reloadNode();
                            }
                        }
                        if (destinationNode == null) {
                            // update the tree and reload the ui    
//                            TreeHelper.getSingleInstance().reloadLocalCorpusTree();
                            TreeHelper.getSingleInstance().applyRootLocations();
                        }
                    }
//                  progressLabel.setText("");
                    progressBar.setIndeterminate(false);
                    if (totalErrors != 0) {
                        finalMessageString = finalMessageString + "There were " + totalErrors + " errors, some files may not have been copied.\n";
                    }
                    if (xsdErrors != 0) {
                        finalMessageString = finalMessageString + "There were " + xsdErrors + " files that failed to validate and have xml errors.\n";
                    }
                    if (stopSearch) {
                        appendToTaskOutput("copy canceled");
                        System.out.println("copy canceled");
                        finalMessageString = finalMessageString + "The process was canceled, some files may not have been copied.\n";
                    } else {
                        // prevent restart
                        selectedNodes.removeAllElements();
                        //TODO: prevent restart and probably make sure that done files are not redone if stopped
                        if (exportDestinationDirectory != null) {
                            File[] destinationFile = new File[2];
                            removeEmptyDirectoryPaths(exportDestinationDirectory, destinationFile);
                            destinationFile[0].renameTo(destinationFile[1]);
                        }
                    }
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                    finalMessageString = finalMessageString + "There was a critical error.";
                }
//                finalMessageString = finalMessageString + totalLoaded + " files have been copied.\n";
                setUItoStoppedState();
                System.out.println("finalMessageString: " + finalMessageString);
                Object[] options = {"Close", "Details"};
                int detailsOption = JOptionPane.showOptionDialog(LinorgWindowManager.getSingleInstance().linorgFrame,
                        finalMessageString,
                        searchDialog.getTitle(),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        options,
                        options[0]);
                if (detailsOption == 0) {
                    searchDialog.setVisible(false);
                } else {
                    if (!detailsCheckBox.isSelected()) {
                        detailsCheckBox.setSelected(true);
                        showDetails(true);
                        searchDialog.pack();
                    }
                }
                if (exportDestinationDirectory != null) {
                    GuiHelper.getSingleInstance().openFileInExternalApplication(exportDestinationDirectory.toURI());

                }
            }
        }.start();
    }
///////////////////////////////////////////
// end functions that create the threads //
///////////////////////////////////////////
}
