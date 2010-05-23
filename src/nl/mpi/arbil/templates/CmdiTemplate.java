package nl.mpi.arbil.templates;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import nl.mpi.arbil.GuiHelper;
import nl.mpi.arbil.ImdiVocabularies;
import nl.mpi.arbil.LinorgSessionStorage;
import nl.mpi.arbil.LinorgWindowManager;
import nl.mpi.arbil.clarin.CmdiProfileReader;
import nl.mpi.arbil.clarin.CmdiProfileReader.CmdiProfile;
import nl.mpi.arbil.data.ImdiTreeObject;
import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlAnySimpleType;
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

    String nameSpaceString;

    public void loadTemplate(String nameSpaceStringLocal) {
        // testing only
        //super.readTemplate(new File(""), "template_cmdi");
        vocabularyHashTable = new Hashtable<String, ImdiVocabularies.Vocabulary>();
        nameSpaceString = nameSpaceStringLocal;
        // construct the template from the XSD
        try {
            // get the name of this profile
            CmdiProfile cmdiProfile = CmdiProfileReader.getSingleInstance().getProfile(nameSpaceString);
            if (cmdiProfile != null) {
                loadedTemplateName = cmdiProfile.name;// this could be null
            } else {
                loadedTemplateName = nameSpaceString.substring(nameSpaceString.lastIndexOf("/") + 1);
            }

            // create a temp file of the read template data so that it can be compared to a hand made version
            File debugTempFile = File.createTempFile("templatetext", ".tmp");
            debugTempFile.deleteOnExit();
            BufferedWriter debugTemplateFileWriter = new BufferedWriter(new FileWriter(debugTempFile));

            ArrayList<String[]> childNodePathsList = new ArrayList<String[]>();
            ArrayList<String[]> resourceNodePathsList = new ArrayList<String[]>();
            ArrayList<String[]> fieldConstraintList = new ArrayList<String[]>();
            ArrayList<String[]> displayNamePreferenceList = new ArrayList<String[]>();
            ArrayList<String[]> fieldUsageDescriptionList = new ArrayList<String[]>();
            URI xsdUri = new URI(nameSpaceString);
            readSchema(xsdUri, childNodePathsList, resourceNodePathsList, fieldConstraintList, displayNamePreferenceList, fieldUsageDescriptionList);
            childNodePaths = childNodePathsList.toArray(new String[][]{});
            resourceNodePaths = resourceNodePathsList.toArray(new String[][]{});
            fieldConstraints = fieldConstraintList.toArray(new String[][]{});
            fieldUsageArray = fieldUsageDescriptionList.toArray(new String[][]{});

            // sort and construct the preferredNameFields array
            String[][] tempSortableArray = displayNamePreferenceList.toArray(new String[][]{});
            Arrays.sort(tempSortableArray, new Comparator<String[]>() {

                public int compare(String[] o1, String[] o2) {
                    return Integer.valueOf(o1[1]) - Integer.valueOf(o2[1]);
                }
            });
            preferredNameFields = new String[tempSortableArray.length];
            for (int nameFieldCounter = 0; nameFieldCounter < preferredNameFields.length; nameFieldCounter++) {
                preferredNameFields[nameFieldCounter] = tempSortableArray[nameFieldCounter][0];
            }
            // end sort and construct the preferredNameFields array

            if (preferredNameFields.length < 1) {
                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("No preferred field names have been specified, some nodes will not display correctly", "Clarin Profile Error");
            }
            if (fieldUsageArray.length < 1) {
                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("No field descriptions have been provided in the profile, as a result no information about each fields intended use can be provided to users of this profile", "Clarin Profile Error");
            }

            for (String[] currentArray : childNodePaths) {
                System.out.println("loadTemplate: " + currentArray[1] + ":" + currentArray[0]);
                debugTemplateFileWriter.write("<ChildNodePath ChildPath=\"" + currentArray[0] + "\" SubNodeName=\"" + currentArray[1] + "\" />\r\n");
            }
            for (String[] currentArray : resourceNodePaths) {
                System.out.println("loadTemplate: " + currentArray[1] + ":" + currentArray[0]);
                debugTemplateFileWriter.write("<ResourceNodePath RefPath=\"" + currentArray[0] + "\" RefNodeName=\"" + currentArray[1] + "\" />\r\n");
            }
            for (String[] currentArray : fieldConstraints) {
                System.out.println("loadTemplate: " + currentArray[1] + ":" + currentArray[0]);
                debugTemplateFileWriter.write("<FieldConstraint FieldPath=\"" + currentArray[0] + "\" Constraint=\"" + currentArray[1] + "\" />\r\n");
            }
            for (String currentArray : preferredNameFields) {
                System.out.println("loadTemplate: " + currentArray);
                // node that this is not a FieldsShortName but a full field path but the code now supports both while the xml file implies only short
                debugTemplateFileWriter.write("<TreeNodeNameField FieldsShortName==\"" + currentArray + "\" />\r\n");
            }
            for (String[] currentArray : fieldUsageArray) {
                System.out.println("loadTemplate: " + currentArray[1] + ":" + currentArray[0]);
                debugTemplateFileWriter.write("<FieldUsage FieldPath=\"" + currentArray[0] + "\" FieldDescription=\"" + currentArray[1] + "\" />\r\n");
            }
            debugTemplateFileWriter.close();
            // lanunch the hand made template and the generated template for viewing
            LinorgWindowManager.getSingleInstance().openUrlWindowOnce("templatetext", debugTempFile.toURL());
//            LinorgWindowManager.getSingleInstance().openUrlWindowOnce("templatejar", CmdiTemplate.class.getResource("/nl/mpi/arbil/resources/templates/template_cmdi.xml"));
//            LinorgWindowManager.getSingleInstance().openUrlWindowOnce("templatejar", CmdiTemplate.class.getResource("/nl/mpi/arbil/resources/templates/template.xml"));
        } catch (URISyntaxException urise) {
            GuiHelper.linorgBugCatcher.logError(urise);
        } catch (IOException urise) {
            GuiHelper.linorgBugCatcher.logError(urise);
        }
        // this should be adequate for cmdi templates
        //templatesArray = childNodePaths;
        // TODO: complete these
        requiredFields = new String[]{};
        preferredNameFields = new String[]{};
        fieldUsageArray = new String[][]{};
        fieldTriggersArray = new String[][]{};
        autoFieldsArray = new String[][]{};
        genreSubgenreArray = new String[][]{};
        templatesArray = new String[][]{};
    }

    @Override
    public Enumeration listTypesFor(Object targetNodeUserObject) {
        // get the xpath of the target node
        String targetNodeXpath = ((ImdiTreeObject) targetNodeUserObject).getURI().getFragment();
        System.out.println("targetNodeXpath: " + targetNodeXpath);
        if (targetNodeXpath != null) {
            // remove the extraneous node name for a meta node
            targetNodeXpath = targetNodeXpath.replaceAll("\\.[^\\.]+[^\\)]$", "");
            // remove the sibling indexes
            targetNodeXpath = targetNodeXpath.replaceAll("\\(\\d+\\)", "");
        }
        System.out.println("targetNodeXpath: " + targetNodeXpath);
        Vector<String[]> childTypes = new Vector<String[]>();
        if (targetNodeUserObject instanceof ImdiTreeObject) {
            for (String[] childPathString : childNodePaths) {
                boolean allowEntry = false;
                if (targetNodeXpath == null) {
//                    System.out.println("allowing: " + childPathString[0]);
                    allowEntry = true;
                } else if (childPathString[0].startsWith(targetNodeXpath)) {
//                    System.out.println("allowing: " + childPathString[0]);
                    allowEntry = true;
                }
                if (childPathString[0].equals(targetNodeXpath)) {
//                    System.out.println("disallowing addint to itself: " + childPathString[0]);
                    allowEntry = false;
                }
                if (allowEntry) {
                    childTypes.add(new String[]{childPathString[1], childPathString[0]});
                }
            }
            String[][] childTypesArray = childTypes.toArray(new String[][]{});
            childTypes.removeAllElements();
            for (String[] currentChildType : childTypesArray) {
                boolean keepChildType = true;
//                System.out.println("currentChildType: " + currentChildType[1]);
                for (String[] subChildType : childTypesArray) {
//                    System.out.println("subChildType: " + subChildType[1]);
                    if (currentChildType[1].startsWith(subChildType[1])) {
                        if (currentChildType[1].length() != subChildType[1].length()) {
                            keepChildType = false;
//                            System.out.println("removing: " + currentChildType[1]);
//                            System.out.println("based on: " + subChildType[1]);
                        }
                    }
                }
                if (keepChildType) {
                    childTypes.add(currentChildType);
                }
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

    private void readSchema(URI xsdFile, ArrayList<String[]> childNodePathsList, ArrayList<String[]> resourceNodePathsList, ArrayList<String[]> fieldConstraintList, ArrayList<String[]> displayNamePreferenceList, ArrayList<String[]> fieldUsageDescriptionList) {
        File schemaFile = LinorgSessionStorage.getSingleInstance().updateCache(xsdFile.toString(), 5);
        templateFile = schemaFile; // store the template file for later use such as adding child nodes
        try {
            InputStream inputStream = new FileInputStream(schemaFile);
            //Since we're dealing with xml schema files here the character encoding is assumed to be UTF-8
            XmlOptions options = new XmlOptions();
            options.setCharacterEncoding("UTF-8");
            SchemaTypeSystem sts = XmlBeans.compileXsd(new XmlObject[]{XmlObject.Factory.parse(inputStream, options)}, XmlBeans.getBuiltinTypeSystem(), null);
            for (SchemaType schemaType : sts.documentTypes()) {
                System.out.println("T-documentTypes:");
                constructXml(schemaType, childNodePathsList, resourceNodePathsList, fieldConstraintList, displayNamePreferenceList, fieldUsageDescriptionList, "");
                break; // there can only be a single root node and the IMDI schema specifies two (METATRANSCRIPT and VocabularyDef) so we must stop before that error creates another
            }
        } catch (IOException e) {
            GuiHelper.linorgBugCatcher.logError(templateFile.getName(), e);
            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Could not open the required template file: " + templateFile.getName(), "Load Clarin Template");
        } catch (XmlException e) {
            GuiHelper.linorgBugCatcher.logError(templateFile.getName(), e);
            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Could not read the required template file: " + templateFile.getName(), "Load Clarin Template");
        }
    }

    private void constructXml(SchemaType schemaType, ArrayList<String[]> childNodePathsList, ArrayList<String[]> resourceNodePathsList, ArrayList<String[]> fieldConstraintList, ArrayList<String[]> displayNamePreferenceList, ArrayList<String[]> fieldUsageDescriptionList, String pathString) {
//        SchemaAnnotation ann = ((SchemaLocalElement) schemaType.getContentModel()).getAnnotation();
        //System.out.println("SchemaAnnotation: " + schemaType.getDocumentElementName());

//        System.out.println((SchemaLocalElement) schemaType.getContentModel());
//        System.out.println((SchemaLocalElement) schemaType.getContentModel());
//        System.out.println((SchemaLocalElement) schemaType.getContentModel().getParticleChild(0));
//
//        SchemaLocalElement schemaElement = (SchemaLocalElement) schemaType.getContentModel().getParticleChild(0);
//        System.out.println(schemaElement.getAnnotation().getUserInformation()[0].execQuery("//xs:documentation/text()")[0]);

//        for (SchemaProperty schemaProperty : schemaType.getProperties()) {
//            System.out.println("getProperties: " + schemaProperty.getName());
//            System.out.println("getProperties: " + schemaProperty.toString());
//        }
        readControlledVocabularies(schemaType, pathString);
        readDisplayNamePreferences(schemaType, pathString, displayNamePreferenceList);
        readFieldConstrains(schemaType, pathString, fieldConstraintList);
        readFieldUsageDescriptions(schemaType, pathString, fieldUsageDescriptionList);

        for (SchemaProperty schemaProperty : schemaType.getElementProperties()) {
            String localName = schemaProperty.getName().getLocalPart();
            String currentPathString = pathString + "." + localName;
            boolean canHaveMultiple = true;
            if (schemaProperty.getMaxOccurs() == null) {
                // absence of the max occurs also means multiple
                canHaveMultiple = true;
                // todo: also check that min and max are the same because there may be cases of zero required but only one can be added
            } else if (schemaProperty.getMaxOccurs().toString().equals("unbounded")) {
                canHaveMultiple = true;
            } else {
                // todo: take into account max occurs in the add menu
                canHaveMultiple = schemaProperty.getMaxOccurs().intValue() > 1;
            }
            boolean hasSubNodes = false;
            //   SchemaAnnotation ann = ((SchemaLocalElement)ProductTypeDocument.type.getContentModel()).getAnnotation();
            //documentation = SchemaUtils.getDocumentation( schemaType );
            System.out.println("Found template element: " + currentPathString);
            SchemaType currentSchemaType = schemaProperty.getType();
//            System.out.println("getAnnotation: " + schemaProperty.getType().getAnnotation());
//
//            System.out.println("getAnnotation: " + schemaProperty.getContainerType().getAnnotation());
//            System.out.println("getAnnotation: " + schemaProperty.getType().getAnnotation());
//            for (SchemaProperty schemaSubProperty : currentSchemaType.getAttributeProperties()) {
//                System.out.println("getAttributeProperties: " + schemaSubProperty.getName().getLocalPart());
//            }
//            for (SchemaProperty schemaSubProperty : currentSchemaType.getDerivedProperties()) {
//                System.out.println("getDerivedProperties: " + schemaSubProperty.getName().getLocalPart());
//            }
//            for (SchemaProperty schemaSubProperty : currentSchemaType.getElementProperties()) {
//                System.out.println("getElementProperties: " + schemaSubProperty.getName().getLocalPart());
//            }
//            for (SchemaProperty schemaSubProperty : currentSchemaType.getProperties()) {
//                System.out.println("getProperties: " + schemaSubProperty.getName().getLocalPart());
//            }
//            for (SchemaType schemaSubType : currentSchemaType.getAnonymousTypes()) {
//                System.out.println("getAnonymousTypes: " + schemaSubType.getAnnotation());
//            }

//             SchemaAnnotation ann = ((SchemaLocalElement) currentSchemaType.getContentModel()).getAnnotation();
//            System.out.println("SchemaAnnotation: " + ann.getName());
//            SchemaAnnotation ann = ((SchemaLocalElement)ProductTypeDocument.type.getContentModel()).getAnnotation();


//            if (currentSchemaType.getContentType() == SchemaType.ELEMENT_CONTENT
//                    || currentSchemaType.getContentType() == SchemaType.MIXED_CONTENT) {
//                SchemaParticle topParticle = currentSchemaType.getContentModel();
//                // topParticle is non-null if we checked the content
//                navigateParticle(topParticle);
//            }


            // getAnnotations(currentSchemaType);


            constructXml(currentSchemaType, childNodePathsList, resourceNodePathsList, fieldConstraintList, displayNamePreferenceList, fieldUsageDescriptionList, currentPathString);
            hasSubNodes = true; // todo: complete or remove this hasSubNodes case
            if (canHaveMultiple && hasSubNodes) {
                childNodePathsList.add(new String[]{currentPathString, localName});
            }
            boolean hasResourceAttribute = false;
            for (SchemaProperty attributesProperty : currentSchemaType.getAttributeProperties()) {
                if (attributesProperty.getName().getLocalPart().equals("ref")) {
                    hasResourceAttribute = true;
                    break;
                }
            }
            if (hasResourceAttribute) {
                resourceNodePathsList.add(new String[]{currentPathString, localName});
            }

//            System.out.println("type: " + currentSchemaType.getName());
//            System.out.println("getSourceName: " + currentSchemaType.getSourceName());
//            System.out.println("getFullJavaName: " + currentSchemaType.getFullJavaName());
//
//            System.out.println("getSourceName: " + currentSchemaType.getSourceName());
//
//            System.out.println("getFullJavaName: " + currentSchemaType.getUserData());
//
//            System.out.println(".getBuiltinTypeCode: " + currentSchemaType.getBuiltinTypeCode());
//            System.out.println("getFullJavaName: " + currentSchemaType.getFullJavaName());
            // {http://www.w3.org/2001/XMLSchema}string
            // {http://www.w3.org/2001/XMLSchema}date
            // {http://www.w3.org/2001/XMLSchema}anyURI

todo: read in this format            <xs:element maxOccurs="1" minOccurs="1" dcr:datcat="http://www.isocat.org/datcat/DC-2545" ann:documentation="the title of the book" ann:displaypriority="1" name="TitleOfBook" type="complextype-test-profile-book-TitleOfBook">
//            schemaProperty.getType().
        }
    }

    private void readFieldUsageDescriptions(SchemaType schemaType, String nodePath, ArrayList<String[]> fieldUsageDescriptionList) {
    }

    private void readDisplayNamePreferences(SchemaType schemaType, String nodePath, ArrayList<String[]> displayNamePreferenceList) {
    }

    private void readFieldConstrains(SchemaType schemaType, String nodePath, ArrayList<String[]> fieldConstraintList) {
        switch (schemaType.getBuiltinTypeCode()) {
            case SchemaType.BTC_STRING:
                System.out.println("BTC_STRING");
                // no constraint relevant for string
                break;
            case SchemaType.BTC_DATE:
                System.out.println("BTC_DATE");
                fieldConstraintList.add(new String[]{nodePath, "([0-9][0-9][0-9][0-9])((-[0-1][0-9])(-[0-3][0-9])?)?"});// todo: complete this regex
                break;
            case SchemaType.BTC_BOOLEAN:
                System.out.println("BTC_BOOLEAN");
                fieldConstraintList.add(new String[]{nodePath, "true|false"});// todo: complete this regex
                break;
            case SchemaType.BTC_ANY_URI:
                System.out.println("BTC_ANY_URI");
                fieldConstraintList.add(new String[]{nodePath, "[^\\d]+://.*"});// todo: complete this regex
                break;
//                case SchemaType. XML object???:
//                    System.out.println("");
//                    fieldConstraintList.add(new String[]{currentPathString, "[^\\d]+://.*"});// todo: complete this regex
//                    break;
            case 0:
                // no constraint relevant
                break;
            default:
                System.out.println("uknown");
                break;
        }
    }

    private void readControlledVocabularies(SchemaType schemaType, String nodePath) {
        if (schemaType.getEnumerationValues() != null) {
//            System.out.println("Controlled Vocabulary: " + schemaType.toString());
//            System.out.println("Controlled Vocabulary: " + schemaType.getName());

            ImdiVocabularies.Vocabulary vocabulary = ImdiVocabularies.getSingleInstance().getEmptyVocabulary(nameSpaceString + "#" + schemaType.getName());

            for (XmlAnySimpleType anySimpleType : schemaType.getEnumerationValues()) {
//                System.out.println("Value List: " + anySimpleType.getStringValue());
                vocabulary.addEntry(anySimpleType.getStringValue());
            }
            vocabularyHashTable.put(nodePath, vocabulary);
        }
    }

//    public static void printPropertyInfo(SchemaProperty p) {
//        System.out.println("Property name=\"" + p.getName() + "\", type=\"" + p.getType().getName()
//                + "\", maxOccurs=\""
//                + (p.getMaxOccurs() != null ? p.getMaxOccurs().toString() : "unbounded") + "\"");
//    }
//    public void getAnnotations(SchemaType schemaType) {
//        SchemaParticle typeParticle = schemaType.getContentModel();
//        if (typeParticle == null) {
//            return;
//        }
//        SchemaParticle[] childParts =
//                typeParticle.getParticleChildren();
//        if (childParts == null) {
//            return;
//        }
//        for (SchemaParticle part : childParts) {
//            /* I know my property is of element type */
//            if (part.getParticleType() == SchemaParticle.ELEMENT) {
////                if (part.getName().equals(prop.getName())) {
//                SchemaAnnotation ann = ((SchemaLocalElement) schemaType.getContentModel()).getAnnotation();
//                System.out.println("SchemaAnnotation: " + ann);
//
//                System.out.println("SchemaLocalElement: " + ((SchemaLocalElement) part).getAnnotation());
////                }
//            }
//        }
//    }
//
//    public void getAnnotations(SchemaType schemaType, SchemaProperty prop) {
//        SchemaParticle typeParticle = schemaType.getContentModel();
//        if (typeParticle == null) {
//            //return null;
//        }
//        SchemaParticle[] childParts =
//                typeParticle.getParticleChildren();
//        if (childParts == null) {
//            //return null;
//        }
//        for (SchemaParticle part : childParts) {
//            /* I know my property is of element type */
//            if (part.getParticleType() == SchemaParticle.ELEMENT) {
//                if (part.getName().equals(prop.getName())) {
//                    System.out.println("SchemaLocalElement: " + ((SchemaLocalElement) part).getAnnotation());
//                }
//            }
//        }
//    }
//
//    public void navigateParticle(SchemaParticle p) {
//        switch (p.getParticleType()) {
//            case SchemaParticle.ALL:
//            case SchemaParticle.CHOICE:
//            case SchemaParticle.SEQUENCE:
//                // These are "container" particles, so iterate over their children
//                SchemaParticle[] children = p.getParticleChildren();
//                for (int i = 0; i < children.length; i++) {
//                    navigateParticle(children[i]);
//                }
//                break;
//            case SchemaParticle.ELEMENT:
//                printElementInfo((SchemaLocalElement) p);
//                break;
//            default:
//            // There can also be "wildcards" corresponding to <xs:any> elements in the Schema
//        }
//    }
//
//    public void printElementInfo(SchemaLocalElement e) {
//        System.out.println("Element name=\"" + e.getName() + "\", type=\"" + e.getType().getName()
//                + "\", maxOccurs=\""
//                + (e.getMaxOccurs() != null ? e.getMaxOccurs().toString() : "unbounded") + "\"");
//        SchemaAnnotation annotation = e.getAnnotation();
//        if (annotation != null) {
//            SchemaAnnotation.Attribute[] att = annotation.getAttributes();
//            if (att != null && att.length > 0) {
//                System.out.println("  Annotation: " + att[0].getName() + "=\""
//                        + att[0].getValue() + "\"");
//            }
//        }
//    }
    public static void main(String args[]) {
//        new CmdiTemplate().loadTemplate("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1272022528355/xsd");
        new CmdiTemplate().loadTemplate("file:/Users/petwit/Desktop/LocalProfiles/clarin.eu_annotation-test_1272022528355.xsd");
    }
}
