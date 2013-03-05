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
package nl.mpi.arbil.favourites;

import java.io.File;
import java.net.URI;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class FavouritesImporterImplTest {

    private Mockery context = new JUnit4Mockery();

    /**
     * Test of importFavourites method, of class FavouritesImporterImpl.
     */
    @Test
    public void testImportFavourites() throws Exception {
	final URI importDirUri = getClass().getResource("/nl/mpi/arbil/favourites/").toURI();
	final File importDirectory = new File(importDirUri);
	final FavouritesService favouritesService = context.mock(FavouritesService.class);
	final FavouritesImporterImpl instance = new FavouritesImporterImpl(favouritesService);

	context.checking(new Expectations() {
	    {
		// addAsFavourite should get called for each favourite specified in the list
		oneOf(favouritesService).addAsFavourite(importDirUri.resolve("fav-7837654701820957913.cmdi#.CMD.Components.Session.Resources.Source(2).TimePosition(1)"));
		oneOf(favouritesService).addAsFavourite(importDirUri.resolve("fav-3999558434433992672.cmdi"));
		oneOf(favouritesService).addAsFavourite(importDirUri.resolve("fav-1940834862103173169.imdi#.METATRANSCRIPT.Session.MDGroup.Actors.Actor(2)"));
		oneOf(favouritesService).addAsFavourite(importDirUri.resolve("fav-2269212181272190409.cmdi"));
	    }
	});
	instance.importFavourites(importDirectory);
    }

    @Test(expected = FavouritesImportExportException.class)
    public void testImportFavouritesFileDoesNotExist() throws Exception {
	final URI importDirUri = getClass().getResource("/nl/mpi/arbil/favourites/").toURI();
	// A non-existent sub-directory of the resource directory...
	final File importDirectory = new File(importDirUri.resolve("non-existent/"));
	final FavouritesImporterImpl instance = new FavouritesImporterImpl(context.mock(FavouritesService.class));
	instance.importFavourites(importDirectory);
    }
}
