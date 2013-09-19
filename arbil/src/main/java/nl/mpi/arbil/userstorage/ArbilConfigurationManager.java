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
package nl.mpi.arbil.userstorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilConfigurationManager {

    private static final Logger logger = LoggerFactory.getLogger(ArbilConfigurationManager.class);
    public static final String VERBATIM_XML_TREE_STRUCTURE = "verbatimXmlTreeStructure";
    public static final String COPY_NEW_RESOURCES = "copyNewResources";
    private final SessionStorage sessionStorage;

    public ArbilConfigurationManager(SessionStorage sessionStorage) {
	this.sessionStorage = sessionStorage;
    }

    public synchronized ArbilConfiguration read() {
	logger.info("Reading configuration from session storage");
	
	final ArbilConfiguration configuration = new ArbilConfiguration();
	configuration.setVerbatimXmlTreeStructure(sessionStorage.loadBoolean(VERBATIM_XML_TREE_STRUCTURE, false));
	configuration.setCopyNewResourcesToCache(sessionStorage.loadBoolean(COPY_NEW_RESOURCES, false));
	
	logger.debug("Finished reading configuration:\n{}", configuration);
	return configuration;
    }

    public synchronized void write(ArbilConfiguration configuration) {
	logger.info("Writing configuration to session storage");
	logger.debug("Writing configuration values:\n{}", configuration);
	
	sessionStorage.saveBoolean(VERBATIM_XML_TREE_STRUCTURE, configuration.isVerbatimXmlTreeStructure());
	sessionStorage.saveBoolean(COPY_NEW_RESOURCES, configuration.isCopyNewResourcesToCache());
	
	logger.debug("Finished writing configuration");
    }
}
