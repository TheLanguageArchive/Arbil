/**
 * Copyright (C) 2016 The Language Archive, Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.arbil.util;

import nl.mpi.flap.plugin.PluginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BugCatcher that redirects all logged error messages to {@link Logger#error(java.lang.String, java.lang.Throwable) }
 *
 * @author Peter.Withers@mpi.nl
 * @author Twan.Goosen@mpi.nl
 */
public class LoggingBugCatcher implements BugCatcher {

    private final static Logger logger = LoggerFactory.getLogger(LoggingBugCatcher.class);

    public void logException(PluginException exception) {
	logError("plugin error: ", exception);
    }

    public void logError(Exception exception) {
	logError("", exception);
    }

    public void logError(String messageString, Exception exception) {
	logger.error(messageString, exception);
    }
}
