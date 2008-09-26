/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.ButtonGroup;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;

/**
 *
 * @author petwit
 */
public class ImdiTable extends JTable {

    private ImdiHelper.ImdiTableModel imdiTableModel;
    private ImdiFieldViews imdiFieldViews;

    public ImdiTable(ImdiFieldViews localImdiFieldViews, ImdiHelper.ImdiTableModel localImdiTableModel, Enumeration rowNodesEnum, String frameTitle) {
        imdiFieldViews = localImdiFieldViews;
        imdiTableModel = localImdiTableModel;
        imdiTableModel.setShowIcons(true);
        imdiTableModel.addImdiObjects(rowNodesEnum);
        this.setModel(imdiTableModel);
        this.setName(frameTitle);
        //jTable1.doLayout();
        //jTable1.getModel().
        //jTable1.invalidate();

        setColumnWidths();

        this.getTableHeader().addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                System.out.println("table header click");
                targetColumn = ((JTableHeader) evt.getComponent()).columnAtPoint(new Point(evt.getX(), evt.getY()));
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
                                if (!imdiFieldViews.addImdiFieldView(fieldViewName, imdiTableModel.getFieldView())) {
                                    JOptionPane.showMessageDialog(null, "A View with the same name already exists, nothing saved");
                                }
                            }
                        }
                    });

                    JMenuItem editViewMenuItem = new JMenuItem("Edit this view");
                    editViewMenuItem.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent e) {
                            //System.out.println("editViewNenuItem: " + targetTable.toString());
                            JDialog editViewsDialog = new JDialog(JOptionPane.getFrameForComponent(GuiHelper.linorgWindowManager.desktopPane), true);
                            Container dialogcontainer = editViewsDialog.getContentPane();
                            dialogcontainer.setLayout(new BorderLayout());
                            editViewsDialog.setSize(600, 400);
                            editViewsDialog.setBounds(50, 50, 600, 400);
                            TableModel tableModel = imdiFieldViews.getImdiFieldViewTableModel(imdiTableModel.getFieldView());
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
                                        case 3:
                                            if (booleanState) {
                                                imdiTableModel.getFieldView().addHiddenColumn(targetColumnName);
                                            } else {
                                                imdiTableModel.getFieldView().removeHiddenColumn(targetColumnName);
                                            }
                                        case 4:
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

                    popupMenu.add(showOnlyCurrentViewMenuItem);
                    //popupMenu.add(applyViewNenuItem);
                    //popupMenu.add(saveViewMenuItem);
                    popupMenu.add(editViewMenuItem);
                    popupMenu.add(saveViewMenuItem);
                    popupMenu.add(hideColumnMenuItem);
                    // create the views sub menu
                    JMenu fieldViewsMenuItem = new JMenu("Apply Saved View");
                    ButtonGroup viewMenuButtonGroup = new javax.swing.ButtonGroup();
                    String currentGlobalViewLabel = imdiFieldViews.currentGlobalViewName;
                    for (Enumeration savedViewsEnum = imdiFieldViews.getSavedFieldViewLables(); savedViewsEnum.hasMoreElements();) {
                        String currentViewLabel = savedViewsEnum.nextElement().toString();
                        javax.swing.JRadioButtonMenuItem viewLabelRadioButtonMenuItem;
                        viewLabelRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
                        viewMenuButtonGroup.add(viewLabelRadioButtonMenuItem);
                        viewLabelRadioButtonMenuItem.setSelected(currentGlobalViewLabel.equals(currentViewLabel));
                        viewLabelRadioButtonMenuItem.setText(currentViewLabel);
                        viewLabelRadioButtonMenuItem.setName(currentViewLabel);
                        viewLabelRadioButtonMenuItem.addChangeListener(new javax.swing.event.ChangeListener() {

                            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                                // TODO: this should change the data grid view
                                //imdiFieldViews.setCurrentGlobalViewName(((Component) evt.getSource()).getName());
                            }
                        });
                        fieldViewsMenuItem.add(viewLabelRadioButtonMenuItem);
                    }
                    popupMenu.add(fieldViewsMenuItem);
                    popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }
        });

//        this.addMouseListener(new java.awt.event.MouseAdapter() {
//
//            public void mouseClicked(java.awt.event.MouseEvent evt) {
//                if (evt.getButton() == MouseEvent.BUTTON3) {
////                    targetTable = (JTable) evt.getComponent();
////                    System.out.println("set the current table");
//                //jPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
//                }
//            }
//        });
    }

    public void showRowChildData(JDesktopPane destinationComp) {
        Object[] possibilities = ((ImdiHelper.ImdiTableModel) this.getModel()).getChildNames();
        String selectionResult = (String) JOptionPane.showInputDialog(destinationComp, "Select the child node type to display", "Show child nodes", JOptionPane.PLAIN_MESSAGE, null, possibilities, null);

        if ((selectionResult != null) && (selectionResult.length() > 0)) {
            ((ImdiHelper.ImdiTableModel) this.getModel()).addChildTypeToDisplay(selectionResult);
        }
    }

    private void setColumnWidths() {
        int charPixWidth = 100; // this does not need to be accurate but must be more than the number of pixels used to render each character
        for (int columnCount = 0; columnCount < this.getModel().getColumnCount(); columnCount++) {
            this.getColumnModel().getColumn(columnCount).setPreferredWidth(((ImdiHelper.ImdiTableModel) this.getModel()).getColumnLength(columnCount) * charPixWidth);
            System.out.println("preferedWidth: " + ((ImdiHelper.ImdiTableModel) this.getModel()).getColumnLength(columnCount));
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
        tip = getValueAt(rowIndex, colIndex).toString();
        return tip;
    }
    //Implement table header tool tips.
    protected JTableHeader createDefaultTableHeader() {
        return new JTableHeader 

              (columnModel ) {

                   public String getToolTipText(MouseEvent e) {
                String tip = null;
                java.awt.Point p = e.getPoint();
                int index = columnModel.getColumnIndexAtX(p.x);
                //int realIndex = columnModel.getColumn(index).getModelIndex();
                return "Description of how to use the " + getColumnName(index) + " colomn.";
            }
        };
    }
    
    
    public void copySelectedTableRowsToClipBoard(Component destinationComp) {
        int[] selectedRows = this.getSelectedRows();
        // only copy if there is at lease one row selected
        if (selectedRows.length > 0) {
            imdiTableModel.copyImdiRows(selectedRows, GuiHelper.clipboardOwner);
        } else {
            JOptionPane.showMessageDialog(destinationComp, "Nothing to copy");
        }
    }

    public void removeSelectedRowsFromTable() {
        int[] selectedRows = this.getSelectedRows();
        imdiTableModel.removeImdiRows(selectedRows);
    }

    public void highlightMatchingRows(Component destinationComp) {
        int selectedRow = this.getSelectedRow();
        //ImdiHelper.ImdiTableModel tempImdiTableModel = (ImdiHelper.ImdiTableModel) (targetTable.getModel());
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(destinationComp, "No rows have been selected");
            return;
        }
        Vector foundRows = imdiTableModel.getMatchingRows(selectedRow);
        this.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        this.getSelectionModel().clearSelection();
        JOptionPane.showMessageDialog(destinationComp, "Found " + foundRows.size() + " matching rows");
        for (int foundCount = 0; foundCount < foundRows.size(); foundCount++) {
            for (int coloumCount = 0; coloumCount < this.getColumnCount(); coloumCount++) {
                // TODO: this could be more efficient if the array was converted into selection intervals rather than individual rows (although the SelectionModel might already do this)
                this.getSelectionModel().addSelectionInterval((Integer) foundRows.get(foundCount), (Integer) foundRows.get(foundCount));
            }
        }
    }
    //jTable1.setAutoCreateRowSorter(true);
}
