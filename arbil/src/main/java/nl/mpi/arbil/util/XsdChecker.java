package nl.mpi.arbil.util;

/**
 * Document   : XsdChecker
 * Created on : Mon Dec 01 14:07:40 CET 2008
 * @author Peter.Withers@mpi.nl
 */
import nl.mpi.arbil.data.ArbilDataNode;
import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Scanner;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.XMLConstants;
import org.xml.sax.SAXException;
import javax.swing.text.StyleConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import nl.mpi.arbil.data.MetadataFormat;
import nl.mpi.arbil.userstorage.SessionStorage;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

public class XsdChecker extends JSplitPane {

    private static SessionStorage sessionStorage;
    private ArbilResourceResolver resourceResolver = new ArbilResourceResolver();

    public static void setSessionStorage(SessionStorage sessionStorageInstance) {
	sessionStorage = sessionStorageInstance;
    }
    JTextPane outputPane = new JTextPane();
    JTextPane fileViewPane = new JTextPane();
    StyledDocument doc;
    Style styleFatalError;
    Style styleError;
    Style styleWarning;
    Style styleNormal;
    boolean encounteredAdditionalErrors;
    String reportedError = "";

    public XsdChecker() {
	setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
	setTopComponent(new JScrollPane(outputPane));
	setBottomComponent(new JScrollPane(fileViewPane));

	doc = outputPane.getStyledDocument();

	styleNormal = outputPane.addStyle("Normal", null);
	StyleConstants.setForeground(styleNormal, Color.BLACK);

	styleWarning = outputPane.addStyle("Warning", null);
	StyleConstants.setForeground(styleWarning, new Color(0x00, 0x99, 0x00));

	styleError = outputPane.addStyle("Error", null);
	StyleConstants.setForeground(styleError, Color.BLUE);

	styleFatalError = outputPane.addStyle("FatalError", null);
	StyleConstants.setForeground(styleFatalError, Color.RED);
    }

    private Validator createValidator(URL schemaFile) throws Exception /*SAXException, ParserConfigurationException*/ {
	SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	schemaFactory.setResourceResolver(resourceResolver);
	Schema schema = schemaFactory.newSchema(schemaFile);
	Validator validator = schema.newValidator();
	validator.setResourceResolver(resourceResolver);
	return validator;
    }

    private URL getXsd(File metadataFile) {
	//boolean useImdiXSD = imdiFile.getAbsolutePath().toLowerCase().endsWith(".imdi");
	String nameSpaceURI = null;
	try {
	    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
	    documentBuilderFactory.setValidating(false);
	    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
	    Document document = documentBuilder.parse(metadataFile);
//            String[] schemaLocation = document.getDocumentElement().getAttributes().getNamedItem("xsi:schemaLocation").getNodeValue().split("\\s");
//            if (schemaLocation != null && schemaLocation.length > 0) {
//                nameSpaceURI = schemaLocation[schemaLocation.length - 1];
//            }
//            System.out.println("getNamespaceURI: " + document.getFirstChild().getNamespaceURI());
//            System.out.println("getBaseURI: " + document.getFirstChild().getBaseURI());
//            System.out.println("getLocalName: " + document.getFirstChild().getLocalName());
//            System.out.println("toString: " + document.getFirstChild().toString());
//            System.out.println("getAttribute: " + document.getDocumentElement().getAttribute("xmlns"));
//            System.out.println("getNamespaceURI: " + document.getDocumentElement().getNamespaceURI());
	    //nameSpaceURI = document.getDocumentElement().getNamespaceURI();

	    String schemaLocationString = null;
	    Node schemaLocationNode = document.getDocumentElement().getAttributes().getNamedItem("xsi:noNamespaceSchemaLocation");
	    if (schemaLocationNode == null) {
		schemaLocationNode = document.getDocumentElement().getAttributes().getNamedItem("xsi:schemaLocation");
	    }
	    if (schemaLocationNode != null) {
		schemaLocationString = schemaLocationNode.getNodeValue();
		String[] schemaLocation = schemaLocationString.split("\\s");
		schemaLocationString = schemaLocation[schemaLocation.length - 1];
		nameSpaceURI = metadataFile.toURI().resolve(schemaLocationString).toString();
	    }
	    System.out.println("schemaLocationString: " + schemaLocationString);

	} catch (IOException iOException) {
	    BugCatcherManager.getBugCatcher().logError(iOException);
	} catch (ParserConfigurationException parserConfigurationException) {
	    BugCatcherManager.getBugCatcher().logError(parserConfigurationException);
	} catch (SAXException sAXException) {
	    BugCatcherManager.getBugCatcher().logError(sAXException);
	}
	System.out.println("nameSpaceURI: " + nameSpaceURI);
	int daysTillExpire = 15;
	File schemaFile = null;
	if (nameSpaceURI != null && nameSpaceURI.toLowerCase().startsWith("http:/")) {
	    schemaFile = sessionStorage.updateCache(nameSpaceURI, daysTillExpire, false);
	}
	if (nameSpaceURI != null && nameSpaceURI.toLowerCase().startsWith("file:/")) {
	    try {
		// do not make cache copies of local schema files
		schemaFile = new File(new URI(nameSpaceURI));
	    } catch (URISyntaxException ex) {
		BugCatcherManager.getBugCatcher().logError(ex);
	    }
	}
	URL schemaURL = null;
	// if this is a cmdi file then we should just fail here
	// otherwise try to get the imdi schema
	if (!MetadataFormat.isPathImdi(metadataFile.toString())) {
	    try {
		schemaURL = schemaFile.toURL();
	    } catch (Exception e) {
		System.out.println("error getting xsd from the server: " + e.getMessage());
	    }
	} else {
	    if (schemaFile == null || !schemaFile.exists()) {
		// try getting the imdi schema if the name space has failed
		schemaFile = sessionStorage.updateCache("http://www.mpi.nl/IMDI/Schema/IMDI_3.0.xsd", daysTillExpire, false);
	    }
	    if (schemaFile.exists()) {
		try {
		    schemaURL = schemaFile.toURL();
		} catch (Exception e) {
		    System.out.println("error getting xsd from the server: " + e.getMessage());
		}
	    }
	    if (schemaURL == null) {
		// if all else has failed then resort to the imdi schema from the jar file which was updated at build time
		schemaURL = this.getClass().getResource("/nl/mpi/arbil/resources/IMDI/FallBack/IMDI_3.0.xsd");
	    }
	}
	return schemaURL;
    }

    private void alternateCheck(File metadataFile) throws Exception {
	URL schemaURL = getXsd(metadataFile);
	if (schemaURL == null) {
	    doc.insertString(doc.getLength(), "Failed to find the schema file.\n\n", styleFatalError);
	} else {
	    doc.insertString(doc.getLength(), "using schema file: " + schemaURL.getFile() + "\n\n", styleNormal);
	    Source xmlFile = new StreamSource(metadataFile);

	    class CustomErrorHandler implements ErrorHandler {

		File imdiFile;

		public CustomErrorHandler(File imdiFileLocal) {
		    imdiFile = imdiFileLocal;
		}

		private String getLine(int lineNumber) {
		    try {
			String returnText = "";
			Scanner scanner = new Scanner(imdiFile);
			for (int lineCounter = 0; lineCounter < lineNumber - 1; lineCounter++) {
			    returnText = scanner.nextLine();
			}
			// return the line plus the preceding and following lines
			return (lineNumber - 1) + ": " + returnText + "\n" + (lineNumber) + ": " + scanner.nextLine() + "\n" + (lineNumber + 1) + ": " + scanner.nextLine();
		    } catch (FileNotFoundException fileNotFoundException) {
			BugCatcherManager.getBugCatcher().logError(fileNotFoundException);
			return fileNotFoundException.getMessage();
		    }
		}

		public void warning(SAXParseException exception) throws SAXException {
		    try {
			doc.insertString(doc.getLength(), "warning: " + exception.getMessage() + "\nline: " + exception.getLineNumber() + " col: " + exception.getColumnNumber() + "\n" + getLine(exception.getLineNumber()) + "\n", styleWarning);
		    } catch (BadLocationException badLocationException) {
			BugCatcherManager.getBugCatcher().logError(badLocationException);
		    }
		}

		public void error(SAXParseException exception) throws SAXException {
		    try {
			doc.insertString(doc.getLength(), "error: " + exception.getMessage() + "\nline: " + exception.getLineNumber() + " col: " + exception.getColumnNumber() + "\n" + getLine(exception.getLineNumber()) + "\n", styleError);
		    } catch (BadLocationException badLocationException) {
			BugCatcherManager.getBugCatcher().logError(badLocationException);
		    }
		}

		public void fatalError(SAXParseException exception) throws SAXException {
		    try {
			doc.insertString(doc.getLength(), "fatalError: " + exception.getMessage() + "\nline: " + exception.getLineNumber() + " col: " + exception.getColumnNumber() + "\n" + getLine(exception.getLineNumber()) + "\n", styleError);
		    } catch (BadLocationException badLocationException) {
			BugCatcherManager.getBugCatcher().logError(badLocationException);
		    }
		}
	    }
	    final String checkingString = "Checking, please wait...\n\n";
	    final int checkingOffset = doc.getLength();
	    doc.insertString(checkingOffset, checkingString, styleNormal);

	    Validator validator = createValidator(schemaURL);
	    CustomErrorHandler errorHandler = new CustomErrorHandler(metadataFile);
	    validator.setErrorHandler(errorHandler);
	    try {
		validator.validate(xmlFile);
//            System.out.println(xmlFile.getSystemId() + " is valid");
//            doc.insertString(doc.getLength(), xmlFile.getSystemId() + " is valid\n", styleWarning);
	    } catch (SAXException e) {
		System.out.println(xmlFile.getSystemId() + " is NOT valid");
		System.out.println("Reason: " + e.getLocalizedMessage());

		doc.insertString(doc.getLength(), xmlFile.getSystemId() + " is NOT valid\n", styleError);
		doc.insertString(doc.getLength(), "Reason: " + e.getLocalizedMessage() + "\n", styleError);
	    }
	    doc.remove(checkingOffset, checkingString.length());

	}
    }

    public String simpleCheck(File metadataFile) {
	String messageString;
//        System.out.println("simpleCheck: " + imdiFile);
	URL schemaURL = getXsd(metadataFile);
	Source xmlFile = new StreamSource(metadataFile);
	try {
	    Validator validator = createValidator(schemaURL);
	    validator.validate(xmlFile);
	    return null;
	} catch (Exception e) {
//            System.out.println(sourceFile + " is NOT valid");
//            System.out.println("Reason: " + e.getLocalizedMessage());
	    messageString = "Error validating " + metadataFile.toURI() + "\n"
		    + "Reason: " + e.getLocalizedMessage() + "\n";
	    return messageString;
	}
    }

    public void checkXML(final ArbilDataNode dataNode) {
	Runnable checkXmlRunner = new Runnable() {

	    public void run() {
		doCheckXML(dataNode);
	    }
	};
	Thread thread = new Thread(checkXmlRunner);
	thread.start();
    }

    private void doCheckXML(ArbilDataNode imdiObject) {
	encounteredAdditionalErrors = false;
	try {
	    doc.insertString(doc.getLength(), "Checking the IMDI file conformance to the XSD\nThere are three types or messages: ", styleNormal);
	    doc.insertString(doc.getLength(), "Message, ", styleWarning);
	    doc.insertString(doc.getLength(), "Errors, ", styleError);
	    doc.insertString(doc.getLength(), "and ", styleNormal);
	    doc.insertString(doc.getLength(), "Fatal Errors." + "\n\n", styleFatalError);

//            doc.insertString(doc.getLength(), "Exporting imdi file to remove the id attributes\n", styleNormal);
	    synchronized (imdiObject) {
		alternateCheck(imdiObject.getFile());
	    }

	    try {
		fileViewPane.setPage(imdiObject.getURI().toURL());
	    } catch (Exception ex) {
		BugCatcherManager.getBugCatcher().logError(ex);
	    }
	} catch (Exception ex) {
	    encounteredAdditionalErrors = true;
	    reportedError = ex.getMessage();
	    System.out.println(ex.getMessage());
	}
	try {
	    if (encounteredAdditionalErrors) {
		doc.insertString(doc.getLength(), "\n" + reportedError + "\n", styleFatalError);
	    }
	    doc.insertString(doc.getLength(), "\nDone.\n", styleWarning);
	} catch (Exception ex) {
	    System.out.println("error: " + ex.getMessage());
	}
    }
}
