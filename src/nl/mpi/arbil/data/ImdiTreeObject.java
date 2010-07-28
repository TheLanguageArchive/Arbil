package nl.mpi.arbil.data;

import nl.mpi.arbil.MetadataFile.MetadataReader;
import nl.mpi.arbil.templates.ArbilTemplateManager;
import nl.mpi.arbil.templates.ArbilTemplate;
import nl.mpi.arbil.*;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import org.w3c.dom.Document;
import java.io.File;
import java.io.FileInputStream;
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
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;
import nl.mpi.arbil.MetadataFile.CmdiUtils;
import nl.mpi.arbil.clarin.CmdiComponentBuilder;
import nl.mpi.arbil.clarin.CmdiComponentLinkReader;
import nl.mpi.arbil.clarin.FieldUpdateRequest;
import nl.mpi.arbil.MetadataFile.ImdiUtils;
import nl.mpi.arbil.MetadataFile.MetadataUtils;

/**
 * Document   : ImdiTreeObject
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
public class ImdiTreeObject implements Comparable {

    public MetadataUtils metadataUtils;
    public ArbilTemplate nodeTemplate;
//    static ImdiIcons imdiIcons = new ImdiIcons();
//    private static Vector listDiscardedOfAttributes = new Vector(); // a list of all unused imdi attributes, only used for testing
    private boolean debugOn = false;
    private Hashtable<String, ImdiField[]> fieldHashtable; //// TODO: this should be changed to a vector or contain an array so that duplicate named fields can be stored ////
    private ImdiTreeObject[] childArray = new ImdiTreeObject[0];
    public boolean imdiDataLoaded;
    public int resourceFileServerResponse = -1; // -1 = not set otherwise this will be the http response code
    public String hashString;
    public String mpiMimeType = null;
    public String typeCheckerMessage;
    public int matchesInCache;
    public int matchesRemote;
    public int matchesLocalFileSystem;
    public boolean fileNotFound;
    public boolean isInfoLink = false;
    private boolean needsSaveToDisk;
    private String nodeText, lastNodeText = "loading imdi...";
//    private boolean nodeTextChanged = false;
    private URI nodeUri;
    public ImdiField resourceUrlField;
    public CmdiComponentLinkReader cmdiComponentLinkReader = null;
    public boolean isDirectory;
    private ImageIcon icon;
    private boolean nodeEnabled;
    public boolean hasSchemaError = false;
    // merge to one array of domid url imditreeobject
    private String[][] childLinks = new String[0][0]; // each element in this array is an array [linkPath, linkId]. When the link is from an imdi the id will be the node id, when from get links or list direcotry id will be null
    private Vector/*<Component>*/ containersOfThisNode;
    private int isLoadingCount = 0;
    final private Object loadingCountLock = new Object();
    @Deprecated
    public boolean lockedByLoadingThread = false;
//    private boolean isFavourite;
    public boolean hasArchiveHandle = false;
    public boolean hasDomIdAttribute = false; // used to requre a save (that will remove the dom ids) if a node has any residual dom id attributes
//    public boolean autoLoadChildNodes = false;
    //public Vector<String[]> addQueue;
    public boolean scrollToRequested = false;
//    public Vector<ImdiTreeObject> mergeQueue;
//    public boolean jumpToRequested = false; // dubious about this being here but it seems to fit here best
    private ImdiTreeObject domParentImdi = null; // the parent imdi containing the dom, only set for imdi child nodes
    //public String xmlNodeId = null; // only set for imdi child nodes and is the xml node id relating to this imdi tree object
    public File thumbnailFile = null;
    private final Object domLockObjectPrivate = new Object();

    protected ImdiTreeObject(URI localUri) {
//        System.out.println("ImdiTreeObject: " + localUri);
        containersOfThisNode = new Vector<Component>();
//        addQueue = new Vector<String[]>();
        nodeUri = localUri;
        if (nodeUri != null) {
            metadataUtils = ImdiTreeObject.getMetadataUtils(nodeUri.toString());
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
    private String urlEncodePath(String inputPath) {
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
            GuiHelper.linorgBugCatcher.logError(ex);
        }
        return encodedString;
    }

    static public URI conformStringToUrl(String inputUrlString) {
//        System.out.println("conformStringToUrl: " + inputUrlString);
        URI returnUrl = null;
        try {
//            localUrlString = localUrlString.replace("\\", "/");
            if (!inputUrlString.toLowerCase().startsWith("http") && !inputUrlString.toLowerCase().startsWith("file:") && !inputUrlString.toLowerCase().startsWith(".")) {
                returnUrl = new File(inputUrlString).toURI();
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
//                        GuiHelper.linorgBugCatcher.logError(urise);
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
                returnUrl = new URI(protocolComponent, pathComponent, fragmentComponent);
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
            GuiHelper.linorgBugCatcher.logError(ex);
        }
//        System.out.println("conformStringToUrl out: " + returnUrl.toString());
        return normaliseURI(returnUrl);
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
                GuiHelper.linorgBugCatcher.logError(urise);
            }
        }
        return returnURI;
    }
    // static methods for testing imdi file and object types

    static public boolean isImdiNode(Object unknownObj) {
        if (unknownObj == null) {
            return false;
        }
        return (unknownObj instanceof ImdiTreeObject);
    }

    static public boolean isStringLocal(String urlString) {
        return (!urlString.startsWith("http"));
    }

    static public boolean isPathHistoryFile(String urlString) {
//        System.out.println("isStringImdiHistoryFile" + urlString);
//        System.out.println("isStringImdiHistoryFile" + urlString.replaceAll(".imdi.[0-9]*$", ".imdi"));
        return isPathMetadata(urlString.replaceAll("mdi.[0-9]*$", "mdi"));
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

    static public boolean isStringImdiChild(String urlString) {
        return urlString.contains("#.METATRANSCRIPT") || urlString.contains("#.CMD"); // change made for clarin
    }

    static public MetadataUtils getMetadataUtils(String urlString) {
        if (ImdiTreeObject.isPathCmdi(urlString)) {
            return new CmdiUtils();
        } else if (ImdiTreeObject.isPathImdi(urlString)) {
            return new ImdiUtils();
        }
        return null;
    }
    // end static methods for testing imdi file and object types

    public boolean getNeedsSaveToDisk() {
        // when the dom parent node is saved all the sub nodes are also saved so we need to clear this flag
        if (needsSaveToDisk && !this.getParentDomNode().needsSaveToDisk) {
            needsSaveToDisk = false;
        }
        return needsSaveToDisk;
    }

    public boolean hasChangedFields() {
        boolean fieldsHaveChanges = false;
        for (ImdiField[] currentFieldArray : this.fieldHashtable.values()) {
            for (ImdiField currentField : currentFieldArray) {
                if (currentField.fieldNeedsSaveToDisk()) {
                    fieldsHaveChanges = true;
                }
            }
        }
        return fieldsHaveChanges;
    }

    public void setImdiNeedsSaveToDisk(ImdiField originatingField, boolean updateUI) {
        if (resourceUrlField != null && resourceUrlField.equals(originatingField)) {
            hashString = null;
            mpiMimeType = null;
            thumbnailFile = null;
            typeCheckerMessage = null;
            MimeHashQueue.getSingleInstance().addToQueue(this);
        }
        boolean imdiNeedsSaveToDisk = hasChangedFields() || hasDomIdAttribute;
        if (isMetaDataNode() && !isImdiChild()) {
            if (imdiNeedsSaveToDisk == false) {
                for (ImdiTreeObject childNode : getAllChildren()) {
                    if (childNode.needsSaveToDisk) {
                        imdiNeedsSaveToDisk = true;
                    }
                }
            }
            if (this.needsSaveToDisk != imdiNeedsSaveToDisk) {
                if (imdiNeedsSaveToDisk) {
                    ImdiLoader.getSingleInstance().addNodeNeedingSave(this);
                } else {
                    ImdiLoader.getSingleInstance().removeNodesNeedingSave(this);
                }
                this.needsSaveToDisk = imdiNeedsSaveToDisk;
            }
        } else {
            this.needsSaveToDisk = imdiNeedsSaveToDisk; // this must be set before setImdiNeedsSaveToDisk is called
            this.getParentDomNode().setImdiNeedsSaveToDisk(null, updateUI);
        }
        if (updateUI) {
            this.clearIcon();
        }
    }

    public String getAnyMimeType() {
        if (mpiMimeType == null && hasResource()) { // use the format from the imdi file if the type checker failed eg if the file is on the server
            ImdiField[] formatField = fieldHashtable.get("Format");
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
            ImdiField mimeTypeField = new ImdiField(fieldHashtable.size(), this, "Format", this.mpiMimeType, 0);
//            mimeTypeField.fieldID = "x" + fieldHashtable.size();
            addField(mimeTypeField);
        }
    }

    private void initNodeVariables() {
        // loop any indichildnodes and init
        if (childArray != null) {
            for (ImdiTreeObject currentNode : childArray) {
                if (currentNode.isImdiChild()) {
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
        fieldHashtable = new Hashtable<String, ImdiField[]>();
        imdiDataLoaded = false;
        hashString = null;
        //mpiMimeType = null;
        matchesInCache = 0;
        matchesRemote = 0;
        matchesLocalFileSystem = 0;
        fileNotFound = false;
        needsSaveToDisk = false;
//    nodeText = null;
//    urlString = null;
//        resourceUrlField = null;
        isDirectory = false;
        icon = null;
        nodeEnabled = true;
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
        getParentDomNode().needsSaveToDisk = false; // clear any changes
//        if (!this.isImdi()) {
//            initNodeVariables();
//            //loadChildNodes();
//            clearIcon();
//            // TODO: this could just remove the decendant nodes and let the user re open them
//            TreeHelper.getSingleInstance().updateTreeNodeChildren(this);
////            this.clearIcon();
//        } else {
////            if (getParentDomNode().isCorpus()) {
////                getParentDomNode().autoLoadChildNodes = true;
////            }
        ImdiLoader.getSingleInstance().requestReload(getParentDomNode());
//        }
    }

    public void loadImdiDom() {
        System.out.println("loadImdiDom: " + nodeUri.toString());
        if (getParentDomNode() != this) {
            getParentDomNode().loadImdiDom();
        } else {
            synchronized (getParentDomLockObject()) {
                initNodeVariables(); // this might be run too often here but it must be done in the loading thread and it also must be done when the object is created                
                if (!isMetaDataNode() && !isDirectory() && isLocal()) {
                    // if it is an not imdi or a loose file but not a direcotry then get the md5sum
                    MimeHashQueue.getSingleInstance().addToQueue(this);
                    imdiDataLoaded = true;
                }
                if (this.isDirectory()) {
                    getDirectoryLinks();
                    imdiDataLoaded = true;
//            clearIcon();
                }
                if (isMetaDataNode()) {
                    Document nodDom = null;
                    // cacheLocation will be null if useCache = false hence no file has been saved
//        String cacheLocation = null;
                    if (this.isLocal() && !this.getFile().exists() && new File(this.getFile().getAbsolutePath() + ".0").exists()) {
                        // if the file is missing then try to find a valid history file
                        copyLastHistoryToCurrent();
                        LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Missing file has been recovered from the last history item.", "Recover History");
                    }
                    try {
                        //System.out.println("tempUrlString: " + tempUrlString);
                        nodDom = new CmdiComponentBuilder().getDocument(this.getURI());
                        // only read the fields into imdi tree objects if it is not going to be saved to the cache
//            if (!useCache) {
                        if (nodDom == null) {
                            File nodeFile = this.getFile();
                            if (nodeFile != null && nodeFile.exists()) {
                                nodeText = "Could not load IMDI";
                            } else {
                                nodeText = "File not found";
                                fileNotFound = true;
                            }
                        } else {
                            //set the string name to unknown, it will be updated in the tostring function
                            nodeText = "unknown";
                            if (this.isCmdiMetaDataNode()) {
                                // load the links from the cmdi file
                                // the links will be hooked to the relevent nodes when the rest of the xml is read
                                cmdiComponentLinkReader = new CmdiComponentLinkReader();
                                cmdiComponentLinkReader.readLinks(this.getURI());
                            } else {
                                cmdiComponentLinkReader = null;
                            }
                            Vector<String[]> childLinksTemp = new Vector<String[]>();
                            Hashtable<ImdiTreeObject, HashSet<ImdiTreeObject>> parentChildTree = new Hashtable<ImdiTreeObject, HashSet<ImdiTreeObject>>();
                            Hashtable<String, Integer> siblingNodePathCounter = new Hashtable<String, Integer>();
                            // load the fields from the imdi file
                            MetadataReader.getSingleInstance().iterateChildNodes(this, childLinksTemp, nodDom.getFirstChild(), "", "", parentChildTree, siblingNodePathCounter, 0);
                            childLinks = childLinksTemp.toArray(new String[][]{});
                            //ImdiTreeObject[] childArrayTemp = new ImdiTreeObject[childLinks.length];
                            for (ImdiTreeObject currentNode : parentChildTree.keySet()) {
//                        System.out.println("setting childArray on: " + currentNode.getUrlString());
                                // save the old child array
                                ImdiTreeObject[] oldChildArray = currentNode.childArray;
                                // set the new child array
                                currentNode.childArray = parentChildTree.get(currentNode).toArray(new ImdiTreeObject[]{});
                                // check the old child array and for each that is no longer in the child array make sure they are removed from any containers (tables or trees)
                                List currentChildList = Arrays.asList(currentNode.childArray);
                                for (ImdiTreeObject currentOldChild : oldChildArray) {
                                    if (currentChildList.indexOf(currentOldChild) == -1) {
                                        // remove from any containers that its found in
                                        for (Object currentContainer : currentOldChild.getRegisteredContainers()) {
                                            if (currentContainer instanceof ImdiChildCellEditor) {
                                                ((ImdiChildCellEditor) currentContainer).stopCellEditing();
                                            }
                                            if (currentContainer instanceof ImdiTableModel) {
                                                ((ImdiTableModel) currentContainer).removeImdiObjects(new ImdiTreeObject[]{currentOldChild});
                                            }
                                            if (currentContainer instanceof ImdiTree) {
                                                ((ImdiTree) currentContainer).requestResort();
                                            }
                                        }
                                    }
                                }
                            }
                        }
//            }
                        // save this to the cache before deleting the dom
//            if (useCache) {
//                // get the links from the imdi before we dispose of the dom
//                getImdiLinks(nodDom);
////                cacheLocation = saveNodeToCache(nodDom);
//            }
                    } catch (Exception mue) {
                        GuiHelper.linorgBugCatcher.logError(mue);
//            System.out.println("Invalid input URL: " + mue);
                    }
                    //we are now done with the dom so free the memory
                    nodDom = null;
//        return cacheLocation;
                    imdiDataLoaded = true;
//            clearChildIcons();
                }
            }
        }
    }

//        private String getField(String fieldName) {
//            Document itemDom = this.getNodeDom();
//            if (itemDom == null) {
//                return null;
//            }
//            IMDIElement rowValue = api.getIMDIElement(itemDom, fieldName);
//            if (rowValue != null) {
//                return rowValue.getValue();
//            } else {
//                return null;
//            }
//        }
    private void getDirectoryLinks() {
        File[] dirLinkArray = null;
        File nodeFile = this.getFile();
        if (nodeFile != null && nodeFile.isDirectory()) {
            dirLinkArray = nodeFile.listFiles();
            Vector<ImdiTreeObject> childLinksTemp = new Vector<ImdiTreeObject>();
            for (int linkCount = 0; linkCount < dirLinkArray.length; linkCount++) {
                try {
//                    System.out.println("nodeFile: " + nodeFile);
//                    System.out.println("dirLinkArray[linkCount]: " + dirLinkArray[linkCount]);
                    URI childURI = dirLinkArray[linkCount].toURI();
                    ImdiTreeObject currentImdi = ImdiLoader.getSingleInstance().getImdiObjectWithoutLoading(childURI);
                    if (TreeHelper.getSingleInstance().showHiddenFilesInTree || !currentImdi.getFile().isHidden()) {
                        childLinksTemp.add(currentImdi);
                    }
                } catch (Exception ex) {
                    LinorgWindowManager.getSingleInstance().addMessageDialogToQueue(dirLinkArray[linkCount] + " could not be loaded in\n" + nodeUri.toString(), "Load Directory");
                    new LinorgBugCatcher().logError(ex);
                }
            }
            //childLinks = childLinksTemp.toArray(new String[][]{});
            childArray = childLinksTemp.toArray(new ImdiTreeObject[]{});
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
     * @return An integer of the next level of child nodes including corpus links and imdi child nodes.
     */
    public int getChildCount() {
//        System.out.println("getChildCount: " + childLinks.size() + childrenHashtable.size() + " : " + this.getUrlString());
        return childArray.length;
    }

    /**
     * Calls getAllChildren(Vector<ImdiTreeObject> allChildren) and returns the result as an array
     * @return an array of all the child nodes
     */
    public ImdiTreeObject[] getAllChildren() {
        Vector<ImdiTreeObject> allChildren = new Vector<ImdiTreeObject>();
        getAllChildren(allChildren);
        return allChildren.toArray(new ImdiTreeObject[]{});
    }

    /**
     * Used to get all the imdi child nodes (all levels) of a session or all the nodes contained in a corpus (one level only).
     * @param An empty vector, to which all the child nodes will be added.
     */
    public void getAllChildren(Vector<ImdiTreeObject> allChildren) {
        System.out.println("getAllChildren: " + this.getUrlString());
        if (this.isSession() || this.isCatalogue() || this.isImdiChild() || this.isCmdiMetaDataNode()) {
            for (ImdiTreeObject currentChild : childArray) {
                currentChild.getAllChildren(allChildren);
                allChildren.add(currentChild);
            }
        }
    }

    /**
     * Gets an array of the children of this node.
     * @return An array of the next level child nodes.
     */
    public ImdiTreeObject[] getChildArray() {
        return childArray;
    }

//    /**
//     * Used to populate the child list in the show child popup in the imditable.
//     * @return An enumeration of the next level child nodes.
//     */
//    public Enumeration<ImdiTreeObject> getChildEnum() {
//        return childObjectDomIdHash.elements();
//    }
//
//    /**
//     * Used to populate the child nodes in the table cell.
//     * @return A collection of the next level child nodes.
//     */
//    public Collection<ImdiTreeObject> getChildCollection() {
//        return childObjectDomIdHash.values();
//    }
    /**
     * Gets the second level child nodes from the fist level child node matching the child type string.
     * Used to populate the child nodes in the table cell.
     * @param childType The name of the first level child to query.
     * @return An object array of all second level child nodes in the first level node.
     */
    public ImdiTreeObject[] getChildNodesArray(String childType) {
        for (ImdiTreeObject currentNode : childArray) {
            if (currentNode.toString().equals(childType)) {
                return currentNode.getChildArray();
            }
        }
        return null;
    }

    public ArbilTemplate getNodeTemplate() {
        if (nodeTemplate != null && !this.isCorpus()) {
            return nodeTemplate;
        } else if (this.isImdiChild()) {
            return this.getParentDomNode().getNodeTemplate();
        } else {
            //new LinorgBugCatcher().logError(new Exception("Corpus Branch Null Template"));
            return ArbilTemplateManager.getSingleInstance().getDefaultTemplate();
        }
    }

    /**
     * Attache a child node to this node.
     * Only affects objects in memory and used when loading an imdi dom.
     * Will not and should not add a node as a child of itself.
     * To add a node to a dom use addChildNode.
     * @return void
     */
//    public void attachChildNode(ImdiTreeObject destinationNode) {
////        System.out.println("attachChildNodeTo: " + this.getUrlString());
////        System.out.println("attachChildNode: " + destinationNode.getUrlString());
//        if (destinationNode != this) {
//            childrenHashtable.put(destinationNode.getUrlString(), destinationNode);
//        }
//    }
//    /**
//     * Add a resource contained i an imdi object
//     * @return String path to the added node
//     */
//    public String addChildNode(ImdiTreeObject nodeToAdd) {
//        System.out.println("addChildNode: " + nodeToAdd);
//        return addChildNode(null, nodeToAdd.getUrlString(), nodeToAdd.mpiMimeType);
//    }
    // create a subdirectory based on the file name of the node
    // if that fails then the current directory will be returned
    public File getSubDirectory() {
        String currentFileName = this.getFile().getParent();
        if (ImdiTreeObject.isPathImdi(nodeUri.getPath()) || ImdiTreeObject.isPathCmdi(nodeUri.getPath())) {
            currentFileName = currentFileName + File.separatorChar + this.getFile().getName().substring(0, this.getFile().getName().length() - 5);
            File destinationDir = new File(currentFileName);
            if (!destinationDir.exists()) {
                destinationDir.mkdir();
            }
            return destinationDir;
        }
        return new File(this.getFile().getParent());
    }

    /**
     * Loads the child links and returns them as an array
     * @return ImdiTreeObject[] array of child nodes
     */
//    public void loadChildNodes() {
//        for (ImdiTreeObject currentChild : childArray) {
//            GuiHelper.imdiLoader.getImdiObject(null, currentChild.getUrlString());
//        }
//        System.out.println("loadChildNodes: " + this);
    //waitTillLoaded();
//        if (!getParentDomNode().imdiDataLoaded) {
    // if this node has been loaded then do not load again
    // to refresh the node and its children the node should be nulled and recreated
//            autoLoadChildNodes = false;
//        if (!this.isSession()) {
//            //getImdiFieldLinks();
//            for (Enumeration<String[]> childLinksEnum = childLinks.elements(); childLinksEnum.hasMoreElements();) {
//                String currentChildPath = childLinksEnum.nextElement()[0];
//                ImdiTreeObject currentImdi = GuiHelper.imdiLoader.getImdiObject(null, currentChildPath);
////                    System.out.println("adding to list of child nodes 2: " + currentImdi);
//                childrenHashtable.put(currentImdi.getUrlString(), currentImdi);
////                    if (ImdiTreeObject.isStringImdi(currentChildPath)) {
////                        currentImdi.loadImdiDom();
////                    }
//                }
//        }
    // START: this section uses the imdi.api to query the dom for available fields but it has been commented out in favour of the iterateChildNodes function
//                System.err.println("Starting to load fields at: " + System.nanoTime());
//                Long startTime = System.nanoTime();
//                for (int rowNameCounter = 0; rowNameCounter < imdiFieldArray.length; rowNameCounter++) {
//                    if (imdiFieldArray[rowNameCounter][0] || imdiFieldArray[rowNameCounter][1]) {
//                        populateChildFields(imdiFieldViews.getMasterImdiFieldName(rowNameCounter), imdiFieldArray[rowNameCounter][1]);
//                    }
//                }
//                Long nextTime = System.nanoTime();
////                System.err.println("Starting to print fields at: " + System.nanoTime());
//                if (nodDom != null) {
//                    iterateChildNodes(nodDom.getFirstChild(), "");
//                }
//                System.err.println("Done loading fields at: " + System.nanoTime());
//                Long lastTime = System.nanoTime();
//                System.err.println("first method: " + (nextTime - startTime) + " second method: " + (lastTime - nextTime));
//                System.err.println("second method took " + (lastTime - nextTime + 0.0) / (nextTime - startTime) * 100 + "% of the time used by the first");
//                System.err.println("the imdi.api took " + ((nextTime - startTime) / lastTime - nextTime + 0.0) + " times longer");
    // END: this section uses the imdi.api to query the dom for available fields but it has been commented out in favour of the iterateChildNodes function
//        }
//        Vector<ImdiTreeObject> tempImdiVector = new Vector<ImdiTreeObject>();
//        Enumeration nodesToAddEnumeration = childrenHashtable.elements();
//        while (nodesToAddEnumeration.hasMoreElements()) {
//            tempImdiVector.add((ImdiTreeObject) nodesToAddEnumeration.nextElement());
//        }
//        ImdiTreeObject[] returnImdiArray = new ImdiTreeObject[tempImdiVector.size()];
//        tempImdiVector.toArray(returnImdiArray);
//        return returnImdiArray;
//    }
    public boolean containsFieldValue(String fieldName, String searchValue) {
        boolean findResult = false;
        ImdiField[] currentFieldArray = this.fieldHashtable.get(fieldName);
        if (currentFieldArray != null) {
            for (ImdiField currentField : currentFieldArray) {
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
        for (ImdiField[] currentFieldArray : (Collection<ImdiField[]>) this.fieldHashtable.values()) {
            for (ImdiField currentField : currentFieldArray) {
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
     * @return boolean
     */
    public boolean canHaveChildren() {
        return childArray.length > 0;
    }

//    /*
//     * gets an array of
//     */
//    public int[] getRecursiveChildCount() {
////        debugOut("getChildCount: " + this.toString());
//        int[] returnArray = new int[2];
//        returnArray[0] = 0;
//        returnArray[1] = 0;
//        if (imdiDataLoaded) {
//            returnArray[1] += 1; // count this node
//            Enumeration nodesToAddEnumeration = childrenHashtable.elements();
//            while (nodesToAddEnumeration.hasMoreElements()) {
//                // count the child nodes
//                int[] childCount = ((ImdiTreeObject) nodesToAddEnumeration.nextElement()).getRecursiveChildCount();
//                returnArray[0] += childCount[0];
//                returnArray[1] += childCount[1];
//            }
//        } else {
//            if (this.isImdi()) {
//                returnArray[0] = 1;
//            }
//        }
//        return returnArray;
//    }
//    public void loadNextLevelOfChildren(long stopTime) {
////        debugOut("loadNextLevelOfChildren: " + this.toString() + ":" + (System.currentTimeMillis() - stopTime));
//        if (System.currentTimeMillis() > stopTime) {
//            return;
//        }
//        if (this.isImdi()) {
//            if (getParentDomNode().imdiDataLoaded) {
//                Enumeration nodesToAddEnumeration = childrenHashtable.elements();
//                while (nodesToAddEnumeration.hasMoreElements()) {
//                    // load one level of child nodes
//                    ((ImdiTreeObject) nodesToAddEnumeration.nextElement()).loadNextLevelOfChildren(stopTime);
//                    //((ImdiTreeObject) nodesToAddEnumeration.nextElement()).();
//                }
//            }
//        }
////        debugOut("listDiscardedOfAttributes: " + listDiscardedOfAttributes);
//    }
    private void getAllFields(Vector<ImdiField[]> allFields) {
        // returns all fields relevant to the parent node
        // that includes all indinodechild fields but not from any other imdi file
        System.out.println("getAllFields: " + this.toString());
        allFields.addAll(fieldHashtable.values());
        for (ImdiTreeObject currentChild : childArray) {
            if (currentChild.isImdiChild()) {
                currentChild.getAllFields(allFields);
            }
        }
    }

//    public void deleteFromParentDom(String[] childNodeXmlIdArray) {
////        System.out.println("deleteFromParentDom: " + childNodeXmlIdArray);
//        if (this.isImdiChild()) {
//            this.domParentImdi.deleteFromParentDom(new String[]{this.xmlNodeId});
//        } else {
//            // save the node if it need saving
//            if (needsSaveToDisk) {
//                saveChangesToCache(false);
//            }
////            System.out.println("attempting to remove nodes");
//            try {
//                OurURL inUrlLocal = new OurURL(nodeUri.toURL());
//                Document nodDom;
//                nodDom = api.loadIMDIDocument(inUrlLocal, false);
//                for (String currentNodeXmlId : childNodeXmlIdArray) {
//                    IMDIElement target = new IMDIElement(null, currentNodeXmlId);
//                    api.removeIMDIElement(nodDom, target);
//                }
//                api.writeDOM(nodDom, this.getFile(), false);
//                reloadNode();
//            } catch (Exception ex) {
//                GuiHelper.linorgBugCatcher.logError(ex);
//            }
//        }
//    }
//    public void deleteFeilds(ImdiField[] targetImdiFields) {
//        ArrayList<String> pathList = new ArrayList<String>();
//        for (ImdiField currentField : targetImdiFields) {
//            pathList.add(currentField.getFullXmlPath());
//        }
//        CmdiComponentBuilder componentBuilder = new CmdiComponentBuilder();
//        boolean result = componentBuilder.removeChildNodes(this, pathList.toArray(new String[]{}));
//
////        deleteFromDomViaId(domIdList.toArray(new String[]{}));
//    }
    // this is used to delete an IMDI node from a corpus branch
    public void deleteCorpusLink(ImdiTreeObject[] targetImdiNodes) {
        // TODO: There is an issue when deleting child nodes that the remaining nodes xml path (x) will be incorrect as will the xmlnode id hence the node in a table may be incorrect after a delete
        if (needsSaveToDisk) {
            saveChangesToCache(false);
        }
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
            this.getParentDomNode().loadImdiDom();
        }
//        for (ImdiTreeObject currentChildNode : targetImdiNodes) {
////            currentChildNode.clearIcon();
//            TreeHelper.getSingleInstance().updateTreeNodeChildren(currentChildNode);
//        }
        for (ImdiTreeObject removedChild : targetImdiNodes) {
            removedChild.removeFromAllContainers();
        }
        this.getParentDomNode().clearIcon();
        this.getParentDomNode().clearChildIcons();
        clearIcon(); // this must be cleared so that the leaf / branch flag gets set
    }

    public boolean hasCatalogue() {
        for (ImdiTreeObject childNode : childArray) {
//            String currentChildPath = currentLinkPair[0];
//            ImdiTreeObject childNode = ImdiLoader.getSingleInstance().getImdiObject(null, currentChildPath);
            //childNode.waitTillLoaded(); // if the child nodes have not been loaded this will fail so we must wait here
            if (childNode.isCatalogue()) {
                return true;
            }
        }
        return false;
    }

    public boolean addCorpusLink(ImdiTreeObject targetImdiNode) {
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
                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Error adding node, could not wait for file to load", "Loading Error");
                return false;
            }
            if (currentChildPath.equals(targetImdiNode.getUrlString())) {
                linkAlreadyExists = true;
            }
        }
        if (targetImdiNode.getUrlString().equals(this.getUrlString())) {
            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Cannot link or move a node into itself", null);
            return false;
        }
        if (linkAlreadyExists) {
            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue(targetImdiNode + " already exists in " + this + " and will not be added again", null);
            return false;
        } else {
            // if link is not already there
            // if needs saving then save now while you can
            // TODO: it would be nice to warn the user about this, but its a corpus node so maybe it is not important
            if (needsSaveToDisk) {
                saveChangesToCache(true);
            }
            bumpHistory();
            copyLastHistoryToCurrent(); // bump history is normally used afteropen and before save, in this case we cannot use that order so we must make a copy
            synchronized (getParentDomLockObject()) {
                metadataUtils.addCorpusLink(this.getURI(), new URI[]{targetImdiNode.getURI()});
            }
            //loadChildNodes(); // this must not be done here
//            clearIcon(); // this must be cleared so that the leaf / branch flag gets set
            return true;
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
                if (this.isCorpus()) {
                    if (ImdiTreeObject.isPathMetadata(clipBoardString) || ImdiTreeObject.isStringImdiChild(clipBoardString)) {
                        ImdiTreeObject clipboardNode = ImdiLoader.getSingleInstance().getImdiObject(null, conformStringToUrl(clipBoardString));
                        if (LinorgSessionStorage.getSingleInstance().pathIsInsideCache(clipboardNode.getFile())) {
                            if (!(ImdiTreeObject.isStringImdiChild(clipBoardString) && (!this.isSession() && !this.isImdiChild()))) {
                                if (this.getFile().exists()) {
                                    // this must use merge like favoirite to prevent instances end endless loops in corpus branches
                                    new MetadataBuilder().requestAddNode(this, "copy of " + clipboardNode, clipboardNode);
                                } else {
                                    LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("The target node's file does not exist", null);
                                }
                            } else {
                                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Cannot paste session subnodes into a corpus", null);
                            }
                        } else {
                            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("The target file is not in the cache", null);
                        }
                    } else {
                        LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Pasted string is not and IMDI file", null);
                    }
                } else {
                    LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Only corpus branches can be pasted into at this stage", null);
                }
            }
        } catch (Exception ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
        }
    }

    /**
     * Saves the current changes from memory into a new imdi file on disk.
     * Previous imdi files are renamed and kept as a history.
     * the caller is responsible for reloading the node if that is required
     */
    public void saveChangesToCache(boolean updateUI) {
        if (this.isImdiChild()) {
            getParentDomNode().saveChangesToCache(updateUI);
            return;
        }
        System.out.println("saveChangesToCache");
        LinorgJournal.getSingleInstance().clearFieldChangeHistory();
        if (!this.isLocal() /*nodeUri.getScheme().toLowerCase().startsWith("http") */) {
            System.out.println("should not try to save remote files");
            return;
        }
        ArrayList<FieldUpdateRequest> fieldUpdateRequests = new ArrayList<FieldUpdateRequest>();
        Vector<ImdiField[]> allFields = new Vector<ImdiField[]>();
        getAllFields(allFields);
        for (Enumeration<ImdiField[]> fieldsEnum = allFields.elements(); fieldsEnum.hasMoreElements();) {
            {
                ImdiField[] currentFieldArray = fieldsEnum.nextElement();
                for (int fieldCounter = 0; fieldCounter < currentFieldArray.length; fieldCounter++) {
                    ImdiField currentField = currentFieldArray[fieldCounter];
                    if (currentField.fieldNeedsSaveToDisk()) {
                        FieldUpdateRequest currentFieldUpdateRequest = new FieldUpdateRequest();
                        currentFieldUpdateRequest.keyNameValue = currentField.getKeyName();
                        currentFieldUpdateRequest.fieldOldValue = currentField.originalFieldValue;
                        currentFieldUpdateRequest.fieldNewValue = currentField.getFieldValue();
                        currentFieldUpdateRequest.fieldPath = currentField.getFullXmlPath();
                        currentFieldUpdateRequest.fieldLanguageId = currentField.getLanguageId();
                        fieldUpdateRequests.add(currentFieldUpdateRequest);
                    }
                }
            }
        }
        CmdiComponentBuilder componentBuilder = new CmdiComponentBuilder();
        boolean result = componentBuilder.setFieldValues(this, fieldUpdateRequests.toArray(new FieldUpdateRequest[]{}));
        if (result != true) {
            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Error saving changes to disk, check the log file via the help menu for more information.", "Save");
        } else {
            this.needsSaveToDisk = false;
//            // update the icon to indicate the change
//            setImdiNeedsSaveToDisk(null, false);
        }
//        clearIcon(); this is called by setImdiNeedsSaveToDisk
    }

    /**
     * Saves the node dom into the local cache.
     * Before this is called it is recommended to confirm that the destinationDirectory path already exist and is correct, otherwise unintended directories maybe created
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
//                String destinationPath = GuiHelper.linorgSessionStorage.getSaveLocation(this.getUrlString());
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
    public void addField(ImdiField fieldToAdd) {
//        System.addField:out.println("addField: " + this.getUrlString() + " : " + fieldToAdd.xmlPath + " : " + fieldToAdd.getFieldValue());
        ImdiField[] currentFieldsArray = fieldHashtable.get(fieldToAdd.getTranslateFieldName());
        if (currentFieldsArray == null) {
            currentFieldsArray = new ImdiField[]{fieldToAdd};
        } else {
//            System.out.println("appendingField: " + fieldToAdd);
            ImdiField[] appendedFieldsArray = new ImdiField[currentFieldsArray.length + 1];
            System.arraycopy(currentFieldsArray, 0, appendedFieldsArray, 0, currentFieldsArray.length);
            appendedFieldsArray[appendedFieldsArray.length - 1] = fieldToAdd;
            currentFieldsArray = appendedFieldsArray;

//            for (ImdiField tempField : currentFieldsArray) {
//                System.out.println("appended fields: " + tempField);
//            }
        }
        fieldHashtable.put(fieldToAdd.getTranslateFieldName(), currentFieldsArray);

        if (fieldToAdd.xmlPath.endsWith(".ResourceLink") && fieldToAdd.parentImdi.isImdiChild()/* && fieldToAdd.parentImdi.getUrlString().contains("MediaFile")*/) {
            resourceUrlField = fieldToAdd;
            MimeHashQueue.getSingleInstance().addToQueue(this);
        }
    }

    /**
     * Adds a field to the imdi node and creates imdi child nodes if required.
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
////                    GuiHelper.linorgSessionStorage.updateCache(getFullResourceURI());
////                }
//                mimeHashQueue.addToQueue(this);
//            }
//        } else {
//            // pass the label to the child nodes
//            String childsName = fieldToAdd.translatedPath.substring(childLevel, nextChildLevel);
//            //String parentName = fieldLabel.substring(0, firstSeparator);
//            debugOut("childsName: " + childsName);
//            if (!childrenHashtable.containsKey(childsName)) {
//                ImdiTreeObject tempImdiTreeObject = GuiHelper.imdiLoader.getImdiObject(childsName, this.getUrlString() + "#" + fieldToAdd.xmlPath);
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
     * @return A hashtable of the fields
     */
    public Hashtable<String, ImdiField[]> getFields() {
        // store the Hastable for next call
        // if hashtable is null then load from imdi
        return fieldHashtable;
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
     * @return The string comparison result.
     */
    public int compareTo(Object o) throws ClassCastException {
        ImdiTreeNodeSorter imdiTreeNodeSorter = new ImdiTreeNodeSorter();
        return imdiTreeNodeSorter.compare(this, o);
    }

    public synchronized void notifyLoaded() {
        System.out.println("notifyAll");
        getParentDomNode().notifyAll();
    }

    public synchronized boolean waitTillLoaded() {
        System.out.println("waitTillLoaded");
        if (isLoading()) {
            System.out.println("isLoading");
            try {
                getParentDomNode().wait();
                System.out.println("wait");
                if (isLoading()) {
                    System.out.println("but still loading");
                }
            } catch (Exception ex) {
                GuiHelper.linorgBugCatcher.logError(ex);
                return false;
            }
        }
        return true;
    }

    public void updateLoadingState(int countChange) {
        if (this != getParentDomNode()) {
            getParentDomNode().updateLoadingState(countChange);
        } else {
            synchronized (loadingCountLock) {
                isLoadingCount += countChange;
            }
            System.out.println("isLoadingCount: " + isLoadingCount);
            if (!isLoading()) {
//                    this.notifyAll();
                clearChildIcons();
                clearIcon();
            }
        }
    }

    public boolean isLoading() {
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

    public String refreshStringValue() {
        if (isLoading()) {
//            if (lastNodeText.length() > 0) {
//                return lastNodeText;
//            } else {asdasdasd
////                if (nodeText != null && nodeText.length() > 0) {
            return lastNodeText;
//            }
        } else if (lastNodeText.equals("loading imdi...") && getParentDomNode().imdiDataLoaded) {
            lastNodeText = "                      ";
        }
//        if (commonFieldPathString != null && commonFieldPathString.length() > 0) {
//            // todo: use the commonFieldPathString as the node name if not display preference is set or the ones that are set have no value
//            nodeText = commonFieldPathString;
//        }
        boolean foundPreferredNameField = false;
        getLabelString:
        for (String currentPreferredName : this.getNodeTemplate().preferredNameFields) {
            //System.out.println("currentField: " + currentPreferredName);
            for (ImdiField[] currentFieldArray : fieldHashtable.values()) {
//                System.out.println(currentFieldArray[0].getFullXmlPath().replaceAll("\\(\\d+\\)", "") + " : " + currentPreferredName);
                if (currentFieldArray[0].getFullXmlPath().replaceAll("\\(\\d+\\)", "").equals(currentPreferredName)) {
                    for (ImdiField currentField : currentFieldArray) {
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
            ImdiField[] currentFieldArray = fieldHashtable.get(currentPreferredName);
            if (currentFieldArray != null) {
                for (ImdiField currentField : currentFieldArray) {
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

        if (!foundPreferredNameField && isCmdiMetaDataNode() /*&& fieldHashtable.size() > 0 && domParentImdi == this*/ && this.nodeTemplate != null) {
//            if (this.getNodeTemplate().preferredNameFields.length == 0) {
//                nodeText = "no field specified to name this node (" + this.nodeTemplate.getTemplateName() + ")";
//            } else {
            nodeText = "unnamed (" + this.nodeTemplate.getTemplateName() + ")";
//            }
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
                    GuiHelper.linorgBugCatcher.logError(encodingException);
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
        if (lastNodeText.length() == 0) {
            lastNodeText = "                      ";
        }
        return lastNodeText;// + "-" + clearIconCounterGlobal + "-" + clearIconCounter;
//            }
    }

    /**
     * Tests if there is file associated with this node and if it is an archivable type.
     * The file could be either a resource file (getResource) or a loose file (getUrlString).
     * @return boolean
     */
    public boolean isArchivableFile() {
        return mpiMimeType != null;
    }

    /**
     * Tests if a resource file (local or remote) is associated with this node.
     * @return boolean
     */
    public boolean hasResource() {
        return resourceUrlField != null;
    }

    /**
     * Tests if a local resource file is associated with this node.
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
        return !this.isImdiChild() && new File(this.getFile().getAbsolutePath() + ".0").exists();
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
        try {
            if (historyVersion.equals(".x")) {
                this.getFile().delete();
                new File(this.getFile().getAbsolutePath() + ".x").renameTo(this.getFile());
            } else {
                LinorgWindowManager.getSingleInstance().offerUserToSaveChanges();
                if (!new File(this.getFile().getAbsolutePath() + ".x").exists()) {
                    this.getFile().renameTo(new File(this.getFile().getAbsolutePath() + ".x"));
                } else {
                    this.getFile().delete();
                }
                InputStream hisoryFile = new FileInputStream(new File(this.getFile().getAbsolutePath() + historyVersion));
                OutputStream activeVersionFile = new FileOutputStream(this.getFile(), true);

                byte[] copyBuffer = new byte[1024];
                int len;
                while ((len = hisoryFile.read(copyBuffer)) > 0) {
                    activeVersionFile.write(copyBuffer, 0, len);
                }
                hisoryFile.close();
                activeVersionFile.close();
            }
            ImdiLoader.getSingleInstance().requestReload(getParentDomNode());
        } catch (Exception e) {
            // user canceled the save action
            // todo: alert user that nothing was done
            return false;
        }
        return true;
    }

    /*
     * Increment the history file so that a new current file can be saved without overwritting the old
     */
    public void bumpHistory() {
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
                nextFile.renameTo(lastFile);
                System.out.println("renaming: " + nextFile + " : " + lastFile);
            } else {
                headVersion.renameTo(lastFile);
                System.out.println("renaming: " + headVersion + " : " + lastFile);
            }
        }
    }

    private void copyLastHistoryToCurrent() {
        try {
            FileOutputStream outFile = new FileOutputStream(this.getFile());
            InputStream inputStream = new FileInputStream(new File(this.getFile().getAbsolutePath() + ".0"));
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
            outFile.close();
        } catch (IOException iOException) {
            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Could not copy file when recovering from the last history file.", "Recover History");
            GuiHelper.linorgBugCatcher.logError(iOException);
        }
    }

    /**
     * Resolves the full path to a resource file if it exists.
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
                        GuiHelper.linorgBugCatcher.logError(urise);
                    }
                }
            }
            return resourceUri;
        } catch (Exception urise) {
            GuiHelper.linorgBugCatcher.logError(urise);
            System.out.println("URISyntaxException: " + urise.getMessage());
            return null;
        }
    }

    /**
     * Gets the ULR string provided when the node was created.
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
     * Gets the ImdiTreeObject parent of an imdi child node.
     * The returned node will be able to reload/save the dom for this node.
     * Only relevant for imdi child nodes.
     * @return ImdiTreeObject
     */
    public ImdiTreeObject getParentDomNode() {
//        System.out.println("nodeUri: " + nodeUri);
        if (domParentImdi == null) {
            if (nodeUri.getFragment() != null) {
                try {
                    //domParentImdi = ImdiLoader.getSingleInstance().getImdiObject(null, new URI(nodeUri.getScheme(), nodeUri.getUserInfo(), nodeUri.getHost(), nodeUri.getPort(), nodeUri.getPath(), nodeUri.getQuery(), null /* fragment removed */));
                    // the uri is created via the uri(string) constructor to prevent re-url-encoding the url
                    domParentImdi = ImdiLoader.getSingleInstance().getImdiObject(null, new URI(nodeUri.toString().split("#")[0] /* fragment removed */));
//                    System.out.println("nodeUri: " + nodeUri);
                } catch (URISyntaxException ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            } else {
                domParentImdi = this;
            }
        }
        return domParentImdi;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public boolean isMetaDataNode() {
        if (nodeUri != null /* && nodDom != null*/) {
            if (isImdiChild()) {
                return true;
            } else {
                return ImdiTreeObject.isPathMetadata(nodeUri.getPath());
            }
        }
        return false;
    }

    public boolean isCmdiMetaDataNode() {
        if (nodeUri != null /* && nodDom != null*/) {
            if (isImdiChild()) {
                return getParentDomNode().isCmdiMetaDataNode();
            } else {
                return ImdiTreeObject.isPathCmdi(nodeUri.getPath());
            }
        }
        return false;
    }

    /**
     * Tests if this node represents an imdi file or if if it represents a child node from an imdi file (created by adding fields with child nodes).
     * @return boolean
     */
    public boolean isImdiChild() {
        return ImdiTreeObject.isStringImdiChild(this.getUrlString());
    }

    public boolean isSession() {
        // test if this node is a session
        ImdiField[] nameFields = fieldHashtable.get("Name");
        if (nameFields != null) {
            return nameFields[0].xmlPath.equals(MetadataReader.imdiPathSeparator + "METATRANSCRIPT" + MetadataReader.imdiPathSeparator + "Session" + MetadataReader.imdiPathSeparator + "Name");
        }
        return false;
    }

    /**
     * Tests if this node is a meta node that contains no fields and only child nodes, such as the Languages, Actors, MediaFiles nodes etc..
     * @return boolean
     */
    public boolean isEmptyMetaNode() {
        return this.getFields().size() == 0;
    }

    public boolean isCatalogue() {
        // test if this node is a catalogue
        ImdiField[] nameFields = fieldHashtable.get("Name");
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
        ImdiField[] nameFields = fieldHashtable.get("Name");
        if (nameFields != null) {
            return nameFields[0].xmlPath.equals(MetadataReader.imdiPathSeparator + "METATRANSCRIPT" + MetadataReader.imdiPathSeparator + "Corpus" + MetadataReader.imdiPathSeparator + "Name");
        }
        return false;
    }

    public boolean isLocal() {
        if (nodeUri != null) {
            return ImdiTreeObject.isStringLocal(nodeUri.getScheme());
        } else {
            return false;
        }
    }

    public boolean isEditable() {
        if (isLocal()) {
            return (LinorgSessionStorage.getSingleInstance().pathIsInsideCache(this.getFile()))
                    || LinorgSessionStorage.getSingleInstance().pathIsInFavourites(this.getFile());
        } else {
            return false;

        }
    }

    /**
     * Returns the URI object for this node.
     * @return A URI that this node represents.
     */
    public URI getURI() {
        try {
            return nodeUri; // new URI(nodeUri.toString()); // a copy of
        } catch (Exception ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
            return null;
        }
    }

    public File getFile() {
//        System.out.println("getFile: " + nodeUri.toString());
        if (nodeUri.getScheme().toLowerCase().equals("file")) {
            try {
                return new File(new URI(nodeUri.toString().split("#")[0] /* fragment removed */));
            } catch (Exception urise) {
                System.err.println("nodeUri: " + nodeUri);
                GuiHelper.linorgBugCatcher.logError(urise);
            }
        }
        return null;
    }

    public String getParentDirectory() {
        String parentPath = this.getUrlString().substring(0, this.getUrlString().lastIndexOf("/")) + "/"; // this is a url so don't use the path separator
        return parentPath;
    }

    public void registerContainer(Object containerToAdd) {
//        System.out.println("registerContainer: " + containerToAdd + " : " + this);
        if (!getParentDomNode().imdiDataLoaded && !isLoading()) { // TODO: this is probably not the best way to do this and might be better in a manager class
            ImdiLoader.getSingleInstance().requestReload(getParentDomNode());
        }
        if (containerToAdd != null) {
            // todo: handle null here more agressively
            if (!containersOfThisNode.contains(containerToAdd)) {
                containersOfThisNode.add(containerToAdd);
            }
        }
    }

    public Object[] getRegisteredContainers() {
        if (containersOfThisNode != null && containersOfThisNode.size() > 0) {
            return containersOfThisNode.toArray();
        } else {
            return new Object[]{};
        }
    }

    /**
     * Removes a UI containers from the list of containers interested in this node.
     * @param containerToRemove The container to be removed from the list.
     */
    public void removeContainer(Object containerToRemove) {
        // TODO: make sure that containers are removed when a node is removed from the tree, otherwise memory will not get freed
//        System.out.println("de registerContainer: " + containerToRemove);
        containersOfThisNode.remove(containerToRemove);
    }

    /**
     * Clears the icon for all the imdi child nodes of this node.
     * Used when loading a session dom.
     */
    public void clearChildIcons() {
//        System.out.println("clearChildIconsParent: " + this);
        for (ImdiTreeObject currentChild : childArray) {
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
        icon = ImdiIcons.getSingleInstance().getIconForImdi(ImdiTreeObject.this); // to avoid a race condition (where the loading icons remains after load) this is also set here rather than nulling the icon
//                System.out.println("clearIcon invokeLater" + ImdiTreeObject.this.toString());
//                System.out.println("containersOfThisNode: " + containersOfThisNode.size());
        // here we need to cause an update in the tree and table gui so that the new icon can be loaded
        for (Enumeration containersIterator = containersOfThisNode.elements(); containersIterator.hasMoreElements();) { // changed back to a vector due to threading issues here
            try { // TODO: the need for this try catch indicates that there is a threading issue in the way that imdichild nodes are reloaded within an imdi parent node and this should be reorganised to be more systematic and hierarchical
                Object currentContainer = containersIterator.nextElement();
//                    System.out.println("currentContainer: " + currentContainer.toString());
                if (currentContainer instanceof ImdiTableModel) {
                    ((ImdiTableModel) currentContainer).requestReloadTableData(); // this must be done because the fields have been replaced and nead to be reloaded in the tables
                }
                if (currentContainer instanceof ImdiChildCellEditor) {
                    ((ImdiChildCellEditor) currentContainer).updateEditor(ImdiTreeObject.this);
                }
                if (currentContainer instanceof ImdiTree) {
                    ((ImdiTree) currentContainer).requestResort();
                }
            } catch (java.util.NoSuchElementException ex) {
                GuiHelper.linorgBugCatcher.logError(ex);
            }
        }
//            }
//        });
//        System.out.println("end clearIcon: " + this);
    }

    public void removeFromAllContainers() {
        for (Enumeration containersIterator = containersOfThisNode.elements(); containersIterator.hasMoreElements();) { // changed back to a vector due to threading issues here
            try {
                Object currentContainer = containersIterator.nextElement();
                if (currentContainer instanceof ImdiTableModel) {
                    ((ImdiTableModel) currentContainer).removeImdiObjects(new ImdiTreeObject[]{this}); // this must be done because the fields have been replaced and nead to be reloaded in the tables
                }
                if (currentContainer instanceof ImdiChildCellEditor) {
                    ((ImdiChildCellEditor) currentContainer).stopCellEditing();
                }
                if (currentContainer instanceof ImdiTree) {
                    ImdiTree currentTreeNode = (ImdiTree) currentContainer;
                    currentTreeNode.requestResort();
                }
            } catch (java.util.NoSuchElementException ex) {
                GuiHelper.linorgBugCatcher.logError(ex);
            }
        }
//            }
//        });
//        System.out.println("end clearIcon: " + this);
    }

    public boolean isFavorite() {
        if (!this.isLocal()) {
            // only local files can be favourites
            return false;
        }
        return LinorgSessionStorage.getSingleInstance().pathIsInFavourites(this.getFile());
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
     * @return The icon for this node.
     */
    public ImageIcon getIcon() {
        if (icon == null) {
            return ImdiIcons.getSingleInstance().loadingIcon;
        }
        return icon;
    }
}
