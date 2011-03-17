package nl.mpi.arbil.ui;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.net.URI;
import java.util.ArrayList;
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
import nl.mpi.arbil.ArbilVersion;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeContainer;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.data.ArbilFieldComparator;
import nl.mpi.arbil.util.ArbilActionBuffer;
import nl.mpi.arbil.util.BugCatcher;
import nl.mpi.arbil.util.MessageDialogHandler;

/**
 * Document   : ArbilTableModel
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
public class ArbilTableModel extends AbstractTableModel implements ArbilDataNodeContainer {

    private static MessageDialogHandler messageDialogHandler;

    public static void setMessageDialogHandler(MessageDialogHandler handler) {
        messageDialogHandler = handler;
    }
    private static BugCatcher bugCatcher;

    public static void setBugCatcher(BugCatcher bugCatcherInstance) {
        bugCatcher = bugCatcherInstance;
    }
    private static ClipboardOwner clipboardOwner;

    public static void setClipboardOwner(ClipboardOwner clipboardOwnerInstance) {
        clipboardOwner = clipboardOwnerInstance;
    }
    // variables used by the thread
    private boolean reloadRequested = false;
    private boolean treeNodeSortQueueRunning = false;
    // end variables used by the thread
    private boolean showIcons = false;
    private Hashtable<String, ArbilDataNode> dataNodeHash = new Hashtable<String, ArbilDataNode>();
    private HashMap<String, ArbilField> filteredColumnNames = new HashMap<String, ArbilField>();
    private Vector childColumnNames = new Vector();
    private ArbilFieldView tableFieldView;
    private boolean horizontalView = false;
    private int sortColumn = -1;
    private JLabel hiddenColumnsLabel;
    public boolean hideContextMenuAndStatusBar;
    boolean sortReverse = false;
    private DefaultListModel listModel = new DefaultListModel(); // used by the image display panel
    private Vector highlightCells = new Vector();
    private String[] highFieldPaths = new String[]{};
    private String[] singleNodeViewHeadings = new String[]{"Field Name", "Value"};
    private String[] columnNames = new String[0];
    private Object[][] data = new Object[0][0];
    private Color cellColour[][] = new Color[0][0];

    public ArbilTableModel() {
        tableFieldView = ArbilFieldViews.getSingleInstance().getCurrentGlobalView().clone();
    }

    public void setHiddenColumnsLabel(JLabel hiddenColumnsLabelLocal) {
        hiddenColumnsLabel = hiddenColumnsLabelLocal;
    }

    public DefaultListModel getListModel(ArbilSplitPanel arbilSplitPanel) {
        ArbilListDataListener listDataListener = new ArbilListDataListener(arbilSplitPanel);
        listModel.addListDataListener(listDataListener);
        return listModel;
    }
    // end code related to the list display of resources and loose files

    public void setCurrentView(ArbilFieldView localFieldView) {
        ArbilFieldView tempFieldView = localFieldView.clone();
        for (Enumeration oldKnowenColoumns = tableFieldView.getKnownColumns(); oldKnowenColoumns.hasMoreElements();) {
            tempFieldView.addKnownColumn(oldKnowenColoumns.nextElement().toString());
        }
        tableFieldView = tempFieldView;
        requestReloadTableData();
    }

    public ArbilDataNode[] getSelectedDataNodes(int[] selectedRows) {
        ArbilDataNode[] selectedNodesArray = new ArbilDataNode[selectedRows.length];
        for (int selectedRowCounter = 0; selectedRowCounter < selectedRows.length; selectedRowCounter++) {
            selectedNodesArray[selectedRowCounter] = getDataNodeFromRow(selectedRows[selectedRowCounter]);
        }
        return selectedNodesArray;
    }

    public boolean containsArbilDataNode(ArbilDataNode findable) {
        if (findable == null) {
            return false;
        }
        return dataNodeHash.contains(findable);
    }

    public int getArbilDataNodeCount() {
        return dataNodeHash.size();
    }

    public Enumeration getArbilDataNodes() {
        return dataNodeHash.elements();
    }

    public String[] getArbilDataNodesURLs() {
        return dataNodeHash.keySet().toArray(new String[]{});
    }

    public void setShowIcons(boolean localShowIcons) {
        showIcons = localShowIcons;
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
            } else if (currentObject instanceof URI) {
                addArbilDataNode((URI) currentObject);
            }
        }
        requestReloadTableData();
    }

    public void addSingleArbilDataNode(ArbilDataNode arbilDataNode) {
        addArbilDataNode(arbilDataNode);
        requestReloadTableData();
    }

    public void addArbilDataNode(URI nodeURI) {
        addArbilDataNode(ArbilDataNodeLoader.getSingleInstance().getArbilDataNode(null, nodeURI));
    }

    private void addArbilDataNode(ArbilDataNode arbilDataNode) {
        if (arbilDataNode != null) {
            // on start up the previous windows are loaded and the nodes will not be loaded hence they will have no fields, so we have to check for that here
            if (arbilDataNode.isDirectory() || (!arbilDataNode.getParentDomNode().isLoading() && arbilDataNode.isEmptyMetaNode())) {
                // add child nodes if there are no fields ie actors node will add all the actors
                // add child nodes if it is a directory
                // this is non recursive and does not reload the table
                for (ArbilDataNode currentChild : arbilDataNode.getChildArray()) {
                    dataNodeHash.put(currentChild.getUrlString(), currentChild);
                    currentChild.registerContainer(this);
                }
            } else {
                dataNodeHash.put(arbilDataNode.getUrlString(), arbilDataNode);
                arbilDataNode.registerContainer(this);
            }
        }
    }

    private ArbilDataNode[] updateAllDataNodes() {
        ArbilDataNode[] returnArray = dataNodeHash.values().toArray(new ArbilDataNode[]{});
        filteredColumnNames.clear();
        int hiddenColumnCount = 0;
        for (ArbilDataNode currentRowNode : returnArray) {
            for (ArbilField[] currentFieldArray : currentRowNode.getFields().values().toArray(new ArbilField[][]{})) {
                for (ArbilField currentField : currentFieldArray) {
                    String currentColumnName = currentField.getTranslateFieldName();
                    if (tableFieldView.viewShowsColumn(currentColumnName)) {
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
                        hiddenColumnCount++;
                    }
                    tableFieldView.addKnownColumn(currentColumnName);
                }
            }
        }
        if (hiddenColumnsLabel != null) {
            hiddenColumnsLabel.setVisible(!hideContextMenuAndStatusBar && hiddenColumnCount > 0);
            hiddenColumnsLabel.setText(hiddenColumnCount + " columns hidden (edit \"Column View\" in the table header to show)");
        }
        return returnArray;
    }

    private void updateImageDisplayPanel() {
        listModel.removeAllElements();
        ImageBoxRenderer tempImageBoxRenderer = new ImageBoxRenderer();
        for (int rowCounter = 0; rowCounter < data.length; rowCounter++) {
            ArbilDataNode currentRowDataNode = getDataNodeFromRow(rowCounter);
            if (currentRowDataNode != null) {
                if (tempImageBoxRenderer.canDisplay(currentRowDataNode)) {
                    if (!listModel.contains(currentRowDataNode)) {
                        listModel.addElement(currentRowDataNode);
                    }
                }
            }
        }
    }

    public void removeAllArbilDataNodeRows() {
        listModel.removeAllElements();
        for (Enumeration removableNodes = dataNodeHash.elements(); removableNodes.hasMoreElements();) {
            ((ArbilDataNode) removableNodes.nextElement()).removeContainer(this);
        }
        dataNodeHash.clear();
        filteredColumnNames.clear();
        columnNames = new String[0];
        data = new Object[0][0];
        cellColour = new Color[0][0];
        // add the icon column if icons are to be displayed
        setShowIcons(showIcons);
        requestReloadTableData();
    }

    private ArbilDataNode getDataNodeFromRow(int rowNumber) {
        // TODO: find error removing rows // look again here...
        // if that the first column is the imdi node (ergo string and icon) use that to remove the row
        if (data[rowNumber][0] instanceof ArbilDataNode) {
            return (ArbilDataNode) data[rowNumber][0];
        } else if (data[rowNumber][0] instanceof ArbilField[]) {
            return ((ArbilField[]) data[rowNumber][columnNames.length - 1])[0].parentDataNode;
        } else {
            // in the case that the icon and sting are not displayed then try to get the imdifield in order to get the imdinode
            // TODO: this will fail if the imdiobject for the row does not have a field to display for the first column because there will be no imdi nor field in the first coloumn
            return ((ArbilField) data[rowNumber][columnNames.length - 1]).parentDataNode;
            //throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public void removeArbilDataNodeRows(int[] selectedRows) {
        ArbilDataNode[] nodesToRemove = new ArbilDataNode[selectedRows.length];
        for (int selectedRowCounter = 0; selectedRowCounter < selectedRows.length; selectedRowCounter++) {
            System.out.println("removing: " + selectedRowCounter);
            nodesToRemove[selectedRowCounter] = getDataNodeFromRow(selectedRows[selectedRowCounter]);
        }
        removeArbilDataNodes(nodesToRemove);
    }

    // utility to join an array to a comma separated string
    private String joinArray(Object[] arrayToJoin) {
        String joinedString = "";
        for (Object currentArrayItem : arrayToJoin) {
            joinedString = joinedString + "," + currentArrayItem.toString();
        }
        if (joinedString.length() > 1) {
            joinedString = joinedString.substring(1);
        }
        return joinedString;
    }

    public void copyHtmlEmbedTagToClipboard(int tableHeight, int tableWidth) {
        try {
            ArbilVersion arbilVersion = new ArbilVersion();
            // TODO: the clas path specified here needs to be dynamically generated
            String embedTagString = "<APPLET CODEBASE=\"http://www.mpi.nl/tg/j2se/jnlp/arbil/\" CODE=\"nl.mpi.arbil.ArbilTableApplet.class\" ARCHIVE=\"arbil-" + arbilVersion.currentMajor + "-" + arbilVersion.currentMinor + "-" + arbilVersion.currentRevision + ".jar,lib/corpusstructure-1.6.1.jar,lib/imdiapi-1.0.6.jar,lib/log4j-1.2.14.jar,lib/saxon8.jar,lib/saxon8-dom.jar,lib/typecheck-1.5.16185.jar,lib/xalan-2.6.0.jar,lib/xercesImpl-2.9.0.jar\"";
            embedTagString = embedTagString + " WIDTH=" + tableWidth + " HEIGHT=" + tableHeight + " >\n";
            embedTagString = embedTagString + "  <PARAM NAME=\"ImdiFileList\" VALUE=\"" + joinArray(this.getArbilDataNodesURLs()) + "\">\n";
            embedTagString = embedTagString + "  <PARAM NAME=\"ShowOnlyColumns\" VALUE=\"" + joinArray(this.columnNames) + "\">\n";
            embedTagString = embedTagString + "  <PARAM NAME=\"ChildNodeColumns\" VALUE=\"" + joinArray(this.childColumnNames.toArray()) + "\">\n";
            embedTagString = embedTagString + "  <PARAM NAME=\"HighlightText\" VALUE=\"" + joinArray(this.highlightCells.toArray()) + "\">\n";
            embedTagString = embedTagString + "</APPLET>";
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            StringSelection stringSelection = new StringSelection(embedTagString);
            clipboard.setContents(stringSelection, clipboardOwner);
        } catch (Exception ex) {
            bugCatcher.logError(ex);
        }
    }

    public void copyArbilFields(ArbilField[] selectedCells) {
        String csvSeparator = "\t"; // excel seems to work with tab but not comma 
        String copiedString = "";
        copiedString = copiedString + "\"" + singleNodeViewHeadings[0] + "\"" + csvSeparator;
        copiedString = copiedString + "\"" + singleNodeViewHeadings[1] + "\"";
        copiedString = copiedString + "\n";
        boolean isFirstCol = true;
        for (ArbilField currentField : selectedCells) {
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
        clipboard.setContents(stringSelection, clipboardOwner);
    }

    public void copyArbilRows(int[] selectedRows) {
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
        clipboard.setContents(stringSelection, clipboardOwner);
    }

    public String pasteIntoArbilFields(ArbilField[] selectedCells) {
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
            String[] clipBoardLines = clipBoardString.split("\"(\r?\n|\r)\"");
            if (clipBoardLines.length == 1) {
                // re try in case the csv text is not quoted
                clipBoardLines = clipBoardString.split("\r?\n|\r");
            }
            if (clipBoardLines.length == 1) {
                String messageString = selectedCells.length + " fields will be overwritten with the single value on the clipboard.\nContinue?";
                if (messageDialogHandler.showConfirmDialogBox(messageString, "Paste")) {
                    for (ArbilField targetField : selectedCells) {
                        targetField.setFieldValue(clipBoardString, true, false);
                        pastedCount++;
                    }
                } else {
                    return null;
                }
            } else if (clipBoardLines.length > 1) {
                String areYouSureMessageString = "";
                ArrayList<Object[]> pasteList = new ArrayList<Object[]>();
                int deletingValuesCounter = 0;
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
                    resultMessage = "Incorrect data to paste.\nThe data must be copied either from a table where only one IMDI file is displayed\nor by selecting individual cells in the table.";
                }
                if (singleNodeAxis) {
                    boolean pasteOneFieldToAll = clipBoardLines.length == 2; /* skip the header */
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
                                for (ArbilField targetField : selectedCells) {
                                    System.out.println("targetField: " + targetField.getTranslateFieldName());
                                    //messagebox "The copied field name does not match the destination, do you want to paste anyway?"
                                    if (currentFieldName.equals(targetField.getTranslateFieldName()) || pasteOneFieldToAll) {
                                        if (currentFieldValue.trim().length() == 0 && targetField.getFieldValue().trim().length() > 0) {
                                            deletingValuesCounter++;
                                        }
                                        pasteList.add(new Object[]{targetField, currentFieldValue});
                                    }
                                }
                            }
                        }
                    }
                    if (pastedFieldOverwritten) {
                        areYouSureMessageString = areYouSureMessageString + "Two fields of the same name are to be pasted into this table,\nthis will cause at least one field to be overwritten by another.\n\n";
                    }
                    if (deletingValuesCounter > 0) {
                        areYouSureMessageString = areYouSureMessageString + "There are " + deletingValuesCounter + " fields that will have their contents deleted by this paste action.\n\n";
                    }
                    if (areYouSureMessageString.length() > 0) {
                        if (!messageDialogHandler.showConfirmDialogBox(areYouSureMessageString + "Continue?", "Paste")) {
                            return null;
                        }
                    }
                    for (Object[] pasteListObject : pasteList) {
                        ArbilField currentField = (ArbilField) pasteListObject[0];
                        String currentValue = (String) pasteListObject[1];
                        currentField.setFieldValue(currentValue, true, false);
                        pastedCount++;
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
            bugCatcher.logError(ex);
        }
        return resultMessage;
    }

    public void removeArbilDataNodes(ArbilDataNode[] nodesToRemove) {
        for (ArbilDataNode arbilDataNode : nodesToRemove) {
            if (arbilDataNode != null) {
                System.out.println("removing: " + arbilDataNode.toString());
                listModel.removeElement(arbilDataNode);
                dataNodeHash.remove(arbilDataNode.getUrlString());
                arbilDataNode.removeContainer(this);
            }
        }
        // refresh the table data
        requestReloadTableData();
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
                        if (new ArbilTableCellRenderer(dataTemp[rowCounter][colCounter]).getText().equals(currentText)) {
                            cellColourTemp[rowCounter][colCounter] = currentHighlightColur;
                        }
                    }
                }
            }
            for (String currentFeildPath : highFieldPaths) {
                if (currentFeildPath != null) {
                    for (int rowCounter = 0; rowCounter < dataTemp.length; rowCounter++) {
                        for (int colCounter = 0; colCounter < dataTemp[rowCounter].length; colCounter++) {
                            if (dataTemp[rowCounter][colCounter] instanceof ArbilField && dataTemp[rowCounter][colCounter] != null) {
                                String fullXmlPath = ((ArbilField) dataTemp[rowCounter][colCounter]).getFullXmlPath();
                                if (fullXmlPath != null && fullXmlPath.equals(currentFeildPath)
                                        || ((ArbilField) dataTemp[rowCounter][colCounter]).getFullXmlPath().equals(currentFeildPath.replaceFirst("\\(1\\)$", ""))) {
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

    private Object[][] allocateCellData(int rows, int cols) {
        Object[][] dataTemp = new Object[rows][cols];
        return dataTemp;
    }

    /**
     * @return the horizontalView
     */
    public boolean isHorizontalView() {
        return horizontalView;
    }

    /**
     * Data node is to be removed from the table
     * @param dataNode Data node that should be removed
     */
    public void dataNodeRemoved(ArbilDataNode dataNode) {
        removeArbilDataNodes(new ArbilDataNode[]{dataNode});
    }

    /**
     * Data node is clearing its icon
     * @param dataNode Data node that is clearing its icon
     */
    public void dataNodeIconCleared(ArbilDataNode dataNode) {
        requestReloadTableData();
    }

//    private class TableRowComparator implements Comparator<ImdiField[]> {
    private class TableRowComparator implements Comparator {

        int sortColumn = 0;
        boolean sortReverse = false;

        public TableRowComparator(int tempSortColumn, boolean tempSortReverse) {
            sortColumn = tempSortColumn;
            sortReverse = tempSortReverse;
//            System.out.println("TableRowComparator: " + sortColumn + ":" + sortReverse);
        }

        //public int compare(ImdiField[] firstRowArray, ImdiField[] secondRowArray) {
        public int compare(Object firstRowArray, Object secondRowArray) {
            if (sortColumn >= 0) {
                // (done by setting when the hor ver setting changes) need to add a check for horizontal view and -1 which is invalid
                String baseValueA = new ArbilTableCellRenderer(((Object[]) firstRowArray)[sortColumn]).getText();
                String comparedValueA = new ArbilTableCellRenderer(((Object[]) secondRowArray)[sortColumn]).getText();
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
                    int baseIntA = ((ArbilField) ((Object[]) firstRowArray)[1]).getFieldOrder();
                    int comparedIntA = ((ArbilField) ((Object[]) secondRowArray)[1]).getFieldOrder();
                    int returnValue = baseIntA - comparedIntA;
                    if (returnValue == 0) {
                        // if the xml node order is the same then also sort on the strings
                        String baseStrA = ((ArbilField) ((Object[]) firstRowArray)[1]).getFieldValue();
                        String comparedStrA = ((ArbilField) ((Object[]) secondRowArray)[1]).getFieldValue();
                        returnValue = baseStrA.compareToIgnoreCase(comparedStrA);
                    }
                    return returnValue;
//                    } else {
//                        return 0;
//                    }
                } catch (Exception ex) {
                    bugCatcher.logError(ex);
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
    
    private ArbilActionBuffer reloadRunner = new ArbilActionBuffer("TableReload-" + this.hashCode(), 50) {

        @Override
        public void executeAction() {
            reloadTableDataPrivate();
        }
    };

    public void requestReloadTableData() {
        reloadRunner.requestActionAndNotify();
    }

    // this will be hit by each imdi node in the table when the application starts hence it needs to be either put in a queue or be synchronised
    private synchronized void reloadTableDataPrivate() { // with the queue this does not need to be synchronised but in this case it will not slow things too much
//        System.out.println("reloadTableData");
        int previousColumnCount = getColumnCount();
        String[] columnNamesTemp = new String[0];
        Object[][] dataTemp = new Object[0][0];

        ArbilDataNode[] tableRowsArbilArray = updateAllDataNodes();

        // set the view to either horizontal or vertical and set the default sort
        boolean lastHorizontalView = horizontalView;
        horizontalView = tableRowsArbilArray.length > 1;
        if (!horizontalView) { // set the table for a single image if that is all that is shown
            //if (imdiObjectHash.size() == listModel.getSize()) { // TODO: this does not account for when a resource is shown
            // TODO: this does not account for when a resource is shown
            if (filteredColumnNames.isEmpty()) {
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
            ArbilField[] displayedColumnNames = filteredColumnNames.values().toArray(new ArbilField[filteredColumnNames.size()]);
            Arrays.sort(displayedColumnNames, new ArbilFieldComparator());
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
            for (ArbilField currentColumn : displayedColumnNames) {
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

            dataTemp = allocateCellData(tableRowsArbilArray.length, columnNamesTemp.length);

            int rowCounter = 0;
            for (ArbilDataNode currentNode : tableRowsArbilArray) {
//                System.out.println("currentNode: " + currentNode.toString());
                Hashtable<String, ArbilField[]> fieldsHash = currentNode.getFields();
                if (showIcons) {
                    //data[rowCounter][0] = new JLabel(currentNode.toString(), currentNode.getIcon(), JLabel.LEFT);
                    dataTemp[rowCounter][0] = currentNode;
                }
                for (int columnCounter = firstFreeColumn; columnCounter < columnNamesTemp.length; columnCounter++) {
                    //System.out.println("columnNames[columnCounter]: " + columnNames[columnCounter] + " : " + columnCounter);
                    if (columnCounter < columnNamesTemp.length - childColumnNames.size()) {
                        ArbilField[] currentValue = fieldsHash.get(columnNamesTemp[columnCounter]);
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
        } else {
            // display the single node view
            columnNamesTemp = singleNodeViewHeadings;
            if (tableRowsArbilArray.length == 0) {
                dataTemp = allocateCellData(0, columnNamesTemp.length);
            } else {
                if (tableRowsArbilArray[0] != null) {
                    Hashtable<String, ArbilField[]> fieldsHash = tableRowsArbilArray[0].getFields();
                    // calculate the real number of rows
                    Vector<ArbilField> allRowFields = new Vector();
                    for (Enumeration<ArbilField[]> valuesEnum = fieldsHash.elements(); valuesEnum.hasMoreElements();) {
                        ArbilField[] currentFieldArray = valuesEnum.nextElement();
                        for (ArbilField currentField : currentFieldArray) {
                            if (currentField.xmlPath.length() > 0) { // prevent non fields being displayed
                                if (tableFieldView.viewShowsColumn(currentField.getTranslateFieldName())) {
                                    allRowFields.add(currentField);
                                }
                            }
                        }
                    }
                    dataTemp = allocateCellData(allRowFields.size(), 2);
//                    Enumeration<String> labelsEnum = fieldsHash.keys();
//                    Enumeration<ImdiField[]> valuesEnum = fieldsHash.elements();
                    int rowCounter = 0;
                    for (Enumeration<ArbilField> allFieldsEnum = allRowFields.elements(); allFieldsEnum.hasMoreElements();) {
                        ArbilField currentField = allFieldsEnum.nextElement();
                        dataTemp[rowCounter][0] = currentField.getTranslateFieldName();
                        dataTemp[rowCounter][1] = currentField;
                        rowCounter++;
                    }
                }
            }
        }
        // update the table model, note that this could be more specific, ie. just row or all it the columns have changed
        //fireTableDataChanged();
        sortTableRows(columnNamesTemp, dataTemp);
        columnNames = columnNamesTemp;
        cellColour = setCellColours(dataTemp);
        Object[][] prevousData = data;
        data = dataTemp;
        if (previousColumnCount != getColumnCount() || prevousData.length != data.length) {
            try {
                fireTableStructureChanged();
            } catch (Exception ex) {
                bugCatcher.logError(ex);
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
        updateImageDisplayPanel(); // update the image panel now the rows have been sorted and the data array updated
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return data.length;
    }

    @Override
    public String getColumnName(int col) {
        if (col < columnNames.length) {
            return columnNames[col];
        } else {
            return "";
        }
    }

    public Object getValueAt(int row, int col) {
        try {
            return data[row][col];
        } catch (Exception e) {
            return null;
        }
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
            return new Color(0xFFFFFF);
        }
    }

    @Override
    // JTable uses this method to determine the default renderer
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
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
        requestReloadTableData();
    }

    public void hideColumn(int columnIndex) {
        System.out.println("hideColumn: " + columnIndex);
        // TODO: hide column
        System.out.println("hideColumn: " + getColumnName(columnIndex));
        if (!childColumnNames.remove(getColumnName(columnIndex))) {
            tableFieldView.addHiddenColumn(getColumnName(columnIndex));
        }
        requestReloadTableData();
    }

    public void showOnlyCurrentColumns() {
        tableFieldView.setShowOnlyColumns(columnNames);
    }

    public ArbilFieldView getFieldView() {
        return tableFieldView;
    }

    public void addChildTypeToDisplay(String childType) {
        System.out.println("addChildTypeToDisplay: " + childType);
        childColumnNames.add(childType);
        requestReloadTableData();
    }

    public Object[] getChildNames() {
        Vector childNames = new Vector();
        Enumeration arbilRowsEnum = dataNodeHash.elements();
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

    public void copyCellToColumn(int row, int col) {
        // if the col or row provided here is invalid then we want to know about it so don't try to prevent such an error
        //        if (row == -1 || col == -1) {
        //            return;
        //        }
        System.out.println("copyCellToColumn for row: " + row + " col: " + col);
        for (int rowCounter = 0; rowCounter < getRowCount(); rowCounter++) {
            if (rowCounter != row) {
                // TODO: a user may want to copy fields with multiple values to the whole column eg descritions in multiple languages
                if (data[rowCounter][col] instanceof ArbilField) {
                    ((ArbilField) data[rowCounter][col]).setFieldValue(((ArbilField) data[row][col]).getFieldValue(), false, false);
                }
                fireTableCellUpdated(rowCounter, col);
            }
        }
    }

    public void highlightMatchingText(String highlightText) {
        highlightCells.add(highlightText);
        cellColour = setCellColours(data);
        fireTableDataChanged();
    }

    public void highlightMatchingFieldPaths(String[] fieldPaths) {
        highFieldPaths = fieldPaths;
        cellColour = setCellColours(data);
        fireTableDataChanged();
    }

    public void highlightMatchingCells(int row, int col) {
        highlightCells.add(new ArbilTableCellRenderer(data[row][col]).getText());
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
