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
package nl.mpi.arbil.ui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import nl.mpi.arbil.util.TreeHelper;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilTableController {

    public static final String DELETE_ROW_ACTION_KEY = "deleteRow";
    private final TreeHelper treeHelper;

    public ArbilTableController(TreeHelper treeHelper) {
	this.treeHelper = treeHelper;
    }

    public void initKeyMapping(ArbilTable table) {
	table.getInputMap(JTable.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), DELETE_ROW_ACTION_KEY);
	table.getActionMap().put(DELETE_ROW_ACTION_KEY, deleteRowAction);
    }
    
    private final Action deleteRowAction = new AbstractAction() {
	public void actionPerformed(ActionEvent e) {
	    treeHelper.deleteNodes(e.getSource());
	}
    };
}
