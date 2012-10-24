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

import java.awt.Color;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.border.LineBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilNode;

/**
 * Document   : ArbilTreeRenderer
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
public class ArbilTreeRenderer implements TreeCellRenderer {
    
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
	JLabel returnComponent;
	DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
	if (node.getUserObject() instanceof ArbilNode) {
	    ArbilNode arbilNode = (ArbilNode) node.getUserObject();
	    // create the object with parameters so the jvm has a chance to reused objects in memory
	    returnComponent = new JLabel(arbilNode.toString(), arbilNode.getIcon(), JLabel.LEFT);
	    
	    if (arbilNode instanceof ArbilDataNode) {
		if (((ArbilDataNode) arbilNode).isContainerNode()) {
		    returnComponent.setForeground(Color.DARK_GRAY);
		    ((JLabel) returnComponent).setText("<html><u>" + arbilNode.toString() + "</u></html>");
		}
		if (/*!sel && */((ArbilDataNode) arbilNode).hasSchemaError) {
		    returnComponent.setForeground(Color.RED);
		}
		if (/*!sel && */((ArbilDataNode) arbilNode).getNeedsSaveToDisk(true)) {
		    returnComponent.setForeground(Color.BLUE);
		}
	    }
	} else if (node.getUserObject() instanceof JLabel) {
	    // create the object with parameters so the jvm has a chance to reused objects in memory
	    returnComponent = new JLabel(((JLabel) node.getUserObject()).getText(), ((JLabel) node.getUserObject()).getIcon(), JLabel.LEFT);
	} else {
	    return new JLabel();
	}
	if (selected) {
	    returnComponent.setOpaque(true);
	    returnComponent.setBackground(tree.getBackground().darker());
	}
	return returnComponent;
    }
}
