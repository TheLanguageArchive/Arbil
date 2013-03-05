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
import java.util.Arrays;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.favourites.FavouritesExporter;
import nl.mpi.arbil.favourites.FavouritesTestUtil;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.flap.plugin.PluginDialogHandler;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ExportActionTest {

    public static final String ACTION_COMMAND = "command";
    private Mockery context = new JUnit4Mockery();
    private int ACTION_ID = 1;

    /**
     * Test of actionPerformed method, of class ExportAction.
     */
    @Test
    public void testActionPerformed() throws Exception {
	final PluginDialogHandler dialogHandler = context.mock(PluginDialogHandler.class);
	final FavouritesExporter exporter = context.mock(FavouritesExporter.class);
	final ExportUI ui = context.mock(ExportUI.class);

	final File exportLocation = File.createTempFile(getClass().getSimpleName(), null);
	exportLocation.deleteOnExit();

	final ArbilDataNode[] favouriteNodes = FavouritesTestUtil.createFavouritesNodes(context, context.mock(SessionStorage.class));

	context.checking(new Expectations() {
	    {
		exactly(1).of(equal(dialogHandler)).method("showFileSelectBox");
		will(returnValue(new File[]{exportLocation}));

		oneOf(ui).getSelectedFavourites();
		will(returnValue(Arrays.asList(favouriteNodes)));

		oneOf(exporter).exportFavourites(exportLocation, favouriteNodes);

		allowing(equal(dialogHandler)).method("addMessageDialogToQueue");
	    }
	});

	final ExportAction instance = new ExportAction(dialogHandler, exporter);
	final ActionEvent event = new ActionEvent(ui, ACTION_ID, ACTION_COMMAND);
	instance.actionPerformed(event);
    }
}
