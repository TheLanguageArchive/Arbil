/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.mpi.arbil.util;

import java.io.File;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface BugCatcher {

    File getLogFile();

    void grabApplicationShot();

    void logError(Exception exception);

    void logError(String messageString, Exception exception);

}
