package nl.mpi.arbil.ui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import nl.mpi.arbil.ArbilIcons;
import nl.mpi.arbil.ArbilMetadataException;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeService;
import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.data.ArbilTreeHelper;
import nl.mpi.arbil.data.DataNodeLoader;
import nl.mpi.arbil.data.MetadataBuilder;
import nl.mpi.arbil.data.importexport.ArbilCsvImporter;
import nl.mpi.arbil.ui.ImportExportDialog;
import nl.mpi.arbil.templates.ArbilTemplate;
import nl.mpi.arbil.templates.ArbilTemplateManager;
import nl.mpi.arbil.templates.ArbilTemplateManager.MenuItemData;
import nl.mpi.arbil.templates.ArbilFavourites;
import nl.mpi.arbil.ui.ArbilTree;
import nl.mpi.arbil.ui.ArbilTreePanels;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.ui.fieldeditors.ArbilLongFieldEditor;
import nl.mpi.arbil.util.XsdChecker;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.TreeHelper;
import nl.mpi.arbil.util.WindowManager;

/**
 * Context menu for tree UI components
 * 
 * @author Twan Goosen
 */
public class TreeContextMenu extends ArbilContextMenu {

    private static SessionStorage sessionStorage;

    public static void setSessionStorage(SessionStorage sessionStorageInstance) {
	sessionStorage = sessionStorageInstance;
    }
    private static TreeHelper treeHelper;

    public static void setTreeHelper(TreeHelper treeHelperInstance) {
	treeHelper = treeHelperInstance;
    }
    private static WindowManager windowManager;

    public static void setWindowManager(WindowManager windowManagerInstance) {
	windowManager = windowManagerInstance;
    }
    private static MessageDialogHandler dialogHandler;

    public static void setMessageDialogHandler(MessageDialogHandler dialogHandlerInstance) {
	dialogHandler = dialogHandlerInstance;
    }
    private static DataNodeLoader dataNodeLoader;

    public static void setDataNodeLoader(DataNodeLoader dataNodeLoaderInstance) {
	dataNodeLoader = dataNodeLoaderInstance;
    }

    public TreeContextMenu(ArbilTree tree) {
	super();
	this.tree = tree;
	setInvoker(tree);

	selectedTreeNodes = tree.getSelectedNodes();
	leadSelectedTreeNode = tree.getLeadSelectionDataNode();
    }

    @Override
    protected void setUpMenu() {
	setUpItems();
	setUpActions();
    }

    private void setUpItems() {
	int nodeLevel = -1;
	int selectionCount = 0;
	boolean showRemoveLocationsTasks = false;
	boolean showAddLocationsTasks = false;
	selectionCount = tree.getSelectionCount();
	if (selectionCount > 0) {
	    nodeLevel = tree.getSelectionPath().getPathCount();
	}

	showRemoveLocationsTasks = (selectionCount == 1 && nodeLevel == 2) || selectionCount > 1;
	showAddLocationsTasks = selectionCount == 1 && nodeLevel == 1;

	viewSelectedNodesMenuItem.setText("View Selected");
	viewSelectedSubnodesMenuItem.setText(leadSelectedTreeNode != null && leadSelectedTreeNode.isEditable() ? "Edit all Metadata" : "View all Metadata");
	editInLongFieldEditor.setText(leadSelectedTreeNode != null && leadSelectedTreeNode.getParentDomNode().isEditable() ? "Edit in Long Field Editor" : "View in Long Field Editor");
//        mergeWithFavouritesMenu.setEnabled(false);
	deleteMenuItem.setEnabled(true);

	if (getTreePanel() != null) {
	    if (tree == getTreePanel().remoteCorpusTree) {
		removeRemoteCorpusMenuItem.setVisible(showRemoveLocationsTasks);
		addRemoteCorpusMenuItem.setVisible(showAddLocationsTasks);
		copyBranchMenuItem.setVisible(selectionCount > 0 && nodeLevel > 1);
		addDefaultLocationsMenuItem.setVisible(showAddLocationsTasks);
		searchRemoteBranchMenuItem.setVisible(selectionCount > 0 && nodeLevel > 1 && !leadSelectedTreeNode.isCmdiMetaDataNode());
	    }
	    if (tree == getTreePanel().localCorpusTree) {
		viewSelectedNodesMenuItem.setText("View/Edit Selected");
		//removeCachedCopyMenuItem.setVisible(showRemoveLocationsTasks);
		pasteMenuItem1.setVisible(selectionCount > 0 && nodeLevel > 1);
		searchSubnodesMenuItem.setVisible(selectionCount > 0);
		// a corpus can be added even at the root node
		addMenu.setVisible(selectionCount == 1); // && /*nodeLevel > 1 &&*/ treeHelper.arbilTreePanel.localCorpusTree.getSelectionCount() > 0/* && ((DefaultMutableTreeNode)localCorpusTree.getSelectionPath().getLastPathComponent()).getUserObject() instanceof */); // could check for imdi childnodes
//            addMenu.setEnabled(nodeLevel > 1); // not yet functional so lets dissable it for now
//            addMenu.setToolTipText("test balloon on dissabled menu item");
		deleteMenuItem.setVisible(nodeLevel > 1);
		boolean nodeIsChild = false;
		if (leadSelectedTreeNode != null) {
		    nodeIsChild = leadSelectedTreeNode.isChildNode();

		    validateMenuItem.setVisible(!nodeIsChild);
		    historyMenu.setVisible(leadSelectedTreeNode.hasHistory());
		    exportMenuItem.setVisible(!nodeIsChild);
		    importCsvMenuItem.setVisible(leadSelectedTreeNode.isCorpus());
		    importBranchMenuItem.setVisible(leadSelectedTreeNode.isCorpus());
		    reImportBranchMenuItem.setVisible(leadSelectedTreeNode.archiveHandle != null && !leadSelectedTreeNode.isChildNode());
		}
		// set up the favourites menu
		addFromFavouritesMenu.setVisible(true);
	    }
	    if (tree == getTreePanel().localDirectoryTree) {
		removeLocalDirectoryMenuItem.setVisible(showRemoveLocationsTasks);
		if (showAddLocationsTasks) {
		    showHiddenFilesMenuItem.setState(treeHelper.isShowHiddenFilesInTree());
		    showHiddenFilesMenuItem.setVisible(true);
		}
		addLocalDirectoryMenuItem.setVisible(showAddLocationsTasks);
		if (leadSelectedTreeNode != null) {
		    copyBranchMenuItem.setVisible(leadSelectedTreeNode.isCorpus() || leadSelectedTreeNode.isSession());
		}
	    }
	}


	if (leadSelectedTreeNode != null) {

	    if (leadSelectedTreeNode.canHaveResource()) {
		setManualResourceLocationMenuItem.setVisible(true);
	    }

	    if (leadSelectedTreeNode.isFavorite()) {
		boolean isFavouriteTopLevel = treeHelper.isInFavouritesNodes(leadSelectedTreeNode);
		addToFavouritesMenuItem.setVisible(false);
		removeFromFavouritesMenuItem.setVisible(isFavouriteTopLevel);
		removeFromFavouritesMenuItem.setEnabled(isFavouriteTopLevel);

		addMenu.setVisible(selectedTreeNodes.length == 1);// for now adding is limited to single node selections
		viewSelectedNodesMenuItem.setText("View/Edit Selected");
		// for now deleting is limited to single node selections, to prevent top level favourites for being deleted in multi-selections
		deleteMenuItem.setVisible(!isFavouriteTopLevel && selectedTreeNodes.length == 1);
		deleteMenuItem.setEnabled(!isFavouriteTopLevel && selectedTreeNodes.length == 1);
	    } else { // Nodes that are not favourites
		removeFromFavouritesMenuItem.setVisible(false);
		addToFavouritesMenuItem.setVisible(leadSelectedTreeNode.isMetaDataNode());
		addToFavouritesMenuItem.setEnabled(!leadSelectedTreeNode.isCorpus() && leadSelectedTreeNode.isMetaDataNode());
	    }
	} else {
	    addToFavouritesMenuItem.setVisible(false);
	}

	ArbilDataNode[] selectedNodes = tree.getSelectedNodes();

	copyNodeUrlMenuItem.setVisible((selectionCount == 1 && nodeLevel > 1) || selectionCount > 1);
	viewSelectedNodesMenuItem.setVisible(selectionCount >= 1 && nodeLevel > 1);
	reloadSubnodesMenuItem.setVisible(selectionCount > 0 && nodeLevel > 1);
	viewSelectedSubnodesMenuItem.setVisible(selectionCount > 0 && nodeLevel > 1
		&& selectedNodes[0].isMetaDataNode() && !tree.getSelectedNodes()[0].isCorpus());
	editInLongFieldEditor.setVisible(selectionCount > 0 && nodeLevel > 1
		&& !selectedNodes[0].isEmptyMetaNode());
    }

    private void setUpActions() {
	viewSelectedNodesMenuItem.setText("View Selected");
	viewSelectedNodesMenuItem.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		viewSelectedNodes();
	    }
	});
	addItem(CATEGORY_NODE, PRIORITY_TOP, viewSelectedNodesMenuItem);

	editInLongFieldEditor.addActionListener(new ActionListener() {

	    public void actionPerformed(ActionEvent e) {
		startLongFieldEditor();
	    }
	});
	addItem(CATEGORY_NODE, PRIORITY_TOP + 1, editInLongFieldEditor);

	viewSelectedSubnodesMenuItem.addActionListener(new ActionListener() {

	    public void actionPerformed(ActionEvent e) {
		viewSelectedSubnodes();
	    }
	});
	addItem(CATEGORY_NODE, PRIORITY_TOP + 2, viewSelectedSubnodesMenuItem);

	deleteMenuItem.setText("Delete");
	deleteMenuItem.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    treeHelper.deleteNodes(getInvoker());
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	addItem(CATEGORY_EDIT, PRIORITY_TOP + 10, deleteMenuItem);

	copyNodeUrlMenuItem.setText("Copy");
	copyNodeUrlMenuItem.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    if (selectedTreeNodes == null) {
			dialogHandler.addMessageDialogToQueue("No node selected", "Copy");
		    } else {
			ArbilTree sourceTree = (ArbilTree) getInvoker();
			sourceTree.copyNodeUrlToClipboard(selectedTreeNodes);
		    }
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	addItem(CATEGORY_EDIT, PRIORITY_TOP + 15, copyNodeUrlMenuItem);

	pasteMenuItem1.setText("Paste");
	pasteMenuItem1.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    for (ArbilDataNode currentNode : selectedTreeNodes) {
			currentNode.pasteIntoNode();
		    }
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	addItem(CATEGORY_EDIT, PRIORITY_TOP + 20, pasteMenuItem1);

	searchRemoteBranchMenuItem.setText("Search Remote Corpus");
	searchRemoteBranchMenuItem.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    searchRemoteSubnodesMenuItemActionPerformed(evt);
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	addItem(CATEGORY_NODE, PRIORITY_MIDDLE, searchRemoteBranchMenuItem);

	copyBranchMenuItem.setText("Import to Local Corpus");
	copyBranchMenuItem.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    copyBranchMenuItemActionPerformed(evt);
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	addItem(CATEGORY_REMOTE_CORPUS, PRIORITY_MIDDLE, copyBranchMenuItem);

	searchSubnodesMenuItem.setText("Search");
	searchSubnodesMenuItem.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    searchSubnodesMenuItemActionPerformed(evt);
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	addItem(CATEGORY_NODE, PRIORITY_MIDDLE, searchSubnodesMenuItem);

	reloadSubnodesMenuItem.setText("Reload");
	reloadSubnodesMenuItem.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    for (ArbilDataNode currentNode : selectedTreeNodes) {
			// this reload will first clear the save is required flag then reload
			currentNode.reloadNode();
		    }
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	addItem(CATEGORY_NODE, PRIORITY_MIDDLE + 5, reloadSubnodesMenuItem);

	addMenu.setText("Add");
	addMenu.addMenuListener(new javax.swing.event.MenuListener() {

	    public void menuCanceled(javax.swing.event.MenuEvent evt) {
	    }

	    public void menuDeselected(javax.swing.event.MenuEvent evt) {
	    }

	    public void menuSelected(javax.swing.event.MenuEvent evt) {
		try {
		    initAddMenu(addMenu, leadSelectedTreeNode);
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	addItem(CATEGORY_ADD_FAVOURITES, PRIORITY_TOP, addMenu);

	addFromFavouritesMenu.setText("Add From Favourites");
	addFromFavouritesMenu.addMenuListener(new javax.swing.event.MenuListener() {

	    public void menuCanceled(javax.swing.event.MenuEvent evt) {
	    }

	    public void menuDeselected(javax.swing.event.MenuEvent evt) {
	    }

	    public void menuSelected(javax.swing.event.MenuEvent evt) {
		initAddFromFavouritesMenu();
	    }
	});
	addItem(CATEGORY_ADD_FAVOURITES, PRIORITY_MIDDLE, addFromFavouritesMenu);

	if (leadSelectedTreeNode != null && leadSelectedTreeNode.isContainerNode()) {
	    addToFavouritesMenuItem.setText("Add Children To Favourites List");
	} else {
	    addToFavouritesMenuItem.setText("Add To Favourites List");
	}
	addToFavouritesMenuItem.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    ArbilFavourites.getSingleInstance().toggleFavouritesList(((ArbilTree) getInvoker()).getSelectedNodes(), true);
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	addItem(CATEGORY_ADD_FAVOURITES, PRIORITY_MIDDLE + 5, addToFavouritesMenuItem);

	removeFromFavouritesMenuItem.setText("Remove from Favourites List");
	removeFromFavouritesMenuItem.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    ArbilFavourites.getSingleInstance().toggleFavouritesList(((ArbilTree) getInvoker()).getSelectedNodes(), false);
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	addItem(CATEGORY_ADD_FAVOURITES, PRIORITY_MIDDLE + 5, removeFromFavouritesMenuItem);

//        mergeWithFavouritesMenu.setText("Merge With Favourite");
//        mergeWithFavouritesMenu.setActionCommand("Merge With Favouurite");

	validateMenuItem.setText("Check XML Conformance");
	validateMenuItem.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    validateMenuItemActionPerformed(evt);
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	addItem(CATEGORY_XML, PRIORITY_MIDDLE, validateMenuItem);

	historyMenu.setText("History");
	historyMenu.addMenuListener(new javax.swing.event.MenuListener() {

	    public void menuCanceled(javax.swing.event.MenuEvent evt) {
	    }

	    public void menuDeselected(javax.swing.event.MenuEvent evt) {
	    }

	    public void menuSelected(javax.swing.event.MenuEvent evt) {
		try {
		    initHistoryMenu();
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	addItem(CATEGORY_EDIT, PRIORITY_BOTTOM, historyMenu);


	addRemoteCorpusMenuItem.setText("Add Remote Location");

	addRemoteCorpusMenuItem.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    addRemoteCorpusMenuItemActionPerformed(evt);
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});

	addItem(CATEGORY_REMOTE_CORPUS, PRIORITY_MIDDLE, addRemoteCorpusMenuItem);

	addDefaultLocationsMenuItem.setText("Add Default Remote Locations");
	addDefaultLocationsMenuItem.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    addDefaultLocationsMenuItemActionPerformed(evt);
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	addItem(CATEGORY_REMOTE_CORPUS, PRIORITY_MIDDLE + 5, addDefaultLocationsMenuItem);

	removeRemoteCorpusMenuItem.setText("Remove Remote Location");
	removeRemoteCorpusMenuItem.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    for (ArbilDataNode selectedNode : selectedTreeNodes) {
			treeHelper.removeLocation(selectedNode);
		    }
		    treeHelper.applyRootLocations();
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	addItem(CATEGORY_REMOTE_CORPUS, PRIORITY_MIDDLE + 10, removeRemoteCorpusMenuItem);

	removeCachedCopyMenuItem.setText("Remove Cache Link");
	removeCachedCopyMenuItem.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    removeCachedCopyMenuItemActionPerformed(evt);
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	addItem(CATEGORY_DISK, PRIORITY_BOTTOM + 5, removeCachedCopyMenuItem);

	addLocalDirectoryMenuItem.setText("Add Working Directory");

	addLocalDirectoryMenuItem.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    addLocalDirectoryMenuItemActionPerformed(evt);
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});

	addItem(CATEGORY_WORKING_DIR, PRIORITY_TOP, addLocalDirectoryMenuItem);

	showHiddenFilesMenuItem.setText("Show Hidden Files");
	showHiddenFilesMenuItem.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    treeHelper.setShowHiddenFilesInTree(showHiddenFilesMenuItem.getState());
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	addItem(CATEGORY_WORKING_DIR, PRIORITY_MIDDLE, showHiddenFilesMenuItem);

	removeLocalDirectoryMenuItem.setText("Remove Link to Directory");
	removeLocalDirectoryMenuItem.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    for (ArbilDataNode selectedNode : selectedTreeNodes) {
			treeHelper.removeLocation(selectedNode);
		    }
		    treeHelper.applyRootLocations();
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});

	addItem(CATEGORY_REMOTE_CORPUS, PRIORITY_BOTTOM, removeLocalDirectoryMenuItem);

//        viewChangesMenuItem.setText("View Changes");
//        viewChangesMenuItem.setEnabled(false);
//        viewChangesMenuItem.addActionListener(new java.awt.event.ActionListener() {
//
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                try {
//                    for (ArbilDataNode currentNode : selectedTreeNodes) {
////                        LinorgWindowManager.getSingleInstance().openDiffWindow(currentNode);
//                    }
//                } catch (Exception ex) {
//                    BugCatcherManager.getBugCatcher().logError(ex);
//                }
//            }
//        });
//        add(viewChangesMenuItem);

//        sendToServerMenuItem.setText("Send to Server");
//        sendToServerMenuItem.setEnabled(false);
//        sendToServerMenuItem.addActionListener(new java.awt.event.ActionListener() {
//
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                try {
//                    sendToServerMenuItemActionPerformed(evt);
//
//                } catch (Exception ex) {
//                    BugCatcherManager.getBugCatcher().logError(ex);
//                }
//            }
//        });
//        add(sendToServerMenuItem);

	exportMenuItem.setText("Export");
	exportMenuItem.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    ImportExportDialog importExportDialog = new ImportExportDialog(getTreePanel().remoteCorpusTree);
		    importExportDialog.selectExportDirectoryAndExport(((ArbilTree) getInvoker()).getSelectedNodes());
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	addItem(CATEGORY_DISK, PRIORITY_TOP + 5, exportMenuItem);

	importCsvMenuItem.setText("Import CSV");
	importCsvMenuItem.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    ArbilCsvImporter csvImporter = new ArbilCsvImporter(leadSelectedTreeNode);
		    csvImporter.doImport();
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	addItem(CATEGORY_IMPORT, PRIORITY_TOP, importCsvMenuItem);

	importBranchMenuItem.setText("Import Branch");
	importBranchMenuItem.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    ImportExportDialog importExportDialog = new ImportExportDialog(getTreePanel().localCorpusTree); // TODO: this may not always be to correct component and this code should be updated
		    importExportDialog.setDestinationNode(leadSelectedTreeNode);
		    importExportDialog.importArbilBranch();

		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	addItem(CATEGORY_IMPORT, PRIORITY_TOP + 5, importBranchMenuItem);

	reImportBranchMenuItem.setText("Re-Import this Branch");
	reImportBranchMenuItem.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		reImportBranch();
	    }
	});
	addItem(CATEGORY_IMPORT, PRIORITY_MIDDLE, reImportBranchMenuItem);

	setManualResourceLocationMenuItem.setText("Insert Manual Resource Location");
	setManualResourceLocationMenuItem.addActionListener(new ActionListener() {

	    public void actionPerformed(ActionEvent e) {
		String initialValue;
		if (leadSelectedTreeNode.hasLocalResource()) {
		    initialValue = leadSelectedTreeNode.resourceUrlField.getFieldValue();
		} else {
		    initialValue = "";
		}
		String manualLocation = (String) JOptionPane.showInputDialog(windowManager.getMainFrame(), "Enter the resource URI:", "Manual resource location", JOptionPane.PLAIN_MESSAGE, null, null, initialValue);
		if (manualLocation != null) { // Not canceled
		    try {
			URI locationURI = new URI(manualLocation);
			leadSelectedTreeNode.insertResourceLocation(locationURI);
		    } catch (URISyntaxException ex) {
			dialogHandler.addMessageDialogToQueue("The URI entered as a resource location is invalid. Please check the location and try again.", "Invalid URI");
		    } catch (ArbilMetadataException ex) {
			BugCatcherManager.getBugCatcher().logError(ex);
			dialogHandler.addMessageDialogToQueue("Could not add resource to the metadata. Check the error log for details.", "Error adding resource");
		    }
		}
	    }
	});
	addItem(CATEGORY_RESOURCE, PRIORITY_BOTTOM + 1, setManualResourceLocationMenuItem);
    }

    private void copyBranchMenuItemActionPerformed(java.awt.event.ActionEvent evt) {

	try {
	    ImportExportDialog importExportDialog = new ImportExportDialog(tree);
	    importExportDialog.copyToCache(tree.getSelectedNodes());
	} catch (Exception ex) {
	    BugCatcherManager.getBugCatcher().logError(ex);
	}
    }

    private void addLocalDirectoryMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
	File[] selectedFiles = dialogHandler.showFileSelectBox("Add Working Directory", true, true, false);
	if (selectedFiles != null && selectedFiles.length > 0) {
	    for (File currentDirectory : selectedFiles) {
		treeHelper.addLocationInteractive(currentDirectory.toURI());
	    }
	}
    }

    private void addRemoteCorpusMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
	String addableLocation = (String) JOptionPane.showInputDialog(windowManager.getMainFrame(), "Enter the URL", "Add Location", JOptionPane.PLAIN_MESSAGE);

	if ((addableLocation != null) && (addableLocation.length() > 0)) {
	    try {
		treeHelper.addLocationInteractive(ArbilDataNodeService.conformStringToUrl(addableLocation));
	    } catch (URISyntaxException ex) {
		dialogHandler.addMessageDialogToQueue("Failed to add location to remote corpus. See error log for details.", "Error");
		BugCatcherManager.getBugCatcher().logError(ex);
	    }
	}
    }

    private void addDefaultLocationsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
	if (0 < treeHelper.addDefaultCorpusLocations()) {
	    treeHelper.applyRootLocations();

	} else {
	    // alert the user when the node already exists and cannot be added again
	    dialogHandler.addMessageDialogToQueue("The default locations already exists and will not be added again", "Add Default Locations");

	}
    }

    private void removeCachedCopyMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
	DefaultMutableTreeNode selectedTreeNode = null;
    }

    private void searchSubnodesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
	windowManager.openSearchTable(((ArbilTree) getTreePanel().localCorpusTree).getAllSelectedNodes(), "Search");
    }

    private void searchRemoteSubnodesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
	windowManager.openSearchTable(((ArbilTree) getTreePanel().remoteCorpusTree).getSelectedNodes(), "Search Remote Corpus");
    }

    private void sendToServerMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
    }

    private void validateMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
	for (ArbilDataNode currentNode : selectedTreeNodes) {
	    if (currentNode.getNeedsSaveToDisk(false)
		    && JOptionPane.YES_OPTION == dialogHandler.showDialogBox(
		    "Validation will be against the file on disk. Save changes first?",
		    "Validation",
		    JOptionPane.YES_NO_OPTION,
		    JOptionPane.WARNING_MESSAGE)) {
		currentNode.saveChangesToCache(true);
		currentNode.reloadNode();
	    }
	    XsdChecker xsdChecker = new XsdChecker();
	    ((ArbilWindowManager) windowManager).createWindow("XsdChecker", xsdChecker);
	    xsdChecker.checkXML(currentNode);
	    xsdChecker.setDividerLocation(0.5);
	}
    }

    public void initAddMenu(JMenu addMenu, Object targetNodeUserObject) {
	addMenu.removeAll();

	// For corpus nodes (and other nodes that are non-data i.e. root nodes(?)), show selected templates and profiles

	if (!(targetNodeUserObject instanceof ArbilDataNode) || ((ArbilDataNode) targetNodeUserObject).isCorpus()) {
	    // consume the selected templates here rather than the clarin profile list
	    for (MenuItemData currentAddable : ArbilTemplateManager.getSingleInstance().getSelectedTemplatesMenuItems()) {
		// Check type. For root nodes (null object), allow all. For IMDI corpus, we should only allow IMDI
		if (!(targetNodeUserObject instanceof ArbilDataNode) || currentAddable.type == MenuItemData.Type.IMDI) {
		    JMenuItem addMenuItem;
		    addMenuItem = new JMenuItem();
		    addMenuItem.setText(currentAddable.menuText);
		    addMenuItem.setName(currentAddable.menuText);
		    addMenuItem.setActionCommand(currentAddable.menuAction);
		    addMenuItem.setToolTipText(currentAddable.menuToolTip);
		    addMenuItem.setIcon(currentAddable.menuIcon);
		    addMenuItem.addActionListener(new java.awt.event.ActionListener() {

			public void actionPerformed(java.awt.event.ActionEvent evt) {
			    try {
				if (leadSelectedTreeNode != null) {
				    new MetadataBuilder().requestAddNode(leadSelectedTreeNode, evt.getActionCommand(), ((JMenuItem) evt.getSource()).getText());
				} else {
				    // no nodes found that were valid imdi tree objects so we can assume that tis is the tree root
				    new MetadataBuilder().requestRootAddNode(evt.getActionCommand(), ((JMenuItem) evt.getSource()).getText());
				}
			    } catch (Exception ex) {
				BugCatcherManager.getBugCatcher().logError(ex);
			    }
			}
		    });
		    addMenu.add(addMenuItem);
		}
	    }
	}

	// For all data nodes, get list of allowed types and add them to the menu

	if (targetNodeUserObject instanceof ArbilDataNode) {
	    final ArbilTemplate currentTemplate = ((ArbilDataNode) targetNodeUserObject).getNodeTemplate();

	    MetadataBuilder mdBuilder = new MetadataBuilder();

	    for (Enumeration menuItemName = currentTemplate.listTypesFor(targetNodeUserObject); menuItemName.hasMoreElements();) {
		final String[] currentField = (String[]) menuItemName.nextElement();
		final String nodeText = currentField[0];
		final String nodeType = currentField[1];

		JMenuItem addMenuItem;
		addMenuItem = new JMenuItem();
		addMenuItem.setText(nodeText);
		addMenuItem.setName(nodeText);
		addMenuItem.setToolTipText(nodeType);
		if (null != currentTemplate.pathIsChildNode(nodeType)) {
		    addMenuItem.setIcon(ArbilIcons.getSingleInstance().dataIcon);
		} else {
		    addMenuItem.setIcon(ArbilIcons.getSingleInstance().fieldIcon);
		}

		if (mdBuilder.canAddChildNode((ArbilDataNode) targetNodeUserObject, nodeType)) {

		    addMenuItem.addActionListener(new java.awt.event.ActionListener() {

			public void actionPerformed(java.awt.event.ActionEvent evt) {
			    try {
				if (leadSelectedTreeNode != null) {
				    if (!leadSelectedTreeNode.getParentDomNode().getNeedsSaveToDisk(false)
					    || dialogHandler.showConfirmDialogBox(
					    "Adding a node will save pending changes to \""
					    + leadSelectedTreeNode.getParentDomNode().toString()
					    + "\" to disk. Do you want to proceed?", "Save pending changes?")) {
					new MetadataBuilder().requestAddNode(leadSelectedTreeNode, nodeType, nodeText);
				    }
				} else {
				    // no nodes found that were valid imdi tree objects so we can assume that tis is the tree root
				    new MetadataBuilder().requestRootAddNode(nodeType, ((JMenuItem) evt.getSource()).getText());
				}
			    } catch (Exception ex) {
				BugCatcherManager.getBugCatcher().logError(ex);
			    }
			}
		    });
		} else {
		    addMenuItem.setEnabled(false);
		}
		addMenu.add(addMenuItem);
	    }
	}
    }

    public void initHistoryMenu() {
	historyMenu.removeAll();
	for (String[] currentHistory : leadSelectedTreeNode.getHistoryList()) {
	    JMenuItem revertHistoryMenuItem;
	    revertHistoryMenuItem = new JMenuItem();
	    revertHistoryMenuItem.setText(currentHistory[0]);
	    revertHistoryMenuItem.setName(currentHistory[0]);
	    revertHistoryMenuItem.setActionCommand(currentHistory[1]);
	    revertHistoryMenuItem.addActionListener(new java.awt.event.ActionListener() {

		public void actionPerformed(java.awt.event.ActionEvent evt) {
		    try {
			if (!leadSelectedTreeNode.resurrectHistory(evt.getActionCommand())) {
			    dialogHandler.addMessageDialogToQueue("Could not revert version, no changes made", "History");
			}
		    } catch (Exception ex) {
			BugCatcherManager.getBugCatcher().logError(ex);
		    }
		}
	    });
	    historyMenu.add(revertHistoryMenuItem);
	}
    }

    public void initAddFromFavouritesMenu() {
	addFromFavouritesMenu.removeAll();
	Object targetObject = leadSelectedTreeNode;
	if (targetObject == null) {
	    // No lead selected tree node, so pass local corpus root node
	    targetObject = ((DefaultMutableTreeNode) treeHelper.getLocalCorpusTreeModel().getRoot()).getUserObject();
	}
	for (Enumeration menuItemName = ArbilFavourites.getSingleInstance().listFavouritesFor(targetObject); menuItemName.hasMoreElements();) {
	    String[] currentField = (String[]) menuItemName.nextElement();
//            System.out.println("MenuText: " + currentField[0]);
//            System.out.println("ActionCommand: " + currentField[1]);

	    JMenuItem addFavouriteMenuItem;
	    addFavouriteMenuItem = new JMenuItem();
	    addFavouriteMenuItem.setText(currentField[0]);
	    addFavouriteMenuItem.setName(currentField[0]);
	    addFavouriteMenuItem.setActionCommand(currentField[1]);
	    addFavouriteMenuItem.addActionListener(new java.awt.event.ActionListener() {

		public void actionPerformed(java.awt.event.ActionEvent evt) {
		    try {
			String favouriteUrlString = evt.getActionCommand();
			ArbilDataNode templateDataNode = dataNodeLoader.getArbilDataNode(null, ArbilDataNodeService.conformStringToUrl(favouriteUrlString));
			if (leadSelectedTreeNode != null) {
			    new MetadataBuilder().requestAddNode(leadSelectedTreeNode, ((JMenuItem) evt.getSource()).getText(), templateDataNode);
			} else {
			    new MetadataBuilder().requestAddRootNode(templateDataNode, ((JMenuItem) evt.getSource()).getText());
			}
//                    treeHelper.getImdiChildNodes(targetNode);
//                    String addedNodeUrlString = treeHelper.addImdiChildNode(targetNode, linorgFavourites.getNodeType(imdiTemplateUrlString), ((JMenuItem) evt.getSource()).getText());
//                    imdiLoader.getImdiObject("", addedNodeUrlString).requestMerge(imdiLoader.getImdiObject("", imdiTemplateUrlString));
//                    loop child nodes and insert them into the new node
//                    ImdiTreeObject templateImdiObject = GuiHelper.imdiLoader.getImdiObject("", imdiTemplateUrlString);
//                    ImdiTreeObject targetImdiObject = GuiHelper.imdiLoader.getImdiObject("", addedNodeUrl);
//
//                    for (Enumeration<ImdiTreeObject> childTemplateEnum = templateImdiObject.getChildEnum(); childTemplateEnum.hasMoreElements();) {
//                        ImdiTreeObject currentTemplateChild = childTemplateEnum.nextElement();
//                        String addedNodeUrl = treeHelper.addImdiChildNode(targetNode, linorgFavourites.getNodeType(currentTemplateChild.getUrlString()), currentTemplateChild.toString());
//                        linorgFavourites.mergeFromFavourite(addedNodeUrl, imdiTemplateUrlString, true);
//                    }
//                    treeHelper.reloadLocalCorpusTree(targetNode);
		    } catch (Exception ex) {
			dialogHandler.addMessageDialogToQueue("Failed to add from favourites, see error log for details.", "Error");
			BugCatcherManager.getBugCatcher().logError(ex);
		    }
		}
	    });
	    addFromFavouritesMenu.add(addFavouriteMenuItem);
	}
    }

    private void viewSelectedNodes() {
	try {
	    ArrayList<ArbilDataNode> filteredNodes = new ArrayList<ArbilDataNode>();
	    for (ArbilDataNode currentItem : ((ArbilTree) getInvoker()).getSelectedNodes()) {
		if (currentItem.isMetaDataNode() || currentItem.getFields().size() > 0) {
		    filteredNodes.add(currentItem);
		} else {
		    try {
			windowManager.openUrlWindowOnce(currentItem.toString(), currentItem.getURI().toURL());
		    } catch (MalformedURLException murle) {
			BugCatcherManager.getBugCatcher().logError(murle);
		    }
		}
	    }
	    if (filteredNodes.size() > 0) {
		windowManager.openFloatingTableOnce(filteredNodes.toArray(new ArbilDataNode[]{}), null);
	    }
	} catch (Exception ex) {
	    BugCatcherManager.getBugCatcher().logError(ex);
	}
    }

    private void viewSelectedSubnodes() {
	ArbilDataNode[] selectedNodes = ((ArbilTree) getInvoker()).getSelectedNodes();
	ArrayList<ArbilDataNode> filteredNodes = new ArrayList<ArbilDataNode>(selectedNodes.length);
	for (ArbilDataNode dataNode : selectedNodes) {
	    if (dataNode.isSession() || dataNode.isMetaDataNode()) {
		filteredNodes.add(dataNode);
	    }
	}
	if (!filteredNodes.isEmpty()) {
	    windowManager.openFloatingSubnodesWindows(filteredNodes.toArray(new ArbilDataNode[0]));
	}
    }

    private void reImportBranch() {
	try {
	    URI remoteDataFile = sessionStorage.getOriginatingUri(leadSelectedTreeNode.getURI());
	    if (remoteDataFile != null) {
		ArbilDataNode originatingNode = dataNodeLoader.getArbilDataNodeWithoutLoading(remoteDataFile);
		if (originatingNode.isLocal() && !originatingNode.getFile().exists()) {
		    dialogHandler.addMessageDialogToQueue("The origional file location cannot be found", "Re Import Branch");
		} else if (originatingNode.isMetaDataNode()) {
		    ImportExportDialog importExportDialog = new ImportExportDialog(getTreePanel().localCorpusTree); // TODO: this may not always be to correct component and this code should be updated
		    importExportDialog.setDestinationNode(leadSelectedTreeNode); // TODO: do not re add the location in this case
		    importExportDialog.copyToCache(new ArbilDataNode[]{originatingNode});
		} else {
		    dialogHandler.addMessageDialogToQueue("Could not determine the origional node type", "Re Import Branch");
		}
	    } else {
		dialogHandler.addMessageDialogToQueue("Could not determine the origional location", "Re Import Branch");
	    }
	} catch (Exception ex) {
	    BugCatcherManager.getBugCatcher().logError(ex);
	}
    }

    /**
     * Starts long field editor for selected nodes
     */
    private void startLongFieldEditor() {
	ArbilDataNode[] selectedNodes = ((ArbilTree) getInvoker()).getSelectedNodes();
	for (ArbilDataNode node : selectedNodes) {
	    if (node.getFields().size() > 0) {
		// Get fields for the node
		List<ArbilField[]> fieldArrays = node.getFieldsSorted();
		// Show the editor
		new ArbilLongFieldEditor().showEditor(fieldArrays.get(0), fieldArrays.get(0)[0].getFieldValue(), 0);
	    }
	}
    }

    private ArbilTreePanels getTreePanel() {
	if (treeHelper instanceof ArbilTreeHelper) {
	    return ((ArbilTreeHelper) treeHelper).getArbilTreePanel();
	} else {
	    return null;
	}
    }

    @Override
    protected void setAllInvisible() {
	removeCachedCopyMenuItem.setVisible(false);
	removeLocalDirectoryMenuItem.setVisible(false);
	addLocalDirectoryMenuItem.setVisible(false);
	showHiddenFilesMenuItem.setVisible(false);
	removeRemoteCorpusMenuItem.setVisible(false);
	addRemoteCorpusMenuItem.setVisible(false);
	copyBranchMenuItem.setVisible(false);
	searchRemoteBranchMenuItem.setVisible(false);
	copyNodeUrlMenuItem.setVisible(false);
	pasteMenuItem1.setVisible(false);

	searchSubnodesMenuItem.setVisible(false);
	reloadSubnodesMenuItem.setVisible(false);
	addDefaultLocationsMenuItem.setVisible(false);
	addMenu.setVisible(false);
	deleteMenuItem.setVisible(false);
	viewSelectedNodesMenuItem.setVisible(false);
	addFromFavouritesMenu.setVisible(false);
	//viewChangesMenuItem.setVisible(false);
	//sendToServerMenuItem.setVisible(false);
	validateMenuItem.setVisible(false);
	historyMenu.setVisible(false);
	exportMenuItem.setVisible(false);
	importCsvMenuItem.setVisible(false);
	importBranchMenuItem.setVisible(false);
	reImportBranchMenuItem.setVisible(false);
	addToFavouritesMenuItem.setVisible(false);
	removeFromFavouritesMenuItem.setVisible(false);
	viewSelectedSubnodesMenuItem.setVisible(false);
	editInLongFieldEditor.setVisible(false);

	setManualResourceLocationMenuItem.setVisible(false);
    }
    private ArbilTree tree;
    private JMenu addFromFavouritesMenu = new JMenu();
    private JMenuItem addLocalDirectoryMenuItem = new JMenuItem();
    private JCheckBoxMenuItem showHiddenFilesMenuItem = new JCheckBoxMenuItem();
    private JMenuItem addDefaultLocationsMenuItem = new JMenuItem();
    private JMenu addMenu = new JMenu();
    private JMenuItem addRemoteCorpusMenuItem = new JMenuItem();
    private JMenuItem addToFavouritesMenuItem = new JMenuItem();
    private JMenuItem removeFromFavouritesMenuItem = new JMenuItem();
    private JMenuItem copyBranchMenuItem = new JMenuItem();
    private JMenuItem searchRemoteBranchMenuItem = new JMenuItem();
    private JMenuItem copyNodeUrlMenuItem = new JMenuItem();
    private JMenuItem deleteMenuItem = new JMenuItem();
    private JMenuItem exportMenuItem = new JMenuItem();
    private JMenuItem importCsvMenuItem = new JMenuItem();
    private JMenuItem importBranchMenuItem = new JMenuItem();
    private JMenuItem reImportBranchMenuItem = new JMenuItem();
//    private JMenu mergeWithFavouritesMenu = new JMenu();
    private JMenuItem pasteMenuItem1 = new JMenuItem();
    private JMenuItem reloadSubnodesMenuItem = new JMenuItem();
    private JMenuItem removeCachedCopyMenuItem = new JMenuItem();
    private JMenuItem removeLocalDirectoryMenuItem = new JMenuItem();
    private JMenuItem removeRemoteCorpusMenuItem = new JMenuItem();
    private JMenuItem searchSubnodesMenuItem = new JMenuItem();
    private JMenuItem sendToServerMenuItem = new JMenuItem();
    private JMenuItem validateMenuItem = new JMenuItem();
    private JMenu historyMenu = new JMenu();
    private JMenuItem viewChangesMenuItem = new JMenuItem();
    private JMenuItem viewSelectedNodesMenuItem = new JMenuItem();
    private JMenuItem viewSelectedSubnodesMenuItem = new JMenuItem();
    private JMenuItem editInLongFieldEditor = new JMenuItem();
    private JMenuItem setManualResourceLocationMenuItem = new JMenuItem();
}
