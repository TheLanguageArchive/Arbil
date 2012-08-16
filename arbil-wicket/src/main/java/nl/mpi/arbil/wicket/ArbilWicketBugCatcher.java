package nl.mpi.arbil.wicket;

import nl.mpi.arbil.plugin.PluginException;
import nl.mpi.arbil.util.BugCatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilWicketBugCatcher implements BugCatcher {
    
    private final static Logger logger = LoggerFactory.getLogger(ArbilWicketBugCatcher.class);
    
    public void logError(Exception exception) {
	logger.error(exception.getMessage(), exception);
    }
    
    public void logError(String messageString, Exception exception) {
	logger.error(messageString, exception);
    }
    
    public void logException(PluginException exception) {
	logError(null, exception);
    }
}
