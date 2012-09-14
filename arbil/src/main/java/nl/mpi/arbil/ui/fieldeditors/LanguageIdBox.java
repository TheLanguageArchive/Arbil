package nl.mpi.arbil.ui.fieldeditors;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;
import javax.swing.JComboBox;
import nl.mpi.arbil.clarin.profiles.CmdiTemplate;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ImdiDocumentationLanguages;
import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.data.ArbilVocabularyItem;
import nl.mpi.arbil.data.CmdiDocumentationLanguages;
import nl.mpi.arbil.util.BugCatcherManager;

/**
 *  Document   : LanguageIdBox
 *  Created on : Sep 14, 2010, 3:58:33 PM
 *  Author     : Peter Withers
 */
public class LanguageIdBox extends JComboBox {

    public final static int languageSelectWidth = 100;
    static String defaultLanguageDropDownValue = "<select>";

    public LanguageIdBox(final ArbilField cellField, Rectangle parentCellRect) {
	String fieldLanguageId = cellField.getLanguageId();
//        if (fieldLanguageId != null) {
	ArbilVocabularyItem selectedItem = null;
	this.setEditable(false);
	List<ArbilVocabularyItem> languageItemArray = getLanguageItems(cellField.getParentDataNode());
	if (languageItemArray != null) {
	    Collections.sort(languageItemArray);
	    for (ArbilVocabularyItem currentItem : languageItemArray) {
		this.addItem(currentItem);
		// the code and description values have become unreliable due to changes to the controlled vocabularies see https://trac.mpi.nl/ticket/563#
		if (fieldLanguageId != null
			&& (fieldLanguageId.equals(currentItem.itemCode) || fieldLanguageId.equals(currentItem.descriptionString))) {
		    selectedItem = currentItem;
		}
	    }
	    this.addItem(defaultLanguageDropDownValue);

	    if (selectedItem != null) {
		this.setSelectedItem(selectedItem);
	    } else {
		this.setSelectedItem(defaultLanguageDropDownValue);
	    }
	    this.addActionListener(new ActionListener() {

		public void actionPerformed(ActionEvent e) {
		    try {
			if (LanguageIdBox.this.getSelectedItem() instanceof ArbilVocabularyItem) {
			    ArbilVocabularyItem selectedLanguage = (ArbilVocabularyItem) LanguageIdBox.this.getSelectedItem();
			    String languageCode = selectedLanguage.itemCode;
			    if (languageCode == null) {
				// the code and description values have become unreliable due to changes to the controlled vocabularies see https://trac.mpi.nl/ticket/563#
				languageCode = selectedLanguage.descriptionString;
			    }
			    cellField.setLanguageId(languageCode, true, false);
			} else {
			    if (defaultLanguageDropDownValue.equals(LanguageIdBox.this.getSelectedItem())) {
				cellField.setLanguageId(null, true, false);
			    }
			}
		    } catch (Exception ex) {
			BugCatcherManager.getBugCatcher().logError(ex);
		    }
		}
	    });
	}
	if (parentCellRect != null) {
	    this.setPreferredSize(new Dimension(languageSelectWidth, parentCellRect.height));
	}
    }

    private List<ArbilVocabularyItem> getLanguageItems(final ArbilDataNode parentDataNode) {
	if (parentDataNode.getNodeTemplate() instanceof CmdiTemplate) {
	    return CmdiDocumentationLanguages.getSingleInstance().getLanguageListSubsetForCmdi();
	} else {
	    return ImdiDocumentationLanguages.getSingleInstance().getLanguageListSubsetForImdi();
	}
    }
}
