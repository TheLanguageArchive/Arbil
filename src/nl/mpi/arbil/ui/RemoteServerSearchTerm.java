package nl.mpi.arbil.ui;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import nl.mpi.arbil.data.ArbilDataNode;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Document   : RemoteServerSearchTerm
 * Created on : Sept 22, 2010, 15:31:54 PM
 * @author Peter.Withers@mpi.nl
 */
public class RemoteServerSearchTerm extends javax.swing.JPanel {

    javax.swing.JPanel thisPanel = this;
    ArbilNodeSearchPanel parentPanel;
    private JTextField searchField;
    private JLabel resultCountLabel;
    public String searchString = "";
    private String valueFieldMessage = "<remote server search term (required)>";
    URI[] searchResults = null;
    String lastSearchString = null;

    public RemoteServerSearchTerm(ArbilNodeSearchPanel parentPanelLocal) {
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
        resultCountLabel = new JLabel();
        this.add(searchField);
        this.add(resultCountLabel);
    }

    private String[] performSearch(String searchString, ArbilDataNode[] arbilDataNodeArray) {
        ArrayList<String> returnArray = new ArrayList<String>();
        int maxResultNumber = 1000;
        try {
            String fullQueryString = "http://corpus1.mpi.nl/ds/imdi_search/servlet?action=getMatches";
            fullQueryString += "&num=" + maxResultNumber;
            fullQueryString += "&query=" + URLEncoder.encode(searchString, "UTF-8");
            fullQueryString += "&type=simple";
            for (ArbilDataNode arbilDataNode : arbilDataNodeArray) {
                if (arbilDataNode.archiveHandle != null) {
                    fullQueryString += "&archiveHandle=" + arbilDataNode.archiveHandle;
                } else {
                    ArbilWindowManager.getSingleInstance().addMessageDialogToQueue("Cannot search \"" + arbilDataNode + "\" because it does not have an archive handle", "Remote Search");
                }
            }
            // to search a branch we need the node id and to get that we need to have the handle and that might not exist, also to do any of that we would need to use an xmlrpc and include the lamusapi jar file to all versions of the application, so we will just search the entire archive since that takes about the same time to return the results
            //fullQueryString += "&nodeid=" + nodeidString; //MPI77915%23
            // &nodeid=MPI556280%23&nodeid=MPI84114%23&nodeid=MPI77915%23
            fullQueryString += "&returnType=xml";
//            try {
//                LinorgWindowManager.getSingleInstance().openUrlWindowOnce("Search Result", new URL(fullQueryString));
//            } catch (MalformedURLException exception) {
//                GuiHelper.linorgBugCatcher.logError(exception);
//            }
            System.out.println("QueryString: " + fullQueryString);
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setValidating(false);
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document resultsDocument = documentBuilder.parse(fullQueryString);

            String handleXpath = "/ImdiSearchResponse/Result/Match/URL";

            NodeList domIdNodeList = org.apache.xpath.XPathAPI.selectNodeList(resultsDocument, handleXpath);
            for (int nodeCounter = 0; nodeCounter < domIdNodeList.getLength(); nodeCounter++) {
                Node urlNode = domIdNodeList.item(nodeCounter);
                if (urlNode != null) {
                    System.out.println(urlNode.getTextContent());
                    returnArray.add(urlNode.getTextContent());
                }
            }
        } catch (DOMException exception) {
            GuiHelper.linorgBugCatcher.logError(exception);
        } catch (IOException exception) {
            GuiHelper.linorgBugCatcher.logError(exception);
        } catch (ParserConfigurationException exception) {
            GuiHelper.linorgBugCatcher.logError(exception);
        } catch (SAXException exception) {
            GuiHelper.linorgBugCatcher.logError(exception);
        } catch (TransformerException exception) {
            GuiHelper.linorgBugCatcher.logError(exception);
        }
        if (returnArray.size() >= maxResultNumber) {
            ArbilWindowManager.getSingleInstance().addMessageDialogToQueue("Found more results than can be displayed, only showing the first " + maxResultNumber + " results", "Remote Search");
        }
        return returnArray.toArray(new String[]{});
    }

    public URI[] getServerSearchResults(ArbilDataNode[] arbilDataNodeArray) {
        if (searchField.getText().equals(valueFieldMessage) || searchField.getText().equals("")) {
            ArbilWindowManager.getSingleInstance().addMessageDialogToQueue("No remote search term provided, cannot search remotely", "Remote Search");
            return new URI[]{};
        } else {
            if (searchField.getText().equals(lastSearchString)) {
                System.out.println("remote search term unchanged, returning last server response");
                return searchResults;
            } else {
                ArrayList<URI> foundNodes = new ArrayList<URI>();
                lastSearchString = searchField.getText();
                for (String resultString : performSearch(lastSearchString, arbilDataNodeArray)) {
                    try {
                        foundNodes.add(new URI(resultString));
                    } catch (URISyntaxException exception) {
                        GuiHelper.linorgBugCatcher.logError(exception);
                    }
                }
                searchResults = foundNodes.toArray(new URI[]{});
                resultCountLabel.setText(searchResults.length + " found on server ");

//    // todo: add remote search: use console output of the applet search at http://corpus1.mpi.nl/ds/imdi_browser/
//    // for example http://corpus1.mpi.nl/ds/imdi_search/servlet?action=getMatches&num=50&query=Sebastian&type=simple&nodeid=MPI77915%23&returnType=xml
                // todo searver side search
                return searchResults;
            }
        }
    }
}
