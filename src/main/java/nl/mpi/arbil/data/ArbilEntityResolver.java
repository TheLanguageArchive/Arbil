package nl.mpi.arbil.data;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcher;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *  Document   : ArbilEntityResolver
 *  Created on : Sep 16, 2010, 1:36:19 PM
 *  Author     : Peter Withers
 */
public class ArbilEntityResolver implements EntityResolver {

    private static BugCatcher bugCatcher;

    public static void setBugCatcher(BugCatcher bugCatcherInstance) {
        bugCatcher = bugCatcherInstance;
    }
    private static SessionStorage sessionStorage;

    public static void setSessionStorage(SessionStorage sessionStorageInstance) {
	sessionStorage = sessionStorageInstance;
    }
    private URI parentUri;

    public ArbilEntityResolver(URI parentUriLocal) {
        parentUri = parentUriLocal;
    }

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
//        System.out.println("publicId: " + publicId);
//        System.out.println("systemId: " + systemId);
        String targetString;
        // todo: test with http://www.loc.gov/ndnp/xml/alto-1-2.xsd issue: exception: no protocol: ./xlink.xsd need to resolve local references
        if (parentUri != null) {
            URI resolvedSystemIdURL = parentUri.resolve(systemId);
            targetString = resolvedSystemIdURL.toString();
        } else {
            targetString = systemId;
        }
        File cachedfile = sessionStorage.updateCache(targetString, 7);
        if (!cachedfile.exists()) {
            // todo: pull the file out of the jar
            bugCatcher.logError(new Exception("dependant xsd not stored in the jar for offline first time use: " + cachedfile));
        }
        String cachedfileString = cachedfile.toURI().toString();
//        System.out.println("cachedfileString: " + cachedfileString);
        return new InputSource(cachedfileString);
        // to use the default behaviour by returning null
    }
}
