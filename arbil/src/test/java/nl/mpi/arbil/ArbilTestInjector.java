package nl.mpi.arbil;

import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.data.ArbilTreeHelper;
import nl.mpi.arbil.data.DataNodeLoader;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.ArbilBugCatcher;
import nl.mpi.arbil.util.ArbilMimeHashQueue;
import nl.mpi.arbil.util.BugCatcher;
import nl.mpi.arbil.util.DefaultMimeHashQueue;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.MimeHashQueue;
import nl.mpi.arbil.util.TreeHelper;
import nl.mpi.arbil.util.WindowManager;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilTestInjector extends ArbilInjector {

    public synchronized void injectHandlers() {

	final ApplicationVersionManager versionManager = new ApplicationVersionManager(new ArbilVersion());
	injectVersionManager(versionManager);

	final BugCatcher bugCatcher = new ArbilBugCatcher();
	injectBugCatcher(bugCatcher);

	final MessageDialogHandler messageDialogHandler = new MockDialogHandler();
	injectDialogHandler(messageDialogHandler);

	final WindowManager windowManager = new MockWindowManager();
	injectWindowManager(windowManager);

	final SessionStorage sessionStorage = new MockSessionStorage();
	injectSessionStorage(sessionStorage);

	final TreeHelper treeHelper = new ArbilTreeHelper();
	injectTreeHelper(treeHelper);
	
	final MimeHashQueue mimeHashQueue = new DefaultMimeHashQueue();
	injectMimeHashQueue(mimeHashQueue);

	ArbilDataNodeLoader.setSessionStorage(sessionStorage);
	final DataNodeLoader dataNodeLoader = new ArbilDataNodeLoader(bugCatcher, messageDialogHandler, sessionStorage, mimeHashQueue, treeHelper);
	injectDataNodeLoader(dataNodeLoader);
    }
}
