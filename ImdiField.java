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

    static private ImdiVocabularies imdiVocabularies = new ImdiVocabularies();
    public ImdiTreeObject parentImdi;
    public String xmlPath;
    public String translatedPath;
//        public String nodeName;
    public String fieldValue = "";
    public String fieldID;
    private String vocabularyKey;
    private boolean hasVocabularyType = false;
    public boolean vocabularyIsOpen;
    public boolean vocabularyIsList;
    public boolean fieldNeedsSaveToDisk = false;
    private Hashtable fieldAttributes = new Hashtable();

    public ImdiField(ImdiTreeObject localParentImdi, String tempPath, String tempValue) {
        parentImdi = localParentImdi;
        fieldValue = tempValue;
        xmlPath = tempPath;
    //translatedPath = translateFieldName(tempPath + siblingSpacer);
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(String fieldValue) {
        if (!this.fieldValue.equals(fieldValue)) {
            this.fieldValue = fieldValue;
            parentImdi.imdiNeedsSaveToDisk = true;
            fieldNeedsSaveToDisk = true;
            parentImdi.clearIcon();
        }
    }

    public boolean hasVocabulary() {
        return (vocabularyKey != null);
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
        return (fieldValue != null && /*fieldValue.trim().length() > 0 && */ !xmlPath.contains("CorpusLink"));
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
//        debugOut("attributeName: " + attributeName);
//        debugOut("attributeValue: " + attributeValue);
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

    public void translateFieldName(String fieldName) {
        // TODO: move this to the imdischema class
        // replace the xml paths with user friendly node names
        fieldName = fieldName.replace(ImdiSchema.imdiPathSeparator + "METATRANSCRIPT" + ImdiSchema.imdiPathSeparator + "Session" + ImdiSchema.imdiPathSeparator + "Resources" + ImdiSchema.imdiPathSeparator + "WrittenResource", "WrittenResource");
        fieldName = fieldName.replace(ImdiSchema.imdiPathSeparator + "METATRANSCRIPT" + ImdiSchema.imdiPathSeparator + "Session" + ImdiSchema.imdiPathSeparator + "MDGroup" + ImdiSchema.imdiPathSeparator + "Actors" + ImdiSchema.imdiPathSeparator + "Actor", "Actors");
        fieldName = fieldName.replace(ImdiSchema.imdiPathSeparator + "METATRANSCRIPT" + ImdiSchema.imdiPathSeparator + "Session" + ImdiSchema.imdiPathSeparator + "Resources" + ImdiSchema.imdiPathSeparator + "Anonyms", "Anonyms");
        fieldName = fieldName.replace(ImdiSchema.imdiPathSeparator + "METATRANSCRIPT" + ImdiSchema.imdiPathSeparator + "Session" + ImdiSchema.imdiPathSeparator + "Resources" + ImdiSchema.imdiPathSeparator + "MediaFile", "MediaFiles");
        fieldName = fieldName.replace(ImdiSchema.imdiPathSeparator + "METATRANSCRIPT" + ImdiSchema.imdiPathSeparator + "Session" + ImdiSchema.imdiPathSeparator + "MDGroup", "");
        fieldName = fieldName.replace(ImdiSchema.imdiPathSeparator + "METATRANSCRIPT" + ImdiSchema.imdiPathSeparator + "Session", "Session");
        fieldName = fieldName.replace(ImdiSchema.imdiPathSeparator + "METATRANSCRIPT" + ImdiSchema.imdiPathSeparator + "Corpus", "Corpus");
        translatedPath = fieldName;
    }
}
