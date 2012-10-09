package nl.mpi.arbil.ui.menu;

import java.awt.Component;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.data.importexport.ArbilToHtmlConverter;
import nl.mpi.arbil.data.metadatafile.ImdiUtils;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.ui.GuiHelper;

/**
 * Abstract base class for context menus
 * 
 * @author Twan Goosen
 */
public abstract class ArbilContextMenu extends JPopupMenu {

    private void applyMenuItems() {
	boolean first = true;
	for (List<OrderedMenuItem> category : itemsMap.values()) {
	    if (!category.isEmpty()) {
		Collections.sort(category);
		if (!first) {
		    add(new JSeparator());
		} else {
		    first = false;
		}
		for (OrderedMenuItem item : category) {
		    add(item.menuItem);
		}
	    }
	}
    }

    public void show(int posX, int posY) {
	// Set common and concrete invisible
	setCommonInvisible();
	setAllInvisible();

	prepareItemCategories();

	// Set up concrete menu
	setUpMenu();

	// Set up common items & actions
	setUpCommonMenuItems();
	setUpCommonActions();

	// build menu from added items
	applyMenuItems();

	// Configure separators
	configureMenuSeparators();

	// Show menu
	super.show(getInvoker(), posX, posY);
	requestFocusInWindow();
    }

    protected abstract void setUpMenu();

    protected abstract void setAllInvisible();

    private void setUpCommonActions() {
	browseForResourceFileMenuItem.setText("Browse For Resource File");
	browseForResourceFileMenuItem.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    File[] selectedFiles = ArbilWindowManager.getSingleInstance().showFileSelectBox("Select Resource File", false, false, false);
		    if (selectedFiles != null && selectedFiles.length > 0) {
			leadSelectedTreeNode.resourceUrlField.setFieldValue(selectedFiles[0].toURL().toExternalForm(), true, false);
		    }
		} catch (Exception ex) {
		    GuiHelper.linorgBugCatcher.logError(ex);
		}
	    }
	});
	addItem(CATEGORY_RESOURCE, PRIORITY_BOTTOM, browseForResourceFileMenuItem);

	saveMenuItem.setText("Save Changes to Disk");
	saveMenuItem.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    for (ArbilDataNode selectedNode : selectedTreeNodes) {
			System.out.println("userObject: " + selectedNode);
			// reloading will first check if a save is required then save and reload
			ArbilDataNodeLoader.getSingleInstance().requestReload((ArbilDataNode) selectedNode.getParentDomNode());
		    }

		} catch (Exception ex) {
		    GuiHelper.linorgBugCatcher.logError(ex);
		}
	    }
	});

	addItem(CATEGORY_DISK, PRIORITY_TOP, saveMenuItem);

	overrideTypeCheckerDecision.setText("Override Type Checker Decision");
	overrideTypeCheckerDecision.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    new ImdiUtils().overrideTypecheckerDecision(selectedTreeNodes);
		} catch (Exception ex) {
		    GuiHelper.linorgBugCatcher.logError(ex);
		}
	    }
	});
	addItem(CATEGORY_WORKING_DIR, PRIORITY_TOP, overrideTypeCheckerDecision);

	openInExternalApplicationMenuItem.setText("Open in External Application");
	// todo: add custom applicaitons menu with dialogue to enter them: suffix, switches, applicaiton file
	openInExternalApplicationMenuItem.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    openFileInExternalApplication(selectedTreeNodes);
		} catch (Exception ex) {
		    GuiHelper.linorgBugCatcher.logError(ex);
		}
	    }
	});
	addItem(CATEGORY_DISK, PRIORITY_BOTTOM, openInExternalApplicationMenuItem);

	viewXmlMenuItem.setText("View XML");
	viewXmlMenuItem.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    for (ArbilDataNode currentNode : selectedTreeNodes) {
			GuiHelper.getSingleInstance().openImdiXmlWindow(currentNode, false, false);
		    }
		} catch (Exception ex) {
		    GuiHelper.linorgBugCatcher.logError(ex);
		}
	    }
	});

	addItem(CATEGORY_XML, PRIORITY_TOP, viewXmlMenuItem);
	viewXmlMenuItemFormatted.setText("View IMDI Formatted");
	viewXmlMenuItemFormatted.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    for (ArbilDataNode currentNode : selectedTreeNodes) {
			GuiHelper.getSingleInstance().openImdiXmlWindow(currentNode, true, false);
		    }
		} catch (Exception ex) {
		    GuiHelper.linorgBugCatcher.logError(ex);
		}
	    }
	});
	addItem(CATEGORY_XML, PRIORITY_TOP + 5, viewXmlMenuItemFormatted);

	openXmlMenuItemFormatted.setText("Open IMDI Formatted");
	openXmlMenuItemFormatted.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    for (ArbilDataNode currentNode : selectedTreeNodes) {
			GuiHelper.getSingleInstance().openImdiXmlWindow(currentNode, true, true);
		    }
		} catch (Exception ex) {
		    GuiHelper.linorgBugCatcher.logError(ex);
		}
	    }
	});
	addItem(CATEGORY_XML, PRIORITY_TOP + 10, openXmlMenuItemFormatted);

	exportHtmlMenuItemFormatted.setText("Export IMDI to HTML");
	exportHtmlMenuItemFormatted.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		try {
		    URI uri = new ArbilToHtmlConverter().exportImdiToHtml(selectedTreeNodes);
		    if (uri != null) {
			System.out.println("Converted to html in " + uri.toString());
			GuiHelper.getSingleInstance().openFileInExternalApplication(uri);
		    }
		} catch (Exception ex) {
		    GuiHelper.linorgBugCatcher.logError(ex);
		    ArbilWindowManager.getSingleInstance().addMessageDialogToQueue("Export to HTML failed. Check the error log for details.", "Export failed");
		}
	    }
	});
	addItem(CATEGORY_XML, PRIORITY_TOP + 15, exportHtmlMenuItemFormatted);
    }

    protected void setUpCommonMenuItems() {
//        todo: continue moving common menu items here
	if (leadSelectedTreeNode != null) {
	    // TODO: test that the node is editable
	    //if (leadSelectedTreeNode.is)
	    saveMenuItem.setVisible(leadSelectedTreeNode.getNeedsSaveToDisk(false));// save sould always be available if the node has been edited

	    if (leadSelectedTreeNode.hasResource()) {
		browseForResourceFileMenuItem.setVisible(true);
	    }
	    if (!leadSelectedTreeNode.isChildNode() && leadSelectedTreeNode.isMetaDataNode()) {
		viewXmlMenuItem.setVisible(true);
		if (!leadSelectedTreeNode.isCmdiMetaDataNode()) {
		    // These are (for now) IMDI only
		    viewXmlMenuItemFormatted.setVisible(true);
		    openXmlMenuItemFormatted.setVisible(true);
		    exportHtmlMenuItemFormatted.setVisible(true);
		}
	    }
	    openInExternalApplicationMenuItem.setVisible(true);
	    overrideTypeCheckerDecision.setVisible(!leadSelectedTreeNode.isMetaDataNode() && leadSelectedTreeNode.mpiMimeType == null);
	}
    }

    private void configureMenuSeparators() {
	// hide and show the separators so that no two separators are displayed without a menu item inbetween
	boolean lastWasSeparator = true;
	Component lastVisibleComponent = null;
	for (Component currentComponent : getComponents()) {
	    if (currentComponent instanceof JSeparator) {
//                if (lastWasSeparator == true) {
		currentComponent.setVisible(!lastWasSeparator);
//                }
		lastWasSeparator = true;
	    } else if (currentComponent.isVisible()) {
		lastWasSeparator = false;
	    }
	    if (currentComponent.isVisible()) {
		lastVisibleComponent = currentComponent;
	    }
	}
	if (lastVisibleComponent != null && lastVisibleComponent instanceof JSeparator) {
	    lastVisibleComponent.setVisible(false);
	}
    }

    private void openFileInExternalApplication(ArbilDataNode[] selectedNodes) {
	for (ArbilDataNode currentNode : selectedNodes) {
	    URI targetUri = null;
	    if (currentNode.hasResource()) {
		targetUri = currentNode.getFullResourceURI();
	    } else {
		targetUri = currentNode.getURI();
		if (targetUri.getFragment() != null) {
		    try {
			targetUri = new URI(targetUri.getScheme(), targetUri.getHost(), targetUri.getPath(), null);
		    } catch (URISyntaxException ex) {
			// Should not happen, derrived from valid URI
			throw new AssertionError(ex);
		    }
		}
	    }
	    GuiHelper.getSingleInstance().openFileInExternalApplication(targetUri);
	}
    }

    protected void setCommonInvisible() {
	viewXmlMenuItem.setVisible(false);
	viewXmlMenuItemFormatted.setVisible(false);
	openXmlMenuItemFormatted.setVisible(false);
	exportHtmlMenuItemFormatted.setVisible(false);
	overrideTypeCheckerDecision.setVisible(false);
	openInExternalApplicationMenuItem.setVisible(false);
	browseForResourceFileMenuItem.setVisible(false);
	saveMenuItem.setVisible(false);
    }

    /**
     * Defines some (or all) item categories in a specific order, so that
     * the order in which actual items are added will not affect the order
     * of these categories
     */
    protected void prepareItemCategories() {
	addItemCategory(CATEGORY_NODE);
	addItemCategory(CATEGORY_RESOURCE);
	addItemCategory(CATEGORY_EDIT);

	addItemCategory(CATEGORY_REMOTE_CORPUS);
	addItemCategory(CATEGORY_WORKING_DIR);
	addItemCategory(CATEGORY_TABLE_CELL);
	addItemCategory(CATEGORY_TABLE_ROW);
	addItemCategory(CATEGORY_ADD_FAVOURITES);

	addItemCategory(CATEGORY_DISK);
	addItemCategory(CATEGORY_IMPORT);
	addItemCategory(CATEGORY_XML);
    }

    protected final void addItemCategory(String category) {
	if (!itemsMap.containsKey(category)) {
	    itemsMap.put(category, new ArrayList<OrderedMenuItem>());
	}
    }

    protected final void addItem(String category, int priority, JMenuItem item) {
	addItemCategory(category);
	itemsMap.get(category).add(new OrderedMenuItem(priority, item));
    }
    protected ArbilDataNode[] selectedTreeNodes = null;
    protected ArbilDataNode leadSelectedTreeNode = null;
    private JMenuItem browseForResourceFileMenuItem = new JMenuItem();
    private JMenuItem viewXmlMenuItem = new JMenuItem();
    private JMenuItem viewXmlMenuItemFormatted = new JMenuItem();
    private JMenuItem openInExternalApplicationMenuItem = new JMenuItem();
    private JMenuItem openXmlMenuItemFormatted = new JMenuItem();
    private JMenuItem exportHtmlMenuItemFormatted = new JMenuItem();
    private JMenuItem overrideTypeCheckerDecision = new JMenuItem();
    private JMenuItem saveMenuItem = new JMenuItem();
    private LinkedHashMap<String, List<OrderedMenuItem>> itemsMap = new LinkedHashMap<String, List<OrderedMenuItem>>();
    protected final static String CATEGORY_NODE = "node";
    protected final static String CATEGORY_EDIT = "edit";
    protected final static String CATEGORY_ADD_FAVOURITES = "add+favourites";
    protected final static String CATEGORY_XML = "xml";
    protected final static String CATEGORY_DISK = "disk";
    protected final static String CATEGORY_REMOTE_CORPUS = "remote corpus";
    protected final static String CATEGORY_RESOURCE = "resource";
    protected final static String CATEGORY_WORKING_DIR = "working dir";
    protected final static String CATEGORY_TABLE_CELL = "table cell";
    protected final static String CATEGORY_TABLE_ROW = "table row";
    protected final static String CATEGORY_IMPORT = "import";
    protected final static int PRIORITY_TOP = 0;
    protected final static int PRIORITY_MIDDLE = 50;
    protected final static int PRIORITY_BOTTOM = 100;

    private static class OrderedMenuItem implements Comparable<OrderedMenuItem> {

	private final JMenuItem menuItem;
	private final Integer itemPriority;

	private OrderedMenuItem(int priority, JMenuItem item) {
	    menuItem = item;
	    itemPriority = Integer.valueOf(priority);
	}

	/** hashCode has to match equals has to match compareTo */
	@Override
	public int hashCode() {
	    return itemPriority.hashCode();
	}

	/** OrderedMenuItem only used in List yet, but e.g. hashes need equals */
	@Override
	public boolean equals(final Object o) {
	    if (o instanceof OrderedMenuItem) {
		return (itemPriority.equals(((OrderedMenuItem) o).itemPriority));
	    }
	    return false;
	}

	public int compareTo(OrderedMenuItem o) {
	    return itemPriority.compareTo(o.itemPriority);
	}
    }
}
