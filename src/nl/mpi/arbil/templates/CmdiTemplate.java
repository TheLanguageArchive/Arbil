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
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Vector;
import nl.mpi.arbil.GuiHelper;
import nl.mpi.arbil.LinorgSessionStorage;
import nl.mpi.arbil.LinorgWindowManager;
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
        // testing only
        //super.readTemplate(new File(""), "template_cmdi");

        // construct the template from the XSD
        try {
            // create a temp file of the read template data so that it can be compared to a hand made version
            File debugTempFile = File.createTempFile("templatetext", ".tmp");
            debugTempFile.deleteOnExit();
            BufferedWriter debugTemplateFileWriter = new BufferedWriter(new FileWriter(debugTempFile));

            ArrayList<String[]> childNodePathsList = new ArrayList<String[]>();
            URI xsdUri = new URI(nameSpaceString);
            readSchema(xsdUri, childNodePathsList);
            childNodePaths = childNodePathsList.toArray(new String[][]{});
            for (String[] currentArray : childNodePaths) {
                System.out.println("loadTemplate: " + currentArray[1] + ":" + currentArray[0]);
                debugTemplateFileWriter.write("<ChildNodePath ChildPath=\"" + currentArray[0] + "\" SubNodeName=\"" + currentArray[1] + "\" />\r\n");
            }
            debugTemplateFileWriter.close();
            // lanunch the hand made template and the generated template for viewing
//            LinorgWindowManager.getSingleInstance().openUrlWindowOnce("templatetext", debugTempFile.toURL());
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
        fieldConstraints = new String[][]{};
        preferredNameFields = new String[]{};
        fieldUsageArray = new String[][]{};
        fieldTriggersArray = new String[][]{};
        autoFieldsArray = new String[][]{};
    }

    @Override
    public Enumeration listTypesFor(Object targetNodeUserObject) {
        // get the xpath of the target node
        String targetNodeXpath = ((ImdiTreeObject) targetNodeUserObject).getURI().getFragment();
        System.out.println("targetNodeXpath: " + targetNodeXpath);
        if (targetNodeXpath != null) {
            targetNodeXpath = targetNodeXpath.replaceAll("\\(\\d+\\)", "");
        }
        System.out.println("targetNodeXpath: " + targetNodeXpath);
        Vector<String[]> childTypes = new Vector<String[]>();
        if (targetNodeUserObject instanceof ImdiTreeObject) {
            for (String[] childPathString : childNodePaths) {
                boolean allowEntry = false;
                if (targetNodeXpath == null) {
                    System.out.println("allowing: " + childPathString[0]);
                    allowEntry = true;
                } else if (childPathString[0].startsWith(targetNodeXpath)) {
                    System.out.println("allowing: " + childPathString[0]);
                    allowEntry = true;
                }
                if (targetNodeXpath != null && childPathString[0].length() == targetNodeXpath.length()) {
                    System.out.println("disallowing: " + childPathString[0]);
                    allowEntry = false;
                }
                // remove types that require a container type that has not already been added to the target
                for (String[] childPathTest : childNodePaths) {
                    // only if the test path is valid
                    if (targetNodeXpath == null || childPathTest[0].startsWith(targetNodeXpath)) {
                        if (childPathString[0].startsWith(childPathTest[0])) {
                            if (childPathTest[0].length() < childPathString[0].length()) {
                                System.out.println("removing: " + childPathString[0]);
                                System.out.println("based on: " + childPathTest[0]);
                                allowEntry = false;
                            }
                        }
                    }
                }
                // TODO: check that the sub node addables are being correctly listed in the context menu
//                System.out.println("childPathString[0]: " + childPathString[0]);
//                System.out.println("childPathString[1]: " + childPathString[1]);
                if (allowEntry) {
                    childTypes.add(new String[]{childPathString[1], childPathString[0]});
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

    private void readSchema(URI xsdFile, ArrayList<String[]> childNodePathsList) {
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
                constructXml(schemaType, childNodePathsList, "");
                break; // there can only be a single root node and the IMDI schema specifies two (METATRANSCRIPT and VocabularyDef) so we must stop before that error creates another
            }
        } catch (IOException e) {
            GuiHelper.linorgBugCatcher.logError(e);
        } catch (XmlException e) {
            GuiHelper.linorgBugCatcher.logError(e);
        }
    }

    private void constructXml(SchemaType schemaType, ArrayList<String[]> childNodePathsList, String pathString) {
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
            System.out.println("Found template element: " + currentPathString);
            SchemaType currentSchemaType = schemaProperty.getType();
            constructXml(currentSchemaType, childNodePathsList, currentPathString);
            hasSubNodes = true; // todo: complete or remove this hasSubNodes case
            if (canHaveMultiple && hasSubNodes) {
                childNodePathsList.add(new String[]{currentPathString, localName});
            }
        }
    }
}
