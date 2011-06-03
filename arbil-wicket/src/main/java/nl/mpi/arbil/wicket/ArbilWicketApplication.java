package nl.mpi.arbil.wicket;

import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.wicket.pages.HomePage;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebApplication;

public class ArbilWicketApplication extends WebApplication {

    /**
     * Constructor
     */
    public ArbilWicketApplication() {
	ArbilWicketInjector.injectHandlers();
    }

    /**
     * @see org.apache.wicket.Application#getHomePage()
     */
    public Class<HomePage> getHomePage() {
	return HomePage.class;
    }

    @Override
    public Session newSession(Request request, Response response) {
	return new ArbilWicketSession(this, request);
    }

    @Override
    public RequestCycle newRequestCycle(Request request, Response response) {
	return new ArbilWicketRequestCycle(this, request, response);
    }

    /**
     * Creates a new arbil SessionStorage
     * @return New session storage object
     */
    public SessionStorage newSessionStorage() {
	return ArbilSessionStorage.getSingleInstance();
    }
}
