/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.arbil.data;

import java.io.Serializable;

public class ArbilVocabularyItem implements Comparable, Serializable {

    public final String itemDisplayName;
    public final String itemCode;
    public final String followUpVocabulary;
    public String descriptionString;

    public ArbilVocabularyItem(String itemDisplayName, String itemCode, String followUpVocabulary) {
	this.itemDisplayName = itemDisplayName;
	this.itemCode = itemCode;
	this.followUpVocabulary = followUpVocabulary;
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

    public String getDisplayValue() {
	if (itemDisplayName != null) {
	    return itemDisplayName;
	} else {
	    return itemCode;
	}
    }

    public boolean hasItemCode() {
	return itemCode != null;
    }

    @Override
    public String toString() {
	return getDisplayValue();
    }

    public int compareTo(Object otherObject) {
	return this.toString().compareTo(otherObject.toString());
    }
}
