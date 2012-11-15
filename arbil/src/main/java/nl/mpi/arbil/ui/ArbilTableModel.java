/**
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.arbil.ui;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.regex.Pattern;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.data.ArbilTableCell;
import nl.mpi.arbil.util.ApplicationVersion;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.ArbilActionBuffer;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;

/**
 * Implementation of AbstractArbilTableModel for the Swing UI
 *
 * @author Peter.Withers@mpi.nl
 */
public class ArbilTableModel extends AbstractArbilTableModel implements ClipboardOwner {

    public static final char CSV_NEWLINE = '\n';
    public static final String CSV_SEPARATOR = "\t"; // excel seems to work with tab but not comma
    public static final String CSV_DOUBLE_QUOTE = "\"\"";
    public static final char CSV_QUOTE = '\"';
    public static final String CSV_QUOTE_STRING = "\"";
    /**
     * Pattern for libraries that should not be included in the 'archive' attribute of the applet HTML code.
     *
     * @see #copyHtmlEmbedTagToClipboard(int, int)
     */
    public static final Pattern APPLET_LIBS_EXCLUDE_PATTERN = Pattern.compile(".*(arbil-help).*jar");
    private Hashtable<String, ArbilDataNode> dataNodeHash = new Hashtable<String, ArbilDataNode>();
    private ArbilTableCell[][] data = new ArbilTableCell[0][0];
    private DefaultListModel listModel = new DefaultListModel(); // used by the image display panel
    private boolean widthsChanged = true;
    private JLabel hiddenColumnsLabel;
    public boolean hideContextMenuAndStatusBar;
    private final ImageBoxRenderer imageBoxRenderer;
    // Handlers to be injected
    protected static MessageDialogHandler messageDialogHandler;

    public static void setMessageDialogHandler(MessageDialogHandler handler) {
	messageDialogHandler = handler;
    }
    private static ApplicationVersionManager versionManager;

    public static void setVersionManager(ApplicationVersionManager versionManagerInstance) {
	versionManager = versionManagerInstance;
    }
    // End handlers

    public ArbilTableModel(ImageBoxRenderer imageBoxRenderer) {
	super(ArbilFieldViews.getSingleInstance().getCurrentGlobalView().clone());
	this.imageBoxRenderer = imageBoxRenderer;
    }

    public ArbilTableModel(ArbilFieldView tableFieldView, ImageBoxRenderer imageBoxRenderer) {
	super(tableFieldView);
	this.imageBoxRenderer = imageBoxRenderer;
    }

    public DefaultListModel getListModel(ArbilSplitPanel arbilSplitPanel) {
	ArbilListDataListener listDataListener = new ArbilListDataListener(arbilSplitPanel);
	listModel.addListDataListener(listDataListener);
	return listModel;
    }
    // end code related to the list display of resources and loose files

    private void updateImageDisplayPanel() {
	listModel.removeAllElements();
	if (imageBoxRenderer != null) {
	    for (int rowCounter = 0; rowCounter < data.length; rowCounter++) {
		ArbilDataNode currentRowDataNode = getDataNodeFromRow(rowCounter);
		if (currentRowDataNode != null) {
		    if (imageBoxRenderer.canDisplay(currentRowDataNode)) {
			if (!listModel.contains(currentRowDataNode)) {
			    listModel.addElement(currentRowDataNode);
			}
		    }
		}
	    }
	}
    }

    public void setHiddenColumnsLabel(JLabel hiddenColumnsLabelLocal) {
	hiddenColumnsLabel = hiddenColumnsLabelLocal;
    }

    @Override
    protected void updateHiddenColumnsLabel(int hiddenColumnCount, int hiddenCellsCount) {
	if (hiddenColumnsLabel != null) {
	    hiddenColumnsLabel.setVisible(!hideContextMenuAndStatusBar && hiddenColumnCount > 0);
	    hiddenColumnsLabel.setText(hiddenCellsCount + " cells hidden in " + hiddenColumnCount + " columns (edit \"Column View\" in the table header to show)");
	}
    }

    /**
     * utility to join an array to a comma separated string
     *
     * @param arrayToJoin
     * @return
     */
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

    /**
     *
     * @param prefix prefix that needs to be prepended to all jar files in the list
     * @param skipPattern regular expression that matches files that need to be skipped (full paths)
     * @return a comma separated string list of .jar files (only the file names) that are on the current class path and do not match
     * the specified skip pattern
     */
    private CharSequence getJarsFromClassPath(String prefix, Pattern skipPattern) {
	final String property = System.getProperty("java.class.path");
	if (property != null) {
	    final String[] tokens = property.split(":");
	    final StringBuilder sb = new StringBuilder(tokens.length);
	    for (String token : tokens) {
		if (token.endsWith(".jar") && (skipPattern == null || !skipPattern.matcher(token).matches())) {
		    File jarFile = new File(token);
		    sb.append(prefix).append(jarFile.getName()).append(",");
		}
	    }
	    // Remove final trailing ,
	    sb.replace(sb.length() - 1, sb.length(), "");
	    return sb;
	}
	return "";
    }

    public void copyHtmlEmbedTagToClipboard(int tableHeight, int tableWidth) {
	try {
	    final ApplicationVersion appVersion = versionManager.getApplicationVersion();
	    // Construct HTML for embedding the applet
	    final StringBuilder embadTagStringBuilder = new StringBuilder();
	    embadTagStringBuilder.append("<applet codebase=\"http://www.mpi.nl/tg/j2se/jnlp/arbil/\"\n");
	    // Main class to run
	    embadTagStringBuilder.append(" code=\"nl.mpi.arbil.ui.applet.ArbilTableApplet.class\"\n");
	    // Define class path jars, first arbil main jar
	    embadTagStringBuilder.append(" archive=\"arbil-").append(appVersion.branch).append("-").append(appVersion.currentMajor).append("-").append(appVersion.currentMinor).append("-").append(appVersion.currentRevision).append(".jar,");
	    // Add all jars in current classpath, except those in the exlude pattern defined in this class
	    embadTagStringBuilder.append(getJarsFromClassPath("lib/", APPLET_LIBS_EXCLUDE_PATTERN)).append("\"\n");
	    embadTagStringBuilder.append(String.format(" width=\"%d\" height=\"%d\" >\n", tableWidth, tableHeight));
	    // Application parameters
	    embadTagStringBuilder.append(" <param name=\"ImdiFileList\" value=\"").append(joinArray(this.getArbilDataNodesURLs())).append("\">\n");
	    embadTagStringBuilder.append(" <param name=\"ShowOnlyColumns\" value=\"").append(joinArray(getColumnNames())).append("\">\n");
	    embadTagStringBuilder.append(" <param name=\"ChildNodeColumns\" value=\"").append(joinArray(getChildColumnNames().toArray())).append("\">\n");
	    embadTagStringBuilder.append(" <param name=\"HighlightText\" value=\"").append(joinArray(getHighlightCells().toArray())).append("\">\n");
	    embadTagStringBuilder.append("</applet>");

	    final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	    StringSelection stringSelection = new StringSelection(embadTagStringBuilder.toString());
	    clipboard.setContents(stringSelection, this);
	} catch (Exception ex) {
	    BugCatcherManager.getBugCatcher().logError(ex);
	}
    }

    /**
     * Data node is to be removed from the table
     *
     * @param dataNode Data node that should be removed
     */
    public void dataNodeRemoved(ArbilNode dataNode) {
	if (dataNode instanceof ArbilDataNode) {
	    removeArbilDataNodes(new ArbilDataNode[]{(ArbilDataNode) dataNode});
	} else {
	    throw new UnsupportedOperationException("Cannot remove an ArbilNode from the table");
	}
    }

    /**
     * Data node is clearing its icon
     *
     * @param dataNode Data node that is clearing its icon
     */
    public void dataNodeIconCleared(ArbilNode dataNode) {
	requestReloadTableData();
    }

    /**
     * A new child node has been added to the destination node
     *
     * @param destination Node to which a node has been added
     * @param newNode The newly added node
     */
    public void dataNodeChildAdded(ArbilNode destination, ArbilNode newNode) {
	// Nothing to do
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
	clipboard.setContents(stringSelection, this);
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
	clipboard.setContents(stringSelection, this);
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
	    BugCatcherManager.getBugCatcher().logError(ex);
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
	return data.toString();
    }

    public void lostOwnership(Clipboard clipboard, Transferable contents) {
	System.out.println("lost clipboard ownership");
    }
}
