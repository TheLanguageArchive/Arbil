/**
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package nl.mpi.arbil.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import nl.mpi.arbil.userstorage.SessionStorage;
import nl.mpi.arbil.util.BugCatcher;

/**
 *
 * @author Peter Withers
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class DocumentationLanguages {

    private final String selectedLanguagesKey;
    
    
    private static BugCatcher bugCatcher;

    public static void setBugCatcher(BugCatcher bugCatcherInstance) {
	bugCatcher = bugCatcherInstance;
    }
    
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
	    bugCatcher.logError("No selectedLanguages file, will create one now.", e);
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
	    bugCatcher.logError("No selectedLanguages file, will create one now.", e);
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
	    bugCatcher.logError(ex);
	}
	saveSelectedLanguages(selectedLanguages);
    }

    protected void saveSelectedLanguages(List<String> selectedLanguages) {
	try {
	    sessionStorage.saveStringArray(selectedLanguagesKey, selectedLanguages.toArray(new String[]{}));
	} catch (IOException ex) {
	    bugCatcher.logError(ex);
	}
    }
}
