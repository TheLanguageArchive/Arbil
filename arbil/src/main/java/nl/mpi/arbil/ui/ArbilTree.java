package nl.mpi.arbil.ui;

import javax.swing.tree.TreeModel;
import nl.mpi.arbil.ui.menu.TreeContextMenu;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeContainer;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.data.ArbilTreeHelper;
import nl.mpi.arbil.util.ArbilActionBuffer;
import nl.mpi.arbil.util.BugCatcher;
import nl.mpi.arbil.util.TreeHelper;
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
    protected ArbilTable customPreviewTable = null;
    private boolean clearSelectionOnFocusLost = false;
    private HashMap<ArbilNode, TreeNode> treeNodeMap = new HashMap<ArbilNode, TreeNode>();

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
    private static TreeHelper treeHelper;

    public static void setTreeHelper(TreeHelper treeHelperInstance) {
	treeHelper = treeHelperInstance;
    }

    public void setCustomPreviewTable(ArbilTable customPreviewTable) {
	this.customPreviewTable = customPreviewTable;
    }

    public void setClearSelectionOnFocusLost(boolean clearSelectionOnFocusLost) {
	this.clearSelectionOnFocusLost = clearSelectionOnFocusLost;
    }

    @Override
    public void setModel(TreeModel newModel) {
	// If model already set with ArbilNode root node (unlikely), remove this as container and remove from map
	if (getModel() != null) {
	    if (getModel().getRoot() instanceof DefaultMutableTreeNode) {
		Object rootUserObject = ((DefaultMutableTreeNode) getModel().getRoot()).getUserObject();
		if (rootUserObject instanceof ArbilNode) {
		    treeNodeMap.remove((ArbilNode) rootUserObject);
		    ((ArbilNode) rootUserObject).removeContainer(this);
		}
	    }
	}

	// Do default model setting
	super.setModel(newModel);

	// If model has ArbilRootNode root object, add it to map and register as container
	if (newModel.getRoot() instanceof DefaultMutableTreeNode) {
	    Object rootUserObject = ((DefaultMutableTreeNode) newModel.getRoot()).getUserObject();
	    if (rootUserObject instanceof ArbilNode) {
		treeNodeMap.put((ArbilNode) rootUserObject, (TreeNode) newModel.getRoot());
		((ArbilNode) rootUserObject).registerContainer(this);
	    }
	}
    }

    public ArbilTree() {
	this.addMouseListener(new java.awt.event.MouseAdapter() {

//                public void mouseClicked(java.awt.event.MouseEvent evt) {
//                    treeMouseClick(evt);
//                }
	    @Override
	    public void mousePressed(java.awt.event.MouseEvent evt) {
		treeMousePressedReleased(evt);

		putSelectionIntoPreviewTable();
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
		if (evt.getPath() == null) {
		    //There is no selection.
		} else {
		    // load imdi data if not already loaded
		    ArbilTree.this.requestResort();
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
		putSelectionIntoPreviewTable();
	    }
	});

	this.addFocusListener(new FocusListener() {

	    public void focusGained(FocusEvent e) {
	    }

	    public void focusLost(FocusEvent e) {
		if (clearSelectionOnFocusLost) {
		    ArbilTree.this.clearSelection();
		}
	    }
	});

	// enable tool tips.
	ToolTipManager.sharedInstance().registerComponent(this);
	// enable the tree icons
	this.setCellRenderer(new ArbilTreeRenderer());
	((DefaultTreeModel) treeModel).setAsksAllowsChildren(true);
    }

    protected void putSelectionIntoPreviewTable() {
	ArbilTable targetPreviewTable = customPreviewTable;
	if (targetPreviewTable == null && PreviewSplitPanel.isPreviewTableShown() && PreviewSplitPanel.getInstance().getPreviewTable() != null) {
	    // if a custom preview table has not been set then check for the application wide preview table and use that if it is enabled
	    targetPreviewTable = PreviewSplitPanel.getInstance().getPreviewTable();
	}
	if (targetPreviewTable != null) {
	    TableCellEditor currentCellEditor = targetPreviewTable.getCellEditor(); // stop any editing so the changes get stored
	    if (currentCellEditor != null) {
		currentCellEditor.stopCellEditing();
	    }
	    final ArbilDataNode leadSelectionDataNode = ArbilTree.this.getLeadSelectionDataNode();
	    final ArbilTableModel arbilTableModel = targetPreviewTable.getArbilTableModel();
	    if (!(arbilTableModel.getArbilDataNodeCount() == 1 && arbilTableModel.containsArbilDataNode(leadSelectionDataNode))) {
		arbilTableModel.removeAllArbilDataNodeRows();
		arbilTableModel.addSingleArbilDataNode(leadSelectionDataNode);
	    }
	}
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
	    treeHelper.deleteNodes((JTree) evt.getSource());
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
	ArbilNode returnObject = getLeadSelectionNode();
	if (returnObject != null && returnObject instanceof ArbilDataNode) {
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

    private void sortDescendentNodes(DefaultMutableTreeNode currentNode/*, TreePath[] selectedPaths*/) {
	if (rootNodeChildren == null) {
	    return;
	}
	// todo: consider returning a list of tree paths for the nodes that are opened and have no open children
//        if (currentNode.getUserObject() instanceof JLabel) {
//            System.out.println("currentNode: " + ((JLabel) currentNode.getUserObject()).getText());
//        } else {
//            System.out.println("currentNode: " + currentNode);
//        }
	boolean isExpanded = true;
	ArbilNode[] childDataNodeArray = rootNodeChildren;
	if (currentNode.getUserObject() instanceof ArbilNode) {
	    ArbilNode curentDataNode = (ArbilNode) currentNode.getUserObject();
	    if (curentDataNode != null) {
		childDataNodeArray = curentDataNode.getChildArray();
		isExpanded = this.isExpanded(new TreePath((currentNode).getPath()));
	    }
	}
	Arrays.sort(childDataNodeArray);
	DefaultTreeModel thisTreeModel = (DefaultTreeModel) treeModel;

	if (!isExpanded) {
	    currentNode.setAllowsChildren(childDataNodeArray.length > 0);
	    thisTreeModel.nodeChanged(currentNode);
	} else {
	    if (childDataNodeArray.length > 0) {
		// never disable allows children when there are child nodes!
		// but allows children must be set before nodes can be added (what on earth were they thinking!)
		currentNode.setAllowsChildren(true);
		thisTreeModel.nodeChanged(currentNode);
	    }
	    for (int childIndex = 0; childIndex < childDataNodeArray.length; childIndex++) {
		// search for an existing node and move it if required
		for (int modelChildIndex = 0; modelChildIndex < thisTreeModel.getChildCount(currentNode); modelChildIndex++) {
		    if (((DefaultMutableTreeNode) thisTreeModel.getChild(currentNode, modelChildIndex)).getUserObject().equals(childDataNodeArray[childIndex])) {
			if (childIndex != modelChildIndex) {
			    DefaultMutableTreeNode shiftedNode = (DefaultMutableTreeNode) thisTreeModel.getChild(currentNode, modelChildIndex);
			    thisTreeModel.removeNodeFromParent(shiftedNode);
			    thisTreeModel.insertNodeInto(shiftedNode, currentNode, childIndex);
			} else {
			    thisTreeModel.nodeChanged((DefaultMutableTreeNode) thisTreeModel.getChild(currentNode, modelChildIndex));
			}
			break;
		    }
		}
		// check if using an existing node failed and if so then add a new node
		if (childIndex >= thisTreeModel.getChildCount(currentNode) || !((DefaultMutableTreeNode) thisTreeModel.getChild(currentNode, childIndex)).getUserObject().equals(childDataNodeArray[childIndex])) {
		    childDataNodeArray[childIndex].registerContainer(this);
		    DefaultMutableTreeNode addableNode = new DefaultMutableTreeNode(childDataNodeArray[childIndex]);
		    thisTreeModel.insertNodeInto(addableNode, currentNode, childIndex);
		    treeNodeMap.put(childDataNodeArray[childIndex], addableNode);
		}
	    }
	    // remove any extraneous nodes from the end
	    for (int childIndex = thisTreeModel.getChildCount(currentNode) - 1; childIndex >= childDataNodeArray.length; childIndex--) {
		final DefaultMutableTreeNode toRemove = (DefaultMutableTreeNode) thisTreeModel.getChild(currentNode, childIndex);
		thisTreeModel.removeNodeFromParent(toRemove);
		if (toRemove.getUserObject() instanceof ArbilNode) {
		    treeNodeMap.remove((ArbilNode) toRemove.getUserObject());
		}
	    }
	    for (int childIndex = 0; childIndex < thisTreeModel.getChildCount(currentNode); childIndex++) {
		//for (Enumeration<DefaultMutableTreeNode> childTreeNodeEnum = currentNode.children(); childTreeNodeEnum.hasMoreElements();) {
		sortDescendentNodes((DefaultMutableTreeNode) thisTreeModel.getChild(currentNode, childIndex));
	    }
	}
    }
    public ArbilNode[] rootNodeChildren;

    public void requestResort() {
	sortRunner.requestActionAndNotify();
    }

    /**
     * Data node is to be removed from this tree
     * @param dataNode Data node that should be removed
     */
    public void dataNodeRemoved(ArbilNode dataNode) {
	requestResort();
    }

    /**
     * Data node is clearing its icon
     * @param dataNode Data node that is clearing its icon
     */
    public void dataNodeIconCleared(ArbilNode dataNode) {
	requestResort();
    }
    private final Object sortRunnerLock = new Object();
    // NOTE: This nameless ArbilActionBuffer class/object is not serializable, while ArbilTree is
    private final ArbilActionBuffer sortRunner = new ArbilActionBuffer("ArbilTree sort thread", 100, 150, sortRunnerLock) {

	@Override
	protected void executeAction() {
	    sortDescendentNodes((DefaultMutableTreeNode) ArbilTree.this.getModel().getRoot());
	}
    }; // ArbilActionBuffer
    private JListToolTip listToolTip = new JListToolTip();

    /**
     * A new child node has been added to the destination node. Tries to expand tree to show the new node and select it.
     * @param destination Node to which a node has been added
     * @param newNode The newly added node
     */
    public void dataNodeChildAdded(final ArbilNode destination, final ArbilNode newNode) {
	// Find treeNode for destination ArbilNode
	final TreeNode destinationNode = treeNodeMap.get(destination);
	if (destinationNode != null) {
	    // Find path from destination node to the newly added node
	    List<ArbilNode> nodePath = createArbilNodePath(destination, newNode);
	    if (nodePath != null) {
		// Create a tree path from this node path
		TreePath newNodePath = findTreePathForNodePath(destinationNode, nodePath);
		// Select the new node
		setSelectionPath(newNodePath);
		scrollPathToVisible(newNodePath);
	    }
	}
    }

    /**
     * Takes arbil node path and maps to TreePath starting mapping from specified root tree node
     * @param rootTreeNode TreeNode to start with
     * @param arbilNodePath Path of arbil nodes that correspond to children of rootTreeNode
     * @return TreePath (from tree root) for the arbilNodePath
     */
    private TreePath findTreePathForNodePath(final TreeNode rootTreeNode, List<ArbilNode> arbilNodePath) {
	// Create root tree path
	TreePath treePath = createTreePathForTreeNode(rootTreeNode);
	// Start mapping node path to tree path
	for (ArbilNode currentTargetNode : arbilNodePath) {
	    // Expand current node so children will become available...
	    expandPath(treePath);
	    // ...and give the sort runner some time to react to the expansion before proceeding...
	    synchronized (sortRunnerLock) {
		try {
		    sortRunnerLock.wait(250);
		} catch (InterruptedException ex) {
		}
	    }
	    treePath = extendTreePathWithChildNode(treePath, currentTargetNode);
	}
	return treePath;
    }

    /**
     * Extends a tree path by adding the TreeNode that has a specified ArbilNode as user object
     * @param treePath TreePath to extend
     * @param currentTargetNode
     * @return Extend tree path (if target not found, returns original)
     */
    private TreePath extendTreePathWithChildNode(TreePath treePath, ArbilNode currentTargetNode) {
	// Get current tree node
	final Object lastPathComponent = treePath.getLastPathComponent();
	if (lastPathComponent instanceof TreeNode) {
	    final TreeNode currentTreeNode = (TreeNode) lastPathComponent;
	    // Traverse current node children to find match for target node
	    for (int i = 0; i < currentTreeNode.getChildCount(); i++) {
		TreeNode currentTreeNodeChild = currentTreeNode.getChildAt(i);
		if (currentTreeNodeChild instanceof DefaultMutableTreeNode) {
		    // Check if tree node has current target arbil node as user object
		    if (((DefaultMutableTreeNode) currentTreeNodeChild).getUserObject().equals(currentTargetNode)) {
			// Extend tree path and continue to next child
			treePath = treePath.pathByAddingChild(currentTreeNodeChild);
			break;
		    }
		}
	    }
	}
	return treePath;
    }

    /**
     * 
     * @param treeNode
     * @return Complete treepath for the specified treeNode
     */
    private static TreePath createTreePathForTreeNode(TreeNode treeNode) {
	ArrayList pathList = new ArrayList();
	TreeNode node = treeNode;
	// Construct tree path
	while (node != null) {
	    pathList.add(node);
	    node = node.getParent();
	}
	Collections.reverse(pathList);
	return new TreePath(pathList.toArray());
    }

    /**
     * Creates list that represents path from a root node to a target node (not including root node)
     * @param rootNode Departure node
     * @param targetNode Target node
     * @return List starting at first child of rootnode on path and ending with targetnode, or null if not found
     */
    private static List<ArbilNode> createArbilNodePath(final ArbilNode rootNode, final ArbilNode targetNode) {
	for (ArbilNode child : rootNode.getChildArray()) {
	    if (child.equals(targetNode)) {
		// This child is the target node, path consists of single node
		return Collections.singletonList(targetNode);
	    } else {
		final List<ArbilNode> childList = createArbilNodePath(child, targetNode);
		if (childList != null) {
		    // Target found in child path, append to child and return
		    final LinkedList<ArbilNode> list = new LinkedList<ArbilNode>();
		    list.add(child);
		    list.addAll(childList);
		    return list;
		}
	    }
	}
	return null;
    }
}
