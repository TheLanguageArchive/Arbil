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
package nl.mpi.arbil;

import java.io.File;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
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

    public int showDialogBox(String message, String title, int optionType, int messageType, Object[] options, Object initialValue) {
        logMessage(title, message + " " + options.toString());
        return 0;
    }

    public File[] showMetadataFileSelectBox(String titleText, boolean multipleSelect) {
        logMessage(titleText, null);
        return null;
    }

    public File[] showDirectorySelectBox(String titleText, boolean multipleSelect) {
        logMessage(titleText, null);
        return null;
    }

    public File[] showFileSelectBox(String titleText, boolean directorySelectOnly, boolean multipleSelect, Map<String, javax.swing.filechooser.FileFilter> fileFilterMap, DialogueType dialogueType, JComponent customAccessory) {
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
