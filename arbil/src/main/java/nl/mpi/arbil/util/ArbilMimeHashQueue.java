package nl.mpi.arbil.util;

import java.net.CookieHandler;
import java.util.Collection;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.util.task.ArbilTaskListener;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 * @author Peter.Withers@mpi.nl
 */
public class ArbilMimeHashQueue extends DefaultMimeHashQueue {

    private static boolean allowCookies = false; // this is a silly place for this and should find a better home, but the cookies are only dissabled for the permissions test in this class
    private static ArbilMimeHashQueue singleInstance = null;

    static synchronized public ArbilMimeHashQueue getSingleInstance() {
//        System.out.println("DefaultMimeHashQueue getSingleInstance");
	if (singleInstance == null) {
	    if (!allowCookies) {
		CookieHandler.setDefault(new ShibCookieHandler());
	    }
	    singleInstance = new ArbilMimeHashQueue();
	    singleInstance.startMimeHashQueueThread();
//            System.out.println("CookieHandler: " + java.net.CookieHandler.class.getResource("/META-INF/MANIFEST.MF"));
//            System.out.println("CookieHandler: " + java.net.CookieHandler.class.getResource("/java/net/CookieHandler.class"));
	}
	return singleInstance;
    }

    private ArbilMimeHashQueue() {
	super();
    }

    /**
     * @param aAllowCookies the allowCookies to set
     */
    public static void setAllowCookies(boolean aAllowCookies) {
	allowCookies = aAllowCookies;
    }

    @Override
    protected Collection<ArbilTaskListener> getTaskListeners() {
	return ArbilWindowManager.getSingleInstance().getTaskListeners();
    }
}
