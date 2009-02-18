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
import javax.swing.JTextArea;
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

    private void addIconLabel(Object tempObject) {
        JLabel jLabel = new JLabel(tempObject.toString());
        if (tempObject instanceof ImdiTreeObject) {
            jLabel.setIcon(((ImdiTreeObject) tempObject).getIcon());
        }
        jLabel.doLayout();
        jPanel.add(jLabel);
    }

    private void addDetailLabel(String prefixString, Object tempObject) {
        if (tempObject != null) {
            String labelString = tempObject.toString();
            if (labelString.length() > 100) {
                labelString = labelString.substring(0, 100) + "...";
            }
            JLabel jLabel = new JLabel(preSpaces + prefixString + labelString);
            jLabel.doLayout();
            jPanel.add(jLabel);
        }
    }

    private void addLabelsForImdiObject(ImdiTreeObject tempObject) {

        if (tempObject.isImdi()) {
            Hashtable tempFields = tempObject.getFields();
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
                //JTextField
                JTextArea jTextArea = new JTextArea();
                jTextArea.setText(targetObject.toString());
                jTextArea.setBackground(getBackground());
//                    jTextArea.setLineWrap(true);                    
//                    jTextArea.setColumns(100);
                jTextArea.doLayout();
                jPanel.add(jTextArea);
            }
            jPanel.doLayout();
            doLayout();
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
