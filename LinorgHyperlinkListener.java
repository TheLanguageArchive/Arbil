/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 *
 * @author petwit
 */
public class LinorgHyperlinkListener implements HyperlinkListener {

    public void hyperlinkUpdate(HyperlinkEvent evt) {
        System.out.println("hyperlinkUpdate");
        if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            JEditorPane pane = (JEditorPane) evt.getSource();
//            try {
//                System.out.println(evt.getURL());
//                pane.setPage(evt.getURL());
//            } catch (IOException e) {
//                System.out.println(e.getMessage());
//            }
            LinorgWindowManager.getSingleInstance().openUrlWindowOnce(evt.getURL().toString(), evt.getURL());
        }
    }
}
