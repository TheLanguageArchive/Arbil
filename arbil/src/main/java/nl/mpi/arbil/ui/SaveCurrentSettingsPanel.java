package nl.mpi.arbil.ui;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import nl.mpi.arbil.data.ArbilDataNode;

/**
 * Document   : SaveCurrentSettingsPanel
 * Created on : Nov 19, 2010, 11:34:47 PM
 * @author Peter.Withers@mpi.nl
 */
public class SaveCurrentSettingsPanel extends JPanel {

    JButton saveButton;
    JTextField saveName;
    private String defaultMessage = "<enter name to save these settings>";
    ImportExportDialog importExportDialog;

    public SaveCurrentSettingsPanel(ImportExportDialog importExportDialogLocal, String currentSaveName) {
        importExportDialog = importExportDialogLocal;
        this.setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));
        saveButton = new JButton("Save Settings");
        saveName = new JTextField();
        if (currentSaveName != null) {
            saveButton.setEnabled(true);
            saveName.setText(currentSaveName);
        } else {
            saveName.setText(defaultMessage);
            saveButton.setEnabled(false);
        }
        saveName.addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent e) {
                if (saveName.getText().equals(defaultMessage)) {
                    saveName.setText("");
                    saveName.setForeground(Color.BLACK);
                    saveButton.setEnabled(true);
                }
            }

            public void focusLost(FocusEvent e) {
                if (saveName.getText().length() == 0) {
                    saveName.setText(defaultMessage);
                    saveName.setForeground(Color.lightGray);
                    saveButton.setEnabled(false);
                } else {
                    saveButton.setEnabled(true);
                }
            }
        });

        saveButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // this text check will never get run so has been excluded
//                if (saveName.getText().equals(defaultMessage) || saveName.getText().length() < 3) {
//                    LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Please enter a name to save this export as", "Save Current Export");
//                } else {
                // todo: save the settings to a text file
//                }
                // todo: save the settings to a text file
                System.out.println(importExportDialog.overwriteCheckBox.isSelected());
                System.out.println(importExportDialog.copyFilesImportCheckBox.isSelected());
                System.out.println(importExportDialog.renameFileToLamusFriendlyName.isSelected());
                System.out.println(importExportDialog.renameFileToNodeName.isSelected());
                System.out.println(importExportDialog.shibbolethCheckBox.isSelected());
                System.out.println(importExportDialog.overwriteCheckBox.isSelected());

                System.out.println(importExportDialog.exportDestinationDirectory.toString());
                for (ArbilDataNode arbilDataNode : importExportDialog.selectedNodes) {
                    System.out.println(arbilDataNode.getUrlString());
                }
            }
        });
        this.add(saveName);
        this.add(saveButton);
    }
}
