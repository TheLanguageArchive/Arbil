package nl.mpi.arbil;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
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

    public LinorgSplitPanel(ImdiTable localImdiTable) {
//            setBackground(new Color(0xFF00FF));
        this.setLayout(new BorderLayout());

        imdiTable = localImdiTable;
        splitPane = new JSplitPane();

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
        tableScrollPane = new JScrollPane(imdiTable);
    }

    public void setSplitDisplay() {
        this.removeAll();
        if (fileList.getModel().getSize() == 0) {
            this.add(tableScrollPane);
        } else {
            splitPane.setTopComponent(tableScrollPane);
            splitPane.setTopComponent(tableScrollPane);
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
        imdiTable.doLayout();
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

