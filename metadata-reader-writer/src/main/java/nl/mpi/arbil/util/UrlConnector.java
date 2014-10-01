/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for
 * Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.arbil.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility that can provide a {@link URLConnection} for a given {@link URI},
 * while (by default) following redirects
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 * @author Peter Withers
 */
public class UrlConnector {

    private final static Logger logger = LoggerFactory.getLogger(UrlConnector.class);

    private final URI location;
    private URI redirectedUri;
    private URLConnection connection;

    private boolean followRedirects = true;
    private int maxRedirects = 5;

    /**
     * Creates the connector, but will not attempt to create the actual
     * connection; for this, use {@link #connect() } after construction
     *
     * @param location URI to connect to
     */
    public UrlConnector(URI location) {
        this.location = location;
    }

    /**
     *
     * @return the original location
     */
    public URI getLocation() {
        return location;
    }

    /**
     *
     * @return the redirected location, will be null as long as {@link #connect()
     * } has not been called
     */
    public URI getRedirectedUri() {
        return redirectedUri;
    }

    /**
     *
     * @return the obtained connection, will be null as long as {@link #connect()
     * } has not been called
     */
    public URLConnection getConnection() {
        return connection;
    }

    /**
     *
     * @param followRedirects whether to follow redirects (true by default)
     */
    public void setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    /**
     *
     * @param maxRedirects maximum number of redirects to follow (5 by default)
     */
    public void setMaxRedirects(int maxRedirects) {
        this.maxRedirects = maxRedirects;
    }

    /**
     * Opens a connection, handling redirects if enabled
     *
     * @return whether a connection has been established successfully
     * @see #setFollowRedirects(boolean)
     * @throws IOException
     */
    public boolean connect() throws IOException {
        // start with a 'redirect' at the original location
        redirectedUri = location;
        connection = location.toURL().openConnection();

        if (followRedirects && maxRedirects > 0) {
            return followRedirects();
        } else {
            return true;
        }
    }

    private boolean followRedirects() throws IOException {
        int redirectsLeft = maxRedirects;
        while (redirectsLeft >= 0) {
            redirectsLeft--;
            if (connection instanceof HttpURLConnection) {
                // make connection and check response code
                final HttpURLConnection httpConnection = (HttpURLConnection) connection;
                httpConnection.setInstanceFollowRedirects(true);
                final int stat = httpConnection.getResponseCode();

                if (stat >= 300 && stat <= 307 && stat != 306 && stat != HttpURLConnection.HTTP_NOT_MODIFIED) {
                    logger.debug("Encountered HTTP {} at {}. {} redirects left.", stat, redirectedUri, maxRedirects);
                    try {
                        connection = getConnectionFromRedirect(httpConnection, redirectedUri);
                        if (connection == null) {
                            logger.warn("Connection not established");
                            return false;
                        }
                    } catch (URISyntaxException ex) {
                        logger.error("Encountered illegal location while following redirect", ex);
                        return false;
                    }
                } else {
                    // we got a non-redirect response this time
                    return true;
                }
            } else {
                // not an http URL, assume this goes well
                return true;
            }
        }

        logger.warn("Stopped following redirects after {} hops", maxRedirects);
        return false;
    }

    private URLConnection getConnectionFromRedirect(HttpURLConnection currentConnection, URI currentLocation) throws URISyntaxException, IOException {
        final String locationField = currentConnection.getHeaderField("Location");
        logger.debug("Redirected to {}", locationField);
        if (locationField != null) {
            redirectedUri = new URI(locationField);

            if (isIllegalRedirect(currentLocation, redirectedUri)) {
                logger.error("Redirect from HTTPS to HTTP detected: {} -> {}", currentLocation, redirectedUri);
                return null;
            } else {
                return redirectedUri.toURL().openConnection();
            }
        } else {
            logger.error("No location field provided for redirect (HTTP {}) at {}", currentConnection.getResponseCode(), currentLocation);
            return null;
        }
    }

    private boolean isIllegalRedirect(URI source, URI target) {
        return source.getScheme().equalsIgnoreCase("https")
                && target.getScheme().equalsIgnoreCase("http");
    }

}
