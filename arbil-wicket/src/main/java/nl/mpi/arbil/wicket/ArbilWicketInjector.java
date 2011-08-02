package nl.mpi.arbil.wicket;

import nl.mpi.arbil.ArbilInjector;
import nl.mpi.arbil.data.DataNodeLoader;
import nl.mpi.arbil.data.DefaultDataNodeLoader;
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
    private final static DataNodeLoader dataNodeLoader = new ArbilWicketDataNodeLoaderProxy();

    public static void injectHandlers() {	
	ArbilSessionStorage.setBugCatcher(bugCatcher);
	ArbilSessionStorage.setMessageDialogHandler(messageDialogHandler);
	ArbilSessionStorage.setWindowManager(windowManager);
	DefaultDataNodeLoader.setSessionStorage(sessionStorage);

	injectBugCatcher(bugCatcher);
	injectDialogHandler(messageDialogHandler);
	injectWindowManager(windowManager);
	injectSessionStorage(sessionStorage);
	injectDataNodeLoader(dataNodeLoader);
	injectTreeHelper(treeHelper);
    }
}
