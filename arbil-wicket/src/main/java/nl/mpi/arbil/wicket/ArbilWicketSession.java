package nl.mpi.arbil.wicket;

import nl.mpi.arbil.userstorage.SessionStorage;
import org.apache.wicket.Request;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebSession;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilWicketSession extends WebSession {

    private SessionStorage sessionStorage;

    public ArbilWicketSession(Request request, SessionStorage sessionStorage) {
	super(request);
	this.sessionStorage = sessionStorage;
    }

    public static ArbilWicketSession get() {
	return (ArbilWicketSession) Session.get();
    }

    /**
     * @return This session's SessionStorage
     */
    public SessionStorage getSessionStorage() {
	return sessionStorage;
    }
}
