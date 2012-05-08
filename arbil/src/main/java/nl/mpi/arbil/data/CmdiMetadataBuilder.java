package nl.mpi.arbil.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import nl.mpi.arbil.ArbilMetadataException;
import nl.mpi.arbil.clarin.profiles.CmdiProfileReader;
import nl.mpi.arbil.data.metadatafile.ImdiUtils;
import nl.mpi.arbil.templates.ArbilFavourites;
import nl.mpi.arbil.templates.ArbilTemplate;
import nl.mpi.arbil.templates.ArbilTemplateManager;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ApplicationVersion;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.TreeHelper;
import nl.mpi.arbil.util.WindowManager;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 * @author Peter Withers
 */
public class CmdiMetadataBuilder implements MetadataBuilder {

    private final MessageDialogHandler messageDialogHandler;
    private final WindowManager windowManager;
    private final SessionStorage sessionStorage;
    private final TreeHelper treeHelper;
    private final DataNodeLoader dataNodeLoader;
    private final ApplicationVersionManager versionManager;
    private final ArbilComponentBuilder arbilComponentBuilder = new ArbilComponentBuilder();

    public CmdiMetadataBuilder(MessageDialogHandler messageDialogHandler, WindowManager windowManager, SessionStorage sessionStorage, TreeHelper treeHelper, DataNodeLoader dataNodeLoader, ApplicationVersionManager versionManager) {
	this.messageDialogHandler = messageDialogHandler;
	this.windowManager = windowManager;
	this.sessionStorage = sessionStorage;
	this.treeHelper = treeHelper;
	this.dataNodeLoader = dataNodeLoader;
	this.versionManager = versionManager;
    }

    /**
     * Requests to add a node on basis of a given existing node to the local corpus
     *
     * @param nodeType Name of node type
     * @param addableNode Node to base new node on
     */
    @Override
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
    @Override
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
				return canInsertFromTemplate(destinationNode.getNodeTemplate(), nodeType, targetXmlPath, nodDom);
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
    @Override
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
    @Override
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

    @Override
    public void addNode(final ArbilDataNode destinationNode, final String nodeTypeDisplayNameLocal, final ArbilDataNode addableNode) throws ArbilMetadataException {
	synchronized (destinationNode.getParentDomLockObject()) {
	    if (addableNode.isMetaDataNode()) {
		addMetaDataNode(destinationNode, nodeTypeDisplayNameLocal, addableNode);
	    } else {
		addNonMetaDataNode(destinationNode, nodeTypeDisplayNameLocal, addableNode);
	    }
	}
    }

    private void addNodeToRoot(final String nodeTypeDisplayNameLocal, final ArbilDataNode addableNode) throws ArbilMetadataException {
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
		    nodeType = getNodeTypeFromMimeType(currentArbilNode.mpiMimeType);
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

    private String getNodeTypeFromMimeType(String mimeType) {
	System.out.println("getNodeTypeFromMimeType: " + mimeType);
	for (String[] formatType : new String[][]{
		    {"http://www.mpi.nl/IMDI/Schema/WrittenResource-Format.xml", ".METATRANSCRIPT.Session.Resources.WrittenResource", "Manual/WrittenResource"},
		    {"http://www.mpi.nl/IMDI/Schema/MediaFile-Format.xml", ".METATRANSCRIPT.Session.Resources.MediaFile", "Manual/MediaFile"}
		}) {
	    if (formatType[2].equals(mimeType)) {
		System.out.println("UsingOverrideNodeType: " + formatType[1]);
		return formatType[1];
	    } else if (ArbilVocabularies.getSingleInstance().vocabularyContains(formatType[0], mimeType)) {
		System.out.println("NodeType: " + formatType[1]);
		//                    if (mimeType.equals("image/jpeg")) {
		return formatType[1];
	    }
	}
	return null;
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
		return getNodeTypeFromMimeType(currentArbilNode.mpiMimeType);
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
    @Override
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
				addedNodePath = insertFromTemplate(destinationNode.getNodeTemplate(), destinationNode.getURI(), destinationNode.getSubDirectory(), nodeType, targetXmlPath, nodDom, resourceUri, mimeType);
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

    @Override
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
	    templateUrl = CmdiMetadataBuilder.class.getResource("/nl/mpi/arbil/resources/templates/" + templateType.substring(1) + ".xml");
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

    /**
     * Checks whether the component builder will be able to insert a node of
     * specified type in the specified target DOM
     */
    private boolean canInsertFromTemplate(ArbilTemplate currentTemplate, String elementName, String targetXmlPath, Document targetImdiDom) throws ArbilMetadataException {
	// This may be done more efficiently, but for now we basically prepare
	// an insertion up to the point we have the destination node and
	// potentially addable node which we can pass to the component builder
	// and ask whether this can actually be done.

	int maxOccurs = currentTemplate.getMaxOccursForTemplate(elementName);
	if (maxOccurs < 0) {
	    return true;
	}

	URI addedPathURI = null;
	try {
	    String templateFileString = templateFileStringFromElementName(elementName);
	    URL templateUrl = urlForTemplateFile(currentTemplate, templateFileString);
	    String targetRef = xPathFromXmlPath(targetXmlPath, elementName);
	    String targetXpath = xPathFromTargetRef(targetRef);

	    if (templateUrl != null) {
		Document insertableSectionDoc = ArbilComponentBuilder.getDocument(templateUrl.toURI());

		if (insertableSectionDoc != null) {
		    Node insertableNode = org.apache.xpath.XPathAPI.selectSingleNode(insertableSectionDoc, "/:InsertableSection/:*");
		    if (insertableNode != null) {
			Node addableNode = targetImdiDom.importNode(insertableNode, true);
			Node destinationNode = org.apache.xpath.XPathAPI.selectSingleNode(targetImdiDom, targetXpath);

			return ArbilComponentBuilder.canInsertNode(destinationNode, addableNode, maxOccurs);
		    }
		}
	    }
	} catch (URISyntaxException ex) {
	    BugCatcherManager.getBugCatcher().logError(ex);
	} catch (MalformedURLException ex) {
	    BugCatcherManager.getBugCatcher().logError(ex);
	} catch (DOMException exception) {
	    BugCatcherManager.getBugCatcher().logError(exception);
	} catch (IOException exception) {
	    BugCatcherManager.getBugCatcher().logError(exception);
	} catch (ParserConfigurationException exception) {
	    BugCatcherManager.getBugCatcher().logError(exception);
	} catch (SAXException exception) {
	    BugCatcherManager.getBugCatcher().logError(exception);
	} catch (TransformerException exception) {
	    BugCatcherManager.getBugCatcher().logError(exception);
	}
	System.out.println("addedPathString: " + addedPathURI);
	return false;
    }

    private static String xPathFromTargetRef(String targetRef) {
	String targetXpath = targetRef;
	// convert to xpath for the api
	targetXpath = targetXpath.replace(".", "/:");
	targetXpath = targetXpath.replace(")", "]");
	targetXpath = targetXpath.replace("(", "[");
	return targetXpath;
    }

    private static String xPathFromXmlPath(String targetXmlPath, String elementName) {
	// prepare the parent node
	String targetXpath = targetXmlPath;
	if (targetXpath == null) {
	    targetXpath = elementName;
	} else {
	    // make sure we have a complete path
	    // for instance METATRANSCRIPT.Session.MDGroup.Actors.Actor(x).Languages.Language
	    // requires /:METATRANSCRIPT/:Session/:MDGroup/:Actors/:Actor[6].Languages
	    // not /:METATRANSCRIPT/:Session/:MDGroup/:Actors/:Actor[6]
	    // the last path component (.Language) will be removed later
	    String[] targetXpathArray = targetXpath.split("\\)");
	    String[] elementNameArray = elementName.split("\\)");
	    StringBuilder targetXpathSB = new StringBuilder();
	    for (int partCounter = 0; partCounter < elementNameArray.length; partCounter++) {
		if (targetXpathArray.length > partCounter) {
		    targetXpathSB.append(targetXpathArray[partCounter]);
		} else {
		    targetXpathSB.append(elementNameArray[partCounter]);
		}
		targetXpathSB.append(')');
	    }
	    targetXpath = targetXpathSB.toString().replaceAll("\\)$", "");
	}
	targetXpath = targetXpath.substring(0, targetXpath.lastIndexOf("."));
	return targetXpath;
    }

    private static URL urlForTemplateFile(ArbilTemplate currentTemplate, String templateFileString) throws MalformedURLException {
	URL templateUrl;
	File templateFile = new File(currentTemplate.getTemplateComponentDirectory(), templateFileString + ".xml");
	System.out.println("templateFile: " + templateFile.getAbsolutePath());
	if (templateFile.exists()) {
	    templateUrl = templateFile.toURI().toURL();
	} else {
	    templateUrl = CmdiMetadataBuilder.class.getResource("/nl/mpi/arbil/resources/templates/" + templateFileString + ".xml");
	}
	return templateUrl;
    }

    private static String templateFileStringFromElementName(String elementName) {
	String templateFileString = elementName.substring(1); //TODO: this level of path change should not be done here but in the original caller
	System.out.println("templateFileString: " + templateFileString);
	templateFileString = templateFileString.replaceAll("\\(\\d*?\\)", "(x)");
	System.out.println("templateFileString(x): " + templateFileString);
	templateFileString = templateFileString.replaceAll("\\(x\\)$", "");
	return templateFileString;
    }

    private URI insertFromTemplate(ArbilTemplate currentTemplate, URI targetMetadataUri, File resourceDestinationDirectory, String elementName, String targetXmlPath, Document targetImdiDom, URI resourceUrl, String mimeType) throws ArbilMetadataException {
	System.out.println("insertFromTemplate: " + elementName + " : " + resourceUrl);
	System.out.println("targetXpath: " + targetXmlPath);
	String insertBefore = currentTemplate.getInsertBeforeOfTemplate(elementName);
	System.out.println("insertBefore: " + insertBefore);
	final int maxOccurs = currentTemplate.getMaxOccursForTemplate(elementName);
	System.out.println("maxOccurs: " + maxOccurs);
	URI addedPathURI = null;
	try {
	    String templateFileString = templateFileStringFromElementName(elementName);
	    URL templateUrl = urlForTemplateFile(currentTemplate, templateFileString);
	    String targetRef = xPathFromXmlPath(targetXmlPath, elementName);
	    String targetXpath = xPathFromTargetRef(targetRef);

	    System.out.println("targetXpath: " + targetXpath);
	    System.out.println("templateUrl: " + templateUrl);

	    if (templateUrl == null) {
		messageDialogHandler.addMessageDialogToQueue("No template found for: " + elementName.substring(1), "Load Template");
		BugCatcherManager.getBugCatcher().logError(new Exception("No template found for: " + elementName.substring(1)));
	    } else {
		Document insertableSectionDoc = ArbilComponentBuilder.getDocument(templateUrl.toURI());

		if (insertableSectionDoc == null) {
		    messageDialogHandler.addMessageDialogToQueue("Error reading template", "Insert from Template");
		} else {
		    // insert values into the section that about to be added
		    if (resourceUrl != null) {
			insertValuesForAddingFromTemplate(insertableSectionDoc, resourceUrl, resourceDestinationDirectory, targetMetadataUri);
		    }
		    if (mimeType != null) {
			insertMimeTypeForAddingFromTemplate(insertableSectionDoc, mimeType);
		    }

		    Node insertableNode = org.apache.xpath.XPathAPI.selectSingleNode(insertableSectionDoc, "/:InsertableSection/:*");
		    if (insertableNode == null) {
			BugCatcherManager.getBugCatcher().logError(new Exception("InsertableSection not found in the template"));
		    }
		    return importNodesAddedFromTemplate(targetImdiDom, targetMetadataUri, targetXpath, targetRef, insertableNode, insertBefore, maxOccurs);
		}
	    }
	} catch (URISyntaxException ex) {
	    BugCatcherManager.getBugCatcher().logError(ex);
	} catch (MalformedURLException ex) {
	    BugCatcherManager.getBugCatcher().logError(ex);
	} catch (DOMException exception) {
	    BugCatcherManager.getBugCatcher().logError(exception);
	    return null;
	} catch (IOException exception) {
	    BugCatcherManager.getBugCatcher().logError(exception);
	    return null;
	} catch (ParserConfigurationException exception) {
	    BugCatcherManager.getBugCatcher().logError(exception);
	    return null;
	} catch (SAXException exception) {
	    BugCatcherManager.getBugCatcher().logError(exception);
	    return null;
	} catch (TransformerException exception) {
	    BugCatcherManager.getBugCatcher().logError(exception);
	    return null;
	}
	System.out.println("addedPathString: " + addedPathURI);
	return addedPathURI;
    }

    private URI importNodesAddedFromTemplate(Document targetImdiDom, URI targetMetadataUri, String targetXpath, String targetRef, Node insertableNode, String insertBefore, final int maxOccurs) throws URISyntaxException, DOMException, ArbilMetadataException, TransformerException {
	Node addableNode = targetImdiDom.importNode(insertableNode, true);
	Node destinationNode = org.apache.xpath.XPathAPI.selectSingleNode(targetImdiDom, targetXpath);
	Node addedNode = ArbilComponentBuilder.insertNodeInOrder(destinationNode, addableNode, insertBefore, maxOccurs);
	String nodeFragment = ArbilComponentBuilder.convertNodeToNodePath(targetImdiDom, addedNode, targetRef);
	//                            try {
	System.out.println("nodeFragment: " + nodeFragment);
	// return the child node url and path in the xml
	// first strip off any fragment then add the full node fragment
	return new URI(targetMetadataUri.toString().split("#")[0] + "#" + nodeFragment);
    }

    private void insertMimeTypeForAddingFromTemplate(Document insertableSectionDoc, String mimeType) throws DOMException, TransformerException {
	if (mimeType.equals("image/jpeg")) {
	    // TODO: consider replacing this with exif imdifields in the original imdiobject and doing a merge
	    //                    Hashtable exifTags = getExifMetadata(resourcePath);
	    //                    String dateExifTag = "date";
	    //                    if (exifTags.contains(dateExifTag)) {
	    //                        Node linkNode = org.apache.xpath.XPathAPI.selectSingleNode(insertableSectionDoc, "/:InsertableSection/:MediaFile/:Date");
	    //                        linkNode.setTextContent(exifTags.get(dateExifTag).toString());
	    //                    }
	}
	Node linkNode = org.apache.xpath.XPathAPI.selectSingleNode(insertableSectionDoc, "/:InsertableSection/:*/:Format");
	linkNode.setTextContent(mimeType);
    }

    private void insertValuesForAddingFromTemplate(Document insertableSectionDoc, URI resourceUrl, File resourceDestinationDirectory, URI targetMetadataUri) throws UnsupportedEncodingException, DOMException, TransformerException {
	URI finalResourceUrl = resourceUrl;
	//                    String localFilePath = resourcePath; // will be changed when copied to the cache
	// copy the file to the imdi directory
	try {
	    if (sessionStorage.getOptions().isCopyNewResourcesToCache()) {
		//                            URL resourceUrl = new URL(resourcePath);
		//                    String resourcesDirName = "resources";
		File originalFile = new File(resourceUrl);
		int suffixIndex = originalFile.getName().lastIndexOf(".");
		String targetFilename = originalFile.getName().substring(0, suffixIndex);
		String targetSuffix = originalFile.getName().substring(suffixIndex);
		System.out.println("targetFilename: " + targetFilename + " targetSuffix: " + targetSuffix);
		///////////////////////////////////////////////////////////////////////
		// use the nodes child directory
		File destinationFileCopy = new File(resourceDestinationDirectory, targetFilename + targetSuffix);
		int fileCounter = 0;
		while (destinationFileCopy.exists()) {
		    fileCounter++;
		    destinationFileCopy = new File(resourceDestinationDirectory, targetFilename + "(" + fileCounter + ")" + targetSuffix);
		}
		URI fullURI = destinationFileCopy.toURI();
		finalResourceUrl = targetMetadataUri.relativize(fullURI);
		//destinationFileCopy.getAbsolutePath().replace(destinationFile.getParentFile().getPath(), "./").replace("\\", "/").replace("//", "/");
		// for easy reading in the fields keep the file in the same directory
		//                        File destinationDirectory = new File(destinationFile.getParentFile().getPath());
		//                        File destinationFileCopy = File.createTempFile(targetFilename, targetSuffix, destinationDirectory);
		//                        localFilePath = "./" + destinationFileCopy.getName();
		///////////////////////////////////////////////////////////////////////
		copyToDisk(resourceUrl.toURL(), destinationFileCopy);
		System.out.println("destinationFileCopy: " + destinationFileCopy.toString());
	    }
	} catch (Exception ex) {
	    //localFilePath = resourcePath; // link to the original file
	    BugCatcherManager.getBugCatcher().logError(ex);
	}
	Node linkNode = org.apache.xpath.XPathAPI.selectSingleNode(insertableSectionDoc, "/:InsertableSection/:*/:ResourceLink");
	String decodeUrlString = URLDecoder.decode(finalResourceUrl.toString(), "UTF-8");
	linkNode.setTextContent(decodeUrlString);
    }

    private static URI copyToDisk(URL sourceURL, File targetFile) {
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
