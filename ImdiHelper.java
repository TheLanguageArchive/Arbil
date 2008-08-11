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
import java.util.Enumeration;
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

        Hashtable fieldHashtable = new Hashtable();
        Hashtable childrenHashtable = new Hashtable();
        boolean imdiDataLoaded = false;

        protected ImdiTreeObject(String localNodeText, String localUrlString) {
            nodeText = localNodeText;
            nodDom = null;
            urlString = localUrlString;
            //icon = null;//idleIcon;//null;//new ImageIcon(getClass().getResource(imageName)); 
            isDirectory = false;
            if (urlString != null) {
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

        private String getField(String fieldName) {
            Document itemDom = this.getNodeDom();
            if (itemDom == null) {
                return null;
            }
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

        private boolean populateChildFields(String fieldNameString) {
            // this be called when loading children or loading fields
            System.out.println("fieldNameString: " + fieldNameString);
            boolean valueFound = false;
            int counterFieldPosition = fieldNameString.indexOf("(X)");
            if (-1 < counterFieldPosition) {
                int itemValueCounter = 1;
                valueFound = true;
                String firstHalf = fieldNameString.substring(0, counterFieldPosition + 1);
                String secondHalf = fieldNameString.substring(counterFieldPosition + 2);
                while (valueFound) {
                    fieldNameString = firstHalf + itemValueCounter + secondHalf;
                    if (-1 < fieldNameString.indexOf("(X)")) {
                        valueFound = populateChildFields(fieldNameString);
                    } else {
                        System.out.println("checking x value for: " + fieldNameString);
                        String cellValue = this.getField(fieldNameString);
                        valueFound = cellValue != null;
                        if (valueFound && cellValue.length() > 0) {
                            this.addField(fieldNameString, cellValue);
                        }
                    }
                    itemValueCounter++;
                }
            } else {
                System.out.println("checking value for: " + fieldNameString);
                String cellValue = this.getField(fieldNameString);
                valueFound = cellValue != null;
                if (valueFound && cellValue.length() > 0) {
                    this.addField(fieldNameString, cellValue);
                }
            }
            return valueFound;
        }

        public ImdiTreeObject[] getChildren(String[] imdiFieldArray) {
            Vector tempImdiVector = new Vector();
            if (!imdiDataLoaded) {
                // if this node has been loaded then do not load again
                // to refresh the node and its children the node should be nulled and recreated
                imdiDataLoaded = true;
                String[] linkArray = getLinks();
                //Hashtable nodesToAdd = new Hashtable();
                //ImdiTreeObject[] returnImdiArray = new ImdiTreeObject[linkArray.length + imdiFieldArray.length];
                if (linkArray != null) {
                    for (int linkCount = 0; linkCount < linkArray.length && linkCount < 10; linkCount++) {
                        System.out.println("linkArray: " + linkArray[linkCount]);
                        ImdiTreeObject currentImdi = new ImdiTreeObject(null, linkArray[linkCount]);
                        tempImdiVector.add(currentImdi);
                        if (linkArray[linkCount].endsWith(".imdi") && linkCount < 9) {
                            currentImdi.loadImdiDom();
                        }
                    }
                }
                for (int rowNameCounter = 0; rowNameCounter < imdiFieldArray.length; rowNameCounter++) {
                    populateChildFields(imdiFieldArray[rowNameCounter]);
                }
            }
            Enumeration nodesToAddEnumeration = childrenHashtable.elements();
            while (nodesToAddEnumeration.hasMoreElements()) {
                tempImdiVector.add((ImdiTreeObject) nodesToAddEnumeration.nextElement());
            }
            ImdiTreeObject[] returnImdiArray = new ImdiTreeObject[tempImdiVector.size()];
            tempImdiVector.toArray(returnImdiArray);
            return returnImdiArray;
        }

        public void addField(String fieldLabel, String fieldValue) {
            if (isImdi()) {
                if (fieldLabel.startsWith("Session.")) {
                    fieldLabel = fieldLabel.substring(8);
                } else if (fieldLabel.startsWith("Corpus.")) {
                    fieldLabel = fieldLabel.substring(7);
                }
            }
            int firstDotSeparator = fieldLabel.indexOf(".");
            if (firstDotSeparator == -1) {
                // add the label to this level node
//                if (fieldLabel == null) fieldLabel = "oops null";
//                if (fieldValue == null) fieldValue = "oops null";
                System.out.println("fieldLabel: " + fieldLabel + " cellValue: " + fieldValue);
                fieldHashtable.put(fieldLabel, fieldValue);
            } else {
                // pass the label to the child nodes
                String childsName = fieldLabel.substring(0, firstDotSeparator);
                if (!childrenHashtable.containsKey(childsName)) {
                    childrenHashtable.put(childsName, new ImdiTreeObject(childsName, null));
                }
                ((ImdiTreeObject) childrenHashtable.get(childsName)).addField(fieldLabel.substring(firstDotSeparator + 1), fieldValue);
            }
        }

        public Hashtable getFields() {
            // store the Hastable for next call
            // if hastable is null then load from imdi
            return fieldHashtable;
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
                return isImdiChild();
            }
        }

        public boolean isImdiChild() {
            return !fieldHashtable.isEmpty() || !childrenHashtable.isEmpty();
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
