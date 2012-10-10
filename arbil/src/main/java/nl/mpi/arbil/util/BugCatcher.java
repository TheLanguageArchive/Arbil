/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

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
