/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolTip;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

/**
 *
 * @author petwit
 */
public class ImdiTable extends JTable {

    private ImdiTableModel imdiTableModel;
    JListToolTip listToolTip = new JListToolTip();

    public ImdiTable(ImdiTableModel localImdiTableModel, Enumeration rowNodesEnum, String frameTitle) {
        imdiTableModel = localImdiTableModel;
        imdiTableModel.setShowIcons(true);
        if (rowNodesEnum != null) {
            imdiTableModel.addImdiObjects(rowNodesEnum);
        }
        this.setModel(imdiTableModel);
        this.setName(frameTitle);
        //jTable1.doLayout();
        //jTable1.getModel().
        //jTable1.invalidate();

        setColumnWidths();
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        this.getTableHeader().addMouseListener(new java.awt.event.MouseAdapter() {
//            public void mousePressed(java.awt.event.MouseEvent evt) {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                System.out.println("table header click");
                targetColumn = convertColumnIndexToModel(((JTableHeader) evt.getComponent()).columnAtPoint(new Point(evt.getX(), evt.getY())));
                //targetTable = ((JTableHeader) evt.getComponent()).getTable();
                if (evt.getButton() == MouseEvent.BUTTON1) {
                    imdiTableModel.sortByColumn(targetColumn);
                }
                System.out.println("columnIndex: " + targetColumn);
                if (evt.getButton() == MouseEvent.BUTTON3) {
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

                    JMenuItem saveViewMenuItem = new JMenuItem("Save this view");
                    saveViewMenuItem.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent e) {
                            //System.out.println("saveViewNenuItem: " + targetTable.toString());
                            String fieldViewName = (String) JOptionPane.showInputDialog(null, "Enter a name to save this view as", "Save View", JOptionPane.PLAIN_MESSAGE);
                            // if the user did not cancel
                            if (fieldViewName != null) {
                                if (!GuiHelper.imdiFieldViews.addImdiFieldView(fieldViewName, imdiTableModel.getFieldView())) {
                                    JOptionPane.showMessageDialog(GuiHelper.linorgWindowManager.linorgFrame, "A View with the same name already exists, nothing saved");
                                }
                            }
                        }
                    });

                    JMenuItem editViewMenuItem = new JMenuItem("Edit this view");
                    editViewMenuItem.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent e) {
                            //System.out.println("editViewNenuItem: " + targetTable.toString());
                            JDialog editViewsDialog = new JDialog(JOptionPane.getFrameForComponent(GuiHelper.linorgWindowManager.linorgFrame), true);
                            Container dialogcontainer = editViewsDialog.getContentPane();
                            dialogcontainer.setLayout(new BorderLayout());
                            editViewsDialog.setSize(600, 400);
                            editViewsDialog.setBounds(50, 50, 600, 400);
                            TableModel tableModel = GuiHelper.imdiFieldViews.getImdiFieldViewTableModel(imdiTableModel.getFieldView());
                            tableModel.addTableModelListener(new TableModelListener() {

                                //private JTable dilalogTargetTable = targetTable;
                                public void tableChanged(TableModelEvent e) {
                                    TableModel localTableModel = (TableModel) e.getSource();
                                    String targetColumnName = localTableModel.getValueAt(e.getFirstRow(), 0).toString();
                                    boolean booleanState = localTableModel.getValueAt(e.getFirstRow(), e.getColumn()).equals(true);
//                                    System.out.println("name: " + targetColumnName);
//                                    System.out.println("value: " + booleanState);
//                                    System.out.println("pos: " + e.getColumn());
                                    switch (e.getColumn()) {
                                        case 2:
                                            if (booleanState) {
                                                imdiTableModel.getFieldView().addShowOnlyColumn(targetColumnName);
                                            } else {
                                                imdiTableModel.getFieldView().removeShowOnlyColumn(targetColumnName);
                                            }
                                            break;
                                        case 3:
                                            if (booleanState) {
                                                imdiTableModel.getFieldView().addHiddenColumn(targetColumnName);
                                            } else {
                                                imdiTableModel.getFieldView().removeHiddenColumn(targetColumnName);
                                            }
                                            break;
                                        case 4:
                                            if (booleanState) {
                                                imdiTableModel.getFieldView().addAlwaysShowColumn(targetColumnName);
                                            } else {
                                                imdiTableModel.getFieldView().removeAlwaysShowColumn(targetColumnName);
                                            }
                                            break;
                                    }
                                    imdiTableModel.reloadTableData();
                                //                                  throw new UnsupportedOperationException("Not supported yet.");
                                }
                            });
                            JTable ordertable = new JTable(tableModel);
                            JScrollPane js = new JScrollPane(ordertable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
                            js.setBounds(10, 10, 550, 350);
                            dialogcontainer.add(js);
                            editViewsDialog.add(js);
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
                    JMenu fieldViewsMenuItem = new JMenu("Apply Saved View");
                    ButtonGroup viewMenuButtonGroup = new javax.swing.ButtonGroup();
                    //String currentGlobalViewLabel = GuiHelper.imdiFieldViews.currentGlobalViewName;
                    for (Enumeration savedViewsEnum = GuiHelper.imdiFieldViews.getSavedFieldViewLables(); savedViewsEnum.hasMoreElements();) {
                        String currentViewLabel = savedViewsEnum.nextElement().toString();
                        javax.swing.JMenuItem viewLabelMenuItem;
                        viewLabelMenuItem = new javax.swing.JMenuItem();
                        viewMenuButtonGroup.add(viewLabelMenuItem);
                        //  viewLabelMenuItem.setSelected(currentGlobalViewLabel.equals(currentViewLabel));
                        viewLabelMenuItem.setText(currentViewLabel);
                        viewLabelMenuItem.setName(currentViewLabel);
                        viewLabelMenuItem.addActionListener(new ActionListener() {

                            public void actionPerformed(ActionEvent evt) {
                                imdiTableModel.setCurrentView(GuiHelper.imdiFieldViews.getView(((Component) evt.getSource()).getName()));
                            }
                        });
                        fieldViewsMenuItem.add(viewLabelMenuItem);
                    }
                    popupMenu.add(fieldViewsMenuItem);

                    JCheckBoxMenuItem constrainWidthMenuItem = new JCheckBoxMenuItem("Constrain table width");
                    constrainWidthMenuItem.setSelected(getAutoResizeMode() == JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
                    constrainWidthMenuItem.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent e) {
                            if (((JCheckBoxMenuItem) e.getSource()).isSelected()) {
                                System.out.println("AUTO_RESIZE_SUBSEQUENT_COLUMNS");
                                setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
                            } else {
                                System.out.println("AUTO_RESIZE_OFF");
                                setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                            }
                            // set sizes of the columns
                            setColumnWidths();
                        }
                    });

                    popupMenu.add(constrainWidthMenuItem);
                    popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }
        });

        this.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getButton() == MouseEvent.BUTTON3) {
                    // set the clicked cell selected
                    java.awt.Point p = evt.getPoint();
                    int clickedRow = rowAtPoint(p);
                    int clickedColumn = columnAtPoint(p);
                    boolean clickedRowAlreadySelected = isRowSelected(clickedRow);
                    
                    if (!evt.isShiftDown() && !evt.isControlDown()) {
                        // if it is the right mouse button and there is already a selection then do not proceed in changing the selection
                        if (!((evt.getButton() == MouseEvent.BUTTON3 && clickedRowAlreadySelected))) {
                            // if the modifier keys are down then leave the selection alone for the sake of more normal behaviour
                            getSelectionModel().clearSelection();
                            // make sure the clicked cell is selected
                            getSelectionModel().addSelectionInterval(clickedRow, clickedRow);
                            getColumnModel().getSelectionModel().addSelectionInterval(clickedColumn, clickedColumn);
                        // make sure the clicked cell is the lead selection
//                    getSelectionModel().setLeadSelectionIndex(rowIndex);
//                    getColumnModel().getSelectionModel().setLeadSelectionIndex(colIndex);
                        }
                    }
                }

                if (evt.getButton() == MouseEvent.BUTTON3) {
//                    targetTable = (JTable) evt.getComponent();
//                    System.out.println("set the current table");
                    JPopupMenu windowedTablePopupMenu;
                    windowedTablePopupMenu = new javax.swing.JPopupMenu();
                    windowedTablePopupMenu.setName("windowedTablePopupMenu");

                    if (getSelectedRow() != -1) {
                        JMenuItem copySelectedRowsMenuItem = new javax.swing.JMenuItem();
                        copySelectedRowsMenuItem.setText("Copy Selected Rows");
                        copySelectedRowsMenuItem.addActionListener(new java.awt.event.ActionListener() {

                            public void actionPerformed(java.awt.event.ActionEvent evt) {
                                copySelectedTableRowsToClipBoard();
                            }
                        });
                        windowedTablePopupMenu.add(copySelectedRowsMenuItem);

                        JMenuItem matchingRowsMenuItem = new javax.swing.JMenuItem();
                        matchingRowsMenuItem.setText("Select Matching Rows"); // NOI18N
                        matchingRowsMenuItem.addActionListener(new java.awt.event.ActionListener() {

                            public void actionPerformed(java.awt.event.ActionEvent evt) {
                                highlightMatchingRows();
                            }
                        });
                        windowedTablePopupMenu.add(matchingRowsMenuItem);

                        JMenuItem showChildNodesMenuItem = new javax.swing.JMenuItem();
                        showChildNodesMenuItem.setText("Show Child Nodes"); // NOI18N
                        showChildNodesMenuItem.addActionListener(new java.awt.event.ActionListener() {

                            public void actionPerformed(java.awt.event.ActionEvent evt) {
                                showRowChildData();
                            }
                        });
                        windowedTablePopupMenu.add(showChildNodesMenuItem);

                        JMenuItem removeSelectedRowsMenuItem = new javax.swing.JMenuItem();
                        removeSelectedRowsMenuItem.setText("Remove Selected Rows");
                        removeSelectedRowsMenuItem.addActionListener(new java.awt.event.ActionListener() {

                            public void actionPerformed(java.awt.event.ActionEvent evt) {
                                removeSelectedRowsFromTable();
                            }
                        });
                        windowedTablePopupMenu.add(removeSelectedRowsMenuItem);
                    }


                    if (getSelectedRow() != -1 && getSelectedColumn() != -1) {
                        // add a divider for the cell functions
                        windowedTablePopupMenu.add(new JSeparator());

                        JMenuItem copyCellToColumnMenuItem = new javax.swing.JMenuItem();
                        copyCellToColumnMenuItem.setText("Copy Cell to Whole Column"); // NOI18N
                        copyCellToColumnMenuItem.addActionListener(new java.awt.event.ActionListener() {

                            public void actionPerformed(java.awt.event.ActionEvent evt) {

                                if (0 == JOptionPane.showConfirmDialog(GuiHelper.linorgWindowManager.linorgFrame, "About to replace all values in column \"" + imdiTableModel.getColumnName(getSelectedColumn()) + "\"\nwith the value \"" + imdiTableModel.getValueAt(getSelectedRow(), getSelectedColumn()) + "\"", "Copy cell to whole column", JOptionPane.YES_NO_OPTION)) {
                                    imdiTableModel.copyCellToColumn(getSelectedRow(), getSelectedColumn());
                                }
                            }
                        });
                        windowedTablePopupMenu.add(copyCellToColumnMenuItem);

                        JMenuItem matchingCellsMenuItem = new javax.swing.JMenuItem();
                        matchingCellsMenuItem.setText("Highlight Matching Cells"); // NOI18N
                        matchingCellsMenuItem.addActionListener(new java.awt.event.ActionListener() {

                            public void actionPerformed(java.awt.event.ActionEvent evt) {
                                imdiTableModel.highlightMatchingCells(getSelectedRow(), getSelectedColumn());
                            }
                        });
                        windowedTablePopupMenu.add(matchingCellsMenuItem);

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
                    }
                }
            }
        });
    }

    public JToolTip createToolTip() {
        System.out.println("createToolTip");
        listToolTip.updateList();
        return listToolTip;
    }

//    @Override
//    public Dimension getPreferredScrollableViewportSize() {
//        System.out.println("getPreferredScrollableViewportSize");
//        Dimension size = super.getPreferredScrollableViewportSize();
//        return new Dimension(Math.min(getPreferredSize().width, size.width), size.height);
//    }
    @Override
    public TableCellEditor getCellEditor(int row, int viewcolumn) {
        int modelcolumn = convertColumnIndexToModel(viewcolumn);
        Object cellField = getModel().getValueAt(row, modelcolumn);
        if (cellField instanceof ImdiField) {
            System.out.println("getCellEditor: " + cellField.toString());
            if (((ImdiField) cellField).hasVocabulary()) {
                System.out.println("Has Vocabulary");
                JComboBox comboBox = new JComboBox();
                comboBox.setEditable(((ImdiField) cellField).vocabularyIsOpen);
                for (Enumeration vocabularyList = ((ImdiField) cellField).getVocabulary(); vocabularyList.hasMoreElements();) {
                    comboBox.addItem(vocabularyList.nextElement());
                }
// TODO: enable multiple selection for vocabulary lists
                comboBox.setSelectedItem(cellField.toString());
                return new DefaultCellEditor(comboBox);
            } else {
                //return super.getCellEditor(row, modelcolumn);
                return new DefaultCellEditor(new JTextField());
            }
        } else if (cellField instanceof Object[]) {
            return new ImdiChildCellEditor();
        }
        return super.getCellEditor(row, modelcolumn);
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int viewcolumn) {
        int modelcolumn = convertColumnIndexToModel(viewcolumn);
        Object cellField = getModel().getValueAt(row, modelcolumn);
        if (cellField instanceof ImdiTreeObject) {
            DefaultTableCellRenderer iconLabelRenderer = new DefaultTableCellRenderer();
            iconLabelRenderer.setIcon(((ImdiTreeObject) cellField).getIcon());
            iconLabelRenderer.setText(((ImdiTreeObject) cellField).toString());
            return iconLabelRenderer;
        } else if (cellField instanceof Object[]) {
            System.out.println("adding child nodes to cell");
            DefaultTableCellRenderer multiIconLabelRenderer = new DefaultTableCellRenderer();
            int currentIconXPosition = 0;

            int width = ImdiTreeObject.imdiIcons.corpusicon.getIconWidth() * ((Object[]) cellField).length;
            int height = ImdiTreeObject.imdiIcons.corpusicon.getIconHeight();
            if (width == 0) { // make sure that zero length child cells have a width
                width = ImdiTreeObject.imdiIcons.corpusicon.getIconWidth();
            }
            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2d = (Graphics2D) bufferedImage.getGraphics().create();

            Object[] childArray = (Object[]) cellField;
            for (Object childImdiObject : childArray) {
                Icon currentIcon = ((ImdiTreeObject) childImdiObject).getIcon();
                currentIcon.paintIcon(multiIconLabelRenderer, g2d, currentIconXPosition, 0);
                currentIconXPosition += currentIcon.getIconWidth();
            }
            g2d.dispose();
            multiIconLabelRenderer.setIcon(new ImageIcon(bufferedImage));
            multiIconLabelRenderer.setText(""); 
            return multiIconLabelRenderer;
        } else { //        add cell background colour
            DefaultTableCellRenderer fieldLabelRenderer = new DefaultTableCellRenderer();
            fieldLabelRenderer.setText(cellField.toString());
            fieldLabelRenderer.setBackground(imdiTableModel.getCellColour(row, modelcolumn));
            if (imdiTableModel.hasValueChanged(row, modelcolumn)) {
                fieldLabelRenderer.setForeground(Color.blue);
            }
            return fieldLabelRenderer;
        }
//        return super.getCellRenderer(row, modelcolumn);
    }

    public void showRowChildData() {
        Object[] possibilities = ((ImdiTableModel) this.getModel()).getChildNames();
        String selectionResult = (String) JOptionPane.showInputDialog(GuiHelper.linorgWindowManager.linorgFrame, "Select the child node type to display", "Show child nodes", JOptionPane.PLAIN_MESSAGE, null, possibilities, null);

        if ((selectionResult != null) && (selectionResult.length() > 0)) {
            ((ImdiTableModel) this.getModel()).addChildTypeToDisplay(selectionResult);
        }
    }

    private void setColumnWidths() {
        int charPixWidth = 9; // this does not need to be accurate but must be more than the number of pixels used to render each character
        for (int columnCount = 0; columnCount < this.getModel().getColumnCount(); columnCount++) {
            this.getColumnModel().getColumn(columnCount).setPreferredWidth(((ImdiTableModel) this.getModel()).getColumnLength(columnCount) * charPixWidth);
            System.out.println("preferedWidth: " + ((ImdiTableModel) this.getModel()).getColumnLength(columnCount));
        }
    }
//    private JTable targetTable = null; // this is used to track the originator of the table's menu actions, this method is not preferable however the menu event does not pass on the originator
    //private int targetRow;
    private int targetColumn;
    // create a window containing a table of nodes
//    public ImdiTable(Enumeration rowNodesEnum, String frameTitle) {
//        javax.swing.JTable jTable1;
//        javax.swing.JScrollPane jScrollPane6;
//        jScrollPane6 = new javax.swing.JScrollPane();
//        jTable1 = new ImdiTable() {

//            public TableCellEditor getCellEditor(int row, int column) {
//                if (column == 2) {
//                    JComboBox comboBox = new JComboBox();
//                    comboBox.addItem("Item 1");
//                    comboBox.addItem("Item 2");
//                    comboBox.addItem("Item 3");
//                    comboBox.addItem("Item 4");
//                    comboBox.addItem("Item 5");
//                    comboBox.addItem("Item 6");
//                    return new DefaultCellEditor(comboBox);
//                }
//                if (column == 3) {
//                    //openFloatingTable(this, getImdiTableModel({new ImdiHelper.ImdiTreeObject("one", null), new ImdiHelper.ImdiTreeObject("one", null)}), frameTitle, jPopupMenu);
//                }
//                return super.getCellEditor(row, column);
//            }
    // this cell renderer may have caused redraw issues
//            public TableCellRenderer getCellRenderer(int row, int column) {
//                if (column == 3 && (row == 2 || row == 0)) {
//                    TableCellRenderer listTableCellRenderer = new TableCellRenderer() {
//
//                        public Component getTableCellRendererComponent(JTable table, Object color, boolean isSelected, boolean hasFocus, int row, int column) {
//                            JList cellList;
//                            if (row == 0) {
//                                cellList = new JList(new Object[]{"Item 1", "Item 2", "Item 3", "Item 4", "Item 5"});
//                            } else {
//                                cellList = new JList(new Object[]{"Item 1", "Item 2", "Item 5"});
//                            }
//                            //cellListHeight = cellList.getPreferredSize().height;
//                            table.setRowHeight(row, cellList.getPreferredSize().height);
//                            return cellList;
//                        }
//                    };
////                    if (cellListHeight > super.getRowHeight()) {
////                        super.setRowHeight(row, cellListHeight);
////                    }
////                        public Component getTableCellRendererComponent(JTable jTable,
////                                Object obj, boolean isSelected, boolean hasFocus, int row,
////                                int column) {
////                            setText((String) obj);
////                            int height_wanted = (int) getPreferredSize().getHeight();
////                            if (height_wanted != jTable.getRowHeight(row)) {
////                                jTable.setRowHeight(row, height_wanted);
////                            }
////                            return this;
////                        }
//                    return listTableCellRenderer;
//                } //                if (row == 5) {
//                //                    setBackground(Color.green);
//                //                }
//                return super.getCellRenderer(row, column);
//            }
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
//Implement table header tool tips.
    protected JTableHeader createDefaultTableHeader() {
        return new JTableHeader(columnModel) {

            public String getToolTipText(MouseEvent e) {
                String tip = null;
                java.awt.Point p = e.getPoint();
                int index = columnModel.getColumnIndexAtX(p.x);
                //int realIndex = columnModel.getColumn(index).getModelIndex();
                return GuiHelper.imdiSchema.getHelpForField(getColumnName(index));
            }
        };
    }

    public void copySelectedTableRowsToClipBoard() {
        int[] selectedRows = this.getSelectedRows();
        // only copy if there is at lease one row selected
        if (selectedRows.length > 0) {
            imdiTableModel.copyImdiRows(selectedRows, GuiHelper.clipboardOwner);
        } else {
            JOptionPane.showMessageDialog(GuiHelper.linorgWindowManager.linorgFrame, "Nothing to copy");
        }
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
            JOptionPane.showMessageDialog(GuiHelper.linorgWindowManager.linorgFrame, "No rows have been selected");
            return;
        }
        Vector foundRows = imdiTableModel.getMatchingRows(selectedRow);
        this.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        this.getSelectionModel().clearSelection();
        JOptionPane.showMessageDialog(GuiHelper.linorgWindowManager.linorgFrame, "Found " + foundRows.size() + " matching rows");
        for (int foundCount = 0; foundCount < foundRows.size(); foundCount++) {
            for (int coloumCount = 0; coloumCount < this.getColumnCount(); coloumCount++) {
                // TODO: this could be more efficient if the array was converted into selection intervals rather than individual rows (although the SelectionModel might already do this)
                this.getSelectionModel().addSelectionInterval((Integer) foundRows.get(foundCount), (Integer) foundRows.get(foundCount));
            }
        }
    }
    //jTable1.setAutoCreateRowSorter(true);
}
