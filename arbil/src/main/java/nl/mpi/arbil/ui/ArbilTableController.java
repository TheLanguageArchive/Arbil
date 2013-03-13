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

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import nl.mpi.arbil.ArbilMetadataException;
import nl.mpi.arbil.data.ArbilComponentBuilder;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.data.MetadataBuilder;
import nl.mpi.arbil.data.NodeCreationCallback;
import nl.mpi.arbil.templates.ArbilTemplate;
import nl.mpi.arbil.ui.fieldeditors.ArbilLongFieldEditor;
import nl.mpi.arbil.ui.menu.TableContextMenu;
import nl.mpi.arbil.ui.menu.TableHeaderContextMenu;
import nl.mpi.arbil.util.BugCatcherManager;
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
	windowManager.openFloatingTableOnce(nodes, String.format("%s in %s", fieldName, registeredOwner));
    }

    public void showRowChildData(ArbilTableModel tableModel) {
	Object[] possibilities = tableModel.getChildNames();
	String selectionResult = (String) JOptionPane.showInputDialog(windowManager.getMainFrame(), "Select the child node type to display", "Show child nodes", JOptionPane.PLAIN_MESSAGE, null, possibilities, null);
//      TODO: JOptionPane.show it would be good to have a miltiple select here
	if ((selectionResult != null) && (selectionResult.length() > 0)) {
	    tableModel.addChildTypeToDisplay(selectionResult);
	}
    }

    public void viewSelectedTableRows(ArbilTable table) {
	int[] selectedRows = table.getSelectedRows();
	windowManager.openFloatingTableOnce(table.getArbilTableModel().getSelectedDataNodes(selectedRows), null);
    }

    public void showColumnViewsEditor(ArbilTable table) {
	table.updateStoredColumnWidths();
	try {
	    ArbilFieldViewTable fieldViewTable = new ArbilFieldViewTable(table.getArbilTableModel());
	    JDialog editViewsDialog = new JDialog(JOptionPane.getFrameForComponent(windowManager.getMainFrame()), true);
	    editViewsDialog.setTitle("Editing Current Column View");
	    JScrollPane js = new JScrollPane(fieldViewTable);
	    editViewsDialog.getContentPane().add(js);
	    editViewsDialog.setBounds(50, 50, 600, 400);
	    editViewsDialog.setVisible(true);
	} catch (Exception ex) {
	    BugCatcherManager.getBugCatcher().logError(ex);
	}
    }

    public void saveCurrentColumnView(ArbilTable table) {
	try {
	    String fieldViewName = (String) JOptionPane.showInputDialog(null, "Enter a name to save this Column View as", "Save Column View", JOptionPane.PLAIN_MESSAGE);
	    // if the user did not cancel
	    if (fieldViewName != null) {
		table.updateStoredColumnWidths();
		if (!ArbilFieldViews.getSingleInstance().addArbilFieldView(fieldViewName, table.getArbilTableModel().getFieldView())) {
		    dialogHandler.addMessageDialogToQueue("A Column View with the same name already exists, nothing saved", "Save Column View");
		}
	    }
	} catch (Exception ex) {
	    BugCatcherManager.getBugCatcher().logError(ex);
	}
    }

    /**
     * Deletes the fields selected in the provided table from their parent nodes
     *
     * @param table table to get selection from
     * @return whether deletion was carried out
     */
    public boolean deleteSelectedFields(ArbilTable table) {
	final ArbilField[] selectedFields = table.getSelectedFields();
	if (selectedFields != null) {
	    return deleteFields(Arrays.asList(selectedFields));
	} else {
	    return false;
	}
    }

    /**
     * Attempts to delete the field represented by the specified column name from all nodes visible as rows in the table.
     * A check whether deletion of the field is allowed is done for each row/node; if one fails, a message gets presented and the entire
     * operation gets aborted
     *
     * @param table table to get nodes to delete from
     * @param columnName name of the column/field to remove
     * @return whether deletion was carried out
     */
    public boolean deleteColumnFieldFromAllNodes(ArbilTable table, String columnName) {
	final List<ArbilDataNode> rowNodes = Collections.list(table.getArbilTableModel().getArbilDataNodes());
	final List<ArbilField> fieldsToDelete = new ArrayList<ArbilField>(rowNodes.size());

	for (ArbilDataNode rowNode : rowNodes) {
	    final ArbilField[] fields = rowNode.getFields().get(columnName);
	    if (fields != null) {
		for (ArbilField field : fields) {
		    // Check whether deletion is allowed...
		    final ArbilTemplate fieldParentTemplate = field.getParentDataNode().getNodeTemplate();
		    final String fieldPath = field.getGenericFullXmlPath();
		    if (fieldParentTemplate.pathIsDeleteableField(fieldPath)) {
			// Add to deletion list
			fieldsToDelete.add(fields[0]);
		    } else {
			dialogHandler.addMessageDialogToQueue("This field cannot be deleted from one or more of the shown nodes", "Cannot delete");
			return false;
		    }
		}
	    }
	}
	if (dialogHandler.showConfirmDialogBox(String.format("Delete %d instance of the field '%s'?", fieldsToDelete.size(), columnName), "Delete field")) {
	    return deleteFields(fieldsToDelete);
	} else {
	    return false;
	}
    }

    private boolean deleteFields(Collection<ArbilField> fields) {
	// to delete these fields they must be separated into imdi tree objects and request delete for each one
	// todo: the delete field action should also be available in the long field editor
	final ArbilComponentBuilder componentBuilder = new ArbilComponentBuilder();
	try {
	    final Map<ArbilDataNode, List<String>> selectedFieldHashtable = getFieldDeletionMap(fields);
	    // delete sets of fields per parent node
	    for (ArbilDataNode currentDataNode : selectedFieldHashtable.keySet()) {
		final String[] fieldsToDelete = selectedFieldHashtable.get(currentDataNode).toArray(new String[]{});
		if (componentBuilder.removeChildNodes(currentDataNode, fieldsToDelete)) {
		    currentDataNode.reloadNode();
		} else {
		    dialogHandler.addMessageDialogToQueue(java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Menus").getString("ERROR DELETING FIELDS, CHECK THE LOG FILE VIA THE HELP MENU FOR MORE INFORMATION."), java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Menus").getString("DELETE FIELD"));
		}
	    }
	    return true;
	} catch (Exception ex) {
	    BugCatcherManager.getBugCatcher().logError(ex);
	    return false;
	}
    }

    private Map<ArbilDataNode, List<String>> getFieldDeletionMap(Collection<ArbilField> fields) {
	final Map<ArbilDataNode, List<String>> selectedFieldHashtable = new HashMap<ArbilDataNode, List<String>>();
	for (ArbilField currentField : fields) {
	    final ArbilDataNode fieldParent = currentField.getParentDataNode();
	    // Get list of paths to delete for this field's parent. Create one if needed
	    if (!selectedFieldHashtable.containsKey(fieldParent)) {
		selectedFieldHashtable.put(fieldParent, new ArrayList<String>());
	    }
	    final List<String> fieldParentList = selectedFieldHashtable.get(fieldParent);
	    // add the path of this field to the list
	    fieldParentList.add(currentField.getFullXmlPath());
	}
	return selectedFieldHashtable;
    }

    public void copySelectedCellToColumn(ArbilTable table) {
	try {
	    final String messageString = java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Menus").getString("ABOUT TO REPLACE ALL VALUES IN COLUMN {0} WITH THE VALUE {1}(<MULTIPLE VALUES> WILL NOT BE AFFECTED)"), new Object[]{table.getArbilTableModel().getColumnName(table.getSelectedColumn()), table.getArbilTableModel().getValueAt(table.getSelectedRow(), table.getSelectedColumn())});
	    // TODO: change this to copy to selected rows
	    if (!(table.getArbilTableModel().getTableCellContentAt(table.getSelectedRow(), table.getSelectedColumn()) instanceof ArbilField)) {
		dialogHandler.addMessageDialogToQueue(java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Menus").getString("CANNOT COPY THIS TYPE OF FIELD"), java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Menus").getString("COPY CELL TO WHOLE COLUMN"));
	    } else if (0 == JOptionPane.showConfirmDialog(windowManager.getMainFrame(), messageString, java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Menus").getString("COPY CELL TO WHOLE COLUMN"), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE)) {
		table.getArbilTableModel().copyCellToColumn(table.getSelectedRow(), table.getSelectedColumn());
	    }
	} catch (Exception ex) {
	    BugCatcherManager.getBugCatcher().logError(ex);
	}
    }

    public void deleteNodes(ArbilTable table) {
	treeHelper.deleteNodes(table);
    }

    public void jumpToSelectionInTree(ArbilTable table) {
	try {
	    treeHelper.jumpToSelectionInTree(false, table.getDataNodeForSelection());
	} catch (Exception ex) {
	    BugCatcherManager.getBugCatcher().logError(ex);
	}
    }

    public void showContextForSelectedNodes(ArbilTable table) {
	final Set<ArbilDataNode> parentNodes = new HashSet<ArbilDataNode>();
	for (ArbilField selectedField : table.getSelectedFields()) {
	    parentNodes.add(selectedField.getParentDataNode().getParentDomNode());
	}
	windowManager.openFloatingSubnodesWindows(parentNodes.toArray(new ArbilDataNode[]{}));
    }

    public void highlightMatchingRows(ArbilTable table) {
	final ArbilTableModel tableModel = table.getArbilTableModel();
	final int selectedRow = table.getSelectedRow();
	if (selectedRow == -1) {
	    dialogHandler.addMessageDialogToQueue("No rows have been selected", "Highlight Matching Rows");
	    return;
	}
	final List<Integer> foundRows = tableModel.getMatchingRows(selectedRow);
	table.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	table.getSelectionModel().clearSelection();
	dialogHandler.addMessageDialogToQueue("Found " + foundRows.size() + " matching rows", "Highlight Matching Rows");
	for (int foundCount = 0; foundCount < foundRows.size(); foundCount++) {
	    for (int coloumCount = 0; coloumCount < table.getColumnCount(); coloumCount++) {
		// TODO: this could be more efficient if the array was converted into selection intervals rather than individual rows (although the SelectionModel might already do this)
		table.getSelectionModel().addSelectionInterval(foundRows.get(foundCount), foundRows.get(foundCount));
	    }
	}
    }

    public void startLongFieldEditorForSelectedFields(ArbilTable table) {
	final ArbilTableModel tableModel = table.getArbilTableModel();
	int[] selectedRows = table.getSelectedRows();
	if (selectedRows.length > 0) {
	    int[] selectedCols;
	    if (table.getCellSelectionEnabled()) {
		selectedCols = table.getSelectedColumns();
	    } else {
		selectedCols = new int[table.getColumnCount()];
		for (int colCounter = 0; colCounter < selectedCols.length; colCounter++) {
		    selectedCols[colCounter] = colCounter;
		}
	    }
	    for (int currentRow : selectedRows) {
		if (tableModel.isHorizontalView() && table.getSelectionModel().getSelectionMode() == ListSelectionModel.MULTIPLE_INTERVAL_SELECTION && table.getSelectedColumnCount() > 0) {
		    // Entire row selected in horizontal view - try to open the long field editor for data node
		    Object currentCellValue = table.getTableCellContentAt(currentRow, table.getSelectedColumns()[0]);
		    if (currentCellValue instanceof ArbilDataNode) {
			ArbilDataNode node = (ArbilDataNode) currentCellValue;
			if (node.getFields().size() > 0) {
			    // Get fields for the node
			    List<ArbilField[]> fieldArrays = node.getFieldsSorted();
			    // Show the editor
			    new ArbilLongFieldEditor(table).showEditor(fieldArrays.get(0), fieldArrays.get(0)[0].getFieldValue(), 0);
			}
		    }
		} else {
		    for (int currentCol : selectedCols) {
			Object currentCellValue = table.getTableCellContentAt(currentRow, currentCol);
			if (currentCellValue instanceof ArbilField || currentCellValue instanceof ArbilField[]) {
			    new ArbilTableCellEditor(this).startLongfieldEditor(table, table.getTableCellAt(currentRow, currentCol), false, currentRow, currentCol);
			}
		    }
		}
	    }
	}
    }

    /**
     * Use when cell value is field place holder, meaning that the node does not
     * contain the selected field and may not be able to.
     */
    public synchronized void addFieldFromPlaceholder(ArbilTable table, int selectedFieldIndex, ArbilFieldPlaceHolder placeholder) {
	final String xmlPath = placeholder.getFieldName();
	final ArbilDataNode dataNode = placeholder.getArbilDataNode();

	//Investigate case, and initiate editor if possible
	final boolean canContainField = dataNode.getNodeTemplate().nodeCanContainType(dataNode, xmlPath);
	if (canContainField) {
	    // TODO: case of multifield (https://trac.mpi.nl/ticket/2469)
	    final ArbilField selectedField = table.getArbilTableModel().getColumnField(selectedFieldIndex);
	    final MetadataBuilder metadataBuilder = new MetadataBuilder();
	    try {
		metadataBuilder.addChildNode(dataNode, xmlPath, null, null, null, new NodeCreationCallback() {
		    public void nodeCreated(ArbilDataNode dataNode, URI nodeURI) {
			final String keyName = selectedField.getKeyName();
			if (keyName != null) {
			    // case of keys (https://trac.mpi.nl/ticket/2468)
			    setKeyName(dataNode, nodeURI, keyName);
			} // else: case of optional field (https://trac.mpi.nl/ticket/2470) covered
		    }
		});
	    } catch (ArbilMetadataException mdEx) {
		logger.error("Error while trying to create field", mdEx);
		dialogHandler.addMessageDialogToQueue("Could not create field. See error log for details", "Error");
	    }
	}
    }

    private void setKeyName(final ArbilDataNode dataNode, URI addedNodeUri, final String keyName) throws NumberFormatException {
	if (dataNode.waitTillLoaded()) {
	    final String fieldPath = addedNodeUri.getFragment();//.substring(dataNode.getURI().getFragment().length() + 1);
	    final Pattern pattern = Pattern.compile("^(.*?)(\\((\\d+)\\))?$");
	    final Matcher matcher = pattern.matcher(fieldPath);
	    if (matcher.matches()) {
		final String fieldPathBase = matcher.group(1);
		final String fieldPathIndexString = matcher.group(3);
		final int fieldPathIndex = fieldPathIndexString == null ? 0 : Integer.parseInt(fieldPathIndexString) - 1; // path index starts at 1, array at 0
		for (ArbilField[] fieldArray : dataNode.getFields().values()) {
		    if (fieldArray.length > fieldPathIndex && fieldArray[fieldPathIndex].getFullXmlPath().startsWith(fieldPathBase)) {
			fieldArray[fieldPathIndex].setKeyName(keyName);
		    }
		}
	    }
	} else {
	    // something went wrong while waiting until loaded
	}
    }

    public void checkPopup(MouseEvent evt, boolean checkSelection) {
	final ArbilTable table = getEventSourceAsArbilTable(evt);
	final ArbilTableModel tableModel = table.getArbilTableModel();

	if (!tableModel.hideContextMenuAndStatusBar && evt.isPopupTrigger()) {
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
			table.changeSelection(clickedRow, clickedColumn, false, evt.isShiftDown());
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
	public void mouseClicked(MouseEvent evt) {
	    final ArbilTable table = getEventSourceAsArbilTable(evt);
	    final ArbilTableModel tableModel = table.getArbilTableModel();

	    if (evt.getButton() == MouseEvent.BUTTON1) {
		tableModel.sortByColumn(table.convertColumnIndexToModel(((JTableHeader) evt.getComponent()).columnAtPoint(new Point(evt.getX(), evt.getY()))));
		table.getTableHeader().revalidate();
	    }
	    checkTableHeaderPopup(evt);
	}

	private void checkTableHeaderPopup(MouseEvent evt) {
	    //final JTableHeader tableHeader = (JTableHeader) evt.getSource();
	    final ArbilTable table = getEventSourceAsArbilTable(evt);// (ArbilTable) tableHeader.getTable();
	    final ArbilTableModel tableModel = table.getArbilTableModel();

	    if (!tableModel.hideContextMenuAndStatusBar && evt.isPopupTrigger()) {
		final int targetColumn = table.convertColumnIndexToModel(((JTableHeader) evt.getComponent()).columnAtPoint(new Point(evt.getX(), evt.getY())));
		logger.debug("showing header menu for column {}", targetColumn);
		final JPopupMenu popupMenu = new TableHeaderContextMenu(ArbilTableController.this, table, targetColumn);
		popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
	    }
	}
    }

    private ArbilTable getEventSourceAsArbilTable(InputEvent event) {
	final Object source = event.getSource();
	if (source instanceof ArbilTable) {
	    // Source is a table
	    return (ArbilTable) source;
	}
	if (source instanceof JTableHeader) {
	    // Source is table header, get its table
	    final JTable table = ((JTableHeader) source).getTable();
	    if (table instanceof ArbilTable) {
		// Only intereseted in ArbilTables
		return (ArbilTable) table;
	    } else {
		logger.warn("InputEvent coming from header of a JTable that is not an ArbilTable");
	    }
	}
	if (source instanceof Component) {
	    // Source is some other component, look for table ancestor
	    final Container tableAncestor = SwingUtilities.getAncestorOfClass(ArbilTable.class, (Component) source);
	    if (tableAncestor != null) {
		return (ArbilTable) tableAncestor;
	    }
	}
	// Giving up, this should not happen!
	logger.warn("Could not find ArbilTable associated with InputEvent source {}", source);
	throw new RuntimeException("Cannot find ArbilTable in component hierarchy from event");
    }

    private class DeleteRowAction extends AbstractAction {

	public void actionPerformed(ActionEvent e) {
	    treeHelper.deleteNodes(e.getSource());
	}
    }
}
