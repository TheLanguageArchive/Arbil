package nl.mpi.arbil.ui;

import nl.mpi.arbil.userstorage.LinorgSessionStorage;
import java.util.ArrayList;
import java.util.Arrays;
import nl.mpi.arbil.data.ImdiVocabularies.VocabularyItem;
import nl.mpi.arbil.data.ImdiVocabularies;


/**
 *  Document   : DocumentationLanguages
 *  Created on : Jul 6, 2010, 4:05:46 PM
 *  Author     : Peter Withers
 */
public class DocumentationLanguages {

    private static String languageVocabularyUrl = null;

    public VocabularyItem[] getallLanguages() {
        if (languageVocabularyUrl == null) {
            languageVocabularyUrl = LinorgSessionStorage.getSingleInstance().loadString("languageVocabularyUrl");
            if (languageVocabularyUrl == null || languageVocabularyUrl.equals("http://www.mpi.nl/IMDI/Schema/ISO639-2Languages.xml")) {
                languageVocabularyUrl = "http://www.mpi.nl/IMDI/Schema/MPI-Languages.xml";
                LinorgSessionStorage.getSingleInstance().saveString("LanguageVocabularyUrl", languageVocabularyUrl);
            }
        }
        return ImdiVocabularies.getSingleInstance().getVocabulary(null, languageVocabularyUrl).getVocabularyItems();
    }

    public ArrayList<String> getSelectedLanguagesArrayList() {
        ArrayList<String> selectedLanguages = new ArrayList<String>();
        try {
            selectedLanguages.addAll(Arrays.asList(LinorgSessionStorage.getSingleInstance().loadStringArray("selectedLanguages")));
        } catch (Exception e) {
            GuiHelper.linorgBugCatcher.logError("No selectedLanguages file, will create one now.", e);
            addDefaultTemplates();
        }
        return selectedLanguages;
    }

    public ImdiVocabularies.VocabularyItem[] getLanguageListSubset() {
        ArrayList<VocabularyItem> languageListSubset = new ArrayList<VocabularyItem>();
        ArrayList<String> selectedLanguages = getSelectedLanguagesArrayList();
        for (VocabularyItem currentVocabItem : getallLanguages()) {
            if (selectedLanguages.contains(currentVocabItem.languageName)) {
                languageListSubset.add(currentVocabItem);
            }
        }
        return languageListSubset.toArray(new VocabularyItem[]{});
    }

    private void addDefaultTemplates() {
        for (VocabularyItem currentTemplate : getallLanguages()) {
            addselectedLanguages(currentTemplate.languageName);
        }
    }

    public void addselectedLanguages(String templateString) {
        ArrayList<String> selectedLanguages = new ArrayList<String>();
        try {
            selectedLanguages.addAll(Arrays.asList(LinorgSessionStorage.getSingleInstance().loadStringArray("selectedLanguages")));
        } catch (Exception e) {
            GuiHelper.linorgBugCatcher.logError("No selectedLanguages file, will create one now.", e);
        }
        selectedLanguages.add(templateString);
        LinorgSessionStorage.getSingleInstance().saveStringArray("selectedLanguages", selectedLanguages.toArray(new String[]{}));
    }

    public void removeselectedLanguages(String templateString) {
        ArrayList<String> selectedLanguages = new ArrayList<String>();
        selectedLanguages.addAll(Arrays.asList(LinorgSessionStorage.getSingleInstance().loadStringArray("selectedLanguages")));
        while (selectedLanguages.contains(templateString)) {
            selectedLanguages.remove(templateString);
        }
        LinorgSessionStorage.getSingleInstance().saveStringArray("selectedLanguages", selectedLanguages.toArray(new String[]{}));
    }
}
