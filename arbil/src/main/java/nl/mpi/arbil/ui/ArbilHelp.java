package nl.mpi.arbil.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import javax.swing.SwingUtilities;
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

    final static public String SHOTCUT_KEYS_PAGE = "Short Cut Keys";
    final static public String INTRODUCTION_PAGE = "2. Quick Introduction";
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

	jTextPane1.setContentType("text/html");
	((HTMLDocument) jTextPane1.getDocument()).setBase(this.getClass().getResource(helpResourceBase));
	indexTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

	rootContentsNode = new DefaultMutableTreeNode("Contents");
	helpTreeModel = new DefaultTreeModel(rootContentsNode, true);
	indexTree.setModel(helpTreeModel);

	populateIndex(rootContentsNode);

	indexTree.setSelectionRow(1);
    }

    private DefaultMutableTreeNode findNode(DefaultMutableTreeNode currentNode, String helpPage) {
	System.out.println("currentNode: " + currentNode);
	if (currentNode.getUserObject().toString().equals(helpPage)) {
	    return currentNode;
	}
	if (currentNode.getChildCount() >= 0) {
	    for (Enumeration e = currentNode.children(); e.hasMoreElements();) {
		DefaultMutableTreeNode nextNode = (DefaultMutableTreeNode) e.nextElement();
		DefaultMutableTreeNode foundNode = findNode(nextNode, helpPage);
		if (foundNode != null) {
		    return foundNode;
		}
	    }
	}
	return null;
    }

    public void setCurrentPage(String helpPage) {
	DefaultMutableTreeNode foundNode = findNode(rootContentsNode, helpPage);
	if (foundNode != null) {
	    final TreePath targetTreePath = new TreePath(((DefaultMutableTreeNode) foundNode).getPath());
	    SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    indexTree.scrollPathToVisible(targetTreePath);
		    indexTree.setSelectionPath(targetTreePath);
		}
	    });
	}
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

	jSplitPane1 = new javax.swing.JSplitPane();
	jScrollPane1 = new javax.swing.JScrollPane();
	indexTree = new javax.swing.JTree();
	jScrollPane2 = new javax.swing.JScrollPane();
	jTextPane1 = new javax.swing.JTextPane();
	jTextPane1.addHyperlinkListener(new ArbilHyperlinkListener());

	setLayout(new java.awt.BorderLayout());

	jSplitPane1.setDividerLocation(200);

	indexTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
	    public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
		indexTreeValueChanged(evt);
	    }
	});
	jScrollPane1.setViewportView(indexTree);

	jSplitPane1.setLeftComponent(jScrollPane1);

	jTextPane1.setEditable(false);
	jScrollPane2.setViewportView(jTextPane1);

	jSplitPane1.setRightComponent(jScrollPane2);

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

    private void showHelpItem(final String itemResource) {
	final StringBuilder completeHelpText = new StringBuilder();
	final InputStream itemStream = getClass().getResourceAsStream(helpResourceBase + itemResource);
	try {
	    if (itemStream == null) {
		completeHelpText.append("Page not found");
	    } else {
		BufferedReader bufferedHelpReader = new BufferedReader(new InputStreamReader(itemStream));
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
	    }
	} finally {
	    if (itemStream != null) {
		try {
		    itemStream.close();
		} catch (IOException ioEx) {
		    completeHelpText.append("<p><strong>I/O exception while handling help resource");
		    BugCatcherManager.getBugCatcher().logError(ioEx);
		}
	    }
	}
	jTextPane1.setText(completeHelpText.toString());
    }
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JTree indexTree;
}
