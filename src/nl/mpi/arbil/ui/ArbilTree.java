package nl.mpi.arbil.ui;

import nl.mpi.arbil.ui.menu.TreeContextMenu;
import nl.mpi.arbil.data.ImdiTableModel;
import nl.mpi.arbil.data.TreeHelper;
import java.awt.FontMetrics;
import java.awt.Graphics;
import nl.mpi.arbil.data.ArbilNodeObject;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JToolTip;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

/**
 * Document   : ImdiTree
 * Created on : Feb 16, 2009, 3:58:50 PM
 * @author Peter.Withers@mpi.nl
 */
public class ArbilTree extends JTree {

    JListToolTip listToolTip = new JListToolTip();

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
                    System.out.println("jTree1MouseDragged");
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
//                    TreeHelper.getSingleInstance().addToSortQueue(parentNode);
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
//                    TreeHelper.getSingleInstance().addToSortQueue(parentNode);
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
                    ((ImdiTableModel) PreviewSplitPanel.previewTable.getModel()).removeAllImdiRows();
                    ((ImdiTableModel) PreviewSplitPanel.previewTable.getModel()).addSingleImdiObject(((ArbilTree) evt.getSource()).getLeadSelectionNode());
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
            //new ContextMenu().showTreePopup(evt.getSource(), evt.getX(), evt.getY());
            new TreeContextMenu(this).show(evt.getX(), evt.getY());
        }
    }

    private void treeKeyTyped(java.awt.event.KeyEvent evt) {
        System.out.println(evt.paramString());
        if (evt.getKeyChar() == java.awt.event.KeyEvent.VK_ENTER) {
            ArbilWindowManager.getSingleInstance().openFloatingTableOnce(((ArbilTree) evt.getSource()).getSelectedNodes(), null);
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
            new TreeContextMenu(this).show(selectionBounds.x, selectionBounds.y);
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

            if (targetObject instanceof ArbilNodeObject) {
                listToolTip.setTartgetObject(targetObject);
                tip = ((ArbilNodeObject) targetObject).getUrlString(); // this is required to be unique to the node so that the tip is updated
            } else {
                listToolTip.setTartgetObject(null);
            }
        }
        return tip;
    }

    public ArbilNodeObject[] getSelectedNodes() {
        ArrayList<ArbilNodeObject> selectedNodes = new ArrayList<ArbilNodeObject>();
        for (int selectedCount = 0; selectedCount < this.getSelectionCount(); selectedCount++) {
            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) this.getSelectionPaths()[selectedCount].getLastPathComponent();
            if (parentNode.getUserObject() instanceof ArbilNodeObject) {
                ArbilNodeObject currentTreeObject = (ArbilNodeObject) parentNode.getUserObject();
//                if (currentTreeObject.isEmptyMetaNode()) {
                // exchange the meta nodes for its child nodes
//                    for (ImdiTreeObject subChildNode : currentTreeObject.getChildArray()) {
//                        selectedNodes.add(subChildNode);
//                    }
//                } else {
                selectedNodes.add(currentTreeObject);
//                }
            }
        }
        return selectedNodes.toArray(new ArbilNodeObject[]{});
    }

    public ArbilNodeObject getLeadSelectionNode() {
        DefaultMutableTreeNode selectedTreeNode = null;
        ArbilNodeObject returnObject = null;
        javax.swing.tree.TreePath currentNodePath = this.getSelectionPath();
        if (currentNodePath != null) {
            selectedTreeNode = (DefaultMutableTreeNode) currentNodePath.getLastPathComponent();
        }
        if (selectedTreeNode != null && selectedTreeNode.getUserObject() instanceof ArbilNodeObject) {
            returnObject = (ArbilNodeObject) selectedTreeNode.getUserObject();
        }
        return returnObject;
    }

    public void copyNodeUrlToClipboard(ArbilNodeObject[] selectedNodes) {
        if (selectedNodes != null) {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            String copiedNodeUrls = null;
            for (ArbilNodeObject currentNode : selectedNodes) {
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
                        GuiHelper.linorgBugCatcher.logError(murle);
                    }
                }
            }
            StringSelection stringSelection = new StringSelection(copiedNodeUrls);
            clipboard.setContents(stringSelection, GuiHelper.getClipboardOwner());
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
        if (currentNode.getUserObject() instanceof JLabel) {
            System.out.println("currentNode: " + ((JLabel) currentNode.getUserObject()).getText());
        } else {
            System.out.println("currentNode: " + currentNode);
        }
        boolean isExpanded = true;
        ArbilNodeObject[] childImdiObjectArray = rootNodeChildren;
        if (currentNode instanceof DefaultMutableTreeNode) {
            if (currentNode.getUserObject() instanceof ArbilNodeObject) {
                ArbilNodeObject curentImdiObject = (ArbilNodeObject) currentNode.getUserObject();
                if (curentImdiObject != null) {
                    childImdiObjectArray = curentImdiObject.getChildArray();
                    isExpanded = this.isExpanded(new TreePath((currentNode).getPath()));
                }
            }
        }
        Arrays.sort(childImdiObjectArray);

        if (!isExpanded) {
            currentNode.setAllowsChildren(childImdiObjectArray.length > 0);
            ((DefaultTreeModel) treeModel).nodeChanged(currentNode);
        } else {
            if (childImdiObjectArray.length > 0) {
                // never disable allows children when there are child nodes!
                // but allows children must be set before nodes can be added (what on earth were they thinking!)
                currentNode.setAllowsChildren(true);
                ((DefaultTreeModel) treeModel).nodeChanged(currentNode);
            }
            for (int childIndex = 0; childIndex < childImdiObjectArray.length; childIndex++) {
                // search for an existing node and move it if required
                for (int modelChildIndex = 0; modelChildIndex < ((DefaultTreeModel) treeModel).getChildCount(currentNode); modelChildIndex++) {
                    if (((DefaultMutableTreeNode) ((DefaultTreeModel) treeModel).getChild(currentNode, modelChildIndex)).getUserObject().equals(childImdiObjectArray[childIndex])) {
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
                if (childIndex >= ((DefaultTreeModel) treeModel).getChildCount(currentNode) || !((DefaultMutableTreeNode) ((DefaultTreeModel) treeModel).getChild(currentNode, childIndex)).getUserObject().equals(childImdiObjectArray[childIndex])) {
                    childImdiObjectArray[childIndex].registerContainer(this);
                    DefaultMutableTreeNode addableNode = new DefaultMutableTreeNode(childImdiObjectArray[childIndex]);
                    ((DefaultTreeModel) treeModel).insertNodeInto(addableNode, currentNode, childIndex);
                }
            }
            // remove any extraneous nodes from the end
            for (int childIndex = ((DefaultTreeModel) treeModel).getChildCount(currentNode) - 1; childIndex >= childImdiObjectArray.length; childIndex--) {
                ((DefaultTreeModel) treeModel).removeNodeFromParent(((DefaultMutableTreeNode) ((DefaultTreeModel) treeModel).getChild(currentNode, childIndex)));
            }
            for (int childIndex = 0; childIndex < ((DefaultTreeModel) treeModel).getChildCount(currentNode); childIndex++) {
                //for (Enumeration<DefaultMutableTreeNode> childTreeNodeEnum = currentNode.children(); childTreeNodeEnum.hasMoreElements();) {
                sortDescendentNodes((DefaultMutableTreeNode) ((DefaultTreeModel) treeModel).getChild(currentNode, childIndex));
            }
            /*
            if (childImdiObjectArray.length > 0) {
            // never disable allows children when there are child nodes!
            // but allows children must be set before nodes can be added (what on earth were they thinking!)
            currentNode.setAllowsChildren(true);
            }
            if (!isExpanded) {
            for (int childIndex = childImdiObjectArray.length; childIndex < currentNode.getChildCount(); childIndex++) {
            // remove any excess nodes so that if set allows children is to be cleared then the nodes will be de registered
            DefaultMutableTreeNode removedNode = (DefaultMutableTreeNode) currentNode.getChildAt(childIndex);
            ImdiTreeObject removedTreeObject = (ImdiTreeObject) removedNode.getUserObject();
            removedTreeObject.removeContainer(this);
            }
            // this remove all children should resolve the issue of loading nodes being left after node deletion and a tree resort
            currentNode.removeAllChildren();
            //            ((DefaultTreeModel) treeModel).nodeStructureChanged(currentNode);
            // set allows children must not be called before the nodes are deregistered from their imdi tree nodes
            currentNode.setAllowsChildren(childImdiObjectArray.length > 0);
            } else {
            int originalChildCount = currentNode.getChildCount();
            // add any additional nodes to the end
            for (int childIndex = currentNode.getChildCount(); childIndex < childImdiObjectArray.length; childIndex++) {
            DefaultMutableTreeNode addableNode = new DefaultMutableTreeNode(childImdiObjectArray[childIndex]);
            currentNode.add(addableNode);
            ((DefaultTreeModel) treeModel).nodesWereInserted(currentNode, new int[]{childIndex});
            childImdiObjectArray[childIndex].registerContainer(this);
            }
            // remove any extraneous nodes from the end
            for (int childIndex = currentNode.getChildCount() - 1; childIndex >= childImdiObjectArray.length; childIndex--) {
            DefaultMutableTreeNode removedNode = (DefaultMutableTreeNode) currentNode.getChildAt(childIndex);
            ImdiTreeObject removedTreeObject = (ImdiTreeObject) removedNode.getUserObject();
            currentNode.remove(childIndex);
            ((DefaultTreeModel) treeModel).nodesWereRemoved(currentNode, new int[]{childIndex}, new DefaultMutableTreeNode[]{removedNode});
            removedTreeObject.removeContainer(this);
            }
            // swap the data for each out of order node (because the tree model and the tree are near impossible to keep in sync and maybe this method might work when all other methods have failed)
            for (int childIndex = 0; childIndex < childImdiObjectArray.length; childIndex++) {
            if (!((DefaultMutableTreeNode) currentNode.getChildAt(childIndex)).getUserObject().equals(childImdiObjectArray[childIndex])) {
            //
            //                    boolean isPresortIsExpanded = false;
            //                    for (Enumeration<DefaultMutableTreeNode> childTreeNodeEnum = currentNode.children(); childTreeNodeEnum.hasMoreElements();) {
            //                        DefaultMutableTreeNode preSortTreeNode = childTreeNodeEnum.nextElement();
            //                        if (preSortTreeNode.getUserObject().equals(childImdiObjectArray[childIndex])) {
            //                            isPresortIsExpanded = this.isExpanded(new TreePath((preSortTreeNode).getPath()));
            //                            // maintain the expanded states
            //                            this.setExpandedState(new TreePath((currentNode).getPath()), isPresortIsExpanded);
            //                            this.setExpandedState(new TreePath((preSortTreeNode).getPath()), this.isExpanded(new TreePath((currentNode).getPath())));
            //                        }
            //                    }
            // just clear the expanded state to keep it simple
            //                    this.setExpandedState(new TreePath((currentNode).getPath()), false);
            //this.collapsePath(new TreePath((currentNode).getPath()));
            //                    this.collapseRow(this.getRowForPath(new TreePath((currentNode).getPath())));
            ((DefaultMutableTreeNode) currentNode.getChildAt(childIndex)).setUserObject(childImdiObjectArray[childIndex]);
            childImdiObjectArray[childIndex].registerContainer(this);
            //                    ((DefaultTreeModel) treeModel).nodeStructureChanged(currentNode.getChildAt(childIndex));
            //            ((DefaultMutableTreeNode) currentNode.getChildAt(childIndex)).isPresortIsExpanded
            //                    if (isPresortIsExpanded) {
            //                    this.setExpandedState(new TreePath((currentNode).getPath()), isPresortIsExpanded);
            //                    }
            }
            if (childIndex < originalChildCount) {
            ((DefaultTreeModel) treeModel).nodesChanged(currentNode, new int[]{childIndex});
            }
            }
            for (Enumeration<DefaultMutableTreeNode> childTreeNodeEnum = currentNode.children(); childTreeNodeEnum.hasMoreElements();) {
            sortDescendentNodes(childTreeNodeEnum.nextElement());
            }
             */
            ///////////////////////////////////////////////////////////////////////////////////////
            /*
            //            int originalChildNodeCount = currentNode.getChildCount();
            //            ArrayList<Integer> changedIndexes = new ArrayList<Integer>();
            // loop the existing nodes and add swap or remove as required
            for (int childIndex = 0; childIndex < childImdiObjectArray.length; childIndex++) {
            //                System.out.println("currentNode: " + currentNode.getChildAt(childIndex));
            boolean addNodeRequired = true;
            if (currentNode.getChildCount() > childIndex) {
            Object currentUserObject = ((DefaultMutableTreeNode) currentNode.getChildAt(childIndex)).getUserObject();
            System.out.println("comparing: " + childImdiObjectArray[childIndex] + "\nwith     : " + currentUserObject);
            if (!childImdiObjectArray[childIndex].equals(currentUserObject)) {
            // this is an out of order node so make corrections:
            // loop the remaining nodes in the branch trying to find the required node
            for (int branchChildIndex = childIndex; branchChildIndex < currentNode.getChildCount(); branchChildIndex++) {
            DefaultMutableTreeNode comparisonTreeNode = (DefaultMutableTreeNode) currentNode.getChildAt(branchChildIndex);
            if (childImdiObjectArray[childIndex].equals((comparisonTreeNode).getUserObject())) {
            System.out.println("inserting: " + currentNode.getChildAt(branchChildIndex));
            
            ((DefaultTreeModel) treeModel).insertNodeInto(comparisonTreeNode, currentNode, childIndex);
            //                                currentNode.remove(comparisonTreeNode);
            //                                ((DefaultTreeModel) treeModel).nodesWereRemoved(currentNode, new int[]{branchChildIndex}, new DefaultMutableTreeNode[]{comparisonTreeNode});                
            //                                currentNode.insert(comparisonTreeNode, childIndex);
            // update the tree at the end of this branch
            //                                changedIndexes.add(childIndex);
            //                                changedIndexes.add(branchChildIndex);
            //                                ((DefaultTreeModel) treeModel).nodesChanged(currentNode, new int[]{childIndex});
            //                                ((DefaultTreeModel) treeModel).nodesChanged(currentNode, new int[]{branchChildIndex});
            //                                ((DefaultTreeModel) treeModel).nodesWereInserted(currentNode, new int[]{childIndex});
            //                                ((DefaultTreeModel) treeModel).nodesWereRemoved(currentNode, new int[]{branchChildIndex}, new DefaultMutableTreeNode[]{comparisonTreeNode});
            //                                ((DefaultTreeModel) treeModel).nodeChanged(comparisonTreeNode);
            
            
            //                                DefaultMutableTreeNode addableNode = new DefaultMutableTreeNode(childImdiObjectArray[childIndex]);
            //                                currentNode.insert(addableNode, childIndex);                  
            //                                ((DefaultTreeModel) treeModel).nodesWereInserted(currentNode, new int[]{childIndex});
            
            addNodeRequired = false;
            break;
            }
            }
            } else {
            // here we are updating the node incase the icon or text has changed (while this is excessive we are using a swing tree so lets just make its simple lest it becomes a monster)
            ((DefaultTreeModel) treeModel).nodesChanged(currentNode, new int[]{childIndex});
            addNodeRequired = false;
            }
            //                    changedIndexes.add(childIndex);
            //                    ((DefaultTreeModel) treeModel).nodeChanged((DefaultMutableTreeNode) currentNode.getChildAt(childIndex));
            //                    ((DefaultTreeModel) treeModel).nodesChanged(currentNode, new int[]{childIndex});
            }
            if (addNodeRequired) {
            // if this is still out of order or there are not the right number of nodes then add a new node
            DefaultMutableTreeNode addableNode = new DefaultMutableTreeNode(childImdiObjectArray[childIndex]);
            ((DefaultTreeModel) treeModel).insertNodeInto(addableNode, currentNode, childIndex);
            //                    currentNode.add(addableNode);
            //                    changedIndexes.add(childIndex);
            //                    ((DefaultTreeModel) treeModel).nodesWereInserted(currentNode, new int[]{childIndex});
            childImdiObjectArray[childIndex].registerContainer(this);
            }
            }
            //            ArrayList<Integer> changedIndexesFiltered = new ArrayList<Integer>();
            //            int modelNodeChildCount = ((DefaultTreeModel) treeModel).getChildCount(currentNode);
            //                for (Integer currentInteger : changedIndexes) {
            //                // make sure there are no nulls and no indexes greater than the branch node count
            //                    if (currentInteger != null && currentInteger < modelNodeChildCount) {
            //                        changedIndexesFiltered.add(currentInteger);
            //                    }
            //                }
            //                // update the changed indexes in the tree model
            //                int[] changedIndArray = new int[changedIndexesFiltered.size()];
            //                for (int updateCounter = 0; updateCounter < changedIndexesFiltered.size(); updateCounter++) {
            //                    changedIndArray[updateCounter] = changedIndexesFiltered.get(updateCounter);
            //                }                
            //                ((DefaultTreeModel) treeModel).nodesChanged(currentNode, changedIndArray);
            
            // remove any extraneous nodes from the end
            for (int childIndex = currentNode.getChildCount() - 1; childIndex >= childImdiObjectArray.length; childIndex--) {
            DefaultMutableTreeNode removedNode = (DefaultMutableTreeNode) currentNode.getChildAt(childIndex);
            ImdiTreeObject removedTreeObject = (ImdiTreeObject) removedNode.getUserObject();
            ((DefaultTreeModel) treeModel).removeNodeFromParent(removedNode);
            //                currentNode.remove(childIndex);
            //                ((DefaultTreeModel) treeModel).nodesWereRemoved(currentNode, new int[]{childIndex}, new DefaultMutableTreeNode[]{removedNode});
            removedTreeObject.removeContainer(this);
            }
            
            //            ((DefaultTreeModel) treeModel).nodeStructureChanged(currentNode);
            
            for (Enumeration<DefaultMutableTreeNode> childTreeNodeEnum = currentNode.children(); childTreeNodeEnum.hasMoreElements();) {
            sortDescendentNodes(childTreeNodeEnum.nextElement());
            }
            // todo: keep the selected nodes and the open branches durring the search (a slowly loading tree ends up with the wrong branches open)
            // set allows children must! be done ofter any nodes are removed, otherwise the child count will return zero and the model will not get updated! (yay, swing sucks)
            //            currentNode.setAllowsChildren(allowsChildren);
             */
        }
    }
    static final Object sortLockObject = new Object();
    private boolean sortThreadRunning = false;
    private boolean sortRequested = false;
    public ArbilNodeObject[] rootNodeChildren;

    public void requestResort() {
        synchronized (sortLockObject) {
            sortRequested = true;
            if (!sortThreadRunning) {
                sortThreadRunning = true;
                new Thread() {

                    @Override
                    public void run() {
                        try {
                            while (sortRequested) {
                                sleep(100); // leave a delay so as to not take up too much thread time and allow more nodes to be loaded in the mean time
                                sortRequested = false;
//                                TreePath[] selectedPaths = ImdiTree.this.getSelectionPaths();
//                                TreePath[] expandedPaths = ImdiTree.this.getExpandedDescendants(null);
                                sortDescendentNodes((DefaultMutableTreeNode) ArbilTree.this.getModel().getRoot());
//                                ImdiTree.this.setSelectionPaths(selectedPaths);
                            }
                        } catch (Exception exception) {
                            GuiHelper.linorgBugCatcher.logError(exception);
                        }
                        synchronized (sortLockObject) {
                            // syncronising this is excessive but harmless
                            sortThreadRunning = false;
                        }
                    }
                }.start();
            }
        }
    }
}
