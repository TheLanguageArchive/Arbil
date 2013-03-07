/*
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
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
package nl.mpi.arbil.ui.menu;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.table.TableColumn;
import nl.mpi.arbil.ui.ArbilFieldViewTable;
import nl.mpi.arbil.ui.ArbilFieldViews;
import nl.mpi.arbil.ui.ArbilSplitPanel;
import nl.mpi.arbil.ui.ArbilTable;
import nl.mpi.arbil.ui.ArbilTableModel;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.WindowManager;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class TableHeaderContextMenu extends JPopupMenu {

    private final ArbilTable table;
    private final ArbilTableModel tableModel;
    private final MessageDialogHandler dialogHandler;
    private final WindowManager windowManager;

    public TableHeaderContextMenu(final ArbilTable table, final ArbilTableModel tableModel, final int targetColumn, final MessageDialogHandler dialogHandler, final WindowManager windowManager) {
	this.table = table;
	this.tableModel = tableModel;
	this.dialogHandler = dialogHandler;
	this.windowManager = windowManager;
	initMenuItems(targetColumn);
    }

    private void initMenuItems(final int targetColumn) {
	final String targetColumnName = tableModel.getColumnName(targetColumn);

	final JMenuItem saveViewMenuItem = new JMenuItem("Save Current Column View");
	saveViewMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		try {
		    //logger.debug("saveViewNenuItem: " + targetTable.toString());
		    String fieldViewName = (String) JOptionPane.showInputDialog(null, "Enter a name to save this Column View as", "Save Column View", JOptionPane.PLAIN_MESSAGE);
		    // if the user did not cancel
		    if (fieldViewName != null) {
			table.updateStoredColumnWidhts();
			if (!ArbilFieldViews.getSingleInstance().addArbilFieldView(fieldViewName, tableModel.getFieldView())) {
			    dialogHandler.addMessageDialogToQueue("A Column View with the same name already exists, nothing saved", "Save Column View");
			}
		    }
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	final JMenuItem editViewMenuItem = new JMenuItem("Edit this Column View");
	editViewMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		table.updateStoredColumnWidhts();
		try {
		    ArbilFieldViewTable fieldViewTable = new ArbilFieldViewTable(tableModel);
		    JDialog editViewsDialog = new JDialog(JOptionPane.getFrameForComponent(windowManager.getMainFrame()), true);
		    editViewsDialog.setTitle("Editing Current Column View");
		    JScrollPane js = new JScrollPane(fieldViewTable);
		    editViewsDialog.getContentPane().add(js);
		    editViewsDialog.setBounds(50, 50, 600, 400);
		    editViewsDialog.setVisible(true);
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	final JMenuItem showOnlyCurrentViewMenuItem = new JMenuItem("Limit View to Current Columns");
	showOnlyCurrentViewMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		try {
		    //logger.debug("saveViewNenuItem: " + targetTable.toString());
		    tableModel.showOnlyCurrentColumns();
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
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
			tableModel.setCurrentView(ArbilFieldViews.getSingleInstance().getView(((Component) evt.getSource()).getName()));
			table.doResizeColumns();
		    } catch (Exception ex) {
			BugCatcherManager.getBugCatcher().logError(ex);
		    }
		}
	    });
	    fieldViewsMenuItem.add(viewLabelMenuItem);
	}
	final JMenuItem copyEmbedTagMenuItem = new JMenuItem("Copy Table For Website");
	copyEmbedTagMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		// find the table dimensions
		Component sizedComponent = table;
		Component currentComponent = table;
		while (currentComponent.getParent() != null) {
		    currentComponent = currentComponent.getParent();
		    if (currentComponent instanceof ArbilSplitPanel) {
			sizedComponent = currentComponent;
		    }
		}
		tableModel.copyHtmlEmbedTagToClipboard(sizedComponent.getHeight(), sizedComponent.getWidth());
	    }
	});
	final JMenuItem setAllColumnsSizeFromColumn = new JMenuItem("Make all columns the size of \"" + targetColumnName + "\"");
	setAllColumnsSizeFromColumn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		int targetWidth = table.getColumnModel().getColumn(targetColumn).getWidth();
		for (int i = 0; i < table.getColumnCount(); i++) {
		    TableColumn column = table.getColumnModel().getColumn(i);
		    tableModel.getFieldView().setColumnWidth(column.getHeaderValue().toString(), targetWidth);
		}
		table.doResizeColumns();
	    }
	});
	final JMenuItem setAllColumnsSizeAuto = new JMenuItem("Make all columns fit contents");
	setAllColumnsSizeAuto.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		tableModel.getFieldView().resetColumnWidths();
		table.doResizeColumns();
	    }
	});
	final JMenuItem setColumnSizeAuto = new JMenuItem("Make column fit contents");
	setColumnSizeAuto.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		tableModel.getFieldView().setColumnWidth(targetColumnName, null);
		table.doResizeColumns(Arrays.asList(targetColumn));
	    }
	});
	final JCheckBoxMenuItem setFixedColumnSize = new JCheckBoxMenuItem("Fixed column size");
	setFixedColumnSize.setSelected(tableModel.getPreferredColumnWidth(targetColumnName) != null);

	setFixedColumnSize.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		tableModel.getFieldView().setColumnWidth(targetColumnName,
			setFixedColumnSize.isSelected()
			? table.getColumnModel().getColumn(targetColumn).getWidth()
			: null);
		table.doResizeColumns(Collections.singleton(Integer.valueOf(targetColumn)));
	    }
	});

	if (tableModel.isHorizontalView()) {
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

	    add(thisColumnMenu);
	    add(allColumnsMenu);
	    add(createShowChildNodesMenuItem(targetColumn));
	    add(new JSeparator());
	}

	add(fieldViewsMenuItem);
	add(saveViewMenuItem);
	add(editViewMenuItem);
	add(showOnlyCurrentViewMenuItem);

	add(new JSeparator());
	add(copyEmbedTagMenuItem);
    }

    private JMenuItem createHideColumnMenuItem(final int targetColumn) {
	// prevent hide column menu showing when the session column is selected because it cannot be hidden
	JMenuItem hideColumnMenuItem = new JMenuItem("Hide Column");
	hideColumnMenuItem.setActionCommand("" + targetColumn);
	hideColumnMenuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		try {
		    tableModel.hideColumn(targetColumn);
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
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
		    table.showRowChildData();
		} catch (Exception ex) {
		    BugCatcherManager.getBugCatcher().logError(ex);
		}
	    }
	});
	return showChildNodesMenuItem;
    }
}
