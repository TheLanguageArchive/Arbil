package nl.mpi.arbil.util;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Document   : ShibCookieHandler
 * Created on : Monday Dec 11 15:05:21 CET 2010
 * @author Peter.Withers@mpi.nl
 */
public class ShibCookieHandler extends CookieHandler {

    @Override
    public Map<String, List<String>> get(URI uri, Map<String, List<String>> requestHeaders) throws IOException {
//        throw new UnsupportedOperationException("Not supported yet.");
        return new HashMap<String, List<String>>();
    }

    @Override
    public void put(URI uri, Map<String, List<String>> responseHeaders) throws IOException {
        // block all cookies by ignoring this call
//        throw new UnsupportedOperationException("Not supported yet.");
    }
}
