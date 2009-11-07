package nl.mpi.arbil;

import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.ExpandVetoException;
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

        this.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {

            @Override
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                System.out.println("jTree1MouseDragged");
                JComponent c = (JComponent) evt.getSource();
                TransferHandler th = c.getTransferHandler();
                th.exportAsDrag(c, evt, TransferHandler.COPY);
            }
        });
        this.addTreeExpansionListener(new javax.swing.event.TreeExpansionListener() {

            public void treeExpanded(javax.swing.event.TreeExpansionEvent evt) {
                DefaultMutableTreeNode parentNode = null;
                if (evt.getPath() == null) {
                } else {
                    parentNode = (DefaultMutableTreeNode) (evt.getPath().getLastPathComponent());
                    TreeHelper.getSingleInstance().addToSortQueue(parentNode);
                }
            }
            public void treeCollapsed(javax.swing.event.TreeExpansionEvent evt) {
            }
        });
        this.addTreeWillExpandListener(new javax.swing.event.TreeWillExpandListener() {

            public void treeWillCollapse(javax.swing.event.TreeExpansionEvent evt) throws javax.swing.tree.ExpandVetoException {
                if (evt.getPath().getPathCount() == 1) {
                    System.out.println("root node cannot be collapsed");
                    throw new ExpandVetoException(evt, "root node cannot be collapsed");
                }
            }

            public void treeWillExpand(javax.swing.event.TreeExpansionEvent evt) throws javax.swing.tree.ExpandVetoException {
                DefaultMutableTreeNode parentNode = null;
                if (evt.getPath() == null) {
                    //There is no selection.
                } else {
                    parentNode = (DefaultMutableTreeNode) (evt.getPath().getLastPathComponent());
                    // load imdi data if not already loaded
                    TreeHelper.getSingleInstance().addToSortQueue(parentNode);
                }
            }
        });

        this.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {

            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                if (PreviewSplitPanel.previewTableShown && PreviewSplitPanel.previewTable != null) {
                    ((ImdiTableModel) PreviewSplitPanel.previewTable.getModel()).removeAllImdiRows();
                    ((ImdiTableModel) PreviewSplitPanel.previewTable.getModel()).addSingleImdiObject(((ImdiTree) evt.getSource()).getLeadSelectionNode());
                }
            }
        });

        // enable tool tips.
        ToolTipManager.sharedInstance().registerComponent(this);
        // enable the tree icons
        this.setCellRenderer(new ImdiTreeRenderer());
        // enable drag and drop
        GuiHelper.arbilDragDrop.addDrag(this);
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

    @Override
    public JToolTip createToolTip() {
//        System.out.println("createToolTip");
//        return super.createToolTip();
        listToolTip.updateList();
        return listToolTip;
    }

    @Override
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
        ArrayList<ImdiTreeObject> selectedNodes = new ArrayList<ImdiTreeObject>();
        for (int selectedCount = 0; selectedCount < this.getSelectionCount(); selectedCount++) {
            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) this.getSelectionPaths()[selectedCount].getLastPathComponent();
            if (parentNode.getUserObject() instanceof ImdiTreeObject) {
                ImdiTreeObject currentTreeObject = (ImdiTreeObject) parentNode.getUserObject();
//                if (currentTreeObject.isMetaNode()) {
                // exchange the meta nodes for its child nodes
//                    for (ImdiTreeObject subChildNode : currentTreeObject.getChildArray()) {
//                        selectedNodes.add(subChildNode);
//                    }
//                } else {
                selectedNodes.add(currentTreeObject);
//                }
            }
        }
        return selectedNodes.toArray(new ImdiTreeObject[]{});
    }

    public ImdiTreeObject getLeadSelectionNode() {
        DefaultMutableTreeNode selectedTreeNode = null;
        ImdiTreeObject returnObject = null;
        javax.swing.tree.TreePath currentNodePath = this.getSelectionPath();
        if (currentNodePath != null) {
            selectedTreeNode = (DefaultMutableTreeNode) currentNodePath.getLastPathComponent();
        }
        if (selectedTreeNode != null && selectedTreeNode.getUserObject() instanceof ImdiTreeObject) {
            returnObject = (ImdiTreeObject) selectedTreeNode.getUserObject();
        }
        return returnObject;
    }

    public void copyNodeUrlToClipboard(ImdiTreeObject[] selectedNodes) {
        if (selectedNodes != null) {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            String copiedNodeUrls = null;
            for (ImdiTreeObject currentNode : selectedNodes) {
                if (currentNode != null) {
                    if (copiedNodeUrls == null) {
                        copiedNodeUrls = "";
                    } else {
                        copiedNodeUrls = copiedNodeUrls.concat("\n");
                    }
                    if (currentNode.hasResource()) {
                        copiedNodeUrls = copiedNodeUrls.concat(currentNode.getFullResourcePath());
                    } else {
                        copiedNodeUrls = copiedNodeUrls.concat(currentNode.getUrlString());
                    }
                }
            }
            StringSelection stringSelection = new StringSelection(copiedNodeUrls);
            clipboard.setContents(stringSelection, GuiHelper.clipboardOwner);
            System.out.println("copied: \n" + copiedNodeUrls);
        }
    }
//    public void scrollToNode(String imdiUrlString) {
//        System.out.println("scrollToNode: " + imdiUrlString);
//        // get imdi object 
//        ImdiTreeObject targetImdiNode = GuiHelper.imdiLoader.getImdiObject(null, imdiUrlString);
//        scrollToNode(targetImdiNode);
//    }
}
