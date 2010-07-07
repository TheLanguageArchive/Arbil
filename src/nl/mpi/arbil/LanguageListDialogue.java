package nl.mpi.arbil;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import nl.mpi.arbil.ImdiVocabularies.VocabularyItem;
import nl.mpi.arbil.templates.TemplateDialogue;

/**
 *  Document   : LanguageListDialogue
 *  Created on : Jul 6, 2010, 3:00:09 PM
 *  Author     : Peter Withers
 */
public class LanguageListDialogue extends TemplateDialogue {

    ArrayList<JCheckBox> checkBoxArray;

    public LanguageListDialogue(JDialog parentFrameLocal) {
        super(parentFrameLocal);
    }

    public static void showLanguageDialogue() {
        //showDialogue("Available Languages");
        JDialog dialog = new JDialog(LinorgWindowManager.getSingleInstance().linorgFrame, "Available Languages", true);
        LanguageListDialogue templateDialogue = new LanguageListDialogue(dialog);
        templateDialogue.populateLists();
        dialog.setContentPane(templateDialogue);
        dialog.pack();
        dialog.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (((JCheckBox) e.getSource()).isSelected()) {
            new DocumentationLanguages().addselectedLanguages(e.getActionCommand());
        } else {
            new DocumentationLanguages().removeselectedLanguages(e.getActionCommand());
        }
    }

    @Override
    protected void populateLists() {
        clarinProfilesPanel.getParent().remove(clarinProfilesPanel);
        internalTemplatesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Languages to display in the feild language select box"));
        ArrayList<String> selectedLanguages = new DocumentationLanguages().getSelectedLanguagesArrayList();
        checkBoxArray = new ArrayList<JCheckBox>();
        for (VocabularyItem currentTemplate : new DocumentationLanguages().getallLanguages()) {
            JCheckBox languageCheckBox;
            languageCheckBox = new JCheckBox();
            languageCheckBox.setText(currentTemplate.languageName);
            languageCheckBox.setName(currentTemplate.languageName);
            languageCheckBox.setActionCommand(currentTemplate.languageName);
            languageCheckBox.setSelected(selectedLanguages.contains(currentTemplate.languageName));
            languageCheckBox.setToolTipText(currentTemplate.languageName);
            languageCheckBox.addActionListener(this);
            checkBoxArray.add(languageCheckBox);
        }
        addSorted(templatesPanel, checkBoxArray);

        JButton selectAllButton = new JButton();
        selectAllButton.setText("Select All");
        selectAllButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                for (JCheckBox currentCheckBox : checkBoxArray) {
                    currentCheckBox.setSelected(true);
                }
            }
        });
        JButton selectNoneButton = new JButton();
        selectNoneButton.setText("Clear Selection");
        selectNoneButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                for (JCheckBox currentCheckBox : checkBoxArray) {
                    currentCheckBox.setSelected(false);
                }
            }
        });
        internalTemplatesButtonPanel.removeAll();
        internalTemplatesButtonPanel.add(selectNoneButton);
        internalTemplatesButtonPanel.add(selectAllButton);
    }
}
