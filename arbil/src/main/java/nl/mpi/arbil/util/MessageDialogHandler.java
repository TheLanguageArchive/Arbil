package nl.mpi.arbil.util;

import java.io.File;
import nl.mpi.arbil.plugin.PluginDialogHandler;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface MessageDialogHandler extends PluginDialogHandler {

    File[] showMetadataFileSelectBox(String titleText, boolean multipleSelect);

    File[] showDirectorySelectBox(String titleText, boolean multipleSelect);

    File showEmptyExportDirectoryDialogue(String titleText);

    /**
     * Shows a dialog that allows a user to confirm save pending changes
     *
     * @throws Exception When user cancels save action
     */
    void offerUserToSaveChanges() throws Exception; // Maybe should be put in a separate interface...

    /**
     * Displays a dialog asking the user whether to go ahead and save changes.
     * Intended for use in actions that require the pending changes on an entity
     * to be saved (mainly mutations on a metadata file)
     *
     * @param Name of the entity that needs saving in order to proceed
     * @return Whether user agrees on saving the changes
     */
    boolean askUserToSaveChanges(String entityName);
}
