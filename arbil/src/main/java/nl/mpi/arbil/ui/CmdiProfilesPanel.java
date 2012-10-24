/**
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package nl.mpi.arbil.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import nl.mpi.arbil.clarin.profiles.CmdiProfileReader;
import nl.mpi.arbil.clarin.profiles.CmdiProfileReader.ProfileSelection;
import nl.mpi.arbil.templates.ArbilTemplateManager;
import nl.mpi.arbil.util.MessageDialogHandler;
import nl.mpi.arbil.util.WindowManager;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class CmdiProfilesPanel extends JPanel {

    private final JDialog parentFrame;
    private static WindowManager windowManager;

    public static void setWindowManager(WindowManager windowManagerInstance) {
        windowManager = windowManagerInstance;
    }
    private static MessageDialogHandler dialogHandler;

    public static void setMessageDialogHandler(MessageDialogHandler dialogHandlerInstance) {
        dialogHandler = dialogHandlerInstance;
    }
    private javax.swing.JPanel clarinPanel;
    private javax.swing.JPanel profileReloadPanel;
    private javax.swing.JProgressBar profileReloadProgressBar;
    protected javax.swing.JCheckBox profileSelectionCheckBox;
    private javax.swing.JScrollPane clarinScrollPane;
    private javax.swing.JButton reloadListButton;
    private javax.swing.JButton reloadProfilesButton;
    private javax.swing.JButton downloadAllButton;
    private JTextArea profileInstructionsArea;
    private boolean firstLoad = true;

    public CmdiProfilesPanel(JDialog parentFrameLocal) {
        this.parentFrame = parentFrameLocal;

	profileReloadPanel = new javax.swing.JPanel();
	profileReloadProgressBar = new javax.swing.JProgressBar();
	clarinScrollPane = new javax.swing.JScrollPane();
	profileSelectionCheckBox = new JCheckBox();
	reloadListButton = new javax.swing.JButton();
	reloadProfilesButton = new javax.swing.JButton();
	downloadAllButton = new javax.swing.JButton();
	clarinPanel = new javax.swing.JPanel();
	JPanel profileReloadTopPanel = new JPanel();
	setLayout(new java.awt.BorderLayout());

//	profileReloadPanel.setLayout(new javax.swing.BoxLayout(profileReloadPanel, javax.swing.BoxLayout.PAGE_AXIS));
        profileReloadPanel.setLayout(new BorderLayout());
        profileReloadPanel.setAlignmentX(SwingConstants.LEFT);
        profileReloadTopPanel.setLayout(new javax.swing.BoxLayout(profileReloadTopPanel, javax.swing.BoxLayout.LINE_AXIS));

	reloadListButton.setText("Refresh list");
	reloadListButton.setToolTipText("Download the latest clarin profiles");
	reloadListButton.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		reloadListButtonActionPerformed(evt);
	    }
	});


	reloadProfilesButton.setText("Reload selection");
	reloadProfilesButton.setToolTipText("Clear cached copies and re-download the selected profiles");
	reloadProfilesButton.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		reloadProfilesButtonActionPerformed(evt);
	    }
	});

	downloadAllButton.setText("Download all");
	downloadAllButton.setToolTipText("Download all profiles for offline use (including unselected profiles)");
	downloadAllButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downloadAllButtonActionPerformed(evt);
            }
        });

        profileSelectionCheckBox.setText("Only load profiles selected for manual editing");
        profileSelectionCheckBox.setSelected(CmdiProfileReader.getSingleInstance().getSelection() == ProfileSelection.SELECTED);

	profileReloadTopPanel.add(reloadListButton);
	profileReloadTopPanel.add(reloadProfilesButton);
	profileReloadTopPanel.add(downloadAllButton);
	profileReloadTopPanel.add(profileReloadProgressBar);
	profileReloadPanel.add(profileReloadTopPanel, BorderLayout.CENTER);
	profileReloadPanel.add(profileSelectionCheckBox, BorderLayout.SOUTH);

        add(profileReloadPanel, java.awt.BorderLayout.PAGE_END);

        clarinScrollPane.setViewportView(clarinPanel);

        add(clarinScrollPane, java.awt.BorderLayout.CENTER);

        profileInstructionsArea = new JTextArea("Profiles selected below will become available in the 'Add' menu of the local corpus. By default, only selected profiles will be downloaded for offline use.");
        profileInstructionsArea.setEditable(false);
        profileInstructionsArea.setLineWrap(true);
        profileInstructionsArea.setWrapStyleWord(true);
        profileInstructionsArea.setOpaque(false);

        JPanel profilesTopButtonsPanel = new JPanel();
        profilesTopButtonsPanel.setLayout(new javax.swing.BoxLayout(profilesTopButtonsPanel, javax.swing.BoxLayout.LINE_AXIS));

        JButton addButton = new JButton();
        addButton.setText("Add URL");
        addButton.setToolTipText("Add a profile URL to the list");
        addButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                String newDirectoryName = JOptionPane.showInputDialog(windowManager.getMainFrame(), "Enter the profile URL", "Add Profile", JOptionPane.PLAIN_MESSAGE, null, null, null).toString();
                ArbilTemplateManager.getSingleInstance().addSelectedTemplates("custom:" + newDirectoryName);
                populateList();
            }
        });
        profilesTopButtonsPanel.add(addButton);
        JButton browseButton = new JButton();
        browseButton.setText("Add File");
        browseButton.setToolTipText("Browse for local profiles");
        browseButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                for (File selectedFile : dialogHandler.showFileSelectBox("Select Profile", false, true, null, MessageDialogHandler.DialogueType.open, null)) {
                    ArbilTemplateManager.getSingleInstance().addSelectedTemplates("custom:" + selectedFile.toURI().toString());
                }
                populateList();
            }
        });
        profilesTopButtonsPanel.add(browseButton);

        JPanel profilesTopPanel = new JPanel();
        profilesTopPanel.setLayout(new javax.swing.BoxLayout(profilesTopPanel, javax.swing.BoxLayout.Y_AXIS));
        profileInstructionsArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        profilesTopButtonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        profilesTopPanel.add(profileInstructionsArea);
        profilesTopPanel.add(profilesTopButtonsPanel);
        add(profilesTopPanel, java.awt.BorderLayout.PAGE_START);
    }

    public void downloadProfiles(final boolean forceUpdate) {
        loadProfiles(forceUpdate, true);
    }

    public void loadProfileDescriptions(final boolean forceUpdate) {
        loadProfiles(forceUpdate, false);
    }

    public void setInstructionsVisible(boolean visible) {
        profileInstructionsArea.setVisible(visible);
    }

    private void showProgressBar(boolean show) {
	reloadListButton.setVisible(!show);
	reloadProfilesButton.setVisible(!show);
	downloadAllButton.setVisible(!show);
	profileSelectionCheckBox.setEnabled(!show);
	profileReloadProgressBar.setVisible(show);
    }

    /**
     * @param forceUpdate whether to force update even if cached copies not out of date
     * @param updateProfilesCache whether to update the profiles cache as well (i.e. in addition to the profiles list)
     */
    private void loadProfiles(final boolean forceUpdate, final boolean updateProfilesCache) {
	CmdiProfileReader.getSingleInstance().setSelection(
		profileSelectionCheckBox.isSelected() ? ProfileSelection.SELECTED : ProfileSelection.ALL);
	clarinPanel.removeAll();
	clarinPanel.add(new JTextField("Loading, please wait..."));
	showProgressBar(true);
	doLayout();
	new Thread("loadProfiles") {

            @Override
            public void run() {
                CmdiProfileReader cmdiProfileReader = CmdiProfileReader.getSingleInstance();
                if (updateProfilesCache) {
                    cmdiProfileReader.refreshProfilesAndUpdateCache(profileReloadProgressBar, forceUpdate);
                } else {
                    cmdiProfileReader.refreshProfiles(forceUpdate);
                }
                SwingUtilities.invokeLater(new Runnable() {

		    public void run() {
			showProgressBar(false);
			populateList();
			doLayout();
		    }
		});

            }
        }.start();
    }

    private void reloadListButtonActionPerformed(java.awt.event.ActionEvent evt) {
        loadProfileDescriptions(true);
    }

    private void reloadProfilesButtonActionPerformed(ActionEvent evt) {
	SwingUtilities.invokeLater(new Runnable() {

	    public void run() {
		showProgressBar(true);
		profileReloadProgressBar.setIndeterminate(true);
		profileReloadProgressBar.setString("");
		doLayout();
	    }
	});

	new Thread() {

	    @Override
	    public void run() {
		// Get CMDI profiles from selected templates
		final List<String> profilesToReload = ArbilTemplateManager.getSingleInstance().getCMDIProfileHrefs();

		SwingUtilities.invokeLater(new Runnable() {

		    public void run() {
			profileReloadProgressBar.setIndeterminate(false);
			profileReloadProgressBar.setMinimum(0);
			profileReloadProgressBar.setMaximum(profilesToReload.size() + 1);
			profileReloadProgressBar.setValue(1);
		    }
		});

		// Reload each of them
		for (String xsdHref : profilesToReload) {
		    CmdiProfileReader.getSingleInstance().storeProfileInCache(xsdHref, 0);
		    SwingUtilities.invokeLater(new Runnable() {

			public void run() {
			    profileReloadProgressBar.setValue(profileReloadProgressBar.getValue() + 1);
			}
		    });
		}

		SwingUtilities.invokeLater(new Runnable() {

		    public void run() {
			profileReloadProgressBar.setValue(0);
			showProgressBar(false);
			doLayout();

			dialogHandler.addMessageDialogToQueue("The profiles have been re-downloaded. You need to restart Arbil in order to apply any changes.", "Restart Arbil");
		    }
		});
	    }
	}.start();
    }

    private void downloadAllButtonActionPerformed(java.awt.event.ActionEvent evt) {
        downloadProfiles(true);
    }

    public synchronized void populateList() {
	populateProfilesList();
	if (firstLoad) {
	    parentFrame.pack();
	    TemplateDialogue.setDialogHeight(parentFrame);
	    firstLoad = false;
	}
    }

    protected void populateProfilesList() {
	List<String> selectedTemplates = ArbilTemplateManager.getSingleInstance().getSelectedTemplates();

	// add clarin types
	List<JCheckBox> checkBoxArray = new ArrayList<JCheckBox>();
	final CmdiProfileReader cmdiProfileReader = CmdiProfileReader.getSingleInstance();
	for (final CmdiProfileReader.CmdiProfile currentCmdiProfile : cmdiProfileReader.cmdiProfileArray) {
	    final String templateId = ArbilTemplateManager.CLARIN_PREFIX + currentCmdiProfile.getXsdHref();
	    JCheckBox clarinProfileCheckBox;
	    clarinProfileCheckBox = new JCheckBox();
	    clarinProfileCheckBox.setText(currentCmdiProfile.name);
	    clarinProfileCheckBox.setName(currentCmdiProfile.name);
	    clarinProfileCheckBox.setActionCommand(templateId);
	    clarinProfileCheckBox.setSelected(selectedTemplates.contains(templateId));
	    clarinProfileCheckBox.setToolTipText(currentCmdiProfile.description);
	    clarinProfileCheckBox.addActionListener(TemplateDialogue.templateSelectionListener);
	    clarinProfileCheckBox.addActionListener(new DownloadSchemaActionListener(clarinProfileCheckBox, cmdiProfileReader, currentCmdiProfile.getXsdHref()));
	    checkBoxArray.add(clarinProfileCheckBox);
	}
	for (String currentSepectedProfile : selectedTemplates) {
	    if (currentSepectedProfile.startsWith(ArbilTemplateManager.CUSTOM_PREFIX)) {
		String customUrlString = currentSepectedProfile.substring(ArbilTemplateManager.CUSTOM_PREFIX.length());
		String customName = currentSepectedProfile.replaceAll("[/.]xsd$", "");
		if (customName.contains("/")) {
		    customName = customName.substring(customName.lastIndexOf("/") + 1);
		}
		JCheckBox clarinProfileCheckBox;
		clarinProfileCheckBox = new JCheckBox();
		clarinProfileCheckBox.setText(customName);
		clarinProfileCheckBox.setName(customName);
		clarinProfileCheckBox.setActionCommand(currentSepectedProfile);
		clarinProfileCheckBox.setSelected(true);
		clarinProfileCheckBox.setToolTipText("custom profile, uncheck to remove");
		clarinProfileCheckBox.addActionListener(TemplateDialogue.templateSelectionListener);
		clarinProfileCheckBox.addActionListener(new DownloadSchemaActionListener(clarinProfileCheckBox, cmdiProfileReader, customUrlString));
		checkBoxArray.add(clarinProfileCheckBox);
	    }
	}
	TemplateDialogue.addSorted(clarinPanel, checkBoxArray);
    }

    private static class DownloadSchemaActionListener implements ActionListener {

        private CmdiProfileReader reader;
        private String url;
        private JCheckBox checkBox;

        public DownloadSchemaActionListener(JCheckBox clarinProfileCheckBox, CmdiProfileReader reader, String url) {
            this.reader = reader;
            this.url = url;
            this.checkBox = clarinProfileCheckBox;
        }

        public void actionPerformed(ActionEvent e) {
            new Thread() {

                @Override
                public void run() {
                    if (checkBox.isSelected()) {
                        reader.storeProfileInCache(url, 0);
                    }
                }
            }.start();
        }
    }
}
