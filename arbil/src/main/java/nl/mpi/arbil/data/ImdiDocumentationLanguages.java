package nl.mpi.arbil.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;

/**
 * Document : ImdiDocumentationLanguages
 * Created on : Jul 6, 2010, 4:05:46 PM
 * Author : Peter Withers
 */
public class ImdiDocumentationLanguages extends DocumentationLanguages implements ArbilVocabularyFilter {

    private static SessionStorage sessionStorage;

    public static void setSessionStorage(SessionStorage sessionStorageInstance) {
	sessionStorage = sessionStorageInstance;
    }
    private static final String IMDI_LANGUAGE_VOCABULARY_URL_KEY = "LanguageVocabularyUrl";
    private static final String SELECTED_LANGUAGES_KEY = "selectedLanguages";
    private static final String OLD_MPI_LANGUAGE_VOCABULARY_URL = "http://www.mpi.nl/IMDI/Schema/ISO639-2Languages.xml";
    private static final String MPI_LANGUAGE_VOCABULARY_URL = "http://www.mpi.nl/IMDI/Schema/MPI-Languages.xml";
    private static String imdiLanguageVocabularyUrl = null;

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
    private static ImdiDocumentationLanguages singleInstance = null;

    public synchronized static ImdiDocumentationLanguages getSingleInstance() {
	if (singleInstance == null) {
	    singleInstance = new ImdiDocumentationLanguages(SELECTED_LANGUAGES_KEY, sessionStorage);
	}
	return singleInstance;
    }

    private ImdiDocumentationLanguages(String selectedLanguagesKey, SessionStorage sessionStorage) {
	super(selectedLanguagesKey, sessionStorage);
    }

    public synchronized List<ArbilVocabularyItem> getAllLanguages() {
	return ArbilVocabularies.getSingleInstance().getVocabulary(null, getLanguageVocabularyUrlForImdi()).getVocabularyItemsUnfiltered();
    }

    public List<ArbilVocabularyItem> getLanguageListSubset() {
	return getLanguageListSubset(getAllLanguages());
    }

    public List<ArbilVocabularyItem> filterVocabularyItems(List<ArbilVocabularyItem> items) {
	List<ArbilVocabularyItem> vocabClone = new ArrayList<ArbilVocabularyItem>(items);
	vocabClone.retainAll(getLanguageListSubset());
	return vocabClone;
    }
}
