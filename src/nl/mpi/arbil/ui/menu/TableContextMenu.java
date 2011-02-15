package nl.mpi.arbil.ui.menu;

import java.util.ArrayList;
import java.util.Hashtable;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;
import nl.mpi.arbil.data.ArbilComponentBuilder;
import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.data.ArbilNodeObject;
import nl.mpi.arbil.ui.ArbilSplitPanel;
import nl.mpi.arbil.ui.ArbilTable;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.ui.GuiHelper;

/**
 * Context menu for table UI components
 *
 * @author Twan Goosen
 */
public class TableContextMenu extends ArbilContextMenu {

    public TableContextMenu(ArbilTable table) {
        super();
        this.table = table;
        setInvoker(table);

        selectedTreeNodes = table.getSelectedRowsFromTable();
        leadSelectedTreeNode = table.getImdiNodeForSelection();
    }

    @Override
    protected void setUpMenu() {
        setUpItems();
        setUpActions();
    }

    private void setUpItems() {
        if (table.getSelectedRow() != -1) {
            copySelectedRowsMenuItem.setVisible(true);
            pasteIntoSelectedRowsMenuItem.setVisible(true);
            if (table.imdiTableModel.isHorizontalView()) {
                viewSelectedRowsMenuItem.setVisible(true);
                matchingRowsMenuItem.setVisible(true);
                removeSelectedRowsMenuItem.setVisible(true);
                showChildNodesMenuItem.setVisible(true);
            }
            boolean canDeleteSelectedFields = true;
            ArbilField[] currentSelection = table.getSelectedFields();
            for (ArbilField currentField : currentSelection) {
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
            for (ArbilField currentField : currentSelection) {
                if (currentField.fieldNeedsSaveToDisk()) {
                    revertFieldMenuItem.setVisible(true);
                    break;
                }
            }
        }
        if (table.getSelectedRow() != -1 && table.getSelectedColumn() != -1) {
            // add a divider for the cell functions
            //cellMenuDivider.setVisible(true);
            if (table.imdiTableModel.isHorizontalView() && table.getSelectionModel().getSelectionMode() == ListSelectionModel.SINGLE_INTERVAL_SELECTION) {
                copyCellToColumnMenuItem.setVisible(true);
                hideSelectedColumnsMenuItem.setVisible(true);
            }
            if (!table.imdiTableModel.isHorizontalView() || table.getSelectionModel().getSelectionMode() == ListSelectionModel.SINGLE_INTERVAL_SELECTION) {
                // show the cell only menu items
                openInLongFieldEditorMenuItem.setVisible(true); // this should not show for the node icon cell
                matchingCellsMenuItem.setVisible(true);
            }
            jumpToNodeInTreeMenuItem.setVisible(true);
            clearCellColoursMenuItem.setVisible(true);
        }
        if (table.getParent().getParent().getParent().getParent() instanceof ArbilSplitPanel) {
            // test the LinorgSplitPanel exists before showing this
            searchReplaceMenuItem.setVisible(true);
        }
    }

    private void setUpActions() {
        copySelectedRowsMenuItem.setText("Copy");
        copySelectedRowsMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    table.copySelectedTableRowsToClipBoard();
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        add(copySelectedRowsMenuItem);

        pasteIntoSelectedRowsMenuItem.setText("Paste");
        pasteIntoSelectedRowsMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    table.pasteIntoSelectedTableRowsFromClipBoard();
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        add(pasteIntoSelectedRowsMenuItem);

        add(new JSeparator());
        // field menu items
        openInLongFieldEditorMenuItem.setText("Open in Long Field Editor");
        openInLongFieldEditorMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    table.startLongFieldEditorForSelectedFields();
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        add(openInLongFieldEditorMenuItem);

        hideSelectedColumnsMenuItem.setText("Hide Selected Columns");
        hideSelectedColumnsMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    table.hideSelectedColumnsFromTable();
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        add(hideSelectedColumnsMenuItem);

        showChildNodesMenuItem.setText("Show Child Nodes");
        showChildNodesMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    table.showRowChildData();
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        add(showChildNodesMenuItem);

        deleteFieldMenuItem.setText("Delete MultiField");
        deleteFieldMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    ArbilField[] selectedFields = table.getSelectedFields();
                    if (selectedFields != null) {
//                                  to delete these fields they must be separated into imdi tree objects and request delete for each one
//                                  todo: the delete field action should also be available in the long field editor
                        Hashtable<ArbilNodeObject, ArrayList> selectedFieldHashtable = new Hashtable<ArbilNodeObject, ArrayList>();
                        for (ArbilField currentField : selectedFields) {
                            ArrayList currentList = selectedFieldHashtable.get(currentField.parentImdi);
                            if (currentList == null) {
                                currentList = new ArrayList();
                                selectedFieldHashtable.put(currentField.parentImdi, currentList);
                            }
                            currentList.add(currentField.getFullXmlPath());
                        }
                        for (ArbilNodeObject currentImdiObject : selectedFieldHashtable.keySet()) {
                            ArbilComponentBuilder componentBuilder = new ArbilComponentBuilder();
                            boolean result = componentBuilder.removeChildNodes(currentImdiObject, (String[]) selectedFieldHashtable.get(currentImdiObject).toArray(new String[]{}));
                            if (result) {
                                currentImdiObject.reloadNode();
                            } else {
                                ArbilWindowManager.getSingleInstance().addMessageDialogToQueue("Error deleting fields, check the log file via the help menu for more information.", "Delete Field");
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
        add(deleteFieldMenuItem);

        revertFieldMenuItem.setText("Revert Selected Fields");
        revertFieldMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    ArbilField[] selectedFields = table.getSelectedFields();
                    if (selectedFields != null) {
                        for (ArbilField currentField : selectedFields) {
                            currentField.revertChanges();
                        }
                    }
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        add(revertFieldMenuItem);

        copyCellToColumnMenuItem.setText("Copy Cell to Whole Column"); // NOI18N
        copyCellToColumnMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    // TODO: change this to copy to selected rows
                    if (!(table.imdiTableModel.getValueAt(table.getSelectedRow(), table.getSelectedColumn()) instanceof ArbilField)) {
                        ArbilWindowManager.getSingleInstance().addMessageDialogToQueue("Cannot copy this type of field", "Copy Cell to Whole Column");
                    } else if (0 == JOptionPane.showConfirmDialog(ArbilWindowManager.getSingleInstance().linorgFrame, "About to replace all values in column \"" + table.imdiTableModel.getColumnName(table.getSelectedColumn()) + "\"\nwith the value \"" + table.imdiTableModel.getValueAt(table.getSelectedRow(), table.getSelectedColumn()) + "\"\n(<multiple values> will not be affected)", "Copy cell to whole column", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE)) {
                        table.imdiTableModel.copyCellToColumn(table.getSelectedRow(), table.getSelectedColumn());
                    }
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        add(copyCellToColumnMenuItem);

        matchingCellsMenuItem.setText("Highlight Matching Cells");
        matchingCellsMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    table.imdiTableModel.highlightMatchingCells(table.getSelectedRow(), table.getSelectedColumn());
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        add(matchingCellsMenuItem);

        clearCellColoursMenuItem.setText("Clear Cell Highlight"); // NOI18N
        clearCellColoursMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    table.imdiTableModel.clearCellColours();
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
    }

    @Override
    protected void setAllInvisible() {
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
        showChildNodesMenuItem.setVisible(false);
    }
    private ArbilTable table;
    private JMenuItem copySelectedRowsMenuItem = new JMenuItem();
    private JMenuItem pasteIntoSelectedRowsMenuItem = new JMenuItem();
    private JMenuItem viewSelectedRowsMenuItem = new JMenuItem();
    private JMenuItem matchingRowsMenuItem = new JMenuItem();
    private JMenuItem removeSelectedRowsMenuItem = new JMenuItem();
    private JMenuItem hideSelectedColumnsMenuItem = new JMenuItem();
    private JMenuItem searchReplaceMenuItem = new JMenuItem();
    private JMenuItem deleteFieldMenuItem = new JMenuItem();
    private JMenuItem revertFieldMenuItem = new JMenuItem();
    private JMenuItem copyCellToColumnMenuItem = new JMenuItem();
    private JMenuItem matchingCellsMenuItem = new JMenuItem();
    private JMenuItem openInLongFieldEditorMenuItem = new JMenuItem();
    private JMenuItem clearCellColoursMenuItem = new JMenuItem();
    private JMenuItem jumpToNodeInTreeMenuItem = new JMenuItem();
    private JMenuItem showChildNodesMenuItem = new JMenuItem();
}
