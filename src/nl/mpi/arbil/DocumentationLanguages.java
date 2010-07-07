package nl.mpi.arbil;

import java.util.ArrayList;
import java.util.Arrays;
import nl.mpi.arbil.ImdiVocabularies.VocabularyItem;

/**
 *  Document   : DocumentationLanguages
 *  Created on : Jul 6, 2010, 4:05:46 PM
 *  Author     : Peter Withers
 */
public class DocumentationLanguages {

    public VocabularyItem[] getallLanguages() {
        return ImdiVocabularies.getSingleInstance().getVocabulary(null, "http://www.mpi.nl/IMDI/Schema/ISO639-2Languages.xml").getVocabularyItems();
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
