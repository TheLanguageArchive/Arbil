package nl.mpi.arbil.util;

import nl.mpi.arbil.plugin.PluginBugCatcher;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface BugCatcher extends PluginBugCatcher {

    void logError(Exception exception);

    void logError(String messageString, Exception exception);
}
