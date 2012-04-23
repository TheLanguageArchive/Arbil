package nl.mpi.arbil.util;

import java.io.File;
import java.util.HashMap;
import javax.swing.JComponent;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface MessageDialogHandler {

    static enum DialogueType {

        open, save, custom
    };

    void addMessageDialogToQueue(String messageString, String messageTitle);

    boolean showConfirmDialogBox(String messageString, String messageTitle);

    int showDialogBox(String message, String title, int optionType, int messageType);

    int showDialogBox(String message, String title, int optionType, int messageType, Object[] options, Object initialValue);

    File[] showMetadataFileSelectBox(String titleText, boolean multipleSelect);

    File[] showDirectorySelectBox(String titleText, boolean multipleSelect);

    File[] showFileSelectBox(String titleText, boolean directorySelectOnly, boolean multipleSelect, HashMap<String, javax.swing.filechooser.FileFilter> fileFilterMap, DialogueType dialogueType, JComponent customAccessory);

    File showEmptyExportDirectoryDialogue(String titleText);

    /**
     * Shows a dialog that allows a user to confirm save pending changes
     *
     * @throws Exception When user cancels save action
     */
    void offerUserToSaveChanges() throws Exception; // Maybe should be put in a separate interface...

    /**
     * Displays a dialog asking the user whether to go ahead and save changes. Intended for use in actions
     * that require the pending changes on an entity to be saved (mainly mutations on a metadata file)
     *
     * @param Name of the entity that needs saving in order to proceed
     * @return Whether user agrees on saving the changes
     */
    boolean askUserToSaveChanges(String entityName);
}
