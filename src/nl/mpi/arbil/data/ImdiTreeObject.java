package nl.mpi.arbil.data;

import nl.mpi.arbil.*;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import mpi.imdi.api.*;
import mpi.util.OurURL;
import org.w3c.dom.Document;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import mpi.util.URIUtil;

/**
 * Document   : ImdiTreeObject
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
public class ImdiTreeObject implements Comparable {
    // TODO: move the api into a wrapper class

    public static IMDIDom api = new IMDIDom();
    public ArbilTemplate currentTemplate;
//    static ImdiIcons imdiIcons = new ImdiIcons();
    private static Vector listDiscardedOfAttributes = new Vector(); // a list of all unused imdi attributes, only used for testing    
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
    private boolean needsSaveToDisk;
    private String nodeText, lastNodeText = "loading imdi...";
//    private boolean nodeTextChanged = false;
    private URI nodeUri;
    public ImdiField resourceUrlField;
    public boolean isDirectory;
    private ImageIcon icon;
    private boolean nodeEnabled;
    // merge to one array of domid url imditreeobject
    private String[][] childLinks = new String[0][0]; // each element in this array is an array [linkPath, linkId]. When the link is from an imdi the id will be the node id, when from get links or list direcotry id will be null
    private Vector/*<Component>*/ containersOfThisNode;
    private int isLoadingCount = 0;
    final private Object loadingCountLock = new Object();
    public boolean lockedByLoadingThread = false;
//    private boolean isFavourite;
    public boolean hasArchiveHandle = false;
//    public boolean autoLoadChildNodes = false;
    public Vector<String[]> addQueue;
    public boolean scrollToRequested = false;
//    public Vector<ImdiTreeObject> mergeQueue;
//    public boolean jumpToRequested = false; // dubious about this being here but it seems to fit here best
    private ImdiTreeObject domParentImdi = null; // the parent imdi containing the dom, only set for imdi child nodes
    public String xmlNodeId = null; // only set for imdi child nodes and is the xml node id relating to this imdi tree object
    public File thumbnailFile = null;
    private final Object domLockObject = new Object();

    protected ImdiTreeObject(URI localUri) {
//        debugOut("ImdiTreeObject: " + localNodeText + " : " + localUrlString);
        containersOfThisNode = new Vector<Component>();
        addQueue = new Vector<String[]>();
        nodeUri = localUri;
        initNodeVariables();
    }

    // set the node text only if it is null
    public void setNodeText(String localNodeText) {
        if (nodeText == null) {
            nodeText = localNodeText;
        }
    }

    static public URI conformStringToUrl(String inputUrlString) {
//        System.out.println("conformStringToUrl in: " + inputUrlString);
        URI returnUrl = null;
        try {
//            localUrlString = localUrlString.replace("\\", "/");
            if (!inputUrlString.toLowerCase().startsWith("http:") && !inputUrlString.toLowerCase().startsWith("file:")) {
                returnUrl = new File(inputUrlString).toURI();
            } else {
                returnUrl = new URI(inputUrlString);
            }
        } catch (Exception ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
        }
        return returnUrl;
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

    static public boolean isStringImdiHistoryFile(String urlString) {
//        System.out.println("isStringImdiHistoryFile" + urlString);
//        System.out.println("isStringImdiHistoryFile" + urlString.replaceAll(".imdi.[0-9]*$", ".imdi"));
        return isStringImdi(urlString.replaceAll(".imdi.[0-9]*$", ".imdi"));
    }

    static public boolean isStringImdi(String urlString) {
        return urlString.endsWith(".imdi");
    }

    static public boolean isStringImdiChild(String urlString) {
        return urlString.contains("#.METATRANSCRIPT");
    }

    static public void requestRootAddNode(String nodeType, String nodeTypeDisplayName) {
        ImdiTreeObject imdiTreeObject;
        imdiTreeObject = new ImdiTreeObject(LinorgSessionStorage.getSingleInstance().getNewImdiFileName(LinorgSessionStorage.getSingleInstance().getSaveLocation("")));
        imdiTreeObject.requestAddNode(nodeType, nodeTypeDisplayName);
    }
    // end static methods for testing imdi file and object types

    public boolean getNeedsSaveToDisk() {
        // when the dom parent node is saved all the sub nodes are also saved so we need to clear this flag
        if (needsSaveToDisk && !this.getParentDomNode().needsSaveToDisk) {
            needsSaveToDisk = false;
        }
        return needsSaveToDisk;
    }

    public void setImdiNeedsSaveToDisk(boolean imdiNeedsSaveToDisk, boolean updateUI) {
        if (resourceUrlField != null && resourceUrlField.fieldNeedsSaveToDisk) {
            hashString = null;
            mpiMimeType = null;
            thumbnailFile = null;
            typeCheckerMessage = null;
            MimeHashQueue.getSingleInstance().addToQueue(this);
        }
        if (isImdiChild()) {
            this.getParentDomNode().setImdiNeedsSaveToDisk(imdiNeedsSaveToDisk, updateUI);
        } else {
            if (this.needsSaveToDisk != imdiNeedsSaveToDisk) {
                if (imdiNeedsSaveToDisk) {
                    ImdiLoader.getSingleInstance().addNodeNeedingSave(this);
                } else {
                    ImdiLoader.getSingleInstance().removeNodesNeedingSave(this);
                }
            }
        }
        this.needsSaveToDisk = imdiNeedsSaveToDisk;
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
        if (!isImdi() && isLocal() && mpiMimeType != null) {
            // add the mime type
            ImdiField mimeTypeField = new ImdiField(this, "Format", this.mpiMimeType);
            mimeTypeField.fieldID = "x" + fieldHashtable.size();
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
        if (currentTemplate == null) {
            // this will be overwritten when the imdi file is read, provided that a template is specified in the imdi file
            currentTemplate = ArbilTemplateManager.getSingleInstance().getCurrentTemplate();
        }
        fieldHashtable = new Hashtable<String, ImdiField[]>();
        imdiDataLoaded = false;
        hashString = null;
        //mpiMimeType = null;
        matchesInCache = 0;
        matchesRemote = 0;
        matchesLocalFileSystem = 0;
        fileNotFound = false;
        setImdiNeedsSaveToDisk(false, false);
//    nodeText = null;
//    urlString = null;
//        resourceUrlField = null;
        isDirectory = false;
        icon = null;
        nodeEnabled = true;
//        isLoadingCount = true;
        if (nodeUri != null) {
            if (!isImdi() && isLocal()) {
                File fileObject = getFile();
                if (fileObject != null) {
                    this.isDirectory = fileObject.isDirectory();
                }
                if (fileObject.exists()) {
                    try {
                        int currentFieldId = 1;
                        nodeText = fileObject.getName();
                        // TODO move this to the mime hash queue
                        ImdiField sizeField = new ImdiField(this, "Size", "");
                        sizeField.fieldID = "x" + currentFieldId++;
                        addField(sizeField);
                        // add the modified date
                        Date mtime = new Date(fileObject.lastModified());
                        String mTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(mtime);
                        ImdiField dateField = new ImdiField(this, "last modified", mTimeString);
                        dateField.fieldID = "x" + currentFieldId++;
                        addField(dateField);
                        // get exif tags
//                System.out.println("get exif tags");
                        ImdiField[] exifFields = ImdiSchema.getSingleInstance().getExifMetadata(this);
                        for (ImdiField currentField : exifFields) {
                            currentField.fieldID = "x" + currentFieldId++;
                            addField(currentField);
//                    System.out.println(currentField.fieldValue);
                        }
                    } catch (Exception ex) {
                        GuiHelper.linorgBugCatcher.logError(this.getUrlString() + "\n" + fileObject.getAbsolutePath(), ex);
                    }
                }
            }
            if (!isImdi() && nodeText == null) {
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

    synchronized public void loadImdiDom() {
        System.out.println("loadImdiDom: " + nodeUri.toString());
        if (getParentDomNode() != this) {
            getParentDomNode().loadImdiDom();
        } else {
            synchronized (domLockObject) {
                initNodeVariables(); // this might be run too often here but it must be done in the loading thread and it also must be done when the object is created
                if (!isImdi() && !isDirectory() && isLocal()) {
                    // if it is an not imdi or a loose file but not a direcotry then get the md5sum
                    MimeHashQueue.getSingleInstance().addToQueue(this);
                    imdiDataLoaded = true;
                }
                if (this.isDirectory()) {
                    getDirectoryLinks();
                    imdiDataLoaded = true;
//            clearIcon();
                }
                if (isImdi()) {
                    Document nodDom = null;
                    // cacheLocation will be null if useCache = false hence no file has been saved
//        String cacheLocation = null;
                    try {
                        //System.out.println("tempUrlString: " + tempUrlString);
                        if (false) {
                            // TODO: resolve why this is not functioning, till then the subsequent stanza is used
                            try {
                                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                                nodDom = builder.parse(nodeUri.toURL().openStream());
                            } catch (Exception ex) {
                                GuiHelper.linorgBugCatcher.logError(ex);
//                    System.out.println("Could not parse dom: " + this.getUrlString());
                            }
                        } else {
                            OurURL inUrlLocal = null;
                            inUrlLocal = new OurURL(nodeUri.toURL());
                            nodDom = api.loadIMDIDocument(inUrlLocal, false);
                            if (this.isLocal() && this.getFile().exists() && nodDom == null) { // if the file is local and the file exits then the we sould be able to expect the api to open the file so warn the user that something unusal occured
                                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("The IMDI API could not load the file\n" + this.getUrlString(), "Load IMDI File");
                                GuiHelper.linorgBugCatcher.logError(new Exception(ImdiTreeObject.api.getMessage()));
                                // todo: if the file is zero bytes offer to revert to a previous version if it exists
                            }
                        }

                        // only read the fields into imdi tree objects if it is not going to be saved to the cache
//            if (!useCache) {
                        if (nodDom == null) {
                            if (this.getFile().exists()) {
                                nodeText = "Could not load IMDI";
                            } else {
                                nodeText = "File not found";
                                fileNotFound = true;
                            }
                        } else {
                            //set the string name to unknown, it will be updated in the tostring function
                            nodeText = "unknown";
                            Vector<String[]> childLinksTemp = new Vector<String[]>();
                            Hashtable<ImdiTreeObject, HashSet<ImdiTreeObject>> parentChildTree = new Hashtable<ImdiTreeObject, HashSet<ImdiTreeObject>>();
                            // load the fields from the imdi file
                            ImdiSchema.getSingleInstance().iterateChildNodes(this, childLinksTemp, nodDom.getFirstChild(), "", parentChildTree);
                            childLinks = childLinksTemp.toArray(new String[][]{});
                            ImdiTreeObject[] childArrayTemp = new ImdiTreeObject[childLinks.length];
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
                                            if (currentContainer instanceof DefaultMutableTreeNode) {
                                                DefaultMutableTreeNode currentTreeNode = (DefaultMutableTreeNode) currentContainer;
                                                DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) ((DefaultMutableTreeNode) currentContainer).getParent();
                                                if (parentNode != null) { // TODO this could be reduced as it is also sort of done in clear icon
                                                    TreeHelper.getSingleInstance().addToSortQueue(parentNode);
                                                } else {
                                                    TreeHelper.getSingleInstance().addToSortQueue(currentTreeNode);
                                                }
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
                    getParentDomNode().notifyAll();
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
        if (nodeFile != null) {
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
        if (this.isSession() || this.isImdiChild()) {
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
        if (this.getFile().getName().endsWith(".imdi")) {
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
     * Add a new node based on a template and optionally attach a resource
     * @return String path to the added node
     */
    public URI addChildNode(String nodeType, String targetXmlPath, String resourcePath, String mimeType) {
        System.out.println("addChildNode:: " + nodeType + " : " + resourcePath);
        System.out.println("targetXmlPath:: " + targetXmlPath);
        if (needsSaveToDisk) {
            saveChangesToCache(true);
        }
        URI addedNodePath = null;
        ImdiTreeObject destinationNode;
        if (currentTemplate.isImdiChildType(nodeType) || (resourcePath != null && this.isSession())) {
            System.out.println("adding to current node");
            destinationNode = this;
            try {
                synchronized (domLockObject) {
                    OurURL inUrlLocal = new OurURL(nodeUri.toURL());
                    System.out.println("inUrlLocal: " + inUrlLocal);
                    Document nodDom = api.loadIMDIDocument(inUrlLocal, false);
                    if (nodDom == null) {
                        LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("The IMDI file could not be opened via the IMDI API", "Add Node");
                        GuiHelper.linorgBugCatcher.logError(new Exception(ImdiTreeObject.api.getMessage()));
                    } else {
//                api.writeDOM(nodDom, this.getFile(), true); // remove the id attributes
//                System.out.println("addChildNode: insertFromTemplate");
//                System.out.println("inUrlLocal: " + inUrlLocal);
                        addedNodePath = ImdiSchema.getSingleInstance().insertFromTemplate(this.currentTemplate, this.getFile(), getSubDirectory(), nodeType, targetXmlPath, nodDom, resourcePath, mimeType);
//                System.out.println("addChildNode: save");
//                nodDom = api.loadIMDIDocument(inUrlLocal, false);
                        bumpHistory();
                        api.writeDOM(nodDom, this.getFile(), false); // add the id attributes
                    }
                }
            } catch (Exception ex) {
//                System.out.println("addChildNode: " + ex.getMessage());
                GuiHelper.linorgBugCatcher.logError(ex);
            }
//            needsSaveToDisk = true;
        } else {
            System.out.println("adding new node");
            URI targetFileURI = LinorgSessionStorage.getSingleInstance().getNewImdiFileName(getSubDirectory());
            addedNodePath = ImdiSchema.getSingleInstance().addFromTemplate(new File(targetFileURI), nodeType);
            destinationNode = ImdiLoader.getSingleInstance().getImdiObject(null, targetFileURI);
            if (this.getFile().exists()) {
                this.addCorpusLink(destinationNode);
                reloadNode();
            } else {
                // TODO: this should not really be here
                TreeHelper.getSingleInstance().addLocation(destinationNode.getURI());
                TreeHelper.getSingleInstance().applyRootLocations();
            }
//            destinationNode.saveChangesToCache();
//            destinationNode.needsSaveToDisk = true;
        }
//        //load then save the dom via the api to make sure there are id fields to each node then reload this imdi object
        //        destinationNode.updateImdiFileNodeIds();

        // begin temp test
        //            ImdiField fieldToAdd1 = new ImdiField("test.field", "unset");
        //            fieldToAdd1.translateFieldName("test.field.translated");
        //            addableImdiChild.addField(fieldToAdd1, 0);
        // end temp test
        //for (Enumeration fieldsToAdd = GuiHelper.imdiFieldViews.getCurrentGlobalView().getAlwaysShowColumns(); fieldsToAdd.hasMoreElements();) {
        //        for (Enumeration fieldsToAdd = GuiHelper.imdiSchema.listFieldsFor(this, nodeType, getNextImdiChildIdentifier(), resourcePath); fieldsToAdd.hasMoreElements();) {
        //            String[] currentField = (String[]) fieldsToAdd.nextElement();
        //            System.out.println("fieldToAdd: " + currentField[0]);
        //            System.out.println("valueToAdd: " + currentField[1]);
        //            ImdiField fieldToAdd = new ImdiField(destinationNode, currentField[0], currentField[1]);
        //            //fieldToAdd.translateFieldName(nodePath + siblingSpacer);
        //            fieldToAdd.translateFieldName(currentField[0]);
        //            if (GuiHelper.linorgJournal.saveJournalEntry(fieldToAdd.parentImdi.getUrlString(), fieldToAdd.xmlPath, null, fieldToAdd.fieldValue)) {
        //                destinationNode.addField(fieldToAdd, 0, addedImdiNodes, false);
        //            }
        //        }
        //        if (destinationNode != this) {
        ////            System.out.println("adding to list of child nodes 1: " + destinationNode);
        //            childrenHashtable.put(destinationNode.getUrlString(), destinationNode);
        //        }
        return addedNodePath;
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
                System.out.println("containsFieldValue: " + currentField.fieldValue + ":" + searchValue);
                if (currentField.fieldValue.toLowerCase().contains(searchValue.toLowerCase())) {
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
                System.out.println("containsFieldValue: " + currentField.fieldValue + ":" + searchValue);
                if (currentField.fieldValue.toLowerCase().contains(searchValue.toLowerCase())) {
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
    public void deleteFeilds(ImdiField[] targetImdiFields) {
        ArrayList<String> domIdList = new ArrayList<String>();
        for (ImdiField currentField : targetImdiFields) {
            domIdList.add(currentField.fieldID);
        }
        deleteFromDomViaId(domIdList.toArray(new String[]{}));
    }

// this is used to delete an IMDI node from a corpus branch
    public void deleteCorpusLink(ImdiTreeObject[] targetImdiNodes) {
        // retrieve the node id for the link
        ArrayList<String> fieldIdList = new ArrayList<String>();
        String linkIdString = null;
        for (String[] currentLinkPair : childLinks) {
            String currentChildPath = currentLinkPair[0];
//                System.out.println("currentChildPath: " + currentChildPath);
            for (ImdiTreeObject currentImdiNode : targetImdiNodes) {
//                    System.out.println("targetImdiNode :  " + currentImdiNode.getUrlString());
                if (currentChildPath.equals(currentImdiNode.getUrlString())) {
//                      System.out.println("currentLinkPair[1]: " + currentLinkPair[1]);
                    linkIdString = currentLinkPair[1];
                    fieldIdList.add(linkIdString);
                }
            }
        }
        if (fieldIdList.size() > 0) {
            deleteFromDomViaId(fieldIdList.toArray(new String[]{}));
        }
        for (ImdiTreeObject currentImdiNode : targetImdiNodes) {
            if (currentImdiNode.isCatalogue()) {
                // the catalogue implemention in the imdi api requires special treatment and so must be done in this way not via deleteFromDomViaId
                // do this last because it might change the domid ordering
                deleteCatalogueLink();
            }
        }
        //loadChildNodes(); // this must not be done here
        clearIcon(); // this must be cleared so that the leaf / branch flag gets set
    }

    public void deleteCatalogueLink() {
        // the catalogue implemention in the imdi api requires special treatment and so must be done in this way not via deleteFromDomViaId 
        Document nodDom;
        try {
            if (needsSaveToDisk) {
                saveChangesToCache(false);
            }
            synchronized (domLockObject) {
                OurURL inUrlLocal = new OurURL(nodeUri.toURL());
                nodDom = api.loadIMDIDocument(inUrlLocal, false);
                if (nodDom == null) {
                    GuiHelper.linorgBugCatcher.logError(new Exception(ImdiTreeObject.api.getMessage()));
                    LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Error loading via the IMDI API", "Remove Catalogue");
                } else {
                    api.createIMDILink(nodDom, null, "", "", WSNodeType.CATALOGUE, "");
                    bumpHistory();
                    api.writeDOM(nodDom, this.getFile(), false);
                    reloadNode();
                }
            }
        } catch (Exception ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
        }
    }

    // this is used to delete imdi child nodes and to delete individual fields
    public void deleteFromDomViaId(String[] domIdArray) {
        // TODO: There is an issue when deleting child nodes that the remaining nodes xml path (x) will be incorrect as will the xmlnode id hence the node in a table may be incorrect after a delete
        Document nodDom;
        try {
            if (needsSaveToDisk) {
                saveChangesToCache(false);
            }
            synchronized (domLockObject) {
                OurURL inUrlLocal = new OurURL(nodeUri.toURL());
                nodDom = api.loadIMDIDocument(inUrlLocal, false);
                if (nodDom == null) {
                    GuiHelper.linorgBugCatcher.logError(new Exception(ImdiTreeObject.api.getMessage()));
                    LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Error removing via the IMDI API", "Remove");
                } else {
                    for (String currentImdiDomId : domIdArray) {
                        IMDIElement target = new IMDIElement(null, currentImdiDomId);
                        api.removeIMDIElement(nodDom, target);
                    }
                    bumpHistory();
                    api.writeDOM(nodDom, this.getFile(), false);
                    reloadNode();
                }
            }
        } catch (Exception ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
        }
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

            Document nodDom;
            try {
                synchronized (domLockObject) {
                    OurURL inUrlLocal = new OurURL(nodeUri.toURL());
                    nodDom = api.loadIMDIDocument(inUrlLocal, false);
                    if (nodDom == null) {
                        GuiHelper.linorgBugCatcher.logError(new Exception(ImdiTreeObject.api.getMessage()));
                        LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Error reading via the IMDI API", "Add Link");
                        return false;
                    } else {
                        int nodeType = WSNodeType.CORPUS;
                        if (targetImdiNode.isSession()) {
                            nodeType = WSNodeType.SESSION;            // url: IMDI location, for link normalization.  urlToLink: target URL
                            // linkName: for CorpusLink name / for InfoFile description
                            // linkType: WSNodeType value  spec: where to put the link in the IMDI,
                            // NOTE: spec should only be used for linkType InfoFile...
                            // public IMDILink createIMDILink(Document doc, OurURL url, String urlToLink, String linkName, int linkType, String spec);
                        } else if (targetImdiNode.isCatalogue()) {
                            nodeType = WSNodeType.CATALOGUE;
                        }
// TODO: at this point due to the api we cannot get the id of the newly created link, so we will probably have to unload this object and reload the dom
                        System.out.println("createIMDILink: " + targetImdiNode.getUrlString());
                        api.createIMDILink(nodDom, inUrlLocal, targetImdiNode.getUrlString(), /*targetImdiNode.toString()*/ "", nodeType, "");
                        bumpHistory();
                        api.writeDOM(nodDom, this.getFile(), false);
                    }
                }
            } catch (Exception ex) {
                GuiHelper.linorgBugCatcher.logError(ex);
//            System.out.println("Exception: " + ex.getMessage());
            }
            //loadChildNodes(); // this must not be done here
//            clearIcon(); // this must be cleared so that the leaf / branch flag gets set
            return true;
        }
    }

    public void updateImdiFileNodeIds() {
        if (getParentDomNode() != this) {
            getParentDomNode().updateImdiFileNodeIds();
        } else {
            //load then save the dom via the api to make sure there are id fields to each node then reload this imdi object
            System.out.println("updateImdiFileNodeIds");
            try {
                synchronized (domLockObject) {
//            System.out.println("removing NodeIds");
                    OurURL inUrlLocal = new OurURL(nodeUri.toURL());
                    Document nodDom = api.loadIMDIDocument(inUrlLocal, false);
                    if (nodDom == null) {
                        GuiHelper.linorgBugCatcher.logError(new Exception(ImdiTreeObject.api.getMessage()));
                        LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Error reading via the IMDI API", "Update IMDI");
                        return;
                    }
                    api.writeDOM(nodDom, this.getFile(), true);
//            System.out.println("adding NodeIds");
                    Document nodDomSecondLoad = api.loadIMDIDocument(inUrlLocal, false, null);
                    if (nodDom == null) {
                        GuiHelper.linorgBugCatcher.logError(new Exception(ImdiTreeObject.api.getMessage()));
                        LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Error reading via the IMDI API", "Update IMDI");
                        return;
                    }
                    api.writeDOM(nodDomSecondLoad, this.getFile(), false);
                }
//            System.out.println("reloading updateNodeIds");
//            reloadImdiNode(false);
                loadImdiDom();
            } catch (Exception mue) {
                GuiHelper.linorgBugCatcher.logError(mue);
                System.out.println("Invalid input URL: " + mue);
            }
        }
    }

    /**
     * Exports the imdi file for use in other applications.
     * The exported file has the id attributes removed via the api.
     * @param targetFile
     */
    public void exportImdiFile(File exportFile) {
        try {
            synchronized (domLockObject) {
                Document nodDom;
                nodDom = api.loadIMDIDocument(new OurURL(nodeUri.toURL()), false);
                if (nodDom == null) {
                    GuiHelper.linorgBugCatcher.logError(new Exception(ImdiTreeObject.api.getMessage()));
                    LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Error reading via the IMDI API", "Export IMDI");
                } else {
                    api.writeDOM(nodDom, exportFile, true);
                }
            }
        } catch (Exception ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
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
                    if (ImdiTreeObject.isStringImdi(clipBoardString) || ImdiTreeObject.isStringImdiChild(clipBoardString)) {
                        ImdiTreeObject clipboardNode = ImdiLoader.getSingleInstance().getImdiObject(null, conformStringToUrl(clipBoardString));
                        if (LinorgSessionStorage.getSingleInstance().pathIsInsideCache(clipboardNode.getFile())) {
                            if (!(ImdiTreeObject.isStringImdiChild(clipBoardString) && (!this.isSession() && !this.isImdiChild()))) {
                                if (this.getFile().exists()) {
                                    // this must use merge like favoirite to prevent instances end endless loops in corpus branches
                                    this.requestAddNode("copy of " + clipboardNode, clipboardNode);
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

    public boolean requestAddNode(String nodeTypeDisplayName, ImdiTreeObject addableImdiNode) {
//        TODO: move the arbil link on the tools page as Peter requested
        boolean returnValue = true;
        ImdiTreeObject[] sourceImdiNodeArray;
        if (addableImdiNode.isMetaNode()) {
            sourceImdiNodeArray = addableImdiNode.getChildArray();
        } else {
            sourceImdiNodeArray = new ImdiTreeObject[]{addableImdiNode};
        }

        for (ImdiTreeObject currentImdiNode : sourceImdiNodeArray) {
            String nodeType;
            String favouriteUrlString = null;
            String resourceUrl = null;
            String mimeType = null;
            if (currentImdiNode.isArchivableFile() && !currentImdiNode.isImdi()) {
                nodeType = ImdiSchema.getSingleInstance().getNodeTypeFromMimeType(currentImdiNode.mpiMimeType);
                resourceUrl = currentImdiNode.getUrlString();
                mimeType = currentImdiNode.mpiMimeType;
                nodeTypeDisplayName = "Resource";
            } else {
                nodeType = LinorgFavourites.getSingleInstance().getNodeType(currentImdiNode, this);
                favouriteUrlString = currentImdiNode.getUrlString();
            }
            if (nodeType == null) {
                returnValue = false;
            }
            String targetXmlPath = nodeUri.getFragment();
            if (nodeType == null) { // targetXmlPath hass been  added at this point to preserve the sub node (N) which otherwise had been lost for the (x) and this is required to add to a sub node correctly
                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Cannot add this type of node", null);
            } else {
//            if (this.isImdiChild()) {
//                System.out.println("requestAddNodeChild: " + this.getUrlString());
//                this.domParentImdi.requestAddNode(nodeType, this.nodeUrl.getRef(), nodeTypeDisplayName, favouriteUrlString, resourceUrl, mimeType);
//            } else {
                System.out.println("requestAddNode: " + nodeType + " : " + nodeTypeDisplayName + " : " + favouriteUrlString + " : " + resourceUrl);
                this.getParentDomNode().addQueue.add(new String[]{nodeType, targetXmlPath, nodeTypeDisplayName, favouriteUrlString, resourceUrl, mimeType});
                ImdiLoader.getSingleInstance().requestReload(this);
            }
        }
//            }
        return returnValue;
    }

    public void requestAddNode(String nodeType, String nodeTypeDisplayName) {
        System.out.println("requestAddNode: " + nodeType + " : " + nodeTypeDisplayName);
        this.getParentDomNode().addQueue.add(new String[]{nodeType, null, nodeTypeDisplayName, null, null, null});
        ImdiLoader.getSingleInstance().requestReload(this);
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
        Document nodDom;
        OurURL inUrlLocal = null;
        if (!this.isLocal() /*nodeUri.getScheme().toLowerCase().startsWith("http") */ ) {
            System.out.println("should not try to save remote files");
            setImdiNeedsSaveToDisk(false, updateUI);
            return;
        }
        System.out.println("tempUrlString: " + this.getFile());
        try {
//            if (!this.getFile().exists()) {
//                createFileInCache();
//            }
            synchronized (domLockObject) {
                inUrlLocal = new OurURL(nodeUri.toURL());
                nodDom = api.loadIMDIDocument(inUrlLocal, false);

                if (nodDom == null) {
                    GuiHelper.linorgBugCatcher.logError(new Exception(ImdiTreeObject.api.getMessage()));
                    LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Error reading via the IMDI API", "Save IMDI");
                } else {
                    System.out.println("writeDOM");
                    // make the required changes to the dom
                    // TODO: make the changes to the dom before saving
                    // refer to: /data1/repos/trunk/src/java/mpi/imdi/api/TestDom.java

                    Vector<ImdiField[]> allFields = new Vector<ImdiField[]>();
                    getAllFields(allFields);

                    Vector<ImdiField> addedFields = new Vector<ImdiField>();
                    for (Enumeration<ImdiField[]> fieldsEnum = allFields.elements(); fieldsEnum.hasMoreElements();) {
                        {
                            ImdiField[] currentFieldArray = fieldsEnum.nextElement();
                            for (int fieldCounter = 0; fieldCounter < currentFieldArray.length; fieldCounter++) {
                                ImdiField currentField = currentFieldArray[fieldCounter];
                                if (currentField.fieldNeedsSaveToDisk) {
                                    if (currentField.fieldID == null) {
                                        addedFields.add(currentField);
                                    } else {
                                        //////////////////////////////////////////////////

                                        System.out.println("trying to save: " + currentField.fieldID + " : " + currentField.getFieldValue());
                                        String keyName = currentField.getKeyName();
                                        if (keyName != null) {
                                            api.setKeyValuePair(nodDom, currentField.fieldID, keyName, currentField.getFieldValue());
//                                        changedElement = new IMDIElement(null, currentField.fieldID);
//                                        changedElement.setSpec("x:Name");
//                                        changedElement.setValue(currentField.getFieldValue());
//                                        IMDIElement ies = api.setIMDIElement(nodDom, changedElement);
                                        } else {
                                            IMDIElement changedElement;
                                            changedElement = new IMDIElement(null, currentField.fieldID);
                                            changedElement.setValue(currentField.getFieldValue());
                                            IMDIElement ie = api.setIMDIElement(nodDom, changedElement);
                                            System.out.println("ie spec: " + ie.getSpec());
                                            System.out.println("ie.id: " + ie.getDomId());
                                            System.out.println("ie.spec: " + ie.getSpec());
                                        }
                                        currentField.fieldNeedsSaveToDisk = false;
//                                GuiHelper.linorgJournal.saveJournalEntry(currentField.parentImdi.getUrlString(), currentField.xmlPath, currentField.getFieldValue(), "", "save");
                                        String fieldLanguageId = currentField.getLanguageId();
                                        if (fieldLanguageId != null) {
                                            IMDILink changedLink;
                                            changedLink = api.getIMDILink(nodDom, null, currentField.fieldID);
                                            System.out.println("trying to save language id: " + fieldLanguageId);
                                            changedLink.setLanguageId(fieldLanguageId);
                                            api.changeIMDILink(nodDom, null, changedLink);
                                            LinorgJournal.getSingleInstance().saveJournalEntry(currentField.parentImdi.getUrlString(), currentField.xmlPath + ":LanguageId", fieldLanguageId, "", "save");
                                        }
//////////////////////////////////////////////////
//                                String elementNameForApi = currentField.fieldID;
//                                if (currentField.fieldID == null) {
//                                    elementNameForApi = apiPath;
//                                    GuiHelper.linorgBugCatcher.logError(currentField.getFullXmlPath(), new Exception("No domid for field when trying to save"));
//                                }
//                                api.setKeyValuePair(nodDom, apiPath, currentField.getKeyName(), currentField.getFieldValue());



////                                 TODO: keys must be added here and in the favourites copy
//                                if (currentField.fieldID == null) {
//                                    GuiHelper.linorgBugCatcher.logError(currentField.getFullXmlPath(), new Exception("No domid for field when trying to save"));
//                                    // if the field does not have an id attribite then it must now be created in the imdi file via the imdi api
//                                    // Mangle the path to suit the imdi api
//
//                                    System.out.println("trying to add: " + apiPath + " : " + currentField.getFieldValue());
//                                    changedElement = api.addIMDIElement(nodDom, apiPath);
//                                } else {
//                                    // set value

//                                    System.out.println("changedElement: " + changedElement.getSpec());
//                                    api.setKeyValuePair(nodDom, currentField.fieldID, keyName, currentField.getFieldValue());
//
////                                    changedElement.
//
////                                    mpi.imdi.api.IMDIXMLFormat imdiXmlFormat = new mpi.imdi.api.IMDIXMLFormat();
//////                                            generateSpecFromNode(Node node)
////                                    mpi.imdi.api.IMDIXMLForma
//
//                                    System.out.println("Warning: cannot save key name values");
//                                // there appears to be no other way to do this via the api
//                                //changedElement.
////                                    String elementSpec = changedElement.getSpec();
////                                    System.out.println("elementSpec: " + elementSpec);
////                                    elementSpec = elementSpec + ".Name";
////                                    System.out.println("elementSpec: " + elementSpec);
////                                    IMDIElement keyNameElement = api.getIMDIElement(nodDom, elementSpec);
////                                    System.out.println("keyNameElement: " + keyNameElement);
////                                    keyNameElement.setValue(keyName);
//                                }
                                    }
                                }
                            }
                        }
                    }
                    for (Enumeration<ImdiField> fieldsEnum = addedFields.elements(); fieldsEnum.hasMoreElements();) {
                        ImdiField currentField = fieldsEnum.nextElement();
                        //////
                    }
                    bumpHistory();

                    api.writeDOM(nodDom, this.getFile(), true); // remove the id attributes
                    nodDom = api.loadIMDIDocument(inUrlLocal, false);
                    if (nodDom == null) {
                        GuiHelper.linorgBugCatcher.logError(new Exception(ImdiTreeObject.api.getMessage()));
                        LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Error reading via the IMDI API after save", "Update IMDI");
                    } else {
                        api.writeDOM(nodDom, this.getFile(), false); // add the id attributes in the correct order
                    }
                    // update the icon to indicate the change
                    setImdiNeedsSaveToDisk(false, updateUI);
                }
            }
        } catch (Exception mue) {
            GuiHelper.linorgBugCatcher.logError(mue);
            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Error saving via the IMDI API", "Save Error");
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
//            if (childsLabel.equals(ImdiSchema.imdiPathSeparator + "ResourceLink")) {
////                        // resolve the relative location of the file
////                        File resourceFile = new File(this.getFile().getParent(), fieldToAdd.fieldValue);
////                        resourceUrlString = resourceFile.getCanonicalPath();
//                resourceUrlString = fieldToAdd.fieldValue;
////                if (useCache) {
////                    GuiHelper.linorgSessionStorage.updateCache(getFullResourcePath());
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

    /**
     * Compares this node to another based on its type and string value.
     * @return The string comparison result.
     */
    public int compareTo(Object o) throws ClassCastException {
        ImdiTreeNodeSorter imdiTreeNodeSorter = new ImdiTreeNodeSorter();
        return imdiTreeNodeSorter.compare(this, o);
    }

    public synchronized boolean waitTillLoaded() {
        if (isLoading()) {
            try {
                getParentDomNode().wait(1000);
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
//                this.notifyAll();
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
        if (isLoading()) {
//            if (lastNodeText.length() > 0) {
//                return lastNodeText;
//            } else {asdasdasd
////                if (nodeText != null && nodeText.length() > 0) {
            return lastNodeText;
//            }
        }
//        String nameText = "";
//        TODO: move this to a list loaded from the templates or similar
        String[] preferredNameFields = {"Name", "Id"};
        for (String currentPreferredName : preferredNameFields) {
            ImdiField[] currentFieldArray = fieldHashtable.get(currentPreferredName);
            if (currentFieldArray != null) {
                for (ImdiField currentField : currentFieldArray) {
                    if (currentField != null) {
                        nodeText = currentField.toString();
                        break;
                    }
                }
            }
        }
        if (hasResource()) {
            String resourcePathString = getFullResourceURI().toString();
            int lastIndex = resourcePathString.lastIndexOf("/");
//                if (lastIndex)
            nodeText = resourcePathString.substring(lastIndex + 1);
        }
//        nodeTextChanged = lastNodeText.equals(nodeText + nameText);
        if (nodeText != null) {
            lastNodeText = nodeText;
        }
        if (lastNodeText.length() == 0) {
            lastNodeText = "      ";
        }
        return lastNodeText;
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
        if (resourceUrlField.fieldValue.toLowerCase().startsWith("http")) {
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
            if (resourceUrlField.fieldValue.length() == 0) {
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
        return resourceUrlField.fieldValue;
    }

    public boolean hasHistory() {
        if (!this.isLocal()) {
            // only local files can have a history
            return false;
        }
        return !this.isImdiChild() && new File(this.getFile().getAbsolutePath() + ".0").exists();
    }

    public String[][] getHistoryList() {
        Vector<String[]> historyVector = new Vector<String[]>();
        int versionCounter = 0;
        File currentHistoryFile;
//        historyVector.add(new String[]{"Current", ""});
        if (new File(this.getFile().getAbsolutePath() + ".x").exists()) {
            historyVector.add(new String[]{"Last Save", ".x"});
        }
        do {
            currentHistoryFile = new File(this.getFile().getAbsolutePath() + "." + versionCounter);
            if (currentHistoryFile.exists()) {
                Date mtime = new Date(currentHistoryFile.lastModified());
                String mTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(mtime);
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

    /**
     * Resolves the full path to a resource file if it exists.
     * @return The path to remote resource if it exists.
     */
    public URI getFullResourceURI() {
        try {
            String targetUrlString = resourceUrlField.fieldValue;
//            boolean urlIsComplete = (targetUrlString.startsWith("file:") || targetUrlString.startsWith("http:") || targetUrlString.startsWith("https:"));
//            if (!urlIsComplete || targetUrlString.startsWith(".")) {
//                targetUrlString = this.getParentDirectory() + targetUrlString;
//                //targetUrlString = targetUrlString.replace("/./", "/");
//            }
//            ;
//            System.out.println("URIUtil: " + URIUtil.newURI(targetUrlString));
//            System.out.println("resourceUri: " + targetUrlString);
//            System.out.println("nodeUri: " + nodeUri);
//            URI resourceUri = new URI(targetUrlString);
//            System.out.println("resourceUri: " + resourceUri);
            return nodeUri.resolve(URIUtil.newURI(targetUrlString)).normalize();
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
        return nodeUri.toString();
    }

    /**
     * Gets the ImdiTreeObject parent of an imdi child node.
     * The returned node will be able to reload/save the dom for this node.
     * Only relevant for imdi child nodes.
     * @return ImdiTreeObject
     */
    public ImdiTreeObject getParentDomNode() {
        if (domParentImdi == null) {
            if (isImdiChild()) {
                try {
                    domParentImdi = ImdiLoader.getSingleInstance().getImdiObject(null, new URI(nodeUri.getScheme(), nodeUri.getUserInfo(), nodeUri.getHost(), nodeUri.getPort(), nodeUri.getPath(), nodeUri.getQuery(), null /* fragment removed */));
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

    public boolean isImdi() {
        if (nodeUri != null /* && nodDom != null*/) {
            if (isImdiChild()) {
                return true;
            } else {
                return ImdiTreeObject.isStringImdi(nodeUri.getPath());
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
            return nameFields[0].xmlPath.equals(ImdiSchema.imdiPathSeparator + "METATRANSCRIPT" + ImdiSchema.imdiPathSeparator + "Session" + ImdiSchema.imdiPathSeparator + "Name");
        }
        return false;
    }

    /**
     * Tests if this node is a meta node that contains no fields and only child nodes, such as the Languages, Actors, MediaFiles nodes etc..
     * @return boolean
     */
    public boolean isMetaNode() {
        return this.getFields().size() == 0;
    }

    public boolean isCatalogue() {
        // test if this node is a catalogue
        ImdiField[] nameFields = fieldHashtable.get("Name");
        if (nameFields != null) {
            return nameFields[0].xmlPath.equals(ImdiSchema.imdiPathSeparator + "METATRANSCRIPT" + ImdiSchema.imdiPathSeparator + "Catalogue" + ImdiSchema.imdiPathSeparator + "Name");
        }
        return false;
    }

    public boolean isCorpus() {
        // test if this node is a session
        ImdiField[] nameFields = fieldHashtable.get("Name");
        if (nameFields != null) {
            return nameFields[0].xmlPath.equals(ImdiSchema.imdiPathSeparator + "METATRANSCRIPT" + ImdiSchema.imdiPathSeparator + "Corpus" + ImdiSchema.imdiPathSeparator + "Name");
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
        if (this.isLocal()) {
            if (isImdiChild()) {
                return this.getParentDomNode().getFile();
            }
            return new File(nodeUri);
        } else {
            return null;
        }
        // TODO: (this comment might be out of date) reconsider this code. file.toUrl does not url encode and should not be used. file.toURI.toUrl does but this breaks many file actions
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
            containersOfThisNode.add(containerToAdd);
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
        for (ImdiTreeObject currentChild : childArray) {
            currentChild.clearChildIcons();
            currentChild.clearIcon();
        }
    }
//    public void addJumpToInTreeRequest() {
//        jumpToRequested = true;
//    }
    /**
     * Clears the icon calculated in "getIcon()" and notifies any UI containers of this node.
     */
    public void clearIcon() {
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
                if (currentContainer instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode currentTreeNode = (DefaultMutableTreeNode) currentContainer;
                    DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) ((DefaultMutableTreeNode) currentContainer).getParent();
                    if (parentNode != null) {
                        TreeHelper.getSingleInstance().addToSortQueue(parentNode);
                    } else {
                        TreeHelper.getSingleInstance().addToSortQueue(currentTreeNode);
                    }
                }
            } catch (java.util.NoSuchElementException ex) {
                //GuiHelper.linorgBugCatcher.logError(ex);
            }
        }
//            }
//        });
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
