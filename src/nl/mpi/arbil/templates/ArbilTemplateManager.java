package nl.mpi.arbil.templates;

import nl.mpi.arbil.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;
import javax.swing.ImageIcon;
import nl.mpi.arbil.clarin.CmdiProfileReader;
import nl.mpi.arbil.clarin.CmdiProfileReader.CmdiProfile;
import nl.mpi.arbil.data.ImdiSchema;

/**
 * ArbilTemplateManager.java
 * Created on Jul 15, 2009, 11:56:57 AM
 * @author Peter.Withers@mpi.nl
 */
public class ArbilTemplateManager {

    //private String defaultArbilTemplateName;
    static private ArbilTemplateManager singleInstance = null;
    private Hashtable<String, ArbilTemplate> templatesHashTable;
    private String[] builtInTemplates2 = {"Default", "Sign Language"}; // the first item in this list is the default template
//    private ArbilTemplate defaultArbilTemplate;
    //public String[] builtInTemplates = {"Corpus Branch (internal)", "Session (internal)", "Catalogue (internal)", "Sign Language (internal)"};

    static synchronized public ArbilTemplateManager getSingleInstance() {
        if (singleInstance == null) {
            singleInstance = new ArbilTemplateManager();
        }
        return singleInstance;
    }

    public File createTemplate(String selectedTemplate) {
        if (selectedTemplate.length() == 0) {
            return null;
        } else if (Arrays.binarySearch(builtInTemplates2, selectedTemplate) > -1) {
            return null;
        } else {
            File selectedTemplateFile = getTemplateFile(selectedTemplate);
            selectedTemplateFile.getParentFile().mkdir();
            LinorgSessionStorage.getSingleInstance().saveRemoteResource(ImdiSchema.class.getResource("/nl/mpi/arbil/resources/templates/template.xml"), selectedTemplateFile, null, true, new DownloadAbortFlag());
            new File(selectedTemplateFile.getParentFile(), "components").mkdir(); // create the components directory
            File examplesDirectory = new File(selectedTemplateFile.getParentFile(), "example-components");
            examplesDirectory.mkdir(); // create the example components directory
            // copy example components from the jar file            
            for (String[] pathString : ArbilTemplateManager.getSingleInstance().getTemplate(builtInTemplates2[0]).templatesArray) {
                LinorgSessionStorage.getSingleInstance().saveRemoteResource(ImdiSchema.class.getResource("/nl/mpi/arbil/resources/templates/" + pathString[0]), new File(examplesDirectory, pathString[0]), null, true, new DownloadAbortFlag());
            }
            // copy example "format.xsl" from the jar file which is used in the imdi to html conversion
            LinorgSessionStorage.getSingleInstance().saveRemoteResource(ImdiSchema.class.getResource("/nl/mpi/arbil/resources/xsl/imdi-viewer.xsl"), new File(selectedTemplateFile.getParentFile(), "example-format.xsl"), null, true, new DownloadAbortFlag());
            return selectedTemplateFile;
        }
    }

    public File getTemplateFile(String currentTemplate) {
        File currentTemplateFile = new File(getTemplateDirectory().getAbsolutePath() + File.separatorChar + currentTemplate + File.separatorChar + "template.xml");
//        if (!currentTemplateFile.getParentFile().exists()) {
//            currentTemplateFile.getParentFile().mkdir();
//        }
        return currentTemplateFile;
    }

//    public boolean defaultTemplateIsCurrentTemplate() {
//        return defaultArbilTemplateName.equals(builtInTemplates2[0]);
//    }
//    public String getCurrentTemplateName() {
//        return defaultArbilTemplateName;
//    }
//    public void setCurrentTemplate(String currentTemplateLocal) {
//        defaultArbilTemplateName = currentTemplateLocal;
//        try {
//            LinorgSessionStorage.getSingleInstance().saveString("CurrentTemplate", currentTemplateLocal);
//        } catch (Exception ex) {
//            GuiHelper.linorgBugCatcher.logError(ex);
//        }
//    }
    public File getTemplateDirectory() {
        return new File(LinorgSessionStorage.getSingleInstance().storageDirectory, "templates");
    }

    public void addSelectedTemplates(String templateString) {
        ArrayList<String> selectedTamplates = new ArrayList<String>();
        try {
            selectedTamplates.addAll(Arrays.asList(LinorgSessionStorage.getSingleInstance().loadStringArray("selectedTemplates")));
        } catch (Exception e) {
            GuiHelper.linorgBugCatcher.logError("No selectedTemplates file, will create one now.", e);
        }
        selectedTamplates.add(templateString);
        LinorgSessionStorage.getSingleInstance().saveStringArray("selectedTemplates", selectedTamplates.toArray(new String[]{}));
    }

    public void removeSelectedTemplates(String templateString) {
        ArrayList<String> selectedTamplates = new ArrayList<String>();
        selectedTamplates.addAll(Arrays.asList(LinorgSessionStorage.getSingleInstance().loadStringArray("selectedTemplates")));
        while (selectedTamplates.contains(templateString)) {
            selectedTamplates.remove(templateString);
        }
        LinorgSessionStorage.getSingleInstance().saveStringArray("selectedTemplates", selectedTamplates.toArray(new String[]{}));
    }

    public ArrayList<String> getSelectedTemplateArrayList() {
        ArrayList<String> selectedTamplates = new ArrayList<String>();
        try {
            selectedTamplates.addAll(Arrays.asList(LinorgSessionStorage.getSingleInstance().loadStringArray("selectedTemplates")));
        } catch (Exception e) {
            GuiHelper.linorgBugCatcher.logError("No selectedTemplates file, will create one now.", e);
        }
        return selectedTamplates;
    }

    public class MenuItemData {

        public String menuText;
        public String menuAction;
        public String menuToolTip;
        public ImageIcon menuIcon;
    }

    public MenuItemData[] getSelectedTemplates() {
        ImdiIcons imdiIcons = ImdiIcons.getSingleInstance();
        String[] locationsArray = LinorgSessionStorage.getSingleInstance().loadStringArray("selectedTemplates");
        if (locationsArray == null || locationsArray.length == 0) {
            addSelectedTemplates("builtin:METATRANSCRIPT.Corpus.xml");
            addSelectedTemplates("builtin:METATRANSCRIPT.Catalogue.xml");
            addSelectedTemplates("builtin:METATRANSCRIPT.Session.xml");
            locationsArray = LinorgSessionStorage.getSingleInstance().loadStringArray("selectedTemplates");
        }
        MenuItemData[] returnArray = new MenuItemData[locationsArray.length];
        for (int insertableCounter = 0; insertableCounter < locationsArray.length; insertableCounter++) {
            returnArray[insertableCounter] = new MenuItemData();
            if (locationsArray[insertableCounter].startsWith("builtin:")) {
                String currentString = locationsArray[insertableCounter].substring("builtin:".length());
                for (String currentTemplateName[] : ArbilTemplateManager.getSingleInstance().getTemplate(null).rootTemplatesArray) {
                    if (currentString.equals(currentTemplateName[0])) {
                        returnArray[insertableCounter].menuText = currentTemplateName[1];
                        returnArray[insertableCounter].menuAction = "." + currentTemplateName[0].replaceFirst("\\.xml$", "");
                        returnArray[insertableCounter].menuToolTip = currentTemplateName[1];
                        if (returnArray[insertableCounter].menuText.contains("Corpus")) {
                            returnArray[insertableCounter].menuIcon = imdiIcons.corpusnodeColorIcon;
                        } else if (returnArray[insertableCounter].menuText.contains("Catalogue")) {
                            returnArray[insertableCounter].menuIcon = imdiIcons.catalogueColorIcon;
                        } else {
                            returnArray[insertableCounter].menuIcon = imdiIcons.sessionColorIcon;
                        }
                    }
                }
            } else if (locationsArray[insertableCounter].startsWith("custom:")) {
                String currentString = locationsArray[insertableCounter].substring("custom:".length());
                returnArray[insertableCounter].menuText = currentString.substring(currentString.lastIndexOf("/") + 1);
                returnArray[insertableCounter].menuAction = currentString;
                returnArray[insertableCounter].menuToolTip = currentString;
                returnArray[insertableCounter].menuIcon = imdiIcons.clarinIcon;
            } else if (locationsArray[insertableCounter].startsWith("template:")) {
                // todo:
                String currentString = locationsArray[insertableCounter].substring("template:".length());
                returnArray[insertableCounter].menuText = currentString + " (not available)";
                returnArray[insertableCounter].menuAction = currentString;
                returnArray[insertableCounter].menuToolTip = currentString;
                returnArray[insertableCounter].menuIcon = imdiIcons.sessionColorIcon;
            } else if (locationsArray[insertableCounter].startsWith("clarin:")) {
                String currentString = locationsArray[insertableCounter].substring("clarin:".length());
                CmdiProfile cmdiProfile = CmdiProfileReader.getSingleInstance().getProfile(currentString);
                returnArray[insertableCounter].menuText = cmdiProfile.name;
                returnArray[insertableCounter].menuAction = cmdiProfile.getXsdHref();
                returnArray[insertableCounter].menuToolTip = cmdiProfile.description;
                returnArray[insertableCounter].menuIcon = imdiIcons.clarinIcon;
            }
        }
        Arrays.sort(returnArray, new Comparator() {

            public int compare(Object firstItem, Object secondItem) {
                return (((MenuItemData) firstItem).menuText.compareToIgnoreCase(((MenuItemData) secondItem).menuText));
            }
        });
        return returnArray;





//     
//                Vector childTypes = new Vector();
//        if (targetNodeUserObject instanceof ImdiTreeObject) {
//            String xpath = ImdiSchema.getNodePath((ImdiTreeObject) targetNodeUserObject);
//            childTypes = getSubnodesFromTemplatesDir(xpath); // add the main entries based on the node path of the target
//            if (((ImdiTreeObject) targetNodeUserObject).isCorpus()) { // add any corpus node entries
//                for (String[] currentTemplate : rootTemplatesArray) {
//                    boolean suppressEntry = false;
//                    if (currentTemplate[1].equals("Catalogue")) {
//                        if (((ImdiTreeObject) targetNodeUserObject).hasCatalogue()) {
//                            // make sure the catalogue can only be added once
//                            suppressEntry = true;
//                        }
//                    }
//                    if (!suppressEntry) {
//                        childTypes.add(new String[]{currentTemplate[1], "." + currentTemplate[0].replaceFirst("\\.xml$", "")});
//                    }
//                }
//            }
////            System.out.println("childTypes: " + childTypes);
//        } else {
//            // add the the root node items
//            for (String[] currentTemplate : rootTemplatesArray) {
//                if (!currentTemplate[1].equals("Catalogue")) {// make sure the catalogue can not be added at the root level
//                    childTypes.add(new String[]{"Unattached " + currentTemplate[1], "." + currentTemplate[0].replaceFirst("\\.xml$", "")});
//                }
//            }
//        }
//        Collections.sort(childTypes, new Comparator() {
//
//            public int compare(Object o1, Object o2) {
//                String value1 = ((String[]) o1)[0];
//                String value2 = ((String[]) o2)[0];
//                return value1.compareTo(value2);
//            }
//        });
//        return childTypes.elements();






//    HashSet<String> locationsSet = new HashSet<String>();
//            for (ImdiTreeObject[] currentTreeArray : new ImdiTreeObject[][]{remoteCorpusNodes, localCorpusNodes, localFileNodes, favouriteNodes}) {
//                for (ImdiTreeObject currentLocation : currentTreeArray) {
//                    locationsSet.add(currentLocation.getUrlString());
//                }
//            }
//            if (nodesToAdd != null) {
//                for (ImdiTreeObject currentAddable : nodesToAdd) {
//                    locationsSet.add(currentAddable.getUrlString());
//                }
//            }
//            if (nodesToRemove != null) {
//                for (ImdiTreeObject currentRemoveable : nodesToRemove) {
//                    locationsSet.remove(currentRemoveable.getUrlString());
//                }
//            }
//            Vector<String> locationsList = new Vector<String>(); // this vector is kept for backwards compatability
//            for (String currentLocation : locationsSet) {
//                locationsList.add(URLDecoder.decode(currentLocation, "UTF-8"));
//            }
//            //LinorgSessionStorage.getSingleInstance().saveObject(locationsList, "locationsList");
//            LinorgSessionStorage.getSingleInstance().saveStringArray("locationsList", locationsList.toArray(new String[]{}));
    }

    public String[] getAvailableTemplates() {
        File templatesDir = getTemplateDirectory();
        if (!templatesDir.exists()) {
            templatesDir.mkdir();
        }
        ArrayList<String> templateList = new ArrayList<String>();
        String[] templatesList = templatesDir.list();
        for (String currentTemplateName : templatesList) {
            // if the template file does not exist then remove from the array
            if (getTemplateFile(currentTemplateName).exists()) {
                templateList.add(currentTemplateName);
            }
        }
//        for (String currentTemplateName : builtInTemplates) {
//            // add the Default and SignLanguage built in templates
//            templateList.add(currentTemplateName);
//        }
        templatesList = templateList.toArray(new String[]{});
        Arrays.sort(templatesList);
        return templatesList;
    }

    private ArbilTemplateManager() {
        templatesHashTable = new Hashtable<String, ArbilTemplate>();
//        defaultArbilTemplateName = LinorgSessionStorage.getSingleInstance().loadString("CurrentTemplate");
//        if (defaultArbilTemplateName == null) {
//            defaultArbilTemplateName = builtInTemplates2[0];
//            LinorgSessionStorage.getSingleInstance().saveString("CurrentTemplate", defaultArbilTemplateName);
//        }
    }

    public ArbilTemplate getDefaultTemplate() {
        return getTemplate(builtInTemplates2[0]);
    }

    public ArbilTemplate getCmdiTemplate(String nameSpaceString) {
        if (nameSpaceString != null) {
            CmdiTemplate cmdiTemplate = (CmdiTemplate) templatesHashTable.get(nameSpaceString);
            if (cmdiTemplate == null) {
                cmdiTemplate = new CmdiTemplate();
                cmdiTemplate.loadTemplate(nameSpaceString);
                templatesHashTable.put(nameSpaceString, cmdiTemplate);
            }
            return cmdiTemplate;
        } else {
            GuiHelper.linorgBugCatcher.logError(new Exception("Name space URL not provided, cannot load the CMDI template, please check the XML file and ensure that the name space is specified."));
            return null;
        }
    }

    public ArbilTemplate getTemplate(String templateName) {
        ArbilTemplate returnTemplate = new ArbilTemplate();
        if (templateName == null) {
            templateName = builtInTemplates2[0]; // if the template does not exist the default values will be loaded
        }
        if (!templatesHashTable.containsKey(templateName)) {
//                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Template Not Found: " + templateName, "Arbil Template Manager");
            returnTemplate.readTemplate(getTemplateFile(templateName), templateName);
            templatesHashTable.put(templateName, returnTemplate);
        } else {
            returnTemplate = templatesHashTable.get(templateName);
        }
        return returnTemplate;
    }
}
