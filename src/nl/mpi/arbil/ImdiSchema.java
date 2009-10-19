package nl.mpi.arbil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Vector;
import javax.imageio.*;
import javax.imageio.metadata.*;
import mpi.util.OurURL;
import org.w3c.dom.*;

/**
 * Document   : ImdiSchema
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
public class ImdiSchema {

    /**
     * http://www.mpi.nl/IMDI/Schema/IMDI_3.0.xsd
     */
    public File selectedTemplateDirectory = null;
    static String imdiPathSeparator = ".";
    public boolean copyNewResourcesToCache = true;

    public boolean nodeCanExistInNode(ImdiTreeObject targetImdiObject, ImdiTreeObject childImdiObject) {
        String targetImdiPath = getNodePath((ImdiTreeObject) targetImdiObject);
        String childPath = getNodePath((ImdiTreeObject) childImdiObject);
        targetImdiPath = targetImdiPath.replaceAll("\\(\\d*?\\)", "\\(x\\)");
        childPath = childPath.replaceAll("\\(\\d*?\\)", "\\(x\\)");
//        System.out.println("nodeCanExistInNode: " + targetImdiPath + " : " + childPath);
        int targetBranchCount = targetImdiPath.replaceAll("[^(]*", "").length();
        int childBranchCount = childPath.replaceAll("[^(]*", "").length();
//        System.out.println("targetBranchCount: " + targetBranchCount + " childBranchCount: " + childBranchCount);
        boolean hasCorrectSubNodeCount = childBranchCount - targetBranchCount < 2;
        return hasCorrectSubNodeCount && !childPath.equals(targetImdiPath) && childPath.startsWith(targetImdiPath);
    }

    public static String getNodePath(ImdiTreeObject targetImdiObject) {
        //TODO: this should probably be moved into the imditreeobject
        String xpath;
        if (targetImdiObject.isSession()) {
            xpath = imdiPathSeparator + "METATRANSCRIPT" + imdiPathSeparator + "Session";
        } else if (targetImdiObject.isCatalogue()) {
            xpath = imdiPathSeparator + "METATRANSCRIPT" + imdiPathSeparator + "Catalogue";
        } else {
            xpath = imdiPathSeparator + "METATRANSCRIPT" + imdiPathSeparator + "Corpus";
        }
        Object[] nodePathArray = ((ImdiTreeObject) targetImdiObject).getUrlString().split("#");
//        System.out.println("nodePath0: " + nodePathArray[0]);
        if (nodePathArray.length > 1) {
            String nodePath = nodePathArray[1].toString();
            xpath = nodePath;
//            System.out.println("nodePath1: " + nodePath);
        // convert the dot path to xpath
//            xpath = nodePath.replaceAll("(\\(.?\\))?\\.", ".");
//                xpath = nodePath.replaceAll("(\\(.?\\))?", "/");
//            System.out.println("xpath: " + xpath);
        }
        return xpath;
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
    public ImdiField[] getExifMetadata(ImdiTreeObject resourceImdi) {
        Vector<ImdiField> exifTagFields = new Vector();
        System.out.println("tempGetExif: " + resourceImdi.getFile());
        try {
            URL url = resourceImdi.getURL();
            if (resourceImdi.getFile().getName().contains(".")) {
                String fileSuffix = resourceImdi.getFile().getName().substring(resourceImdi.getFile().getName().lastIndexOf(".") + 1);
                System.out.println("tempGetExifSuffix: " + fileSuffix);
                Iterator readers = ImageIO.getImageReadersBySuffix(fileSuffix);
                if (readers.hasNext()) {
                    ImageReader reader = (ImageReader) readers.next();
                    reader.setInput(ImageIO.createImageInputStream(url.openStream()));
                    IIOMetadata metadata = reader.getImageMetadata(0);
                    if (metadata != null) {
                        String[] names = metadata.getMetadataFormatNames();
                        for (int i = 0; i < names.length; ++i) {
                            System.out.println();
                            System.out.println("METADATA FOR FORMAT: " + names[i]);
                            decendExifTree(resourceImdi, metadata.getAsTree(names[i]), null/*"." + names[i]*/, exifTagFields);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
//            System.out.println("Exception: " + ex.getMessage());
        }
        System.out.println("end tempGetExif");
        return exifTagFields.toArray(new ImdiField[]{});
    }

    public void decendExifTree(ImdiTreeObject resourceImdi, Node node, String prefixString, Vector<ImdiField> exifTagFields) {
        if (prefixString == null) {
            prefixString = "EXIF"; // skip the first node name    
        } else {
            prefixString = prefixString + imdiPathSeparator + node.getNodeName();
        }
        NamedNodeMap namedNodeMap = node.getAttributes();
        if (namedNodeMap != null) {
            for (int attributeCounter = 0; attributeCounter < namedNodeMap.getLength(); attributeCounter++) {
                String attributeName = namedNodeMap.item(attributeCounter).getNodeName();
                String attributeValue = namedNodeMap.item(attributeCounter).getNodeValue();
                exifTagFields.add(new ImdiField(resourceImdi, prefixString + imdiPathSeparator + attributeName, attributeValue));
            }
        }
        if (node.hasChildNodes()) {
            NodeList children = node.getChildNodes();
            for (int i = 0, ub = children.getLength(); i < ub; ++i) {
                decendExifTree(resourceImdi, children.item(i), prefixString, exifTagFields);
            }
        }
    }
    // end functions to extract the exif data from images

    public boolean isImdiChildType(String childType) {
        if (childType == null) {
            return false;
        }
        return !childType.equals(imdiPathSeparator + "METATRANSCRIPT" + imdiPathSeparator + "Session") && !childType.equals(imdiPathSeparator + "METATRANSCRIPT" + imdiPathSeparator + "Corpus") && !childType.equals(imdiPathSeparator + "METATRANSCRIPT" + imdiPathSeparator + "Catalogue");
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


    public URL addFromTemplate(File destinationFile, String templateType) {
        System.out.println("addFromJarTemplateFile: " + templateType + " : " + destinationFile);
        URL addedPathUrl = null;
        // copy the template to disk
        URL templateUrl = ImdiSchema.class.getResource("/nl/mpi/arbil/resources/templates/" + templateType.substring(1) + ".xml");
//        GuiHelper.linorgWindowManager.openUrlWindow(templateType, templateUrl);
//        System.out.println("templateFile: " + templateFile);
        addedPathUrl = copyToDisk(templateUrl, destinationFile);
        try {
            Document addedDocument = ImdiTreeObject.api.loadIMDIDocument(new OurURL(addedPathUrl), false);
            if (addedDocument == null) {
                GuiHelper.linorgBugCatcher.logError(new Exception(ImdiTreeObject.api.getMessage()));
                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Error inserting create date via the IMDI API", "Add from Template");
            } else {
                Node linkNode = org.apache.xpath.XPathAPI.selectSingleNode(addedDocument, "/:METATRANSCRIPT");
                NamedNodeMap metatranscriptAttributes = linkNode.getAttributes();
                LinorgVersion currentVersion = new LinorgVersion();
                String arbilVersionString = "Arbil." + currentVersion.currentMajor + "." + currentVersion.currentMinor + "." + currentVersion.currentRevision;
                metatranscriptAttributes.getNamedItem("Originator").setNodeValue(arbilVersionString);
                metatranscriptAttributes.getNamedItem("Date").setNodeValue(new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));
                ImdiTreeObject.api.writeDOM(addedDocument, new File(addedPathUrl.getFile()), false);
            }
        } catch (Exception ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
        }
        return addedPathUrl;
    }

    private URL copyToDisk(URL sourceURL, File targetFile) {
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
            out.flush();
            out.close();
            return targetFile.toURL();
        } catch (Exception ex) {
            System.out.println("copyToDisk: " + ex);
            GuiHelper.linorgBugCatcher.logError(ex);
        }
        return null;
    }

    public String getNodeTypeFromMimeType(String mimeType) {
        System.out.println("getNodeTypeFromMimeType: " + mimeType);
        for (String[] formatType : new String[][]{
                    {"http://www.mpi.nl/IMDI/Schema/WrittenResource-Format.xml", ".METATRANSCRIPT.Session.Resources.WrittenResource"},
                    {"http://www.mpi.nl/IMDI/Schema/MediaFile-Format.xml", ".METATRANSCRIPT.Session.Resources.MediaFile"}
                }) {
            if (ImdiVocabularies.getSingleInstance().vocabularyContains(formatType[0], mimeType)) {
                System.out.println("NodeType: " + formatType[1]);
//                    if (mimeType.equals("image/jpeg")) {
                return formatType[1];
            }
        }
        LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("There is no controlled vocabulary for either Written Resource or Media File that match \"" + mimeType + "\"", "Add Resource");
        return null;
    }

    public String insertFromTemplate(ArbilTemplate currentTemplate, File destinationFile, File resourceDirectory, String elementName, String targetXmlPath, Document targetImdiDom, String resourcePath, String mimeType) {
        System.out.println("insertFromTemplate: " + elementName + " : " + resourcePath);
        System.out.println("targetXpath: " + targetXmlPath);
        String addedPathString = null;
//        System.out.println("targetImdiDom: " + targetImdiDom.getTextContent());
        String targetXpath = targetXmlPath;
        String targetRef;
        String templateFileString = null;
        try {
            if (targetImdiDom == null) {
                throw (new Exception("targetImdiDom is null"));
            }
            templateFileString = elementName.substring(1);//TODO: this level of path change should not be done here but in the original caller
            System.out.println("templateFileString: " + templateFileString);
            templateFileString = templateFileString.replaceAll("\\(\\d*?\\)", "(x)");
            System.out.println("templateFileString(x): " + templateFileString);
            templateFileString = templateFileString.replaceAll("\\(x\\)$", "");
            URL templateUrl = ImdiSchema.class.getResource("/nl/mpi/arbil/resources/templates/" + templateFileString + ".xml");
            // prepare the parent node
            if (targetXpath == null) {
                targetXpath = elementName;
            } else {
                ///////////////////////////////
//                insertFromTemplate: .METATRANSCRIPT.Session.MDGroup.Actors.Actor(x).Languages.Language : null
//                targetXpath: .METATRANSCRIPT.Session.MDGroup.Actors.Actor(2).Languages.Language
//                templateFileString: METATRANSCRIPT.Session.MDGroup.Actors.Actor(x).Languages.Language
//                templateFileString(x): METATRANSCRIPT.Session.MDGroup.Actors.Actor(x).Languages.Language
//                targetXpath: /:METATRANSCRIPT/:Session/:MDGroup/:Actors/:Actor[2]/:Languages
                ///////////////////////////////
//                insertFromTemplate: .METATRANSCRIPT.Session.MDGroup.Actors.Actor(x).Languages.Language : null
//                targetXpath: .METATRANSCRIPT.Session.MDGroup.Actors.Actor(2)
//                templateFileString: METATRANSCRIPT.Session.MDGroup.Actors.Actor(x).Languages.Language
//                templateFileString(x): METATRANSCRIPT.Session.MDGroup.Actors.Actor(x).Languages.Language
//                targetXpath: /:METATRANSCRIPT/:Session/:MDGroup/:Actors/:Actor[2]/:Languages
                ///////////////////////////////
//                insertFromTemplate: .METATRANSCRIPT.Session.MDGroup.Actors.Actor : null
//                targetXpath: .METATRANSCRIPT.Session.MDGroup.Actors.Actor
//                templateFileString: METATRANSCRIPT.Session.MDGroup.Actors.Actor
//                templateFileString(x): METATRANSCRIPT.Session.MDGroup.Actors.Actor
//                targetXpath: /:METATRANSCRIPT/:Session/:MDGroup/:Actors
                ///////////////////////////////
//                insertFromTemplate: .METATRANSCRIPT.Session.MDGroup.Actors.Actor : null
//                targetXpath: null
//                templateFileString: METATRANSCRIPT.Session.MDGroup.Actors.Actor
//                templateFileString(x): METATRANSCRIPT.Session.MDGroup.Actors.Actor
//                targetXpath: /:METATRANSCRIPT/:Session/:MDGroup/:Actors
                ///////////////////////////////
//                insertFromTemplate: .METATRANSCRIPT.Session.Resources.MediaFile : null
//                targetXpath: .METATRANSCRIPT.Session.Resources.MediaFile(1)
//                templateFileString: METATRANSCRIPT.Session.Resources.MediaFile
//                templateFileString(x): METATRANSCRIPT.Session.Resources.MediaFile
                ///////////////////////////////
                // make sure we have a complete path
                // for instance METATRANSCRIPT.Session.MDGroup.Actors.Actor(x).Languages.Language
                // requires /:METATRANSCRIPT/:Session/:MDGroup/:Actors/:Actor[6].Languages
                // not /:METATRANSCRIPT/:Session/:MDGroup/:Actors/:Actor[6]
                // the last path component (.Language) will be removed later
                String[] targetXpathArray = targetXpath.split("\\)");
                String[] elementNameArray = elementName.split("\\)");
                targetXpath = "";
                for (int partCounter = 0; partCounter < elementNameArray.length; partCounter++) {
                    if (targetXpathArray.length > partCounter) {
                        targetXpath = targetXpath + targetXpathArray[partCounter] + ")";
                    } else {
                        targetXpath = targetXpath + elementNameArray[partCounter] + ")";
                    }
                }
                targetXpath = targetXpath.replaceAll("\\)$", "");
            }
            targetRef = targetXpath.replaceAll("\\(\\d*?$", ""); // clean up the end of the string
//            if (!targetXpath.endsWith(")")) {
            targetXpath = targetXpath.substring(0, targetXpath.lastIndexOf("."));
//            }
            // convert to xpath for the api
            targetXpath = targetXpath.replace(".", "/:");
//            targetXpath = targetXpath.replace(")", "]");
//            targetXpath = targetXpath.replace("(", "[position()=");
            targetXpath = targetXpath.replace(")", "]");
            targetXpath = targetXpath.replace("(", "[");
            System.out.println("targetXpath: " + targetXpath);
            System.out.println("templateUrl: " + templateUrl);
            if (templateUrl == null) {
                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("No template found for: " + elementName.substring(1), "Load Template");
                throw (new Exception("No template found for: " + elementName.substring(1)));
            }
            Document insertableSectionDoc = ImdiTreeObject.api.loadIMDIDocument(new OurURL(templateUrl), false);
            if (insertableSectionDoc == null) {
                GuiHelper.linorgBugCatcher.logError(new Exception(ImdiTreeObject.api.getMessage()));
                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Error reading via the IMDI API", "Insert from Template");
            } else {
                // insert values into the section that about to be added
                if (resourcePath != null) {
                    String localFilePath = resourcePath; // will be changed when copied to the cache
                    // copy the file to the imdi directory
                    try {
                        //// TODO: the resource should be optionaly copied or moved into the cache or hardlinked
                        if (copyNewResourcesToCache) {
                            URL resourceUrl = new URL(resourcePath);
//                    String resourcesDirName = "resources";
                            File originalFile = new File(resourceUrl.getFile());
                            int suffixIndex = originalFile.getName().lastIndexOf(".");
                            String targetFilename = originalFile.getName().substring(0, suffixIndex);
                            String targetSuffix = originalFile.getName().substring(suffixIndex);
                            System.out.println("targetFilename: " + targetFilename + " targetSuffix: " + targetSuffix);
                            ///////////////////////////////////////////////////////////////////////
                            // use the nodes child directory
                            File destinationFileCopy = File.createTempFile(targetFilename, targetSuffix, resourceDirectory);
                            localFilePath = destinationFileCopy.getAbsolutePath().replace(destinationFile.getParentFile().getPath(), "./").replace("\\", "/").replace("//", "/");
                            // for easy reading in the fields keep the file in the same directory
//                        File destinationDirectory = new File(destinationFile.getParentFile().getPath());
//                        File destinationFileCopy = File.createTempFile(targetFilename, targetSuffix, destinationDirectory);
//                        localFilePath = "./" + destinationFileCopy.getName();
                            ///////////////////////////////////////////////////////////////////////
                            copyToDisk(resourceUrl, destinationFileCopy);
                            System.out.println("destinationFileCopy: " + destinationFileCopy.toString());
                        }
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

                Node insertableNode = org.apache.xpath.XPathAPI.selectSingleNode(insertableSectionDoc, "/:InsertableSection/:*");
                if (insertableNode == null) {
                    throw (new Exception("InsertableSection not found in the template"));
                }
                // import the new section to the target dom
                Node addableNode = targetImdiDom.importNode(insertableNode, true);

                Node insertBeforeNode = null;
                String insertBeforeCSL = insertableSectionDoc.getDocumentElement().getAttribute("InsertBefore");
                if (insertBeforeCSL != null && insertBeforeCSL.length() > 0) {
                    String[] insertBeforeArray = insertableSectionDoc.getDocumentElement().getAttribute("InsertBefore").split(",");
                    // find the node to add the new section before
                    int insertBeforeCounter = 0;
                    while (insertBeforeNode == null & insertBeforeCounter < insertBeforeArray.length) {
                        System.out.println("insertbefore: " + insertBeforeArray);
                        insertBeforeNode = org.apache.xpath.XPathAPI.selectSingleNode(targetImdiDom, targetXpath + "/:" + insertBeforeArray[insertBeforeCounter]);
                        insertBeforeCounter++;
                    }
                }

                // find the node to add the new section to
                Node targetNode = org.apache.xpath.XPathAPI.selectSingleNode(targetImdiDom, targetXpath);
                Node addedNode;
                if (insertBeforeNode != null) {
                    System.out.println("inserting before: " + insertBeforeNode.getNodeName());
                    addedNode = targetNode.insertBefore(addableNode, insertBeforeNode);
                } else {
                    System.out.println("inserting");
                    addedNode = targetNode.appendChild(addableNode);
                }
                addedPathString = destinationFile.toURL().toString() + "#" + targetRef;
                String childsMetaNode = currentTemplate.pathIsChildNode(elementName.replaceAll("\\(\\d*?\\)", ""));
                if (childsMetaNode != null) {
                    Node currentNode = addedNode.getParentNode().getFirstChild();
                    int siblingCount = 0;
                    while (currentNode != null) {
                        System.out.println("currentNode: " + currentNode.getLocalName());
                        if (addedNode.getLocalName().equals(currentNode.getLocalName())) {
                            siblingCount++;
                        }
                        currentNode = currentNode.getNextSibling();
                    }
                    addedPathString = addedPathString + "(" + siblingCount + ")";
                } else {
                    // make sure elements like description show the parent node rather than trying to get a non existing node
                    addedPathString = destinationFile.toURL().toString();
                }
            }
        } catch (Exception ex) {
            System.out.println("insertFromTemplate: " + ex.getMessage());
            System.out.println("exception with targetXpath: " + targetXpath);
//            System.out.println("templateUrl: " + templateUrl);
            GuiHelper.linorgBugCatcher.logError("exception with targetXpath: " + targetXpath + "\ntemplateFileString: " + templateFileString, ex);
        }
        System.out.println("addedPathString: " + addedPathString);
        return addedPathString;
    }

    public String correctLinkPath(String parentPath, String linkString) {
        String linkPath;
        if (!linkString.toLowerCase().startsWith("http:") && !linkString.toLowerCase().startsWith("file:")) {
//                    linkPath = parentPath /*+ File.separatorChar*/ + fieldToAdd.getFieldValue();
            linkPath = parentPath + linkString;
        } else if (linkString.toLowerCase().startsWith("&root;")) {
            linkPath = parentPath + linkString.substring(6);
        } else {
            linkPath = linkString;
        }
//                    System.out.println("linkPath: " + linkPath);
//                    linkPath = new URL(linkPath).getPath();
        // clean the path for the local file system
        linkPath = linkPath.replaceAll("/\\./", "/");
        linkPath = linkPath.substring(0, 6) + (linkPath.substring(6).replaceAll("[/]+/", "/"));
        while (linkPath.contains("/../")) {
//                        System.out.println("linkPath: " + linkPath);
            linkPath = linkPath.replaceFirst("/[^/]+/\\.\\./", "/");
        }
//                    System.out.println("linkPathCorrected: " + linkPath);
        return linkPath;
    }

    public void iterateChildNodes(ImdiTreeObject parentNode, Vector<String[]> childLinks, Node startNode, String nodePath) {
//        System.out.println("iterateChildNodes: " + nodePath);
        //loop all nodes
        // each end node becomes a field
        // any node that passes pathIsChildNode becomes a subnode in a node named by the result string of pathIsChildNode
        // the id of the node that passes pathIsChildNode is stored in the subnode to allow for deletion from the dom if needed

        // add the fields and nodes 
        for (Node childNode = startNode; childNode != null; childNode = childNode.getNextSibling()) {
            String localName = childNode.getLocalName();
            // get the xml node id
            String xmlNodeId = null;
            NamedNodeMap attributesMap = childNode.getAttributes();
            if (attributesMap != null) {
                Node xmlNodeIdAtt = attributesMap.getNamedItem("id");
                if (xmlNodeIdAtt != null) {
                    xmlNodeId = xmlNodeIdAtt.getNodeValue();
                }// end get the xml node id
                Node catalogueLinkAtt = attributesMap.getNamedItem("CatalogueLink");
                if (catalogueLinkAtt != null) {
                    String catalogueLink = catalogueLinkAtt.getNodeValue();
                    if (catalogueLink.length() > 0) {
                        childLinks.add(new String[]{correctLinkPath(parentNode.getParentDirectory(), catalogueLink), "CatalogueLink"});
                    }
                }
                Node archiveHandleAtt = attributesMap.getNamedItem("ArchiveHandle");
                if (archiveHandleAtt != null) {
                    parentNode.hasArchiveHandle = true;
                }
                Node templateOriginatorAtt = attributesMap.getNamedItem("Originator");
                if (templateOriginatorAtt != null) {
                    String templateOriginator = templateOriginatorAtt.getNodeValue();
                    parentNode.currentTemplate = ArbilTemplateManager.getSingleInstance().getTemplate(templateOriginator);
                }
            }
            String siblingNodePath = nodePath + ImdiSchema.imdiPathSeparator + localName;
            //if (localName != null && GuiHelper.imdiSchema.nodesChildrenCanHaveSiblings(nodePath + "." + localName)) {

            ImdiTreeObject destinationNode;
            String childsMetaNode = parentNode.currentTemplate.pathIsChildNode(siblingNodePath);
//            System.out.println("pathIsChildNode: " + childsMetaNode + " : " + siblingNodePath);
            if (localName != null && childsMetaNode != null) {
                String siblingSpacer = "";
                String pathUrlXpathSeparator = "";
                if (!parentNode.getUrlString().contains("#")) {
                    pathUrlXpathSeparator = "#";
                }
                ImdiTreeObject metaNodeImdiTreeObject = GuiHelper.imdiLoader.getImdiObject(null, parentNode.getUrlString() + pathUrlXpathSeparator + siblingNodePath);
                metaNodeImdiTreeObject.setNodeText(childsMetaNode);
                // add brackets to conform with the imdi api notation
                siblingSpacer = "(" + (metaNodeImdiTreeObject.getChildCount() + 1) + ")";
                ImdiTreeObject subNodeImdiTreeObject = GuiHelper.imdiLoader.getImdiObject(null, parentNode.getUrlString() + pathUrlXpathSeparator + siblingNodePath + siblingSpacer);
                subNodeImdiTreeObject.xmlNodeId = xmlNodeId;
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
                    if (attributeValue != null /*&& attributeValue.length() > 0*/) {
                        // always add attrubutes even if without a value providing they are not null
                        fieldToAdd.addAttribute(attributeName, attributeValue);
                    }
                    if (fieldToAdd.xmlPath.endsWith("Description")) {
                        if (attributeName.equals("Link") && attributeValue.length() > 0) {
                            childLinks.add(new String[]{correctLinkPath(parentNode.getParentDirectory(), attributeValue), fieldToAdd.fieldID});
                        }
                    }
                }
            }
            if (shouldAddCurrent && fieldToAdd.isDisplayable()) {
//                System.out.println("Adding: " + fieldToAdd);
//                debugOut("nextChild: " + fieldToAdd.xmlPath + siblingSpacer + " : " + fieldToAdd.fieldValue);
//                fieldToAdd.translateFieldName(siblingNodePath);
                destinationNode.addField(fieldToAdd);
            } else if (shouldAddCurrent && fieldToAdd.xmlPath.contains("CorpusLink") && fieldValue.length() > 0) {
//                System.out.println("LinkValue: " + fieldValue);
//                System.out.println("ParentPath: " + parentPath);
//                System.out.println("Parent: " + this.getUrlString());
                try {
                    String linkPath = correctLinkPath(parentNode.getParentDirectory(), fieldToAdd.getFieldValue());
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
}
