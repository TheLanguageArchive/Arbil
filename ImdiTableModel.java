/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.DefaultListModel;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author petwit
 */
public class ImdiTableModel extends AbstractTableModel {

    private boolean showIcons = false;
    private Hashtable imdiObjectHash = new Hashtable();
    private Hashtable allColumnNames = new Hashtable();
    Vector childColumnNames = new Vector();
    LinorgFieldView tableFieldView;
    private int[] maxColumnWidths;
    int sortColumn = 0;
    boolean sortReverse = false;
    DefaultListModel listModel = new DefaultListModel(); // used by the image display panel
    Vector highlightCells = new Vector();

    public ImdiTableModel() {
        tableFieldView = GuiHelper.imdiFieldViews.getCurrentGlobalView().clone();
    }

    public DefaultListModel getListModel(LinorgSplitPanel imdiSplitPanel) {
        ImdiListDataListener listDataListener = new ImdiListDataListener(imdiSplitPanel);
        listModel.addListDataListener(listDataListener);
        return listModel;
    }
    // end code related to the list display of resources and loose files
    public void setCurrentView(LinorgFieldView localFieldView) {
        LinorgFieldView tempFieldView = localFieldView.clone();
        for (Enumeration oldKnowenColoumns = tableFieldView.getKnownColumns(); oldKnowenColoumns.hasMoreElements();) {
            tempFieldView.addKnownColumn(oldKnowenColoumns.nextElement().toString());
        }
        tableFieldView = tempFieldView;
        reloadTableData();
    }

    public ImdiTreeObject[] getSelectedImdiNodes(int[] selectedRows) {
        ImdiTreeObject[] selectedNodesArray = new ImdiTreeObject[selectedRows.length];
        for (int selectedRowCounter = 0; selectedRowCounter < selectedRows.length; selectedRowCounter++) {
            selectedNodesArray[selectedRowCounter] = getImdiNodeFromRow(selectedRows[selectedRowCounter]);
        }
        return selectedNodesArray;
    }

    public Enumeration getImdiNodes() {
        return imdiObjectHash.elements();
    }
    
    public Enumeration getImdiNodesURLs() {
        return imdiObjectHash.keys();
    }

    public void setShowIcons(boolean localShowIcons) {
        showIcons = localShowIcons;
    }

    public void addImdiObjects(ImdiTreeObject[] nodesToAdd) {
        for (int draggedCounter = 0; draggedCounter < nodesToAdd.length; draggedCounter++) {
            addImdiObject(nodesToAdd[draggedCounter]);
        }
        reloadTableData();
    }

    public void addImdiObjects(Enumeration nodesToAdd) {
        while (nodesToAdd.hasMoreElements()) {
            Object currentObject = nodesToAdd.nextElement();
            //System.out.println("addImdiObjects: " + currentObject.toString());
            addImdiObject((ImdiTreeObject) currentObject);
        }
        reloadTableData();
    }

    public void addSingleImdiObject(ImdiTreeObject imdiTreeObject) {
        addImdiObject(imdiTreeObject);
        reloadTableData();
    }

    private void addImdiObject(ImdiTreeObject imdiTreeObject) {
        if (imdiTreeObject != null) {
            imdiObjectHash.put(imdiTreeObject.getUrlString(), imdiTreeObject);
            imdiTreeObject.registerContainer(this);
        }
    }
    private void updateAllImdiObjects() {
        for (Enumeration nodesEnum = imdiObjectHash.elements(); nodesEnum.hasMoreElements();){            
            ImdiTreeObject imdiTreeObject = (ImdiTreeObject)nodesEnum.nextElement();        
            if (imdiTreeObject.isArchivableFile() || imdiTreeObject.hasResource()) {
                System.out.println("Adding to jlist: " + imdiTreeObject.toString());
                if (!listModel.contains(imdiTreeObject)) {
                    listModel.addElement(imdiTreeObject);
                }
            } else {
                System.out.println("Not adding to jlist: " + imdiTreeObject.toString());
            }
            System.out.println("isArchivableFile: " + imdiTreeObject.isArchivableFile());
            System.out.println("hasResource: " + imdiTreeObject.hasResource());
            Enumeration fieldNames = imdiTreeObject.getFields().keys();
            while (fieldNames.hasMoreElements()) {
                String currentColumnName = fieldNames.nextElement().toString();
                // keep track of the number of times that columns are used by updating the column use count hashtable
                Object currentColumnUse = allColumnNames.get(currentColumnName);
                int currentColumnUseCount = 0;
                if (currentColumnUse == null) {
                    currentColumnUseCount = 1;
                    tableFieldView.addKnownColumn(currentColumnName);
                } else {
                    currentColumnUseCount = ((Integer) currentColumnUse) + 1;
                }
                allColumnNames.put(currentColumnName, currentColumnUseCount);
            }

//            Enumeration tempColoumnEnum = columnNameHash.keys();
//            while (tempColoumnEnum.hasMoreElements()) {
//                System.out.println("column: " + tempColoumnEnum.nextElement());
//            }

//            Vector vecSort = new Vector(columnNameHash.keySet());
//            Collections.sort(vecSort, Collections.reverseOrder());
//            Iterator it = vecSort.iterator();
//            while (it.hasNext()) {
//                String element = (String) it.next();
//                System.out.println(element);
//            }
        }
    }

    public void removeAllImdiRows() {
        listModel.removeAllElements();
        for (Enumeration removableNodes = imdiObjectHash.elements(); removableNodes.hasMoreElements();){
            ((ImdiTreeObject)removableNodes.nextElement()).removeContainer(this);
        }
        imdiObjectHash.clear();
        allColumnNames.clear();
        columnNames = new String[0];
        data = new Object[0][0];
        cellColour = new Color[0][0];
        // add the icon column if icons are to be displayed
        setShowIcons(showIcons);
        reloadTableData();
    }

    private ImdiTreeObject getImdiNodeFromRow(int rowNumber) {
        // TODO: find error removing rows // look again here...
        // if that the first column is the imdi node (ergo string and icon) use that to remove the row
        if (data[rowNumber][0] instanceof ImdiTreeObject) {
            return (ImdiTreeObject) data[rowNumber][0];
        } else {
            // in the case that the icon and sting are not displayed then try to get the imdifield in order to get the imdinode
            // TODO: this will fail if the imdiobject for the row does not have a field to display for the first column because there will be no imdi nor field in the first coloumn
            return ((ImdiField) data[rowNumber][columnNames.length - 1]).parentImdi;
        //throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public void removeImdiRows(int[] selectedRows) {
        Vector nodesToRemove = new Vector();
        for (int selectedRowCounter = 0; selectedRowCounter < selectedRows.length; selectedRowCounter++) {
            System.out.println("removing: " + selectedRowCounter);
            nodesToRemove.add(getImdiNodeFromRow(selectedRows[selectedRowCounter]));
        }
        removeImdiObjects(nodesToRemove.elements());
    }

    public void copyImdiRows(int[] selectedRows, ClipboardOwner clipBoardOwner) {
        String csvSeparator = "\t"; // excel seems to work with tab but not comma 
        String copiedString = "";
        int firstColumn = 0;
        if (showIcons) {
            firstColumn = 1;
        }
        // add the headers
        int columnCount = getColumnCount();
        for (int selectedColCounter = firstColumn; selectedColCounter < columnCount; selectedColCounter++) {
            copiedString = copiedString + "\"" + getColumnName(selectedColCounter) + "\"";
            if (selectedColCounter < columnCount - 1) {
                copiedString = copiedString + csvSeparator;
            }
        }
        copiedString = copiedString + "\n";
        // add the cell data
        for (int selectedRowCounter = 0; selectedRowCounter < selectedRows.length; selectedRowCounter++) {
            System.out.println("copying row: " + selectedRowCounter);
            for (int selectedColCounter = firstColumn; selectedColCounter < columnCount; selectedColCounter++) {
                copiedString = copiedString + "\"" + data[selectedRows[selectedRowCounter]][selectedColCounter].toString().replace("\"", "\"\"") + "\"";
                if (selectedColCounter < columnCount - 1) {
                    copiedString = copiedString + csvSeparator;
                }
            }
            copiedString = copiedString + "\n";
        }
        System.out.println("copiedString: " + copiedString);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection stringSelection = new StringSelection(copiedString);
        clipboard.setContents(stringSelection, clipBoardOwner);
    }

    public void removeImdiObjects(Enumeration nodesToRemove) {
        while (nodesToRemove.hasMoreElements()) {
            Object currentObject = nodesToRemove.nextElement();
            if (currentObject instanceof ImdiTreeObject) {
                ImdiTreeObject imdiTreeObject = (ImdiTreeObject) currentObject;
                System.out.println("removing: " + imdiTreeObject.toString());
                //if (imdiTreeObject.isArchivableFile() || imdiTreeObject.hasResource()) {
                listModel.removeElement(imdiTreeObject);
                //}
                // remove the node
                imdiObjectHash.remove(imdiTreeObject.getUrlString());
                imdiTreeObject.removeContainer(this);
                // update the used columns
                Enumeration fieldNames = imdiTreeObject.getFields().keys();
                while (fieldNames.hasMoreElements()) {
                    String currentColumnName = fieldNames.nextElement().toString();
                    //System.out.println("currentColumnName: " + currentColumnName);
                    int currentColumnUse = (Integer) allColumnNames.get(currentColumnName);
                    currentColumnUse--;
                    if (currentColumnUse == 0) {
                        allColumnNames.remove(currentColumnName);
                    } else {
                        allColumnNames.put(currentColumnName, currentColumnUse);
                    }
                }
            }
        }
        // refresh the table data
        reloadTableData();
    }

    public void clearCellColours() {
        highlightCells.clear();
        for (int rowCounter = 0; rowCounter < cellColour.length; rowCounter++) {
            for (int colCounter = 0; colCounter < cellColour[rowCounter].length; colCounter++) {
                cellColour[rowCounter][colCounter] = new Color(0xFFFFFF);
            }
        }
        fireTableDataChanged();
    }

    private Color[][] setCellColours(Object[][] dataTemp) {
        Color[][] cellColourTemp;
        if (dataTemp.length == 0) {
            cellColourTemp = new Color[0][0];
        } else {
            cellColourTemp = new Color[dataTemp.length][dataTemp[0].length];
            Color currentHighlightColur = new Color(0xFFFFFF);
            for (Enumeration currentHighlight = highlightCells.elements(); currentHighlight.hasMoreElements();) {
                // rotate the next colour
                currentHighlightColur = new Color(currentHighlightColur.getGreen() - 40, currentHighlightColur.getRed(), currentHighlightColur.getBlue());
                String currentText = currentHighlight.nextElement().toString();
                // search the table for matching cells
                for (int rowCounter = 0; rowCounter < dataTemp.length; rowCounter++) {
                    for (int colCounter = 0; colCounter < dataTemp[rowCounter].length; colCounter++) {
                        if (dataTemp[rowCounter][colCounter].toString().equals(currentText)) {
                            cellColourTemp[rowCounter][colCounter] = currentHighlightColur;
                        }
                    }
                }
            }
        }
        return cellColourTemp;
    }

    private Object[][] allocateCellData(int rows, int cols) {
        Object[][] dataTemp = new Object[rows][cols];
        return dataTemp;
    }

    private class TableRowComparator implements Comparator {

        int sortColumn = 0;
        boolean sortReverse = false;

        public TableRowComparator(int tempSortColumn, boolean tempSortReverse) {
            sortColumn = tempSortColumn;
            sortReverse = tempSortReverse;
            System.out.println("TableRowComparator: " + sortColumn + ":" + sortReverse);
        }

        public int compare(Object firstRowArray, Object secondRowArray) {
            String baseValueA = ((Object[]) firstRowArray)[sortColumn].toString();
            String comparedValueA = ((Object[]) secondRowArray)[sortColumn].toString();
            // TODO: add the second or more sort column
//            if (!(baseValueA.equals(comparedValueA))) {
//                return baseValueB.compareTo(comparedValueB);
//            } else {
//                return baseValueA.compareTo(comparedValueA);
//            }
            int returnValue = baseValueA.compareTo(comparedValueA);
            if (!sortReverse) {
                returnValue = 1 - returnValue;
            }
            return returnValue;
        }
    }

    private void sortTableRows(String[] columnNamesTemp, Object[][] dataTemp) {
        System.out.println("sortTableRows");
        if (sortColumn < columnNamesTemp.length) {
            Arrays.sort(dataTemp, new TableRowComparator(sortColumn, sortReverse));
        }
    }

    public void reloadTableData() {
        String[] columnNamesTemp = new String[0];
        Object[][] dataTemp = new Object[0][0];
        
        updateAllImdiObjects();

        if (imdiObjectHash.size() > 1) {
            // display the grid view

            // calculate which of the available columns to show
            Enumeration columnNameEnum = allColumnNames.keys();
            Vector displayedColumnNames = new Vector();
            while (columnNameEnum.hasMoreElements()) {
                String currentColumnString = (String) columnNameEnum.nextElement();
                if (tableFieldView.viewShowsColumn(currentColumnString)) {
                    displayedColumnNames.add(currentColumnString);
                }
            }
            // end calculate which of the available columns to show

            // set the column offset to accomadate the icon which is not in the column hashtable
            int firstFreeColumn = 0;
            if (showIcons) {
                System.out.println("showing icon");
                // this assumes that the icon will always be in the leftmost column
                firstFreeColumn = 1;
            }

            // create and populate the column names array and prepend the icon and append the imdinode
            columnNamesTemp = new String[displayedColumnNames.size() + firstFreeColumn + childColumnNames.size()];
            int columnPopulateCounter = firstFreeColumn;
            if (columnNamesTemp.length > 0) {
                columnNamesTemp[0] = " "; // make sure the the icon column is shown its string is not null
            }
            for (Enumeration currentColumnEnum = displayedColumnNames.elements(); currentColumnEnum.hasMoreElements();) {
                System.out.println("columnPopulateCounter: " + columnPopulateCounter);
                columnNamesTemp[columnPopulateCounter] = currentColumnEnum.nextElement().toString();
                columnPopulateCounter++;
            }
            // populate the child node column titles
            for (Enumeration childColEnum = childColumnNames.elements(); childColEnum.hasMoreElements();) {
                columnNamesTemp[columnPopulateCounter] = childColEnum.nextElement().toString();
                columnPopulateCounter++;
            }

            // end create the column names array and prepend the icon and append the imdinode

            maxColumnWidths = new int[columnNamesTemp.length];

            dataTemp = allocateCellData(imdiObjectHash.size(), columnNamesTemp.length);

            Enumeration imdiRowsEnum = imdiObjectHash.elements();
            int rowCounter = 0;
            while (imdiRowsEnum.hasMoreElements()) {
                ImdiTreeObject currentNode = (ImdiTreeObject) imdiRowsEnum.nextElement();
                System.out.println("currentNode: " + currentNode.toString());
                Hashtable fieldsHash = currentNode.getFields();
                if (showIcons) {
                    //data[rowCounter][0] = new JLabel(currentNode.toString(), currentNode.getIcon(), JLabel.LEFT);
                    dataTemp[rowCounter][0] = currentNode;
                    maxColumnWidths[0] = currentNode.toString().length();
                }
                for (int columnCounter = firstFreeColumn; columnCounter < columnNamesTemp.length; columnCounter++) {
                    //System.out.println("columnNames[columnCounter]: " + columnNames[columnCounter] + " : " + columnCounter);
                    if (columnCounter < columnNamesTemp.length - childColumnNames.size()) {
                        Object currentValue = fieldsHash.get(columnNamesTemp[columnCounter]);
                        if (currentValue != null) {
                            dataTemp[rowCounter][columnCounter] = currentValue;
                        } else {
                            dataTemp[rowCounter][columnCounter] = "";
                        }
                    } else {
                        // populate the cell with any the child nodes for the current child nodes column
                        dataTemp[rowCounter][columnCounter] = currentNode.getChildNodesArray(columnNamesTemp[columnCounter]);
                        // prevent null values
                        if (dataTemp[rowCounter][columnCounter] == null) {
                            dataTemp[rowCounter][columnCounter] = "";
                        }
                    }

                    //record the column string lengths 
                    int currentLength = (dataTemp[rowCounter][columnCounter].toString()).length();
                    if (maxColumnWidths[columnCounter] < currentLength) {
                        maxColumnWidths[columnCounter] = currentLength;
                    }
                }
                rowCounter++;
            }
//            // display the column names use count for testing only
//            Enumeration tempEnum = columnNameHash.elements();
//            int tempColCount = 0;
//            while (tempEnum.hasMoreElements()){
//                data[0][tempColCount] = tempEnum.nextElement().toString();
//                tempColCount++;
//            }
        } else {
            // display the single node view
            maxColumnWidths = new int[2];
            columnNamesTemp = new String[]{"Name", "Value"};
            if (imdiObjectHash.size() == 0) {
                dataTemp = allocateCellData(0, 2);
            } else {
                Enumeration imdiRowsEnum = imdiObjectHash.elements();
                if (imdiRowsEnum.hasMoreElements()) {
                    Hashtable fieldsHash = ((ImdiTreeObject) imdiRowsEnum.nextElement()).getFields();
                    dataTemp = allocateCellData(fieldsHash.size(), 2);
                    Enumeration labelsEnum = fieldsHash.keys();
                    Enumeration valuesEnum = fieldsHash.elements();
                    int rowCounter = 0;
                    while (labelsEnum.hasMoreElements() && valuesEnum.hasMoreElements()) {
                        dataTemp[rowCounter][0] = labelsEnum.nextElement();
                        dataTemp[rowCounter][1] = valuesEnum.nextElement();

                        //record the column string lengths 
                        int currentLength = (dataTemp[rowCounter][0].toString()).length();
                        if (maxColumnWidths[0] < currentLength) {
                            maxColumnWidths[0] = currentLength;
                        }
                        currentLength = (dataTemp[rowCounter][1].toString()).length();
                        if (maxColumnWidths[1] < currentLength) {
                            maxColumnWidths[1] = currentLength;
                        }
                        rowCounter++;
                    }
                }
            }
        }
        // update the table model, note that this could be more specific, ie. just row or all it the columns have changed
        //fireTableDataChanged();
        sortTableRows(columnNamesTemp, dataTemp);
        cellColour = setCellColours(dataTemp);
        columnNames = columnNamesTemp;
        data = dataTemp;
        fireTableStructureChanged();
    }
    private String[] columnNames = new String[0];
    private Object[][] data = new Object[0][0];
    Color cellColour[][] = new Color[0][0];

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return data.length;
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public int getColumnLength(int col) {
        return maxColumnWidths[col];
    }

    public Object getValueAt(int row, int col) {
        if (row > -1 && col > -1) {
            return data[row][col];
        } else {
            return null;
        }
    }

    public boolean hasValueChanged(int row, int col) {
        if (row > -1 && col > -1) {
            if (data[row][col] instanceof ImdiField) {
                return ((ImdiField) data[row][col]).fieldNeedsSaveToDisk;
            }
        }
        return false;
    }

    public Color getCellColour(int row, int col) {
        return cellColour[row][col];
    }

    // JTable uses this method to determine the default renderer
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        boolean returnValue = false;
        if (data[row][col] instanceof ImdiField) {
            returnValue = ((ImdiField) data[row][col]).parentImdi.isLocal();
        }
        System.out.println("Cell is ImdiField: " + returnValue);
//        System.out.println("result: " + (data[row][col] instanceof ImdiHelper.ImdiField));
        if (returnValue == false) {
            if (data[row][col] instanceof Object[]) {
                System.out.println("Cell is a list of child nodes");
                returnValue = true;
            }
        }
        return (returnValue);
    }

    public void setValueAt(Object value, int row, int col) {
        System.out.println("setValueAt: " + value.toString() + " : " + row + " : " + col);
        if (data[row][col] instanceof ImdiField) {
            ImdiField currentField = ((ImdiField) data[row][col]);
            if (GuiHelper.linorgJournal.saveJournalEntry(currentField.parentImdi.getUrlString(), currentField.xmlPath, currentField.getFieldValue(), value.toString())) {
                currentField.setFieldValue(value.toString());
            }
            fireTableCellUpdated(row, col);
        } else if (data[row][col] instanceof Object[]) {
            System.out.println("cell is a child list so do not edit");
        } else {
            data[row][col] = value;
            fireTableCellUpdated(row, col);
        }
        fireTableCellUpdated(row, col);
    }

    public void sortByColumn(int columnIndex) {
        // TODO: sort columns
        System.out.println("sortByColumn: " + columnIndex);
        // set the reverse sort flag
        if (sortColumn == columnIndex) {
            sortReverse = !sortReverse;
        } else {
            sortReverse = false;
        }
        //set the current sort column
        sortColumn = columnIndex;
        //fireTableStructureChanged();
        reloadTableData();
    }

    public void hideColumn(int columnIndex) {
        System.out.println("hideColumn: " + columnIndex);
        // TODO: hide column
        System.out.println("hideColumn: " + getColumnName(columnIndex));
        if (!childColumnNames.remove(getColumnName(columnIndex))) {
            tableFieldView.addHiddenColumn(getColumnName(columnIndex));
        }
        reloadTableData();
    }

    public void showOnlyCurrentColumns() {
        tableFieldView.setShowOnlyColumns(columnNames);
    }

    public LinorgFieldView getFieldView() {
        return tableFieldView;
    }

    public void addChildTypeToDisplay(String childType) {
        System.out.println("addChildTypeToDisplay: " + childType);
        childColumnNames.add(childType);
        reloadTableData();
    }

    public Object[] getChildNames() {
        Vector childNames = new Vector();
        Enumeration imdiRowsEnum = imdiObjectHash.elements();
        while (imdiRowsEnum.hasMoreElements()) {
            Enumeration childEnum = ((ImdiTreeObject) imdiRowsEnum.nextElement()).getChildEnum();
            while (childEnum.hasMoreElements()) {
                // TODO: maybe check the children for children before adding them to this list
                String currentChildName = childEnum.nextElement().toString();
                if (!childNames.contains(currentChildName)) {
                    childNames.add(currentChildName);
                }
            }
        }
        return childNames.toArray();
    }

    public void copyCellToColumn(int row, int col) {
        // if the col or row provided here is invalid then we want to know about it so don't try to prevent such an error
        //        if (row == -1 || col == -1) {
        //            return;
        //        }
        System.out.println("copyCellToColumn for row: " + row + " col: " + col);
        for (int rowCounter = 0; rowCounter < getRowCount(); rowCounter++) {
            if (rowCounter != row) {
                setValueAt(data[row][col].toString(), rowCounter, col);
                fireTableCellUpdated(rowCounter, col);
            }
        }
    }

    public void highlightMatchingCells(int row, int col) {
        highlightCells.add(data[row][col].toString());
        cellColour = setCellColours(data);
        fireTableDataChanged();
    }

    public Vector getMatchingRows(int sampleRowNumber) {
        System.out.println("MatchingRows for: " + sampleRowNumber);
        Vector matchedRows = new Vector();
        if (sampleRowNumber > -1 && sampleRowNumber < getRowCount()) {
            for (int rowCounter = 0; rowCounter < getRowCount(); rowCounter++) {
                boolean rowMatches = true;
                for (int colCounter = 0; colCounter < getColumnCount(); colCounter++) {
                    if (!getValueAt(rowCounter, colCounter).toString().equals(getValueAt(sampleRowNumber, colCounter).toString())) {
                        rowMatches = false;
                        break;
                    }
                }
                //System.out.println("Checking: " + getValueAt(sampleRowNumber, 0) + " : " + getValueAt(rowCounter, 0));
                if (rowMatches) {
                    //System.out.println("Matched: " + rowCounter + " : " + getValueAt(rowCounter, 0));
                    matchedRows.add(rowCounter);
                }
            }
        }
        return matchedRows;
    }
}
