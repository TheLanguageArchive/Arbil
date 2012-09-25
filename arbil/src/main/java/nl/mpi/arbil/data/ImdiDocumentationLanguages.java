package nl.mpi.arbil.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import nl.mpi.arbil.userstorage.SessionStorage;

/**
 * Document : ImdiDocumentationLanguages
 * Created on : Jul 6, 2010, 4:05:46 PM
 *
 * @author Peter Withers
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ImdiDocumentationLanguages extends DocumentationLanguages implements ArbilVocabularyFilter {

    private SessionStorage sessionStorage;
    private static final String IMDI_LANGUAGE_VOCABULARY_URL_KEY = "LanguageVocabularyUrl";
    private static final String SELECTED_LANGUAGES_KEY = "selectedLanguages";
    private static final String OLD_MPI_LANGUAGE_VOCABULARY_URL = "http://www.mpi.nl/IMDI/Schema/ISO639-2Languages.xml";
    private static final String MPI_LANGUAGE_VOCABULARY_URL = "http://www.mpi.nl/IMDI/Schema/MPI-Languages.xml";
    private static String imdiLanguageVocabularyUrl = null;

    public synchronized String getLanguageVocabularyUrlForImdi() {
	if (imdiLanguageVocabularyUrl == null) {
	    imdiLanguageVocabularyUrl = sessionStorage.loadString(IMDI_LANGUAGE_VOCABULARY_URL_KEY);
	    if (imdiLanguageVocabularyUrl == null || imdiLanguageVocabularyUrl.equals(OLD_MPI_LANGUAGE_VOCABULARY_URL)) {
		imdiLanguageVocabularyUrl = MPI_LANGUAGE_VOCABULARY_URL;
		sessionStorage.saveString(IMDI_LANGUAGE_VOCABULARY_URL_KEY, imdiLanguageVocabularyUrl);
	    }
	}
	return imdiLanguageVocabularyUrl;
    }

    public ImdiDocumentationLanguages(SessionStorage sessionStorage) {
	super(SELECTED_LANGUAGES_KEY, sessionStorage);
	this.sessionStorage = sessionStorage;
    }

    public synchronized List<ArbilVocabularyItem> getAllLanguages() {
	return ArbilVocabularies.getSingleInstance().getVocabulary(null, getLanguageVocabularyUrlForImdi()).getVocabularyItemsUnfiltered();
    }

    public List<ArbilVocabularyItem> getSortedLanguageListSubset() {
	//TODO: Do this sorting only when required (i.e. cache sorted list)
	List<ArbilVocabularyItem> languages = getLanguageListSubset(getAllLanguages());
	Collections.sort(languages);
	return languages;
    }

    public List<ArbilVocabularyItem> filterVocabularyItems(List<ArbilVocabularyItem> items) {
	List<ArbilVocabularyItem> vocabClone = new ArrayList<ArbilVocabularyItem>(items);
	vocabClone.retainAll(getSortedLanguageListSubset());
	return vocabClone;
    }
}
