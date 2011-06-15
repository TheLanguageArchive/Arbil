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
	String csvSeparator = "\t"; // excel seems to work with tab but not comma
	String copiedString = "";
	int firstColumn = 0;
	if (isShowIcons() && isHorizontalView()) {
	    // horizontalView excludes icon display
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
	clipboard.setContents(stringSelection, ArbilTableModel.clipboardOwner);
    }

    public void copyArbilFields(ArbilField[] selectedCells) {
	String csvSeparator = "\t"; // excel seems to work with tab but not comma
	String copiedString = "";
	copiedString = copiedString + "\"" + SINGLE_NODE_VIEW_HEADINGS[0] + "\"" + csvSeparator;
	copiedString = copiedString + "\"" + SINGLE_NODE_VIEW_HEADINGS[1] + "\"";
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
		    firstLine = clipBoardLines[0].split("\t");
		}
		boolean singleNodeAxis = false;
		String regexString = "[(\"^)($\")]";
		System.out.println("regexString: " + (firstLine[0].replaceAll(regexString, "")));
		if (firstLine[0].replaceAll(regexString, "").equals(SINGLE_NODE_VIEW_HEADINGS[0]) && firstLine[1].replaceAll(regexString, "").equals(SINGLE_NODE_VIEW_HEADINGS[1])) {
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
