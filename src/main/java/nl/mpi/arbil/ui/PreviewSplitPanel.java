package nl.mpi.arbil.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.table.TableCellEditor;

/**
 * PreviewSplitPanel.java
 * Created on Jul 9, 2009, 2:31:20 PM
 * @author Peter.Withers@mpi.nl
 */
public class PreviewSplitPanel extends javax.swing.JSplitPane {

    static public ArbilTable previewTable = null;
    static public boolean previewTableShown = false;
    private JScrollPane rightScrollPane;
    private JLabel previewHiddenColumnLabel;
    private JPanel previewPanel;
    private Container parentComponent;

    public PreviewSplitPanel() {
        this.setDividerSize(5);
        this.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        this.setName("rightSplitPane");

        previewHiddenColumnLabel = new javax.swing.JLabel(" ");
        previewTable = new ArbilTable(new ArbilTableModel(), "Preview");
        previewTable.getArbilTableModel().setHiddenColumnsLabel(previewHiddenColumnLabel);
        rightScrollPane = new JScrollPane(previewTable);
        previewPanel = new JPanel(new java.awt.BorderLayout());
        previewPanel.add(rightScrollPane, BorderLayout.CENTER);
        previewPanel.add(previewHiddenColumnLabel, BorderLayout.SOUTH);
//        this.setLayout(new BorderLayout());
    }

    public void setPreviewPanel(boolean showPreview) {
        Container selectedComponent;
        if (parentComponent == null) {
            parentComponent = this.getParent();
        }
        if (!showPreview) {
            // remove the right split split and show only the jdesktoppane
            parentComponent.remove(this);
            selectedComponent = ArbilWindowManager.getSingleInstance().desktopPane;
            TableCellEditor currentCellEditor = PreviewSplitPanel.previewTable.getCellEditor(); // stop any editing so the changes get stored
            if (currentCellEditor != null) {
                currentCellEditor.stopCellEditing();
            }
            // clear the grid to keep things tidy
            previewTable.getArbilTableModel().removeAllArbilDataNodeRows();
        } else {
            // put the jdesktoppane and the preview grid back into the right split pane
            this.remove(ArbilWindowManager.getSingleInstance().desktopPane);
            this.setDividerLocation(0.25);
            this.setTopComponent(previewPanel);
            this.setBottomComponent(ArbilWindowManager.getSingleInstance().desktopPane);
            // update the preview data grid
            TableCellEditor currentCellEditor = PreviewSplitPanel.previewTable.getCellEditor(); // stop any editing so the changes get stored
            if (currentCellEditor != null) {
                currentCellEditor.stopCellEditing();
            }
            previewTable.getArbilTableModel().removeAllArbilDataNodeRows();
            selectedComponent = this;
//            guiHelper.addToGridData(previewTable.getModel(), getSelectedNodes(new JTree[]{remoteCorpusTree, localCorpusTree, localDirectoryTree}));
        }
        if (parentComponent instanceof JSplitPane) {
            int parentDividerLocation = ((JSplitPane) parentComponent).getDividerLocation();
            ((JSplitPane) parentComponent).setBottomComponent(selectedComponent);
            ((JSplitPane) parentComponent).setDividerLocation(parentDividerLocation);
        } else {
            parentComponent.add(selectedComponent);
        }
        previewTableShown = showPreview;
    }
}
