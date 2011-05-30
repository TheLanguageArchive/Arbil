package nl.mpi.arbil;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.mpi.arbil.util.BugCatcher;

public class MockBugCatcher implements BugCatcher {

    private static final Logger log = Logger.getLogger(MockBugCatcher.class.toString());

    public void logError(Exception exception) {
	log.log(Level.SEVERE, exception.getMessage(), exception);
    }

    public void logError(String messageString, Exception exception) {
	log.log(Level.SEVERE, messageString, exception);
    }

    public File getLogFile() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    public void grabApplicationShot() {
	throw new UnsupportedOperationException("Not supported yet.");
    }
}
