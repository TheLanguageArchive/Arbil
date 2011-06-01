package nl.mpi.arbil.wicket;

import nl.mpi.arbil.ArbilInjector;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcher;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.WindowManager;

/**
 * Injects arbil wicket implementations of handlers
 * NOTE: (some) handlers should operate as a proxy to the session-specific 
 * handlers, e.g. each user will need its own session storage
 * 
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilWicketInjector extends ArbilInjector {

    private static BugCatcher bugCatcher = new ArbilWicketBugCatcher();
    private static MessageDialogHandler messageDialogHandler = new ArbilWicketMessageDialogHandler();
    private static WindowManager windowManager = new ArbilWicketWindowManager();
    private static SessionStorage sessionStorage = null;
    
    public static void injectHandlers() {
	ArbilSessionStorage.setBugCatcher(bugCatcher);
	ArbilSessionStorage.setMessageDialogHandler(messageDialogHandler);
	ArbilSessionStorage.setWindowManager(windowManager);
	sessionStorage = ArbilSessionStorage.getSingleInstance();
	injectHandlers(messageDialogHandler, windowManager, sessionStorage, bugCatcher, null);
    }
}
