package nl.mpi.arbil.templates;

import nl.mpi.arbil.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import nl.mpi.arbil.data.ImdiSchema;

/**
 * ArbilTemplateManager.java
 * Created on Jul 15, 2009, 11:56:57 AM
 * @author Peter.Withers@mpi.nl
 */
public class ArbilTemplateManager {

    private String defaultArbilTemplateName;
    static private ArbilTemplateManager singleInstance = null;
    private Hashtable<String, ArbilTemplate> templatesHashTable;
//    private String[] builtInTemplates = {"Default", "Sign Language"}; // the first item in this list is the default template
//    private ArbilTemplate defaultArbilTemplate;
    public String[] builtInTemplates = {"Corpus Branch (internal)", "Session (internal)", "Catalogue (internal)", "Sign Language (internal)"};

    static synchronized public ArbilTemplateManager getSingleInstance() {
        if (singleInstance == null) {
            singleInstance = new ArbilTemplateManager();
        }
        return singleInstance;
    }

    public File createTemplate(String selectedTemplate) {
        if (selectedTemplate.length() == 0) {
            return null;
        } else if (Arrays.binarySearch(builtInTemplates, selectedTemplate) > -1) {
            return null;
        } else {
            File selectedTemplateFile = getTemplateFile(selectedTemplate);
            selectedTemplateFile.getParentFile().mkdir();
            LinorgSessionStorage.getSingleInstance().saveRemoteResource(ImdiSchema.class.getResource("/nl/mpi/arbil/resources/templates/template.xml"), selectedTemplateFile, null, true, new DownloadAbortFlag());
            new File(selectedTemplateFile.getParentFile(), "components").mkdir(); // create the components directory
            File examplesDirectory = new File(selectedTemplateFile.getParentFile(), "example-components");
            examplesDirectory.mkdir(); // create the example components directory
            // copy example components from the jar file            
            for (String[] pathString : ArbilTemplateManager.getSingleInstance().getTemplate(builtInTemplates[0]).templatesArray) {
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

    public boolean defaultTemplateIsCurrentTemplate() {
        return defaultArbilTemplateName.equals(builtInTemplates[0]);
    }

    public String getCurrentTemplateName() {
        return defaultArbilTemplateName;
    }

    public void setCurrentTemplate(String currentTemplateLocal) {
        defaultArbilTemplateName = currentTemplateLocal;
        try {
            LinorgSessionStorage.getSingleInstance().saveString("CurrentTemplate", currentTemplateLocal);
        } catch (Exception ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
        }
    }

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

    public String[][] getSelectedTemplates() {
        String[] locationsArray = LinorgSessionStorage.getSingleInstance().loadStringArray("selectedTemplates");
        String[][] returnArray = new String[locationsArray.length][3];
        for (int insertableCounter = 0; insertableCounter < locationsArray.length; insertableCounter++) {
            returnArray[insertableCounter][0] = locationsArray[insertableCounter] + "a";
            returnArray[insertableCounter][1] = locationsArray[insertableCounter] + "b";
            returnArray[insertableCounter][2] = locationsArray[insertableCounter] + "c";
        }
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
        for (String currentTemplateName : builtInTemplates) {
            // add the Default and SignLanguage built in templates
            templateList.add(currentTemplateName);
        }
        templatesList = templateList.toArray(new String[]{});
        Arrays.sort(templatesList);
        return templatesList;
    }

    private ArbilTemplateManager() {
        templatesHashTable = new Hashtable<String, ArbilTemplate>();
        defaultArbilTemplateName = LinorgSessionStorage.getSingleInstance().loadString("CurrentTemplate");
        if (defaultArbilTemplateName == null) {
            defaultArbilTemplateName = builtInTemplates[0];
            LinorgSessionStorage.getSingleInstance().saveString("CurrentTemplate", defaultArbilTemplateName);
        }
    }

    public ArbilTemplate getCurrentTemplate() {
        return getTemplate(defaultArbilTemplateName);
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
            templateName = builtInTemplates[0]; // if the template does not exist the default values will be loaded
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
