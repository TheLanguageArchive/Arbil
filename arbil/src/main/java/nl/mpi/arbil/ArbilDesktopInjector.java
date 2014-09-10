/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for
 * Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.arbil;

import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.data.ArbilTreeHelper;
import nl.mpi.arbil.ui.ArbilTableController;
import nl.mpi.arbil.ui.ArbilTreeController;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.ui.ImageBoxRenderer;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.util.ApplicationVersion;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.ArbilLogConfigurer;
import nl.mpi.arbil.util.ArbilMimeHashQueue;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.LoggingBugCatcher;
import nl.mpi.arbil.util.MessageDialogHandler;

/**
 * Takes care of injecting certain class instances into objects or classes. This
 * provides us with a sort of dependency injection, which enables loosening the
 * coupling between for example data classes and UI classes.
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilDesktopInjector extends ArbilSwingInjector {

    private ArbilTreeHelper treeHelper;
    private ArbilMimeHashQueue mimeHashQueue;
    private ArbilWindowManager windowManager;
    private ArbilDataNodeLoader dataNodeLoader;
    private ImageBoxRenderer imageBoxRenderer;
    private ArbilSessionStorage sessionStorage;
    private ArbilTreeController treeController;
    private ArbilTableController tableController;

    /**
     * <strong>To be used for testing scenario's only<strong>, will generate a
     * new {@link ApplicationVersion} and {@link ArbilLogConfigurer}. For
     * production workflows, use {@link #injectHandlers(nl.mpi.arbil.util.ApplicationVersionManager, nl.mpi.arbil.util.ArbilLogConfigurer)
     * }
     */
    public synchronized void injectDefaultHandlers() {
        final ArbilVersion arbilVersion = new ArbilVersion();
        injectHandlers(new ArbilSessionStorage(), new ApplicationVersionManager(arbilVersion), new ArbilLogConfigurer(arbilVersion, "arbil-log"));
    }

    /**
     * Does initial injection into static classes. Needs to be called only once.
     */
    public synchronized void injectHandlers(final ArbilSessionStorage sessionStorage, final ApplicationVersionManager versionManager, final ArbilLogConfigurer logManager) {
        this.injectVersionManager(versionManager);
        this.sessionStorage = sessionStorage;

        // From now on log to application storage directory
        logManager.configureLoggingFromSessionStorage(sessionStorage);
        injectSessionStorage(sessionStorage);

        BugCatcherManager.setBugCatcher(new LoggingBugCatcher());

        windowManager = new ArbilWindowManager();
        windowManager.setSessionStorage(sessionStorage);
        windowManager.setVersionManager(versionManager);
        imageBoxRenderer = new ImageBoxRenderer();
        windowManager.setImageBoxRenderer(imageBoxRenderer);

        final MessageDialogHandler messageDialogHandler = windowManager;
        sessionStorage.setMessageDialogHandler(messageDialogHandler);
        injectDialogHandler(messageDialogHandler);

        injectWindowManager(windowManager);

        mimeHashQueue = new ArbilMimeHashQueue(windowManager, sessionStorage);
        mimeHashQueue.setMessageDialogHandler(messageDialogHandler);

        treeHelper = new ArbilTreeHelper(sessionStorage, messageDialogHandler);
        windowManager.setTreeHelper(treeHelper);
        sessionStorage.setTreeHelper(treeHelper);
        injectTreeHelper(treeHelper);

        dataNodeLoader = new ArbilDataNodeLoader(messageDialogHandler, sessionStorage, mimeHashQueue, treeHelper);
        treeHelper.setDataNodeLoader(dataNodeLoader);
        mimeHashQueue.setDataNodeLoader(dataNodeLoader);
        windowManager.setDataNodeLoader(dataNodeLoader);
        injectDataNodeLoader(dataNodeLoader);

        tableController = new ArbilTableController(treeHelper, messageDialogHandler, windowManager);
        windowManager.setTableController(tableController);

        treeController = new ArbilTreeController(sessionStorage, treeHelper, windowManager, messageDialogHandler, dataNodeLoader, mimeHashQueue, versionManager);
    }

    /**
     * Should not be called before injectDefaultHandlers()!!
     *
     * @return the treeHelper
     */
    public ArbilTreeHelper getTreeHelper() {
        return treeHelper;
    }

    /**
     * Should not be called before injectDefaultHandlers()!!
     *
     * @return the tree controller
     */
    public ArbilTreeController getTreeController() {
        return treeController;
    }

    /**
     * Should not be called before injectDefaultHandlers()!!
     *
     * @return the treeHelper
     */
    public ArbilMimeHashQueue getMimeHashQueue() {
        return mimeHashQueue;
    }

    /**
     * Should not be called before injectDefaultHandlers()!!
     *
     * @return the treeHelper
     */
    public ArbilWindowManager getWindowManager() {
        return windowManager;
    }

    /**
     * Should not be called before injectDefaultHandlers()!!
     *
     * @return the treeHelper
     */
    public ArbilDataNodeLoader getDataNodeLoader() {
        return dataNodeLoader;
    }

    public ImageBoxRenderer getImageBoxRenderer() {
        return imageBoxRenderer;
    }

    public ArbilTableController getTableController() {
        return tableController;
    }

    public ArbilSessionStorage getSessionStorage() {
        return sessionStorage;
    }
}
