package nl.mpi.arbil.wicket;

import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.userstorage.SessionStorage;
import org.apache.wicket.Request;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebSession;

/**
 * Session for ArbilWicket, keeps user/session specific stuff, such as SessionStorage
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilWicketSession extends WebSession {

    private SessionStorage sessionStorage;
    private ArbilWicketApplication application;

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
}
