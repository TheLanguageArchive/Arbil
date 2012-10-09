package nl.mpi.arbil.util;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.mpi.arbil.data.ArbilEntityResolver;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.InputSource;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilResourceResolver implements LSResourceResolver {
    //private CatalogResolver catRes = new CatalogResolver();

    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
	try {
	    ArbilEntityResolver resolver = new ArbilEntityResolver(new URI(baseURI));
	    InputSource resolveEntity = resolver.resolveEntity(publicId,systemId);

	    DOMImplementationLS domImplementation;
	    try {
		domImplementation = (DOMImplementationLS) DOMImplementationRegistry.newInstance().getDOMImplementation("LS");
	    } catch (ClassCastException e) {
		throw new RuntimeException(e);
	    } catch (ClassNotFoundException e) {
		throw new RuntimeException(e);
	    } catch (InstantiationException e) {
		throw new RuntimeException(e);
	    } catch (IllegalAccessException e) {
		throw new RuntimeException(e);
	    }
	    LSInput lsInput = domImplementation.createLSInput();
	    lsInput.setByteStream(resolveEntity.getByteStream());
	    lsInput.setCharacterStream(resolveEntity.getCharacterStream());
	    return lsInput;
	} catch (Exception ex) {
	    Logger.getLogger(ArbilResourceResolver.class.getName()).log(Level.SEVERE, null, ex);
	}
	return null;
    }
}
