/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import java.awt.event.MouseEvent;
import java.util.Enumeration;
import javax.swing.JToolTip;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
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

            if (targetObject instanceof ImdiTreeObject) {
                listToolTip.setTartgetObject(targetObject);
                tip = ((ImdiTreeObject) targetObject).getUrlString(); // this is required to be unique to the node so that the tip is updated
            } else {
                listToolTip.setTartgetObject(null);
            }
        }
        return tip;
    }

//    public void scrollToNode(String imdiUrlString) {
//        System.out.println("scrollToNode: " + imdiUrlString);
//        // get imdi object 
//        ImdiTreeObject targetImdiNode = GuiHelper.imdiLoader.getImdiObject(null, imdiUrlString);
//        scrollToNode(targetImdiNode);
//    }

    public void scrollToNode(ImdiTreeObject targetImdiNode) {
        System.out.println("scrollToNode: " + targetImdiNode);
//        DefaultTreeModel treeModel = 
        // get imdi object 
//        ImdiTreeObject targetImdiNode = GuiHelper.imdiLoader.getImdiObject(null, imdiUrlString);
//        if (targetImdiNode.isImdiChild()) {
//            // get the dom parent
//            ImdiTreeObject parentImdiNode = targetImdiNode.getParentDomNode();
//            System.out.println("parentImdiNode: " + parentImdiNode);
//            // get parent tree node 
//            for (Enumeration registeredContainers = parentImdiNode.getRegisteredContainers(); registeredContainers.hasMoreElements();) {
//                Object currentContainer = registeredContainers.nextElement();
//                System.out.println("parentImdiNode registeredContainers: " + currentContainer);
//                if (currentContainer instanceof DefaultMutableTreeNode) {
//                    // refresh the tree for the node
//                    // refresh the parent tree (including the target node)
//                    GuiHelper.treeHelper.loadAndRefreshDescendantNodes((DefaultMutableTreeNode) currentContainer);
//                }
//            }
//        }
        // get tree node 
        for (Enumeration registeredContainers = targetImdiNode.getRegisteredContainers(); registeredContainers.hasMoreElements();) {
            Object currentContainer = registeredContainers.nextElement();
            System.out.println("targetImdiNode registeredContainers: " + currentContainer);
            if (currentContainer instanceof DefaultMutableTreeNode) {
                final TreePath targetTreePath = new TreePath(((DefaultMutableTreeNode) currentContainer).getPath());
//                System.out.println("trying to scroll to" + targetTreePath);
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        System.out.println("scrollToNode targetTreePath: " + targetTreePath);
                        scrollPathToVisible(targetTreePath);
                        setSelectionPath(targetTreePath);
                    }
                });
            }
        }
    }
}
