package nl.mpi.arbil;

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
 * @author Peter.Withers@mpi.nl
 */
public class ImdiTree extends JTree {

    JListToolTip listToolTip = new JListToolTip();

    public ImdiTree() {
        this.addMouseListener(new java.awt.event.MouseAdapter() {

//                public void mouseClicked(java.awt.event.MouseEvent evt) {
//                    treeMouseClick(evt);
//                }
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                treeMousePressedReleased(evt);
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                treeMousePressedReleased(evt);
            }
        });

        this.addKeyListener(new java.awt.event.KeyAdapter() { // TODO: this is failing to get the menu key event

//                @Override
//                public void keyReleased(KeyEvent evt) {
//                    treeKeyTyped(evt);
//                }
            @Override
            public void keyTyped(java.awt.event.KeyEvent evt) {
                treeKeyTyped(evt);
            }//                @Override
//                public void keyPressed(java.awt.event.KeyEvent evt) {
//                    treeKeyTyped(evt);
//                }
            });
    }

    private void treeMousePressedReleased(java.awt.event.MouseEvent evt) {
// TODO add your handling code here:
        // test if click was over a selected node
        javax.swing.tree.TreePath clickedNodePath = ((javax.swing.JTree) evt.getSource()).getPathForLocation(evt.getX(), evt.getY());

        int clickedNodeInt = ((javax.swing.JTree) evt.getSource()).getClosestRowForLocation(evt.getX(), evt.getY());
        int leadSelectedInt = ((javax.swing.JTree) evt.getSource()).getLeadSelectionRow();

        boolean clickedPathIsSelected = (((javax.swing.JTree) evt.getSource()).isPathSelected(clickedNodePath));
        if (evt.isPopupTrigger() /* evt.getButton() == MouseEvent.BUTTON3 || evt.isMetaDown()*/) {
            // this is simplified and made to match the same type of actions as the imditable 
            if (!evt.isShiftDown() && !evt.isControlDown() && !clickedPathIsSelected) {
                ((javax.swing.JTree) evt.getSource()).clearSelection();
                ((javax.swing.JTree) evt.getSource()).addSelectionPath(clickedNodePath);
            }
        }
//    if (evt.isPopupTrigger() /* evt.getButton() == MouseEvent.BUTTON3 || evt.isMetaDown()) {
//        if (/*(*/evt.isPopupTrigger() /* !evt.isControlDown() /*&& evt.getButton() == 1*/ /*&& !evt.isShiftDown())*/ /* || ((evt.getButton() == MouseEvent.BUTTON3 || evt.isMetaDown()) && !clickedPathIsSelected)*/) {
//            System.out.println("alt not down so clearing selection");
//            ((javax.swing.JTree) evt.getSource()).clearSelection();
////        if (evt.getSource() != remoteCorpusTree) {
////            remoteCorpusTree.clearSelection();
////        }
////        if (evt.getSource() != localCorpusTree) {
////            localCorpusTree.clearSelection();
////        }
////        if (evt.getSource() != localDirectoryTree) {
////            localDirectoryTree.clearSelection();
////        }
//            ((javax.swing.JTree) evt.getSource()).setSelectionPath(((javax.swing.JTree) evt.getSource()).getPathForLocation(evt.getX(), evt.getY()));
//        } else if (clickedPathIsSelected) {
//            System.out.println("alt down over selected node");
//            ((javax.swing.JTree) evt.getSource()).removeSelectionPath(clickedNodePath);
//        } else {
//            System.out.println("alt down over unselected node");
//            ((javax.swing.JTree) evt.getSource()).addSelectionPath(clickedNodePath);
//        }
//        if (evt.isShiftDown()) {
//            System.out.println("shift down");
//            ((javax.swing.JTree) evt.getSource()).addSelectionInterval(leadSelectedInt, clickedNodeInt);
//        }
//    }
        if (evt.isPopupTrigger() /* evt.getButton() == MouseEvent.BUTTON3 || evt.isMetaDown()*/) {
            ContextMenu.getSingleInstance().showTreePopup(evt.getSource(), evt.getX(), evt.getY());
        }
    }

    private void treeKeyTyped(java.awt.event.KeyEvent evt) {
        System.out.println(evt.paramString());
        if (evt.getKeyChar() == java.awt.event.KeyEvent.VK_ENTER) {
            LinorgWindowManager.getSingleInstance().openFloatingTableOnce(((ImdiTree) evt.getSource()).getSelectedNodes(), null);
        }
        if (evt.getKeyChar() == java.awt.event.KeyEvent.VK_DELETE) {
//        GuiHelper.treeHelper.deleteNode(GuiHelper.treeHelper.getSingleSelectedNode((JTree) evt.getSource()));
            TreeHelper.getSingleInstance().deleteNode((JTree) evt.getSource());
        }
        System.out.println("evt.getKeyChar(): " + evt.getKeyChar());
        System.out.println("VK_CONTEXT_MENU: " + java.awt.event.KeyEvent.VK_CONTEXT_MENU);
        if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_CONTEXT_MENU) {
//        DefaultMutableTreeNode leadSelection = (DefaultMutableTreeNode) ((JTree) evt.getSource()).getSelectionPath().getLastPathComponent();
            Rectangle selectionBounds = ((JTree) evt.getSource()).getRowBounds(((JTree) evt.getSource()).getLeadSelectionRow());
            ContextMenu.getSingleInstance().showTreePopup(evt.getSource(), selectionBounds.x, selectionBounds.y);
        }
    }

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

    public ImdiTreeObject[] getSelectedNodes() {
        ImdiTreeObject[] selectedNodes = new ImdiTreeObject[this.getSelectionCount()];
        // iterate over allthe selected nodes in the available trees
//        for (int treeCount = 0; treeCount < treesToSearch.length; treeCount++) {
        for (int selectedCount = 0; selectedCount < this.getSelectionCount(); selectedCount++) {
            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) this.getSelectionPaths()[selectedCount].getLastPathComponent();
            if (parentNode.getUserObject() instanceof ImdiTreeObject) {
                selectedNodes[selectedCount] = (ImdiTreeObject) parentNode.getUserObject();
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

    public boolean scrollToNode(ImdiTreeObject targetImdiNode) {
        boolean returnValue = false;
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
                returnValue = true;
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
        return returnValue;
    }
}
