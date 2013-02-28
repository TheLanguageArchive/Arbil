/*
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
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
package nl.mpi.arbil.ui.favourites;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Collections;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.filechooser.FileFilter;
import nl.mpi.arbil.favourites.FavouritesExporter;
import nl.mpi.arbil.favourites.FavouritesImportExportException;
import nl.mpi.arbil.favourites.FavouritesImporter;
import nl.mpi.flap.plugin.PluginDialogHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller class for handling import requests from the UI
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ImportAction extends AbstractAction {

    private final static Logger logger = LoggerFactory.getLogger(ImportAction.class);
    private final PluginDialogHandler dialogHandler;
    private final FavouritesImporter importer;
    private final static Map<String, FileFilter> importFileMap = Collections.<String, FileFilter>singletonMap("favsExport", new FileFilter() {
	@Override
	public boolean accept(File f) {
	    return f.isDirectory() || f.getName().equals(FavouritesExporter.FAVOURITES_LIST_FILE);
	}

	@Override
	public String getDescription() {
	    return "Favourites export file";
	}
    });

    public ImportAction(PluginDialogHandler dialogHandler, FavouritesImporter importer) {
	super("import");
	this.dialogHandler = dialogHandler;
	this.importer = importer;
    }

    public void actionPerformed(ActionEvent e) {
	importFavourites();
	if (e.getSource() instanceof ImportUI) {
	    ((ImportUI) e.getSource()).refresh();
	}
    }

    private void importFavourites() {
	final File[] exportLocation = dialogHandler.showFileSelectBox("Select favourites export to import", false, false, importFileMap, PluginDialogHandler.DialogueType.open, null);
	if (exportLocation != null && exportLocation.length > 0 && exportLocation[0] != null) {
	    try {
		if (exportLocation[0].isDirectory()) {
		    importer.importFavourites(exportLocation[0]);
		} else {
		    // If export file is selected instead of directory, pass parent (= directory)
		    importer.importFavourites(exportLocation[0].getParentFile());
		}
		dialogHandler.addMessageDialogToQueue("Favourites have been imported", "Import complete");
	    } catch (FavouritesImportExportException ex) {
		logger.error("An error occurred while importing favourites", ex);
		dialogHandler.addMessageDialogToQueue(
			String.format("An error occurred while importing favourites:\n%s.\nSee error log for details.",
			ex.getMessage()), "Error");
	    }
	}
    }
}
