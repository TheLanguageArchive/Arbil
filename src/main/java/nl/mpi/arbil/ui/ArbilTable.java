package nl.mpi.arbil.ui;

import nl.mpi.arbil.ui.menu.TableContextMenu;
import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.data.ArbilDataNode;
import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JToolTip;
import javax.swing.ListSelectionModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import nl.mpi.arbil.data.ArbilDataNodeTableCell;
import nl.mpi.arbil.data.ArbilTableCell;
import nl.mpi.arbil.ui.fieldeditors.ArbilLongFieldEditor;

/**
 * Document   : ArbilTable
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
public class ArbilTable extends JTable {

    public ArbilTableModel arbilTableModel;
    JListToolTip listToolTip = new JListToolTip();

    public ArbilTable(ArbilTableModel localArbilTableModel, String frameTitle) {
	arbilTableModel = localArbilTableModel;
	arbilTableModel.setShowIcons(true);
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
    }

    private void initTableMouseListener() {

	this.addMouseListener(new java.awt.event.MouseAdapter() {

	    @Override
	    public void mousePressed(MouseEvent evt) {
//                System.out.println("mousePressed");
		checkPopup(evt, true);
	    }

	    @Override
	    public void mouseReleased(MouseEvent evt) {
//                System.out.println("mouseReleased");
		checkPopup(evt, true);
	    }
//            @Override
//            public void mouseClicked(java.awt.event.MouseEvent evt) {
//                System.out.println("mouseClicked");
//                checkPopup(evt);
//            }
	});
    }

    private void initHeaderMouseListener() {
	this.getTableHeader().addMouseListener(new java.awt.event.MouseAdapter() {

	    //            public void mousePressed(java.awt.event.MouseEvent evt) {
	    @Override
	    public void mousePressed(MouseEvent evt) {
		//                System.out.println("mousePressed");
		checkTableHeaderPopup(evt);
	    }

	    @Override
	    public void mouseReleased(MouseEvent evt) {
		//                System.out.println("mouseReleased");
		checkTableHeaderPopup(evt);
	    }

	    @Override
	    public void mouseClicked(java.awt.event.MouseEvent evt) {
		System.out.println("mouseClicked");
		System.out.println("table header click");
		//targetTable = ((JTableHeader) evt.getComponent()).getTable();
		if (evt.getButton() == MouseEvent.BUTTON1) {
		    arbilTableModel.sortByColumn(convertColumnIndexToModel(((JTableHeader) evt.getComponent()).columnAtPoint(new Point(evt.getX(), evt.getY()))));
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
	    //targetTable = ((JTableHeader) evt.getComponent()).getTable();
	    final int targetColumn = convertColumnIndexToModel(((JTableHeader) evt.getComponent()).columnAtPoint(new Point(evt.getX(), evt.getY())));
	    final String targetColumnName = arbilTableModel.getColumnName(targetColumn);
	    System.out.println("columnIndex: " + targetColumn);
	    final JPopupMenu popupMenu = new JPopupMenu();
	    // TODO: also add show only selected columns
	    // TODO: also add hide selected columns

	    final JMenuItem saveViewMenuItem = new JMenuItem("Save Current Column View");
	    saveViewMenuItem.addActionListener(new ActionListener() {

		public void actionPerformed(ActionEvent e) {
		    try {
			//System.out.println("saveViewNenuItem: " + targetTable.toString());
			String fieldViewName = (String) JOptionPane.showInputDialog(null, "Enter a name to save this Column View as", "Save Column View", JOptionPane.PLAIN_MESSAGE);
			// if the user did not cancel
			if (fieldViewName != null) {
			    updateStoredColumnWidhts();

			    if (!ArbilFieldViews.getSingleInstance().addArbilFieldView(fieldViewName, arbilTableModel.getFieldView())) {
				ArbilWindowManager.getSingleInstance().addMessageDialogToQueue("A Column View with the same name already exists, nothing saved", "Save Column View");
			    }
			}
		    } catch (Exception ex) {
			GuiHelper.linorgBugCatcher.logError(ex);
		    }
		}
	    });
	    final JMenuItem editViewMenuItem = new JMenuItem("Edit this Column View");
	    editViewMenuItem.addActionListener(new ActionListener() {

		public void actionPerformed(ActionEvent e) {
		    updateStoredColumnWidhts();
		    try {
			ArbilFieldViewTable fieldViewTable = new ArbilFieldViewTable(arbilTableModel);
			JDialog editViewsDialog = new JDialog(JOptionPane.getFrameForComponent(ArbilWindowManager.getSingleInstance().linorgFrame), true);
			editViewsDialog.setTitle("Editing Current Column View");
			JScrollPane js = new JScrollPane(fieldViewTable);
			editViewsDialog.getContentPane().add(js);
			editViewsDialog.setBounds(50, 50, 600, 400);
			editViewsDialog.setVisible(true);
		    } catch (Exception ex) {
			GuiHelper.linorgBugCatcher.logError(ex);
		    }
		}
	    });
	    final JMenuItem showOnlyCurrentViewMenuItem = new JMenuItem("Limit View to Current Columns");
	    showOnlyCurrentViewMenuItem.addActionListener(new ActionListener() {

		public void actionPerformed(ActionEvent e) {
		    try {
			//System.out.println("saveViewNenuItem: " + targetTable.toString());
			arbilTableModel.showOnlyCurrentColumns();
		    } catch (Exception ex) {
			GuiHelper.linorgBugCatcher.logError(ex);
		    }
		}
	    });
	    //popupMenu.add(applyViewNenuItem);
	    //popupMenu.add(saveViewMenuItem);
	    // create the views sub menu
	    final JMenu fieldViewsMenuItem = new JMenu("Column View for this Table");
	    ButtonGroup viewMenuButtonGroup = new javax.swing.ButtonGroup();
	    //String currentGlobalViewLabel = GuiHelper.imdiFieldViews.currentGlobalViewName;
	    for (Enumeration savedViewsEnum = ArbilFieldViews.getSingleInstance().getSavedFieldViewLables(); savedViewsEnum.hasMoreElements();) {
		String currentViewLabel = savedViewsEnum.nextElement().toString();
		javax.swing.JMenuItem viewLabelMenuItem;
		viewLabelMenuItem = new javax.swing.JMenuItem();
		viewMenuButtonGroup.add(viewLabelMenuItem);
		//  viewLabelMenuItem.setSelected(currentGlobalViewLabel.equals(currentViewLabel));
		viewLabelMenuItem.setText(currentViewLabel);
		viewLabelMenuItem.setName(currentViewLabel);
		viewLabelMenuItem.addActionListener(new ActionListener() {

		    public void actionPerformed(ActionEvent evt) {
			try {
			    arbilTableModel.setCurrentView(ArbilFieldViews.getSingleInstance().getView(((Component) evt.getSource()).getName()));
			    doResizeColumns();
			} catch (Exception ex) {
			    GuiHelper.linorgBugCatcher.logError(ex);
			}
		    }
		});
		fieldViewsMenuItem.add(viewLabelMenuItem);
	    }
	    final JMenuItem copyEmbedTagMenuItem = new JMenuItem("Copy Table For Website");
	    copyEmbedTagMenuItem.addActionListener(new ActionListener() {

		public void actionPerformed(ActionEvent e) {
		    // find the table dimensions
		    Component sizedComponent = ArbilTable.this;
		    Component currentComponent = ArbilTable.this;
		    while (currentComponent.getParent() != null) {
			currentComponent = currentComponent.getParent();
			if (currentComponent instanceof ArbilSplitPanel) {
			    sizedComponent = currentComponent;
			}
		    }
		    arbilTableModel.copyHtmlEmbedTagToClipboard(sizedComponent.getHeight(), sizedComponent.getWidth());
		}
	    });
	    final JMenuItem setAllColumnsSizeFromColumn = new JMenuItem("Make all columns the size of \"" + targetColumnName + "\"");
	    setAllColumnsSizeFromColumn.addActionListener(new ActionListener() {

		public void actionPerformed(ActionEvent e) {
		    int targetWidth = getColumnModel().getColumn(targetColumn).getWidth();
		    for (int i = 0; i < getColumnCount(); i++) {
			TableColumn column = getColumnModel().getColumn(i);
			arbilTableModel.getFieldView().setColumnWidth(column.getHeaderValue().toString(), targetWidth);
		    }
		    doResizeColumns();
		}
	    });
	    final JMenuItem setAllColumnsSizeAuto = new JMenuItem("Make all columns fit contents");
	    setAllColumnsSizeAuto.addActionListener(new ActionListener() {

		public void actionPerformed(ActionEvent e) {
		    arbilTableModel.getFieldView().resetColumnWidths();
		    doResizeColumns();
		}
	    });
	    final JMenuItem setColumnSizeAuto = new JMenuItem("Make column fit contents");
	    setColumnSizeAuto.addActionListener(new ActionListener() {

		public void actionPerformed(ActionEvent e) {
		    arbilTableModel.getFieldView().setColumnWidth(targetColumnName, null);
		    doResizeColumns(Arrays.asList(targetColumn));
		}
	    });
	    final JCheckBoxMenuItem setFixedColumnSize = new JCheckBoxMenuItem("Fixed column size");
	    setFixedColumnSize.setSelected(arbilTableModel.getPreferredColumnWidth(targetColumnName) != null);

	    setFixedColumnSize.addActionListener(new ActionListener() {

		public void actionPerformed(ActionEvent e) {
		    arbilTableModel.getFieldView().setColumnWidth(targetColumnName,
			    setFixedColumnSize.isSelected()
			    ? getColumnModel().getColumn(targetColumn).getWidth()
			    : null);
		}
	    });


	    final JMenu thisColumnMenu = new JMenu("This column" + " (" + (targetColumnName.trim().length() == 0 ? "nameless" : targetColumnName) + ")");
	    thisColumnMenu.add(setFixedColumnSize);
	    thisColumnMenu.add(setColumnSizeAuto);
	    if (targetColumn != 0) {
		thisColumnMenu.add(new JSeparator());
		thisColumnMenu.add(createHideColumnMenuItem(targetColumn));
	    }
	    final JMenu allColumnsMenu = new JMenu("All columns");
	    allColumnsMenu.add(setAllColumnsSizeFromColumn);
	    allColumnsMenu.add(setAllColumnsSizeAuto);

	    popupMenu.add(thisColumnMenu);
	    popupMenu.add(allColumnsMenu);
	    if (arbilTableModel.isHorizontalView()) {
		popupMenu.add(createShowChildNodesMenuItem(targetColumn));
	    }

	    popupMenu.add(new JSeparator());
	    popupMenu.add(fieldViewsMenuItem);
	    popupMenu.add(saveViewMenuItem);
	    popupMenu.add(editViewMenuItem);
	    popupMenu.add(showOnlyCurrentViewMenuItem);

	    popupMenu.add(new JSeparator());
	    popupMenu.add(copyEmbedTagMenuItem);

	    popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
	}
    }

    private JMenuItem createHideColumnMenuItem(final int targetColumn) {
	// prevent hide column menu showing when the session column is selected because it cannot be hidden
	JMenuItem hideColumnMenuItem = new JMenuItem("Hide Column");
	hideColumnMenuItem.setActionCommand("" + targetColumn);
	hideColumnMenuItem.addActionListener(new ActionListener() {

	    public void actionPerformed(ActionEvent e) {
		try {
		    //System.out.println("hideColumnMenuItem: " + targetTable.toString());
		    arbilTableModel.hideColumn(targetColumn);
		} catch (Exception ex) {
		    GuiHelper.linorgBugCatcher.logError(ex);
		}
	    }
	});
	return hideColumnMenuItem;
    }

    private JMenuItem createShowChildNodesMenuItem(final int targetColumn) {
	JMenuItem showChildNodesMenuItem = new javax.swing.JMenuItem();
	showChildNodesMenuItem.setText("Show Child Nodes");
	showChildNodesMenuItem.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    showRowChildData();
		} catch (Exception ex) {
		    GuiHelper.linorgBugCatcher.logError(ex);
		}
	    }
	});
	return showChildNodesMenuItem;
    }

    public void checkPopup(java.awt.event.MouseEvent evt, boolean checkSelection) {
	if (!arbilTableModel.hideContextMenuAndStatusBar && evt.isPopupTrigger() /* evt.getButton() == MouseEvent.BUTTON3 || evt.isMetaDown()*/) {
	    // set the clicked cell selected
	    java.awt.Point p = evt.getPoint();
	    int clickedRow = rowAtPoint(p);
	    int clickedColumn = columnAtPoint(p);
	    boolean clickedRowAlreadySelected = isRowSelected(clickedRow);

	    if (checkSelection && !evt.isShiftDown() && !evt.isControlDown()) {
		// if it is the right mouse button and there is already a selection then do not proceed in changing the selection
		if (!(((evt.isPopupTrigger() /* evt.getButton() == MouseEvent.BUTTON3 || evt.isMetaDown()*/) && clickedRowAlreadySelected))) {
		    if (clickedRow > -1 & clickedRow > -1) {
			// if the modifier keys are down then leave the selection alone for the sake of more normal behaviour
			getSelectionModel().clearSelection();
			// make sure the clicked cell is selected
//                        System.out.println("clickedRow: " + clickedRow + " clickedRow: " + clickedRow);
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

	if (evt.isPopupTrigger() /* evt.getButton() == MouseEvent.BUTTON3 || evt.isMetaDown()*/) {
//                    targetTable = (JTable) evt.getComponent();
//                    System.out.println("set the current table");

	    TableCellEditor tableCellEditor = this.getCellEditor();
	    if (tableCellEditor != null) {
		tableCellEditor.stopCellEditing();
	    }
	    new TableContextMenu(this).show(evt.getX(), evt.getY());
	    //new OldContextMenu().showTreePopup(evt.getSource(), evt.getX(), evt.getY());
	}
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
	return /*getParent() instanceof JViewport && */ getPreferredSize().height < getParent().getHeight();
    }

    @Override
    public JToolTip createToolTip() {
//        System.out.println("createToolTip");
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
	String selectionResult = (String) JOptionPane.showInputDialog(ArbilWindowManager.getSingleInstance().linorgFrame, "Select the child node type to display", "Show child nodes", JOptionPane.PLAIN_MESSAGE, null, possibilities, null);
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
//            GuiHelper.linorgBugCatcher.logError(exception);
	    System.out.println("getRowHeight could not get the font metrics, using the default row height");
	}
	return super.getRowHeight();
    }
    int lastColumnCount = -1;
    int lastRowCount = -1;
    int lastColumnPreferedWidth = 0;
    int totalPreferedWidth = 0;
    public final static int MIN_COLUMN_WIDTH = 50;
    public final static int MAX_COLUMN_WIDTH = 300;

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

    private void doResizeColumns() {
	doResizeColumns(null);
    }

    private void doResizeColumns(Collection<Integer> columnsToResize) {
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
		    } else {
			// Calculate required width
			for (int rowCounter = 0; rowCounter < this.getRowCount(); rowCounter++) {
			    arbilCellRenderer.setValue(arbilTableModel.getTableCellAt(rowCounter, convertColumnIndexToModel(columnCounter)));
			    int requiredWidth = arbilCellRenderer.getRequiredWidth(fontMetrics);
			    if (currentWidth < requiredWidth) {
				currentWidth = requiredWidth;
			    }
			}
			if (currentWidth > MAX_COLUMN_WIDTH) {
			    currentWidth = MAX_COLUMN_WIDTH;
			}
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
	    totalPreferedWidth = totalColumnWidth;
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
		String tip = null;
		java.awt.Point p = e.getPoint();
		int index = columnModel.getColumnIndexAtX(p.x);
		//int realIndex = columnModel.getColumn(index).getModelIndex();
		//return GuiHelper.imdiSchema.getHelpForField(getColumnName(index));
		return getColumnName(index);
	    }
	};
    }

    public void copySelectedTableRowsToClipBoard() {
	int[] selectedRows = this.getSelectedRows();
	// only copy if there is at lease one row selected
	if (selectedRows.length > 0) {
	    System.out.println("coll select mode: " + this.getColumnSelectionAllowed());
	    System.out.println("cell select mode: " + this.getCellSelectionEnabled());
	    // when a user selects a cell and uses ctrl+a to change the selection the selection mode does not change from cell to row allCellsSelected is to resolve this error
	    boolean allCellsSelected = this.getSelectedRowCount() == this.getRowCount() && this.getSelectedColumnCount() == this.getColumnCount();
	    if (this.getCellSelectionEnabled() && !allCellsSelected) {
		System.out.println("cell select mode");
		ArbilField[] selectedFields = getSelectedFields();
		if (selectedFields != null) {
		    arbilTableModel.copyArbilFields(selectedFields);
		}
	    } else {
		System.out.println("row select mode");
		arbilTableModel.copyArbilRows(selectedRows);
	    }
	} else {
	    ArbilWindowManager.getSingleInstance().addMessageDialogToQueue("Nothing selected to copy", "Table Copy");
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
//                    System.out.println("row/col: " + currentRow + " : " + currentCol);
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
		ArbilWindowManager.getSingleInstance().addMessageDialogToQueue(pasteResult, "Paste into Table");
	    }
	} else {
	    ArbilWindowManager.getSingleInstance().addMessageDialogToQueue("No rows selected", "Paste into Table");
	}
    }

    public void viewSelectedTableRows() {
	int[] selectedRows = this.getSelectedRows();
	ArbilWindowManager.getSingleInstance().openFloatingTableOnce(arbilTableModel.getSelectedDataNodes(selectedRows), null);
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
	    ArbilWindowManager.getSingleInstance().addMessageDialogToQueue("No rows have been selected", "Highlight Matching Rows");
	    return;
	}
	Vector foundRows = arbilTableModel.getMatchingRows(selectedRow);
	this.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	this.getSelectionModel().clearSelection();
	ArbilWindowManager.getSingleInstance().addMessageDialogToQueue("Found " + foundRows.size() + " matching rows", "Highlight Matching Rows");
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
     * @param row
     * @param col
     * @return Content of the table cell at the specified location
     */
    public Object getTableCellContentAt(int row, int col) {
	ArbilTableCell cell = getTableCellAt(row, col);
	return cell != null ? cell.getContent() : null;
    }
}
