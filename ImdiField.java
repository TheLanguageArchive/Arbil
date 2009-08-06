package mpi.linorg;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Document   : ImdiField
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
public class ImdiField {

    public ImdiTreeObject parentImdi;
    public String xmlPath;
    private String translatedPath = null;
    public String fieldValue = "";
    public String fieldID;
    private String vocabularyKey;
    private boolean hasVocabularyType = false;
    public boolean vocabularyIsOpen;
    public boolean vocabularyIsList;
    public boolean fieldNeedsSaveToDisk = false;
    private Hashtable<String, String> fieldAttributes = new Hashtable();
    private int isLongField = -1;

    public ImdiField(ImdiTreeObject localParentImdi, String tempPath, String tempValue) {
        parentImdi = localParentImdi;
        fieldValue = tempValue;
        xmlPath = tempPath;
    }

    public boolean isLongField() {
        if (isLongField == -1) {
            // calculate length and count line breaks
            // TODO: the length use to trigger a long field is currently quite abitary but should however relate to the length of the text field in the UI
            if (fieldValue.length() > 50 || fieldValue.contains("\n")) {
                isLongField = 1;
            } else {
                isLongField = 0;
            }
        }
        return isLongField == 1;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    // returns the full xml path with the path indexes replaced by x's
    public String getGenericFullXmlPath() {
        return getFullXmlPath().replaceAll("\\(\\d*?\\)", "(x)");
    }

    public String getFullXmlPath() {
        String[] pathStringArray = this.parentImdi.getUrlString().split("#");
        if (pathStringArray.length > 1) {
            return pathStringArray[1] + this.xmlPath;
        } else {
            return this.xmlPath;
        }
    }

    public void setFieldValue(String fieldValue, boolean updateUI) {
        if (!this.fieldValue.equals(fieldValue)) {
            GuiHelper.linorgJournal.saveJournalEntry(this.parentImdi.getUrlString(), this.xmlPath, this.fieldValue, fieldValue, "edit");
            this.fieldValue = fieldValue;
            new FieldChangeTriggers().actOnChange(this);
            parentImdi.setImdiNeedsSaveToDisk(true, updateUI);
            fieldNeedsSaveToDisk = true;
            isLongField = -1;
        }
    }

    public boolean hasVocabulary() {
        return (vocabularyKey != null);
    }

    public String getLanguageId() {
        return fieldAttributes.get("LanguageId");
    }

    public void setLanguageId(String languageId, boolean updateUI) {
        String oldLanguageId = getLanguageId();
        if (!languageId.equals(oldLanguageId)) {
            GuiHelper.linorgJournal.saveJournalEntry(this.parentImdi.getUrlString(), this.xmlPath + ":LanguageId", oldLanguageId, languageId, "edit");
            fieldAttributes.put("LanguageId", languageId);
//            fieldLanguageId = languageId;
            parentImdi.setImdiNeedsSaveToDisk(true, updateUI);
            fieldNeedsSaveToDisk = true;
            isLongField = -1;
        }

    }

    public Enumeration<ImdiVocabularies.VocabularyItem> getLanguageList() { // TODO: move this url to somewhere appropriate (preferably in the imdi file)
//        return a list if objects 
        return ImdiVocabularies.getSingleInstance().getVocabulary(this, "http://www.mpi.nl/IMDI/Schema/ISO639-2Languages.xml").vocabularyItems.elements();
    }

    public ImdiVocabularies.Vocabulary getVocabulary() {
        if (vocabularyKey == null) {
            return null;
        }
        // make sure that the current value is in the list if it is an open vocabulary (this could be done in a better place ie on first load whe all the values are available)
//        if (vocabularyIsOpen && fieldValue != null && fieldValue.length() > 0) {
//            imdiVocabularies.addVocabularyEntry(parentImdi, vocabularyKey, fieldValue);
//        }
        return ImdiVocabularies.getSingleInstance().getVocabulary(this, vocabularyKey);
    }

    public ImdiField[] getSiblingField(String pathString) {
        System.out.println("getSiblingField: " + pathString);
        for (ImdiField[] tempField : parentImdi.getFields().values().toArray(new ImdiField[][]{})) {
            System.out.println("tempField[0].getFullXmlPath(): " + tempField[0].getFullXmlPath());
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
        // set up the vocabularies
        if (hasVocabularyType) {
            Object linkAttribute = fieldAttributes.get("Link");
            if (linkAttribute != null) {
                vocabularyKey = linkAttribute.toString();
                ImdiVocabularies.getSingleInstance().getVocabulary(this, vocabularyKey);
            }
        }
    // end set up the vocabularies
    }

    public void addAttribute(String attributeName, String attributeValue) {
//        System.out.println("attributeName: " + attributeName);
//        System.out.println("attributeValue: " + attributeValue);
        // TODO: this could be done as required no on load. ie when getId is called 
        if (attributeName.equals("id")) {
            fieldID = attributeValue;
        }
        // look for the vocabulary type 
        if (attributeName.equals("Type")) {
            //System.out.println("setVocabularyType");
            hasVocabularyType = true;
            if (attributeValue.equals("OpenVocabularyList")) {
                vocabularyIsList = true;
                vocabularyIsOpen = true;
            } else if (attributeValue.equals("OpenVocabulary")) {
                vocabularyIsList = false;
                vocabularyIsOpen = true;
            } else if (attributeValue.equals("ClosedVocabularyList")) {
                vocabularyIsList = true;
                vocabularyIsOpen = false;
            } else if (attributeValue.equals("ClosedVocabulary")) {
                vocabularyIsList = false;
                vocabularyIsOpen = false;
            } else {
                hasVocabularyType = false;
            }
        }
        fieldAttributes.put(attributeName, attributeValue);
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
        return fieldAttributes.get("Name");
    }

    public void setKeyName(String keyName, boolean updateUI) {
        System.out.println("setKeyName: " + keyName);
        String lastValue = getKeyName();
        System.out.println("lastValue: " + lastValue);
        if (lastValue != null) {
            if (!lastValue.equals(keyName)) { // only if the value is different
//                if (fieldAttributes.contains("Name")) { // only if there is already a key name
                // TODO: resolve how to log key name changes
                GuiHelper.linorgJournal.saveJournalEntry(this.parentImdi.getUrlString(), this.xmlPath, lastValue, keyName, "editkeyname");
                fieldAttributes.put("Name", keyName);
                parentImdi.setImdiNeedsSaveToDisk(true, updateUI);
                fieldNeedsSaveToDisk = true;
                getTranslateFieldName();
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
                Object keyValue = fieldAttributes.get("Name");
                if (keyValue != null) {
//                    System.out.println("Key value valid: " + keyValue.toString());
                    fieldName = fieldName + ImdiSchema.imdiPathSeparator + keyValue.toString();
                }
//                xmlPath = xmlPath + ImdiSchema.imdiPathSeparator + attributeValue;

            }
            if (fieldName.startsWith(".")) {
                fieldName = fieldName.substring(1);
            }
            translatedPath = fieldName;
        }
        return translatedPath;
    }
}
