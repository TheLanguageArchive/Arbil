/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.arbil.data;

public class ArbilVocabularyItem implements Comparable {

    public String itemDisplayName;
    public String itemCode;
    public String followUpVocabulary;
    public String descriptionString;

    public ArbilVocabularyItem(String languageNameLocal, String languageCodeLocal, String followUpVocabularyLocal) {
        itemDisplayName = languageNameLocal;
        itemCode = languageCodeLocal;
        followUpVocabulary = followUpVocabularyLocal;
    }

    @Override
    public String toString() {
        return itemDisplayName;
    }

    public int compareTo(Object otherObject) {
        return this.toString().compareTo(otherObject.toString());
    }
}
