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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.DataNodeLoader;
import nl.mpi.arbil.data.DataNodeLoaderThreadManager;
import nl.mpi.arbil.data.DefaultDataNodeLoader;
import nl.mpi.arbil.userstorage.SessionStorage;
import org.jmock.Expectations;
import org.jmock.Mockery;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class FavouritesTestUtil {

    public static ArbilDataNode[] createFavouritesNodes(Mockery context, SessionStorage sessionStorage) throws IOException, URISyntaxException {
	final DataNodeLoader loader = getDataNodeLoader(context, sessionStorage);

	final List<ArbilDataNode> favouriteNodes = new ArrayList<ArbilDataNode>();
	final InputStream favouritesListStream = FavouritesExporterImplTest.class.getResourceAsStream("/nl/mpi/arbil/favourites/favourites.config");
	final URI favsBaseUri = FavouritesExporterImplTest.class.getResource("/nl/mpi/arbil/favourites/").toURI();

	final BufferedReader favouritesListReader = new BufferedReader(new InputStreamReader(favouritesListStream));
	try {
	    String line;
	    while (null != (line = favouritesListReader.readLine())) {
		if (!line.startsWith("#")) {
		    final URI favouriteUri = favsBaseUri.resolve(line);
		    // Hard to mock, use actual data node loader
		    final ArbilDataNode node = loader.getArbilDataNodeWithoutLoading(favouriteUri);
		    favouriteNodes.add(node);
		}
	    }
	    return favouriteNodes.toArray(new ArbilDataNode[]{});
	} finally {
	    favouritesListReader.close();
	}
    }

    private static DataNodeLoader getDataNodeLoader(final Mockery context, final SessionStorage sessionStorage) {
	// Configure expectancies and return values of mock objects on creation of data node loader
	context.checking(new Expectations() {
	    {
		// The data node loader will request this
		allowing(equal(sessionStorage)).method("loadBoolean").with(equal("schemaCheckLocalFiles"), equal(false));;
		will(returnValue(false));
	    }
	});
	final DataNodeLoader loader = new DefaultDataNodeLoader(new DataNodeLoaderThreadManager()) {
	    // sort of broken extension since we don't set a ArbilDataNodeService; not needed here though since we only "get without loading"
	};
	return loader;
    }
}
