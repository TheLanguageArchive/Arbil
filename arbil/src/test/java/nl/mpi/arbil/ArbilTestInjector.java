/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for Psycholinguistics
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
package nl.mpi.arbil;

import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.data.ArbilTreeHelper;
import nl.mpi.arbil.data.DataNodeLoader;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.BugCatcher;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.DefaultMimeHashQueue;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.WindowManager;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilTestInjector extends ArbilInjector {

    ArbilTreeHelper treeHelper;
    DataNodeLoader dataNodeLoader;
    SessionStorage sessionStorage;

    public void injectHandlers() {
	injectHandlers(null);
    }

    public synchronized void injectHandlers(SessionStorage aSessionStorage) {

	final ApplicationVersionManager versionManager = new ApplicationVersionManager(new ArbilVersion());
	injectVersionManager(versionManager);

	if (aSessionStorage != null) {
	    sessionStorage = aSessionStorage;
	} else {
	    sessionStorage = new MockSessionStorage();
	}
	injectSessionStorage(sessionStorage);

	final BugCatcher bugCatcher = new MockBugCatcher();
	BugCatcherManager.setBugCatcher(bugCatcher);

	final MessageDialogHandler messageDialogHandler = new MockDialogHandler();
	injectDialogHandler(messageDialogHandler);

	final WindowManager windowManager = new MockWindowManager();
	injectWindowManager(windowManager);

	treeHelper = new ArbilTreeHelper(sessionStorage, messageDialogHandler);
	injectTreeHelper(treeHelper);

	final DefaultMimeHashQueue mimeHashQueue = new DefaultMimeHashQueue(sessionStorage);

	dataNodeLoader = new ArbilDataNodeLoader(messageDialogHandler, sessionStorage, mimeHashQueue, treeHelper);
	treeHelper.setDataNodeLoader(dataNodeLoader);
	injectDataNodeLoader(dataNodeLoader);

	treeHelper.init();
    }
}
