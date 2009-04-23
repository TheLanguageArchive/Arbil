/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.util.Vector;
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

    public Vector getSelectedNodes() {
        Vector selectedNodes = new Vector();
        // iterate over allthe selected nodes in the available trees
//        for (int treeCount = 0; treeCount < treesToSearch.length; treeCount++) {
        for (int selectedCount = 0; selectedCount < this.getSelectionCount(); selectedCount++) {
            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) this.getSelectionPaths()[selectedCount].getLastPathComponent();
            if (parentNode.getUserObject() instanceof ImdiTreeObject) {
                selectedNodes.add(parentNode.getUserObject());
            }
        }
//        }
        return selectedNodes;
    }

    public Object getSingleSelectedNode() {
//        System.out.println("getSingleSelectedNode: " + sourceObject);

        DefaultMutableTreeNode selectedTreeNode = null;
        Object returnObject = null;
        javax.swing.tree.TreePath currentNodePath = this.getSelectionPath();
        if (currentNodePath != null) {
            selectedTreeNode = (DefaultMutableTreeNode) currentNodePath.getLastPathComponent();
        }
        if (selectedTreeNode != null) {
            returnObject = selectedTreeNode.getUserObject();
        }
        return returnObject;
    }

    public void copyNodeUrlToClipboard(ImdiTreeObject selectedNode) {
        if (selectedNode != null) {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            StringSelection stringSelection = new StringSelection(selectedNode.getUrlString());
            clipboard.setContents(stringSelection, GuiHelper.clipboardOwner);
            System.out.println("copied: " + selectedNode.getUrlString());
        }
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
        for (Object currentContainer : targetImdiNode.getRegisteredContainers()) {
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
