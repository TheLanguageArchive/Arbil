package nl.mpi.arbil.templates;

import nl.mpi.arbil.*;
import nl.mpi.arbil.templates.ArbilTemplate;
import nl.mpi.arbil.templates.CmdiTemplate;
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
    private String[] builtInTemplates = {"Default", "Sign Language"}; // the first item in this list is the default template
//    private ArbilTemplate defaultArbilTemplate;

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
            // todo: copy example components from the jar file
            // todo: copy example "format.xsl" from the jar file which is used in the imdi to html conversion
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
