package nl.mpi.arbil.util;

import java.net.CookieHandler;
import java.util.Collection;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.task.ArbilTaskListener;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 * @author Peter.Withers@mpi.nl
 */
public class ArbilMimeHashQueue extends DefaultMimeHashQueue {

    private static boolean allowCookies = false; // this is a silly place for this and should find a better home, but the cookies are only dissabled for the permissions test in this class
    private ArbilWindowManager windowManager;

    public ArbilMimeHashQueue(ArbilWindowManager windowManager, SessionStorage sessionStorage) {
	super(sessionStorage);
	this.windowManager = windowManager;
    }

    public void init() {
	CookieHandler.setDefault(new ShibCookieHandler());
	startMimeHashQueueThread();
    }

    /**
     * @param aAllowCookies the allowCookies to set
     */
    public static void setAllowCookies(boolean aAllowCookies) {
	allowCookies = aAllowCookies;
    }

    @Override
    protected Collection<ArbilTaskListener> getTaskListeners() {
	return windowManager.getTaskListeners();
    }
}
