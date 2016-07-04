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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class WebstartHelper {

    public static final String WEBSTART_UPDATE_URL_PROPERTY = "nl.mpi.webstartUpdateUrl";
    private final static Logger logger = LoggerFactory.getLogger(WebstartHelper.class);

    public boolean isWebStart() {
	logger.debug("hasWebStartUrl");
	String webstartUpdateUrl = System.getProperty(WEBSTART_UPDATE_URL_PROPERTY);
	logger.debug("webstartUpdateUrl: {}", webstartUpdateUrl);
	return null != webstartUpdateUrl;
    }

    public String getWebstartUrl() {
	return System.getProperty(WEBSTART_UPDATE_URL_PROPERTY);
    }
}
