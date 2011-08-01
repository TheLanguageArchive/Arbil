package nl.mpi.arbil.wicket;

import nl.mpi.arbil.data.ArbilDataNodeLoaderThreadManager;
import nl.mpi.arbil.data.LoaderThreadManager;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.TreeHelper;
import org.apache.wicket.Application;
import org.apache.wicket.Request;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebSession;

/**
 * Session for ArbilWicket, keeps user/session specific stuff, such as SessionStorage
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilWicketSession extends WebSession {

    private SessionStorage sessionStorage;
    private TreeHelper treeHelper;
    private ArbilWicketApplication application;
    private LoaderThreadManager loaderThreadManager;

    public ArbilWicketSession(ArbilWicketApplication application, Request request) {
	super(request);
	this.application = application;
    }

    public static ArbilWicketSession get() {
	return (ArbilWicketSession) Session.get();
    }

    /**
     * @return This session's SessionStorage
     */
    public synchronized SessionStorage getSessionStorage() {
	if (sessionStorage == null) {
	    sessionStorage = application.newSessionStorage();
	}
	return sessionStorage;
    }

    /**
     * 
     * @return This session's TreeHelper
     */
    public synchronized TreeHelper getTreeHelper() {
	if (treeHelper == null) {
	    treeHelper = application.newTreeHelper(getSessionStorage());
	}
	return treeHelper;
    }

    /**
     * 
     * @return This session's LoaderThreadManager
     */
    public synchronized LoaderThreadManager getLoaderThreadManager() {
	if (loaderThreadManager == null) {
	    loaderThreadManager = application.newLoaderThreadManager(this);
	}
	return loaderThreadManager;
    }
}
