package nl.mpi.arbil.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import nl.mpi.arbil.clarin.profiles.CmdiTemplate;
import nl.mpi.arbil.templates.ArbilTemplateManager;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;

/**
 * Document : ImdiDocumentationLanguages
 * Created on : Jul 6, 2010, 4:05:46 PM
 * Author : Peter Withers
 */
public class CmdiDocumentationLanguages {

    private static SessionStorage sessionStorage;

    public static void setSessionStorage(SessionStorage sessionStorageInstance) {
	sessionStorage = sessionStorageInstance;
    }
    private static final String CMDI_LANGUAGE_VOCABULARY_URL_KEY = "CmdiLanguageVocabularyUrl";
    private static final String CMDI_LANGUAGE_VOCABULARY_PATH_KEY = "CmdiLanguageVocabularyPath";
    private static final String SELECTED_LANGUAGES_KEY = "selectedLanguagesCmdi";
    public static final String CMDI_VOCABULARY_URL = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/components/clarin.eu:cr1:c_1271859438110/xsd";
    public static final String CMDI_VOCABULARY_PATH = ".CMD.Components.ISO639.iso-639-3-code";
    private static String cmdiLanguageVocabularyUrl = null;
    private static String cmdiLanguageVocabularyPath = null;

    public synchronized static String getLanguageVocabularyUrlForCmdi() {
	if (cmdiLanguageVocabularyUrl == null) {
	    cmdiLanguageVocabularyUrl = sessionStorage.loadString(CMDI_LANGUAGE_VOCABULARY_URL_KEY);
	    if (cmdiLanguageVocabularyUrl == null) {
		cmdiLanguageVocabularyUrl = CMDI_VOCABULARY_URL;
		sessionStorage.saveString(CMDI_LANGUAGE_VOCABULARY_URL_KEY, cmdiLanguageVocabularyUrl);
	    }
	}
	return cmdiLanguageVocabularyUrl;
    }

    public synchronized static String getLanguageVocabularyPathForCmdi() {
	if (cmdiLanguageVocabularyPath == null) {
	    cmdiLanguageVocabularyPath = sessionStorage.loadString(CMDI_LANGUAGE_VOCABULARY_PATH_KEY);
	    if (cmdiLanguageVocabularyPath == null) {
		cmdiLanguageVocabularyPath = CMDI_VOCABULARY_PATH;
		sessionStorage.saveString(CMDI_LANGUAGE_VOCABULARY_PATH_KEY, cmdiLanguageVocabularyPath);
	    }
	}
	return cmdiLanguageVocabularyPath;
    }
    private static CmdiDocumentationLanguages singleInstance = null;

    public synchronized static CmdiDocumentationLanguages getSingleInstance() {
	if (singleInstance == null) {
	    singleInstance = new CmdiDocumentationLanguages();
	}
	return singleInstance;
    }

    private CmdiDocumentationLanguages() {
    }

    public synchronized List<ArbilVocabularyItem> getAllLanguagesForCmdi() {
	CmdiTemplate profile = (CmdiTemplate) ArbilTemplateManager.getSingleInstance().getCmdiTemplate(getLanguageVocabularyUrlForCmdi());
	if (profile != null) {
	    ArbilVocabulary vocab = profile.getFieldVocabulary(getLanguageVocabularyPathForCmdi());
	    if (vocab != null) {
		return vocab.getVocabularyItems();
	    }
	}
	return null;
    }

    public List<ArbilVocabularyItem> getLanguageListSubsetForCmdi() {
	// No subset for CMDI yet, selection from dialog only applies to IMDI
	return getAllLanguagesForCmdi();
    }

    public synchronized void addselectedLanguage(String templateString) {
	List<String> selectedLanguages = getSelectedLanguages();
	if (selectedLanguages == null) {
	    selectedLanguages = Collections.singletonList(templateString);
	} else {
	    selectedLanguages.add(templateString);
	}
	saveSelectedLanguages(selectedLanguages);
    }

    private ArrayList<String> getSelectedLanguages() {
	ArrayList<String> selectedLanguages = new ArrayList<String>();
	try {
	    selectedLanguages.addAll(Arrays.asList(sessionStorage.loadStringArray(SELECTED_LANGUAGES_KEY)));
	} catch (Exception e) {
	    BugCatcherManager.getBugCatcher().logError("No selectedLanguages file, will create one now.", e);
	    return null;
	}
	return selectedLanguages;
    }

    public synchronized void removeselectedLanguages(String templateString) {
	ArrayList<String> selectedLanguages = new ArrayList<String>();
	try {
	    selectedLanguages.addAll(Arrays.asList(sessionStorage.loadStringArray(SELECTED_LANGUAGES_KEY)));
	    while (selectedLanguages.contains(templateString)) {
		selectedLanguages.remove(templateString);
	    }
	} catch (IOException ex) {
	    BugCatcherManager.getBugCatcher().logError(ex);
	}
	saveSelectedLanguages(selectedLanguages);
    }

    private void saveSelectedLanguages(List<String> selectedLanguages) {
	try {
	    sessionStorage.saveStringArray(SELECTED_LANGUAGES_KEY, selectedLanguages.toArray(new String[]{}));
	} catch (IOException ex) {
	    BugCatcherManager.getBugCatcher().logError(ex);
	}
    }
}
