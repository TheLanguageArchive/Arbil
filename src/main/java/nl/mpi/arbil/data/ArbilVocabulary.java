package nl.mpi.arbil.data;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;

public class ArbilVocabulary implements Serializable {

    private Vector<ArbilVocabularyItem> vocabularyItems = new Vector<ArbilVocabularyItem>();
//        this VocabularyRedirect code has been replaced by the templates
//        public String vocabularyRedirectField = null; // the sibling imdi field that changes this vocabularies location
    private String vocabularyUrl = null;

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

    public ArbilVocabularyItem findVocabularyItem(String searchString) {
        for (ArbilVocabularyItem currentVocabularyItem : vocabularyItems.toArray(new ArbilVocabularyItem[]{})) {
            if (currentVocabularyItem.itemDisplayName.equals(searchString)) {
                return currentVocabularyItem;
            }
        }
        return null;
    }

    public String resolveFollowUpUrl(String folowUpString) {
        String vocabUrlDirectory = vocabularyUrl.substring(0, vocabularyUrl.lastIndexOf("/") + 1);
        System.out.println("vocabUrlDirectory: " + vocabUrlDirectory);
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

    public List<ArbilVocabularyItem> getVocabularyItemsUnfiltered(){
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