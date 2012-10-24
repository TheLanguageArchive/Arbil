/**
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package nl.mpi.arbil.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.TransferHandler;
import javax.swing.table.TableCellEditor;

/**
 * PreviewSplitPanel.java
 * Created on Jul 9, 2009, 2:31:20 PM
 * @author Peter.Withers@mpi.nl
 */
public class PreviewSplitPanel extends javax.swing.JSplitPane {

    private static PreviewSplitPanel instance;

    public static synchronized PreviewSplitPanel getInstance() {
	if (instance == null) {
	    instance = new PreviewSplitPanel();
	}
	return instance;
    }
    private ArbilTable previewTable = null;
    private boolean previewTableShown = false;
    private JScrollPane rightScrollPane;
    private JLabel previewHiddenColumnLabel;
    private JPanel previewPanel;
    private Container parentComponent;

    private PreviewSplitPanel() {
	this.setDividerSize(5);
	this.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
	this.setName("rightSplitPane");

	previewHiddenColumnLabel = new javax.swing.JLabel(" ");
	previewTable = new ArbilTable(new ArbilTableModel(), "Preview");
	previewTable.getArbilTableModel().setHiddenColumnsLabel(previewHiddenColumnLabel);
	initCopyPaste();

	rightScrollPane = new JScrollPane(previewTable);
	previewPanel = new JPanel(new java.awt.BorderLayout());
	previewPanel.add(rightScrollPane, BorderLayout.CENTER);
	previewPanel.add(previewHiddenColumnLabel, BorderLayout.SOUTH);
    }

    private void initCopyPaste() {
	previewTable.setDragEnabled(false);
	previewTable.setTransferHandler(new TransferHandler() {

	    @Override
	    public boolean importData(JComponent comp, Transferable t) {
		// Import is always from clipboard (no drag in preview table)
		if (comp instanceof ArbilTable) {
		    ((ArbilTable) comp).pasteIntoSelectedTableRowsFromClipBoard();
		}
		return false;
	    }

	    @Override
	    public void exportToClipboard(JComponent comp, Clipboard clip, int action) throws IllegalStateException {
		if (comp instanceof ArbilTable) {
		    ((ArbilTable) comp).copySelectedTableRowsToClipBoard();
		}
	    }
	});
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
	    TableCellEditor currentCellEditor = previewTable.getCellEditor(); // stop any editing so the changes get stored
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
	    TableCellEditor currentCellEditor = previewTable.getCellEditor(); // stop any editing so the changes get stored
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

    /**
     * @return the previewTableShown
     */
    public static boolean isPreviewTableShown() {
	return getInstance().previewTableShown;
    }

    /**
     * @return the previewTable
     */
    public ArbilTable getPreviewTable() {
	return previewTable;
    }
}
