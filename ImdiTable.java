package mpi.linorg;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
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
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JToolTip;
import javax.swing.ListSelectionModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 * Document   : ImdiTable
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
public class ImdiTable extends JTable {

    private ImdiTableModel imdiTableModel;
    JListToolTip listToolTip = new JListToolTip();

    public ImdiTable(ImdiTableModel localImdiTableModel, String frameTitle) {
        imdiTableModel = localImdiTableModel;
        imdiTableModel.setShowIcons(true);
//        if (rowNodesArray != null) {
//            imdiTableModel.addImdiObjects(rowNodesArray);
//        }
        this.setModel(imdiTableModel);
        this.setName(frameTitle);
        this.setCellSelectionEnabled(true);
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        this.setGridColor(Color.LIGHT_GRAY);
        
        this.getTableHeader().addMouseListener(new java.awt.event.MouseAdapter() {
//            public void mousePressed(java.awt.event.MouseEvent evt) {

            @Override
            public void mousePressed(MouseEvent evt) {
//                System.out.println("mousePressed");
                checkPopup(evt);
            }

            @Override
            public void mouseReleased(MouseEvent evt) {
//                System.out.println("mouseReleased");
                checkPopup(evt);
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                System.out.println("mouseClicked");
                System.out.println("table header click");
                targetColumn = convertColumnIndexToModel(((JTableHeader) evt.getComponent()).columnAtPoint(new Point(evt.getX(), evt.getY())));
                //targetTable = ((JTableHeader) evt.getComponent()).getTable();
                if (evt.getButton() == MouseEvent.BUTTON1) {
                    imdiTableModel.sortByColumn(targetColumn);
                }
                checkPopup(evt);
            }

            private void checkPopup(java.awt.event.MouseEvent evt) {
                System.out.println("columnIndex: " + targetColumn);
                if (evt.isPopupTrigger() /* evt.getButton() == MouseEvent.BUTTON3*/) {
                    //targetTable = ((JTableHeader) evt.getComponent()).getTable();
                    System.out.println("columnIndex: " + targetColumn);

                    JPopupMenu popupMenu = new JPopupMenu();

                    JMenuItem hideColumnMenuItem = new JMenuItem("Hide column: \"" + imdiTableModel.getColumnName(targetColumn) + "\"");
                    hideColumnMenuItem.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent e) {
                            //System.out.println("hideColumnMenuItem: " + targetTable.toString());
                            imdiTableModel.hideColumn(targetColumn);
                        }
                    });

                    JMenuItem saveViewMenuItem = new JMenuItem("Save this Column View");
                    saveViewMenuItem.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent e) {
                            //System.out.println("saveViewNenuItem: " + targetTable.toString());
                            String fieldViewName = (String) JOptionPane.showInputDialog(null, "Enter a name to save this Column View as", "Save Column View", JOptionPane.PLAIN_MESSAGE);
                            // if the user did not cancel
                            if (fieldViewName != null) {
                                if (!ImdiFieldViews.getSingleInstance().addImdiFieldView(fieldViewName, imdiTableModel.getFieldView())) {
                                    LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("A Column View with the same name already exists, nothing saved", "Save Column View");
                                }
                            }
                        }
                    });

                    JMenuItem editViewMenuItem = new JMenuItem("Edit this Column View");
                    editViewMenuItem.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent e) {
                            ImdiFieldViewTable fieldViewTable = new ImdiFieldViewTable(imdiTableModel);
                            JDialog editViewsDialog = new JDialog(JOptionPane.getFrameForComponent(LinorgWindowManager.getSingleInstance().linorgFrame), true);
                            editViewsDialog.setTitle("Editing Current Column View");

                            JScrollPane js = new JScrollPane(fieldViewTable);
                            editViewsDialog.getContentPane().add(js);
                            editViewsDialog.setBounds(50, 50, 600, 400);
                            editViewsDialog.setVisible(true);
                        }
                    });

                    JMenuItem showOnlyCurrentViewMenuItem = new JMenuItem("Show only the current columns");
                    showOnlyCurrentViewMenuItem.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent e) {
                            //System.out.println("saveViewNenuItem: " + targetTable.toString());
                            imdiTableModel.showOnlyCurrentColumns();
                        }
                    });
                    //popupMenu.add(applyViewNenuItem);
                    //popupMenu.add(saveViewMenuItem);
                    popupMenu.add(editViewMenuItem);
                    popupMenu.add(showOnlyCurrentViewMenuItem);
                    popupMenu.add(saveViewMenuItem);
                    popupMenu.add(hideColumnMenuItem);
                    // create the views sub menu
                    JMenu fieldViewsMenuItem = new JMenu("Apply Saved Column View");
                    ButtonGroup viewMenuButtonGroup = new javax.swing.ButtonGroup();
                    //String currentGlobalViewLabel = GuiHelper.imdiFieldViews.currentGlobalViewName;
                    for (Enumeration savedViewsEnum = ImdiFieldViews.getSingleInstance().getSavedFieldViewLables(); savedViewsEnum.hasMoreElements();) {
                        String currentViewLabel = savedViewsEnum.nextElement().toString();
                        javax.swing.JMenuItem viewLabelMenuItem;
                        viewLabelMenuItem = new javax.swing.JMenuItem();
                        viewMenuButtonGroup.add(viewLabelMenuItem);
                        //  viewLabelMenuItem.setSelected(currentGlobalViewLabel.equals(currentViewLabel));
                        viewLabelMenuItem.setText(currentViewLabel);
                        viewLabelMenuItem.setName(currentViewLabel);
                        viewLabelMenuItem.addActionListener(new ActionListener() {

                            public void actionPerformed(ActionEvent evt) {
                                imdiTableModel.setCurrentView(ImdiFieldViews.getSingleInstance().getView(((Component) evt.getSource()).getName()));
                            }
                        });
                        fieldViewsMenuItem.add(viewLabelMenuItem);
                    }
                    popupMenu.add(fieldViewsMenuItem);
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
        if (evt.isPopupTrigger() /* evt.getButton() == MouseEvent.BUTTON3 || evt.isMetaDown()*/) {
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
                        getSelectionModel().addSelectionInterval(clickedRow, clickedRow);
                        getColumnModel().getSelectionModel().addSelectionInterval(clickedColumn, clickedColumn);
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
            JPopupMenu windowedTablePopupMenu;
            windowedTablePopupMenu = new javax.swing.JPopupMenu();
            windowedTablePopupMenu.setName("windowedTablePopupMenu");

            if (getSelectedRow() != -1) {

                JMenuItem copySelectedRowsMenuItem = new javax.swing.JMenuItem();
                copySelectedRowsMenuItem.setText("Copy");
                copySelectedRowsMenuItem.addActionListener(new java.awt.event.ActionListener() {

                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        copySelectedTableRowsToClipBoard();
                    }
                });
                windowedTablePopupMenu.add(copySelectedRowsMenuItem);

                JMenuItem pasteIntoSelectedRowsMenuItem = new javax.swing.JMenuItem();
                pasteIntoSelectedRowsMenuItem.setText("Paste");
                pasteIntoSelectedRowsMenuItem.addActionListener(new java.awt.event.ActionListener() {

                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        pasteIntoSelectedTableRowsToClipBoard();
                    }
                });
                windowedTablePopupMenu.add(pasteIntoSelectedRowsMenuItem);

                if (imdiTableModel.horizontalView) {
                    JMenuItem viewSelectedRowsMenuItem = new javax.swing.JMenuItem();
                    viewSelectedRowsMenuItem.setText("View Selected Rows");
                    viewSelectedRowsMenuItem.addActionListener(new java.awt.event.ActionListener() {

                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                            viewSelectedTableRows();
                        }
                    });
                    windowedTablePopupMenu.add(viewSelectedRowsMenuItem);
                }

                if (imdiTableModel.horizontalView) {
                    JMenuItem matchingRowsMenuItem = new javax.swing.JMenuItem();
                    matchingRowsMenuItem.setText("Select Matching Rows"); // NOI18N
                    matchingRowsMenuItem.addActionListener(new java.awt.event.ActionListener() {

                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                            highlightMatchingRows();
                        }
                    });
                    windowedTablePopupMenu.add(matchingRowsMenuItem);
                }

                if (imdiTableModel.horizontalView) {
                    JMenuItem showChildNodesMenuItem = new javax.swing.JMenuItem();
                    showChildNodesMenuItem.setText("Show Child Nodes"); // NOI18N
                    showChildNodesMenuItem.addActionListener(new java.awt.event.ActionListener() {

                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                            showRowChildData();
                        }
                    });
                    windowedTablePopupMenu.add(showChildNodesMenuItem);
                }

                if (imdiTableModel.horizontalView) {
                    JMenuItem removeSelectedRowsMenuItem = new javax.swing.JMenuItem();
                    removeSelectedRowsMenuItem.setText("Remove Selected Rows");
                    removeSelectedRowsMenuItem.addActionListener(new java.awt.event.ActionListener() {

                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                            removeSelectedRowsFromTable();
                        }
                    });
                    windowedTablePopupMenu.add(removeSelectedRowsMenuItem);
                }
            }


            if (getSelectedRow() != -1 && getSelectedColumn() != -1) {
                // add a divider for the cell functions
                windowedTablePopupMenu.add(new JSeparator());

                if (imdiTableModel.horizontalView && getSelectionModel().getSelectionMode() == ListSelectionModel.SINGLE_INTERVAL_SELECTION) {
                    JMenuItem copyCellToColumnMenuItem = new javax.swing.JMenuItem();
                    copyCellToColumnMenuItem.setText("Copy Cell to Whole Column"); // NOI18N
                    copyCellToColumnMenuItem.addActionListener(new java.awt.event.ActionListener() {

                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                            if (!(imdiTableModel.getValueAt(getSelectedRow(), getSelectedColumn()) instanceof ImdiField)) {
                                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Cannot copy this type of field", "Copy Cell to Whole Column");
                            } else if (0 == JOptionPane.showConfirmDialog(LinorgWindowManager.getSingleInstance().linorgFrame, "About to replace all values in column \"" + imdiTableModel.getColumnName(getSelectedColumn()) + "\"\nwith the value \"" + imdiTableModel.getValueAt(getSelectedRow(), getSelectedColumn()) + "\"", "Copy cell to whole column", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE)) {
                                imdiTableModel.copyCellToColumn(getSelectedRow(), getSelectedColumn());
                            }
                        }
                    });
                    windowedTablePopupMenu.add(copyCellToColumnMenuItem);
                }

                JMenuItem matchingCellsMenuItem = new javax.swing.JMenuItem();
                matchingCellsMenuItem.setText("Highlight Matching Cells"); // NOI18N
                matchingCellsMenuItem.addActionListener(new java.awt.event.ActionListener() {

                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        imdiTableModel.highlightMatchingCells(getSelectedRow(), getSelectedColumn());
                    }
                });
                windowedTablePopupMenu.add(matchingCellsMenuItem);

                JMenuItem jumpToNodeInTreeMenuItem = new javax.swing.JMenuItem();
                jumpToNodeInTreeMenuItem.setText("Jump to in Tree"); // NOI18N
                jumpToNodeInTreeMenuItem.addActionListener(new java.awt.event.ActionListener() {

                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        TreeHelper.getSingleInstance().jumpToSelectionInTree(false, getImdiNodeForSelection());
                    }
                });
                windowedTablePopupMenu.add(jumpToNodeInTreeMenuItem);

                JMenuItem clearCellColoursMenuItem = new javax.swing.JMenuItem();
                clearCellColoursMenuItem.setText("Clear Cell Highlight"); // NOI18N
                clearCellColoursMenuItem.addActionListener(new java.awt.event.ActionListener() {

                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        imdiTableModel.clearCellColours();
                    }
                });
                windowedTablePopupMenu.add(clearCellColoursMenuItem);
            }
            if (windowedTablePopupMenu.getComponentCount() > 0) {
                windowedTablePopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
                TableCellEditor tableCellEditor = ImdiTable.this.getCellEditor();
                if (tableCellEditor != null) {
                    tableCellEditor.stopCellEditing();
                }
            }
        }
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return /*getParent() instanceof JViewport && */ getPreferredSize().height < getParent().getHeight();
    }

    @Override
    public void doLayout() {
        setColumnWidths();
        super.doLayout();
    }

    public JToolTip createToolTip() {
        System.out.println("createToolTip");
        listToolTip.updateList();
        return listToolTip;
    }

    @Override
    public TableCellEditor getCellEditor(int row, int viewcolumn) {
        int modelcolumn = convertColumnIndexToModel(viewcolumn);
        Object cellField = getModel().getValueAt(row, modelcolumn);
//        System.out.println("getCellEditor: " + cellField.toString());
        return new ImdiChildCellEditor();
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int viewcolumn) {
        int modelcolumn = convertColumnIndexToModel(viewcolumn);
        ImdiTableCellRenderer imdiCellRenderer = new ImdiTableCellRenderer();
        imdiCellRenderer.setBackground(imdiTableModel.getCellColour(row, modelcolumn));
        return imdiCellRenderer;
    }

    public void showRowChildData() {
        Object[] possibilities = ((ImdiTableModel) this.getModel()).getChildNames();
        String selectionResult = (String) JOptionPane.showInputDialog(LinorgWindowManager.getSingleInstance().linorgFrame, "Select the child node type to display", "Show child nodes", JOptionPane.PLAIN_MESSAGE, null, possibilities, null);
//      TODO: JOptionPane.show it would be good to have a miltiple select here
        if ((selectionResult != null) && (selectionResult.length() > 0)) {
            ((ImdiTableModel) this.getModel()).addChildTypeToDisplay(selectionResult);
        }
    }
    int lastColumnCount = -1;

    private void setColumnWidths() {
        // resize the columns only if the number of columns have changed
        boolean resizeColumns = lastColumnCount != this.getModel().getColumnCount();
        lastColumnCount = this.getModel().getColumnCount();

        int charPixWidth = 9; // this does not need to be accurate but must be more than the number of pixels used to render each character
        int maxColumnWidth = 300;
        int totalWidth = 0;
        int minWidth = 50;
        for (int columnCount = 0; columnCount < this.getColumnModel().getColumnCount(); columnCount++) {
//            System.out.println("defaultPreferedWidth: " + this.getColumnModel().getColumn(columnCount).getPreferredWidth());
            // setPreferredWidth || setMinWidth
            int currentWidth = ((ImdiTableModel) this.getModel()).getColumnWidth(columnCount) * charPixWidth;
            if (currentWidth > maxColumnWidth) {
                currentWidth = maxColumnWidth;
            }
            if (currentWidth < minWidth) {
                currentWidth = minWidth;
            }
            if (resizeColumns) {
                this.getColumnModel().getColumn(columnCount).setPreferredWidth(currentWidth);
            }
            totalWidth += this.getColumnModel().getColumn(columnCount).getPreferredWidth();
//            System.out.println("preferedWidth: " + ((ImdiTableModel) this.getModel()).getColumnWidth(columnCount));
        }
        int parentWidth = 800;
        int parentHeight = 600;
        if (this.getParent() != null) {
            parentWidth = this.getParent().getWidth();
            parentHeight = this.getParent().getHeight();
        }
//        System.out.println("totalWidth: " + totalWidth + "ParentWidth: " + parentWidth);
//        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//        setPreferredScrollableViewportSize(new Dimension(Math.max(totalWidth, parentWidth), parentHeight));
//        if (totalWidth < parentWidth) {
//            int lastColumn = this.getModel().getColumnCount() - 1;
//            if (lastColumn >= 0) {
//                this.getColumnModel().getColumn(lastColumn).setPreferredWidth(this.getColumnModel().getColumn(lastColumn).getPreferredWidth() + parentWidth - totalWidth);
//            }
//        }
        //        setPreferredSize(new Dimension(Math.max(totalWidth, parentWidth), parentHeight));
        if (totalWidth < parentWidth) {
//            System.out.println("AUTO_RESIZE_SUBSEQUENT_COLUMNS");
//            setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
            setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        } else {
//            System.out.println("AUTO_RESIZE_OFF");
            setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        }
    }
    private int targetColumn;
    //Implement table cell tool tips.
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
        if (imdiTableModel.horizontalView) {
            boolean rowSelection = (imdiTableModel.getValueAt(rowIndex, columnIndex) instanceof ImdiTreeObject);
//        System.out.println("rowSelection: " + rowSelection + ":" + clickedRow + ":" + clickedColumn);
            if (!imdiTableModel.horizontalView) {
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
            if (this.getCellSelectionEnabled()) {
                System.out.println("cell select mode");
                ImdiField[] selectedFields = getSelectedFields();
                if (selectedFields != null) {
                    imdiTableModel.copyImdiFields(selectedFields, GuiHelper.clipboardOwner);
                }
            } else {
                System.out.println("row select mode");
                imdiTableModel.copyImdiRows(selectedRows, GuiHelper.clipboardOwner);
            }
        } else {
            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Nothing selected to copy", "Table Copy");
        }
    }

    private ImdiField[] getSelectedFields() {
        HashSet<ImdiField> selectedFields = new HashSet<ImdiField>();
        int[] selectedRows = this.getSelectedRows();
        if (selectedRows.length > 0) {
            int[] selectedCols = this.getSelectedColumns();
            for (int currentRow : selectedRows) {
                for (int currentCol : selectedCols) {
                    System.out.println("row/col: " + currentRow + " : " + currentCol);
                    // this could be an imdifield array and must handled accortingly
                    if (this.getValueAt(currentRow, currentCol) instanceof ImdiField) {
                        selectedFields.add((ImdiField) this.getValueAt(currentRow, currentCol));
                    } else if (this.getValueAt(currentRow, currentCol) instanceof ImdiField[]) {
                        for (ImdiField currentField : (ImdiField[]) this.getValueAt(currentRow, currentCol)) {
                            selectedFields.add(currentField);
                        }
                    }
                }
            }
            return selectedFields.toArray(new ImdiField[]{});
        } else {
            return null;
        }
    }

    public void pasteIntoSelectedTableRowsToClipBoard() {
        ImdiField[] selectedFields = getSelectedFields();
        if (selectedFields != null) {
            String pasteResult = imdiTableModel.pasteIntoImdiFields(selectedFields);
            if (pasteResult != null) {
                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue(pasteResult, "Paste into Table");
            }
        } else {
            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("No rows selected", "Paste into Table");
        }
    }

    public void viewSelectedTableRows() {
        int[] selectedRows = this.getSelectedRows();
        LinorgWindowManager.getSingleInstance().openFloatingTableOnce(imdiTableModel.getSelectedImdiNodes(selectedRows), null);
    }

    public ImdiTreeObject getImdiNodeForSelection() {
        Object cellValue = imdiTableModel.getValueAt(getSelectedRow(), getSelectedColumn());
        ImdiTreeObject cellImdiNode = null;
        if (cellValue instanceof ImdiField) {
            cellImdiNode = ((ImdiField) cellValue).parentImdi;
        } else if (cellValue instanceof ImdiField[]) {
            cellImdiNode = ((ImdiField[]) cellValue)[0].parentImdi;
        } else if (cellValue instanceof ImdiTreeObject) {
            cellImdiNode = (ImdiTreeObject) cellValue;
        } else if (cellValue instanceof ImdiTreeObject[]) {
            cellImdiNode = ((ImdiTreeObject[]) cellValue)[0];
        }
        return cellImdiNode;
    }

    public ImdiTreeObject[] getSelectedRowsFromTable() {
        int[] selectedRows = this.getSelectedRows();
        return imdiTableModel.getSelectedImdiNodes(selectedRows);
    }

    public void removeSelectedRowsFromTable() {
        int[] selectedRows = this.getSelectedRows();
        imdiTableModel.removeImdiRows(selectedRows);
    }

    public void highlightMatchingRows() {
        int selectedRow = this.getSelectedRow();
        //ImdiHelper.ImdiTableModel tempImdiTableModel = (ImdiHelper.ImdiTableModel) (targetTable.getModel());
        if (selectedRow == -1) {
            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("No rows have been selected", "Highlight Matching Rows");
            return;
        }
        Vector foundRows = imdiTableModel.getMatchingRows(selectedRow);
        this.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        this.getSelectionModel().clearSelection();
        LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Found " + foundRows.size() + " matching rows", "Highlight Matching Rows");
        for (int foundCount = 0; foundCount < foundRows.size(); foundCount++) {
            for (int coloumCount = 0; coloumCount < this.getColumnCount(); coloumCount++) {
                // TODO: this could be more efficient if the array was converted into selection intervals rather than individual rows (although the SelectionModel might already do this)
                this.getSelectionModel().addSelectionInterval((Integer) foundRows.get(foundCount), (Integer) foundRows.get(foundCount));
            }
        }
    }
    //jTable1.setAutoCreateRowSorter(true);
}
