package nl.mpi.arbil;

import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
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
                String arbilscriptString = evt.getDescription().substring("arbilscript:".length());
                System.out.println("acting on arbilscript: " + arbilscriptString);
                String[] commandsArray = arbilscriptString.split("&");
                for (String commandString : commandsArray) {
                    System.out.println("commandString: " + commandString);
                }
                if (commandsArray[0].startsWith("add=")) {
                    String nodeTypeString = commandsArray[0].substring("add=".length());
                    System.out.println("nodeTypeString: " + nodeTypeString);
                    ImdiTreeObject.requestRootAddNode(nodeTypeString, "Wizard Corpus");
                }
            } else if (evt.getURL() != null) {
                LinorgWindowManager.getSingleInstance().openUrlWindowOnce(evt.getURL().toString(), evt.getURL());
            }
        }
    }
}
