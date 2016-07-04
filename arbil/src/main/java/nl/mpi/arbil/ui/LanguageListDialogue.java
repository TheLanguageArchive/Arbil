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
package nl.mpi.arbil.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import nl.mpi.arbil.data.ArbilVocabularyItem;
import nl.mpi.arbil.data.DocumentationLanguages;
import nl.mpi.arbil.templates.ArbilTemplateManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.WindowManager;

/**
 * Document : LanguageListDialogue
 * Created on : Jul 6, 2010, 3:00:09 PM
 * Author : Peter Withers
 */
public class LanguageListDialogue extends TemplateDialogue {

    private static final ResourceBundle widgets = ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets");
    private ArrayList<JCheckBox> checkBoxArray;
    private final DocumentationLanguages documentationLanguages;

    public LanguageListDialogue(JDialog parentFrameLocal, WindowManager windowManager, MessageDialogHandler dialogueHandler) {
	super(windowManager, dialogueHandler, parentFrameLocal);
	this.documentationLanguages = ArbilTemplateManager.getSingleInstance().getDefaultTemplate().getDocumentationLanguages();
    }

    public static void showLanguageDialogue(WindowManager windowManager, MessageDialogHandler dialogueHandler) {
	//showDialogue("Available Languages");
	final JDialog dialog = new JDialog(windowManager.getMainFrame(), widgets.getString("AVAILABLE LANGUAGES"), true);
	final LanguageListDialogue languageListDialogue = new LanguageListDialogue(dialog, windowManager, dialogueHandler);
	languageListDialogue.populateLists();

	dialog.setContentPane(languageListDialogue);
	dialog.pack();
	setDialogHeight(dialog, windowManager);
	dialog.setVisible(true);
    }

    @Override
    protected void populateLists() {
	cmdiProfilesPanel.getParent().remove(cmdiProfilesPanel);
	internalTemplatesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(widgets.getString("LANGUAGES TO DISPLAY IN THE FIELD LANGUAGE SELECT BOX FOR IMDI")));
	List<String> selectedLanguages = documentationLanguages.getSelectedLanguagesOrDefault();
	checkBoxArray = new ArrayList<JCheckBox>();
	for (ArbilVocabularyItem currentTemplate : documentationLanguages.getAllLanguages()) {
	    JCheckBox languageCheckBox;
	    languageCheckBox = new JCheckBox();
	    languageCheckBox.setText(currentTemplate.itemDisplayName);
	    languageCheckBox.setName(currentTemplate.itemDisplayName);
	    languageCheckBox.setActionCommand(currentTemplate.itemDisplayName);
	    languageCheckBox.setSelected(selectedLanguages.contains(currentTemplate.itemDisplayName));
	    languageCheckBox.setToolTipText(currentTemplate.itemDisplayName);
	    languageCheckBox.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if (((JCheckBox) e.getSource()).isSelected()) {
			documentationLanguages.addselectedLanguage(e.getActionCommand());
		    } else {
			documentationLanguages.removeselectedLanguages(e.getActionCommand());
		    }
		}
	    });
	    checkBoxArray.add(languageCheckBox);
	}
	addSorted(templatesPanel, checkBoxArray);

	JButton selectAllButton = new JButton();
	selectAllButton.setText(widgets.getString("LANGUAGES_SELECT ALL"));
	selectAllButton.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		for (JCheckBox currentCheckBox : checkBoxArray) {
		    currentCheckBox.setSelected(true);
		    documentationLanguages.addselectedLanguage(currentCheckBox.getActionCommand());
		}
	    }
	});
	JButton selectNoneButton = new JButton();
	selectNoneButton.setText(widgets.getString("LANGUAGES_CLEAR SELECTION"));
	selectNoneButton.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		for (JCheckBox currentCheckBox : checkBoxArray) {
		    currentCheckBox.setSelected(false);
		    documentationLanguages.removeselectedLanguages(currentCheckBox.getActionCommand());
		}
	    }
	});
	internalTemplatesButtonPanel.removeAll();
	internalTemplatesButtonPanel.add(selectNoneButton);
	internalTemplatesButtonPanel.add(selectAllButton);
    }
}
