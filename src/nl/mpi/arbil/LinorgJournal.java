package nl.mpi.arbil;

import java.io.FileWriter;
import java.util.ArrayList;

/**
 * Document   : LinorgJournal
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
public class LinorgJournal {

    public enum UndoType {

        Value, LanguageId, KeyName
    }

    private class HistoryItem {

        ImdiField targetField;
        String oldValue;
        String newValue;
        UndoType undoType;
    }

    private LinorgJournal() {
    }
    static private LinorgJournal singleInstance = null;

    static synchronized public LinorgJournal getSingleInstance() {
        System.out.println("LinorgJournal getSingleInstance");
        if (singleInstance == null) {
            singleInstance = new LinorgJournal();
        }
        return singleInstance;
    }
    ArrayList<HistoryItem> fieldChangeHistory;
    int currentFieldChangeHistoryItem = 0;

    public synchronized void recordFieldChange(ImdiField targetField, String oldValue, String newValue, UndoType undoType) {
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
            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("The field value is out of sync with the history item", "Undo/Redo");
            GuiHelper.linorgBugCatcher.logError(new Exception("ChangeFromHistory old value does not match current value"));
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
        try {
            FileWriter journalFile = new FileWriter(LinorgSessionStorage.getSingleInstance().storageDirectory + "linorgjornal.log", true);
            System.out.println("Journal: " + imdiUrl + "," + imdiNodePath + "," + oldValue + "," + newValue);
            journalFile.append("\"" + imdiUrl + imdiNodePath + "\",\"" + oldValue + "\",\"" + newValue + "\",\"" + eventType + "\"\n");
            journalFile.close();
            returnValue = true;
        } catch (Exception ex) {
            returnValue = false;
            GuiHelper.linorgBugCatcher.logError(ex);
            System.err.println("failed to write to the journal: " + ex.getMessage());
        }
        return (returnValue);
    }
}
