package nl.mpi.arbil.ui;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * Document   : ArbilNodeSearchTerm
 * Created on : Feb 17, 2009, 3:11:54 PM
 * @author Peter.Withers@mpi.nl
 */
public class ArbilNodeSearchTerm extends javax.swing.JPanel {

    private javax.swing.JPanel thisPanel = this;
    private ArbilNodeSearchPanel parentPanel;
    private javax.swing.JComboBox notComboBox;
    private javax.swing.JComboBox booleanComboBox;
    private javax.swing.JComboBox nodeTypeComboBox;
    private javax.swing.JButton removeButton;
    private javax.swing.JTextField searchField;
    private javax.swing.JTextField searchColumn;
    private boolean notEqual = false;
    private boolean booleanAnd = false;
    private String nodeType = "";
    private String searchString = "";
    private String searchFieldName = "";
    private String columnFieldMessage = "<column (optional)>";
    private String valueFieldMessage = "<value (optional)>";

    public ArbilNodeSearchTerm(ArbilNodeSearchPanel parentPanelLocal) {
        parentPanel = parentPanelLocal;
        nodeTypeComboBox = new javax.swing.JComboBox();
        searchField = new javax.swing.JTextField(valueFieldMessage);
        searchField.setForeground(Color.lightGray);
        searchColumn = new javax.swing.JTextField(columnFieldMessage);
        searchColumn.setForeground(Color.lightGray);
        notComboBox = new javax.swing.JComboBox();
        booleanComboBox = new javax.swing.JComboBox();
        removeButton = new javax.swing.JButton();

        this.setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        booleanComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"and", "or"}));
        booleanComboBox.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    parentPanel.stopSearch();
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        this.add(booleanComboBox);

        nodeTypeComboBox.setModel(
                new javax.swing.DefaultComboBoxModel(new String[]{"All",
                    "Corpus", "Session", "Catalogue", "Actor", "Language", "MediaFile", "Source", "WrittenResource"
                }));
        nodeTypeComboBox.addActionListener(
                new java.awt.event.ActionListener() {

                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        try {
                            parentPanel.stopSearch();
                        } catch (Exception ex) {
                            GuiHelper.linorgBugCatcher.logError(ex);
                        }
                    }
                });

        this.add(nodeTypeComboBox);

        searchField.addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals(valueFieldMessage)) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }

            public void focusLost(FocusEvent e) {
                if (searchField.getText().length() == 0) {
                    searchField.setText(valueFieldMessage);
                    searchField.setForeground(Color.lightGray);
                }
            }
        });

        searchColumn.addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent e) {
                if (searchColumn.getText().equals(columnFieldMessage)) {
                    searchColumn.setText("");
                    searchColumn.setForeground(Color.BLACK);
                }
            }

            public void focusLost(FocusEvent e) {
                if (searchColumn.getText().length() == 0) {
                    searchColumn.setText(columnFieldMessage);
                    searchColumn.setForeground(Color.lightGray);
                }
            }
        });

        searchColumn.addKeyListener(new java.awt.event.KeyAdapter() {

            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                parentPanel.stopSearch();
            }
        });
        this.add(searchColumn);

        searchField.addKeyListener(new java.awt.event.KeyAdapter() {

            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                parentPanel.stopSearch();
            }
        });
        this.add(searchField);

        notComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"==", "!="}));
        notComboBox.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    parentPanel.stopSearch();
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        this.add(notComboBox);

        removeButton.setText("-");
        removeButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    parentPanel.getSearchTermsPanel().remove(thisPanel);
                    parentPanel.revalidate();
                    parentPanel.stopSearch();
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        this.add(removeButton);
    }

    public void populateSearchTerm() {
        if (searchField.getText().equals(valueFieldMessage)) {
            setSearchString("");
        } else {
            setSearchString(searchField.getText());
        }
        if (searchColumn.getText().equals(columnFieldMessage)) {
            setSearchFieldName("");
        } else {
            setSearchFieldName(searchColumn.getText());
        }
        setNodeType(nodeTypeComboBox.getSelectedItem().toString());
        if (booleanComboBox.isVisible()) {
            setBooleanAnd(booleanComboBox.getSelectedItem().toString().equals("and"));
        } else {
            setBooleanAnd(true);
        }
        setNotEqual(notComboBox.getSelectedItem().toString().equals("!="));
    }

    public void setBooleanVisible(boolean visibleValue) {
        booleanComboBox.setVisible(visibleValue);
    }

    /**
     * @return the notEqual
     */
    public boolean isNotEqual() {
	return notEqual;
    }

    /**
     * @param notEqual the notEqual to set
     */
    public void setNotEqual(boolean notEqual) {
	this.notEqual = notEqual;
    }

    /**
     * @return the booleanAnd
     */
    public boolean isBooleanAnd() {
	return booleanAnd;
    }

    /**
     * @param booleanAnd the booleanAnd to set
     */
    public void setBooleanAnd(boolean booleanAnd) {
	this.booleanAnd = booleanAnd;
    }

    /**
     * @return the nodeType
     */
    public String getNodeType() {
	return nodeType;
    }

    /**
     * @param nodeType the nodeType to set
     */
    public void setNodeType(String nodeType) {
	this.nodeType = nodeType;
    }

    /**
     * @return the searchString
     */
    public String getSearchString() {
	return searchString;
    }

    /**
     * @param searchString the searchString to set
     */
    public void setSearchString(String searchString) {
	this.searchString = searchString;
    }

    /**
     * @return the searchFieldName
     */
    public String getSearchFieldName() {
	return searchFieldName;
    }

    /**
     * @param searchFieldName the searchFieldName to set
     */
    public void setSearchFieldName(String searchFieldName) {
	this.searchFieldName = searchFieldName;
    }
}
