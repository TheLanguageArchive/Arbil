package nl.mpi.arbil.templates;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import javax.swing.ImageIcon;
import nl.mpi.arbil.ArbilIcons;
import nl.mpi.arbil.clarin.profiles.CmdiProfileReader;
import nl.mpi.arbil.clarin.profiles.CmdiProfileReader.CmdiProfile;
import nl.mpi.arbil.data.ArbilEntityResolver;
import nl.mpi.arbil.data.metadatafile.ArbilMetadataReader;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.DownloadAbortFlag;
import nl.mpi.metadata.cmdi.api.CMDIApi;

/**
 * ArbilTemplateManager.java
 * Created on Jul 15, 2009, 11:56:57 AM
 *
 * @author Peter.Withers@mpi.nl
 */
public class ArbilTemplateManager {

    public static final String CLARIN_PREFIX = "clarin:";
    private static SessionStorage sessionStorage;

    public static void setSessionStorage(SessionStorage sessionStorageInstance) {
	sessionStorage = sessionStorageInstance;
    }
    //private String defaultArbilTemplateName;
    static private ArbilTemplateManager singleInstance = null;
    private Hashtable<String, ArbilTemplate> templatesHashTable;
    private String[] builtInTemplates2 = {"Default", "Sign Language"}; // the first item in this list is the default template
    private CMDIApi cmdiApi = new CMDIApi(new ArbilEntityResolver(null));

//    private ImdiTemplate defaultArbilTemplate;
    //public String[] builtInTemplates = {"Corpus Branch (internal)", "Session (internal)", "Catalogue (internal)", "Sign Language (internal)"};
    static synchronized public ArbilTemplateManager getSingleInstance() {
	if (singleInstance == null) {
	    singleInstance = new ArbilTemplateManager();
	}
	return singleInstance;
    }

    /**
     * Create new template of the given name from default
     *
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
	    sessionStorage.saveRemoteResource(ArbilTemplateManager.class.getResource("/nl/mpi/arbil/resources/templates/template.xml"), selectedTemplateFile, null, true, false, new DownloadAbortFlag(), null);
	    // Make components directory
	    File componentsDirectory = new File(selectedTemplateFile.getParentFile(), "components");
	    componentsDirectory.mkdir(); // create the components directory
	    // Copy default.xml from jar to components directory
	    sessionStorage.saveRemoteResource(ArbilTemplateManager.class.getResource("/nl/mpi/arbil/resources/templates/default.xml"), new File(componentsDirectory, "default.xml"), null, true, false, new DownloadAbortFlag(), null);
	    // Make example-components directory
	    File examplesDirectory = new File(selectedTemplateFile.getParentFile(), "example-components");
	    if (!examplesDirectory.mkdir()) { // create the example components directory
		BugCatcherManager.getBugCatcher().logError(new IOException("Could not create example components directory: " + examplesDirectory));
	    }
	    // copy example components from the jar file
	    for (String[] pathString : ArbilTemplateManager.getSingleInstance().getTemplate(builtInTemplates2[0]).getTemplatesArray()) {
		sessionStorage.saveRemoteResource(ArbilTemplateManager.class.getResource("/nl/mpi/arbil/resources/templates/" + pathString[0]), new File(examplesDirectory, pathString[0]), null, true, false, new DownloadAbortFlag(), null);
	    }
	    // copy example "format.xsl" from the jar file which is used in the imdi to html conversion
	    sessionStorage.saveRemoteResource(ArbilTemplateManager.class.getResource("/nl/mpi/arbil/resources/xsl/imdi-viewer.xsl"), new File(selectedTemplateFile.getParentFile(), "example-format.xsl"), null, true, false, new DownloadAbortFlag(), null);
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
	return new File(sessionStorage.getStorageDirectory(), "templates");
    }

    public void addSelectedTemplates(String templateString) {
	ArrayList<String> selectedTemplates = new ArrayList<String>();
	try {
	    selectedTemplates.addAll(Arrays.asList(loadSelectedTemplates()));
	} catch (Exception e) {
	    BugCatcherManager.getBugCatcher().logError("No selectedTemplates file, will create one now.", e);
	}
	if (!selectedTemplates.contains(templateString)) {
	    selectedTemplates.add(templateString);
	}
	try {
	    saveSelectedTemplates(selectedTemplates);
	} catch (IOException ex) {
	    BugCatcherManager.getBugCatcher().logError("Could not crate new selectedTemplates file.", ex);
	}
    }

    private String[] loadSelectedTemplates() throws IOException {
	return sessionStorage.loadStringArray("selectedTemplates");
    }

    private void saveSelectedTemplates(ArrayList<String> selectedTamplates) throws IOException {
	sessionStorage.saveStringArray("selectedTemplates", selectedTamplates.toArray(new String[]{}));
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
	    BugCatcherManager.getBugCatcher().logError("Could not load or create selectedTemplates file.", ex);
	}
    }

    public ArrayList<String> getSelectedTemplateArrayList() {
	ArrayList<String> selectedTamplates = new ArrayList<String>();
	try {
	    selectedTamplates.addAll(Arrays.asList(loadSelectedTemplates()));
	} catch (Exception e) {
	    BugCatcherManager.getBugCatcher().logError("No selectedTemplates file, will create one now.", e);
	    addDefaultImdiTemplates();
	}
	return selectedTamplates;
    }

    public static class MenuItemData {

	public enum Type {

	    IMDI, CMDI, OTHER
	}
	public Type type;
	public String menuText;
	public String menuAction;
	public String menuToolTip;
	public ImageIcon menuIcon;
    }

    public void addDefaultImdiTemplates() {
	addSelectedTemplates("builtin:METATRANSCRIPT.Corpus.xml");
	addSelectedTemplates("builtin:METATRANSCRIPT.Catalogue.xml");
	addSelectedTemplates("builtin:METATRANSCRIPT.Session.xml");
    }

    public void removeDefaultImdiTemplates() {
	removeSelectedTemplates("builtin:METATRANSCRIPT.Corpus.xml");
	removeSelectedTemplates("builtin:METATRANSCRIPT.Catalogue.xml");
	removeSelectedTemplates("builtin:METATRANSCRIPT.Session.xml");
    }

    private MenuItemData createMenuItemForTemplate(String location) {
	ArbilIcons arbilIcons = ArbilIcons.getSingleInstance();
	MenuItemData menuItem = new MenuItemData();
	menuItem.type = MenuItemData.Type.IMDI;
	if (location.startsWith("builtin:")) {
	    String currentString = location.substring("builtin:".length());
	    for (String currentTemplateName[] : ArbilTemplateManager.getSingleInstance().getTemplate(null).getRootTemplatesArray()) {
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
	} else if (location.startsWith("template:")) {
	    String currentString = location.substring("template:".length());
	    menuItem.menuText = currentString;
	    menuItem.menuAction = currentString;
	    menuItem.menuToolTip = currentString;
	    menuItem.menuIcon = arbilIcons.sessionColorIcon;
	} else if (location.startsWith("custom:")) {
	    menuItem.type = MenuItemData.Type.CMDI;

	    String currentString = location.substring("custom:".length());
	    String customName = currentString.replaceAll("[/.]xsd$", "");
	    if (customName.contains("/")) {
		customName = customName.substring(customName.lastIndexOf("/") + 1);
	    }

	    menuItem.menuText = customName;
	    menuItem.menuAction = currentString;
	    menuItem.menuToolTip = currentString;
	    menuItem.menuIcon = arbilIcons.clarinIcon;
	} else if (location.startsWith(CLARIN_PREFIX)) {
	    menuItem.type = MenuItemData.Type.CMDI;

	    String currentString = location.substring(CLARIN_PREFIX.length());
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
	    BugCatcherManager.getBugCatcher().logError(ex);
	}
	if (locationsArray == null || locationsArray.length == 0) {
	    try {
		addDefaultImdiTemplates();
		locationsArray = loadSelectedTemplates();
	    } catch (IOException ex) {
		BugCatcherManager.getBugCatcher().logError(ex);
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
//            String xpath = ArbilMetadataReader.getNodePath((ImdiTreeObject) targetNodeUserObject);
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
	    if (!templatesDir.mkdir()) {
		BugCatcherManager.getBugCatcher().logError(new IOException("Could not create template directory: " + templatesDir));
	    }
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
    private final Map<String, Object> cmdiLoadingState = new HashMap<String, Object>();

    /**
     * Gets a CMDI template by namespace string. Each template gets loaded only once, so callers might end
     * up in a queue.
     *
     * @param nameSpaceString
     * @return
     */
    public ArbilTemplate getCmdiTemplate(String nameSpaceString) throws URISyntaxException {
	if (nameSpaceString != null) {
	    try {
		// Check if template is being loaded and if so retrieve it
		ArbilTemplate template = waitForCmdiTemplateProfileLoading(nameSpaceString);
		// Did we get a loaded CMDI profile template?
		if (template == null) {
		    // Template has not been loaded, do so now
		    template = loadCmdiTemplateProfile(nameSpaceString);
		}
		if (template == null) {
		    BugCatcherManager.getBugCatcher().logError("Could not load CMDI profile with URI " + nameSpaceString, null);
		    return null;
		}
		return template;
	    } catch (InterruptedException iEx) {
		BugCatcherManager.getBugCatcher().logError("Interrupted while waiting for CMDI profile to load", iEx);
		return null;
	    }
	} else {
	    BugCatcherManager.getBugCatcher().logError(new Exception("Name space URL not provided, cannot load the CMDI template, please check the XML file and ensure that the name space is specified."));
	    return null;
	}
    }

    /**
     * Checks if template is being loaded, and if so wait for it to finish loading.
     * If the template has not been loaded and is not being loading, creates a lock object in
     * {@link #cmdiLoadingState}.
     *
     * @param nameSpaceString
     * @return CMDI profile template if it was already loaded. Null if it still needs to be loaded
     * @throws InterruptedException if interrupted while waiting
     */
    private ArbilTemplate waitForCmdiTemplateProfileLoading(String nameSpaceString) throws InterruptedException {
	synchronized (cmdiLoadingState) {
	    // Look for lock object for namespace string
	    while (cmdiLoadingState.containsKey(nameSpaceString)) {
		// Lock object found, wait for loading to finish
		cmdiLoadingState.wait();
	    }
	    // Nothing is loading, see if already loaded
	    ArbilTemplate cmdiTemplate = templatesHashTable.get(nameSpaceString);
	    if (cmdiTemplate != null) {
		// Was already loaded, return
		return cmdiTemplate;
	    } else {
		// Not loading, not loaded. Prepare for loading now.
		cmdiLoadingState.put(nameSpaceString, new Object());
		return null;
	    }
	}
    }

    /**
     * Loads a CMDI profile template and tells others to wait for it.
     * After loading has finished, removes the lock object from {@link #cmdiLoadingState} and notifies
     * all waiting threads.
     *
     * @param nameSpaceString
     * @return Loaded CMDI template
     */
    private ArbilTemplate loadCmdiTemplateProfile(String nameSpaceString) throws URISyntaxException {
	ArbilTemplate cmdiTemplate = new MetadataAPITemplate(cmdiApi, new URI(nameSpaceString));
	if (cmdiTemplate.readTemplate()) {
	    templatesHashTable.put(nameSpaceString, cmdiTemplate);
	} else {
	    cmdiTemplate = null;
	}

	synchronized (cmdiLoadingState) {
	    cmdiLoadingState.remove(nameSpaceString);
	    cmdiLoadingState.notifyAll();
	}

	return cmdiTemplate;
    }

    public ArbilTemplate getTemplate(String templateName) {
	if (templateName == null || templateName.length() < 1) {
	    return getDefaultTemplate(); // if the template string is not provided the default template is used
	}
	if (!templatesHashTable.containsKey(templateName)) {
//                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Template Not Found: " + templateName, "Arbil Template Manager");
	    ImdiTemplate returnTemplate = new ImdiTemplate(getTemplateFile(templateName), templateName);
	    if (returnTemplate.readTemplate()) {
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
