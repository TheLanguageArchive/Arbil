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
package nl.mpi.arbil.ui;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JToolTip;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import nl.mpi.arbil.ArbilIcons;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.data.ArbilTableCell;
import nl.mpi.arbil.ui.fieldeditors.ArbilLongFieldEditor;
import nl.mpi.arbil.ui.menu.TableContextMenu;
import nl.mpi.arbil.ui.menu.TableHeaderContextMenu;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.TreeHelper;
import nl.mpi.arbil.util.WindowManager;
import nl.mpi.flap.plugin.PluginArbilTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Document : ArbilTable
 * Created on :
 *
 * @author Peter.Withers@mpi.nl
 */
public class ArbilTable extends JTable implements PluginArbilTable {

    public final static int MIN_COLUMN_WIDTH = 50;
    public final static int MAX_COLUMN_WIDTH = 300;
    private final static Logger logger = LoggerFactory.getLogger(ArbilTable.class);
    private static WindowManager windowManager;

    public static void setWindowManager(WindowManager windowManagerInstance) {
	windowManager = windowManagerInstance;
    }
    private static MessageDialogHandler dialogHandler;

    public static void setMessageDialogHandler(MessageDialogHandler dialogHandlerInstance) {
	dialogHandler = dialogHandlerInstance;
    }
    private final ArbilTableModel arbilTableModel;
    private final JListToolTip listToolTip = new JListToolTip();
    private int lastColumnCount = -1;
    private int lastRowCount = -1;
    private int lastColumnPreferedWidth = 0;
    protected boolean allowNodeDrop = true;
    private final ArbilTableController tableController;

    public ArbilTable(ArbilTableModel arbilTableModel, ArbilTableController tableController, String frameTitle) {
	this.tableController = tableController;
	this.arbilTableModel = arbilTableModel;
	this.arbilTableModel.setShowIcons(true);
//        if (rowNodesArray != null) {
//            imdiTableModel.addImdiObjects(rowNodesArray);
//        }
	this.setModel(arbilTableModel);
	this.setName(frameTitle);
	this.setCellSelectionEnabled(true);
	setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	this.setGridColor(Color.LIGHT_GRAY);

	initHeaderMouseListener();
	initTableMouseListener();
	tableController.initKeyMapping(this);
    }

    @Override
    public void setTableHeader(JTableHeader tableHeader) {
	super.setTableHeader(tableHeader);
	final TableCellRenderer defaultRenderer = tableHeader.getDefaultRenderer();
	tableHeader.setDefaultRenderer(new TableCellRenderer() {
	    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		Component component = defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if (component instanceof JLabel) {
		    if (arbilTableModel.getSortColumn() == column) {
			//String text = ((JLabel) component).getText();
			//((JLabel) component).setText((arbilTableModel.isSortReverse() ? "[z-a] " : "[a-z] ") + text);
			((JLabel) component).setHorizontalTextPosition(SwingConstants.TRAILING);
			((JLabel) component).setIcon(arbilTableModel.isSortReverse() ? ArbilIcons.getSingleInstance().orderDesc : ArbilIcons.getSingleInstance().orderAsc);
		    }
		}
		return component;
	    }
	});
    }

    private void initTableMouseListener() {

	this.addMouseListener(new java.awt.event.MouseAdapter() {
	    @Override
	    public void mousePressed(MouseEvent evt) {
		checkPopup(evt, true);
	    }

	    @Override
	    public void mouseReleased(MouseEvent evt) {
		checkPopup(evt, true);
	    }
	});
    }

    private void initHeaderMouseListener() {
	this.getTableHeader().addMouseListener(new java.awt.event.MouseAdapter() {
	    //            public void mousePressed(java.awt.event.MouseEvent evt) {
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
		logger.debug("mouseClicked");
		logger.debug("table header click");
		//targetTable = ((JTableHeader) evt.getComponent()).getTable();
		if (evt.getButton() == MouseEvent.BUTTON1) {
		    arbilTableModel.sortByColumn(convertColumnIndexToModel(((JTableHeader) evt.getComponent()).columnAtPoint(new Point(evt.getX(), evt.getY()))));
		    getTableHeader().revalidate();
		}
		checkTableHeaderPopup(evt);
	    }

	    private void checkTableHeaderPopup(java.awt.event.MouseEvent evt) {
		handleTableHeaderPopup(evt);
	    }
	});
    }

    private void handleTableHeaderPopup(MouseEvent evt) {
	if (!arbilTableModel.hideContextMenuAndStatusBar && evt.isPopupTrigger()) {
	    final int targetColumn = convertColumnIndexToModel(((JTableHeader) evt.getComponent()).columnAtPoint(new Point(evt.getX(), evt.getY())));
	    logger.debug("showing header menu for column {}", targetColumn);
	    final JPopupMenu popupMenu = new TableHeaderContextMenu(this, arbilTableModel, targetColumn, dialogHandler, windowManager);
	    popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
	}
    }

    public void checkPopup(java.awt.event.MouseEvent evt, boolean checkSelection) {
	if (!arbilTableModel.hideContextMenuAndStatusBar && evt.isPopupTrigger() /* evt.getButton() == MouseEvent.BUTTON3 || evt.isMetaDown() */) {
	    // set the clicked cell selected
	    java.awt.Point p = evt.getPoint();
	    int clickedRow = rowAtPoint(p);
	    int clickedColumn = columnAtPoint(p);
	    boolean clickedRowAlreadySelected = isRowSelected(clickedRow);

	    if (checkSelection && !evt.isShiftDown() && !evt.isControlDown()) {
		// if it is the right mouse button and there is already a selection then do not proceed in changing the selection
		if (!(((evt.isPopupTrigger() /* evt.getButton() == MouseEvent.BUTTON3 || evt.isMetaDown() */) && clickedRowAlreadySelected))) {
		    if (clickedRow > -1 & clickedRow > -1) {
			// if the modifier keys are down then leave the selection alone for the sake of more normal behaviour
			getSelectionModel().clearSelection();
			// make sure the clicked cell is selected
//                        logger.debug("clickedRow: " + clickedRow + " clickedRow: " + clickedRow);
//                        getSelectionModel().addSelectionInterval(clickedRow, clickedRow);
//                        getColumnModel().getSelectionModel().addSelectionInterval(clickedColumn, clickedColumn);
			changeSelection(clickedRow, clickedColumn, false, evt.isShiftDown());
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

	    TableCellEditor tableCellEditor = this.getCellEditor();
	    if (tableCellEditor != null) {
		tableCellEditor.stopCellEditing();
	    }
	    new TableContextMenu(this, tableController).show(evt.getX(), evt.getY());
	}
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
	return /* getParent() instanceof JViewport && */ getPreferredSize().height < getParent().getHeight();
    }

    @Override
    public JToolTip createToolTip() {
//        logger.debug("createToolTip");
	listToolTip.updateList();
	return listToolTip;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
//        Object cellValue = arbilTableModel.getValueAt(row, convertColumnIndexToModel(column));
//        // only and always allow imdi fields or array objects because the editabilty of them is determinied in the field editor
//        return (cellValue instanceof Object[] || cellValue instanceof ArbilField);
	return true;
    }

    @Override
    public TableCellEditor getCellEditor(int row, int viewcolumn) {
	return new ArbilTableCellEditor();
    }

    @Override
    public ArbilTableCellRenderer getCellRenderer(int row, int viewcolumn) {
	int modelcolumn = convertColumnIndexToModel(viewcolumn);
	ArbilTableCellRenderer arbilCellRenderer = new ArbilTableCellRenderer();
	arbilCellRenderer.setBackground(arbilTableModel.getCellColour(row, modelcolumn));
	return arbilCellRenderer;
    }

    public void showRowChildData() {
	Object[] possibilities = this.getArbilTableModel().getChildNames();
	String selectionResult = (String) JOptionPane.showInputDialog(windowManager.getMainFrame(), "Select the child node type to display", "Show child nodes", JOptionPane.PLAIN_MESSAGE, null, possibilities, null);
//      TODO: JOptionPane.show it would be good to have a miltiple select here
	if ((selectionResult != null) && (selectionResult.length() > 0)) {
	    this.getArbilTableModel().addChildTypeToDisplay(selectionResult);
	}
    }

    @Override
    public void doLayout() {
	super.doLayout();
	setColumnWidths();
    }

    @Override
    public int getRowHeight() {
	try {
	    if (this.getGraphics() != null) {
		FontMetrics fontMetrics = this.getGraphics().getFontMetrics();
		int requiredHeight = fontMetrics.getHeight();
		return requiredHeight;
	    }
	} catch (Exception exception) {
//            BugCatcherManager.getBugCatcher().logError(exception);
	    logger.debug("getRowHeight could not get the font metrics, using the default row height");
	}
	return super.getRowHeight();
    }

    public void setColumnWidths() {
	// resize the columns only if the number of columns or rows have changed
	boolean resizeColumns = arbilTableModel.isWidthsChanged() || lastColumnCount != this.getModel().getColumnCount() || lastRowCount != this.getModel().getRowCount();
	lastColumnCount = this.getModel().getColumnCount();
	lastRowCount = this.getModel().getRowCount();
	int parentWidth = this.getParent().getWidth();
	if (this.getRowCount() > 0 && this.getColumnCount() > 2) {
	    if (resizeColumns) {
		doResizeColumns();
	    } else if (this.getParent() != null) {
		int lastColumnWidth = this.getColumnModel().getColumn(this.getColumnModel().getColumnCount() - 1).getWidth();
		int totalColWidth = this.getColumnModel().getTotalColumnWidth();
		boolean lastcolumnSquished = lastColumnWidth < MIN_COLUMN_WIDTH;
		if (parentWidth > totalColWidth) {
		    setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		} else if (parentWidth < totalColWidth) { // if the widths are equal then don't change anything
		    setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		} else if (lastcolumnSquished) { // unless the last column is squished
		    setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		    this.getColumnModel().getColumn(this.getColumnModel().getColumnCount() - 1).setPreferredWidth(lastColumnPreferedWidth);
		}
	    }
	} else {
	    setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
	}
    }

    public void doResizeColumns() {
	doResizeColumns(null);
    }

    public void doResizeColumns(Collection<Integer> columnsToResize) {
	setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	ArbilTableCellRenderer arbilCellRenderer = new ArbilTableCellRenderer();
	int totalColumnWidth = 0;
	int columnCount = this.getColumnModel().getColumnCount();
	Graphics g = getGraphics();
	try {
	    FontMetrics fontMetrics = g.getFontMetrics();
	    for (int columnCounter = 0; columnCounter < columnCount; columnCounter++) {

		TableColumn tableColumn = getColumnModel().getColumn(columnCounter);
		int currentWidth;
		if (columnsToResize == null || columnsToResize.contains(columnCounter)) {
		    currentWidth = MIN_COLUMN_WIDTH;
		    // Check if a column width has been stored for the current field view
		    Integer storedColumnWidth = arbilTableModel.getPreferredColumnWidth(tableColumn.getHeaderValue().toString());
		    if (storedColumnWidth != null) {
			// Use stored column width
			currentWidth = storedColumnWidth.intValue();
			tableColumn.setMaxWidth(currentWidth);
			tableColumn.setMinWidth(currentWidth);
			tableColumn.setResizable(false);
		    } else {
			// Calculate required width
			for (int rowCounter = 0; rowCounter < this.getRowCount(); rowCounter++) {
			    int requiredWidth = arbilCellRenderer.getRequiredWidth(fontMetrics, arbilTableModel.getTableCellAt(rowCounter, convertColumnIndexToModel(columnCounter)));
			    if (currentWidth < requiredWidth) {
				currentWidth = requiredWidth;
			    }
			}
			// No strict width limits for fields without width sepcification in column view 
			tableColumn.setMinWidth(0); // This is not MIN_COLUMN_WIDTH because size smaller than that triggers change in auto resizing policy
			tableColumn.setMaxWidth(MAX_COLUMN_WIDTH);
			tableColumn.setResizable(true);
		    }
		} else {
		    currentWidth = tableColumn.getWidth();
		}
		tableColumn.setPreferredWidth(currentWidth);
		tableColumn.setWidth(currentWidth);
		totalColumnWidth += tableColumn.getWidth();
		lastColumnPreferedWidth = currentWidth;
		//                    this.getColumnModel().getColumn(columnCounter).setWidth(currentWidth);
	    }
	    if (this.getParent().getWidth() > totalColumnWidth) {
		setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
	    } else {
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    }
	} finally {
	    g.dispose();
	    arbilTableModel.setWidthsChanged(false);
	}
    }

    public void updateStoredColumnWidhts() {
	final ArbilFieldView fieldView = arbilTableModel.getFieldView();
	for (int i = 0; i < getColumnModel().getColumnCount(); i++) {
	    TableColumn column = getColumnModel().getColumn(i);
	    final String columnName = column.getHeaderValue().toString();
	    if (fieldView.hasColumnWidthForColumn(columnName)) {
		fieldView.setColumnWidth(columnName, column.getWidth());
	    }
	}
    }

    //Implement table cell tool tips.
    @Override
    public String getToolTipText(MouseEvent e) {
	String tip = null;
	java.awt.Point p = e.getPoint();
	int rowIndex = rowAtPoint(p);
	int colIndex = columnAtPoint(p);
	if (rowIndex >= 0 && colIndex >= 0) {
	    tip = getTableCellContentAt(rowIndex, colIndex).toString();
	    listToolTip.setTartgetObject(getTableCellContentAt(rowIndex, colIndex));
	} else {
	    listToolTip.setTartgetObject(null);
	}
	return tip;
    }

    @Override
    public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
	if (arbilTableModel.isHorizontalView()) {
	    boolean rowSelection = (arbilTableModel.getTableCellContentAt(rowIndex, columnIndex) instanceof ArbilDataNode);
	    if (!arbilTableModel.isHorizontalView()) {
		this.setRowSelectionAllowed(true);
		this.setColumnSelectionAllowed(false);
	    } else {
		if (rowSelection) {
		    this.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		    this.setRowSelectionAllowed(true);
		    this.setColumnSelectionAllowed(false);
		} else {
		    this.setRowSelectionAllowed(true);
		    this.setColumnSelectionAllowed(true);
		    this.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		}
	    }

	    super.changeSelection(rowIndex, columnIndex, toggle, extend);
	} else {
	    this.setRowSelectionAllowed(true);
	    this.setColumnSelectionAllowed(false);
	    AWTEvent currentEvent = Toolkit.getDefaultToolkit().getSystemEventQueue().getCurrentEvent();
	    if (currentEvent instanceof KeyEvent && currentEvent != null) {
		{
		    KeyEvent nextPress = (KeyEvent) currentEvent;
		    if (nextPress.isShiftDown()) {
			rowIndex--;
			if (rowIndex < 0) {
			    rowIndex = getRowCount() - 1;
			}
		    }
		}
	    }
	    super.changeSelection(rowIndex, 1, toggle, extend);
	}
    }

    @Override
//Implement table header tool tips.
    protected JTableHeader createDefaultTableHeader() {
	return new JTableHeader(columnModel) {
	    @Override
	    public String getToolTipText(MouseEvent e) {
		Point p = e.getPoint();
		int index = columnModel.getColumnIndexAtX(p.x);
		//int realIndex = columnModel.getColumn(index).getModelIndex();
		//return GuiHelper.imdiSchema.getHelpForField(getColumnName(index));
		final String columnName = getColumnName(index);
		Integer preferredColumnWidth = arbilTableModel.getPreferredColumnWidth(columnName);
		if (preferredColumnWidth == null) {
		    return columnName;
		} else {
		    return String.format("%s [fixed width]", columnName);
		}
	    }
	};
    }

    public void copySelectedTableRowsToClipBoard() {
	int[] selectedRows = this.getSelectedRows();
	// only copy if there is at lease one row selected
	if (selectedRows.length > 0) {
	    logger.debug("coll select mode: {}", this.getColumnSelectionAllowed());
	    logger.debug("cell select mode: {}", this.getCellSelectionEnabled());
	    // when a user selects a cell and uses ctrl+a to change the selection the selection mode does not change from cell to row allCellsSelected is to resolve this error
	    boolean allCellsSelected = this.getSelectedRowCount() == this.getRowCount() && this.getSelectedColumnCount() == this.getColumnCount();
	    if (this.getCellSelectionEnabled() && !allCellsSelected) {
		logger.debug("cell select mode");
		ArbilField[] selectedFields = getSelectedFields();
		if (selectedFields != null) {
		    arbilTableModel.copyArbilFields(selectedFields);
		}
	    } else {
		logger.debug("row select mode");
		arbilTableModel.copyArbilRows(selectedRows);
	    }
	} else {
	    dialogHandler.addMessageDialogToQueue("Nothing selected to copy", "Table Copy");
	}
    }

    public void startLongFieldEditorForSelectedFields() {
	int[] selectedRows = this.getSelectedRows();
	if (selectedRows.length > 0) {
	    int[] selectedCols;
	    if (this.getCellSelectionEnabled()) {
		selectedCols = this.getSelectedColumns();
	    } else {
		selectedCols = new int[this.getColumnCount()];
		for (int colCounter = 0; colCounter < selectedCols.length; colCounter++) {
		    selectedCols[colCounter] = colCounter;
		}
	    }
	    for (int currentRow : selectedRows) {
		if (arbilTableModel.isHorizontalView() && getSelectionModel().getSelectionMode() == ListSelectionModel.MULTIPLE_INTERVAL_SELECTION && getSelectedColumnCount() > 0) {
		    // Entire row selected in horizontal view - try to open the long field editor for data node
		    Object currentCellValue = getTableCellContentAt(currentRow, getSelectedColumns()[0]);
		    if (currentCellValue instanceof ArbilDataNode) {
			ArbilDataNode node = (ArbilDataNode) currentCellValue;
			if (node.getFields().size() > 0) {
			    // Get fields for the node
			    List<ArbilField[]> fieldArrays = node.getFieldsSorted();
			    // Show the editor
			    new ArbilLongFieldEditor(this).showEditor(fieldArrays.get(0), fieldArrays.get(0)[0].getFieldValue(), 0);
			}
		    }
		} else {
		    for (int currentCol : selectedCols) {
			Object currentCellValue = getTableCellContentAt(currentRow, currentCol);
			if (currentCellValue instanceof ArbilField || currentCellValue instanceof ArbilField[]) {
			    new ArbilTableCellEditor().startLongfieldEditor(this, getTableCellAt(currentRow, currentCol), false, currentRow, currentCol);
			}
		    }
		}
	    }
	}
    }

    public ArbilField[] getSelectedFields() {
	// there is a limitation in the jtable in the way selections can be made so there is no point making this more complicated than a single contigious selection
	HashSet<ArbilField> selectedFields = new HashSet<ArbilField>();
	int[] selectedRows = this.getSelectedRows();
	if (selectedRows.length > 0) {
	    int[] selectedCols;
	    if (this.getCellSelectionEnabled()) {
		selectedCols = this.getSelectedColumns();
	    } else {
		selectedCols = new int[this.getColumnCount()];
		for (int colCounter = 0; colCounter < selectedCols.length; colCounter++) {
		    selectedCols[colCounter] = colCounter;
		}
	    }
	    for (int currentRow : selectedRows) {
		for (int currentCol : selectedCols) {
//                    logger.debug("row/col: " + currentRow + " : " + currentCol);
		    // this could be an imdifield array and must handled accortingly
		    if (getTableCellContentAt(currentRow, currentCol) instanceof ArbilField) {
			selectedFields.add((ArbilField) getTableCellContentAt(currentRow, currentCol));
		    } else if (getTableCellContentAt(currentRow, currentCol) instanceof ArbilField[]) {
			for (ArbilField currentField : (ArbilField[]) getTableCellContentAt(currentRow, currentCol)) {
			    selectedFields.add(currentField);
			}
		    }
		}
	    }
	    return selectedFields.toArray(new ArbilField[]{});
	} else {
	    return null;
	}
    }

    public void pasteIntoSelectedTableRowsFromClipBoard() {
	ArbilField[] selectedFields = getSelectedFields();
	if (selectedFields != null) {
	    String pasteResult = arbilTableModel.pasteIntoArbilFields(selectedFields);
	    if (pasteResult != null) {
		dialogHandler.addMessageDialogToQueue(pasteResult, "Paste into Table");
	    }
	} else {
	    dialogHandler.addMessageDialogToQueue("No rows selected", "Paste into Table");
	}
    }

    public void viewSelectedTableRows() {
	int[] selectedRows = this.getSelectedRows();
	windowManager.openFloatingTableOnce(arbilTableModel.getSelectedDataNodes(selectedRows), null);
    }

    public ArbilDataNode getDataNodeForSelection() {
	Object cellValue = arbilTableModel.getTableCellContentAt(getSelectedRow(), getSelectedColumn());
	ArbilDataNode cellDataNode = null;
	if (cellValue instanceof ArbilField) {
	    cellDataNode = ((ArbilField) cellValue).getParentDataNode();
	} else if (cellValue instanceof ArbilField[]) {
	    cellDataNode = ((ArbilField[]) cellValue)[0].getParentDataNode();
	} else if (cellValue instanceof ArbilDataNode) {
	    cellDataNode = (ArbilDataNode) cellValue;
	} else if (cellValue instanceof ArbilDataNode[]) {
	    cellDataNode = ((ArbilDataNode[]) cellValue)[0];
	}
	return cellDataNode;
    }

    public ArbilDataNode[] getSelectedRowsFromTable() {
	int[] selectedRows = this.getSelectedRows();
	return arbilTableModel.getSelectedDataNodes(selectedRows);
    }

    public void hideSelectedColumnsFromTable() {
	int[] selectedColumns = this.getSelectedColumns();
	Integer[] selectedModelColumns = new Integer[selectedColumns.length];
	for (int columnCounter = 0; columnCounter < selectedColumns.length; columnCounter++) {
	    selectedModelColumns[columnCounter] = convertColumnIndexToModel(selectedColumns[columnCounter]);
	}
	// this must be sorted otherwise it will fail when many columns are selected to be removed, so it is converter via convertColumnIndexToModel and then to an array then sort the array descending before removing columns
	// it might be better to remove by column name instead
	Arrays.sort(selectedModelColumns, Collections.reverseOrder());
	for (int selectedModelCol : selectedModelColumns) {
	    arbilTableModel.hideColumn(selectedModelCol);
	}
    }

    public void removeSelectedRowsFromTable() {
	int[] selectedRows = this.getSelectedRows();
	arbilTableModel.removeArbilDataNodeRows(selectedRows);
    }

    public void highlightMatchingRows() {
	int selectedRow = this.getSelectedRow();
	if (selectedRow == -1) {
	    dialogHandler.addMessageDialogToQueue("No rows have been selected", "Highlight Matching Rows");
	    return;
	}
	Vector foundRows = arbilTableModel.getMatchingRows(selectedRow);
	this.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	this.getSelectionModel().clearSelection();
	dialogHandler.addMessageDialogToQueue("Found " + foundRows.size() + " matching rows", "Highlight Matching Rows");
	for (int foundCount = 0; foundCount < foundRows.size(); foundCount++) {
	    for (int coloumCount = 0; coloumCount < this.getColumnCount(); coloumCount++) {
		// TODO: this could be more efficient if the array was converted into selection intervals rather than individual rows (although the SelectionModel might already do this)
		this.getSelectionModel().addSelectionInterval((Integer) foundRows.get(foundCount), (Integer) foundRows.get(foundCount));
	    }
	}
    }

    public ArbilTableModel getArbilTableModel() {
	return arbilTableModel;
    }

    /**
     * Gets the table cell at the specified location. Same result as getValue(row,col) but
     * casted to ArbilTableCell (or null if casting fails)
     *
     * @param row
     * @param col
     * @return Table cell at the specified location
     */
    public ArbilTableCell getTableCellAt(int row, int col) {
	Object object = getValueAt(row, col);
	if (object instanceof ArbilTableCell) {
	    return (ArbilTableCell) object;
	} else {
	    return null;
	}
    }

    /**
     * Gets the content of the table cell at the specified location.
     * Same result as ((ArbilTableCell) getValue(row,col)).getContent(), unless casting
     * fails or ArbilTableCell is null, then null is returned.
     *
     * @param row
     * @param col
     * @return Content of the table cell at the specified location
     */
    public Object getTableCellContentAt(int row, int col) {
	ArbilTableCell cell = getTableCellAt(row, col);
	return cell != null ? cell.getContent() : null;
    }

    /**
     * Whether tables allows nodes to be dropped on it
     *
     * @return the value of allowNodeDrop
     */
    public boolean isAllowNodeDrop() {
	return allowNodeDrop;
    }

    /**
     * Set whether tables allows nodes to be dropped on it
     *
     * @param allowNodeDrop new value of allowNodeDrop
     */
    public void setAllowNodeDrop(boolean allowNodeDrop) {
	this.allowNodeDrop = allowNodeDrop;
    }
}
