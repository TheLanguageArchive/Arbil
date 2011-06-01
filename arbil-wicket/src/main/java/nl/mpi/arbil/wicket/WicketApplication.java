package nl.mpi.arbil.wicket;

import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.wicket.pages.HomePage;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebApplication;

/**
 * Application object for your web application. If you want to run this application without deploying, run the Start class.
 * 
 * @see nl.mpi.arbil.wicket.Start#main(String[])
 */
public class WicketApplication extends WebApplication {

    /**
     * Constructor
     */
    public WicketApplication() {
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
	return new ArbilWicketSession(request, ArbilSessionStorage.getSingleInstance());
    }
}
