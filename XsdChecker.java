package mpi.linorg;

/**
 * Document   : XsdChecker
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
import java.awt.Color;
import java.io.File;
import java.net.URL;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.XMLConstants;
import org.xml.sax.SAXException;
import javax.swing.text.StyleConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

public class XsdChecker extends JSplitPane {
//        SimpleAttributeSet set = new SimpleAttributeSet();
//        StyleConstants.setForeground(set, Color.red);
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

    private Validator createValidator(URL schemaFile) throws SAXException, ParserConfigurationException {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(schemaFile);
        return schema.newValidator();
    }

    private URL getXsd() {
        int daysTillExpire = 15;
        String cachePath = GuiHelper.linorgSessionStorage.updateCache("http://www.mpi.nl/IMDI/Schema/IMDI_3.0.xsd", daysTillExpire);
        URL schemaURL = null;
        File schemaFile = new File(cachePath);
        if (schemaFile.exists()) {
            try {
                schemaURL = schemaFile.toURL();
            } catch (Exception e) {
                System.out.println("error getting xsd from the server: " + e.getMessage());
            }
        }
        if (schemaURL == null) {
            schemaURL = this.getClass().getResource("/mpi/metadata/schemas/IMDI/IMDI_3_0_11.xsd");
        }
        return schemaURL;
    }

    private void alternateCheck(File imdiFile) throws Exception {
        URL schemaURL = getXsd();
        doc.insertString(doc.getLength(), "using schema file: " + schemaURL.getFile() + "\n\n", styleNormal);
        Source xmlFile = new StreamSource(imdiFile);

        Validator validator = createValidator(schemaURL);
        try {
            validator.validate(xmlFile);
            System.out.println(xmlFile.getSystemId() + " is valid");
            doc.insertString(doc.getLength(), xmlFile.getSystemId() + " is valid\n", styleWarning);
        } catch (SAXException e) {
            System.out.println(xmlFile.getSystemId() + " is NOT valid");
            System.out.println("Reason: " + e.getLocalizedMessage());

            doc.insertString(doc.getLength(), xmlFile.getSystemId() + " is NOT valid\n", styleError);
            doc.insertString(doc.getLength(), "Reason: " + e.getLocalizedMessage() + "\n", styleError);
        }
    }

    public String simpleCheck(File imdiFile, String sourceFile) {
        String messageString;
//        System.out.println("simpleCheck: " + imdiFile);
        URL schemaURL = getXsd();
        Source xmlFile = new StreamSource(imdiFile);
        try {
            Validator validator = createValidator(schemaURL);
            validator.validate(xmlFile);
            return null;
        } catch (Exception e) {
//            System.out.println(sourceFile + " is NOT valid");
//            System.out.println("Reason: " + e.getLocalizedMessage());
            messageString = sourceFile + " is NOT valid\n" +
                    "Reason: " + e.getLocalizedMessage() + "\n";
            return messageString;
        }
    }

    public void checkXML(ImdiTreeObject imdiObject) {
        encounteredAdditionalErrors = false;
        try {
            doc.insertString(doc.getLength(), "Checking the IMDI file conformance to the XSD\nThere are three types or messages: ", styleNormal);
            doc.insertString(doc.getLength(), "Message, ", styleWarning);
            doc.insertString(doc.getLength(), "Errors, ", styleError);
            doc.insertString(doc.getLength(), "and ", styleNormal);
            doc.insertString(doc.getLength(), "Fatal Errors." + "\n\n", styleFatalError);

            File tempFile = File.createTempFile("linorg", ".imdi");
            doc.insertString(doc.getLength(), "Exporting imdi file to remove the id attributes\n", styleNormal);
            doc.insertString(doc.getLength(), "using temp file: " + tempFile.getCanonicalPath() + "\n", styleNormal);
            imdiObject.exportImdiFile(tempFile);
            alternateCheck(tempFile);
            try {
                fileViewPane.setPage(tempFile.toURL());
            } catch (Exception ex) {
                GuiHelper.linorgBugCatcher.logError(ex);
            }
            tempFile.deleteOnExit();
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
