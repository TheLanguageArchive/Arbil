package nl.mpi.arbil.templates;

import java.io.IOException;
import nl.mpi.arbil.util.DownloadAbortFlag;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.clarin.profiles.CmdiTemplate;
import nl.mpi.arbil.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;
import javax.swing.ImageIcon;
import nl.mpi.arbil.clarin.profiles.CmdiProfileReader;
import nl.mpi.arbil.clarin.profiles.CmdiProfileReader.CmdiProfile;
import nl.mpi.arbil.data.metadatafile.MetadataReader;
import nl.mpi.arbil.util.BugCatcher;

/**
 * ArbilTemplateManager.java
 * Created on Jul 15, 2009, 11:56:57 AM
 * @author Peter.Withers@mpi.nl
 */
public class ArbilTemplateManager {

    private static BugCatcher bugCatcher;

    public static void setBugCatcher(BugCatcher bugCatcherInstance) {
        bugCatcher = bugCatcherInstance;
    }
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

    /**
     * Create new template of the given name from default
     * @param selectedTemplate Name of the new template. Cannot be empty or equal to the name of a built in template
     * @return File handle to the newly created template file, or null if the request is invalid
     */
    public File createTemplate(String selectedTemplate) {
        if (selectedTemplate.length() == 0) {
            // Template name cannot be null
            return null;
        } else if (Arrays.binarySearch(builtInTemplates2, selectedTemplate) > -1) {
            // Cannot have the name of one of the built in templates
            return null;
        } else {
            File selectedTemplateFile = getTemplateFile(selectedTemplate);
            // Make directory for new template
            selectedTemplateFile.getParentFile().mkdir();
            // Copy template.xml from jar to the new directory
            ArbilSessionStorage.getSingleInstance().saveRemoteResource(MetadataReader.class.getResource("/nl/mpi/arbil/resources/templates/template.xml"), selectedTemplateFile, null, true, new DownloadAbortFlag(), null);
            // Make components directory
            File componentsDirectory = new File(selectedTemplateFile.getParentFile(), "components");
            componentsDirectory.mkdir(); // create the components directory
            // Copy default.xml from jar to components directory
            ArbilSessionStorage.getSingleInstance().saveRemoteResource(MetadataReader.class.getResource("/nl/mpi/arbil/resources/templates/default.xml"), new File(componentsDirectory, "default.xml"), null, true, new DownloadAbortFlag(), null);
            // Make example-components directory
            File examplesDirectory = new File(selectedTemplateFile.getParentFile(), "example-components");
            examplesDirectory.mkdir(); // create the example components directory
            // copy example components from the jar file
            for (String[] pathString : ArbilTemplateManager.getSingleInstance().getTemplate(builtInTemplates2[0]).templatesArray) {
                ArbilSessionStorage.getSingleInstance().saveRemoteResource(MetadataReader.class.getResource("/nl/mpi/arbil/resources/templates/" + pathString[0]), new File(examplesDirectory, pathString[0]), null, true, new DownloadAbortFlag(), null);
            }
            // copy example "format.xsl" from the jar file which is used in the imdi to html conversion
            ArbilSessionStorage.getSingleInstance().saveRemoteResource(MetadataReader.class.getResource("/nl/mpi/arbil/resources/xsl/imdi-viewer.xsl"), new File(selectedTemplateFile.getParentFile(), "example-format.xsl"), null, true, new DownloadAbortFlag(), null);
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

    public File getDefaultComponentOfTemplate(String currentTemplate) {
        File currentTemplateFile = new File(getTemplateDirectory().getAbsolutePath() + File.separatorChar + currentTemplate + File.separatorChar + "components" + File.separatorChar + "default.xml");
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
        return new File(ArbilSessionStorage.getSingleInstance().storageDirectory, "templates");
    }

    public void addSelectedTemplates(String templateString) {
        ArrayList<String> selectedTamplates = new ArrayList<String>();
        try {
            selectedTamplates.addAll(Arrays.asList(loadSelectedTemplates()));
        } catch (Exception e) {
            bugCatcher.logError("No selectedTemplates file, will create one now.", e);
        }
        selectedTamplates.add(templateString);
        try {
            saveSelectedTemplates(selectedTamplates);
        } catch (IOException ex) {
            bugCatcher.logError("Could not crate new selectedTemplates file.", ex);
        }
    }

    private String[] loadSelectedTemplates() throws IOException {
        return ArbilSessionStorage.getSingleInstance().loadStringArray("selectedTemplates");
    }

    private void saveSelectedTemplates(ArrayList<String> selectedTamplates) throws IOException {
        ArbilSessionStorage.getSingleInstance().saveStringArray("selectedTemplates", selectedTamplates.toArray(new String[]{}));
    }

    public void removeSelectedTemplates(String templateString) {
        ArrayList<String> selectedTamplates = new ArrayList<String>();
        try {
            selectedTamplates.addAll(Arrays.asList(loadSelectedTemplates()));
            while (selectedTamplates.contains(templateString)) {
                selectedTamplates.remove(templateString);
            }
            saveSelectedTemplates(selectedTamplates);
        } catch (IOException ex) {
            bugCatcher.logError("Could not load or create selectedTemplates file.", ex);
        }
    }

    public ArrayList<String> getSelectedTemplateArrayList() {
        ArrayList<String> selectedTamplates = new ArrayList<String>();
        try {
            selectedTamplates.addAll(Arrays.asList(loadSelectedTemplates()));
        } catch (Exception e) {
            bugCatcher.logError("No selectedTemplates file, will create one now.", e);
            addDefaultTemplates();
        }
        return selectedTamplates;
    }

    public class MenuItemData {

        public String menuText;
        public String menuAction;
        public String menuToolTip;
        public ImageIcon menuIcon;
    }

    private void addDefaultTemplates() {
        addSelectedTemplates("builtin:METATRANSCRIPT.Corpus.xml");
        addSelectedTemplates("builtin:METATRANSCRIPT.Catalogue.xml");
        addSelectedTemplates("builtin:METATRANSCRIPT.Session.xml");
    }

    private MenuItemData createMenuItemForTemplate(String location) {
        ArbilIcons arbilIcons = ArbilIcons.getSingleInstance();
        MenuItemData menuItem = new MenuItemData();
        if (location.startsWith("builtin:")) {
            String currentString = location.substring("builtin:".length());
            for (String currentTemplateName[] : ArbilTemplateManager.getSingleInstance().getTemplate(null).rootTemplatesArray) {
                if (currentString.equals(currentTemplateName[0])) {
                    menuItem.menuText = currentTemplateName[1];
                    menuItem.menuAction = "." + currentTemplateName[0].replaceFirst("\\.xml$", "");
                    menuItem.menuToolTip = currentTemplateName[1];
                    if (menuItem.menuText.contains("Corpus")) {
                        menuItem.menuIcon = arbilIcons.corpusnodeColorIcon;
                    } else if (menuItem.menuText.contains("Catalogue")) {
                        menuItem.menuIcon = arbilIcons.catalogueColorIcon;
                    } else {
                        menuItem.menuIcon = arbilIcons.sessionColorIcon;
                    }
                }
            }
        } else if (location.startsWith("custom:")) {
            String currentString = location.substring("custom:".length());
            menuItem.menuText = currentString.substring(currentString.lastIndexOf("/") + 1);
            menuItem.menuAction = currentString;
            menuItem.menuToolTip = currentString;
            menuItem.menuIcon = arbilIcons.clarinIcon;
        } else if (location.startsWith("template:")) {
            // todo:
            String currentString = location.substring("template:".length());
            menuItem.menuText = currentString + " (not available)";
            menuItem.menuAction = currentString;
            menuItem.menuToolTip = currentString;
            menuItem.menuIcon = arbilIcons.sessionColorIcon;
        } else if (location.startsWith("clarin:")) {
            String currentString = location.substring("clarin:".length());
            CmdiProfile cmdiProfile = CmdiProfileReader.getSingleInstance().getProfile(currentString);
            if (cmdiProfile == null) {
                menuItem.menuText = "<unknown>";
                menuItem.menuAction = "<unknown>";
                menuItem.menuToolTip = currentString;
            } else {
                menuItem.menuText = cmdiProfile.name;
                menuItem.menuAction = cmdiProfile.getXsdHref();
                menuItem.menuToolTip = cmdiProfile.description;
            }
            menuItem.menuIcon = arbilIcons.clarinIcon;
        }
        return menuItem;
    }

    public MenuItemData[] getSelectedTemplatesMenuItems() {
        String[] locationsArray = null;
        try {
            locationsArray = loadSelectedTemplates();
        } catch (IOException ex) {
            bugCatcher.logError(ex);
        }
        if (locationsArray == null || locationsArray.length == 0) {
            try {
                addDefaultTemplates();
                locationsArray = loadSelectedTemplates();
            } catch (IOException ex) {
                bugCatcher.logError(ex);
            }
        }
        if (locationsArray == null) {
            return new MenuItemData[0];
        }

        MenuItemData[] returnArray = new MenuItemData[locationsArray.length];
        for (int insertableCounter = 0; insertableCounter < locationsArray.length; insertableCounter++) {
            returnArray[insertableCounter] = createMenuItemForTemplate(locationsArray[insertableCounter]);
        }
        Arrays.sort(returnArray, new Comparator() {

            public int compare(Object firstItem, Object secondItem) {
                return (((MenuItemData) firstItem).menuText.compareToIgnoreCase(((MenuItemData) secondItem).menuText));
            }
        });
        return returnArray;
    }

//     
//                Vector childTypes = new Vector();
//        if (targetNodeUserObject instanceof ImdiTreeObject) {
//            String xpath = MetadataReader.getNodePath((ImdiTreeObject) targetNodeUserObject);
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
            bugCatcher.logError(new Exception("Name space URL not provided, cannot load the CMDI template, please check the XML file and ensure that the name space is specified."));
            return null;
        }
    }

    public ArbilTemplate getTemplate(String templateName) {
        ArbilTemplate returnTemplate = new ArbilTemplate();
        if (templateName == null || templateName.length() < 1) {
            return getDefaultTemplate(); // if the template string is not provided the default template is used
        }
        if (!templatesHashTable.containsKey(templateName)) {
//                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Template Not Found: " + templateName, "Arbil Template Manager");
            if (returnTemplate.readTemplate(getTemplateFile(templateName), templateName)) {
                templatesHashTable.put(templateName, returnTemplate);
                return returnTemplate;
            } else {
                return getDefaultTemplate();
            }
        } else {
            return templatesHashTable.get(templateName);
        }
    }
}
