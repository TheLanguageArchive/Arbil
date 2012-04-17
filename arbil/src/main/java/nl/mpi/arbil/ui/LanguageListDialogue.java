package nl.mpi.arbil.ui;

import nl.mpi.arbil.data.DocumentationLanguages;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import nl.mpi.arbil.data.ArbilVocabularyItem;
import nl.mpi.arbil.util.WindowManager;

/**
 *  Document   : LanguageListDialogue
 *  Created on : Jul 6, 2010, 3:00:09 PM
 *  Author     : Peter Withers
 */
public class LanguageListDialogue extends TemplateDialogue implements ActionListener {

    private static WindowManager windowManager;

    public static void setWindowManager(WindowManager windowManagerInstance) {
	windowManager = windowManagerInstance;
    }
    ArrayList<JCheckBox> checkBoxArray;

    public LanguageListDialogue(JDialog parentFrameLocal) {
	super(parentFrameLocal);
    }

    public static void showLanguageDialogue() {
	//showDialogue("Available Languages");
	JDialog dialog = new JDialog(windowManager.getMainFrame(), "Available Languages", true);
	LanguageListDialogue templateDialogue = new LanguageListDialogue(dialog);
	templateDialogue.populateLists();
	dialog.setContentPane(templateDialogue);
	dialog.pack();
	dialog.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
	if (((JCheckBox) e.getSource()).isSelected()) {
	    DocumentationLanguages.getSingleInstance().addselectedLanguage(e.getActionCommand());
	} else {
	    DocumentationLanguages.getSingleInstance().removeselectedLanguages(e.getActionCommand());
	}
    }

    @Override
    protected void populateLists() {
	cmdiProfilesPanel.getParent().remove(cmdiProfilesPanel);
	internalTemplatesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Languages to display in the field language select box for IMDI"));
	List<String> selectedLanguages = DocumentationLanguages.getSingleInstance().getSelectedLanguagesArrayList();
	checkBoxArray = new ArrayList<JCheckBox>();
	for (ArbilVocabularyItem currentTemplate : DocumentationLanguages.getSingleInstance().getAllLanguagesForImdi()) {
	    JCheckBox languageCheckBox;
	    languageCheckBox = new JCheckBox();
	    languageCheckBox.setText(currentTemplate.itemDisplayName);
	    languageCheckBox.setName(currentTemplate.itemDisplayName);
	    languageCheckBox.setActionCommand(currentTemplate.itemDisplayName);
	    languageCheckBox.setSelected(selectedLanguages.contains(currentTemplate.itemDisplayName));
	    languageCheckBox.setToolTipText(currentTemplate.itemDisplayName);
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
		    DocumentationLanguages.getSingleInstance().addselectedLanguage(currentCheckBox.getActionCommand());
		}
	    }
	});
	JButton selectNoneButton = new JButton();
	selectNoneButton.setText("Clear Selection");
	selectNoneButton.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		for (JCheckBox currentCheckBox : checkBoxArray) {
		    currentCheckBox.setSelected(false);
		    DocumentationLanguages.getSingleInstance().removeselectedLanguages(currentCheckBox.getActionCommand());
		}
	    }
	});
	internalTemplatesButtonPanel.removeAll();
	internalTemplatesButtonPanel.add(selectNoneButton);
	internalTemplatesButtonPanel.add(selectAllButton);
    }
}
