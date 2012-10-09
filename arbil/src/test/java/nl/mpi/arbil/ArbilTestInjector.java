package nl.mpi.arbil;

import java.awt.datatransfer.ClipboardOwner;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.data.ArbilTreeHelper;
import nl.mpi.arbil.data.DataNodeLoader;
import nl.mpi.arbil.ui.GuiHelper;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ApplicationVersionManager;
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

	final ApplicationVersionManager versionManager = new ApplicationVersionManager(new ArbilVersion());
	injectVersionManager(versionManager);

	final BugCatcher bugCatcher = GuiHelper.linorgBugCatcher;
	injectBugCatcher(bugCatcher);

	final MessageDialogHandler messageDialogHandler = new MockDialogHandler();
	injectDialogHandler(messageDialogHandler);

	final WindowManager windowManager = new MockWindowManager();
	injectWindowManager(windowManager);

	final ClipboardOwner clipboardOwner = GuiHelper.getClipboardOwner();
	injectClipboardOwner(clipboardOwner);

	final SessionStorage sessionStorage = new MockSessionStorage();
	ArbilTestInjector.injectSessionStorage(sessionStorage);

	ArbilDataNodeLoader.setSessionStorage(sessionStorage);
	final DataNodeLoader dataNodeLoader = ArbilDataNodeLoader.getSingleInstance();
	injectDataNodeLoader(dataNodeLoader);

	final TreeHelper treeHelper = ArbilTreeHelper.getSingleInstance();
	injectTreeHelper(treeHelper);
    }
}
