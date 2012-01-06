package nl.mpi.arbil.ui;

import nl.mpi.arbil.util.XsdChecker;
import nl.mpi.arbil.data.ArbilJournal;
import nl.mpi.arbil.util.DownloadAbortFlag;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.data.ArbilDataNode;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
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
import nl.mpi.arbil.data.metadatafile.MetadataUtils;
import nl.mpi.arbil.ArbilMetadataException;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.data.DataNodeLoader;
import nl.mpi.arbil.data.MetadataFormat;
import nl.mpi.arbil.data.importexport.ShibbolethNegotiator;
import nl.mpi.arbil.util.TreeHelper;

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
    protected JCheckBox copyFilesCheckBox;
    protected JCheckBox renameFileToNodeName;
    protected JCheckBox renameFileToLamusFriendlyName;
    protected JCheckBox detailsCheckBox;
    protected JCheckBox overwriteCheckBox;
    protected JCheckBox shibbolethCheckBox;
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
    protected Vector<ArbilDataNode> selectedNodes;
    ArbilDataNode destinationNode = null;
    protected File exportDestinationDirectory = null;
    DownloadAbortFlag downloadAbortFlag = new DownloadAbortFlag();
    ShibbolethNegotiator shibbolethNegotiator = null;
    Vector<URI> validationErrors = new Vector<URI>();
    Vector<URI> metaDataCopyErrors = new Vector<URI>();
    Vector<URI> fileCopyErrors = new Vector<URI>();
    private static TreeHelper treeHelper;

    public static void setTreeHelper(TreeHelper treeHelperInstance) {
	treeHelper = treeHelperInstance;
    }
    private static DataNodeLoader dataNodeLoader;

    public static void setDataNodeLoader(DataNodeLoader dataNodeLoaderInstance) {
	dataNodeLoader = dataNodeLoaderInstance;
    }

    private void setNodesPanel(ArbilDataNode selectedNode, JPanel nodePanel) {
	JLabel currentLabel = new JLabel(selectedNode.toString(), selectedNode.getIcon(), JLabel.CENTER);
	nodePanel.add(currentLabel);
    }

    private void setNodesPanel(Vector selectedNodes, JPanel nodePanel) {
//            setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.PAGE_AXIS));
//        nodePanel.setLayout(new java.awt.GridLayout());
//        add(nodePanel);
	for (Enumeration<ArbilDataNode> selectedNodesEnum = selectedNodes.elements(); selectedNodesEnum.hasMoreElements();) {
	    ArbilDataNode currentNode = selectedNodesEnum.nextElement();
	    JLabel currentLabel = new JLabel(currentNode.toString(), currentNode.getIcon(), JLabel.CENTER);
	    nodePanel.add(currentLabel);
	}
    }

    private void setLocalCacheToNodesPanel(JPanel nodePanel) {
	DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeHelper.getLocalCorpusTreeModel().getRoot();
	ArbilNode rootArbilNode = (ArbilNode) rootNode.getUserObject();
	JLabel currentLabel = new JLabel(rootArbilNode.toString(), rootArbilNode.getIcon(), JLabel.CENTER);
	nodePanel.add(currentLabel);
    }

    private void setLocalFileToNodesPanel(JPanel nodePanel, File destinationDirectory) {
	DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeHelper.getLocalDirectoryTreeModel().getRoot();
	ArbilNode rootArbilNode = (ArbilNode) rootNode.getUserObject();
	JLabel currentLabel = new JLabel(destinationDirectory.getPath(), rootArbilNode.getIcon(), JLabel.CENTER);
	nodePanel.add(currentLabel);
    }

    public void importArbilBranch() {
	File[] selectedFiles = ArbilWindowManager.getSingleInstance().showFileSelectBox("Import", false, true, true);
	if (selectedFiles != null) {
	    Vector importNodeVector = new Vector();
	    for (File currentFile : selectedFiles) {
		ArbilDataNode nodeToImport = dataNodeLoader.getArbilDataNode(null, currentFile.toURI());
		importNodeVector.add(nodeToImport);
	    }
	    copyToCache(importNodeVector);
	}
    }

    public void selectExportDirectoryAndExport(ArbilDataNode[] localCorpusSelectedNodes) {
	// make sure the chosen directory is empty
	// export the tree, maybe adjusting resource links so that resource files do not need to be copied
	searchDialog.setTitle("Export Branch");
	File destinationDirectory = ArbilWindowManager.getSingleInstance().showEmptyExportDirectoryDialogue(searchDialog.getTitle());
	if (destinationDirectory != null) {
	    exportFromCache(new Vector(Arrays.asList(localCorpusSelectedNodes)), destinationDirectory);
	}
    }

    private void exportFromCache(Vector localSelectedNodes, File destinationDirectory) {
	selectedNodes = localSelectedNodes;
//        searchDialog.setTitle("Export Branch");
	if (!selectedNodesContainDataNode()) {
	    ArbilWindowManager.getSingleInstance().addMessageDialogToQueue("No relevant nodes are selected", searchDialog.getTitle());
	    return;
	}
	setNodesPanel(selectedNodes, inputNodePanel);
	setLocalFileToNodesPanel(outputNodePanel, destinationDirectory);
	//String mirrorNameString = JOptionPane.showInputDialog(destinationComp, "Enter a tile for the local mirror");

	exportDestinationDirectory = destinationDirectory;
	searchDialog.setVisible(true);
    }

    public void copyToCache(ArbilDataNode[] localSelectedNodes) {
	copyToCache(new Vector(Arrays.asList(localSelectedNodes)));
    }

    // sets the destination branch for the imported nodes
    public void setDestinationNode(ArbilDataNode localDestinationNode) {
	destinationNode = localDestinationNode;
	setNodesPanel(destinationNode, outputNodePanel);
    }

    public void copyToCache(Vector localSelectedNodes) {
	selectedNodes = localSelectedNodes;
	searchDialog.setTitle("Import Branch");
	if (!selectedNodesContainDataNode()) {
	    ArbilWindowManager.getSingleInstance().addMessageDialogToQueue("No relevant nodes are selected", searchDialog.getTitle());
	    return;
	}
	setNodesPanel(selectedNodes, inputNodePanel);
	if (destinationNode == null) {
	    setLocalCacheToNodesPanel(outputNodePanel);
	}
	searchDialog.setVisible(true);
    }

    private boolean selectedNodesContainDataNode() {
	Enumeration selectedNodesEnum = selectedNodes.elements();
	while (selectedNodesEnum.hasMoreElements()) {
	    if (selectedNodesEnum.nextElement() instanceof ArbilDataNode) {
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
	    shibbolethCheckBox.setVisible(showFlag && copyFilesCheckBox.isSelected());
	    shibbolethPanel.setVisible(showFlag && copyFilesCheckBox.isSelected());
	    outputNodePanel.setVisible(false);
	    inputNodePanel.setVisible(false);
//            searchDialog.pack();
	    outputNodePanel.setVisible(true);
	    inputNodePanel.setVisible(true);
	    System.out.println(searchDialog.getSize());
	    if (showFlag) {
		searchDialog.setMinimumSize(new Dimension(467, 500));
	    } else {
		searchDialog.setMinimumSize(new Dimension(316, 126));
		searchDialog.setSize(new Dimension(316, 126));
	    }
	    searchDialog.setResizable(showFlag);
	}
    }

    // the targetComponent is used to place the import dialog
    public ImportExportDialog(Component targetComponent) throws Exception {
	ArbilWindowManager.getSingleInstance().offerUserToSaveChanges();
	searchDialog = new JDialog(JOptionPane.getFrameForComponent(ArbilWindowManager.getSingleInstance().linorgFrame), true);
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
//                        linorgBugCatcher.logError(ignore);
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

	detailsCheckBox = new JCheckBox("Show Details and Options", false);
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
	detailsPanel.setLayout(new BorderLayout());

	copyFilesCheckBox = new JCheckBox("Copy Resource Files (if available)", false);
	renameFileToNodeName = new JCheckBox("Rename Metadata Files (to match local corpus tree names)", true);
	renameFileToLamusFriendlyName = new JCheckBox("Limit Characters in File Names (LAMUS friendly format)", true);
	overwriteCheckBox = new JCheckBox("Overwrite Local Changes", false);
	shibbolethCheckBox = new JCheckBox("Shibboleth authentication via the SURFnet method", false);

	// NOTE TG 11/4/2011: In ticket #679 it was decided to disable shibboleth authentication until the entire chain is functional.
	// This requires some work on the server.
	shibbolethCheckBox.setEnabled(false);

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

	JPanel detailsTopPanel = new JPanel();
	detailsTopPanel.setLayout(new BoxLayout(detailsTopPanel, BoxLayout.PAGE_AXIS));
	JPanel detailsTopCheckBoxPanel = new JPanel();
	detailsTopCheckBoxPanel.setLayout(new BoxLayout(detailsTopCheckBoxPanel, BoxLayout.PAGE_AXIS));

	detailsTopCheckBoxPanel.add(renameFileToNodeName);
	detailsTopCheckBoxPanel.add(renameFileToLamusFriendlyName);
	detailsTopCheckBoxPanel.add(overwriteCheckBox);
	detailsTopCheckBoxPanel.add(copyFilesCheckBox);
	detailsTopCheckBoxPanel.add(shibbolethCheckBox);

	JPanel paddingPanel = new JPanel();
	paddingPanel.setLayout(new BoxLayout(paddingPanel, BoxLayout.LINE_AXIS));
	JPanel leftPadding = new JPanel();
	leftPadding.setMaximumSize(new Dimension(500, 100));
	paddingPanel.add(leftPadding);
	paddingPanel.add(detailsTopCheckBoxPanel);
	paddingPanel.add(new JPanel());
	detailsTopPanel.add(paddingPanel);
	detailsTopPanel.add(shibbolethPanel);
	detailsPanel.add(detailsTopPanel, BorderLayout.NORTH);

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

	bottomPanel.add(new SaveCurrentSettingsPanel(this, null));
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
			ArbilWindowManager.getSingleInstance().openFloatingTableOnce(metaDataCopyErrors.toArray(new URI[]{}), progressFailedLabelText);
		    }
		    if (validationErrors.size() > 0) {
			ArbilWindowManager.getSingleInstance().openAllChildNodesInFloatingTableOnce(validationErrors.toArray(new URI[]{}), progressXmlErrorsLabelText);
		    }
		    if (fileCopyErrors.size() > 0) {
			ArbilTableModel resourceFileErrorsTable = ArbilWindowManager.getSingleInstance().openFloatingTableOnceGetModel(fileCopyErrors.toArray(new URI[]{}), resourceCopyErrorsLabelText);
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
	overwriteCheckBox.setEnabled(false);
	copyFilesCheckBox.setEnabled(false);
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
	overwriteCheckBox.setEnabled(true);
	copyFilesCheckBox.setEnabled(true);

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
//
//    private void removeEmptyDirectoryPaths(File currentDirectory, File[] destinationFile) {
//	File[] childDirectories = currentDirectory.listFiles();
//	if (childDirectories != null && childDirectories.length == 1) {
//	    removeEmptyDirectoryPaths(childDirectories[0], destinationFile);
//	    if (childDirectories[0].isDirectory()) {
//		childDirectories[0].delete();
//	    }
//	} else {
//	    try {
//		File tempFile = destinationFile[0] = File.createTempFile("tmp-" + currentDirectory.getName(), "", exportDestinationDirectory);
//		destinationFile[1] = new File(exportDestinationDirectory, currentDirectory.getName());
//		if (tempFile.delete()) {
//		    if (!currentDirectory.renameTo(tempFile)) {
//			GuiHelper.linorgBugCatcher.logError(new Exception("Error while renaming file"));
//		    }
//		}
//	    } catch (Exception ex) {
//		GuiHelper.linorgBugCatcher.logError(ex);
//	    }
//	}
//    }
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
	createPerformCopyThread("performCopy").start();
    }

    private Thread createPerformCopyThread(String threadName) {
	return new Thread(threadName) {

	    int freeGbWarningPoint = 3;
	    int xsdErrors = 0;
	    int totalLoaded = 0;
	    int totalErrors = 0;
	    int totalExisting = 0;
	    int resourceCopyErrors = 0;
	    String finalMessageString = "";
	    File directoryForSizeTest;
	    boolean testFreeSpace;

	    @Override
	    public void run() {
		String javaVersionString = System.getProperty("java.version");
		// TG: Apparently test not required for version >= 1.5 (2011/2/3)
		testFreeSpace = !(javaVersionString.startsWith("1.4.") || javaVersionString.startsWith("1.5."));

		directoryForSizeTest = exportDestinationDirectory != null
			? exportDestinationDirectory
			: ArbilSessionStorage.getSingleInstance().getCacheDirectory();


		// Append message about copying resource files to the copy output
		if (copyFilesCheckBox.isSelected()) {
		    resourceCopyOutput.append("'Copy Resource Files' is selected: Resource files will be downloaded where appropriate permission are granted." + "\n");
		} else {
		    resourceCopyOutput.append("'Copy Resource Files' is not selected: No resource files will be downloaded, however they will be still accessible via the web server." + "\n");
		}

		try {
		    // Copy the selected nodes
		    copyElements(selectedNodes.elements());
		} catch (Exception ex) {
		    GuiHelper.linorgBugCatcher.logError(ex);
		    finalMessageString = finalMessageString + "There was a critical error.";
		}

		// Done copying
		setUItoStoppedState();
		System.out.println("finalMessageString: " + finalMessageString);
		Object[] options = {"Close", "Details"};
		int detailsOption = JOptionPane.showOptionDialog(ArbilWindowManager.getSingleInstance().linorgFrame, finalMessageString, searchDialog.getTitle(), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
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

	    private void copyElements(Enumeration selectedNodesEnum) {
		XsdChecker xsdChecker = new XsdChecker();
		waitTillVisible();
		progressBar.setIndeterminate(true);
		ArrayList<ArbilDataNode> finishedTopNodes = new ArrayList<ArbilDataNode>();
		Hashtable<URI, RetrievableFile> seenFiles = new Hashtable<URI, RetrievableFile>();
		ArrayList<URI> getList = new ArrayList<URI>();
		ArrayList<URI> doneList = new ArrayList<URI>();
		while (selectedNodesEnum.hasMoreElements() && !stopSearch) {
		    Object currentElement = selectedNodesEnum.nextElement();
		    if (currentElement instanceof ArbilDataNode) {
			copyElement(currentElement, getList, seenFiles, doneList, xsdChecker, finishedTopNodes);
		    }
		}
		finalMessageString = finalMessageString + "Processed " + totalLoaded + " Metadata Files.\n";
		if (exportDestinationDirectory == null) {
		    if (!stopSearch) {
			for (ArbilDataNode currentFinishedNode : finishedTopNodes) {
			    if (destinationNode != null) {
				if (!destinationNode.getURI().equals(currentFinishedNode.getURI())) {
				    destinationNode.addCorpusLink(currentFinishedNode);
				}
			    } else {
				if (!treeHelper.addLocation(currentFinishedNode.getURI())) {
				    finalMessageString = finalMessageString + "The location:\n" + currentFinishedNode + "\nalready exists and need not be added again\n";
				}
			    }
			    currentFinishedNode.reloadNode();
			}
		    }
		    if (destinationNode == null) {
			treeHelper.applyRootLocations();
		    } else {
			destinationNode.reloadNode();
		    }
		}
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
		    selectedNodes.removeAllElements();
		}
	    }

	    private void copyElement(Object currentElement, ArrayList<URI> getList, Hashtable<URI, RetrievableFile> seenFiles, ArrayList<URI> doneList, XsdChecker xsdChecker, ArrayList<ArbilDataNode> finishedTopNodes) {
		URI currentGettableUri = ((ArbilDataNode) currentElement).getParentDomNode().getURI();
		getList.add(currentGettableUri);
		if (!seenFiles.containsKey(currentGettableUri)) {
		    seenFiles.put(currentGettableUri, new RetrievableFile(((ArbilDataNode) currentElement).getParentDomNode().getURI(), exportDestinationDirectory));
		}
		while (!stopSearch && getList.size() > 0) {
		    RetrievableFile currentRetrievableFile = seenFiles.get(getList.remove(0));
		    copyFile(currentRetrievableFile, seenFiles, doneList, getList, xsdChecker);
		}
		if (exportDestinationDirectory == null) {
		    File newNodeLocation = ArbilSessionStorage.getSingleInstance().getSaveLocation(((ArbilDataNode) currentElement).getParentDomNode().getUrlString());
		    finishedTopNodes.add(dataNodeLoader.getArbilDataNodeWithoutLoading(newNodeLocation.toURI()));
		}
	    }

	    private void copyFile(RetrievableFile currentRetrievableFile, Hashtable<URI, RetrievableFile> seenFiles, ArrayList<URI> doneList, ArrayList<URI> getList, XsdChecker xsdChecker) {
		try {
		    if (!doneList.contains(currentRetrievableFile.sourceURI)) {
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
			MetadataUtils currentMetdataUtil = ArbilDataNode.getMetadataUtils(currentRetrievableFile.sourceURI.toString());
			if (currentMetdataUtil == null) {
			    throw new ArbilMetadataException("Metadata format could not be determined");
			}
			ArrayList<URI[]> uncopiedLinks = new ArrayList<URI[]>();
			URI[] linksUriArray = currentMetdataUtil.getCorpusLinks(currentRetrievableFile.sourceURI);
			if (linksUriArray != null) {
			    copyLinks(linksUriArray, seenFiles, currentRetrievableFile, getList, uncopiedLinks);
			}
			boolean replacingExitingFile = currentRetrievableFile.destinationFile.exists() && overwriteCheckBox.isSelected();
			if (currentRetrievableFile.destinationFile.exists()) {
			    totalExisting++;
			}
			if (currentRetrievableFile.destinationFile.exists() && !overwriteCheckBox.isSelected()) {
			    appendToTaskOutput(currentRetrievableFile.sourceURI.toString());
			    appendToTaskOutput("Destination already exists, skipping file: " + currentRetrievableFile.destinationFile.getAbsolutePath());
			} else {
			    if (replacingExitingFile) {
				appendToTaskOutput("Replaced: " + currentRetrievableFile.destinationFile.getAbsolutePath());
			    } else {
			    }
			    ArbilDataNode destinationNode = dataNodeLoader.getArbilDataNodeWithoutLoading(currentRetrievableFile.destinationFile.toURI());
			    if (destinationNode.getNeedsSaveToDisk(false)) {
				destinationNode.saveChangesToCache(true);
			    }
			    if (destinationNode.hasHistory()) {
				destinationNode.bumpHistory();
			    }
			    if (!currentRetrievableFile.destinationFile.getParentFile().exists()) {
				if (!currentRetrievableFile.destinationFile.getParentFile().mkdir()) {
				    GuiHelper.linorgBugCatcher.logError(new IOException("Could not create missing parent directory for " + currentRetrievableFile.destinationFile));
				}
			    }
			    currentMetdataUtil.copyMetadataFile(currentRetrievableFile.sourceURI, currentRetrievableFile.destinationFile, uncopiedLinks.toArray(new URI[][]{}), true);
			    ArbilJournal.getSingleInstance().saveJournalEntry(currentRetrievableFile.destinationFile.getAbsolutePath(), "", currentRetrievableFile.sourceURI.toString(), "", journalActionString);
			    String checkerResult;
			    checkerResult = xsdChecker.simpleCheck(currentRetrievableFile.destinationFile, currentRetrievableFile.sourceURI);
			    if (checkerResult != null) {
				xmlOutput.append(currentRetrievableFile.sourceURI.toString() + "\n");
				xmlOutput.append("destination path: " + currentRetrievableFile.destinationFile.getAbsolutePath());
				System.out.println("checkerResult: " + checkerResult);
				xmlOutput.append(checkerResult + "\n");
				xmlOutput.setCaretPosition(xmlOutput.getText().length() - 1);
				validationErrors.add(currentRetrievableFile.sourceURI);
				xsdErrors++;
			    }
			    if (replacingExitingFile) {
				dataNodeLoader.requestReloadOnlyIfLoaded(currentRetrievableFile.destinationFile.toURI());
			    }
			}
		    }
		} catch (ArbilMetadataException ex) {
		    GuiHelper.linorgBugCatcher.logError(currentRetrievableFile.sourceURI.toString(), ex);
		    totalErrors++;
		    metaDataCopyErrors.add(currentRetrievableFile.sourceURI);
		    appendToTaskOutput("Unable to process the file: " + currentRetrievableFile.sourceURI + " (" + ex.getMessage() + ")");
		} catch (MalformedURLException ex) {
		    GuiHelper.linorgBugCatcher.logError(currentRetrievableFile.sourceURI.toString(), ex);
		    totalErrors++;
		    metaDataCopyErrors.add(currentRetrievableFile.sourceURI);
		    appendToTaskOutput("Unable to process the file: " + currentRetrievableFile.sourceURI);
		    System.out.println("Error getting links from: " + currentRetrievableFile.sourceURI);
		} catch (IOException ex) {
		    GuiHelper.linorgBugCatcher.logError(currentRetrievableFile.sourceURI.toString(), ex);
		    totalErrors++;
		    metaDataCopyErrors.add(currentRetrievableFile.sourceURI);
		    appendToTaskOutput("Unable to process the file: " + currentRetrievableFile.sourceURI);
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
		    testFreeSpace();
		}
	    }

	    private void copyLinks(URI[] linksUriArray, Hashtable<URI, RetrievableFile> seenFiles, RetrievableFile currentRetrievableFile, ArrayList<URI> getList, ArrayList<URI[]> uncopiedLinks) throws MalformedURLException {
		for (int linkCount = 0; linkCount < linksUriArray.length && !stopSearch; linkCount++) {
		    System.out.println("Link: " + linksUriArray[linkCount].toString());
		    String currentLink = linksUriArray[linkCount].toString();
		    URI gettableLinkUri = linksUriArray[linkCount].normalize();
		    if (!seenFiles.containsKey(gettableLinkUri)) {
			seenFiles.put(gettableLinkUri, new RetrievableFile(gettableLinkUri, currentRetrievableFile.childDestinationDirectory));
		    }
		    RetrievableFile retrievableLink = seenFiles.get(gettableLinkUri);
		    if (MetadataFormat.isPathMetadata(currentLink)) {
			getList.add(gettableLinkUri);
			if (renameFileToNodeName.isSelected() && exportDestinationDirectory != null) {
			    retrievableLink.calculateTreeFileName(renameFileToLamusFriendlyName.isSelected());
			} else {
			    retrievableLink.calculateUriFileName();
			}
			uncopiedLinks.add(new URI[]{linksUriArray[linkCount], retrievableLink.destinationFile.toURI()});
		    } else {
			if (!copyFilesCheckBox.isSelected()) {
			    uncopiedLinks.add(new URI[]{linksUriArray[linkCount], linksUriArray[linkCount]});
			} else {
			    File downloadFileLocation;
			    if (exportDestinationDirectory == null) {
				downloadFileLocation = ArbilSessionStorage.getSingleInstance().updateCache(currentLink, shibbolethNegotiator, false, false, downloadAbortFlag, resourceProgressLabel);
			    } else {
				if (renameFileToNodeName.isSelected() && exportDestinationDirectory != null) {
				    retrievableLink.calculateTreeFileName(renameFileToLamusFriendlyName.isSelected());
				} else {
				    retrievableLink.calculateUriFileName();
				}
				if (!retrievableLink.destinationFile.getParentFile().exists()) {
				    if (!retrievableLink.destinationFile.getParentFile().mkdirs()) {
					GuiHelper.linorgBugCatcher.logError(new IOException("Could not create missing parent directory for " + retrievableLink.destinationFile));
				    }
				}
				downloadFileLocation = retrievableLink.destinationFile;
				resourceProgressLabel.setText(" ");
				ArbilSessionStorage.getSingleInstance().saveRemoteResource(new URL(currentLink), downloadFileLocation, shibbolethNegotiator, true, false, downloadAbortFlag, resourceProgressLabel);
				resourceProgressLabel.setText(" ");
			    }
			    if (downloadFileLocation != null && downloadFileLocation.exists()) {
				appendToTaskOutput("Downloaded resource: " + downloadFileLocation.getAbsolutePath());
				uncopiedLinks.add(new URI[]{linksUriArray[linkCount], downloadFileLocation.toURI()});
			    } else {
				resourceCopyOutput.append("Download failed: " + currentLink + " \n");
				fileCopyErrors.add(currentRetrievableFile.sourceURI);
				uncopiedLinks.add(new URI[]{linksUriArray[linkCount], linksUriArray[linkCount]});
				resourceCopyErrors++;
			    }
			    resourceCopyOutput.setCaretPosition(resourceCopyOutput.getText().length() - 1);
			}
		    }
		}
	    }

	    private void testFreeSpace() {
		try {
		    int freeGBytes = (int) (directoryForSizeTest.getFreeSpace() / 1073741824);
		    diskSpaceLabel.setText(diskFreeLabelText + freeGBytes + "GB");
		    if (freeGbWarningPoint > freeGBytes) {
			progressBar.setIndeterminate(false);
			if (JOptionPane.YES_OPTION == ArbilWindowManager.getSingleInstance().showDialogBox("There is only " + freeGBytes + "GB free space left on the disk.\nTo you still want to continue?", searchDialog.getTitle(), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE)) {
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
	};
    }
///////////////////////////////////////////
// end functions that create the threads //
///////////////////////////////////////////

    private class RetrievableFile {

	public RetrievableFile(URI sourceURILocal, File destinationDirectoryLocal) {
	    sourceURI = sourceURILocal;
	    destinationDirectory = destinationDirectoryLocal;
	}

	private String makeFileNameLamusFriendly(String fileNameString) {
	    String friendlyFileName = fileNameString.replaceAll("[^A-Za-z0-9-]", "_");
	    friendlyFileName = friendlyFileName.replaceAll("__+", "_");
	    return friendlyFileName;
	}

	public void calculateUriFileName() {
	    if (destinationDirectory != null) {
		destinationFile = ArbilSessionStorage.getSingleInstance().getExportPath(sourceURI.toString(), destinationDirectory.getPath());
	    } else {
		destinationFile = ArbilSessionStorage.getSingleInstance().getSaveLocation(sourceURI.toString());
	    }
	    childDestinationDirectory = destinationDirectory;
	}

	public void calculateTreeFileName(boolean lamusFriendly) {
	    fileSuffix = sourceURI.toString().substring(sourceURI.toString().lastIndexOf("."));
	    ArbilDataNode currentNode = dataNodeLoader.getArbilDataNode(null, sourceURI);
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
	File destinationDirectory;
	File childDestinationDirectory;
	File destinationFile;
	String fileSuffix;
    }
}
