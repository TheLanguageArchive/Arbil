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

import nl.mpi.arbil.data.ArbilField;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *  Document   : FindReplacePanel
 *  Created on : Jun 21, 2010, 10:13:22 AM
 *  Author     : Peter Withers
 */
public class FindReplacePanel extends JPanel implements ActionListener, FocusListener {

    String defaultFindText = "<enter find text>";
    String defaultReplaceText = "<enter replacement text>";
    ArbilSplitPanel splitPanel;
    JButton closeButton;
    JTextField seachField;
    JTextField replaceField;
    JCheckBox useJavaRegExCheckBox;
    JCheckBox replaceAllCheckBox;
    JButton searchButton;
    JButton replaceButton;

    public FindReplacePanel(ArbilSplitPanel splitPanelLocal) {	
	splitPanel = splitPanelLocal;
	seachField = new JTextField(defaultFindText);
	replaceField = new JTextField(defaultReplaceText);
	seachField.setName(defaultFindText);
	replaceField.setName(defaultReplaceText);
	seachField.setForeground(Color.gray);
	replaceField.setForeground(Color.gray);
	useJavaRegExCheckBox = new JCheckBox("Regular Expression");
	replaceAllCheckBox = new JCheckBox("Replace All");
	useJavaRegExCheckBox.setToolTipText("Use Java Style Regular Expressions (eg: "
		+ ". any "
		+ "\\d digit "
		+ "\\s whitespace "
		+ "? zero or once "
		+ "* zero or more "
		+ "+ one or more "
		+ "^ line start "
		+ "$ line end)");

	searchButton = new JButton("Find Next");
	replaceButton = new JButton("Replace Selected");
	closeButton = new JButton("Close");
	seachField.addActionListener(this);
	replaceField.addActionListener(this);
//        useJavaRegExCheckBox.addActionListener(this);
	searchButton.addActionListener(this);
	replaceButton.addActionListener(this);
	closeButton.addActionListener(this);
	seachField.addFocusListener(this);
	replaceField.addFocusListener(this);
	this.add(seachField);
	this.add(replaceField);
//        this.add(useJavaRegExCheckBox);
//        this.add(replaceAllCheckBox);
	this.add(searchButton);
	this.add(replaceButton);
	this.add(closeButton);

    }

    public void actionPerformed(ActionEvent actionEvent) {
	System.out.println(actionEvent.toString());
	if (actionEvent.getSource().equals(closeButton)) {
	    splitPanel.showSearchPane();
	    return;
	}
	String searchString = "";
	if (!seachField.getText().equals(defaultFindText)) {
	    searchString = seachField.getText();
	}
	String replaceString = null;
	if (actionEvent.getSource().equals(replaceButton)) {
	    replaceString = "";
	    if (!replaceField.getText().equals(defaultReplaceText)) {
		replaceString = replaceField.getText();
	    }
	}

	if (actionEvent.getSource().equals(replaceButton)) {
	    for (ArbilField currentField : splitPanel.arbilTable.getSelectedFields()) {
		if (currentField.getParentDataNode().isEditable() && currentField.getFieldValue().contains(searchString)) {
		    String currentFieldString = currentField.getFieldValue();
		    if (currentFieldString.contains(searchString)) {
			currentFieldString = currentFieldString.replace(searchString, replaceString);
			currentField.setFieldValue(currentFieldString, true, false);
		    }
		}
	    }
	    // in the case where a user has selected a number of rows to replace in we do not want to clear their selection
	    return;
	}

	boolean replaceAll = (actionEvent.getSource().equals(replaceButton) && replaceAllCheckBox.isSelected());

	int startColumn = splitPanel.arbilTable.getSelectedColumn();
	int startRow = splitPanel.arbilTable.getSelectedRow();
	boolean firstTime = true;
	if (startColumn < 0) {
	    firstTime = false;
	    startColumn = 0;
	}
	if (startRow < 0) {
	    firstTime = false;
	    startRow = 0;
	}
	for (int currentRow = startRow; currentRow < splitPanel.arbilTable.getRowCount(); currentRow++) {
	    for (int currentColumn = startColumn; currentColumn < splitPanel.arbilTable.getColumnCount(); currentColumn++) {
		if (firstTime) {
		    firstTime = false;
		} else {
		    Object currentCellValue = splitPanel.arbilTable.getTableCellContentAt(currentRow, currentColumn);
		    if (currentCellValue instanceof ArbilField) {
			currentCellValue = new ArbilField[]{(ArbilField) currentCellValue};
		    }
		    if (currentCellValue instanceof ArbilField[]) {
			for (ArbilField currentField : (ArbilField[]) currentCellValue) {
			    if (currentField.getFieldValue().contains(searchString)) {
				splitPanel.arbilTable.setCellSelectionEnabled(true);
				splitPanel.arbilTable.changeSelection(currentRow, currentColumn, false, false);
//                                if (!replaceAll) {
				return;
//                                }
			    }
			}
		    }
		}
	    }
	    startColumn = 0;
	}
    }

    public void focusGained(FocusEvent e) {
	JTextField currentTextField = (JTextField) e.getSource();
	if (currentTextField.getText().equals(defaultFindText) || currentTextField.getText().equals(defaultReplaceText)) {
	    currentTextField.setText("");
	}
	currentTextField.setForeground(Color.black);
    }

    public void focusLost(FocusEvent e) {
	JTextField currentTextField = (JTextField) e.getSource();
	if (currentTextField.getText().length() < 1) {// || currentTextField.getText().equals(currentTextField.getName())) {
	    currentTextField.setText(currentTextField.getName());
	    currentTextField.setForeground(Color.gray);
	}
    }
    
    public void requestFocusOnSearchField(){
	seachField.requestFocus();
    }
}
