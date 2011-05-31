package nl.mpi.arbil.wicket;

import nl.mpi.arbil.ArbilInjector;
import nl.mpi.arbil.wicket.pages.HomePage;
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
	ArbilInjector.injectHandlers();
    }

    /**
     * @see org.apache.wicket.Application#getHomePage()
     */
    public Class<HomePage> getHomePage() {
	return HomePage.class;
    }
}
