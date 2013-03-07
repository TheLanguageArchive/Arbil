/*
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package nl.mpi.arbil.ui;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.ui.menu.TableContextMenu;
import nl.mpi.arbil.ui.menu.TableHeaderContextMenu;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.TreeHelper;
import nl.mpi.arbil.util.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilTableController {

    private final static Logger logger = LoggerFactory.getLogger(ArbilTableController.class);
    public static final String DELETE_ROW_ACTION_KEY = "deleteRow";
    /** Services used by controller */
    private final TreeHelper treeHelper;
    private final MessageDialogHandler dialogHandler;
    private final WindowManager windowManager;
    /** Listeners provided by controller */
    private final MouseListener tableMouseListener = new TableMouseListener();
    private final MouseListener tableHeaderMouseListener = new TableHeaderMouseListener();
    /** Actions provided by controller */
    private final Action deleteRowAction = new DeleteRowAction();

    public ArbilTableController(TreeHelper treeHelper, MessageDialogHandler dialogHandler, WindowManager windowManager) {
	this.treeHelper = treeHelper;
	this.dialogHandler = dialogHandler;
	this.windowManager = windowManager;
    }

    public void initKeyMapping(ArbilTable table) {
	table.getInputMap(JTable.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), DELETE_ROW_ACTION_KEY);
	table.getActionMap().put(DELETE_ROW_ACTION_KEY, deleteRowAction);
    }

    public MouseListener getTableMouseListener() {
	return tableMouseListener;
    }

    public MouseListener getTableHeaderMouseListener() {
	return tableHeaderMouseListener;
    }

    public void openNodesInNewTable(ArbilDataNode[] nodes, String fieldName, ArbilDataNode registeredOwner) {
	windowManager.openFloatingTableOnce(nodes, String.format("%s in %s", fieldName, registeredOwner.toString()));
    }

    private class DeleteRowAction extends AbstractAction {

	public void actionPerformed(ActionEvent e) {
	    treeHelper.deleteNodes(e.getSource());
	}
    }

    public void checkPopup(java.awt.event.MouseEvent evt, boolean checkSelection) {
	final ArbilTable table = (ArbilTable) evt.getSource();
	final ArbilTableModel tableModel = table.getArbilTableModel();

	if (!tableModel.hideContextMenuAndStatusBar && evt.isPopupTrigger() /* evt.getButton() == MouseEvent.BUTTON3 || evt.isMetaDown() */) {
	    // set the clicked cell selected
	    java.awt.Point p = evt.getPoint();
	    int clickedRow = table.rowAtPoint(p);
	    int clickedColumn = table.columnAtPoint(p);
	    boolean clickedRowAlreadySelected = table.isRowSelected(clickedRow);

	    if (checkSelection && !evt.isShiftDown() && !evt.isControlDown()) {
		// if it is the right mouse button and there is already a selection then do not proceed in changing the selection
		if (!(((evt.isPopupTrigger() /* evt.getButton() == MouseEvent.BUTTON3 || evt.isMetaDown() */) && clickedRowAlreadySelected))) {
		    if (clickedRow > -1 & clickedRow > -1) {
			// if the modifier keys are down then leave the selection alone for the sake of more normal behaviour
			table.getSelectionModel().clearSelection();
			// make sure the clicked cell is selected
//                        logger.debug("clickedRow: " + clickedRow + " clickedRow: " + clickedRow);
//                        getSelectionModel().addSelectionInterval(clickedRow, clickedRow);
//                        getColumnModel().getSelectionModel().addSelectionInterval(clickedColumn, clickedColumn);
			table.changeSelection(clickedRow, clickedColumn, false, evt.isShiftDown());
			// make sure the clicked cell is the lead selection
//                    getSelectionModel().setLeadSelectionIndex(rowIndex);
//                    getColumnModel().getSelectionModel().setLeadSelectionIndex(colIndex);
		    }
		}
	    }
	}

	if (evt.isPopupTrigger() /* evt.getButton() == MouseEvent.BUTTON3 || evt.isMetaDown() */) {
//                    targetTable = (JTable) evt.getComponent();
//                    logger.debug("set the current table");

	    TableCellEditor tableCellEditor = table.getCellEditor();
	    if (tableCellEditor != null) {
		tableCellEditor.stopCellEditing();
	    }
	    new TableContextMenu(table, this).show(evt.getX(), evt.getY());
	}
    }

    private class TableMouseListener extends MouseAdapter {

	@Override
	public void mousePressed(MouseEvent evt) {
	    checkPopup(evt, true);
	}

	@Override
	public void mouseReleased(MouseEvent evt) {
	    checkPopup(evt, true);
	}
    }

    private class TableHeaderMouseListener extends MouseAdapter {

	@Override
	public void mousePressed(MouseEvent evt) {
	    checkTableHeaderPopup(evt);
	}

	@Override
	public void mouseReleased(MouseEvent evt) {
	    checkTableHeaderPopup(evt);
	}

	@Override
	public void mouseClicked(java.awt.event.MouseEvent evt) {
	    final JTableHeader tableHeader = (JTableHeader) evt.getSource();
	    final ArbilTable table = (ArbilTable) tableHeader.getTable();
	    final ArbilTableModel tableModel = table.getArbilTableModel();

	    if (evt.getButton() == MouseEvent.BUTTON1) {
		tableModel.sortByColumn(table.convertColumnIndexToModel(((JTableHeader) evt.getComponent()).columnAtPoint(new Point(evt.getX(), evt.getY()))));
		table.getTableHeader().revalidate();
	    }
	    checkTableHeaderPopup(evt);
	}

	private void checkTableHeaderPopup(java.awt.event.MouseEvent evt) {
	    final JTableHeader tableHeader = (JTableHeader) evt.getSource();
	    final ArbilTable table = (ArbilTable) tableHeader.getTable();
	    final ArbilTableModel tableModel = table.getArbilTableModel();

	    if (!tableModel.hideContextMenuAndStatusBar && evt.isPopupTrigger()) {
		final int targetColumn = table.convertColumnIndexToModel(((JTableHeader) evt.getComponent()).columnAtPoint(new Point(evt.getX(), evt.getY())));
		logger.debug("showing header menu for column {}", targetColumn);
		final JPopupMenu popupMenu = new TableHeaderContextMenu(table, tableModel, targetColumn, dialogHandler, windowManager);
		popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
	    }
	}
    }
}
