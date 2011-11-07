package nl.mpi.arbil.data;

import java.io.Serializable;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nl.mpi.arbil.data.metadatafile.MetadataReader;
import nl.mpi.arbil.userstorage.SessionStorage;

/**
 * Document   : ArbilField
 * Created on : Wed Dec 03 13:29:30 CET 2008
 * @author Peter.Withers@mpi.nl
 */
public class ArbilField implements Serializable {

    private transient ArbilDataNode parentDataNode;
    private URI parentDataNodeURI;
    public String xmlPath;
    private String translatedPath = null;
    private String fieldValue = "";
    public String originalFieldValue = fieldValue;
    private String cvUrlString;
    private int fieldOrder = -1;
    private ArbilVocabulary fieldVocabulary = null;
    private boolean hasVocabularyType = false;
    private boolean vocabularyIsOpen;
    private boolean vocabularyIsList;
    private boolean attributeField;
    private String keyName = null;
    private String originalKeyName = null;
    private String languageId = null;
    private String originalLanguageId = null;
    private int isRequiredField = -1;
    private int canValidateField = -1;
    private int siblingCount;
    private static SessionStorage sessionStorage;
    private List<String[]> attributePaths;
    private Map<String, Object> attributeValuesMap;
    private Map<String, Object> originalAttributeValuesMap;

    public static void setSessionStorage(SessionStorage sessionStorageInstance) {
	sessionStorage = sessionStorageInstance;
    }
    private static DataNodeLoader dataNodeLoader;

    public static void setDataNodeLoader(DataNodeLoader dataNodeLoaderInstance) {
	dataNodeLoader = dataNodeLoaderInstance;
    }

    /**
     * Creates arbil field with no field attributes from the schema
     * @param fieldOrderLocal
     * @param localParentDataNode
     * @param tempPath
     * @param tempValue
     * @param tempSiblingCount 
     */
    public ArbilField(int fieldOrderLocal, ArbilDataNode localParentDataNode, String tempPath, String tempValue, int tempSiblingCount) {
	this(fieldOrderLocal, localParentDataNode, tempPath, tempValue, tempSiblingCount, null, null);
    }

    /**
     * 
     * @param fieldOrderLocal
     * @param localParentDataNode
     * @param tempPath
     * @param tempValue
     * @param tempSiblingCount
     * @param attributePaths Paths of attribute fields allowed by the schema
     * @param attributeValuesMap Values of field attributes
     */
    public ArbilField(int fieldOrderLocal, ArbilDataNode localParentDataNode, String tempPath, String tempValue, int tempSiblingCount, List<String[]> attributePaths, Map<String, Object> attributeValuesMap) {
	fieldOrder = fieldOrderLocal;
	setParentDataNode(localParentDataNode);
	fieldValue = tempValue;
	originalFieldValue = fieldValue;
	xmlPath = tempPath;
	siblingCount = tempSiblingCount;

	// Is field an attribute field?
	attributeField = tempPath.matches("^.*\\.@[^.]*$"); // last section should start with .@

	// Set field attributes paths and values
	this.attributePaths = attributePaths;
	if (attributeValuesMap != null) {
	    this.attributeValuesMap = new HashMap<String, Object>(attributeValuesMap);
	    originalAttributeValuesMap = Collections.unmodifiableMap(new HashMap<String, Object>(attributeValuesMap));
	}
    }

//private String originalValue = null;
//    public boolean fieldNeedsSaveToDisk() {
//        if (originalValue == null) {
//            return false;
//        } else {
//            return fieldValue.compareTo(originalValue) != 0;
//        }
//    }
    public boolean isRequiredField() {
	if (isRequiredField < 0) {
	    isRequiredField = 0;
	    String fullXmlPath = getGenericFullXmlPath();
	    for (String currentRequiredField : getParentDataNode().getNodeTemplate().requiredFields) {
		if (fullXmlPath.matches(currentRequiredField)) {
		    isRequiredField = 1;
		    break;
		}
	    }
	}
	return isRequiredField == 1;
    }

    public boolean fieldValueValidates() {
	return fieldValueValidatesToTemplate() && fieldValueValidatesToVocabulary();
    }

    private boolean fieldValueValidatesToVocabulary() {
//      if this has a closed vocabulary then check that the current value matches one of the values in the vocabulary
	if (hasVocabulary() && !isVocabularyOpen()) {
	    return null != getVocabulary().findVocabularyItem(fieldValue);
	} else {
	    return true;
	}
    }

    private boolean fieldValueValidatesToTemplate() {
	boolean isValidValue = true;
	if (canValidateField != 0) { // only do this the first time or once a field constraint has been found
	    canValidateField = 0;
	    String fullXmlPath = getGenericFullXmlPath();
	    for (String[] currentRequiredField : getParentDataNode().getNodeTemplate().fieldConstraints) {
		if (fullXmlPath.matches(currentRequiredField[0])) {
		    canValidateField = 1;
		    isValidValue = (fieldValue.matches(currentRequiredField[1]));
		    break;
		}
	    }
	}
	return isValidValue;
    }

    private static boolean valuesDiffer(Object leftString, Object rightString) {
	if (leftString == null) {
	    return rightString != null;
	}
	return (!leftString.equals(rightString));
    }

    private static boolean valuesDiffer(final Map<String, Object> originalMap, final Map<String, Object> currentMap) {
	if (originalMap == null) {
	    return currentMap != null;
	} else {
	    // Original map is set
	    if (currentMap == null) {
		return true;
	    }
	    // Both maps are set, see if they are equal
	    if (originalMap.size() != currentMap.size()) {
		return true;
	    } else {
		// We now know both key sets are of the same size. Check for each key in the original set if it exists and is equal in the 
		// current set. If they all are, there are now differences
		for (Map.Entry<String, Object> originalEntry : originalMap.entrySet()) {
		    final Object currentValue = currentMap.get(originalEntry.getKey());
		    if (currentValue == null) {
			return originalEntry.getValue() != null;
		    } else {
			return !currentValue.equals(originalEntry.getValue());
		    }
		}
	    }
	}
	return false;
    }

    public boolean fieldNeedsSaveToDisk() {
	if (valuesDiffer(originalFieldValue, fieldValue)) {
	    return true;
	}
	if (valuesDiffer(originalLanguageId, languageId)) {
	    return true;
	}
	if (valuesDiffer(originalKeyName, keyName)) {
	    return true;
	}
	if (valuesDiffer(originalAttributeValuesMap, attributeValuesMap)) {
	    return true;
	}
	return false;
    }

    public String getFieldValue() {
//        return getFullXmlPath();
	return fieldValue;
    }

    /**
     * 
     * @return The value of the field as it should be represented in the XML (e.g. item code rather than display name)
     */
    public String getFieldValueForXml() {
	if (hasVocabulary()) {
	    ArbilVocabularyItem vocabularyItem = getVocabulary().findVocabularyItem(getFieldValue());
	    if (vocabularyItem != null) {
		return vocabularyItem.getValue();
	    }
	}
	return getFieldValue();
    }

    // returns the full xml path with the path indexes replaced by x's
    public String getGenericFullXmlPath() {
	return getFullXmlPath().replaceAll("\\(\\d*?\\)", "(x)").replaceFirst("\\(x\\)$", "");
    }

    public String getFullXmlPath() {
	String returnValue;
	String[] pathStringArray = this.getParentDataNode().getUrlString().split("#");
	if (pathStringArray.length > 1) {
	    returnValue = pathStringArray[1] + this.xmlPath;
	} else {
	    returnValue = this.xmlPath;
	}
	if (siblingCount > 0) {
	    returnValue = returnValue + "(" + (siblingCount + 1) + ")";
	}
	return returnValue;
    }

    public void setFieldValue(String fieldValueToBe, boolean updateUI, boolean excludeFromUndoHistory) {
	// todo: put this in to a syncronised lock so that it cannot change the value while the node is being modified
	// todo: consider the case of the node reloading with a different xpath then the lock allowing the edit, so it would be better to prevent the starting of the edit in the first place
	fieldValueToBe = fieldValueToBe.trim();
	if (!this.fieldValue.equals(fieldValueToBe)) {
	    if (!excludeFromUndoHistory) {
		ArbilJournal.getSingleInstance().recordFieldChange(this, this.fieldValue, fieldValueToBe, ArbilJournal.UndoType.Value);
	    }
	    ArbilJournal.getSingleInstance().saveJournalEntry(this.getParentDataNode().getUrlString(), getFullXmlPath(), this.fieldValue, fieldValueToBe, "edit");
	    this.fieldValue = fieldValueToBe;
	    new FieldChangeTriggers().actOnChange(this);
	    // this now scans all fields in the imdiparent and its child nodes to set the "needs save to disk" flag in the imdi nodes
	    getParentDataNode().setDataNodeNeedsSaveToDisk(this, updateUI);
	}
    }

    public boolean hasVocabulary() {
	return (fieldVocabulary != null);
    }

    public String getLanguageId() {
	return languageId;
    }

    public void setLanguageId(String languageIdLocal, boolean updateUI, boolean excludeFromUndoHistory) {
	// todo: put this in to a syncronised lock so that it cannot change the value while the node is being modified
	// todo: consider the case of the node reloading with a different xpath then the lock allowing the edit, so it would be better to prevent the starting of the edit in the first place
	String oldLanguageId = getLanguageId();
	boolean valueChanged = false;
	// this is expanded for readability
	if (languageIdLocal == null) {
	    valueChanged = (oldLanguageId != null);
	} else if (!languageIdLocal.equals(oldLanguageId)) {
	    valueChanged = true;
	}
	if (valueChanged) {// if the value has changed then record it in the undo list and the journal
	    if (!excludeFromUndoHistory) {
		ArbilJournal.getSingleInstance().recordFieldChange(this, this.getLanguageId(), languageIdLocal, ArbilJournal.UndoType.LanguageId);
	    }
	    ArbilJournal.getSingleInstance().saveJournalEntry(this.getParentDataNode().getUrlString(), getFullXmlPath() + ":LanguageId", oldLanguageId, languageIdLocal, "edit");
	    //addFieldAttribute("LanguageId", languageIdLocal);
	    languageId = languageIdLocal;
//            fieldLanguageId = languageId;
	    getParentDataNode().setDataNodeNeedsSaveToDisk(this, updateUI);
	}

    }

    public ArbilVocabulary getVocabulary() {
	return fieldVocabulary;
    }

    public boolean isAttributeField() {
	return attributeField;
    }

    public ArbilField[] getSiblingField(String pathString) {
//        System.out.println("getSiblingField: " + pathString);
	for (ArbilField[] tempField : getParentDataNode().getFields().values().toArray(new ArbilField[][]{})) {
//            System.out.println("tempField[0].getFullXmlPath(): " + tempField[0].getFullXmlPath());
	    if (tempField[0].getFullXmlPath().equals(pathString) || tempField[0].getGenericFullXmlPath().equals(pathString)) {
		return tempField;
	    }
	}
	return null;
    }

    public boolean isDisplayable() {
	return (fieldValue != null && /*fieldValue.trim().length() > 0 && */ !xmlPath.contains("CorpusLink") && !xmlPath.endsWith(".Keys") && !xmlPath.endsWith(".History"));
    }

    public void finishLoading() {
    }

    public void revertChanges() {
	setFieldValue(originalFieldValue, false, false);
	setLanguageId(originalLanguageId, false, false);
	setKeyName(originalKeyName, false, false);
	boolean updateUI = true;
	getParentDataNode().setDataNodeNeedsSaveToDisk(this, updateUI);
    }

    public void setFieldAttribute(String cvType, String cvUrlString, String languageIdLocal, String keyNameLocal) {
	// todo: put this in to a syncronised lock so that it cannot change the value while the node is being modified
	// todo: consider the case of the node reloading with a different xpath then the lock allowing the edit, so it would be better to prevent the starting of the edit in the first place
	languageId = languageIdLocal;
	originalLanguageId = languageId;
	keyName = keyNameLocal;
	originalKeyName = keyName;
	this.cvUrlString = cvUrlString;
	// set for the vocabulary type
	hasVocabularyType = false;
	if (cvType != null) {
	    if (cvType.equals("OpenVocabularyList")) {
		vocabularyIsList = true;
		vocabularyIsOpen = true;
		hasVocabularyType = true;
	    } else if (cvType.equals("OpenVocabulary")) {
		vocabularyIsList = false;
		vocabularyIsOpen = true;
		hasVocabularyType = true;
	    } else if (cvType.equals("ClosedVocabularyList")) {
		vocabularyIsList = true;
		vocabularyIsOpen = false;
		hasVocabularyType = true;
	    } else if (cvType.equals("ClosedVocabulary")) {
		vocabularyIsList = false;
		vocabularyIsOpen = false;
		hasVocabularyType = true;
	    }
	}
	loadVocabulary();
    }

    /**
     * Gets paths of editable field attributes
     * @return List of template paths. Template path is string array with [path,description,...] 
     * @see nl.mpi.arbil.templates.ArbilTemplate
     */
    public synchronized List<String[]> getAttributePaths() {
	return Collections.unmodifiableList(attributePaths);
    }

    /**
     * 
     * @return Whether there schema support editable attributes on this field
     */
    public synchronized boolean hasEditableFieldAttributes() {
	return attributePaths != null && attributePaths.size() > 0;
    }

    public synchronized Map<String, Object> getAttributeValuesMap() {
	if (attributeValuesMap == null) {
	    return null;
	} else {
	    return Collections.unmodifiableMap(attributeValuesMap);
	}
    }

    /**
     * Gets the value of an editable field attribute
     * @param attributePath Path to get value for
     * @return Value for path (null if not set)
     */
    public synchronized Object getAttributeValue(String attributePath) {
	if (attributeValuesMap != null) {
	    return attributeValuesMap.get(attributePath);
	}
	return null;
    }

    /**
     * Sets the value for an editable field attribute
     * @param attributePath Path to set value on
     * @param value Null to unset
     */
    public synchronized void setAttributeValue(String attributePath, Object value, boolean updateUI) {
	if (valuesDiffer(value, getAttributeValue(attributePath))) {
	    if (attributeValuesMap == null) {
		attributeValuesMap = new HashMap<String, Object>();
	    }

	    if (value == null) {
		attributeValuesMap.remove(attributePath);
	    } else {
		attributeValuesMap.put(attributePath, value);
	    }
	    getParentDataNode().setDataNodeNeedsSaveToDisk(this, updateUI);
	}
    }

    public void loadVocabulary() {
	if (hasVocabularyType) {
	    if (cvUrlString != null && cvUrlString.length() > 0) {
		fieldVocabulary = ArbilVocabularies.getSingleInstance().getVocabulary(this, cvUrlString);
		if (cvUrlString.equals(DocumentationLanguages.getLanguageVocabularyUrl())) {
		    fieldVocabulary.setFilter(DocumentationLanguages.getSingleInstance());
		}
	    }
	} else {
	    // vocabularies specified in the xml override vocabularies defined in the schema
	    if (getParentDataNode().getParentDomNode().nodeTemplate != null) {
		// get the schema vocabularies
		String strippedXmlPath = this.getGenericFullXmlPath().replaceAll("\\(x\\)", "");
//                System.out.println("parentImdi.getParentDomNode().nodeTemplate: " + parentImdi.getParentDomNode().nodeTemplate.loadedTemplateName);
//                System.out.println("strippedXmlPath: " + strippedXmlPath);
		fieldVocabulary = getParentDataNode().getParentDomNode().nodeTemplate.getFieldVocabulary(strippedXmlPath);
	    }
	}
	if (fieldVocabulary != null) {
	    // Vocabulary loaded, check if field value is a code - if so, set to display name. Conversion back to code on
	    // saving is done in getFieldValueForXml()
	    ArbilVocabularyItem vocabItem = fieldVocabulary.getVocabularyItemByCode(fieldValue);
	    if (vocabItem != null) {
		fieldValue = vocabItem.getDisplayValue();
	    } // otherwise item is not known as a code, use value as presented
	}
    }

    @Override
    public String toString() {
//            System.out.println("ImdiField: " + fieldValue);
//            if (!isDisplayable()) {
//                return "check attributes";// fieldAttributes.keys().toString();
//            }
	return getFieldValue();
    }

    public int getFieldOrder() {
	return fieldOrder;
    }

    public String getKeyName() {
	return keyName;
    }

    /**
     *
     * @param keyNameLocal
     * @param updateUI
     * @param excludeFromUndoHistory
     * @return Key name has actually been changed
     */
    public boolean setKeyName(String keyNameLocal, boolean updateUI, boolean excludeFromUndoHistory) {
	// todo: put this in to a syncronised lock so that it cannot change the value while the node is being modified
	// todo: consider the case of the node reloading with a different xpath then the lock allowing the edit, so it would be better to prevent the starting of the edit in the first place
	System.out.println("setKeyName: " + keyNameLocal);
	String lastValue = getKeyName();
	System.out.println("lastValue: " + lastValue);
	if (lastValue != null) {
	    if (!lastValue.equals(keyNameLocal)) { // only if the value is different
//                if (fieldAttributes.contains("Name")) { // only if there is already a key name
		if (!excludeFromUndoHistory) {
		    ArbilJournal.getSingleInstance().recordFieldChange(this, this.getKeyName(), keyNameLocal, ArbilJournal.UndoType.KeyName);
		}
		// TODO: resolve how to log key name changes
		ArbilJournal.getSingleInstance().saveJournalEntry(this.getParentDataNode().getUrlString(), getFullXmlPath(), lastValue, keyNameLocal, "editkeyname");
		keyName = keyNameLocal;
		translatedPath = null;
		getTranslateFieldName();
		getParentDataNode().setDataNodeNeedsSaveToDisk(this, updateUI);
		if (getParentDataNode().getNeedsSaveToDisk(false)) {
		    getParentDataNode().saveChangesToCache(true);
		}
		getParentDataNode().reloadNode();
		return true;
	    }
	}
	return false;
    }

    public String getTranslateFieldName() {
	if (translatedPath == null) {
	    String fieldName = xmlPath;
	    // TODO: move this to the imdischema class
	    // replace the xml paths with user friendly node names
//            fieldName = fieldName.replace(MetadataReader.imdiPathSeparator + "METATRANSCRIPT" + MetadataReader.imdiPathSeparator + "Session" + MetadataReader.imdiPathSeparator + "Resources" + MetadataReader.imdiPathSeparator + "WrittenResource", "WrittenResource");
//            fieldName = fieldName.replace(MetadataReader.imdiPathSeparator + "METATRANSCRIPT" + MetadataReader.imdiPathSeparator + "Session" + MetadataReader.imdiPathSeparator + "MDGroup" + MetadataReader.imdiPathSeparator + "Actors" + MetadataReader.imdiPathSeparator + "Actor", "Actors");
//            fieldName = fieldName.replace(MetadataReader.imdiPathSeparator + "METATRANSCRIPT" + MetadataReader.imdiPathSeparator + "Session" + MetadataReader.imdiPathSeparator + "Resources" + MetadataReader.imdiPathSeparator + "Anonyms", "Anonyms");
//            fieldName = fieldName.replace(MetadataReader.imdiPathSeparator + "METATRANSCRIPT" + MetadataReader.imdiPathSeparator + "Session" + MetadataReader.imdiPathSeparator + "Resources" + MetadataReader.imdiPathSeparator + "MediaFile", "MediaFiles");
//            fieldName = fieldName.replace(MetadataReader.imdiPathSeparator + "METATRANSCRIPT" + MetadataReader.imdiPathSeparator + "Session" + MetadataReader.imdiPathSeparator + "MDGroup", "");
	    fieldName = fieldName.replace(MetadataReader.imdiPathSeparator + "METATRANSCRIPT" + MetadataReader.imdiPathSeparator + "Session" + MetadataReader.imdiPathSeparator + "MDGroup", "");
	    fieldName = fieldName.replace(MetadataReader.imdiPathSeparator + "METATRANSCRIPT" + MetadataReader.imdiPathSeparator + "Session", "");
	    fieldName = fieldName.replace(MetadataReader.imdiPathSeparator + "METATRANSCRIPT" + MetadataReader.imdiPathSeparator + "Corpus", "");
	    fieldName = fieldName.replace(MetadataReader.imdiPathSeparator + "METATRANSCRIPT" + MetadataReader.imdiPathSeparator + "Catalogue", "");

	    // handle the clarin path names
	    fieldName = fieldName.replaceFirst("^\\.CMD\\.Components\\.[^\\.]+\\.", "");

//                    if (attributeName.equals("Name")) {
	    if (fieldName.endsWith("Keys.Key")) {
//                System.out.println("Found key for: " + xmlPath);
		//String keyValue = getFieldAttribute("Name");
		if (keyName != null) {
//                    System.out.println("Key value valid: " + keyValue.toString());
		    fieldName = fieldName + MetadataReader.imdiPathSeparator + keyName;
		}
//                xmlPath = xmlPath + MetadataReader.imdiPathSeparator + attributeValue;

	    }
	    if (fieldName.startsWith(".")) {
		fieldName = fieldName.substring(1);
	    }
	    if (sessionStorage.isUseLanguageIdInColumnName()) {
		// add the language id to the column name if available
		if (getLanguageId() != null && getLanguageId().length() > 0) {
		    fieldName = fieldName + " [" + getLanguageId() + "]";
		}
	    }
	    translatedPath = fieldName;
	}
//        System.out.println("xmlPath: " + xmlPath);
//        System.out.println("translatedPath: " + translatedPath);
	return translatedPath;
    }

    /**
     * @return the vocabularyIsOpen
     */
    public boolean isVocabularyOpen() {
	return vocabularyIsOpen;
    }

    /**
     * @return the vocabularyIsList
     */
    public boolean isVocabularyList() {
	return vocabularyIsList;
    }

    /**
     * @return the parentDataNode
     */
    public synchronized ArbilDataNode getParentDataNode() {
	if (parentDataNode == null && parentDataNodeURI != null) {
	    parentDataNode = dataNodeLoader.getArbilDataNode(null, parentDataNodeURI);
	}
	return parentDataNode;
    }

    /**
     * @param parentDataNode the parentDataNode to set
     */
    public final synchronized void setParentDataNode(ArbilDataNode parentDataNode) {
	this.parentDataNode = parentDataNode;
	this.parentDataNodeURI = parentDataNode != null ? parentDataNode.getURI() : null;
    }
}
