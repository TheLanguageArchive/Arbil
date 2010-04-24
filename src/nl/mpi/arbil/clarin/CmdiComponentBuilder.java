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
import nl.mpi.arbil.LinorgSessionStorage;
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

    public URI insertChildComponent(ImdiTreeObject targetNode, String cmdiComponentId) {
        System.out.println("insertChildComponent: " + cmdiComponentId);
        String targetXpath = targetNode.getURI().getFragment();
        System.out.println("targetXpath: " + targetXpath);
        File cmdiNodeFile = targetNode.getFile();
        String nodeFragment = "";
        try {
            // load the schema
            SchemaType schemaType = getFirstSchemaType(targetNode.getNodeTemplate().templateFile);
            // load the dom
            Document targetDocument = getDocument(cmdiNodeFile);
            // bump the history
            targetNode.bumpHistory();
            // insert the new section
            try {
//                printoutDocument(targetDocument);
                nodeFragment = insertSectionToXpath(targetDocument, targetDocument.getFirstChild(), schemaType, targetXpath, cmdiComponentId);
            } catch (Exception exception) {
                GuiHelper.linorgBugCatcher.logError(exception);
            }
            // save the dom
            savePrettyFormatting(targetDocument, cmdiNodeFile); // note that we want to make sure that this gets saved even without changes because we have bumped the history ant there will be no file otherwise
        } catch (IOException exception) {
            GuiHelper.linorgBugCatcher.logError(exception);
        } catch (ParserConfigurationException exception) {
            GuiHelper.linorgBugCatcher.logError(exception);
        } catch (SAXException exception) {
            GuiHelper.linorgBugCatcher.logError(exception);
        }
//       diff_match_patch diffTool= new diff_match_patch();
//       diffTool.diff_main(targetXpath, targetXpath);
        try {
            System.out.println("nodeFragment: " + nodeFragment);
            // return the child node url and path in the xml
            // first strip off any fragment then add the full node fragment
            return new URI(targetNode.getURI().toString().split("#")[0] + "#" + nodeFragment);
        } catch (URISyntaxException exception) {
            GuiHelper.linorgBugCatcher.logError(exception);
            return null;
        }
    }

    private String insertSectionToXpath(Document targetDocument, Node documentNode, SchemaType schemaType, String targetXpath, String xsdPath) throws Exception {
        System.out.println("insertSectionToXpath");
        Node foundNode = null;
        Node foundPreviousNode = null;
        SchemaProperty foundProperty = null;
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
                foundPreviousNode = foundNode;
                //}
                foundNode = null;
                while (documentNode != null) {
                    System.out.println("documentNode: " + documentNode.getNodeName());
//                    System.out.println("documentNode: " + documentNode.toString());
                    if (currentPathComponent.equals(documentNode.getNodeName())) {
                        foundNode = documentNode;
                        documentNode = documentNode.getFirstChild();
                        break;
                    } else {
                        documentNode = documentNode.getNextSibling();
                    }
                }
                if (foundNode == null && foundPreviousNode == null) {
                    throw new Exception("failed to find the node path in the document: " + currentPathComponent);
                }
            }
        }
//        System.out.println("Adding marker node");
//        Element currentElement = targetDocument.createElement("AddedChildNode");
//        currentElement.setTextContent(xsdPath);
//        foundPreviousNode.appendChild(currentElement);
        System.out.println("Adding destination node");
        Element childStartElement = appendNode(targetDocument, null, foundPreviousNode, foundProperty);
        System.out.println("Adding destination sub nodes node");
        constructXml(foundProperty.getType(), xsdPath, targetDocument, null, childStartElement);
        System.out.println("Calculating the added fragment");
        String nodeFragment = childStartElement.getTagName();
        Node parentNode = childStartElement.getParentNode();
        while (parentNode != null) {
            // TODO: handle sibbling node counts
            System.out.println("nodeFragment: " + nodeFragment);
            nodeFragment = parentNode.getNodeName() + "." + nodeFragment;
            if (parentNode.isSameNode(targetDocument.getDocumentElement())) {
                break;
            }
            parentNode = parentNode.getParentNode();
        }
        nodeFragment = "." + nodeFragment;
        System.out.println("nodeFragment: " + nodeFragment);
        return nodeFragment;
        //return childStartElement.
//        try {
        // find the start node of the xml and of the xsd
//            Node targetNode = org.apache.xpath.XPathAPI.selectSingleNode(targetDocument, targetXpath);

        // add the new section to the xml

//        } catch (TransformerException exception) {
//            GuiHelper.linorgBugCatcher.logError(exception);
//        }
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
            GuiHelper.linorgBugCatcher.logError(e);
        }
        return null;
    }

    private void readSchema(Document workingDocument, URI xsdFile) {
        File schemaFile = LinorgSessionStorage.getSingleInstance().updateCache(xsdFile.toString(), 5);
        SchemaType schemaType = getFirstSchemaType(schemaFile);
        constructXml(schemaType, "documentTypes", workingDocument, xsdFile.toString(), null);
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

    private void constructXml(SchemaType schemaType, String pathString, Document workingDocument, String nameSpaceUri, Node parentElement) {
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

        for (SchemaProperty schemaProperty : schemaType.getElementProperties()) {
            //for (int childCounter = 0; childCounter < schemaProperty.getMinOccurs().intValue(); childCounter++) {
            // if the searched element is a child node of the given node return
            // its SchemaType
            //if (properties[i].getName().toString().equals(element)) {
            pathString = pathString + "." + schemaProperty.getName().getLocalPart();
            System.out.println("Found Element: " + pathString);
            SchemaType currentSchemaType = schemaProperty.getType();
            // if the searched element was not a child of the given Node
            // then again for each of these child nodes search recursively in
            // their child nodes, in the case they are a complex type, because
            // only complex types have child nodes
            //currentSchemaType.getAttributeProperties();
            if (schemaProperty.getMinOccurs() != BigInteger.ZERO) {
                Element currentElement = appendNode(workingDocument, nameSpaceUri, parentElement, schemaProperty);
                //     if ((schemaProperty.getType() != null) && (!(currentSchemaType.isSimpleType()))) {
                constructXml(currentSchemaType, pathString, workingDocument, nameSpaceUri, currentElement);
                //     }
            }
            //}
        }
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
