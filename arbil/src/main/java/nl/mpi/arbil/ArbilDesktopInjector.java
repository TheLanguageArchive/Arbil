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
import nl.mpi.arbil.ui.ArbilTreeController;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.ui.ImageBoxRenderer;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.ArbilBugCatcher;
import nl.mpi.arbil.util.ArbilMimeHashQueue;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbilcommons.journal.ArbilJournal;

/**
 * Takes care of injecting certain class instances into objects or classes.
 * This provides us with a sort of dependency injection, which enables loosening
 * the coupling between for example data classes and UI classes.
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilDesktopInjector extends ArbilSwingInjector {

    private ArbilTreeHelper treeHelper;
    private ArbilMimeHashQueue mimeHashQueue;
    private ArbilWindowManager windowManager;
    private ArbilDataNodeLoader dataNodeLoader;
    private ImageBoxRenderer imageBoxRenderer;
    private ArbilTreeController treeController;

    public synchronized void injectHandlers() {
	injectHandlers(new ApplicationVersionManager(new ArbilVersion()));
    }

    /**
     * Does initial injection into static classes. Needs to be called only once.
     */
    public synchronized void injectHandlers(final ApplicationVersionManager versionManager) {
	injectVersionManager(versionManager);

	final ArbilSessionStorage sessionStorage = new ArbilSessionStorage();
	injectSessionStorage(sessionStorage);

	BugCatcherManager.setBugCatcher(new ArbilBugCatcher(sessionStorage, versionManager));

        ArbilJournal.setBugCatcher(BugCatcherManager.getBugCatcher());
	windowManager = new ArbilWindowManager();
	windowManager.setSessionStorage(sessionStorage);
	windowManager.setVersionManager(versionManager);
	imageBoxRenderer = new ImageBoxRenderer();
	windowManager.setImageBoxRenderer(imageBoxRenderer);

	final MessageDialogHandler messageDialogHandler = windowManager;
	sessionStorage.setMessageDialogHandler(messageDialogHandler);
	injectDialogHandler(messageDialogHandler);

	sessionStorage.setWindowManager(windowManager);
	injectWindowManager(windowManager);

	mimeHashQueue = new ArbilMimeHashQueue(windowManager, sessionStorage);
	mimeHashQueue.setMessageDialogHandler(messageDialogHandler);
	injectMimeHashQueue(mimeHashQueue);

	treeHelper = new ArbilTreeHelper(sessionStorage, messageDialogHandler);
	windowManager.setTreeHelper(treeHelper);
	sessionStorage.setTreeHelper(treeHelper);
	injectTreeHelper(treeHelper);

	dataNodeLoader = new ArbilDataNodeLoader(messageDialogHandler, sessionStorage, mimeHashQueue, treeHelper);
	treeHelper.setDataNodeLoader(dataNodeLoader);
	mimeHashQueue.setDataNodeLoader(dataNodeLoader);
	windowManager.setDataNodeLoader(dataNodeLoader);
	injectDataNodeLoader(dataNodeLoader);

	treeController = new ArbilTreeController(sessionStorage, treeHelper, windowManager, messageDialogHandler, dataNodeLoader);
    }

    /**
     * Should not be called before injectHandlers()!!
     *
     * @return the treeHelper
     */
    public ArbilTreeHelper getTreeHelper() {
	return treeHelper;
    }

    /**
     * Should not be called before injectHandlers()!!
     *
     * @return the tree controller
     */
    public ArbilTreeController getTreeController() {
	return treeController;
    }

    /**
     * Should not be called before injectHandlers()!!
     *
     * @return the treeHelper
     */
    public ArbilMimeHashQueue getMimeHashQueue() {
	return mimeHashQueue;
    }

    /**
     * Should not be called before injectHandlers()!!
     *
     * @return the treeHelper
     */
    public ArbilWindowManager getWindowManager() {
	return windowManager;
    }

    /**
     * Should not be called before injectHandlers()!!
     *
     * @return the treeHelper
     */
    public ArbilDataNodeLoader getDataNodeLoader() {
	return dataNodeLoader;
    }

    public ImageBoxRenderer getImageBoxRenderer() {
	return imageBoxRenderer;
    }
}
