package nl.mpi.arbil;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import mpi.imdi.api.*;
import mpi.util.OurURL;
import org.w3c.dom.Document;
import java.net.MalformedURLException;
import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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
    private Hashtable<String, ImdiTreeObject> childrenHashtable;
    private boolean imdiDataLoaded;
    public String hashString;
    public String mpiMimeType;
    public String typeCheckerMessage;
    public int matchesInCache;
    public int matchesRemote;
    public int matchesLocalFileSystem;
    public boolean fileNotFound;
    public boolean needsSaveToDisk;
    private String nodeText, lastNodeText = "";
    private boolean nodeTextChanged = false;
    private URL nodeUrl;
    private String resourceUrlString;
    public boolean isDirectory;
    private ImageIcon icon;
    private boolean nodeEnabled;
    private Vector<String[]> childLinks; // each element in this vector is an array [linkPath, linkId]. When the link is from an imdi the id will be the node id, when from get links or list direcotry id will be null
    private HashSet containersOfThisNode;
    private int isLoadingCount = 0;
    public boolean lockedByLoadingThread = false;
    private boolean isFavourite;
    public boolean autoLoadChildNodes = false;
    public Vector<String[]> addQueue;
    public boolean scrollToRequested = false;
//    public Vector<ImdiTreeObject> mergeQueue;
//    public boolean jumpToRequested = false; // dubious about this being here but it seems to fit here best
    private ImdiTreeObject domParentImdi = null; // the parent imdi containing the dom, only set for imdi child nodes
    public String xmlNodeId = null; // only set for imdi child nodes and is the xml node id relating to this imdi tree object

    protected ImdiTreeObject(String localNodeText, String localUrlString) {
//        debugOut("ImdiTreeObject: " + localNodeText + " : " + localUrlString);
        containersOfThisNode = new HashSet();
        addQueue = new Vector<String[]>();
        nodeText = localNodeText;
        nodeUrl = conformStringToUrl(localUrlString);
        initNodeVariables();
    }

    // set the node text only if it is null
    public void setNodeText(String localNodeText) {
        if (nodeText == null) {
            nodeText = localNodeText;
        }
    }

    static public URL conformStringToUrl(String inputUrlString) {
        URL returnUrl = null;
        try {
//            localUrlString = localUrlString.replace("\\", "/");
            if (!inputUrlString.toLowerCase().startsWith("http:") && !inputUrlString.toLowerCase().startsWith("file:")) {
                returnUrl = new File(inputUrlString).toURL();
            } else {
                returnUrl = new URL(inputUrlString);
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
        return (!urlString.startsWith("http:"));
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
    // end static methods for testing imdi file and object types

    private void debugOut(String messageString) {
        if (debugOn) {
            System.out.println(messageString);
        }
    }

    public void setImdiNeedsSaveToDisk(boolean imdiNeedsSaveToDisk, boolean updateUI) {
        if (isImdiChild()) {
            this.getParentDomNode().setImdiNeedsSaveToDisk(imdiNeedsSaveToDisk, updateUI);
        } else {
            if (this.needsSaveToDisk != imdiNeedsSaveToDisk) {
                if (imdiNeedsSaveToDisk) {
                    GuiHelper.imdiLoader.addNodeNeedingSave(this);
                } else {
                    GuiHelper.imdiLoader.removeNodesNeedingSave(this);
                }
            }
        }
        this.needsSaveToDisk = imdiNeedsSaveToDisk;
        if (updateUI) {
            this.clearIcon();
        }
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
        if (childrenHashtable != null) {
            for (Enumeration childEnum = childrenHashtable.elements(); childEnum.hasMoreElements();) {
                ImdiTreeObject currentNode = (ImdiTreeObject) childEnum.nextElement();
                if (currentNode.isImdiChild()) {
                    currentNode.initNodeVariables();
                }
            }
        }
        if (currentTemplate == null) {
            // this will be overwritten when the imdi file is read, provided that a template is specified in the imdi file
            currentTemplate = ArbilTemplateManager.getSingleInstance().getDefaultTemplate();
        }
        fieldHashtable = new Hashtable<String, ImdiField[]>();
        childrenHashtable = new Hashtable<String, ImdiTreeObject>();
        imdiDataLoaded = false;
        hashString = null;
        mpiMimeType = null;
        matchesInCache = 0;
        matchesRemote = 0;
        matchesLocalFileSystem = 0;
        fileNotFound = false;
        setImdiNeedsSaveToDisk(false, false);
//    nodeText = null;
//    urlString = null;
        resourceUrlString = null;
        isDirectory = false;
        icon = null;
        nodeEnabled = true;
        childLinks = new Vector<String[]>();
//        isLoadingCount = true;
        if (nodeUrl != null) {
            if (!isImdi() && isLocal()) {
                File fileObject = getFile();
                if (fileObject != null) {
                    this.isDirectory = fileObject.isDirectory();
                }
                int currentFieldId = 1;
                nodeText = fileObject.getName();
                ImdiField sizeField = new ImdiField(this, "Size", (fileObject.length() / 1024) + "KB");
                sizeField.fieldID = "x" + currentFieldId++;
                addField(sizeField);
                // add the modified date
                Date mtime = new Date(fileObject.lastModified());
                String mTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(mtime);
                ImdiField dateField = new ImdiField(this, "last modified", mTimeString);
                dateField.fieldID = "x" + currentFieldId++;
                addField(dateField);
                // get exif tags
                ImdiField[] exifFields = GuiHelper.imdiSchema.getExifMetadata(this);
                for (ImdiField currentField : exifFields) {
                    currentField.fieldID = "x" + currentFieldId++;
                    addField(currentField);
                }
            }
            if (!isImdi() && nodeText == null) {
                nodeText = this.getUrlString();
            }
        }
        if (!isImdi() && !isDirectory()) {
            // if it is an not imdi or a loose file but not a direcotry then get the md5sum
            MimeHashQueue.getSingleInstance().addToQueue(this);
        }
        if (this.isDirectory()) {
            getDirectoryLinks();
            clearIcon();
        }
    }

    public void reloadNode() {
        System.out.println("reloadNode: " + isLoading());
        getParentDomNode().needsSaveToDisk = false; // clear any changes
        if (!this.isImdi()) {
            initNodeVariables();
            loadChildNodes();
            clearIcon();
            TreeHelper.getSingleInstance().updateTreeNodeChildren(this);
        } else {
            if (getParentDomNode().isCorpus()) {
                getParentDomNode().autoLoadChildNodes = true;
            }
            GuiHelper.imdiLoader.requestReload(getParentDomNode());
        }
    }

//    private void reloadImdiNode() {
//        System.out.println("reloadImdiNode: " + this + childrenHashtable.size());
//
//    }
    synchronized public void loadImdiDom() {
        System.out.println("loadImdiDom: " + this.getFile().getName());
        initNodeVariables(); // this might be run too often here but it must be done in the loading thread and it also must be done when the object is created
        Document nodDom = null;
        // cacheLocation will be null if useCache = false hence no file has been saved
//        String cacheLocation = null;
        try {
            //System.out.println("tempUrlString: " + tempUrlString);
            if (false) {
                // TODO: resolve why this is not functioning, till then the subsequent stanza is used
                try {
                    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                    nodDom = builder.parse(nodeUrl.openStream());
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
//                    System.out.println("Could not parse dom: " + this.getUrlString());
                }
            } else {
                OurURL inUrlLocal = null;
                inUrlLocal = new OurURL(this.getUrlString());
                nodDom = api.loadIMDIDocument(inUrlLocal, false);
            }

            // only read the fields into imdi tree objects if it is not going to be saved to the cache
//            if (!useCache) {
            if (nodDom == null) {
                nodeText = "Could not load IMDI";
                fileNotFound = true;
            } else {
                //set the string name to unknown, it will be updated in the tostring function
                nodeText = "unknown";
                // load the fields from the imdi file
                GuiHelper.imdiSchema.iterateChildNodes(this, childLinks, nodDom.getFirstChild(), "");
            }
//            }
        // save this to the cache before deleting the dom
//            if (useCache) {
//                // get the links from the imdi before we dispose of the dom
//                getImdiLinks(nodDom);
////                cacheLocation = saveNodeToCache(nodDom);
//            }
        } catch (MalformedURLException mue) {
            GuiHelper.linorgBugCatcher.logError(mue);
//            System.out.println("Invalid input URL: " + mue);
            nodeText = "Invalid input URL";
        }
        //we are now done with the dom so free the memory
        nodDom = null;
//        return cacheLocation;
        clearChildIcons();
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
        String[] dirLinkArray = null;
        File nodeFile = this.getFile();
        dirLinkArray = nodeFile.list();
        for (int linkCount = 0; linkCount < dirLinkArray.length; linkCount++) {
            String currentLink = this.getUrlString() + File.separatorChar + dirLinkArray[linkCount];
//            System.out.println("currentLink: " + currentLink);
            childLinks.add(new String[]{currentLink, null});
        }
    }

//    private void getImdiLinks(Document nodDom) {
//        try {
//            if (nodDom != null) {
//                OurURL baseURL = new OurURL(this.getUrlString());
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
        return childLinks.size() + childrenHashtable.size();
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
            for (Enumeration childEnum = childrenHashtable.elements(); childEnum.hasMoreElements();) {
                ((ImdiTreeObject) childEnum.nextElement()).getAllChildren(allChildren);
            }
        }
        allChildren.addAll(childrenHashtable.values());
    }

    /**
     * Used to populate the child list in the show child popup in the imditable.
     * @return An enumeration of the next level child nodes.
     */
    public Enumeration<ImdiTreeObject> getChildEnum() {
        return childrenHashtable.elements();
    }

    /**
     * Used to populate the child nodes in the table cell.
     * @return A collection of the next level child nodes.
     */
    public Collection<ImdiTreeObject> getChildCollection() {
        return childrenHashtable.values();
    }

    /**
     * Gets the second level child nodes from the fist level child node matching the child type string.
     * Used to populate the child nodes in the table cell.
     * @param childType The name of the first level child to query.
     * @return An object array of all second level child nodes in the first level node.
     */
    public ImdiTreeObject[] getChildNodesArray(String childType) {
        for (Enumeration childEnum = childrenHashtable.elements(); childEnum.hasMoreElements();) {
            ImdiTreeObject currentNode = (ImdiTreeObject) childEnum.nextElement();
            if (currentNode.toString().equals(childType)) {
                return currentNode.getChildCollection().toArray(new ImdiTreeObject[0]);
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
    public void attachChildNode(ImdiTreeObject destinationNode) {
//        System.out.println("attachChildNodeTo: " + this.getUrlString());
//        System.out.println("attachChildNode: " + destinationNode.getUrlString());
        if (destinationNode != this) {
            childrenHashtable.put(destinationNode.getUrlString(), destinationNode);
        }
    }

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
    public String addChildNode(String nodeType, String targetXmlPath, String resourcePath, String mimeType) {
        System.out.println("addChildNode:: " + nodeType + " : " + resourcePath);
        System.out.println("addChildNode:: " + nodeType + " : " + resourcePath);
        if (needsSaveToDisk) {
            saveChangesToCache(true);
        }
        String addedNodePath = null;
        ImdiTreeObject destinationNode;
        if (GuiHelper.imdiSchema.isImdiChildType(nodeType) || (resourcePath != null && this.isSession())) {
            System.out.println("adding to current node");
            destinationNode = this;
            try {
                OurURL inUrlLocal = new OurURL(this.getFile().toURL());
                Document nodDom = api.loadIMDIDocument(inUrlLocal, false);
//                api.writeDOM(nodDom, this.getFile(), true); // remove the id attributes
//                System.out.println("addChildNode: insertFromTemplate");
                addedNodePath = GuiHelper.imdiSchema.insertFromTemplate(this.currentTemplate, this.getFile(), getSubDirectory(), nodeType, targetXmlPath, nodDom, resourcePath, mimeType);
//                System.out.println("addChildNode: save");
//                nodDom = api.loadIMDIDocument(inUrlLocal, false);
                api.writeDOM(nodDom, this.getFile(), false); // add the id attributes
            } catch (Exception ex) {
//                System.out.println("addChildNode: " + ex.getMessage());
                GuiHelper.linorgBugCatcher.logError(ex);
            }
//            imdiNeedsSaveToDisk = true;
        } else {
            System.out.println("adding new node");
            String targetFileName = getSubDirectory().getAbsolutePath() + File.separatorChar + LinorgSessionStorage.getSingleInstance().getNewImdiFileName();
            addedNodePath = GuiHelper.imdiSchema.addFromTemplate(new File(targetFileName), nodeType);
            destinationNode = GuiHelper.imdiLoader.getImdiObject(null, targetFileName);
            if (this.getFile().exists()) {
                this.addCorpusLink(destinationNode);
            } else {
                // TODO: this should not really be here
                TreeHelper.getSingleInstance().addLocation(destinationNode.getUrlString());
                TreeHelper.getSingleInstance().applyRootLocations();
            }
//            destinationNode.saveChangesToCache();
//            destinationNode.imdiNeedsSaveToDisk = true;
        }
        //load then save the dom via the api to make sure there are id fields to each node then reload this imdi object
        destinationNode.updateImdiFileNodeIds();

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
    public ImdiTreeObject[] loadChildNodes() {
        System.out.println("loadChildNodes: " + this);
        if (!imdiDataLoaded) {
            // if this node has been loaded then do not load again
            // to refresh the node and its children the node should be nulled and recreated
            imdiDataLoaded = true;
            autoLoadChildNodes = false;
            if (!this.isSession()) {
                //getImdiFieldLinks();
                for (Enumeration<String[]> childLinksEnum = childLinks.elements(); childLinksEnum.hasMoreElements();) {
                    String currentChildPath = childLinksEnum.nextElement()[0];
                    ImdiTreeObject currentImdi = GuiHelper.imdiLoader.getImdiObject(null, currentChildPath);
//                    System.out.println("adding to list of child nodes 2: " + currentImdi);
                    childrenHashtable.put(currentImdi.getUrlString(), currentImdi);
//                    if (ImdiTreeObject.isStringImdi(currentChildPath)) {
//                        currentImdi.loadImdiDom();
//                    }
                }
            }
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

        }
        Vector<ImdiTreeObject> tempImdiVector = new Vector<ImdiTreeObject>();
        Enumeration nodesToAddEnumeration = childrenHashtable.elements();
        while (nodesToAddEnumeration.hasMoreElements()) {
            tempImdiVector.add((ImdiTreeObject) nodesToAddEnumeration.nextElement());
        }
        ImdiTreeObject[] returnImdiArray = new ImdiTreeObject[tempImdiVector.size()];
        tempImdiVector.toArray(returnImdiArray);
        return returnImdiArray;
    }

    public boolean containsFieldValue(String searchValue) {
        boolean findResult = false;
        for (ImdiField[] currentFieldArray : (Collection<ImdiField[]>) this.fieldHashtable.values()) {
            for (ImdiField currentField : currentFieldArray) {
                System.out.println("containsFieldValue: " + currentField.fieldValue + ":" + searchValue);
                if (currentField.fieldValue.toLowerCase().contains(searchValue.toLowerCase())) {
                    findResult = true;
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
        return childLinks.size() > 0 || childrenHashtable.size() > 0;
    }

    public int[] getRecursiveChildCount() {
//        debugOut("getChildCount: " + this.toString());
        int[] returnArray = new int[2];
        returnArray[0] = 0;
        returnArray[1] = 0;
        if (imdiDataLoaded) {
            returnArray[1] += 1; // count this node
            Enumeration nodesToAddEnumeration = childrenHashtable.elements();
            while (nodesToAddEnumeration.hasMoreElements()) {
                // count the child nodes
                int[] childCount = ((ImdiTreeObject) nodesToAddEnumeration.nextElement()).getRecursiveChildCount();
                returnArray[0] += childCount[0];
                returnArray[1] += childCount[1];
            }
        } else {
            if (this.isImdi()) {
                returnArray[0] = 1;
            }
        }
        return returnArray;
    }

    public void loadNextLevelOfChildren(long stopTime) {
//        debugOut("loadNextLevelOfChildren: " + this.toString() + ":" + (System.currentTimeMillis() - stopTime));
        if (System.currentTimeMillis() > stopTime) {
            return;
        }
        if (this.isImdi()) {
            if (imdiDataLoaded) {
                Enumeration nodesToAddEnumeration = childrenHashtable.elements();
                while (nodesToAddEnumeration.hasMoreElements()) {
                    // load one level of child nodes
                    ((ImdiTreeObject) nodesToAddEnumeration.nextElement()).loadNextLevelOfChildren(stopTime);
                //((ImdiTreeObject) nodesToAddEnumeration.nextElement()).();
                }
            } else {
                loadChildNodes();
            }
        }
//        debugOut("listDiscardedOfAttributes: " + listDiscardedOfAttributes);
    }

    /**
     * Checks if there are changes saved on disk that have not been sent to the server.
     * @return boolean
     */
    public boolean needsChangesSentToServer() {
        return new File(this.getFile().getPath() + ".0").exists();
    }

    private void getAllFields(Vector<ImdiField[]> allFields) {
        // returns all fields relevant to the parent node
        // that includes all indinodechild fields but not from any other imdi file
        System.out.println("getAllFields: " + this.toString());
        allFields.addAll(fieldHashtable.values());
        for (Enumeration childEnum = childrenHashtable.elements(); childEnum.hasMoreElements();) {
            ImdiTreeObject currentChild = ((ImdiTreeObject) childEnum.nextElement());
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
//            if (imdiNeedsSaveToDisk) {
//                saveChangesToCache(false);
//            }
////            System.out.println("attempting to remove nodes");
//            try {
//                OurURL inUrlLocal = new OurURL(this.getFile().toURL());
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
        for (Enumeration<String[]> childLinksEnum = childLinks.elements(); childLinksEnum.hasMoreElements();) {
            String[] currentLinkPair = childLinksEnum.nextElement();
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
    }

    public void deleteCatalogueLink() {
        // the catalogue implemention in the imdi api requires special treatment and so must be done in this way not via deleteFromDomViaId 
        Document nodDom;
        try {
            if (needsSaveToDisk) {
                saveChangesToCache(false);
            }
            OurURL inUrlLocal = new OurURL(this.getFile().toURL());
            nodDom = api.loadIMDIDocument(inUrlLocal, false);
            api.createIMDILink(nodDom, null, "", "", WSNodeType.CATALOGUE, "");
            api.writeDOM(nodDom, this.getFile(), false);
            reloadNode();
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
            OurURL inUrlLocal = new OurURL(this.getFile().toURL());
            nodDom = api.loadIMDIDocument(inUrlLocal, false);
            for (String currentImdiDomId : domIdArray) {
                IMDIElement target = new IMDIElement(null, currentImdiDomId);
                api.removeIMDIElement(nodDom, target);
            }
            api.writeDOM(nodDom, this.getFile(), false);
            reloadNode();
        } catch (Exception ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
        }
    }

    public boolean hasCatalogue() {
        for (Enumeration<String[]> childLinksEnum = childLinks.elements(); childLinksEnum.hasMoreElements();) {
            String[] currentLinkPair = childLinksEnum.nextElement();
            String currentChildPath = currentLinkPair[0];
            ImdiTreeObject childNode = GuiHelper.imdiLoader.getImdiObject(null, currentChildPath);
            childNode.waitTillLoaded(); // if the child nodes have not been loaded this will fail so we must wait here
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
        for (Enumeration<String[]> childLinksEnum = childLinks.elements(); childLinksEnum.hasMoreElements();) {
            String[] currentLinkPair = childLinksEnum.nextElement();
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
                OurURL inUrlLocal = new OurURL(this.getFile().toURL());
                nodDom = api.loadIMDIDocument(inUrlLocal, false);
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
                api.writeDOM(nodDom, this.getFile(), false);
            } catch (Exception ex) {
                GuiHelper.linorgBugCatcher.logError(ex);
//            System.out.println("Exception: " + ex.getMessage());
            }
            return true;
        }
    }

    private void updateImdiFileNodeIds() {
        System.out.println("updateImdiFileNodeIds");
        try {
//            System.out.println("removing NodeIds");
            OurURL inUrlLocal = new OurURL(this.getFile().toURL());
            Document nodDom = api.loadIMDIDocument(inUrlLocal, false);
            api.writeDOM(nodDom, this.getFile(), true);
//            System.out.println("adding NodeIds");
            Document nodDomSecondLoad = api.loadIMDIDocument(inUrlLocal, false, null);
            api.writeDOM(nodDomSecondLoad, this.getFile(), false);
//            System.out.println("reloading updateNodeIds");
//            reloadImdiNode(false);
            loadImdiDom();
        } catch (Exception mue) {
            GuiHelper.linorgBugCatcher.logError(mue);
            System.out.println("Invalid input URL: " + mue);
        }
    }

    /**
     * Exports the imdi file for use in other applications.
     * The exported file has the id attributes removed via the api.
     * @param targetFile
     */
    public void exportImdiFile(File exportFile) {
        try {
            Document nodDom;
            nodDom = api.loadIMDIDocument(new OurURL(this.getFile().toURL()), false);
            api.writeDOM(nodDom, exportFile, true);
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
                        ImdiTreeObject clipboardNode = GuiHelper.imdiLoader.getImdiObject(null, clipBoardString);
                        if (LinorgSessionStorage.getSingleInstance().pathIsInsideCache(clipboardNode.getFile())) {
                            if (!(ImdiTreeObject.isStringImdiChild(clipBoardString) && (!this.isSession() && !this.isImdiChild()))) {
                                if (this.getFile().exists()) {
                                    // this must use merge like favoirite to prevent instances end endless loops in corpus branches
                                    this.requestAddNode(LinorgFavourites.getSingleInstance().getNodeType(clipboardNode, this), "Copy of " + clipboardNode, clipboardNode.getUrlString(), null, null);
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

    public void requestAddNode(String nodeType, String nodeTypeDisplayName, String favouriteUrlString, String resourceUrl, String mimeType) {
        String targetXmlPath = this.getURL().getRef();
        if (nodeType == null) {
            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Cannot add this type of node", null);
        } else {
//            if (this.isImdiChild()) {
//                System.out.println("requestAddNodeChild: " + this.getUrlString());
//                this.domParentImdi.requestAddNode(nodeType, this.nodeUrl.getRef(), nodeTypeDisplayName, favouriteUrlString, resourceUrl, mimeType);
//            } else {
            System.out.println("requestAddNode: " + nodeType + " : " + nodeTypeDisplayName + " : " + favouriteUrlString + " : " + resourceUrl);
            this.getParentDomNode().addQueue.add(new String[]{nodeType, targetXmlPath, nodeTypeDisplayName, favouriteUrlString, resourceUrl, mimeType});
            GuiHelper.imdiLoader.requestReload(this);
//            }
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
        Document nodDom;
        OurURL inUrlLocal = null;
        if (nodeUrl.getProtocol().toLowerCase().startsWith("http")) {
            System.out.println("should not try to save remote files");
            setImdiNeedsSaveToDisk(false, updateUI);
            return;
        }
        System.out.println("tempUrlString: " + this.getFile());
        try {
//            if (!this.getFile().exists()) {
//                createFileInCache();
//            }
            inUrlLocal = new OurURL(this.getFile().toURL());
            nodDom = api.loadIMDIDocument(inUrlLocal, false);

            if (nodDom == null) {
                System.out.println("Could not load IMDI");
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
                //////
                // update the files version number
                //String destinationPath = GuiHelper.linorgSessionStorage.getSaveLocation(this.getUrl());
                //TODO: the template add does not create a new history file so move this into a common area for both
                int versionCounter = 0;
                while (new File(this.getFile() + "." + versionCounter).exists()) {
                    versionCounter++;
                }
                while (versionCounter >= 0) {
                    File lastFile = new File(this.getFile().getPath() + "." + versionCounter);
                    versionCounter--;
                    File nextFile = new File(this.getFile().getPath() + "." + versionCounter);
                    if (versionCounter >= 0) {
                        nextFile.renameTo(lastFile);
                        System.out.println("renaming: " + nextFile + " : " + lastFile);
                    } else {
                        this.getFile().renameTo(lastFile);
                        System.out.println("renaming: " + this.getFile() + " : " + lastFile);
                    }
                }
                //////
                api.writeDOM(nodDom, this.getFile(), true); // remove the id attributes
                nodDom = api.loadIMDIDocument(inUrlLocal, false);
                api.writeDOM(nodDom, this.getFile(), false); // add the id attributes in the correct order                
                // update the icon to indicate the change
                setImdiNeedsSaveToDisk(false, updateUI);
            }
        } catch (MalformedURLException mue) {
            GuiHelper.linorgBugCatcher.logError(mue);
//            System.out.println("Invalid input URL: " + mue);
            nodeText = "Invalid input URL";
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
            resourceUrlString = fieldToAdd.fieldValue;
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
     * Compares this node to another based on its toString value.
     * @return The string comparison result.
     */
    public int compareTo(Object o) throws ClassCastException {
        if (!(o instanceof ImdiTreeObject)) {
            throw new ClassCastException("ImdiTreeObject expected.");
        }
        if (o instanceof ImdiTreeObject) {
            if (((ImdiTreeObject) o).isDirectory && !this.isDirectory) {
                return +1;
            } else if (!((ImdiTreeObject) o).isDirectory && this.isDirectory) {
                return -1;
            }
        }
        return this.toString().compareToIgnoreCase(((ImdiTreeObject) o).toString());
    }

    synchronized boolean waitTillLoaded() {
        if (isLoading()) {
            try {
                this.wait();
            } catch (Exception ex) {
                return false;
            }
        }
        return true;
    }

    synchronized void updateLoadingState(int countChange) {
        isLoadingCount += countChange;
        if (!isLoading()) {
            this.notifyAll();
        }
    }

    public boolean isLoading() {
        return isLoadingCount > 0;
    }

    @Override
    public String toString() {
        if (isLoading()) {
            if (lastNodeText.length() > 0) {
                return lastNodeText;
            } else {
                lastNodeText = "loading imdi..."; // note that this is different from the text shown by treehelper "adding..."
                return lastNodeText;
            }
        }
        String nameText = "";
//        TODO: move this to a list loaded from the templates or similar
        String[] preferredNameFields = {"Name", "ResourceLink", "Id"};
        for (String currentPreferredName : preferredNameFields) {
            ImdiField[] currentFieldArray = fieldHashtable.get(currentPreferredName);
            if (currentFieldArray != null) {
                for (ImdiField currentField : currentFieldArray) {
                    if (currentField != null) {
                        nodeText = "";
                        nameText = currentField.toString();
                        break;
                    }
                }
                if (!nameText.equals("")) {
                    break;
                }
            }
        }
        nodeTextChanged = lastNodeText.equals(nodeText + nameText);
        lastNodeText = nodeText + nameText;
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
        return resourceUrlString != null;
    }

    /**
     * Tests if a local resource file is associated with this node.
     * @return boolean
     */
    public boolean hasLocalResource() {
        if (resourceUrlString == null) {
            return false;
        }
        if (resourceUrlString.toLowerCase().startsWith("http")) {
            return false;
        }
        if (resourceUrlString.toLowerCase().startsWith(".") && !this.isLocal()) {
            return false;
        }
        return true;
    }

    /**
     * Gets the ULR string of the resource file if it is available.
     * @return a URL string of the resource file
     */
    private String getResource() {
        return resourceUrlString;
    }

    /**
     * Resolves the full path to a resource file if it exists.
     * @return The path to remote resource if it exists.
     */
    public String getFullResourcePath() {
        String targetUrlString = resourceUrlString;
        if (targetUrlString.startsWith(".")) {
            targetUrlString = this.getParentDirectory() + targetUrlString;
            //targetUrlString = targetUrlString.replace("/./", "/");
        }
        return targetUrlString;
    }

    /**
     * Gets the ULR string provided when the node was created.
     * @return a URL string of the IMDI
     */
    public String getUrlString() {
        return nodeUrl.toString();
    }

    /**
     * Gets the ImdiTreeObject parent of an imdi child node.
     * The returned node will be able to reload/save the dom for this node.
     * Only relevant for imdi child nodes.
     * @return ImdiTreeObject
     */
    public ImdiTreeObject getParentDomNode() {
        if (domParentImdi == null) {
            domParentImdi = GuiHelper.imdiLoader.getImdiObject(null, getUrlString().split("#")[0]);
        }
        return domParentImdi;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public boolean isImdi() {
        if (nodeUrl != null /* && nodDom != null*/) {
            if (isImdiChild()) {
                return true;
            } else {
                return ImdiTreeObject.isStringImdi(this.getUrlString());
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
        if (nodeUrl != null) {
            return ImdiTreeObject.isStringLocal(this.getUrlString());
        } else {
            return false;
        }
    }

    /**
     * Returns a URL object for this node.
     * @return A URL that this node represents.
     */
    public URL getURL() {
        return nodeUrl;
    }

    public File getFile() {
        return new File(nodeUrl.getFile());
    }

    public String getParentDirectory() {
        String parentPath = this.getUrlString().substring(0, this.getUrlString().lastIndexOf("/")) + "/"; // this is a url so don't use the path separator
        return parentPath;
    }

    public void registerContainer(Object containerToAdd) {
//        System.out.println("registerContainer: " + containerToAdd + " : " + this);
        containersOfThisNode.add(containerToAdd);
    }

    public Object[] getRegisteredContainers() {
        return containersOfThisNode.toArray();
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
        for (Enumeration childNodesEnum = this.getChildEnum(); childNodesEnum.hasMoreElements();) {
            ImdiTreeObject currentChild = (ImdiTreeObject) childNodesEnum.nextElement();
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
        System.out.println("clearIcon: " + this);
//        System.out.println("containersOfThisNode: " + containersOfThisNode.size());
//        SwingUtilities.invokeLater(new Runnable() {

//            public void run() {
        icon = ImdiIcons.getSingleInstance().getIconForImdi(ImdiTreeObject.this); // to avoid a race condition (where the loading icons remains after load) this is also set here rather than nulling the icon
//                System.out.println("clearIcon invokeLater" + ImdiTreeObject.this.toString());
//                System.out.println("containersOfThisNode: " + containersOfThisNode.size());
        // here we need to cause an update in the tree and table gui so that the new icon can be loaded
        for (Object currentContainer : containersOfThisNode.toArray()) {
//                    System.out.println("currentContainer: " + currentContainer.toString());
            if (currentContainer instanceof ImdiTableModel) {
                ((ImdiTableModel) currentContainer).reloadTableData(); // this must be done because the fields have been replaced and nead to be reloaded in the tables
            }
            if (currentContainer instanceof ImdiChildCellEditor) {
                ((ImdiChildCellEditor) currentContainer).updateEditor(ImdiTreeObject.this);
            }
            if (currentContainer instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode currentTreeNode = (DefaultMutableTreeNode) currentContainer;
                DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) ((DefaultMutableTreeNode) currentContainer).getParent();
                if (parentNode != null) {
                    TreeHelper.getSingleInstance().updateTreeNodeChildren(parentNode);
                } else {
                    TreeHelper.getSingleInstance().updateTreeNodeChildren(currentTreeNode);
                }
            }
        }
//            }
//        });
    }

    public boolean isFavorite() {
        return isFavourite;
    }

    public void setFavouriteStatus(boolean favouriteStatus) {
        isFavourite = favouriteStatus;
        clearIcon();
    }

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
