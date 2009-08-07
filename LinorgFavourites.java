package mpi.linorg;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Document   : LinorgFavourites
 * Created on : Mar 3, 2009, 11:19:14 AM
 * @author Peter.Withers@mpi.nl
 */
public class LinorgFavourites {

    private Hashtable<String, ImdiTreeObject> userFavourites;
    static private LinorgFavourites singleInstance = null;

    static synchronized public LinorgFavourites getSingleInstance() {
        System.out.println("LinorgTemplates getSingleInstance");
        if (singleInstance == null) {
            singleInstance = new LinorgFavourites();
        }
        return singleInstance;
    }

    private LinorgFavourites() {
        loadSelectedTemplates();
    }

    private void loadSelectedTemplates() {
        Vector<String> userFavouritesStrings;
        try {
            userFavouritesStrings = (Vector<String>) LinorgSessionStorage.getSingleInstance().loadObject("selectedFavourites");
        } catch (Exception ex) {
            System.out.println("load selectedFavourites failed: " + ex.getMessage());
            userFavouritesStrings = new Vector<String>();
        }
        userFavourites = new Hashtable<String, ImdiTreeObject>();
        // loop templates and load the imdi objects then set the template flags for each
        for (Enumeration<String> templatesEnum = userFavouritesStrings.elements(); templatesEnum.hasMoreElements();) {
            ImdiTreeObject currentImdiObject = GuiHelper.imdiLoader.getImdiObject(null, templatesEnum.nextElement());
            currentImdiObject.setTemplateStatus(true);
            userFavourites.put(currentImdiObject.getUrlString(), currentImdiObject);
        }
    }

    public void toggleFavouritesList(ImdiTreeObject[] imdiObjectArray, boolean setAsTempate) {
        System.out.println("toggleTemplateList: " + setAsTempate);
        for (ImdiTreeObject currentImdiObject : imdiObjectArray) {
            if (currentImdiObject.getFields().size() == 0) {
                // note: the way that favourites are shown in a table will not show meta nodes but their child nodes instead
                setAsTempate = false;
            }
            if (setAsTempate) {
                addAsTemplate(currentImdiObject.getUrlString());
            } else {
                removeFromFavourites(currentImdiObject.getUrlString());
            }
            currentImdiObject.setTemplateStatus(setAsTempate);
        }
    }

    private void addAsTemplate(String imdiUrlString) {
        if (!userFavourites.containsKey(imdiUrlString)) {
            userFavourites.put(imdiUrlString, GuiHelper.imdiLoader.getImdiObject(null, imdiUrlString));
            saveSelectedTemplates();
            loadSelectedTemplates();
        }
    }

    private void removeFromFavourites(String imdiUrlString) {
        while (userFavourites.containsKey(imdiUrlString)) {
            userFavourites.remove(imdiUrlString);
        }
        saveSelectedTemplates();
        loadSelectedTemplates();
    }

    public void saveSelectedTemplates() {
        try {
            LinorgSessionStorage.getSingleInstance().saveObject(new Vector(userFavourites.keySet()), "selectedFavourites");
        } catch (Exception ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
        }
    }

    public ImdiTreeObject[] listAllFavourites() {
        return userFavourites.values().toArray(new ImdiTreeObject[userFavourites.size()]);
    }

    public Enumeration listFavouritesFor(Object targetNodeUserObject) {
        System.out.println("listTemplatesFor: " + targetNodeUserObject);
        Vector<String[]> validTemplates = new Vector<String[]>();
        if (targetNodeUserObject instanceof ImdiTreeObject) {
            ImdiTreeObject targetImdiObject = (ImdiTreeObject) targetNodeUserObject;
            boolean targetIsCorpus = targetImdiObject.isCorpus();
            boolean targetIsSession = targetImdiObject.isSession();
            boolean targetIsImdiChild = targetImdiObject.isImdiChild();
            for (Enumeration<ImdiTreeObject> imdiObjectEnum = userFavourites.elements(); imdiObjectEnum.hasMoreElements();) {
                ImdiTreeObject currentTemplateObject = imdiObjectEnum.nextElement();
                boolean addThisTemplate = false;
                if (targetIsCorpus && !currentTemplateObject.isImdiChild()) {
                    addThisTemplate = true;
                } else if (targetIsSession && currentTemplateObject.isImdiChild()) {
                    addThisTemplate = GuiHelper.imdiSchema.nodeCanExistInNode(targetImdiObject, currentTemplateObject);
                } else if (targetIsImdiChild && currentTemplateObject.isImdiChild()) {
                    addThisTemplate = GuiHelper.imdiSchema.nodeCanExistInNode(targetImdiObject, currentTemplateObject);
                }
                if (addThisTemplate) {
//                    System.out.println("adding: " + currentTemplateObject);
                    validTemplates.add(new String[]{currentTemplateObject.toString(), currentTemplateObject.getUrlString()});
                } else {
                    // imdi child templates cannot be added to a corpus
                    // sessions cannot be added to a session
//                    System.out.println("omitting: " + currentTemplateObject);
                }
            }
        }
        return validTemplates.elements();
    }

    public String getNodeType(ImdiTreeObject templateImdiObject, ImdiTreeObject targetImdiObject) {
        // takes the source path and destination path and creates a complete combined path
        // in:
        // .METATRANSCRIPT.Session.MDGroup.Actors.Actor(12).Languages.Language(5)
        // .METATRANSCRIPT.Session.MDGroup.Actors.Actor(27)
        // out: 
        // .METATRANSCRIPT.Session.MDGroup.Actors.Actor(27).Languages.Language

        String templateXmlPath = templateImdiObject.getURL().getRef();
        String targetXmlPath = targetImdiObject.getURL().getRef();
        System.out.println("getNodeType: \ntemplateXmlPath: " + templateXmlPath + "\ntargetXmlPath:" + targetXmlPath);
        String returnValue;
        if (templateImdiObject.isSession()) {
            returnValue = ImdiSchema.imdiPathSeparator + "METATRANSCRIPT" + ImdiSchema.imdiPathSeparator + "Session";
        } else if (templateImdiObject.isCorpus()) {
            returnValue = ImdiSchema.imdiPathSeparator + "METATRANSCRIPT" + ImdiSchema.imdiPathSeparator + "Corpus";
        } else if (templateImdiObject.isImdiChild()) {
            if (targetXmlPath == null) {
                returnValue = templateXmlPath.replaceAll("\\(\\d*?\\)$", "");
            } else {
                // pass the (x) values on in the return value
                System.out.println("targetXmlPath: " + targetXmlPath);
                System.out.println("templateXmlPath: " + templateXmlPath);
                templateXmlPath = templateXmlPath.replaceAll("\\(\\d*?\\)$", "");
                String[] splitTemplateXmlPath = templateXmlPath.split("\\)");
                String[] splitTargetXmlPath = targetXmlPath.split("\\)");
                System.out.println("splitTemplateXmlPath: " + splitTemplateXmlPath.length + " splitTargetXmlPath: " + splitTargetXmlPath.length);
                returnValue = "";
                for (int partCounter = 0; partCounter < splitTemplateXmlPath.length; partCounter++) {
                    if (splitTargetXmlPath.length > partCounter) {
                        returnValue = returnValue.concat(splitTargetXmlPath[partCounter] + ")");
                    } else {
                        returnValue = returnValue.concat(splitTemplateXmlPath[partCounter] + ")");
                    }
                }
                returnValue = returnValue.replaceAll("\\)$", "");
            }
//            System.out.println("getNodeType returnValue: " + returnValue);
//                        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
//                        // preserve the child node path
//                        if (targetNodePath.replaceAll("[^(]*", "").length() >= templateNodePath.replaceAll("[^(]*", "").length()) {
//                        String currentTemplateXPath = currentTemplate.replaceFirst("\\.xml$", "");
//                        String currentTemplateName = currentTemplateXPath.substring(currentTemplateXPath.lastIndexOf(".") + 1);
//                        System.out.println("currentTemplateXPath: " + currentTemplateXPath);
//                        System.out.println("targetNodePath: " + targetNodePath);
//                        String destinationXPath;
//                        if (currentTemplateXPath.contains(")")) {
//                            destinationXPath = targetNodePath + currentTemplateXPath.substring(currentTemplateXPath.lastIndexOf(")") + 1);
//                        } else {
//                            destinationXPath = currentTemplateXPath;
//                        }
//                        }
//                        // end preserve the child node path
//                        ////////////////////////////////////////////////////////////////////////////////////////////////////////////

//                returnValue = returnValue.replaceAll("\\(\\d+\\)", "");
        } else {
            returnValue = null;
        }
        System.out.println("getNodeTypeReturnValue: " + returnValue);
//        System.out.println("for : " + templateXmlPath);
        return returnValue;
    }

    public void mergeFromFavourite(ImdiTreeObject targetImdiObject, ImdiTreeObject templateImdiObject, boolean overwriteValues) {
//        System.out.println("mergeFromFavourite: " + addedNodeUrl + " : " + imdiTemplateUrl);
//        ImdiTreeObject templateImdiObject = GuiHelper.imdiLoader.getImdiObject("", imdiTemplateObject);
//        ImdiTreeObject targetImdiObject = GuiHelper.imdiLoader.getImdiObject("", addedNodeUrl);

//        System.out.println("targetImdiObjectLoading: " + targetImdiObject.isLoading);
//        while (targetImdiObject.isLoading) {
//            try {
//                Thread.currentThread().sleep(500);
//            } catch (InterruptedException ie) {
//                GuiHelper.linorgBugCatcher.logError(ie);
//            }
//        }

        Hashtable<String, ImdiField[]> targetFieldsHash = targetImdiObject.getFields();
        for (Enumeration<ImdiField[]> templateFieldEnum = templateImdiObject.getFields().elements(); templateFieldEnum.hasMoreElements();) {
            ImdiField[] currentTemplateFields = templateFieldEnum.nextElement();
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
//        targetImdiObject.clearIcon();
//        targetImdiObject.saveChangesToCache();
    // loop the child nodes and add them accordingly

    }
}
