package nl.mpi.arbil.ui;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import nl.mpi.arbil.ArbilVersion;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.data.ArbilTableCell;
import nl.mpi.arbil.util.ArbilActionBuffer;
import nl.mpi.arbil.util.MessageDialogHandler;

/**
 * Implementation of AbstractArbilTableModel for the Swing UI
 * @author Peter.Withers@mpi.nl
 */
public class ArbilTableModel extends AbstractArbilTableModel {

    public static final char CSV_NEWLINE = '\n';
    public static final String CSV_SEPARATOR = "\t"; // excel seems to work with tab but not comma
    public static final String CSV_DOUBLE_QUOTE = "\"\"";
    public static final char CSV_QUOTE = '\"';
    public static final String CSV_QUOTE_STRING = "\"";
    
    private Hashtable<String, ArbilDataNode> dataNodeHash = new Hashtable<String, ArbilDataNode>();
    private ArbilTableCell[][] data = new ArbilTableCell[0][0];
    private DefaultListModel listModel = new DefaultListModel(); // used by the image display panel
    private boolean widthsChanged = true;
    private JLabel hiddenColumnsLabel;
    public boolean hideContextMenuAndStatusBar;
    // Handlers to be injected
    protected static MessageDialogHandler messageDialogHandler;

    public static void setMessageDialogHandler(MessageDialogHandler handler) {
	messageDialogHandler = handler;
    }
    private static ClipboardOwner clipboardOwner;

    public static void setClipboardOwner(ClipboardOwner clipboardOwnerInstance) {
	clipboardOwner = clipboardOwnerInstance;
    }
    // End handlers

    public ArbilTableModel() {
	super(ArbilFieldViews.getSingleInstance().getCurrentGlobalView().clone());
    }

    public ArbilTableModel(ArbilFieldView tableFieldView) {
	super(tableFieldView);
    }

    public DefaultListModel getListModel(ArbilSplitPanel arbilSplitPanel) {
	ArbilListDataListener listDataListener = new ArbilListDataListener(arbilSplitPanel);
	listModel.addListDataListener(listDataListener);
	return listModel;
    }
    // end code related to the list display of resources and loose files

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

    public void setHiddenColumnsLabel(JLabel hiddenColumnsLabelLocal) {
	hiddenColumnsLabel = hiddenColumnsLabelLocal;
    }

    @Override
    protected void updateHiddenColumnsLabel(int hiddenColumnCount) {
	if (hiddenColumnsLabel != null) {
	    hiddenColumnsLabel.setVisible(!hideContextMenuAndStatusBar && hiddenColumnCount > 0);
	    hiddenColumnsLabel.setText(hiddenColumnCount + " columns hidden (edit \"Column View\" in the table header to show)");
	}
    }
    // utility to join an array to a comma separated string

    private String joinArray(Object[] arrayToJoin) {
	StringBuilder joinedString = new StringBuilder();
	boolean first = true;
	for (Object currentArrayItem : arrayToJoin) {
	    if (!first) {
	        joinedString.append(',');
            }
	    first = false;
	    joinedString.append(currentArrayItem.toString());
	}
	return joinedString.toString();
    }

    public void copyHtmlEmbedTagToClipboard(int tableHeight, int tableWidth) {
	try {
	    ArbilVersion arbilVersion = new ArbilVersion();
	    // TODO: the clas path specified here needs to be dynamically generated
	    String embedTagString = "<APPLET CODEBASE=\"http://www.mpi.nl/tg/j2se/jnlp/arbil/\" CODE=\"nl.mpi.arbil.ArbilTableApplet.class\" ARCHIVE=\"arbil-" + arbilVersion.currentMajor + "-" + arbilVersion.currentMinor + "-" + arbilVersion.currentRevision + ".jar,lib/corpusstructure-1.6.1.jar,lib/imdiapi-1.0.6.jar,lib/log4j-1.2.14.jar,lib/saxon8.jar,lib/saxon8-dom.jar,lib/typecheck-1.5.16185.jar,lib/xalan-2.6.0.jar,lib/xercesImpl-2.9.0.jar\"";
	    embedTagString = embedTagString + " WIDTH=" + tableWidth + " HEIGHT=" + tableHeight + " >\n";
	    embedTagString = embedTagString + "  <PARAM NAME=\"ImdiFileList\" VALUE=\"" + joinArray(this.getArbilDataNodesURLs()) + "\">\n";
	    embedTagString = embedTagString + "  <PARAM NAME=\"ShowOnlyColumns\" VALUE=\"" + joinArray(getColumnNames()) + "\">\n";
	    embedTagString = embedTagString + "  <PARAM NAME=\"ChildNodeColumns\" VALUE=\"" + joinArray(getChildColumnNames().toArray()) + "\">\n";
	    embedTagString = embedTagString + "  <PARAM NAME=\"HighlightText\" VALUE=\"" + joinArray(getHighlightCells().toArray()) + "\">\n";
	    embedTagString = embedTagString + "</APPLET>";
	    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	    StringSelection stringSelection = new StringSelection(embedTagString);
	    clipboard.setContents(stringSelection, clipboardOwner);
	} catch (Exception ex) {
	    getBugCatcher().logError(ex);
	}
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

    /**
     * @return the widthsChanged
     */
    public boolean isWidthsChanged() {
	return widthsChanged;
    }

    /**
     * @param widthsChanged the widthsChanged to set
     */
    public void setWidthsChanged(boolean widthsChanged) {
	this.widthsChanged = widthsChanged;
    }

    /**
     * @return the dataNodeHash
     */
    protected Hashtable<String, ArbilDataNode> getDataNodeHash() {
	return dataNodeHash;
    }

    // NOTE: ArbilActionBuffer is not serializable but ArbilTableModel should be!
    private ArbilActionBuffer reloadRunner = new ArbilActionBuffer("TableReload-" + this.hashCode(), 50) {

	@Override
	public void executeAction() {
	    reloadTableDataPrivate();
	}
    };

    @Override
    protected synchronized void reloadTableDataPrivate() {
	super.reloadTableDataPrivate();
	updateImageDisplayPanel(); // update the image panel now the rows have been sorted and the data array updated
	setWidthsChanged(false);
    }

    public void requestReloadTableData() {
	reloadRunner.requestActionAndNotify();
    }

    public void setPreferredColumnWidth(String columnName, Integer width) {
	Integer currentWidth = getFieldView().getColumnWidth(columnName);
	if (!isWidthsChanged()
		&& !(width == null && currentWidth == null || width != null && width.equals(currentWidth))) {
	    setWidthsChanged(true);
	    fireTableStructureChanged();
	}
	getFieldView().setColumnWidth(columnName, width);
    }

    public Integer getPreferredColumnWidth(String columnName) {
	return getFieldView().getColumnWidth(columnName);
    }

    public void copyArbilRows(int[] selectedRows) {
	int firstColumn = 0;
	if (isShowIcons() && isHorizontalView()) {
	    // horizontalView excludes icon display
	    firstColumn = 1;
	}
	// add the headers
	final int columnCount = getColumnCount();
	final StringBuilder copiedString = new StringBuilder();
	for (int selectedColCounter = firstColumn; selectedColCounter < columnCount; selectedColCounter++) {
	    copiedString.append(CSV_QUOTE).append(getColumnName(selectedColCounter)).append(CSV_QUOTE);
	    if (selectedColCounter < columnCount - 1) {
		copiedString.append(CSV_SEPARATOR);
	    }
	}
	copiedString.append(CSV_NEWLINE);
	// add the cell data
	for (int selectedRowCounter = 0; selectedRowCounter < selectedRows.length; selectedRowCounter++) {
	    System.out.println("copying row: " + selectedRowCounter);
	    for (int selectedColCounter = firstColumn; selectedColCounter < columnCount; selectedColCounter++) {
		copiedString.append(CSV_QUOTE).append(data[selectedRows[selectedRowCounter]][selectedColCounter].toString().replace(CSV_QUOTE_STRING, CSV_DOUBLE_QUOTE)).append(CSV_QUOTE);
		if (selectedColCounter < columnCount - 1) {
		    copiedString.append(CSV_SEPARATOR);
		}
	    }
	    copiedString.append(CSV_NEWLINE);
	}
	//System.out.println("copiedString: " + this.get getCellSelectionEnabled());
	System.out.println("copiedString: " + copiedString.toString());
	Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	StringSelection stringSelection = new StringSelection(copiedString.toString());
	clipboard.setContents(stringSelection, ArbilTableModel.clipboardOwner);
    }

    public void copyArbilFields(ArbilField[] selectedCells) {
	StringBuilder copiedString = new StringBuilder();
	copiedString.append(CSV_QUOTE).append(SINGLE_NODE_VIEW_HEADING_NAME.replace(CSV_QUOTE_STRING, CSV_DOUBLE_QUOTE)).append(CSV_QUOTE).append(CSV_SEPARATOR);
	copiedString.append(CSV_QUOTE).append(SINGLE_NODE_VIEW_HEADING_VALUE.replace(CSV_QUOTE_STRING, CSV_DOUBLE_QUOTE)).append(CSV_QUOTE);
	copiedString.append(CSV_NEWLINE);
	boolean isFirstCol = true;
	for (ArbilField currentField : selectedCells) {
	    if (!isFirstCol) {
		copiedString.append(CSV_SEPARATOR);
		isFirstCol = false;
	    }
	    copiedString.append(CSV_QUOTE).append(currentField.getTranslateFieldName()).append(CSV_QUOTE).append(CSV_SEPARATOR);
	    copiedString.append(CSV_QUOTE).append(currentField.getFieldValue()).append(CSV_QUOTE);
	    copiedString.append(CSV_NEWLINE);
	}
	System.out.println("copiedString: " + copiedString.toString());
	Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	StringSelection stringSelection = new StringSelection(copiedString.toString());
	clipboard.setContents(stringSelection, ArbilTableModel.clipboardOwner);
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
	    if (clipBoardData != null) {
		//TODO: check that this is not null first but let it pass on null so that the no data to paste messages get sent to the user
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
		    firstLine = clipBoardLines[0].split(CSV_SEPARATOR);
		}
		boolean singleNodeAxis = false;
		String regexString = "[(\"^)($\")]";
		System.out.println("regexString: " + (firstLine[0].replaceAll(regexString, "")));
		if (firstLine[0].replaceAll(regexString, "").equals(SINGLE_NODE_VIEW_HEADING_NAME) && firstLine[1].replaceAll(regexString, "").equals(SINGLE_NODE_VIEW_HEADING_VALUE)) {
		    singleNodeAxis = true;
		}
		if (!singleNodeAxis) {
		    resultMessage = "Incorrect data to paste.\nThe data must be copied either from a table where only one IMDI file is displayed\nor by selecting individual cells in the table.";
		}
		if (singleNodeAxis) {
		    boolean pasteOneFieldToAll = clipBoardLines.length == 2; /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */ /* skip the header */
		    HashSet<String> pastedFieldNames = new HashSet();
		    for (int lineCounter = 1; lineCounter < clipBoardLines.length; lineCounter++) {
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
	    getBugCatcher().logError(ex);
	}
	return resultMessage;
    }

    @Override
    public void removeAllArbilDataNodeRows() {
	listModel.removeAllElements();
	super.removeAllArbilDataNodeRows();
    }

    @Override
    protected void removeArbilDataNode(ArbilDataNode arbilDataNode) {
	System.out.println("removing: " + arbilDataNode.toString());
	listModel.removeElement(arbilDataNode);
	super.removeArbilDataNode(arbilDataNode);
    }

    /**
     * @return the data
     */
    protected ArbilTableCell[][] getData() {
	return data;
    }

    /**
     * @param data the data to set
     */
    protected void setData(ArbilTableCell[][] data) {
	this.data = data;
    }

    protected String getRenderedText(ArbilTableCell data) {
	return new ArbilTableCellRenderer(data).getText();
    }
}
