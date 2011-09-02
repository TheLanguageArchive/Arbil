package nl.mpi.arbil.ui.menu;

import java.util.ArrayList;
import java.util.Hashtable;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import nl.mpi.arbil.data.ArbilComponentBuilder;
import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilTreeHelper;
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
	leadSelectedTreeNode = table.getDataNodeForSelection();
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
	    openInLongFieldEditorMenuItem.setVisible(true);
	    if (table.getArbilTableModel().isHorizontalView()) {
		viewSelectedRowsMenuItem.setVisible(true);
		matchingRowsMenuItem.setVisible(true);
		removeSelectedRowsMenuItem.setVisible(true);
		showChildNodesMenuItem.setVisible(true);
	    }
	    boolean canDeleteSelectedFields = true;
	    ArbilField[] currentSelection = table.getSelectedFields();
	    for (ArbilField currentField : currentSelection) {
		if (!currentField.getParentDataNode().getNodeTemplate().pathIsDeleteableField(currentField.getGenericFullXmlPath())) {
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
	    if (table.getArbilTableModel().isHorizontalView() && table.getSelectionModel().getSelectionMode() == ListSelectionModel.SINGLE_INTERVAL_SELECTION) {
		copyCellToColumnMenuItem.setVisible(true);
		hideSelectedColumnsMenuItem.setVisible(true);
	    }
	    //if (!table.arbilTableModel.isHorizontalView() || table.getSelectionModel().getSelectionMode() == ListSelectionModel.SINGLE_INTERVAL_SELECTION) {
	    // show the cell only menu items
	    if (!table.getArbilTableModel().isHorizontalView() || table.getSelectionModel().getSelectionMode() == ListSelectionModel.SINGLE_INTERVAL_SELECTION) {
		// show the cell only menu items
		matchingCellsMenuItem.setVisible(true);
	    }
	    jumpToNodeInTreeMenuItem.setVisible(false); // TODO: if functional, set to true here!
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
	addItem(CATEGORY_EDIT, PRIORITY_BOTTOM + 5, copySelectedRowsMenuItem);

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
	addItem(CATEGORY_EDIT, PRIORITY_BOTTOM + 10, pasteIntoSelectedRowsMenuItem);

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
	addItem(CATEGORY_TABLE_CELL, PRIORITY_TOP + 15, hideSelectedColumnsMenuItem);

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

	deleteFieldMenuItem.setText("Delete MultiField");
	deleteFieldMenuItem.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    ArbilField[] selectedFields = table.getSelectedFields();
		    if (selectedFields != null) {
//                                  to delete these fields they must be separated into imdi tree objects and request delete for each one
//                                  todo: the delete field action should also be available in the long field editor
			Hashtable<ArbilDataNode, ArrayList> selectedFieldHashtable = new Hashtable<ArbilDataNode, ArrayList>();
			for (ArbilField currentField : selectedFields) {
			    ArrayList currentList = selectedFieldHashtable.get(currentField.getParentDataNode());
			    if (currentList == null) {
				currentList = new ArrayList();
				selectedFieldHashtable.put(currentField.getParentDataNode(), currentList);
			    }
			    currentList.add(currentField.getFullXmlPath());
			}
			for (ArbilDataNode currentDataNode : selectedFieldHashtable.keySet()) {
			    ArbilComponentBuilder componentBuilder = new ArbilComponentBuilder();
			    boolean result = componentBuilder.removeChildNodes(currentDataNode, (String[]) selectedFieldHashtable.get(currentDataNode).toArray(new String[]{}));
			    if (result) {
				currentDataNode.reloadNode();
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

	copyCellToColumnMenuItem.setText("Copy Cell to Whole Column"); // NOI18N
	copyCellToColumnMenuItem.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    // TODO: change this to copy to selected rows
		    if (!(table.getArbilTableModel().getTableCellContentAt(table.getSelectedRow(), table.getSelectedColumn()) instanceof ArbilField)) {
			ArbilWindowManager.getSingleInstance().addMessageDialogToQueue("Cannot copy this type of field", "Copy Cell to Whole Column");
		    } else if (0 == JOptionPane.showConfirmDialog(ArbilWindowManager.getSingleInstance().linorgFrame, "About to replace all values in column \"" + table.getArbilTableModel().getColumnName(table.getSelectedColumn()) + "\"\nwith the value \"" + table.getArbilTableModel().getValueAt(table.getSelectedRow(), table.getSelectedColumn()) + "\"\n(<multiple values> will not be affected)", "Copy cell to whole column", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE)) {
			table.getArbilTableModel().copyCellToColumn(table.getSelectedRow(), table.getSelectedColumn());
		    }
		} catch (Exception ex) {
		    GuiHelper.linorgBugCatcher.logError(ex);
		}
	    }
	});
	addItem(CATEGORY_EDIT, PRIORITY_BOTTOM, copyCellToColumnMenuItem);

	matchingCellsMenuItem.setText("Highlight Matching Cells");
	matchingCellsMenuItem.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    table.getArbilTableModel().highlightMatchingCells(table.getSelectedRow(), table.getSelectedColumn());
		} catch (Exception ex) {
		    GuiHelper.linorgBugCatcher.logError(ex);
		}
	    }
	});

	clearCellColoursMenuItem.setText("Clear Cell Highlight");
	clearCellColoursMenuItem.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    table.getArbilTableModel().clearCellColours();
		} catch (Exception ex) {
		    GuiHelper.linorgBugCatcher.logError(ex);
		}
	    }
	});
	searchReplaceMenuItem.setText("Find/Replace");
	searchReplaceMenuItem.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    ((ArbilSplitPanel) table.getParent().getParent().getParent().getParent()).showSearchPane();
		} catch (Exception ex) {
		    GuiHelper.linorgBugCatcher.logError(ex);
		}
	    }
	});

	viewSelectedRowsMenuItem.setText("View Selected Rows");
	viewSelectedRowsMenuItem.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    table.viewSelectedTableRows();
		} catch (Exception ex) {
		    GuiHelper.linorgBugCatcher.logError(ex);
		}
	    }
	});

	matchingRowsMenuItem.setText("Select Matching Rows"); // NOI18N
	matchingRowsMenuItem.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    table.highlightMatchingRows();
		} catch (Exception ex) {
		    GuiHelper.linorgBugCatcher.logError(ex);
		}
	    }
	});

	removeSelectedRowsMenuItem.setText("Remove Selected Rows");
	removeSelectedRowsMenuItem.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    table.removeSelectedRowsFromTable();
		} catch (Exception ex) {
		    GuiHelper.linorgBugCatcher.logError(ex);
		}
	    }
	});
	jumpToNodeInTreeMenuItem.setText("Jump to in Tree"); // NOI18N
	jumpToNodeInTreeMenuItem.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    ArbilTreeHelper.getSingleInstance().jumpToSelectionInTree(false, table.getDataNodeForSelection());
		} catch (Exception ex) {
		    GuiHelper.linorgBugCatcher.logError(ex);
		}
	    }
	});
	jumpToNodeInTreeMenuItem.setEnabled(false);

	addItem(CATEGORY_TABLE_CELL, PRIORITY_TOP + 10, openInLongFieldEditorMenuItem);
	addItem(CATEGORY_TABLE_CELL, PRIORITY_TOP + 15, hideSelectedColumnsMenuItem);
	addItem(CATEGORY_TABLE_CELL, PRIORITY_TOP + 20, showChildNodesMenuItem);
	addItem(CATEGORY_TABLE_CELL, PRIORITY_TOP + 25, copyCellToColumnMenuItem);
	addItem(CATEGORY_TABLE_CELL, PRIORITY_TOP + 30, matchingCellsMenuItem);
	addItem(CATEGORY_TABLE_CELL, PRIORITY_TOP + 35, clearCellColoursMenuItem);

	addItem(CATEGORY_TABLE_CELL, PRIORITY_BOTTOM + 10, searchReplaceMenuItem);
	addItem(CATEGORY_TABLE_CELL, PRIORITY_BOTTOM + 15, deleteFieldMenuItem);
	addItem(CATEGORY_TABLE_CELL, PRIORITY_BOTTOM + 20, revertFieldMenuItem);

	addItem(CATEGORY_TABLE_ROW, PRIORITY_TOP + 10, viewSelectedRowsMenuItem);
	addItem(CATEGORY_TABLE_ROW, PRIORITY_TOP + 15, matchingRowsMenuItem);
	addItem(CATEGORY_TABLE_ROW, PRIORITY_TOP + 20, removeSelectedRowsMenuItem);
	addItem(CATEGORY_TABLE_ROW, PRIORITY_BOTTOM + 10, jumpToNodeInTreeMenuItem);

	addItem(CATEGORY_EDIT, PRIORITY_MIDDLE + 10, copySelectedRowsMenuItem);
	addItem(CATEGORY_EDIT, PRIORITY_MIDDLE + 15, pasteIntoSelectedRowsMenuItem);
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
