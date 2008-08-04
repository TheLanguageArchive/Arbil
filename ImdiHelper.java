/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import mpi.imdi.api.*;
import mpi.util.OurURL;
import org.w3c.dom.Document;
import java.net.MalformedURLException;
import java.io.File;

/**
 *
 * @author petwit
 */
public class ImdiHelper {

    //private static OurURL baseURL = null; // for link resolution
    private static IMDIDom api = new IMDIDom();

    public ImdiTreeObject getTreeNodeObject(String urlString) {
        OurURL inUrlLocal = null;
        ImdiTreeObject imdiTreeObject;
        if (!urlString.endsWith(".imdi")) {
            imdiTreeObject = new ImdiTreeObject(null, null, urlString);
        } else {
            try {
                inUrlLocal = new OurURL(urlString);
                imdiTreeObject = getImdiTreeNode(inUrlLocal);
            } catch (MalformedURLException mue) {
                System.out.println("Invalid input URL: " + mue);
                imdiTreeObject = new ImdiTreeObject("Invalid input URL", null, urlString);
            }
        }
        return imdiTreeObject;
    }

    public ImdiTreeObject getImdiTreeNode(OurURL inUrl) {
        ImdiTreeObject imdiTreeObject;
        String fullUrlSting = inUrl.getProtocol() + "://" + inUrl.getHost() + inUrl.getPath();
        Document dom = null;

        if (!inUrl.getFile().endsWith(".imdi")) {
            imdiTreeObject = new ImdiTreeObject(inUrl.getFile(), null, fullUrlSting);
        } else {
            dom = api.loadIMDIDocument(inUrl, false);

            if (dom == null) {
                imdiTreeObject = new ImdiTreeObject("Could not load IMDI", null, fullUrlSting);
            } else {
                String nodeName = "unknown";
                IMDIElement ie = api.getIMDIElement(dom, "Session.Name");
                if (ie != null) {
                    nodeName = "SN: " + ie.getValue();
                } else {
                    IMDIElement corpusname = api.getIMDIElement(dom, "Corpus.Name");
                    if (corpusname != null) {
                        nodeName = "CN: " + corpusname.getValue();
                    }
                }
                imdiTreeObject = new ImdiTreeObject(nodeName, dom, fullUrlSting);
            }
        }
        return imdiTreeObject;
    }

    public String getField(ImdiTreeObject imdiTreeObject, String fieldName) {
        Document itemDom = imdiTreeObject.getNodeDom();
        IMDIElement rowValue = api.getIMDIElement(itemDom, fieldName);
        if (rowValue != null) {
            return rowValue.getValue();
        } else {
            return null;
        }
    }

    public String[] getLinks(ImdiTreeObject imdiTreeObject) {
        String[] returnArray = null;
        if (imdiTreeObject.isDirectory()) {
            File nodeFile = imdiTreeObject.getFile();
            //System.out.println("Listing: " + nodeFile.toURI());
            returnArray = nodeFile.list();
            for (int linkCount = 0; linkCount < returnArray.length; linkCount++) {
                returnArray[linkCount] = imdiTreeObject.getUrl() + returnArray[linkCount];
            }
        //System.out.println("Listing: " + nodeFile.toURI() + ":" + returnArray);
        } else {
            try {
                OurURL baseURL = new OurURL(imdiTreeObject.getUrl());
                IMDILink[] links = api.getIMDILinks(imdiTreeObject.getNodeDom(), baseURL, WSNodeType.UNKNOWN);
                if (links != null) {
                    returnArray = new String[links.length];
                    for (int linkCount = 0; linkCount < links.length; linkCount++) {
                        returnArray[linkCount] = links[linkCount].getRawURL().toString();
                        System.out.println("link:" + returnArray[linkCount]);
                    }
                }
            } catch (MalformedURLException mue) {
                System.out.println("Error getting links: " + mue);
                returnArray = new String[]{"Invalid input file from parent"};
            }
        }
        return returnArray;
    }

    public class ImdiTreeObject {

        protected ImdiTreeObject(String localNodeText, Document localNodeDom, String localUrlString) {
            this.nodeText = localNodeText;
            this.nodDom = localNodeDom;
            this.urlString = localUrlString;
            //this.icon = //new ImageIcon(getClass().getResource(imageName)); 
            this.IsDirectory = false;
            if (!isImdi() && isLocal()) {
                File fileObject = getFile();
                if (fileObject != null) {
                    this.IsDirectory = fileObject.isDirectory();
                }
                nodeText = fileObject.getName();
            }
            if (nodeText == null){
                nodeText = urlString;
            }
        }
        // Return text for display
        public String toString() {
            return nodeText;
        }

        public Document getNodeDom() {
            return nodDom;
        }

        public String getUrl() {
            return urlString;
        }

        public boolean isDirectory() {
            return IsDirectory;
        }

        public boolean isImdi() {
            if (urlString != null && nodDom != null) {
                return urlString.endsWith(".imdi");
            } else {
                return false;
            }
        }

        public boolean isLocal() {
            if (urlString != null) {
                return urlString.startsWith("file://");
            } else {
                return false;
            }
        }

        public File getFile() {
            return new File(urlString.replaceFirst("file://", "/"));
        }
//        // Return the icon
//        public Icon getIcon() {
//            return icon;
//        }
        protected String nodeText;
        protected Document nodDom;
        protected String urlString;
        protected boolean IsDirectory;
        //protected Icon icon;
    }
}
