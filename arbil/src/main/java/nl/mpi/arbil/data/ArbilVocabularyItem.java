/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.arbil.data;

import java.io.Serializable;

public class ArbilVocabularyItem implements Comparable, Serializable {

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

    public int hashCode() {
        return this.toString().hashCode();
    }

    public boolean equals(Object otherObject) {
        if (otherObject instanceof ArbilVocabularyItem) {
            return this.toString().equals(otherObject.toString());
        }
        return false;
    }

    public int compareTo(Object otherObject) {
        return this.toString().compareTo(otherObject.toString());
    }
}
