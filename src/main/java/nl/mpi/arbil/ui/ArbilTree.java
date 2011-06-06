package nl.mpi.arbil.ui;

import nl.mpi.arbil.ui.menu.TreeContextMenu;
import nl.mpi.arbil.data.ArbilTreeHelper;
import java.awt.FontMetrics;
import java.awt.Graphics;
import nl.mpi.arbil.data.ArbilDataNode;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import nl.mpi.arbil.data.ArbilDataNodeContainer;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.util.ArbilActionBuffer;
import nl.mpi.arbil.util.BugCatcher;
import nl.mpi.arbil.util.WindowManager;

/**
 * Interactive representation of a tree of arbil nodes
 *
 * Document   : ArbilTree
 * Created on : Feb 16, 2009, 3:58:50 PM
 * @author Peter.Withers@mpi.nl
 * 
 * @see ArbilDataNode
 * @see ArbilTreeHelper
 * @see ArbilTreeRenderer
 * @see TreeContextMenu
 */
public class ArbilTree extends JTree implements ArbilDataNodeContainer {

    private static BugCatcher bugCatcher;

    public static void setBugCatcher(BugCatcher bugCatcherInstance) {
        bugCatcher = bugCatcherInstance;
    }
    private static WindowManager windowManager;

    public static void setWindowManager(WindowManager windowManagerInstance) {
        windowManager = windowManagerInstance;
    }
    private static ClipboardOwner clipboardOwner;

    public static void setClipboardOwner(ClipboardOwner clipboardOwnerInstance) {
        clipboardOwner = clipboardOwnerInstance;
    }

    public ArbilTree() {
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
                if (evt.getModifiers() == 0 && evt.getButton() == MouseEvent.BUTTON1) {
                    JComponent c = (JComponent) evt.getSource();
                    TransferHandler th = c.getTransferHandler();
                    th.exportAsDrag(c, evt, TransferHandler.COPY);
                }
            }
        });
        this.addTreeExpansionListener(new javax.swing.event.TreeExpansionListener() {

            public void treeExpanded(javax.swing.event.TreeExpansionEvent evt) {
                DefaultMutableTreeNode parentNode = null;
                if (evt.getPath() == null) {
                    //There is no selection.
                } else {
                    parentNode = (DefaultMutableTreeNode) (evt.getPath().getLastPathComponent());
                    // load imdi data if not already loaded
                    ArbilTree.this.requestResort();
//                    ArbilTreeHelper.getSingleInstance().addToSortQueue(parentNode);
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
//                DefaultMutableTreeNode parentNode = null;
//                if (evt.getPath() == null) {
//                    //There is no selection.
//                } else {
//                    parentNode = (DefaultMutableTreeNode) (evt.getPath().getLastPathComponent());
//                    // load imdi data if not already loaded
//                    ArbilTreeHelper.getSingleInstance().addToSortQueue(parentNode);
//                }
            }
        });

        this.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {

            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                if (PreviewSplitPanel.previewTableShown && PreviewSplitPanel.previewTable != null) {
                    TableCellEditor currentCellEditor = PreviewSplitPanel.previewTable.getCellEditor(); // stop any editing so the changes get stored
                    if (currentCellEditor != null) {
                        currentCellEditor.stopCellEditing();
                    }
                    PreviewSplitPanel.previewTable.getArbilTableModel().removeAllArbilDataNodeRows();
                    PreviewSplitPanel.previewTable.getArbilTableModel().addSingleArbilDataNode(((ArbilTree) evt.getSource()).getLeadSelectionDataNode());
                }
            }
        });

        // enable tool tips.
        ToolTipManager.sharedInstance().registerComponent(this);
        // enable the tree icons
        this.setCellRenderer(new ArbilTreeRenderer());
        // enable drag and drop
        ArbilDragDrop.getSingleInstance().addDrag(this);
        ((DefaultTreeModel) treeModel).setAsksAllowsChildren(true);
    }

    private void treeMousePressedReleased(java.awt.event.MouseEvent evt) {
        // test if click was over a selected node
        TreePath clickedNodePath = ((JTree) evt.getSource()).getPathForLocation(evt.getX(), evt.getY());

        boolean clickedPathIsSelected = (((JTree) evt.getSource()).isPathSelected(clickedNodePath));
        if (evt.isPopupTrigger()) {
            // this is simplified and made to match the same type of actions as the imditable 
            if (!evt.isShiftDown() && !evt.isControlDown() && !clickedPathIsSelected) {
                ((javax.swing.JTree) evt.getSource()).clearSelection();
                ((javax.swing.JTree) evt.getSource()).addSelectionPath(clickedNodePath);
            }
            new TreeContextMenu(this).show(evt.getX(), evt.getY());
        }
    }

    private void treeKeyTyped(java.awt.event.KeyEvent evt) {
        if (evt.getKeyChar() == java.awt.event.KeyEvent.VK_ENTER) {
            windowManager.openFloatingTableOnce(((ArbilTree) evt.getSource()).getSelectedNodes(), null);
        }
        if (evt.getKeyChar() == java.awt.event.KeyEvent.VK_DELETE) {
//        GuiHelper.treeHelper.deleteNode(GuiHelper.treeHelper.getSingleSelectedNode((JTree) evt.getSource()));
            ArbilTreeHelper.getSingleInstance().deleteNodes((JTree) evt.getSource());
        }
        if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_CONTEXT_MENU) {
//        DefaultMutableTreeNode leadSelection = (DefaultMutableTreeNode) ((JTree) evt.getSource()).getSelectionPath().getLastPathComponent();
            Rectangle selectionBounds = ((JTree) evt.getSource()).getRowBounds(((JTree) evt.getSource()).getLeadSelectionRow());
            new TreeContextMenu(this).show(selectionBounds.x, selectionBounds.y);
        }
    }

    @Override
    public JToolTip createToolTip() {
        listToolTip.updateList();
        return listToolTip;
    }

    @Override
    public int getRowHeight() {
        Graphics g = this.getGraphics();
        if (g != null) {
            try {
                FontMetrics fontMetrics = g.getFontMetrics();
                int requiredHeight = fontMetrics.getHeight();
                return requiredHeight;
            } catch (Exception exeption) {
            } finally {
                g.dispose();
            }
        }
        return super.getRowHeight();
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        String tip = null;
        java.awt.Point p = event.getPoint();
        TreePath treePath = ((ArbilTree) event.getComponent()).getPathForLocation(p.x, p.y);
        if (getRowForLocation(event.getX(), event.getY()) == -1) {
            listToolTip.setTartgetObject(null);
        } else {
            TreePath curPath = getPathForLocation(event.getX(), event.getY());
            Object targetObject = ((DefaultMutableTreeNode) curPath.getLastPathComponent()).getUserObject();

            if (targetObject instanceof ArbilDataNode) {
                listToolTip.setTartgetObject(targetObject);
                tip = ((ArbilDataNode) targetObject).getUrlString(); // this is required to be unique to the node so that the tip is updated
            } else {
                listToolTip.setTartgetObject(null);
            }
        }
        return tip;
    }

    public ArbilNode[] getAllSelectedNodes() {
        return getSelectedNodesOfType(ArbilNode.class).toArray(new ArbilNode[]{});
    }

    public ArbilDataNode[] getSelectedNodes() {
        return getSelectedNodesOfType(ArbilDataNode.class).toArray(new ArbilDataNode[]{});
    }

    public <T> ArrayList<T> getSelectedNodesOfType(Class<T> type) {
        ArrayList<T> selectedNodes = new ArrayList<T>();
        for (int selectedCount = 0; selectedCount < this.getSelectionCount(); selectedCount++) {
            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) this.getSelectionPaths()[selectedCount].getLastPathComponent();
            if (type.isAssignableFrom(parentNode.getUserObject().getClass())) {
                T currentTreeObject = (T) parentNode.getUserObject();
                selectedNodes.add(currentTreeObject);
            }
        }
        return selectedNodes;
    }


    public ArbilNode getLeadSelectionNode() {
        DefaultMutableTreeNode selectedTreeNode = null;
        ArbilNode returnObject = null;
        javax.swing.tree.TreePath currentNodePath = this.getSelectionPath();
        if (currentNodePath != null) {
            selectedTreeNode = (DefaultMutableTreeNode) currentNodePath.getLastPathComponent();
        }
        if (selectedTreeNode != null && selectedTreeNode.getUserObject() instanceof ArbilNode) {
            returnObject = (ArbilNode) selectedTreeNode.getUserObject();
        }
        return returnObject;
    }

    public ArbilDataNode getLeadSelectionDataNode() {
        DefaultMutableTreeNode selectedTreeNode = null;
        ArbilNode returnObject = getLeadSelectionNode();
        if(returnObject != null && returnObject instanceof ArbilDataNode){
            return (ArbilDataNode) returnObject;
        }
        return null;
    }

    public void copyNodeUrlToClipboard(ArbilDataNode[] selectedNodes) {
        if (selectedNodes != null) {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            String copiedNodeUrls = null;
            for (ArbilDataNode currentNode : selectedNodes) {
                if (currentNode != null) {
                    if (copiedNodeUrls == null) {
                        copiedNodeUrls = "";
                    } else {
                        copiedNodeUrls = copiedNodeUrls.concat("\n");
                    }
                    try {
                        if (currentNode.hasResource()) {
                            copiedNodeUrls = copiedNodeUrls.concat(URLDecoder.decode(currentNode.getFullResourceURI().toString(), "UTF-8"));
                        } else {
                            copiedNodeUrls = copiedNodeUrls.concat(URLDecoder.decode(currentNode.getURI().toString(), "UTF-8"));
                        }
                    } catch (UnsupportedEncodingException murle) {
                        bugCatcher.logError(murle);
                    }
                }
            }
            StringSelection stringSelection = new StringSelection(copiedNodeUrls);
            clipboard.setContents(stringSelection, clipboardOwner);
            System.out.println("copied: \n" + copiedNodeUrls);
        }
    }
//    public void scrollToNode(String imdiUrlString) {
//        System.out.println("scrollToNode: " + imdiUrlString);
//        // get imdi object 
//        ImdiTreeObject targetImdiNode = GuiHelper.imdiLoader.getImdiObject(null, imdiUrlString);
//        scrollToNode(targetImdiNode);
//    }

    private void sortDescendentNodes(DefaultMutableTreeNode currentNode/*, TreePath[] selectedPaths*/) {
        // todo: consider returning a list of tree paths for the nodes that are opened and have no open children
//        if (currentNode.getUserObject() instanceof JLabel) {
//            System.out.println("currentNode: " + ((JLabel) currentNode.getUserObject()).getText());
//        } else {
//            System.out.println("currentNode: " + currentNode);
//        }
        boolean isExpanded = true;
        ArbilDataNode[] childDataNodeArray = rootNodeChildren;
        if (currentNode instanceof DefaultMutableTreeNode) {
            if (currentNode.getUserObject() instanceof ArbilDataNode) {
                ArbilDataNode curentDataNode = (ArbilDataNode) currentNode.getUserObject();
                if (curentDataNode != null) {
                    childDataNodeArray = curentDataNode.getChildArray();
                    isExpanded = this.isExpanded(new TreePath((currentNode).getPath()));
                }
            }
        }
        Arrays.sort(childDataNodeArray);

        if (!isExpanded) {
            currentNode.setAllowsChildren(childDataNodeArray.length > 0);
            ((DefaultTreeModel) treeModel).nodeChanged(currentNode);
        } else {
            if (childDataNodeArray.length > 0) {
                // never disable allows children when there are child nodes!
                // but allows children must be set before nodes can be added (what on earth were they thinking!)
                currentNode.setAllowsChildren(true);
                ((DefaultTreeModel) treeModel).nodeChanged(currentNode);
            }
            for (int childIndex = 0; childIndex < childDataNodeArray.length; childIndex++) {
                // search for an existing node and move it if required
                for (int modelChildIndex = 0; modelChildIndex < ((DefaultTreeModel) treeModel).getChildCount(currentNode); modelChildIndex++) {
                    if (((DefaultMutableTreeNode) ((DefaultTreeModel) treeModel).getChild(currentNode, modelChildIndex)).getUserObject().equals(childDataNodeArray[childIndex])) {
                        if (childIndex != modelChildIndex) {
                            DefaultMutableTreeNode shiftedNode = (DefaultMutableTreeNode) ((DefaultTreeModel) treeModel).getChild(currentNode, modelChildIndex);
                            ((DefaultTreeModel) treeModel).removeNodeFromParent(shiftedNode);
                            ((DefaultTreeModel) treeModel).insertNodeInto(shiftedNode, currentNode, childIndex);
                        } else {
                            ((DefaultTreeModel) treeModel).nodeChanged((DefaultMutableTreeNode) ((DefaultTreeModel) treeModel).getChild(currentNode, modelChildIndex));
                        }
                        break;
                    }
                }
                // check if using an existing node failed and if so then add a new node
                if (childIndex >= ((DefaultTreeModel) treeModel).getChildCount(currentNode) || !((DefaultMutableTreeNode) ((DefaultTreeModel) treeModel).getChild(currentNode, childIndex)).getUserObject().equals(childDataNodeArray[childIndex])) {
                    childDataNodeArray[childIndex].registerContainer(this);
                    DefaultMutableTreeNode addableNode = new DefaultMutableTreeNode(childDataNodeArray[childIndex]);
                    ((DefaultTreeModel) treeModel).insertNodeInto(addableNode, currentNode, childIndex);
                }
            }
            // remove any extraneous nodes from the end
            for (int childIndex = ((DefaultTreeModel) treeModel).getChildCount(currentNode) - 1; childIndex >= childDataNodeArray.length; childIndex--) {
                ((DefaultTreeModel) treeModel).removeNodeFromParent(((DefaultMutableTreeNode) ((DefaultTreeModel) treeModel).getChild(currentNode, childIndex)));
            }
            for (int childIndex = 0; childIndex < ((DefaultTreeModel) treeModel).getChildCount(currentNode); childIndex++) {
                //for (Enumeration<DefaultMutableTreeNode> childTreeNodeEnum = currentNode.children(); childTreeNodeEnum.hasMoreElements();) {
                sortDescendentNodes((DefaultMutableTreeNode) ((DefaultTreeModel) treeModel).getChild(currentNode, childIndex));
            }
        }
    }
    public ArbilDataNode[] rootNodeChildren;

    public void requestResort() {
        sortRunner.requestActionAndNotify();
    }

    /**
     * Data node is to be removed from this tree
     * @param dataNode Data node that should be removed
     */
    public void dataNodeRemoved(ArbilDataNode dataNode) {
        requestResort();
    }

    /**
     * Data node is clearing its icon
     * @param dataNode Data node that is clearing its icon
     */
    public void dataNodeIconCleared(ArbilDataNode dataNode) {
        requestResort();
    }
    ArbilActionBuffer sortRunner = new ArbilActionBuffer("ArbilTree sort thread", 50, 150) {

        @Override
        protected void executeAction() {
            sortDescendentNodes((DefaultMutableTreeNode) ArbilTree.this.getModel().getRoot());
        }
    };
    private JListToolTip listToolTip = new JListToolTip();
}
