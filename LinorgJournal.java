package mpi.linorg;

import java.io.FileWriter;
import java.util.ArrayList;

/**
 * Document   : LinorgJournal
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
public class LinorgJournal {

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
    ArrayList<Object[]> fieldChangeHistory;
    int currentFieldChangeHistoryItem = 0;

    public synchronized void recordFieldChange(ImdiField targetField, String oldValue, String newValue) {
        if (fieldChangeHistory == null) {
            fieldChangeHistory = new ArrayList<Object[]>();
            currentFieldChangeHistoryItem = 0;
        }
        if (currentFieldChangeHistoryItem < fieldChangeHistory.size()) {
            fieldChangeHistory = new ArrayList(fieldChangeHistory.subList(0, currentFieldChangeHistoryItem));
        }
        fieldChangeHistory.add(new Object[]{targetField, oldValue, newValue});
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
        Object[] changeHistoryItem = fieldChangeHistory.get(--currentFieldChangeHistoryItem);
        makeChangeFromHistoryItem((ImdiField) changeHistoryItem[0], (String) changeHistoryItem[2], (String) changeHistoryItem[1]);
    }

    public void redoFromFieldChangeHistory() {
        Object[] changeHistoryItem = fieldChangeHistory.get(currentFieldChangeHistoryItem++);
        makeChangeFromHistoryItem((ImdiField) changeHistoryItem[0], (String) changeHistoryItem[1], (String) changeHistoryItem[2]);
    }

    public void clearFieldChangeHistory() {
        fieldChangeHistory = null;
        currentFieldChangeHistoryItem = 0;
    }

    private void makeChangeFromHistoryItem(ImdiField targetField, String oldValue, String newValue) {
        if (!targetField.getFieldValue().equals(oldValue)) {
            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("The field value is out of sync with the history item", "Undo/Redo");
            GuiHelper.linorgBugCatcher.logError(new Exception("ChangeFromHistory old value does not match current value"));
        } else {
            targetField.setFieldValue(newValue, true, true);
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
