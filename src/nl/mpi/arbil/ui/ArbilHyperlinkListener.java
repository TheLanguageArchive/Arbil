package nl.mpi.arbil.ui;

import nl.mpi.arbil.data.ImdiTableModel;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.data.TreeHelper;
import nl.mpi.arbil.data.ArbilField;
import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.net.URI;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JTextField;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import nl.mpi.arbil.ArbilMetadataException;
import nl.mpi.arbil.data.ImdiLoader;
import nl.mpi.arbil.data.metadatafile.MetadataReader;
import nl.mpi.arbil.data.ArbilNodeObject;
import nl.mpi.arbil.data.MetadataBuilder;

/**
 * Document   : LinorgHyperlinkListener
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
public class ArbilHyperlinkListener implements HyperlinkListener {

    public void hyperlinkUpdate(HyperlinkEvent evt) {
//        System.out.println("hyperlinkUpdate");
        if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            JEditorPane pane = (JEditorPane) evt.getSource();

//            HTMLDocument doc = (HTMLDocument) pane.getDocument();
//            System.out.println("# of Components in JTextPane: " + pane.getComponentCount());

//            try {
//                System.out.println(evt.getURL());
//                pane.setPage(evt.getURL());
//            } catch (IOException e) {
//                System.out.println(e.getMessage());
//            }
//            System.out.println("getURL: " +evt.getURL());
//            System.out.println("getDescription: " + evt.getDescription());
//            System.out.println(evt.getSourceElement());
//            System.out.println(evt.getEventType());
//            System.out.println(evt.toString());
//            System.out.println(evt.getSource());
            if (evt.getDescription().startsWith("arbilscript:")) {
                try {
                    ArbilNodeObject currentImdiObject = null;
                    String arbilscriptString = evt.getDescription().substring("arbilscript:".length());
                    System.out.println("acting on arbilscript: " + arbilscriptString);
                    String[] commandsArray = arbilscriptString.split("&");
                    for (String commandString : commandsArray) {
                        System.out.println("commandString: " + commandString);
                        if (commandString.startsWith("add=")) {
                            String nodeTypeString = commandString.substring("add=".length());
                            System.out.println("nodeTypeString: " + nodeTypeString);
                            currentImdiObject = addNode(currentImdiObject, nodeTypeString, "Wizard Corpus", null, null, null);
                        }
                        if (commandString.startsWith("set=")) {
                            String[] fieldCommand = commandString.substring("set=".length()).split(":");
                            System.out.println("set: " + fieldCommand[0] + " = " + fieldCommand[1]);
                            setField(currentImdiObject, fieldCommand[0], fieldCommand[1]);
                        }
                    }
                    // read the form values
                    // todo: resolve the issue of not being able to get the html name of the components, only the index number is available
                    for (int i = 0; i < pane.getComponentCount(); i++) {
                        Container c = (Container) pane.getComponent(i);
                        System.out.println(c.getComponentCount());
                        Component swingComponentOfHTMLInputType = c.getComponent(0);
                        System.out.println(swingComponentOfHTMLInputType.getClass().getName());
                        if (swingComponentOfHTMLInputType instanceof JTextField) {
                            JTextField tf = (JTextField) swingComponentOfHTMLInputType;
                            System.out.println(tf.getName());
                            System.out.println(tf.getText());
                            System.out.println(tf.getAction());
                            System.out.println(swingComponentOfHTMLInputType.getName());
                            System.out.println(swingComponentOfHTMLInputType.getName());
                            System.out.println(swingComponentOfHTMLInputType.getName());
                            String formCommandString = swingComponentOfHTMLInputType.getName();
                            System.out.println("formCommandString: " + formCommandString);
                            if (formCommandString != null && formCommandString.startsWith("arbilscript:set=")) {
                                String nodeTypeString = formCommandString.substring("arbilscript:set=".length());
                                System.out.println("nodeTypeString: " + nodeTypeString);
                                currentImdiObject = addNode(currentImdiObject, nodeTypeString, tf.getText(), null, null, null);
                            }
                        } else if (swingComponentOfHTMLInputType instanceof JButton) {
                        }
                    }
                } catch (ArbilMetadataException exception) {
                    ArbilWindowManager.getSingleInstance().addMessageDialogToQueue(exception.getLocalizedMessage(), "Insert node error");
                }

            } else if (evt.getURL() != null) {
                ArbilWindowManager.getSingleInstance().openUrlWindowOnce(evt.getURL().toString(), evt.getURL());
            }
        }
    }

    private void setField(ArbilNodeObject currentImdiObject, String fieldPath, String FieldValue) {
        for (ArbilField[] currentField : currentImdiObject.getFields().values()) {
            if (currentField[0].getFullXmlPath().endsWith(fieldPath)) {
                currentField[0].setFieldValue(FieldValue, true, true);
            }
        }
    }

    // note that this must not be used on nodes currently being edited because it bypasses the imdi loader process
    private ArbilNodeObject addNode(ArbilNodeObject parentNode, String nodeType, String nodeTypeDisplayName, String targetXmlPath, URI resourceUri, String mimeType) throws ArbilMetadataException {
        System.out.println("wizard add node: " + nodeType);
        System.out.println("adding into: " + parentNode);
        ArbilNodeObject addedImdiObject;
        if (parentNode == null) {
            URI targetFileURI = ArbilSessionStorage.getSingleInstance().getNewImdiFileName(ArbilSessionStorage.getSingleInstance().getCacheDirectory(), nodeType);
            targetFileURI = MetadataReader.getSingleInstance().addFromTemplate(new File(targetFileURI), nodeType);
            addedImdiObject = ImdiLoader.getSingleInstance().getImdiObject(null, targetFileURI);
            TreeHelper.getSingleInstance().addLocation(targetFileURI);
            TreeHelper.getSingleInstance().applyRootLocations();
        } else {
            parentNode.saveChangesToCache(true);
            addedImdiObject = ImdiLoader.getSingleInstance().getImdiObject(null, new MetadataBuilder().addChildNode(parentNode, nodeType, targetXmlPath, resourceUri, mimeType));
        }
        addedImdiObject.waitTillLoaded();
        ImdiTableModel imdiTableModel = ArbilWindowManager.getSingleInstance().openFloatingTableOnce(new ArbilNodeObject[]{addedImdiObject}, nodeTypeDisplayName);
        return addedImdiObject;
    }
}
