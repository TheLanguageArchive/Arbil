package nl.mpi.arbil.clarin.profiles;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import nl.mpi.arbil.ArbilDesktopInjector;
import nl.mpi.arbil.data.ArbilEntityResolver;
import nl.mpi.arbil.data.ArbilVocabularies;
import nl.mpi.arbil.clarin.profiles.CmdiProfileReader.CmdiProfile;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilVocabulary;
import nl.mpi.arbil.templates.ArbilTemplate;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcher;
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
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * CmdiTemplate.java
 * Created on March 10, 2010, 17:34:45 AM
 * @author Peter.Withers@mpi.nl
 */
public class CmdiTemplate extends ArbilTemplate {

    public static final String RESOURCE_REFERENCE_ATTRIBUTE = "ref";
    private static MessageDialogHandler messageDialogHandler;

    public static void setMessageDialogHandler(MessageDialogHandler handler) {
	messageDialogHandler = handler;
    }
    private static BugCatcher bugCatcher;

    public static void setBugCatcher(BugCatcher bugCatcherInstance) {
	bugCatcher = bugCatcherInstance;
    }
    private static SessionStorage sessionStorage;

    public static void setSessionStorage(SessionStorage sessionStorageInstance) {
	sessionStorage = sessionStorageInstance;
    }
    private String nameSpaceString;
    private String filterString[] = {".CMD.Resources.", ".CMD.Header."};
    private File schemaFile = null;
    private Document schemaDocument;

    private static class ArrayListGroup {

	public ArrayList<String[]> childNodePathsList = new ArrayList<String[]>();
	public ArrayList<String[]> addableComponentPathsList = new ArrayList<String[]>();
	public ArrayList<String[]> resourceNodePathsList = new ArrayList<String[]>();
	public ArrayList<String[]> fieldConstraintList = new ArrayList<String[]>();
	public ArrayList<String[]> displayNamePreferenceList = new ArrayList<String[]>();
	public ArrayList<String[]> fieldUsageDescriptionList = new ArrayList<String[]>();
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
	    makeGuiNamesUnique();

	    // sort and construct the preferredNameFields array
	    String[][] tempSortableArray = arrayListGroup.displayNamePreferenceList.toArray(new String[][]{});
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
	} catch (URISyntaxException urise) {
	    bugCatcher.logError(urise);
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
//                    System.out.println("currentTemplateGuiName: " + currentTemplateGuiName);
//                    System.out.println("secondTemplateGuiName: " + secondTemplateGuiName);
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
//                                    System.out.println("templateToChangeGuiName: " + templateToChangeGuiName);
//                                    System.out.println("templateToChangePath: " + templateToChangePath);
//                                    System.out.println("new templateToChange[1]: " + templateToChange[1]);
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

    @Override
    public Enumeration listTypesFor(Object targetNodeUserObject) {
	// get the xpath of the target node
	String targetNodeXpath = ((ArbilDataNode) targetNodeUserObject).getURI().getFragment();
	System.out.println("targetNodeXpath: " + targetNodeXpath);
	boolean isComponentPath = false;
	if (targetNodeXpath != null) {
	    isComponentPath = targetNodeXpath.endsWith(")");
	    // remove the extraneous node name for a meta node
//            targetNodeXpath = targetNodeXpath.replaceAll("\\.[^\\.]+[^\\)]$", "");
	    // remove the sibling indexes
	    targetNodeXpath = targetNodeXpath.replaceAll("\\(\\d+\\)", "");
	}
	System.out.println("targetNodeXpath: " + targetNodeXpath);
	Vector<String[]> childTypes = new Vector<String[]>();
	if (targetNodeUserObject instanceof ArbilDataNode) {
	    for (String[] childPathString : templatesArray) {
//                System.out.println("Testing: " + childPathString[1] + childPathString[0]);
//                System.out.println(childPathString[0] + " : " + targetNodeXpath);
		boolean allowEntry = false;
		if (targetNodeXpath == null) {
//                    System.out.println("allowing due to null path: " + childPathString[0]);
		    allowEntry = true;
		} else if (childPathString[0].startsWith(targetNodeXpath)) {
//                    System.out.println("allowing: " + childPathString[0]);
		    allowEntry = true;
		}
		if (childPathString[0].equals(targetNodeXpath) && isComponentPath) {
//                    System.out.println("disallowing addint to itself: " + childPathString[0]);
		    allowEntry = false;
		}
		for (String currentFilter : filterString) {
		    if (childPathString[0].startsWith(currentFilter)) {
			allowEntry = false;
		    }
		}
		if (allowEntry) {
//                    System.out.println("allowing: " + childPathString[0]);
		    childTypes.add(new String[]{childPathString[1], childPathString[0]});
		}
	    }
	    String[][] childTypesArray = childTypes.toArray(new String[][]{});
	    childTypes.removeAllElements();
	    for (String[] currentChildType : childTypesArray) {
		// filter out sub nodes that cannot be added at the current level because they require an intermediate node to be added, ie "actors language" requires an "actor"
		boolean keepChildType = true;
//                System.out.println("currentChildType: " + currentChildType[1]);
		for (String[] subChildType : childTypesArray) {
//                    System.out.println("subChildType: " + subChildType[1]);
		    if (currentChildType[1].startsWith(subChildType[1])) {
			String remainderString = currentChildType[1].substring(subChildType[1].length());
			//if (currentChildType[1].length() != subChildType[1].length()) {
			if (remainderString.contains(".")) {
			    keepChildType = false;
//                            System.out.println("remainder of path: " + remainderString);
//                            System.out.println("removing: " + currentChildType[1]);
//                            System.out.println("based on: " + subChildType[1]);
			}
		    }
		}
		if (keepChildType) {
//                    System.out.println("keeping: : " + currentChildType[1]);
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
	if (xsdFile.getScheme().equals("file")) {
	    schemaFile = new File(xsdFile);
	} else {
	    schemaFile = sessionStorage.updateCache(xsdFile.toString(), 100);
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
//            System.out.println("XmlObject.Factory:" + XmlObject.Factory.class.toString());
	    SchemaType schemaType = sts.documentTypes()[0];
	    constructXml(schemaType, arrayListGroup, "");
//            for (SchemaType schemaType : sts.documentTypes()) {
////                System.out.println("T-documentTypes:");
//                constructXml(schemaType, arrayListGroup, "", "");
//                break; // there can only be a single root node and the IMDI schema specifies two (METATRANSCRIPT and VocabularyDef) so we must stop before that error creates another
//            }
	} catch (IOException e) {
	    bugCatcher.logError(templateFile.getName(), e);
	    messageDialogHandler.addMessageDialogToQueue("Could not open the required template file: " + templateFile.getName(), "Load Clarin Template");
	} catch (XmlException e) {
	    bugCatcher.logError(templateFile.getName(), e);
	    messageDialogHandler.addMessageDialogToQueue("Could not read the required template file: " + templateFile.getName(), "Load Clarin Template");
	}
    }

    private int constructXml(final SchemaType schemaType, ArrayListGroup arrayListGroup, final String pathString) {
	int childCount = 0;
//        boolean hasMultipleElementsInOneNode = false;
	int subNodeCount = 0;
	readControlledVocabularies(schemaType, pathString);
	readFieldConstrains(schemaType, pathString, arrayListGroup.fieldConstraintList);

	// search for annotations
	SchemaParticle topParticle = schemaType.getContentModel();
	searchForAnnotations(topParticle, pathString, arrayListGroup);
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

		// while keeping the .cmd.components part filter out all unrequired path component for use in the menus
//            if (currentHasMultipleNodes || filterString.startsWith(currentPathString)) {
//                currentNodeMenuName = nodeMenuName + "." + localName;
//            } else {
//                currentNodeMenuName = nodeMenuName;
//            }
//                  currentNodeMenuName = localName;
//            currentNodeMenuName = currentNodeMenuName.replaceFirst("^\\.CMD\\.Components\\.[^\\.]+\\.", "");
		int maxOccurs;
		boolean canHaveMultiple = true;
		if (schemaProperty.getMaxOccurs() == null) {
		    // absence of the max occurs also means multiple
		    maxOccurs = -1;
		    canHaveMultiple = true;
		    // todo: also check that min and max are the same because there may be cases of zero required but only one can be added
		} else if (schemaProperty.getMaxOccurs().toString().equals("unbounded")) {
		    maxOccurs = -1;
		    canHaveMultiple = true;
		} else {
		    // store the max occurs for use in the add menu etc
		    maxOccurs = schemaProperty.getMaxOccurs().intValue();
		    canHaveMultiple = schemaProperty.getMaxOccurs().intValue() > 1;
		}
		if (!canHaveMultiple) {
		    // todo: limit the number of instances that can be added to a xml file basedon the max bounds
		    canHaveMultiple = schemaProperty.getMinOccurs().intValue() != schemaProperty.getMaxOccurs().intValue();
		}
//            boolean hasSubNodes = false;
		// start: temp code for extracting all field names
//                System.out.println("Found template element: " + currentPathString);
//                boolean foundEntry = false;
//                for (String[] currentEntry : arrayListGroup.fieldUsageDescriptionList) {
//                    if (currentPathString.startsWith(currentEntry[0])) {
//                        currentEntry[0] = currentPathString;
//                        foundEntry = true;
//                        break;
//                    }
//                }
//                if (!foundEntry) {
//                    arrayListGroup.fieldUsageDescriptionList.add(new String[]{currentPathString, ""});
//                }
		// end: temp code for extracting all field names
		SchemaType currentSchemaType = schemaProperty.getType();
//            String nodeMenuNameForChild;
//            if (canHaveMultiple) {
//                // reset the node menu name when traversing through into a subnode
//                nodeMenuNameForChild = localName;
////                nodeMenuName = nodeMenuName + "." + localName;
//            } else {
//                nodeMenuName = nodeMenuName + "." + localName;
//                nodeMenuNameForChild = nodeMenuName;
//            }
//            nodeMenuNameForChild = "";
		currentNodeMenuName = localName;
		// boolean childHasMultipleElementsInOneNode =
		subNodeCount = constructXml(currentSchemaType, arrayListGroup, currentPathString);
//            if (!hasMultipleElementsInOneNode) {
//                hasMultipleElementsInOneNode = childHasMultipleElementsInOneNode;
//            }

//            System.out.println("childNodeChildCount: " + childCount + " : " + hasMultipleElementsInOneNode + " : " + currentPathString);

//            nodeMenuNameForChild = nodeMenuNameForChild.replaceFirst("^\\.CMD\\.Components\\.[^\\.]+\\.", "");
//            boolean hasMultipleSubNodes = childCount < childNodeChildCount - 1; // todo: complete or remove this hasSubNodes case
		if (canHaveMultiple && subNodeCount > 0) {
//                todo check for case of one or only single sub element and when found do not add as a child path
		    arrayListGroup.childNodePathsList.add(new String[]{currentPathString, pathString.substring(pathString.lastIndexOf(".") + 1)});
		}// else if (canHaveMultiple) {
//                    System.out.println("Skipping sub node path: " + currentPathString + " : " + currentNodeMenuName);
//                }
		if (canHaveMultiple) {
		    String insertBefore = "";
		    arrayListGroup.addableComponentPathsList.add(new String[]{currentPathString, currentNodeMenuName, insertBefore, Integer.toString(maxOccurs)});
		}
		boolean hasResourceAttribute = false;
		for (SchemaProperty attributesProperty : currentSchemaType.getAttributeProperties()) {
		    if (attributesProperty.getName().getLocalPart().equals("ref")) {
			hasResourceAttribute = true;
			break;
		    }
		}
		if (hasResourceAttribute) {
		    arrayListGroup.resourceNodePathsList.add(new String[]{currentPathString, localName});
		}
	    }
	}
//        if (childCount > 1) {
//            hasMultipleElementsInOneNode = true;
//        }
	subNodeCount = subNodeCount + currentNodeChildCount;
	return subNodeCount;
    }

//    SchemaParticle topParticle = schemaType.getContentModel();
    private void searchForAnnotations(SchemaParticle schemaParticle, String nodePathBase, ArrayListGroup arrayListGroup) {
//        System.out.println("searchForAnnotations" + nodePath);
	if (schemaParticle != null) {
	    switch (schemaParticle.getParticleType()) {
		case SchemaParticle.SEQUENCE:
		    for (SchemaParticle schemaParticleChild : schemaParticle.getParticleChildren()) {
			String nodePath;
			if (schemaParticleChild.getName() != null) {
			    nodePath = nodePathBase + "." + schemaParticleChild.getName().getLocalPart();
			} else {
			    nodePath = nodePathBase + ".unnamed";
			    bugCatcher.logError(new Exception("unnamed node at: " + nodePath));
			}
			searchForAnnotations(schemaParticleChild, nodePath, arrayListGroup);
		    }
		    break;
		case SchemaParticle.ELEMENT:
		    SchemaLocalElement schemaLocalElement = (SchemaLocalElement) schemaParticle;
		    saveAnnotationData(schemaLocalElement, nodePathBase, arrayListGroup);
		    break;
	    }
	}
    }

    private void saveAnnotationData(SchemaLocalElement schemaLocalElement, String nodePath, ArrayListGroup arrayListGroup) {
	SchemaAnnotation schemaAnnotation = schemaLocalElement.getAnnotation();
	if (schemaAnnotation != null) {
	    for (SchemaAnnotation.Attribute annotationAttribute : schemaAnnotation.getAttributes()) {
		System.out.println("  Annotation: " + annotationAttribute.getName() + " : " + annotationAttribute.getValue());
		//Annotation: {ann}documentation : the title of the book
		//Annotation: {ann}displaypriority : 1
		// todo: the url here could be removed provided that it does not make it to unspecific
		if ("{http://www.clarin.eu}displaypriority".equals(annotationAttribute.getName().toString())) {
		    arrayListGroup.displayNamePreferenceList.add(new String[]{nodePath, annotationAttribute.getValue()});
		}
		if ("{http://www.clarin.eu}documentation".equals(annotationAttribute.getName().toString())) {
		    arrayListGroup.fieldUsageDescriptionList.add(new String[]{nodePath, annotationAttribute.getValue()});
		}
	    }
	}
    }

    private void readFieldConstrains(SchemaType schemaType, String nodePath, ArrayList<String[]> fieldConstraintList) {
	switch (schemaType.getBuiltinTypeCode()) {
	    case SchemaType.BTC_STRING:
//                System.out.println("BTC_STRING");
		// no constraint relevant for string
		break;
	    case SchemaType.BTC_DATE:
//                System.out.println("BTC_DATE");
		fieldConstraintList.add(new String[]{nodePath, "([0-9][0-9][0-9][0-9])((-[0-1][0-9])(-[0-3][0-9])?)?"});// todo: complete this regex
		break;
	    case SchemaType.BTC_BOOLEAN:
//                System.out.println("BTC_BOOLEAN");
		fieldConstraintList.add(new String[]{nodePath, "true|false"});// todo: complete this regex
		break;
	    case SchemaType.BTC_ANY_URI:
//                System.out.println("BTC_ANY_URI");
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
//                System.out.println("uknown");
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
		schemaDocument = documentBuilder.parse(schemaFile);
	    } catch (IOException ex) {
		bugCatcher.logError("Error while parsing schema", ex);
	    } catch (ParserConfigurationException ex) {
		bugCatcher.logError("Error while parsing schema", ex);
	    } catch (SAXException ex) {
		bugCatcher.logError("Error while parsing schema", ex);
	    }
	}
	return schemaDocument;
    }

    /**
     * 
     * Gets labels for vocabulary enumerations like:
     * 
     * <xs:enumeration	value="csu"
     *			dcr:datcat="http://cdb.iso.org/lg/CDB-00138674-001" 
     *			ann:label="Central Sudanic languages"/>
     * 
     * @param vocabularyName Name of the vocabulary in the profile schema
     * @return HashMap that has [value => label] mapping for the vocabulary
     * @throws TransformerException Thrown on node selection through XPathAPI if transformation fails
     */
    private HashMap<String, String> getDescriptionsForVocabulary(String vocabularyName) throws TransformerException {
	// Get document node (should not be null)
	Document schemaDoc = getSchemaDocument();
	if (schemaDoc == null) {
	    return null;
	} else {
	    // Find all enumeration values within the vocabulary
	    NodeList enumNodes = XPathAPI.selectNodeList(getSchemaDocument(), "//*[@name='" + vocabularyName + "']//xs:restriction/xs:enumeration");
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

    private void readControlledVocabularies(SchemaType schemaType, String nodePath) {
	XmlAnySimpleType[] enumerationValues = schemaType.getEnumerationValues();

	if (enumerationValues != null && enumerationValues.length > 0) {
	    ArbilVocabulary vocabulary = ArbilVocabularies.getSingleInstance().getEmptyVocabulary(nameSpaceString + "#" + schemaType.getName());

	    HashMap<String, String> descriptions = null;
	    try {
		// Get descriptions (ann:label attributes on vocabulary enumeration elements)
		descriptions = getDescriptionsForVocabulary(schemaType.getBaseType().getName().getLocalPart());
	    } catch (Exception ex) {
		// Fall back to using just the values, no descriptions
		bugCatcher.logError(ex);
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
	    System.out.println("vocabularyHashTable.put: " + nodePath);
	    vocabularyHashTable.put(nodePath, vocabulary);
	}
    }

    public static void main(String args[]) {
	ArbilDesktopInjector.injectHandlers();
	new CmdiTemplate().loadTemplate("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1271859438164/xsd");
//        new CmdiTemplate().loadTemplate("http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1272022528355/xsd");
//	new CmdiTemplate().loadTemplate("file:/Users/petwit/Desktop/LocalProfiles/clarin.eu_annotation-test_1272022528355.xsd");
    }
}
