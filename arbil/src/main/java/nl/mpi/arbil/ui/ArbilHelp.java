package nl.mpi.arbil.ui;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import nl.mpi.arbil.help.HelpIndex;
import nl.mpi.arbil.help.HelpItem;
import nl.mpi.arbil.help.HelpItemsParser;
import nl.mpi.arbil.util.BugCatcherManager;
import org.xml.sax.SAXException;

/**
 * Document : ArbilHelp.java
 * Created on : March 9, 2009, 1:38 PM
 *
 * @author Peter Withers <Peter.Withers@mpi.nl>
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilHelp extends javax.swing.JPanel {

    final static public String SHOTCUT_KEYS_PAGE = "Shortcut Keys";
    final static public String INTRODUCTION_PAGE = "Overview";
    final static public String helpWindowTitle = "Help Viewer";
    final static public String DEFAULT_HELPSET = "Help";
    final static public String IMDI_HELPSET = "IMDI";
    final static public String CMDI_HELPSET = "CMDI";
    private JTabbedPane tabbedPane;
    private JScrollPane helpTextScrollPane;
    private JSplitPane jSplitPane1;
    private JTextPane helpTextPane;
    private HelpTree currentTree;
    private final Map<String, HelpTree> helpTreesMap;
    private final List<HelpTree> helpTrees;
    static private ArbilHelp singleInstance = null;

    public static synchronized ArbilHelp getArbilHelpInstance() throws IOException, SAXException {
	//TODO: This should not be a singleton...
	if (singleInstance == null) {
	    final String imdiHelpResourceBase = "/nl/mpi/arbil/resources/html/help/arbil-imdi/";
	    final HelpResourceSet imdiHelpSet = new HelpResourceSet(IMDI_HELPSET, ArbilHelp.class, imdiHelpResourceBase, imdiHelpResourceBase + "arbil-imdi.xml");
	    final String cmdiHelpResourceBase = "/nl/mpi/arbil/resources/html/help/arbil-cmdi/";
	    final HelpResourceSet cmdiHelpSet = new HelpResourceSet(CMDI_HELPSET, ArbilHelp.class, cmdiHelpResourceBase, cmdiHelpResourceBase + "arbil-cmdi.xml");

	    singleInstance = new ArbilHelp(Arrays.asList(imdiHelpSet, cmdiHelpSet));
	}
	return singleInstance;
    }

    /**
     *
     * @param resourcesClass Class for accessing resources (through {@link Class#getResourceAsStream(java.lang.String) })
     * @param helpResourceBase specification of base package (accessible from resourcesClass) for help resources
     * @param indexXml xml that specifies the contents of the help resources
     * @throws IOException
     * @throws SAXException
     */
    public ArbilHelp(final Class resourcesClass, final String helpResourceBase, final String indexXml) throws IOException, SAXException {
	this(Collections.singletonList(new HelpResourceSet(DEFAULT_HELPSET, resourcesClass, helpResourceBase, indexXml)));
    }

    /**
     *
     * @param helpSets Help resource sets to include (each will create a tab with a tree). Should contain at least one item.
     * @throws IOException
     * @throws SAXException
     */
    public ArbilHelp(final List<HelpResourceSet> helpSets) throws IOException, SAXException {
	if (helpSets.size() < 1) {
	    throw new IllegalArgumentException("Should provide at least one help resource set");
	}

	initComponents();

	final HelpItemsParser parser = new HelpItemsParser();
	this.helpTrees = new ArrayList<HelpTree>(helpSets.size());
	this.helpTreesMap = new HashMap<String, HelpTree>(helpSets.size());
	for (HelpResourceSet helpSet : helpSets) {
	    HelpTree helpTree = createHelpTree(helpSet, parser);
	    helpTrees.add(helpTree);
	    helpTreesMap.put(helpSet.getName(), helpTree);
	}

	currentTree = helpTrees.get(0);
	initHelpTreeTabs();
    }

    private HelpTree createHelpTree(HelpResourceSet helpSet, final HelpItemsParser parser) throws SAXException, IOException {
	HelpTree helpTree = new HelpTree(helpSet);
	initHelpTreeComponents(helpTree);
	final InputStream helpStream = helpSet.getResourcesClass().getResourceAsStream(helpSet.getIndexXml());
	if (helpStream != null) {
	    try {
		helpTree.setHelpIndex(parser.parse(helpStream));
	    } finally {
		helpStream.close();
	    }
	} else {
	    HelpIndex helpIndex = new HelpIndex();
	    final HelpItem helpItem = new HelpItem();
	    helpItem.setName("Help contents not found");
	    helpIndex.addSubItem(helpItem);
	    helpTree.setHelpIndex(helpIndex);
	}
	helpTree.getIndexTree().getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	helpTree.setRootContentsNode(new DefaultMutableTreeNode("Contents"));
	helpTree.setHelpTreeModel(new DefaultTreeModel(helpTree.getRootContentsNode(), true));
	helpTree.getIndexTree().setModel(helpTree.getHelpTreeModel());
	populateIndex(helpTree);
	return helpTree;
    }

    private HelpTree getHelpTree(String resourceSetName) {
	return helpTreesMap.get(resourceSetName);
    }

    /**
     *
     * @param helpPage page title <strong>without section number</strong> of the help page (file name will be looked up)
     */
    public void setCurrentPage(String helpSet, String helpPage) {
	final HelpItem helpFile = getHelpFileByName(getHelpTree(helpSet).getHelpIndex(), helpPage);
	if (helpFile != null) {
	    showHelpItem(getHelpTree(helpSet), helpFile.getFile());
	} else {
	    BugCatcherManager.getBugCatcher().logError(new Exception("Help page not found: " + helpPage));
	}
    }

    private HelpItem getHelpFileByName(HelpIndex parentItem, String name) {
	for (HelpItem child : parentItem.getSubItems()) {
	    if (child.getName().replaceAll("^\\d+\\.", "").trim().equals(name)) {
		return child;
	    } else {
		HelpItem childResult = getHelpFileByName(child, name);
		if (childResult != null) {
		    return childResult;
		}
	    }
	}
	return null;
    }

    private void populateIndex(HelpTree tree) {
	final DefaultMutableTreeNode rootNode = tree.getRootContentsNode();
	for (HelpItem item : tree.getHelpIndex().getSubItems()) {
	    populateIndex(rootNode, item);
	}
	tree.getHelpTreeModel().reload(rootNode);
    }

    private void populateIndex(DefaultMutableTreeNode parent, HelpItem item) {
	DefaultMutableTreeNode itemNode = new DefaultMutableTreeNode(item, item.getSubItems().size() > 0);
	//helpTreeModel.insertNodeInto(node, parent, parent.getChildCount());
	parent.add(itemNode);
	for (HelpItem subItem : item.getSubItems()) {
	    populateIndex(itemNode, subItem);
	}
    }

    private void initComponents() {
	helpTextPane = new javax.swing.JTextPane();
	helpTextPane.addHyperlinkListener(new ArbilHyperlinkListener());
	helpTextPane.setMinimumSize(new Dimension(100, 100));
	helpTextPane.setContentType("text/html;charset=UTF-8");
	helpTextPane.setEditable(false);

	helpTextScrollPane = new javax.swing.JScrollPane();
	helpTextScrollPane.setViewportView(helpTextPane);

	jSplitPane1 = new javax.swing.JSplitPane();
	jSplitPane1.setDividerLocation(250);
	jSplitPane1.setRightComponent(helpTextScrollPane);

	setLayout(new java.awt.BorderLayout());
	add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }

    private void initHelpTreeComponents(HelpTree helpTree) {
	JTree indexTree = new javax.swing.JTree();
	indexTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
	    public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
		indexTreeValueChanged();
	    }
	});
	helpTree.setIndexTree(indexTree);

	JScrollPane indexScrollPane = new JScrollPane();
	indexScrollPane.setViewportView(indexTree);
	helpTree.setIndexScrollPane(indexScrollPane);
    }

    private void initHelpTreeTabs() {
	//TODO: If only one helpTree, skip the tabs
	tabbedPane = new JTabbedPane();
	for (HelpTree tree : helpTrees) {
	    tabbedPane.add(tree.getHelpResourceSet().getName(), tree.getIndexScrollPane());
	}
	tabbedPane.addChangeListener(new ChangeListener() {
	    public void stateChanged(ChangeEvent e) {
		updateTabState();
	    }
	});
	jSplitPane1.setLeftComponent(tabbedPane);
	updateTabState();
    }

    private void updateTabState() {
	currentTree = helpTrees.get(tabbedPane.getSelectedIndex());
	((HTMLDocument) helpTextPane.getDocument()).setBase(currentTree.getHelpResourceSet().getResourcesClass().getResource(currentTree.getHelpResourceSet().getHelpResourceBase()));
	if (currentTree.getIndexTree().getSelectionCount() == 0) {
	    // Select first node so that there is a selection for this tree
	    currentTree.getIndexTree().setSelectionRow(1);
	} else {
	    // Trigger update for existing selection of new current tree
	    indexTreeValueChanged();
	}
    }

    private void indexTreeValueChanged() {
	DefaultMutableTreeNode node = (DefaultMutableTreeNode) currentTree.getIndexTree().getLastSelectedPathComponent();
	if (node != null) {
	    Object nodeInfo = node.getUserObject();
	    if (nodeInfo instanceof HelpItem) {
		showHelpItem(currentTree, ((HelpItem) nodeInfo).getFile());
	    }
	}
    }

    public boolean showHelpItem(String helpSetName, URL itemURL) {
	final HelpTree helpTree = getHelpTree(helpSetName);
	final HelpResourceSet helpSet = helpTree.getHelpResourceSet();

	try {
	    final URI baseUri = helpSet.getResourcesClass().getResource(helpSet.getHelpResourceBase()).toURI();
	    if (itemURL.toString().startsWith(baseUri.toString())) {
		URI relativeURI = baseUri.relativize(itemURL.toURI());
		// Update index, which will show the help item if found. Use only path, i.e. ignore the fragment
		// TODO: Keep fragment info
		return updateIndex(helpTree, relativeURI.getPath().toString(), relativeURI.getFragment());
	    }
	} catch (URISyntaxException usEx) {
	    BugCatcherManager.getBugCatcher().logError(usEx);
	}
	return false;
    }

    private void showHelpItem(HelpTree helpTree, final String itemResource) {
	final HelpResourceSet helpSet = helpTree.getHelpResourceSet();

	final StringBuilder completeHelpText = new StringBuilder();
	final InputStream itemStream = helpSet.getResourcesClass().getResourceAsStream(helpSet.getHelpResourceBase() + itemResource);
	if (itemStream == null) {
	    completeHelpText.append("Page not found");
	} else {
	    try {
		BufferedReader bufferedHelpReader = new BufferedReader(new InputStreamReader(itemStream, "UTF-8"));
		try {
		    for (String helpLine = bufferedHelpReader.readLine(); helpLine != null; helpLine = bufferedHelpReader.readLine()) {
			completeHelpText.append(helpLine);
		    }
		} catch (IOException ioEx) {
		    completeHelpText.append("<p><strong>I/O exception while reading help contents</strong></p>");
		    BugCatcherManager.getBugCatcher().logError(ioEx);
		} finally {
		    try {
			bufferedHelpReader.close();
		    } catch (IOException ioEx) {
			completeHelpText.append("<p><strong>I/O exception while close stream</strong></p>");
			BugCatcherManager.getBugCatcher().logError(ioEx);
		    }
		}
	    } catch (UnsupportedEncodingException ueEx) {
		completeHelpText.append("<p><strong>I/O exception while close stream</strong></p>");
		BugCatcherManager.getBugCatcher().logError(ueEx);
	    }
	}
	helpTextPane.setText(completeHelpText.toString());
	// Scroll to top
	helpTextPane.setCaretPosition(0);
	updateIndex(helpTree, itemResource);
    }

    private boolean updateIndex(final HelpTree tree, final String itemResource, final String fragment) {
	if (updateIndex(tree, itemResource)) {
	    if (fragment != null) {
		//TODO: Jump to fragment. The code below may not do this reliably
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			helpTextPane.scrollToReference(fragment);
		    }
		});
	    }
	    return true;
	}
	return false;
    }

    /**
     * Sets the selection of the index tree to the item that refers to the specified file
     *
     * @param itemResource file to select
     */
    private boolean updateIndex(final HelpTree tree, final String itemResource) {
	if (itemResource == null) {
	    return false;
	}

	// First check current selection
	DefaultMutableTreeNode lastPathComponent = (DefaultMutableTreeNode) tree.getIndexTree().getSelectionPath().getLastPathComponent();
	if (lastPathComponent.getUserObject() instanceof HelpItem) {
	    final HelpItem helpItem = (HelpItem) lastPathComponent.getUserObject();
	    if (itemResource.equals(helpItem.getFile())) {
		// Selection is equal to specified item, do not change selection
		return true;
	    }
	}

	// Search node with specified resource
	DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getIndexTree().getModel().getRoot();
	DefaultMutableTreeNode selectionItem = findChild(root, itemResource);
	if (selectionItem != null) {
	    // Node found, set selection
	    final TreePath treePath = new TreePath(selectionItem.getPath());
	    tree.getIndexTree().setSelectionPath(treePath);
	    // And scroll to it
	    tree.getIndexTree().scrollPathToVisible(treePath);
	    return true;
	} else {
	    return false;
	}
    }

    private DefaultMutableTreeNode findChild(DefaultMutableTreeNode root, String itemResource) {
	for (int i = 0; i < root.getChildCount(); i++) {
	    DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) root.getChildAt(i);
	    Object userObject = childNode.getUserObject();
	    if (userObject instanceof HelpItem) {
		HelpItem helpItem = (HelpItem) userObject;
		if (helpItem.getFile().equals(itemResource)) {
		    return childNode;
		}
	    }

	    DefaultMutableTreeNode findChild = findChild(childNode, itemResource);
	    if (findChild != null) {
		return findChild;
	    }
	}
	return null;
    }

    public static class HelpResourceSet {

	private final String name;
	private final Class resourcesClass;
	private final String helpResourceBase;
	private final String indexXml;

	public HelpResourceSet(String name, Class resourcesClass, String helpResourceBase, String indexXml) {
	    this.name = name;
	    this.resourcesClass = resourcesClass;
	    this.helpResourceBase = helpResourceBase;
	    this.indexXml = indexXml;
	}

	public String getName() {
	    return name;
	}

	public Class getResourcesClass() {
	    return resourcesClass;
	}

	public String getHelpResourceBase() {
	    return helpResourceBase;
	}

	public String getIndexXml() {
	    return indexXml;
	}
    }

    private static class HelpTree {

	private final HelpResourceSet helpResourceSet;
	private HelpIndex helpIndex;
	private javax.swing.JTree indexTree;
	private DefaultTreeModel helpTreeModel;
	private DefaultMutableTreeNode rootContentsNode;
	private javax.swing.JScrollPane indexScrollPane;

	public JScrollPane getIndexScrollPane() {
	    return indexScrollPane;
	}

	public void setIndexScrollPane(JScrollPane indexScrollPane) {
	    this.indexScrollPane = indexScrollPane;
	}

	public DefaultTreeModel getHelpTreeModel() {
	    return helpTreeModel;
	}

	public void setHelpTreeModel(DefaultTreeModel helpTreeModel) {
	    this.helpTreeModel = helpTreeModel;
	}

	public DefaultMutableTreeNode getRootContentsNode() {
	    return rootContentsNode;
	}

	public void setRootContentsNode(DefaultMutableTreeNode rootContentsNode) {
	    this.rootContentsNode = rootContentsNode;
	}

	public HelpTree(HelpResourceSet helpResourceSet) {
	    this.helpResourceSet = helpResourceSet;
	}

	public HelpIndex getHelpIndex() {
	    return helpIndex;
	}

	public void setHelpIndex(HelpIndex helpIndex) {
	    this.helpIndex = helpIndex;
	}

	public JTree getIndexTree() {
	    return indexTree;
	}

	public void setIndexTree(JTree indexTree) {
	    this.indexTree = indexTree;
	}

	public HelpResourceSet getHelpResourceSet() {
	    return helpResourceSet;
	}
    }
}
