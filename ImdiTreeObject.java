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
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;
import org.w3c.dom.NamedNodeMap;

/**
 *
 * @author petwit
 */
public class ImdiTreeObject implements Comparable {
    // TODO: move the api into a wrapper class
    private static IMDIDom api = new IMDIDom();
    public static MimeHashQueue mimeHashQueue = new MimeHashQueue(); // used to calculate mime types and md5 sums
    static ImdiIcons imdiIcons = new ImdiIcons();
    private static Vector listDiscardedOfAttributes = new Vector(); // a list of all unused imdi attributes, only used for testing    
    private boolean debugOn = false;
    private Hashtable fieldHashtable = new Hashtable();
    private Hashtable childrenHashtable = new Hashtable();
    private boolean imdiDataLoaded = false;
    public String hashString = null;
    public String mpiMimeType = null;
    public int matchesLocal = 0;
    public int matchesRemote = 0;
    public int matchesLocalResource = 0;
    public int imdiChildCounter = 0; // used to keep the imdi child nodes unique when they are added
    public boolean imdiNeedsSaveToDisk = false;
    private String nodeText;
    private String urlString;
    private String resourceUrlString;
    public boolean isDirectory;
    private Icon icon;
    private boolean nodeEnabled = true;
    private String[] imdiLinkArray; // an array of links found in the imdi or the listing of the directory depending on the object
    private Vector containersOfThisNode = new Vector();

    // ImdiTreeObject parentImdi; // the parent imdi not the imdi child which display
    protected ImdiTreeObject(String localNodeText, String localUrlString) {
//        debugOut("ImdiTreeObject: " + localNodeText + " : " + localUrlString);
        nodeText = localNodeText;
        urlString = localUrlString;
        icon = null;//idleIcon;//null;//new ImageIcon(getClass().getResource(imageName)); 
        isDirectory = false;
        if (urlString != null) {
            if (!isImdi() && isLocal()) {
                File fileObject = getFile();
                if (fileObject != null) {
                    this.isDirectory = fileObject.isDirectory();
                }
                nodeText = fileObject.getName();
            }
            if (!isImdi() && nodeText == null) {
                nodeText = urlString;
            }
        }
        if (!isImdiChild() && !isDirectory()) {
            // if it is an imdi or a loose file but not a direcotry then get the md5sum
            mimeHashQueue.addToQueue(this);
        }

        if (ImdiTreeObject.isStringImdi(urlString) || ImdiTreeObject.isStringImdiHistoryFile(urlString)) {
            loadImdiDom(false);
        }
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

    public String loadImdiDom(boolean useCache) {
        Document nodDom;
        // cacheLocation will be null if useCache = false hence no file has been saved
        String cacheLocation = null;
        try {
            // TODO: check that the imdi is not already in the cache and check that the url is not pointing to a cache file
            OurURL inUrlLocal = null;
            String tempUrlString;
            if (!urlString.startsWith("http") && !urlString.startsWith("file")) {
                tempUrlString = "file://" + urlString;
            } else {
                tempUrlString = urlString;
            }
            //System.out.println("tempUrlString: " + tempUrlString);
            inUrlLocal = new OurURL(tempUrlString);
            nodDom = api.loadIMDIDocument(inUrlLocal, false);
            if (nodDom == null) {
                nodeText = "Could not load IMDI";
            } else {
                //set the string name to unknown, it will be updated in the tostring function
                nodeText = "unknown";
                // load the fields from the imdi file
                iterateChildNodes(nodDom.getFirstChild(), "", useCache);
            }
            // get the links from the imdi before we dispose of the dom
            imdiLinkArray = getImdiLinks(nodDom);
            // save this to the cache before deleting the dom
            if (useCache) {
                cacheLocation = saveNodeToCache(nodDom);
            }
        } catch (MalformedURLException mue) {
            System.out.println("Invalid input URL: " + mue);
            nodeText = "Invalid input URL";
        }
        //we are now done with the dom so free the memory
        nodDom = null;
        return cacheLocation;
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
    private String[] getDirectoryLinks() {
        String[] returnArray = null;
        File nodeFile = this.getFile();
        returnArray = nodeFile.list();
        for (int linkCount = 0; linkCount < returnArray.length; linkCount++) {
            returnArray[linkCount] = this.getUrlString() + File.separatorChar + returnArray[linkCount];
        }
        return returnArray;
    }

    private String[] getImdiLinks(Document nodDom) {
        //System.out.println("getImdiLinks for: " + this.toString());
        String[] returnArray = null;
        try {
            if (nodDom != null) {
                OurURL baseURL = new OurURL(this.getUrlString());
//                debugOut("getIMDILinks");
                IMDILink[] links = api.getIMDILinks(nodDom, baseURL, WSNodeType.CORPUS);
//                debugOut("links.length: " + links.length);
                if (links != null) {
                    returnArray = new String[links.length];
                    for (int linkCount = 0; linkCount < links.length; linkCount++) {
                        returnArray[linkCount] = links[linkCount].getRawURL().toString();
                    //System.out.println("link:" + returnArray[linkCount]);
                    }
                }
            }
        } catch (MalformedURLException mue) {
            System.out.println("Error getting links: " + mue);
            returnArray = new String[]{"Invalid input file from parent"};
        }
        return returnArray;
    }

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
            imdiNeedsSaveToDisk = true;
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String currentFileName = this.getFile().getParent();
//                if (currentFileName.endsWith(".imdi")) {
            //currentFileName = currentFileName.substring(0, currentFileName.length() - 5);
//                }
            destinationNode = new ImdiTreeObject("new child", currentFileName + File.separatorChar + formatter.format(new Date()) + ".imdi");
            addedImdiNodes.add(destinationNode);
            destinationNode.imdiNeedsSaveToDisk = true;
        }
        // begin temp test
//            ImdiField fieldToAdd1 = new ImdiField("test.field", "unset");
//            fieldToAdd1.translateFieldName("test.field.translated");
//            addableImdiChild.addField(fieldToAdd1, 0);
        // end temp test
        //for (Enumeration fieldsToAdd = GuiHelper.imdiFieldViews.getCurrentGlobalView().getAlwaysShowColumns(); fieldsToAdd.hasMoreElements();) {
        for (Enumeration fieldsToAdd = GuiHelper.imdiSchema.listFieldsFor(nodeType, getNextImdiChildIdentifier(), resourcePath); fieldsToAdd.hasMoreElements();) {
            String[] currentField = (String[]) fieldsToAdd.nextElement();
            System.out.println("fieldToAdd: " + currentField[0]);
            System.out.println("valueToAdd: " + currentField[1]);
            ImdiField fieldToAdd = new ImdiField(destinationNode, currentField[0], currentField[1]);
            //fieldToAdd.translateFieldName(nodePath + siblingSpacer);
            fieldToAdd.translateFieldName(currentField[0]);
            if (GuiHelper.linorgJournal.saveJournalEntry(fieldToAdd.parentImdi.getUrlString(), fieldToAdd.xmlPath, null, fieldToAdd.fieldValue)) {
                destinationNode.addField(fieldToAdd, 0, addedImdiNodes, false);
            }
        }
        if (destinationNode != this) {
            childrenHashtable.put(destinationNode.getUrlString(), destinationNode);
        }
        return addedImdiNodes;
    }

    public ImdiTreeObject[] loadChildNodes(boolean saveToCache) {
        if (!imdiDataLoaded) {
            // if this node has been loaded then do not load again
            // to refresh the node and its children the node should be nulled and recreated
            imdiDataLoaded = true;
            if (this.isDirectory()) {
                imdiLinkArray = getDirectoryLinks();// put the directory listing into an array
            }
            if (imdiLinkArray != null) {
                if (this.isSession()) {
//                        for (int linkCount = 0; linkCount < linkArray.length && linkCount < 10; linkCount++) {
//                            //this fails because the imdi.api.getlinks returns more link types than media files
//                            //this.addField("Session.MediaFile(" + (linkCount + 1) + ").ResolvedURL", linkArray[linkCount]);
//                            //getHash(new File(linkArray[linkCount]), urlString);
//                        }
                    } else {
                    //System.out.println("loadChildNodes(non session): " + this.toString());
                    for (int linkCount = 0; linkCount < imdiLinkArray.length /*&& linkCount < 10*/; linkCount++) {
                        //System.out.println("linkArray: " + imdiLinkArray[linkCount]);
                        ImdiTreeObject currentImdi = new ImdiTreeObject(null, imdiLinkArray[linkCount]);
//                        tempImdiVector.add(currentImdi);
                        childrenHashtable.put(currentImdi.getUrlString(), currentImdi);
                        if (ImdiTreeObject.isStringImdi(imdiLinkArray[linkCount])/* && linkCount < 9*/) { //TODO: remove this limitation of nine links
                            currentImdi.loadImdiDom(saveToCache);
                        }
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

    private void iterateChildNodes(Node startNode, String nodePath, boolean useCache) {
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
            //if (localName != null && GuiHelper.imdiSchema.nodesChildrenCanHaveSiblings(nodePath + "." + localName)) {
            if (localName != null && childrenWithSiblings.containsKey(localName)) {
                // add brackets to conform with the imdi api notation
                siblingSpacer = "(" + getNextImdiChildIdentifier() + ")";
            } else {
                siblingSpacer = "";
            }
            ImdiField fieldToAdd = new ImdiField(this, nodePath, childNode.getNodeValue());
            // TODO: about to write this function
            //GuiHelper.imdiSchema.convertXmlPathToUiPath();
            if (fieldToAdd.isDisplayable()) {
//                debugOut("nextChild: " + fieldToAdd.xmlPath + siblingSpacer + " : " + fieldToAdd.fieldValue);
                fieldToAdd.translateFieldName(nodePath + siblingSpacer);
                this.addField(fieldToAdd, 0, null, useCache);
                // TODO: keep track of actual valid values here and only add to siblingCounter if siblings really exist
                // TODO: note that this method does not use any attributes without a node value
                NamedNodeMap namedNodeMap = childNode.getParentNode().getAttributes();
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
            } else if (debugOn && !fieldToAdd.xmlPath.contains("CorpusLink")) {
                // the corpus link nodes are used but via the api.getlinks so dont log them here
                NamedNodeMap namedNodeMap = childNode.getParentNode().getAttributes();
                if (namedNodeMap != null) {
                    for (int attributeCounter = 0; attributeCounter < namedNodeMap.getLength(); attributeCounter++) {
                        String attributeName = fieldToAdd.xmlPath + ":" + namedNodeMap.item(attributeCounter).getNodeName();
                        // add all attributes even if they contain no value
                        // TODO: check if this should be removed yet
                        if (!listDiscardedOfAttributes.contains(attributeName) && !attributeName.endsWith(":id")) {
                            // also ignore any id attributes that would have been attached to blank fields
                            listDiscardedOfAttributes.add(attributeName);
                        }
                    }
                }
            }
            fieldToAdd.finishLoading();
            iterateChildNodes(childNode.getFirstChild(), nodePath + "." + localName + siblingSpacer, useCache);
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
        boolean returnValue = false;
        if (imdiLinkArray != null) {
            returnValue = imdiLinkArray.length > 0;
        }
        returnValue = returnValue || childrenHashtable.size() > 0;
        return returnValue;
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

    public void loadNextLevelOfChildren(long stopTime, boolean saveToCache) {
//        debugOut("loadNextLevelOfChildren: " + this.toString() + ":" + (System.currentTimeMillis() - stopTime));
        if (System.currentTimeMillis() > stopTime) {
            return;
        }
        if (this.isImdi()) {
            if (imdiDataLoaded) {
                Enumeration nodesToAddEnumeration = childrenHashtable.elements();
                while (nodesToAddEnumeration.hasMoreElements()) {
                    // load one level of child nodes
                    ((ImdiTreeObject) nodesToAddEnumeration.nextElement()).loadNextLevelOfChildren(stopTime, saveToCache);
                //((ImdiTreeObject) nodesToAddEnumeration.nextElement()).();
                }
            } else {
                loadChildNodes(saveToCache);
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
            String parentUrl = this.urlString.split("#")[0];
            targetUrlString = parentUrl.substring(0, parentUrl.lastIndexOf("/")) + "/" + targetUrlString;
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

    /**
     * Saves the current changes from memory into a new imdi file on disk.
     * Previous imdi files are renamed and kept as a history.
     */
    public void saveChangesToCache() {
        System.out.println("saveChangesToCache");
        Document nodDom;
        OurURL inUrlLocal = null;
        if (urlString.startsWith("http")) {
            System.out.println("should not try to save remote files");
            return;
        }
        System.out.println("tempUrlString: " + this.getFile());
        try {
            if (this.getFile().exists()) {
                inUrlLocal = new OurURL(this.getFile().toURL());
                nodDom = api.loadIMDIDocument(inUrlLocal, false);
            } else {
                if (this.isSession()) {
                    nodDom = api.createIMDIDOM(WSNodeType.SESSION);
                } else {
                    nodDom = api.createIMDIDOM(WSNodeType.CORPUS);
                }
            }
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
                        IMDIElement ie = api.addIMDIElement(nodDom, currentField.xmlPath);
                        // TODO: the field is not added and this section needs to be completed
                        currentField.fieldID = ie.getDomId();
                    }
                    if (currentField.fieldNeedsSaveToDisk) {
                        // set value
                        System.out.println("trying to save: " + currentField.fieldID + " : " + currentField.getFieldValue());
                        IMDIElement target = new IMDIElement(null, currentField.fieldID);
                        target.setValue(currentField.getFieldValue());
                        IMDIElement ie = api.setIMDIElement(nodDom, target);
                        currentField.fieldNeedsSaveToDisk = false;
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
                // update the icon to indicate the change
                imdiNeedsSaveToDisk = false;
                clearIcon();
            }
        } catch (MalformedURLException mue) {
            System.out.println("Invalid input URL: " + mue);
            nodeText = "Invalid input URL";
        }
    }

    /**
     * Saves the node dom into the local cache.
     * Before this is called it is recommended to confirm that the destinationDirectory path already exist and is correct, otherwise unintended directories maybe created
     * @param nodDom The dom for this node that will be saved.
     * @return A string path of the saved location.
     */
    public String saveNodeToCache(Document nodDom) {
        String cacheLocation = null;
//        debugOut("saveBrachToLocal: " + this.toString());
        if (this.isImdi() && !this.isImdiChild()) {
            if (nodDom != null) {
                //System.out.println("saveBrachToLocal: " + this.getUrl());
                //System.out.println("saveBrachToLocal: " + this.nodDom.);

                String destinationPath = GuiHelper.linorgSessionStorage.getSaveLocation(this.getUrlString());

//                debugOut("destinationPath: " + destinationPath);
                File tempFile = new File(destinationPath);
                // only save the file if it does not exist, otherwise local changes would be lost and it would be pointless anyway
                if (tempFile.exists()) {
                    System.out.println("this imdi is already in the cache");
                } else {
                    // this function of the imdi.api will modify the imdi file as it saves it "(will be normalized and possibly de-domId-ed)"
                    // this will make it dificult to determin if changes are from this function of by the user deliberatly making a chage
                    api.writeDOM(nodDom, new File(destinationPath), false);
                    // at this point the file should exist and not have been modified by the user
                    // create hash index with server url but basedon the saved file
                    // note that if the imdi.api has changed this file then it will not be detected
                    // TODO: it will be best to change this to use the server api get mb5 sum when it is written
                    // TODO: there needs to be some mechanism to check for changes on the server and update the local copy
                    //getHash(tempFile, this.getUrl());
                    System.out.println("imdi should be saved in cache now");
                }
                // no point iterating child nodes which have not been loaded, it is better to do the outside this function
//                    Enumeration nodesToAddEnumeration = childrenHashtable.elements();
//                    while (nodesToAddEnumeration.hasMoreElements()) {
////                        ((ImdiTreeObject) nodesToAddEnumeration.nextElement()).saveBrachToLocal(destinationDirectory);
//                    }
                cacheLocation = destinationPath;

            }
        }
        return cacheLocation;
    }

    /**
     * Adds a field to the imdi node and creates imdi child nodes if required.
     * @param fieldToAdd The field to be added.
     * @param childLevel For internal use and should be zero. Used to track the distance in imdi child nodes from the imdi node.
     * @param addedImdiNodes Returns with all the imdi child nodes that have been added during the process.
     * @param useCache If true the the imdi file will be saved to the cache.
     */
    private void addField(ImdiField fieldToAdd, int childLevel, Vector addedImdiNodes, boolean useCache) {
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
            if (childsLabel.equals(".ResourceLink")) {
//                        // resolve the relative location of the file
//                        File resourceFile = new File(this.getFile().getParent(), fieldToAdd.fieldValue);
//                        resourceUrlString = resourceFile.getCanonicalPath();
                resourceUrlString = fieldToAdd.fieldValue;
                if (useCache) {
                    GuiHelper.linorgSessionStorage.updateCache(getFullResourcePath());
                }
                mimeHashQueue.addToQueue(this);
            }
        } else {
            // pass the label to the child nodes
            String childsName = fieldToAdd.translatedPath.substring(childLevel, nextChildLevel);
            //String parentName = fieldLabel.substring(0, firstSeparator);
            debugOut("childsName: " + childsName);
            if (!childrenHashtable.containsKey(childsName)) {
                ImdiTreeObject tempImdiTreeObject = new ImdiTreeObject(childsName, this.getUrlString() + "#" + fieldToAdd.xmlPath);
                if (addedImdiNodes != null) {
                    addedImdiNodes.add(tempImdiTreeObject);
                }
                tempImdiTreeObject.imdiDataLoaded = true;
                childrenHashtable.put(childsName, tempImdiTreeObject);
            }
            ((ImdiTreeObject) childrenHashtable.get(childsName)).addField(fieldToAdd, nextChildLevel + 1, addedImdiNodes, useCache);
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
        // Return text for display
        if (fieldHashtable.containsKey("Session.Name")) {
            nodeText = fieldHashtable.get("Session.Name").toString();
        } else if (fieldHashtable.containsKey("Corpus.Name")) {
            nodeText = fieldHashtable.get("Corpus.Name").toString();
        }

        String nameText = "";
        if (fieldHashtable.containsKey(".Name")) {
            nodeText = "";
            nameText = /*") " +*/ fieldHashtable.get(".Name").toString();
        } else if (fieldHashtable.containsKey(".ResourceLink")) {
            nodeText = "";
            nameText = /*") " +*/ fieldHashtable.get(".ResourceLink").toString();
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
        return urlString;
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
        if (urlString != null /* && nodDom != null*/) {
            if (isImdiChild()) {
                return true;
            } else {
                return ImdiTreeObject.isStringImdi(urlString);
            }
        }
        return false;
    }

    /**
     * Tests if this node represents an imdi file or if if it represents a child node from an imdi file (created by adding fields with child nodes).
     * @return boolean
     */
    public boolean isImdiChild() {
        return ImdiTreeObject.isStringImdiChild(urlString);
    }

    public boolean isSession() {
        // test if this node is a session
        return fieldHashtable.containsKey("Session.Name");
    }

    public boolean isLocal() {
        if (urlString != null) {
            return ImdiTreeObject.isStringLocal(urlString);
        } else {
            return false;
        }
    }

    /**
     * Returns a URL object for this node.
     * @return A URL that this node represents.
     */
    public URL getURL() {
        try {
            if (urlString.startsWith("http")) {
                return new URL(urlString);
            } else {
                return getFile().toURL();
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        //linorgWindowManager.openUrlWindow(nodeName, nodeUrl);
        }
        return null;
    }

    public File getFile() {
        return new File(urlString.replaceFirst("file://", ""));
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
            //TODO: update the icons for any duplicate nodes
            DefaultMutableTreeNode currentTreeNode = ((DefaultMutableTreeNode) containersForNode.nextElement());
            System.out.println("containersOfThisNode: " + currentTreeNode.toString());
//                //nodeChanged(TreeNode node): Invoke this method after you've changed how node is to be represented in the tree.
            try {
                if (!this.isLocal()) {
                    GuiHelper.treeHelper.remoteCorpusTreeModel.nodeChanged(currentTreeNode);
                } else if (this.isImdi()) {
                    // TODO: there has been a race condition here, probably not resolved
                    //addToQueue: ../Media/Jul050801-R.mpg
                    //addToQueue: ../Media/Jul050801-R.mpeg
                    //addToQueue: ../Media/Jul050801-R.wav
                    //null
                    //clearIcon: phidang_talk
                    //containersOfThisNode: phidang_talk
                    //Exception in thread "Thread-1" java.lang.ArrayIndexOutOfBoundsException: node has no children
                    //System.out.println("getPathToRoot: " + GuiHelper.treeHelper.localCorpusTreeModel.getPathToRoot(currentTreeNode));
                    GuiHelper.treeHelper.localCorpusTreeModel.nodeChanged(currentTreeNode);
                } else {
                    GuiHelper.treeHelper.localDirectoryTreeModel.nodeChanged(currentTreeNode);
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        // nodeStructureChanged(TreeNode node): Invoke this method if you've totally changed the children of node and its childrens children...
//                GuiHelper.treeHelper.remoteCorpusTreeModel.nodeStructureChanged(currentTreeNode);
//                GuiHelper.treeHelper.localCorpusTreeModel.nodeStructureChanged(currentTreeNode);
//                GuiHelper.treeHelper.localDirectoryTreeModel.nodeStructureChanged(currentTreeNode);
        }
    }

    /**
     * If not already done calculates the required icon for this node in its current state.
     * Once calculated the stored icon will be returned. 
     * To clear the icon and recalculate it "clearIcon()" should be called.
     * @return The icon for this node.
     */
    public Icon getIcon() {
        if (icon == null) {
            this.getMimeHashResult();
            icon = imdiIcons.getIconForImdi(this);
        }
        return icon;
    }
}
