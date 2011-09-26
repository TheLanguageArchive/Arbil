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

    /**
     * 
     * @return Value for vocabulary item: itemCode if it is set, otherwise display name
     */
    public String getValue() {
	if (itemCode != null) {
	    return itemCode;
	} else {
	    return itemDisplayName;
	}
    }
    
    public String getDisplayValue(){
	if(itemDisplayName != null){
	    return itemDisplayName;
	} else{
	    return itemCode;
	}
    }

    @Override
    public String toString() {
	return itemDisplayName;
    }

    public int compareTo(Object otherObject) {
	return this.toString().compareTo(otherObject.toString());
    }
}
