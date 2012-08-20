package nl.mpi.arbil.ui;

import nl.mpi.arbil.data.ArbilField;
import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JTextField;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import nl.mpi.arbil.ArbilMetadataException;
import nl.mpi.arbil.data.metadatafile.MetadataReader;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.DataNodeLoader;
import nl.mpi.arbil.data.MetadataBuilder;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcher;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.TreeHelper;
import nl.mpi.arbil.util.WindowManager;
import org.xml.sax.SAXException;

/**
 * Document : ArbilHyperlinkListener
 * Created on :
 *
 * @author Peter.Withers@mpi.nl
 */
public class ArbilHyperlinkListener implements HyperlinkListener {

    private static SessionStorage sessionStorage;

    public static void setSessionStorage(SessionStorage sessionStorageInstance) {
	sessionStorage = sessionStorageInstance;
    }
    private static TreeHelper treeHelper;

    public static void setTreeHelper(TreeHelper treeHelperInstance) {
	treeHelper = treeHelperInstance;
    }
    private static WindowManager windowManager;

    public static void setWindowManager(WindowManager windowManagerInstance) {
	windowManager = windowManagerInstance;
    }
    private static MessageDialogHandler dialogHandler;

    public static void setMessageDialogHandler(MessageDialogHandler dialogHandlerInstance) {
	dialogHandler = dialogHandlerInstance;
    }
    private static DataNodeLoader dataNodeLoader;

    public static void setDataNodeLoader(DataNodeLoader dataNodeLoaderInstance) {
	dataNodeLoader = dataNodeLoaderInstance;
    }

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
		    ArbilDataNode currentImdiObject = null;
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
		    dialogHandler.addMessageDialogToQueue(exception.getLocalizedMessage(), "Insert node error");
		}

	    } else if (evt.getURL() != null) {
		try {
		    // Try to open in ArbilHelp
		    if (ArbilHelp.getArbilHelpInstance().showHelpItem(evt.getURL())) {
			return;
		    }
		} catch (IOException ioEx) {
		    BugCatcherManager.getBugCatcher().logError(ioEx);
		} catch (SAXException saxEx) {
		    BugCatcherManager.getBugCatcher().logError(saxEx);
		}
		// Could not be opened in ArbilHelp, tell window manager to show
		windowManager.openUrlWindowOnce(evt.getURL().toString(), evt.getURL());
	    }
	}
    }

    private void setField(ArbilDataNode currentImdiObject, String fieldPath, String FieldValue) {
	for (ArbilField[] currentField : currentImdiObject.getFields().values()) {
	    if (currentField[0].getFullXmlPath().endsWith(fieldPath)) {
		currentField[0].setFieldValue(FieldValue, true, true);
	    }
	}
    }

    // note that this must not be used on nodes currently being edited because it bypasses the imdi loader process
    private ArbilDataNode addNode(ArbilDataNode parentNode, String nodeType, String nodeTypeDisplayName, String targetXmlPath, URI resourceUri, String mimeType) throws ArbilMetadataException {
	System.out.println("wizard add node: " + nodeType);
	System.out.println("adding into: " + parentNode);
	ArbilDataNode addedImdiObject;
	if (parentNode == null) {
	    URI targetFileURI = sessionStorage.getNewArbilFileName(sessionStorage.getCacheDirectory(), nodeType);
	    targetFileURI = MetadataReader.getSingleInstance().addFromTemplate(new File(targetFileURI), nodeType);
	    addedImdiObject = dataNodeLoader.getArbilDataNode(null, targetFileURI);
	    treeHelper.addLocation(targetFileURI);
	    treeHelper.applyRootLocations();
	} else {
	    parentNode.saveChangesToCache(true);
	    addedImdiObject = dataNodeLoader.getArbilDataNode(null, new MetadataBuilder().addChildNode(parentNode, nodeType, targetXmlPath, resourceUri, mimeType));
	}
	addedImdiObject.waitTillLoaded();
	windowManager.openFloatingTableOnce(new ArbilDataNode[]{addedImdiObject}, nodeTypeDisplayName);
	return addedImdiObject;
    }
}
