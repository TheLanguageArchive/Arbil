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
 * Document : DocumentationLanguages
 * Created on : Jul 6, 2010, 4:05:46 PM
 * Author : Peter Withers
 */
public class DocumentationLanguages implements ArbilVocabularyFilter {

    private static SessionStorage sessionStorage;

    public static void setSessionStorage(SessionStorage sessionStorageInstance) {
	sessionStorage = sessionStorageInstance;
    }
    private static final String IMDI_LANGUAGE_VOCABULARY_URL_KEY = "LanguageVocabularyUrl";
    private static final String CMDI_LANGUAGE_VOCABULARY_URL_KEY = "CmdiLanguageVocabularyUrl";
    private static final String CMDI_LANGUAGE_VOCABULARY_PATH_KEY = "CmdiLanguageVocabularyPath";
    private static final String SELECTED_LANGUAGES_KEY = "selectedLanguages";
    private static final String OLD_MPI_LANGUAGE_VOCABULARY_URL = "http://www.mpi.nl/IMDI/Schema/ISO639-2Languages.xml";
    private static final String MPI_LANGUAGE_VOCABULARY_URL = "http://www.mpi.nl/IMDI/Schema/MPI-Languages.xml";
    public static final String CMDI_VOCABULARY_URL = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/components/clarin.eu:cr1:c_1271859438110/xsd";
    public static final String CMDI_VOCABULARY_PATH = ".CMD.Components.ISO639.iso-639-3-code";
    private static String imdiLanguageVocabularyUrl = null;
    private static String cmdiLanguageVocabularyUrl = null;
    private static String cmdiLanguageVocabularyPath = null;

    public synchronized static String getLanguageVocabularyUrlForImdi() {
	if (imdiLanguageVocabularyUrl == null) {
	    imdiLanguageVocabularyUrl = sessionStorage.loadString(IMDI_LANGUAGE_VOCABULARY_URL_KEY);
	    if (imdiLanguageVocabularyUrl == null || imdiLanguageVocabularyUrl.equals(OLD_MPI_LANGUAGE_VOCABULARY_URL)) {
		imdiLanguageVocabularyUrl = MPI_LANGUAGE_VOCABULARY_URL;
		sessionStorage.saveString(IMDI_LANGUAGE_VOCABULARY_URL_KEY, imdiLanguageVocabularyUrl);
	    }
	}
	return imdiLanguageVocabularyUrl;
    }

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
    private static DocumentationLanguages singleInstance = null;

    public synchronized static DocumentationLanguages getSingleInstance() {
	if (singleInstance == null) {
	    singleInstance = new DocumentationLanguages();
	}
	return singleInstance;
    }

    private DocumentationLanguages() {
    }

    public synchronized List<ArbilVocabularyItem> getAllLanguagesForImdi() {
	return ArbilVocabularies.getSingleInstance().getVocabulary(null, getLanguageVocabularyUrlForImdi()).getVocabularyItemsUnfiltered();
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

    public synchronized List<String> getSelectedLanguagesArrayList() {
	ArrayList<String> selectedLanguages = new ArrayList<String>();
	try {
	    selectedLanguages.addAll(Arrays.asList(sessionStorage.loadStringArray(SELECTED_LANGUAGES_KEY)));
	    return selectedLanguages;
	} catch (Exception e) {
	    BugCatcherManager.getBugCatcher().logError("No selectedLanguages file, will create one now.", e);
	    return addDefaultLanguages();
	}
    }

    public List<ArbilVocabularyItem> getLanguageListSubsetForCmdi() {
	// No subset for CMDI yet, selection from dialog only applies to IMDI
	return getAllLanguagesForCmdi();
    }

    public List<ArbilVocabularyItem> getLanguageListSubsetForImdi() {
	return getLanguageListSubset(getAllLanguagesForImdi());
    }

    private synchronized List<ArbilVocabularyItem> getLanguageListSubset(List<ArbilVocabularyItem> allLanguages) {
	List<ArbilVocabularyItem> languageListSubset = new ArrayList<ArbilVocabularyItem>();
	if (allLanguages != null) {
	    List<String> selectedLanguages = getSelectedLanguagesArrayList();
	    for (ArbilVocabularyItem currentVocabItem : allLanguages) {
		if (selectedLanguages.contains(currentVocabItem.itemDisplayName)) {
		    languageListSubset.add(currentVocabItem);
		}
	    }
	}
	return languageListSubset;//.toArray(new ArbilVocabularyItem[]{});
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

    private List<String> addDefaultLanguages() {
	final List<ArbilVocabularyItem> imdiLanguages = getAllLanguagesForImdi();

	List<String> selectedLanguages = getSelectedLanguages();
	if (selectedLanguages == null) {
	    selectedLanguages = new ArrayList<String>(imdiLanguages.size());
	}

	for (ArbilVocabularyItem currentTemplate : imdiLanguages) {
	    selectedLanguages.add(currentTemplate.itemDisplayName);
	}
	saveSelectedLanguages(selectedLanguages);
	return getSelectedLanguages();
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

    public List<ArbilVocabularyItem> filterVocabularyItems(List<ArbilVocabularyItem> items) {
	List<ArbilVocabularyItem> vocabClone = new ArrayList<ArbilVocabularyItem>(items);
	vocabClone.retainAll(getLanguageListSubsetForImdi());
	return vocabClone;
    }
}
