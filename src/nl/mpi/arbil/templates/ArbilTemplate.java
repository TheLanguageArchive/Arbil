package nl.mpi.arbil.templates;

import nl.mpi.arbil.data.ArbilVocabularies;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.metadatafile.MetadataReader;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import nl.mpi.arbil.clarin.profiles.CmdiProfileReader;
import nl.mpi.arbil.data.ArbilVocabulary;
import nl.mpi.arbil.util.BugCatcher;
import nl.mpi.arbil.util.MessageDialogHandler;
import org.xml.sax.SAXException;

/**
 * ArbilTemplate.java
 * Created on Aug 14, 2009, 11:30:20 AM
 * @author Peter.Withers@mpi.nl
 */
public class ArbilTemplate {
    private static MessageDialogHandler messageDialogHandler;

    public static void setMessageDialogHandler(MessageDialogHandler handler)
    {
        messageDialogHandler = handler;
    }

    private static BugCatcher bugCatcher;

    public static void setBugCatcher(BugCatcher bugCatcherInstance){
        bugCatcher = bugCatcherInstance;
    }

    public File templateFile;
    protected Hashtable<String, ArbilVocabulary> vocabularyHashTable = null; // this is used by clarin vocabularies. clarin vocabularies are also stored with the imdi vocabularies in the Imdi Vocabularies class.
    public String loadedTemplateName;
    public String[] preferredNameFields;
    public String[][] fieldTriggersArray;
    /*
    <FieldTriggers>
    <comment>The field triggers cause the target field to be set after the source field is edited, the value set in the target is determined by the controlled vocabulary on the source field</comment>
    <comment>The primary use fof these triggers are to set the corresponding language code when the language name field is changed is set</comment>
    <comment>The SourceFieldValue sets the source of the data to be inserted into the target field from the source fields controlled vocabulary. Possible values relate to the vocabulary xml format and include: "Content" "Value" "Code" "FollowUp".</comment>
    <FieldTrigger SourceFieldPath=".METATRANSCRIPT.Session.MDGroup.Content.Languages.Language(x).Name" TargetFieldPath=".METATRANSCRIPT.Session.MDGroup.Content.Languages.Language(x).Id" SourceFieldValue = "Content" />
    <FieldTrigger SourceFieldPath=".METATRANSCRIPT.Session.MDGroup.Actors.Actor(x).Languages.Language(x).Name" TargetFieldPath=".METATRANSCRIPT.Session.MDGroup.Actors.Actor(x).Languages.Language(x).Id" SourceFieldValue = "Content" />
    <FieldTrigger SourceFieldPath=".METATRANSCRIPT.Session.MDGroup.Actors.Actor(x).Language.Name" TargetFieldPath=".METATRANSCRIPT.Session.MDGroup.Actors.Actor(x).Language.Id" SourceFieldValue = "Content" />
    <FieldTrigger SourceFieldPath=".METATRANSCRIPT.Catalogue.SubjectLanguages.Language(x).Name" TargetFieldPath=".METATRANSCRIPT.Catalogue.SubjectLanguages.Language(x).Id" SourceFieldValue = "Content" />
    <FieldTrigger SourceFieldPath=".METATRANSCRIPT.Catalogue.DocumentLanguages.Language.Name" TargetFieldPath=".METATRANSCRIPT.Catalogue.DocumentLanguages.Language.Id" SourceFieldValue = "Content" />
    <comment>The LexiconResource field has no related id field and so is excluded from this list ".METATRANSCRIPT.Session.Resources.LexiconResource(x).MetaLanguages.Language"</comment>
    </FieldTriggers>
     */
    public String[][] genreSubgenreArray;
    /*        
    <comment>The field pairs listed here will be linked as genre subgenre where the subgenre field gets its controlled vocabulary from the genre fields current selection</comment>
    <GenreSubgenre Subgenre=".METATRANSCRIPT.Session.MDGroup.Content.SubGenre" Genre=".METATRANSCRIPT.Session.MDGroup.Content.Genre" Description="description" />
     */
    public String[] requiredFields;
    /*  
    <?xml version="1.0" encoding="UTF-8"?>
    <template>
    <comment>The fields listed here as required fields will be highlighted in the application until they have a value entered</comment>
    <RequiredField FieldPath=".METATRANSCRIPT.Session.Title" />
    <RequiredField FieldPath=".METATRANSCRIPT.Session.Name" />
    <RequiredField FieldPath=".METATRANSCRIPT.Session.Description" />
    <RequiredField FieldPath=".METATRANSCRIPT.Session.MDGroup.Content.Genre" />
    <RequiredField FieldPath=".METATRANSCRIPT.Session.MDGroup.Content.SubGenre" />
    <RequiredField FieldPath=".METATRANSCRIPT.Session.Resources.Anonyms.Access.Contact.Email" />
    <RequiredField FieldPath=".METATRANSCRIPT.Session.MDGroup.Project.Title" />
    <RequiredField FieldPath=".METATRANSCRIPT.Corpus.Title" />
    <RequiredField FieldPath=".METATRANSCRIPT.Session.Title" />
    <RequiredField FieldPath=".METATRANSCRIPT.Session.MDGroup.Project.Id" />
    </template>
     */
    public String[][] fieldConstraints;
//        (ISO639(-1|-2|-3)?:.*)?"/>
//			<xsd:pattern value="(RFC3066:.*)?"/>
//			<xsd:pattern value="(RFC1766:.*)?"/>
//			<xsd:pattern value="(SIL:.*)?"/>
//                        "[0-9][0-9]:[0-9][0-9]:[0-9][0-9]:?[0-9]*|Unknown|Unspecified"
    //   };
    /*
    <FieldConstraints>
    <comment>The fields listed here will be required to match the regex constraint and will be highlighted in the application if they do not</comment>
    <FieldConstraint FieldPath=".METATRANSCRIPT.Session.MDGroup.Actors.Actor(15).BirthDate", Constraint="([0-9]+)((-[0-9]+)(-[0-9]+)?)?">
    <FieldConstraint FieldPath=".METATRANSCRIPT.Session.Date", Constraint="([0-9]+)((-[0-9]+)(-[0-9]+)?)?">
    <FieldConstraint FieldPath=".METATRANSCRIPT.Session.Resources.Anonyms.Access.Date", Constraint="([0-9]+)((-[0-9]+)(-[0-9]+)?)?">
    <FieldConstraint FieldPath=".METATRANSCRIPT.Session.MDGroup.Actors.Actor(3).BirthDate", Constraint="([0-9]+)((-[0-9]+)(-[0-9]+)?)?">
    <FieldConstraint FieldPath=".METATRANSCRIPT.Session.Resources.Anonyms.Access.Contact.Email", Constraint="([.]+)@([.]+)">
    </FieldConstraints>        
     */
    protected String[][] childNodePaths;
    protected String[][] fieldUsageArray;
    protected String[][] resourceNodePaths; // this could be initialised for IMDI templates but at this stage it will not be used in IMDI nodes

    public boolean pathCanHaveResource(String nodePath) {
        // so far this is only used by cmdi but should probably replace the methods used by the equivalent imdi code
        if (nodePath == null) {
            if (resourceNodePaths.length > 0) {
                return true;
            }
        } else {
            // todo: consider allowing add to sub nodes that require adding in the way that IMDI resourceses are added
            nodePath = nodePath.replaceAll("\\(\\d+\\)", "");
            //System.out.println("pathThatCanHaveResource: " + nodePath);
            for (String[] currentPath : resourceNodePaths) {
                //System.out.println("pathCanHaveResource: " + currentPath[0]);
                if (currentPath[0].equals(nodePath)) { // todo: at the moment we are limiting the add of a resource to only the existing nodes and not adding sub nodes to suit
                    return true;
                }
            }
        }
        return false;
    }

    public boolean pathIsEditableField(String nodePath) {
//        System.out.println("nodePath: " + nodePath);
        for (String[] pathString : childNodePaths) {
            if (pathString[0].startsWith(nodePath) || pathString[0].equals(nodePath)) {
                return false;
            }
        }
        for (String[] pathString : templatesArray) { // some profiles do not have sub nodes hence this needs to be checked also
            if (pathString[0].startsWith(nodePath) && !pathString[0].equals(nodePath)) {
                return false;
            }
        }
        return true;
    }

    public String pathIsChildNode(String nodePath) {
//        System.out.println("pathIsChildNode");
//        System.out.println("nodePath: " + nodePath);
        for (String[] pathString : childNodePaths) {
            if (nodePath.endsWith((pathString[0]))) {
//                System.out.println("pathString[1]: " + pathString[1]);
                return pathString[1];
            }
        }
        return null;
        /*
        <ChildNodePaths>
        <comment>The child node paths are used to determin the points at which to add a meta node in the user interface and to provide the text for the meta node name</comment>
        <ChildNodePath ChildPath=".METATRANSCRIPT.Session.MDGroup.Content.Languages.Language" SubNodeName="Languages" />
        <ChildNodePath ChildPath=".Languages.Language" SubNodeName="Languages" />
        <ChildNodePath ChildPath=".METATRANSCRIPT.Session.MDGroup.Actors.Actor" SubNodeName="Actors" />
        <ChildNodePath ChildPath=".METATRANSCRIPT.Session.Resources.MediaFile" SubNodeName="MediaFiles" />
        <ChildNodePath ChildPath=".METATRANSCRIPT.Session.Resources.WrittenResource" SubNodeName="WrittenResources" />
        <ChildNodePath ChildPath=".METATRANSCRIPT.Session.Resources.Source" SubNodeName="Sources" />
        <ChildNodePath ChildPath=".METATRANSCRIPT.Session.Resources.LexiconResource" SubNodeName="LexiconResource" />
        <ChildNodePath ChildPath=".METATRANSCRIPT.Catalogue.Location" SubNodeName="Location" />
        <ChildNodePath ChildPath=".METATRANSCRIPT.Catalogue.SubjectLanguages.Language" SubNodeName="SubjectLanguages" />
        </ChildNodePaths>
         */
    }
    protected String[][] templatesArray; // TODO: separate the filename from the xpath by adding the nodepath as a separate value so that there can be multiple of the same type
    protected String[][] rootTemplatesArray;
    public String[][] autoFieldsArray;

    public boolean pathIsDeleteableField(String nodePath) {
        // modify the path to match the file name until the file name and assosiated array is updated to contain the xmpath filename and menu text
        String imdiNodePath = nodePath.substring(1) + ".xml";
        String cmdiNodePath = nodePath.replaceAll("\\(x\\)", "");
        for (String[] pathString : templatesArray) {
            if (pathString[0].equals((cmdiNodePath)) || pathString[0].equals(imdiNodePath)) {
                return true;
            }
        }
        return false;
    }

    public int getMaxOccursForTemplate(String templatPath) {
        // modify the path to match the file name until the file name and assosiated array is updated to contain the xmpath filename and menu text
        String cmdiNodePath = templatPath.replaceAll("\\(\\d*?\\)", "(x)");
        cmdiNodePath = cmdiNodePath.replaceAll("\\(x\\)$", "");
        String imdiNodePath = cmdiNodePath.substring(1) + ".xml";
        for (String[] pathString : templatesArray) {
            if (pathString[0].equals((cmdiNodePath)) || pathString[0].equals(imdiNodePath)) {
                Integer returnValue = Integer.parseInt(pathString[3]);
                if (returnValue == null) {
                    return -1;
                } else {
                    return returnValue;
                }
            }
        }
        return -1;
    }

    public String getInsertBeforeOfTemplate(String templatPath) {
        // modify the path to match the file name until the file name and assosiated array is updated to contain the xmpath filename and menu text
        String cmdiNodePath = templatPath.replaceAll("\\(\\d*?\\)", "(x)");
        cmdiNodePath = cmdiNodePath.replaceAll("\\(x\\)$", "");
        String imdiNodePath = cmdiNodePath.substring(1) + ".xml";
        for (String[] pathString : templatesArray) {
            if (pathString[0].equals((cmdiNodePath)) || pathString[0].equals(imdiNodePath)) {
                if (pathString[2] != null) {
                    return pathString[2];
                } else {
                    return "";
                }
            }
        }
        return "";
    }

    public boolean isArbilChildNode(String childType) {
        boolean returnValue = false;
        if (childType != null) {
            if (CmdiProfileReader.pathIsProfile(childType)) {
                returnValue = false;
            } else {
                returnValue = true;
                String childTypeTemp = (childType + ".xml").substring(1);
                for (String[] currentTemplate : rootTemplatesArray) {
                    if (childTypeTemp.equals(currentTemplate[0])) {
                        returnValue = false;
                    }
                }
                if (returnValue) {
                    // this has been added to resolve an issue detecting custom templates
                    returnValue = false;
                    for (String[] currentTemplate : templatesArray) {
                        if (childTypeTemp.equals(currentTemplate[0])) {
                            returnValue = true;
                        }
                    }
                }
            }
        }
        return returnValue; //!childType.equals(imdiPathSeparator + "METATRANSCRIPT" + imdiPathSeparator + "Session") && !childType.equals(imdiPathSeparator + "METATRANSCRIPT" + imdiPathSeparator + "Corpus") && !childType.equals(imdiPathSeparator + "METATRANSCRIPT" + imdiPathSeparator + "Catalogue");
    }

    private Vector getSubnodesFromTemplatesDir(String nodepath) {
        Vector<String[]> returnVector = new Vector<String[]>();
        System.out.println("getSubnodesOf: " + nodepath);
        String targetNodePath = nodepath.substring(0, nodepath.lastIndexOf(")") + 1);
        nodepath = nodepath.replaceAll("\\(\\d*?\\)", "\\(x\\)");
        System.out.println("nodepath: " + nodepath);
        System.out.println("targetNodePath: " + targetNodePath);

        /*try {
        //            System.out.println("get templatesDirectory");
        File templatesDirectory = new File(this.getClass().getResource("/nl/mpi/arbil/resources/templates/").getFile());
        //            System.out.println("check templatesDirectory");
        if (templatesDirectory.exists()) { // compare the templates directory to the array and throw if there is a discrepancy
        //                System.out.println("using templatesDirectory");
        String[] testingListing = templatesDirectory.list();
        Arrays.sort(testingListing);
        for (String itemString : testingListing) {
        System.out.println("\"" + itemString + "\",");
        }
        Arrays.sort(templatesArray, new Comparator() {

        public int compare(Object obj1, Object obj2) {
        return ((String[]) obj1)[0].compareToIgnoreCase(((String[]) obj2)[0]);
        }
        });
        int linesRead = 0;
        for (String[] currentTemplate : templatesArray) {
        //                    System.out.println("currentTemplate: " + currentTemplate + " : " + testingListing[linesRead]);
        if (testingListing != null) {
        if (!testingListing[linesRead].equals(currentTemplate[0])) {
        System.out.println("error: " + currentTemplate[0] + " : " + testingListing[linesRead]);
        //                            GuiHelper.linorgBugCatcher.logError(new Exception("error in the templates array"));
        }
        }
        linesRead++;
        }
        if (testingListing != null) {
        if (testingListing.length - 2 != linesRead) {
        System.out.println(testingListing[linesRead]);
        //                        GuiHelper.linorgBugCatcher.logError(new Exception("error missing line in the templates array"));
        }
        }
        }
        } catch (Exception ex) {
        GuiHelper.linorgBugCatcher.logError(ex);
        }*/
        for (String[] currentTemplate : templatesArray) {
//            ==================================== TemplateComponent-FileName-NodePath-DisplayName
//            String templateFileName = currentTemplate[0];
//            String templateNodePath = currentTemplate[1];
//            String templateDisplayName = currentTemplate[2];
//            if (!templateFileName.endsWith("Session") && !templateFileName.endsWith("Catalogue")) { // sessions cannot be added to a session
//                if (templateNodePath.startsWith(nodepath)) {
//                    if (targetNodePath.replaceAll("[^(]*", "").length() >= templateNodePath.replaceAll("[^(]*", "").length()) {// check the string engh has not changed here due to the lack of the .xml
//            currentTemplate[0] = "." + currentTemplate[0];
//            ==================================== TemplateComponent-FileName-NodePath-DisplayName
            if (!currentTemplate[0].endsWith("Session.xml") && !currentTemplate[0].endsWith("Catalogue.xml")) { // sessions cannot be added to a session
                if (currentTemplate[0].startsWith(nodepath.substring(1))) {
                    if (targetNodePath.replaceAll("[^(]*", "").length() >= currentTemplate[0].replaceAll("[^(]*", "").length()) {
                        String currentValue = currentTemplate[0].replaceFirst("\\.xml$", "");
//                            String currentTemplateXPath = currentTemplate[0].replaceFirst("\\.xml$", "");
//                            String currentTemplateName = currentTemplateXPath.substring(nodepath.length());
//                        System.out.println("currentTemplateXPath: " + currentTemplateXPath);
//                        System.out.println("targetNodePath: " + targetNodePath);
//                            String destinationXPath;
//                            if (currentTemplateXPath.contains(")")) {
//                                destinationXPath = targetNodePath + currentTemplateXPath.substring(currentTemplateXPath.lastIndexOf(")") + 1);
//                            } else {
//                                destinationXPath = currentTemplateXPath;
//                            }
//                        System.out.println("destinationXPath: " + destinationXPath);
//            ====================================
//                        System.out.println(currentTemplate[1] + " : ." + currentValue);
                        returnVector.add(new String[]{currentTemplate[1], "." + currentValue});// TODO: update the menu title to include location and exact file name from the template
                    }
                }
            }
        }
        return returnVector;
    }

    public String getParentOfField(String targetFieldPath) {
        if (targetFieldPath == null) {
            return "";
        }
        String testString = targetFieldPath.replaceAll("\\(\\d+\\)", "");
        String bestMatch = "";
        for (String[] currentTemplate : childNodePaths) {
            String currentNodePath = currentTemplate[0];
            if (testString.startsWith(currentNodePath)) {
                if (bestMatch.length() < currentNodePath.length()) {
                    bestMatch = currentNodePath;
                }
            }
        }
        String returnString = targetFieldPath;
        while (returnString.split("\\.").length > bestMatch.split("\\.").length) {
            returnString = returnString.replaceFirst("\\.[^\\.]+$", "");
        }
        return returnString;
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
        if (targetNodeUserObject instanceof ArbilDataNode) {
            String xpath = MetadataReader.getNodePath((ArbilDataNode) targetNodeUserObject);
            childTypes = getSubnodesFromTemplatesDir(xpath); // add the main entries based on the node path of the target
            if (((ArbilDataNode) targetNodeUserObject).isCorpus()) { // add any corpus node entries
                for (String[] currentTemplate : rootTemplatesArray) {
                    boolean suppressEntry = false;
                    if (currentTemplate[1].equals("Catalogue")) {
                        if (((ArbilDataNode) targetNodeUserObject).hasCatalogue()) {
                            // make sure the catalogue can only be added once
                            suppressEntry = true;
                        }
                    }
                    if (!suppressEntry) {
                        childTypes.add(new String[]{currentTemplate[1], "." + currentTemplate[0].replaceFirst("\\.xml$", "")});
                    }
                }
            }
//            System.out.println("childTypes: " + childTypes);
        } else {
            // add the the root node items
            for (String[] currentTemplate : rootTemplatesArray) {
                if (!currentTemplate[1].equals("Catalogue")) {// make sure the catalogue can not be added at the root level
                    childTypes.add(new String[]{"Unattached " + currentTemplate[1], "." + currentTemplate[0].replaceFirst("\\.xml$", "")});
                }
            }
        }
        Collections.sort(childTypes, new Comparator() {

            public int compare(Object o1, Object o2) {
                String value1 = ((String[]) o1)[0];
                String value2 = ((String[]) o2)[0];
                return value1.compareTo(value2);
            }
        });
        return childTypes.elements();
    }

    public ArrayList<String> listAllTemplates() {
        ArrayList<String> returnArrayList = new ArrayList<String>();
        for (String[] currentTemplate : templatesArray) {
            returnArrayList.add(currentTemplate[0]);
        }
        return returnArrayList;
    }

    public String getHelpStringForField(String fieldName) {
        fieldName = fieldName.replaceAll("\\([0-9]+\\)\\.", ".");
        for (String[] currentUsageArray : fieldUsageArray) {
            if (currentUsageArray[0].equals(fieldName)) {
                return currentUsageArray[1];
            }
        }
        return "No usage description found in this template for: " + fieldName;
    }

    public ArbilVocabulary getFieldVocabulary(String nodePath) {
        if (vocabularyHashTable != null) {
            return vocabularyHashTable.get(nodePath);
        }
        return null;
    }

    public boolean readTemplate(File templateConfigFile, String templateName) {
        templateFile = templateConfigFile;
        // testing: parseXsdForUsageDescriptions();
        try {
            javax.xml.parsers.SAXParserFactory saxParserFactory = javax.xml.parsers.SAXParserFactory.newInstance();
            javax.xml.parsers.SAXParser saxParser = saxParserFactory.newSAXParser();
            org.xml.sax.XMLReader xmlReader = saxParser.getXMLReader();
            xmlReader.setFeature("http://xml.org/sax/features/validation", false);
            xmlReader.setFeature("http://xml.org/sax/features/namespaces", true);
            xmlReader.setContentHandler(new org.xml.sax.helpers.DefaultHandler() {

                ArrayList<String> requiredFieldsList = new ArrayList<String>();
                ArrayList<String[]> genreSubgenreList = new ArrayList<String[]>();
                ArrayList<String[]> fieldConstraintList = new ArrayList<String[]>();
                ArrayList<String[]> fieldTriggersList = new ArrayList<String[]>();
                ArrayList<String[]> childNodePathsList = new ArrayList<String[]>();
                ArrayList<String[]> templateComponentList = new ArrayList<String[]>();
                ArrayList<String[]> rootTemplateComponentList = new ArrayList<String[]>();
                ArrayList<String[]> fieldUsageList = new ArrayList<String[]>();
                ArrayList<String[]> autoFieldsList = new ArrayList<String[]>();
                ArrayList<String> preferredNameFieldsList = new ArrayList<String>();

                @Override
                public void startElement(String uri, String name, String qName, org.xml.sax.Attributes atts) {
                    if (name.equals("RequiredField")) {
                        String vocabName = atts.getValue("FieldPath");
                        requiredFieldsList.add(vocabName);
                    }
                    if (name.equals("GenreSubgenre")) {
                        String subgenre = atts.getValue("Subgenre");
                        String genre = atts.getValue("Genre");
                        String description = atts.getValue("Description");
                        genreSubgenreList.add(new String[]{subgenre, genre, description});
                    }
                    if (name.equals("FieldConstraint")) {
                        String fieldPath = atts.getValue("FieldPath");
                        String constraint = atts.getValue("Constraint");
                        fieldConstraintList.add(new String[]{fieldPath, constraint});
                    }
                    if (name.equals("FieldTrigger")) {
                        String sourceFieldPath = atts.getValue("SourceFieldPath");
                        String targetFieldPath = atts.getValue("TargetFieldPath");
                        String description = atts.getValue("SourceFieldValue");
                        fieldTriggersList.add(new String[]{sourceFieldPath, targetFieldPath, description});
                    }
                    if (name.equals("ChildNodePath")) {
                        String childPath = atts.getValue("ChildPath");
                        String subNodeName = atts.getValue("SubNodeName");
                        childNodePathsList.add(new String[]{childPath, subNodeName});
                    }
                    if (name.equals("TemplateComponent")) {
                        String fileName = atts.getValue("FileName");
                        String displayName = atts.getValue("DisplayName");
                        String insertBefore = atts.getValue("InsertBefore");
                        String maxOccurs = atts.getValue("MaxOccurs");
                        if (insertBefore == null) {
                            insertBefore = "";
//                            System.out.println(insertBefore + " : " + maxOccurs);
                        }
                        if (maxOccurs == null) {
                            maxOccurs = "-1";
                        }
                        templateComponentList.add(new String[]{fileName, displayName, insertBefore, maxOccurs});
                    }
                    if (name.equals("RootTemplateComponent")) {
                        String fileName = atts.getValue("FileName");
                        String displayName = atts.getValue("DisplayName");
                        rootTemplateComponentList.add(new String[]{fileName, displayName});
                    }
                    if (name.equals("FieldUsage")) {
                        String fieldPath = atts.getValue("FieldPath");
                        String fieldDescription = atts.getValue("FieldDescription");
                        fieldUsageList.add(new String[]{fieldPath, fieldDescription});
                    }
                    if (name.equals("AutoField")) {
                        String fieldPath = atts.getValue("FieldPath");
                        String fileAttribute = atts.getValue("FileAttribute");
                        autoFieldsList.add(new String[]{fieldPath, fileAttribute});
                    }
                    if (name.equals("TreeNodeNameField")) {
                        String fieldsShortName = atts.getValue("FieldsShortName");
                        preferredNameFieldsList.add(fieldsShortName);
                    }
                }

                @Override
                public void endDocument() throws SAXException {
                    super.endDocument();
                    requiredFields = requiredFieldsList.toArray(new String[]{});
                    genreSubgenreArray = genreSubgenreList.toArray(new String[][]{});
                    fieldConstraints = fieldConstraintList.toArray(new String[][]{});
                    fieldTriggersArray = fieldTriggersList.toArray(new String[][]{});
                    childNodePaths = childNodePathsList.toArray(new String[][]{});
                    templatesArray = templateComponentList.toArray(new String[][]{});
                    rootTemplatesArray = rootTemplateComponentList.toArray(new String[][]{});
                    fieldUsageArray = fieldUsageList.toArray(new String[][]{});
                    autoFieldsArray = autoFieldsList.toArray(new String[][]{});
                    preferredNameFields = preferredNameFieldsList.toArray(new String[]{});
                }
            });
            loadedTemplateName = templateName;
            URL internalTemplateName = MetadataReader.class.getResource("/nl/mpi/arbil/resources/templates/" + templateName + ".xml");
            if (templateConfigFile.exists()) {
                xmlReader.parse(templateConfigFile.getPath());
            } else if (templateName.equals("Sign Language") || templateName.equals("template_cmdi")) {// (new File(internalTemplateName.getFile()).exists()) {
                xmlReader.parse(internalTemplateName.toExternalForm());
            } else {
                loadedTemplateName = "Default"; // (" + loadedTemplateName + ") n/a";
                // todo: LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("A template could not be read.\n" + templateConfigFile.getAbsolutePath() + "\nThe default template will be used instead.", "Load Template");
                xmlReader.parse(MetadataReader.class.getResource("/nl/mpi/arbil/resources/templates/template.xml").toExternalForm());
            }
            return true;
        } catch (Exception ex) {
            messageDialogHandler.addMessageDialogToQueue("The required template could not be read.", "Load Template");
            bugCatcher.logError("A template could not be read.", ex);
            return false;
        }
    }

    public String getTemplateName() {
        return loadedTemplateName;
    }

    public File getTemplateDirectory() {
        File currentTemplateDirectory = new File(ArbilTemplateManager.getSingleInstance().getTemplateDirectory(), loadedTemplateName);
        if (!currentTemplateDirectory.exists()) {
            currentTemplateDirectory.mkdir();
        }
        return currentTemplateDirectory;
    }

    public File getTemplateComponentDirectory() {
        File currentTemplateComponentDirectory = new File(getTemplateDirectory(), "components");
        if (!currentTemplateComponentDirectory.exists()) {
            currentTemplateComponentDirectory.mkdir();
        }
        return currentTemplateComponentDirectory;
    }

    /**
     * @return the rootTemplatesArray
     */
    public String[][] getRootTemplatesArray() {
        return rootTemplatesArray;
    }
}
