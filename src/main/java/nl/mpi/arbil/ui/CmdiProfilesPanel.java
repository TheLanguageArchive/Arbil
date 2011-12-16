package nl.mpi.arbil.ui;

import java.awt.BorderLayout;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import nl.mpi.arbil.clarin.profiles.CmdiProfileReader;
import nl.mpi.arbil.clarin.profiles.CmdiProfileReader.ProfileSelection;
import nl.mpi.arbil.templates.ArbilTemplateManager;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class CmdiProfilesPanel extends JPanel {

    private javax.swing.JPanel clarinPanel;
    private javax.swing.JPanel profileReloadPanel;
    private javax.swing.JProgressBar profileReloadProgressBar;
    protected javax.swing.JCheckBox profileSelectionCheckBox;
    private javax.swing.JScrollPane clarinScrollPane;
    private javax.swing.JButton reloadButton;
    private JDialog parentFrame;

    public CmdiProfilesPanel(JDialog parentFrameLocal) {
	this.parentFrame = parentFrameLocal;

	profileReloadPanel = new javax.swing.JPanel();
	profileReloadProgressBar = new javax.swing.JProgressBar();
	clarinScrollPane = new javax.swing.JScrollPane();
	profileSelectionCheckBox = new JCheckBox();
	reloadButton = new javax.swing.JButton();
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

	profileSelectionCheckBox.setText("Only load profiles selected for manual editing");
	profileSelectionCheckBox.setSelected(CmdiProfileReader.getSingleInstance().getSelection() == ProfileSelection.SELECTED);

	profileReloadTopPanel.add(reloadButton);
	profileReloadTopPanel.add(profileReloadProgressBar);
	profileReloadPanel.add(profileReloadTopPanel, BorderLayout.CENTER);
	profileReloadPanel.add(profileSelectionCheckBox, BorderLayout.SOUTH);

	add(profileReloadPanel, java.awt.BorderLayout.PAGE_END);

	clarinScrollPane.setViewportView(clarinPanel);

	add(clarinScrollPane, java.awt.BorderLayout.CENTER);

	JPanel profilesTopPanel = new JPanel();
	profilesTopPanel.setLayout(new javax.swing.BoxLayout(profilesTopPanel, javax.swing.BoxLayout.LINE_AXIS));

//        jTextField1.setText("<profile url>"); //todo: complete this
//        profilesTopPanel.add(jTextField1);

	JButton addButton = new JButton();
	addButton.setText("Add URL");
	addButton.setToolTipText("Add a profile URL to the list");
	addButton.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		String newDirectoryName = JOptionPane.showInputDialog(ArbilWindowManager.getSingleInstance().getMainFrame(), "Enter the profile URL", "Add Profile", JOptionPane.PLAIN_MESSAGE, null, null, null).toString();
		ArbilTemplateManager.getSingleInstance().addSelectedTemplates("custom:" + newDirectoryName);
		populateList();
	    }
	});
	profilesTopPanel.add(addButton);
	JButton browseButton = new JButton();
	browseButton.setText("Add File");
	browseButton.setToolTipText("Browse for local profiles");
	browseButton.addActionListener(new java.awt.event.ActionListener() {

	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		for (File selectedFile : ArbilWindowManager.getSingleInstance().showFileSelectBox("Select Profile", false, true, false)) {
		    ArbilTemplateManager.getSingleInstance().addSelectedTemplates("custom:" + selectedFile.toURI().toString());
		}
		populateList();
	    }
	});
	profilesTopPanel.add(browseButton);
	add(profilesTopPanel, java.awt.BorderLayout.PAGE_START);

    }

    public void loadProfiles(final boolean forceUpdate) {
	CmdiProfileReader.getSingleInstance().setSelection(
		profileSelectionCheckBox.isSelected() ? ProfileSelection.SELECTED : ProfileSelection.ALL);
	clarinPanel.removeAll();
	clarinPanel.add(new JTextField("Loading, please wait..."));
	reloadButton.setVisible(false);
	profileSelectionCheckBox.setEnabled(false);
	profileReloadProgressBar.setVisible(true);
	this.doLayout();
	new Thread("loadProfiles") {

	    @Override
	    public void run() {
		CmdiProfileReader cmdiProfileReader = CmdiProfileReader.getSingleInstance();
		cmdiProfileReader.refreshProfiles(profileReloadProgressBar, forceUpdate);
		profileReloadProgressBar.setVisible(false);
		reloadButton.setVisible(true);
		profileSelectionCheckBox.setEnabled(true);
		populateList();
		doLayout();
	    }
	}.start();
    }

    private void reloadButtonActionPerformed(java.awt.event.ActionEvent evt) {
	loadProfiles(true);
    }

    public void populateList() {
	populateProfilesList();
	parentFrame.pack();
    }

    protected void populateProfilesList() {
	ArrayList<String> selectedTamplates = ArbilTemplateManager.getSingleInstance().getSelectedTemplateArrayList();

	// add clarin types
	ArrayList<JCheckBox> checkBoxArray = new ArrayList<JCheckBox>();
	CmdiProfileReader cmdiProfileReader = CmdiProfileReader.getSingleInstance();
	for (CmdiProfileReader.CmdiProfile currentCmdiProfile : cmdiProfileReader.cmdiProfileArray) {
	    final String templateId = ArbilTemplateManager.CLARIN_PREFIX + currentCmdiProfile.getXsdHref();
	    JCheckBox clarinProfileCheckBox;
	    clarinProfileCheckBox = new JCheckBox();
	    clarinProfileCheckBox.setText(currentCmdiProfile.name);
	    clarinProfileCheckBox.setName(currentCmdiProfile.name);
	    clarinProfileCheckBox.setActionCommand(templateId);
	    clarinProfileCheckBox.setSelected(selectedTamplates.contains(templateId));
	    clarinProfileCheckBox.setToolTipText(currentCmdiProfile.description);
	    clarinProfileCheckBox.addActionListener(TemplateDialogue.templateSelectionListener);
	    checkBoxArray.add(clarinProfileCheckBox);
	}
	for (String currentSepectedProfile : selectedTamplates) {
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
		checkBoxArray.add(clarinProfileCheckBox);
	    }
	}
	TemplateDialogue.addSorted(clarinPanel, checkBoxArray);
    }
}
