package nl.mpi.arbil.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
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

    private boolean firstLoad = true;
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
    private javax.swing.JButton reloadButton;
    private JDialog parentFrame;
    private javax.swing.JButton downloadAllButton;
    private JTextArea profileInstructionsArea;

    public CmdiProfilesPanel(JDialog parentFrameLocal) {
        this.parentFrame = parentFrameLocal;

        profileReloadPanel = new javax.swing.JPanel();
        profileReloadProgressBar = new javax.swing.JProgressBar();
        clarinScrollPane = new javax.swing.JScrollPane();
        profileSelectionCheckBox = new JCheckBox();
        reloadButton = new javax.swing.JButton();
        downloadAllButton = new javax.swing.JButton();
        clarinPanel = new javax.swing.JPanel();
        JPanel profileReloadTopPanel = new JPanel();
        setLayout(new java.awt.BorderLayout());

//	profileReloadPanel.setLayout(new javax.swing.BoxLayout(profileReloadPanel, javax.swing.BoxLayout.PAGE_AXIS));
        profileReloadPanel.setLayout(new BorderLayout());
        profileReloadPanel.setAlignmentX(SwingConstants.LEFT);
        profileReloadTopPanel.setLayout(new javax.swing.BoxLayout(profileReloadTopPanel, javax.swing.BoxLayout.LINE_AXIS));

        reloadButton.setText("Reload Clarin Profiles");
        reloadButton.setToolTipText("Download the latest clarin profiles");
        reloadButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reloadButtonActionPerformed(evt);
            }
        });

        downloadAllButton.setText("Download all profiles");
        downloadAllButton.setToolTipText("Download all profiles for offline use (including unselected profiles)");
        downloadAllButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downloadAllButtonActionPerformed(evt);
            }
        });

        profileSelectionCheckBox.setText("Only load profiles selected for manual editing");
        profileSelectionCheckBox.setSelected(CmdiProfileReader.getSingleInstance().getSelection() == ProfileSelection.SELECTED);

        profileReloadTopPanel.add(reloadButton);
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
                for (File selectedFile : dialogHandler.showFileSelectBox("Select Profile", false, true, null)) {
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

    /**
     * @param forceUpdate whether to force update even if cached copies not out of date
     * @param updateProfilesCache whether to update the profiles cache as well (i.e. in addition to the profiles list)
     */
    private void loadProfiles(final boolean forceUpdate, final boolean updateProfilesCache) {
        CmdiProfileReader.getSingleInstance().setSelection(
                profileSelectionCheckBox.isSelected() ? ProfileSelection.SELECTED : ProfileSelection.ALL);
        clarinPanel.removeAll();
        clarinPanel.add(new JTextField("Loading, please wait..."));
        reloadButton.setVisible(false);
        downloadAllButton.setVisible(false);
        profileSelectionCheckBox.setEnabled(false);
        profileReloadProgressBar.setVisible(true);
        this.doLayout();
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
                        profileReloadProgressBar.setVisible(false);
                        reloadButton.setVisible(true);
                        downloadAllButton.setVisible(true);
                        profileSelectionCheckBox.setEnabled(true);
                        populateList();
                        doLayout();
                    }
                });

            }
        }.start();
    }

    private void reloadButtonActionPerformed(java.awt.event.ActionEvent evt) {
        loadProfileDescriptions(true);
    }

    private void downloadAllButtonActionPerformed(java.awt.event.ActionEvent evt) {
        downloadProfiles(true);
    }

    public synchronized void populateList() {
        populateProfilesList();
        if (firstLoad) {
            parentFrame.pack();
            firstLoad = false;
        }
    }

    protected void populateProfilesList() {
        ArrayList<String> selectedTemplates = ArbilTemplateManager.getSingleInstance().getSelectedTemplateArrayList();

        // add clarin types
        ArrayList<JCheckBox> checkBoxArray = new ArrayList<JCheckBox>();
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
            if (currentSepectedProfile.startsWith("custom:")) {
                String customUrlString = currentSepectedProfile.substring("custom:".length());
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
