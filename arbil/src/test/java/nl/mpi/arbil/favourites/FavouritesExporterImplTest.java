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
package nl.mpi.arbil.favourites;

import java.io.File;
import java.io.IOException;
import nl.mpi.arbil.ArbilVersion;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.userstorage.SessionStorage;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class FavouritesExporterImplTest {

    private Mockery context = new JUnit4Mockery();
    private SessionStorage sessionStorage;
    private File exportLocation;

    @Before
    public void setUp() throws Exception {
	// Services to mock
	sessionStorage = context.mock(SessionStorage.class);
	// Temporary location to export to
	exportLocation = createTempExportDir();
	exportLocation.deleteOnExit();
    }

    private File createTempExportDir() throws IOException {
         //TODO: Use JUnit TemporaryFolder
	final File location = File.createTempFile(getClass().getSimpleName(), null);
	location.delete();
	location.mkdir();
	return location;
    }

    @After
    public void cleanUp() throws Exception {
	if (!deleteRecursively(exportLocation)) {
	    throw new Exception("could not delete temporary output directory");
	}
    }

    private boolean deleteRecursively(File file) {
	if (!file.exists()) {
	    return false;
	} else {
	    if (file.isDirectory()) {
		for (File dirChild : file.listFiles()) {
		    if (!deleteRecursively(dirChild)) {
			return false;
		    }
		}
	    }
	    return file.delete();
	}
    }

    /**
     * Test of exportFavourites method, of class FavouritesExporterImpl.
     */
    @Test
    public void testExportFavourites() throws Exception {
	// Nodes that are in the current favourites store
	final ArbilDataNode[] favouritesNodes = FavouritesTestUtil.createFavouritesNodes(context, sessionStorage);
	// Create instance to test on
	final FavouritesExporterImpl instance = new FavouritesExporterImpl(sessionStorage, new ArbilVersion());

	// Configure expectancies and return values of mock objects during export
	context.checking(new Expectations() {
	    {
		// Exporter will request favourites dir to make relative URIs
		oneOf(sessionStorage).getFavouritesDir();
		will(returnValue(new File(getClass().getResource("/nl/mpi/arbil/favourites").toURI())));
	    }
	});

	// Do the actual export
	instance.exportFavourites(exportLocation, favouritesNodes);
	// Should contain copies of all favourites and one config file
	assertSame(favouritesNodes.length + 1, exportLocation.list().length);
	// Config file should exist
	assertTrue(new File(exportLocation, FavouritesExporter.FAVOURITES_LIST_FILE).exists());
	// TODO: Test contents of config file...
    }
}
