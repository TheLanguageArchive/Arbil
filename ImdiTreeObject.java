/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import mpi.imdi.api.*;
import mpi.util.OurURL;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import java.net.MalformedURLException;
import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 *
 * @author petwit
 */
public class ImdiTreeObject implements Comparable {
    // TODO: move the api into a wrapper class
    public static IMDIDom api = new IMDIDom();
    public static MimeHashQueue mimeHashQueue = new MimeHashQueue(); // used to calculate mime types and md5 sums
    static ImdiIcons imdiIcons = new ImdiIcons();
    private static Vector listDiscardedOfAttributes = new Vector(); // a list of all unused imdi attributes, only used for testing    
    private boolean debugOn = false;
    private Hashtable fieldHashtable;
    private Hashtable childrenHashtable;
    private boolean imdiDataLoaded;
    public String hashString;
    public String mpiMimeType;
    public int matchesLocal;
    public int matchesRemote;
    public int matchesLocalResource;
    public int imdiChildCounter; // used to keep the imdi child nodes unique when they are added
    public boolean fileNotFound;
    public boolean imdiNeedsSaveToDisk;
    private String nodeText;
    private URL nodeUrl;
    private String resourceUrlString;
    public boolean isDirectory;
    private ImageIcon icon;
    private boolean nodeEnabled;
    private Vector childLinks; // each element in this vector is an array [linkPath, linkId]. When the link is from an imdi the id will be the node id, when from get links or list direcotry id will be null
    private Vector containersOfThisNode;
    public boolean isLoading;

    // ImdiTreeObject parentImdi; // the parent imdi not the imdi child which display
    protected ImdiTreeObject(String localNodeText, String localUrlString) {
//        debugOut("ImdiTreeObject: " + localNodeText + " : " + localUrlString);
        nodeText = localNodeText;
        try {
//            localUrlString = localUrlString.replace("\\", "/");
            if (!localUrlString.toLowerCase().startsWith("http") && !localUrlString.toLowerCase().startsWith("file")) {
                nodeUrl = new File(localUrlString).toURL();
            } else {
                nodeUrl = new URL(localUrlString);
            }
        } catch (Exception ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
        }
        initNodeVariables();
    }
    // static methods for testing imdi file and object types
    static public boolean isImdiNode(Object unknownObj) {
        if (unknownObj == null) {
            return false;
        }
        return (unknownObj instanceof ImdiTreeObject);
    }

    static public boolean isStringLocal(String urlString) {
        return (!urlString.startsWith("http://"));
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

    private void initNodeVariables() {
        fieldHashtable = new Hashtable();
        childrenHashtable = new Hashtable();
        imdiDataLoaded = false;
        hashString = null;
        mpiMimeType = null;
        matchesLocal = 0;
        matchesRemote = 0;
        matchesLocalResource = 0;
        imdiChildCounter = 0;
        fileNotFound = false;
        imdiNeedsSaveToDisk = false;
//    nodeText = null;
//    urlString = null;
        resourceUrlString = null;
        isDirectory = false;
        icon = null;
        nodeEnabled = true;
        childLinks = new Vector();
        containersOfThisNode = new Vector();
        isLoading = false;


        icon = null;//idleIcon;//null;//new ImageIcon(getClass().getResource(imageName)); 
        isDirectory = false;
        if (nodeUrl != null) {
            if (!isImdi() && isLocal()) {
                File fileObject = getFile();
                if (fileObject != null) {
                    this.isDirectory = fileObject.isDirectory();
                }
                nodeText = fileObject.getName();
            }
            if (!isImdi() && nodeText == null) {
                nodeText = this.getUrlString();
            }
        }
        if (!isImdi() && !isDirectory()) {
            // if it is an not imdi or a loose file but not a direcotry then get the md5sum
            mimeHashQueue.addToQueue(this);
        }
    }

    public void reloadImdiNode() {
        // TODO: iterate through imdichildnodes and subdirectories clearing as we go
        for (Enumeration childEnum = childrenHashtable.elements(); childEnum.hasMoreElements();) {
            ImdiTreeObject childNode = (ImdiTreeObject) childEnum.nextElement();
//            if (!childNode.isCorpus()) {
            childNode.reloadImdiNode();
//            }
        }

//        for (Enumeration containersForNode = containersOfThisNode.elements(); containersForNode.hasMoreElements();) {
//            Object currentContainer = containersForNode.nextElement();
//            if (currentContainer instanceof DefaultMutableTreeNode) {
//                DefaultMutableTreeNode currentTreeNode = (DefaultMutableTreeNode) currentContainer;
//                currentTreeNode.removeAllChildren();
////                for (Enumeration childToAddEnum = this.getChildEnum();childToAddEnum.hasMoreElements();){
////                    childToAddEnum
////                }
////                currentTreeNode
//            }
//        }

        initNodeVariables();
        if (ImdiTreeObject.isStringImdi(this.getUrlString()) || ImdiTreeObject.isStringImdiHistoryFile(this.getUrlString())) {
            loadImdiDom();
        }
        clearIcon();
    }

    public void loadImdiDom() {
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
                iterateChildNodes(nodDom.getFirstChild(), "");
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
            String currentLink = this.getUrlString() + dirLinkArray[linkCount];
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
     * Used to populate the child list in the show child popup in the imditable.
     * @return An enumeration of the next level child nodes.
     */
    public Enumeration getChildEnum() {
        return childrenHashtable.elements();
    }

    /**
     * Used to populate the child nodes in the table cell.
     * @return A collection of the next level child nodes.
     */
    public Collection getChildCollection() {
        return childrenHashtable.values();
    }

    /**
     * Gets the second level child nodes from the fist level child node matching the child type string.
     * Used to populate the child nodes in the table cell.
     * @param childType The name of the first level child to query.
     * @return An object array of all second level child nodes in the first level node.
     */
    public Object[] getChildNodesArray(String childType) {
        for (Enumeration childEnum = childrenHashtable.elements(); childEnum.hasMoreElements();) {
            ImdiTreeObject currentNode = (ImdiTreeObject) childEnum.nextElement();
            if (currentNode.toString().equals(childType)) {
                return currentNode.getChildCollection().toArray();
            }
        }
        return null;
    }

    public Vector addChildNode(ImdiTreeObject nodeToAdd) {
        System.out.println("addChildNode: " + nodeToAdd);
        // TODO: the resource should be optionaly copied or moved into the cache or hardlinked
        Vector addedImdiNodes = addChildNode(nodeToAdd.mpiMimeType, nodeToAdd.getUrlString());
        return addedImdiNodes;
    }

    public Vector addChildNode(String nodeType, String resourcePath) {
        System.out.println("addChildNode: " + nodeType + " : " + resourcePath);
        Vector addedImdiNodes = new Vector();
        ImdiTreeObject destinationNode;
        if (GuiHelper.imdiSchema.isImdiChildType(nodeType)) {
            destinationNode = this;
            try {
                OurURL inUrlLocal = new OurURL(this.getFile().toURL());
                Document nodDom = api.loadIMDIDocument(inUrlLocal, false);
//                System.out.println("addChildNode: insertFromTemplate");
                GuiHelper.imdiSchema.insertFromTemplate(nodeType, nodDom);
//                System.out.println("addChildNode: save");
                api.writeDOM(nodDom, this.getFile(), false);                
            } catch (Exception ex) {
//                System.out.println("addChildNode: " + ex.getMessage());
                GuiHelper.linorgBugCatcher.logError(ex);
            }
            imdiNeedsSaveToDisk = true;
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String currentFileName = this.getFile().getParent();
//                if (currentFileName.endsWith(".imdi")) {
            //currentFileName = currentFileName.substring(0, currentFileName.length() - 5);
//                }
            String targetFileName = currentFileName + File.separatorChar + formatter.format(new Date()) + ".imdi";

            GuiHelper.imdiSchema.addFromTemplate(new File(targetFileName), nodeType);
            destinationNode = GuiHelper.imdiLoader.getImdiObject("new child", targetFileName);
            this.addCorpusLink(destinationNode);
            addedImdiNodes.add(destinationNode);
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
        if (destinationNode != this) {
            childrenHashtable.put(destinationNode.getUrlString(), destinationNode);
        }
        return addedImdiNodes;
    }

    public ImdiTreeObject[] loadChildNodes() {
        if (!imdiDataLoaded) {
            // if this node has been loaded then do not load again
            // to refresh the node and its children the node should be nulled and recreated
            imdiDataLoaded = true;
            if (this.isDirectory()) {
                getDirectoryLinks();
            }
            if (!this.isSession()) {
                //getImdiFieldLinks();
                for (Enumeration childLinksEnum = childLinks.elements(); childLinksEnum.hasMoreElements();) {
                    String currentChildPath = ((String[]) childLinksEnum.nextElement())[0];
                    ImdiTreeObject currentImdi = GuiHelper.imdiLoader.getImdiObject(null, currentChildPath);
                    childrenHashtable.put(currentImdi.getUrlString(), currentImdi);
                    if (ImdiTreeObject.isStringImdi(currentChildPath)) {
                        currentImdi.loadImdiDom();
                    }
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
        Vector<ImdiTreeObject> tempImdiVector = new Vector();
        Enumeration nodesToAddEnumeration = childrenHashtable.elements();
        while (nodesToAddEnumeration.hasMoreElements()) {
            tempImdiVector.add((ImdiTreeObject) nodesToAddEnumeration.nextElement());
        }
        ImdiTreeObject[] returnImdiArray = new ImdiTreeObject[tempImdiVector.size()];
        tempImdiVector.toArray(returnImdiArray);
        return returnImdiArray;
    }

    private void iterateChildNodes(Node startNode, String nodePath) {
//        System.out.println("iterateChildNodes: " + nodePath);
        String siblingSpacer = "";
        Vector childNames = new Vector();
        Hashtable childrenWithSiblings = new Hashtable();
        // loop through all the child nodes to find any that have more than one of the same name and therefore should be in a imdichildnode
        for (Node childNode = startNode; childNode != null; childNode = childNode.getNextSibling()) {
            String localName = childNode.getLocalName();
            if (localName != null) {
                if (childNames.contains(localName)) {
//                        if (childNode.getChildNodes().getLength() > 1) /* this is to prevent nodes with only text and no sub nodes getting imdichilds node all to themselves */ {
                    childrenWithSiblings.put(localName, 1);
//                    debugOut("childrenWithSiblings: " + localName);
//                        }
                } else {
                    childNames.add(localName);
                }
            }
        }
        // add the fields and nodes 
        for (Node childNode = startNode; childNode != null; childNode = childNode.getNextSibling()) {
            String localName = childNode.getLocalName();
            String siblingNodePath = nodePath + ImdiSchema.imdiPathSeparator + localName;
            //if (localName != null && GuiHelper.imdiSchema.nodesChildrenCanHaveSiblings(nodePath + "." + localName)) {
            if (localName != null && childrenWithSiblings.containsKey(localName)) {
                // add brackets to conform with the imdi api notation
                siblingSpacer = "(" + getNextImdiChildIdentifier() + ")";
            } else {
                siblingSpacer = "";
            }
//            System.out.println("getLocalName: " + childNode.getLocalName());
//            System.out.println("hasChildNodes: " + childNode.hasChildNodes());
            boolean shouldAddCurrent = false;
            NodeList childNodes = childNode.getChildNodes();
            // if there is no child nodes or there is only one and it is text then add the field
            if ((childNodes.getLength() == 0 && localName != null) || (childNodes.getLength() == 1 && childNodes.item(0).getNodeType() == Node.TEXT_NODE)) {
//                System.out.println("should add");
                shouldAddCurrent = true;
            }
//            System.out.println("getChildNodes: " + childNode.getChildNodes().getLength());
            String fieldValue;
            if (childNodes.getLength() == 1) {
                fieldValue = childNodes.item(0).getTextContent();
            } else {
                fieldValue = "";
            }
            ImdiField fieldToAdd = new ImdiField(this, siblingNodePath, fieldValue);

            // TODO: about to write this function
            //GuiHelper.imdiSchema.convertXmlPathToUiPath();

            // TODO: keep track of actual valid values here and only add to siblingCounter if siblings really exist
            // TODO: note that this method does not use any attributes without a node value
            NamedNodeMap namedNodeMap = childNode.getAttributes();
            if (namedNodeMap != null) {
                for (int attributeCounter = 0; attributeCounter < namedNodeMap.getLength(); attributeCounter++) {
                    String attributeName = namedNodeMap.item(attributeCounter).getNodeName();
                    String attributeValue = namedNodeMap.item(attributeCounter).getNodeValue();
                    if (attributeValue != null && attributeValue.length() > 0) {
                        // only add attributes if they contain a value
                        fieldToAdd.addAttribute(attributeName, attributeValue);
                    }
                }
            }
            if (shouldAddCurrent && fieldToAdd.isDisplayable()) {
//                debugOut("nextChild: " + fieldToAdd.xmlPath + siblingSpacer + " : " + fieldToAdd.fieldValue);
                fieldToAdd.translateFieldName(siblingNodePath + siblingSpacer);
                this.addField(fieldToAdd, 0, null);
            } else if (shouldAddCurrent && fieldToAdd.xmlPath.contains("CorpusLink") && fieldValue.length() > 0) {
                String parentPath = this.getParentDirectory();
                System.out.println("LinkValue: " + fieldValue);
                System.out.println("ParentPath: " + parentPath);
                System.out.println("Parent: " + this.getUrlString());
                String linkPath;
                try {
                    if (!fieldToAdd.getFieldValue().toLowerCase().startsWith("http")) {
//                    linkPath = parentPath /*+ File.separatorChar*/ + fieldToAdd.getFieldValue();
                        linkPath = parentPath + fieldToAdd.getFieldValue();
                    } else if (fieldToAdd.getFieldValue().toLowerCase().startsWith("&root;")) {
                        linkPath = parentPath + fieldToAdd.getFieldValue().substring(6);
                    } else {
                        linkPath = fieldToAdd.getFieldValue();
                    }
                    System.out.println("linkPath: " + linkPath);
                    //linkPath = linkPath.replaceAll("/[^/]*/\\.\\./", "/");
                    System.out.println("linkPathCorrected: " + linkPath);
                    childLinks.add(new String[]{linkPath, fieldToAdd.fieldID});
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
//                    System.out.println("Exception CorpusLink: " + ex.getMessage());
                }
            }
//            if (debugOn && !fieldToAdd.xmlPath.contains("CorpusLink")) {
//                // the corpus link nodes are used but via the api.getlinks so dont log them here
//                NamedNodeMap namedNodeMap = childNode.getParentNode().getAttributes();
//                if (namedNodeMap != null) {
//                    for (int attributeCounter = 0; attributeCounter < namedNodeMap.getLength(); attributeCounter++) {
//                        String attributeName = fieldToAdd.xmlPath + ":" + namedNodeMap.item(attributeCounter).getNodeName();
//                        // add all attributes even if they contain no value
//                        // TODO: check if this should be removed yet
//                        if (!listDiscardedOfAttributes.contains(attributeName) && !attributeName.endsWith(":id")) {
//                            // also ignore any id attributes that would have been attached to blank fields
//                            listDiscardedOfAttributes.add(attributeName);
//                        }
//                    }
//                }
//            }
            fieldToAdd.finishLoading();
            iterateChildNodes(childNode.getFirstChild(), siblingNodePath + siblingSpacer);
        }
    }

    public void searchNodes(Hashtable foundNodes, String searchString) {
        if (!foundNodes.containsKey(this.getUrlString())) {
//                debugOut("searching: " + this.getUrl());
            if (this.getUrlString().contains(searchString)) {
                foundNodes.put(this.getUrlString(), this);
                debugOut("found: " + this.getUrlString());
            }
        }
        Enumeration nodesToAddEnumeration = childrenHashtable.elements();
        while (nodesToAddEnumeration.hasMoreElements()) {
            ((ImdiTreeObject) nodesToAddEnumeration.nextElement()).searchNodes(foundNodes, searchString);
        }
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

    public int[] getChildCount() {
//        debugOut("getChildCount: " + this.toString());
        int[] returnArray = new int[2];
        returnArray[0] = 0;
        returnArray[1] = 0;
        if (imdiDataLoaded) {
            returnArray[1] += 1; // count this node
            Enumeration nodesToAddEnumeration = childrenHashtable.elements();
            while (nodesToAddEnumeration.hasMoreElements()) {
                // count the child nodes
                int[] childCount = ((ImdiTreeObject) nodesToAddEnumeration.nextElement()).getChildCount();
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
     * Resolves the full path to a resource file if it exists.
     * @return The path to remote resource if it exists.
     */
    public String getFullResourcePath() {
        String targetUrlString = resourceUrlString;
        if (targetUrlString.startsWith(".")) {
            targetUrlString = this.getParentDirectory() + targetUrlString;
        }
        return targetUrlString;
    }

    /**
     * Checks if there are changes saved on disk that have not been sent to the server.
     * @return boolean
     */
    public boolean needsChangesSentToServer() {
        return new File(this.getFile().getPath() + ".0").exists();
    }

    private void getAllFields(Vector allFields) {
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

    public void deleteCorpusLink(ImdiTreeObject targetImdiNode) {
        System.out.println("deleteCorpusLink: " + targetImdiNode.getUrlString());
        Document nodDom;
        try {
            OurURL inUrlLocal = new OurURL(this.getFile().toURL());
            nodDom = api.loadIMDIDocument(inUrlLocal, false);
//            System.out.println("Trying to delete: " + targetImdiNode + " from: " + this.toString());
            // retrieve the node id for the link
            String linkIdString = null;
            for (Enumeration childLinksEnum = childLinks.elements(); childLinksEnum.hasMoreElements();) {
                String[] currentLinkPair = ((String[]) childLinksEnum.nextElement());
                String currentChildPath = currentLinkPair[0];
//                System.out.println("currentChildPath: " + currentChildPath);
//                System.out.println("targetImdiNode :  " + targetImdiNode.getUrlString());
                if (currentChildPath.equals(targetImdiNode.getUrlString())) {
//                    System.out.println("currentLinkPair[1]: " + currentLinkPair[1]);
                    linkIdString = currentLinkPair[1];
                    break;
                }
            }
//            System.out.println("linkIdString: " + linkIdString);

            IMDIElement target = new IMDIElement(null, linkIdString);
//            System.out.println("attempting to remove link");
            api.removeIMDIElement(nodDom, target);
            api.writeDOM(nodDom, this.getFile(), false);
            reloadImdiNode();
//            throw (new Exception("deleteCorpusLink not yet implemented"));
        } catch (Exception ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
//            System.out.println("Exception: " + ex.getMessage());
        }
    }

    public void addCorpusLink(ImdiTreeObject targetImdiNode) {
        // if needs saving then save now while you can
        // TODO: it would be nice to warn the user about this, but its a corpus node so maybe it is not important
        if (imdiNeedsSaveToDisk) {
            saveChangesToCache();
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
            }
            // TODO: at this point due to the api we cannot get the id of the newly created link, so we will probably have to unload this object and reload the dom
            api.createIMDILink(nodDom, inUrlLocal, targetImdiNode.getUrlString(), targetImdiNode.toString(), nodeType, "");
            api.writeDOM(nodDom, this.getFile(), false);
            reloadImdiNode();
        } catch (Exception ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
//            System.out.println("Exception: " + ex.getMessage());
        }
    }

    public void updateImdiFileNodeIds() {
        System.out.println("updateNodeIds: " + this.getFile());
        try {
            System.out.println("removing NodeIds");
            OurURL inUrlLocal = new OurURL(this.getFile().toURL());
            Document nodDom = api.loadIMDIDocument(inUrlLocal, false);
            api.writeDOM(nodDom, this.getFile(), true);
            System.out.println("adding NodeIds");
            Document nodDomSecondLoad = api.loadIMDIDocument(inUrlLocal, false, null);
            api.writeDOM(nodDomSecondLoad, this.getFile(), false);
            System.out.println("reloading updateNodeIds");
            reloadImdiNode();
        } catch (Exception mue) {
            GuiHelper.linorgBugCatcher.logError(mue);
            System.out.println("Invalid input URL: " + mue);
        }
    }

    /**
     * Saves the current changes from memory into a new imdi file on disk.
     * Previous imdi files are renamed and kept as a history.
     */
    public void saveChangesToCache() {
        System.out.println("saveChangesToCache");
        Document nodDom;
        OurURL inUrlLocal = null;
        if (nodeUrl.getProtocol().toLowerCase().startsWith("http")) {
            System.out.println("should not try to save remote files");
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
                //String destinationPath = GuiHelper.linorgSessionStorage.getSaveLocation(this.getUrl());
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
                System.out.println("writeDOM");
                // make the required changes to the dom
                // TODO: make the changes to the dom before saving
                // refer to: /data1/repos/trunk/src/java/mpi/imdi/api/TestDom.java

                Vector allFields = new Vector();
                getAllFields(allFields);


                for (Enumeration<ImdiField> fieldsEnum = allFields.elements(); fieldsEnum.hasMoreElements();) {
                    ImdiField currentField = fieldsEnum.nextElement();
                    if (currentField.fieldID == null) {
                        // here we are assuming that if there is no fieldID then it is a new field so it will be added here
//                        IMDIElement ie = api.addIMDIElement(nodDom, currentField.xmlPath);
//                        // TODO: the field is not added and this section needs to be completed
//                        currentField.fieldID = ie.getDomId();
                    }
                    if (currentField.fieldNeedsSaveToDisk) {
                        if (currentField.fieldID == null) {
                            String apiPath = currentField.xmlPath.replace(".METATRANSCRIPT.", "");
                            System.out.println("trying to add: " + apiPath + " : " + currentField.getFieldValue());
                            api.addIMDIElement(nodDom, apiPath);
                            api.setIMDIValue(nodDom, apiPath, currentField.getFieldValue());
                            currentField.fieldNeedsSaveToDisk = false;
                        } else {
                            // set value
                            System.out.println("trying to save: " + currentField.fieldID + " : " + currentField.getFieldValue());
                            IMDIElement target = new IMDIElement(null, currentField.fieldID);
                            target.setValue(currentField.getFieldValue());
                            IMDIElement ie = api.setIMDIElement(nodDom, target);
                            currentField.fieldNeedsSaveToDisk = false;
                        }
//                            checkOkay("Set IMDI element: " + args, ie != null);
//                            if (ie != null) {
//                                printElement(ie);                    // save the dom / imdi file
//                            }
                    }
                }


                // add element
//                    IMDIElement ie = api.addIMDIElement(nodDom, args);
//                    checkOkay("Create IMDI element: " + args, ie != null);
//                    if (ie != null) {
//                        printElement(ie);
//                    }
//                    api.getIMDIElement(nodDom, hashString);
//                    api.setIMDIElement(nodDom, arg1);
//                    writeDOM(nodDom, this.getFile(), false);
//                    // remove element
//                    IMDIElement target = null;
//                    if ((args.indexOf('.') != -1) || !args.startsWith("i")) {
//                        target = new IMDIElement(args);
//                    } else {
//                        target = new IMDIElement("-none-", args);
//                    }
//                    checkOkay("Delete IMDI element " + target,
//                            api.removeIMDIElement(dom, target));






                api.writeDOM(nodDom, this.getFile(), false);
                reloadImdiNode();
                // update the icon to indicate the change
                imdiNeedsSaveToDisk = false;
            }
        } catch (MalformedURLException mue) {
            GuiHelper.linorgBugCatcher.logError(mue);
//            System.out.println("Invalid input URL: " + mue);
            nodeText = "Invalid input URL";
        }
        clearIcon();
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
    /**
     * Adds a field to the imdi node and creates imdi child nodes if required.
     * @param fieldToAdd The field to be added.
     * @param childLevel For internal use and should be zero. Used to track the distance in imdi child nodes from the imdi node.
     * @param addedImdiNodes Returns with all the imdi child nodes that have been added during the process.
     * @param useCache If true the the imdi file will be saved to the cache.
     */
    private void addField(ImdiField fieldToAdd, int childLevel, Vector addedImdiNodes) {
        // TODO: modify this so that each child node gets the full filename and full xml path
//            if (isImdi()) {
//                if (fieldLabel.startsWith("Session.")) {
//                    fieldLabel = fieldLabel.substring(8);
//                } else if (fieldLabel.startsWith("Corpus.")) {
//                    fieldLabel = fieldLabel.substring(7);
//                }
//            }
        //fieldUrl.substring(firstSeparator + 1)
        int nextChildLevel = fieldToAdd.translatedPath.replace(")", "(").indexOf("(", childLevel);
        debugOut("fieldLabel: " + fieldToAdd.translatedPath + " cellValue: " + fieldToAdd.fieldValue + " childLevel: " + childLevel + " nextChildLevel: " + nextChildLevel);
        if (nextChildLevel == -1) {
            // add the label to this level node
//                if (fieldLabel == null) fieldLabel = "oops null";
//                if (fieldValue == null) fieldValue = "oops null";
            String childsLabel = fieldToAdd.translatedPath.substring(childLevel);
            fieldHashtable.put(childsLabel, fieldToAdd);

//                if (childsLabel.endsWith(".Date")) {
//                    DateFormat df = new SimpleDateFormat("yyyy-MM-DD");
//                    try {
//                        nodeDate = df.parse(fieldToAdd.fieldValue);
//                        if (minNodeDate == null) {
//                            minNodeDate = nodeDate;
//                            maxNodeDate = nodeDate;
//                        }
//                        if (nodeDate.before(minNodeDate)) {
//                            minNodeDate = nodeDate;
//                        }
//                        if (nodeDate.after(maxNodeDate)) {
//                            maxNodeDate = nodeDate;
//                        }
//                    } catch (Exception ex) {
//                        System.err.println(ex.getMessage());
//                    }
//                }
            // if the node contains a ResourceLink then save the location in resourceUrlString and create a hash for the file
            if (childsLabel.equals(ImdiSchema.imdiPathSeparator + "ResourceLink")) {
//                        // resolve the relative location of the file
//                        File resourceFile = new File(this.getFile().getParent(), fieldToAdd.fieldValue);
//                        resourceUrlString = resourceFile.getCanonicalPath();
                resourceUrlString = fieldToAdd.fieldValue;
//                if (useCache) {
//                    GuiHelper.linorgSessionStorage.updateCache(getFullResourcePath());
//                }
                mimeHashQueue.addToQueue(this);
            }
        } else {
            // pass the label to the child nodes
            String childsName = fieldToAdd.translatedPath.substring(childLevel, nextChildLevel);
            //String parentName = fieldLabel.substring(0, firstSeparator);
            debugOut("childsName: " + childsName);
            if (!childrenHashtable.containsKey(childsName)) {
                ImdiTreeObject tempImdiTreeObject = GuiHelper.imdiLoader.getImdiObject(childsName, this.getUrlString() + "#" + fieldToAdd.xmlPath);
                if (addedImdiNodes != null) {
                    addedImdiNodes.add(tempImdiTreeObject);
                }
                tempImdiTreeObject.imdiDataLoaded = true;
                childrenHashtable.put(childsName, tempImdiTreeObject);
            }
            ((ImdiTreeObject) childrenHashtable.get(childsName)).addField(fieldToAdd, nextChildLevel + 1, addedImdiNodes);
        }
    }

    /**
     * Gets the fields in this node, this does not include any imdi child fields.
     * To get all fields relevant the imdi file use "getAllFields()" which includes imdi child fields.
     * @return A hashtable of the fields
     */
    public Hashtable getFields() {
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
        return this.toString().compareTo(((ImdiTreeObject) o).toString());
    }

    @Override
    public String toString() {
        if (isLoading) {
            return "loading imdi..."; // note that this is different from the text shown my treehelper "adding..."
        }
        // Return text for display
        if (fieldHashtable.containsKey("Session" + ImdiSchema.imdiPathSeparator + "Name")) {
            nodeText = fieldHashtable.get("Session" + ImdiSchema.imdiPathSeparator + "Name").toString();
        } else if (fieldHashtable.containsKey("Corpus" + ImdiSchema.imdiPathSeparator + "Name")) {
            nodeText = fieldHashtable.get("Corpus" + ImdiSchema.imdiPathSeparator + "Name").toString();
        }

        String nameText = "";
        if (fieldHashtable.containsKey(ImdiSchema.imdiPathSeparator + "Name")) {
            nodeText = "";
            nameText = /*") " +*/ fieldHashtable.get(ImdiSchema.imdiPathSeparator + "Name").toString();
        } else if (fieldHashtable.containsKey(ImdiSchema.imdiPathSeparator + "ResourceLink")) {
            nodeText = "";
            nameText = /*") " +*/ fieldHashtable.get(ImdiSchema.imdiPathSeparator + "ResourceLink").toString();
        }
//            if (mpiMimeType != null) {
//            return " [L:" + matchesLocal + " R:" + matchesRemote + " LR:" + matchesLocalResource + "]" + nodeText + " : " + hashString + ":" + mpiMimeType + ":" + resourceUrlString;
//            } else {
        return nodeText + nameText;
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
     * Tests if a resource file is associated with this node.
     * @return boolean
     */
    public boolean hasResource() {
        return resourceUrlString != null;
    }

    /**
     * Gets the ULR string of the resource file if it is available.
     * @return a URL string of the resource file
     */
    public String getResource() {
        return resourceUrlString;
    }

    /**
     * Gets the ULR string provided when the node was created.
     * @return a URL string of the IMDI
     */
    public String getUrlString() {
        return nodeUrl.toString();
    }

    private void getMimeHashResult() {
        hashString = mimeHashQueue.getHashResult(this);
        mpiMimeType = mimeHashQueue.getMimeResult(this);

        // there is no point counting matches when the hash does not exist, ie when there is no file.          
        if (hashString != null) {
            //System.out.println("countMatches <<<<<<<<<<< " + this.toString());
            matchesLocal = 0;
            matchesRemote = 0;
            matchesLocalResource = 0;
            if (hashString != null) {
                for (Enumeration listOfMatches = mimeHashQueue.getDuplicateList(hashString); listOfMatches.hasMoreElements();) {
                    String currentUrl = listOfMatches.nextElement().toString();
                    //System.out.println("currentUrl: " + currentUrl);
                    if (ImdiTreeObject.isStringLocal(currentUrl)) {
                        if (ImdiTreeObject.isStringImdiChild(currentUrl)) {
                            matchesLocalResource++;
                        } else {
                            matchesLocal++;
                        }
                    } else {
                        matchesRemote++;
                    }
                }
            //System.out.println(">>> [L:" + matchesLocal + " R:" + matchesRemote + "]");
            }
        }
    }

    public int getNextImdiChildIdentifier() {
        return imdiChildCounter++;
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
        return fieldHashtable.containsKey("Session" + ImdiSchema.imdiPathSeparator + "Name");
    }

    public boolean isCorpus() {
        // test if this node is a session
        return fieldHashtable.containsKey("Corpus" + ImdiSchema.imdiPathSeparator + "Name");
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
        containersOfThisNode.add(containerToAdd);
    }

    /**
     * Removes a UI containers from the list of containers interested in this node.
     * @param containerToRemove The container to be removed from the list.
     */
    public void removeContainer(Object containerToRemove) {
        // TODO: make sure that containers are removed when a node is removed from the tree, otherwise memory will not get freed
        containersOfThisNode.remove(containerToRemove);
    }

    /**
     * Clears the icon calculated in "getIcon()" and notifies any UI containers of this node.
     */
    public void clearIcon() {
        System.out.println("clearIcon: " + this.toString());
        icon = null;
        // here we need to cause an update in the tree gui so that the new icon can be loaded
        for (Enumeration containersForNode = containersOfThisNode.elements(); containersForNode.hasMoreElements();) {
            Object currentContainer = containersForNode.nextElement();
            if (currentContainer instanceof ImdiTableModel) {
//                ((ImdiTableModel) currentContainer).fireTableDataChanged();
                ((ImdiTableModel) currentContainer).reloadTableData(); // this must be done because the fields have been replaced and nead to be reloaded in the tables
            }
            if (currentContainer instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode currentTreeNode = (DefaultMutableTreeNode) currentContainer;
                System.out.println("containersOfThisNode: " + currentTreeNode.toString());
//                //nodeChanged(TreeNode node): Invoke this method after you've changed how node is to be represented in the tree.
                /////////////////////////////////////
                // resort the branch since the node name may have changed
                DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) ((DefaultMutableTreeNode) currentContainer).getParent();
                ArrayList children = Collections.list(parentNode.children());
                Collections.sort(children, new TreeStringComparator());
//                parentNode.removeAllChildren();
                Iterator childrenIterator = children.iterator();
                while (childrenIterator.hasNext()) {
                    DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) childrenIterator.next();
                    parentNode.add(currentNode);
                }
                /////////////////////////////////////

                try {
                    if (!this.isLocal()) {
                        GuiHelper.treeHelper.remoteCorpusTreeModel.nodeChanged(currentTreeNode);
                        GuiHelper.treeHelper.remoteCorpusTreeModel.nodeStructureChanged(currentTreeNode);
                        GuiHelper.treeHelper.remoteCorpusTreeModel.nodeStructureChanged(parentNode);
                    } else if (this.isImdi()) {
                        GuiHelper.treeHelper.localCorpusTreeModel.nodeChanged(currentTreeNode);
                        GuiHelper.treeHelper.localCorpusTreeModel.nodeStructureChanged(currentTreeNode);
                        GuiHelper.treeHelper.localCorpusTreeModel.nodeStructureChanged(parentNode);
                    } else {
                        GuiHelper.treeHelper.localDirectoryTreeModel.nodeChanged(currentTreeNode);
                        GuiHelper.treeHelper.localDirectoryTreeModel.nodeStructureChanged(currentTreeNode);
                        GuiHelper.treeHelper.localDirectoryTreeModel.nodeStructureChanged(parentNode);
                    }
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
//                    System.out.println(ex.getMessage());
                }
//                ////////////////////
//                DefaultMutableTreeNode parentTreeNode = (DefaultMutableTreeNode) ((DefaultMutableTreeNode) currentContainer).getParent();
//                int currentIndex = parentTreeNode.getIndex(currentTreeNode);
//                int childNodeCounter = 0;
//                for (Enumeration childNodeEnum = parentTreeNode.children(); childNodeEnum.hasMoreElements();) {
//                    DefaultMutableTreeNode siblingTreeNode = (DefaultMutableTreeNode) childNodeEnum.nextElement();
//                    System.out.println("currentTreeNode: " + currentTreeNode.toString());
//                    System.out.println("siblingTreeNode: " + siblingTreeNode.toString());
//                    int compValue = siblingTreeNode.toString().compareToIgnoreCase(currentTreeNode.toString());
//                    System.out.println("compare: " + compValue);
//                    if (currentTreeNode != siblingTreeNode) {
//                        if (compValue >= 0) {
//                            System.out.println("swap to here if not lesser: " + currentIndex + " : " + childNodeCounter);
//                            parentTreeNode.remove(currentTreeNode);
//                            int targetIndex = childNodeCounter - 1;
//                            if (targetIndex < 0) {
//                                targetIndex = 0;
//                            }
//                            parentTreeNode.insert(currentTreeNode, targetIndex);
//                            GuiHelper.treeHelper.localCorpusTreeModel.nodeStructureChanged(currentTreeNode);
//                            GuiHelper.treeHelper.localCorpusTreeModel.nodeStructureChanged(parentTreeNode);
////                           /////////// GuiHelper.treeHelper.localCorpusTreeModel.reload();
//                            break;
//                        }
//                    }
//                    childNodeCounter++;
//                }
//            ////////////////////
            }
        }
    }

    class TreeStringComparator implements Comparator {

        public int compare(Object object1, Object object2) {
            if (!(object1 instanceof DefaultMutableTreeNode && object2 instanceof DefaultMutableTreeNode)) {
                throw new IllegalArgumentException("not a DefaultMutableTreeNode object");
            }
            String string1 = ((DefaultMutableTreeNode) object1).getUserObject().toString();
            String string2 = ((DefaultMutableTreeNode) object2).getUserObject().toString();
            return string1.compareToIgnoreCase(string2);
        }
    }

    /**
     * If not already done calculates the required icon for this node in its current state.
     * Once calculated the stored icon will be returned. 
     * To clear the icon and recalculate it "clearIcon()" should be called.
     * @return The icon for this node.
     */
    public ImageIcon getIcon() {
        if (icon == null) {
            this.getMimeHashResult();
            icon = imdiIcons.getIconForImdi(this);
        }
        return icon;
    }
}
