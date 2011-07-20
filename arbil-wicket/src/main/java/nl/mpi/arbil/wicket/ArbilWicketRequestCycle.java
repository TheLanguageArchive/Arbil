package nl.mpi.arbil.wicket;

import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.TreeHelper;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebRequestCycle;

/**
 * Extension of standard WebRequestCycle. Hooks into default request cycle
 * to store the session's (Arbil)SessionStorage object in an InheritableThreadLocal variable
 * so that the session storage is available to the session storage proxy in child threads
 * @see SessionStorage
 * @see ArbilWicketSessionStorageSessionProxy
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilWicketRequestCycle extends WebRequestCycle {

    private final static InheritableThreadLocal<SessionStorage> sessionStorage = new InheritableThreadLocal<SessionStorage>();
    private final static InheritableThreadLocal<TreeHelper> treeHelper = new InheritableThreadLocal<TreeHelper>();

    /**
     * 
     * @return The session's SessionStorage local to this thread or it's parent
     */
    public static SessionStorage getSessionStorage() {
	return sessionStorage.get();
    }

    /**
     * 
     * @return The session's TreeHelper local to this thread or it's parent
     */
    public static TreeHelper getTreeHelper() {
	return treeHelper.get();
    }

    public ArbilWicketRequestCycle(WebApplication application, Request request, Response response) {
	super(application, (WebRequest) request, response);
    }

    @Override
    protected void onBeginRequest() {
	super.onBeginRequest();
	// Take sessionStorage from current session and put in threadlocal variable
	sessionStorage.set(getArbilSession().getSessionStorage());
	// Take treeHelper from current session and put in threadlocal variable
	treeHelper.set(getArbilSession().getTreeHelper());
    }

    @Override
    protected void onEndRequest() {
	super.onEndRequest();
	// Remove sessionStorage from threadlocal variable
	sessionStorage.remove();
	treeHelper.remove();
    }

    ArbilWicketSession getArbilSession() {
	return (ArbilWicketSession) getSession();
    }
}
