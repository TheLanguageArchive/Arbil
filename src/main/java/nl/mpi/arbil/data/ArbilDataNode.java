package nl.mpi.arbil.data;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import nl.mpi.arbil.ArbilIcons;
import nl.mpi.arbil.ArbilMetadataException;
import nl.mpi.arbil.clarin.CmdiComponentLinkReader;
import nl.mpi.arbil.data.metadatafile.CmdiUtils;
import nl.mpi.arbil.data.metadatafile.ImdiUtils;
import nl.mpi.arbil.data.metadatafile.MetadataReader;
import nl.mpi.arbil.data.metadatafile.MetadataUtils;
import nl.mpi.arbil.templates.ArbilTemplate;
import nl.mpi.arbil.templates.ArbilTemplateManager;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ArrayComparator;
import nl.mpi.arbil.util.BugCatcher;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.MimeHashQueue;
import nl.mpi.arbil.util.MimeHashQueue.TypeCheckerState;
import nl.mpi.arbil.util.TreeHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Document : ArbilDataNode formerly known as ImdiTreeObject
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilDataNode extends ArbilNode implements Comparable {

    public MetadataUtils metadataUtils;
    public ArbilTemplate nodeTemplate;
    private Hashtable<String, ArbilField[]> fieldHashtable; //// TODO: this should be changed to a vector or contain an array so that duplicate named fields can be stored ////
    private ArbilDataNode[] childArray = new ArbilDataNode[0];
    private boolean dataLoaded;
    public int resourceFileServerResponse = -1; // -1 = not set otherwise this will be the http response code
    public String hashString;
    public String mpiMimeType = null;
    public String typeCheckerMessage;
    private TypeCheckerState typeCheckerState = TypeCheckerState.UNCHECKED;
    public int matchesInCache;
    public int matchesRemote;
    public int matchesLocalFileSystem;
    public boolean fileNotFound;
    public boolean isInfoLink = false;
    private boolean singletonMetadataNode = false;
    private boolean nodeNeedsSaveToDisk;
    private String nodeText, lastNodeText = NODE_LOADING_TEXT;
    //    private boolean nodeTextChanged = false;
    private URI nodeUri;
    private boolean containerNode = false;
    public ArbilField resourceUrlField;
    public CmdiComponentLinkReader cmdiComponentLinkReader = null;
    public boolean isDirectory;
    private ImageIcon icon;
    private boolean nodeEnabled;
    public boolean hasSchemaError = false;
    // merge to one array of domid url ArbilDataNode
    private String[][] childLinks = new String[0][0]; // each element in this array is an array [linkPath, linkId]. When the link is from an imdi the id will be the node id, when from get links or list direcotry id will be null    
    private int isLoadingCount = 0;
    final private Object loadingCountLock = new Object();
    @Deprecated
    public boolean lockedByLoadingThread = false;
    //    private boolean isFavourite;
    public String archiveHandle = null;
    public boolean hasDomIdAttribute = false; // used to requre a save (that will remove the dom ids) if a node has any residual dom id attributes
    //    public boolean autoLoadChildNodes = false;
    //public Vector<String[]> addQueue;
    public boolean scrollToRequested = false;
    //    public Vector<ImdiTreeObject> mergeQueue;
    //    public boolean jumpToRequested = false; // dubious about this being here but it seems to fit here best
    private ArbilDataNode domParentNode = null; // the parent imdi containing the dom, only set for imdi child nodes
    //public String xmlNodeId = null; // only set for imdi child nodes and is the xml node id relating to this imdi tree object
    public File thumbnailFile = null;
    private final Object domLockObjectPrivate = new Object();
    private final static String NODE_LOADING_TEXT = "loading node...";
    private static MessageDialogHandler messageDialogHandler;

    public static void setMessageDialogHandler(MessageDialogHandler handler) {
	messageDialogHandler = handler;
    }
    private static BugCatcher bugCatcher;

    public static void setBugCatcher(BugCatcher bugCatcherInstance) {
	bugCatcher = bugCatcherInstance;
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
    private static MimeHashQueue mimeHashQueue;

    public static void setMimeHashQueue(MimeHashQueue mimeHashQueueInstance) {
	mimeHashQueue = mimeHashQueueInstance;
    }

    protected ArbilDataNode(URI localUri) {
	super();
	//        addQueue = new Vector<String[]>();
	nodeUri = localUri;
	if (nodeUri != null) {
	    metadataUtils = ArbilDataNode.getMetadataUtils(nodeUri.toString());
	}
	initNodeVariables();
    }

    // set the node text only if it is null
    public void setNodeText(String localNodeText) {
	if (nodeText == null) {
	    nodeText = localNodeText;
	}
    }

    // TODO: this is not used yet but may be required for unicode paths
    private static String urlEncodePath(String inputPath) {
	// url encode the path elements
	String encodedString = null;
	try {
	    for (String inputStringPart : inputPath.split("/")) {
		//                    System.out.println("inputStringPart: " + inputStringPart);
		if (encodedString == null) {
		    encodedString = URLEncoder.encode(inputStringPart, "UTF-8");
		} else {
		    encodedString = encodedString + "/" + URLEncoder.encode(inputStringPart, "UTF-8");
		}
	    }
	} catch (Exception ex) {
	    bugCatcher.logError(ex);
	}
	return encodedString;
    }

    static public URI conformStringToUrl(String inputUrlString) {
	try {
	    //            localUrlString = localUrlString.replace("\\", "/");
	    if (!inputUrlString.toLowerCase().startsWith("http") && !inputUrlString.toLowerCase().startsWith("file:") && !inputUrlString.toLowerCase().startsWith(".")) {
		return new File(inputUrlString).toURI();
	    } else {
		// apache method
		//                URI tempURI = new URI(inputUrlString);
		//                URI returnURI = URIUtils.createURI(tempURI.getScheme(), tempURI.getHost(), tempURI.getPort(), tempURI.getPath(), tempURI.getQuery(), tempURI.getFragment());
		//                return returnURI;
		// end apache method : this requires the uri to be broken into its parts so we might as well do it with the standard classes
		// mpi method
		//                URI returnURI = URIUtil.newURI(inputUrlString);
		// end mpi method : this will url encode the # etc. and therefore loose the fragment and other parts
		//                boolean isUncPath = inputUrlString.toLowerCase().startsWith("file:////");
		//                if (isUncPath) {
		//                    try {
		//                        returnURI = new URI("file:////" + returnURI.toString().substring("file:/".length()));
		//                    } catch (URISyntaxException urise) {
		//                       .logError(urise);
		//                    }
		//                }

		// separate the path and protocol
		int protocolEndIndex;
		if (inputUrlString.startsWith(".")) {
		    // TODO: this is un tested for ./ paths, but at this stage it appears unlikey to ever be needed
		    protocolEndIndex = 0;
		} else {
		    protocolEndIndex = inputUrlString.indexOf(":/");
		}
		//                while (inputUrlString.charAt(protocolEndIndex) == '/') {
		//                    protocolEndIndex++;
		//                }
		String protocolComponent = inputUrlString.substring(0, protocolEndIndex);
		String remainingComponents = inputUrlString.substring(protocolEndIndex + 1);
		String[] pathComponentArray = remainingComponents.split("#");
		String pathComponent = pathComponentArray[0];
		String fragmentComponent = null;
		if (pathComponentArray.length > 1) {
		    fragmentComponent = pathComponentArray[1];
		}
		// note that this must be done as separate parameters not a single string otherwise it will not get url encoded
		// TODO: this could require the other url components to be added here
		return new URI(protocolComponent, pathComponent, fragmentComponent);
		//                System.out.println("returnUrl: " + returnUrl);
		////                int protocolEndIndex = inputUrlString.lastIndexOf("/", "xxxx:".length());

		//                String pathComponentEncoded = URLEncoder.encode(pathComponent, "UTF-8");
		//                returnUrl = new URI(protocolComponent + pathComponentEncoded);
		//                System.out.println("returnUrl: " + returnUrl);
	    }
	    //            // if the imdi api finds only one / after the file: it will interpret the url as relative and make a bit of a mess of it, so we have to make sure that we have two for the url and one for the root
	    //            if (returnUrl.toString().toLowerCase().startsWith("file:") && !returnUrl.toString().toLowerCase().startsWith("file:///")) {
	    //                // here we assume that this application does not use relative file paths
	    //                returnUrl = new URL("file", "", "//" + returnUrl.getPath());
	    //            }
	    //            System.out.println("conformStringToUrl URI: " + new URI(returnUrl.toString()));
	} catch (Exception ex) {
	    bugCatcher.logError(ex);
	    return null;
	}
	//        System.out.println("conformStringToUrl out: " + returnUrl.toString());
    }

    static public URI normaliseURI(URI inputURI) {
	//        System.out.println("normaliseURI: " + inputURI);
	boolean isUncPath = inputURI.toString().toLowerCase().startsWith("file:////");
	URI returnURI = inputURI.normalize();
	if (isUncPath) {
	    try {
		// note that this must use the single string parameter to prevent re url encoding
		returnURI = new URI("file:////" + returnURI.toString().substring("file:/".length()));
	    } catch (URISyntaxException urise) {
		bugCatcher.logError(urise);
	    }
	}
	return returnURI;
    }
    // static methods for testing imdi file and object types

    static public boolean isArbilDataNode(Object unknownObj) {
	if (unknownObj == null) {
	    return false;
	}
	return (unknownObj instanceof ArbilDataNode);
    }

    static public boolean isStringLocal(String urlString) {
	return (!urlString.startsWith("http")); // todo: should this test for "file" instead, in the case of ftp this will fail and cause a null pointer in pathIsInFavourites
    }

    static public boolean isPathHistoryFile(String urlString) {
	return MetadataFormat.isPathMetadata(urlString.replaceAll("mdi.[0-9]*$", "mdi"));
    }

    static public boolean isPathMetadata(String urlString) {
	return isPathImdi(urlString) || isPathCmdi(urlString); // change made for clarin
    }

    static public boolean isPathImdi(String urlString) {
	return urlString.endsWith(".imdi");
    }

    static public boolean isPathCmdi(String urlString) {
	return urlString.endsWith(".cmdi");
    }

    static public boolean isStringChildNode(String urlString) {
	return urlString.contains("#."); // anything with a fragment is a sub node //urlString.contains("#.METATRANSCRIPT") || urlString.contains("#.CMD"); // change made for clarin
    }

    static public MetadataUtils getMetadataUtils(String urlString) {
	if (MetadataFormat.isPathCmdi(urlString)) {
	    return new CmdiUtils();
	} else if (MetadataFormat.isPathImdi(urlString)) {
	    return new ImdiUtils();
	}
	return null;
    }
    // end static methods for testing imdi file and object types

    public boolean getNeedsSaveToDisk(boolean onlyOfSubNode) {
	// when the dom parent node is saved all the sub nodes are also saved so we need to clear this flag
	if (nodeNeedsSaveToDisk && !this.getParentDomNode().nodeNeedsSaveToDisk) {
	    nodeNeedsSaveToDisk = false;
	}
	if (onlyOfSubNode) {
	    return nodeNeedsSaveToDisk;
	} else {
	    return this.getParentDomNode().nodeNeedsSaveToDisk;
	}
    }

    public boolean hasChangedFields() {
	for (ArbilField[] currentFieldArray : this.fieldHashtable.values()) {
	    for (ArbilField currentField : currentFieldArray) {
		if (currentField.fieldNeedsSaveToDisk()) {
		    return true;
		}
	    }
	}
	return false;
    }

    /**
     * Searches for pending changes in this node or one of its subnodes
     *
     * @return Whether this node or any of its descendants has changed fields
     * @see hasChangedFields()
     */
    public boolean hasChangedFieldsInSubtree() {
	if (hasChangedFields()) {
	    return true;
	} else {
	    for (ArbilDataNode child : getChildArray()) {
		if (child.hasChangedFieldsInSubtree()) {
		    return true;
		}
	    }
	}
	return false;
    }

    public void setDataNodeNeedsSaveToDisk(ArbilField originatingField, boolean updateUI) {
	if (resourceUrlField != null && resourceUrlField.equals(originatingField)) {
	    hashString = null;
	    mpiMimeType = null;
	    thumbnailFile = null;
	    typeCheckerMessage = null;
	    mimeHashQueue.addToQueue(this);
	}
	boolean needsSaveToDisk = hasChangedFields() || hasDomIdAttribute;
	if (isMetaDataNode() && !isChildNode()) {
	    if (needsSaveToDisk == false) {
		for (ArbilDataNode childNode : getAllChildren()) {
		    if (childNode.nodeNeedsSaveToDisk) {
			needsSaveToDisk = true;
		    }
		}
	    }
	    if (this.nodeNeedsSaveToDisk != needsSaveToDisk) {
		if (needsSaveToDisk) {
		    dataNodeLoader.addNodeNeedingSave(this);
		} else {
		    dataNodeLoader.removeNodesNeedingSave(this);
		}
		this.nodeNeedsSaveToDisk = needsSaveToDisk;
	    }
	} else {
	    this.nodeNeedsSaveToDisk = needsSaveToDisk; // this must be set before setImdiNeedsSaveToDisk is called
	    this.getParentDomNode().setDataNodeNeedsSaveToDisk(null, updateUI);
	}
	if (updateUI) {
	    this.clearIcon();
	}
    }

    public String getAnyMimeType() {
	if (mpiMimeType == null && hasResource()) { // use the format from the imdi file if the type checker failed eg if the file is on the server
	    ArbilField[] formatField = fieldHashtable.get("Format");
	    if (formatField != null && formatField.length > 0) {
		return formatField[0].getFieldValue();
	    }
	}
	return mpiMimeType;
    }

    public void setMimeType(String[] typeCheckerMessageArray) {
	mpiMimeType = typeCheckerMessageArray[0];
	typeCheckerMessage = typeCheckerMessageArray[1];
	if (!isMetaDataNode() && isLocal() && mpiMimeType != null) {
	    // add the mime type for loose files
	    ArbilField mimeTypeField = new ArbilField(fieldHashtable.size(), this, "Format", this.mpiMimeType, 0, false, null, null);
	    //            mimeTypeField.fieldID = "x" + fieldHashtable.size();
	    addField(mimeTypeField);
	}
    }

    private String getNodeTypeNameFromUriFragment(String nodeFragmentName) {
	if (nodeFragmentName == null) {
	    return null;
	}
	nodeFragmentName = nodeFragmentName.substring(nodeFragmentName.lastIndexOf(".") + 1);
	nodeFragmentName = nodeFragmentName.replaceAll("\\(\\d+\\)", "");
	return nodeFragmentName;
    }

    private void initNodeVariables() {
	// loop any indichildnodes and init
	if (childArray != null) {
	    for (ArbilDataNode currentNode : childArray) {
		if (currentNode.isChildNode()) {
		    currentNode.initNodeVariables();
		}
	    }
	}
	//        if (currentTemplate == null) {
	//            // this will be overwritten when the imdi file is read, provided that a template is specified in the imdi file
	//            if (isPathCmdi(nodeUri.getPath())) {
	//                // this must be loaded with the name space uri
	//                //   currentTemplate = ArbilTemplateManager.getSingleInstance().getCmdiTemplate();
	//            } else {
	//                currentTemplate = ArbilTemplateManager.getSingleInstance().getCurrentTemplate();
	//            }
	//        }
	fieldHashtable = new Hashtable<String, ArbilField[]>();
	dataLoaded = false;
	hashString = null;
	//mpiMimeType = null;
	matchesInCache = 0;
	matchesRemote = 0;
	matchesLocalFileSystem = 0;
	fileNotFound = false;
	nodeNeedsSaveToDisk = false;
	//    nodeText = null;
	//    urlString = null;
	//        resourceUrlField = null;
	isDirectory = false;
	icon = null;
	nodeEnabled = true;
	singletonMetadataNode = false;
	containerNode = false;
	//        isLoadingCount = true;
	if (nodeUri != null) {
	    if (!isMetaDataNode() && isLocal()) {
		File fileObject = getFile();
		if (fileObject != null) {
		    this.nodeText = fileObject.getName();
		    this.isDirectory = fileObject.isDirectory();
		    // TODO: check this on a windows box with a network drive and linux with symlinks
		    //                    this.isDirectory = !fileObject.isFile();
		    //                    System.out.println("isFile" + fileObject.isFile());
		    //                    System.out.println("isDirectory" + fileObject.isDirectory());
		    //                    System.out.println("getAbsolutePath" + fileObject.getAbsolutePath());
		}
	    }
	    if (!isMetaDataNode() && nodeText == null) {
		nodeText = this.getUrlString();
	    }
	}
    }

    public void reloadNode() {
	System.out.println("reloadNode: " + isLoading());
	getParentDomNode().nodeNeedsSaveToDisk = false; // clear any changes
	//        if (!this.isImdi()) {
	//            initNodeVariables();
	//            //loadChildNodes();
	//            clearIcon();
	//            // TODO: this could just remove the decendant nodes and let the user re open them
	//            ArbilTreeHelper.getSingleInstance().updateTreeNodeChildren(this);
	////            this.clearIcon();
	//        } else {
	////            if (getParentDomNode().isCorpus()) {
	////                getParentDomNode().autoLoadChildNodes = true;
	////            }
	dataNodeLoader.requestReload(getParentDomNode());
	//        }
    }

    public void loadArbilDom() {
	if (getParentDomNode() != this) {
	    getParentDomNode().loadArbilDom();
	} else {
	    synchronized (getParentDomLockObject()) {
		initNodeVariables(); // this might be run too often here but it must be done in the loading thread and it also must be done when the object is created
		if (!isMetaDataNode() && !isDirectory() && isLocal()) {
		    // if it is an not imdi or a loose file but not a direcotry then get the md5sum
		    mimeHashQueue.addToQueue(this);
		    dataLoaded = true;
		}
		if (this.isDirectory()) {
		    getDirectoryLinks();
		    dataLoaded = true;
		    //            clearIcon();
		}
		if (isMetaDataNode()) {
		    loadMetadataDom();
		    dataLoaded = true;
		}
	    }
	}
    }

    private void loadMetadataDom() {
	if (this.isLocal() && !this.getFile().exists() && new File(this.getFile().getAbsolutePath() + ".0").exists()) {
	    // if the file is missing then try to find a valid history file
	    copyLastHistoryToCurrent();
	    messageDialogHandler.addMessageDialogToQueue("Missing file has been recovered from the last history item.", "Recover History");
	}
	try {
	    //set the string name to unknown, it will be updated in the tostring function
	    nodeText = "unknown";
	    initComponentLinkReader();
	    updateMetadataChildNodes();
	} catch (Exception mue) {
	    bugCatcher.logError(this.getUrlString(), mue);
	    //            System.out.println("Invalid input URL: " + mue);
	    File nodeFile = this.getFile();
	    if (nodeFile != null && nodeFile.exists()) {
		nodeText = "Could not load data";
	    } else {
		nodeText = "File not found";
		fileNotFound = true;
	    }
	}
    }

    private void initComponentLinkReader() {
	if (this.isCmdiMetaDataNode()) {
	    // load the links from the cmdi file
	    // the links will be hooked to the relevent nodes when the rest of the xml is read
	    cmdiComponentLinkReader = new CmdiComponentLinkReader();
	    cmdiComponentLinkReader.readLinks(this.getURI());
	} else {
	    cmdiComponentLinkReader = null;
	}
    }

    private void updateMetadataChildNodes() throws ParserConfigurationException, SAXException, IOException, TransformerException, ArbilMetadataException {
	Document nodDom = ArbilComponentBuilder.getDocument(this.getURI());
	Hashtable<ArbilDataNode, HashSet<ArbilDataNode>> parentChildTree = new Hashtable<ArbilDataNode, HashSet<ArbilDataNode>>();
	childLinks = loadMetadataChildNodes(nodDom, parentChildTree);
	checkRemovedChildNodes(parentChildTree);
    }

    private String[][] loadMetadataChildNodes(Document nodDom, Hashtable<ArbilDataNode, HashSet<ArbilDataNode>> parentChildTree) throws TransformerException, ArbilMetadataException {
	Vector<String[]> childLinks = new Vector<String[]>();
	Hashtable<String, Integer> siblingNodePathCounter = new Hashtable<String, Integer>();
	// get the metadata format information required to read this nodes metadata
	final String metadataStartPath = MetadataFormat.getMetadataStartPath(nodeUri.getPath());
	String fullNodePath = "";
	Node startNode = nodDom.getFirstChild();
	if (metadataStartPath.length() > 0) {
	    fullNodePath = metadataStartPath.substring(0, metadataStartPath.lastIndexOf("."));
	    final String metadataXpath = metadataStartPath.replaceAll("\\.", "/:"); //"/:Kinnate/:Entity";
	    final Node metadataNode = org.apache.xpath.XPathAPI.selectSingleNode(startNode, metadataXpath);
	    // if this fails then we probably want to fail the reading of the node
	    if (metadataNode == null) {
		throw new ArbilMetadataException("Failed to find the start node for the metadata to read: " + fullNodePath);
	    }
	    startNode = metadataNode;
	}
	// load the fields from the imdi file
	MetadataReader.getSingleInstance().iterateChildNodes(this, childLinks, startNode, fullNodePath, fullNodePath, parentChildTree, siblingNodePathCounter, 0);
	if (isCmdiMetaDataNode()) {
	    // Add all links that have no references to the root node (might confuse users but at least it will show what's going on)
	    MetadataReader.getSingleInstance().addUnreferencedResources(this, parentChildTree, childLinks);
	}
	return childLinks.toArray(new String[][]{});
    }

    private void checkRemovedChildNodes(Hashtable<ArbilDataNode, HashSet<ArbilDataNode>> parentChildTree) {
	//ImdiTreeObject[] childArrayTemp = new ImdiTreeObject[childLinks.length];
	for (Entry<ArbilDataNode, HashSet<ArbilDataNode>> entry : parentChildTree.entrySet()) {
	    ArbilDataNode currentNode = entry.getKey();
	    // System.out.println("setting childArray on: " + currentNode.getUrlString());
	    // save the old child array
	    ArbilDataNode[] oldChildArray = currentNode.childArray;
	    // set the new child array
	    currentNode.childArray = parentChildTree.get(currentNode).toArray(new ArbilDataNode[]{});
	    // check the old child array and for each that is no longer in the child array make sure they are removed from any containers (tables or trees)
	    List currentChildList = Arrays.asList(currentNode.childArray);
	    for (ArbilDataNode currentOldChild : oldChildArray) {
		if (currentChildList.indexOf(currentOldChild) == -1) {
		    // remove from any containers that its found in
		    for (ArbilDataNodeContainer currentContainer : currentOldChild.getRegisteredContainers()) {
			currentContainer.dataNodeRemoved(currentOldChild);
		    }
		}
	    }
	}
    }

    private void getDirectoryLinks() {
	File[] dirLinkArray = null;
	File nodeFile = this.getFile();
	if (nodeFile != null && nodeFile.isDirectory()) {
	    dirLinkArray = nodeFile.listFiles();
	    Vector<ArbilDataNode> childLinksTemp = new Vector<ArbilDataNode>();
	    for (int linkCount = 0; linkCount < dirLinkArray.length; linkCount++) {
		try {
		    //                    System.out.println("nodeFile: " + nodeFile);
		    //                    System.out.println("dirLinkArray[linkCount]: " + dirLinkArray[linkCount]);
		    URI childURI = dirLinkArray[linkCount].toURI();
		    ArbilDataNode currentNode = dataNodeLoader.getArbilDataNodeWithoutLoading(childURI);
		    if (treeHelper.isShowHiddenFilesInTree() || !currentNode.getFile().isHidden()) {
			childLinksTemp.add(currentNode);
		    }
		} catch (Exception ex) {
		    messageDialogHandler.addMessageDialogToQueue(dirLinkArray[linkCount] + " could not be loaded in\n" + nodeUri.toString(), "Load Directory");
		    bugCatcher.logError(ex);
		}
	    }
	    //childLinks = childLinksTemp.toArray(new String[][]{});
	    childArray = childLinksTemp.toArray(new ArbilDataNode[]{});
	}
    }

    //    private void getImdiLinks(Document nodDom) {
    //        try {
    //            if (nodDom != null) {
    //                OurURL baseURL = new OurURL(nodeUri.toURL());
    ////                debugOut("getIMDILinks");
    //                IMDILink[] links = api.getIMDILinks(nodDom, baseURL, WSNodeType.CORPUS);
    ////                debugOut("links.length: " + links.length);
    //                if (links != null) {
    //                    for (int linkCount = 0; linkCount < links.length; linkCount++) {
    //                        childLinks.add(new String[]{links[linkCount].getRawURL().toString(), null});
    //                    }
    //                }
    //            }
    //        } catch (MalformedURLException mue) {
    //            System.out.println("Error getting links: " + mue);
    //        }
    //    }
    //        private boolean populateChildFields(String fieldNameString, boolean alwaysShow) {
    //            // this is called when loading children and when loading fields
    //            //System.out.println("fieldNameString: " + fieldNameString);
    //            boolean valueFound = false;
    //            int counterFieldPosition = fieldNameString.indexOf("(X)");
    //            if (-1 < counterFieldPosition) {
    //                int itemValueCounter = 1;
    //                valueFound = true;
    //                String firstHalf = fieldNameString.substring(0, counterFieldPosition + 1);
    //                String secondHalf = fieldNameString.substring(counterFieldPosition + 2);
    //                while (valueFound) {
    //                    fieldNameString = firstHalf + itemValueCounter + secondHalf;
    //                    if (-1 < fieldNameString.indexOf("(X)")) {
    //                        valueFound = populateChildFields(fieldNameString, alwaysShow);
    //                    } else {
    //                        boolean isWrongFieldType = false;
    //                        if (isImdi()) {
    //                            if (isSession() && fieldNameString.startsWith("Corpus.")) {
    //                                // TODO: we could speed things up by not asking the imdi.api for the value of this field, however if there is data so show (presumably erroneous data) it should still be shown
    //                                isWrongFieldType = true;
    //                            } else if (fieldNameString.startsWith("Session.")) {
    //                                isWrongFieldType = true;
    //                            }
    //                        }
    //                        //System.out.println("checking x value for: " + fieldNameString);
    //                        String cellValue = this.getField(fieldNameString);
    //                        valueFound = cellValue != null;
    //                        if (valueFound && cellValue.length() > 0) {
    //                            this.addField(fieldNameString, 0, cellValue);
    //                        } else if (alwaysShow) {
    //                            if (!isWrongFieldType) {
    //                                this.addField(fieldNameString, 0, "");
    //                            }
    //                        }
    //                    }
    //                    itemValueCounter++;
    //                }
    //            } else {
    //                //System.out.println("checking value for: " + fieldNameString);
    //                String cellValue = this.getField(fieldNameString);
    //                valueFound = cellValue != null;
    //                if (valueFound && cellValue.length() > 0) {
    //                    this.addField(fieldNameString, 0, cellValue);
    //                }
    //            }
    //            return valueFound;
    //        }
    /**
     * Count the next level of child nodes. (non recursive)
     *
     * @return An integer of the next level of child nodes including corpus links and Arbil child nodes.
     */
    public int getChildCount() {
	//        System.out.println("getChildCount: " + childLinks.size() + childrenHashtable.size() + " : " + this.getUrlString());
	return childArray.length;
    }

    /**
     * Calls getAllChildren(Vector<ArbilDataNode> allChildren) and returns the result as an array
     *
     * @return an array of all the child nodes
     */
    public ArbilDataNode[] getAllChildren() {
	Vector<ArbilDataNode> allChildren = new Vector<ArbilDataNode>();
	getAllChildren(allChildren);
	return allChildren.toArray(new ArbilDataNode[]{});
    }

    /**
     * Used to get all the Arbil child nodes (all levels) of a session or all the nodes contained in a corpus (one level only).
     *
     * @param An empty vector, to which all the child nodes will be added.
     */
    public void getAllChildren(Vector<ArbilDataNode> allChildren) {
	System.out.println("getAllChildren: " + this.getUrlString());
	if (this.isSession() || this.isCatalogue() || this.isChildNode() || this.isCmdiMetaDataNode()) {
	    for (ArbilDataNode currentChild : childArray) {
		if (currentChild != this) { // Should not happen but prevent looping by self reference
		    currentChild.getAllChildren(allChildren);
		    allChildren.add(currentChild);
		}
	    }
	}
    }

    /**
     * Gets an array of the children of this node.
     *
     * @return An array of the next level child nodes.
     */
    public ArbilDataNode[] getChildArray() {
	return childArray;
    }

    /**
     * Gets the second level child nodes from the fist level child node matching the child type string.
     * Used to populate the child nodes in the table cell.
     *
     * @param childType The name of the first level child to query.
     * @return An object array of all second level child nodes in the first level node.
     */
    public ArbilDataNode[] getChildNodesArray(String childType) {
	for (ArbilDataNode currentNode : childArray) {
	    if (currentNode.toString().equals(childType)) {
		return currentNode.getChildArray();
	    }
	}
	return null;
    }

    /**
     * Recursively checks all subnodes and their URI fragments, tries to find a match to the provided path
     *
     * @param path Path to match
     * @return Matching child node, if found. Otherwise null
     */
    public ArbilDataNode getChildByPath(String path) {
	if (childArray != null && childArray.length > 0) {
	    for (ArbilDataNode child : childArray) {
		if (child.getURI() != null && path.equals(child.getURI().getFragment())) {
		    return child;
		} else {
		    ArbilDataNode childMatch = child.getChildByPath(path);
		    if (childMatch != null) {
			return childMatch;
		    }
		}
	    }
	}
	return null;
    }

    public ArbilTemplate getNodeTemplate() {
	if (nodeTemplate != null && !this.isCorpus()) {
	    return nodeTemplate;
	} else if (this.isChildNode()) {
	    return this.getParentDomNode().getNodeTemplate();
	} else {
	    //new LinorgBugCatcher().logError(new Exception("Corpus Branch Null Template"));
	    return ArbilTemplateManager.getSingleInstance().getDefaultTemplate();
	}
    }

    /**
     * create a subdirectory based on the file name of the node
     * if that fails then the current directory will be returned
     *
     * @return
     */
    public File getSubDirectory() {
	String currentFileName = this.getFile().getParent();
	if (MetadataFormat.isPathImdi(nodeUri.getPath()) || MetadataFormat.isPathCmdi(nodeUri.getPath())) {
	    currentFileName = currentFileName + File.separatorChar + this.getFile().getName().substring(0, this.getFile().getName().length() - 5);
	    File destinationDir = new File(currentFileName);
	    if (!destinationDir.exists()) {
		if (!destinationDir.mkdir()) {
		    bugCatcher.logError(new Exception("Could not create directory " + destinationDir.getAbsolutePath()));
		}
	    }
	    return destinationDir;
	}
	return new File(this.getFile().getParent());
    }

    public boolean containsFieldValue(String fieldName, String searchValue) {
	boolean findResult = false;
	ArbilField[] currentFieldArray = this.fieldHashtable.get(fieldName);
	if (currentFieldArray != null) {
	    for (ArbilField currentField : currentFieldArray) {
		System.out.println("containsFieldValue: " + currentField.getFieldValue() + ":" + searchValue);
		if (currentField.getFieldValue().toLowerCase().contains(searchValue.toLowerCase())) {
		    return true;
		}
	    }
	}
	System.out.println("result: " + findResult + ":" + this);
	return findResult;
    }

    public boolean containsFieldValue(String searchValue) {
	boolean findResult = false;
	for (ArbilField[] currentFieldArray : (Collection<ArbilField[]>) this.fieldHashtable.values()) {
	    for (ArbilField currentField : currentFieldArray) {
		System.out.println("containsFieldValue: " + currentField.getFieldValue() + ":" + searchValue);
		if (currentField.getFieldValue().toLowerCase().contains(searchValue.toLowerCase())) {
		    return true;
		}
	    }
	}
	System.out.println("result: " + findResult + ":" + this);
	return findResult;
    }

    // this is used to disable the node in the tree gui
    public boolean getNodeEnabled() {
	//       ---      TODO: here we could look through all the fields in this node against the current filed view, if node are showing then return false
	//       ---      when the global field view is changed then set all nodeEnabled blaaaa
	return nodeEnabled;
    }

    /**
     * Tests if this node has child nodes even if they are not yet loaded.
     *
     * @return boolean
     */
    public boolean canHaveChildren() {
	return childArray.length > 0;
    }

    // this is used to delete an IMDI node from a corpus branch
    public void deleteCorpusLink(ArbilDataNode[] targetImdiNodes) {
	// TODO: There is an issue when deleting child nodes that the remaining nodes xml path (x) will be incorrect as will the xmlnode id hence the node in a table may be incorrect after a delete
	if (nodeNeedsSaveToDisk) {
	    saveChangesToCache(false);
	}
	try {
	    bumpHistory();
	    copyLastHistoryToCurrent(); // bump history is normally used afteropen and before save, in this case we cannot use that order so we must make a copy
	    synchronized (getParentDomLockObject()) {
		System.out.println("deleting by corpus link");
		URI[] copusUriList = new URI[targetImdiNodes.length];
		for (int nodeCounter = 0; nodeCounter < targetImdiNodes.length; nodeCounter++) {
		    //                if (targetImdiNodes[nodeCounter].hasResource()) {
		    //                    copusUriList[nodeCounter] = targetImdiNodes[nodeCounter].getFullResourceURI(); // todo: should this resouce case be used here? maybe just the uri
		    //                } else {
		    copusUriList[nodeCounter] = targetImdiNodes[nodeCounter].getURI();
		    //                }
		}
		metadataUtils.removeCorpusLink(this.getURI(), copusUriList);
		this.getParentDomNode().loadArbilDom();
	    }
	    //        for (ImdiTreeObject currentChildNode : targetImdiNodes) {
	    ////            currentChildNode.clearIcon();
	    //            ArbilTreeHelper.getSingleInstance().updateTreeNodeChildren(currentChildNode);
	    //        }
	    for (ArbilDataNode removedChild : targetImdiNodes) {
		removedChild.removeFromAllContainers();
	    }
	} catch (IOException ex) {
	    // Usually renaming issue. Try block includes add corpus link because this should not be attempted if history saving failed.
	    bugCatcher.logError("I/O exception while deleting nodes from " + this.toString(), ex);
	    messageDialogHandler.addMessageDialogToQueue("Could not delete nodes because an error occurred while saving history for node. See error log for details.", "Error while moving nodes");
	}

	this.getParentDomNode().clearIcon();
	this.getParentDomNode().clearChildIcons();
	clearIcon(); // this must be cleared so that the leaf / branch flag gets set
    }

    public boolean hasCatalogue() {
	for (ArbilDataNode childNode : childArray) {
	    //            String currentChildPath = currentLinkPair[0];
	    //            ImdiTreeObject childNode = ImdiLoader.getSingleInstance().getImdiObject(null, currentChildPath);
	    //childNode.waitTillLoaded(); // if the child nodes have not been loaded this will fail so we must wait here
	    if (childNode.isCatalogue()) {
		return true;
	    }
	}
	return false;
    }

    public boolean addCorpusLink(ArbilDataNode targetImdiNode) {
	boolean linkAlreadyExists = false;
	if (targetImdiNode.isCatalogue()) {
	    if (this.hasCatalogue()) {
		//                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Only one catalogue can be added", null);
		// prevent adding a second catalogue file
		return false;
	    }
	}
	for (String[] currentLinkPair : childLinks) {
	    String currentChildPath = currentLinkPair[0];
	    if (!targetImdiNode.waitTillLoaded()) { // we must wait here before we can tell if it is a catalogue or not
		messageDialogHandler.addMessageDialogToQueue("Error adding node, could not wait for file to load", "Loading Error");
		return false;
	    }
	    if (currentChildPath.equals(targetImdiNode.getUrlString())) {
		linkAlreadyExists = true;
	    }
	}
	if (targetImdiNode.getUrlString().equals(this.getUrlString())) {
	    messageDialogHandler.addMessageDialogToQueue("Cannot link or move a node into itself", null);
	    return false;
	}
	if (linkAlreadyExists) {
	    messageDialogHandler.addMessageDialogToQueue(targetImdiNode + " already exists in " + this + " and will not be added again", null);
	    return false;
	} else {
	    // if link is not already there
	    // if needs saving then save now while you can
	    // TODO: it would be nice to warn the user about this, but its a corpus node so maybe it is not important
	    if (nodeNeedsSaveToDisk) {
		saveChangesToCache(true);
	    }
	    try {
		bumpHistory();
		copyLastHistoryToCurrent(); // bump history is normally used afteropen and before save, in this case we cannot use that order so we must make a copy
		synchronized (getParentDomLockObject()) {
		    return metadataUtils.addCorpusLink(this.getURI(), new URI[]{targetImdiNode.getURI()});
		}
	    } catch (IOException ex) {
		// Usually renaming issue. Try block includes add corpus link because this should not be attempted if history saving failed.
		bugCatcher.logError("I/O exception while moving node " + targetImdiNode.toString() + " to " + this.toString(), ex);
		messageDialogHandler.addMessageDialogToQueue("Could not move nodes because an error occurred while saving history for node. See error log for details.", "Error while moving nodes");
		return false;
	    }
	}
    }

    public void pasteIntoNode() {
	Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	Transferable transfer = clipboard.getContents(null);
	try {
	    String clipBoardString = "";
	    Object clipBoardData = transfer.getTransferData(DataFlavor.stringFlavor);
	    if (clipBoardData != null) {//TODO: check that this is not null first but let it pass on null so that the no data to paste messages get sent to the user
		clipBoardString = clipBoardData.toString();
		System.out.println("clipBoardString: " + clipBoardString);

		String[] elements;
		if (clipBoardString.contains("\n")) {
		    elements = clipBoardString.split("\n");
		} else {
		    elements = new String[]{clipBoardString};
		}
		for (String element : elements) {
		}
		for (ArbilDataNode clipboardNode : pasteIntoNode(elements)) {
		    new MetadataBuilder().requestAddNode(this, "copy of " + clipboardNode, clipboardNode);
		}
	    }
	} catch (Exception ex) {
	    bugCatcher.logError(ex);
	}
    }

//    if (currentNode.getNeedsSaveToDisk(false)) {
//			    if (JOptionPane.CANCEL_OPTION == ArbilWindowManager.getSingleInstance().showDialogBox("The nodes to be copied contain unsaved changes.\nUnless these changes are saved, the resulting nodes will be copies of the currently saved nodes.\nContinue anyway?", "Copying with unsaved changes", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)) {
//				return;
//			    }
//			    break;
//			}
    private Collection<ArbilDataNode> pasteIntoNode(String[] clipBoardStrings) {
	ArrayList<ArbilDataNode> nodesToAdd = new ArrayList<ArbilDataNode>();
	boolean ignoreSaveChanges = false;
	for (String clipBoardString : clipBoardStrings) {
	    if (this.isCorpus()) {
		if (MetadataFormat.isPathMetadata(clipBoardString) || ArbilDataNode.isStringChildNode(clipBoardString)) {
		    ArbilDataNode clipboardNode = dataNodeLoader.getArbilDataNode(null, conformStringToUrl(clipBoardString));
		    if (sessionStorage.pathIsInsideCache(clipboardNode.getFile())) {
			if (!(ArbilDataNode.isStringChildNode(clipBoardString) && (!this.isSession() && !this.isChildNode()))) {
			    if (this.getFile().exists()) {
				if (!ignoreSaveChanges && clipboardNode.getNeedsSaveToDisk(false)) {
				    if (JOptionPane.CANCEL_OPTION == messageDialogHandler.showDialogBox(
					    "Some of the nodes to be copied contain unsaved changes.\nUnless they are saved, these changes will not be present in the resulting nodes. Continue anyway?", "Copying with unsaved changes", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)) {
					return new ArrayList<ArbilDataNode>(0);
				    } else {
					ignoreSaveChanges = true;
				    }
				}

				// this must use merge like favoirite to prevent instances end endless loops in corpus branches
				nodesToAdd.add(clipboardNode);
			    } else {
				messageDialogHandler.addMessageDialogToQueue("The target node's file does not exist", null);
			    }
			} else {
			    messageDialogHandler.addMessageDialogToQueue("Cannot paste session subnodes into a corpus", null);
			}
		    } else {
			messageDialogHandler.addMessageDialogToQueue("The target file is not in the cache", null);
		    }
		} else {
		    messageDialogHandler.addMessageDialogToQueue("Pasted string is not and IMDI file", null);
		}
	    } else if (this.isMetaDataNode() || this.isSession()) {
		// Get source node
		ArbilDataNode templateDataNode = dataNodeLoader.getArbilDataNode(null, conformStringToUrl(clipBoardString));
		// Check if it can be contained by destination node
		if (MetadataReader.getSingleInstance().nodeCanExistInNode(this, templateDataNode)) {
		    // Add source to destination
		    new MetadataBuilder().requestAddNode(this, templateDataNode.toString(), templateDataNode);
		} else {
		    // Invalid copy/paste...
		    messageDialogHandler.addMessageDialogToQueue("Cannot copy '" + templateDataNode.toString() + "' to '" + this.toString() + "'", "Cannot copy");
		}
	    } else { // Not corpus, session or metadata
		messageDialogHandler.addMessageDialogToQueue("Nodes of this type cannot be pasted into at this stage", null);
	    }
	}
	return nodesToAdd;
    }

    /**
     * Saves the current changes from memory into a new imdi file on disk.
     * Previous imdi files are renamed and kept as a history.
     * the caller is responsible for reloading the node if that is required
     */
    public synchronized void saveChangesToCache(boolean updateUI) {
	if (this != getParentDomNode()) {
	    //        if (this.isImdiChild()) {
	    getParentDomNode().saveChangesToCache(updateUI);
	    return;
	}
	System.out.println("saveChangesToCache");
	ArbilJournal.getSingleInstance().clearFieldChangeHistory();
	if (!this.isLocal() /* nodeUri.getScheme().toLowerCase().startsWith("http") */) {
	    System.out.println("should not try to save remote files");
	    return;
	}
	ArrayList<FieldUpdateRequest> fieldUpdateRequests = createFieldUpdateRequests();
	ArbilComponentBuilder componentBuilder = new ArbilComponentBuilder();
	boolean result = componentBuilder.setFieldValues(this, fieldUpdateRequests.toArray(new FieldUpdateRequest[]{}));
	if (!result) {
	    messageDialogHandler.addMessageDialogToQueue("Error saving changes to disk, check the log file via the help menu for more information.", "Save");
	} else {
	    this.nodeNeedsSaveToDisk = false;
	    //            // update the icon to indicate the change
	    //            setImdiNeedsSaveToDisk(null, false);
	}
	//        clearIcon(); this is called by setImdiNeedsSaveToDisk
    }

    private ArrayList<FieldUpdateRequest> createFieldUpdateRequests() {
	ArrayList<FieldUpdateRequest> fieldUpdateRequests = new ArrayList<FieldUpdateRequest>();
	Vector<ArbilField[]> allFields = new Vector<ArbilField[]>();
	getAllFields(allFields);
	for (Enumeration<ArbilField[]> fieldsEnum = allFields.elements(); fieldsEnum.hasMoreElements();) {
	    {
		ArbilField[] currentFieldArray = fieldsEnum.nextElement();
		for (int fieldCounter = 0; fieldCounter < currentFieldArray.length; fieldCounter++) {
		    ArbilField currentField = currentFieldArray[fieldCounter];
		    if (currentField.fieldNeedsSaveToDisk()) {
			FieldUpdateRequest currentFieldUpdateRequest = new FieldUpdateRequest();
			currentFieldUpdateRequest.keyNameValue = currentField.getKeyName();
			currentFieldUpdateRequest.fieldOldValue = currentField.originalFieldValue;
			currentFieldUpdateRequest.fieldNewValue = currentField.getFieldValueForXml();
			currentFieldUpdateRequest.fieldPath = currentField.getFullXmlPath();
			currentFieldUpdateRequest.fieldLanguageId = currentField.getLanguageId();
			currentFieldUpdateRequest.attributeValuesMap = currentField.getAttributeValuesMap();
			fieldUpdateRequests.add(currentFieldUpdateRequest);
		    }
		}
	    }
	}
	return fieldUpdateRequests;
    }

    /**
     * Vector gets populated with all fields relevant to the parent node
     * that includes all indinodechild fields but not from any other imdi file
     *
     * @param allFields Vector to populate
     */
    private void getAllFields(Vector<ArbilField[]> allFields) {
	System.out.println("getAllFields: " + this.toString());
	allFields.addAll(fieldHashtable.values());
	for (ArbilDataNode currentChild : childArray) {
	    if (currentChild.isChildNode()) {
		currentChild.getAllFields(allFields);
	    }
	}
    }

    /**
     * Saves the node dom into the local cache.
     * Before this is called it is recommended to confirm that the destinationDirectory path already exist and is correct, otherwise
     * unintended directories maybe created
     *
     * @param nodDom The dom for this node that will be saved.
     * @return A string path of the saved location.
     */
    //    public String saveNodeToCache(Document nodDom) {
    //        String cacheLocation = null;
    //        System.out.println("saveBranchToLocal: " + this.toString());
    //        if (this.isImdi() && !this.isImdiChild()) {
    //            if (nodDom != null) {
    //                //System.out.println("saveBranchToLocal: " + this.getUrl());
    //                //System.out.println("saveBranchToLocal: " + this.nodDom.);
    //
    //                String destinationPath = linorgSessionStorage.getSaveLocation(this.getUrlString());
    //
    ////                debugOut("destinationPath: " + destinationPath);
    //                File tempFile = new File(destinationPath);
    //                // only save the file if it does not exist, otherwise local changes would be lost and it would be pointless anyway
    //                if (tempFile.exists()) {
    //                    System.out.println("this imdi is already in the cache");
    //                } else {
    //                    // this function of the imdi.api will modify the imdi file as it saves it "(will be normalized and possibly de-domId-ed)"
    //                    // this will make it dificult to determin if changes are from this function of by the user deliberatly making a chage
    //                    api.writeDOM(nodDom, new File(destinationPath), false);
    //                    // at this point the file should exist and not have been modified by the user
    //                    // create hash index with server url but basedon the saved file
    //                    // note that if the imdi.api has changed this file then it will not be detected
    //                    // TODO: it will be best to change this to use the server api get mb5 sum when it is written
    //                    // TODO: there needs to be some mechanism to check for changes on the server and update the local copy
    //                    //getHash(tempFile, this.getUrl());
    //                    System.out.println("imdi should be saved in cache now");
    //                }
    //                // no point iterating child nodes which have not been loaded, it is better to do the outside this function
    ////                    Enumeration nodesToAddEnumeration = childrenHashtable.elements();
    ////                    while (nodesToAddEnumeration.hasMoreElements()) {
    //////                        ((ImdiTreeObject) nodesToAddEnumeration.nextElement()).saveBranchToLocal(destinationDirectory);
    ////                    }
    //                cacheLocation = destinationPath;
    //
    //            }
    //        }
    //        return cacheLocation;
    //    }
    public void addField(ArbilField fieldToAdd) {
	//        System.addField:out.println("addField: " + this.getUrlString() + " : " + fieldToAdd.xmlPath + " : " + fieldToAdd.getFieldValue());
	ArbilField[] currentFieldsArray = fieldHashtable.get(fieldToAdd.getTranslateFieldName());
	if (currentFieldsArray == null) {
	    currentFieldsArray = new ArbilField[]{fieldToAdd};
	} else {
	    //            System.out.println("appendingField: " + fieldToAdd);
	    ArbilField[] appendedFieldsArray = new ArbilField[currentFieldsArray.length + 1];
	    System.arraycopy(currentFieldsArray, 0, appendedFieldsArray, 0, currentFieldsArray.length);
	    appendedFieldsArray[appendedFieldsArray.length - 1] = fieldToAdd;
	    currentFieldsArray = appendedFieldsArray;

	    //            for (ImdiField tempField : currentFieldsArray) {
	    //                System.out.println("appended fields: " + tempField);
	    //            }
	}
	fieldHashtable.put(fieldToAdd.getTranslateFieldName(), currentFieldsArray);

	if (fieldToAdd.xmlPath.endsWith(".ResourceLink") && fieldToAdd.getParentDataNode().isChildNode()/* && fieldToAdd.parentImdi.getUrlString().contains("MediaFile") */) {
	    resourceUrlField = fieldToAdd;
	    mimeHashQueue.addToQueue(this);
	}
    }

    /**
     * Adds a field to the imdi node and creates imdi child nodes if required.
     *
     * @param fieldToAdd The field to be added.
     * @param childLevel For internal use and should be zero. Used to track the distance in imdi child nodes from the imdi node.
     * @param addedImdiNodes Returns with all the imdi child nodes that have been added during the process.
     * @param useCache If true the the imdi file will be saved to the cache.
     */
    //    private void addField(ImdiField fieldToAdd, int childLevel, Vector addedImdiNodes) {
    //        // TODO: modify this so that each child node gets the full filename and full xml path
    ////            if (isImdi()) {
    ////                if (fieldLabel.startsWith("Session.")) {
    ////                    fieldLabel = fieldLabel.substring(8);
    ////                } else if (fieldLabel.startsWith("Corpus.")) {
    ////                    fieldLabel = fieldLabel.substring(7);
    ////                }
    ////            }
    //        //fieldUrl.substring(firstSeparator + 1)
    //        // TODO: move this and we write to imdischema
    //        int nextChildLevel = fieldToAdd.translatedPath.replace(")", "(").indexOf("(", childLevel);
    //        debugOut("fieldLabel: " + fieldToAdd.translatedPath + " cellValue: " + fieldToAdd.fieldValue + " childLevel: " + childLevel + " nextChildLevel: " + nextChildLevel);
    //        if (nextChildLevel == -1) {
    //            // add the label to this level node
    ////                if (fieldLabel == null) fieldLabel = "oops null";
    ////                if (fieldValue == null) fieldValue = "oops null";
    //            String childsLabel = fieldToAdd.translatedPath.substring(childLevel);
    //            fieldHashtable.put(childsLabel, fieldToAdd);
    //
    ////                if (childsLabel.endsWith(".Date")) {
    ////                    DateFormat df = new SimpleDateFormat("yyyy-MM-DD");
    ////                    try {
    ////                        nodeDate = df.parse(fieldToAdd.fieldValue);
    ////                        if (minNodeDate == null) {
    ////                            minNodeDate = nodeDate;
    ////                            maxNodeDate = nodeDate;
    ////                        }
    ////                        if (nodeDate.before(minNodeDate)) {
    ////                            minNodeDate = nodeDate;
    ////                        }
    ////                        if (nodeDate.after(maxNodeDate)) {
    ////                            maxNodeDate = nodeDate;
    ////                        }
    ////                    } catch (Exception ex) {
    ////                        System.err.println(ex.getMessage());
    ////                    }
    ////                }
    //            // if the node contains a ResourceLink then save the location in resourceUrlString and create a hash for the file
    //            if (childsLabel.equals(MetadataReader.imdiPathSeparator + "ResourceLink")) {
    ////                        // resolve the relative location of the file
    ////                        File resourceFile = new File(this.getFile().getParent(), fieldToAdd.fieldValue);
    ////                        resourceUrlString = resourceFile.getCanonicalPath();
    //                resourceUrlString = fieldToAdd.fieldValue;
    ////                if (useCache) {
    ////                    linorgSessionStorage.getFromCache(getFullResourceURI());
    ////                }
    //                mimeHashQueue.addToQueue(this);
    //            }
    //        } else {
    //            // pass the label to the child nodes
    //            String childsName = fieldToAdd.translatedPath.substring(childLevel, nextChildLevel);
    //            //String parentName = fieldLabel.substring(0, firstSeparator);
    //            debugOut("childsName: " + childsName);
    //            if (!childrenHashtable.containsKey(childsName)) {
    //                ImdiTreeObject tempImdiTreeObject = imdiLoader.getImdiObject(childsName, this.getUrlString() + "#" + fieldToAdd.xmlPath);
    //                if (addedImdiNodes != null) {
    //                    addedImdiNodes.add(tempImdiTreeObject);
    //                }
    //                tempImdiTreeObject.imdiDataLoaded = true;
    ////                System.out.println("adding to list of child nodes 3: " + tempImdiTreeObject);
    //                childrenHashtable.put(childsName, tempImdiTreeObject);
    //            }
    //            ((ImdiTreeObject) childrenHashtable.get(childsName)).addField(fieldToAdd, nextChildLevel + 1, addedImdiNodes);
    //        }
    //    }
    /**
     * Gets the fields in this node, this does not include any imdi child fields.
     * To get all fields relevant the imdi file use "getAllFields()" which includes imdi child fields.
     *
     * @return A hashtable of the fields
     */
    public Hashtable<String, ArbilField[]> getFields() {
	// store the Hastable for next call
	// if hashtable is null then load from imdi
	return fieldHashtable;
    }

    /**
     * Returns the fields of this data note sorted by field order
     *
     * @return
     */
    public List<ArbilField[]> getFieldsSorted() {
	List<ArbilField[]> fieldArrays = new ArrayList<ArbilField[]>(getFields().values());
	Collections.sort(fieldArrays, new ArrayComparator<ArbilField>(new ArbilFieldComparator(), 0));
	return fieldArrays;
    }

    //    public String getCommonFieldPathString() {
    //        // find repetitious path strings in the fields for this node so they can be omitted from the table display
    //        if (commonFieldPathString == null) {
    //            if (fieldHashtable.size() < 2) {
    //                // if there is only one field name then it would be reduced to zero length which we do not want
    //                commonFieldPathString = "";
    //            } else {
    //                String commonPath = null;
    //                for (ImdiField[] currentField : fieldHashtable.values()) {
    //                    if (commonPath == null) {
    //                        commonPath = currentField[0].xmlPath;
    //                    } else {
    //                        int matchingIndex = commonPath.length();
    //                        while (matchingIndex > 0 && !commonPath.substring(0, matchingIndex).equals(currentField[0].xmlPath.substring(0, matchingIndex))) {
    //                            System.out.println("matchingIndex: " + matchingIndex + "\t" + commonPath.substring(0, matchingIndex));
    //                            matchingIndex--;
    //                        }
    //                        commonPath = commonPath.substring(0, matchingIndex);
    //                    }
    //                }
    //                commonFieldPathString = commonPath;
    //            }
    //        }
    //        return commonFieldPathString;
    //    }
    /**
     * Compares this node to another based on its type and string value.
     *
     * @return The string comparison result.
     */
    public int compareTo(Object o) throws ClassCastException {
	if (isFavorite()) {
	    return favouriteSorter.compare(this, o);
	} else {
	    return dataNodeSorter.compare(this, o);
	}
    }

    public synchronized void notifyLoaded() {
	getParentDomNode().notifyAll();
    }

    /**
     * If isLoading(), i.e. loading state > 0, waits for loading state to become 0
     *
     * @return
     */
    public synchronized boolean waitTillLoaded() {
	System.out.println("waitTillLoaded");
	if (this != getParentDomNode()) { // isloading does this parent check pretty much already
	    return getParentDomNode().waitTillLoaded();
	} else {
	    if (isLoading()) {
		System.out.println("isLoading");
		try {
		    getParentDomNode().wait();
		    System.out.println("wait");
		    if (isLoading()) {
			bugCatcher.logError(new Exception("waited till loaded but its still loading: " + this.getUrlString()));
		    }
		} catch (Exception ex) {
		    bugCatcher.logError(ex);
		    return false;
		}
	    }
	    return true;
	}
    }

    public void updateLoadingState(int countChange) {
	if (this != getParentDomNode()) {
	    getParentDomNode().updateLoadingState(countChange);
	} else {
	    final boolean wasLoading = isLoading();
	    synchronized (loadingCountLock) {
		isLoadingCount += countChange;
	    }
//            System.out.println("isLoadingCount: " + isLoadingCount);
	    if (wasLoading != isLoading()) {
		//                    this.notifyAll();
		clearChildIcons();
		clearIcon();
	    }
	}
    }

    public synchronized boolean isLoading() {
	return getParentDomNode().isLoadingCount > 0;
    }

    @Override
    public String toString() {
	if (lastNodeText != null) {
	    return lastNodeText;
	} else {
	    return "unknown";
	}
    }

    public boolean isNodeTextDetermined() {
	return lastNodeText != null && !lastNodeText.equals(NODE_LOADING_TEXT);
    }

    public String refreshStringValue() {
	if (isLoading()) {
	    //            if (lastNodeText.length() > 0) {
	    //                return lastNodeText;
	    //            } else {asdasdasd
	    ////                if (nodeText != null && nodeText.length() > 0) {
	    return lastNodeText;
	    //            }
	} else if (lastNodeText.equals(NODE_LOADING_TEXT) && getParentDomNode().dataLoaded) {
	    lastNodeText = "                      ";
	}
	//        if (commonFieldPathString != null && commonFieldPathString.length() > 0) {
	//            // todo: use the commonFieldPathString as the node name if not display preference is set or the ones that are set have no value
	//            nodeText = commonFieldPathString;
	//        }
	boolean foundPreferredNameField = false;
	boolean preferredNameFieldExists = false;

	//final String nodePath = getNodePath();
	getLabelString:
	for (String currentPreferredName : this.getNodeTemplate().preferredNameFields) {
	    for (ArbilField[] currentFieldArray : fieldHashtable.values().toArray(new ArbilField[][]{})) {

		// TODO: Field of child nodes should not give name to node. Line below will acomplish this but also ignores preferred names on
		// nodes that get ALL their fields from child elements in the XML (in case of 1:1 truncation)
		// if (!currentFieldArray[0].getTranslateFieldName().contains(".")) { // Field of child nodes should not give name to node

		if (currentFieldArray[0].getFullXmlPath().replaceAll("\\(\\d+\\)", "").equals(currentPreferredName)) {
		    preferredNameFieldExists = true;
		    for (ArbilField currentField : currentFieldArray) {
			if (currentField != null) {
			    if (currentField.toString().trim().length() > 0) {
				nodeText = currentField.toString();
				foundPreferredNameField = true;
				break getLabelString;
			    }
			}
		    }
		}
	    }
	    ArbilField[] currentFieldArray = fieldHashtable.get(currentPreferredName);
	    if (currentFieldArray != null) {
		for (ArbilField currentField : currentFieldArray) {
		    if (currentField != null) {
			if (currentField.toString().trim().length() > 0) {
			    nodeText = currentField.toString();
			    //                            System.out.println("nodeText: " + nodeText);
			    foundPreferredNameField = true;
			    break getLabelString;
			}
		    }
		}
	    }
	}
	if (!foundPreferredNameField && this.isCmdiMetaDataNode()/* && isCmdiMetaDataNode() *//* && fieldHashtable.size() > 0 && domParentImdi == this */) {
	    String unamedText;
	    String nodeFragmentName = this.getURI().getFragment();
	    if (nodeFragmentName != null) {
		nodeFragmentName = getNodeTypeNameFromUriFragment(nodeFragmentName);
		unamedText = nodeFragmentName;
	    } else if (this.nodeTemplate != null) {
		//            if (this.getNodeTemplate().preferredNameFields.length == 0) {
		//                nodeText = "no field specified to name this node (" + this.nodeTemplate.getTemplateName() + ")";
		//            } else {
		unamedText = this.nodeTemplate.getTemplateName();
	    } else {
		unamedText = "";
	    }
	    if (preferredNameFieldExists) {
		nodeText = unamedText + " (unnamed)";
	    } else {
		nodeText = unamedText;
	    }
	}
	//        if (!foundPreferredNameField && isCmdiMetaDataNode() && domParentImdi == this && fieldHashtable.size() > 0) {
	//            // only if no name has been found and only for cmdi nodes and only when this is the dom parent node
	//            nodeText = fieldHashtable.elements().nextElement()[0].getFullXmlPath().split("\\.")[3];
	//        }
	if (hasResource()) {
	    URI resourceUri = getFullResourceURI();
	    if (resourceUri != null) {
		String resourcePathString = resourceUri.toString();
		int lastIndex = resourcePathString.lastIndexOf("/");
		//                if (lastIndex)
		resourcePathString = resourcePathString.substring(lastIndex + 1);
		try {
		    resourcePathString = URLDecoder.decode(resourcePathString, "UTF-8");
		} catch (UnsupportedEncodingException encodingException) {
		    bugCatcher.logError(encodingException);
		}
		nodeText = resourcePathString;
	    }
	}
	if (isInfoLink) {
	    String infoTitle = fieldHashtable.values().iterator().next()[0].getFieldValue();
	    infoTitle = infoTitle.trim();
	    if (infoTitle.length() > 0) {
		nodeText = infoTitle;
	    }
	}
	//        nodeTextChanged = lastNodeText.equals(nodeText + nameText);
	if (nodeText != null) {
	    if (isMetaDataNode()) {
		File nodeFile = this.getFile();
		if (nodeFile != null && !isHeadRevision()) {
		    nodeText = nodeText + " (rev:" + getHistoryLabelStringForFile(nodeFile) + ")";
		}
	    }
	    lastNodeText = nodeText;
	}

	if (isContainerNode()) {
	    lastNodeText = String.format("%1$s (%2$d)", lastNodeText, getChildCount());
	} else if (isSingletonMetadataNode()) {
	    StringBuilder nodeTextSB = new StringBuilder(getNodeTypeNameFromUriFragment(getURI().getFragment()));
	    if (nodeText != null && nodeText.length() > 0) {
		nodeTextSB.append(" (").append(nodeText).append(")");
	    }
	    lastNodeText = nodeTextSB.toString();
	}

	if (lastNodeText.length() == 0) {
	    lastNodeText = "                      ";
	}
	return lastNodeText;// + "-" + clearIconCounterGlobal + "-" + clearIconCounter;
	//            }
    }

    /**
     * Tests if there is file associated with this node and if it is an archivable type.
     * The file could be either a resource file (getResource) or a loose file (getUrlString).
     *
     * @return boolean
     */
    public boolean isArchivableFile() {
	return mpiMimeType != null;
    }

    /**
     * Tests if a resource file (local or remote) is associated with this node.
     *
     * @return boolean
     */
    public boolean hasResource() {
	return resourceUrlField != null;
    }

    public boolean canHaveResource() {
	if (hasResource()) {
	    return true;
	} else if (isCmdiMetaDataNode()) {
	    final ArbilTemplate template = getNodeTemplate();
	    if (template != null) {
		return template.pathCanHaveResource(nodeUri.getFragment());
	    }
	}
	return false;
    }

    /**
     * Inserts/sets resource location. Behavior will depend on node type
     *
     * @param location Location to insert/set
     */
    public void insertResourceLocation(URI location) throws ArbilMetadataException {
	if (isCmdiMetaDataNode()) {
	    ArbilDataNode resourceNode = null;
	    try {
		resourceNode = dataNodeLoader.getArbilDataNodeWithoutLoading(location);
	    } catch (Exception ex) {
		throw new ArbilMetadataException("Error creating resource node for URI: " + location.toString(), ex);
	    }
	    if (resourceNode == null) {
		throw new ArbilMetadataException("Unknown error creating resource node for URI: " + location.toString());
	    }

	    new MetadataBuilder().requestAddNode(this, null, resourceNode);
	} else {
	    if (hasResource()) {
		resourceUrlField.setFieldValue(location.toString(), true, false);
	    }
	}
    }

    /**
     * Tests if a local resource file is associated with this node.
     *
     * @return boolean
     */
    public boolean hasLocalResource() {
	if (!hasResource()) {
	    return false;
	}
	if (resourceUrlField.getFieldValue().toLowerCase().startsWith("http")) {
	    return false;
	}
	if (!this.isLocal()) {
	    return false;
	} else {
	    return true;
	}
    }

    public boolean resourceFileNotFound() {
	if (hasLocalResource()) {
	    if (resourceUrlField.getFieldValue().length() == 0) {
		return true;
	    }
	    try {
		return !(new File(this.getFullResourceURI())).exists();
	    } catch (Exception e) {
		return true;
	    }
	} else {
	    return false;
	}
    }

    /**
     * Gets the ULR string of the resource file if it is available.
     *
     * @return a URL string of the resource file
     */
    private String getResource() {
	return resourceUrlField.getFieldValue();
    }

    public boolean hasHistory() {
	if (!this.isLocal()) {
	    // only local files can have a history
	    return false;
	}
	return !this.isChildNode() && new File(this.getFile().getAbsolutePath() + ".0").exists();
    }

    private String getHistoryLabelStringForFile(File historyFile) {
	Date mtime = new Date(historyFile.lastModified());
	String mTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(mtime);
	return mTimeString;
    }

    private boolean isHeadRevision() {
	return !(new File(this.getFile().getAbsolutePath() + ".x").exists());
    }

    public String[][] getHistoryList() {
	Vector<String[]> historyVector = new Vector<String[]>();
	int versionCounter = 0;
	File currentHistoryFile;
	//        historyVector.add(new String[]{"Current", ""});
	if (!isHeadRevision()) {
	    historyVector.add(new String[]{"Last Save", ".x"});
	}
	do {
	    currentHistoryFile = new File(this.getFile().getAbsolutePath() + "." + versionCounter);
	    if (currentHistoryFile.exists()) {
		String mTimeString = getHistoryLabelStringForFile(currentHistoryFile);
		historyVector.add(new String[]{mTimeString, "." + versionCounter});
	    }
	    versionCounter++;
	} while (currentHistoryFile.exists());
	return historyVector.toArray(new String[][]{{}});
    }

    public boolean resurrectHistory(String historyVersion) {
	InputStream historyFile = null;
	OutputStream activeVersionFile = null;
	try {
	    if (historyVersion.equals(".x")) {
		if (this.getFile().delete()) {
		    if (!new File(this.getFile().getAbsolutePath() + ".x").renameTo(this.getFile())) {
			throw new IOException("Could not rename history file '" + this.getFile().getAbsolutePath() + ".x'");
		    }
		} else {
		    throw new IOException("Could not delete old history file: " + this.getFile().getAbsolutePath());
		}
	    } else {
		try {
		    messageDialogHandler.offerUserToSaveChanges();
		} catch (Exception e) {
		    // user canceled the save action
		    // todo: alert user that nothing was done
		    return false;
		}
		if (!new File(this.getFile().getAbsolutePath() + ".x").exists()) {
		    if (!this.getFile().renameTo(new File(this.getFile().getAbsolutePath() + ".x"))) {
			throw new IOException("Could not rename to history file: " + getFile().getAbsolutePath());
		    }
		} else {
		    if (!this.getFile().delete()) {
			throw new IOException("Could not delete history file: " + getFile().getAbsolutePath());
		    }
		}
		historyFile = new FileInputStream(new File(this.getFile().getAbsolutePath() + historyVersion));
		activeVersionFile = new FileOutputStream(this.getFile(), true);

		byte[] copyBuffer = new byte[1024];
		int len;
		while ((len = historyFile.read(copyBuffer)) > 0) {
		    activeVersionFile.write(copyBuffer, 0, len);
		}

	    }
	} catch (FileNotFoundException e) {
	    messageDialogHandler.addMessageDialogToQueue(e.getLocalizedMessage() + ". History may be broken for " + this.toString(), "File not found");
	    bugCatcher.logError(e);
	    return false;
	} catch (IOException e) {
	    messageDialogHandler.addMessageDialogToQueue(e.getLocalizedMessage() + ". History may be broken for " + this.toString(), "Error while reading or writing to disk");
	    bugCatcher.logError(e);
	    return false;
	} finally {
	    if (null != historyFile) {
		try {
		    historyFile.close();
		} catch (IOException ex) {
		    bugCatcher.logError(ex);
		}
	    }
	    if (null != activeVersionFile) {
		try {
		    activeVersionFile.close();
		} catch (IOException ex) {
		    bugCatcher.logError(ex);
		}
	    }
	}
	dataNodeLoader.requestReload(getParentDomNode());

	return true;
    }

    /*
     * Increment the history file so that a new current file can be saved without overwritting the old
     */
    public void bumpHistory() throws IOException {
	// update the files version number
	//TODO: the template add does not create a new history file
	int versionCounter = 0;
	File headVersion = this.getFile();
        //        if the .x file (the last head) exist then replace the current with it
	if (new File(this.getFile().getAbsolutePath() + ".x").exists()) {
            versionCounter++;
	    headVersion = new File(this.getFile().getAbsolutePath() + ".x");
        }
	while (new File(this.getFile().getAbsolutePath() + "." + versionCounter).exists()) {
            versionCounter++;
        }
        while (versionCounter >= 0) {
	    File lastFile = new File(this.getFile().getAbsolutePath() + "." + versionCounter);
            versionCounter--;
	    File nextFile = new File(this.getFile().getAbsolutePath() + "." + versionCounter);
            if (versionCounter >= 0) {
                System.out.println("renaming: " + nextFile + " : " + lastFile);
                if (!nextFile.renameTo(lastFile)) {
                    throw new IOException("Error while copying history files for metadata. Could not rename " + nextFile.toString() + " to " + lastFile.toString());
                }
            } else {
                System.out.println("renaming: " + headVersion + " : " + lastFile);
                if (!headVersion.renameTo(lastFile)) {
                    throw new IOException("Error while copying history files for metadata. Could not rename " + headVersion.toString() + " to " + lastFile.toString());
                }
            }
        }
    }

    private void copyLastHistoryToCurrent() {
	FileOutputStream outFile = null;
	InputStream inputStream = null;
	try {
	    outFile = new FileOutputStream(this.getFile());
	    inputStream = new FileInputStream(new File(this.getFile().getAbsolutePath() + ".0"));
	    int bufferLength = 1024 * 4;
	    byte[] buffer = new byte[bufferLength];
	    int bytesread = 0;
	    while (bytesread >= 0) {
		bytesread = inputStream.read(buffer);
		if (bytesread == -1) {
		    break;
		}
		outFile.write(buffer, 0, bytesread);
	    }
	} catch (IOException iOException) {
	    messageDialogHandler.addMessageDialogToQueue("Could not copy file when recovering from the last history file.", "Recover History");
	    bugCatcher.logError(iOException);
	} finally {
	    if (inputStream != null) {
		try {
		    inputStream.close();
		} catch (IOException ex) {
		    bugCatcher.logError(ex);
		}
	    }
	    if (outFile != null) {
		try {
		    outFile.close();
		} catch (IOException ex) {
		    bugCatcher.logError(ex);
		}
	    }
	}
    }

    /**
     * Resolves the full path to a resource file if it exists.
     *
     * @return The path to remote resource if it exists.
     */
    public URI getFullResourceURI() {
	try {
	    String targetUriString = resourceUrlField.getFieldValue();
	    String[] uriParts = targetUriString.split(":/", 2);
	    URI targetUri;
	    if (uriParts.length > 1) {
		// todo: this will not allow urls that have square brackets in them due to yet another bug in the java URI class
		//                String bracketEncodedPath = uriParts[1].replaceAll("\\[", "%5B");
		//                bracketEncodedPath = bracketEncodedPath.replaceAll("\\]", "%5D");
		String bracketEncodedPath = uriParts[1];
		//org.apache.commons.httpclient.URI test = null;

		//if (bracketEncodedPath.c)
		targetUri = new URI(uriParts[0], "/" + bracketEncodedPath, null);
	    } else {
		targetUri = new URI(null, targetUriString, null);
	    }
	    //            System.out.println("nodeUri: " + nodeUri);
	    URI resourceUri = nodeUri.resolve(targetUri);
	    //            System.out.println("targetUriString: " + targetUriString);
	    //            System.out.println("targetUri: " + targetUri);
	    //            System.out.println("resourceUri: " + resourceUri);
	    if (!targetUri.equals(resourceUri)) {
		// maintain the UNC path
		boolean isUncPath = nodeUri.toString().toLowerCase().startsWith("file:////");
		if (isUncPath) {
		    try {
			resourceUri = new URI("file:////" + resourceUri.toString().substring("file:/".length()));
		    } catch (URISyntaxException urise) {
			bugCatcher.logError(urise);
		    }
		}
	    }
	    return resourceUri;
	} catch (Exception urise) {
	    bugCatcher.logError(urise);
	    System.out.println("URISyntaxException: " + urise.getMessage());
	    return null;
	}
    }

    /**
     * Gets the ULR string provided when the node was created.
     *
     * @return a URL string of the IMDI
     */
    public String getUrlString() {
	// TODO: update the uses of this to use the uri not a string
	return nodeUri.toString();
    }

    public Object getParentDomLockObject() {
	return getParentDomNode().domLockObjectPrivate;
    }

    /**
     * Gets the ArbilDataNode parent of an imdi child node.
     * The returned node will be able to reload/save the dom for this node.
     * Only relevant for imdi child nodes.
     *
     * @return ArbilDataNode
     */
    public synchronized ArbilDataNode getParentDomNode() {
	//        System.out.println("nodeUri: " + nodeUri);
	if (domParentNode == null) {
	    if (nodeUri.getFragment() != null) {
		try {
		    //domParentImdi = ImdiLoader.getSingleInstance().getImdiObject(null, new URI(nodeUri.getScheme(), nodeUri.getUserInfo(), nodeUri.getHost(), nodeUri.getPort(), nodeUri.getPath(), nodeUri.getQuery(), null /* fragment removed */));
		    // the uri is created via the uri(string) constructor to prevent re-url-encoding the url
		    domParentNode = dataNodeLoader.getArbilDataNode(null, new URI(nodeUri.toString().split("#")[0] /* fragment removed */));
		    //                    System.out.println("nodeUri: " + nodeUri);
		} catch (URISyntaxException ex) {
		    bugCatcher.logError(ex);
		}
	    } else {
		domParentNode = this;
	    }
	}
	return domParentNode;
    }

    public boolean isDirectory() {
	return isDirectory;
    }

    public boolean isMetaDataNode() {
	if (nodeUri != null /* && nodDom != null */) {
	    if (isChildNode()) {
		return true;
	    } else {
		return MetadataFormat.isPathMetadata(nodeUri.getPath());
	    }
	}
	return false;
    }

    public boolean isCmdiMetaDataNode() {
	if (nodeUri != null /* && nodDom != null */) {
	    if (isChildNode()) {
		return getParentDomNode().isCmdiMetaDataNode();
	    } else {
		return MetadataFormat.isPathCmdi(nodeUri.getPath());
	    }
	}
	return false;
    }

    /**
     * Tests if this node represents an imdi file or if if it represents a child node from an imdi file (created by adding fields with child
     * nodes).
     *
     * @return boolean
     */
    public boolean isChildNode() {
	return ArbilDataNode.isStringChildNode(this.getUrlString());
    }

    public boolean isSession() {
	// test if this node is a session
	ArbilField[] nameFields = fieldHashtable.get("Name");
	if (nameFields != null) {
	    return nameFields[0].xmlPath.equals(MetadataReader.imdiPathSeparator + "METATRANSCRIPT" + MetadataReader.imdiPathSeparator + "Session" + MetadataReader.imdiPathSeparator + "Name");
	}
	return false;
    }

    /**
     * Tests if this node is a meta node that contains no fields and only child nodes, such as the Languages, Actors, MediaFiles nodes etc..
     *
     * @return boolean
     */
    public boolean isEmptyMetaNode() {
	return this.getFields().isEmpty();
    }

    public boolean isCatalogue() {
	// test if this node is a catalogue
	ArbilField[] nameFields = fieldHashtable.get("Name");
	if (nameFields != null) {
	    return nameFields[0].xmlPath.equals(MetadataReader.imdiPathSeparator + "METATRANSCRIPT" + MetadataReader.imdiPathSeparator + "Catalogue" + MetadataReader.imdiPathSeparator + "Name");
	}
	return false;
    }

    public boolean isCorpus() {
	if (isCmdiMetaDataNode()) {
	    return false;
	}
	// test if this node is a corpus
	ArbilField[] nameFields = fieldHashtable.get("Name");
	if (nameFields != null) {
	    return nameFields[0].xmlPath.equals(MetadataReader.imdiPathSeparator + "METATRANSCRIPT" + MetadataReader.imdiPathSeparator + "Corpus" + MetadataReader.imdiPathSeparator + "Name");
	}
	return false;
    }

    public boolean isLocal() {
	if (nodeUri != null) {
	    return ArbilDataNode.isStringLocal(nodeUri.getScheme());
	} else {
	    return false;
	}
    }

    public boolean isEditable() {
	if (isLocal()) {
	    return (sessionStorage.pathIsInsideCache(this.getFile()))
		    || sessionStorage.pathIsInFavourites(this.getFile());
	} else {
	    return false;

	}
    }

    /**
     * Returns the URI object for this node.
     *
     * @return A URI that this node represents.
     */
    public URI getURI() {
	try {
	    return nodeUri; // new URI(nodeUri.toString()); // a copy of
	} catch (Exception ex) {
	    bugCatcher.logError(ex);
	    return null;
	}
    }

    public File getFile() {
	//        System.out.println("getFile: " + nodeUri.toString());
	if (nodeUri.getScheme().toLowerCase().equals("file")) {
	    try {
		return new File(new URI(nodeUri.toString().split("#")[0] /* fragment removed */));
	    } catch (Exception urise) {
		bugCatcher.logError(nodeUri.toString(), urise);
	    }
	}
	return null;
    }

    public String getParentDirectory() {
	String parentPath = this.getUrlString().substring(0, this.getUrlString().lastIndexOf("/")) + "/"; // this is a url so don't use the path separator
	return parentPath;
    }

    @Override
    public void registerContainer(ArbilDataNodeContainer containerToAdd) {
	// Node is contained by some object so make sure it's fully loaded or at least loading
	if (!getParentDomNode().dataLoaded && !isLoading()) {
	    dataNodeLoader.requestReload(getParentDomNode());
	}
	super.registerContainer(containerToAdd);
    }

    /**
     * Clears the icon for all the imdi child nodes of this node.
     * Used when loading a session dom.
     */
    public void clearChildIcons() {
	//        System.out.println("clearChildIconsParent: " + this);
	for (ArbilDataNode currentChild : childArray) {
	    //            if (!currentChild.equals(currentChild.getParentDomNode())) {
	    //                System.out.println("clearChildIcons: " + currentChild);
	    currentChild.clearChildIcons();
	    currentChild.clearIcon();
	    //            }
	}
    }
    //    public void addJumpToInTreeRequest() {
    //        jumpToRequested = true;
    //    }

    /**
     * Clears the icon calculated in "getIcon()" and notifies any UI containers of this node.
     */
    public void clearIcon() {
	refreshStringValue();
	//        System.out.println("clearIcon: " + this);
	//        System.out.println("containersOfThisNode: " + containersOfThisNode.size());
	//        SwingUtilities.invokeLater(new Runnable() {

	//            public void run() {
	icon = ArbilIcons.getSingleInstance().getIconForNode(ArbilDataNode.this); // to avoid a race condition (where the loading icons remains after load) this is also set here rather than nulling the icon
	//                System.out.println("clearIcon invokeLater" + ImdiTreeObject.this.toString());
	//                System.out.println("containersOfThisNode: " + containersOfThisNode.size());
	// here we need to cause an update in the gui containers so that the new icon can be loaded
	for (Enumeration<ArbilDataNodeContainer> containersIterator = containersOfThisNode.elements(); containersIterator.hasMoreElements();) { // changed back to a vector due to threading issues here
	    try { // TODO: the need for this try catch indicates that there is a threading issue in the way that imdichild nodes are reloaded within an imdi parent node and this should be reorganised to be more systematic and hierarchical
		ArbilDataNodeContainer currentContainer = containersIterator.nextElement();
		currentContainer.dataNodeIconCleared(this);
	    } catch (java.util.NoSuchElementException ex) {
		bugCatcher.logError(ex);
	    }
	}
	//            }
	//        });
	//        System.out.println("end clearIcon: " + this);
    }

    public synchronized void removeFromAllContainers() {
	// todo: this should also scan all child nodes and also remove them in the same way
	for (ArbilDataNode currentChildNode : this.getAllChildren()) {
	    currentChildNode.removeFromAllContainers();
	}
	for (ArbilDataNodeContainer currentContainer : containersOfThisNode.toArray(new ArbilDataNodeContainer[]{})) {
	    try {
		//ArbilDataNodeContainer currentContainer = containersIterator.nextElement();
		currentContainer.dataNodeRemoved(this);
	    } catch (java.util.NoSuchElementException ex) {
		bugCatcher.logError(ex);
	    }
	}
    }
    private Boolean isFavorite = null;

    public boolean isFavorite() {
	// Is being cached because comparator checks this every time
	if (isFavorite == null) {
	    if (!this.isLocal()) {
		// only local files can be favourites
		return false;
	    }
	    isFavorite = sessionStorage.pathIsInFavourites(this.getFile());
	}
	return isFavorite;

	//        return getParentDomNode().isFavourite;
    }

    //    public void setFavouriteStatus(boolean favouriteStatus) {
    //        getParentDomNode().isFavourite = favouriteStatus;
    //        clearIcon();
    //    }
    /**
     * If not already done calculates the required icon for this node in its current state.
     * Once calculated the stored icon will be returned.
     * To clear the icon and recalculate it "clearIcon()" should be called.
     *
     * @return The icon for this node.
     */
    public ImageIcon getIcon() {
	if (icon == null) {
	    return ArbilIcons.getSingleInstance().loadingIcon;
	}
	return icon;
    }
    private static ArbilNodeSorter dataNodeSorter = new ArbilNodeSorter();
    private static ArbilNodeSorter favouriteSorter = new ArbilFavouritesSorter();

    /**
     * @return the dataLoaded
     */
    public boolean isDataLoaded() {
	if (isChildNode()) {
	    return getParentDomNode().dataLoaded;
	} else {
	    return dataLoaded;
	}
    }

    /**
     * @param dataLoaded the dataLoaded to set
     */
    public void setDataLoaded(boolean dataLoaded) {
	this.dataLoaded = dataLoaded;
    }

    /**
     * @return Whether a resource URI has been set for this node
     */
    public boolean isResourceSet() {
	return resourceUrlField != null && resourceUrlField.getFieldValue().length() > 0;
    }

    public void invalidateThumbnails() {
	thumbnailFile = null;
	for (ArbilDataNode node : getChildArray()) {
	    node.invalidateThumbnails();
	}
    }
//
//    @Override
//    public boolean equals(Object obj) {
//	if (obj instanceof ArbilDataNode && obj != null) {
//	    return nodeUri.equals(((ArbilDataNode) obj).nodeUri);
//	} else {
//	    return super.equals(obj);
//	}
//    }
//
//    @Override
//    public int hashCode() {
//	return nodeUri.hashCode();
//    }

    /**
     * @return Whether node is conflated with metanode because if it is singleton (e.g. Project, Content). Null if this does not apply.
     */
    public boolean isSingletonMetadataNode() {
	return singletonMetadataNode;
    }

    /**
     * @param singletonMetadataNodeName Whether this node is conflated with metanode because it is singleton (e.g. Project, Content)
     */
    public void setSingletonMetadataNode(boolean singletonMetadataNodeName) {
	this.singletonMetadataNode = singletonMetadataNodeName;
    }

    /**
     * Get the value of containerNode
     *
     * @return the value of containerNode
     */
    public boolean isContainerNode() {
	return containerNode;
    }

    /**
     * Set the value of containerNode
     *
     * @param containerNode new value of containerNode
     */
    public void setContainerNode(boolean containerNode) {
	this.containerNode = containerNode;
    }

    /**
     * @return the cmdiComponentLinkReader
     */
    public CmdiComponentLinkReader getCmdiComponentLinkReader() {
	return getParentDomNode().cmdiComponentLinkReader;
    }

    /**
     * @return the typeCheckerState
     */
    public TypeCheckerState getTypeCheckerState() {
	return typeCheckerState;
    }

    /**
     * @param typeCheckerState the typeCheckerState to set
     */
    public void setTypeCheckerState(TypeCheckerState typeCheckerState) {
	this.typeCheckerState = typeCheckerState;
    }
}
