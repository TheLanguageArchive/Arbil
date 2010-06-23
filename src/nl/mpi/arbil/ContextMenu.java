package nl.mpi.arbil;

import nl.mpi.arbil.templates.ArbilTemplateManager;
import nl.mpi.arbil.templates.ArbilTemplate;
import nl.mpi.arbil.importexport.ImportExportDialog;
import nl.mpi.arbil.data.ImdiTreeObject;
import java.awt.Component;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.tree.DefaultMutableTreeNode;
import nl.mpi.arbil.clarin.CmdiComponentBuilder;
import nl.mpi.arbil.data.ImdiLoader;
import nl.mpi.arbil.data.MetadataBuilder;
import nl.mpi.arbil.importexport.ArbilCsvImporter;
import nl.mpi.arbil.templates.ArbilTemplateManager.MenuItemData;

/**
 * Document   : ContextMenu
 * Created on : Apr 24, 2009, 3:09:47 PM
 * code moved from LinorgFrame
 * @author Peter.Withers@mpi.nl
 */
public class ContextMenu {

    private JMenu addFromFavouritesMenu;
    private JMenuItem addLocalDirectoryMenuItem;
    private JCheckBoxMenuItem showHiddenFilesMenuItem;
    private JMenuItem addDefaultLocationsMenuItem;
    private JMenu addMenu;
    private JMenuItem addRemoteCorpusMenuItem;
    private JMenuItem addToFavouritesMenuItem;
    private JMenuItem copyBranchMenuItem;
    private JMenuItem copyImdiUrlMenuItem;
    private JMenuItem deleteMenuItem;
    private JMenuItem exportMenuItem;
    private JMenuItem importCsvMenuItem;
    private JMenuItem importBranchMenuItem;
    private JMenuItem reImportBranchMenuItem;
//    private JMenu favouritesMenu;
    private JMenu mergeWithFavouritesMenu;
    private JMenuItem pasteMenuItem1;
    private JMenuItem reloadSubnodesMenuItem;
    private JMenuItem removeCachedCopyMenuItem;
    private JMenuItem removeLocalDirectoryMenuItem;
    private JMenuItem removeRemoteCorpusMenuItem;
    private JMenuItem saveMenuItem;
    private JMenuItem searchSubnodesMenuItem;
    private JMenuItem sendToServerMenuItem;
    private JPopupMenu treePopupMenu;
//    private JSeparator treePopupMenuSeparator1;
//    private JSeparator treePopupMenuSeparator2;
    private JMenuItem validateMenuItem;
    private JMenu historyMenu;
    private JMenuItem viewChangesMenuItem;
    private JMenuItem browseForResourceFileMenuItem;
    private JMenuItem viewSelectedNodesMenuItem;
    private JMenuItem viewXmlMenuItem;
    private JMenuItem viewInBrrowserMenuItem;
    private JMenuItem viewXmlMenuItemFormatted;
    private JMenuItem openXmlMenuItemFormatted;
    private JMenuItem exportHtmlMenuItemFormatted;
    private JMenuItem overrideTypeCheckerDecision;
    static private ContextMenu singleInstance = null;
    //////////
    // table menu items
    private JMenuItem copySelectedRowsMenuItem;
    private JMenuItem pasteIntoSelectedRowsMenuItem;
    private JMenuItem viewSelectedRowsMenuItem;
    private JMenuItem matchingRowsMenuItem;
    private JMenuItem removeSelectedRowsMenuItem;
    private JMenuItem hideSelectedColumnsMenuItem;
    private JMenuItem searchReplaceMenuItem;
    private JMenuItem deleteFieldMenuItem;
    private JMenuItem revertFieldMenuItem;
    private JMenuItem copyCellToColumnMenuItem;
//    private JSeparator cellMenuDivider;
//    private JSeparator cellTableDivider;
    private JMenuItem matchingCellsMenuItem;
    private JMenuItem openInLongFieldEditorMenuItem;
    private JMenuItem clearCellColoursMenuItem;
    private JMenuItem jumpToNodeInTreeMenuItem;
    //////////
    ImdiTreeObject[] selectedTreeNodes = null;
    ImdiTreeObject leadSelectedTreeNode = null;
    ImdiTable currentTable = null;

    static synchronized public ContextMenu getSingleInstance() {
//        TODO: this should really be removed and a new instance made each time
        System.out.println("ContextMenu getSingleInstance");
        if (singleInstance == null) {
            singleInstance = new ContextMenu();
        }
        return singleInstance;
    }

    private ContextMenu() {
        treePopupMenu = new JPopupMenu();
        viewSelectedNodesMenuItem = new JMenuItem();
        copyImdiUrlMenuItem = new JMenuItem();
        pasteMenuItem1 = new JMenuItem();
        copyBranchMenuItem = new JMenuItem();
        searchSubnodesMenuItem = new JMenuItem();
        reloadSubnodesMenuItem = new JMenuItem();
        addMenu = new JMenu();
//        favouritesMenu = new JMenu();
        addToFavouritesMenuItem = new JMenuItem();
        addFromFavouritesMenu = new JMenu();
        mergeWithFavouritesMenu = new JMenu();
        deleteMenuItem = new JMenuItem();
//        treePopupMenuSeparator1 = new JSeparator();
        viewXmlMenuItem = new JMenuItem();
        viewXmlMenuItemFormatted = new JMenuItem();
        openXmlMenuItemFormatted = new JMenuItem();
        exportHtmlMenuItemFormatted = new JMenuItem();
        overrideTypeCheckerDecision = new JMenuItem();
        viewInBrrowserMenuItem = new JMenuItem();
        browseForResourceFileMenuItem = new JMenuItem();
        validateMenuItem = new JMenuItem();
        historyMenu = new JMenu();
//        treePopupMenuSeparator2 = new JSeparator();
        addRemoteCorpusMenuItem = new JMenuItem();
        addDefaultLocationsMenuItem = new JMenuItem();
        removeRemoteCorpusMenuItem = new JMenuItem();
        removeCachedCopyMenuItem = new JMenuItem();
        addLocalDirectoryMenuItem = new JMenuItem();
        showHiddenFilesMenuItem = new JCheckBoxMenuItem();
        removeLocalDirectoryMenuItem = new JMenuItem();
        saveMenuItem = new JMenuItem();
        viewChangesMenuItem = new JMenuItem();
        sendToServerMenuItem = new JMenuItem();
        exportMenuItem = new JMenuItem();
        importCsvMenuItem = new JMenuItem();
        importBranchMenuItem = new JMenuItem();
        reImportBranchMenuItem = new JMenuItem();
        //////////
        // table menu items
        copySelectedRowsMenuItem = new JMenuItem();
        pasteIntoSelectedRowsMenuItem = new JMenuItem();
        viewSelectedRowsMenuItem = new JMenuItem();
        matchingRowsMenuItem = new JMenuItem();
        removeSelectedRowsMenuItem = new JMenuItem();
        hideSelectedColumnsMenuItem = new JMenuItem();
        searchReplaceMenuItem = new JMenuItem();
        deleteFieldMenuItem = new JMenuItem();
        revertFieldMenuItem = new JMenuItem();
        copyCellToColumnMenuItem = new JMenuItem();
//        cellMenuDivider = new JSeparator();
//        cellTableDivider = new JSeparator();
        matchingCellsMenuItem = new JMenuItem();
        openInLongFieldEditorMenuItem = new JMenuItem();
        clearCellColoursMenuItem = new JMenuItem();
        jumpToNodeInTreeMenuItem = new JMenuItem();
        //////////
        // table menu items
        copySelectedRowsMenuItem.setText("Copy");
        copySelectedRowsMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    currentTable.copySelectedTableRowsToClipBoard();
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        treePopupMenu.add(copySelectedRowsMenuItem);

        pasteIntoSelectedRowsMenuItem.setText("Paste");
        pasteIntoSelectedRowsMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    currentTable.pasteIntoSelectedTableRowsFromClipBoard();
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        treePopupMenu.add(pasteIntoSelectedRowsMenuItem);

        treePopupMenu.add(new JSeparator());
        // field menu items
        openInLongFieldEditorMenuItem.setText("Open in Long Field Editor");
        openInLongFieldEditorMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    currentTable.startLongFieldEditorForSelectedFields();
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        treePopupMenu.add(openInLongFieldEditorMenuItem);

        hideSelectedColumnsMenuItem.setText("Hide Selected Columns");
        hideSelectedColumnsMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    currentTable.hideSelectedColumnsFromTable();
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        treePopupMenu.add(hideSelectedColumnsMenuItem);

        deleteFieldMenuItem.setText("Delete MultiField");
        deleteFieldMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    ImdiField[] selectedFields = currentTable.getSelectedFields();
                    if (selectedFields != null) {
//                                  to delete these fields they must be separated into imdi tree objects and request delete for each one
//                                  todo: the delete field action should also be available in the long field editor
                        Hashtable<ImdiTreeObject, ArrayList> selectedFieldHashtable = new Hashtable<ImdiTreeObject, ArrayList>();
                        for (ImdiField currentField : selectedFields) {
                            ArrayList currentList = selectedFieldHashtable.get(currentField.parentImdi);
                            if (currentList == null) {
                                currentList = new ArrayList();
                                selectedFieldHashtable.put(currentField.parentImdi, currentList);
                            }
                            currentList.add(currentField.getFullXmlPath());
                        }
                        for (ImdiTreeObject currentImdiObject : selectedFieldHashtable.keySet()) {
                            CmdiComponentBuilder componentBuilder = new CmdiComponentBuilder();
                            boolean result = componentBuilder.removeChildNodes(currentImdiObject, (String[]) selectedFieldHashtable.get(currentImdiObject).toArray(new String[]{}));
                            if (result) {
                                currentImdiObject.reloadNode();
                            } else {
                                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Error deleting fields, check the log file via the help menu for ore information.", "Delete Field");
                            }
                            //currentImdiObject.deleteFromDomViaId((String[]) selectedFieldHashtable.get(currentImdiObject).toArray(new String[]{}));
//                            GuiHelper.linorgBugCatcher.logError(new Exception("deleteFromDomViaId"));
                        }
                    }
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        treePopupMenu.add(deleteFieldMenuItem);

        revertFieldMenuItem.setText("Revert Selected Fields");
        revertFieldMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    ImdiField[] selectedFields = currentTable.getSelectedFields();
                    if (selectedFields != null) {
                        for (ImdiField currentField : selectedFields) {
                            currentField.revertChanges();
                        }
                    }
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        treePopupMenu.add(revertFieldMenuItem);

        copyCellToColumnMenuItem.setText("Copy Cell to Whole Column"); // NOI18N
        copyCellToColumnMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    // TODO: change this to copy to selected rows
                    if (!(currentTable.imdiTableModel.getValueAt(currentTable.getSelectedRow(), currentTable.getSelectedColumn()) instanceof ImdiField)) {
                        LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Cannot copy this type of field", "Copy Cell to Whole Column");
                    } else if (0 == JOptionPane.showConfirmDialog(LinorgWindowManager.getSingleInstance().linorgFrame, "About to replace all values in column \"" + currentTable.imdiTableModel.getColumnName(currentTable.getSelectedColumn()) + "\"\nwith the value \"" + currentTable.imdiTableModel.getValueAt(currentTable.getSelectedRow(), currentTable.getSelectedColumn()) + "\"\n(<multiple values> will not be affected)", "Copy cell to whole column", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE)) {
                        currentTable.imdiTableModel.copyCellToColumn(currentTable.getSelectedRow(), currentTable.getSelectedColumn());
                    }
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        treePopupMenu.add(copyCellToColumnMenuItem);

        matchingCellsMenuItem.setText("Highlight Matching Cells");
        matchingCellsMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    currentTable.imdiTableModel.highlightMatchingCells(currentTable.getSelectedRow(), currentTable.getSelectedColumn());
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        treePopupMenu.add(matchingCellsMenuItem);

        clearCellColoursMenuItem.setText("Clear Cell Highlight"); // NOI18N
        clearCellColoursMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    currentTable.imdiTableModel.clearCellColours();
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        treePopupMenu.add(clearCellColoursMenuItem);

        searchReplaceMenuItem.setText("Find/Replace");
        searchReplaceMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {                    
                    ((LinorgSplitPanel) currentTable.getParent().getParent().getParent().getParent()).showSearchPane();
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        treePopupMenu.add(searchReplaceMenuItem);

        treePopupMenu.add(new JSeparator());
        // row menu items
        viewSelectedRowsMenuItem.setText("View Selected Rows");
        viewSelectedRowsMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    currentTable.viewSelectedTableRows();
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        treePopupMenu.add(viewSelectedRowsMenuItem);


        matchingRowsMenuItem.setText("Select Matching Rows"); // NOI18N
        matchingRowsMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    currentTable.highlightMatchingRows();
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        treePopupMenu.add(matchingRowsMenuItem);

        removeSelectedRowsMenuItem.setText("Remove Selected Rows");
        removeSelectedRowsMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    currentTable.removeSelectedRowsFromTable();
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        treePopupMenu.add(removeSelectedRowsMenuItem);

        jumpToNodeInTreeMenuItem.setText("Jump to in Tree"); // NOI18N
        jumpToNodeInTreeMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    TreeHelper.getSingleInstance().jumpToSelectionInTree(false, currentTable.getImdiNodeForSelection());
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        treePopupMenu.add(jumpToNodeInTreeMenuItem);
        treePopupMenu.add(new JSeparator());
        //////////
        //////////
        viewSelectedNodesMenuItem.setText("View Selected");

        viewSelectedNodesMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    ArrayList<ImdiTreeObject> filteredNodes = new ArrayList<ImdiTreeObject>();
                    for (ImdiTreeObject currentItem : ((ImdiTree) treePopupMenu.getInvoker()).getSelectedNodes()) {
                        if (currentItem.isMetaDataNode() || currentItem.getFields().size() > 0) {
                            filteredNodes.add(currentItem);
                        } else {
                            try {
                                LinorgWindowManager.getSingleInstance().openUrlWindowOnce(currentItem.toString(), currentItem.getURI().toURL());
                            } catch (MalformedURLException murle) {
                                GuiHelper.linorgBugCatcher.logError(murle);
                            }
                        }
                    }
                    if (filteredNodes.size() > 0) {
                        LinorgWindowManager.getSingleInstance().openFloatingTableOnce(filteredNodes.toArray(new ImdiTreeObject[]{}), null);
                    }
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(viewSelectedNodesMenuItem);
        copyImdiUrlMenuItem.setText("Copy");

        copyImdiUrlMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    if (selectedTreeNodes == null) {
                        LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("No node selected", "Copy");
                    } else {
                        ImdiTree sourceTree = (ImdiTree) treePopupMenu.getInvoker();
                        sourceTree.copyNodeUrlToClipboard(selectedTreeNodes);
                    }
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(copyImdiUrlMenuItem);
        pasteMenuItem1.setText("Paste");

        pasteMenuItem1.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    for (ImdiTreeObject currentNode : selectedTreeNodes) {
                        currentNode.pasteIntoNode();
                    }
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(pasteMenuItem1);
        copyBranchMenuItem.setText("Import to Local Corpus");

        copyBranchMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    copyBranchMenuItemActionPerformed(evt);
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(copyBranchMenuItem);
        searchSubnodesMenuItem.setText("Search");

        searchSubnodesMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    searchSubnodesMenuItemActionPerformed(evt);
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(searchSubnodesMenuItem);
        reloadSubnodesMenuItem.setText("Reload");

        reloadSubnodesMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    for (ImdiTreeObject currentNode : selectedTreeNodes) {
                        // this reload will first clear the save is required flag then reload
                        currentNode.reloadNode();
                    }
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(reloadSubnodesMenuItem);
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
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(addMenu);
//        favouritesMenu.setText("Favourites");
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
        treePopupMenu.add(addFromFavouritesMenu);
//        favouritesMenu.add(addFromFavouritesMenu);

        addToFavouritesMenuItem.setText("Set As Favourite");
        addToFavouritesMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    LinorgFavourites.getSingleInstance().toggleFavouritesList(((ImdiTree) treePopupMenu.getInvoker()).getSelectedNodes(), addToFavouritesMenuItem.getActionCommand().equals("true"));
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(addToFavouritesMenuItem);

        mergeWithFavouritesMenu.setText("Merge With Favourite");

        mergeWithFavouritesMenu.setActionCommand("Merge With Favouurite");

        browseForResourceFileMenuItem.setText("Browse For Resource File");
        browseForResourceFileMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    File[] selectedFiles = LinorgWindowManager.getSingleInstance().showFileSelectBox("Select Resource File", false, false, false);
                    if (selectedFiles != null && selectedFiles.length > 0) {
                        leadSelectedTreeNode.resourceUrlField.setFieldValue(selectedFiles[0].toURL().toExternalForm(), true, false);
                    }
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        treePopupMenu.add(browseForResourceFileMenuItem);

//        favouritesMenu.add(mergeWithFavouritesMenu);
        deleteMenuItem.setText("Delete");

        deleteMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    TreeHelper.getSingleInstance().deleteNode(treePopupMenu.getInvoker());
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(deleteMenuItem);

        treePopupMenu.add(new JSeparator());

        overrideTypeCheckerDecision.setText("Override Type Checker Decision");
        overrideTypeCheckerDecision.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    String titleString = "Override Type Checker Decision";
                    String messageString = "The type checker does not recognise the selected file/s, which means that they\nare not an archivable type. This action will override that decision and allow you\nto add the file/s to a session, as either media or written resources,\nhowever it might not be possible to import the result to the copus server.";
                    String[] optionStrings = {"WrittenResource", "MediaFile", "Cancel"};
                    int userSelection = JOptionPane.showOptionDialog(LinorgWindowManager.getSingleInstance().linorgFrame.getContentPane(), messageString, titleString, JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, optionStrings, optionStrings[2]);
                    if (optionStrings[userSelection].equals("WrittenResource") || optionStrings[userSelection].equals("MediaFile")) {
                        for (ImdiTreeObject currentNode : selectedTreeNodes) {
                            if (currentNode.mpiMimeType == null) {
                                currentNode.mpiMimeType = "Manual/" + optionStrings[userSelection];
                                currentNode.typeCheckerMessage = "Manually overridden (might not be compatible with the archive)";
                                currentNode.clearIcon();
                            }
                        }
                    }
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        treePopupMenu.add(overrideTypeCheckerDecision);

        viewInBrrowserMenuItem.setText("Open in External Application");
        viewInBrrowserMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    openFileInBrowser(selectedTreeNodes);
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        treePopupMenu.add(viewInBrrowserMenuItem);

        viewXmlMenuItem.setText("View XML");

        viewXmlMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    for (ImdiTreeObject currentNode : selectedTreeNodes) {
                        GuiHelper.getSingleInstance().openImdiXmlWindow(currentNode, false, false);
                    }
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(viewXmlMenuItem);
        viewXmlMenuItemFormatted.setText("View IMDI Formatted");

        viewXmlMenuItemFormatted.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    for (ImdiTreeObject currentNode : selectedTreeNodes) {
                        GuiHelper.getSingleInstance().openImdiXmlWindow(currentNode, true, false);
                    }
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(viewXmlMenuItemFormatted);
        openXmlMenuItemFormatted.setText("Open IMDI Formatted");
        openXmlMenuItemFormatted.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    for (ImdiTreeObject currentNode : selectedTreeNodes) {
                        GuiHelper.getSingleInstance().openImdiXmlWindow(currentNode, true, true);
                    }
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        treePopupMenu.add(openXmlMenuItemFormatted);

        exportHtmlMenuItemFormatted.setText("Export IMDI to HTML");
        exportHtmlMenuItemFormatted.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    new ImdiToHtmlConverter().exportImdiToHtml(selectedTreeNodes);
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        treePopupMenu.add(exportHtmlMenuItemFormatted);

        validateMenuItem.setText("Check XML Conformance");

        validateMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    validateMenuItemActionPerformed(evt);
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(validateMenuItem);

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
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        treePopupMenu.add(historyMenu);


        treePopupMenu.add(new JSeparator());
        addRemoteCorpusMenuItem.setText("Add Remote Location");

        addRemoteCorpusMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    addRemoteCorpusMenuItemActionPerformed(evt);
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(addRemoteCorpusMenuItem);
        addDefaultLocationsMenuItem.setText("Add Default Remote Locations");

        addDefaultLocationsMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    addDefaultLocationsMenuItemActionPerformed(evt);
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(addDefaultLocationsMenuItem);
        removeRemoteCorpusMenuItem.setText("Remove Remote Location");

        removeRemoteCorpusMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    for (ImdiTreeObject selectedNode : selectedTreeNodes) {
                        TreeHelper.getSingleInstance().removeLocation(selectedNode);
                    }
                    TreeHelper.getSingleInstance().applyRootLocations();
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(removeRemoteCorpusMenuItem);
        removeCachedCopyMenuItem.setText("Remove Cache Link");

        removeCachedCopyMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    removeCachedCopyMenuItemActionPerformed(evt);
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(removeCachedCopyMenuItem);
        addLocalDirectoryMenuItem.setText("Add Working Directory");

        addLocalDirectoryMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    addLocalDirectoryMenuItemActionPerformed(evt);
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(addLocalDirectoryMenuItem);

        showHiddenFilesMenuItem.setText("Show Hidden Files");
        showHiddenFilesMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    TreeHelper.getSingleInstance().setShowHiddenFilesInTree(showHiddenFilesMenuItem.getState());
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        treePopupMenu.add(showHiddenFilesMenuItem);

        removeLocalDirectoryMenuItem.setText("Remove Link to Directory");

        removeLocalDirectoryMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    for (ImdiTreeObject selectedNode : selectedTreeNodes) {
                        TreeHelper.getSingleInstance().removeLocation(selectedNode);
                    }
                    TreeHelper.getSingleInstance().applyRootLocations();
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(removeLocalDirectoryMenuItem);
        saveMenuItem.setText("Save Changes to Disk");

        saveMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    for (ImdiTreeObject selectedNode : selectedTreeNodes) {
                        System.out.println("userObject: " + selectedNode);
                        // reloading will first check if a save is required then save and reload
                        ImdiLoader.getSingleInstance().requestReload((ImdiTreeObject) selectedNode.getParentDomNode());
                    }

                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(saveMenuItem);
        viewChangesMenuItem.setText("View Changes");
        viewChangesMenuItem.setEnabled(false);

        viewChangesMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    for (ImdiTreeObject currentNode : selectedTreeNodes) {
//                        LinorgWindowManager.getSingleInstance().openDiffWindow(currentNode);
                    }
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(viewChangesMenuItem);
        sendToServerMenuItem.setText("Send to Server");

        sendToServerMenuItem.setEnabled(false);

        sendToServerMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    sendToServerMenuItemActionPerformed(evt);

                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(sendToServerMenuItem);
        exportMenuItem.setText("Export");

        exportMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    ImportExportDialog importExportDialog = new ImportExportDialog(TreeHelper.getSingleInstance().arbilTreePanel.remoteCorpusTree);
                    importExportDialog.selectExportDirectoryAndExport(((ImdiTree) treePopupMenu.getInvoker()).getSelectedNodes());
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(exportMenuItem);
        importCsvMenuItem.setText("Import CSV");

        importCsvMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    ArbilCsvImporter csvImporter = new ArbilCsvImporter(leadSelectedTreeNode);
                    csvImporter.doImport();
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(importCsvMenuItem);

        importBranchMenuItem.setText("Import Branch");
        importBranchMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    ImportExportDialog importExportDialog = new ImportExportDialog(TreeHelper.getSingleInstance().arbilTreePanel.localCorpusTree); // TODO: this may not always be to correct component and this code should be updated
                    importExportDialog.setDestinationNode(leadSelectedTreeNode);
                    importExportDialog.importImdiBranch();

                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        treePopupMenu.add(importBranchMenuItem);

        reImportBranchMenuItem.setText("Re-Import this Branch");
        reImportBranchMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    URI remoteImdiFile = LinorgSessionStorage.getSingleInstance().getOriginatingUri(leadSelectedTreeNode.getURI());
                    if (remoteImdiFile != null) {
                        ImdiTreeObject originatingNode = ImdiLoader.getSingleInstance().getImdiObjectWithoutLoading(remoteImdiFile);
                        if (originatingNode.isLocal() && !originatingNode.getFile().exists()) {
                            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("The origional file location cannot be found", "Re Import Branch");
                        } else if (originatingNode.isMetaDataNode()) {
                            ImportExportDialog importExportDialog = new ImportExportDialog(TreeHelper.getSingleInstance().arbilTreePanel.localCorpusTree); // TODO: this may not always be to correct component and this code should be updated
                            importExportDialog.setDestinationNode(leadSelectedTreeNode); // TODO: do not re add the location in this case
                            importExportDialog.copyToCache(new ImdiTreeObject[]{originatingNode});
                        } else {
                            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Could not determine the origional node type", "Re Import Branch");
                        }
                    } else {
                        LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Could not determine the origional location", "Re Import Branch");
                    }
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        treePopupMenu.add(reImportBranchMenuItem);
    }

    private void copyBranchMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:    
        if (treePopupMenu.getInvoker() instanceof JTree) {
            try {
                ImportExportDialog importExportDialog = new ImportExportDialog(treePopupMenu.getInvoker());
                importExportDialog.copyToCache(((ImdiTree) treePopupMenu.getInvoker()).getSelectedNodes());
            } catch (Exception ex) {
                GuiHelper.linorgBugCatcher.logError(ex);
            }
        }
    }

    private void addLocalDirectoryMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        File[] selectedFiles = LinorgWindowManager.getSingleInstance().showFileSelectBox("Add Working Directory", true, true, false);
        if (selectedFiles != null && selectedFiles.length > 0) {
            for (File currentDirectory : selectedFiles) {
                TreeHelper.getSingleInstance().addLocationGui(currentDirectory.toURI());
            }
        }
    }

    private void addRemoteCorpusMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        //GEN-FIRST:event_addRemoteCorpusMenuItemActionPerformed
        // TODO add your handling code here:    
        String addableLocation = (String) JOptionPane.showInputDialog(LinorgWindowManager.getSingleInstance().linorgFrame, "Enter the URL", "Add Location", JOptionPane.PLAIN_MESSAGE);

        if ((addableLocation != null) && (addableLocation.length() > 0)) {
            TreeHelper.getSingleInstance().addLocationGui(ImdiTreeObject.conformStringToUrl(addableLocation));
        }
    }//GEN-LAST:event_addRemoteCorpusMenuItemActionPerformed

    private void addDefaultLocationsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        //GEN-FIRST:event_addDefaultLocationsMenuItemActionPerformed
        // TODO add your handling code here:    
        if (0 < TreeHelper.getSingleInstance().addDefaultCorpusLocations()) {
            TreeHelper.getSingleInstance().applyRootLocations();

        } else {
            // alert the user when the node already exists and cannot be added again       
            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("The defalut locations already exists and will not be added again", "Add Default Locations");

        }
    }//GEN-LAST:event_addDefaultLocationsMenuItemActionPerformed

    private void removeCachedCopyMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        //GEN-FIRST:event_removeCachedCopyMenuItemActionPerformed
        // TODO add your handling code here://    
        DefaultMutableTreeNode selectedTreeNode = null;
        //    if (localCorpusTree.getSelectionPath() != null) {
        //        selectedTreeNode = (DefaultMutableTreeNode) localCorpusTree.getSelectionPath().getLastPathComponent();
        //    }
        //    GuiHelper.treeHelper.removeSelectedLocation(selectedTreeNode);
    }
    //GEN-LAST:event_removeCachedCopyMenuItemActionPerformed

    private void searchSubnodesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        //GEN-FIRST:event_searchSubnodesMenuItemActionPerformed
        // TODO add your handling code here:    
        LinorgWindowManager.getSingleInstance().openSearchTable(((ImdiTree) TreeHelper.getSingleInstance().arbilTreePanel.localCorpusTree).getSelectedNodes(), "Search");

    }//GEN-LAST:event_searchSubnodesMenuItemActionPerformed

    private void sendToServerMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        //GEN-FIRST:event_sendToServerMenuItemActionPerformed
        // TODO add your handling code here:
    }
    //GEN-LAST:event_sendToServerMenuItemActionPerformed

    private void validateMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        for (ImdiTreeObject currentNode : selectedTreeNodes) {
            // todo: offer to save node first
            XsdChecker xsdChecker = new XsdChecker();
            LinorgWindowManager.getSingleInstance().createWindow("XsdChecker", xsdChecker);
            xsdChecker.checkXML(currentNode);
            xsdChecker.setDividerLocation(0.5);
        }
    }

    private void openFileInBrowser(ImdiTreeObject[] selectedNodes) {
        for (ImdiTreeObject currentNode : selectedNodes) {
            URI targetUri = null;
            if (currentNode.hasResource()) {
                targetUri = currentNode.getFullResourceURI();
            } else {
                targetUri = currentNode.getURI();
            }
            GuiHelper.getSingleInstance().openFileInExternalApplication(targetUri);
        }
    }

    public void initAddMenu(JMenu addMenu, Object targetNodeUserObject) {
        boolean menuItemsAdded = false;
        addMenu.removeAll();
//        System.out.println("initAddMenu: " + targetNodeUserObject);
        ArbilTemplate currentTemplate;
        if (targetNodeUserObject instanceof ImdiTreeObject && !((ImdiTreeObject) targetNodeUserObject).isCorpus()) {
            ImdiIcons imdiIcons = ImdiIcons.getSingleInstance();
            currentTemplate = ((ImdiTreeObject) targetNodeUserObject).getNodeTemplate();
            for (Enumeration menuItemName = currentTemplate.listTypesFor(targetNodeUserObject); menuItemName.hasMoreElements();) {
                String[] currentField = (String[]) menuItemName.nextElement();
//            System.out.println("MenuText: " + currentField[0]);
//            System.out.println("ActionCommand: " + currentField[1]);

                JMenuItem addMenuItem;
                addMenuItem = new JMenuItem();
                addMenuItem.setText(currentField[0]);
                addMenuItem.setName(currentField[0]);
                addMenuItem.setToolTipText(currentField[1]);
                addMenuItem.setActionCommand(currentField[1]);
                addMenuItem.setIcon(imdiIcons.dataIcon);
                addMenuItem.addActionListener(new java.awt.event.ActionListener() {

                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        try {
//                        boolean nodesFoundToAddTo = false;
//                        for (ImdiTreeObject currentNode : selectedTreeNodes) {
//                            if (currentNode != null) {
//                                currentNode.requestAddNode(evt.getActionCommand(), ((JMenuItem) evt.getSource()).getText());
//                                nodesFoundToAddTo = true;
//                            }
//                        }
//                        if (!nodesFoundToAddTo) {
//                            // no nodes found that were valid imdi tree objects so we can assume that tis is the tree root
//                            ImdiTreeObject imdiTreeObject;
//                            imdiTreeObject = new ImdiTreeObject(LinorgSessionStorage.getSingleInstance().getSaveLocation(LinorgSessionStorage.getSingleInstance().getNewImdiFileName()));
//                            imdiTreeObject.requestAddNode(evt.getActionCommand(), ((JMenuItem) evt.getSource()).getText());
//                        }
                            if (leadSelectedTreeNode != null) {
                                new MetadataBuilder().requestAddNode(leadSelectedTreeNode, evt.getActionCommand(), ((JMenuItem) evt.getSource()).getText());
                            } else {
                                // no nodes found that were valid imdi tree objects so we can assume that tis is the tree root
                                new MetadataBuilder().requestRootAddNode(evt.getActionCommand(), ((JMenuItem) evt.getSource()).getText());
                            }
                        } catch (Exception ex) {
                            GuiHelper.linorgBugCatcher.logError(ex);
                        }
                    }
                });
                addMenu.add(addMenuItem);
                menuItemsAdded = true;
            }
        } else {
            // consume the selected templates here rather than the clarin profile list
            for (MenuItemData currentAddable : ArbilTemplateManager.getSingleInstance().getSelectedTemplates()) {
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
                            GuiHelper.linorgBugCatcher.logError(ex);
                        }
                    }
                });
                addMenu.add(addMenuItem);
            }


            //if ((targetNodeUserObject instanceof ImdiTreeObject && ((ImdiTreeObject) targetNodeUserObject).isCorpus()) || !(targetNodeUserObject instanceof ImdiTreeObject)) {
            // Allow clarin add menu for corpus nodes and the tree root node
//                CmdiProfileReader cmdiProfileReader = new CmdiProfileReader();
//                if (menuItemsAdded && cmdiProfileReader.cmdiProfileArray.size() > 0) {
//                    addMenu.add(new JSeparator());
//                }
//                JMenu clarinAddMenu = new JMenu("Clarin Profiles");
//                addMenu.add(clarinAddMenu);
//                for (CmdiProfileReader.CmdiProfile currentCmdiProfile : cmdiProfileReader.cmdiProfileArray) {
//                    JMenuItem addMenuItem;
//                    addMenuItem = new JMenuItem();
//                    addMenuItem.setText(currentCmdiProfile.name);
//                    addMenuItem.setName(currentCmdiProfile.name);
//                    addMenuItem.setActionCommand(currentCmdiProfile.getXsdHref());
//                    addMenuItem.setToolTipText(currentCmdiProfile.description);
//                    addMenuItem.addActionListener(new java.awt.event.ActionListener() {
//
//                        public void actionPerformed(java.awt.event.ActionEvent evt) {
//                            try {
//                                if (leadSelectedTreeNode != null) {
//                                    leadSelectedTreeNode.requestAddNode(evt.getActionCommand(), ((JMenuItem) evt.getSource()).getText());
//                                } else {
//                                    // no nodes found that were valid imdi tree objects so we can assume that tis is the tree root
//                                    ImdiTreeObject.requestRootAddNode(evt.getActionCommand(), ((JMenuItem) evt.getSource()).getText());
//                                }
//                            } catch (Exception ex) {
//                                GuiHelper.linorgBugCatcher.logError(ex);
//                            }
//                        }
//                    });
//                    clarinAddMenu.add(addMenuItem);
//                }
//                clarinAddMenu.add(new JSeparator());
//                JMenuItem reloadProfilesMenuItem;
//                reloadProfilesMenuItem = new JMenuItem();
//                reloadProfilesMenuItem.setText("<Reload This List>");
//                reloadProfilesMenuItem.setToolTipText("Reload this Clarin profile list from the server");
//                reloadProfilesMenuItem.addActionListener(new java.awt.event.ActionListener() {
//
//                    public void actionPerformed(java.awt.event.ActionEvent evt) {
//                        CmdiProfileReader cmdiProfileReader = new CmdiProfileReader();
//                        cmdiProfileReader.refreshProfiles();
//                    }
//                });
//                clarinAddMenu.add(reloadProfilesMenuItem);
            //  }
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
                            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Could not revert version, no changes made", "History");
                        }
                    } catch (Exception ex) {
                        GuiHelper.linorgBugCatcher.logError(ex);
                    }
                }
            });
            historyMenu.add(revertHistoryMenuItem);
        }
    }

    public void initAddFromFavouritesMenu() {
        addFromFavouritesMenu.removeAll();
        for (Enumeration menuItemName = LinorgFavourites.getSingleInstance().listFavouritesFor(leadSelectedTreeNode); menuItemName.hasMoreElements();) {
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
                        String imdiFavouriteUrlString = evt.getActionCommand();
                        ImdiTreeObject templateImdiObject = ImdiLoader.getSingleInstance().getImdiObject(null, ImdiTreeObject.conformStringToUrl(imdiFavouriteUrlString));
                        if (leadSelectedTreeNode != null) {
                            new MetadataBuilder().requestAddNode(leadSelectedTreeNode, ((JMenuItem) evt.getSource()).getText(), templateImdiObject);
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
                        GuiHelper.linorgBugCatcher.logError(ex);
                    }
                }
            });
            addFromFavouritesMenu.add(addFavouriteMenuItem);
        }
    }

    public void showTreePopup(Object eventSource, int posX, int posY) {
        if (((java.awt.Component) eventSource).isShowing()) {
            // set up the context menu
            removeCachedCopyMenuItem.setVisible(false);
            removeLocalDirectoryMenuItem.setVisible(false);
            addLocalDirectoryMenuItem.setVisible(false);
            showHiddenFilesMenuItem.setVisible(false);
            removeRemoteCorpusMenuItem.setVisible(false);
            addRemoteCorpusMenuItem.setVisible(false);
            copyBranchMenuItem.setVisible(false);
            copyImdiUrlMenuItem.setVisible(false);
            pasteMenuItem1.setVisible(false);
            viewXmlMenuItem.setVisible(false);
            viewXmlMenuItemFormatted.setVisible(false);
            openXmlMenuItemFormatted.setVisible(false);
            exportHtmlMenuItemFormatted.setVisible(false);
            overrideTypeCheckerDecision.setVisible(false);
            viewInBrrowserMenuItem.setVisible(false);
            browseForResourceFileMenuItem.setVisible(false);
            searchSubnodesMenuItem.setVisible(false);
            reloadSubnodesMenuItem.setVisible(false);
            addDefaultLocationsMenuItem.setVisible(false);
            addMenu.setVisible(false);
            deleteMenuItem.setVisible(false);
            viewSelectedNodesMenuItem.setVisible(false);
            addFromFavouritesMenu.setVisible(false);
            saveMenuItem.setVisible(false);
            viewChangesMenuItem.setVisible(false);
            sendToServerMenuItem.setVisible(false);
            validateMenuItem.setVisible(false);
            historyMenu.setVisible(false);
            exportMenuItem.setVisible(false);
            importCsvMenuItem.setVisible(false);
            importBranchMenuItem.setVisible(false);
            reImportBranchMenuItem.setVisible(false);
            addToFavouritesMenuItem.setVisible(false);
            //////////
            // table menu items
            copySelectedRowsMenuItem.setVisible(false);
            pasteIntoSelectedRowsMenuItem.setVisible(false);
            viewSelectedRowsMenuItem.setVisible(false);
            matchingRowsMenuItem.setVisible(false);
            removeSelectedRowsMenuItem.setVisible(false);
            hideSelectedColumnsMenuItem.setVisible(false);
            deleteFieldMenuItem.setVisible(false);
            revertFieldMenuItem.setVisible(false);
            copyCellToColumnMenuItem.setVisible(false);
            matchingCellsMenuItem.setVisible(false);
            openInLongFieldEditorMenuItem.setVisible(false);
            clearCellColoursMenuItem.setVisible(false);
            searchReplaceMenuItem.setVisible(false);
            jumpToNodeInTreeMenuItem.setVisible(false);
            //////////
            // menu separators
//        treePopupMenuSeparator1.setVisible(true);
//        treePopupMenuSeparator2.setVisible(true);
//        cellMenuDivider.setVisible(true);
//        cellTableDivider.setVisible(true);
            //////////
            currentTable = null;
            if (eventSource instanceof ImdiTable) {
                currentTable = ((ImdiTable) eventSource);
                selectedTreeNodes = currentTable.getSelectedRowsFromTable();
                leadSelectedTreeNode = currentTable.getImdiNodeForSelection();
//            selectionCount = selectedTreeNodes.length;
                setupTableMenuItems();
            } else if (eventSource instanceof JList) {
                JList currentJList = ((JList) eventSource);
                Object[] selectedObjects = currentJList.getSelectedValues();
                selectedTreeNodes = new ImdiTreeObject[selectedObjects.length];
                for (int objectCounter = 0; objectCounter < selectedObjects.length; objectCounter++) {
                    selectedTreeNodes[objectCounter] = (ImdiTreeObject) selectedObjects[objectCounter];
                }
                leadSelectedTreeNode = (ImdiTreeObject) currentJList.getSelectedValue();
//            selectionCount = selectedTreeNodes.length;
                setUpImagePreviewMenu();
            } else if (eventSource instanceof ImdiTree) {

                selectedTreeNodes = ((ImdiTree) eventSource).getSelectedNodes();
                leadSelectedTreeNode = ((ImdiTree) eventSource).getLeadSelectionNode();
                setUpTreeMenuItems(eventSource);
            }
            setCommonMenuItems();

            if (eventSource instanceof Component) {
                // store the event source
                treePopupMenu.setInvoker((Component) eventSource);
            }
            configureMenuSeparators();
            // show the context menu
            treePopupMenu.show((java.awt.Component) eventSource, posX, posY);
            treePopupMenu.requestFocusInWindow();
        }
    }

    private void configureMenuSeparators() {
        // hide and show the separators so that no two separators are displayed without a menu item inbetween
        boolean lastWasSeparator = true;
        Component lastVisibleComponent = null;
        for (Component currentComponent : treePopupMenu.getComponents()) {
            if (currentComponent instanceof JSeparator) {
//                if (lastWasSeparator == true) {
                currentComponent.setVisible(!lastWasSeparator);
//                }
                lastWasSeparator = true;
            } else if (currentComponent.isVisible()) {
                lastWasSeparator = false;
            }
            if (currentComponent.isVisible()) {
                lastVisibleComponent = currentComponent;
            }
        }
        if (lastVisibleComponent != null && lastVisibleComponent instanceof JSeparator) {
            lastVisibleComponent.setVisible(false);
        }
    }

    private void setCommonMenuItems() {
//        todo: continue moving common menu items here
        if (leadSelectedTreeNode != null) {
            // TODO: test that the node is editable
            //if (leadSelectedTreeNode.is)
            if (leadSelectedTreeNode.hasResource()) {
                browseForResourceFileMenuItem.setVisible(true);
            }
            if (!leadSelectedTreeNode.isImdiChild() && leadSelectedTreeNode.isMetaDataNode()) {
                viewXmlMenuItem.setVisible(true);
                viewXmlMenuItemFormatted.setVisible(true);
                openXmlMenuItemFormatted.setVisible(true);
                exportHtmlMenuItemFormatted.setVisible(true);
            }
            viewInBrrowserMenuItem.setVisible(true);
            overrideTypeCheckerDecision.setVisible(!leadSelectedTreeNode.isMetaDataNode() && leadSelectedTreeNode.mpiMimeType == null);
        }
    }

    private void setUpTreeMenuItems(Object eventSource) {
        int nodeLevel = -1;
        int selectionCount = 0;
//        boolean showContextMenu = true;
        boolean showRemoveLocationsTasks = false;
        boolean showAddLocationsTasks = false;
        selectionCount = ((JTree) eventSource).getSelectionCount();
        if (selectionCount > 0) {
            nodeLevel = ((JTree) eventSource).getSelectionPath().getPathCount();
        }
        showRemoveLocationsTasks = (selectionCount == 1 && nodeLevel == 2) || selectionCount > 1;
        showAddLocationsTasks = selectionCount == 1 && nodeLevel == 1;
//        Object leadSelectedTreeObject = ((ImdiTree) eventSource).getSingleSelectedNode();
        //System.out.println("path count: " + ((JTree) evt.getSource()).getSelectionPath().getPathCount());

        viewSelectedNodesMenuItem.setText("View Selected");
        mergeWithFavouritesMenu.setEnabled(false);
        deleteMenuItem.setEnabled(true);

        if (eventSource == TreeHelper.getSingleInstance().arbilTreePanel.remoteCorpusTree) {
            removeRemoteCorpusMenuItem.setVisible(showRemoveLocationsTasks);
            addRemoteCorpusMenuItem.setVisible(showAddLocationsTasks);
            copyBranchMenuItem.setVisible(selectionCount > 0 && nodeLevel > 1);
            addDefaultLocationsMenuItem.setVisible(showAddLocationsTasks);
        }
        if (eventSource == TreeHelper.getSingleInstance().arbilTreePanel.localCorpusTree) {
            viewSelectedNodesMenuItem.setText("View/Edit Selected");
            //removeCachedCopyMenuItem.setVisible(showRemoveLocationsTasks);
            pasteMenuItem1.setVisible(selectionCount > 0 && nodeLevel > 1);
            searchSubnodesMenuItem.setVisible(selectionCount > 0 && nodeLevel > 1);
            // a corpus can be added even at the root node
            addMenu.setVisible(selectionCount == 1); // && /*nodeLevel > 1 &&*/ TreeHelper.getSingleInstance().arbilTreePanel.localCorpusTree.getSelectionCount() > 0/* && ((DefaultMutableTreeNode)localCorpusTree.getSelectionPath().getLastPathComponent()).getUserObject() instanceof */); // could check for imdi childnodes
//            addMenu.setEnabled(nodeLevel > 1); // not yet functional so lets dissable it for now
//            addMenu.setToolTipText("test balloon on dissabled menu item");
            deleteMenuItem.setVisible(nodeLevel > 1);
            boolean nodeIsImdiChild = false;
            if (leadSelectedTreeNode != null) {
                nodeIsImdiChild = leadSelectedTreeNode.isImdiChild();
                //if (leadSelectedTreeNode.getNeedsSaveToDisk()) {
                // saveMenuItem.setVisible(true);
                //} else if (leadSelectedTreeNode.hasHistory()) {
                //viewChangesMenuItem.setVisible(true);
                //sendToServerMenuItem.setVisible(true);
                //}
                validateMenuItem.setVisible(!nodeIsImdiChild);
                historyMenu.setVisible(leadSelectedTreeNode.hasHistory());
                exportMenuItem.setVisible(!nodeIsImdiChild);
                importCsvMenuItem.setVisible(leadSelectedTreeNode.isCorpus());
                importBranchMenuItem.setVisible(leadSelectedTreeNode.isCorpus());
                reImportBranchMenuItem.setVisible(leadSelectedTreeNode.hasArchiveHandle && !leadSelectedTreeNode.isImdiChild());

                // set up the favourites menu
                addFromFavouritesMenu.setVisible(true);
            }
            //deleteMenuItem.setEnabled(!nodeIsImdiChild && selectionCount == 1);
//            addMenu.setEnabled(!nodeIsImdiChild);
//            showContextMenu = true; //nodeLevel != 1;
        }
        if (eventSource == TreeHelper.getSingleInstance().arbilTreePanel.localDirectoryTree) {
            removeLocalDirectoryMenuItem.setVisible(showRemoveLocationsTasks);
            if (showAddLocationsTasks) {
                showHiddenFilesMenuItem.setState(TreeHelper.getSingleInstance().showHiddenFilesInTree);
                showHiddenFilesMenuItem.setVisible(true);
            }
            addLocalDirectoryMenuItem.setVisible(showAddLocationsTasks);
            if (leadSelectedTreeNode != null) {
                copyBranchMenuItem.setVisible(leadSelectedTreeNode.isCorpus() || leadSelectedTreeNode.isSession());
            }
        }
        if (leadSelectedTreeNode != null) {
            saveMenuItem.setVisible(leadSelectedTreeNode.getNeedsSaveToDisk());// save sould always be available if the node has been edited
            if (leadSelectedTreeNode.isFavorite()) {
                addToFavouritesMenuItem.setVisible(true);
                addToFavouritesMenuItem.setEnabled(true);
                addMenu.setVisible(selectedTreeNodes.length == 1);// for now adding is limited to single node selections
                viewSelectedNodesMenuItem.setText("View/Edit Selected");
                addToFavouritesMenuItem.setText("Remove From Favourites List");
                addToFavouritesMenuItem.setActionCommand("false");
                deleteMenuItem.setEnabled(false);
            } else {
                addToFavouritesMenuItem.setVisible(leadSelectedTreeNode.isMetaDataNode());
                addToFavouritesMenuItem.setEnabled(!leadSelectedTreeNode.isCorpus() && leadSelectedTreeNode.isMetaDataNode());
                addToFavouritesMenuItem.setText("Add To Favourites List");
                addToFavouritesMenuItem.setActionCommand("true");
            }
        } else {
            addToFavouritesMenuItem.setVisible(false);
        }

        copyImdiUrlMenuItem.setVisible((selectionCount == 1 && nodeLevel > 1) || selectionCount > 1); // show the copy menu providing some nodes are selected and the root node is not the only one selected

        viewSelectedNodesMenuItem.setVisible(selectionCount >= 1 && nodeLevel > 1);
        reloadSubnodesMenuItem.setVisible(selectionCount > 0 && nodeLevel > 1);

        // hide show the separators
        //treePopupMenuSeparator2.setVisible(nodeLevel != 1 && showRemoveLocationsTasks && eventSource != TreeHelper.getSingleInstance().arbilTreePanel.localDirectoryTree);
        //treePopupMenuSeparator1.setVisible(nodeLevel != 1 && eventSource == TreeHelper.getSingleInstance().arbilTreePanel.localCorpusTree);
    }

    private void setupTableMenuItems() {
        if (currentTable != null) {
            if (currentTable.getSelectedRow() != -1) {
                copySelectedRowsMenuItem.setVisible(true);
                pasteIntoSelectedRowsMenuItem.setVisible(true);
                if (currentTable.imdiTableModel.horizontalView) {
                    viewSelectedRowsMenuItem.setVisible(true);
                    matchingRowsMenuItem.setVisible(true);
                    removeSelectedRowsMenuItem.setVisible(true);
                }
                boolean canDeleteSelectedFields = true;
                ImdiField[] currentSelection = currentTable.getSelectedFields();
                for (ImdiField currentField : currentSelection) {
                    if (!currentField.parentImdi.getNodeTemplate().pathIsDeleteableField(currentField.getGenericFullXmlPath())) {
                        canDeleteSelectedFields = false;
                        break;
                    }
                }
                if (canDeleteSelectedFields && currentSelection.length > 0) {
                    String menuText = "Delete " + currentSelection[0].getTranslateFieldName();
                    if (currentSelection.length > 1) {
                        menuText = menuText + " X " + currentSelection.length;
                    }
                    deleteFieldMenuItem.setText(menuText);
                    deleteFieldMenuItem.setVisible(true);
                }

                // set up the revert field menu
                for (ImdiField currentField : currentSelection) {
                    if (currentField.fieldNeedsSaveToDisk()) {
                        revertFieldMenuItem.setVisible(true);
                        break;
                    }
                }
            }
            if (currentTable.getSelectedRow() != -1 && currentTable.getSelectedColumn() != -1) {
                // add a divider for the cell functions
                //cellMenuDivider.setVisible(true);
                if (currentTable.imdiTableModel.horizontalView && currentTable.getSelectionModel().getSelectionMode() == ListSelectionModel.SINGLE_INTERVAL_SELECTION) {
                    copyCellToColumnMenuItem.setVisible(true);
                    hideSelectedColumnsMenuItem.setVisible(true);
                }
                if (!currentTable.imdiTableModel.horizontalView || currentTable.getSelectionModel().getSelectionMode() == ListSelectionModel.SINGLE_INTERVAL_SELECTION) {
                    // show the cell only menu items
                    openInLongFieldEditorMenuItem.setVisible(true); // this should not show for the node icon cell
                    matchingCellsMenuItem.setVisible(true);
                }
                jumpToNodeInTreeMenuItem.setVisible(true);
                clearCellColoursMenuItem.setVisible(true);
            }
            if (currentTable.getParent().getParent().getParent().getParent() instanceof LinorgSplitPanel) {
                // test the LinorgSplitPanel exists before showing this
                searchReplaceMenuItem.setVisible(true);
            }
        }
    }

    private void setUpImagePreviewMenu() {
    }

    public static void main(String args[]) {
        new ContextMenu().treePopupMenu.setVisible(true);
    }
}
