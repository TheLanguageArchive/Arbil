package nl.mpi.arbil;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.table.AbstractTableModel;

/**
 * Document   : ImdiTableModel
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
public class ImdiTableModel extends AbstractTableModel {

    private boolean showIcons = false;
    private Hashtable<String, ImdiTreeObject> imdiObjectHash = new Hashtable<String, ImdiTreeObject>();
    private HashMap<String, ImdiField> allColumnNames = new HashMap<String, ImdiField>();
    Vector childColumnNames = new Vector();
    LinorgFieldView tableFieldView;
    private int[] maxColumnWidths;
    boolean horizontalView = false;
    private int sortColumn = -1;
    private JLabel hiddenColumnsLabel;
    boolean sortReverse = false;
    DefaultListModel listModel = new DefaultListModel(); // used by the image display panel
    Vector highlightCells = new Vector();
    String[] singleNodeViewHeadings = new String[]{"IMDI Field", "Value"};

    public ImdiTableModel() {
        tableFieldView = ImdiFieldViews.getSingleInstance().getCurrentGlobalView().clone();
    }

    public void setHiddenColumnsLabel(JLabel hiddenColumnsLabelLocal) {
        hiddenColumnsLabel = hiddenColumnsLabelLocal;
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

    public boolean containsImdiNode(ImdiTreeObject findable) {
        return imdiObjectHash.contains(findable);
    }

    public int getImdiNodeCount() {
        return imdiObjectHash.size();
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
            // on start up the previous windows are loaded and the imdi nodes will not be loaded hence they will have no fields, so we have to check for that here
            if (imdiTreeObject.isDirectory() || (!imdiTreeObject.getParentDomNode().isLoading() && imdiTreeObject.getFields().size() == 0)) {
                // add child nodes if there are no fields ie actors node will add all the actors
                // add child nodes if it is a directory
                // this is non recursive and does not reload the table
                for (ImdiTreeObject currentChild : imdiTreeObject.loadChildNodes()) {
                    imdiObjectHash.put(currentChild.getUrlString(), currentChild);
                    currentChild.registerContainer(this);
                }
            } else {
                imdiObjectHash.put(imdiTreeObject.getUrlString(), imdiTreeObject);
                imdiTreeObject.registerContainer(this);
            }
        }
    }

    private ImdiTreeObject[] updateAllImdiObjects() {
        ImdiTreeObject[] returnImdiArray = new ImdiTreeObject[imdiObjectHash.size()];
        allColumnNames.clear();
        int hiddenColumnCount = 0;
        Enumeration<ImdiTreeObject> nodesEnum = imdiObjectHash.elements();
        for (int imdiArrayCounter = 0; imdiArrayCounter < returnImdiArray.length; imdiArrayCounter++) {
            if (nodesEnum.hasMoreElements()) {
                returnImdiArray[imdiArrayCounter] = nodesEnum.nextElement();
            } else {
                returnImdiArray[imdiArrayCounter] = null;
            }
            if (!returnImdiArray[imdiArrayCounter].isImdi() || returnImdiArray[imdiArrayCounter].isArchivableFile() || returnImdiArray[imdiArrayCounter].hasResource()) {
                // on application reload a file may be readded to a table before the type checker gets a chance to run, since a file must have been checked for it to get here we bypass that check at this point
//                System.out.println("Adding to jlist: " + imdiTreeObject.toString());
                if (!listModel.contains(returnImdiArray[imdiArrayCounter])) {
                    listModel.addElement(returnImdiArray[imdiArrayCounter]);
                }
            } else {
//                System.out.println("Not adding to jlist: " + imdiTreeObject.toString());
            }
//            System.out.println("isArchivableFile: " + imdiTreeObject.isArchivableFile());
//            System.out.println("hasResource: " + imdiTreeObject.hasResource());
            for (Enumeration<ImdiField[]> columnFields = returnImdiArray[imdiArrayCounter].getFields().elements(); columnFields.hasMoreElements();) {
                for (ImdiField currentField : columnFields.nextElement()) {
                    String currentColumnName = currentField.getTranslateFieldName();
                    if (tableFieldView.viewShowsColumn(currentColumnName)) {
                        if (!allColumnNames.containsValue(currentColumnName)) {
                            allColumnNames.put(currentColumnName, currentField);
                        } else {
                            // update the min id value
                            ImdiField lastStoredField = allColumnNames.get(currentColumnName);
                            if (currentField.getFieldID() != -1) {
                                if (lastStoredField.getFieldID() > currentField.getFieldID()) {
                                    allColumnNames.put(currentColumnName, currentField);
                                }
                            }
                        }
                    } else {
                        hiddenColumnCount++;
                    }
                    tableFieldView.addKnownColumn(currentColumnName);
                }
            }
        }
        if (hiddenColumnsLabel != null) {
            hiddenColumnsLabel.setVisible(hiddenColumnCount > 0);
            hiddenColumnsLabel.setText(hiddenColumnCount + " columns hidden (edit \"Column View\" in the table header to show)");
        }
        return returnImdiArray;
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

    public void copyImdiFields(ImdiField[] selectedCells, ClipboardOwner clipBoardOwner) {
        String csvSeparator = "\t"; // excel seems to work with tab but not comma 
        String copiedString = "";
        copiedString = copiedString + "\"" + singleNodeViewHeadings[0] + "\"" + csvSeparator;
        copiedString = copiedString + "\"" + singleNodeViewHeadings[1] + "\"";
        copiedString = copiedString + "\n";
        boolean isFirstCol = true;
        for (ImdiField currentField : selectedCells) {
            if (!isFirstCol) {
                copiedString = copiedString + csvSeparator;
                isFirstCol = false;
            }
            copiedString = copiedString + "\"" + currentField.getTranslateFieldName() + "\"" + csvSeparator;
            copiedString = copiedString + "\"" + currentField.getFieldValue() + "\"";
            copiedString = copiedString + "\n";
        }
        System.out.println("copiedString: " + copiedString);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection stringSelection = new StringSelection(copiedString);
        clipboard.setContents(stringSelection, clipBoardOwner);
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
        //System.out.println("copiedString: " + this.get getCellSelectionEnabled());
        System.out.println("copiedString: " + copiedString);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection stringSelection = new StringSelection(copiedString);
        clipboard.setContents(stringSelection, clipBoardOwner);
    }

    public String pasteIntoImdiFields(ImdiField[] selectedCells) {
        boolean pastedFieldOverwritten = false;
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
                    HashSet<String> pastedFieldNames = new HashSet();
                    for (int lineCounter = 1 /* skip the header */; lineCounter < clipBoardLines.length; lineCounter++) {
                        String clipBoardLine = clipBoardLines[lineCounter];
                        System.out.println("clipBoardLine: " + clipBoardLine);
                        String[] clipBoardCells = clipBoardLine.split("\\t");
                        System.out.println("clipBoardCells.length: " + clipBoardCells.length);
                        if (clipBoardCells.length != 2) {
                            resultMessage = "Inconsistent number of columns in the data to paste.\nThe pasted data could be incorrect.";
                        } else {
                            String currentFieldName = clipBoardCells[0].replaceAll(regexString, "");
                            String currentFieldValue = clipBoardCells[1].replaceAll(regexString, "");
                            if (pastedFieldNames.contains(currentFieldName)) {
                                pastedFieldOverwritten = true;
                            } else {
                                pastedFieldNames.add(currentFieldName);
                            }
                            if (selectedCells != null) {
                                for (ImdiField targetField : selectedCells) {
                                    System.out.println("targetField: " + targetField.getTranslateFieldName());
                                    if (currentFieldName.equals(targetField.getTranslateFieldName())) {
                                        targetField.setFieldValue(currentFieldValue, true);
                                        pastedCount++;
                                    }
                                }
                            }
                        }
                    }

                    if (pastedFieldOverwritten) {
                        if (resultMessage == null) {
                            resultMessage = "";
                        } else {
                            resultMessage = resultMessage + "\n";
                        }
                        resultMessage = resultMessage + "Two fields of the same name were pasted, causing at least one field to be overwritten by another";
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
                        if (new ImdiTableCellRenderer(dataTemp[rowCounter][colCounter]).getText().equals(currentText)) {
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
                String baseValueA = new ImdiTableCellRenderer(((Object[]) firstRowArray)[sortColumn]).getText();
                String comparedValueA = new ImdiTableCellRenderer(((Object[]) secondRowArray)[sortColumn]).getText();
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
//                    if (baseValueA != null && comparedValueA != null) { // if either id is null then check why it is being draw when it should be reloaded first
                    int baseIntA = ((ImdiField) ((Object[]) firstRowArray)[1]).getFieldID();
                    int comparedIntA = ((ImdiField) ((Object[]) secondRowArray)[1]).getFieldID();
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
        int[] maxColumnWidthsTemp;

        ImdiTreeObject[] tableRowsImdiArray = updateAllImdiObjects();

        // set the view to either horizontal or vertical and set the default sort
        boolean lastHorizontalView = horizontalView;
        horizontalView = tableRowsImdiArray.length > 1;
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
            ImdiField[] displayedColumnNames = allColumnNames.values().toArray(new ImdiField[allColumnNames.size()]);
            Arrays.sort(displayedColumnNames, new Comparator() {

                public int compare(Object firstColumn, Object secondColumn) {
                    try {
                        int baseIntA = ((ImdiField) firstColumn).getFieldID();
                        int comparedIntA = ((ImdiField) secondColumn).getFieldID();
                        int returnValue = baseIntA - comparedIntA;
                        return returnValue;
                    } catch (Exception ex) {
                        GuiHelper.linorgBugCatcher.logError(ex);
                        return 1;
                    }
                }
            });
            // end calculate which of the available columns to show

            // set the column offset to accomadate the icon which is not in the column hashtable
            int firstFreeColumn = 0;
            if (showIcons) {
//                System.out.println("showing icon");
                // this assumes that the icon will always be in the leftmost column
                firstFreeColumn = 1;
            }

            // create and populate the column names array and prepend the icon and append the imdinode
            columnNamesTemp = new String[displayedColumnNames.length + firstFreeColumn + childColumnNames.size()];
            int columnPopulateCounter = firstFreeColumn;
            if (columnNamesTemp.length > 0) {
                columnNamesTemp[0] = " "; // make sure the the icon column is shown its string is not null
            }
            for (ImdiField currentColumn : displayedColumnNames) {
//                System.out.println("columnPopulateCounter: " + columnPopulateCounter);
                columnNamesTemp[columnPopulateCounter] = currentColumn.getTranslateFieldName();
                columnPopulateCounter++;
            }
            // populate the child node column titles
            for (Enumeration childColEnum = childColumnNames.elements(); childColEnum.hasMoreElements();) {
                columnNamesTemp[columnPopulateCounter] = childColEnum.nextElement().toString();
                columnPopulateCounter++;
            }

            // end create the column names array and prepend the icon and append the imdinode

            maxColumnWidthsTemp = new int[columnNamesTemp.length];
            dataTemp = allocateCellData(tableRowsImdiArray.length, columnNamesTemp.length);

            int rowCounter = 0;
            for (ImdiTreeObject currentNode : tableRowsImdiArray) {
//                System.out.println("currentNode: " + currentNode.toString());
                Hashtable<String, ImdiField[]> fieldsHash = currentNode.getFields();
                if (showIcons) {
                    //data[rowCounter][0] = new JLabel(currentNode.toString(), currentNode.getIcon(), JLabel.LEFT);
                    dataTemp[rowCounter][0] = currentNode;
                    maxColumnWidthsTemp[0] = currentNode.toString().length();
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
                    if (maxColumnWidthsTemp[columnCounter] < currentLength) {
                        maxColumnWidthsTemp[columnCounter] = currentLength;
                    }
                }
                rowCounter++;
            }
//            System.out.println("setting column widths: " + maxColumnWidths);
//            // display the column names use count for testing only
//            Enumeration tempEnum = columnNameHash.elements();
//            int tempColCount = 0;
//            while (tempEnum.hasMoreElements()){
//                data[0][tempColCount] = tempEnum.nextElement().toString();
//                tempColCount++;
//            }
        } else {
            // display the single node view
            maxColumnWidthsTemp = new int[2];
            columnNamesTemp = singleNodeViewHeadings;
            if (tableRowsImdiArray.length == 0) {
                dataTemp = allocateCellData(0, 2);
            } else {
                if (tableRowsImdiArray[0] != null) {
                    Hashtable<String, ImdiField[]> fieldsHash = tableRowsImdiArray[0].getFields();
                    // calculate the real number of rows
                    Vector<ImdiField> allRowFields = new Vector();
                    for (Enumeration<ImdiField[]> valuesEnum = fieldsHash.elements(); valuesEnum.hasMoreElements();) {
                        ImdiField[] currentFieldArray = valuesEnum.nextElement();
                        for (ImdiField currentField : currentFieldArray) {
                            if (tableFieldView.viewShowsColumn(currentField.getTranslateFieldName())) {
                                allRowFields.add(currentField);
                            }
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
                        if (maxColumnWidthsTemp[0] < currentLength) {
                            maxColumnWidthsTemp[0] = currentLength;
                        }
                        currentLength = (dataTemp[rowCounter][1].toString()).length();
                        if (maxColumnWidthsTemp[1] < currentLength) {
                            maxColumnWidthsTemp[1] = currentLength;
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
        maxColumnWidths = maxColumnWidthsTemp;
        Object[][] prevousData = data;
        data = dataTemp;
        if (previousColumnCount != getColumnCount() || prevousData.length != data.length) {
            try {
                fireTableStructureChanged();
            } catch (Exception ex) {
                GuiHelper.linorgBugCatcher.logError(ex);
            }
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

    @Override
    // JTable uses this method to determine the default renderer
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        boolean returnValue = false;
        if (data[row][col] instanceof ImdiField) {
            returnValue = ((ImdiField) data[row][col]).parentImdi.isLocal() && ((ImdiField) data[row][col]).parentImdi.isImdi() && ((ImdiField) data[row][col]).fieldID != null;
        } else if (data[row][col] instanceof ImdiField[]) {
            returnValue = ((ImdiField[]) data[row][col])[0].parentImdi.isLocal() && ((ImdiField[]) data[row][col])[0].fieldID != null;
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

//    @Override
//    public void setValueAt(Object value, int row, int col) { // error here maybe return without doing anything
//        System.out.println("setValueAt: " + value.toString() + " : " + row + " : " + col);
////        if (data[row][col] instanceof ImdiField) {
////            // multiple field colums will not be edited here or saved here
////            ImdiField currentField = ((ImdiField) data[row][col]);
////            currentField.setFieldValue(value.toString(), true);
////            fireTableCellUpdated(row, col);
////        } else if (data[row][col] instanceof Object[]) {
////            System.out.println("cell is a child list so do not edit");
////        } else {
////            // TODO: is this even valid, presumably this will be a string and therefore not saveable to the imdi
//////            data[row][col] = value;
//////            fireTableCellUpdated(row, col);
////        }
////        fireTableCellUpdated(row, col);
//    }

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
        highlightCells.add(new ImdiTableCellRenderer(data[row][col]).getText());
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
