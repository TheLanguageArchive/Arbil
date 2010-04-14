package nl.mpi.arbil;

import nl.mpi.arbil.data.ImdiTreeObject;
import nl.mpi.arbil.data.ImdiSchema;

/**
 * Document   : ImdiField
 * Created on : Wed Dec 03 13:29:30 CET 2008
 * @author Peter.Withers@mpi.nl
 */
public class ImdiField {

    public ImdiTreeObject parentImdi;
    public String xmlPath;
    private String translatedPath = null;
    public String fieldValue = "";
    public String originalFieldValue = fieldValue;
    @Deprecated
    public String fieldID;
    private ImdiVocabularies.Vocabulary fieldVocabulary = null;
    private boolean hasVocabularyType = false;
    public boolean vocabularyIsOpen;
    public boolean vocabularyIsList;
    private String keyName = null;
    private String originalKeyName = null;
    private String languageId = null;
    private String originalLanguageId = null;
    private int isRequiredField = -1;
    private int canValidateField = -1;

    public ImdiField(ImdiTreeObject localParentImdi, String tempPath, String tempValue) {
        parentImdi = localParentImdi;
        fieldValue = tempValue;
        originalFieldValue = fieldValue;
        xmlPath = tempPath;
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
            for (String currentRequiredField : parentImdi.currentTemplate.requiredFields) {
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
        if (hasVocabulary() && !vocabularyIsOpen) {
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
            for (String[] currentRequiredField : parentImdi.currentTemplate.fieldConstraints) {
                if (fullXmlPath.matches(currentRequiredField[0])) {
                    canValidateField = 1;
                    isValidValue = (fieldValue.matches(currentRequiredField[1]));
                    break;
                }
            }
        }
        return isValidValue;
    }

    private boolean valuesDiffer(String leftString, String rightString) {
        if (leftString == null) {
            return rightString != null;
        }
        return (!leftString.equals(rightString));
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
        return false;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    // returns the full xml path with the path indexes replaced by x's
    public String getGenericFullXmlPath() {
        return getFullXmlPath().replaceAll("\\(\\d*?\\)", "(x)");
    }

    public String getFullXmlPath() {
        String returnValue;
        String[] pathStringArray = this.parentImdi.getUrlString().split("#");
        if (pathStringArray.length > 1) {
            returnValue = pathStringArray[1] + this.xmlPath;
        } else {
            returnValue = this.xmlPath;
        }
        return returnValue;
    }

    public void setFieldValue(String fieldValueToBe, boolean updateUI, boolean excludeFromUndoHistory) {
        fieldValueToBe = fieldValueToBe.trim();
        if (!this.fieldValue.equals(fieldValueToBe)) {
            if (!excludeFromUndoHistory) {
                LinorgJournal.getSingleInstance().recordFieldChange(this, this.fieldValue, fieldValueToBe, LinorgJournal.UndoType.Value);
            }
            LinorgJournal.getSingleInstance().saveJournalEntry(this.parentImdi.getUrlString(), this.xmlPath, this.fieldValue, fieldValueToBe, "edit");
            this.fieldValue = fieldValueToBe;
            new FieldChangeTriggers().actOnChange(this);
            // this now scans all fields in the imdiparent and its child nodes to set the "needs save to disk" flag in the imdi nodes
            parentImdi.setImdiNeedsSaveToDisk(this, updateUI);
        }
    }

    public boolean hasVocabulary() {
        return (fieldVocabulary != null);
    }

    public String getLanguageId() {
        return languageId;
    }

    public void setLanguageId(String languageIdLocal, boolean updateUI, boolean excludeFromUndoHistory) {
        String oldLanguageId = getLanguageId();
        boolean valueChanged = false;
        // this is expanded for readability
        if (oldLanguageId == null && languageIdLocal == null) {
            valueChanged = false;
        } else if (languageIdLocal == null && oldLanguageId != null) {
            valueChanged = true;
        } else if (!languageIdLocal.equals(oldLanguageId)) {
            valueChanged = true;
        }
        if (valueChanged) {// if the value has changed then record it in the undo list and the journal
            if (!excludeFromUndoHistory) {
                LinorgJournal.getSingleInstance().recordFieldChange(this, this.getLanguageId(), languageIdLocal, LinorgJournal.UndoType.LanguageId);
            }
            LinorgJournal.getSingleInstance().saveJournalEntry(this.parentImdi.getUrlString(), this.xmlPath + ":LanguageId", oldLanguageId, languageIdLocal, "edit");
            //addFieldAttribute("LanguageId", languageIdLocal);
            languageId = languageIdLocal;
//            fieldLanguageId = languageId;
            parentImdi.setImdiNeedsSaveToDisk(this, updateUI);
        }

    }

    public ImdiVocabularies.VocabularyItem[] getLanguageList() {
        // TODO: move this url to somewhere appropriate (preferably in the imdi file)
        return ImdiVocabularies.getSingleInstance().getVocabulary(this, "http://www.mpi.nl/IMDI/Schema/ISO639-2Languages.xml").getVocabularyItems();
    }

    public ImdiVocabularies.Vocabulary getVocabulary() {
        return fieldVocabulary;
    }

    public ImdiField[] getSiblingField(String pathString) {
//        System.out.println("getSiblingField: " + pathString);
        for (ImdiField[] tempField : parentImdi.getFields().values().toArray(new ImdiField[][]{})) {
//            System.out.println("tempField[0].getFullXmlPath(): " + tempField[0].getFullXmlPath());
            if (tempField[0].getFullXmlPath().equals(pathString)) {
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
        parentImdi.setImdiNeedsSaveToDisk(this, updateUI);
    }

    public void setFieldAttribute(String fieldIDLocal, String cvType, String cvUrlString, String languageIdLocal, String keyNameLocal) {
        fieldID = fieldIDLocal;
        languageId = languageIdLocal;
        originalLanguageId = languageId;
        keyName = keyNameLocal;
        originalKeyName = keyName;
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
        if (hasVocabularyType) {
            if (cvUrlString != null && cvUrlString.length() > 0) {
                fieldVocabulary = ImdiVocabularies.getSingleInstance().getVocabulary(this, cvUrlString);
            }
        }
    }

    @Override
    public String toString() {
//            System.out.println("ImdiField: " + fieldValue);
//            if (!isDisplayable()) {
//                return "check attributes";// fieldAttributes.keys().toString();
//            }
        return fieldValue;
    }

    public int getFieldID() {
        if (fieldID != null) {
            return Integer.parseInt(fieldID.substring(1));
        } else {
            return -1;
        }
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyNameLocal, boolean updateUI, boolean excludeFromUndoHistory) {
        System.out.println("setKeyName: " + keyNameLocal);
        String lastValue = getKeyName();
        System.out.println("lastValue: " + lastValue);
        if (lastValue != null) {
            if (!lastValue.equals(keyNameLocal)) { // only if the value is different
//                if (fieldAttributes.contains("Name")) { // only if there is already a key name
                if (!excludeFromUndoHistory) {
                    LinorgJournal.getSingleInstance().recordFieldChange(this, this.getKeyName(), keyNameLocal, LinorgJournal.UndoType.KeyName);
                }
                // TODO: resolve how to log key name changes
                LinorgJournal.getSingleInstance().saveJournalEntry(this.parentImdi.getUrlString(), this.xmlPath, lastValue, keyNameLocal, "editkeyname");
                keyName = keyNameLocal;
                translatedPath = null;
                getTranslateFieldName();
//                addFieldAttribute("Name", keyNameLocal);
                parentImdi.setImdiNeedsSaveToDisk(this, updateUI);
//                }
            }
        }
    }

    public String getTranslateFieldName() {
        if (translatedPath == null) {
            String fieldName = xmlPath;
            // TODO: move this to the imdischema class
            // replace the xml paths with user friendly node names
//            fieldName = fieldName.replace(ImdiSchema.imdiPathSeparator + "METATRANSCRIPT" + ImdiSchema.imdiPathSeparator + "Session" + ImdiSchema.imdiPathSeparator + "Resources" + ImdiSchema.imdiPathSeparator + "WrittenResource", "WrittenResource");
//            fieldName = fieldName.replace(ImdiSchema.imdiPathSeparator + "METATRANSCRIPT" + ImdiSchema.imdiPathSeparator + "Session" + ImdiSchema.imdiPathSeparator + "MDGroup" + ImdiSchema.imdiPathSeparator + "Actors" + ImdiSchema.imdiPathSeparator + "Actor", "Actors");
//            fieldName = fieldName.replace(ImdiSchema.imdiPathSeparator + "METATRANSCRIPT" + ImdiSchema.imdiPathSeparator + "Session" + ImdiSchema.imdiPathSeparator + "Resources" + ImdiSchema.imdiPathSeparator + "Anonyms", "Anonyms");
//            fieldName = fieldName.replace(ImdiSchema.imdiPathSeparator + "METATRANSCRIPT" + ImdiSchema.imdiPathSeparator + "Session" + ImdiSchema.imdiPathSeparator + "Resources" + ImdiSchema.imdiPathSeparator + "MediaFile", "MediaFiles");
//            fieldName = fieldName.replace(ImdiSchema.imdiPathSeparator + "METATRANSCRIPT" + ImdiSchema.imdiPathSeparator + "Session" + ImdiSchema.imdiPathSeparator + "MDGroup", "");
            fieldName = fieldName.replace(ImdiSchema.imdiPathSeparator + "METATRANSCRIPT" + ImdiSchema.imdiPathSeparator + "Session" + ImdiSchema.imdiPathSeparator + "MDGroup", "");
            fieldName = fieldName.replace(ImdiSchema.imdiPathSeparator + "METATRANSCRIPT" + ImdiSchema.imdiPathSeparator + "Session", "");
            fieldName = fieldName.replace(ImdiSchema.imdiPathSeparator + "METATRANSCRIPT" + ImdiSchema.imdiPathSeparator + "Corpus", "");
            fieldName = fieldName.replace(ImdiSchema.imdiPathSeparator + "METATRANSCRIPT" + ImdiSchema.imdiPathSeparator + "Catalogue", "");


//                    if (attributeName.equals("Name")) {
            if (fieldName.endsWith("Keys.Key")) {
//                System.out.println("Found key for: " + xmlPath);
                //String keyValue = getFieldAttribute("Name");
                if (keyName != null) {
//                    System.out.println("Key value valid: " + keyValue.toString());
                    fieldName = fieldName + ImdiSchema.imdiPathSeparator + keyName;
                }
//                xmlPath = xmlPath + ImdiSchema.imdiPathSeparator + attributeValue;

            }
            if (fieldName.startsWith(".")) {
                fieldName = fieldName.substring(1);
            }
            if (LinorgSessionStorage.getSingleInstance().useLanguageIdInColumnName) {
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
}
