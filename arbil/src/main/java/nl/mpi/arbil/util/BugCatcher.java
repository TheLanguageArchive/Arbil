package nl.mpi.arbil.util;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface BugCatcher {

    void logError(Exception exception);

    void logError(String messageString, Exception exception);

}
