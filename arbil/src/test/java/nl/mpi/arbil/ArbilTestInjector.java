package nl.mpi.arbil;

import java.awt.GraphicsEnvironment;
import java.awt.datatransfer.ClipboardOwner;
import nl.mpi.arbil.data.ArbilTreeHelper;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.ui.GuiHelper;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcher;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.TreeHelper;
import nl.mpi.arbil.util.WindowManager;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilTestInjector extends ArbilInjector {

    public static synchronized void injectHandlers() {
	final BugCatcher bugCatcher = GuiHelper.linorgBugCatcher;
	injectBugCatcher(bugCatcher);

	final MessageDialogHandler messageDialogHandler = GraphicsEnvironment.isHeadless() ? new MockDialogHandler() : ArbilWindowManager.getSingleInstance();
	injectDialogHandler(messageDialogHandler);

	final WindowManager windowManager = GraphicsEnvironment.isHeadless() ? new MockWindowManager() : ArbilWindowManager.getSingleInstance();
	injectWindowManager(windowManager);

	final ClipboardOwner clipboardOwner = GuiHelper.getClipboardOwner();
	injectClipboardOwner(clipboardOwner);

	final SessionStorage sessionStorage = new MockSessionStorage();
	ArbilTestInjector.injectSessionStorage(sessionStorage);

	final TreeHelper treeHelper = ArbilTreeHelper.getSingleInstance();
	injectTreeHelper(treeHelper);
    }
}
