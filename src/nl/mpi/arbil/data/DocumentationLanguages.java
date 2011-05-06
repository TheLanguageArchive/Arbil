package nl.mpi.arbil.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import nl.mpi.arbil.userstorage.ArbilSessionStorage;
import nl.mpi.arbil.util.BugCatcher;

/**
 *  Document   : DocumentationLanguages
 *  Created on : Jul 6, 2010, 4:05:46 PM
 *  Author     : Peter Withers
 */
public class DocumentationLanguages implements ArbilVocabularyFilter {

    private static BugCatcher bugCatcher;

    public static void setBugCatcher(BugCatcher bugCatcherInstance) {
        bugCatcher = bugCatcherInstance;
    }
    private static final String LANGUAGE_VOCABULARY_URL_KEY = "LanguageVocabularyUrl";
    private static final String SELECTED_LANGUAGES_KEY = "selectedLanguages";
    private static final String OLD_MPI_LANGUAGE_VOCABULARY_URL = "http://www.mpi.nl/IMDI/Schema/ISO639-2Languages.xml";
    private static final String MPI_LANGUAGE_VOCABULARY_URL = "http://www.mpi.nl/IMDI/Schema/MPI-Languages.xml";
    private static String languageVocabularyUrl = null;

    public synchronized static String getLanguageVocabularyUrl() {
        if (languageVocabularyUrl == null) {
            languageVocabularyUrl = ArbilSessionStorage.getSingleInstance().loadString(LANGUAGE_VOCABULARY_URL_KEY);
            if (languageVocabularyUrl == null || languageVocabularyUrl.equals(OLD_MPI_LANGUAGE_VOCABULARY_URL)) {
                languageVocabularyUrl = MPI_LANGUAGE_VOCABULARY_URL;
                ArbilSessionStorage.getSingleInstance().saveString(LANGUAGE_VOCABULARY_URL_KEY, languageVocabularyUrl);
            }
        }
        return languageVocabularyUrl;
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

    public synchronized List<ArbilVocabularyItem> getallLanguages() {
        return ArbilVocabularies.getSingleInstance().getVocabulary(null, getLanguageVocabularyUrl()).getVocabularyItemsUnfiltered();
    }

    public synchronized ArrayList<String> getSelectedLanguagesArrayList() {
        ArrayList<String> selectedLanguages = new ArrayList<String>();
        try {
            selectedLanguages.addAll(Arrays.asList(ArbilSessionStorage.getSingleInstance().loadStringArray(SELECTED_LANGUAGES_KEY)));
        } catch (Exception e) {
            bugCatcher.logError("No selectedLanguages file, will create one now.", e);
            addDefaultTemplates();
        }
        return selectedLanguages;
    }

    public synchronized List<ArbilVocabularyItem> getLanguageListSubset() {
        ArrayList<ArbilVocabularyItem> languageListSubset = new ArrayList<ArbilVocabularyItem>();
        ArrayList<String> selectedLanguages = getSelectedLanguagesArrayList();
        for (ArbilVocabularyItem currentVocabItem : getallLanguages()) {
            if (selectedLanguages.contains(currentVocabItem.itemDisplayName)) {
                languageListSubset.add(currentVocabItem);
            }
        }
        return languageListSubset;//.toArray(new ArbilVocabularyItem[]{});
    }

    private void addDefaultTemplates() {
        for (ArbilVocabularyItem currentTemplate : getallLanguages()) {
            addselectedLanguages(currentTemplate.itemDisplayName);
        }
    }

    public synchronized void addselectedLanguages(String templateString) {
        ArrayList<String> selectedLanguages = new ArrayList<String>();
        try {
            selectedLanguages.addAll(Arrays.asList(ArbilSessionStorage.getSingleInstance().loadStringArray(SELECTED_LANGUAGES_KEY)));
        } catch (Exception e) {
            bugCatcher.logError("No selectedLanguages file, will create one now.", e);
        }
        selectedLanguages.add(templateString);
        saveSelectedLanguages(selectedLanguages);
    }

    public synchronized void removeselectedLanguages(String templateString) {
        ArrayList<String> selectedLanguages = new ArrayList<String>();
        try {
            selectedLanguages.addAll(Arrays.asList(ArbilSessionStorage.getSingleInstance().loadStringArray(SELECTED_LANGUAGES_KEY)));
            while (selectedLanguages.contains(templateString)) {
                selectedLanguages.remove(templateString);
            }
        } catch (IOException ex) {
            bugCatcher.logError(ex);
        }
        saveSelectedLanguages(selectedLanguages);
    }

    private void saveSelectedLanguages(ArrayList<String> selectedLanguages) {
        try {
            ArbilSessionStorage.getSingleInstance().saveStringArray(SELECTED_LANGUAGES_KEY, selectedLanguages.toArray(new String[]{}));
        } catch (IOException ex) {
            bugCatcher.logError(ex);
        }
    }



    public List<ArbilVocabularyItem> filterVocabularyItems(List<ArbilVocabularyItem> items) {
        List<ArbilVocabularyItem> vocabClone = new ArrayList<ArbilVocabularyItem>(items);
        vocabClone.retainAll(getLanguageListSubset());
        return vocabClone;
    }
}
