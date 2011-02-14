package nl.mpi.arbil.MetadataFile;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import javax.xml.parsers.ParserConfigurationException;
import nl.mpi.arbil.ui.GuiHelper;
import nl.mpi.arbil.data.ArbilComponentBuilder;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *  Document   : CmdiUtils
 *  Created on : May 22, 2010, 10:30:36 AM
 *  Author     : Peter Withers
 */
public class CmdiUtils implements MetadataUtils {

    public boolean addCorpusLink(URI nodeURI, URI[] linkURI) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean copyMetadataFile(URI sourceURI, File destinationFile, URI[][] linksToUpdate, boolean updateLinks) {
        ArbilComponentBuilder cmdiComponentBuilder = new ArbilComponentBuilder();
        try {
            Document document = cmdiComponentBuilder.getDocument(sourceURI);
            // todo: update links
            cmdiComponentBuilder.savePrettyFormatting(document, destinationFile);
            return true;
        } catch (IOException e) {
            GuiHelper.linorgBugCatcher.logError(e);
        } catch (ParserConfigurationException e) {
            GuiHelper.linorgBugCatcher.logError(e);
        } catch (SAXException e) {
            GuiHelper.linorgBugCatcher.logError(e);
        }
        return false;
    }

    public URI[] getCorpusLinks(URI nodeURI) {
        // todo: return links and consider implications of it
//        CmdiComponentLinkReader cmdiComponentLinkReader = new CmdiComponentLinkReader();
//        ArrayList<CmdiResourceLink> currentLinks = cmdiComponentLinkReader.readLinks(nodeURI);
        ArrayList<URI> returnUriList = new ArrayList<URI>();
//        for (CmdiResourceLink currentCmdiResourceLink : currentLinks) {
//            returnUriList.add(cmdiComponentLinkReader.getLinkUrlString(currentCmdiResourceLink.resourceProxyId));
//        }
        return returnUriList.toArray(new URI[]{});
    }

    public boolean moveMetadataFile(URI sourceURI, File destinationFile, boolean updateLinks) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean removeCorpusLink(URI nodeURI, URI[] linkURI) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
