package mpi.linorg;

import java.io.FileWriter;

/**
 * Document   : LinorgJournal
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
public class LinorgJournal {

    public boolean saveJournalEntry(String imdiUrl, String imdiNodePath, String oldValue, String newValue, String eventType) {
        boolean returnValue = false;
        try {
            FileWriter journalFile = new FileWriter(GuiHelper.linorgSessionStorage.storageDirectory + "linorgjornal.log", true);
            System.out.println("Journal: " + imdiUrl + "," + imdiNodePath + "," + oldValue + "," + newValue);
            journalFile.append("\""+imdiUrl + imdiNodePath + "\",\"" + oldValue + "\",\"" + newValue + "\",\"" + eventType + "\"\n");
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
