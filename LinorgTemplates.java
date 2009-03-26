/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Document   : LinorgTemplates
 * Created on : Mar 3, 2009, 11:19:14 AM
 * @author petwit
 */
public class LinorgTemplates {

    private Vector<String> selectedTemplates;
    private Vector<ImdiTreeObject> imdiTemplateObjects;

    public LinorgTemplates() {
        loadSelectedTemplates();
    }

    private void loadSelectedTemplates() {
        try {
            selectedTemplates = (Vector<String>) GuiHelper.linorgSessionStorage.loadObject("selectedTemplates");
        } catch (Exception ex) {
            System.out.println("load selectedTemplates failed: " + ex.getMessage());
            selectedTemplates = new Vector<String>();
        }
        imdiTemplateObjects = new Vector<ImdiTreeObject>();
        // loop templates and load the imdi objects then set the template flags for each
        for (Enumeration<String> templatesEnum = selectedTemplates.elements(); templatesEnum.hasMoreElements();) {
            ImdiTreeObject currentImdiObject = GuiHelper.imdiLoader.getImdiObject("", templatesEnum.nextElement());
            currentImdiObject.setTemplateStatus(true);
            imdiTemplateObjects.add(currentImdiObject);
        }
    }

    public void toggleTemplateList(Vector<ImdiTreeObject> imdiObjectVector, boolean setAsTempate) {
        System.out.println("toggleTemplateList: " + setAsTempate);
        for (Enumeration<ImdiTreeObject> imdiObjectEnum = imdiObjectVector.elements(); imdiObjectEnum.hasMoreElements();) {
            ImdiTreeObject currentImdiObject = imdiObjectEnum.nextElement();
            if (setAsTempate) {
                addAsTemplate(currentImdiObject.getUrlString());
            } else {
                removeFromTemplates(currentImdiObject.getUrlString());
            }
            currentImdiObject.setTemplateStatus(setAsTempate);
        }
    }

    public void addAsTemplate(String imdiUrlString) {
        if (!selectedTemplates.contains(imdiUrlString)) {
            selectedTemplates.add(imdiUrlString);
            saveSelectedTemplates();
            loadSelectedTemplates();
        }
    }

    public void removeFromTemplates(String imdiUrlString) {
        while (selectedTemplates.contains(imdiUrlString)) {
            selectedTemplates.remove(imdiUrlString);
        }
        saveSelectedTemplates();
        loadSelectedTemplates();
    }

    public void saveSelectedTemplates() {
        try {
            GuiHelper.linorgSessionStorage.saveObject(selectedTemplates, "selectedTemplates");
        } catch (Exception ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
        }
    }

    public Enumeration listTemplatesFor(Object targetNodeUserObject) {
        System.out.println("listTemplatesFor: " + targetNodeUserObject);
        Vector<String[]> validTemplates = new Vector<String[]>();
        if (targetNodeUserObject instanceof ImdiTreeObject) {
            ImdiTreeObject targetImdiObject = (ImdiTreeObject) targetNodeUserObject;
            boolean targetIsCorpus = targetImdiObject.isCorpus();
            boolean targetIsSession = targetImdiObject.isSession();
            boolean targetIsImdiChild = targetImdiObject.isImdiChild();
            for (Enumeration<ImdiTreeObject> imdiObjectEnum = imdiTemplateObjects.elements(); imdiObjectEnum.hasMoreElements();) {
                ImdiTreeObject currentTemplateObject = imdiObjectEnum.nextElement();
                boolean addThisTemplate = false;
                if (targetIsCorpus && !currentTemplateObject.isImdiChild()) {
                    addThisTemplate = true;
                } else if (targetIsSession && currentTemplateObject.isImdiChild()) {
                    addThisTemplate = true;
                } else if (targetIsImdiChild && currentTemplateObject.isImdiChild()) {
                    addThisTemplate = GuiHelper.imdiSchema.nodeCanExistInNode(targetImdiObject, currentTemplateObject);
                }
                if (addThisTemplate) {
                    System.out.println("adding: " + currentTemplateObject);
                    validTemplates.add(new String[]{currentTemplateObject.toString(), currentTemplateObject.getUrlString()});
                } else {
                    // imdi child templates cannot be added to a corpus
                    // sessions cannot be added to a session
                    System.out.println("omitting: " + currentTemplateObject);
                }
            }
        }
        return validTemplates.elements();
    }

    public String getNodeType(String imdiTemplateUrlString) {
        System.out.println("getNodeType: " + imdiTemplateUrlString);
        String returnValue;
        if (imdiTemplateUrlString.contains("#")) {
            returnValue = imdiTemplateUrlString.split("#")[1].split("\\(")[0];
        } else {
            returnValue = ImdiSchema.imdiPathSeparator + "METATRANSCRIPT" + ImdiSchema.imdiPathSeparator + "Session";
        }
        System.out.println("getNodeTypeReturnValue: " + returnValue);
        return returnValue;
    }

    public void mergeFromTemplate(ImdiTreeObject targetImdiObject, ImdiTreeObject templateImdiObject, boolean overwriteValues) {
//        System.out.println("mergeFromTemplate: " + addedNodeUrl + " : " + imdiTemplateUrl);
//        ImdiTreeObject templateImdiObject = GuiHelper.imdiLoader.getImdiObject("", imdiTemplateObject);
//        ImdiTreeObject targetImdiObject = GuiHelper.imdiLoader.getImdiObject("", addedNodeUrl);
        Hashtable<String, ImdiField[]> targetFieldsHash = targetImdiObject.getFields();
        for (Enumeration<ImdiField[]> templateFeildEnum = templateImdiObject.getFields().elements(); templateFeildEnum.hasMoreElements();) {
            ImdiField[] currentTemplateFields = templateFeildEnum.nextElement();
            if (currentTemplateFields.length > 0) {
                ImdiField[] targetNodeFields = targetFieldsHash.get(currentTemplateFields[0].getTranslateFieldName());

                System.out.println("TranslateFieldName: " + currentTemplateFields[0].getTranslateFieldName());
                System.out.println("targetImdiObjectLoading: " + targetImdiObject.isLoading());
                if (targetNodeFields != null) {
                    System.out.println("copy fields");
                    for (int fieldCounter = 0; fieldCounter < currentTemplateFields.length; fieldCounter++) {
                        ImdiField currentField;
                        if (targetNodeFields.length > fieldCounter) {// error here adding a template node
                            // copy to the exisiting fields
                            currentField = targetNodeFields[fieldCounter];
                            currentField.setFieldValue(currentTemplateFields[fieldCounter].getFieldValue(), false);
                        } else {
                            // add sub nodes if they dont already exist
                            currentField = new ImdiField(targetImdiObject, currentTemplateFields[fieldCounter].xmlPath, currentTemplateFields[fieldCounter].getFieldValue());
                            targetImdiObject.addField(currentField);
                            currentField.fieldNeedsSaveToDisk = true;
                        }
                        String currentLanguageId = currentTemplateFields[fieldCounter].getLanguageId();
                        if (currentLanguageId != null) {
                            currentField.setLanguageId(currentLanguageId, false);
                        }
                    }
                }
            }
        }
//        targetImdiObject.saveChangesToCache();
    }
}
