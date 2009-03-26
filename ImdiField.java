/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 *
 * @author petwit
 */
public class ImdiField {

    static public ImdiVocabularies imdiVocabularies = new ImdiVocabularies();
    public ImdiTreeObject parentImdi;
    public String xmlPath;
    private String translatedPath = null;
//        public String nodeName;
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
    //translatedPath = translateFieldName(tempPath + siblingSpacer);
    }

    public boolean isLongField() {
        if (isLongField == -1) {
            // calculate length and count line breaks
            // TODO: the length chosed in currently abitary and should relate to the length of the text field
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

    public void setFieldValue(String fieldValue, boolean updateUI) {
        if (!this.fieldValue.equals(fieldValue)) {
            GuiHelper.linorgJournal.saveJournalEntry(this.parentImdi.getUrlString(), this.xmlPath, this.fieldValue, fieldValue, "edit");
            this.fieldValue = fieldValue;
            parentImdi.setImdiNeedsSaveToDisk(true);
            fieldNeedsSaveToDisk = true;
            isLongField = -1;
            if (updateUI) {
                parentImdi.clearIcon();
            }
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
            parentImdi.setImdiNeedsSaveToDisk(true);
            fieldNeedsSaveToDisk = true;
            isLongField = -1;
            if (updateUI) {
                parentImdi.clearIcon();
            }
        }

    }

    public Enumeration getLanguageList() { // TODO: move this url to somewhere appropriate
        return imdiVocabularies.getVocabulary("http://www.mpi.nl/IMDI/Schema/LanguagesID.xml");
    }

    public Enumeration getVocabulary() {
        if (vocabularyKey == null) {
            return null;
        }
        // make sure that the current value is in the list if it is an open vocabulary (this could be done in a better place ie on first load whe all the values are available)
        if (vocabularyIsOpen) {
            imdiVocabularies.addVocabularyEntry(vocabularyKey, fieldValue);
        }
        return imdiVocabularies.getVocabulary(vocabularyKey);
    }

    public boolean isDisplayable() {
        return (fieldValue != null && /*fieldValue.trim().length() > 0 && */ !xmlPath.contains("CorpusLink") && !xmlPath.endsWith(".Keys"));
    }

    public void finishLoading() {
        // set up the vocabularies
        if (hasVocabularyType) {
            Object linkAttribute = fieldAttributes.get("Link");
            if (linkAttribute != null) {
                vocabularyKey = linkAttribute.toString();
                imdiVocabularies.getVocabulary(vocabularyKey);
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
