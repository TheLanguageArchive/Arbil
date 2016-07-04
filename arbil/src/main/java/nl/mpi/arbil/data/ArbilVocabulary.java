/**
 * Copyright (C) 2016 The Language Archive, Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.arbil.data;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;

public class ArbilVocabulary implements Serializable {

    private final Vector<ArbilVocabularyItem> vocabularyItems = new Vector<ArbilVocabularyItem>();
    private final String vocabularyUrl;

    public ArbilVocabulary(String locationUrl) {
	vocabularyUrl = locationUrl;
    }

    public void addEntry(String entryString, String entryCode) {
	boolean itemExistsInVocab = false;
	for (ArbilVocabularyItem currentVocabularyItem : vocabularyItems.toArray(new ArbilVocabularyItem[]{})) {
	    if (currentVocabularyItem.itemDisplayName.equals(entryString)) {
		itemExistsInVocab = true;
	    }
	}
	if (!itemExistsInVocab) {
	    vocabularyItems.add(new ArbilVocabularyItem(entryString, entryCode, null));
	}
    }

    public ArbilVocabularyItem findVocabularyItem(String itemDisplayName) {
	if (itemDisplayName != null) {
	    for (ArbilVocabularyItem currentVocabularyItem : vocabularyItems.toArray(new ArbilVocabularyItem[]{})) {
		if (itemDisplayName.equals(currentVocabularyItem.itemDisplayName)) {
		    return currentVocabularyItem;
		}
	    }
	}
	return null;
    }

    public ArbilVocabularyItem getVocabularyItemByCode(String itemCode) {
	if (itemCode != null) {
	    for (ArbilVocabularyItem currentVocabularyItem : vocabularyItems.toArray(new ArbilVocabularyItem[]{})) {
		if (itemCode.equals(currentVocabularyItem.itemCode)) {
		    return currentVocabularyItem;
		}
	    }
	}
	return null;
    }

    public String resolveFollowUpUrl(String folowUpString) {
	String vocabUrlDirectory = vocabularyUrl.substring(0, vocabularyUrl.lastIndexOf("/") + 1);
	return (vocabUrlDirectory + folowUpString);
    }

    /**
     * @return the vocabularyItems
     */
    public List<ArbilVocabularyItem> getVocabularyItems() {
	if (filter == null) {
	    return vocabularyItems;
	} else {
	    return filter.filterVocabularyItems(vocabularyItems);
	}
    }

    public List<ArbilVocabularyItem> getVocabularyItemsUnfiltered() {
	return vocabularyItems;
    }

    /**
     * @return the vocabularyUrl
     */
    public String getVocabularyUrl() {
	return vocabularyUrl;
    }

    public void setFilter(ArbilVocabularyFilter filter) {
	this.filter = filter;
    }
    private transient ArbilVocabularyFilter filter = null;
}
