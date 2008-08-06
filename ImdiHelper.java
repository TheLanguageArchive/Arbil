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
import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author petwit
 */
public class ImdiHelper {

    //private static OurURL baseURL = null; // for link resolution
    private static IMDIDom api = new IMDIDom();

    public ImdiTreeObject getTreeNodeObject(String urlString) {
        ImdiTreeObject imdiTreeObject;
        imdiTreeObject = new ImdiTreeObject(null, urlString);
        if (urlString.endsWith(".imdi")) {
            imdiTreeObject.loadImdiDom();
        }
        return imdiTreeObject;
    }//    static Icon collapsedicon = new ImageIcon("/icons/Opener_open_black.png");
    public class ImdiTreeObject {

        protected ImdiTreeObject(String localNodeText, String localUrlString) {
            nodeText = localNodeText;
            nodDom = null;
            urlString = localUrlString;
            //icon = null;//idleIcon;//null;//new ImageIcon(getClass().getResource(imageName)); 
            isDirectory = false;
            if (!isImdi() && isLocal()) {
                File fileObject = getFile();
                if (fileObject != null) {
                    this.isDirectory = fileObject.isDirectory();
                }
                nodeText = fileObject.getName();
            }
            if (!isImdi() && nodeText == null) {
                nodeText = urlString;
            }
        }

        public void loadImdiDom() {
            try {
                OurURL inUrlLocal = null;
                inUrlLocal = new OurURL(urlString);
                nodDom = api.loadIMDIDocument(inUrlLocal, false);
                if (nodDom == null) {
                    nodeText = "Could not load IMDI";
                } else {
                    nodeText = "unknown";
                    IMDIElement ie = api.getIMDIElement(nodDom, "Session.Name");
                    if (ie != null) {
                        nodeText = "SN: " + ie.getValue();
                        isSession = true;
                    } else {
                        IMDIElement corpusname = api.getIMDIElement(nodDom, "Corpus.Name");
                        if (corpusname != null) {
                            nodeText = "CN: " + corpusname.getValue();
                        }
                    }
                }
            } catch (MalformedURLException mue) {
                System.out.println("Invalid input URL: " + mue);
                nodeText = "Invalid input URL";
            }
        }

        public String getField(String fieldName) {
            Document itemDom = this.getNodeDom();
            IMDIElement rowValue = api.getIMDIElement(itemDom, fieldName);
            if (rowValue != null) {
                return rowValue.getValue();
            } else {
                return null;
            }
        }

        private String[] getLinks() {
            String[] returnArray = null;
            if (this.isDirectory()) {
                File nodeFile = this.getFile();
                returnArray = nodeFile.list();
                for (int linkCount = 0; linkCount < returnArray.length; linkCount++) {
                    returnArray[linkCount] = this.getUrl() + returnArray[linkCount];
                }
            } else {
                try {
                    if (this.getNodeDom() != null) {
                        OurURL baseURL = new OurURL(this.getUrl());
                        IMDILink[] links = api.getIMDILinks(this.getNodeDom(), baseURL, WSNodeType.UNKNOWN);
                        if (links != null) {
                            returnArray = new String[links.length];
                            for (int linkCount = 0; linkCount < links.length; linkCount++) {
                                returnArray[linkCount] = links[linkCount].getRawURL().toString();
                                System.out.println("link:" + returnArray[linkCount]);
                            }
                        }
                    }
                } catch (MalformedURLException mue) {
                    System.out.println("Error getting links: " + mue);
                    returnArray = new String[]{"Invalid input file from parent"};
                }
            }
            return returnArray;
        }

        public ImdiTreeObject[] getChildren(String[] imdiFieldArray) {
            String[] linkArray = getLinks();
            //ImdiTreeObject[] returnImdiArray = new ImdiTreeObject[linkArray.length + imdiFieldArray.length];
            Vector tempImdiVector = new Vector();
            if (linkArray != null) {
                for (int linkCount = 0; linkCount < linkArray.length && linkCount < 10; linkCount++) {
                    ImdiTreeObject currentImdi = new ImdiTreeObject(null, linkArray[linkCount]);
                    tempImdiVector.add(currentImdi);
                    if (linkArray[linkCount].endsWith(".imdi") && linkCount < 9) {
                        currentImdi.loadImdiDom();
                    }
                }
            }
            for (int rowNameCounter = 0; rowNameCounter < imdiFieldArray.length; rowNameCounter++) {
                if (-1 < imdiFieldArray[rowNameCounter].indexOf("(X)")) {
                    int itemValueCounter = 1;
                    boolean valueFound = true;
                    while (valueFound) {
                        String[] splitFieldName = imdiFieldArray[rowNameCounter].split("\\(X\\)");
                        String currentFieldName = splitFieldName[0] + "(" + itemValueCounter + ")";
                        System.out.println(imdiFieldArray[rowNameCounter] + " splitFieldName: " + splitFieldName.length);
                        System.out.println("currentFieldName: " + currentFieldName);
                        
                        String cellValue =  this.getField(currentFieldName);// + splitFieldName[1]); // this does not check for short arrays nor for multiple (X)'s
                        valueFound = cellValue != null;
                        if (valueFound && cellValue.length() > 0) {
                            //tempImdiVector.add(new ImdiTreeObject(cellValue, cellValue));
                            tempImdiVector.add(new ImdiTreeObject(currentFieldName, null));
                        }
                        itemValueCounter++;
                    }
                }
            }
            ImdiTreeObject[] returnImdiArray = new ImdiTreeObject[tempImdiVector.size()];
            tempImdiVector.toArray(returnImdiArray);
            return returnImdiArray;
        }

        public Hashtable getFields() {
            // store the Hastable for next call
            // if hastable is null then load from imdi
            return new Hashtable();
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
            return isDirectory;
        }

        public boolean isImdi() {
            if (urlString != null /* && nodDom != null*/) {
                return urlString.endsWith(".imdi");
            } else {
                return false;
            }
        }

        public boolean isSession() {
            return isSession;
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
        private String nodeText;
        private Document nodDom;
        private String urlString;
        private boolean isDirectory;
        private boolean isSession;
        //protected Icon icon;
    }
}
