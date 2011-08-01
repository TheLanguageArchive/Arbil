package nl.mpi.arbil;

import java.awt.datatransfer.ClipboardOwner;
import nl.mpi.arbil.data.ArbilDataNodeLoaderThreadManager;
import nl.mpi.arbil.data.ArbilTreeHelper;
import nl.mpi.arbil.data.LoaderThreadManager;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.ui.GuiHelper;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcher;
import nl.mpi.arbil.util.MessageDialogHandler;
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

    /**
     * Does initial injection into static classes. Needs to be called only once.
     */
    public static synchronized void injectHandlers() {
	final BugCatcher bugCatcher = GuiHelper.linorgBugCatcher;
	ArbilSessionStorage.setBugCatcher(bugCatcher);
	injectBugCatcher(bugCatcher);
	
	final MessageDialogHandler messageDialogHandler = ArbilWindowManager.getSingleInstance();
	ArbilSessionStorage.setMessageDialogHandler(messageDialogHandler);
	injectDialogHandler(messageDialogHandler);
	
	final LoaderThreadManager loaderThreadManager = new ArbilDataNodeLoaderThreadManager();
	injectLoaderThreadManager(loaderThreadManager);
	
	final WindowManager windowManager = ArbilWindowManager.getSingleInstance();	
	ArbilSessionStorage.setWindowManager(windowManager);
	injectWindowManager(windowManager);
	
	final ClipboardOwner clipboardOwner = GuiHelper.getClipboardOwner();
	injectClipboardOwner(clipboardOwner);
	
	ArbilSessionStorage.setBugCatcher(bugCatcher);
	final SessionStorage sessionStorage = ArbilSessionStorage.getSingleInstance();
	injectSessionStorage(sessionStorage);
	
	final TreeHelper treeHelper = ArbilTreeHelper.getSingleInstance();
	injectTreeHelper(treeHelper);
    }
}
