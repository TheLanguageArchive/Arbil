package nl.mpi.arbil;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.net.URI;
import nl.mpi.arbil.data.ImdiTreeObject;

/**
 * Document   : RemoteServerSearchTerm
 * Created on : Sept 22, 2010, 15:31:54 PM
 * @author Peter.Withers@mpi.nl
 */
public class RemoteServerSearchTerm extends javax.swing.JPanel {

    javax.swing.JPanel thisPanel = this;
    ImdiNodeSearchPanel parentPanel;
    private javax.swing.JTextField searchField;
    public String searchString = "";
    private String valueFieldMessage = "<remote server search term (required)>";
    URI[] searchResults = null;
    String lastSearchString = null;

    public RemoteServerSearchTerm(ImdiNodeSearchPanel parentPanelLocal) {
        parentPanel = parentPanelLocal;
        searchField = new javax.swing.JTextField(valueFieldMessage);
        searchField.setForeground(Color.lightGray);

        this.setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        searchField.addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals(valueFieldMessage)) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }

            public void focusLost(FocusEvent e) {
                if (searchField.getText().length() == 0) {
                    searchField.setText(valueFieldMessage);
                    searchField.setForeground(Color.lightGray);
                }
            }
        });

        searchField.addKeyListener(new java.awt.event.KeyAdapter() {

            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                parentPanel.stopSearch();
            }
        });
        this.add(searchField);

    }

    public URI[] getServerSearchResults(ImdiTreeObject[] imdiTreeObject) {
        if (searchField.getText().equals(valueFieldMessage) || searchField.getText().equals("")) {
            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("No remote search term provided, cannot search remotely", "Remote Search");
            return new URI[]{};
        } else {
            if (searchField.getText().equals(lastSearchString)) {
                System.out.println("remote search term unchanged, returning last server response");
                return searchResults;
            } else {
                lastSearchString = searchField.getText();
                searchResults = new URI[]{imdiTreeObject[0].getURI()};



//    // todo: add remote search: use console output of the applet search at http://corpus1.mpi.nl/ds/imdi_browser/
//    // for example http://corpus1.mpi.nl/ds/imdi_search/servlet?action=getMatches&num=50&query=Sebastian&type=simple&nodeid=MPI77915%23&returnType=xml
                // todo searver side search
                return searchResults;
            }
        }
    }
}
