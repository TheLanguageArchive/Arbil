package nl.mpi.arbil.util;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class BugCatcherManager {

    private static BugCatcher bugCatcher;
    private static BugCatcher fallBackBugCatcher;

    /**
     * @return the bugCatcher
     */
    public static synchronized BugCatcher getBugCatcher() {
	if (bugCatcher == null) {
	    if (fallBackBugCatcher == null) {
		System.err.println("BugCatcher requested but no instance has been configured. Using fallback BugCatcher.");
		fallBackBugCatcher = new FallbackBugCatcher();
	    }
	    return fallBackBugCatcher;
	} else {
	    return bugCatcher;
	}
    }

    /**
     * @param aBugCatcher the bugCatcher to set
     */
    public static synchronized void setBugCatcher(BugCatcher aBugCatcher) {
	bugCatcher = aBugCatcher;
    }

    /**
     * Fallback BugCatcher implementation that simply puts the logs on the standard error output
     */
    private static class FallbackBugCatcher implements BugCatcher {

	public void logError(Exception exception) {
	    exception.printStackTrace(System.err);
	}

	public void logError(String messageString, Exception exception) {
	    System.err.println(messageString);
	    exception.printStackTrace(System.err);
	}
    }
}
