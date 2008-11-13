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
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.AbstractTableModel;
import mpi.linorg.ImdiHelper.ImdiTreeObject;

/**
 *
 * @author petwit
 */
public class ImdiTableModel extends AbstractTableModel {

    private boolean showIcons = false;
    private Hashtable imdiObjectHash = new Hashtable();
    private Hashtable allColumnNames = new Hashtable();
    LinorgFieldView tableFieldView;
    private int[] maxColumnWidths;
    int sortColumn = 0;
    boolean sortReverse = false;
    DefaultListModel listModel = new DefaultListModel(); // used by the image display panel
    Color currentHighlightColur = new Color(0xFFFFFF);

    public ImdiTableModel() {
        tableFieldView = GuiHelper.imdiFieldViews.getCurrentGlobalView().clone();
    }
    
    // code related to the list display of resources and loose files
    class ImdiListDataListener implements ListDataListener {

        private LinorgWindowManager.ImdiSplitPanel imdiSplitPanel;

        public ImdiListDataListener(LinorgWindowManager.ImdiSplitPanel localImdiSplitPanel) {
            imdiSplitPanel = localImdiSplitPanel;
        }

        public void contentsChanged(ListDataEvent e) {
            if (imdiSplitPanel != null) {
                imdiSplitPanel.setSplitDisplay();
            }
        }

        public void intervalAdded(ListDataEvent e) {
            if (imdiSplitPanel != null) {
                imdiSplitPanel.setSplitDisplay();
            }
        }

        public void intervalRemoved(ListDataEvent e) {
            if (imdiSplitPanel != null) {
                imdiSplitPanel.setSplitDisplay();
            }
        }
    }

    public DefaultListModel getListModel(LinorgWindowManager.ImdiSplitPanel imdiSplitPanel) {
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

    public ImdiHelper.ImdiTreeObject[] getSelectedImdiNodes(int[] selectedRows) {
        ImdiHelper.ImdiTreeObject[] selectedNodesArray = new ImdiHelper.ImdiTreeObject[selectedRows.length];
        for (int selectedRowCounter = 0; selectedRowCounter < selectedRows.length; selectedRowCounter++) {
            selectedNodesArray[selectedRowCounter] = getImdiNodeFromRow(selectedRows[selectedRowCounter]);
        }
        return selectedNodesArray;
    }

    public Enumeration getImdiNodes() {
        return imdiObjectHash.elements();
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
            addImdiObject((ImdiTreeObject) nodesToAdd.nextElement());
        }
        reloadTableData();
    }

    public void addSingleImdiObject(ImdiTreeObject imdiTreeObject) {
        addImdiObject(imdiTreeObject);
        reloadTableData();
    }

    private void addImdiObject(ImdiTreeObject imdiTreeObject) {
        imdiObjectHash.put(imdiTreeObject.getUrl(), imdiTreeObject);
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

    public void removeAllImdiRows() {
        listModel.removeAllElements();
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
        if (data[rowNumber][0] instanceof ImdiHelper.ImdiTreeObject) {
            return (ImdiTreeObject) data[rowNumber][0];
        } else {
            // in the case that the icon and sting are not displayed then try to get the imdifield in order to get the imdinode
            // TODO: this will fail if the imdiobject for the row does not have a field to display for the first column because there will be no imdi nor field in the first coloumn
            return ((ImdiHelper.ImdiField) data[rowNumber][columnNames.length - 1]).parentImdi;
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
        String copiedString = "";
        int firstColumn = 0;
        if (showIcons) {
            firstColumn = 1;
        }
        // add the headers
        for (int selectedColCounter = firstColumn; selectedColCounter < getColumnCount(); selectedColCounter++) {
            copiedString = copiedString + getColumnName(selectedColCounter) + ",";
        }
        copiedString = copiedString + "\n";
        // add the cell data
        for (int selectedRowCounter = 0; selectedRowCounter < selectedRows.length; selectedRowCounter++) {
            System.out.println("copying row: " + selectedRowCounter);
            for (int selectedColCounter = firstColumn; selectedColCounter < getColumnCount(); selectedColCounter++) {
                copiedString = copiedString + "\"" + data[selectedRows[selectedRowCounter]][selectedColCounter].toString().replace("\"", "\"\"") + "\",";
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
                imdiObjectHash.remove(imdiTreeObject.getUrl());
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
//        for (int rowCounter = 0; rowCounter < cellColour.length; rowCounter++) {
//            for (int colCounter = 0; colCounter < cellColour[rowCounter].length; colCounter++) {
        for (int rowCounter = 0; rowCounter < getRowCount(); rowCounter++) {
            for (int colCounter = 0; colCounter < getColumnCount(); colCounter++) {
                cellColour[rowCounter][colCounter] = new Color(0xFFFFFF);
            }
        }
        currentHighlightColur = new Color(0xFFFFFF);
        fireTableDataChanged();
    }

    private void allocateCellData(int rows, int cols) {
        data = new Object[rows][cols];
        cellColour = new Color[rows][cols];
        clearCellColours();
    }

    public class TableRowComparator implements Comparator {

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

    private void sortTableRows() {
        System.out.println("sortTableRows");
        Arrays.sort(data, new TableRowComparator(sortColumn, sortReverse));
    }

    public void reloadTableData() {
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
            columnNames = new String[displayedColumnNames.size() + firstFreeColumn];
            int columnPopulateCounter = firstFreeColumn;
            columnNames[0] = " "; // make sure the the icon column is shown its string is not null
            for (Enumeration currentColumnEnum = displayedColumnNames.elements(); currentColumnEnum.hasMoreElements();) {
                System.out.println("columnPopulateCounter: " + columnPopulateCounter);
                columnNames[columnPopulateCounter] = currentColumnEnum.nextElement().toString();
                columnPopulateCounter++;
            }
            // end create the column names array and prepend the icon and append the imdinode

            maxColumnWidths = new int[columnNames.length];

            allocateCellData(imdiObjectHash.size(), columnNames.length);

            Enumeration imdiRowsEnum = imdiObjectHash.elements();
            int rowCounter = 0;
            while (imdiRowsEnum.hasMoreElements()) {
                ImdiTreeObject currentNode = (ImdiTreeObject) imdiRowsEnum.nextElement();
                System.out.println("currentNode: " + currentNode.toString());
                Hashtable fieldsHash = currentNode.getFields();
                if (showIcons) {
                    //data[rowCounter][0] = new JLabel(currentNode.toString(), currentNode.getIcon(), JLabel.LEFT);
                    data[rowCounter][0] = currentNode;
                    maxColumnWidths[0] = currentNode.toString().length();
                }
                for (int columnCounter = firstFreeColumn; columnCounter < columnNames.length; columnCounter++) {
                    //System.out.println("columnNames[columnCounter]: " + columnNames[columnCounter] + " : " + columnCounter);
                    Object currentValue = fieldsHash.get(columnNames[columnCounter]);
                    if (currentValue != null) {
                        data[rowCounter][columnCounter] = currentValue;
                    } else {
                        data[rowCounter][columnCounter] = "";
                    }

                    //record the column string lengths 
                    int currentLength = (data[rowCounter][columnCounter].toString()).length();
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
            columnNames = new String[]{"Name", "Value"};
            if (imdiObjectHash.size() == 0) {
                allocateCellData(0, 2);
            } else {
                Enumeration imdiRowsEnum = imdiObjectHash.elements();
                if (imdiRowsEnum.hasMoreElements()) {
                    Hashtable fieldsHash = ((ImdiTreeObject) imdiRowsEnum.nextElement()).getFields();
                    allocateCellData(fieldsHash.size(), 2);
                    Enumeration labelsEnum = fieldsHash.keys();
                    Enumeration valuesEnum = fieldsHash.elements();
                    int rowCounter = 0;
                    while (labelsEnum.hasMoreElements() && valuesEnum.hasMoreElements()) {
                        data[rowCounter][0] = labelsEnum.nextElement();
                        data[rowCounter][1] = valuesEnum.nextElement();

                        //record the column string lengths 
                        int currentLength = (data[rowCounter][0].toString()).length();
                        if (maxColumnWidths[0] < currentLength) {
                            maxColumnWidths[0] = currentLength;
                        }
                        currentLength = (data[rowCounter][1].toString()).length();
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
        sortTableRows();
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
        return data[row][col];
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
        if (data[row][col] instanceof ImdiHelper.ImdiField) {
            returnValue = ((ImdiHelper.ImdiField) data[row][col]).parentImdi.isLocal();
        }
        System.out.println("Cell is ImdiField: " + returnValue);
//        System.out.println("result: " + (data[row][col] instanceof ImdiHelper.ImdiField));
        return (returnValue);
    }

    public void setValueAt(Object value, int row, int col) {
        System.out.println("setValueAt: " + value.toString() + " : " + row + " : " + col);
        if (data[row][col] instanceof ImdiHelper.ImdiField) {
            ImdiHelper.ImdiField currentField = ((ImdiHelper.ImdiField) data[row][col]);
            if (GuiHelper.linorgJournal.saveJournalEntry(currentField.parentImdi.getUrl(), currentField.xmlPath, currentField.getFieldValue(), value.toString())) {
                currentField.setFieldValue(value.toString());
            }
        } else {
            data[row][col] = value;
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
        tableFieldView.addHiddenColumn(getColumnName(columnIndex));
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
    // TODO: add child type as column
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
        //clearColours();
        // if the col or row provided here is invalid then we want to know about it so don't try to prevent such an error
//        if (row == -1 || col == -1) {
//            return;
//        }

        // rotate the next colour
        currentHighlightColur = new Color(currentHighlightColur.getGreen() - 40, currentHighlightColur.getRed(), currentHighlightColur.getBlue());

        System.out.println("highlightMatchingCells for row: " + row + " col: " + col);
        for (int rowCounter = 0; rowCounter < getRowCount(); rowCounter++) {
            for (int colCounter = 0; colCounter < getColumnCount(); colCounter++) {
                if (data[row][col].toString().equals(data[rowCounter][colCounter].toString())) {
                    cellColour[rowCounter][colCounter] = currentHighlightColur;

                //currentHighlightColur = new Color(0xFFFFFF);
                }
            }
        }
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
