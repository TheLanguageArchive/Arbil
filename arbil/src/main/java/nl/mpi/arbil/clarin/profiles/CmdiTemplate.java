package nl.mpi.arbil.clarin.profiles;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerException;
import nl.mpi.arbil.ArbilDesktopInjector;
import nl.mpi.arbil.clarin.profiles.CmdiProfileReader.CmdiProfile;
import nl.mpi.arbil.data.ArbilComponentBuilder;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilEntityResolver;
import nl.mpi.arbil.data.ArbilVocabularies;
import nl.mpi.arbil.data.ArbilVocabulary;
import nl.mpi.arbil.data.CmdiDocumentationLanguages;
import nl.mpi.arbil.templates.ArbilTemplate;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import org.apache.xmlbeans.SchemaAnnotation;
import org.apache.xmlbeans.SchemaLocalElement;
import org.apache.xmlbeans.SchemaParticle;
import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xpath.CachedXPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * CmdiTemplate.java
 * Created on March 10, 2010, 17:34:45 AM
 *
 * @author Peter.Withers@mpi.nl
 */
public class CmdiTemplate extends ArbilTemplate {

    public static final String RESOURCE_REFERENCE_ATTRIBUTE = "ref";
    public static final String LANGUAGE_ATTRIBUTE = String.format("{%1$s}lang", ArbilComponentBuilder.encodeNsUriForAttributePath("http://www.w3.org/XML/1998/namespace")); // {http://www.w3.org/XML/1998/namespace}lang
    /**
     * Attributes that are reserved by CMDI and should show up as editable.
     * Namespace URI's should appear encoded in this list
     */
    public final static Collection<String> RESERVED_ATTRIBUTES = Collections.unmodifiableCollection(Arrays.asList(
	    RESOURCE_REFERENCE_ATTRIBUTE // resource proxy ref attribute
	    , LANGUAGE_ATTRIBUTE, "componentId" // componentId
	    , "ComponentId" // componentId, alternate spelling in some profiles
	    ));
    public final static String DATCAT_URI_DESCRIPTION_POSTFIX = ".dcif?workingLanguage=en";
    /**
     * Pattern of URIs that should not be parsed as DCIF (but may occur as datcat URIs)
     */
    public final static Pattern DATCAT_URI_SKIP_PATTERN = Pattern.compile(".*purl\\.org.*");
    public static final int SCHEMA_CACHE_EXPIRY_DAYS = 100;
    private final static SAXParserFactory parserFactory = SAXParserFactory.newInstance();
    private static MessageDialogHandler messageDialogHandler;

    public static void setMessageDialogHandler(MessageDialogHandler handler) {
	messageDialogHandler = handler;
    }
    private final SessionStorage sessionStorage;

    public CmdiTemplate(SessionStorage sessionStorage) {
	super(new CmdiDocumentationLanguages(sessionStorage));
	this.sessionStorage = sessionStorage;
    }
    private String nameSpaceString;
    // todo: these filter strings should really be read from the metadata format
    private String filterString[] = {".CMD.Resources.", ".CMD.Header.", ".Kinnate.Entity."};
    private Document schemaDocument;
    private Map<String, String> dataCategoriesMap;
    private final Map<String, String> dataCategoryDescriptionMap = Collections.synchronizedMap(new HashMap<String, String>());
    protected HashSet<String> allowsLanguageIdPathList;

    private static class ArrayListGroup {

	public ArrayList<String[]> childNodePathsList = new ArrayList<String[]>();
	public ArrayList<String[]> addableComponentPathsList = new ArrayList<String[]>();
	public ArrayList<String[]> resourceNodePathsList = new ArrayList<String[]>();
	public ArrayList<String[]> fieldConstraintList = new ArrayList<String[]>();
	public ArrayList<String[]> displayNamePreferenceList = new ArrayList<String[]>();
	public ArrayList<String[]> fieldUsageDescriptionList = new ArrayList<String[]>();
	public Map<String, String> dataCategoriesMap = Collections.synchronizedMap(new HashMap<String, String>());
	public HashSet<String> allowsLanguageIdPathsList = new HashSet<String>();
    }

    private static class ElementCardinality {

	public int maxOccurs;
	public boolean canHaveMultiple;
    }

    public void loadTemplate(String nameSpaceStringLocal) {
	vocabularyHashTable = new Hashtable<String, ArbilVocabulary>();
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

	    ArrayListGroup arrayListGroup = new ArrayListGroup();
	    URI xsdUri = new URI(nameSpaceString);
	    readSchema(xsdUri, arrayListGroup);
	    childNodePaths = arrayListGroup.childNodePathsList.toArray(new String[][]{});
	    templatesArray = arrayListGroup.addableComponentPathsList.toArray(new String[][]{});
	    resourceNodePaths = arrayListGroup.resourceNodePathsList.toArray(new String[][]{});
	    fieldConstraints = arrayListGroup.fieldConstraintList.toArray(new String[][]{});
	    fieldUsageArray = arrayListGroup.fieldUsageDescriptionList.toArray(new String[][]{});
	    allowsLanguageIdPathList = arrayListGroup.allowsLanguageIdPathsList;
	    dataCategoriesMap = arrayListGroup.dataCategoriesMap;
	    makeGuiNamesUnique();

	    // sort and construct the preferredNameFields array
	    String[][] tempSortableArray = arrayListGroup.displayNamePreferenceList.toArray(new String[][]{});
	    Arrays.sort(tempSortableArray, displayNamePreferenceComparator);
	    preferredNameFields = new String[tempSortableArray.length];
	    for (int nameFieldCounter = 0; nameFieldCounter < getPreferredNameFields().length; nameFieldCounter++) {
		preferredNameFields[nameFieldCounter] = tempSortableArray[nameFieldCounter][0];
	    }
	    // end sort and construct the preferredNameFields array
	} catch (URISyntaxException urise) {
	    BugCatcherManager.getBugCatcher().logError(urise);
	}
	// this should be adequate for cmdi templates
	//templatesArray = childNodePaths;
	// TODO: complete these
	requiredFields = new String[]{};
	fieldTriggersArray = new String[][]{};
	autoFieldsArray = new String[][]{};
	genreSubgenreArray = new String[][]{};
    }

    private void makeGuiNamesUnique() {
	// template array is the super set while childnodes array is shorter
	boolean allGuiNamesUnique = false;
	while (!allGuiNamesUnique) {
	    allGuiNamesUnique = true;
	    for (String[] currentTemplate : templatesArray) {
		String currentTemplateGuiName = currentTemplate[1];
		String currentTemplatePath = currentTemplate[0];
		for (String[] secondTemplate : templatesArray) {
		    String secondTemplateGuiName = secondTemplate[1];
		    String secondTemplatePath = secondTemplate[0];
		    if (!currentTemplatePath.equals(secondTemplatePath)) {
			if (currentTemplateGuiName.equals(secondTemplateGuiName)) {
			    allGuiNamesUnique = false;
			    for (String[] templateToChange : templatesArray) {
				String templateToChangeGuiName = templateToChange[1];
				String templateToChangePath = templateToChange[0];
				if (templateToChangeGuiName.equals(currentTemplateGuiName)) {
				    int pathCount = templateToChangeGuiName.split("\\.").length;
				    String[] templateToChangePathParts = templateToChangePath.split("\\.");
				    templateToChange[1] = templateToChangePathParts[templateToChangePathParts.length - pathCount - 1] + "." + templateToChangeGuiName;
				}
			    }
			    for (String[] templateToChange : childNodePaths) {
				String templateToChangeGuiName = templateToChange[1];
				String templateToChangePath = templateToChange[0];
				if (templateToChangeGuiName.equals(currentTemplateGuiName)) {
				    int pathCount = templateToChangeGuiName.split("\\.").length;
				    String[] templateToChangePathParts = templateToChangePath.split("\\.");
				    templateToChange[1] = templateToChangePathParts[templateToChangePathParts.length - pathCount - 1] + "." + templateToChangeGuiName;
				}
			    }
			}
		    }
		}
	    }
	}
    }

    public List<String[]> getEditableAttributesForPath(final String path) {
	LinkedList<String[]> attributePaths = new LinkedList<String[]>();
	final String pathAsPrefix = path + ".";
	for (String[] templatePath : templatesArray) {
	    if (ArbilComponentBuilder.pathIsAttribute(templatePath[0]) // should be an attribute
		    && templatePath[0].startsWith(pathAsPrefix) // should be a child of path
		    && pathIsEditableAttribute(templatePath[0])) { // should be editable
		attributePaths.add(templatePath);
	    }
	}
	return attributePaths;
    }

    @Override
    public Enumeration listTypesFor(Object targetNodeUserObject) {
	// get the xpath of the target node
	String targetNodeXpath = ((ArbilDataNode) targetNodeUserObject).getURI().getFragment();
	boolean isComponentPath = false;
	if (targetNodeXpath != null) {
	    isComponentPath = targetNodeXpath.endsWith(")");
	    // remove the extraneous node name for a meta node
//            targetNodeXpath = targetNodeXpath.replaceAll("\\.[^\\.]+[^\\)]$", "");
	    // remove the sibling indexes
	    targetNodeXpath = targetNodeXpath.replaceAll("\\(\\d+\\)", "");
	}
	Vector<String[]> childTypes = new Vector<String[]>();
	if (targetNodeUserObject instanceof ArbilDataNode) {
	    for (String[] childPathString : templatesArray) {
		boolean allowEntry = false;
		// allowing due to null path
		if (targetNodeXpath == null) {
		    allowEntry = true;
		} else if (childPathString[0].startsWith(targetNodeXpath)) {
		    allowEntry = true;
		}
		//disallowing addint to itself
		if (childPathString[0].equals(targetNodeXpath) && isComponentPath) {
		    allowEntry = false;
		}
		for (String currentFilter : filterString) {
		    if (childPathString[0].startsWith(currentFilter)) {
			allowEntry = false;
		    }
		}
		if (allowEntry) {
		    childTypes.add(new String[]{childPathString[1], childPathString[0]});
		}
	    }
	    String[][] childTypesArray = childTypes.toArray(new String[][]{});
	    childTypes.removeAllElements();
	    for (String[] currentChildType : childTypesArray) {
		// filter out sub nodes that cannot be added at the current level because they require an intermediate node to be added, ie "actors language" requires an "actor"
		boolean keepChildType = (!ArbilComponentBuilder.pathIsAttribute(currentChildType[1]) || pathIsEditableField(currentChildType[1]));

		if (keepChildType) {
		    for (String[] subChildType : childTypesArray) {
			if (!ArbilComponentBuilder.pathIsAttribute(subChildType[1]) && currentChildType[1].startsWith(subChildType[1])) {
			    String remainderString = currentChildType[1].substring(subChildType[1].length());
			    //if (currentChildType[1].length() != subChildType[1].length()) {
			    if (remainderString.contains(".")) {
				keepChildType = false;
			    }
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

    private void readSchema(URI xsdFile, ArrayListGroup arrayListGroup) {
	File schemaFile;
	if (xsdFile.getScheme().equals("file")) {
	    schemaFile = new File(xsdFile);
	} else {
	    schemaFile = sessionStorage.updateCache(xsdFile.toString(), SCHEMA_CACHE_EXPIRY_DAYS, false);
	}
	templateFile = schemaFile; // store the template file for later use such as adding child nodes
	try {
	    InputStream inputStream = new FileInputStream(schemaFile);
	    //Since we're dealing with xml schema files here the character encoding is assumed to be UTF-8
	    XmlOptions xmlOptions = new XmlOptions();
	    xmlOptions.setCharacterEncoding("UTF-8");
	    xmlOptions.setEntityResolver(new ArbilEntityResolver(xsdFile));
//            xmlOptions.setCompileDownloadUrls();
	    SchemaTypeSystem sts = XmlBeans.compileXsd(new XmlObject[]{XmlObject.Factory.parse(inputStream, xmlOptions)}, XmlBeans.getBuiltinTypeSystem(), xmlOptions);
	    SchemaType schemaType = sts.documentTypes()[0];
	    constructXml(schemaType, arrayListGroup, "", new CachedXPathAPI());
//            for (SchemaType schemaType : sts.documentTypes()) {
////                System.out.println("T-documentTypes:");
//                constructXml(schemaType, arrayListGroup, "", "");
//                break; // there can only be a single root node and the IMDI schema specifies two (METATRANSCRIPT and VocabularyDef) so we must stop before that error creates another
//            }
	} catch (IOException e) {
	    BugCatcherManager.getBugCatcher().logError(getTemplateFile().getName(), e);
	    messageDialogHandler.addMessageDialogToQueue("Could not open the required template file: " + getTemplateFile().getName(), "Load Clarin Template");
	} catch (XmlException e) {
	    BugCatcherManager.getBugCatcher().logError(getTemplateFile().getName(), e);
	    messageDialogHandler.addMessageDialogToQueue("Could not read the required template file: " + getTemplateFile().getName(), "Load Clarin Template");
	}
    }

    private int constructXml(final SchemaType schemaType, ArrayListGroup arrayListGroup, final String pathString, final CachedXPathAPI xPathAPI) {
	int childCount = 0;
//        boolean hasMultipleElementsInOneNode = false;
	int subNodeCount = 0;
	readControlledVocabularies(schemaType, pathString, xPathAPI);
	readFieldConstrains(schemaType, pathString, arrayListGroup.fieldConstraintList);

	// search for annotations
	SchemaParticle topParticle = schemaType.getContentModel();
	searchForAnnotations(topParticle, pathString, arrayListGroup, xPathAPI);
	// end search for annotations
	SchemaProperty[] schemaPropertyArray = schemaType.getElementProperties();
	//        boolean currentHasMultipleNodes = schemaPropertyArray.length > 1;
	int currentNodeChildCount = 0;
	for (SchemaProperty schemaProperty : schemaPropertyArray) {
	    childCount++;
	    String localName = schemaProperty.getName().getLocalPart();
	    String currentPathString = pathString + "." + localName;
	    String currentNodeMenuName;
	    if (localName != null) {
		currentNodeChildCount++;
		ElementCardinality cardinality = determineElementCardinality(schemaProperty);
		SchemaType currentSchemaType = schemaProperty.getType();
		currentNodeMenuName = localName;
		subNodeCount = constructXml(currentSchemaType, arrayListGroup, currentPathString, xPathAPI);
		if (cardinality.canHaveMultiple) {
		    if (subNodeCount > 0) {
//                todo check for case of one or only single sub element and when found do not add as a child path
			arrayListGroup.childNodePathsList.add(new String[]{currentPathString, pathString.substring(pathString.lastIndexOf(".") + 1)});
		    }
		    String insertBefore = "";
		    arrayListGroup.addableComponentPathsList.add(new String[]{currentPathString, currentNodeMenuName, insertBefore, Integer.toString(cardinality.maxOccurs)});
		}
		readElementAttributes(currentSchemaType, arrayListGroup, currentPathString, currentNodeMenuName, localName);
	    }
	}
	subNodeCount = subNodeCount + currentNodeChildCount;
	return subNodeCount;
    }

    private ElementCardinality determineElementCardinality(SchemaProperty schemaProperty) {
	ElementCardinality cardinality = new ElementCardinality();
	if (schemaProperty.getMaxOccurs() == null) {
	    // absence of the max occurs also means multiple
	    cardinality.maxOccurs = -1;
	    cardinality.canHaveMultiple = true;
	    // todo: also check that min and max are the same because there may be cases of zero required but only one can be added
	} else if (schemaProperty.getMaxOccurs().toString().equals("unbounded")) {
	    cardinality.maxOccurs = -1;
	    cardinality.canHaveMultiple = true;
	} else {
	    // store the max occurs for use in the add menu etc
	    cardinality.maxOccurs = schemaProperty.getMaxOccurs().intValue();
	    cardinality.canHaveMultiple = schemaProperty.getMaxOccurs().intValue() > 1;
	}
	if (!cardinality.canHaveMultiple) {
	    // todo: limit the number of instances that can be added to a xml file basedon the max bounds
	    cardinality.canHaveMultiple = schemaProperty.getMinOccurs().intValue() != schemaProperty.getMaxOccurs().intValue();
	}
	return cardinality;
    }

    private void readElementAttributes(SchemaType currentSchemaType, ArrayListGroup arrayListGroup, String currentPathString, String currentNodeMenuName, String localName) {
	boolean hasResourceAttribute = false;
	for (SchemaProperty attributesProperty : currentSchemaType.getAttributeProperties()) {
	    final String attributeName = getAttributePathSection(attributesProperty.getName().getNamespaceURI(), attributesProperty.getName().getLocalPart());
	    if (attributeName.equals(RESOURCE_REFERENCE_ATTRIBUTE)) {
		hasResourceAttribute = true;
	    } else if (attributeName.equals(LANGUAGE_ATTRIBUTE)) {
		arrayListGroup.allowsLanguageIdPathsList.add(currentPathString);
	    }
	    final String insertBefore = "";
	    final String attributePath = currentPathString + ".@" + attributeName;
	    final String displayName = currentNodeMenuName + "." + attributeName.replaceAll("\\{.*\\}", "");
	    arrayListGroup.addableComponentPathsList.add(new String[]{attributePath, displayName, insertBefore, "1"});
	}
	if (hasResourceAttribute) {
	    arrayListGroup.resourceNodePathsList.add(new String[]{currentPathString, localName});
	}
    }

    public static String getAttributePathSection(QName qName) {
	return getAttributePathSection(qName.getNamespaceURI(), qName.getLocalPart());
    }

    public static String getAttributePathSection(String nsURI, String localPart) {
	if (nsURI != null && nsURI.length() > 0) {
	    nsURI = ArbilComponentBuilder.encodeNsUriForAttributePath(nsURI);
	    StringBuilder attributeNameSb = new StringBuilder("{");
	    attributeNameSb.append(nsURI);
	    attributeNameSb.append("}");
	    return attributeNameSb.append(localPart).toString();
	}

	return localPart;
    }

    private void searchForAnnotations(SchemaParticle schemaParticle, String nodePathBase, ArrayListGroup arrayListGroup, CachedXPathAPI xPathAPI) {
	if (schemaParticle != null) {
	    switch (schemaParticle.getParticleType()) {
		case SchemaParticle.SEQUENCE:
		    for (SchemaParticle schemaParticleChild : schemaParticle.getParticleChildren()) {
			String nodePath;
			if (schemaParticleChild.getName() != null) {
			    nodePath = nodePathBase + "." + schemaParticleChild.getName().getLocalPart();
			} else {
			    nodePath = nodePathBase + ".unnamed";
			    BugCatcherManager.getBugCatcher().logError(new Exception("unnamed node at: " + nodePath));
			}
			searchForAnnotations(schemaParticleChild, nodePath, arrayListGroup, xPathAPI);
		    }
		    break;
		case SchemaParticle.ELEMENT:
		    SchemaLocalElement schemaLocalElement = (SchemaLocalElement) schemaParticle;
		    saveAnnotationData(schemaLocalElement, nodePathBase, arrayListGroup);
		    break;
	    }
	} else {
	    // In case of complex type, try on element specification (xs:element)
	    Document schemaDoc = getSchemaDocument();
	    if (schemaDoc != null) {
		// Path to element specification in schema file
		String elementPath = nodePathBase.replaceFirst("\\.", "//*[@name='").replaceAll("\\.", "']//*[@name='") + "']";
		try {
		    // Get element specification
		    Node elementSpecNode = xPathAPI.selectSingleNode(schemaDoc, elementPath);
		    if (elementSpecNode != null) {
			// Get all attributes on the xs:element and look for annotation data
			NamedNodeMap attributes = elementSpecNode.getAttributes();
			for (int i = 0; i < attributes.getLength(); i++) {
			    final Node attrNode = attributes.item(i);
			    // Convert to {nsUri}localname format
			    final String nodeName = new QName(attrNode.getNamespaceURI(), attrNode.getLocalName()).toString();
			    // Check for annotation data and if so save to data structure
			    saveAnnotationData(nodePathBase, nodeName, attrNode.getNodeValue(), arrayListGroup);
			}
		    }
		} catch (TransformerException ex) {
		    // Failure to get element, so nothing can be found
		}
	    }
	}
    }

    private void saveAnnotationData(SchemaLocalElement schemaLocalElement, String nodePath, ArrayListGroup arrayListGroup) {
	SchemaAnnotation schemaAnnotation = schemaLocalElement.getAnnotation();
	if (schemaAnnotation != null) {
	    for (SchemaAnnotation.Attribute annotationAttribute : schemaAnnotation.getAttributes()) {
		final String annotationValue = annotationAttribute.getValue();
		final String annotationName = annotationAttribute.getName().toString();
		saveAnnotationData(nodePath, annotationName, annotationValue, arrayListGroup);
	    }
	}
    }

    private void saveAnnotationData(String nodePath, final String annotationName, final String annotationValue, ArrayListGroup arrayListGroup) {
	//Annotation: {ann}documentation : the title of the book
	//Annotation: {ann}displaypriority : 1
	// todo: the url here could be removed provided that it does not make it to unspecific

	if (!"".equals(annotationValue)) {
	    if ("{http://www.clarin.eu}displaypriority".equals(annotationName)) {
		arrayListGroup.displayNamePreferenceList.add(new String[]{nodePath, annotationValue});
	    }
	    if ("{http://www.clarin.eu}documentation".equals(annotationName)) {
		arrayListGroup.fieldUsageDescriptionList.add(new String[]{nodePath, annotationValue});
	    }
	    if ("{http://www.isocat.org/ns/dcr}datcat".equals(annotationName)) {
		arrayListGroup.dataCategoriesMap.put(nodePath, annotationValue);
	    }
	}
    }

    private void readFieldConstrains(SchemaType schemaType, String nodePath, ArrayList<String[]> fieldConstraintList) {
	switch (schemaType.getBuiltinTypeCode()) {
	    case SchemaType.BTC_STRING:
		// no constraint relevant for string
		break;
	    case SchemaType.BTC_DATE:
		fieldConstraintList.add(new String[]{nodePath, "([0-9][0-9][0-9][0-9])((-[0-1][0-9])(-[0-3][0-9])?)?"});// todo: complete this regex
		break;
	    case SchemaType.BTC_BOOLEAN:
		fieldConstraintList.add(new String[]{nodePath, "true|false"});// todo: complete this regex
		break;
	    case SchemaType.BTC_ANY_URI:
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
		break;
	}
    }

    private synchronized Document getSchemaDocument() {
	if (schemaDocument == null) {
	    try {
		// Parse schema document
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setValidating(false);
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		schemaDocument = documentBuilder.parse(getTemplateFile());
	    } catch (IOException ex) {
		BugCatcherManager.getBugCatcher().logError("Error while parsing schema", ex);
	    } catch (ParserConfigurationException ex) {
		BugCatcherManager.getBugCatcher().logError("Error while parsing schema", ex);
	    } catch (SAXException ex) {
		BugCatcherManager.getBugCatcher().logError("Error while parsing schema", ex);
	    }
	}
	return schemaDocument;
    }

    /**
     *
     * Gets labels for vocabulary enumerations like:
     *
     * <xs:enumeration	value="csu"
     * dcr:datcat="http://cdb.iso.org/lg/CDB-00138674-001"
     * ann:label="Central Sudanic languages"/>
     *
     * @param vocabularyName Name of the vocabulary in the profile schema
     * @return HashMap that has [value => label] mapping for the vocabulary
     * @throws TransformerException Thrown on node selection through XPathAPI if transformation fails
     */
    private HashMap<String, String> getDescriptionsForVocabulary(String vocabularyName, CachedXPathAPI xPathAPI) throws TransformerException {
	// Get document node (should not be null)
	Document schemaDoc = getSchemaDocument();
	if (schemaDoc == null) {
	    return null;
	} else {
	    // Find all enumeration values within the vocabulary
	    NodeList enumNodes = xPathAPI.selectNodeList(schemaDoc, "//*[@name='" + vocabularyName + "']//xs:restriction/xs:enumeration");
	    HashMap<String, String> descriptions = new HashMap<String, String>(enumNodes.getLength());
	    for (int i = 0; i < enumNodes.getLength(); i++) {
		NamedNodeMap attMap = enumNodes.item(i).getAttributes();
		// Look for label
		Node labelNode = attMap.getNamedItem("ann:label");
		if (labelNode != null) {
		    // Map label on value
		    Node valueNode = attMap.getNamedItem("value");
		    if (valueNode != null) {
			descriptions.put(valueNode.getTextContent(), labelNode.getTextContent());
		    }
		}
	    }
	    return descriptions;
	}
    }

    private void readControlledVocabularies(SchemaType schemaType, String nodePath, CachedXPathAPI xPathAPI) {
	XmlAnySimpleType[] enumerationValues = schemaType.getEnumerationValues();

	if (enumerationValues != null && enumerationValues.length > 0) {
	    ArbilVocabulary vocabulary = ArbilVocabularies.getSingleInstance().getEmptyVocabulary(nameSpaceString + "#" + schemaType.getName());

	    HashMap<String, String> descriptions = null;
	    try {
		// Get descriptions (ann:label attributes on vocabulary enumeration elements)
		descriptions = getDescriptionsForVocabulary(schemaType.getBaseType().getName().getLocalPart(), xPathAPI);
	    } catch (Exception ex) {
		// Fall back to using just the values, no descriptions
		BugCatcherManager.getBugCatcher().logError(ex);
	    }

	    for (XmlAnySimpleType anySimpleType : schemaType.getEnumerationValues()) {
		String entryCode = anySimpleType.getStringValue();
		String description = descriptions == null ? null : descriptions.get(entryCode);

		if (description == null || description.length() == 0) {
		    vocabulary.addEntry(entryCode, null);
		} else {
		    vocabulary.addEntry(description, entryCode);
		}
	    }
	    vocabularyHashTable.put(nodePath, vocabulary);
	}
    }

    @Override
    public String getHelpStringForField(String fieldName) {
	fieldName = fieldName.replaceAll("\\([0-9]+\\)\\.", ".");
	// First try using documentation from CMDI. This is stored in fieldUsageArray, super implementation gets this
	String fieldUsageString = getFieldUsageStringForField(fieldName);
	if (fieldUsageString != null) {
	    return fieldUsageString;
	} else {
	    // Get description from data category definition
	    String datCat = dataCategoriesMap.get(fieldName);
	    if (datCat != null) {
		// Create description request URI for datcat URI
		return getDescriptionForDataCategory(datCat);
	    } else {
		return "No usage description found in this template for: " + fieldName;
	    }
	}
    }

    /**
     * Creates a thread that downloads all descriptions that are not in cache or require refresh and reads data category descriptions
     */
    public synchronized void startLoadingDatacategoryDescriptions() {
	Runnable descriptionLoader = new Runnable() {
	    public void run() {
		for (String dcUri : dataCategoriesMap.values()) {
		    synchronized (CmdiTemplate.this) {
			try {
			    getDescriptionForDataCategory(dcUri);
			    // Wait some time
			    CmdiTemplate.this.wait(100);
			} catch (InterruptedException ex) {
			}
		    }
		}
	    }
	};
	Thread descriptionLoaderThread = new Thread(descriptionLoader);
	descriptionLoaderThread.setPriority(Thread.MIN_PRIORITY);
	descriptionLoaderThread.start();
    }

    /**
     * Gets the description for the specified data category. On first request, attempts to read description from the specified URI (assuming
     * it is an URL). Does not attempt reading if the provided URI matches {@link #DATCAT_URI_SKIP_PATTERN}
     *
     * @param dcUri URI of data category to get description for
     * @return description string or a "No description available" string if no description could be found.
     */
    private String getDescriptionForDataCategory(String dcUri) {
	if (dataCategoryDescriptionMap.containsKey(dcUri)) {
	    // Read description from DCIF only once per session
	    return dataCategoryDescriptionMap.get(dcUri);
	} else {
	    // Read description from DCIF
	    try {
		if (!DATCAT_URI_SKIP_PATTERN.matcher(dcUri).matches()) {
		    String description = readDescriptionForDataCategory(dcUri);
		    if (description != null) {
			dataCategoryDescriptionMap.put(dcUri, description);
			return description;
		    }
		}
	    } catch (ParserConfigurationException ex) {
		BugCatcherManager.getBugCatcher().logError("Exception while trying to process data category at " + dcUri, ex);
	    } catch (SAXException ex) {
		BugCatcherManager.getBugCatcher().logError("Exception while trying to process data category at " + dcUri, ex);
	    } catch (IOException ex) {
		BugCatcherManager.getBugCatcher().logError("Exception while trying to process data category at " + dcUri, ex);
	    }
	    return "Data category: <" + dcUri + ">. No description available. See error log for details.";
	}
    }

    /**
     * Reads the description for a given data category (identified by dcUri) from its DCIF
     *
     * @param dcUri URI that identifies the data category
     * @return Description string found in DCIF. Null if none found.
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    private String readDescriptionForDataCategory(String dcUri) throws ParserConfigurationException, SAXException, IOException {
	String datCatURI = dcUri.concat(DATCAT_URI_DESCRIPTION_POSTFIX);
	File datCatFile = sessionStorage.getFromCache(datCatURI, true); // follow redirects for datCatFiles
	if (datCatFile == null) {
	    BugCatcherManager.getBugCatcher().logError("File not found for data category URI " + dcUri, null);
	    return null;
	} else {
	    SAXParser parser = parserFactory.newSAXParser();
	    DataCategoryDescriptionHandler handler = new DataCategoryDescriptionHandler();
	    parser.parse(datCatFile, handler);
	    return handler.getDescription();
	}
    }

    @Override
    public boolean pathIsEditableField(final String nodePath) {
	final String nodePathAsParent = nodePath + ".";
	String[] pathTokens = nodePath.split("\\.");
	if (ArbilComponentBuilder.pathIsAttribute(pathTokens)) {
	    return pathIsEditableAttribute(pathTokens)
		    && !pathIsEditableField(nodePath.replaceAll("\\.[^.]*$", "")); // If parent is editable field then
	} else {
	    for (String[] pathString : childNodePaths) {
		if (!ArbilComponentBuilder.pathIsAttribute(pathString[0]) // fields can have attributes, so ignore these
			&& (pathString[0].startsWith(nodePathAsParent) || pathString[0].equals(nodePath))) {
		    return false;
		}
	    }
	    for (String[] pathString : templatesArray) { // some profiles do not have sub nodes hence this needs to be checked also
		if (!ArbilComponentBuilder.pathIsAttribute(pathString[0]) // fields can have attributes, so ignore these
			&& (pathString[0].startsWith(nodePathAsParent) && !pathString[0].equals(nodePath))) {
		    return false;
		}
	    }
	    return true;
	}
    }

    public static boolean pathIsEditableAttribute(String path) {
	// Could do some regex matching here, would be more efficient...
	return pathIsEditableAttribute(path.split("\\."));
    }

    /**
     *
     * @param pathTokens Path tokens, assuming that pathIsAttribute(pathTokens)
     * @return Whether this is an editable attribute
     */
    public static boolean pathIsEditableAttribute(String[] pathTokens) {
	if (pathTokens.length <= 3) {
	    // Root level attributes are not editable. E.g. {"","CMD","@CMDVersion"}
	    return false;
	}

	return !RESERVED_ATTRIBUTES.contains(pathTokens[pathTokens.length - 1].substring(1)); // remove @
    }

    public boolean pathAllowsLanguageId(String path) {
	return allowsLanguageIdPathList.contains(path);
    }

    public static void main(String args[]) {
	final ArbilDesktopInjector injector = new ArbilDesktopInjector();
	injector.injectHandlers();
	CmdiTemplate template = new CmdiTemplate(new ArbilSessionStorage());
	template.loadTemplate("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1289827960126/xsd");
    }
    /**
     * Compares for display preference. Paths of equal length get grouped together. Within those groups, ordering is on basis of
     * displayPriority
     */
    private static Comparator<String[]> displayNamePreferenceComparator = new Comparator<String[]>() {
	public int compare(String[] o1, String[] o2) {
	    int depthComp = getPathLength(o1[0]) - getPathLength(o2[0]);
	    if (depthComp == 0) {
		// Equal path length, compare using displayPriority
		return Integer.valueOf(o1[1]) - Integer.valueOf(o2[1]);
	    } else {
		// Unequal path lengths, keep apart
		return depthComp;
	    }
	}

	private int getPathLength(String str) {
	    int count = 0;
	    for (char c : str.toCharArray()) {
		if (c == '.') {
		    count++;
		}
	    }
	    return count;
	}
    };
}
