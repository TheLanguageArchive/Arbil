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
 * @author Peter.Withers@mpi.nl
 */
public class ArbilHelp extends javax.swing.JPanel {

    final static public String SHOTCUT_KEYS_PAGE = "Shortcut Keys";
    final static public String INTRODUCTION_PAGE = "Overview";
    final static public String helpWindowTitle = "Help Viewer";
    private final HelpIndex helpIndex;
    private final String helpResourceBase;
    private final DefaultTreeModel helpTreeModel;
    private final DefaultMutableTreeNode rootContentsNode;
    static private ArbilHelp singleInstance = null;

    static synchronized public ArbilHelp getArbilHelpInstance() throws IOException, SAXException {
	//TODO: This should not be a singleton...
	if (singleInstance == null) {
	    final String helpResourceBase = "/nl/mpi/arbil/resources/html/help/";
	    singleInstance = new ArbilHelp(ArbilHelp.class, helpResourceBase, helpResourceBase + "arbil.xml");
	}
	return singleInstance;
    }

    /**
     *
     * @param resourcesClass Class for getting resources
     * @param helpResourceBase specification of base package for help resources
     * @param indexXml xml that specifies
     * @throws IOException
     * @throws SAXException
     */
    public ArbilHelp(final Class resourcesClass, final String helpResourceBase, final String indexXml) throws IOException, SAXException {
	initComponents();

	this.helpResourceBase = helpResourceBase;

	final HelpItemsParser parser = new HelpItemsParser();
	final InputStream helpStream = getClass().getResourceAsStream(indexXml);
	try {
	    helpIndex = parser.parse(helpStream);
	} finally {
	    helpStream.close();
	}

	helpTextPane.setContentType("text/html");
	((HTMLDocument) helpTextPane.getDocument()).setBase(this.getClass().getResource(helpResourceBase));
	indexTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

	rootContentsNode = new DefaultMutableTreeNode("Contents");
	helpTreeModel = new DefaultTreeModel(rootContentsNode, true);
	indexTree.setModel(helpTreeModel);

	populateIndex(rootContentsNode);

	indexTree.setSelectionRow(1);
    }

    /**
     *
     * @param helpPage page title <strong>without section number</strong> of the help page (file name will be looked up)
     */
    public void setCurrentPage(String helpPage) {
	final HelpItem helpFile = getHelpFileByName(helpIndex, helpPage);
	if (helpFile != null) {
	    showHelpItem(helpFile.getFile());
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

    private void populateIndex(DefaultMutableTreeNode root) {
	for (HelpItem item : helpIndex.getSubItems()) {
	    populateIndex(root, item);
	}
	helpTreeModel.reload(root);
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
	indexTree = new javax.swing.JTree();
	indexTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
	    public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
		indexTreeValueChanged(evt);
	    }
	});

	indexScrollPane = new javax.swing.JScrollPane();
	indexScrollPane.setViewportView(indexTree);

	helpTextPane = new javax.swing.JTextPane();
	helpTextPane.addHyperlinkListener(new ArbilHyperlinkListener());
	helpTextPane.setMinimumSize(new Dimension(100, 100));
	helpTextPane.setContentType("text/html;charset=UTF-8");
	helpTextPane.setEditable(false);

	helpTextScrollPane = new javax.swing.JScrollPane();
	helpTextScrollPane.setViewportView(helpTextPane);

	jSplitPane1 = new javax.swing.JSplitPane();
	jSplitPane1.setDividerLocation(250);
	jSplitPane1.setLeftComponent(indexScrollPane);
	jSplitPane1.setRightComponent(helpTextScrollPane);

	setLayout(new java.awt.BorderLayout());
	add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }

    private void indexTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {
	DefaultMutableTreeNode node = (DefaultMutableTreeNode) indexTree.getLastSelectedPathComponent();
	if (node != null) {
	    Object nodeInfo = node.getUserObject();
	    if (nodeInfo instanceof HelpItem) {
		showHelpItem(((HelpItem) nodeInfo).getFile());
	    }
	}
    }

    public boolean showHelpItem(URL itemURL) {
	try {
	    final URI baseUri = getClass().getResource(helpResourceBase).toURI();
	    if (itemURL.toString().startsWith(baseUri.toString())) {
		URI relativeURI = baseUri.relativize(itemURL.toURI());
		// Update index, which will show the help item if found. Use only path, i.e. ignore the fragment
		// TODO: Keep fragment info
		return updateIndex(relativeURI.getPath().toString());
	    }
	} catch (URISyntaxException usEx) {
	    BugCatcherManager.getBugCatcher().logError(usEx);
	}
	return false;
    }

    private void showHelpItem(final String itemResource) {
	final StringBuilder completeHelpText = new StringBuilder();
	final InputStream itemStream = getClass().getResourceAsStream(helpResourceBase + itemResource);
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
	updateIndex(itemResource);
    }
    private javax.swing.JScrollPane indexScrollPane;
    private javax.swing.JScrollPane helpTextScrollPane;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextPane helpTextPane;
    private javax.swing.JTree indexTree;

    /**
     * Sets the selection of the index tree to the item that refers to the specified file
     *
     * @param itemResource file to select
     */
    private boolean updateIndex(String itemResource) {
	// First check current selection
	DefaultMutableTreeNode lastPathComponent = (DefaultMutableTreeNode) indexTree.getSelectionPath().getLastPathComponent();
	if (lastPathComponent.getUserObject() instanceof HelpItem) {
	    if (((HelpItem) lastPathComponent.getUserObject()).getFile().equals(itemResource)) {
		// Selection is equal to specified item, do not change selection
		return true;
	    }
	}

	// Search node with specified resource
	DefaultMutableTreeNode root = (DefaultMutableTreeNode) indexTree.getModel().getRoot();
	DefaultMutableTreeNode selectionItem = findChild(root, itemResource);
	if (selectionItem != null) {
	    // Node found, set selection
	    final TreePath treePath = new TreePath(selectionItem.getPath());
	    indexTree.setSelectionPath(treePath);
	    // And scroll to it
	    indexTree.scrollPathToVisible(treePath);
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
}
