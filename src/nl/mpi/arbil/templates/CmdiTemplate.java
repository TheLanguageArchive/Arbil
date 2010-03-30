package nl.mpi.arbil.templates;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Vector;
import nl.mpi.arbil.GuiHelper;
import nl.mpi.arbil.LinorgSessionStorage;
import nl.mpi.arbil.clarin.CmdiComponentLinkReader;
import nl.mpi.arbil.data.ImdiTreeObject;
import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

/**
 * CmdiTemplate.java
 * Created on March 10, 2010, 17:34:45 AM
 * @author Peter.Withers@mpi.nl
 */
public class CmdiTemplate extends ArbilTemplate {

    public void loadTemplate(String nameSpaceString) {
        try {
            ArrayList<String[]> childNodePathsList = new ArrayList<String[]>();
            URI xsdUri = new URI(nameSpaceString);
            readSchema(xsdUri, childNodePathsList);
            childNodePaths = childNodePathsList.toArray(new String[][]{});
            for (String[] currentArray : childNodePaths) {
                System.out.println(currentArray[1] + ":" + currentArray[0]);
            }
        } catch (URISyntaxException urise) {
            GuiHelper.linorgBugCatcher.logError(urise);
        }
        // TODO: complete these
        requiredFields = new String[]{};
        fieldConstraints = new String[][]{};
        fieldUsageArray = new String[][]{};
    }

    public Enumeration listTypesFor(Object targetNodeUserObject) {
        Vector childTypes = new Vector();
        if (targetNodeUserObject instanceof ImdiTreeObject) {
            CmdiComponentLinkReader cmdiComponentLinkReader = new CmdiComponentLinkReader();
            for (CmdiComponentLinkReader.CmdiComponentLink cmdiComponentLink : cmdiComponentLinkReader.readLinks(((ImdiTreeObject) targetNodeUserObject).getURI())) {
                childTypes.add(new String[]{cmdiComponentLink.filename, cmdiComponentLink.componentId});
            }

            Collections.sort(childTypes, new Comparator() {

                public int compare(Object o1, Object o2) {
                    String value1 = ((String[]) o1)[0];
                    String value2 = ((String[]) o2)[0];
                    return value1.compareTo(value2);
                }
            });
        }
        return childTypes.elements();
    }

    private void readSchema(URI xsdFile, ArrayList<String[]> childNodePathsList) {
        File schemaFile = LinorgSessionStorage.getSingleInstance().updateCache(xsdFile.toString(), 5);
        try {
            InputStream inputStream = new FileInputStream(schemaFile);
            //Since we're dealing with xml schema files here the character encoding is assumed to be UTF-8
            XmlOptions options = new XmlOptions();
            options.setCharacterEncoding("UTF-8");
            SchemaTypeSystem sts = XmlBeans.compileXsd(new XmlObject[]{XmlObject.Factory.parse(inputStream, options)}, XmlBeans.getBuiltinTypeSystem(), null);
            for (SchemaType schemaType : sts.documentTypes()) {
                System.out.println("documentTypes:");
                constructXml(schemaType, childNodePathsList, "");
                break; // there can only be a single root node and the IMDI schema specifies two (METATRANSCRIPT and VocabularyDef) so we must stop before that error creates another
            }
//            for (SchemaType schemaType : sts.attributeTypes()) {
//                System.out.println("attributeTypes:");
//                printSchemaType(schemaType);
//            }
//            for (SchemaType schemaType : sts.globalTypes()) {
//                System.out.println("globalTypes:");
//                printSchemaType(schemaType);
//            }
        } catch (IOException e) {
            GuiHelper.linorgBugCatcher.logError(e);
        } catch (XmlException e) {
            GuiHelper.linorgBugCatcher.logError(e);
        }
    }

    private void constructXml(SchemaType schemaType, ArrayList<String[]> childNodePathsList, String pathString) {
        //System.out.println("printSchemaType " + schemaType.toString());
//        for (SchemaType schemaSubType : schemaType.getAnonymousTypes()) {
//            System.out.println("getAnonymousTypes:");
//            constructXml(schemaSubType, pathString + ".*getAnonymousTypes*", workingDocument, parentElement);
//        }
//        for (SchemaType schemaSubType : schemaType.getUnionConstituentTypes()) {
//            System.out.println("getUnionConstituentTypes:");
//            printSchemaType(schemaSubType);
//        }
//        for (SchemaType schemaSubType : schemaType.getUnionMemberTypes()) {
//            System.out.println("getUnionMemberTypes:");
//            constructXml(schemaSubType, pathString + ".*getUnionMemberTypes*", workingDocument, parentElement);
//        }
//        for (SchemaType schemaSubType : schemaType.getUnionSubTypes()) {
//            System.out.println("getUnionSubTypes:");
//            printSchemaType(schemaSubType);
//        }
        //SchemaType childType =schemaType.

        /////////////////////

        for (SchemaProperty schemaProperty : schemaType.getElementProperties()) {
            String localName = schemaProperty.getName().getLocalPart();
            pathString = pathString + "." + localName;
            boolean canHaveMultiple = true;
            if (schemaProperty.getMaxOccurs() != null) {
                canHaveMultiple = schemaProperty.getMaxOccurs().intValue() > 1;
            }
            if (canHaveMultiple) {
                childNodePathsList.add(new String[]{pathString, localName});
            }
            //for (int childCounter = 0; childCounter < schemaProperty.getMinOccurs().intValue(); childCounter++) {
            System.out.println("Found Element: " + pathString);
            SchemaType currentSchemaType = schemaProperty.getType();
            if ((schemaProperty.getType() != null) && (!(currentSchemaType.isSimpleType()))) {
                constructXml(currentSchemaType, childNodePathsList, pathString);
            }
            //}
        }
    }
}
