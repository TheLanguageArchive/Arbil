package nl.mpi.arbil;

import java.io.File;
import java.io.IOException;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *  Document   : ArbilEntityResolver
 *  Created on : Sep 16, 2010, 1:36:19 PM
 *  Author     : Peter Withers
 */
public class ArbilEntityResolver implements EntityResolver {

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
//        System.out.println("publicId: " + publicId);
//        System.out.println("systemId: " + systemId);
        File cachedfile = LinorgSessionStorage.getSingleInstance().updateCache(systemId, 7);
        if (!cachedfile.exists()) {
            // todo: pull the file out of the jar
            new LinorgBugCatcher().logError(new Exception("dependant xsd not stored in the jar for offline first time use: " + cachedfile));
        }
        String cachedfileString = cachedfile.toURI().toString();
//        System.out.println("cachedfileString: " + cachedfileString);
        return new InputSource(cachedfileString);
        // to use the default behaviour by returning null
    }
}
