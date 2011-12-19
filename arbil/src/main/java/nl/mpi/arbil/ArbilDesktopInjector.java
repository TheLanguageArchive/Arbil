package nl.mpi.arbil;

import java.awt.datatransfer.ClipboardOwner;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.data.ArbilTreeHelper;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.ui.GuiHelper;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.ApplicationVersionManager;
import nl.mpi.arbil.util.ArbilBugCatcher;
import nl.mpi.arbil.util.ArbilMimeHashQueue;
import nl.mpi.arbil.util.MessageDialogHandler;

/**
 * Takes care of injecting certain class instances into objects or classes.
 * This provides us with a sort of dependency injection, which enables loosening
 * the coupling between for example data classes and UI classes.
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilDesktopInjector extends ArbilSwingInjector {

    private ArbilTreeHelper treeHelper;
    private ArbilMimeHashQueue mimeHashQueue;
    private ArbilWindowManager windowManager;
    private ArbilBugCatcher bugCatcher;
    private ArbilDataNodeLoader dataNodeLoader;

    public synchronized void injectHandlers() {
	injectHandlers(new ApplicationVersionManager(new ArbilVersion()));
    }

    /**
     * Does initial injection into static classes. Needs to be called only once.
     */
    public synchronized void injectHandlers(final ApplicationVersionManager versionManager) {
	injectVersionManager(versionManager);

	final SessionStorage sessionStorage = new ArbilSessionStorage();
	ArbilBugCatcher.setSessionStorage(sessionStorage);
	ArbilDataNodeLoader.setSessionStorage(sessionStorage);
	ArbilMimeHashQueue.setSessionStorage(sessionStorage);
	ArbilWindowManager.setSessionStorage(sessionStorage);
	injectSessionStorage(sessionStorage);

	bugCatcher = new ArbilBugCatcher();
	ArbilWindowManager.setBugCatcher(bugCatcher);
	ArbilSessionStorage.setBugCatcher(bugCatcher);
	ArbilMimeHashQueue.setBugCatcher(bugCatcher);
	injectBugCatcher(bugCatcher);

	windowManager = new ArbilWindowManager();

	final MessageDialogHandler messageDialogHandler = windowManager;
	ArbilSessionStorage.setMessageDialogHandler(messageDialogHandler);
	ArbilMimeHashQueue.setMessageDialogHandler(messageDialogHandler);
	injectDialogHandler(messageDialogHandler);

	ArbilSessionStorage.setWindowManager(windowManager);
	injectWindowManager(windowManager);

	final ClipboardOwner clipboardOwner = GuiHelper.getClipboardOwner();
	injectClipboardOwner(clipboardOwner);

	mimeHashQueue = new ArbilMimeHashQueue(windowManager);
	injectMimeHashQueue(mimeHashQueue);

	dataNodeLoader = new ArbilDataNodeLoader();
	ArbilMimeHashQueue.setDataNodeLoader(dataNodeLoader);
	ArbilWindowManager.setDataNodeLoader(dataNodeLoader);
	injectDataNodeLoader(dataNodeLoader);

	treeHelper = new ArbilTreeHelper();
	ArbilWindowManager.setTreeHelper(treeHelper);
	ArbilSessionStorage.setTreeHelper(treeHelper);
	injectTreeHelper(treeHelper);
    }

    /**
     * Should not be called before injectHandlers()!!
     * @return the treeHelper
     */
    public ArbilTreeHelper getTreeHelper() {
	return treeHelper;
    }

    /**
     * Should not be called before injectHandlers()!!
     * @return the treeHelper
     */
    public ArbilMimeHashQueue getMimeHashQueue() {
	return mimeHashQueue;
    }

    /**
     * Should not be called before injectHandlers()!!
     * @return the treeHelper
     */
    public ArbilWindowManager getWindowManager() {
	return windowManager;
    }

    /**
     * Should not be called before injectHandlers()!!
     * @return the treeHelper
     */
    public ArbilBugCatcher getBugCatcher() {
	return bugCatcher;
    }

    /**
     * Should not be called before injectHandlers()!!
     * @return the treeHelper
     */
    public ArbilDataNodeLoader getDataNodeLoader() {
	return dataNodeLoader;
    }
}
