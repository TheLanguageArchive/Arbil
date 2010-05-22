package nl.mpi.arbil.MetadataFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import mpi.imdi.api.IMDIDom;
import mpi.imdi.api.IMDILink;
import mpi.imdi.api.WSNodeType;
import mpi.util.OurURL;
import nl.mpi.arbil.GuiHelper;
import nl.mpi.arbil.LinorgWindowManager;
import nl.mpi.arbil.data.ImdiTreeObject;
import org.w3c.dom.Document;

/**
 *  Document   : ImdiUtils
 *  Created on : May 21, 2010, 9:30:03 PM
 *  Author     : Peter Withers
 */
public class ImdiUtils implements MetadataUtils {

    public static IMDIDom api = new IMDIDom();

    public boolean addCorpusLink(ImdiTreeObject targetImdiNodes, ImdiTreeObject[] childImdiNodes) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean addCorpusLink(URI nodeURI, URI linkURI) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean copyImdiFile(URI sourceURI, File destinationFile, URI[] linksToUpdate, boolean updateLinks) {
        try {
            mpi.util.OurURL inUrlLocal = new mpi.util.OurURL(sourceURI.toURL());
            mpi.util.OurURL destinationUrl = new mpi.util.OurURL(destinationFile.toURL());

            org.w3c.dom.Document nodDom = api.loadIMDIDocument(inUrlLocal, false);
            if (nodDom == null) {
                GuiHelper.linorgBugCatcher.logError(new Exception(api.getMessage()));
                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Error reading via the IMDI API", "Copy IMDI File");
                return false;
            } else {
                mpi.imdi.api.IMDILink[] links = api.getIMDILinks(nodDom, inUrlLocal, mpi.imdi.api.WSNodeType.UNKNOWN);
                if (links != null && updateLinks) {
                    for (mpi.imdi.api.IMDILink currentLink : links) {
                        for (URI updatableLink : linksToUpdate) {
                            try {
                                if (currentLink.getRawURL().toURL().toURI().equals(updatableLink)) {
                                    api.changeIMDILink(nodDom, destinationUrl, currentLink);
                                }
                            } catch (URISyntaxException exception) {
                                GuiHelper.linorgBugCatcher.logError(exception);
                            }
                        }
                    }
                }
                boolean removeIdAttributes = true;
                api.writeDOM(nodDom, destinationFile, removeIdAttributes);
                return true;
            }
        } catch (IOException e) {
            GuiHelper.linorgBugCatcher.logError(e);
            return false;
        }
    }

    public boolean moveImdiFile(URI sourceURI, File destinationFile, boolean updateLinks) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean removeCorpusLink(ImdiTreeObject targetImdiNodes, ImdiTreeObject[] childImdiNodes) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean removeCorpusLink(URI nodeURI, URI linkURI) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public URI[] getCorpusLinks(URI nodeURI) {
        try {
            OurURL destinationUrl = new OurURL(nodeURI.toString());
            Document nodDom = api.loadIMDIDocument(destinationUrl, false);
            IMDILink[] allImdiLinks;
            allImdiLinks = api.getIMDILinks(nodDom, destinationUrl, WSNodeType.UNKNOWN);
            if (allImdiLinks != null) {
                URI[] returnUriArray = new URI[allImdiLinks.length];
                for (int linkCount = 0; linkCount < allImdiLinks.length; linkCount++) {
                    try {
                        returnUriArray[linkCount] = allImdiLinks[linkCount].getRawURL().toURL().toURI();
                    } catch (URISyntaxException exception) {
                        GuiHelper.linorgBugCatcher.logError(exception);
                        LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Error reading one of the links via the IMDI API", "Get Links");
                    }
                }
                return returnUriArray;
            }

        } catch (MalformedURLException exception) {
            GuiHelper.linorgBugCatcher.logError(exception);
            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Error reading links via the IMDI API", "Get Links");
        }
        return null;
    }

////    public boolean updateSingleLink(URI oldLocationURI, File currentLocationFile) {
////        throw new UnsupportedOperationException("Not supported yet.");
////    }
//    private boolean addCorpusLink(ImdiTreeObject imdiTreeObject, ImdiTreeObject targetImdiNode) {
//        boolean linkAlreadyExists = false;
//        if (targetImdiNode.isCatalogue()) {
//            if (imdiTreeObject.hasCatalogue()) {
////                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Only one catalogue can be added", null);
//                // prevent adding a second catalogue file
//                return false;
//            }
//        }
////        for (String[] currentLinkPair : imdiTreeObject.childLinks) {
////            String currentChildPath = currentLinkPair[0];
////            if (!targetImdiNode.waitTillLoaded()) { // we must wait here before we can tell if it is a catalogue or not
////                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Error adding node, could not wait for file to load", "Loading Error");
////                return false;
////            }
////            if (currentChildPath.equals(targetImdiNode.getUrlString())) {
////                linkAlreadyExists = true;
////            }
////        }
//        if (targetImdiNode.getUrlString().equals(imdiTreeObject.getUrlString())) {
//            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Cannot link or move a node into itself", null);
//            return false;
//        }
//        if (linkAlreadyExists) {
//            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue(targetImdiNode + " already exists in " + this + " and will not be added again", null);
//            return false;
//        } else {
//            // if link is not already there
//            // if needs saving then save now while you can
//            // TODO: it would be nice to warn the user about this, but its a corpus node so maybe it is not important
//            if (imdiTreeObject.getNeedsSaveToDisk()) {
//                imdiTreeObject.saveChangesToCache(true);
//            }
//
//            Document nodDom;
//            try {
//                synchronized (imdiTreeObject.domLockObject) {
//                    OurURL inUrlLocal = new OurURL(imdiTreeObject.getURI().toURL());
//                    nodDom = api.loadIMDIDocument(inUrlLocal, false);
//                    if (nodDom == null) {
//                        GuiHelper.linorgBugCatcher.logError(new Exception(api.getMessage()));
//                        LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Error reading via the IMDI API", "Add Link");
//                        return false;
//                    } else {
//                        int nodeType = WSNodeType.CORPUS;
//                        if (targetImdiNode.isSession()) {
//                            nodeType = WSNodeType.SESSION;            // url: IMDI location, for link normalization.  urlToLink: target URL
//                            // linkName: for CorpusLink name / for InfoFile description
//                            // linkType: WSNodeType value  spec: where to put the link in the IMDI,
//                            // NOTE: spec should only be used for linkType InfoFile...
//                            // public IMDILink createIMDILink(Document doc, OurURL url, String urlToLink, String linkName, int linkType, String spec);
//                        } else if (targetImdiNode.isCatalogue()) {
//                            nodeType = WSNodeType.CATALOGUE;
//                        }
//// TODO: at this point due to the api we cannot get the id of the newly created link, so we will probably have to unload this object and reload the dom
//                        System.out.println("createIMDILink: " + targetImdiNode.getUrlString());
//                        api.createIMDILink(nodDom, inUrlLocal, targetImdiNode.getUrlString(), /*targetImdiNode.toString()*/ "", nodeType, "");
//                        imdiTreeObject.bumpHistory();
//                        api.writeDOM(nodDom, imdiTreeObject.getFile(), false);
//                    }
//                }
//            } catch (Exception ex) {
//                GuiHelper.linorgBugCatcher.logError(ex);
////            System.out.println("Exception: " + ex.getMessage());
//            }
//            //loadChildNodes(); // this must not be done here
////            clearIcon(); // this must be cleared so that the leaf / branch flag gets set
//            return true;
//        }
//    }
//    // this is used to delete an IMDI node from a corpus branch
//
//    private void deleteCorpusLink(ImdiTreeObject imdiTreeObject, ImdiTreeObject[] targetImdiNodes) {
//        // retrieve the node id for the link
//        ArrayList<String> fieldIdList = new ArrayList<String>();
//        String linkIdString = null;
////        for (String[] currentLinkPair : imdiTreeObject.childLinks) {
////            String currentChildPath = currentLinkPair[0];
//////                System.out.println("currentChildPath: " + currentChildPath);
////            for (ImdiTreeObject currentImdiNode : targetImdiNodes) {
//////                    System.out.println("targetImdiNode :  " + currentImdiNode.getUrlString());
////                if (currentChildPath.equals(currentImdiNode.getUrlString())) {
//////                      System.out.println("currentLinkPair[1]: " + currentLinkPair[1]);
////                    linkIdString = currentLinkPair[1];
////                    fieldIdList.add(linkIdString);
////                }
////            }
////        }
//        if (fieldIdList.size() > 0) {
////            todo: this use must stay since it is the only reliable way to delete links via the api
//            //deleteFromDomViaId(fieldIdList.toArray(new String[]{}));
//            GuiHelper.linorgBugCatcher.logError(new Exception("deleteFromDomViaId"));
//        }
//        for (ImdiTreeObject currentImdiNode : targetImdiNodes) {
//            if (currentImdiNode.isCatalogue()) {
//                // the catalogue implemention in the imdi api requires special treatment and so must be done in this way not via deleteFromDomViaId
//                // do this last because it might change the domid ordering
//                deleteCatalogueLink(imdiTreeObject);
//            }
//        }
//        //loadChildNodes(); // this must not be done here
//        imdiTreeObject.clearIcon(); // this must be cleared so that the leaf / branch flag gets set
//    }
//
//    private void deleteCatalogueLink(ImdiTreeObject imdiTreeObject) {
////        todo move this to a separate class and remove the api from this class
//        // the catalogue implemention in the imdi api requires special treatment and so must be done in this way not via deleteFromDomViaId
//        Document nodDom;
//        try {
//            if (imdiTreeObject.getNeedsSaveToDisk()) {
//                imdiTreeObject.saveChangesToCache(false);
//            }
//            synchronized (imdiTreeObject.domLockObject) {
//                OurURL inUrlLocal = new OurURL(imdiTreeObject.getURI().toURL());
//                nodDom = api.loadIMDIDocument(inUrlLocal, false);
//                if (nodDom == null) {
//                    GuiHelper.linorgBugCatcher.logError(new Exception(api.getMessage()));
//                    LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Error loading via the IMDI API", "Remove Catalogue");
//                } else {
//                    api.createIMDILink(nodDom, null, "", "", WSNodeType.CATALOGUE, "");
//                    imdiTreeObject.bumpHistory();
//                    api.writeDOM(nodDom, imdiTreeObject.getFile(), false);
//                    imdiTreeObject.reloadNode();
//                }
//            }
//        } catch (Exception ex) {
//            GuiHelper.linorgBugCatcher.logError(ex);
//        }
//    }
}
