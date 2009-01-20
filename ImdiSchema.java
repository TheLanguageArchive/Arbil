/*
 * ImdiSchema is used to read the imdi schema 
 * and provide a list of valid fields and field constraints
 */
package mpi.linorg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

/**
 *
 * @author petwit
 */
public class ImdiSchema {

    /**
     * When complete this function will parse the imdi schema
     */
    static String imdiPathSeparator = ".";
    public void parseXSD() {
//        http://www.mpi.nl/IMDI/Schema/IMDI_3.0.xsd
//        /data1/repos/trunk/src/java/mpi/metadata/profiles/IMDI_3.0.xsd
//        /data1/repos/trunk/src/java/mpi/metadata/schemas/IMDI/IMDI_3_0_6.xsd
//        /data1/repos/trunk/src/java/mpi/metadata/schemas/IMDI/IMDI_3.0.xsd
//        /data1/repos/trunk/src/java/mpi/metadata/schemas/IMDI/IMDI_1.8.xsd
//        /data1/repos/trunk/src/java/mpi/metadata/schemas/IMDI/IMDI_3_0_8.xsd
//        /data1/repos/trunk/src/java/mpi/metadata/schemas/IMDI/IMDI_1.9.xsd
//        /data1/repos/trunk/src/java/mpi/metadata/schemas/IMDI/IMDI_2.9.xsd
//        /data1/repos/trunk/src/java/mpi/metadata/schemas/IMDI/IMDI_3_0_7_01.xsd
//        /data1/repos/trunk/src/java/mpi/imdi/api/resources/IMDI_3.0.xsd
//        /data1/repos/trunk/src/java/mpi/bcarchive/typecheck/resources/IMDI_3_0_7.xsd
//        /data1/repos/trunk/src/java/mpi/bcarchive/typecheck/resources/IMDI_3_0_8.xsd
//        /data1/repos/trunk/src/java/mpi/vocabs/CV/www.mpi.nl/IMDI/Schema/IMDI_3.0.xsd
//        /data1/repos/trunk/webapps/index_server/web/IMDI_3.0.xsd
//        /data1/repos/trunk/resources/testdata/imdi/imdi_2/IMDI_3.0.xsd
//        /data1/repos/trunk/resources/testdata/imdi/imdi_3/Profiles/IMDI_3.0.xsd
//        /data1/repos/trunk/build/mpi/vocabs/CV/www.mpi.nl/IMDI/Schema/IMDI_3.0.xsd
//        /data1/repos/trunk/build/mpi/metadata/profiles/IMDI_3.0.xsd
//        /data1/repos/trunk/build/mpi/metadata/schemas/IMDI/IMDI_1.9.xsd
//        /data1/repos/trunk/build/mpi/metadata/schemas/IMDI/IMDI_1.8.xsd
//        /data1/repos/trunk/build/mpi/metadata/schemas/IMDI/IMDI_3.0.xsd
//        /data1/repos/trunk/build/mpi/metadata/schemas/IMDI/IMDI_3_0_8.xsd
//        /data1/repos/trunk/build/mpi/metadata/schemas/IMDI/IMDI_3_0_6.xsd
//        /data1/repos/trunk/build/mpi/metadata/schemas/IMDI/IMDI_3_0_7_01.xsd
//        /data1/repos/trunk/build/mpi/metadata/schemas/IMDI/IMDI_2.9.xsd
//        /data1/repos/trunk/build/mpi/bcarchive/typecheck/resources/IMDI_3_0_8.xsd
//        /data1/repos/trunk/build/mpi/bcarchive/typecheck/resources/IMDI_3_0_7.xsd
//        /data1/repos/trunk/build/mpi/imdi/api/resources/IMDI_3.0.xsd
    }

    /**
     * This function is only a place holder and will be replaced.
     * @param targetNodeUserObject The imdi node that will receive the new child.
     * @return An enumeration of Strings for the available child types, one of which will be passed to "listFieldsFor()".
     */
    public Enumeration listTypesFor(Object targetNodeUserObject) {
        // temp method for testing until replaced
        // TODO: implement this using data from the xsd on the server (server version needs to be updated)
        Vector childTypes = new Vector();
        if (targetNodeUserObject instanceof ImdiTreeObject) {
            if (((ImdiTreeObject) targetNodeUserObject).isSession() || ((ImdiTreeObject) targetNodeUserObject).isImdiChild()) {
//                childTypes.add(new String[]{"Region", ".METATRANSCRIPT.Session.MDGroup.Location.Region"});
                childTypes.add(new String[]{"Language", ".METATRANSCRIPT.Session.MDGroup.Content.Languages.Language"});
                childTypes.add(new String[]{"Actor", ".METATRANSCRIPT.Session.MDGroup.Actors.Actor"});
//                childTypes.add(new String[]{"Actor Language", ".METATRANSCRIPT.Session.MDGroup.Actors.Actor.Languages.Language"});
                childTypes.add(new String[]{"Media File", ".METATRANSCRIPT.Session.Resources.MediaFile"});
                childTypes.add(new String[]{"Written Resource", ".METATRANSCRIPT.Session.Resources.WrittenResource"});
//                childTypes.add(new String[]{"Lexicon Resource Description", ".METATRANSCRIPT.Session.Resources.LexiconResource.Description"});
                childTypes.add(new String[]{"Source", ".METATRANSCRIPT.Session.Resources.Source"});
//                childTypes.add(new String[]{"", ""});
//                childTypes.add(new String[]{"", ""});
//                childTypes.add(new String[]{"", ""});      
            } else if (!((ImdiTreeObject) targetNodeUserObject).isImdiChild()) {
                childTypes.add(new String[]{"Corpus", imdiPathSeparator + "METATRANSCRIPT" + imdiPathSeparator + "Corpus"});
                childTypes.add(new String[]{"Session", imdiPathSeparator + "METATRANSCRIPT" + imdiPathSeparator + "Session"});
            }
            System.out.println("childTypes: " + childTypes);
        } else {
            // corpus can be added to the root node
            childTypes.add(new String[]{"Corpus", imdiPathSeparator + "METATRANSCRIPT" + imdiPathSeparator + "Corpus"});
        }
        return childTypes.elements();
    }

    /** 
     * This function is only a place holder and will be replaced 
     * @param childType is the chosen child type
     * @return enumeration of potential fields for this child type
     */
//    public Enumeration listFieldsFor(ImdiTreeObject targetImdiObject, String childType, int imdiChildIdentifier, String resourcePath) {
//        Vector childTypes = getSubnodesOf(/*getNodePath(targetImdiObject) + "/" +*/childType, false);
//        return childTypes.elements();
////        System.out.println("listFieldsFor: " + childType);
////        if (childType.contains("image")) {
////            childType = "MediaFile";
////        // temp method for testing until replaced
////        // TODO: implement this using data from the xsd on the server (server version needs to be updated)
////        }
////        Vector fieldTypes = new Vector();
////        String xmlPrePath = "";
////        if (childType.equals("Actor")) {
////            xmlPrePath = ".METATRANSCRIPT.Session.MDGroup.Actors.Actor(" + imdiChildIdentifier + ")"; // TODO resolve what method to use for imdichildren
////        //fieldTypes.add(xmlPrePath + ".Actor");
////        } else if (childType.equals("WrittenResource")) {
////            xmlPrePath = ".METATRANSCRIPT.Session.Resources.WrittenResource(" + imdiChildIdentifier + ")";
////            fieldTypes.add(new String[]{xmlPrePath + ".WrittenResource", "unset"});
////        } else if (childType.equals("Anonym")) {
////            xmlPrePath = ".METATRANSCRIPT.Session.Resources.Anonyms(" + imdiChildIdentifier + ")";
////            fieldTypes.add(new String[]{xmlPrePath + ".Anonyms", "unset"});
////        } else if (childType.equals("MediaFile")) {
////            xmlPrePath = ".METATRANSCRIPT.Session.Resources.MediaFile(" + imdiChildIdentifier + ")";
////            if (resourcePath == null) {
////                resourcePath = "null string";
////            } else {
////                Hashtable exifTags = getExifMetadata(resourcePath);
////                for (Enumeration exifRows = exifTags.keys(); exifRows.hasMoreElements();) {
////                    Object currentKey = exifRows.nextElement();
////                    fieldTypes.add(new String[]{xmlPrePath + ".exif" + currentKey.toString(), exifTags.get(currentKey).toString()});
////                }
////            }
////            fieldTypes.add(new String[]{xmlPrePath + ".Type", "unset"});
////            fieldTypes.add(new String[]{xmlPrePath + ".TimePosition.Start", "unset"});
////            fieldTypes.add(new String[]{xmlPrePath + ".TimePosition.End", "unset"});
////            fieldTypes.add(new String[]{xmlPrePath + ".Quality", "unset"});
////            fieldTypes.add(new String[]{xmlPrePath + ".AccessDate", "unset"});
////            fieldTypes.add(new String[]{xmlPrePath + ".ResourceLink", resourcePath});
////            fieldTypes.add(new String[]{xmlPrePath + ".Format", childType});
////        } else if (childType.equals("Corpus")) {
////            xmlPrePath = ".METATRANSCRIPT.Corpus";
////            fieldTypes.add(new String[]{xmlPrePath + ".Corpus", "unset"});
////        } else if (childType.equals("Session")) {
////            xmlPrePath = ".METATRANSCRIPT.Session";
////            fieldTypes.add(new String[]{xmlPrePath + ".Session", "unset"});
////        }
////        if (!childType.equals("MediaFile")) {
////            fieldTypes.add(new String[]{xmlPrePath + ".Name", "unset"});
////            fieldTypes.add(new String[]{xmlPrePath + ".Description", "unset"});
////            fieldTypes.add(new String[]{xmlPrePath + ".Title", "unset"});
////        }
////        System.out.println("childType: " + childType + " fieldTypes: " + fieldTypes);
////        return fieldTypes.elements();
//    }

    // functions to extract the exif data from images
    // this will probably need to be moved to a more appropriate class
    public Hashtable getExifMetadata(String resourcePath) {
        Hashtable exifTags = new Hashtable();
        System.out.println("tempGetExif: " + resourcePath);
        try {
            File tempFile = new File(resourcePath);
            URL url = tempFile.toURL();
            Iterator readers = ImageIO.getImageReadersBySuffix("jpeg");
            ImageReader reader = (ImageReader) readers.next();
            reader.setInput(ImageIO.createImageInputStream(url.openStream()));
            IIOMetadata metadata = reader.getImageMetadata(0);
            String[] names = metadata.getMetadataFormatNames();
            for (int i = 0; i < names.length; ++i) {
                System.out.println();
                System.out.println("METADATA FOR FORMAT: " + names[i]);
                decendExifTree(metadata.getAsTree(names[i]), null/*"." + names[i]*/, exifTags);
            }
        } catch (Exception ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
//            System.out.println("Exception: " + ex.getMessage());
        }
        System.out.println("end tempGetExif");
        return exifTags;
    }

    public void decendExifTree(Node node, String prefixString, Hashtable exifTags) {
        if (prefixString == null) {
            prefixString = ""; // skip the first node name    
        } else {
            prefixString = prefixString + imdiPathSeparator + node.getNodeName();
        }
        NamedNodeMap namedNodeMap = node.getAttributes();
        if (namedNodeMap != null) {
            for (int attributeCounter = 0; attributeCounter < namedNodeMap.getLength(); attributeCounter++) {
                String attributeName = namedNodeMap.item(attributeCounter).getNodeName();
                String attributeValue = namedNodeMap.item(attributeCounter).getNodeValue();
                exifTags.put(prefixString + imdiPathSeparator + attributeName, attributeValue);
            }
        }
        if (node.hasChildNodes()) {
            NodeList children = node.getChildNodes();
            for (int i = 0, ub = children.getLength(); i < ub; ++i) {
                decendExifTree(children.item(i), prefixString, exifTags);
            }
        }
    }
    // end functions to extract the exif data from images

    public boolean isImdiChildType(String childType) {
        //<xsd:element name="Actor" minOccurs="0" maxOccurs="unbounded">
//        return !childType.equals("Session") && !childType.equals("Corpus");
        return !childType.equals(imdiPathSeparator + "METATRANSCRIPT" + imdiPathSeparator + "Session") && !childType.equals(imdiPathSeparator + "METATRANSCRIPT" + imdiPathSeparator + "Corpus");
    }
//    public boolean nodesChildrenCanHaveSiblings(String xmlPath) {
//        System.out.println("xmlPath: " + xmlPath);
//        return (xmlPath.equals(".METATRANSCRIPT.Session.MDGroup.Actors"));
//    }
//
//    public String[] convertXmlPathToUiPath(String xmlPath) {
//        // TODO write this method
//        // why put in the (x) when it is not representative of the data???
//        return new String[]{"actors", "actor(1)", "name"};
//    }
    public String getHelpForField(String fieldName) {
        return "Usage description for: " + fieldName;
    }    
    public void addFromTemplate(File destinationFile, String templateType) {
        System.out.println("addFromTemplate: " + templateType + " : " + destinationFile);
        // copy the template to disk
        URL templateUrl = ImdiSchema.class.getResource("/mpi/linorg/resources/templates/" + templateType + ".xml");
//        GuiHelper.linorgWindowManager.openUrlWindow(templateType, templateUrl);
        File templateFile = new File(templateUrl.getFile());
//        System.out.println("templateFile: " + templateFile);
        try {
            InputStream in = new FileInputStream(templateFile);
            OutputStream out = new FileOutputStream(destinationFile);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } catch (Exception ex) {
            System.out.println("addFromTemplate: " + ex);
            GuiHelper.linorgBugCatcher.logError(ex);
        }
    }

    public void insertFromTemplate(String elementName, Document targetImdiDom) {
        try {
            File templateFile = new File(ImdiSchema.class.getResource("/mpi/linorg/resources/templates/" + elementName + ".xml").getFile());
            if (templateFile.exists()) {
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                // get the parent node
                String targetXpath = elementName.substring(0, elementName.lastIndexOf("."));                
                // convert to xpath for the api
                targetXpath = targetXpath.replace(".", "/:");
                NodeList targetNodelist = org.apache.xpath.XPathAPI.selectNodeList(targetImdiDom, targetXpath);
                // insert the section into the target imdi
                Document insertableSection = builder.parse(templateFile);
                Node addableNode = targetImdiDom.importNode(insertableSection.getFirstChild(), true);
                targetNodelist.item(0).appendChild(addableNode);
            } else {
                System.out.println("template file not found: " + elementName);
            }
        } catch (Exception ex) {
            System.out.println("insertFromTemplate: " + ex.getMessage());
            GuiHelper.linorgBugCatcher.logError(ex);
        }
//        return null;
    }
}
