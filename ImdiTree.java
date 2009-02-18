/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import java.awt.event.MouseEvent;
import javax.swing.JToolTip;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

/**
 * Document   : ImdiTree
 * Created on : Feb 16, 2009, 3:58:50 PM
 * @author petwit
 */
public class ImdiTree extends JTree {

    JListToolTip listToolTip = new JListToolTip();

    public JToolTip createToolTip() {
        System.out.println("createToolTip");
//        return super.createToolTip();
        listToolTip.updateList();
        return listToolTip;
    }
//
    public String getToolTipText(MouseEvent event) {
        String tip = null;
        java.awt.Point p = event.getPoint();
        TreePath treePath = ((ImdiTree) event.getComponent()).getPathForLocation(p.x, p.y);
        if (getRowForLocation(event.getX(), event.getY()) == -1) {
            listToolTip.setTartgetObject(null);
        } else {
            TreePath curPath = getPathForLocation(event.getX(), event.getY());
            Object targetObject = ((DefaultMutableTreeNode) curPath.getLastPathComponent()).getUserObject();
            
            if (targetObject instanceof ImdiTreeObject){
                listToolTip.setTartgetObject(targetObject);
                tip = ((ImdiTreeObject)targetObject).getUrlString(); // this is required to be unique to the node so that the tip is updated
            }
            else listToolTip.setTartgetObject(null);
        }
        return tip;
    }
}
