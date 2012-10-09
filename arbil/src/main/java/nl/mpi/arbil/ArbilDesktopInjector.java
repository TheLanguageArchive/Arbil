package nl.mpi.arbil;

import java.awt.datatransfer.ClipboardOwner;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.data.ArbilTreeHelper;
import nl.mpi.arbil.data.DataNodeLoader;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.ui.GuiHelper;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.ArbilMimeHashQueue;
import nl.mpi.arbil.util.AuthenticatorStub;
import nl.mpi.arbil.util.BugCatcher;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.MimeHashQueue;
import nl.mpi.arbil.util.TreeHelper;
import nl.mpi.arbil.util.WindowManager;

/**
 * Takes care of injecting certain class instances into objects or classes.
 * This provides us with a sort of dependency injection, which enables loosening
 * the coupling between for example data classes and UI classes.
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilDesktopInjector extends ArbilInjector {

    public static synchronized void injectHandlers() {
	injectHandlers(new ApplicationVersionManager(new ArbilVersion()));
    }

    /**
     * Does initial injection into static classes. Needs to be called only once.
     */
    public static synchronized void injectHandlers(final ApplicationVersionManager versionManager) {
	injectVersionManager(versionManager);

	final BugCatcher bugCatcher = GuiHelper.linorgBugCatcher;
	ArbilSessionStorage.setBugCatcher(bugCatcher);
	ArbilMimeHashQueue.setBugCatcher(bugCatcher);
	injectBugCatcher(bugCatcher);

	final MessageDialogHandler messageDialogHandler = ArbilWindowManager.getSingleInstance();
	ArbilSessionStorage.setMessageDialogHandler(messageDialogHandler);
	ArbilMimeHashQueue.setMessageDialogHandler(messageDialogHandler);
	injectDialogHandler(messageDialogHandler);
	
	// Setting the authenticator here, needs to be done before TreeHelper gets constructed (=initialized). This has been
	// improved in 2.4.x
	try {
	    java.net.Authenticator.setDefault(new AuthenticatorStub(messageDialogHandler));
	} catch (SecurityException sEx) {
	    bugCatcher.logError("Failed to set custom Authenticator. Default authentication dialogs may appear.", sEx);
	}

	final WindowManager windowManager = ArbilWindowManager.getSingleInstance();
	ArbilSessionStorage.setWindowManager(windowManager);
	injectWindowManager(windowManager);

	final ClipboardOwner clipboardOwner = GuiHelper.getClipboardOwner();
	injectClipboardOwner(clipboardOwner);

	ArbilSessionStorage.setBugCatcher(bugCatcher);
	final SessionStorage sessionStorage = ArbilSessionStorage.getSingleInstance();
	ArbilDataNodeLoader.setSessionStorage(sessionStorage);
	ArbilMimeHashQueue.setSessionStorage(sessionStorage);
	injectSessionStorage(sessionStorage);

	final MimeHashQueue mimeHashQueue = ArbilMimeHashQueue.getSingleInstance();
	injectMimeHashQueue(mimeHashQueue);

	final DataNodeLoader dataNodeLoader = ArbilDataNodeLoader.getSingleInstance();
	ArbilMimeHashQueue.setDataNodeLoader(dataNodeLoader);
	injectDataNodeLoader(dataNodeLoader);

	final TreeHelper treeHelper = ArbilTreeHelper.getSingleInstance();
	injectTreeHelper(treeHelper);
    }
}
