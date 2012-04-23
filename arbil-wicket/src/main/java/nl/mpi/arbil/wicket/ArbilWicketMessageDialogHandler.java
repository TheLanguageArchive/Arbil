package nl.mpi.arbil.wicket;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import javax.swing.JComponent;
import javax.swing.filechooser.FileFilter;
import nl.mpi.arbil.util.MessageDialogHandler;
import org.apache.wicket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilWicketMessageDialogHandler implements MessageDialogHandler {

    private final static Logger logger = LoggerFactory.getLogger(ArbilWicketMessageDialogHandler.class);

    public void addMessageDialogToQueue(String messageString, String messageTitle) {
	logger.info(String.format("Message: [%1$s] %2$s", messageTitle, messageString));
	Session.get().info(String.format("Message: [%1$s] %2$s", messageTitle, messageString));
    }

    public boolean showConfirmDialogBox(String messageString, String messageTitle) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public int showDialogBox(String message, String title, int optionType, int messageType) {
	logger.info(String.format("Dialog box: [%1$s] %2$s", title, message));
	Session.get().info(String.format("Message: [%1$s] %2$s", title, message));
	return 0;
    }

    public int showDialogBox(String message, String title, int optionType, int messageType, Object[] options, Object initialValue) {
	logger.info(String.format("Dialog box: [%1$s] %2$s | %3$s", title, message, Arrays.toString(options)));
	Session.get().info(String.format("Message: [%1$s] %2$s | %3$s", title, message, Arrays.toString(options)));
	return 0;
    }

    public File showEmptyExportDirectoryDialogue(String titleText) {
	logger.info("showEmptyExportDirectoryDialogue");
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public void offerUserToSaveChanges() throws Exception {
	logger.info("offerUserToSaveChanges");
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean askUserToSaveChanges(String entityName) {
	logger.info("askUserToSaveChanges");
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public File[] showMetadataFileSelectBox(String titleText, boolean multipleSelect) {
	logger.info("showMetadataFileSelectBox");
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public File[] showDirectorySelectBox(String titleText, boolean multipleSelect) {
	logger.info("showDirectorySelectBox");
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public File[] showFileSelectBox(String titleText, boolean directorySelectOnly, boolean multipleSelect, HashMap<String, FileFilter> fileFilterMap, DialogueType dialogueType, JComponent customAccessory) {
	logger.info("showFileSelectBox");
	throw new UnsupportedOperationException("Not supported yet.");
    }
}
