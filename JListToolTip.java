/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import java.awt.BorderLayout;
import java.awt.Dimension;
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
