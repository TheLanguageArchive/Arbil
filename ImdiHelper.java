/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;
import javax.swing.JTextPane;
import javax.swing.table.DefaultTableModel;
import org.w3c.dom.Document;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import mpi.util.OurURL;
import mpi.imdi.api.*;

/**
 *
 * @author petwit
 */
public class ImdiHelper {

    private static OurURL baseURL = null; // for link resolution
    private static IMDIDom api = new IMDIDom();
    //private Hashtable selectedNodeRows = new Hashtable();
    private Vector selectedNodesList = new Vector();

    public DefaultMutableTreeNode getImdiTreeNode(String urlString) {
        OurURL inUrlLocal = null;
        try {
            inUrlLocal = new OurURL(urlString);
        } catch (MalformedURLException mue) {
            System.out.println("Invalid input file URL: " + mue);
            return null;
        }
        return getImdiTreeNode(inUrlLocal);
    }

    public DefaultMutableTreeNode getImdiTreeNode(OurURL inUrlLocal) {
        OurURL inURL = inUrlLocal;
        if (baseURL == null) {
            baseURL = inURL;
        }
        String fullUrlSting = inUrlLocal.getProtocol() + "://" + inUrlLocal.getHost() + inUrlLocal.getPath();
        Document dom = null;
        DefaultMutableTreeNode treeNode = null;

        if (!inURL.getFile().endsWith(".imdi")) {
            ImdiTreeNode imdiTreeNode = new ImdiTreeNode(inUrlLocal.getPath(), null, fullUrlSting);
            treeNode = new DefaultMutableTreeNode(imdiTreeNode);
            treeNode.setAllowsChildren(imdiTreeNode.isImdi());
        } else {
            dom = api.loadIMDIDocument(inURL, false);

            if (dom == null) {
                treeNode = new DefaultMutableTreeNode("Could not load IMDI");
            } else {
                String nodeName = "unknown";
                IMDIElement ie = api.getIMDIElement(dom, "Session.Name");
                if (ie != null) {
                    nodeName = "SN: " + ie.getValue();
                } else {
                    IMDIElement corpusname = api.getIMDIElement(dom, "Corpus.Name");
                    if (corpusname != null) {
                        nodeName = "CN: " + corpusname.getValue();
                    }
                }
                ImdiTreeNode imdiTreeNode = new ImdiTreeNode(nodeName, dom, fullUrlSting);
                treeNode = new DefaultMutableTreeNode(imdiTreeNode);
            }
        }
        return treeNode;
    }

    public void getImdiChildNodes(/*DefaultTreeModel treeModel, */DefaultMutableTreeNode itemNode) {
        if (itemNode.getChildCount() == 0) {
            if (String.class != itemNode.getUserObject().getClass()) {
                ImdiTreeNode itemImdiTreeNode = (ImdiTreeNode) itemNode.getUserObject();
                if (!itemImdiTreeNode.isImdi()) {
                    System.out.println("file to be opened");
                } else {
                    try {
                        baseURL = new OurURL(itemImdiTreeNode.getNodeDom().getDocumentURI());
                        IMDILink[] links = api.getIMDILinks(itemImdiTreeNode.getNodeDom(), baseURL, WSNodeType.UNKNOWN);
                        if (links != null) {
                            for (int linkCount = 0; linkCount < links.length /*&& linkCount < 3*/; linkCount++) {
                                itemNode.add(getImdiTreeNode(links[linkCount].getRawURL()));
                            }
                        }
                    } catch (MalformedURLException mue) {
                        System.out.println("Invalid input file URL: " + mue);
                        itemNode.add(new DefaultMutableTreeNode("Invalid input file from parent"));
                    }
                }
            }
        }
    }
    private String[] previousRowCells; // this is used to add the first row when the table changes from single to multiple mode

    public void addToGridData(DefaultTableModel tableModel, DefaultMutableTreeNode itemNode, JTextPane jTextPane1) {
        // check that it is an imdi file first
        ImdiTreeNode itemImdiTreeNode = (ImdiTreeNode) itemNode.getUserObject();
        if (itemImdiTreeNode.isImdi()) {
            String[] rowNames = new String[]{"Name", "Session.Name", "Corpus.Name", "Session.Description", "Corpus.Description", "Session.Title", "Corpus.Title"};
            boolean multipleRowMode = (0 < tableModel.getRowCount());
            // if there is only one node to show then set up the table for single display
            if (!multipleRowMode) {
                // set the column titles only if not already set 
                if (tableModel.getColumnCount() != 2) {
                    tableModel.setColumnCount(2);
                    tableModel.setColumnIdentifiers(new String[]{"Name", "Value"});
                }
                // clear the current rows
                tableModel.setRowCount(0);
            } else {
                // set the column titles only if not already set
                if (tableModel.getColumnCount() != rowNames.length) {
                    tableModel.setColumnCount(rowNames.length);
                    tableModel.setColumnIdentifiers(rowNames);
                    // clear the current rows
                    tableModel.setRowCount(0);
                    tableModel.addRow(previousRowCells);
                }
            }

            if (String.class != itemNode.getUserObject().getClass()) {
                Document itemDom = itemImdiTreeNode.getNodeDom();
                String[] currentRowCells = new String[rowNames.length]; // + 1 /* add one for the url which is not displayed but used to identify the row */];
                for (int rowNameCounter = 0; rowNameCounter < rowNames.length; rowNameCounter++) {
                    IMDIElement rowValue = api.getIMDIElement(itemDom, rowNames[rowNameCounter]);
                    if (rowValue != null) {
                        String cellValue = rowValue.getValue();
                        if (!multipleRowMode) {
                            tableModel.addRow(new Object[]{rowNames[rowNameCounter], cellValue});
                        }
                        currentRowCells[rowNameCounter] = cellValue;
                    } else {
                        currentRowCells[rowNameCounter] = "";
                    }
                }
                String hashKey = itemImdiTreeNode.getUrl();
                //currentRowCells[currentRowCells.length - 1] = hashKey;
                System.out.println("Added node to rows hashtable: " + hashKey);
                System.out.println("selectedRowCells: " + currentRowCells.toString());
                if (multipleRowMode) {
                    // add the current row
                    tableModel.addRow(currentRowCells);
                }
                // store the row index 
                selectedNodesList.add(hashKey);

                if (!multipleRowMode) {
                    // add the links
                    IMDILink[] links = api.getIMDILinks(itemImdiTreeNode.getNodeDom(), baseURL, WSNodeType.UNKNOWN);
                    if (links != null) {
                        for (int linkCount = 0; linkCount < links.length /*&& linkCount < 3*/; linkCount++) {
                            tableModel.addRow(new Object[]{"link:" + linkCount, links[linkCount].getRawURL()});
                        }
                    }
                }
                previousRowCells = currentRowCells.clone();
            }
            jTextPane1.setVisible(false);
        } else {
            //todo: display non imdi file
            System.out.println("display non imdi file");
            try {
                jTextPane1.setPage(new URL(itemImdiTreeNode.getUrl()));
                jTextPane1.setVisible(true);
            } catch (Exception ex) {
                // Not a valid URL
                jTextPane1.setVisible(false);
                //jTextPane1.setText("Could not load:" + itemImdiTreeNode.getUrl() + "\n" + "Error:" + ex.getMessage());
            }
        }
    }

    public DefaultTableModel removeAllFromGridData(DefaultTableModel tableModel) {
        tableModel.setRowCount(0);
        selectedNodesList.clear();
        return tableModel;
    }

    public DefaultTableModel removeFromGridData(DefaultTableModel tableModel, DefaultMutableTreeNode itemNode) {
        if (String.class != itemNode.getUserObject().getClass()) {
            ImdiTreeNode itemImdiTreeNode = (ImdiTreeNode) itemNode.getUserObject();
            String hashKey = itemImdiTreeNode.getUrl();
//            System.out.println("hashKey: " + hashKey);
//            for (int rowCounter = 0; rowCounter < tableModel.getRowCount(); rowCounter++) {
//                System.out.println("Row element: " + tableModel.getValueAt(rowCounter, tableModel.getColumnCount()));
//                //tableModel.
//                if (tableModel.getValueAt(rowCounter, tableModel.getColumnCount()) == hashKey) {
//                    tableModel.removeRow(rowCounter);
//                }
//            }
            try {
                tableModel.removeRow(selectedNodesList.indexOf(hashKey));
                selectedNodesList.remove(hashKey);

            // tableModel.
            } catch (Exception ex) {
                System.out.println("removeFromGridData failed: " + ex.toString());
            }
        }
        return tableModel;
    }

    private class ImdiTreeNode {

        protected ImdiTreeNode(String localNodeText, Document localNodeDom, String localUrlString) {
            this.nodeText = localNodeText;
            this.nodDom = localNodeDom;
            this.urlString = localUrlString;
        //this.icon = //new ImageIcon(getClass().getResource(imageName)); 
        }
        // Return text for display
        public String toString() {
            return nodeText;
        }
        // Return the icon
        public Document getNodeDom() {
            return nodDom;
        }

        public String getUrl() {
            return urlString;
        }

        public boolean isImdi() {
            return urlString.endsWith(".imdi");
        }
//        // Return the icon
//        public Icon getIcon() {
//            return icon;
//        }
        protected String nodeText;
        protected Document nodDom;
        protected String urlString;
        //protected Icon icon;
    }
}


