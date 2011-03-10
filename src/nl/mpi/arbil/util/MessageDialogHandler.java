/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.arbil.util;

import java.io.File;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface MessageDialogHandler {

    void addMessageDialogToQueue(String messageString, String messageTitle);

    boolean showConfirmDialogBox(String messageString, String messageTitle);

    int showDialogBox(String message, String title, int optionType, int messageType);

    File[] showFileSelectBox(String titleText, boolean directorySelectOnly, boolean multipleSelect, boolean requireMetadataFiles);

    File showEmptyExportDirectoryDialogue(String titleText);

    /**
     * Shows a dialog that allows a user to confirm save pending changes
     * @throws Exception When user cancels save action
     */
    void offerUserToSaveChanges() throws Exception; // Maybe should be put in a separate interface...
}
