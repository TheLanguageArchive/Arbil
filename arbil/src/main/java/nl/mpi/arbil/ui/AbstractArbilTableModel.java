/**
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.arbil.ui;

import java.awt.Color;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;
import javax.swing.table.AbstractTableModel;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeArrayTableCell;
import nl.mpi.arbil.data.ArbilDataNodeContainer;
import nl.mpi.arbil.data.ArbilDataNodeTableCell;
import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.data.ArbilFieldComparator;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.data.ArbilTableCell;
import nl.mpi.arbil.data.DefaultArbilTableCell;
import nl.mpi.arbil.plugin.PluginArbilDataNode;
import nl.mpi.arbil.plugin.PluginArbilTableModel;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.NumberedStringComparator;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class AbstractArbilTableModel extends AbstractTableModel implements ArbilDataNodeContainer, PluginArbilTableModel {

    // NOTE: [] style arrays are not suitable for storing true constants
    protected final static String SINGLE_NODE_VIEW_HEADING_NAME = "Field Name";
    protected final static String SINGLE_NODE_VIEW_HEADING_VALUE = "Value";
    protected final static String[] SINGLE_NODE_VIEW_HEADINGS = new String[]{SINGLE_NODE_VIEW_HEADING_NAME, SINGLE_NODE_VIEW_HEADING_VALUE};
    private boolean showIcons = false;
    private boolean sortReverse = false;
    private HashMap<String, ArbilField> filteredColumnNames = new HashMap<String, ArbilField>();
    private ArbilFieldView tableFieldView;
    private Vector childColumnNames = new Vector();
    private boolean horizontalView = false;
    private int sortColumn = -1;
    private Vector highlightCells = new Vector();
    private String[] highFieldPaths = new String[]{};
    private String[] columnNames = new String[0];
    private Color cellColour[][] = new Color[0][0];

    public AbstractArbilTableModel(ArbilFieldView tableFieldView) {
        this.tableFieldView = tableFieldView;
    }

    public AbstractArbilTableModel() {
    }

    public void addArbilDataNodes(PluginArbilDataNode[] pluginArbilDataNodes) {
        for (int draggedCounter = 0; draggedCounter < pluginArbilDataNodes.length; draggedCounter++) {
            addArbilDataNode((ArbilDataNode) pluginArbilDataNodes[draggedCounter]);
        }
        requestReloadTableData();
    }

    public void addArbilDataNodes(ArbilDataNode[] nodesToAdd) {
        for (int draggedCounter = 0; draggedCounter < nodesToAdd.length; draggedCounter++) {
            addArbilDataNode(nodesToAdd[draggedCounter]);
        }
        requestReloadTableData();
    }

    /**
     *
     * @param nodesToAdd Enumeration of ArbilDataNodes of URIs
     */
    public void addArbilDataNodes(Enumeration nodesToAdd) {
        while (nodesToAdd.hasMoreElements()) {
            Object currentObject = nodesToAdd.nextElement();
            if (currentObject instanceof ArbilDataNode) {
                addArbilDataNode((ArbilDataNode) currentObject);
            }
        }
        requestReloadTableData();
    }

    public void addChildTypeToDisplay(String childType) {
        System.out.println("addChildTypeToDisplay: " + childType);
        getChildColumnNames().add(childType);
        requestReloadTableData();
    }

    public void addSingleArbilDataNode(ArbilDataNode arbilDataNode) {
        addArbilDataNode(arbilDataNode);
        requestReloadTableData();
    }

    public void clearCellColours() {
        getHighlightCells().clear();
        for (int rowCounter = 0; rowCounter < cellColour.length; rowCounter++) {
            for (int colCounter = 0; colCounter < cellColour[rowCounter].length; colCounter++) {
                cellColour[rowCounter][colCounter] = new Color(16777215);
            }
        }
        fireTableDataChanged();
    }

    public boolean containsArbilDataNode(ArbilDataNode findable) {
        if (findable == null) {
            return false;
        }
        return getDataNodeHash().contains(findable);
    }

    public void copyCellToColumn(int row, int col) {
        // if the col or row provided here is invalid then we want to know about it so don't try to prevent such an error
        //        if (row == -1 || col == -1) {
        //            return;
        //        }
        System.out.println("copyCellToColumn for row: " + row + " col: " + col);
        for (int rowCounter = 0; rowCounter < getRowCount(); rowCounter++) {
            if (rowCounter != row) {
                // TODO: a user may want to copy fields with multiple values to the whole column eg descritions in multiple languages
                if (getData()[rowCounter][col].getContent() instanceof ArbilField) {
                    ((ArbilField) getData()[rowCounter][col].getContent()).setFieldValue(((ArbilField) getData()[row][col].getContent()).getFieldValue(), false, false);
                }
                fireTableCellUpdated(rowCounter, col);
            }
        }
    }

    public boolean isFullyLoadedNodeRequired() {
        return true;
    }

    /**
     * Data node is clearing its icon
     *
     * @param dataNode Data node that is clearing its icon
     */
    public abstract void dataNodeIconCleared(ArbilNode dataNode);

    /**
     * Data node is to be removed from the table
     *
     * @param dataNode Data node that should be removed
     */
    public abstract void dataNodeRemoved(ArbilNode dataNode);

    /**
     * A new child node has been added to the destination node
     *
     * @param destination Node to which a node has been added
     * @param newNode The newly added node
     */
    public abstract void dataNodeChildAdded(ArbilNode destination, ArbilNode newNode);

    public int getArbilDataNodeCount() {
        return getDataNodeHash().size();
    }

    public Enumeration getArbilDataNodes() {
        return getDataNodeHash().elements();
    }

    public String[] getArbilDataNodesURLs() {
        return getDataNodeHash().keySet().toArray(new String[]{});
    }

    //    public boolean hasValueChanged(int row, int col) {
    //        if (row > -1 && col > -1) {
    //            if (data[row][col] instanceof ImdiField) {
    //                return ((ImdiField) data[row][col]).fieldNeedsSaveToDisk;
    //            }
    //            if (data[row][col] instanceof ImdiField[]) {
    //                boolean needsSave = false;
    //                ImdiField[] fieldArray = (ImdiField[]) data[row][col];
    //                for (ImdiField fieldElement : fieldArray) {
    //                    System.out.println("hasValueChanged: " + fieldElement);
    //                    if (fieldElement.fieldNeedsSaveToDisk) {
    //                        needsSave = true;
    //                    }
    //                }
    //                return needsSave;
    //            }
    //        }
    //        return false;
    //    }
    public Color getCellColour(int row, int col) {
        try {
            return cellColour[row][col];
        } catch (Exception e) {
            return new Color(16777215);
        }
    }

    public Object[] getChildNames() {
        Vector childNames = new Vector();
        Enumeration arbilRowsEnum = getDataNodeHash().elements();
        while (arbilRowsEnum.hasMoreElements()) {
            //            Enumeration childEnum = .getChildEnum();
            for (ArbilDataNode currentChild : ((ArbilDataNode) arbilRowsEnum.nextElement()).getChildArray()) {
                // TODO: maybe check the children for children before adding them to this list
                String currentChildName = currentChild.toString();
                if (!childNames.contains(currentChildName)) {
                    childNames.add(currentChildName);
                }
            }
        }
        return childNames.toArray();
    }

    // JTable uses this method to determine the default renderer
    @Override
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    public int getColumnCount() {
        return getColumnNames().length;
    }

    @Override
    public String getColumnName(int col) {
        if (col < getColumnNames().length) {
            return getColumnNames()[col];
        } else {
            return "";
        }
    }

    /**
     * @return the dataNodeHash
     */
    protected abstract Hashtable<String, ArbilDataNode> getDataNodeHash();

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

    public int getRowCount() {
        return getData().length;
    }

    public ArbilDataNode[] getSelectedDataNodes(int[] selectedRows) {
        ArbilDataNode[] selectedNodesArray = new ArbilDataNode[selectedRows.length];
        for (int selectedRowCounter = 0; selectedRowCounter < selectedRows.length; selectedRowCounter++) {
            selectedNodesArray[selectedRowCounter] = getDataNodeFromRow(selectedRows[selectedRowCounter]);
        }
        return selectedNodesArray;
    }

    public ArbilTableCell getTableCellAt(int row, int col) {
        return getData()[row][col];
    }

    public Object getValueAt(int row, int col) {
        try {
            return getTableCellAt(row, col);//.getContent();
        } catch (Exception e) {
            return null;
        }
    }

    public Object getTableCellContentAt(int row, int col) {
        Object tableCell = getValueAt(row, col);
        if (tableCell instanceof ArbilTableCell) {
            return ((ArbilTableCell) tableCell).getContent();
        } else {
            return null;
        }
    }

    public void hideColumn(int columnIndex) {
        System.out.println("hideColumn: " + columnIndex);
        // TODO: hide column
        System.out.println("hideColumn: " + getColumnName(columnIndex));
        if (!childColumnNames.remove(getColumnName(columnIndex))) {
            getFieldView().addHiddenColumn(getColumnName(columnIndex));
        }
        requestReloadTableData();
    }

    protected abstract String getRenderedText(ArbilTableCell data);

    public void highlightMatchingCells(int row, int col) {
        getHighlightCells().add(getRenderedText(getData()[row][col]));
        cellColour = setCellColours(getData());
        fireTableDataChanged();
    }

    public void highlightMatchingFieldPaths(String[] fieldPaths) {
        highFieldPaths = fieldPaths;
        cellColour = setCellColours(getData());
        fireTableDataChanged();
    }

    public void highlightMatchingText(String highlightText) {
        getHighlightCells().add(highlightText);
        cellColour = setCellColours(getData());
        fireTableDataChanged();
    }

    /**
     * @return the horizontalView
     */
    public boolean isHorizontalView() {
        return horizontalView;
    }

    public void removeAllArbilDataNodeRows() {
        for (Enumeration removableNodes = getDataNodeHash().elements(); removableNodes.hasMoreElements();) {
            ((ArbilDataNode) removableNodes.nextElement()).removeContainer(this);
        }
        clearDataNodeHash();
        filteredColumnNames.clear();
        columnNames = new String[0];
        setData(new ArbilTableCell[0][0]);
        cellColour = new Color[0][0];
        // add the icon column if icons are to be displayed
        setShowIcons(isShowIcons());
        requestReloadTableData();
    }

    protected void clearDataNodeHash() {
        getDataNodeHash().clear();
    }

    public void removeArbilDataNodeRows(int[] selectedRows) {
        ArbilDataNode[] nodesToRemove = new ArbilDataNode[selectedRows.length];
        for (int selectedRowCounter = 0; selectedRowCounter < selectedRows.length; selectedRowCounter++) {
            System.out.println("removing: " + selectedRowCounter);
            nodesToRemove[selectedRowCounter] = getDataNodeFromRow(selectedRows[selectedRowCounter]);
        }
        removeArbilDataNodes(nodesToRemove);
    }

    public void removeArbilDataNodes(ArbilDataNode[] nodesToRemove) {
        for (ArbilDataNode arbilDataNode : nodesToRemove) {
            if (arbilDataNode != null) {
                removeArbilDataNode(arbilDataNode);
            }
        }
        // refresh the table data
        requestReloadTableData();
    }

    protected void putInDataNodeHash(ArbilDataNode arbilDataNode) {
        getDataNodeHash().put(arbilDataNode.getUrlString(), arbilDataNode);
    }

    protected void removeArbilDataNode(ArbilDataNode arbilDataNode) {
        removeFromDataNodeHash(arbilDataNode);
        arbilDataNode.removeContainer(this);
    }

    protected void removeFromDataNodeHash(ArbilDataNode arbilDataNode) {
        getDataNodeHash().remove(arbilDataNode.getUrlString());
    }

    public abstract void requestReloadTableData();

    public void setShowIcons(boolean localShowIcons) {
        showIcons = localShowIcons;
    }

    public void showOnlyCurrentColumns() {
        getFieldView().setShowOnlyColumns(getColumnNames());
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
            if (isHorizontalView() || sortReverse == false) {
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
        requestReloadTableData();
    }

    public void setCurrentView(ArbilFieldView localFieldView) {
        ArbilFieldView tempFieldView = localFieldView.clone();
        for (Enumeration oldKnowenColoumns = getFieldView().getKnownColumns(); oldKnowenColoumns.hasMoreElements();) {
            tempFieldView.addKnownColumn(oldKnowenColoumns.nextElement().toString());
        }
        tableFieldView = tempFieldView;
        requestReloadTableData();
    }

    protected void addArbilDataNode(ArbilDataNode arbilDataNode) {
        if (arbilDataNode != null) {
            // on start up the previous windows are loaded and the nodes will not be loaded hence they will have no fields, so we have to check for that here
            if (arbilDataNode.isDirectory() || (!arbilDataNode.getParentDomNode().isLoading() && arbilDataNode.isEmptyMetaNode())) {
                // add child nodes if there are no fields ie actors node will add all the actors
                // add child nodes if it is a directory
                // this is non recursive and does not reload the table
                for (ArbilDataNode currentChild : arbilDataNode.getChildArray()) {
                    putInDataNodeHash(currentChild);
                    currentChild.registerContainer(this);
                }
            } else {
                putInDataNodeHash(arbilDataNode);
                arbilDataNode.registerContainer(this);
            }
        }
    }

    protected ArbilDataNode getDataNodeFromRow(int rowNumber) {
        // TODO: find error removing rows // look again here...
        // if that the first column is the imdi node (ergo string and icon) use that to remove the row
        if (getData()[rowNumber][0].getContent() instanceof ArbilDataNode) {
            return (ArbilDataNode) getData()[rowNumber][0].getContent();
        } else if (getData()[rowNumber][0].getContent() instanceof ArbilField[]) {
            return ((ArbilField[]) getData()[rowNumber][getColumnNames().length - 1].getContent())[0].getParentDataNode();
        } else {
            // in the case that the icon and sting are not displayed then try to get the imdifield in order to get the imdinode
            // TODO: this will fail if the imdiobject for the row does not have a field to display for the first column because there will be no imdi nor field in the first coloumn
            return ((ArbilField) getData()[rowNumber][getColumnNames().length - 1].getContent()).getParentDataNode();
            //throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    // this will be hit by each imdi node in the table when the application starts hence it needs to be either put in a queue or be synchronised
    protected synchronized void reloadTableDataPrivate() { // with the queue this does not need to be synchronised but in this case it will not slow things too much
        int previousColumnCount = getColumnCount();

        ArbilDataNode[] tableRowsArbilArray = updateAllDataNodes();

        updateViewOrientation(tableRowsArbilArray);
        initTableData(tableRowsArbilArray, previousColumnCount);
    }

    private ArbilDataNode[] updateAllDataNodes() {
        ArbilDataNode[] returnArray = getDataNodeHash().values().toArray(new ArbilDataNode[]{});
        filteredColumnNames.clear();
        Set<String> hiddenColumns = new HashSet<String>();
        int hiddenCellsCount = 0;
        for (ArbilDataNode currentRowNode : returnArray) {
            for (ArbilField[] currentFieldArray : currentRowNode.getFields().values().toArray(new ArbilField[][]{})) {
                for (ArbilField currentField : currentFieldArray) {
                    String currentColumnName = currentField.getTranslateFieldName();
                    if (getFieldView().viewShowsColumn(currentColumnName)) {
                        if (!filteredColumnNames.containsKey(currentColumnName)) {
                            filteredColumnNames.put(currentColumnName, currentField);
                        } else {
                            // update the min id value
                            ArbilField lastStoredField = filteredColumnNames.get(currentColumnName);
                            if (currentField.getFieldOrder() != -1) {
                                if (lastStoredField.getFieldOrder() > currentField.getFieldOrder()) {
                                    filteredColumnNames.put(currentColumnName, currentField);
                                }
                            }
                        }
                    } else {
                        hiddenColumns.add(currentColumnName);
                        hiddenCellsCount++;
                    }
                    getFieldView().addKnownColumn(currentColumnName);
                }
            }
        }
        updateHiddenColumnsLabel(hiddenColumns.size(), hiddenCellsCount);
        return returnArray;
    }

    protected abstract void updateHiddenColumnsLabel(int hiddenColumnCount, int hiddenCellsCount);

    private void updateViewOrientation(ArbilDataNode[] tableRowsArbilArray) {
        // set the view to either horizontal or vertical and set the default sort
        boolean lastHorizontalView = isHorizontalView();
        horizontalView = tableRowsArbilArray.length > 1;
        if (!isHorizontalView()) {
            // set the table for a single image if that is all that is shown
            //if (imdiObjectHash.size() == listModel.getSize()) { // TODO: this does not account for when a resource is shown
            // TODO: this does not account for when a resource is shown
            if (filteredColumnNames.isEmpty()) {
                horizontalView = true;
            }
        }
        if (lastHorizontalView != isHorizontalView()) {
            sortReverse = false;
            // update to the default sort
            sortColumn = isHorizontalView() ? 0 : -1;
        }
    }

    private void initTableData(ArbilDataNode[] tableRowsArbilArray, int previousColumnCount) {
        String[] columnNamesTemp; // will contain translated field names (for column headers)
        String[] fieldNames; // will contain actual field names
        ArbilTableCell[][] newData;

        if (isHorizontalView()) {
            // display the grid view
            // calculate which of the available columns to show
            ArbilField[] displayedColumnNames = filteredColumnNames.values().toArray(new ArbilField[filteredColumnNames.size()]);
            Arrays.sort(displayedColumnNames, new ArbilFieldComparator());
            // end calculate which of the available columns to show
            // set the column offset to accomadate the icon which is not in the column hashtable
            int firstFreeColumn = 0;
            if (isShowIcons()) {
                //                System.out.println("showing icon");
                // this assumes that the icon will always be in the leftmost column
                firstFreeColumn = 1;
            }
            columnNamesTemp = new String[displayedColumnNames.length + firstFreeColumn + getChildColumnNames().size()];
            fieldNames = new String[displayedColumnNames.length + firstFreeColumn + getChildColumnNames().size()];
            int columnPopulateCounter = firstFreeColumn;
            if (columnNamesTemp.length > 0) {
                columnNamesTemp[0] = " "; // make sure the the icon column is shown its string is not null
            }
            for (ArbilField currentColumn : displayedColumnNames) {
                //                System.out.println("columnPopulateCounter: " + columnPopulateCounter);
                fieldNames[columnPopulateCounter] = currentColumn.xmlPath;
                columnNamesTemp[columnPopulateCounter] = currentColumn.getTranslateFieldName();
                columnPopulateCounter++;
            }
            // populate the child node column titles
            for (Enumeration childColEnum = getChildColumnNames().elements(); childColEnum.hasMoreElements();) {
                columnNamesTemp[columnPopulateCounter] = childColEnum.nextElement().toString();
                columnPopulateCounter++;
            }
            // end create the column names array and prepend the icon and append the imdinode
            newData = allocateCellData(tableRowsArbilArray.length, columnNamesTemp.length);

            int rowCounter = 0;
            final int childColumnsIndex = columnNamesTemp.length - getChildColumnNames().size();
            for (ArbilDataNode currentNode : tableRowsArbilArray) {
                //                System.out.println("currentNode: " + currentNode.toString());
                Hashtable<String, ArbilField[]> fieldsHash = currentNode.getFields();
                if (isShowIcons()) {
                    // First column contains node icon
                    newData[rowCounter][0] = new ArbilDataNodeTableCell(currentNode);
                }
                for (int columnCounter = firstFreeColumn; columnCounter < columnNamesTemp.length; columnCounter++) {
                    if (columnCounter < childColumnsIndex) {
                        if (fieldsHash.containsKey(columnNamesTemp[columnCounter])) {
                            ArbilField[] currentValue = fieldsHash.get(columnNamesTemp[columnCounter]);
                            if (currentValue.length == 1) {
                                newData[rowCounter][columnCounter] = new DefaultArbilTableCell(currentValue[0]);
                            } else {
                                newData[rowCounter][columnCounter] = new DefaultArbilTableCell(currentValue);
                            }
                        } else {
                            // Field does not exist for node. Insert field placeholder, so that upon editing request the field name
                            // can be resolved (and checked whether the field is actually addable)
                            newData[rowCounter][columnCounter] = new DefaultArbilTableCell(new ArbilFieldPlaceHolder(fieldNames[columnCounter], currentNode));
                        }
                    } else {
                        final ArbilDataNode[] childNodes = currentNode.getChildNodesArray(columnNamesTemp[columnCounter]);
                        if (childNodes != null) {
                            // populate the cell with any the child nodes for the current child nodes column
                            newData[rowCounter][columnCounter] = new ArbilDataNodeArrayTableCell(childNodes);
                        }
                        // prevent null values
                        if (newData[rowCounter][columnCounter] == null) {
                            newData[rowCounter][columnCounter] = new DefaultArbilTableCell("");
                        }
                    }
                }
                rowCounter++;
            }
            //            System.out.println("setting column widths: " + maxColumnWidthsTemp);
            //            // display the column names use count for testing only
            //            Enumeration tempEnum = columnNameHash.elements();
            //            int tempColCount = 0;
            //            while (tempEnum.hasMoreElements()){
            //                data[0][tempColCount] = tempEnum.nextElement().toString();
            //                tempColCount++;
            //            }
            //            System.out.println("setting column widths: " + maxColumnWidthsTemp);
            //            // display the column names use count for testing only
            //            Enumeration tempEnum = columnNameHash.elements();
            //            int tempColCount = 0;
            //            while (tempEnum.hasMoreElements()){
            //                data[0][tempColCount] = tempEnum.nextElement().toString();
            //                tempColCount++;
            //            }
        } else {
            // display the single node view
            columnNamesTemp = SINGLE_NODE_VIEW_HEADINGS;
            if (tableRowsArbilArray.length == 0) {
                newData = allocateCellData(0, columnNamesTemp.length);
            } else {
                if (tableRowsArbilArray[0] != null) {
                    Hashtable<String, ArbilField[]> fieldsHash = tableRowsArbilArray[0].getFields();
                    // calculate the real number of rows
                    Vector<ArbilField> allRowFields = new Vector();
                    for (Enumeration<ArbilField[]> valuesEnum = fieldsHash.elements(); valuesEnum.hasMoreElements();) {
                        ArbilField[] currentFieldArray = valuesEnum.nextElement();
                        for (ArbilField currentField : currentFieldArray) {
                            if (currentField.xmlPath.length() > 0) {
                                // prevent non fields being displayed
                                if (getFieldView().viewShowsColumn(currentField.getTranslateFieldName())) {
                                    allRowFields.add(currentField);
                                }
                            }
                        }
                    }
                    newData = allocateCellData(allRowFields.size(), 2);
                    //                    Enumeration<String> labelsEnum = fieldsHash.keys();
                    //                    Enumeration<ImdiField[]> valuesEnum = fieldsHash.elements();
                    int rowCounter = 0;
                    for (Enumeration<ArbilField> allFieldsEnum = allRowFields.elements(); allFieldsEnum.hasMoreElements();) {
                        ArbilField currentField = allFieldsEnum.nextElement();
                        newData[rowCounter][0] = new DefaultArbilTableCell(currentField.getTranslateFieldName());
                        newData[rowCounter][1] = new DefaultArbilTableCell(currentField);
                        rowCounter++;
                    }
                } else {
                    newData = new ArbilTableCell[0][0];
                }
            }
        }
        // update the table model, note that this could be more specific, ie. just row or all it the columns have changed
        //fireTableDataChanged();
        sortTableRows(columnNamesTemp, newData);
        columnNames = columnNamesTemp;
        cellColour = setCellColours(newData);
        Object[][] prevousData = getData();
        setData(newData);
        if (previousColumnCount != getColumnCount() || prevousData.length != getData().length) {
            try {
                fireTableStructureChanged();
            } catch (Exception ex) {
                BugCatcherManager.getBugCatcher().logError(ex);
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

    private void sortTableRows(String[] columnNamesTemp, Object[][] dataTemp) {
//        System.out.println("sortTableRows");
        if (sortColumn < columnNamesTemp.length) {
            Arrays.sort(dataTemp, new TableRowComparator(sortColumn, sortReverse));
        }
    }

    protected Color[][] setCellColours(ArbilTableCell[][] dataTemp) {
        Color[][] cellColourTemp;
        if (dataTemp.length == 0) {
            cellColourTemp = new Color[0][0];
        } else {
            cellColourTemp = new Color[dataTemp.length][dataTemp[0].length];
            Color currentHighlightColur = new Color(0xFFFFFF);
            for (Enumeration currentHighlight = getHighlightCells().elements(); currentHighlight.hasMoreElements();) {
                // rotate the next colour
                currentHighlightColur = new Color(currentHighlightColur.getGreen() - 40, currentHighlightColur.getRed(), currentHighlightColur.getBlue());
                String currentText = currentHighlight.nextElement().toString();
                // search the table for matching cells
                for (int rowCounter = 0; rowCounter < dataTemp.length; rowCounter++) {
                    for (int colCounter = 0; colCounter < dataTemp[rowCounter].length; colCounter++) {
                        if (getRenderedText(dataTemp[rowCounter][colCounter]).equals(currentText)) {
                            cellColourTemp[rowCounter][colCounter] = currentHighlightColur;
                        }
                    }
                }
            }
            for (String currentFeildPath : highFieldPaths) {
                if (currentFeildPath != null) {
                    for (int rowCounter = 0; rowCounter < dataTemp.length; rowCounter++) {
                        for (int colCounter = 0; colCounter < dataTemp[rowCounter].length; colCounter++) {
                            if (dataTemp[rowCounter][colCounter] != null && dataTemp[rowCounter][colCounter].getContent() instanceof ArbilField) {
                                String fullXmlPath = ((ArbilField) dataTemp[rowCounter][colCounter].getContent()).getFullXmlPath();
                                if (fullXmlPath != null && fullXmlPath.equals(currentFeildPath)
                                        || ((ArbilField) dataTemp[rowCounter][colCounter].getContent()).getFullXmlPath().equals(currentFeildPath.replaceFirst("\\(1\\)$", ""))) {
                                    cellColourTemp[rowCounter][colCounter] = new Color(0xDDCCFF);
//                                if (dataTemp[rowCounter][0] instanceof String)
                                }
                            }
                        }
                    }
                }
            }
        }
        return cellColourTemp;
    }

    protected ArbilTableCell[][] allocateCellData(int rows, int cols) {
        ArbilTableCell[][] dataTemp = new ArbilTableCell[rows][cols];
        return dataTemp;
    }

    /**
     * @return the data
     */
    protected abstract ArbilTableCell[][] getData();

    /**
     * @param data the data to set
     */
    protected abstract void setData(ArbilTableCell[][] data);

    /**
     * @return the childColumnNames
     */
    protected Vector getChildColumnNames() {
        return childColumnNames;
    }

    /**
     * @return the highlightCells
     */
    protected Vector getHighlightCells() {
        return highlightCells;
    }

    /**
     * @return the columnNames
     */
    protected String[] getColumnNames() {
        return columnNames;
    }

    /**
     * @return the showIcons
     */
    protected boolean isShowIcons() {
        return showIcons;
    }

    /**
     * @return the table's FieldView
     */
    public ArbilFieldView getFieldView() {
        return tableFieldView;
    }

    private String getOriginalValue(ArbilTableCell data) {
        Object content = data.getContent();
        if (content instanceof ArbilField) {
            return ((ArbilField) content).originalFieldValue;
        }
        return getRenderedText(data);
    }

    /**
     * @return the sortReverse
     */
    protected boolean isSortReverse() {
        return sortReverse;
    }

    /**
     * @return the sortColumn
     */
    protected int getSortColumn() {
        return sortColumn;
    }

//    private class TableRowComparator implements Comparator<ImdiField[]> {
    private class TableRowComparator extends NumberedStringComparator implements Serializable {

        int sortColumn = 0;
        boolean sortReverse = false;

        public TableRowComparator(int tempSortColumn, boolean tempSortReverse) {
            sortColumn = tempSortColumn;
            sortReverse = tempSortReverse;
//            System.out.println("TableRowComparator: " + sortColumn + ":" + sortReverse);
        }

        //public int compare(ImdiField[] firstRowArray, ImdiField[] secondRowArray) {
        public int compare(Object firstRowArray, Object secondRowArray) {
            if (sortColumn >= 0) { // Sorted by actual values in a column
                // (done by setting when the hor ver setting changes) need to add a check for horizontal view and -1 which is invalid
                String baseValueA = getOriginalValue(((ArbilTableCell[]) firstRowArray)[sortColumn]).toLowerCase();
                String comparedValueA = getOriginalValue(((ArbilTableCell[]) secondRowArray)[sortColumn]).toLowerCase();

                // TODO: add the second or more sort column
//            if (!(baseValueA.equals(comparedValueA))) {
//                return baseValueB.compareTo(comparedValueB);
//            } else {
//                return baseValueA.compareTo(comparedValueA);
//            }
                Integer returnValue = compareNumberedStrings(baseValueA, comparedValueA);
                if (returnValue == null) {
                    returnValue = baseValueA.compareTo(comparedValueA);
                }
                if (sortReverse) {
                    returnValue = 1 - returnValue;
                }
                return returnValue;
            } else { // Sorted by field order
                try {
//                    if (baseValueA != null && comparedValueA != null) { // if either id is null then check why it is being draw when it should be reloaded first
                    int baseIntA = ((ArbilField) ((ArbilTableCell[]) firstRowArray)[1].getContent()).getFieldOrder();
                    int comparedIntA = ((ArbilField) ((ArbilTableCell[]) secondRowArray)[1].getContent()).getFieldOrder();
                    int returnValue = baseIntA - comparedIntA;
                    if (returnValue == 0) {
                        // if the xml node order is the same then also sort on the strings
                        String baseStrA = ((ArbilField) ((ArbilTableCell[]) firstRowArray)[1].getContent()).getFieldValue();
                        String comparedStrA = ((ArbilField) ((ArbilTableCell[]) secondRowArray)[1].getContent()).getFieldValue();
                        returnValue = baseStrA.compareToIgnoreCase(comparedStrA);
                    }
                    return returnValue;
//                    } else {
//                        return 0;
//                    }
                } catch (Exception ex) {
                    BugCatcherManager.getBugCatcher().logError(ex);
                    return 1;
                }
            }
        }
    }
}
