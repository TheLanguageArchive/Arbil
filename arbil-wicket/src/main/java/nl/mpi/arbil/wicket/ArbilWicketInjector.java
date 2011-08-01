package nl.mpi.arbil.wicket;

import nl.mpi.arbil.ArbilInjector;
import nl.mpi.arbil.data.LoaderThreadManager;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcher;
import nl.mpi.arbil.util.MessageDialogHandler;
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

    private final static BugCatcher bugCatcher = new ArbilWicketBugCatcher();
    private final static MessageDialogHandler messageDialogHandler = new ArbilWicketMessageDialogHandler();
    private final static WindowManager windowManager = new ArbilWicketWindowManager();
    private final static SessionStorage sessionStorage = new ArbilWicketSessionStorageSessionProxy();
    private final static TreeHelper treeHelper = new ArbilWicketTreeHelperProxy();
    private final static LoaderThreadManager loaderThreadManager = new ArbilWicketLoaderThreadManagerProxy();

    public static void injectHandlers() {	
	ArbilSessionStorage.setBugCatcher(bugCatcher);
	ArbilSessionStorage.setMessageDialogHandler(messageDialogHandler);
	ArbilSessionStorage.setWindowManager(windowManager);

	injectBugCatcher(bugCatcher);
	injectDialogHandler(messageDialogHandler);
	injectWindowManager(windowManager);
	injectLoaderThreadManager(loaderThreadManager);
	injectSessionStorage(sessionStorage);
	injectTreeHelper(treeHelper);
    }
}
