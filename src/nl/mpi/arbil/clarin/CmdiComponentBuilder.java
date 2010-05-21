package nl.mpi.arbil.clarin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import nl.mpi.arbil.GuiHelper;
import nl.mpi.arbil.LinorgJournal;
import nl.mpi.arbil.LinorgSessionStorage;
import nl.mpi.arbil.LinorgWindowManager;
import nl.mpi.arbil.data.ImdiTreeObject;
import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Document   : CmdiComponentBuilder
 * Created on : Mar 18, 2010, 1:40:35 PM
 * @author Peter.Withers@mpi.nl
 */
public class CmdiComponentBuilder {

    private Document getDocument(File inputFile) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setValidating(false);
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document;
        if (inputFile == null) {
            document = documentBuilder.newDocument();
        } else {
            document = documentBuilder.parse(inputFile);
        }
        return document;
    }

//    private Document createDocument(File inputFile) throws ParserConfigurationException, SAXException, IOException {
//        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
//        documentBuilderFactory.setValidating(false);
//        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
//        Document document = documentBuilder.newDocument();
//        return document;
//    }
    private void savePrettyFormatting(Document document, File outputFile) {
        try {
            // set up input and output
            DOMSource dOMSource = new DOMSource(document);
            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
            StreamResult xmlOutput = new StreamResult(fileOutputStream);
            // configure transformer
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            //transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "testing.dtd");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(dOMSource, xmlOutput);
            xmlOutput.getOutputStream().close();
            //System.out.println(xmlOutput.getWriter().toString());
        } catch (IllegalArgumentException illegalArgumentException) {
            GuiHelper.linorgBugCatcher.logError(illegalArgumentException);
        } catch (TransformerException transformerException) {
            GuiHelper.linorgBugCatcher.logError(transformerException);
        } catch (TransformerFactoryConfigurationError transformerFactoryConfigurationError) {
            System.out.println(transformerFactoryConfigurationError.getMessage());
        } catch (FileNotFoundException notFoundException) {
            GuiHelper.linorgBugCatcher.logError(notFoundException);
        } catch (IOException iOException) {
            GuiHelper.linorgBugCatcher.logError(iOException);
        }
    }

    public URI insertResourceProxy(ImdiTreeObject imdiTreeObject, ImdiTreeObject resourceNode) {
        synchronized (imdiTreeObject.domLockObject) {
//    <.CMD.Resources.ResourceProxyList.ResourceProxy>
//        <ResourceProxyList>
//            <ResourceProxy id="a_text">
//                <ResourceType>Resource</ResourceType>
//                <ResourceRef>bla.txt</ResourceRef>
//            </ResourceProxy>
            String targetXmlPath = imdiTreeObject.getURI().getFragment();
            System.out.println("insertResourceProxy: " + targetXmlPath);
            File cmdiNodeFile = imdiTreeObject.getFile();
            String nodeFragment = "";

            // geerate a uuid for new resource
            String resourceProxyId = UUID.randomUUID().toString();
            try {
                // load the schema
                SchemaType schemaType = getFirstSchemaType(imdiTreeObject.getNodeTemplate().templateFile);
                // load the dom
                Document targetDocument = getDocument(cmdiNodeFile);
                // insert the new section
                try {
                    try {
//                    if (targetXmlPath == null) {
//                        targetXmlPath = ".CMD.Components";
//                    }
                        Node documentNode = selectSingleNode(targetDocument, targetXmlPath);
                        documentNode.getAttributes().getNamedItem("ref").setNodeValue(resourceProxyId);
                    } catch (TransformerException exception) {
                        GuiHelper.linorgBugCatcher.logError(exception);
                        return null;
                    }
//                printoutDocument(targetDocument);
                    Node addedResourceNode = insertSectionToXpath(targetDocument, targetDocument.getFirstChild(), schemaType, ".CMD.Resources.ResourceProxyList", ".CMD.Resources.ResourceProxyList.ResourceProxy");
                    addedResourceNode.getAttributes().getNamedItem("id").setNodeValue(resourceProxyId);
                    for (Node childNode = addedResourceNode.getFirstChild(); childNode != null; childNode = childNode.getNextSibling()) {
                        String localName = childNode.getLocalName();
                        if ("ResourceType".equals(localName)) {
                            childNode.setTextContent(resourceNode.mpiMimeType);
                        }
                        if ("ResourceRef".equals(localName)) {
                            childNode.setTextContent(resourceNode.getUrlString());
                        }
                    }
                } catch (Exception exception) {
                    GuiHelper.linorgBugCatcher.logError(exception);
                    return null;
                }
                // bump the history
                imdiTreeObject.bumpHistory();
                // save the dom
                savePrettyFormatting(targetDocument, cmdiNodeFile); // note that we want to make sure that this gets saved even without changes because we have bumped the history ant there will be no file otherwise
            } catch (IOException exception) {
                GuiHelper.linorgBugCatcher.logError(exception);
                return null;
            } catch (ParserConfigurationException exception) {
                GuiHelper.linorgBugCatcher.logError(exception);
                return null;
            } catch (SAXException exception) {
                GuiHelper.linorgBugCatcher.logError(exception);
                return null;
            }
            return imdiTreeObject.getURI();
        }
    }

    public boolean removeChildNodes(ImdiTreeObject imdiTreeObject, String nodePaths[]) {
        synchronized (imdiTreeObject.domLockObject) {
            System.out.println("removeChildNodes: " + imdiTreeObject);
            File cmdiNodeFile = imdiTreeObject.getFile();
            try {
                Document targetDocument = getDocument(cmdiNodeFile);
                // collect up all the nodes to be deleted without changing the xpath
                ArrayList<Node> selectedNodes = new ArrayList<Node>();
                for (String currentNodePath : nodePaths) {
                    System.out.println("removeChildNodes: " + currentNodePath);
                    // todo: search for and remove any reource links referenced by this node or its sub nodes
                    Node documentNode = selectSingleNode(targetDocument, currentNodePath);
                    selectedNodes.add(documentNode);

                }
                // delete all the nodes now that the xpath is no longer relevant
                for (Node currentNode : selectedNodes) {
                    currentNode.getParentNode().removeChild(currentNode);
                }
                // bump the history
                imdiTreeObject.bumpHistory();
                // save the dom
                savePrettyFormatting(targetDocument, cmdiNodeFile);
                for (String currentNodePath : nodePaths) {
                    // todo log to jornal file
                }
                return true;
            } catch (ParserConfigurationException exception) {
                GuiHelper.linorgBugCatcher.logError(exception);
            } catch (SAXException exception) {
                GuiHelper.linorgBugCatcher.logError(exception);
            } catch (IOException exception) {
                GuiHelper.linorgBugCatcher.logError(exception);
            } catch (TransformerException exception) {
                GuiHelper.linorgBugCatcher.logError(exception);
            }
            return false;
        }
    }

    public boolean setFieldValues(ImdiTreeObject imdiTreeObject, FieldUpdateRequest[] fieldUpdates) {
        synchronized (imdiTreeObject.domLockObject) {
            System.out.println("setFieldValues: " + imdiTreeObject);
            File cmdiNodeFile = imdiTreeObject.getFile();
            try {
                Document targetDocument = getDocument(cmdiNodeFile);
                for (FieldUpdateRequest currentFieldUpdate : fieldUpdates) {
                    System.out.println("currentFieldUpdate: " + currentFieldUpdate.fieldPath);
                    // todo: search for and remove any reource links referenced by this node or its sub nodes
                    Node documentNode = selectSingleNode(targetDocument, currentFieldUpdate.fieldPath);
                    NamedNodeMap attributesMap = documentNode.getAttributes();
                    if (currentFieldUpdate.fieldOldValue.equals(documentNode.getTextContent())) {
                        documentNode.setTextContent(currentFieldUpdate.fieldNewValue);
                    } else {
                        GuiHelper.linorgBugCatcher.logError(new Exception("expecting \'" + currentFieldUpdate.fieldOldValue + "\' not \'" + documentNode.getTextContent() + "\' in " + currentFieldUpdate.fieldPath));
                        return false;
                    }
                    Node keyNameNode = attributesMap.getNamedItem("Name");
                    if (keyNameNode != null && currentFieldUpdate.keyNameValue != null) {
                        keyNameNode.setNodeValue(currentFieldUpdate.keyNameValue);
                    }
                    Node languageNode = attributesMap.getNamedItem("LanguageId");
                    if (languageNode != null && currentFieldUpdate.fieldLanguageId != null) {
                        languageNode.setNodeValue(currentFieldUpdate.fieldLanguageId);
                    }
                }
                // bump the history
                imdiTreeObject.bumpHistory();
                // save the dom
                savePrettyFormatting(targetDocument, cmdiNodeFile);
                for (FieldUpdateRequest currentFieldUpdate : fieldUpdates) {
                    // log to jornal file
                    LinorgJournal.getSingleInstance().saveJournalEntry(imdiTreeObject.getUrlString(), currentFieldUpdate.fieldPath, currentFieldUpdate.fieldOldValue, currentFieldUpdate.fieldNewValue, "save");
                    if (currentFieldUpdate.fieldLanguageId != null) {
                        LinorgJournal.getSingleInstance().saveJournalEntry(imdiTreeObject.getUrlString(), currentFieldUpdate.fieldPath + ":LanguageId", currentFieldUpdate.fieldLanguageId, "", "save");
                    }
                    if (currentFieldUpdate.keyNameValue != null) {
                        LinorgJournal.getSingleInstance().saveJournalEntry(imdiTreeObject.getUrlString(), currentFieldUpdate.fieldPath + ":Name", currentFieldUpdate.keyNameValue, "", "save");
                    }
                }
                return true;
            } catch (ParserConfigurationException exception) {
                GuiHelper.linorgBugCatcher.logError(exception);
            } catch (SAXException exception) {
                GuiHelper.linorgBugCatcher.logError(exception);
            } catch (IOException exception) {
                GuiHelper.linorgBugCatcher.logError(exception);
            } catch (TransformerException exception) {
                GuiHelper.linorgBugCatcher.logError(exception);
            }
            return false;
        }
    }

    public URI insertChildComponent(ImdiTreeObject imdiTreeObject, String targetXmlPath, String cmdiComponentId) {
        synchronized (imdiTreeObject.domLockObject) {
            System.out.println("insertChildComponent: " + cmdiComponentId);
            System.out.println("targetXmlPath: " + targetXmlPath);
            // check for issues with the path
            if (targetXmlPath == null) {
                targetXmlPath = cmdiComponentId.replaceAll("\\.[^.]+$", "");
            } else if (targetXmlPath.replaceAll("\\(\\d+\\)", "").length() == cmdiComponentId.length()) {
                // trim the last path component if the destination equals the new node path
                // i.e. xsdPath: .CMD.Components.Session.Resources.MediaFile.Keys.Key into .CMD.Components.Session.Resources.MediaFile(1).Keys.Key
                targetXmlPath = targetXmlPath.replaceAll("\\.[^.]+$", "");
            }
            // make sure the target xpath has all the required parts
            String[] cmdiComponentArray = cmdiComponentId.split("\\.");
            String[] targetXmlPathArray = targetXmlPath.replaceAll("\\(\\d+\\)", "").split("\\.");
            for (int pathPartCounter = targetXmlPathArray.length; pathPartCounter < cmdiComponentArray.length - 1; pathPartCounter++) {
                System.out.println("adding missing path component: " + cmdiComponentArray[pathPartCounter]);
                targetXmlPath = targetXmlPath + "." + cmdiComponentArray[pathPartCounter];
            }
            // end path corrections

            System.out.println("trimmed targetXmlPath: " + targetXmlPath);
            //String targetXpath = targetNode.getURI().getFragment();
            //System.out.println("targetXpath: " + targetXpath);
            File cmdiNodeFile = imdiTreeObject.getFile();
            String nodeFragment = "";
            try {
                // load the schema
                SchemaType schemaType = getFirstSchemaType(imdiTreeObject.getNodeTemplate().templateFile);
                // load the dom
                Document targetDocument = getDocument(cmdiNodeFile);
                // insert the new section
                try {
//                printoutDocument(targetDocument);
                    Node AddedNode = insertSectionToXpath(targetDocument, targetDocument.getFirstChild(), schemaType, targetXmlPath, cmdiComponentId);
                    nodeFragment = convertNodeToNodePath(targetDocument, AddedNode, targetXmlPath);
                } catch (Exception exception) {
                    GuiHelper.linorgBugCatcher.logError(exception);
                    return null;
                }
                // bump the history
                imdiTreeObject.bumpHistory();
                // save the dom
                savePrettyFormatting(targetDocument, cmdiNodeFile); // note that we want to make sure that this gets saved even without changes because we have bumped the history ant there will be no file otherwise
            } catch (IOException exception) {
                GuiHelper.linorgBugCatcher.logError(exception);
                return null;
            } catch (ParserConfigurationException exception) {
                GuiHelper.linorgBugCatcher.logError(exception);
                return null;
            } catch (SAXException exception) {
                GuiHelper.linorgBugCatcher.logError(exception);
                return null;
            }
//       diff_match_patch diffTool= new diff_match_patch();
//       diffTool.diff_main(targetXpath, targetXpath);
            try {
                System.out.println("nodeFragment: " + nodeFragment);
                // return the child node url and path in the xml
                // first strip off any fragment then add the full node fragment
                return new URI(imdiTreeObject.getURI().toString().split("#")[0] + "#" + nodeFragment);
            } catch (URISyntaxException exception) {
                GuiHelper.linorgBugCatcher.logError(exception);
                return null;
            }
        }
    }

    private Node selectSingleNode(Document targetDocument, String targetXpath) throws TransformerException {
        // convert the syntax inherited from the imdi api into xpath
        String tempXpath = targetXpath.replaceAll("\\.", "/:");
        tempXpath = tempXpath.replaceAll("\\(", "[");
        tempXpath = tempXpath.replaceAll("\\)", "]");
//            tempXpath = "/CMD/Components/Session/MDGroup/Actors";
        System.out.println("tempXpath: " + tempXpath);
        // find the target node of the xml
        return org.apache.xpath.XPathAPI.selectSingleNode(targetDocument, tempXpath);
    }

    private Node insertSectionToXpath(Document targetDocument, Node documentNode, SchemaType schemaType, String targetXpath, String xsdPath) throws Exception {
        System.out.println("insertSectionToXpath");
        System.out.println("xsdPath: " + xsdPath);
        System.out.println("targetXpath: " + targetXpath);
        SchemaProperty foundProperty = null;
        String strippedXpath = null;
        if (targetXpath == null) {
            documentNode = documentNode.getParentNode();
        } else {
            try {
                documentNode = selectSingleNode(targetDocument, targetXpath);
            } catch (TransformerException exception) {
                GuiHelper.linorgBugCatcher.logError(exception);
                return null;
            }
            strippedXpath = targetXpath.replaceAll("\\(\\d+\\)", "");
        }
        // at this point we have the xml node that the user acted on but next must get any additional nodes with the next section        
        System.out.println("strippedXpath: " + strippedXpath);
        for (String currentPathComponent : xsdPath.split("\\.")) {
            if (currentPathComponent.length() > 0) {
                System.out.println("currentPathComponent: " + currentPathComponent);
                //System.out.println("documentNode: " + documentNode.getChildNodes());
                // get the starting point in the schema
                foundProperty = null;
                for (SchemaProperty schemaProperty : schemaType.getProperties()) {
                    String currentName = schemaProperty.getName().getLocalPart();
                    if (currentPathComponent.equals(currentName)) {
                        foundProperty = schemaProperty;
                        break;
                    }
                }
                if (foundProperty == null) {
                    throw new Exception("failed to find the path in the schema: " + currentPathComponent);
                } else {
                    schemaType = foundProperty.getType();
                    System.out.println("foundProperty: " + foundProperty.getName().getLocalPart());
                }
                // get the starting node in the xml document
                // TODO: this will not navigate to the nth child node when the xpath is provided
                //if (foundNode != null) {
                //    // keep the last node found in the chain

//                if (strippedXpath != null && strippedXpath.startsWith("." + currentPathComponent)) {
//                    strippedXpath = strippedXpath.substring(currentPathComponent.length() + 1);
//                    System.out.println("strippedXpath: " + strippedXpath);
//                } else {
//                    Node childNode = documentNode.getFirstChild();
//                    while (childNode != null) {
//                        System.out.println("childNode: " + childNode.getNodeName());
//                        if (currentPathComponent.equals(childNode.getNodeName())) {
//                            System.out.println("found existing: " + currentPathComponent);
//                            documentNode = childNode;
//                            break;
//                        } else {
//                            childNode = childNode.getNextSibling();
//                        }
//                    }
//                    if (documentNode != childNode) {
//                        System.out.println("Adding destination node: " + currentPathComponent);
//                        System.out.println("Into: " + documentNode.getNodeName());
//                        documentNode = (Node) appendNode(targetDocument, null, documentNode, foundProperty);
////                        throw new Exception("failed to find the node path in the document: " + currentPathComponent);
//                    }
//                }
            }
        }
//        System.out.println("Adding marker node");
//        Element currentElement = targetDocument.createElement("MarkerNode");
//        currentElement.setTextContent(xsdPath);
//        documentNode.appendChild(currentElement);
        System.out.println("Adding destination sub nodes node to: " + documentNode.getLocalName());
        return constructXml(foundProperty, xsdPath, targetDocument, null, documentNode);
    }

    private String convertNodeToNodePath(Document targetDocument, Node documentNode, String targetXmlPath) {
        System.out.println("Calculating the added fragment");
        // count siblings to get the correct child index for the fragment
        int siblingCouter = 1;
        Node siblingNode = documentNode.getPreviousSibling();
        while (siblingNode != null) {
            if (documentNode.getLocalName().equals(siblingNode.getLocalName())) {
                siblingCouter++;
            }
            siblingNode = siblingNode.getPreviousSibling();
        }
        // get the current node name
        String nodeFragment = documentNode.getNodeName();
        String nodePathString = targetXmlPath + "." + nodeFragment + "(" + siblingCouter + ")";

//        String nodePathString = "";
//        Node parentNode = documentNode;
//        while (parentNode != null) {
//            // count siblings to get the correct child index for the fragment
//            int siblingCouter = 1;
//            Node siblingNode = parentNode.getPreviousSibling();
//            while (siblingNode != null) {
//                if (parentNode.getLocalName().equals(siblingNode.getLocalName())) {
//                    siblingCouter++;
//                }
//                siblingNode = siblingNode.getPreviousSibling();
//            }
//            // get the current node name
//            String nodeFragment = parentNode.getNodeName();
//
//            nodePathString = "." + nodeFragment + "(" + siblingCouter + ")" + nodePathString;
//            //System.out.println("nodePathString: " + nodePathString);
//            if (parentNode.isSameNode(targetDocument.getDocumentElement())) {
//                break;
//            }
//            parentNode = parentNode.getParentNode();
//        }
//        nodeFragment = "." + nodeFragment;
        System.out.println("nodeFragment: " + nodePathString);
        System.out.println("targetXmlPath: " + targetXmlPath);
        return nodePathString;
        //return childStartElement.
    }

    public URI createComponentFile(URI cmdiNodeFile, URI xsdFile) {
        System.out.println("createComponentFile: " + cmdiNodeFile + " : " + xsdFile);
        try {
            Document workingDocument = getDocument(null);
            readSchema(workingDocument, xsdFile);
            savePrettyFormatting(workingDocument, new File(cmdiNodeFile));
        } catch (IOException e) {
            GuiHelper.linorgBugCatcher.logError(e);
        } catch (ParserConfigurationException e) {
            GuiHelper.linorgBugCatcher.logError(e);
        } catch (SAXException e) {
            GuiHelper.linorgBugCatcher.logError(e);
        }
        return cmdiNodeFile;
    }

    private SchemaType getFirstSchemaType(File schemaFile) {
        try {
            InputStream inputStream = new FileInputStream(schemaFile);
            //Since we're dealing with xml schema files here the character encoding is assumed to be UTF-8
            XmlOptions options = new XmlOptions();
            options.setCharacterEncoding("UTF-8");
            SchemaTypeSystem sts = XmlBeans.compileXsd(new XmlObject[]{XmlObject.Factory.parse(inputStream, options)}, XmlBeans.getBuiltinTypeSystem(), null);
            // there can only be a single root node so we just get the first one, note that the IMDI schema specifies two (METATRANSCRIPT and VocabularyDef)
            return sts.documentTypes()[0];
        } catch (IOException e) {
            GuiHelper.linorgBugCatcher.logError(e);
        } catch (XmlException e) {
            // TODO: this is not really a good place to message this so modify to throw
            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Could not read the XML Schema", "Error inserting node");
            GuiHelper.linorgBugCatcher.logError(e);
        }
        return null;
    }

    private void readSchema(Document workingDocument, URI xsdFile) {
        File schemaFile = LinorgSessionStorage.getSingleInstance().updateCache(xsdFile.toString(), 5);
        SchemaType schemaType = getFirstSchemaType(schemaFile);
        constructXml(schemaType.getElementProperties()[0], "documentTypes", workingDocument, xsdFile.toString(), null);
    }

    private Element appendNode(Document workingDocument, String nameSpaceUri, Node parentElement, SchemaProperty schemaProperty) {
        Element currentElement = workingDocument.createElementNS("http://www.clarin.eu/cmd", schemaProperty.getName().getLocalPart());
        SchemaType currentSchemaType = schemaProperty.getType();
        for (SchemaProperty attributesProperty : currentSchemaType.getAttributeProperties()) {
            currentElement.setAttribute(attributesProperty.getName().getLocalPart(), attributesProperty.getDefaultText());
        }
        if (parentElement == null) {
            // this is probably not the way to set these, however this will do for now (many other methods have been tested and all failed to function correctly)
            currentElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            currentElement.setAttribute("xsi:schemaLocation", "http://www.clarin.eu/cmd " + nameSpaceUri);
            workingDocument.appendChild(currentElement);
        } else {
            parentElement.appendChild(currentElement);
        }
        //currentElement.setTextContent(schemaProperty.getMinOccurs() + ":" + schemaProperty.getMinOccurs());
        return currentElement;
    }

    private Node constructXml(SchemaProperty currentSchemaProperty, String pathString, Document workingDocument, String nameSpaceUri, Node parentElement) {
        Node returnNode = null;
        // this must be tested against getting the actor description not the actor of an imdi profile instance
        String currentPathString = pathString + "." + currentSchemaProperty.getName().getLocalPart();
        System.out.println("Found Element: " + currentPathString);
        SchemaType currentSchemaType = currentSchemaProperty.getType();
        Element currentElement = appendNode(workingDocument, nameSpaceUri, parentElement, currentSchemaProperty);
        returnNode = currentElement;
        //System.out.println("printSchemaType " + schemaType.toString());
//        for (SchemaType schemaSubType : schemaType.getAnonymousTypes()) {
//            System.out.println("getAnonymousTypes:");
//            constructXml(schemaSubType, pathString + ".*getAnonymousTypes*", workingDocument, parentElement);
//        }
//        for (SchemaType schemaSubType : schemaType.getUnionConstituentTypes()) {
//            System.out.println("getUnionConstituentTypes:");
//            printSchemaType(schemaSubType);
//        }
//        for (SchemaType schemaSubType : schemaType.getUnionMemberTypes()) {
//            System.out.println("getUnionMemberTypes:");
//            constructXml(schemaSubType, pathString + ".*getUnionMemberTypes*", workingDocument, parentElement);
//        }
//        for (SchemaType schemaSubType : schemaType.getUnionSubTypes()) {
//            System.out.println("getUnionSubTypes:");
//            printSchemaType(schemaSubType);
//        }
        //SchemaType childType =schemaType.

        /////////////////////

        for (SchemaProperty schemaProperty : currentSchemaType.getElementProperties()) {
            //for (int childCounter = 0; childCounter < schemaProperty.getMinOccurs().intValue(); childCounter++) {
            // if the searched element is a child node of the given node return
            // its SchemaType
            //if (properties[i].getName().toString().equals(element)) {
            // if the searched element was not a child of the given Node
            // then again for each of these child nodes search recursively in
            // their child nodes, in the case they are a complex type, because
            // only complex types have child nodes
            //currentSchemaType.getAttributeProperties();
            //     if ((schemaProperty.getType() != null) && (!(currentSchemaType.isSimpleType()))) {

            if (schemaProperty.getMinOccurs() != BigInteger.ZERO) {
                constructXml(schemaProperty, currentPathString, workingDocument, nameSpaceUri, currentElement);
                //     }
            }
            //}
        }
        return returnNode;
    }

    private void printoutDocument(Document workingDocument) {
        try {
            TransformerFactory tranFactory = TransformerFactory.newInstance();
            Transformer transformer = tranFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            Source src = new DOMSource(workingDocument);
            Result dest = new StreamResult(System.out);
            transformer.transform(src, dest);
        } catch (Exception e) {
            GuiHelper.linorgBugCatcher.logError(e);
        }
    }

    public void testWalk() {
        try {
            //new CmdiComponentBuilder().readSchema();
            //File xsdFile = LinorgSessionStorage.getSingleInstance().updateCache("http://www.mpi.nl/IMDI/Schema/IMDI_3.0.xsd", 5);
            Document workingDocument = getDocument(null);

            //Create instance of DocumentBuilderFactory
            //DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            //Get the DocumentBuilder
            //DocumentBuilder docBuilder = factory.newDocumentBuilder();
            //Create blank DOM Document
            //Document doc = docBuilder.newDocument();

            //create the root element
//            Element root = workingDocument.createElement("root");
//            //all it to the xml tree
//            workingDocument.appendChild(root);
//
//            //create a comment
//            Comment comment = workingDocument.createComment("This is comment");
//            //add in the root element
//            root.appendChild(comment);
//
//            //create child element
//            Element childElement = workingDocument.createElement("Child");
//            //Add the atribute to the child
//            childElement.setAttribute("attribute1", "The value of Attribute 1");
//            root.appendChild(childElement);

            readSchema(workingDocument, new URI("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1264769926773/xsd"));
            printoutDocument(workingDocument);
        } catch (Exception e) {
            GuiHelper.linorgBugCatcher.logError(e);
        }
    }

    public static void main(String args[]) {
        new CmdiComponentBuilder().testWalk();
    }
}
