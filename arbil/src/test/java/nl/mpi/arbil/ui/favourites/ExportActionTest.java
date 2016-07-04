/**
 * Copyright (C) 2016 The Language Archive, Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.arbil.ui.favourites;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.favourites.FavouritesExporter;
import nl.mpi.arbil.favourites.FavouritesImportExportException;
import nl.mpi.arbil.favourites.FavouritesTestUtil;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.flap.plugin.PluginDialogHandler;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ExportActionTest {
    
    private final static String ACTION_COMMAND = "command";
    private final static int ACTION_ID = 1;
    private final Mockery context = new JUnit4Mockery();
    private PluginDialogHandler dialogHandler;
    private FavouritesExporter exporter;
    private ExportUI ui;
    private File exportLocation;
    
    @Before
    public void setUp() throws Exception {
	dialogHandler = context.mock(PluginDialogHandler.class);
	exporter = context.mock(FavouritesExporter.class);
	ui = context.mock(ExportUI.class);
	exportLocation = File.createTempFile(getClass().getSimpleName(), null); //TODO: Use JUnit TemporaryFolder
	exportLocation.deleteOnExit();
    }

    /**
     * Test of actionPerformed method - everything goes according to plan
     */
    @Test
    public void testActionPerformed() throws Exception {
	final ArbilDataNode[] favouriteNodes = FavouritesTestUtil.createFavouritesNodes(context, context.mock(SessionStorage.class));
	context.checking(new Expectations() {
	    {
		// expecting the UI to be asked to provide selected nodes
		oneOf(ui).getSelectedFavourites();
		will(returnValue(Arrays.asList(favouriteNodes)));

		// expecting dialog handler to be asked for an export location
		exactly(1).of(equal(dialogHandler)).method("showFileSelectBox");
		will(returnValue(new File[]{exportLocation}));

		// expecting exporter to be triggered with this location and the selected nodes
		oneOf(exporter).exportFavourites(exportLocation, favouriteNodes);

		// all was fine, expecting verification message to be sent to user through dialog handler
		allowing(equal(dialogHandler)).method("addMessageDialogToQueue");
	    }
	});
	
	final ExportAction instance = new ExportAction(dialogHandler, exporter);
	final ActionEvent event = new ActionEvent(ui, ACTION_ID, ACTION_COMMAND);
	instance.actionPerformed(event);
    }

    /**
     * Test of actionPerformed method - something goes wrong
     */
    @Test
    public void testActionPerformedWithException() throws Exception {
	final ArbilDataNode[] favouriteNodes = FavouritesTestUtil.createFavouritesNodes(context, context.mock(SessionStorage.class));
	context.checking(new Expectations() {
	    {
		// expecting the UI to be asked to provide selected nodes
		oneOf(ui).getSelectedFavourites();
		will(returnValue(Arrays.asList(favouriteNodes)));

		// expecting dialog handler to be asked for an export location
		exactly(1).of(equal(dialogHandler)).method("showFileSelectBox");
		will(returnValue(new File[]{exportLocation}));

		// expecting exporter to be triggered with this location and the selected nodes
		oneOf(exporter).exportFavourites(exportLocation, favouriteNodes);
		// it fails somewhere in the process and throws an exception
		will(throwException(new FavouritesImportExportException("message")));

		// expecting an error message to be thrown towards the user through the dialog handler
		exactly(1).of(equal(dialogHandler)).method("addMessageDialogToQueue");
	    }
	});
	
	final ExportAction instance = new ExportAction(dialogHandler, exporter);
	final ActionEvent event = new ActionEvent(ui, ACTION_ID, ACTION_COMMAND);
	instance.actionPerformed(event);
    }

    /**
     * Test of actionPerformed method - no nodes get selected
     */
    @Test
    public void testActionPerformedWithNoNodesSelected() throws Exception {
	context.checking(new Expectations() {
	    {
		// expecting the UI to be asked to provide selected nodes
		oneOf(ui).getSelectedFavourites();
		// it returns an empty selection, indicating no nodes were selected in UI
		will(returnValue(Collections.emptyList()));

		// expecting a message complaining about the empty selection
		exactly(1).of(equal(dialogHandler)).method("addMessageDialogToQueue");
	    }
	});
	final ExportAction instance = new ExportAction(dialogHandler, exporter);
	final ActionEvent event = new ActionEvent(ui, ACTION_ID, ACTION_COMMAND);
	instance.actionPerformed(event);
    }
}
