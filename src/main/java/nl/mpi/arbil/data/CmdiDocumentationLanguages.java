package nl.mpi.arbil.data;

import java.util.Collections;
import java.util.List;
import nl.mpi.arbil.clarin.profiles.CmdiTemplate;
import nl.mpi.arbil.templates.ArbilTemplateManager;
import nl.mpi.arbil.userstorage.SessionStorage;

/**
 * Document : CmdiDocumentationLanguages
 * Created on : Jul 6, 2010, 4:05:46 PM
 *
 * @author Peter Withers
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class CmdiDocumentationLanguages extends DocumentationLanguages {

    private final SessionStorage sessionStorage;
    private static final String CMDI_LANGUAGE_VOCABULARY_URL_KEY = "CmdiLanguageVocabularyUrl";
    private static final String CMDI_LANGUAGE_VOCABULARY_PATH_KEY = "CmdiLanguageVocabularyPath";
    private static final String SELECTED_LANGUAGES_KEY = "selectedLanguagesCmdi";
    public static final String CMDI_VOCABULARY_URL = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/components/clarin.eu:cr1:c_1271859438110/xsd";
    public static final String CMDI_VOCABULARY_PATH = ".CMD.Components.ISO639.iso-639-3-code";
    private static String cmdiLanguageVocabularyUrl = null;
    private static String cmdiLanguageVocabularyPath = null;
    private List<ArbilVocabularyItem> languages;
    private List<ArbilVocabularyItem> sortedLanguages;

    public synchronized String getLanguageVocabularyUrl() {
	if (cmdiLanguageVocabularyUrl == null) {
	    cmdiLanguageVocabularyUrl = sessionStorage.loadString(CMDI_LANGUAGE_VOCABULARY_URL_KEY);
	    if (cmdiLanguageVocabularyUrl == null) {
		cmdiLanguageVocabularyUrl = CMDI_VOCABULARY_URL;
		sessionStorage.saveString(CMDI_LANGUAGE_VOCABULARY_URL_KEY, cmdiLanguageVocabularyUrl);
	    }
	}
	return cmdiLanguageVocabularyUrl;
    }

    public synchronized String getLanguageVocabularyPath() {
	if (cmdiLanguageVocabularyPath == null) {
	    cmdiLanguageVocabularyPath = sessionStorage.loadString(CMDI_LANGUAGE_VOCABULARY_PATH_KEY);
	    if (cmdiLanguageVocabularyPath == null) {
		cmdiLanguageVocabularyPath = CMDI_VOCABULARY_PATH;
		sessionStorage.saveString(CMDI_LANGUAGE_VOCABULARY_PATH_KEY, cmdiLanguageVocabularyPath);
	    }
	}
	return cmdiLanguageVocabularyPath;
    }

    public CmdiDocumentationLanguages(final SessionStorage sessionStorage) {
	super(SELECTED_LANGUAGES_KEY, sessionStorage);
	this.sessionStorage = sessionStorage;
    }

    public synchronized List<ArbilVocabularyItem> getAllLanguages() {
	// Assumption is that language list doesn't change
	if (languages == null) {
	    languages = getLanguages();
	}
	return languages;
    }

    private synchronized List<ArbilVocabularyItem> getLanguages() {
	CmdiTemplate profile = (CmdiTemplate) ArbilTemplateManager.getSingleInstance().getCmdiTemplate(getLanguageVocabularyUrl());
	if (profile != null) {
	    ArbilVocabulary vocab = profile.getFieldVocabulary(getLanguageVocabularyPath());
	    if (vocab != null) {
		return vocab.getVocabularyItems();
	    }
	}
	return null;
    }

    public synchronized List<ArbilVocabularyItem> getSortedLanguageListSubset() {
	// No subset for CMDI yet, selection from dialog only applies to IMDI
	if (sortedLanguages == null) {
	    // TODO: Subset here as soon as feature (language selection dialog for CMDI) available
	    sortedLanguages = getAllLanguages();
	    if (sortedLanguages != null) {
		Collections.sort(sortedLanguages);
	    }
	}
	return sortedLanguages;
    }
}
