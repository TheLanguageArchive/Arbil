package nl.mpi.arbil.data;
import nl.mpi.arbil.util.MessageDialogHandler;

/**
 * FieldChangeTriggers.java
 * Created on Jul 13, 2009, 4:15:02 PM
 * @author Peter.Withers@mpi.nl
 */
public class FieldChangeTriggers {

    private static MessageDialogHandler messageDialogHandler;

    public static void setMessageDialogHandler(MessageDialogHandler handler) {
        messageDialogHandler = handler;
    }

    // the following strings need to be read from a template file or a vocaulary etc
    public void actOnChange(ArbilField changedArbilField) {
        String fieldPath = changedArbilField.getGenericFullXmlPath();
        System.out.println("fieldPath: " + fieldPath);
        for (String[] currentTrigger : changedArbilField.getParentDataNode().getNodeTemplate().fieldTriggersArray) {
            if (fieldPath.equals(currentTrigger[0])) {
                // we now have the path for two fields:
                // .METATRANSCRIPT.Session.MDGroup.Actors.Actor(x).Languages.Language(x).Id
                // .METATRANSCRIPT.Session.MDGroup.Actors.Actor(2).Languages.Language(3).Name
                // and we need to put the (\d) back into the (x)
                String originalFieldPath = changedArbilField.getFullXmlPath();
                int lastBracketPos = originalFieldPath.lastIndexOf(")");
                int lastTriggerBracket = currentTrigger[1].lastIndexOf(")");
                if (lastTriggerBracket < 0) {
                    messageDialogHandler.addMessageDialogToQueue("Error in trigger from template (missing bracket): " + currentTrigger[1], "Field Trigger");
                    break;
                }
                // care must me taken here to prevet issues with child nodes greater than 9 ie (12), (x) etc.
                String targetFieldPath = originalFieldPath.substring(0, lastBracketPos) + currentTrigger[1].substring(lastTriggerBracket);
                System.out.println("originalFieldPath: " + originalFieldPath);
                System.out.println("targetFieldPath: " + targetFieldPath);
                ArbilField[] targetField = changedArbilField.getSiblingField(targetFieldPath);
                ArbilVocabularyItem vocabItem = changedArbilField.getVocabulary().findVocabularyItem(changedArbilField.getFieldValue());
                if (vocabItem != null) {
                    String valueForTargetField = null;
                    if (currentTrigger[2].equals("Content")) {
                        valueForTargetField = vocabItem.descriptionString;
                    } else if (currentTrigger[2].equals("Value")) {
                        valueForTargetField = vocabItem.itemDisplayName;
                    } else if (currentTrigger[2].equals("Code")) {
                        valueForTargetField = vocabItem.itemCode;
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
