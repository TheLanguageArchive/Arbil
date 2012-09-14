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
public class CmdiDocumentationLanguages extends DocumentationLanguages {

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

    public synchronized static String getLanguageVocabularyUrl() {
	if (cmdiLanguageVocabularyUrl == null) {
	    cmdiLanguageVocabularyUrl = sessionStorage.loadString(CMDI_LANGUAGE_VOCABULARY_URL_KEY);
	    if (cmdiLanguageVocabularyUrl == null) {
		cmdiLanguageVocabularyUrl = CMDI_VOCABULARY_URL;
		sessionStorage.saveString(CMDI_LANGUAGE_VOCABULARY_URL_KEY, cmdiLanguageVocabularyUrl);
	    }
	}
	return cmdiLanguageVocabularyUrl;
    }

    public synchronized static String getLanguageVocabularyPath() {
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
	    singleInstance = new CmdiDocumentationLanguages(SELECTED_LANGUAGES_KEY, sessionStorage);
	}
	return singleInstance;
    }

    private CmdiDocumentationLanguages(String selectedLanguagesKey, SessionStorage sessionStorage) {
	super(selectedLanguagesKey, sessionStorage);
    }

    public synchronized List<ArbilVocabularyItem> getAllLanguages() {
	CmdiTemplate profile = (CmdiTemplate) ArbilTemplateManager.getSingleInstance().getCmdiTemplate(getLanguageVocabularyUrl());
	if (profile != null) {
	    ArbilVocabulary vocab = profile.getFieldVocabulary(getLanguageVocabularyPath());
	    if (vocab != null) {
		return vocab.getVocabularyItems();
	    }
	}
	return null;
    }

    public List<ArbilVocabularyItem> getLanguageListSubset() {
	// No subset for CMDI yet, selection from dialog only applies to IMDI
	return getAllLanguages();
    }
}
