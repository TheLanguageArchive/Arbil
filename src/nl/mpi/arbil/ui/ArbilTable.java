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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Vector;
import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolTip;
import javax.swing.ListSelectionModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;

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
                if (!arbilTableModel.hideContextMenuAndStatusBar && evt.isPopupTrigger() /* evt.getButton() == MouseEvent.BUTTON3*/) {
                    //targetTable = ((JTableHeader) evt.getComponent()).getTable();
                    int targetColumn = convertColumnIndexToModel(((JTableHeader) evt.getComponent()).columnAtPoint(new Point(evt.getX(), evt.getY())));
                    System.out.println("columnIndex: " + targetColumn);

                    JPopupMenu popupMenu = new JPopupMenu();
                    // TODO: also add show only selected columns
                    // TODO: also add hide selected columns
                    if (targetColumn != 0) { // prevent hide column menu showing when the session column is selected because it cannot be hidden
                        JMenuItem hideColumnMenuItem = new JMenuItem("Hide Column: \"" + arbilTableModel.getColumnName(targetColumn) + "\"");
                        hideColumnMenuItem.setActionCommand("" + targetColumn);
                        hideColumnMenuItem.addActionListener(new ActionListener() {

                            public void actionPerformed(ActionEvent e) {
                                try {
                                    //System.out.println("hideColumnMenuItem: " + targetTable.toString());
                                    arbilTableModel.hideColumn(Integer.parseInt(e.getActionCommand()));
                                } catch (Exception ex) {
                                    GuiHelper.linorgBugCatcher.logError(ex);
                                }
                            }
                        });
                        popupMenu.add(hideColumnMenuItem);
                    }
                    if (arbilTableModel.isHorizontalView()) {
                        JMenuItem showChildNodesMenuItem = new javax.swing.JMenuItem();
                        showChildNodesMenuItem.setText("Show Child Nodes"); // NOI18N
                        showChildNodesMenuItem.addActionListener(new java.awt.event.ActionListener() {

                            public void actionPerformed(java.awt.event.ActionEvent evt) {
                                try {
                                    showRowChildData();
                                } catch (Exception ex) {
                                    GuiHelper.linorgBugCatcher.logError(ex);
                                }
                            }
                        });
                        popupMenu.add(showChildNodesMenuItem);
                    }

                    JMenuItem saveViewMenuItem = new JMenuItem("Save Current Column View");
                    saveViewMenuItem.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent e) {
                            try {
                                //System.out.println("saveViewNenuItem: " + targetTable.toString());
                                String fieldViewName = (String) JOptionPane.showInputDialog(null, "Enter a name to save this Column View as", "Save Column View", JOptionPane.PLAIN_MESSAGE);
                                // if the user did not cancel
                                if (fieldViewName != null) {
                                    if (!ArbilFieldViews.getSingleInstance().addArbilFieldView(fieldViewName, arbilTableModel.getFieldView())) {
                                        ArbilWindowManager.getSingleInstance().addMessageDialogToQueue("A Column View with the same name already exists, nothing saved", "Save Column View");
                                    }
                                }
                            } catch (Exception ex) {
                                GuiHelper.linorgBugCatcher.logError(ex);
                            }
                        }
                    });
                    popupMenu.add(saveViewMenuItem);

                    JMenuItem editViewMenuItem = new JMenuItem("Edit this Column View");
                    editViewMenuItem.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent e) {
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
                    popupMenu.add(editViewMenuItem);

                    JMenuItem showOnlyCurrentViewMenuItem = new JMenuItem("Limit View to Current Columns");
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
                    popupMenu.add(showOnlyCurrentViewMenuItem);

                    //popupMenu.add(applyViewNenuItem);
                    //popupMenu.add(saveViewMenuItem);

                    // create the views sub menu
                    JMenu fieldViewsMenuItem = new JMenu("Column View for this Table");
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
                                } catch (Exception ex) {
                                    GuiHelper.linorgBugCatcher.logError(ex);
                                }
                            }
                        });
                        fieldViewsMenuItem.add(viewLabelMenuItem);
                    }
                    popupMenu.add(fieldViewsMenuItem);

                    JMenuItem copyEmbedTagMenuItem = new JMenuItem("Copy Table For Website");
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
                    popupMenu.add(copyEmbedTagMenuItem);
                    popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }
        });

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

    public void checkPopup(java.awt.event.MouseEvent evt, boolean checkSelection) {
        System.out.println("checkPopup");
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
        Object cellValue = arbilTableModel.getValueAt(row, convertColumnIndexToModel(column));
        // only and always allow imdi fields or array objects because the editabilty of them is determinied in the field editor
        return (cellValue instanceof Object[] || cellValue instanceof ArbilField);
    }

    @Override
    public TableCellEditor getCellEditor(int row, int viewcolumn) {
        int modelcolumn = convertColumnIndexToModel(viewcolumn);
        Object cellField = getModel().getValueAt(row, modelcolumn);
//        System.out.println("getCellEditor: " + cellField.toString());
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
        Object[] possibilities = ((ArbilTableModel) this.getModel()).getChildNames();
        String selectionResult = (String) JOptionPane.showInputDialog(ArbilWindowManager.getSingleInstance().linorgFrame, "Select the child node type to display", "Show child nodes", JOptionPane.PLAIN_MESSAGE, null, possibilities, null);
//      TODO: JOptionPane.show it would be good to have a miltiple select here
        if ((selectionResult != null) && (selectionResult.length() > 0)) {
            ((ArbilTableModel) this.getModel()).addChildTypeToDisplay(selectionResult);
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

    public void setColumnWidths() {
        // resize the columns only if the number of columns or rows have changed
        boolean resizeColumns = lastColumnCount != this.getModel().getColumnCount() || lastRowCount != this.getModel().getRowCount();
        lastColumnCount = this.getModel().getColumnCount();
        lastRowCount = this.getModel().getRowCount();
        int maxColumnWidth = 300;
        int minWidth = 50;
        int parentWidth = this.getParent().getWidth();
        if (this.getRowCount() > 0 && this.getColumnCount() > 2) {
            if (resizeColumns) {
                setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                ArbilTableCellRenderer arbilCellRenderer = new ArbilTableCellRenderer();
                int totalColumnWidth = 0;
                int columnCount = this.getColumnModel().getColumnCount();
                Graphics g = getGraphics();
                try {
                    FontMetrics fontMetrics = g.getFontMetrics();
                    for (int columnCounter = 0; columnCounter < columnCount; columnCounter++) {
                        int currentWidth = minWidth;
                        for (int rowCounter = 0; rowCounter < this.getRowCount(); rowCounter++) {
                            arbilCellRenderer.setValue(arbilTableModel.getValueAt(rowCounter, convertColumnIndexToModel(columnCounter)));
                            int requiredWidth = arbilCellRenderer.getRequiredWidth(fontMetrics);
                            if (currentWidth < requiredWidth) {
                                currentWidth = requiredWidth;
                            }
                        }
                        if (currentWidth > maxColumnWidth) {
                            currentWidth = maxColumnWidth;
                        }
                        this.getColumnModel().getColumn(columnCounter).setPreferredWidth(currentWidth);
                        totalColumnWidth += currentWidth;
                        lastColumnPreferedWidth = currentWidth;
//                    this.getColumnModel().getColumn(columnCounter).setWidth(currentWidth);
                    }
                    totalPreferedWidth = totalColumnWidth;
                    if (parentWidth > totalColumnWidth) {
                        setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
                    } else {
                        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                    }
                } finally {
                    g.dispose();
                }
            } else if (this.getParent() != null) {
                int lastColumnWidth = this.getColumnModel().getColumn(this.getColumnModel().getColumnCount() - 1).getWidth();
                int totalColWidth = this.getColumnModel().getTotalColumnWidth();
                boolean lastcolumnSquished = lastColumnWidth < minWidth;
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
    //private int targetColumn;
    //Implement table cell tool tips.

    @Override
    public String getToolTipText(MouseEvent e) {
        String tip = null;
        java.awt.Point p = e.getPoint();
        int rowIndex = rowAtPoint(p);
        int colIndex = columnAtPoint(p);
        int realColumnIndex = convertColumnIndexToModel(colIndex);
        if (rowIndex >= 0 && colIndex >= 0) {
            tip = getValueAt(rowIndex, colIndex).toString();
            listToolTip.setTartgetObject(getValueAt(rowIndex, colIndex));
        } else {
            listToolTip.setTartgetObject(null);
        }
        return tip;
    }

    @Override
    public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
        if (arbilTableModel.isHorizontalView()) {
            boolean rowSelection = (arbilTableModel.getValueAt(rowIndex, columnIndex) instanceof ArbilDataNode);
//        System.out.println("rowSelection: " + rowSelection + ":" + clickedRow + ":" + clickedColumn);
            if (!arbilTableModel.isHorizontalView()) {
                this.setRowSelectionAllowed(true);
                this.setColumnSelectionAllowed(false);
            } else {
                if (rowSelection) {
                    System.out.println("set row select mode");
                    this.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                    this.setRowSelectionAllowed(true);
                    this.setColumnSelectionAllowed(false);
//                cellSelectionModeAsNotReturnedByJTable = false;
                } else {
                    System.out.println("set cell select mode");
//                toggle = false;
//                extend = true;
                    this.setRowSelectionAllowed(true);
                    this.setColumnSelectionAllowed(true);
                    this.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
                }
            }
            System.out.println("coll select mode: " + this.getColumnSelectionAllowed());
            System.out.println("cell select mode: " + this.getCellSelectionEnabled());
            System.out.println("getSelectionMode: " + this.getSelectionModel().getSelectionMode());

            super.changeSelection(rowIndex, columnIndex, toggle, extend);
        } else {
            this.setRowSelectionAllowed(true);
            this.setColumnSelectionAllowed(false);
            AWTEvent currentEvent = Toolkit.getDefaultToolkit().getSystemEventQueue().getCurrentEvent();
            System.out.println("currentEvent: " + currentEvent);
            if (currentEvent instanceof KeyEvent && currentEvent != null) {
                {
                    System.out.println("is KeyEvent");
                    KeyEvent nextPress = (KeyEvent) currentEvent;
                    if (nextPress.isShiftDown()) {
                        System.out.println("VK_SHIFT");
                        rowIndex--;
                        if (rowIndex < 0) {
                            rowIndex = getRowCount() - 1;
                        }
                    }
                }
            }
            System.out.println("rowIndex: " + rowIndex);
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
                for (int currentCol : selectedCols) {
                    Object currentCellValue = this.getValueAt(currentRow, currentCol);
                    if (currentCellValue instanceof ArbilField || currentCellValue instanceof ArbilField[]) {
                        new ArbilTableCellEditor().startLongfieldEditor(this, currentCellValue, false, currentRow, currentCol);
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
                    if (this.getValueAt(currentRow, currentCol) instanceof ArbilField) {
                        selectedFields.add((ArbilField) this.getValueAt(currentRow, currentCol));
                    } else if (this.getValueAt(currentRow, currentCol) instanceof ArbilField[]) {
                        for (ArbilField currentField : (ArbilField[]) this.getValueAt(currentRow, currentCol)) {
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
        Object cellValue = arbilTableModel.getValueAt(getSelectedRow(), getSelectedColumn());
        ArbilDataNode cellDataNode = null;
        if (cellValue instanceof ArbilField) {
            cellDataNode = ((ArbilField) cellValue).parentDataNode;
        } else if (cellValue instanceof ArbilField[]) {
            cellDataNode = ((ArbilField[]) cellValue)[0].parentDataNode;
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
    //jTable1.setAutoCreateRowSorter(true);
}
