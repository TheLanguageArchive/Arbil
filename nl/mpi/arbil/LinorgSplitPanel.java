package mpi.linorg;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;

/**
 * Document   : LinorgSplitPanel
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
public class LinorgSplitPanel extends JPanel {

    private JList fileList;
    public ImdiTable imdiTable;
    private JScrollPane tableScrollPane;
    private JScrollPane listScroller;
    private JSplitPane splitPane;
    private JLabel hiddenColumnsLabel;
    private JPanel tableOuterPanel;

    public LinorgSplitPanel(ImdiTable localImdiTable) {
//            setBackground(new Color(0xFF00FF));
        this.setLayout(new BorderLayout());

        imdiTable = localImdiTable;
        splitPane = new JSplitPane();
        hiddenColumnsLabel = new JLabel();
        tableScrollPane = new JScrollPane(imdiTable);
        tableOuterPanel = new JPanel(new BorderLayout());
        tableOuterPanel.add(tableScrollPane, BorderLayout.CENTER);
        tableOuterPanel.add(hiddenColumnsLabel, BorderLayout.SOUTH);
        ((ImdiTableModel) localImdiTable.getModel()).setHiddenColumnsLabel(hiddenColumnsLabel);
        fileList = new JList(((ImdiTableModel) localImdiTable.getModel()).getListModel(this));
        fileList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        fileList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        fileList.setVisibleRowCount(-1);

        listScroller = new JScrollPane(fileList);
        listScroller.setPreferredSize(new Dimension(250, 80));

        ImageBoxRenderer renderer = new ImageBoxRenderer();
        fileList.setCellRenderer(renderer);
        splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerSize(5);
        fileList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        fileList.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if (fileList.hasFocus()) {
                    if (e.getSource() instanceof JList) {
//                        System.out.println("JList");
                        imdiTable.clearSelection();
                        int minSelectedRow = -1;
                        int maxSelectedRow = -1;
                        for (Object selectedRow : ((JList) e.getSource()).getSelectedValues()) {
                            imdiTable.setColumnSelectionAllowed(false);
                            imdiTable.setRowSelectionAllowed(true);
                            for (int rowCount = 0; rowCount < imdiTable.getRowCount(); rowCount++) {
                                if (imdiTable.getValueAt(rowCount, 0).equals(selectedRow)) {
                                    imdiTable.addRowSelectionInterval(rowCount, rowCount);
                                    if (maxSelectedRow == -1 || maxSelectedRow < rowCount) {
                                        maxSelectedRow = rowCount;
                                    }
                                    if (minSelectedRow == -1 || minSelectedRow > rowCount) {
                                        minSelectedRow = rowCount;
                                    }
                                }
                            }
//                            System.out.println("selectedRow:" + selectedRow);
                            if (maxSelectedRow != -1) {
                                imdiTable.scrollRectToVisible(imdiTable.getCellRect(minSelectedRow, 0, true));
                            }
                        }
                        if (TreeHelper.trackTableSelection) {
                            Object selectedRow[] = ((JList) e.getSource()).getSelectedValues();
                            TreeHelper.getSingleInstance().jumpToSelectionInTree(true, (ImdiTreeObject) selectedRow[0]);
                        }
                    }
                }
            }
        });
        imdiTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if (imdiTable.hasFocus()) {
                    fileList.clearSelection();
                    int minSelectedRow = -1;
                    int maxSelectedRow = -1;
                    for (Object selectedRow : imdiTable.getSelectedRowsFromTable()) {
//                        System.out.println("selectedRow:" + selectedRow);
                        for (int rowCount = 0; rowCount < fileList.getModel().getSize(); rowCount++) {
//                            System.out.println("JList:" + fileList.getModel().getElementAt(rowCount));
                            if (fileList.getModel().getElementAt(rowCount).equals(selectedRow)) {
                                fileList.addSelectionInterval(rowCount, rowCount);
//                                System.out.println("add selection");
                                if (maxSelectedRow == -1 || maxSelectedRow < rowCount) {
                                    maxSelectedRow = rowCount;
                                }
                                if (minSelectedRow == -1 || minSelectedRow > rowCount) {
                                    minSelectedRow = rowCount;
                                }
                            }
                        }
                    }
                    if (maxSelectedRow != -1) {
                        fileList.scrollRectToVisible(fileList.getCellBounds(minSelectedRow, maxSelectedRow));
                    }
                    if (TreeHelper.trackTableSelection) {
                        TreeHelper.getSingleInstance().jumpToSelectionInTree(true, imdiTable.getImdiNodeForSelection());
                    }
                }
            }
        });
    }

    public void setSplitDisplay() {
        this.removeAll();
        if (fileList.getModel().getSize() == 0) {
            this.add(tableOuterPanel);
        } else {
            splitPane.setTopComponent(tableOuterPanel);
//            splitPane.setTopComponent(tableScrollPane);
            splitPane.setBottomComponent(listScroller);
            GuiHelper.imdiDragDrop.addDrag(fileList);
            GuiHelper.imdiDragDrop.addTransferHandler(tableScrollPane);
            this.add(splitPane);
            this.doLayout();
            splitPane.setDividerLocation(0.5);
        }
        GuiHelper.imdiDragDrop.addDrag(imdiTable);
        GuiHelper.imdiDragDrop.addTransferHandler(this);
        this.doLayout();
    }

    @Override
    public void doLayout() {
//        imdiTable.doLayout();
        super.doLayout();
    }

    public void addFocusListener(JInternalFrame internalFrame) {
        internalFrame.addInternalFrameListener(new InternalFrameAdapter() {

            @Override
            public void internalFrameDeactivated(InternalFrameEvent e) {
                TableCellEditor tableCellEditor = imdiTable.getCellEditor();
                if (tableCellEditor != null) {
                    tableCellEditor.stopCellEditing();
                }
                super.internalFrameDeactivated(e);
            }
        });
    }
}

