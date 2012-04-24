package nl.mpi.arbil.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.parsers.ParserConfigurationException;
import nl.mpi.arbil.ArbilMetadataException;
import nl.mpi.arbil.clarin.profiles.CmdiProfileReader;
import nl.mpi.arbil.data.metadatafile.ImdiUtils;
import nl.mpi.arbil.data.metadatafile.ArbilMetadataReader;
import nl.mpi.arbil.templates.ArbilFavourites;
import nl.mpi.arbil.templates.ArbilTemplateManager;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ApplicationVersion;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.TreeHelper;
import nl.mpi.arbil.util.WindowManager;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Document : MetadataBuilder
 * Created on : Jun 9, 2010, 4:03:07 PM
 * Author : Peter Withers
 */
public class MetadataBuilder {

    private static MessageDialogHandler messageDialogHandler;

    public static void setMessageDialogHandler(MessageDialogHandler handler) {
	messageDialogHandler = handler;
    }
    private static WindowManager windowManager;

    public static void setWindowManager(WindowManager windowManagerInstance) {
	windowManager = windowManagerInstance;
    }
    private static SessionStorage sessionStorage;

    public static void setSessionStorage(SessionStorage sessionStorageInstance) {
	sessionStorage = sessionStorageInstance;
    }
    private static TreeHelper treeHelper;

    public static void setTreeHelper(TreeHelper treeHelperInstance) {
	treeHelper = treeHelperInstance;
    }
    private static DataNodeLoader dataNodeLoader;

    public static void setDataNodeLoader(DataNodeLoader dataNodeLoaderInstance) {
	dataNodeLoader = dataNodeLoaderInstance;
    }
    private static ApplicationVersionManager versionManager;

    public static void setVersionManager(ApplicationVersionManager versionManagerInstance) {
	versionManager = versionManagerInstance;
    }
    private ArbilComponentBuilder arbilComponentBuilder = new ArbilComponentBuilder();

    /**
     * Requests to add a new node of given type to root
     *
     * @param nodeType Name of node type
     * @param nodeTypeDisplayName Name to display as node type
     */
    public void requestRootAddNode(String nodeType, String nodeTypeDisplayName) {
	ArbilDataNode arbilDataNode = dataNodeLoader.createNewDataNode(sessionStorage.getNewArbilFileName(sessionStorage.getSaveLocation(""), nodeType));
	requestAddNode(arbilDataNode, nodeType, nodeTypeDisplayName);
    }

    /**
     * Requests to add a node on basis of a given existing node to the local corpus
     *
     * @param nodeType Name of node type
     * @param addableNode Node to base new node on
     */
    public void requestAddRootNode(final ArbilDataNode addableNode, final String nodeTypeDisplayNameLocal) {
	// Start new thread to add the node to its destination
	creatAddAddableNodeThread(null, nodeTypeDisplayNameLocal, addableNode).start();
    }

    /**
     * Checks whether the destinationNode in its current state supports adding a node of the specified type
     *
     * @param destinationNode Proposed destination node
     * @param nodeType Full type name of the node to add
     * @return Whether the node can be added
     */
    public boolean canAddChildNode(final ArbilDataNode destinationNode, final String nodeType) {
	final String targetXmlPath = destinationNode.getURI().getFragment();

	synchronized (destinationNode.getParentDomLockObject()) {
	    // Ignore CMDI metadata
	    if (nodeType.startsWith(".") && destinationNode.isCmdiMetaDataNode()) {
		// Check whether clarin sub node can be added
		return arbilComponentBuilder.canInsertChildComponent(destinationNode, targetXmlPath, nodeType);
	    } else {                // Ignore non-child nodes
		if (destinationNode.getNodeTemplate().isArbilChildNode(nodeType)) {
		    // Do a quick pre-check whether there is a finite number of occurrences
		    if (destinationNode.getNodeTemplate().getMaxOccursForTemplate(nodeType) >= 0) {
			System.out.println("adding to current node");
			try {
			    Document nodDom = ArbilComponentBuilder.getDocument(destinationNode.getURI());
			    if (nodDom == null) {
				messageDialogHandler.addMessageDialogToQueue("The metadata file could not be opened", "Add Node");
			    } else {
				return ArbilMetadataReader.getSingleInstance().canInsertFromTemplate(destinationNode.getNodeTemplate(), nodeType, targetXmlPath, nodDom);
			    }
			} catch (ParserConfigurationException ex) {
			    BugCatcherManager.getBugCatcher().logError(ex);
			} catch (SAXException ex) {
			    BugCatcherManager.getBugCatcher().logError(ex);
			} catch (IOException ex) {
			    BugCatcherManager.getBugCatcher().logError(ex);
			} catch (ArbilMetadataException ex) {
			    BugCatcherManager.getBugCatcher().logError(ex);
			}
		    }
		}
		// Other cases not handled
		return true;
	    }
	}
    }

    /**
     * Requests to add a new node of given type to given destination node
     *
     * @param destinationNode Node to add new node to
     * @param nodeType Name of node type
     * @param nodeTypeDisplayName Name to display as node type
     */
    public void requestAddNode(final ArbilDataNode destinationNode, final String nodeType, final String nodeTypeDisplayName) {
	if (destinationNode.getNeedsSaveToDisk(false)) {
	    destinationNode.saveChangesToCache(true);
	}
	new Thread("requestAddNode") {

	    @Override
	    public void run() {
		ArbilNode addedNode = null;
		destinationNode.updateLoadingState(1);
		synchronized (destinationNode.getParentDomLockObject()) {
		    try {
			System.out.println("requestAddNode: " + nodeType + " : " + nodeTypeDisplayName);
			addedNode = processAddNodes(destinationNode, nodeType, destinationNode.getURI().getFragment(), nodeTypeDisplayName, null, null, null);

			// CODE REMOVED: previously, imdiLoaders was requested to reload destinationNode
		    } catch (ArbilMetadataException exception) {
			messageDialogHandler.addMessageDialogToQueue(exception.getLocalizedMessage(), "Insert node error");
		    }
		}
		destinationNode.updateLoadingState(-1);
		if (addedNode != null) {
		    destinationNode.triggerNodeAdded(addedNode);
		}
	    }
	}.start();
    }

    /**
     * Requests to add a node on basis of a given existing node to the given destination node
     *
     * @param destinationNode Node to add new node to
     * @param nodeType Name of node type
     * @param addableNode Node to base new node on
     */
    public void requestAddNode(final ArbilDataNode destinationNode, final String nodeTypeDisplayNameLocal, final ArbilDataNode addableNode) {
	if (destinationNode.getNeedsSaveToDisk(false)) {
	    destinationNode.saveChangesToCache(true);
	}
	// Start new thread to add the node to its destination
	creatAddAddableNodeThread(destinationNode, nodeTypeDisplayNameLocal, addableNode).start();
    }

    /**
     * Creates a thread to be triggered by requestAddNode for addableNode
     *
     * @param destinationNode Node to add new node to
     * @param nodeType Name of node type
     * @param addableNode Node to base new node on
     * @return New thread that adds the addable node
     */
    private Thread creatAddAddableNodeThread(final ArbilDataNode destinationNode, final String nodeTypeDisplayNameLocal, final ArbilDataNode addableNode) {
	return new Thread("requestAddNode") {

	    @Override
	    public void run() {
		try {
		    if (destinationNode != null) {
			destinationNode.updateLoadingState(1);
			addNode(destinationNode, nodeTypeDisplayNameLocal, addableNode);
		    } else {
			addNodeToRoot(nodeTypeDisplayNameLocal, addableNode);
		    }
		} catch (ArbilMetadataException exception) {
		    messageDialogHandler.addMessageDialogToQueue(exception.getLocalizedMessage(), "Insert node error");
		} catch (UnsupportedOperationException exception) {
		    messageDialogHandler.addMessageDialogToQueue(exception.getLocalizedMessage(), "Insert node error");
		} finally {
		    if (destinationNode != null) {
			destinationNode.updateLoadingState(-1);
		    }
		}
	    }
	};
    }

    public void addNode(final ArbilDataNode destinationNode, final String nodeTypeDisplayNameLocal, final ArbilDataNode addableNode) throws ArbilMetadataException {
	synchronized (destinationNode.getParentDomLockObject()) {
	    if (addableNode.isMetaDataNode()) {
		addMetaDataNode(destinationNode, nodeTypeDisplayNameLocal, addableNode);
	    } else {
		addNonMetaDataNode(destinationNode, nodeTypeDisplayNameLocal, addableNode);
	    }
	}
    }

    public void addNodeToRoot(final String nodeTypeDisplayNameLocal, final ArbilDataNode addableNode) throws ArbilMetadataException {
	if (addableNode.isMetaDataNode()) {
	    addMetaDataNode(null, nodeTypeDisplayNameLocal, addableNode);
	} else {
	    addNonMetaDataNode(null, nodeTypeDisplayNameLocal, addableNode);
	}

    }

    private void addNonMetaDataNode(final ArbilDataNode destinationNode, final String nodeTypeDisplayNameLocal, final ArbilDataNode addableNode) throws ArbilMetadataException {
	String nodeTypeDisplayName = nodeTypeDisplayNameLocal;
	ArbilDataNode[] sourceArbilNodeArray;
	if (addableNode.isContainerNode()) {
	    sourceArbilNodeArray = addableNode.getChildArray();
	} else {
	    sourceArbilNodeArray = new ArbilDataNode[]{addableNode};
	}
	for (ArbilDataNode currentArbilNode : sourceArbilNodeArray) {
	    if (destinationNode.isCmdiMetaDataNode()) {
		new ArbilComponentBuilder().insertResourceProxy(destinationNode, addableNode);
		destinationNode.getParentDomNode().loadArbilDom();
	    } else {
		String nodeType;
		String favouriteUrlString = null;
		URI resourceUrl = null;
		String mimeType = null;
		if (currentArbilNode.isArchivableFile() && !currentArbilNode.isMetaDataNode()) {
		    nodeType = ArbilMetadataReader.getSingleInstance().getNodeTypeFromMimeType(currentArbilNode.mpiMimeType);
		    if (nodeType == null) {
			nodeType = handleUnknownMimetype(currentArbilNode);
		    }
		    resourceUrl = currentArbilNode.getURI();
		    mimeType = currentArbilNode.mpiMimeType;
		    nodeTypeDisplayName = "Resource";
		} else {
		    nodeType = ArbilFavourites.getSingleInstance().getNodeType(currentArbilNode, destinationNode);
		    favouriteUrlString = currentArbilNode.getUrlString();
		}
		if (nodeType != null) {
		    String targetXmlPath = destinationNode.getURI().getFragment();
		    System.out.println("requestAddNode: " + nodeType + " : " + nodeTypeDisplayName + " : " + favouriteUrlString + " : " + resourceUrl);
		    processAddNodes(destinationNode, nodeType, targetXmlPath, nodeTypeDisplayName, favouriteUrlString, mimeType, resourceUrl);
		    destinationNode.getParentDomNode().loadArbilDom();
		}
	    }
	}
    }

    /**
     *
     * @param currentArbilNode
     * @return Manual nodetype, if set. Otherwise null
     */
    private String handleUnknownMimetype(ArbilDataNode currentArbilNode) {
	if (JOptionPane.YES_OPTION == messageDialogHandler.showDialogBox("There is no controlled vocabulary for either Written Resource or Media File that match \""
		+ currentArbilNode.mpiMimeType + "\".\n"
		+ "This probably means that the file is not archivable. However, you can proceed by manually selecting the resource type.\n\n"
		+ "Do you want to proceed?\n\nWARNING: Doing this will not guarantee that your data will be uploadable to the corpus server!",
		"Add Resource",
		JOptionPane.YES_NO_OPTION,
		JOptionPane.PLAIN_MESSAGE)) {
	    String originalMime = currentArbilNode.mpiMimeType;
	    currentArbilNode.mpiMimeType = null;
	    if (new ImdiUtils().overrideTypecheckerDecision(new ArbilDataNode[]{currentArbilNode})) {
		// Try again
		return ArbilMetadataReader.getSingleInstance().getNodeTypeFromMimeType(currentArbilNode.mpiMimeType);
	    } else {
		currentArbilNode.mpiMimeType = originalMime;
	    }
	}
	return null;
    }

    private void addMetaDataNode(final ArbilDataNode destinationNode, final String nodeTypeDisplayNameLocal, final ArbilDataNode addableNode) throws ArbilMetadataException {
	URI addedNodeUri;
	if (addableNode.getURI().getFragment() == null) {
	    if (destinationNode != null) {
		addedNodeUri = sessionStorage.getNewArbilFileName(destinationNode.getSubDirectory(), addableNode.getURI().getPath());
	    } else {
		addedNodeUri = sessionStorage.getNewArbilFileName(sessionStorage.getSaveLocation(""), addableNode.getURI().getPath());
	    }
	    ArbilDataNode.getMetadataUtils(addableNode.getURI().toString()).copyMetadataFile(addableNode.getURI(), new File(addedNodeUri), null, true);
	    ArbilDataNode addedNode = dataNodeLoader.getArbilDataNodeWithoutLoading(addedNodeUri);
	    new ArbilComponentBuilder().removeArchiveHandles(addedNode);
	    if (destinationNode == null) {
		// Destination node null means add to tree root
		treeHelper.addLocation(addedNodeUri);
		treeHelper.applyRootLocations();
	    } else {
		destinationNode.metadataUtils.addCorpusLink(destinationNode.getURI(), new URI[]{addedNodeUri});
	    }
	    addedNode.loadArbilDom();
	    addedNode.scrollToRequested = true;
	} else {
	    if (destinationNode == null) {
		// Cannot add subnode to local corpus tree root
		BugCatcherManager.getBugCatcher().logError(new Exception("Attempt to add child node to local corpus root"));
		return;
	    }
	    addedNodeUri = arbilComponentBuilder.insertFavouriteComponent(destinationNode, addableNode);
	    new ArbilComponentBuilder().removeArchiveHandles(destinationNode);
	}
	if (destinationNode != null) {
	    destinationNode.getParentDomNode().loadArbilDom();
	}
	String newTableTitleString = "new " + addableNode + (destinationNode == null ? "" : (" in " + destinationNode));
	windowManager.openFloatingTableOnce(new URI[]{addedNodeUri}, newTableTitleString);
    }

    private ArbilDataNode processAddNodes(ArbilDataNode currentArbilNode, String nodeType, String targetXmlPath, String nodeTypeDisplayName, String favouriteUrlString, String mimeType, URI resourceUri) throws ArbilMetadataException {

	// make title for imdi table
	String newTableTitleString = "new " + nodeTypeDisplayName;
	if (currentArbilNode.isMetaDataNode() && currentArbilNode.getFile().exists()) {
	    newTableTitleString = newTableTitleString + " in " + currentArbilNode.toString();
	}

	System.out.println("addQueue:-\nnodeType: " + nodeType + "\ntargetXmlPath: " + targetXmlPath + "\nnodeTypeDisplayName: " + nodeTypeDisplayName + "\nfavouriteUrlString: " + favouriteUrlString + "\nresourceUrl: " + resourceUri + "\nmimeType: " + mimeType);
	// Create child node
	URI addedNodeUri = addChildNode(currentArbilNode, nodeType, targetXmlPath, resourceUri, mimeType);
	// Get the newly created data node
	ArbilDataNode addedArbilNode = dataNodeLoader.getArbilDataNodeWithoutLoading(addedNodeUri);
	if (addedArbilNode != null) {
	    addedArbilNode.getParentDomNode().updateLoadingState(+1);
	    ArbilNode destinationNode = currentArbilNode.getParentDomNode();
	    try {
		addedArbilNode.scrollToRequested = true;
		if (currentArbilNode.getFile().exists()) { // if this is a root node request then the target node will not have a file to reload
		    currentArbilNode.getParentDomNode().loadArbilDom();
		} else {
		    // Root node request, destination node should be corpus root node
		    destinationNode = getLocalCorpusRootNode(destinationNode);
		}
		if (currentArbilNode.getParentDomNode() != addedArbilNode.getParentDomNode()) {
		    addedArbilNode.getParentDomNode().loadArbilDom();
		}
	    } finally {
		addedArbilNode.getParentDomNode().updateLoadingState(-1);
	    }
	}
	windowManager.openFloatingTableOnce(new URI[]{addedNodeUri}, newTableTitleString);
	return addedArbilNode;
    }

    /**
     * Add a new node based on a template and optionally attach a resource
     *
     * @return String path to the added node
     */
    public URI addChildNode(ArbilDataNode destinationNode, String nodeType, String targetXmlPath, URI resourceUri, String mimeType) throws ArbilMetadataException {
	System.out.println("addChildNode:: " + nodeType + " : " + resourceUri);
	System.out.println("targetXmlPath:: " + targetXmlPath);
	if (destinationNode.getNeedsSaveToDisk(false)) {
	    destinationNode.saveChangesToCache(true);
	}
	URI addedNodePath = null;
	destinationNode.updateLoadingState(1);
	try {
	    synchronized (destinationNode.getParentDomLockObject()) {
		if (destinationNode.getNeedsSaveToDisk(false)) {
		    destinationNode.saveChangesToCache(false);
		}
		if (nodeType.startsWith(".") && destinationNode.isCmdiMetaDataNode()) {
		    // Add clarin sub nodes
		    addedNodePath = arbilComponentBuilder.insertChildComponent(destinationNode, targetXmlPath, nodeType);
		} else {
		    if (destinationNode.getNodeTemplate().isArbilChildNode(nodeType) || (resourceUri != null && destinationNode.isSession())) {
			System.out.println("adding to current node");
			try {
			    Document nodDom = ArbilComponentBuilder.getDocument(destinationNode.getURI());
			    if (nodDom == null) {
				messageDialogHandler.addMessageDialogToQueue("The metadata file could not be opened", "Add Node");
			    } else {
				addedNodePath = ArbilMetadataReader.getSingleInstance().insertFromTemplate(destinationNode.getNodeTemplate(), destinationNode.getURI(), destinationNode.getSubDirectory(), nodeType, targetXmlPath, nodDom, resourceUri, mimeType);
				destinationNode.bumpHistory();
				ArbilComponentBuilder.savePrettyFormatting(nodDom, destinationNode.getFile());
				dataNodeLoader.requestReload(destinationNode);
			    }
			} catch (ParserConfigurationException ex) {
			    BugCatcherManager.getBugCatcher().logError(ex);
			} catch (SAXException ex) {
			    BugCatcherManager.getBugCatcher().logError(ex);
			} catch (IOException ex) {
			    BugCatcherManager.getBugCatcher().logError(ex);
			}
//            needsSaveToDisk = true;
		    } else {
			System.out.println("adding new node");
			URI targetFileURI = sessionStorage.getNewArbilFileName(destinationNode.getSubDirectory(), nodeType);
			if (CmdiProfileReader.pathIsProfile(nodeType)) {
			    // Is CMDI profile
			    try {
				addedNodePath = arbilComponentBuilder.createComponentFile(targetFileURI, new URI(nodeType), false);
				// TODO: some sort of warning like: "Could not add node of type: " + nodeType; would be useful here or downstream
//                    if (addedNodePath == null) {
//                      LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Could not add node of type: " + nodeType, "Error inserting node");
//                    }
			    } catch (URISyntaxException ex) {
				BugCatcherManager.getBugCatcher().logError(ex);
				return null;
			    }
			} else {
			    addedNodePath = addFromTemplate(new File(targetFileURI), nodeType);
			}
			if (destinationNode.getFile().exists()) {
			    destinationNode.metadataUtils.addCorpusLink(destinationNode.getURI(), new URI[]{addedNodePath});
			    destinationNode.getParentDomNode().loadArbilDom();
			} else {
			    treeHelper.addLocation(addedNodePath);
			    treeHelper.applyRootLocations();
			}
		    }
		    // CODE REMOVED: load then save the dom via the api to make sure there are id fields to each node then reload this imdi object
		}
	    }
	} finally {
	    destinationNode.updateLoadingState(-1);
	}
	return addedNodePath;
    }

    private ArbilNode getLocalCorpusRootNode(ArbilNode destinationNode) {
	Object localTreeRoot = treeHelper.getLocalCorpusTreeModel().getRoot();
	if (localTreeRoot instanceof DefaultMutableTreeNode) {
	    Object userObject = ((DefaultMutableTreeNode) localTreeRoot).getUserObject();
	    if (userObject instanceof ArbilRootNode) {
		destinationNode = (ArbilRootNode) userObject;
	    }
	}
	return destinationNode;
    }

    public URI addFromTemplate(File destinationFile, String templateType) {
	System.out.println("addFromJarTemplateFile: " + templateType + " : " + destinationFile);

	// Get local url for template type
	URL templateUrl = constructTemplateUrl(templateType);
	if (templateUrl == null) {
	    return null;
	}

	// Copy (1:1) template to new local file
	URI addedPathUri = copyToDisk(templateUrl, destinationFile);

	try {
	    // Open new metadata file
	    Document addedDocument = ArbilComponentBuilder.getDocument(addedPathUri);
	    if (addedDocument == null) {
		//                BugCatcherManager.getBugCatcher().logError(new Exception(ImdiTreeObject.api.getMessage()));
		messageDialogHandler.addMessageDialogToQueue("Error inserting create date", "Add from Template");
	    } else {
		// Set some values to new instance of metadata file

		Node linkNode = org.apache.xpath.XPathAPI.selectSingleNode(addedDocument, "/:METATRANSCRIPT");
		NamedNodeMap metatranscriptAttributes = linkNode.getAttributes();

		// Set the arbil version to the present version
		ApplicationVersion currentVersion = versionManager.getApplicationVersion();
		String arbilVersionString = "Arbil." + currentVersion.currentMajor + "." + currentVersion.currentMinor + "." + currentVersion.currentRevision;

		//                todo: the template must be stored at this point
		//                if (!ArbilTemplateManager.getSingleInstance().defaultTemplateIsCurrentTemplate()) {
		//                    if (!templateType.equals(".METATRANSCRIPT.Corpus")) { // prevent corpus branches getting a template so that the global template takes effect
		//                        arbilVersionString = arbilVersionString + ":" + ArbilTemplateManager.getSingleInstance().getCurrentTemplateName();
		//                    }
		//                }
		arbilVersionString = arbilVersionString + ":" + metatranscriptAttributes.getNamedItem("Originator").getNodeValue();
		metatranscriptAttributes.getNamedItem("Originator").setNodeValue(arbilVersionString);
		//metatranscriptAttributes.getNamedItem("Type").setNodeValue(ArbilTemplateManager.getSingleInstance().getCurrentTemplateName());

		// Set the date field to the current data + time
		metatranscriptAttributes.getNamedItem("Date").setNodeValue(new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));
		// Save new document in formatted XML
		ArbilComponentBuilder.savePrettyFormatting(addedDocument, new File(addedPathUri));
	    }
	} catch (Exception ex) {
	    BugCatcherManager.getBugCatcher().logError(ex);
	}
	return addedPathUri;
    }

    private URL constructTemplateUrl(String templateType) {
	URL templateUrl = null;
	if (CmdiProfileReader.pathIsProfile(templateType)) {
	    try {
		return new URL(templateType);
	    } catch (MalformedURLException ex) {
		BugCatcherManager.getBugCatcher().logError(ex);
		templateUrl = null;
	    }
	} else {
	    templateUrl = ArbilMetadataReader.class.getResource("/nl/mpi/arbil/resources/templates/" + templateType.substring(1) + ".xml");
	}

	if (templateUrl == null) {
	    try {
		templateUrl = ArbilTemplateManager.getSingleInstance().getDefaultComponentOfTemplate(templateType).toURI().toURL();
	    } catch (MalformedURLException exception) {
		BugCatcherManager.getBugCatcher().logError(exception);
		return null;
	    }
	}

	return templateUrl;
    }

    public static URI copyToDisk(URL sourceURL, File targetFile) {
	InputStream in = null;
	OutputStream out = null;
	try {
	    in = sourceURL.openStream();
	    out = new FileOutputStream(targetFile);

	    // Transfer bytes from in to out
	    byte[] buf = new byte[1024];
	    int len;
	    while ((len = in.read(buf)) > 0) {
		out.write(buf, 0, len);
	    }
	    in.close();
	    in = null;
	    out.flush();
	    out.close();
	    out = null;
	    return targetFile.toURI();
	} catch (Exception ex) {
	    System.out.println("copyToDisk: " + ex);
	    BugCatcherManager.getBugCatcher().logError(ex);
	} finally {
	    if (in != null) {
		try {
		    in.close();
		} catch (IOException ioe) {
		    BugCatcherManager.getBugCatcher().logError(ioe);
		}
	    }
	    if (out != null) {
		try {
		    out.close();
		} catch (IOException ioe2) {
		    BugCatcherManager.getBugCatcher().logError(ioe2);
		}
	    }
	}
	return null;
    }
}
