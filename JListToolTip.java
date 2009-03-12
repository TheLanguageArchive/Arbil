/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Hashtable;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolTip;

/**
 *
 * @author petwit
 */
class JListToolTip extends JToolTip {

    JPanel jPanel;
    Object targetObject;
    String preSpaces = "      ";

    public JListToolTip() {
        this.setLayout(new BorderLayout());
        jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));
        add(jPanel, BorderLayout.CENTER);
        jPanel.setBackground(getBackground());
        jPanel.setBorder(getBorder());
    }

    private String truncateString(String inputString) {
        if (inputString.length() > 100) {
            inputString = inputString.substring(0, 100) + "...";
        }
        return inputString + " ";// add a space to padd the end of the tooltip
    }

    private void addIconLabel(Object tempObject) {
        JLabel jLabel = new JLabel(truncateString(tempObject.toString()));
        if (tempObject instanceof ImdiTreeObject) {
            jLabel.setIcon(((ImdiTreeObject) tempObject).getIcon());
        }
        jLabel.doLayout();
        jPanel.add(jLabel);
    }

    private void addDetailLabel(String labelString) {
        JLabel jLabel = new JLabel(truncateString(labelString));
        jLabel.doLayout();
        jPanel.add(jLabel);
    }

    private void addDetailLabel(String prefixString, ImdiField[] tempFieldArray) {
        if (tempFieldArray != null) {
            for (ImdiField tempField : tempFieldArray) {
                String labelString = tempField.toString();
                addDetailLabel(preSpaces + prefixString + labelString);
            }
        }
    }

    private void addLabelsForImdiObject(ImdiTreeObject tempObject) {

        if (tempObject.isImdi()) {
            Hashtable<String, ImdiField[]> tempFields = tempObject.getFields();
            addDetailLabel("Name: ", tempFields.get("Name"));
            addDetailLabel("Title: ", tempFields.get("Title"));
            addDetailLabel("Description: ", tempFields.get("Description"));
            addDetailLabel("Format: ", tempFields.get("Format"));
        } else {
            if (!tempObject.isDirectory()) {
                JLabel jLabel = new JLabel(preSpaces + "Unattached file");
                jLabel.doLayout();
                jPanel.add(jLabel);
            }
        }
        
        //if (tempObject.matchesInCache + tempObject.matchesLocalFileSystem + tempObject.matchesRemote > 0){
        if (tempObject.hasResource() || (!tempObject.isImdi() && !tempObject.isDirectory())){
            addDetailLabel(preSpaces + "Copies in cache: " + tempObject.matchesInCache);
            addDetailLabel(preSpaces + "Copies on local file system: " + tempObject.matchesLocalFileSystem);
            addDetailLabel(preSpaces + "Copies on server: ?"/* + tempObject.matchesRemote*/);
        }

        if (!tempObject.isLocal()) {
            JLabel jLabel = new JLabel(preSpaces + "Remote file (read only)");
//            jLabel.setIcon(ImdiTreeObject.imdiIcons.remoteicon);
//            jLabel.setVerticalTextPosition(JLabel.RIGHT);
            jLabel.doLayout();
            jPanel.add(jLabel);
        } else if (tempObject.hasResource()) {
            if (tempObject.fileNotFound) {
                JLabel jLabel = new JLabel(preSpaces + "File not found");
                jLabel.doLayout();
                jPanel.add(jLabel);
            }
        } else if (tempObject.isImdi()) {
            JLabel jLabel = new JLabel(preSpaces + "Local file (editable)");
//            jLabel.setIcon(ImdiTreeObject.imdiIcons.localicon);
//            jLabel.setVerticalTextPosition(JLabel.RIGHT);
            jLabel.doLayout();
            jPanel.add(jLabel);
        }
        if (tempObject.needsChangesSentToServer()) {
            JLabel jLabel = new JLabel(preSpaces + "Local changes not sent to the server");
//            jLabel.setIcon(ImdiTreeObject.imdiIcons.exclamationBlueIcon);
//            jLabel.setVerticalTextPosition(JLabel.RIGHT);
            jLabel.doLayout();
            jPanel.add(jLabel);
        }
        if (tempObject.imdiNeedsSaveToDisk) {
            JLabel jLabel = new JLabel(preSpaces + "Unsaved changes");
//            jLabel.setIcon(ImdiTreeObject.imdiIcons.exclamationRedIcon);
//            jLabel.setVerticalTextPosition(JLabel.RIGHT);
            jLabel.doLayout();
            jPanel.add(jLabel);
        }
        if (tempObject.isTemplate()) {
            JLabel jLabel = new JLabel(preSpaces + "Available in the templates menu");
            jLabel.doLayout();
            jPanel.add(jLabel);
        }
    }

    public void updateList() {
        System.out.println("updateList: " + targetObject);
        jPanel.removeAll();
        if (targetObject != null) {
            if (targetObject instanceof Object[]) {
                for (int childCounter = 0; childCounter < ((Object[]) targetObject).length; childCounter++) {
                    addIconLabel(((Object[]) targetObject)[childCounter]);
                }
            } else if (targetObject instanceof ImdiTreeObject) {
                addIconLabel(targetObject);
                addLabelsForImdiObject((ImdiTreeObject) targetObject);
            } else {
                addDetailLabel(targetObject.toString());
            //JTextField
//                JTextArea jTextArea = new JTextArea();
//                jTextArea.setText(targetObject.toString());
//                jTextArea.setBackground(getBackground());
//                jTextArea.setLineWrap(true);
//                jTextArea.setMaximumSize(new Dimension(300, 300));
//                jTextArea.setColumns(50);
//                jTextArea.doLayout();
//                jPanel.add(jTextArea);
            }
            jPanel.doLayout();
            doLayout();
//            revalidate();
//            validate();
        }
    }

    public String getTipText() {
        // return a zero length string to prevent the tooltip text overlaying the custom tip component
        return "";
    }

    public Dimension getPreferredSize() {
        return jPanel.getPreferredSize();
    }

    public void setTartgetObject(Object targetObjectLocal) {
        targetObject = targetObjectLocal;
    }
}
