/**
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.arbil.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.BorderFactory;
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
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.data.CopyRunner;
import nl.mpi.arbil.data.DataNodeLoader;
import nl.mpi.arbil.data.importexport.ShibbolethNegotiator;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.DownloadAbortFlag;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.TreeHelper;
import nl.mpi.arbil.util.WindowManager;

/**
 * Document : ImportExportDialog Created on :
 *
 * @author Peter.Withers@mpi.nl
 */
public class ImportExportDialog implements ImportExportUI {

    private final static String progressFoundLabelText = "Total Metadata Files Found: ";
    private final static String progressProcessedLabelText = "Total Metadata Files Processed: ";
    private final static String progressAlreadyInCacheLabelText = "Metadata Files already in Local Corpus: ";
    private final static String progressFailedLabelText = "Metadata File Copy Errors: ";
    private final static String progressXmlErrorsLabelText = "Metadata File Validation Errors: ";
    private final static String resourceCopyErrorsLabelText = "Resource File Copy Errors: ";
    
    final private JDialog importExportDialog;
    final private JPanel importExportPanel;
    private JPanel inputNodePanel;
    private JPanel outputNodePanel;
    private JCheckBox copyFilesExportCheckBox;
    private JCheckBox copyFilesImportCheckBox;
    private JCheckBox renameFileToNodeName;
    private JCheckBox renameFileToLamusFriendlyName;
    private JButton showMoreButton;
    private JButton showDetailsButton;
    private JCheckBox overwriteCheckBox;
    private JCheckBox shibbolethCheckBox;
    private JPanel shibbolethPanel;
//    private JProgressBar resourceProgressBar;
    private JLabel resourceProgressLabel;
    private JProgressBar progressBar;
    private JLabel diskSpaceLabel;
    private JPanel moreOptionsPanel;
    private JPanel detailsPanel;
    private JPanel detailsBottomPanel;
    private JLabel progressFoundLabel;
    private JLabel progressProcessedLabel;
    private JLabel progressAlreadyInCacheLabel;
    private JLabel progressFailedLabel;
    private JLabel progressXmlErrorsLabel;
    private JLabel resourceCopyErrorsLabel;
    private JButton showInTableButton;
    private JButton closeButton;
    private JButton stopButton;
    private JButton startButton;
    private JTabbedPane detailsTabPane;
    private JTextArea taskOutput;
    private JTextArea xmlOutput;
    private JTextArea resourceCopyOutput;
    // variables used but the search thread
    // variables used by the copy thread
    // variables used by all threads
    private boolean stopCopy = false;
    private Vector<ArbilDataNode> selectedNodes;
    private ArbilDataNode destinationNode = null;
    private File exportDestinationDirectory = null;
    private DownloadAbortFlag downloadAbortFlag = new DownloadAbortFlag();
    private ShibbolethNegotiator shibbolethNegotiator = null;
    private Vector<URI> validationErrors = new Vector<URI>();
    private Vector<URI> metaDataCopyErrors = new Vector<URI>();
    private Vector<URI> fileCopyErrors = new Vector<URI>();
    private boolean showingMoreOptions = false;
    private boolean showingDetails = false;

    private static TreeHelper treeHelper;
    public static void setTreeHelper(TreeHelper treeHelperInstance) {
	treeHelper = treeHelperInstance;
    }
    private static DataNodeLoader dataNodeLoader;

    public static void setDataNodeLoader(DataNodeLoader dataNodeLoaderInstance) {
	dataNodeLoader = dataNodeLoaderInstance;
    }
    private static SessionStorage sessionStorage;

    public static void setSessionStorage(SessionStorage sessionStorageInstance) {
	sessionStorage = sessionStorageInstance;
    }
    private static WindowManager windowManager;

    public static void setWindowManager(WindowManager windowManagerInstance) {
	windowManager = windowManagerInstance;
    }
    private static MessageDialogHandler dialogHandler;

    public static void setMessageDialogHandler(MessageDialogHandler dialogHandlerInstance) {
	dialogHandler = dialogHandlerInstance;
    }

    public ImportExportDialog(Component targetComponent) throws Exception {
	dialogHandler.offerUserToSaveChanges();

	importExportPanel = new JPanel();
	importExportPanel.setLayout(new BorderLayout());

	importExportPanel.add(createInOutNodePanel(), BorderLayout.NORTH);

	JPanel optionsPanel = new JPanel(new BorderLayout());
	optionsPanel.add(createOptionsPanel(), BorderLayout.CENTER);
	optionsPanel.add(createMoreOptionsPanel(), BorderLayout.SOUTH);
	importExportPanel.add(optionsPanel, BorderLayout.CENTER);

	JPanel dialogBottomPanel = new JPanel(new BorderLayout());
	dialogBottomPanel.add(createStartStopButtonsPanel(), BorderLayout.WEST);
	dialogBottomPanel.add(createDetailsPanel(), BorderLayout.SOUTH);
	importExportPanel.add(dialogBottomPanel, BorderLayout.SOUTH);

	importExportDialog = new JDialog(JOptionPane.getFrameForComponent(windowManager.getMainFrame()), true);
	importExportDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	importExportDialog.addWindowStateListener(new WindowAdapter() {
	    @Override
	    public void windowStateChanged(WindowEvent e) {
		if ((e.getNewState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
		    updateDialog(true, true);
		} else {
		    importExportDialog.pack();
		}
	    }
	});
	importExportDialog.addWindowListener(new WindowAdapter() {
	    @Override
	    public void windowClosing(WindowEvent e) {
		setStopCopy(true);
		downloadAbortFlag.abortDownload = true;
	    }
	});
	importExportDialog.getContentPane().setLayout(new BorderLayout());
	importExportDialog.add(importExportPanel, BorderLayout.CENTER);
	importExportDialog.setLocationRelativeTo(targetComponent);
	importExportDialog.setResizable(false);

	updateDialog(showingMoreOptions, showingDetails); // updateDialog no longer calls pack()

	JPanel closeButtonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
	closeButton = new JButton("Close");
	closeButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		importExportDialog.dispose();
	    }
	});
	closeButtonPanel.add(closeButton);
	closeButtonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));
	importExportDialog.add(closeButtonPanel, BorderLayout.SOUTH);

	importExportDialog.pack();
    }

    private JPanel createOptionsPanel() {
	overwriteCheckBox = new JCheckBox("Overwrite Local Changes", false);
	overwriteCheckBox.setToolTipText("If checked, after import the local version will be an exact copy of the remote version and any local changes will be overwritten. If not checked, previous local changes will remain.");
	copyFilesExportCheckBox = new JCheckBox("Export Resource Files (if available)", false);

	JPanel optionsPanel = new JPanel();
	optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.PAGE_AXIS));
	overwriteCheckBox.setAlignmentX(0);
	optionsPanel.add(overwriteCheckBox);
	copyFilesExportCheckBox.setAlignmentX(0);
	optionsPanel.add(copyFilesExportCheckBox);
	optionsPanel.setAlignmentX(0);
	return optionsPanel;
    }

    private JPanel createMoreOptionsPanel() {
	moreOptionsPanel = new JPanel();

	JPanel moreOptionsButtonPanel = new JPanel(new BorderLayout());
	showMoreButton = new JButton("");
	showMoreButton.setToolTipText("Show/hide additional options");
	showMoreButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		try {
		    updateDialog(!showingMoreOptions, showingDetails);
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	moreOptionsButtonPanel.add(showMoreButton, BorderLayout.WEST);

	moreOptionsPanel.setLayout(new BorderLayout());

	// NOTE TG 11/4/2011: In ticket #679 it was decided to disable shibboleth authentication until the entire chain is functional.
	// This requires some work on the server.
	shibbolethCheckBox.setEnabled(false);

	shibbolethPanel = new JPanel();

	shibbolethCheckBox.setVisible(false);
	shibbolethPanel.setVisible(false);

	shibbolethCheckBox.addActionListener(
		new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			if (shibbolethCheckBox.isSelected()) {
			    if (getShibbolethNegotiator() == null) {
				shibbolethNegotiator = new ShibbolethNegotiator();
			    }
			    shibbolethPanel.add(getShibbolethNegotiator().getControlls());
			} else {
			    shibbolethPanel.removeAll();
			    shibbolethNegotiator = null;
			}
			importExportDialog.pack();
		    }
		});
	copyFilesImportCheckBox.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		shibbolethCheckBox.setVisible(copyFilesImportCheckBox.isSelected());
		shibbolethPanel.setVisible(copyFilesImportCheckBox.isSelected());
		importExportDialog.pack();
	    }
	});

//        JPanel copyFilesCheckBoxPanel = new JPanel();
//        copyFilesCheckBoxPanel.setLayout(new BoxLayout(copyFilesCheckBoxPanel, BoxLayout.X_AXIS));
//        copyFilesCheckBoxPanel.add(copyFilesImportCheckBox);
//        copyFilesCheckBoxPanel.add(new JPanel());
//        detailsPanel.add(copyFilesCheckBoxPanel, BorderLayout.NORTH);

	JPanel moreOptionsTopPanel = new JPanel();
	moreOptionsTopPanel.setLayout(new BoxLayout(moreOptionsTopPanel, BoxLayout.PAGE_AXIS));
	JPanel moreOptionsTopCheckBoxPanel = new JPanel();
	moreOptionsTopCheckBoxPanel.setLayout(new BoxLayout(moreOptionsTopCheckBoxPanel, BoxLayout.PAGE_AXIS));

	moreOptionsTopCheckBoxPanel.add(renameFileToNodeName);
	moreOptionsTopCheckBoxPanel.add(renameFileToLamusFriendlyName);
	moreOptionsTopCheckBoxPanel.add(copyFilesImportCheckBox);
	moreOptionsTopCheckBoxPanel.add(shibbolethCheckBox);

	JPanel paddingPanel = new JPanel();
	paddingPanel.setLayout(new BoxLayout(paddingPanel, BoxLayout.LINE_AXIS));
	JPanel leftPadding = new JPanel();
	leftPadding.setMaximumSize(new Dimension(500, 100));
	paddingPanel.add(leftPadding);
	paddingPanel.add(moreOptionsTopCheckBoxPanel);
	paddingPanel.add(new JPanel());
	moreOptionsTopPanel.add(paddingPanel);
	moreOptionsTopPanel.add(shibbolethPanel);
	moreOptionsPanel.add(moreOptionsTopPanel, BorderLayout.NORTH);

	JPanel moreOptionsContainerPanel = new JPanel();
	moreOptionsContainerPanel.setLayout(new BoxLayout(moreOptionsContainerPanel, BoxLayout.PAGE_AXIS));
	moreOptionsButtonPanel.setAlignmentX(0);
	moreOptionsContainerPanel.add(moreOptionsButtonPanel);
	moreOptionsPanel.setAlignmentX(0);
	moreOptionsContainerPanel.add(moreOptionsPanel);

	moreOptionsContainerPanel.setAlignmentX(0);
	return moreOptionsContainerPanel;
    }

    private JPanel createDetailsPanel() {
	detailsPanel = new JPanel();
	detailsPanel.setLayout(new BorderLayout());

	detailsTabPane = new JTabbedPane();

	taskOutput = new JTextArea(5, 20);
	taskOutput.setMargin(new Insets(5, 5, 5, 5));
	taskOutput.setEditable(false);
	taskOutput.append("The details of the import / export process will be displayed here.\n");
	detailsTabPane.add("Process Details", new JScrollPane(taskOutput));

	xmlOutput = new JTextArea(5, 20);
	xmlOutput.setMargin(new Insets(5, 5, 5, 5));
	xmlOutput.setEditable(false);
	xmlOutput.append("When the metadata files are imported or exported they will be validated (for XML schema conformance) and any errors will be reported here.\n");
	detailsTabPane.add("Validation Errors", new JScrollPane(xmlOutput));

	resourceCopyOutput = new JTextArea(5, 20);
	resourceCopyOutput.setMargin(new Insets(5, 5, 5, 5));
	resourceCopyOutput.setEditable(false);
	resourceCopyOutput.append("If copying of resource files is selected, any file copy errors will be reported here.\n");
	detailsTabPane.add("Resource Copy Errors", new JScrollPane(resourceCopyOutput));

	detailsPanel.add(detailsTabPane, BorderLayout.CENTER);

	detailsBottomPanel = new JPanel();
	detailsBottomPanel.setLayout(new BoxLayout(detailsBottomPanel, BoxLayout.PAGE_AXIS));

	progressFoundLabel = new JLabel(progressFoundLabelText);
	progressProcessedLabel = new JLabel(progressProcessedLabelText);
	progressAlreadyInCacheLabel = new JLabel(progressAlreadyInCacheLabelText);
	progressFailedLabel = new JLabel(progressFailedLabelText);
	progressXmlErrorsLabel = new JLabel(progressXmlErrorsLabelText);
	resourceCopyErrorsLabel = new JLabel(resourceCopyErrorsLabelText);
	showInTableButton = new JButton("Show errors in table");
	showInTableButton.setEnabled(false);
	showInTableButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		try {
		    if (metaDataCopyErrors.size() > 0) {
			windowManager.openFloatingTableOnce(metaDataCopyErrors.toArray(new URI[]{}), progressFailedLabelText);
		    }
		    if (validationErrors.size() > 0) {
			windowManager.openAllChildNodesInFloatingTableOnce(validationErrors.toArray(new URI[]{}), progressXmlErrorsLabelText);
		    }
		    if (fileCopyErrors.size() > 0) {
			AbstractArbilTableModel resourceFileErrorsTable = windowManager.openFloatingTableOnceGetModel(fileCopyErrors.toArray(new URI[]{}), resourceCopyErrorsLabelText);
			//resourceFileErrorsTable.getFieldView().
			resourceFileErrorsTable.addChildTypeToDisplay("MediaFiles");
			resourceFileErrorsTable.addChildTypeToDisplay("WrittenResources");
		    }
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	diskSpaceLabel = new JLabel(CopyRunner.DISK_FREE_LABEL_TEXT);

	progressAlreadyInCacheLabel.setForeground(Color.darkGray);
	progressFailedLabel.setForeground(Color.red);
	progressXmlErrorsLabel.setForeground(Color.red);
	resourceCopyErrorsLabel.setForeground(Color.red);

	//bottomPanel.add(new SaveCurrentSettingsPanel(this, null));
	detailsBottomPanel.add(progressFoundLabel);
	detailsBottomPanel.add(progressProcessedLabel);
	detailsBottomPanel.add(progressAlreadyInCacheLabel);
	detailsBottomPanel.add(progressFailedLabel);
	detailsBottomPanel.add(progressXmlErrorsLabel);
	detailsBottomPanel.add(resourceCopyErrorsLabel);
	detailsBottomPanel.add(showInTableButton);
	detailsBottomPanel.add(diskSpaceLabel);

	resourceProgressLabel = new JLabel(" ");
	detailsBottomPanel.add(resourceProgressLabel);
	detailsPanel.add(detailsBottomPanel, BorderLayout.SOUTH);

	JPanel paddingPanel = new JPanel();
	paddingPanel.setLayout(new BoxLayout(paddingPanel, BoxLayout.LINE_AXIS));
	JPanel leftPadding = new JPanel();
	leftPadding.setMaximumSize(new Dimension(500, 100));
	paddingPanel.add(leftPadding);
	paddingPanel.add(detailsPanel);
	paddingPanel.add(new JPanel());

	paddingPanel.setMinimumSize(new Dimension(800, 200));

	return paddingPanel;
    }

    private JPanel createInOutNodePanel() {
	JPanel inOutNodePanel = new JPanel();
	inOutNodePanel.setLayout(new BoxLayout(inOutNodePanel, BoxLayout.PAGE_AXIS));

	JPanel inputNodeLabelPanel = new JPanel();
	inputNodeLabelPanel.setLayout(new BorderLayout());
	inputNodePanel = new JPanel();
	inputNodePanel.setLayout(new java.awt.GridLayout());
	inputNodeLabelPanel.add(new JLabel("From: "), BorderLayout.LINE_START);
	inputNodeLabelPanel.add(inputNodePanel, BorderLayout.CENTER);
	inputNodeLabelPanel.setAlignmentX(0);
	inOutNodePanel.add(inputNodeLabelPanel);

	JPanel outputNodeLabelPanel = new JPanel();
	outputNodeLabelPanel.setLayout(new BorderLayout());
	outputNodePanel = new JPanel();
	outputNodePanel.setLayout(new java.awt.GridLayout());
	outputNodeLabelPanel.add(new JLabel("To: "), BorderLayout.LINE_START);
	outputNodeLabelPanel.add(outputNodePanel, BorderLayout.CENTER);
	outputNodeLabelPanel.setAlignmentX(0);
	inOutNodePanel.add(outputNodeLabelPanel);

	copyFilesImportCheckBox = new JCheckBox("Import Resource Files (if available)", false);
	renameFileToNodeName = new JCheckBox("Rename Metadata Files (to match local corpus tree names)", true);
	renameFileToLamusFriendlyName = new JCheckBox("Limit Characters in File Names (LAMUS friendly format)", true);
	shibbolethCheckBox = new JCheckBox("Shibboleth authentication via the SURFnet method", false);

	inOutNodePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 0));
	return inOutNodePanel;
    }

    private JPanel createStartStopButtonsPanel() {
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
	startButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		try {
		    performCopy();
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	stopButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		try {
		    setStopCopy(true);
		    downloadAbortFlag.abortDownload = true;
		    stopButton.setEnabled(false);
		    startButton.setEnabled(false);
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});

	showDetailsButton = new JButton("");
	buttonsPanel.add(showDetailsButton);
	showDetailsButton.setToolTipText("Show/hide detailed information");
	showDetailsButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		try {
		    updateDialog(showingMoreOptions, !showingDetails);
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});

	buttonsPanel.setAlignmentX(0);
	return buttonsPanel;
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
	File[] selectedFiles = dialogHandler.showMetadataFileSelectBox("Import", true);
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
	importExportDialog.setTitle("Export Branch");
	File destinationDirectory = dialogHandler.showEmptyExportDirectoryDialogue(importExportDialog.getTitle());
	if (destinationDirectory != null) {
	    exportFromCache(new Vector(Arrays.asList(localCorpusSelectedNodes)), destinationDirectory);
	}
    }

    private void exportFromCache(Vector localSelectedNodes, File destinationDirectory) {
	selectedNodes = localSelectedNodes;
//        searchDialog.setTitle("Export Branch");
	if (!selectedNodesContainDataNode()) {
	    dialogHandler.addMessageDialogToQueue("No relevant nodes are selected", importExportDialog.getTitle());
	    return;
	}
	setNodesPanel(selectedNodes, inputNodePanel);
	setLocalFileToNodesPanel(outputNodePanel, destinationDirectory);
	//String mirrorNameString = JOptionPane.showInputDialog(destinationComp, "Enter a tile for the local mirror");

	exportDestinationDirectory = destinationDirectory;
	updateDialog(showingMoreOptions, showingDetails);
	importExportDialog.setVisible(true);
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
	importExportDialog.setTitle("Import Branch");
	if (!selectedNodesContainDataNode()) {
	    dialogHandler.addMessageDialogToQueue("No relevant nodes are selected", importExportDialog.getTitle());
	    return;
	}
	setNodesPanel(selectedNodes, inputNodePanel);
	if (destinationNode == null) {
	    setLocalCacheToNodesPanel(outputNodePanel);
	}
	importExportDialog.setVisible(true);
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

    private synchronized void updateDialog(boolean optionsFlag, boolean detailsFlag) {
	overwriteCheckBox.setVisible(exportDestinationDirectory == null);
	copyFilesImportCheckBox.setVisible(exportDestinationDirectory == null);
	copyFilesExportCheckBox.setVisible(exportDestinationDirectory != null);

	// showMoreFlag is false the first time this is called when the dialog is initialised so we need to make sure that pack gets called in this case
	// otherwise try to prevent chenging the window size when not required

	if (!optionsFlag || !detailsFlag || showingMoreOptions != optionsFlag || showingDetails != detailsFlag) {
	    detailsTabPane.setVisible(detailsFlag);
	    detailsBottomPanel.setVisible(detailsFlag);

	    /**
	     * Advanced options *
	     */
	    copyFilesImportCheckBox.setVisible(optionsFlag && exportDestinationDirectory == null); // Only for import
	    renameFileToNodeName.setVisible(optionsFlag && exportDestinationDirectory != null); // Only for export
	    renameFileToLamusFriendlyName.setVisible(optionsFlag && exportDestinationDirectory != null); // Only for export
	    shibbolethCheckBox.setVisible(optionsFlag && copyFilesImportCheckBox.isSelected());
	    shibbolethPanel.setVisible(optionsFlag && copyFilesImportCheckBox.isSelected());

	    if (detailsFlag) {
		importExportDialog.setMinimumSize(new Dimension(500, 500));
	    } else {
		importExportDialog.setMinimumSize(null);
	    }

	    showMoreButton.setText(optionsFlag ? "< < Fewer options" : "More options> >");
	    showDetailsButton.setText(detailsFlag ? "< < Hide details" : "Details > >");
	    showingMoreOptions = optionsFlag;
	    showingDetails = detailsFlag;
	    importExportDialog.pack();
	}
    }

    private void performCopy() {
	setUItoRunningState();

	final CopyRunner copyRunner = new CopyRunner(this, sessionStorage, dataNodeLoader, treeHelper);
	new Thread(copyRunner, "performCopy").start();
    }

    @Override
    public void appendToTaskOutput(final String lineOfText) {

	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		taskOutput.append(lineOfText + "\n");
		taskOutput.setCaretPosition(taskOutput.getText().length());
	    }
	});
    }

    private void setUItoRunningState() {
	stopButton.setEnabled(true);
	startButton.setEnabled(false);
	closeButton.setEnabled(false);
	showMoreButton.setEnabled(false);
	showInTableButton.setEnabled(false);
	overwriteCheckBox.setEnabled(false);
	copyFilesExportCheckBox.setEnabled(false);
	copyFilesImportCheckBox.setEnabled(false);
	taskOutput.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	importExportDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    public void setUItoStoppedState() {
	Toolkit.getDefaultToolkit().beep();
	taskOutput.setCursor(null);
	importExportDialog.setCursor(null); //turn off the wait cursor
	//appendToTaskOutput("Done!");
	progressBar.setIndeterminate(false);
//        resourceProgressBar.setIndeterminate(false);
	resourceProgressLabel.setText(" ");
//        progressLabel.setText("");
	stopButton.setEnabled(false);
	startButton.setEnabled(selectedNodes.size() > 0);
	closeButton.setEnabled(true);
	showMoreButton.setEnabled(true);
	showInTableButton.setEnabled(validationErrors.size() > 0 || metaDataCopyErrors.size() > 0 || fileCopyErrors.size() > 0);
	overwriteCheckBox.setEnabled(true);
	copyFilesExportCheckBox.setEnabled(true);
	copyFilesImportCheckBox.setEnabled(true);

	// TODO: add a close button?
	stopCopy = false;
	downloadAbortFlag.abortDownload = false;
    }
    /////////////////////////////////////
    // functions called by the threads //
    /////////////////////////////////////

    public void waitTillVisible() {
	// this is to prevent deadlocks between the thread starting before the dialog is showing which causes the JTextArea to appear without the frame
	while (!importExportDialog.isVisible()) {
	    try {
		Thread.sleep(100);
	    } catch (InterruptedException ignore) {
		BugCatcherManager.getBugCatcher().logError(ignore);
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
//			BugCatcherManager.getBugCatcher().logError(new Exception("Error while renaming file"));
//		    }
//		}
//	    } catch (Exception ex) {
//		BugCatcherManager.getBugCatcher().logError(ex);
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

    @Override
    public boolean isCopyFilesOnImport() {
	return copyFilesImportCheckBox.isSelected();
    }

    @Override
    public boolean isCopyFilesOnExport() {
	return copyFilesExportCheckBox.isSelected();
    }

    @Override
    public boolean isRenameFileToNodeName() {
	return renameFileToNodeName.isSelected();
    }

    @Override
    public boolean isRenameFileToLamusFriendlyName() {
	return renameFileToLamusFriendlyName.isSelected();
    }

    @Override
    public boolean isOverwrite() {
	return overwriteCheckBox.isSelected();
    }

    @Override
    public void appendToResourceCopyOutput(final String text) {
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		resourceCopyOutput.append(text);
		resourceCopyOutput.setCaretPosition(resourceCopyOutput.getText().length() - 1);
	    }
	});
    }

    @Override
    public void appendToXmlOutput(final String text) {
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		xmlOutput.append(text);
		xmlOutput.setCaretPosition(xmlOutput.getText().length() - 1);
	    }
	});
    }

    @Override
    public void addToValidationErrors(URI uri) {
	validationErrors.add(uri);
    }

    @Override
    public void addToMetadataCopyErrors(URI uri) {
	metaDataCopyErrors.add(uri);
    }

    @Override
    public void addToFileCopyErrors(URI uri) {
	fileCopyErrors.add(uri);
    }

    @Override
    public void setDiskspaceState(String text) {
	diskSpaceLabel.setText(text);
    }

    @Override
    public File getExportDestinationDirectory() {
	return exportDestinationDirectory;
    }

    @Override
    public DownloadAbortFlag getDownloadAbortFlag() {
	return downloadAbortFlag;
    }

    @Override
    public ShibbolethNegotiator getShibbolethNegotiator() {
	return shibbolethNegotiator;
    }

    @Override
    public Enumeration<ArbilDataNode> getSelectedNodesEnumeration() {
	return selectedNodes.elements();
    }

    @Override
    public void removeNodeSelection() {
	selectedNodes.removeAllElements();
    }

    @Override
    public synchronized boolean isStopCopy() {
	return stopCopy;
    }

    @Override
    public synchronized void setStopCopy(boolean stopCopy) {
	this.stopCopy = stopCopy;
    }

    @Override
    public ArbilDataNode getDestinationNode() {
	return destinationNode;
    }

    @Override
    public void setProgressIndeterminate(final boolean indeterminate) {

	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		progressBar.setIndeterminate(indeterminate);
	    }
	});
    }

    @Override
    public void setProgressText(final String text) {
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		resourceProgressLabel.setText(text);
	    }
	});
    }

    @Override
    public boolean askContinue(String message) {
	return JOptionPane.YES_OPTION == dialogHandler.showDialogBox(message, importExportDialog.getTitle(), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
    }

    @Override
    public void onCopyStart() {
	waitTillVisible();
    }

    @Override
    public void onCopyEnd(final String finalMessage) throws HeadlessException {

	System.out.println("finalMessageString: " + finalMessage);
	final Object[] options = {"Close", "Details"};

	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		setUItoStoppedState();

		int detailsOption = JOptionPane.showOptionDialog(windowManager.getMainFrame(), finalMessage, importExportDialog.getTitle(), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (detailsOption == 0) {
		    importExportDialog.dispose();
		} else {
		    if (!showingDetails) {
			updateDialog(showingMoreOptions, true);
			importExportDialog.pack();
		    }
		}
		if (getExportDestinationDirectory() != null) {
		    windowManager.openFileInExternalApplication(getExportDestinationDirectory().toURI());
		}
	    }
	});
    }

    @Override
    public void updateStatus(final int getCount, final int totalLoaded, final int totalExisting, final int totalErrors, final int xsdErrors, final int resourceCopyErrors) {
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		progressFoundLabel.setText(progressFoundLabelText + (getCount + totalLoaded));
		progressProcessedLabel.setText(progressProcessedLabelText + totalLoaded);
		progressAlreadyInCacheLabel.setText(progressAlreadyInCacheLabelText + totalExisting);
		progressFailedLabel.setText(progressFailedLabelText + totalErrors);
		progressXmlErrorsLabel.setText(progressXmlErrorsLabelText + xsdErrors);
		resourceCopyErrorsLabel.setText(resourceCopyErrorsLabelText + resourceCopyErrors);
		progressBar.setString(totalLoaded + "/" + (getCount + totalLoaded) + " (" + (totalErrors + xsdErrors + resourceCopyErrors) + " errors)");
	    }
	});
    }
}
