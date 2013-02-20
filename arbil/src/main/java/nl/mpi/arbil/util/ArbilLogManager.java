/*
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package nl.mpi.arbil.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import nl.mpi.arbil.userstorage.CommonsSessionStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public final class ArbilLogManager {

    private final static Logger logger = LoggerFactory.getLogger(ArbilLogManager.class);
    public static final int DEFAULT_MAX_LOG_FILE_SIZE = 2 * 1024 * 1024;
    public static final int DEFAULT_MAX_LOG_FILE_COUNT = 10;

    public boolean configureLoggingFromResource(String resourceName) {
	return configureLogging(ArbilLogManager.class.getResourceAsStream(resourceName));
    }

    public boolean configureLogging(InputStream configurationStream) {
	try {
	    LogManager.getLogManager().readConfiguration(configurationStream);
	    return true;
	} catch (IOException ex) {
	    logger.error("Could not configure initial logging", ex);
	} catch (SecurityException ex) {
	    logger.error("Could not configure initial logging", ex);
	}
	return false;
    }

    /**
     * Removes the existing file logger and creates a new one with the specified file name and log level
     *
     * @param logFile file to log to
     * @param level log level to use for the file
     * @throws IOException if there are IO problems opening the log file
     */
    public void setLogFile(File logFile, Level level) throws IOException {
	final java.util.logging.Logger defaultLogger = LogManager.getLogManager().getLogger("");
	for (Handler handler : defaultLogger.getHandlers()) {
	    if (handler instanceof FileHandler) {
		((FileHandler) handler).close();
	    }
	}
	final FileHandler handler = new FileHandler(logFile.getAbsolutePath(), DEFAULT_MAX_LOG_FILE_SIZE, DEFAULT_MAX_LOG_FILE_COUNT, true);
	handler.setLevel(level);
	defaultLogger.addHandler(handler);
    }

    /**
     * Configures logging from session storages. Attempts in the following order:
     * <ol>
     * <li>Whether there is a logging.properties file in the
     * {@link CommonsSessionStorage#getApplicationSettingsDirectory() application settings storage directory} and if so uses that to
     * configure logging</li>
     * <li>If not, configures the logger to write to an 'arbil.log' file in the application settings directory</li>
     * </ol>
     *
     * @param sessionStorage session storage to base logging configuration on
     * @return whether logging was successfully configured
     */
    public boolean configureLoggingFromSessionStorage(CommonsSessionStorage sessionStorage) {
	final File applicationSettingsDirectory = sessionStorage.getApplicationSettingsDirectory();

	final File loggingPropertiesFile = new File(applicationSettingsDirectory, "logging.properties");
	final boolean foundCustomLoggingProperties = configureFromPropertiesFile(loggingPropertiesFile);

	if (foundCustomLoggingProperties) {
	    return true;
	} else {
	    try {
		// Try to configure a log file in the application storage directory
		final File logFile = new File(applicationSettingsDirectory, "arbil.log");
		logger.debug("Reconfiguring logging to write to {}", logFile);
		setLogFile(logFile, Level.INFO);
		logger.debug("Reconfigured logging to write to {}", logFile);
		return true;
	    } catch (IOException ex) {
		logger.warn("Could not configure log file in session storage directory", ex);
		return false;
	    }
	}
    }

    /**
     * Reads logging configuration from a logging.properties file
     *
     * @param sessionStorage
     * @return whether successful
     */
    private boolean configureFromPropertiesFile(File loggingProperties) {
	logger.debug("Looking for logging logging properties at {}", loggingProperties);
	if (loggingProperties.exists()) {
	    try {
		configureLogging(new FileInputStream(loggingProperties));
		logger.debug("Reconfigured logging from logging properties at {}", loggingProperties);
		return true;
	    } catch (FileNotFoundException ex) {
		// should not occur, existence has been checked for
		logger.error("Could not find file {}", loggingProperties, ex);
	    }
	}
	return false;
    }
}
