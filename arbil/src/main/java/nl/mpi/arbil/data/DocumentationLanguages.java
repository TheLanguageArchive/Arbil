package nl.mpi.arbil.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcherManager;

/**
 *
 * @author Peter Withers
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class DocumentationLanguages {

    private final String selectedLanguagesKey;
    private final SessionStorage sessionStorage;

    public DocumentationLanguages(String selectedLanguagesKey, SessionStorage sessionStorage) {
	this.selectedLanguagesKey = selectedLanguagesKey;
	this.sessionStorage = sessionStorage;
    }

    protected List<String> addDefaultLanguages() {
	final List<ArbilVocabularyItem> imdiLanguages = getAllLanguages();
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

    public synchronized void addselectedLanguage(String templateString) {
	List<String> selectedLanguages = getSelectedLanguages();
	if (selectedLanguages == null) {
	    selectedLanguages = Collections.singletonList(templateString);
	} else {
	    selectedLanguages.add(templateString);
	}
	saveSelectedLanguages(selectedLanguages);
    }

    public abstract List<ArbilVocabularyItem> getAllLanguages();

    protected synchronized List<ArbilVocabularyItem> getLanguageListSubset(List<ArbilVocabularyItem> allLanguages) {
	List<ArbilVocabularyItem> languageListSubset = new ArrayList<ArbilVocabularyItem>();
	if (allLanguages != null) {
	    List<String> selectedLanguages = getSelectedLanguagesArrayList();
	    for (ArbilVocabularyItem currentVocabItem : allLanguages) {
		if (selectedLanguages.contains(currentVocabItem.itemDisplayName)) {
		    languageListSubset.add(currentVocabItem);
		}
	    }
	}
	return languageListSubset; //.toArray(new ArbilVocabularyItem[]{});
	//.toArray(new ArbilVocabularyItem[]{});
    }

    /**
     *
     * @return Sorted selection of documentation languages
     */
    public abstract List<ArbilVocabularyItem> getSortedLanguageListSubset();

    protected ArrayList<String> getSelectedLanguages() {
	ArrayList<String> selectedLanguages = new ArrayList<String>();
	try {
	    selectedLanguages.addAll(Arrays.asList(sessionStorage.loadStringArray(selectedLanguagesKey)));
	} catch (Exception e) {
	    BugCatcherManager.getBugCatcher().logError("No selectedLanguages file, will create one now.", e);
	    return null;
	}
	return selectedLanguages;
    }

    public synchronized List<String> getSelectedLanguagesArrayList() {
	ArrayList<String> selectedLanguages = new ArrayList<String>();
	try {
	    selectedLanguages.addAll(Arrays.asList(sessionStorage.loadStringArray(selectedLanguagesKey)));
	    return selectedLanguages;
	} catch (Exception e) {
	    BugCatcherManager.getBugCatcher().logError("No selectedLanguages file, will create one now.", e);
	    return addDefaultLanguages();
	}
    }

    public synchronized void removeselectedLanguages(String templateString) {
	ArrayList<String> selectedLanguages = new ArrayList<String>();
	try {
	    selectedLanguages.addAll(Arrays.asList(sessionStorage.loadStringArray(selectedLanguagesKey)));
	    while (selectedLanguages.contains(templateString)) {
		selectedLanguages.remove(templateString);
	    }
	} catch (IOException ex) {
	    BugCatcherManager.getBugCatcher().logError(ex);
	}
	saveSelectedLanguages(selectedLanguages);
    }

    protected void saveSelectedLanguages(List<String> selectedLanguages) {
	try {
	    sessionStorage.saveStringArray(selectedLanguagesKey, selectedLanguages.toArray(new String[]{}));
	} catch (IOException ex) {
	    BugCatcherManager.getBugCatcher().logError(ex);
	}
    }
}
