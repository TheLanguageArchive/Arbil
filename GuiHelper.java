/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import java.net.URL;
import java.util.Vector;
import javax.swing.JTextPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author petwit
 */
public class GuiHelper {

    ImdiHelper imdiHelper = new ImdiHelper();
    //private Hashtable selectedNodeRows = new Hashtable();
    private Vector selectedNodesList = new Vector();
    int currentFieldListIndex = 1; // this variable sets the fields from the imdi file that are shown in the grid
    private Vector imdiFieldLists = new Vector();
    
    public GuiHelper(){
            loadImdiFieldLists();
    }
    
    public DefaultMutableTreeNode getImdiTreeNode(String urlString) {
        DefaultMutableTreeNode treeNode;
        ImdiHelper.ImdiTreeObject imdiTreeObject = imdiHelper.getTreeNodeObject(urlString);
        treeNode = new DefaultMutableTreeNode(imdiTreeObject);
        treeNode.setAllowsChildren(imdiTreeObject.isImdi() || imdiTreeObject.isDirectory());
        return treeNode;
    }

    public void getImdiChildNodes(DefaultMutableTreeNode itemNode) {
        if (itemNode.getChildCount() == 0) {
            if (String.class != itemNode.getUserObject().getClass()) {
                ImdiHelper.ImdiTreeObject imdiTreeObject = (ImdiHelper.ImdiTreeObject) itemNode.getUserObject();
                if (!imdiTreeObject.isImdi() && !imdiTreeObject.isDirectory()) {
                    System.out.println("file to be opened");
                } else {
                    String[] linkArray = imdiHelper.getLinks(imdiTreeObject);
                    if (linkArray != null) {
                        for (int linkCount = 0; linkCount < linkArray.length; linkCount++) {
                            itemNode.add(getImdiTreeNode(linkArray[linkCount]));
                        }
                    }
                }
            }
        }
    }
    private String[] previousRowCells; // this is used to add the first row when the table changes from single to multiple mode

    public void addToGridData(DefaultTableModel tableModel, DefaultMutableTreeNode itemNode, JTextPane jTextPane1) {
        // check that it is an imdi file first
        try {
            ImdiHelper.ImdiTreeObject itemImdiTreeObject = (ImdiHelper.ImdiTreeObject) itemNode.getUserObject();
            if (itemImdiTreeObject.isImdi()) {
                String[] rowNames = getCurrentFieldList();
                System.out.println("rowNames: " + rowNames.toString());
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


                    String[] currentRowCells = new String[rowNames.length]; // + 1 /* add one for the url which is not displayed but used to identify the row */];
                    for (int rowNameCounter = 0; rowNameCounter < rowNames.length; rowNameCounter++) {
                        String cellValue = imdiHelper.getField(itemImdiTreeObject, rowNames[rowNameCounter]);
                        if (cellValue != null) {
                            if (!multipleRowMode) {
                                tableModel.addRow(new Object[]{rowNames[rowNameCounter], cellValue});
                            }
                            currentRowCells[rowNameCounter] = cellValue;
                        } else {
                            currentRowCells[rowNameCounter] = "";
                        }
                    }
                    String hashKey = itemImdiTreeObject.getUrl();
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
                        String[] linksArray = imdiHelper.getLinks(itemImdiTreeObject);
                        //IMDILink[] links = api.getIMDILinks(itemImdiTreeObject.getNodeDom(), baseURL, WSNodeType.UNKNOWN);
                        if (linksArray != null) {
                            for (int linkCount = 0; linkCount < linksArray.length /*&& linkCount < 3*/; linkCount++) {
                                tableModel.addRow(new Object[]{"link:" + linkCount, linksArray[linkCount]});
                            }
                        }
                    }
                    previousRowCells = (String[]) currentRowCells.clone();
                }
                jTextPane1.setVisible(false);
            } else {
                //todo: display non imdi file
                System.out.println("display non imdi file");
                try {
                    jTextPane1.setPage(new URL(itemImdiTreeObject.getUrl()));
                    jTextPane1.setVisible(true);
                } catch (Exception ex) {
                    // Not a valid URL
                    jTextPane1.setVisible(false);
                //jTextPane1.setText("Could not load:" + itemImdiTreeNode.getUrl() + "\n" + "Error:" + ex.getMessage());
                }
            }
        } catch (Exception ex2) {
            System.out.println("Error not an ImdiTreeObject:" + ex2.getMessage());
        }
    }

    public DefaultTableModel removeAllFromGridData(DefaultTableModel tableModel) {
        tableModel.setRowCount(0);
        selectedNodesList.clear();
        return tableModel;
    }

    public DefaultTableModel removeFromGridData(DefaultTableModel tableModel, DefaultMutableTreeNode itemNode) {
        if (String.class != itemNode.getUserObject().getClass()) {
            ImdiHelper.ImdiTreeObject itemImdiTreeObject = (ImdiHelper.ImdiTreeObject) itemNode.getUserObject();
            String hashKey = itemImdiTreeObject.getUrl();
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

    public String[] getFieldListLables() {
        String[] labelArray = new String[imdiFieldLists.size()];
        for (int labelCount = 0; labelCount < imdiFieldLists.size(); labelCount++){
            labelArray[labelCount] = ((Object[])imdiFieldLists.get(labelCount))[0].toString();
        }
        return labelArray;
    }

    public String[] getCurrentFieldList() {
        String fieldNamesString = ((Object[])imdiFieldLists.get(currentFieldListIndex))[1].toString();
        String[] returnArray = fieldNamesString.split(",");
        System.out.println("fieldNamesString: " + fieldNamesString);
        System.out.println("returnArray: " + returnArray.length);
        return (returnArray);
    }

    public void setCurrentFieldListIndex(int currentIndex) {
        currentFieldListIndex = currentIndex;
        //System.out.println("currentFieldListIndex: " + currentFieldListIndex);
    }

    public int getCurrentFieldListIndex() {
        return currentFieldListIndex;
    }
    
    private void loadImdiFieldLists(){
        imdiFieldLists.add(new Object[]{"Minimal", "Name,Session.Name,Corpus.Name", null});
        imdiFieldLists.add(new Object[]{"Normal", "Name,Session.Name,Corpus.Name,Session.Description,Corpus.Description,Session.Title,Corpus.Title", null});
        imdiFieldLists.add(new Object[]{"Extra", "Name,History,Session.Name,Corpus.Name,Session.Description,Corpus.Description,Session.Title,Corpus.Title", null});
    }
    
    public javax.swing.table.DefaultTableModel getImdiFieldListsTableModel(){
              
        javax.swing.table.DefaultTableModel returnTableModel =  new javax.swing.table.DefaultTableModel(
            new Object [][] {
                 {null, null, null}
            },
            new String [] {
                "View Name", "Display Fields", "Display"
            }
        ){
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.Boolean.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        };
        returnTableModel.setRowCount(0);
        for (int fieldCount = 0; fieldCount < imdiFieldLists.size(); fieldCount++){
            returnTableModel.addRow((Object[])imdiFieldLists.get(fieldCount));
        }
        returnTableModel.setValueAt(new Boolean(true), currentFieldListIndex, 2);
        return returnTableModel;
    }
public javax.swing.table.DefaultTableModel getLocationsTableModel(){
              
//        javax.swing.table.DefaultTableModel returnTableModel =  new javax.swing.table.DefaultTableModel(
//            new Object [][] {
//                new Object[imdiFieldLists.size()][2]
//            },
//            new String [] {
//                "Tree", "Location"
//            }
//        ){
//            Class[] types = new Class [] {
//                java.lang.String.class, java.lang.String.class
//            };
//
//            public Class getColumnClass(int columnIndex) {
//                return types [columnIndex];
//            }
//        };
//        for (int fieldCount = 0; fieldCount < imdiFieldLists.size(); fieldCount++){
//            returnTableModel.addRow((Object[])imdiFieldLists.get(fieldCount));
//        }
//        return returnTableModel;
    return new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"Remote Corpus", "http://corpus1.mpi.nl/IMDI/metadata/IMDI.imdi"},
                {"Local Corpus", "file://data1/media-archive-copy/Corpusstructure/MPI.imdi"},
                {"Local Directory", "file://data1/media-archive-copy/TestWorkingDirectory/"}
            },
            new String [] {
                "Tree", "Location"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        };
    }    
//    new javax.swing.table.DefaultTableModel(
//            new Object [][] {
//                {"Display Fields", "Name,Session.Name,Corpus.Name,Session.Description,Corpus.Description,Session.Title,Corpus.Title", null},
//                {"Short Display Fields", "Name,Session.Name,Corpus.Name,Session.Description,Corpus.Description", null},
//                {null, null, null}
//            },
//            new String [] {
//                "View Name", "Display Fields", "Display"
//            }
//        ) {
//            Class[] types = new Class [] {
//                java.lang.String.class, java.lang.String.class, java.lang.Boolean.class
//            };
//
//            public Class getColumnClass(int columnIndex) {
//                return types [columnIndex];
//            }
//        }
    
}


