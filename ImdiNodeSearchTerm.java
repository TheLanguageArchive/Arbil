/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

/**
 * Document   : ImdiNodeSearchTerm
 * Created on : Feb 17, 2009, 3:11:54 PM
 * @author petwit
 */
public class ImdiNodeSearchTerm extends javax.swing.JPanel {

    javax.swing.JPanel thisPanel = this;
    ImdiNodeSearchPanel parentPanel;
    private javax.swing.JComboBox notComboBox;
    private javax.swing.JComboBox booleanComboBox;
    private javax.swing.JComboBox nodeTypeComboBox;
    private javax.swing.JButton removeButton;
    private javax.swing.JTextField searchField;
    public boolean notEqual = false;
    public boolean booleanAnd = false;
    public String nodeType = "";
    public String searchString = "";

    public ImdiNodeSearchTerm(ImdiNodeSearchPanel parentPanelLocal) {
        parentPanel = parentPanelLocal;
        nodeTypeComboBox = new javax.swing.JComboBox();
        searchField = new javax.swing.JTextField();
        notComboBox = new javax.swing.JComboBox();
        booleanComboBox = new javax.swing.JComboBox();
        removeButton = new javax.swing.JButton();

        this.setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        booleanComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"and", "or"}));
        booleanComboBox.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                parentPanel.stopSearch();
            }
        });
        this.add(booleanComboBox);

        nodeTypeComboBox.setModel(
                new javax.swing.DefaultComboBoxModel(new String[]{"All",
                    "Corpus", "Session", "Actor", "Language", "MediaFile", "Source", "WrittenResource"
                }));
        nodeTypeComboBox.addActionListener(
                new java.awt.event.ActionListener() {

                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        parentPanel.stopSearch();
                    }
                });

        this.add(nodeTypeComboBox);

        searchField.addKeyListener(new java.awt.event.KeyAdapter() {

            public void keyReleased(java.awt.event.KeyEvent evt) {
                parentPanel.stopSearch();
            }
        });
        this.add(searchField);

        notComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"==", "!="}));
        notComboBox.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                parentPanel.stopSearch();
            }
        });
        this.add(notComboBox);

        removeButton.setText("-");
        removeButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                parentPanel.searchTermsPanel.remove(thisPanel);
                parentPanel.revalidate();
                parentPanel.stopSearch();
            }
        });
        this.add(removeButton);
    }

    public void populateSearchTerm() {
        searchString = searchField.getText();
        nodeType = nodeTypeComboBox.getSelectedItem().toString();
        if (booleanComboBox.isVisible()) {
            booleanAnd = booleanComboBox.getSelectedItem().toString().equals("and");
        } else {
            booleanAnd = true;
        }
        notEqual = notComboBox.getSelectedItem().toString().equals("!=");
    }

    public void setBooleanVisible(boolean visibleValue) {
        booleanComboBox.setVisible(visibleValue);
    }
}
