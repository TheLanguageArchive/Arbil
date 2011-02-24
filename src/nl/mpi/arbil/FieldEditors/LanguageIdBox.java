package nl.mpi.arbil.FieldEditors;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import javax.swing.JComboBox;
import nl.mpi.arbil.DocumentationLanguages;
import nl.mpi.arbil.GuiHelper;
import nl.mpi.arbil.ImdiField;
import nl.mpi.arbil.ImdiVocabularies;

/**
 *  Document   : LanguageIdBox
 *  Created on : Sep 14, 2010, 3:58:33 PM
 *  Author     : Peter Withers
 */
public class LanguageIdBox extends JComboBox {

    public static int languageSelectWidth = 100;
    static String defaultLanguageDropDownValue = "<select>";

    public LanguageIdBox(final ImdiField cellField, Rectangle parentCellRect) {
        String fieldLanguageId = cellField.getLanguageId();
//        if (fieldLanguageId != null) {
        System.out.println("Has LanguageId");
        ImdiVocabularies.VocabularyItem selectedItem = null;
        this.setEditable(false);
        ImdiVocabularies.VocabularyItem[] languageItemArray = new DocumentationLanguages().getLanguageListSubset();
        Arrays.sort(languageItemArray);
        for (ImdiVocabularies.VocabularyItem currentItem : languageItemArray) {
            this.addItem(currentItem);
            // the code and description values have become unreliable due to changes to the controlled vocabularies see https://trac.mpi.nl/ticket/563#
            if (fieldLanguageId.equals(currentItem.languageCode) || fieldLanguageId.equals(currentItem.descriptionString)) {
                selectedItem = currentItem;
            }
        }
        if (selectedItem != null) {
            System.out.println("selectedItem: " + selectedItem);
            this.setSelectedItem(selectedItem);
        } else {
            this.addItem(defaultLanguageDropDownValue);
            this.setSelectedItem(defaultLanguageDropDownValue);
        }
        this.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
//                        ImdiField cellField = (ImdiField) cellValue[cellFieldIndex];
                    if (LanguageIdBox.this.getSelectedItem() instanceof ImdiVocabularies.VocabularyItem) {
                        ImdiVocabularies.VocabularyItem selectedLanguage = (ImdiVocabularies.VocabularyItem) LanguageIdBox.this.getSelectedItem();
                        String languageCode = selectedLanguage.languageCode;
                        if (languageCode == null) {
                            // the code and description values have become unreliable due to changes to the controlled vocabularies see https://trac.mpi.nl/ticket/563#
                            languageCode = selectedLanguage.descriptionString;
                        }
                        cellField.setLanguageId(languageCode, true, false);
                    }
                    LanguageIdBox.this.removeItem(defaultLanguageDropDownValue);
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        if (parentCellRect != null) {
            this.setPreferredSize(new Dimension(languageSelectWidth, parentCellRect.height));
        }
    }
}
