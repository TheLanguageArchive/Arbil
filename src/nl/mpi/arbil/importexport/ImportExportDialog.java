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
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import mpi.imdi.api.*;
import mpi.util.OurURL;
import nl.mpi.arbil.data.ImdiLoader;
import org.w3c.dom.Document;

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
    private JCheckBox detailsCheckBox;
    private JCheckBox overwriteCheckBox;
    private JCheckBox shibbolethCheckBox;
    private JPanel shibbolethPanel;
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
    Vector<String> validationErrors = new Vector<String>();
    Vector<String> metaDataCopyErrors = new Vector<String>();
    Vector<String> fileCopyErrors = new Vector<String>();

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
        JFileChooser fileChooser = new JFileChooser();
        FileFilter imdiFileFilter = new FileFilter() {

            @Override
            public String getDescription() {
                return "IMDI";
            }

            @Override
            public boolean accept(File selectedFile) {
                return selectedFile.getName().toLowerCase().endsWith(".imdi");
            }
        };
        fileChooser.addChoosableFileFilter(imdiFileFilter);
        fileChooser.setMultiSelectionEnabled(true);
        if (JFileChooser.APPROVE_OPTION == fileChooser.showDialog(LinorgWindowManager.getSingleInstance().linorgFrame, "Import")) {
            Vector importNodeVector = new Vector();
            for (File currentFile : fileChooser.getSelectedFiles()) {
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
        JFileChooser fileChooser = new JFileChooser();
//        FileFilter emptyDirectoryFilter = new FileFilter() {
//
//            @Override
//            public String getDescription() {
//                return "Empty Directory";
//            }
//
//            @Override
//            public boolean accept(File selectedFile) {
//                return selectedFile.isDirectory(); // && selectedFile.list().length == 0;
//            }
//        };
//        fileChooser.addChoosableFileFilter(emptyDirectoryFilter);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        String fileDialogTitle = "Export Destination Directory";
        fileChooser.setDialogTitle(fileDialogTitle); // this must be set for mac
        boolean fileSelectDone = false;
        try {
            while (!fileSelectDone) {
                if (JFileChooser.APPROVE_OPTION == fileChooser.showDialog(LinorgWindowManager.getSingleInstance().linorgFrame, fileDialogTitle)) {
                    File destinationDirectory = null;
                    //Vector importNodeVector = new Vector();
                    File parentDirectory = fileChooser.getSelectedFile();
                    boolean createdDirectory = false;
                    if (!parentDirectory.exists() && parentDirectory.getParentFile().exists()) {
                        // create the directory provided that the parent directory exists
                        // ths is here due the the way the mac file select gui leads the user to type in a new directory name
                        createdDirectory = parentDirectory.mkdir();
                        destinationDirectory = parentDirectory;
                    }
                    if (!parentDirectory.exists()) {
                        JOptionPane.showMessageDialog(LinorgWindowManager.getSingleInstance().linorgFrame, "The export directory\n\"" + parentDirectory + "\"\ndoes not exist.\nPlease select or create a directory.", searchDialog.getTitle(), JOptionPane.PLAIN_MESSAGE);
                    } else {
                        if (!createdDirectory) {
                            String newDirectoryName = JOptionPane.showInputDialog(searchDialog, "Enter Export Name", searchDialog.getTitle(), JOptionPane.PLAIN_MESSAGE, null, null, "arbil_export").toString();
                            try {
                                destinationDirectory = new File(parentDirectory.getCanonicalPath() + File.separatorChar + newDirectoryName);
                                destinationDirectory.mkdir();
                            } catch (Exception e) {
                                JOptionPane.showMessageDialog(LinorgWindowManager.getSingleInstance().linorgFrame, "Could not create the export directory + \'" + newDirectoryName + "\'", searchDialog.getTitle(), JOptionPane.PLAIN_MESSAGE);
                            }
                        }
                        if (destinationDirectory != null && destinationDirectory.exists()) {
                            if (destinationDirectory.list().length == 0) {
                                fileSelectDone = true;
                                exportFromCache(new Vector(Arrays.asList(localCorpusSelectedNodes)), destinationDirectory);
                            } else {
                                JOptionPane.showMessageDialog(LinorgWindowManager.getSingleInstance().linorgFrame, "The export directory must be empty", searchDialog.getTitle(), JOptionPane.PLAIN_MESSAGE);
                            }
                        }
                    }
                } else {
                    fileSelectDone = true;
                }
            }
        } catch (Exception e) {
            System.out.println("aborting export");
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
        //exportDestinationDirectory = exportDestinationDirectory + File.separator + mirrorNameString;
        //boolean branchDirCreated = (new File(exportDestinationDirectory)).mkdir();
        // TODO: remove the branch directory and replace it with a named node in the locations settings or just a named imdinode
        if (LinorgSessionStorage.getSingleInstance().cacheDirExists()) {
//            performCopy();
            searchDialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(LinorgWindowManager.getSingleInstance().linorgFrame, "Could not create the local directory", searchDialog.getTitle(), JOptionPane.PLAIN_MESSAGE);
        }
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
        //String mirrorNameString = JOptionPane.showInputDialog(destinationComp, "Enter a tile for the local mirror");

        //exportDestinationDirectory = exportDestinationDirectory + File.separator + mirrorNameString;
        //boolean branchDirCreated = (new File(exportDestinationDirectory)).mkdir();
        // TODO: remove the branch directory and replace it with a named node in the locations settings or just a named imdinode
        if (LinorgSessionStorage.getSingleInstance().cacheDirExists()) {
//            performCopy();
            searchDialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(LinorgWindowManager.getSingleInstance().linorgFrame, "Could not create the local directory", searchDialog.getTitle(), JOptionPane.PLAIN_MESSAGE);
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

    private void showDetails(boolean showFlag) {
        // showFlag is false the first time this is called when the dialog is initialised so we need to make sure that pack gets called in this case
        // otherwise try to prevent chenging the window size when not required
        if (!showFlag || detailsTabPane.isVisible() != showFlag) {
            detailsTabPane.setVisible(showFlag);
            bottomPanel.setVisible(showFlag);
            copyFilesCheckBox.setVisible(showFlag);
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
        buttonsPanel.add(startButton);

        searchPanel.add(buttonsPanel, BorderLayout.SOUTH);

        searchDialog.setLocationRelativeTo(targetComponent);

        showInTableButton.setEnabled(false);
        showInTableButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    if (metaDataCopyErrors.size() > 0) {
                        LinorgWindowManager.getSingleInstance().openFloatingTableOnce(metaDataCopyErrors.toArray(new String[]{}), progressFailedLabelText);
                    }
                    if (validationErrors.size() > 0) {
                        LinorgWindowManager.getSingleInstance().openFloatingTableOnce(validationErrors.toArray(new String[]{}), progressXmlErrorsLabelText);
                    }
                    if (fileCopyErrors.size() > 0) {
                        ImdiTableModel resourceFileErrorsTable = LinorgWindowManager.getSingleInstance().openFloatingTableOnce(fileCopyErrors.toArray(new String[]{}), resourceCopyErrorsLabelText);
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
                File tempFile = destinationFile[0] = File.createTempFile(currentDirectory.getName(), "", exportDestinationDirectory);
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
        new Thread() {

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
                    directoryForSizeTest = new File(LinorgSessionStorage.getSingleInstance().cacheDirectory);
                }
                if (copyFilesCheckBox.isSelected()) {
                    resourceCopyOutput.append("'Copy Resource Files' is selected: Resource files will be downloaded where appropriate permission are granted." + "\n");
                } else {
                    resourceCopyOutput.append("'Copy Resource Files' is not selected: No resource files will be downloaded, however they will be still accessible via the web server." + "\n");
                }

                try {
//                    boolean saveToCache = true;
                    File tempFileForValidator = File.createTempFile("linorg", ".imdi");
                    tempFileForValidator.deleteOnExit();
                    XsdChecker xsdChecker = new XsdChecker();
                    waitTillVisible();
//                    appendToTaskOutput("copying: ");
                    progressBar.setIndeterminate(true);
//                    int[] childCount = countChildern();
//                    appendToTaskOutput("corpus to load: " + childCount[0] + "corpus loaded: " + childCount[1]);
                    Enumeration selectedNodesEnum = selectedNodes.elements();
                    Vector<ImdiTreeObject> finishedTopNodes = new Vector<ImdiTreeObject>();
                    while (selectedNodesEnum.hasMoreElements() && !stopSearch) {
                        Object currentElement = selectedNodesEnum.nextElement();
                        if (currentElement instanceof ImdiTreeObject) {
                            Vector getList = new Vector(); // TODO: make this global so files do not get redone
                            getList.add(((ImdiTreeObject) currentElement).getParentDomNode().getUrlString());
                            while (!stopSearch && getList.size() > 0) {
                                String currentTarget = (String) getList.remove(0);
//                                appendToTaskOutput(currentTarget);
                                try {
//                                    appendToTaskOutput("connecting...");
                                    OurURL inUrlLocal = new OurURL(currentTarget);
//                                    String destinationPath;
                                    File destinationFile;// = new File(destinationPath);
                                    String journalActionString;
                                    if (exportDestinationDirectory == null) {
                                        destinationFile = LinorgSessionStorage.getSingleInstance().getSaveLocation(currentTarget);
                                        journalActionString = "import";
                                    } else {
                                        //TODO: make sure this is correct then remove any directories that contain only one directory
                                        destinationFile = LinorgSessionStorage.getSingleInstance().getExportPath(currentTarget, exportDestinationDirectory.getPath());
                                        journalActionString = "export";
                                    }

//                                    appendToTaskOutput("destination path: " + destinationFile.getAbsolutePath());
                                    OurURL destinationUrl = new OurURL(destinationFile.toURL());

                                    Document nodDom = ImdiTreeObject.api.loadIMDIDocument(inUrlLocal, false);
//                                    appendToTaskOutput("getting links...");
                                    IMDILink[] links;
                                    links = ImdiTreeObject.api.getIMDILinks(nodDom, inUrlLocal, WSNodeType.UNKNOWN);
//                                        links = ImdiTreeObject.api.getIMDILinks(nodDom, inUrlLocal, WSNodeType.CORPUS);
                                    if (links != null) {
                                        for (int linkCount = 0; linkCount < links.length && !stopSearch; linkCount++) {
                                            System.out.println("Link: " + links[linkCount].getRawURL());
                                            String currentLink = links[linkCount].getRawURL().toString();
                                            if (ImdiTreeObject.isStringImdi(currentLink)) {
                                                getList.add(currentLink);
                                            } else /*if (links[linkCount].getType() != null) this null also exists when a resource is local *//* filter out non resources */ {
                                                boolean resourceFileCopied = false;
                                                if (copyFilesCheckBox.isSelected()) {
//                                                    appendToTaskOutput("getting resource file: " + links[linkCount].getType());
//                                                    resourceCopyOutput.append("Type: " + links[linkCount].getType() + "\n");
//                                                    resourceCopyOutput.append(currentLink + "\n");
                                                    File downloadFileLocation;
                                                    if (exportDestinationDirectory == null) {
                                                        downloadFileLocation = LinorgSessionStorage.getSingleInstance().updateCache(currentLink, false, downloadAbortFlag);
                                                    } else {
                                                        downloadFileLocation = LinorgSessionStorage.getSingleInstance().getExportPath(currentLink, exportDestinationDirectory.getPath());
//                                                        System.out.println("downloadLocation: " + downloadLocation);
                                                        LinorgSessionStorage.getSingleInstance().saveRemoteResource(new URL(currentLink), downloadFileLocation, shibbolethNegotiator, true, downloadAbortFlag);
                                                    }
                                                    //resourceCopyOutput.append(downloadFileLocation + "\n");
                                                    if (downloadFileLocation.exists()) {
                                                        resourceFileCopied = true;
                                                        appendToTaskOutput("Downloaded resource: " + downloadFileLocation.getAbsolutePath());
                                                        //resourceCopyOutput.append("Copied " + downloadFileLocation.length() + "b\n");
                                                    } else {
                                                        resourceCopyOutput.append("Download failed: " + currentLink + " \n");
                                                        //resourceCopyOutput.append("path: " + destinationFile.getAbsolutePath());
                                                        //resourceCopyOutput.append("Failed" + "\n");
                                                        fileCopyErrors.add(currentTarget);
                                                        resourceCopyErrors++;
                                                    }
                                                    resourceCopyOutput.setCaretPosition(resourceCopyOutput.getText().length() - 1);
                                                }
                                                if (!resourceFileCopied) {
                                                    ImdiTreeObject.api.changeIMDILink(nodDom, destinationUrl, links[linkCount]);
                                                }
                                            }
//                                            System.out.println("getIMDILinks.getRawURL: " + links[linkCount].getRawURL().toString());
//                                            SystecurrentTree.m.out.println("getIMDILinks.getURL: " + links[linkCount].getURL().toString());
                                        }
                                    }
                                    boolean replacingExitingFile = destinationFile.exists() && overwriteCheckBox.isSelected();
                                    if (destinationFile.exists()) {
                                        totalExisting++;
                                    }
                                    if (destinationFile.exists() && !overwriteCheckBox.isSelected()) {
                                        appendToTaskOutput(currentTarget);
                                        appendToTaskOutput("Destination already exists, skipping file: " + destinationFile.getAbsolutePath());
//                                        appendToTaskOutput("this destination file already exists, skipping file");
                                    } else {
                                        if (replacingExitingFile) {
                                            //appendToTaskOutput(currentTarget);
                                            appendToTaskOutput("Replaced: " + destinationFile.getAbsolutePath());
                                            //appendToTaskOutput("replacing existing file...");
                                        } else {
//                                            appendToTaskOutput("saving to disk...");
                                        }
                                        // this function of the imdi.api will modify the imdi file as it saves it "(will be normalized and possibly de-domId-ed)"
                                        // this will make it dificult to determin if changes are from this function of by the user deliberatly making a chage
                                        boolean removeIdAttributes = exportDestinationDirectory != null;
                                        // TODO: bumpHistory();
                                        ImdiTreeObject destinationNode = ImdiLoader.getSingleInstance().getImdiObjectWithoutLoading(destinationFile.toURI());
                                        if (destinationNode.getNeedsSaveToDisk()) {
                                            destinationNode.saveChangesToCache(true);
                                        }
                                        if (destinationNode.hasHistory()) {
                                            destinationNode.bumpHistory();
                                        }
                                        ImdiTreeObject.api.writeDOM(nodDom, destinationFile, removeIdAttributes);
                                        LinorgJournal.getSingleInstance().saveJournalEntry(destinationFile.getAbsolutePath(), "", currentTarget, "", journalActionString);
                                        // validate the imdi file
//                                        appendToTaskOutput("validating");
                                        String checkerResult;
                                        if (exportDestinationDirectory == null) {
                                            // when not exporting we need to remove the id attributes in order to validate the file
                                            ImdiTreeObject.api.writeDOM(nodDom, tempFileForValidator, true);
                                            checkerResult = xsdChecker.simpleCheck(tempFileForValidator, currentTarget);
                                        } else {
                                            // when exporting we can just validate the destination file
                                            checkerResult = xsdChecker.simpleCheck(destinationFile, currentTarget);
                                        }
                                        if (checkerResult != null) {
                                            xmlOutput.append(currentTarget);
                                            xmlOutput.append("destination path: " + destinationFile.getAbsolutePath());
                                            System.out.println("checkerResult: " + checkerResult);
                                            xmlOutput.append(checkerResult + "\n");
                                            xmlOutput.setCaretPosition(xmlOutput.getText().length() - 1);
//                                            appendToTaskOutput(checkerResult);
                                            validationErrors.add(currentTarget);
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
                                            ImdiLoader.getSingleInstance().requestReloadOnlyIfLoaded(destinationFile.toURI());
                                        }
//                                        appendToTaskOutput("done");
                                    }
                                } catch (Exception ex) {
                                    GuiHelper.linorgBugCatcher.logError(currentTarget, ex);
                                    totalErrors++;
                                    metaDataCopyErrors.add(currentTarget);
                                    appendToTaskOutput("unable to process the file: " + currentTarget);
                                    System.out.println("Error getting links from: " + currentTarget);
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
                            // add the completed node to the done list
                            File newNodeLocation = LinorgSessionStorage.getSingleInstance().getSaveLocation(((ImdiTreeObject) currentElement).getParentDomNode().getUrlString());
                            finishedTopNodes.add(ImdiLoader.getSingleInstance().getImdiObject(null, newNodeLocation.toURI()));
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
                            for (ImdiTreeObject currentFinishedNode : finishedTopNodes.toArray(new ImdiTreeObject[]{})) {
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
            }
        }.start();
    }
///////////////////////////////////////////
// end functions that create the threads //
///////////////////////////////////////////
}
