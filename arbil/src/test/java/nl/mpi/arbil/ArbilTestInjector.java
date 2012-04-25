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

    public ArbilTreeHelper treeHelper;
    public DataNodeLoader dataNodeLoader;
    public SessionStorage sessionStorage;
    public MessageDialogHandler messageDialogHandler;

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

	messageDialogHandler = new MockDialogHandler();
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
