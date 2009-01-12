/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import java.io.FileWriter;

/**
 *
 * @author petwit
 */
public class LinorgJournal {

    public boolean saveJournalEntry(String imdiUrl, String imdiNodePath, String oldValue, String newValue) {
        boolean returnValue = false;
        try {
            FileWriter journalFile = new FileWriter(GuiHelper.linorgSessionStorage.storageDirectory + "linorgjornal.log", true);
            System.out.println("Journal: " + imdiUrl + "," + imdiNodePath + "," + oldValue + "," + newValue);
            journalFile.append(imdiUrl + "," + imdiNodePath + "," + oldValue + "," + newValue + "\n");
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
