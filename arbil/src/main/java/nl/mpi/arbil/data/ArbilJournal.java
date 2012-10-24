/**
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package nl.mpi.arbil.data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcher;
import nl.mpi.arbil.util.MessageDialogHandler;

/**
 * Document   : ArbilJournal
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
public class ArbilJournal {

    private static MessageDialogHandler messageDialogHandler;

    public static void setMessageDialogHandler(MessageDialogHandler handler) {
	messageDialogHandler = handler;
    }
    private static BugCatcher bugCatcher;

    public static void setBugCatcher(BugCatcher bugCatcherInstance) {
	bugCatcher = bugCatcherInstance;
    }
    private static SessionStorage sessionStorage;

    public static void setSessionStorage(SessionStorage sessionStorageInstance) {
	sessionStorage = sessionStorageInstance;
    }

    public enum UndoType {

	Value, LanguageId, KeyName
    }

    private static class HistoryItem {

	ArbilField targetField;
	String oldValue;
	String newValue;
	UndoType undoType;
    }

    private ArbilJournal() {
    }
    static private ArbilJournal singleInstance = null;

    static synchronized public ArbilJournal getSingleInstance() {
	if (singleInstance == null) {
	    singleInstance = new ArbilJournal();
	}
	return singleInstance;
    }
    ArrayList<HistoryItem> fieldChangeHistory;
    int currentFieldChangeHistoryItem = 0;

    public synchronized void recordFieldChange(ArbilField targetField, String oldValue, String newValue, UndoType undoType) {
	if (fieldChangeHistory == null) {
	    fieldChangeHistory = new ArrayList<HistoryItem>();
	    currentFieldChangeHistoryItem = 0;
	}
	if (currentFieldChangeHistoryItem < fieldChangeHistory.size()) {
	    fieldChangeHistory = new ArrayList(fieldChangeHistory.subList(0, currentFieldChangeHistoryItem));
	}
	HistoryItem historyItem = new HistoryItem();
	historyItem.targetField = targetField;
	historyItem.oldValue = oldValue;
	historyItem.newValue = newValue;
	historyItem.undoType = undoType;
	fieldChangeHistory.add(historyItem);
	currentFieldChangeHistoryItem++;
    }

    public boolean canUndo() {
	//fieldChangeHistory.size();
	return currentFieldChangeHistoryItem > 0;
    }

    public boolean canRedo() {
	return fieldChangeHistory != null && currentFieldChangeHistoryItem < fieldChangeHistory.size();
    }

    public void undoFromFieldChangeHistory() {
	if (canUndo()) {
	    HistoryItem changeHistoryItem = fieldChangeHistory.get(--currentFieldChangeHistoryItem);
	    HistoryItem reversedHistoryItem = new HistoryItem();
	    reversedHistoryItem.newValue = changeHistoryItem.oldValue;
	    reversedHistoryItem.oldValue = changeHistoryItem.newValue;
	    reversedHistoryItem.targetField = changeHistoryItem.targetField;
	    reversedHistoryItem.undoType = changeHistoryItem.undoType;
	    makeChangeFromHistoryItem(reversedHistoryItem);
	}
    }

    public void redoFromFieldChangeHistory() {
	if (canRedo()) {
	    HistoryItem changeHistoryItem = fieldChangeHistory.get(currentFieldChangeHistoryItem++);
	    makeChangeFromHistoryItem(changeHistoryItem);
	}
    }

    public void clearFieldChangeHistory() {
	fieldChangeHistory = null;
	currentFieldChangeHistoryItem = 0;
    }

    private void makeChangeFromHistoryItem(HistoryItem historyItem) {
	String currentValue = null;
	switch (historyItem.undoType) {
	    case KeyName:
		currentValue = historyItem.targetField.getKeyName();
		break;
	    case LanguageId:
		currentValue = historyItem.targetField.getLanguageId();
		break;
	    case Value:
		currentValue = historyItem.targetField.getFieldValue();
		break;
	}
	if (currentValue != null && !currentValue.equals(historyItem.oldValue)) {
	    messageDialogHandler.addMessageDialogToQueue("The field value is out of sync with the history item", "Undo/Redo");
	    bugCatcher.logError(new Exception("ChangeFromHistory old value does not match current value"));
	} else {
	    switch (historyItem.undoType) {
		case KeyName:
		    historyItem.targetField.setKeyName(historyItem.newValue, true, true);
		    break;
		case LanguageId:
		    historyItem.targetField.setLanguageId(historyItem.newValue, true, true);
		    break;
		case Value:
		    historyItem.targetField.setFieldValue(historyItem.newValue, true, true);
		    break;
	    }
	}
    }

    // this is also use to record an import event
    public boolean saveJournalEntry(String imdiUrl, String imdiNodePath, String oldValue, String newValue, String eventType) {
	boolean returnValue = false;
        FileWriter journalFile = null;
	try {
	    journalFile = new FileWriter(new File(sessionStorage.getStorageDirectory(), "linorgjornal.log"), true);
	    System.out.println("Journal: " + imdiUrl + "," + imdiNodePath + "," + oldValue + "," + newValue);
	    journalFile.append("\"" + imdiUrl + imdiNodePath + "\",\"" + oldValue + "\",\"" + newValue + "\",\"" + eventType + "\"\n");
	    journalFile.close();
	    journalFile = null;
	    returnValue = true;
	} catch (Exception ex) {
	    returnValue = false;
	    bugCatcher.logError(ex);
	    System.err.println("failed to write to the journal: " + ex.getMessage());
	} finally {
	    if (journalFile != null) try {
	        journalFile.close();
	    } catch (IOException ioe) {
	        bugCatcher.logError(ioe);
	    }
	}
	return (returnValue);
    }
}
