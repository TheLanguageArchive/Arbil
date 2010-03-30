package nl.mpi.arbil.clarin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
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
import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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

    public URI insertChildComponent(File cmdiNodeFile, String cmdiComponentId) {
        System.out.println(cmdiComponentId);
        // TODO: continue here
        return null;
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

    private void readSchema(Document workingDocument, URI xsdFile) {
        File schemaFile = LinorgSessionStorage.getSingleInstance().updateCache(xsdFile.toString(), 5);
        try {
//            Element root = workingDocument.createElement("readSchema");
//            workingDocument.appendChild(root);
            InputStream inputStream = new FileInputStream(schemaFile);
            //Since we're dealing with xml schema files here the character encoding is assumed to be UTF-8
            XmlOptions options = new XmlOptions();
            options.setCharacterEncoding("UTF-8");
            SchemaTypeSystem sts = XmlBeans.compileXsd(new XmlObject[]{XmlObject.Factory.parse(inputStream, options)}, XmlBeans.getBuiltinTypeSystem(), null);
            for (SchemaType schemaType : sts.documentTypes()) {
                System.out.println("documentTypes:");



                constructXml(schemaType, "documentTypes", workingDocument, xsdFile.toString(), null);
                break; // there can only be a single root node and the IMDI schema specifies two (METATRANSCRIPT and VocabularyDef) so we must stop before that error creates another
            }
//            for (SchemaType schemaType : sts.attributeTypes()) {
//                System.out.println("attributeTypes:");
//                printSchemaType(schemaType);
//            }
//            for (SchemaType schemaType : sts.globalTypes()) {
//                System.out.println("globalTypes:");
//                printSchemaType(schemaType);
//            }
        } catch (IOException e) {
            GuiHelper.linorgBugCatcher.logError(e);
        } catch (XmlException e) {
            GuiHelper.linorgBugCatcher.logError(e);
        }
    }

    private Element appendNode(Document workingDocument, String nameSpaceUri, Element parentElement, SchemaProperty schemaProperty) {
        Element currentElement = workingDocument.createElementNS(nameSpaceUri, schemaProperty.getName().getLocalPart());
        SchemaType currentSchemaType = schemaProperty.getType();
        for (SchemaProperty attributesProperty : currentSchemaType.getAttributeProperties()) {
            currentElement.setAttribute(attributesProperty.getName().getLocalPart(), attributesProperty.getDefaultText());
        }
        if (parentElement == null) {
//            currentElement.setAttributeNS(null, null, null)
            workingDocument.appendChild(currentElement);
        } else {
            parentElement.appendChild(currentElement);
        }
        return currentElement;
    }

    private void constructXml(SchemaType schemaType, String pathString, Document workingDocument, String nameSpaceUri, Element parentElement) {
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
                Element currentElement = appendNode(workingDocument, nameSpaceUri, parentElement, schemaProperty);
                // if the searched element was not a child of the given Node
                // then again for each of these child nodes search recursively in
                // their child nodes, in the case they are a complex type, because
                // only complex types have child nodes
                //currentSchemaType.getAttributeProperties();
                if ((schemaProperty.getType() != null) && (!(currentSchemaType.isSimpleType()))) {
                    constructXml(currentSchemaType, pathString, workingDocument, nameSpaceUri, currentElement);
                }
            //}
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

    public static void main(String args[]) {
        new CmdiComponentBuilder().testWalk();
    }
}
