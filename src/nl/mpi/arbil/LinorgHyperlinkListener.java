package nl.mpi.arbil;

import java.io.File;
import java.net.URI;
import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import nl.mpi.arbil.data.ImdiLoader;
import nl.mpi.arbil.data.ImdiSchema;
import nl.mpi.arbil.data.ImdiTreeObject;

/**
 * Document   : LinorgHyperlinkListener
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
public class LinorgHyperlinkListener implements HyperlinkListener {

    public void hyperlinkUpdate(HyperlinkEvent evt) {
//        System.out.println("hyperlinkUpdate");
        if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            JEditorPane pane = (JEditorPane) evt.getSource();
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
                ImdiTreeObject currentImdiObject = null;
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
            } else if (evt.getURL() != null) {
                LinorgWindowManager.getSingleInstance().openUrlWindowOnce(evt.getURL().toString(), evt.getURL());
            }
        }
    }

    private void setField(ImdiTreeObject currentImdiObject, String fieldPath, String FieldValue) {
        for (ImdiField[] currentField : currentImdiObject.getFields().values()) {
            if (currentField[0].getFullXmlPath().endsWith(fieldPath)) {
                currentField[0].setFieldValue(FieldValue, true, true);
            }
        }
    }

    // note that this must not be used on nodes currently being edited because it bypasses the imdi loader process
    private ImdiTreeObject addNode(ImdiTreeObject parentNode, String nodeType, String nodeTypeDisplayName, String targetXmlPath, URI resourceUri, String mimeType) {
        System.out.println("wizard add node: " + nodeType);
        System.out.println("adding into: " + parentNode);
        ImdiTreeObject addedImdiObject;
        if (parentNode == null) {
            URI targetFileURI = LinorgSessionStorage.getSingleInstance().getNewImdiFileName(LinorgSessionStorage.getSingleInstance().getCacheDirectory());
            targetFileURI = ImdiSchema.getSingleInstance().addFromTemplate(new File(targetFileURI), nodeType);
            addedImdiObject = ImdiLoader.getSingleInstance().getImdiObject(null, targetFileURI);
            TreeHelper.getSingleInstance().addLocation(targetFileURI);
            TreeHelper.getSingleInstance().applyRootLocations();
        } else {
            parentNode.saveChangesToCache(true);
            addedImdiObject = ImdiLoader.getSingleInstance().getImdiObject(null, parentNode.addChildNode(nodeType, targetXmlPath, resourceUri, mimeType));
        }
        addedImdiObject.waitTillLoaded();
        ImdiTableModel imdiTableModel = LinorgWindowManager.getSingleInstance().openFloatingTableOnce(new ImdiTreeObject[]{addedImdiObject}, nodeTypeDisplayName);
        return addedImdiObject;
    }
}
