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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import org.w3c.dom.NamedNodeMap;

/**
 *
 * @author petwit
 */
public class ImdiHelper {

    public MimeHashQueue mimeHashQueue = new MimeHashQueue(); // used to calculate mime types and md5 sums
    ImdiVocabularies imdiVocabularies = new ImdiVocabularies();
    Vector listDiscardedOfAttributes = new Vector(); // a list of all unused imdi attributes, only used for testing
    //    static Icon collapsedicon = new ImageIcon("/icons/Opener_open_black.png");
//    static Icon expandedicon = new ImageIcon("/icons/Opener_closed_black.png");
    // TODO: move these icons to the gui section of code, maybe load durring the gui creation and pass to here
    //static Object GuiHelper.linorgSessionStorage;
    static Icon corpusicon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/corpusnode_color.png"));
    static Icon corpuslocalicon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/corpuslocal16x16c.png"));
    static Icon corpuslocalservericon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/corpusserverlocal16x16c.png"));
    static Icon corpusservericon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/corpusserver16x16c.png"));//    corpusserverlocal16x16c.png corpusserver16x16c.png 
//            corpuslocal16x16c.png
//            file16x16.png
//            fileticka16x16.png
    static Icon serverIcon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/server16x16.png"));
    static Icon directoryIcon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/directory16x16.png"));
    static Icon fileIcon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/file16x16.png"));
    static Icon fileTickIcon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/filetick16x16.png"));
    static Icon fileCrossIcon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/filecross16x16.png"));
    static Icon fileUnknown = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/fileunknown16x16.png"));
    static Icon fileUnReadable = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/fileunreadable16x16.png"));
    static Icon fileServerIcon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/fileserver16x16.png"));
    static Icon fileLocalIcon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/filelocal16x16.png"));
    static Icon fileServerLocalIcon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/fileserverlocal16x16.png"));
    static Icon sessionicon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/session_color.png"));
    static Icon sessionlocalservericon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/session_color-serverlocal.png"));
    static Icon sessionlocalicon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/session_color-local.png"));
    static Icon sessionservericon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/session_color-server.png"));
    static Icon writtenresicon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/writtenresource.png"));
    static Icon mediafileicon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/mediafile.png"));
    static Icon videofileicon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/video.png"));
    static Icon audiofileicon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/audio.png"));
    static Icon picturefileicon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/pictures.png"));
    static Icon infofileicon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/infofile.png"));
    static Icon unknownnodeicon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/file.png"));
    static Icon dataicon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/data.png"));
    static Icon stopicon = new ImageIcon(GuiHelper.linorgSessionStorage.getClass().getResource("/mpi/linorg/resources/icons/stop.png"));
    //static Icon directoryIcon = UIManager.getIcon("FileView.directoryIcon");
//    static Icon fileIcon = UIManager.getIcon("FileView.fileIcon");
    //                        UIManager.getIcon("FileView.directoryIcon");
//                        UIManager.getIcon("FileView.fileIcon");
//                        UIManager.getIcon("FileView.computerIcon");
//                        UIManager.getIcon("FileView.hardDriveIcon");
//                        UIManager.getIcon("FileView.floppyDriveIcon");
//
//                        UIManager.getIcon("FileChooser.newFolderIcon");
//                        UIManager.getIcon("FileChooser.upFolderIcon");
//                        UIManager.getIcon("FileChooser.homeFolderIcon");
//                        UIManager.getIcon("FileChooser.detailsViewIcon");
//                        UIManager.getIcon("FileChooser.listViewIcon");

//    static Icon idleIcon = new ImageIcon("build/classes/mpi/linorg/resources/busyicons/idle-icon.png");            
//    static Icon corpusicon = new ImageIcon("build/classes/mpi/linorg/resources/imdi_icons/corpus.png");
//    static Icon sessionicon = new ImageIcon("build/classes/mpi/linorg/resources/imdi_icons/session.png");
//    static Icon writtenresicon = new ImageIcon("build/classes/mpi/linorg/resources/imdi_icons/transcript.png");
//    static Icon mediafileicon = new ImageIcon("build/classes/mpi/linorg/resources/imdi_icons/media.png");
//    static Icon videofileicon = new ImageIcon("build/classes/mpi/linorg/resources/imdi_icons/media.png");
//    static Icon audiofileicon = new ImageIcon("build/classes/mpi/linorg/resources/imdi_icons/media.png");
//    static Icon picturefileicon = new ImageIcon("build/classes/mpi/linorg/resources/imdi_icons/media.png");
//    static Icon infofileicon = new ImageIcon("build/classes/mpi/linorg/resources/imdi_icons/ifile.png");
//    static Icon unknownnodeicon = new ImageIcon("build/classes/mpi/linorg/resources/imdi_icons/file.png");
//    static Icon dataicon = new ImageIcon("build/classes/mpi/linorg/resources/imdi_icons/data.png");
//    static Icon stopicon = new ImageIcon("build/classes/mpi/linorg/resources/imdi_icons/stop.png");
//    static Icon idleIcon = new ImageIcon("build/classes/mpi/linorg/resources/busyicons/idle-icon.png");
    //ResourceMap resourceMap = getResourceMap();
//    ApplicationContext ctxt = getContext();
//        ResourceManager mgr = ctxt.getResourceManager();
//        //resource = mgr.getResourceMap(HelloWorld.class);
//    static Icon collapsedicon = resourceMap.getIcon ("imdiTree.collapsedicon");
//    static Icon expandedicon = resourceMap.getIcon ("imdiTree.expandedicon");
//    static Icon corpusicon = resourceMap.getIcon ("imdiTree.corpusicon");
//    static Icon sessionicon = resourceMap.getIcon ("imdiTree.sessionicon");
//    static Icon writtenresicon = resourceMap.getIcon ("imdiTree.writtenresicon");
//    static Icon mediafileicon = resourceMap.getIcon ("imdiTree.mediafileicon");
//    static Icon videofileicon = resourceMap.getIcon ("imdiTree.videofileicon");
//    static Icon audiofileicon = resourceMap.getIcon ("imdiTree.audiofileicon");
//    static Icon picturefileicon = resourceMap.getIcon ("imdiTree.picturefileicon");
//    static Icon infofileicon = resourceMap.getIcon ("imdiTree.infofileicon");
//    static Icon unknownnodeicon = resourceMap.getIcon ("imdiTree.unknownnodeicon");
//    static Icon stopicon = resourceMap.getIcon ("imdiTree.stopicon");
//    static Icon idleIcon = resourceMap.getIcon ("imdiTree.idleIcon");

    //private static OurURL baseURL = null; // for link resolution
    private static IMDIDom api = new IMDIDom();
//    private Hashtable nodeSumsHashtable = null; // this is a table of md5sums each containing a vector of all matching files. This is saved and reloaded each time the application is started
//    private Hashtable urlToNodeHashtable = new Hashtable(); // this is a table of urls that links to the imdiobject for each url
    boolean debugOn = false;

    private void debugOut(String messageString) {
        if (debugOn) {
            System.out.println(messageString);
        }
    }

    public ImdiTreeObject getTreeNodeObject(String urlString) {
        ImdiTreeObject imdiTreeObject;
        imdiTreeObject = new ImdiTreeObject(null, urlString);
        if (isStringImdi(urlString)) {
            imdiTreeObject.loadImdiDom(false);
        }
        return imdiTreeObject;
    }

    public boolean isImdiNode(Object unknownObj) {
        if (unknownObj == null) {
            return false;
        }
        return (unknownObj instanceof ImdiHelper.ImdiTreeObject);
    }

    public boolean isStringLocal(String urlString) {
        return (!urlString.startsWith("http://"));
    }

    public boolean isStringImdi(String urlString) {
        return urlString.endsWith(".imdi");
    }

    public boolean isStringImdiChild(String urlString) {
        return urlString.contains(".imdi#");
    }

    public class ImdiTreeObject implements Comparable {

        Hashtable fieldHashtable = new Hashtable();
        Hashtable childrenHashtable = new Hashtable();
        boolean imdiDataLoaded = false;
        String hashString = null;
        String mpiMimeType = null;
        int matchesLocal = 0;
        int matchesRemote = 0;
        int matchesLocalResource = 0;
        int imdiChildCounter = 0; // used to keep the imdi child nodes unique when they are added
        private String nodeText;
        private String urlString;
        private String resourceUrlString;
        private boolean isDirectory;
        private Icon icon;
        boolean nodeEnabled = true;
        String[] imdiLinkArray; // an array of links found in the imdi or the listing of the directory depending on the object
        Vector containersOfThisNode = new Vector();

        // ImdiTreeObject parentImdi; // the parent imdi not the imdi child which display
        protected ImdiTreeObject(String localNodeText, String localUrlString) {
            debugOut("ImdiTreeObject: " + localNodeText + " : " + localUrlString);
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
        }

        public void loadImdiDom(boolean useCache) {
            Document nodDom;
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
                    saveNodeToCache(nodDom);
                }
            } catch (MalformedURLException mue) {
                System.out.println("Invalid input URL: " + mue);
                nodeText = "Invalid input URL";
            }
            //we are now done with the dom so free the memory
            nodDom = null;
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
                returnArray[linkCount] = this.getUrl() + File.separatorChar + returnArray[linkCount];
            }
            return returnArray;
        }

        private String[] getImdiLinks(Document nodDom) {
            //System.out.println("getImdiLinks for: " + this.toString());
            String[] returnArray = null;
            try {
                if (nodDom != null) {
                    OurURL baseURL = new OurURL(this.getUrl());
                    debugOut("getIMDILinks");
                    IMDILink[] links = api.getIMDILinks(nodDom, baseURL, WSNodeType.CORPUS);
                    debugOut("links.length: " + links.length);
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
        public Enumeration getChildEnum() {
            return childrenHashtable.elements();
        }

        public Vector addChildNode(ImdiTreeObject nodeToAdd) {
            System.out.println("addChildNode: " + nodeToAdd);
            // TODO: the resource should be optionaly copied or moved into the cache or hardlinked
            Vector addedImdiNodes = addChildNode(nodeToAdd.mpiMimeType, nodeToAdd.getUrl());
            return addedImdiNodes;
        }

        public Vector addChildNode(String nodeType, String resourcePath) {
            System.out.println("addChildNode: " + nodeType + " : " + resourcePath);
            Vector addedImdiNodes = new Vector();
            ImdiTreeObject destinationNode;
            if (GuiHelper.imdiSchema.isImdiChildType(nodeType)) {
                destinationNode = this;
            } else {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
                String currentFileName = this.getUrl();
                if (currentFileName.endsWith(".imdi")) {
                    currentFileName = currentFileName.substring(0, currentFileName.length() - 5);
                }
                destinationNode = new ImdiTreeObject("new child", currentFileName + File.separatorChar + formatter.format(new Date()) + ".imdi");
                addedImdiNodes.add(destinationNode);
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
                if (GuiHelper.linorgJournal.saveJournalEntry(fieldToAdd.parentImdi.getUrl(), fieldToAdd.xmlPath, null, fieldToAdd.fieldValue)) {
                    destinationNode.addField(fieldToAdd, 0, addedImdiNodes, false);
                }
            }
            if (destinationNode != this) {
                childrenHashtable.put(destinationNode.getUrl(), destinationNode);
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
                            childrenHashtable.put(currentImdi.getUrl(), currentImdi);
                            if (isStringImdi(imdiLinkArray[linkCount])/* && linkCount < 9*/) { //TODO: remove this limitation of nine links
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
                        debugOut("childrenWithSiblings: " + localName);
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
                if (fieldToAdd.isDisplayable()) {
                    debugOut("nextChild: " + fieldToAdd.xmlPath + siblingSpacer + " : " + fieldToAdd.fieldValue);
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
            if (!foundNodes.containsKey(this.getUrl())) {
//                debugOut("searching: " + this.getUrl());
                if (this.getUrl().contains(searchString)) {
                    foundNodes.put(this.getUrl(), this);
                    debugOut("found: " + this.getUrl());
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

        public int[] getChildCount() {
            debugOut("getChildCount: " + this.toString());
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
            debugOut("loadNextLevelOfChildren: " + this.toString() + ":" + (System.currentTimeMillis() - stopTime));
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
            debugOut("listDiscardedOfAttributes: " + listDiscardedOfAttributes);
        }

        public String getSaveLocation() {
            if (this.isImdi() && !this.isImdiChild()) {
                //this file name must be set from the imdi url by removing the servername but keeping the path  and appending it to the destination directory
                String fileName = this.getUrl();
//                if (fileName.toLowerCase().startsWith("http://")) {
//                    fileName = fileName.substring("http://".length());
//                } else {
//                    // there may be ftp or other archive types that need different methods
//                    throw new UnsupportedOperationException("Not supported yet.");
//                }
                return getSaveLocation(fileName);
            }
            return null;
        }

        // converts a String path to the cache path
        public String getSaveLocation(String pathString) {
            String cachePath = GuiHelper.linorgSessionStorage.destinationDirectory + pathString.replace("://", "/");
            File tempFile = new File(cachePath);
            if (!tempFile.getParentFile().exists()) {
                tempFile.getParentFile().mkdirs();
            }
            return cachePath;
        }

        // this function returns the URL path to a remote resource
        public String getFullResourcePath() {
            String targetUrlString = resourceUrlString;
            if (targetUrlString.startsWith(".")) {
                String parentUrl = this.urlString.split("#")[0];
                targetUrlString = parentUrl.substring(0, parentUrl.lastIndexOf("/")) + "/" + targetUrlString;
            }
            return targetUrlString;
        }

        private void saveRemoteResource() {
            String targetUrlString = getFullResourcePath();
            System.out.println("saveRemoteResource: " + targetUrlString);
            String destinationPath = getSaveLocation(targetUrlString);
            File tempFile = new File(destinationPath);
            if (tempFile.exists()) {
                System.out.println("this resource is already in the cache");
            } else {
                try {
                    URL u = new URL(targetUrlString);
                    URLConnection yc = u.openConnection();
                    HttpURLConnection h = (HttpURLConnection) yc;
                    //h.setFollowRedirects(false);

                    System.out.println("Code: " + h.getResponseCode() + ", Message: " + h.getResponseMessage());
                    if (h.getResponseCode() != 200) {
                        System.out.println("non 200 response, skipping file");
                    } else {
                        int bufferLength = 1024 * 4;
                        FileOutputStream fout = new FileOutputStream(destinationPath); //targetUrlString
                        System.out.println("getting file");
                        InputStream stream = yc.getInputStream();
                        byte[] buffer = new byte[bufferLength]; // make htis 1024*4 or something and read chunks not the whole file
                        int bytesread = 0;
                        int totalRead = 0;
                        while (bytesread >= 0) {
                            bytesread = stream.read(buffer);
                            totalRead += bytesread;
//                        System.out.println("bytesread: " + bytesread);
//                        System.out.println("Mbs totalRead: " + totalRead / 1048576);
                            if (bytesread == -1) {
                                break;
                            }
                            fout.write(buffer, 0, bytesread);
                        }
                        System.out.println("Downloaded: " + totalRead / 1048576 + " Mbs");
                    }
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }
        }
        // before this is called it is recomended to confirm that the destinationDirectory path already exist and is correct, otherwise unintended directories maybe created
        public void saveNodeToCache(Document nodDom) {
            debugOut("saveBrachToLocal: " + this.toString());
            if (this.isImdi() && !this.isImdiChild()) {
                if (nodDom != null) {
                    //System.out.println("saveBrachToLocal: " + this.getUrl());
                    //System.out.println("saveBrachToLocal: " + this.nodDom.);

                    String destinationPath = getSaveLocation();
                    debugOut("destinationPath: " + destinationPath);
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

                }
            }
        }

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
                        saveRemoteResource();
                    }
                    mimeHashQueue.addToQueue(this);
                }
            } else {
                // pass the label to the child nodes
                String childsName = fieldToAdd.translatedPath.substring(childLevel, nextChildLevel);
                //String parentName = fieldLabel.substring(0, firstSeparator);
                debugOut("childsName: " + childsName);
                if (!childrenHashtable.containsKey(childsName)) {
                    ImdiTreeObject tempImdiTreeObject = new ImdiTreeObject(childsName, this.getUrl() + "#" + fieldToAdd.xmlPath);
                    if (addedImdiNodes != null) {
                        addedImdiNodes.add(tempImdiTreeObject);
                    }
                    tempImdiTreeObject.imdiDataLoaded = true;
                    childrenHashtable.put(childsName, tempImdiTreeObject);
                }
                ((ImdiTreeObject) childrenHashtable.get(childsName)).addField(fieldToAdd, nextChildLevel + 1, addedImdiNodes, useCache);
            }
        }

        public Hashtable getFields() {
            // store the Hastable for next call
            // if hashtable is null then load from imdi
            return fieldHashtable;
        }

        public int compareTo(Object o) throws ClassCastException {
            if (!(o instanceof ImdiTreeObject)) {
                throw new ClassCastException("ImdiTreeObject expected.");
            }
            return this.toString().compareTo(((ImdiTreeObject) o).toString());
        }
        // Return text for display
        @Override
        public String toString() {
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

        public boolean isArchivableFile() {
            return mpiMimeType != null;
        }

        public boolean hasResource() {
            return resourceUrlString != null;
        }

        public String getResource() {
            return resourceUrlString;
        }

        public String getUrl() {
            return urlString;
        }

        public void getMimeHashResult() {
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
                        if (isStringLocal(currentUrl)) {
                            if (isStringImdiChild(currentUrl)) {
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
                    return isStringImdi(urlString);
                }
            }
            return false;
        }

        public boolean isImdiChild() {
            return isStringImdiChild(urlString);
        }

        public boolean isSession() {
            // test if this node is a session
            return fieldHashtable.containsKey("Session.Name");
        }

        public boolean isLocal() {
            if (urlString != null) {
                return isStringLocal(urlString);
            } else {
                return false;
            }
        }

        public File getFile() {
            return new File(urlString.replaceFirst("file://", ""));
        }

        public void registerContainer(Object containerToAdd) {
            containersOfThisNode.add(containerToAdd);
        }

        public void removeContainer(Object containerToAdd) {
            // TODO: make sure that containers are removed when a node is removed from the tree, otherwise memory will not get freed
            containersOfThisNode.remove(containerToAdd);
        }

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
        // Return the icon
        public Icon getIcon() {
            if (icon == null) {
                this.getMimeHashResult();
                if (mpiMimeType != null) {
                    //nodeText = "isImdiChildWithType";
                    //String mediaTypeString = typeObject.toString();
                    //nodeText = mediaTypeString;
                    if (mpiMimeType.contains("audio")) {
                        icon = audiofileicon;
                    } else if (mpiMimeType.contains("video")) {
                        icon = videofileicon;
                    } else if (mpiMimeType.contains("image")) {// ?????
                        icon = picturefileicon;
                    } else if (mpiMimeType.contains("text")) {
                        icon = writtenresicon;
                    } else if (mpiMimeType.contains("nonarchivable")) {
                        icon = fileIcon;
                    } else if (mpiMimeType.contains("unreadable")) {
                        icon = fileUnReadable;
                    } else {
                        icon = fileUnknown; // TODO: add any other required icons; for now if we are not showing a known type then make it known by using an obvious icon
                        nodeText = mpiMimeType + " : " + nodeText;
                    }
                } else if (isImdi()) {
                    if (isImdiChild()) {
                        if (resourceUrlString != null && hashString == null) {
                            icon = fileCrossIcon;
                        } else {
                            icon = dataicon;
                        }
                    } else if (isSession()) {
                        if (isLocal()) {
                            if (matchesRemote == 0) {
                                icon = sessionlocalicon;
                            } else {
                                icon = sessionlocalservericon;
                            }
                        } else {
                            icon = sessionservericon;
                        }
                    } else {
                        if (isLocal()) {
                            if (matchesRemote == 0) {
                                icon = corpuslocalicon;
                            } else {
                                icon = corpuslocalservericon;
                            }
                        } else {
                            // don't show the corpuslocalservericon until the serverside is done, otherwise the icon will show only after copying a branch but not after a restart
//                            if (matchesLocal == 0) {
                            icon = corpusservericon;
//                            } else {
//                                icon = corpuslocalservericon;
//                            }
                        }
                    }
                }
            }
            if (icon == null) {
                if (this.isDirectory) {
                    icon = UIManager.getIcon("FileView.directoryIcon");
                } else {
                    if (isLocal()) {

//                        if (mpiMimeType != null) {
//                            nodeText = "[" + mpiMimeType + "]" + nodeText;
//                            icon = mediafileicon;
//                        } else {
//                            if (matchesLocalResource > 0) {
//                                icon = fileTickIcon;
//                            } else /*if (matchesRemote == 0)*/ {
                        icon = fileUnknown;
//                            }
//                        }
//                        else {
//                            icon = fileServerLocalIcon;
//                        }
                    } else {
//                        if (matchesLocal == 0) {
                        icon = fileServerIcon;
//                        } else {
//                            icon = fileServerIcon;
//                        }
                    }
                }
            }
            return icon;
        }
    }

    class ImdiField {

        public ImdiTreeObject parentImdi;
        public String xmlPath;
        public String translatedPath;
//        public String nodeName;
        private String fieldValue;
        private String fieldID;
        private String vocabularyKey;
        private boolean hasVocabularyType = false;
        public boolean vocabularyIsOpen;
        public boolean vocabularyIsList;
        private Hashtable fieldAttributes = new Hashtable();

        public ImdiField(ImdiTreeObject localParentImdi, String tempPath, String tempValue) {
            parentImdi = localParentImdi;
            fieldValue = tempValue;
            xmlPath = tempPath;
        //translatedPath = translateFieldName(tempPath + siblingSpacer);
        }

        public String getFieldValue() {
            return fieldValue;
        }

        public void setFieldValue(String fieldValue) {
            this.fieldValue = fieldValue;
            parentImdi.clearIcon();
        }

        public boolean hasVocabulary() {
            return (vocabularyKey != null);
        }

        public Enumeration getVocabulary() {
            if (vocabularyKey == null) {
                return null;
            }
            // make sure that the current value is in the list if it is an open vocabulary (this could be done in a better place ie on first load whe all the values are available)
            if (vocabularyIsOpen) {
                imdiVocabularies.addVocabularyEntry(vocabularyKey, fieldValue);
            }
            return imdiVocabularies.getVocabulary(vocabularyKey);
        }

        public boolean isDisplayable() {
            return (fieldValue != null && fieldValue.trim().length() > 0 && !xmlPath.contains("CorpusLink"));
        }

        public void finishLoading() {
            // set up the vocabularies
            if (hasVocabularyType) {
                Object linkAttribute = fieldAttributes.get("Link");
                if (linkAttribute != null) {
                    vocabularyKey = linkAttribute.toString();
                    imdiVocabularies.getVocabulary(vocabularyKey);
                }
            }
        // end set up the vocabularies
        }

        public void addAttribute(String attributeName, String attributeValue) {
            debugOut("attributeName: " + attributeName);
            debugOut("attributeValue: " + attributeValue);
            // look for the vocabulary type
            if (attributeName.equals("Type")) {
                //System.out.println("setVocabularyType");
                hasVocabularyType = true;
                if (attributeValue.equals("OpenVocabularyList")) {
                    vocabularyIsList = true;
                    vocabularyIsOpen = true;
                } else if (attributeValue.equals("OpenVocabulary")) {
                    vocabularyIsList = false;
                    vocabularyIsOpen = true;
                } else if (attributeValue.equals("ClosedVocabularyList")) {
                    vocabularyIsList = true;
                    vocabularyIsOpen = false;
                } else if (attributeValue.equals("ClosedVocabulary")) {
                    vocabularyIsList = false;
                    vocabularyIsOpen = false;
                } else {
                    hasVocabularyType = false;
                }
            }
            fieldAttributes.put(attributeName, attributeValue);
        }

        @Override
        public String toString() {
//            System.out.println("ImdiField: " + fieldValue);
//            if (!isDisplayable()) {
//                return "check attributes";// fieldAttributes.keys().toString();
//            }
            return fieldValue;
        }

        private void translateFieldName(String fieldName) {
            // replace the xml paths with user friendly node names
            fieldName = fieldName.replace(".METATRANSCRIPT.Session.Resources.WrittenResource", "WrittenResource");
            fieldName = fieldName.replace(".METATRANSCRIPT.Session.MDGroup.Actors.Actor", "Actors");
            fieldName = fieldName.replace(".METATRANSCRIPT.Session.Resources.Anonyms", "Anonyms");
            fieldName = fieldName.replace(".METATRANSCRIPT.Session.Resources.MediaFile", "MediaFiles");
            fieldName = fieldName.replace(".METATRANSCRIPT.Session.MDGroup", "");
            fieldName = fieldName.replace(".METATRANSCRIPT.Session", "Session");
            fieldName = fieldName.replace(".METATRANSCRIPT.Corpus", "Corpus");
            translatedPath = fieldName;
        }
    }
}
