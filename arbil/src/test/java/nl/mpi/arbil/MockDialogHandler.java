package nl.mpi.arbil;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.mpi.arbil.util.MessageDialogHandler;

public class MockDialogHandler implements MessageDialogHandler {

    private static final Logger log = Logger.getLogger(MockDialogHandler.class.toString());

    private void logMessage(String messageTitle, String messageString) {
	log.log(Level.INFO, "Message: [{0}] {1}", new Object[]{messageTitle, messageString});
    }

    public void addMessageDialogToQueue(String messageString, String messageTitle) {
	logMessage(messageTitle, messageString);
    }

    public boolean showConfirmDialogBox(String messageString, String messageTitle) {
	logMessage(messageTitle, messageString);
	return true;
    }

    public int showDialogBox(String message, String title, int optionType, int messageType) {
	logMessage(title, message);
	return 0;
    }

    public File[] showFileSelectBox(String titleText, boolean directorySelectOnly, boolean multipleSelect, boolean requireMetadataFiles) {
	logMessage(titleText, null);
	return null;
    }

    public File showEmptyExportDirectoryDialogue(String titleText) {
	logMessage("showEmptyExportDirectoryDialogue", titleText);
	return null;
    }

    public void offerUserToSaveChanges() throws Exception {
	//
    }

    public boolean askUserToSaveChanges(String entityName) {
	logMessage("askUserToSaveChanges", entityName);
	return true;
    }
}
