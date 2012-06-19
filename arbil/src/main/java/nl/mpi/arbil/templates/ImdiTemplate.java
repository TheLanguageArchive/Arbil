package nl.mpi.arbil.templates;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilVocabulary;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import org.xml.sax.SAXException;

/**
 * ImdiTemplate.java
 * Created on Aug 14, 2009, 11:30:20 AM
 *
 * @author Peter.Withers@mpi.nl
 */
public class ImdiTemplate implements ArbilTemplate {

    private static MessageDialogHandler messageDialogHandler;

    public static void setMessageDialogHandler(MessageDialogHandler handler) {
	messageDialogHandler = handler;
    }
    private final File templateFile;
    protected Hashtable<String, ArbilVocabulary> vocabularyHashTable = null; // this is used by clarin vocabularies. clarin vocabularies are also stored with the imdi vocabularies in the Imdi Vocabularies class.
    private String loadedTemplateName;
    protected String[] preferredNameFields;
    protected String[][] fieldTriggersArray;
    /*
     * <FieldTriggers>
     * <comment>The field triggers cause the target field to be set after the source field is edited, the value set in the target is
     * determined by the controlled vocabulary on the source field</comment>
     * <comment>The primary use fof these triggers are to set the corresponding language code when the language name field is changed is
     * set</comment>
     * <comment>The SourceFieldValue sets the source of the data to be inserted into the target field from the source fields controlled
     * vocabulary. Possible values relate to the vocabulary xml format and include: "Content" "Value" "Code" "FollowUp".</comment>
     * <FieldTrigger SourceFieldPath=".METATRANSCRIPT.Session.MDGroup.Content.Languages.Language(x).Name"
     * TargetFieldPath=".METATRANSCRIPT.Session.MDGroup.Content.Languages.Language(x).Id" SourceFieldValue = "Content" />
     * <FieldTrigger SourceFieldPath=".METATRANSCRIPT.Session.MDGroup.Actors.Actor(x).Languages.Language(x).Name"
     * TargetFieldPath=".METATRANSCRIPT.Session.MDGroup.Actors.Actor(x).Languages.Language(x).Id" SourceFieldValue = "Content" />
     * <FieldTrigger SourceFieldPath=".METATRANSCRIPT.Session.MDGroup.Actors.Actor(x).Language.Name"
     * TargetFieldPath=".METATRANSCRIPT.Session.MDGroup.Actors.Actor(x).Language.Id" SourceFieldValue = "Content" />
     * <FieldTrigger SourceFieldPath=".METATRANSCRIPT.Catalogue.SubjectLanguages.Language(x).Name"
     * TargetFieldPath=".METATRANSCRIPT.Catalogue.SubjectLanguages.Language(x).Id" SourceFieldValue = "Content" />
     * <FieldTrigger SourceFieldPath=".METATRANSCRIPT.Catalogue.DocumentLanguages.Language.Name"
     * TargetFieldPath=".METATRANSCRIPT.Catalogue.DocumentLanguages.Language.Id" SourceFieldValue = "Content" />
     * <comment>The LexiconResource field has no related id field and so is excluded from this list
     * ".METATRANSCRIPT.Session.Resources.LexiconResource(x).MetaLanguages.Language"</comment>
     * </FieldTriggers>
     */
    protected String[][] genreSubgenreArray;
    /*
     * <comment>The field pairs listed here will be linked as genre subgenre where the subgenre field gets its controlled vocabulary from
     * the genre fields current selection</comment>
     * <GenreSubgenre Subgenre=".METATRANSCRIPT.Session.MDGroup.Content.SubGenre" Genre=".METATRANSCRIPT.Session.MDGroup.Content.Genre"
     * Description="description" />
     */
    protected String[] requiredFields;
    /*
     * <?xml version="1.0" encoding="UTF-8"?>
     * <template>
     * <comment>The fields listed here as required fields will be highlighted in the application until they have a value entered</comment>
     * <RequiredField FieldPath=".METATRANSCRIPT.Session.Title" />
     * <RequiredField FieldPath=".METATRANSCRIPT.Session.Name" />
     * <RequiredField FieldPath=".METATRANSCRIPT.Session.Description" />
     * <RequiredField FieldPath=".METATRANSCRIPT.Session.MDGroup.Content.Genre" />
     * <RequiredField FieldPath=".METATRANSCRIPT.Session.MDGroup.Content.SubGenre" />
     * <RequiredField FieldPath=".METATRANSCRIPT.Session.Resources.Anonyms.Access.Contact.Email" />
     * <RequiredField FieldPath=".METATRANSCRIPT.Session.MDGroup.Project.Title" />
     * <RequiredField FieldPath=".METATRANSCRIPT.Corpus.Title" />
     * <RequiredField FieldPath=".METATRANSCRIPT.Session.Title" />
     * <RequiredField FieldPath=".METATRANSCRIPT.Session.MDGroup.Project.Id" />
     * </template>
     */
    protected String[][] fieldConstraints;
//        (ISO639(-1|-2|-3)?:.*)?"/>
//			<xsd:pattern value="(RFC3066:.*)?"/>
//			<xsd:pattern value="(RFC1766:.*)?"/>
//			<xsd:pattern value="(SIL:.*)?"/>
//                        "[0-9][0-9]:[0-9][0-9]:[0-9][0-9]:?[0-9]*|Unknown|Unspecified"
    //   };
    /*
     * <FieldConstraints>
     * <comment>The fields listed here will be required to match the regex constraint and will be highlighted in the application if they do
     * not</comment>
     * <FieldConstraint FieldPath=".METATRANSCRIPT.Session.MDGroup.Actors.Actor(15).BirthDate", Constraint="([0-9]+)((-[0-9]+)(-[0-9]+)?)?">
     * <FieldConstraint FieldPath=".METATRANSCRIPT.Session.Date", Constraint="([0-9]+)((-[0-9]+)(-[0-9]+)?)?">
     * <FieldConstraint FieldPath=".METATRANSCRIPT.Session.Resources.Anonyms.Access.Date", Constraint="([0-9]+)((-[0-9]+)(-[0-9]+)?)?">
     * <FieldConstraint FieldPath=".METATRANSCRIPT.Session.MDGroup.Actors.Actor(3).BirthDate", Constraint="([0-9]+)((-[0-9]+)(-[0-9]+)?)?">
     * <FieldConstraint FieldPath=".METATRANSCRIPT.Session.Resources.Anonyms.Access.Contact.Email", Constraint="([.]+)@([.]+)">
     * </FieldConstraints>
     */
    protected String[][] childNodePaths;
    protected String[][] fieldUsageArray;
    protected String[][] resourceNodePaths; // this could be initialised for IMDI templates but at this stage it will not be used in IMDI nodes

    @Override
    public boolean pathCanHaveResource(String nodePath) {
	if (resourceNodePaths != null) {
	    // so far this is only used by cmdi but should probably replace the methods used by the equivalent imdi code
	    if (nodePath == null) {
		// Root can have resource
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
	}
	return false;
    }

    @Override
    public boolean pathIsEditableField(String nodePath) {
//        System.out.println("nodePath: " + nodePath);
	for (String[] pathString : childNodePaths) {
	    if (pathString[0].startsWith(nodePath) || pathString[0].equals(nodePath)) {
		return false;
	    }
	}
	for (String[] pathString : getTemplatesArray()) { // some profiles do not have sub nodes hence this needs to be checked also
	    if (pathString[0].startsWith(nodePath) && !pathString[0].equals(nodePath)) {
		return false;
	    }
	}
	return true;
    }

    @Override
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
	 * <ChildNodePaths>
	 * <comment>The child node paths are used to determin the points at which to add a meta node in the user interface and to provide
	 * the text for the meta node name</comment>
	 * <ChildNodePath ChildPath=".METATRANSCRIPT.Session.MDGroup.Content.Languages.Language" SubNodeName="Languages" />
	 * <ChildNodePath ChildPath=".Languages.Language" SubNodeName="Languages" />
	 * <ChildNodePath ChildPath=".METATRANSCRIPT.Session.MDGroup.Actors.Actor" SubNodeName="Actors" />
	 * <ChildNodePath ChildPath=".METATRANSCRIPT.Session.Resources.MediaFile" SubNodeName="MediaFiles" />
	 * <ChildNodePath ChildPath=".METATRANSCRIPT.Session.Resources.WrittenResource" SubNodeName="WrittenResources" />
	 * <ChildNodePath ChildPath=".METATRANSCRIPT.Session.Resources.Source" SubNodeName="Sources" />
	 * <ChildNodePath ChildPath=".METATRANSCRIPT.Session.Resources.LexiconResource" SubNodeName="LexiconResource" />
	 * <ChildNodePath ChildPath=".METATRANSCRIPT.Catalogue.Location" SubNodeName="Location" />
	 * <ChildNodePath ChildPath=".METATRANSCRIPT.Catalogue.SubjectLanguages.Language" SubNodeName="SubjectLanguages" />
	 * </ChildNodePaths>
	 */
    }
    protected String[][] templatesArray; // TODO: separate the filename from the xpath by adding the nodepath as a separate value so that there can be multiple of the same type
    protected String[][] rootTemplatesArray;
    protected String[][] autoFieldsArray;

    @Override
    public boolean pathIsDeleteableField(String nodePath) {
	// modify the path to match the file name until the file name and assosiated array is updated to contain the xmpath filename and menu text
	String imdiNodePath = nodePath.substring(1) + ".xml";
	String cmdiNodePath = nodePath.replaceAll("\\(x\\)", "");
	for (String[] pathString : getTemplatesArray()) {
	    if (pathString[0].equals((cmdiNodePath)) || pathString[0].equals(imdiNodePath)) {
		return true;
	    }
	}
	return false;
    }

    @Override
    public int getMaxOccursForTemplate(String templatPath) {
	// modify the path to match the file name until the file name and assosiated array is updated to contain the xmpath filename and menu text
	String cmdiNodePath = templatPath.replaceAll("\\(\\d*?\\)", "(x)");
	cmdiNodePath = cmdiNodePath.replaceAll("\\(x\\)$", "");
	String imdiNodePath = cmdiNodePath.substring(1) + ".xml";
	for (String[] pathString : getTemplatesArray()) {
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

    @Override
    public String getInsertBeforeOfTemplate(String templatPath) {
	// modify the path to match the file name until the file name and assosiated array is updated to contain the xmpath filename and menu text
	String cmdiNodePath = templatPath.replaceAll("\\(\\d*?\\)", "(x)");
	cmdiNodePath = cmdiNodePath.replaceAll("\\(x\\)$", "");
	String imdiNodePath = cmdiNodePath.substring(1) + ".xml";
	for (String[] pathString : getTemplatesArray()) {
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

    @Override
    public boolean isArbilChildNode(String childType) {
	boolean returnValue = false;
	if (childType != null) {
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
		for (String[] currentTemplate : getTemplatesArray()) {
		    if (childTypeTemp.equals(currentTemplate[0])) {
			returnValue = true;
		    }
		}
	    }
	}
	return returnValue; //!childType.equals(imdiPathSeparator + "METATRANSCRIPT" + imdiPathSeparator + "Session") && !childType.equals(imdiPathSeparator + "METATRANSCRIPT" + imdiPathSeparator + "Corpus") && !childType.equals(imdiPathSeparator + "METATRANSCRIPT" + imdiPathSeparator + "Catalogue");
    }

    /**
     *
     * @param nodepath
     * @return List of arrays, with String[0] having the value, String[1]
     */
    private List<String[]> getSubnodesFromTemplatesDir(String nodepath) {
	Vector<String[]> returnVector = new Vector<String[]>();
	System.out.println("getSubnodesOf: " + nodepath);
	String targetNodePath = nodepath.substring(0, nodepath.lastIndexOf(")") + 1);
	nodepath = nodepath.replaceAll("\\(\\d*?\\)", "\\(x\\)");
	System.out.println("nodepath: " + nodepath);
	System.out.println("targetNodePath: " + targetNodePath);

	/* try {
	 * // System.out.println("get templatesDirectory");
	 * File templatesDirectory = new File(this.getClass().getResource("/nl/mpi/arbil/resources/templates/").getFile());
	 * // System.out.println("check templatesDirectory");
	 * if (templatesDirectory.exists()) { // compare the templates directory to the array and throw if there is a discrepancy
	 * // System.out.println("using templatesDirectory");
	 * String[] testingListing = templatesDirectory.list();
	 * Arrays.sort(testingListing);
	 * for (String itemString : testingListing) {
	 * System.out.println("\"" + itemString + "\",");
	 * }
	 * Arrays.sort(templatesArray, new Comparator() {
	 *
	 * public int compare(Object obj1, Object obj2) {
	 * return ((String[]) obj1)[0].compareToIgnoreCase(((String[]) obj2)[0]);
	 * }
	 * });
	 * int linesRead = 0;
	 * for (String[] currentTemplate : templatesArray) {
	 * // System.out.println("currentTemplate: " + currentTemplate + " : " + testingListing[linesRead]);
	 * if (testingListing != null) {
	 * if (!testingListing[linesRead].equals(currentTemplate[0])) {
	 * System.out.println("error: " + currentTemplate[0] + " : " + testingListing[linesRead]);
	 * // GuiHelper.linorgBugCatcher.logError(new Exception("error in the templates array"));
	 * }
	 * }
	 * linesRead++;
	 * }
	 * if (testingListing != null) {
	 * if (testingListing.length - 2 != linesRead) {
	 * System.out.println(testingListing[linesRead]);
	 * // GuiHelper.linorgBugCatcher.logError(new Exception("error missing line in the templates array"));
	 * }
	 * }
	 * }
	 * } catch (Exception ex) {
	 * GuiHelper.linorgBugCatcher.logError(ex);
	 * } */
	for (String[] currentTemplate : getTemplatesArray()) {
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

    @Override
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

    @Override
    public Enumeration listTypesFor(Object targetNodeUserObject) {
	return listTypesFor(targetNodeUserObject, false);
    }

    /**
     * This function is only a place holder and will be replaced.
     *
     * @param targetNodeUserObject The imdi node that will receive the new child.
     * @return An enumeration of Strings for the available child types, one of which will be passed to "listFieldsFor()".
     */
    public Enumeration<String[]> listTypesFor(Object targetNodeUserObject, boolean includeCorpusNodeEntries) {
	// Get possible types for object
	List<String[]> childTypes = getTypesFor(targetNodeUserObject, includeCorpusNodeEntries);
	// Sort on field value (not name)
	Collections.sort(childTypes, new Comparator() {

	    public int compare(Object o1, Object o2) {
		String value1 = ((String[]) o1)[0];
		String value2 = ((String[]) o2)[0];
		return value1.compareTo(value2);
	    }
	});
	return Collections.enumeration(childTypes);
    }

    private List<String[]> getTypesFor(Object targetNodeUserObject, boolean includeCorpusNodeEntries) {
	// TODO: implement this using data from the xsd on the server (server version needs to be updated)
	List<String[]> childTypes;
	if (targetNodeUserObject instanceof ArbilDataNode) {
	    ArbilDataNode targetNode = (ArbilDataNode) targetNodeUserObject;
	    String xpath = targetNode.getDataNodeService().getNodePath(targetNode);
	    childTypes = getSubnodesFromTemplatesDir(xpath); // add the main entries based on the node path of the target
	    if (includeCorpusNodeEntries && (targetNode).isCorpus()) { // add any corpus node entries
		for (String[] currentTemplate : rootTemplatesArray) {
		    boolean suppressEntry = false;
		    if (currentTemplate[1].equals("Catalogue")) {
			if ((targetNode).hasCatalogue()) {
			    // make sure the catalogue can only be added once
			    suppressEntry = true;
			}
		    }
		    if (!suppressEntry) {
			childTypes.add(new String[]{currentTemplate[1], "." + currentTemplate[0].replaceFirst("\\.xml$", "")});
		    }
		}
	    }
	} else {
	    childTypes = new ArrayList<String[]>();
	    // add the the root node items
	    for (String[] currentTemplate : rootTemplatesArray) {
		if (!currentTemplate[1].equals("Catalogue")) {// make sure the catalogue can not be added at the root level
		    childTypes.add(new String[]{"Unattached " + currentTemplate[1], "." + currentTemplate[0].replaceFirst("\\.xml$", "")});
		}
	    }
	}
	return childTypes;
    }

    @Override
    public ArrayList<String> listAllTemplates() {
	ArrayList<String> returnArrayList = new ArrayList<String>();
	for (String[] currentTemplate : getTemplatesArray()) {
	    returnArrayList.add(currentTemplate[0]);
	}
	return returnArrayList;
    }

    protected String getFieldUsageStringForField(String normalizedFieldName) {
	for (String[] currentUsageArray : fieldUsageArray) {
	    if (currentUsageArray[0].equals(normalizedFieldName)) {
		return currentUsageArray[1];
	    }
	}
	return null;
    }

    @Override
    public String getHelpStringForField(String fieldName) {
	String helpString = getFieldUsageStringForField(fieldName.replaceAll("\\([0-9]+\\)\\.", "."));
	if (helpString != null) {
	    return helpString;
	} else {
	    return "No usage description found in this template for: " + fieldName;
	}
    }

    @Override
    public ArbilVocabulary getFieldVocabulary(String nodePath) {
	if (vocabularyHashTable != null) {
	    return vocabularyHashTable.get(nodePath);
	}
	return null;
    }

    /**
     * Be <strong>careful</strong> when using this, templateFile and loadedTemplateName will be set to null. If used, child should override
     * {@link #getLoadedTemplateName() } and {@link #getTemplateFile() }.
     */
    protected ImdiTemplate() {
	this.templateFile = null;
	this.loadedTemplateName = null;
    }

    public ImdiTemplate(File templateFile, String loadedTemplateName) {
	this.templateFile = templateFile;
	this.loadedTemplateName = loadedTemplateName;
    }

    @Override
    public boolean readTemplate() {
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
	    URL internalTemplateName = ImdiTemplate.class.getResource("/nl/mpi/arbil/resources/templates/" + getLoadedTemplateName() + ".xml");
	    if (templateFile.exists()) {
		xmlReader.parse(templateFile.getPath());
	    } else if (getLoadedTemplateName().equals("Sign Language") || getLoadedTemplateName().equals("template_cmdi")) {// (new File(internalTemplateName.getFile()).exists()) {
		xmlReader.parse(internalTemplateName.toExternalForm());
	    } else {
		loadedTemplateName = "Default"; // (" + loadedTemplateName + ") n/a";
		// todo: LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("A template could not be read.\n" + templateConfigFile.getAbsolutePath() + "\nThe default template will be used instead.", "Load Template");
		xmlReader.parse(ImdiTemplate.class.getResource("/nl/mpi/arbil/resources/templates/template.xml").toExternalForm());
	    }
	    return true;
	} catch (Exception ex) {
	    messageDialogHandler.addMessageDialogToQueue("The required template could not be read.", "Load Template");
	    BugCatcherManager.getBugCatcher().logError("A template could not be read.", ex);
	    return false;
	}
    }

    @Override
    public String getTemplateName() {
	return getLoadedTemplateName();
    }

    @Override
    public File getTemplateDirectory() {
	File currentTemplateDirectory = new File(ArbilTemplateManager.getSingleInstance().getTemplateDirectory(), getLoadedTemplateName());
	if (!currentTemplateDirectory.exists()) {
	    if (!currentTemplateDirectory.mkdirs()) {
		throw new RuntimeException("Could not create template directory: " + currentTemplateDirectory);
	    }
	}
	return currentTemplateDirectory;
    }

    @Override
    public File getTemplateComponentDirectory() {
	File currentTemplateComponentDirectory = new File(getTemplateDirectory(), "components");
	if (!currentTemplateComponentDirectory.exists()) {
	    if (!currentTemplateComponentDirectory.mkdirs()) {
		throw new RuntimeException("Could not create component template directory: " + currentTemplateComponentDirectory);
	    }
	}
	return currentTemplateComponentDirectory;
    }

    /**
     * @return the rootTemplatesArray
     */
    @Override
    public String[][] getRootTemplatesArray() {
	return rootTemplatesArray;
    }

    /**
     * @return the templateFile
     */
    @Override
    public URL getTemplateFile() {
	try {
	    return templateFile.toURI().toURL();
	} catch (MalformedURLException e) {
	    return null;
	}
    }

    /**
     * @return the preferredNameFields
     */
    public String[] getPreferredNameFields() {
	return preferredNameFields;
    }

    /**
     * @return the fieldTriggersArray
     */
    @Override
    public String[][] getFieldTriggersArray() {
	return fieldTriggersArray;
    }

    /**
     * @return the genreSubgenreArray
     */
    @Override
    public String[][] getGenreSubgenreArray() {
	return genreSubgenreArray;
    }

    /**
     * @return the requiredFields
     */
    @Override
    public String[] getRequiredFields() {
	return requiredFields;
    }

    /**
     * @return the fieldConstraints
     */
    @Override
    public String[][] getFieldConstraints() {
	return fieldConstraints;
    }

    /**
     * @return the autoFieldsArray
     */
    @Override
    public String[][] getAutoFieldsArray() {
	return autoFieldsArray;
    }

    /**
     * @return the templatesArray
     */
    public String[][] getTemplatesArray() {
	return templatesArray;
    }

    /**
     * @return the loadedTemplateName
     */
    public String getLoadedTemplateName() {
	return loadedTemplateName;
    }

    public List<String[]> getEditableAttributesForPath(String path) {
	// CMDI only at this point
	return Collections.emptyList();
    }
}
