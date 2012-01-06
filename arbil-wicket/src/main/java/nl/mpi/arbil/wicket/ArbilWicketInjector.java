package nl.mpi.arbil.wicket;

import nl.mpi.arbil.ArbilInjector;
import nl.mpi.arbil.ArbilVersion;
import nl.mpi.arbil.data.DataNodeLoader;
import nl.mpi.arbil.data.DefaultDataNodeLoader;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.BugCatcher;
import nl.mpi.arbil.util.DefaultMimeHashQueue;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.MimeHashQueue;
import nl.mpi.arbil.util.TreeHelper;
import nl.mpi.arbil.util.WindowManager;

/**
 * Injects arbil wicket implementations of handlers
 * NOTE: (some) handlers should operate as a proxy to the session-specific 
 * handlers, e.g. each user will need its own session storage
 * 
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilWicketInjector extends ArbilInjector {

    private final BugCatcher bugCatcher = new ArbilWicketBugCatcher();
    private final ApplicationVersionManager versionManager = new ApplicationVersionManager(new ArbilVersion());
    private final MessageDialogHandler messageDialogHandler = new ArbilWicketMessageDialogHandler();
    private final WindowManager windowManager = new ArbilWicketWindowManager();
    private final SessionStorage sessionStorage = new ArbilWicketSessionStorageSessionProxy();
    private final TreeHelper treeHelper = new ArbilWicketTreeHelperProxy();
    private final DataNodeLoader dataNodeLoader = new ArbilWicketDataNodeLoaderProxy();

    public void injectHandlers() {	

	injectVersionManager(versionManager);
	injectBugCatcher(bugCatcher);
	injectDialogHandler(messageDialogHandler);
	injectWindowManager(windowManager);
	injectSessionStorage(sessionStorage);
	injectDataNodeLoader(dataNodeLoader);
	injectTreeHelper(treeHelper);
    }

    /**
     * @return the sessionStorage
     */
    protected SessionStorage getSessionStorage() {
	return sessionStorage;
    }

    /**
     * @return the bugCatcher
     */
    protected BugCatcher getBugCatcher() {
	return bugCatcher;
    }

    /**
     * @return the versionManager
     */
    protected ApplicationVersionManager getVersionManager() {
	return versionManager;
    }

    /**
     * @return the messageDialogHandler
     */
    protected MessageDialogHandler getMessageDialogHandler() {
	return messageDialogHandler;
    }

    /**
     * @return the windowManager
     */
    protected WindowManager getWindowManager() {
	return windowManager;
    }

    /**
     * @return the treeHelper
     */
    protected TreeHelper getTreeHelper() {
	return treeHelper;
    }

    /**
     * @return the dataNodeLoader
     */
    protected DataNodeLoader getDataNodeLoader() {
	return dataNodeLoader;
    }
}
