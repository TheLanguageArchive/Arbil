/*
 * ImdiSchema is used to read the imdi schema 
 * and provide a list of valid fields and field constraints
 */
package mpi.linorg;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import javax.imageio.*;
import javax.imageio.metadata.*;
import mpi.util.OurURL;
import org.w3c.dom.*;

/**
 *
 * @author petwit
 */
public class ImdiSchema {

    /**
     * When complete this function will parse the imdi schema
     */
    static String imdiPathSeparator = ".";

    public boolean nodeCanExistInNode(ImdiTreeObject parentImdiObject, ImdiTreeObject childImdiObject) {
        String parentPath = getNodePath((ImdiTreeObject) parentImdiObject);
        String childPath = getNodePath((ImdiTreeObject) childImdiObject);
//        System.out.println("nodeCanExistInNode: " + parentPath + " : " + childPath);
        return childPath.startsWith(parentPath);
    }

    private String getNodePath(ImdiTreeObject targetImdiObject) {
        String xpath;
        xpath = imdiPathSeparator + "METATRANSCRIPT" + imdiPathSeparator + "Session";
        Object[] nodePathArray = ((ImdiTreeObject) targetImdiObject).getUrlString().split("#");
//        System.out.println("nodePath0: " + nodePathArray[0]);
        if (nodePathArray.length > 1) {
            String nodePath = nodePathArray[1].toString();
//            System.out.println("nodePath1: " + nodePath);
            // convert the dot path to xpath
            xpath = nodePath.replaceAll("(\\(.?\\))?\\.", ".");
//                xpath = nodePath.replaceAll("(\\(.?\\))?", "/");
//            System.out.println("xpath: " + xpath);
        }
        return xpath;
    }

    private Vector getSubnodesFromTemplatesDir(final String nodepath) {
        Vector returnVector = new Vector();
        System.out.println("getSubnodesOf: " + nodepath);
        String[] templatesArray = {"METATRANSCRIPT.Session.xml",
            "METATRANSCRIPT.Session.MDGroup.Content.Languages.Language.xml",
            "METATRANSCRIPT.Session.Resources.MediaFile.xml",
            "METATRANSCRIPT.Corpus.xml",
            "METATRANSCRIPT.Session.Resources.WrittenResource.xml",
            "METATRANSCRIPT.Session.MDGroup.Actors.Actor.xml",
            "METATRANSCRIPT.Session.Resources.Source.xml"
        };
        try {
            File templatesDirectory = new File(this.getClass().getResource("/mpi/linorg/resources/templates/").getFile());
            if (templatesDirectory.exists()) { // compare the templates directory to the array and throw if there is a discrepancy
                String[] testingListing = templatesDirectory.list();
                int linesRead = 0;
                for (String currentTemplate : templatesArray) {
                    if (testingListing != null) {
                        if (!testingListing[linesRead].equals(currentTemplate)) {
                            System.out.println(testingListing[linesRead] + " : " + currentTemplate);
                            throw new Exception("error in the templates array");
                        }
                    }
                    currentTemplate = "." + currentTemplate;
                    if (!currentTemplate.endsWith("Session.xml")) { // sessions cannot be added to a session
                        if (currentTemplate.startsWith(nodepath)) {
                            String currentTemplateXPath = currentTemplate.replaceFirst("\\.xml$", "");
                            String currentTemplateName = currentTemplateXPath.substring(currentTemplateXPath.lastIndexOf(".") + 1);
                            returnVector.add(new String[]{currentTemplateName, currentTemplateXPath});
                        }
                    }
                    linesRead++;
                }
                if (testingListing != null) {
                    if (testingListing.length != linesRead) {
                        System.out.println(testingListing[linesRead]);
                        throw new Exception("error missing line in the templates array");
                    }
                }
            }
        } catch (Exception ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
        }
        Collections.sort(returnVector, new Comparator() {

            public int compare(Object o1, Object o2) {
                String value1 = ((String[]) o1)[0];
                String value2 = ((String[]) o2)[0];
                return value1.compareTo(value2);
            }
        });
        return returnVector;
    }

    /**
     * This function is only a place holder and will be replaced.
     * @param targetNodeUserObject The imdi node that will receive the new child.
     * @return An enumeration of Strings for the available child types, one of which will be passed to "listFieldsFor()".
     */
    public Enumeration listTypesFor(Object targetNodeUserObject) {
        // temp method for testing until replaced
        // TODO: implement this using data from the xsd on the server (server version needs to be updated)
        Vector childTypes = new Vector();
        if (targetNodeUserObject instanceof ImdiTreeObject) {
            if (((ImdiTreeObject) targetNodeUserObject).isSession() || ((ImdiTreeObject) targetNodeUserObject).isImdiChild()) {
                String xpath = getNodePath((ImdiTreeObject) targetNodeUserObject);
                childTypes = getSubnodesFromTemplatesDir(xpath);
            } else if (!((ImdiTreeObject) targetNodeUserObject).isImdiChild()) {
                childTypes.add(new String[]{"Corpus Branch", imdiPathSeparator + "METATRANSCRIPT" + imdiPathSeparator + "Corpus"});
                childTypes.add(new String[]{"Session", imdiPathSeparator + "METATRANSCRIPT" + imdiPathSeparator + "Session"});
            }
//            System.out.println("childTypes: " + childTypes);
        } else {
            // corpus can be added to the root node
            childTypes.add(new String[]{"Unattached Corpus Branch", imdiPathSeparator + "METATRANSCRIPT" + imdiPathSeparator + "Corpus"});
        }
        return childTypes.elements();
    }

    /** 
     * This function is only a place holder and will be replaced 
     * @param childType is the chosen child type
     * @return enumeration of potential fields for this child type
     */
//    public Enumeration listFieldsFor(ImdiTreeObject targetImdiObject, String childType, int imdiChildIdentifier, String resourcePath) {
//        Vector childTypes = getSubnodesOf(/*getNodePath(targetImdiObject) + "/" +*/childType, false);
//        return childTypes.elements();
////        System.out.println("listFieldsFor: " + childType);
////        if (childType.contains("image")) {
////            childType = "MediaFile";
////        // temp method for testing until replaced
////        // TODO: implement this using data from the xsd on the server (server version needs to be updated)
////        }
////        Vector fieldTypes = new Vector();
////        String xmlPrePath = "";
////        if (childType.equals("Actor")) {
////            xmlPrePath = ".METATRANSCRIPT.Session.MDGroup.Actors.Actor(" + imdiChildIdentifier + ")"; // TODO resolve what method to use for imdichildren
////        //fieldTypes.add(xmlPrePath + ".Actor");
////        } else if (childType.equals("WrittenResource")) {
////            xmlPrePath = ".METATRANSCRIPT.Session.Resources.WrittenResource(" + imdiChildIdentifier + ")";
////            fieldTypes.add(new String[]{xmlPrePath + ".WrittenResource", "unset"});
////        } else if (childType.equals("Anonym")) {
////            xmlPrePath = ".METATRANSCRIPT.Session.Resources.Anonyms(" + imdiChildIdentifier + ")";
////            fieldTypes.add(new String[]{xmlPrePath + ".Anonyms", "unset"});
////        } else if (childType.equals("MediaFile")) {
////            xmlPrePath = ".METATRANSCRIPT.Session.Resources.MediaFile(" + imdiChildIdentifier + ")";
////            if (resourcePath == null) {
////                resourcePath = "null string";
////            } else {
////                Hashtable exifTags = getExifMetadata(resourcePath);
////                for (Enumeration exifRows = exifTags.keys(); exifRows.hasMoreElements();) {
////                    Object currentKey = exifRows.nextElement();
////                    fieldTypes.add(new String[]{xmlPrePath + ".exif" + currentKey.toString(), exifTags.get(currentKey).toString()});
////                }
////            }
////            fieldTypes.add(new String[]{xmlPrePath + ".Type", "unset"});
////            fieldTypes.add(new String[]{xmlPrePath + ".TimePosition.Start", "unset"});
////            fieldTypes.add(new String[]{xmlPrePath + ".TimePosition.End", "unset"});
////            fieldTypes.add(new String[]{xmlPrePath + ".Quality", "unset"});
////            fieldTypes.add(new String[]{xmlPrePath + ".AccessDate", "unset"});
////            fieldTypes.add(new String[]{xmlPrePath + ".ResourceLink", resourcePath});
////            fieldTypes.add(new String[]{xmlPrePath + ".Format", childType});
////        } else if (childType.equals("Corpus")) {
////            xmlPrePath = ".METATRANSCRIPT.Corpus";
////            fieldTypes.add(new String[]{xmlPrePath + ".Corpus", "unset"});
////        } else if (childType.equals("Session")) {
////            xmlPrePath = ".METATRANSCRIPT.Session";
////            fieldTypes.add(new String[]{xmlPrePath + ".Session", "unset"});
////        }
////        if (!childType.equals("MediaFile")) {
////            fieldTypes.add(new String[]{xmlPrePath + ".Name", "unset"});
////            fieldTypes.add(new String[]{xmlPrePath + ".Description", "unset"});
////            fieldTypes.add(new String[]{xmlPrePath + ".Title", "unset"});
////        }
////        System.out.println("childType: " + childType + " fieldTypes: " + fieldTypes);
////        return fieldTypes.elements();
//    }

// functions to extract the exif data from images
// this will probably need to be moved to a more appropriate class
    public Hashtable getExifMetadata(String resourcePath) {
        Hashtable exifTags = new Hashtable();
        System.out.println("tempGetExif: " + resourcePath);
        try {
            URL url = new URL(resourcePath);
            Iterator readers = ImageIO.getImageReadersBySuffix("jpeg");
            ImageReader reader = (ImageReader) readers.next();
            reader.setInput(ImageIO.createImageInputStream(url.openStream()));
            IIOMetadata metadata = reader.getImageMetadata(0);
            String[] names = metadata.getMetadataFormatNames();
            for (int i = 0; i < names.length; ++i) {
                System.out.println();
                System.out.println("METADATA FOR FORMAT: " + names[i]);
                decendExifTree(metadata.getAsTree(names[i]), null/*"." + names[i]*/, exifTags);
            }
        } catch (Exception ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
//            System.out.println("Exception: " + ex.getMessage());
        }
        System.out.println("end tempGetExif");
        return exifTags;
    }

    public void decendExifTree(Node node, String prefixString, Hashtable exifTags) {
        if (prefixString == null) {
            prefixString = ""; // skip the first node name    
        } else {
            prefixString = prefixString + imdiPathSeparator + node.getNodeName();
        }
        NamedNodeMap namedNodeMap = node.getAttributes();
        if (namedNodeMap != null) {
            for (int attributeCounter = 0; attributeCounter < namedNodeMap.getLength(); attributeCounter++) {
                String attributeName = namedNodeMap.item(attributeCounter).getNodeName();
                String attributeValue = namedNodeMap.item(attributeCounter).getNodeValue();
                exifTags.put(prefixString + imdiPathSeparator + attributeName, attributeValue);
            }
        }
        if (node.hasChildNodes()) {
            NodeList children = node.getChildNodes();
            for (int i = 0, ub = children.getLength(); i < ub; ++i) {
                decendExifTree(children.item(i), prefixString, exifTags);
            }
        }
    }
    // end functions to extract the exif data from images
    public boolean isImdiChildType(String childType) {
        if (childType == null) {
            return false;
        }
        return !childType.equals(imdiPathSeparator + "METATRANSCRIPT" + imdiPathSeparator + "Session") && !childType.equals(imdiPathSeparator + "METATRANSCRIPT" + imdiPathSeparator + "Corpus");
    }
//    public boolean nodesChildrenCanHaveSiblings(String xmlPath) {
//        System.out.println("xmlPath: " + xmlPath);
//        return (xmlPath.equals(".METATRANSCRIPT.Session.MDGroup.Actors"));
//    }
//
//    public String[] convertXmlPathToUiPath(String xmlPath) {
//        // TODO write this method
//        // why put in the (x) when it is not representative of the data???
//        return new String[]{"actors", "actor(1)", "name"};
//    }
    public String getHelpForField(String fieldName) {
        return "Usage description for: " + fieldName;
    }

    public String addFromTemplate(File destinationFile, String templateType) {
        System.out.println("addFromTemplate: " + templateType + " : " + destinationFile);
        String addedPathString = null;
        // copy the template to disk
        URL templateUrl = ImdiSchema.class.getResource("/mpi/linorg/resources/templates/" + templateType.substring(1) + ".xml");
//        GuiHelper.linorgWindowManager.openUrlWindow(templateType, templateUrl);
//        System.out.println("templateFile: " + templateFile);
        addedPathString = copyToDisk(templateUrl, destinationFile);
        return addedPathString;
    }

    private String copyToDisk(URL sourceURL, File targetFile) {
        try {
            InputStream in = sourceURL.openStream();
            OutputStream out = new FileOutputStream(targetFile);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            return targetFile.toURL().toString();
        } catch (Exception ex) {
            System.out.println("copyToDisk: " + ex);
            GuiHelper.linorgBugCatcher.logError(ex);
        }
        return null;
    }

    public String insertFromTemplate(File destinationFile, String elementName, Document targetImdiDom, String resourcePath, String mimeType) {
        System.out.println("insertFromTemplate: " + elementName + " : " + resourcePath);
        String addedPathString = null;
        if (elementName == null) {
            for (String[] formatType : new String[][]{
                        {"http://www.mpi.nl/IMDI/Schema/WrittenResource-Format.xml", ".METATRANSCRIPT.Session.Resources.WrittenResource"},
                        {"http://www.mpi.nl/IMDI/Schema/MediaFile-Format.xml", ".METATRANSCRIPT.Session.Resources.MediaFile"}
                    }) {
                if (ImdiField.imdiVocabularies.vocabularyContains(formatType[0], mimeType)) {
//                    if (mimeType.equals("image/jpeg")) {
                    elementName = formatType[1];
                    break;
                }
            }
        }
        try {
            URL templateUrl = ImdiSchema.class.getResource("/mpi/linorg/resources/templates/" + elementName.substring(1) + ".xml");
            // prepare the parent node
            String targetXpath = elementName.substring(0, elementName.lastIndexOf("."));
            // convert to xpath for the api
            targetXpath = targetXpath.replace(".", "/:");
            Document insertableSectionDoc = ImdiTreeObject.api.loadIMDIDocument(new OurURL(templateUrl), false);
            // insert values into the section that about to be added
            if (resourcePath != null) {
                String localFilePath = resourcePath; // will be changed when copied to the cache
                // copy the file to the imdi directory
                try {
                    //// TODO: the resource should be optionaly copied or moved into the cache or hardlinked
                    URL resourceUrl = new URL(resourcePath);
//                    String resourcesDirName = "resources";
                    File originalFile = new File(resourceUrl.getFile());
                    int suffixIndex = originalFile.getName().lastIndexOf(".");
                    String targetFilename = originalFile.getName().substring(0, suffixIndex);
                    String targetSuffix = originalFile.getName().substring(suffixIndex);
                    System.out.println("targetFilename: " + targetFilename + " targetSuffix: " + targetSuffix);
                    File destinationDirectory = new File(destinationFile.getParentFile().getPath()); // + File.separatorChar + resourcesDirName);
                    System.out.println("destinationDirectory: " + destinationDirectory.toString());
//                    destinationDirectory.mkdir();
                    File destinationFileCopy = File.createTempFile(targetFilename, targetSuffix, destinationDirectory);
                    localFilePath = "." + /*File.separatorChar + resourcesDirName +*/ File.separatorChar + destinationFileCopy.getName();
                    copyToDisk(resourceUrl, destinationFileCopy);
                    System.out.println("destinationFileCopy: " + destinationFileCopy.toString());
                } catch (Exception ex) {
                    //localFilePath = resourcePath; // link to the original file
                    GuiHelper.linorgBugCatcher.logError(ex);
                }

                // find the correct node and set the resourcePath value
                Node linkNode = org.apache.xpath.XPathAPI.selectSingleNode(insertableSectionDoc, "/:InsertableSection/:*/:ResourceLink");
                linkNode.setTextContent(localFilePath);
            }
            if (mimeType != null) {
                if (mimeType.equals("image/jpeg")) {
                    Hashtable exifTags = getExifMetadata(resourcePath);
                    String dateExifTag = "date";
                    if (exifTags.contains(dateExifTag)) {
                        Node linkNode = org.apache.xpath.XPathAPI.selectSingleNode(insertableSectionDoc, "/:InsertableSection/:MediaFile/:Date");
                        linkNode.setTextContent(exifTags.get(dateExifTag).toString());
                    }
                }
                Node linkNode = org.apache.xpath.XPathAPI.selectSingleNode(insertableSectionDoc, "/:InsertableSection/:*/:Format");
                linkNode.setTextContent(mimeType);
            }

            // import the new section to the target dom
            Node addableNode = targetImdiDom.importNode(org.apache.xpath.XPathAPI.selectSingleNode(insertableSectionDoc, "/:InsertableSection/:*"), true);

            Node insertBeforeNode = null;
            String insertBeforeCSL = insertableSectionDoc.getDocumentElement().getAttribute("InsertBefore");
            if (insertBeforeCSL != null && insertBeforeCSL.length() > 0) {
                String[] insertBeforeArray = insertableSectionDoc.getDocumentElement().getAttribute("InsertBefore").split(",");
                System.out.println("insertbefore: " + insertBeforeArray.toString());

                // find the node to add the new section before
                int insertBeforeCounter = 0;
                while (insertBeforeNode == null & insertBeforeCounter < insertBeforeArray.length) {
                    insertBeforeNode = org.apache.xpath.XPathAPI.selectSingleNode(targetImdiDom, targetXpath + "/:" + insertBeforeArray[insertBeforeCounter]);
                    insertBeforeCounter++;
                }
            }

            // find the node to add the new section to
            Node targetNode = org.apache.xpath.XPathAPI.selectSingleNode(targetImdiDom, targetXpath);
            if (insertBeforeNode != null) {
                System.out.println("inserting before: " + insertBeforeNode.getNodeName());
                targetNode.insertBefore(addableNode, insertBeforeNode);
            } else {
                System.out.println("inserting anywhere");
                targetNode.appendChild(addableNode);
            }
            addedPathString = destinationFile.toURL().toString() + "#" + elementName;
            String childsMetaNode = pathIsChildNode(elementName);
            addedPathString = addedPathString + "(" + (GuiHelper.imdiLoader.getImdiObject(childsMetaNode, addedPathString).getChildCount() + 1) + ")";
        } catch (Exception ex) {
            System.out.println("insertFromTemplate: " + ex.getMessage());
            GuiHelper.linorgBugCatcher.logError(ex);
        }
        return addedPathString;
    }

    public void iterateChildNodes(ImdiTreeObject parentNode, Vector childLinks, Node startNode, String nodePath) {
//        System.out.println("iterateChildNodes: " + nodePath);
        //loop all nodes
        // each end node becomes a field
        // any node that passes pathIsChildNode becomes a subnode in a node named by the result string of pathIsChildNode
        // the id of the node that passes pathIsChildNode is stored in the subnode to allow for deletion from the dom if needed

        // add the fields and nodes 
        for (Node childNode = startNode; childNode != null; childNode = childNode.getNextSibling()) {
            String localName = childNode.getLocalName();
            String siblingNodePath = nodePath + ImdiSchema.imdiPathSeparator + localName;
            //if (localName != null && GuiHelper.imdiSchema.nodesChildrenCanHaveSiblings(nodePath + "." + localName)) {

            ImdiTreeObject destinationNode;
            String childsMetaNode = pathIsChildNode(siblingNodePath);
//            System.out.println("pathIsChildNode: " + childsMetaNode + " : " + siblingNodePath);
            if (localName != null && childsMetaNode != null) {
                String siblingSpacer = "";
                String pathUrlXpathSeparator = "";
                if (!parentNode.getUrlString().contains("#")) {
                    pathUrlXpathSeparator = "#";
                }
                ImdiTreeObject metaNodeImdiTreeObject = GuiHelper.imdiLoader.getImdiObject(childsMetaNode, parentNode.getUrlString() + pathUrlXpathSeparator + siblingNodePath);
                // add brackets to conform with the imdi api notation
                siblingSpacer = "(" + (metaNodeImdiTreeObject.getChildCount() + 1) + ")";
                ImdiTreeObject subNodeImdiTreeObject = GuiHelper.imdiLoader.getImdiObject(childsMetaNode, parentNode.getUrlString() + pathUrlXpathSeparator + siblingNodePath + siblingSpacer);
                parentNode.attachChildNode(metaNodeImdiTreeObject);
                metaNodeImdiTreeObject.attachChildNode(subNodeImdiTreeObject);
                destinationNode = subNodeImdiTreeObject;
                siblingNodePath = "";
            } else {
                destinationNode = parentNode;
            }
//            System.out.println("destinationNode: " + destinationNode);
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
            ImdiField fieldToAdd = new ImdiField(destinationNode, siblingNodePath, fieldValue);

            // TODO: about to write this function
            //GuiHelper.imdiSchema.convertXmlPathToUiPath();

            // TODO: keep track of actual valid values here and only add to siblingCounter if siblings really exist
            // TODO: note that this method does not use any attributes without a node value
            NamedNodeMap namedNodeMap = childNode.getAttributes();
            if (namedNodeMap != null) {
                for (int attributeCounter = 0; attributeCounter < namedNodeMap.getLength(); attributeCounter++) {
                    String attributeName = namedNodeMap.item(attributeCounter).getNodeName();
                    String attributeValue = namedNodeMap.item(attributeCounter).getNodeValue();
//                    System.out.println("attributeName: " + attributeName);
//                    System.out.println("attributeValue: " + attributeValue);
                    if (attributeValue != null && attributeValue.length() > 0) {
                        // only add attributes if they contain a value
                        fieldToAdd.addAttribute(attributeName, attributeValue);
                    }
                }
            }
            if (shouldAddCurrent && fieldToAdd.isDisplayable()) {
//                System.out.println("Adding: " + fieldToAdd);
//                debugOut("nextChild: " + fieldToAdd.xmlPath + siblingSpacer + " : " + fieldToAdd.fieldValue);
//                fieldToAdd.translateFieldName(siblingNodePath);
                destinationNode.addField(fieldToAdd);
            } else if (shouldAddCurrent && fieldToAdd.xmlPath.contains("CorpusLink") && fieldValue.length() > 0) {
                String parentPath = destinationNode.getParentDirectory();
//                System.out.println("LinkValue: " + fieldValue);
//                System.out.println("ParentPath: " + parentPath);
//                System.out.println("Parent: " + this.getUrlString());
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
//                    System.out.println("linkPathCorrected: " + linkPath);
                    childLinks.add(new String[]{linkPath, fieldToAdd.fieldID});
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                    System.out.println("Exception CorpusLink: " + ex.getMessage());
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
            iterateChildNodes(destinationNode, childLinks, childNode.getFirstChild(), siblingNodePath);
        }
    }

    public String pathIsChildNode(String nodePath) {
        // TODO: change this to use a master list of types and populate it from the schema
        if (nodePath.contains(".METATRANSCRIPT.Session.MDGroup.Content.Languages.Language")) {
            return "Languages";
        }
        if (nodePath.contains(".Languages.Language")) {
            return "Languages";
        }
        if (nodePath.contains(".METATRANSCRIPT.Session.MDGroup.Actors.Actor")) {
            return "Actors";
        }
        if (nodePath.contains(".METATRANSCRIPT.Session.Resources.MediaFile")) {
            return "MediaFiles";
        }
        if (nodePath.contains(".METATRANSCRIPT.Session.Resources.WrittenResource")) {
            return "WrittenResources";
        }
        if (nodePath.contains(".METATRANSCRIPT.Session.Resources.Source")) {
            return "Sources";
        }
        return null;
    }
}
