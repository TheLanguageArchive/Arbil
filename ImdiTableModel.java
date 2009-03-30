/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
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
    private Vector allColumnNames = new Vector();
    Vector childColumnNames = new Vector();
    LinorgFieldView tableFieldView;
    private int[] maxColumnWidths;
    boolean horizontalView = false;
    int sortColumn = -1;
    boolean sortReverse = false;
    DefaultListModel listModel = new DefaultListModel(); // used by the image display panel
    Vector highlightCells = new Vector();
    String[] singleNodeViewHeadings = new String[]{"IMDI Field", "Value"};

    public ImdiTableModel() {
        tableFieldView = ImdiFieldViews.getSingleInstance().getCurrentGlobalView().clone();
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
            if (imdiTreeObject.isDirectory) {
                // TODO: this could be made non recursive, but only if issues arrise
                addImdiObjects(imdiTreeObject.loadChildNodes());
            } else {
                imdiObjectHash.put(imdiTreeObject.getUrlString(), imdiTreeObject);
                imdiTreeObject.registerContainer(this);
            }
        }
    }

    private void updateAllImdiObjects() {
        allColumnNames.removeAllElements();
        for (Enumeration nodesEnum = imdiObjectHash.elements(); nodesEnum.hasMoreElements();) {
            ImdiTreeObject imdiTreeObject = (ImdiTreeObject) nodesEnum.nextElement();
            if (!imdiTreeObject.isImdi() || imdiTreeObject.isArchivableFile() || imdiTreeObject.hasResource()) {
                // on application reload a file may be readded to a table before the type checker gets a chance to run, since a file must have been checked for it to get here we bypass that check at this point
                System.out.println("Adding to jlist: " + imdiTreeObject.toString());
                if (!listModel.contains(imdiTreeObject)) {
                    listModel.addElement(imdiTreeObject);
                }
            } else {
                System.out.println("Not adding to jlist: " + imdiTreeObject.toString());
            }
            System.out.println("isArchivableFile: " + imdiTreeObject.isArchivableFile());
            System.out.println("hasResource: " + imdiTreeObject.hasResource());
            for (Enumeration<String> fieldNames = imdiTreeObject.getFields().keys(); fieldNames.hasMoreElements();) {
                String currentColumnName = fieldNames.nextElement().toString();
                if (!allColumnNames.contains(currentColumnName)) {
                    allColumnNames.add(currentColumnName);
                }
                tableFieldView.addKnownColumn(currentColumnName);
            }
        }
    }

    public void removeAllImdiRows() {
        listModel.removeAllElements();
        for (Enumeration removableNodes = imdiObjectHash.elements(); removableNodes.hasMoreElements();) {
            ((ImdiTreeObject) removableNodes.nextElement()).removeContainer(this);
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
        } else if (data[rowNumber][0] instanceof ImdiField[]) {
            return ((ImdiField[]) data[rowNumber][columnNames.length - 1])[0].parentImdi;
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
        if (showIcons && horizontalView) { // horizontalView excludes icon display
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

    public String pasteIntoImdiRows(int[] selectedRows, ClipboardOwner clipBoardOwner) {
        int pastedCount = 0;
        String resultMessage = null;
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable transfer = clipboard.getContents(null);
        try {
            String clipBoardString = "";
            Object clipBoardData = transfer.getTransferData(DataFlavor.stringFlavor);
            if (clipBoardData != null) {//TODO: check that this is not null first but let it pass on null so that the no data to paste messages get sent to the user
                clipBoardString = clipBoardData.toString();
            }
            System.out.println("clipBoardString: " + clipBoardString);
            // to do this there must be either two rows or two columns otherwise we should abort
            String[] clipBoardLines = clipBoardString.split("\"\\n\"");
            if (clipBoardLines.length == 1) {
                // re try in case the csv text is not quoted
                clipBoardLines = clipBoardString.split("\n");
            }
            if (clipBoardLines.length > 1) {
                String[] firstLine = clipBoardLines[0].split("\"\\t\"");
                if (firstLine.length == 1) {
                    firstLine = clipBoardLines[0].split("\t");
                }
                boolean singleNodeAxis = false;
                String regexString = "[(\"^)($\")]";
                System.out.println("regexString: " + (firstLine[0].replaceAll(regexString, "")));
                if (firstLine[0].replaceAll(regexString, "").equals(singleNodeViewHeadings[0]) && firstLine[1].replaceAll(regexString, "").equals(singleNodeViewHeadings[1])) {
                    singleNodeAxis = true;
                }
                if (!singleNodeAxis) {
                    resultMessage = "Incorrect data to paste.\nFields must be copied from a table where only one IMDI file is displayed.";
                }
                if (singleNodeAxis) {
                    Hashtable<String, Integer> fieldNamePasteCount = new Hashtable(); // used to count the field index in the fields array if there are multiple values of the same name
                    for (int lineCounter = 1 /* skip the header */; lineCounter < clipBoardLines.length; lineCounter++) {
                        String clipBoardLine = clipBoardLines[lineCounter];
                        System.out.println("clipBoardLine: " + clipBoardLine);
                        String[] clipBoardCells = clipBoardLine.split("\\t");
                        System.out.println("clipBoardCells.length: " + clipBoardCells.length);
                        if (clipBoardCells.length != 2) {
                            resultMessage = "Inconsistent number of columns in the data to paste.\nThe pasted data could be incorrect.";
                        } else {//loop over the selected rows
                            String currentFieldName = clipBoardCells[0].replaceAll(regexString, "");
                            String currentFieldValue = clipBoardCells[1].replaceAll(regexString, "");
                            int currentFieldCounted = 0;
                            if (fieldNamePasteCount.containsKey(currentFieldName)) {
                                currentFieldCounted = fieldNamePasteCount.get(currentFieldName);
                                fieldNamePasteCount.put(currentFieldName, ++currentFieldCounted);
                            } else {
                                fieldNamePasteCount.put(currentFieldName, currentFieldCounted);
                            }
                            for (int selectedRowCounter = 0; selectedRowCounter < selectedRows.length; selectedRowCounter++) {
                                //loop over all cells in the selected rows
                                for (int selectedColCounter = 0; selectedColCounter < getColumnCount(); selectedColCounter++) {
                                    // the cell contents could be either an imdifield or an array of imdifields or an imditreeobject or an array of imditreenodes or a string or null
                                    // only imdifields or arrays of imdifield need to be handled here
                                    ImdiField[] currentFieldArray = null;
                                    if (data[selectedRows[selectedRowCounter]][selectedColCounter] instanceof ImdiField[]) {
                                        currentFieldArray = (ImdiField[]) data[selectedRows[selectedRowCounter]][selectedColCounter];
                                    }
                                    if (data[selectedRows[selectedRowCounter]][selectedColCounter] instanceof ImdiField) {
                                        if (((ImdiField) data[selectedRows[selectedRowCounter]][selectedColCounter]).getTranslateFieldName().equals(currentFieldName)) {
                                            currentFieldArray = ((ImdiField) data[selectedRows[selectedRowCounter]][selectedColCounter]).parentImdi.getFields().get(currentFieldName);
                                        }
                                    }
                                    // todo prevent field array paste different from table view to single view
                                    if (currentFieldArray != null) {
                                        System.out.println("current target Field: " + currentFieldArray[0].getTranslateFieldName());
                                        if (currentFieldArray.length > currentFieldCounted) {
                                            if (currentFieldArray[currentFieldCounted].getTranslateFieldName().equals(currentFieldName)) {
                                                System.out.println("currentFieldName: " + currentFieldName + ":" + currentFieldCounted + ":" + currentFieldValue);
                                                currentFieldArray[currentFieldCounted].setFieldValue(currentFieldValue, true); // while we could reduce ui updates, this may be a paste into multiple nodes, so to keep it simple the field does the ui update rather than saving it till last
                                                pastedCount++;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                resultMessage = "No data to paste.";
            }
            if (pastedCount == 0) {
                if (resultMessage == null) {
                    resultMessage = "No fields matched the data on the clipboard.";
                }
            }
        } catch (Exception ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
        }
        return resultMessage;
    }

    public void removeImdiObjects(Enumeration nodesToRemove) {
        while (nodesToRemove.hasMoreElements()) {
            Object currentObject = nodesToRemove.nextElement();
            if (currentObject instanceof ImdiTreeObject) {
                ImdiTreeObject imdiTreeObject = (ImdiTreeObject) currentObject;
                System.out.println("removing: " + imdiTreeObject.toString());
                listModel.removeElement(imdiTreeObject);
                imdiObjectHash.remove(imdiTreeObject.getUrlString());
                imdiTreeObject.removeContainer(this);
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
//            System.out.println("TableRowComparator: " + sortColumn + ":" + sortReverse);
        }

        public int compare(Object firstRowArray, Object secondRowArray) {
            if (sortColumn >= 0) {
                // (done by setting when the hor ver setting changes) need to add a check for horizontal view and -1 which is invalid
                String baseValueA = ((Object[]) firstRowArray)[sortColumn].toString();
                String comparedValueA = ((Object[]) secondRowArray)[sortColumn].toString();
                // TODO: add the second or more sort column
//            if (!(baseValueA.equals(comparedValueA))) {
//                return baseValueB.compareTo(comparedValueB);
//            } else {
//                return baseValueA.compareTo(comparedValueA);
//            }
                int returnValue = baseValueA.compareTo(comparedValueA);
                if (sortReverse) {
                    returnValue = 1 - returnValue;
                }
                return returnValue;
            } else {
                try {
                    String baseValueA = ((ImdiField) ((Object[]) firstRowArray)[1]).fieldID;
                    String comparedValueA = ((ImdiField) (((Object[]) secondRowArray)[1])).fieldID;
//                    if (baseValueA != null && comparedValueA != null) {
                    int baseIntA = Integer.parseInt(baseValueA.substring(1));
                    int comparedIntA = Integer.parseInt(comparedValueA.substring(1));
                    int returnValue = baseIntA - comparedIntA;
                    return returnValue;
//                    } else {
//                        return 0;
//                    }
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                    return 1;
                }
            }
        }
    }

    private void sortTableRows(String[] columnNamesTemp, Object[][] dataTemp) {
//        System.out.println("sortTableRows");
        if (sortColumn < columnNamesTemp.length) {
            Arrays.sort(dataTemp, new TableRowComparator(sortColumn, sortReverse));
        }
    }

    public void reloadTableData() {
        System.out.println("reloadTableData");
        int previousColumnCount = getColumnCount();
        String[] columnNamesTemp = new String[0];
        Object[][] dataTemp = new Object[0][0];

        updateAllImdiObjects();

        // set the view to either horizontal or vertical and set the default sort
        boolean lastHorizontalView = horizontalView;
        horizontalView = imdiObjectHash.size() > 1;
        if (!horizontalView) { // set the table for a single image if that is all that is shown
            //if (imdiObjectHash.size() == listModel.getSize()) { // TODO: this does not account for when a resource is shown
            // TODO: this does not account for when a resource is shown
            if (allColumnNames.size() == 0) {
                horizontalView = true;
            }
        }

        if (lastHorizontalView != horizontalView) {
            sortReverse = false;
            // update to the default sort
            if (horizontalView) {
                sortColumn = 0;
            } else {
                sortColumn = -1;
            }
        }

        if (horizontalView) {
            // display the grid view

            // calculate which of the available columns to show
            Enumeration columnNameEnum = allColumnNames.elements();
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
//                System.out.println("showing icon");
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
//                System.out.println("columnPopulateCounter: " + columnPopulateCounter);
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
                Hashtable<String, ImdiField[]> fieldsHash = currentNode.getFields();
                if (showIcons) {
                    //data[rowCounter][0] = new JLabel(currentNode.toString(), currentNode.getIcon(), JLabel.LEFT);
                    dataTemp[rowCounter][0] = currentNode;
                    maxColumnWidths[0] = currentNode.toString().length();
                }
                for (int columnCounter = firstFreeColumn; columnCounter < columnNamesTemp.length; columnCounter++) {
                    //System.out.println("columnNames[columnCounter]: " + columnNames[columnCounter] + " : " + columnCounter);
                    if (columnCounter < columnNamesTemp.length - childColumnNames.size()) {
                        ImdiField[] currentValue = fieldsHash.get(columnNamesTemp[columnCounter]);
                        if (currentValue != null) {
                            if (currentValue.length == 1) {
                                dataTemp[rowCounter][columnCounter] = currentValue[0];
                            } else {
                                dataTemp[rowCounter][columnCounter] = currentValue;
                            }
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
            System.out.println("setting column widths: " + maxColumnWidths);
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
            columnNamesTemp = singleNodeViewHeadings;
            if (imdiObjectHash.size() == 0) {
                dataTemp = allocateCellData(0, 2);
            } else {
                Enumeration imdiRowsEnum = imdiObjectHash.elements();
                if (imdiRowsEnum.hasMoreElements()) {
                    Hashtable<String, ImdiField[]> fieldsHash = ((ImdiTreeObject) imdiRowsEnum.nextElement()).getFields();
                    // calculate the real number of rows
                    Vector<ImdiField> allRowFields = new Vector();
                    for (Enumeration<ImdiField[]> valuesEnum = fieldsHash.elements(); valuesEnum.hasMoreElements();) {
                        ImdiField[] currentFieldArray = valuesEnum.nextElement();
                        for (ImdiField currentField : currentFieldArray) {
                            allRowFields.add(currentField);
                        }
                    }
                    dataTemp = allocateCellData(allRowFields.size(), 2);
//                    Enumeration<String> labelsEnum = fieldsHash.keys();
//                    Enumeration<ImdiField[]> valuesEnum = fieldsHash.elements();
                    int rowCounter = 0;
                    for (Enumeration<ImdiField> allFieldsEnum = allRowFields.elements(); allFieldsEnum.hasMoreElements();) {
                        ImdiField currentField = allFieldsEnum.nextElement();
                        dataTemp[rowCounter][0] = currentField.getTranslateFieldName();
                        dataTemp[rowCounter][1] = currentField;
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
        Object[][] prevousData = data;
        data = dataTemp;
        if (previousColumnCount != getColumnCount() || prevousData.length != data.length) {
            fireTableStructureChanged();
        } else {
            for (int rowCounter = 0; rowCounter < getRowCount(); rowCounter++) {
                for (int colCounter = 0; colCounter < getColumnCount(); colCounter++) {
//                    if (prevousData[rowCounter][colCounter] != data[rowCounter][colCounter]) {
//                        System.out.println("fireTableCellUpdated: " + rowCounter + ":" + colCounter);
                        fireTableCellUpdated(rowCounter, colCounter);
//                    }
                }
            }
        }
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

    public int getColumnWidth(int col) {
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
            if (data[row][col] instanceof ImdiField[]) {
                boolean needsSave = false;
                ImdiField[] fieldArray = (ImdiField[]) data[row][col];
                for (ImdiField fieldElement : fieldArray) {
                    System.out.println("hasValueChanged: " + fieldElement);
                    if (fieldElement.fieldNeedsSaveToDisk) {
                        needsSave = true;
                    }
                }
                return needsSave;
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
            returnValue = ((ImdiField) data[row][col]).parentImdi.isLocal() && ((ImdiField) data[row][col]).parentImdi.isImdi();
        } else if (data[row][col] instanceof ImdiField[]) {
            returnValue = ((ImdiField[]) data[row][col])[0].parentImdi.isLocal();
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
            // multiple field colums will not be edited here or saved here
            ImdiField currentField = ((ImdiField) data[row][col]);
            currentField.setFieldValue(value.toString(), true);
            fireTableCellUpdated(row, col);
        } else if (data[row][col] instanceof Object[]) {
            System.out.println("cell is a child list so do not edit");
        } else {
            // TODO: is this even valid, presumably this will be a string and therefore not saveable to the imdi
//            data[row][col] = value;
//            fireTableCellUpdated(row, col);
        }
        fireTableCellUpdated(row, col);
    }

    public void sortByColumn(int columnIndex) {
        // TODO: sort columns
//        System.out.println("sortByColumn: " + columnIndex);
        // set the reverse sort flag
        if (sortColumn == columnIndex) {
            if (horizontalView || sortReverse == false) {
                sortReverse = !sortReverse;
            } else {
//                 set the sort by the imdi field order
                sortColumn = -1;
            }
        } else {
            //set the current sort column
            sortColumn = columnIndex;
            sortReverse = false;
        }
        System.out.println("sortByColumn: " + sortColumn);
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
