package nl.mpi.arbil.ui;

import nl.mpi.arbil.search.ArbilNodeSearchTerm;
import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JPanel;

/**
 * Document   : ArbilNodeSearchTermPanel
 * Created on : Feb 17, 2009, 3:11:54 PM
 * @author Peter.Withers@mpi.nl
 */
public class ArbilNodeSearchTermPanel extends JPanel implements ArbilNodeSearchTerm {

    private javax.swing.JPanel thisPanel = this;
    private ArbilNodeSearchPanel parentPanel;
    private javax.swing.JComboBox notComboBox;
    private javax.swing.JComboBox booleanComboBox;
    private javax.swing.JComboBox nodeTypeComboBox;
    private javax.swing.JButton removeButton;
    private javax.swing.JTextField searchField;
    private ArbilNodeSearchColumnComboBox searchColumn;
    
    public ArbilNodeSearchTermPanel(ArbilNodeSearchPanel parentPanelLocal) {
	parentPanel = parentPanelLocal;
	nodeTypeComboBox = new javax.swing.JComboBox();
	searchField = new javax.swing.JTextField(VALUE_FIELD_MESSAGE);
	searchField.setForeground(Color.lightGray);
	searchColumn = new ArbilNodeSearchColumnComboBox(COLUMN_FIELD_MESSAGE,"");
	searchColumn.getTextField().setForeground(Color.lightGray);
	notComboBox = new javax.swing.JComboBox();
	booleanComboBox = new javax.swing.JComboBox();
	removeButton = new javax.swing.JButton();

	this.setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

	booleanComboBox.setModel(new javax.swing.DefaultComboBoxModel(ArbilNodeSearchTerm.BOOLEAN_TYPES));
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
		new javax.swing.DefaultComboBoxModel(NODE_TYPES));
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
		if (searchField.getText().equals(VALUE_FIELD_MESSAGE)) {
		    searchField.setText("");
		    searchField.setForeground(Color.BLACK);
		}
	    }

	    public void focusLost(FocusEvent e) {
		if (searchField.getText().length() == 0) {
		    searchField.setText(VALUE_FIELD_MESSAGE);
		    searchField.setForeground(Color.lightGray);
		}
	    }
	});

	searchColumn.getTextField().addFocusListener(new FocusListener() {

	    public void focusGained(FocusEvent e) {
		if (searchColumn.getText().equals(COLUMN_FIELD_MESSAGE)) {
		    searchColumn.setText("");
		    searchColumn.getTextField().setForeground(Color.BLACK);
		}
	    }

	    public void focusLost(FocusEvent e) {
		if (searchColumn.getText().length() == 0) {
		    searchColumn.setText(COLUMN_FIELD_MESSAGE);
		    searchColumn.getTextField().setForeground(Color.lightGray);
		}
	    }
	});

	searchColumn.getTextField().addKeyListener(new java.awt.event.KeyAdapter() {

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
	if (searchField.getText().equals(VALUE_FIELD_MESSAGE)) {
	    setSearchString("");
	} else {
	    setSearchString(searchField.getText());
	}
	if (searchColumn.getText().equals(COLUMN_FIELD_MESSAGE)) {
	    setSearchFieldName("");
	} else {
	    setSearchFieldName(searchColumn.getText());
	}
	setNodeType(nodeTypeComboBox.getSelectedItem().toString());
	if (booleanComboBox.isVisible()) {
	    setBooleanAnd(booleanComboBox.getSelectedItem().toString().equals(ArbilNodeSearchTerm.BOOLEAN_AND));
	} else {
	    setBooleanAnd(true);
	}
	setNotEqual(notComboBox.getSelectedItem().toString().equals("!="));
    }
    
    public void addCurrentSearchColumnOption(){
	if(searchFieldName != null && !"".equals(searchFieldName)){
	    searchColumn.addOption(searchFieldName);
	}
    }

    public void setBooleanVisible(boolean visibleValue) {
	booleanComboBox.setVisible(visibleValue);
    }    
    
    protected boolean notEqual = false;
    protected boolean booleanAnd = false;
    protected String nodeType = "";
    protected String searchString = "";
    protected String searchFieldName = "";

    /**
     * @return the nodeType
     */
    public String getNodeType() {
	return nodeType;
    }

    /**
     * @return the searchFieldName
     */
    public String getSearchFieldName() {
	return searchFieldName;
    }

    /**
     * @return the searchString
     */
    public String getSearchString() {
	return searchString;
    }

    /**
     * @return the booleanAnd
     */
    public boolean isBooleanAnd() {
	return booleanAnd;
    }

    /**
     * @return the notEqual
     */
    public boolean isNotEqual() {
	return notEqual;
    }

    /**
     * @param booleanAnd the booleanAnd to set
     */
    public void setBooleanAnd(boolean booleanAnd) {
	this.booleanAnd = booleanAnd;
    }

    /**
     * @param nodeType the nodeType to set
     */
    public void setNodeType(String nodeType) {
	this.nodeType = nodeType;
    }

    /**
     * @param notEqual the notEqual to set
     */
    public void setNotEqual(boolean notEqual) {
	this.notEqual = notEqual;
    }

    /**
     * @param searchFieldName the searchFieldName to set
     */
    public void setSearchFieldName(String searchFieldName) {
	this.searchFieldName = searchFieldName;
    }

    /**
     * @param searchString the searchString to set
     */
    public void setSearchString(String searchString) {
	this.searchString = searchString;
    }
}
