package nl.mpi.arbil.ui;

import nl.mpi.arbil.data.ArbilDataNode;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
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
import nl.mpi.arbil.ui.menu.ImagePreviewContextMenu;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.TreeHelper;

/**
 * Document   : ArbilSplitPanel
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
public class ArbilSplitPanel extends JPanel implements ArbilWindowComponent {

    private static SessionStorage sessionStorage;

    public static void setSessionStorage(SessionStorage sessionStorageInstance) {
	sessionStorage = sessionStorageInstance;
    }
    private static TreeHelper treeHelper;

    public static void setTreeHelper(TreeHelper treeHelperInstance) {
	treeHelper = treeHelperInstance;
    }
    private JList imagePreview;
    public ArbilTable arbilTable;
    private JScrollPane tableScrollPane;
    private JScrollPane listScroller;
    private JSplitPane splitPane;
    private JLabel hiddenColumnsLabel;
    private FindReplacePanel findReplacePanel = null;
    private boolean showSearchPanel = false;
    private JPanel tableOuterPanel;
    boolean selectionChangeInProcess = false; // this is to stop looping selection changes

    public ArbilSplitPanel(ArbilTable localArbilTable) {
//            setBackground(new Color(0xFF00FF));
	this.setLayout(new BorderLayout());

	arbilTable = localArbilTable;
	splitPane = new JSplitPane();
	hiddenColumnsLabel = new JLabel();
	tableScrollPane = new JScrollPane(arbilTable);
	tableScrollPane.addComponentListener(new ComponentListener() {

	    public void componentResized(ComponentEvent e) {
		arbilTable.setColumnWidths();
	    }

	    public void componentMoved(ComponentEvent e) {
	    }

	    public void componentShown(ComponentEvent e) {
	    }

	    public void componentHidden(ComponentEvent e) {
	    }
	});
	tableOuterPanel = new JPanel(new BorderLayout());
	tableOuterPanel.add(tableScrollPane, BorderLayout.CENTER);
	tableOuterPanel.add(hiddenColumnsLabel, BorderLayout.SOUTH);
	localArbilTable.getArbilTableModel().setHiddenColumnsLabel(hiddenColumnsLabel);
	imagePreview = new JList(localArbilTable.getArbilTableModel().getListModel(this));
	imagePreview.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
	imagePreview.setLayoutOrientation(JList.HORIZONTAL_WRAP);
	imagePreview.setVisibleRowCount(-1);

	listScroller = new JScrollPane(imagePreview);
	listScroller.setPreferredSize(new Dimension(250, 80));

	ImageBoxRenderer renderer = new ImageBoxRenderer();
	imagePreview.setCellRenderer(renderer);
	splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
	splitPane.setDividerSize(5);

	imagePreview.addMouseListener(new java.awt.event.MouseAdapter() {

	    @Override
	    public void mousePressed(MouseEvent evt) {
		if (evt.isPopupTrigger()) {
		    new ImagePreviewContextMenu(imagePreview).show(evt.getX(), evt.getY());
		}
	    }

	    @Override
	    public void mouseReleased(MouseEvent evt) {
		if (evt.isPopupTrigger()) {
		    new ImagePreviewContextMenu(imagePreview).show(evt.getX(), evt.getY());
		}
	    }
	});

	imagePreview.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	imagePreview.addListSelectionListener(new ListSelectionListener() {

	    public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting() && !selectionChangeInProcess) { // while this is not thread safe this should only be called by the swing thread via the gui or as a consequence of the enclosed selection changes
		    selectionChangeInProcess = true;
		    if (e.getSource() instanceof JList) {
//                        System.out.println("JList");
			arbilTable.clearSelection();
			int minSelectedRow = -1;
			int maxSelectedRow = -1;
			for (Object selectedRow : ((JList) e.getSource()).getSelectedValues()) {
			    arbilTable.setColumnSelectionAllowed(false);
			    arbilTable.setRowSelectionAllowed(true);
			    for (int rowCount = 0; rowCount < arbilTable.getRowCount(); rowCount++) {
				if (arbilTable.getTableCellContentAt(rowCount, 0).equals(selectedRow)) {
				    arbilTable.addRowSelectionInterval(rowCount, rowCount);
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
				arbilTable.scrollRectToVisible(arbilTable.getCellRect(minSelectedRow, 0, true));
			    }
			}
			if (sessionStorage.isTrackTableSelection()) {
			    treeHelper.jumpToSelectionInTree(true, (ArbilDataNode) ((JList) e.getSource()).getSelectedValue());
			}
		    }
		    selectionChangeInProcess = false;
		}
	    }
	});
	arbilTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

	    public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting() && !selectionChangeInProcess) { // while this is not thread safe this should only be called by the swing thread via the gui or as a consequence of the enclosed selection changes
		    selectionChangeInProcess = true;
		    imagePreview.clearSelection();
		    int minSelectedRow = -1;
		    int maxSelectedRow = -1;
		    for (Object selectedRow : arbilTable.getSelectedRowsFromTable()) {
//                        System.out.println("selectedRow:" + selectedRow);
			for (int rowCount = 0; rowCount < imagePreview.getModel().getSize(); rowCount++) {
//                            System.out.println("JList:" + fileList.getModel().getElementAt(rowCount));
			    if (imagePreview.getModel().getElementAt(rowCount).equals(selectedRow)) {
				imagePreview.addSelectionInterval(rowCount, rowCount);
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
			imagePreview.scrollRectToVisible(imagePreview.getCellBounds(minSelectedRow, maxSelectedRow));
		    }
		    if (sessionStorage.isTrackTableSelection()) {
			treeHelper.jumpToSelectionInTree(true, arbilTable.getDataNodeForSelection());
		    }
		    selectionChangeInProcess = false;
		}
	    }
	});
    }

    public void showSearchPane() {
	if (findReplacePanel == null) {
	    findReplacePanel = new FindReplacePanel(this);
	}
	if (!showSearchPanel) {
	    tableOuterPanel.remove(hiddenColumnsLabel);
	    tableOuterPanel.add(findReplacePanel, BorderLayout.SOUTH);
	} else {
	    tableOuterPanel.remove(findReplacePanel);
	    tableOuterPanel.add(hiddenColumnsLabel, BorderLayout.SOUTH);
	}
	showSearchPanel = !showSearchPanel;
	this.revalidate();
	this.repaint();
    }

    public void setSplitDisplay() {
	this.removeAll();
	if (imagePreview.getModel().getSize() == 0) {
	    this.add(tableOuterPanel);
	} else {
	    splitPane.setTopComponent(tableOuterPanel);
//            splitPane.setTopComponent(tableScrollPane);
	    splitPane.setBottomComponent(listScroller);
	    ArbilDragDrop.getSingleInstance().addDrag(imagePreview);
	    ArbilDragDrop.getSingleInstance().setTransferHandlerOnComponent(tableScrollPane);
	    this.add(splitPane);
	    this.doLayout();
	    splitPane.setDividerLocation(0.5);
	}
	ArbilDragDrop.getSingleInstance().addDrag(arbilTable);
	ArbilDragDrop.getSingleInstance().setTransferHandlerOnComponent(this);
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
		TableCellEditor tableCellEditor = arbilTable.getCellEditor();
		if (tableCellEditor != null) {
		    tableCellEditor.stopCellEditing();
		}
		super.internalFrameDeactivated(e);
	    }
	});
    }

    public void arbilWindowClosed() {
	arbilTable.getArbilTableModel().removeAllArbilDataNodeRows();
    }
}
