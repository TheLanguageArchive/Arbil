package nl.mpi.arbil;

/**
 * FieldChangeTriggers.java
 * Created on Jul 13, 2009, 4:15:02 PM
 * @author Peter.Withers@mpi.nl
 */
public class FieldChangeTriggers {
    // the following strings need to be read from a template file or a vocaulary etc

    public void actOnChange(ImdiField changedImdiField) {
        String fieldPath = changedImdiField.getGenericFullXmlPath();
        System.out.println("fieldPath: " + fieldPath);
        for (String[] currentTrigger : changedImdiField.parentImdi.currentTemplate.fieldTriggersArray) {
            if (fieldPath.equals(currentTrigger[0])) {
                // we now have the path for two fields:
                // .METATRANSCRIPT.Session.MDGroup.Actors.Actor(x).Languages.Language(x).Id
                // .METATRANSCRIPT.Session.MDGroup.Actors.Actor(2).Languages.Language(3).Name
                // and we need to put the (\d) back into the (x)
                String originalFieldPath = changedImdiField.getFullXmlPath();
                int lastBracketPos = originalFieldPath.lastIndexOf(")");
                // care must me taken here to prevet issues with child nodes greater than 9 ie (12), (x) etc.
                String targetFieldPath = originalFieldPath.substring(0, lastBracketPos) + currentTrigger[1].substring(currentTrigger[1].lastIndexOf(")"));
                System.out.println("originalFieldPath: " + originalFieldPath);
                System.out.println("targetFieldPath: " + targetFieldPath);
                ImdiField[] targetField = changedImdiField.getSiblingField(targetFieldPath);
                ImdiVocabularies.VocabularyItem vocabItem = changedImdiField.getVocabulary().findVocabularyItem(changedImdiField.fieldValue);
                if (vocabItem != null) {
                    String valueForTargetField = null;
                    if (currentTrigger[2].equals("Content")) {
                        valueForTargetField = vocabItem.descriptionString;
                    } else if (currentTrigger[2].equals("Value")) {
                        valueForTargetField = vocabItem.languageName;
                    } else if (currentTrigger[2].equals("Code")) {
                        valueForTargetField = vocabItem.languageCode;
                    } else if (currentTrigger[2].equals("FollowUp")) {
                        valueForTargetField = vocabItem.followUpVocabulary;
                    }
                    if (valueForTargetField != null) {
                        targetField[0].setFieldValue(valueForTargetField, true, false);
                    }
                }
            }
        }
    }
}
