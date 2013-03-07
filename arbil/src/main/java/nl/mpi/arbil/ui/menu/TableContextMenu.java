/**
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
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
package nl.mpi.arbil.ui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import javax.swing.ListSelectionModel;
import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.ui.ArbilSplitPanel;
import nl.mpi.arbil.ui.ArbilTable;
import nl.mpi.arbil.ui.ArbilTableController;
import nl.mpi.arbil.util.BugCatcherManager;

/**
 * Context menu for table UI components
 *
 * @author Twan Goosen
 */
public class TableContextMenu extends ArbilContextMenu {

    private final ArbilTableController tableController;

    public TableContextMenu(ArbilTable table, ArbilTableController tableController) {
	super();
	this.tableController = tableController;

	this.table = table;
	setInvoker(table);

	selectedTreeNodes = table.getSelectedRowsFromTable();
	leadSelectedDataNode = table.getDataNodeForSelection();
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
		showInContextMenuItem.setVisible(true);
		if (table.getSelectedColumn() == 0 && leadSelectedDataNode.isLocal()) {
		    deleteFromParentMenuItem.setVisible(true);
		}
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
		String menuText;
		if (currentSelection[0].isAttributeField()) {
		    menuText = java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Menus").getString("DELETE ATTRIBUTE {0} X {1}"), new Object[]{currentSelection[0].getTranslateFieldName(), currentSelection.length});
		} else {
		    menuText = java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Menus").getString("DELETE {0} X {1}"), new Object[]{currentSelection[0].getTranslateFieldName(), currentSelection.length});
		}
//		if (currentSelection.length > 1) {
//		    menuText = menuText + " X " + currentSelection.length;
//		}
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

	    jumpToNodeInTreeMenuItem.setVisible(leadSelectedDataNode.isLocal());
	    clearCellColoursMenuItem.setVisible(true);
	}
	if (table.getParent().getParent().getParent().getParent() instanceof ArbilSplitPanel) {
	    // test the LinorgSplitPanel exists before showing this
	    searchReplaceMenuItem.setVisible(true);
	}
    }

    private void setUpActions() {
	copySelectedRowsMenuItem.setText(java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Menus").getString("COPY"));
	copySelectedRowsMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    table.copySelectedTableRowsToClipBoard();
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	addItem(CATEGORY_EDIT, PRIORITY_BOTTOM + 5, copySelectedRowsMenuItem);

	pasteIntoSelectedRowsMenuItem.setText(java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Menus").getString("PASTE"));
	pasteIntoSelectedRowsMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    table.pasteIntoSelectedTableRowsFromClipBoard();
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	addItem(CATEGORY_EDIT, PRIORITY_BOTTOM + 10, pasteIntoSelectedRowsMenuItem);

	// field menu items
	openInLongFieldEditorMenuItem.setText(java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Menus").getString("OPEN IN LONG FIELD EDITOR"));
	openInLongFieldEditorMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    table.startLongFieldEditorForSelectedFields();
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	hideSelectedColumnsMenuItem.setText(java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Menus").getString("HIDE SELECTED COLUMNS"));
	hideSelectedColumnsMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    table.hideSelectedColumnsFromTable();
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	addItem(CATEGORY_TABLE_CELL_VIEW, PRIORITY_TOP + 15, hideSelectedColumnsMenuItem);

	showChildNodesMenuItem.setText(java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Menus").getString("SHOW CHILD NODES"));
	showChildNodesMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    tableController.showRowChildData(table.getArbilTableModel());
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});

	if (deleteFieldMenuItem.getText() == null || deleteFieldMenuItem.getText().length() == 0) {
	    deleteFieldMenuItem.setText(java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Menus").getString("DELETE MULTIFIELD"));
	}
	deleteFieldMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		tableController.deleteSelectedFields(table);
	    }
	});

	revertFieldMenuItem.setText(java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Menus").getString("REVERT SELECTED FIELDS"));
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
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});

	copyCellToColumnMenuItem.setText("Copy Cell to Whole Column"); // NOI18N
	copyCellToColumnMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		tableController.copySelectedCellToColumn(table);
	    }
	});
	addItem(CATEGORY_EDIT, PRIORITY_BOTTOM, copyCellToColumnMenuItem);

	matchingCellsMenuItem.setText(java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Menus").getString("HIGHLIGHT MATCHING CELLS"));
	matchingCellsMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    table.getArbilTableModel().highlightMatchingCells(table.getSelectedRow(), table.getSelectedColumn());
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	clearCellColoursMenuItem.setText(java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Menus").getString("CLEAR CELL HIGHLIGHT"));
	clearCellColoursMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    table.getArbilTableModel().clearCellColours();
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	searchReplaceMenuItem.setText(java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Menus").getString("FIND/REPLACE"));
	searchReplaceMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    ((ArbilSplitPanel) table.getParent().getParent().getParent().getParent()).showSearchPane();
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});

	viewSelectedRowsMenuItem.setText(java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Menus").getString("VIEW SELECTED ROWS"));
	viewSelectedRowsMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    tableController.viewSelectedTableRows(table);
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});

	matchingRowsMenuItem.setText("Select Matching Rows"); // NOI18N
	matchingRowsMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    table.highlightMatchingRows();
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});

	removeSelectedRowsMenuItem.setText(java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Menus").getString("REMOVE SELECTED ROWS"));
	removeSelectedRowsMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    table.removeSelectedRowsFromTable();
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});

	deleteFromParentMenuItem.setText("Delete Selected Nodes from Parent");
	deleteFromParentMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		tableController.deleteNodes(table);
	    }
	});

	jumpToNodeInTreeMenuItem.setText("Jump to in Tree");
	jumpToNodeInTreeMenuItem.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		tableController.jumpToSelectionInTree(table);
	    }
	});
	jumpToNodeInTreeMenuItem.setEnabled(true);

	showInContextMenuItem.setText("Show Context");
	showInContextMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		tableController.showContextForSelectedNodes(table);
	    }
	});

	addItem(CATEGORY_TABLE_CELL_VIEW, PRIORITY_TOP + 10, openInLongFieldEditorMenuItem);
	addItem(CATEGORY_TABLE_CELL_VIEW, PRIORITY_TOP + 15, hideSelectedColumnsMenuItem);
	addItem(CATEGORY_TABLE_CELL_VIEW, PRIORITY_TOP + 20, showChildNodesMenuItem);
	addItem(CATEGORY_TABLE_CELL_VIEW, PRIORITY_TOP + 25, showInContextMenuItem);
	addItem(CATEGORY_TABLE_CELL_EDIT, PRIORITY_TOP + 10, copyCellToColumnMenuItem);
	addItem(CATEGORY_TABLE_CELL_EDIT, PRIORITY_TOP + 20, matchingCellsMenuItem);
	addItem(CATEGORY_TABLE_CELL_EDIT, PRIORITY_TOP + 30, clearCellColoursMenuItem);

	addItem(CATEGORY_TABLE_CELL_EDIT, PRIORITY_MIDDLE + 10, searchReplaceMenuItem);
	addItem(CATEGORY_TABLE_CELL_EDIT, PRIORITY_MIDDLE + 15, deleteFieldMenuItem);
	addItem(CATEGORY_TABLE_CELL_EDIT, PRIORITY_MIDDLE + 20, revertFieldMenuItem);

	addItem(CATEGORY_TABLE_ROW, PRIORITY_TOP + 10, viewSelectedRowsMenuItem);
	addItem(CATEGORY_TABLE_ROW, PRIORITY_TOP + 15, matchingRowsMenuItem);
	addItem(CATEGORY_TABLE_ROW, PRIORITY_TOP + 20, removeSelectedRowsMenuItem);
	addItem(CATEGORY_TABLE_ROW, PRIORITY_MIDDLE, jumpToNodeInTreeMenuItem);
	addItem(CATEGORY_TABLE_ROW, PRIORITY_BOTTOM + 10, deleteFromParentMenuItem);

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
	showInContextMenuItem.setVisible(false);
	deleteFromParentMenuItem.setVisible(false);
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
    private JMenuItem showInContextMenuItem = new JMenuItem();
    private JMenuItem deleteFromParentMenuItem = new JMenuItem();
}
