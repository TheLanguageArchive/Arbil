/*
 * ImdiSchema is used to read the imdi schema 
 * and provide a list of valid fields and field constraints 
 */
package mpi.linorg;

import java.util.Enumeration;
import java.util.Vector;

/**
 *
 * @author petwit
 */
public class ImdiSchema {

    /**
     * When complete this function will parse the imdi schema
     */
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
     * @param parentToBe is the imdi node that will receive the new child
     * @return an enumeration of Strings for the available child types, one of which will be passed to listFieldsFor().
     */
    public Enumeration listTypesFor(Object targetNodeUserObject) {
        // temp method for testing until replaced
        // TODO: implement this using data from the xsd on the server (server version needs to be updated)
        Vector childTypes = new Vector();
        if (targetNodeUserObject instanceof ImdiHelper.ImdiTreeObject) {
            if (((ImdiHelper.ImdiTreeObject) targetNodeUserObject).isSession()) {
                childTypes.add("Actor");
                childTypes.add("WrittenResource");
                childTypes.add("Anonym");
                childTypes.add("MediaFile");
            } else if (!((ImdiHelper.ImdiTreeObject) targetNodeUserObject).isImdiChild()) {
                childTypes.add("Corpus");
                childTypes.add("Session");
            }
            System.out.println("childTypes: " + childTypes);
        } else {
            // corpus can be added to the root node
            childTypes.add("Corpus");
        }
        return childTypes.elements();
    }

    /**
     * This function is only a place holder and will be replaced 
     * @param childType is the chosen child type
     * @return enumeration of potential fields for this child type
     */
    public Enumeration listFieldsFor(String childType, int imdiChildIdentifier, String resourcePath) {
        System.out.println("listFieldsFor: " + childType);
        if (childType.contains("image")) {
            childType = "MediaFile";
        // temp method for testing until replaced
        // TODO: implement this using data from the xsd on the server (server version needs to be updated)
        }
        Vector fieldTypes = new Vector();
        String xmlPrePath = "";
        if (childType.equals("Actor")) {
            xmlPrePath = ".METATRANSCRIPT.Session.MDGroup.Actors.Actor(" + imdiChildIdentifier + ")"; // TODO resolve what method to use for imdichildren
        //fieldTypes.add(xmlPrePath + ".Actor");
        } else if (childType.equals("WrittenResource")) {
            xmlPrePath = ".METATRANSCRIPT.Session.Resources.WrittenResource(" + imdiChildIdentifier + ")";
            fieldTypes.add(new String[]{xmlPrePath + ".WrittenResource", "unset"});
        } else if (childType.equals("Anonym")) {
            xmlPrePath = ".METATRANSCRIPT.Session.Resources.Anonyms(" + imdiChildIdentifier + ")";
            fieldTypes.add(new String[]{xmlPrePath + ".Anonyms", "unset"});
        } else if (childType.equals("MediaFile")) {
            xmlPrePath = ".METATRANSCRIPT.Session.Resources.MediaFile(" + imdiChildIdentifier + ")";
            if (resourcePath == null) {
                resourcePath = "null string";
            }
            fieldTypes.add(new String[]{xmlPrePath + ".Type", "unset"});
            fieldTypes.add(new String[]{xmlPrePath + ".TimePosition.Start", "unset"});
            fieldTypes.add(new String[]{xmlPrePath + ".TimePosition.End", "unset"});
            fieldTypes.add(new String[]{xmlPrePath + ".Quality", "unset"});
            fieldTypes.add(new String[]{xmlPrePath + ".AccessDate", "unset"});
            fieldTypes.add(new String[]{xmlPrePath + ".ResourceLink", resourcePath});
            fieldTypes.add(new String[]{xmlPrePath + ".Format", childType});
        } else if (childType.equals("Corpus")) {
            xmlPrePath = ".METATRANSCRIPT.Corpus";
            fieldTypes.add(new String[]{xmlPrePath + ".Corpus", "unset"});
        } else if (childType.equals("Session")) {
            xmlPrePath = ".METATRANSCRIPT.Session";
            fieldTypes.add(new String[]{xmlPrePath + ".Session", "unset"});
        }
        if (!childType.equals("MediaFile")) {
            fieldTypes.add(new String[]{xmlPrePath + ".Name", "unset"});
            fieldTypes.add(new String[]{xmlPrePath + ".Description", "unset"});
            fieldTypes.add(new String[]{xmlPrePath + ".Title", "unset"});
        }
        System.out.println("childType: " + childType + " fieldTypes: " + fieldTypes);
        return fieldTypes.elements();
    }

    public boolean isImdiChildType(String childType) {
        return !childType.equals("Session") && !childType.equals("Corpus");
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
}
