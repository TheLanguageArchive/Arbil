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
package nl.mpi.arbil.ui.wizard;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dialog.ModalityType;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

/**
 * Wizard to be shown on first run to guide user through initial setup, mainly configuring for either
 * IMDI or CMDI (or both) and selecting profiles and remote locations.
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class ArbilWizard {

    private JDialog wizardDialog;
    private JPanel wizardContentPanel;
    private CardLayout wizardContentPanelLayout;
    private JButton previousButton;
    private JButton nextButton;
    private JButton cancelButton;
    private JButton finishButton;
    private final Frame owner;
    private final ArbilWizardModel model;
    private final static String NEXT_ACTION = "wizard_action_next";
    private final static String PREVIOUS_ACTION = "wizard_action_previous";
    private final static String CANCEL_ACTION = "wizard_action_cancel";
    private final static String FINISH_ACTION = "wizard_action_finish";

    public ArbilWizard() {
	this(null);
    }

    /**
     * 
     * @param owner Owner frame of dialog. Pass null (or use default constructor) to use Swing defaults
     */
    public ArbilWizard(Frame owner) {
	this.owner = owner;
	initDialog();
	model = new ArbilWizardModel();
    }
    
    public void showDialog(){
	showDialog(false);
    }
    
    public void showModalDialog(){
	showDialog(true);
    }

    private void showDialog(boolean modal) {
	refreshContent();
	if (owner != null) {
	    wizardDialog.setLocationRelativeTo(owner);
	}
	wizardDialog.setModal(modal);
	wizardDialog.setVisible(true);
    }

    public void refreshContent() {
	Object id = model.getCurrentId();
	if (id != null) {
	    final ArbilWizardContent content = model.getCurrent();
	    previousButton.setEnabled(content.getPrevious() != null);
	    nextButton.setVisible(content.getNext() != null);
	    finishButton.setVisible(content.getNext() == null);
	    content.beforeShow();
	    content.refresh();
	    wizardContentPanelLayout.show(wizardContentPanel, id.toString());
	}
	wizardDialog.pack();
    }

    public void addContent(Object id, ArbilWizardContent content) {
	wizardContentPanel.add(content.getContent(), id.toString());
	model.addContent(id, content);
    }

    public void setCurrent(Object id) {
	model.setCurrent(id);
	refreshContent();
    }

    private void initDialog() {
	wizardContentPanel = new JPanel();
	wizardContentPanelLayout = new CardLayout();
        
	wizardContentPanel.setLayout(wizardContentPanelLayout);
        wizardContentPanel.setBackground(Color.WHITE);

	JPanel buttonsPanel = new JPanel(new FlowLayout(SwingConstants.RIGHT));
	buttonsPanel.setBorder(
		BorderFactory.createCompoundBorder(
		BorderFactory.createEmptyBorder(5, 2, 0, 2), // outside empty border (sides and top)
		BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY))); // inside top line
        buttonsPanel.setBackground(Color.WHITE);

	previousButton = new JButton("Previous");
	previousButton.setActionCommand(PREVIOUS_ACTION);
	previousButton.addActionListener(buttonListener);
	buttonsPanel.add(previousButton);

	nextButton = new JButton("Next");
	nextButton.setActionCommand(NEXT_ACTION);
	nextButton.addActionListener(buttonListener);
	buttonsPanel.add(nextButton);

	finishButton = new JButton("Finish");
	finishButton.setActionCommand(FINISH_ACTION);
	finishButton.addActionListener(buttonListener);
	buttonsPanel.add(finishButton);

	cancelButton = new JButton("Cancel");
	cancelButton.setActionCommand(CANCEL_ACTION);
	cancelButton.addActionListener(buttonListener);
	buttonsPanel.add(cancelButton);

	if (owner != null) {
	    wizardDialog = new JDialog(owner);
	} else {
	    wizardDialog = new JDialog();
	}
	wizardDialog.setTitle("Arbil wizard");
	wizardDialog.getContentPane().setLayout(new BorderLayout());
        wizardDialog.getContentPane().setBackground(Color.WHITE);
	wizardDialog.getContentPane().add(wizardContentPanel, BorderLayout.CENTER);
	wizardDialog.getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

	wizardDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	wizardDialog.addWindowListener(new WindowAdapter() {

	    @Override
	    public void windowClosing(WindowEvent e) {
		doCancel();
	    }
	});
    }
    private ActionListener buttonListener = new ActionListener() {

	public void actionPerformed(ActionEvent e) {
	    if (e.getActionCommand().equals(PREVIOUS_ACTION)) {
		doPrevious();
	    } else if (e.getActionCommand().equals(NEXT_ACTION)) {
		doNext();
	    } else if (e.getActionCommand().equals(FINISH_ACTION)) {
		doFinish();
	    } else if (e.getActionCommand().equals(CANCEL_ACTION)) {
		doCancel();
	    }
	}
    };
    
    protected void doNext() {
	if (model.getCurrent() != null) {
	    if (!model.getCurrent().beforeNext()) {
		return;
	    }
	}
	model.next();
	refreshContent();
    }

    protected void doPrevious() {
	if (model.getCurrent() != null) {
	    if (!model.getCurrent().beforePrevious()) {
		return;
	    }
	}
	model.previous();
	refreshContent();
    }

    protected boolean doFinish() {
	if (onFinish()) {
	    wizardDialog.dispose();
	    return true;
	} else {
	    return false;
	}
    }

    protected boolean doCancel() {
	if (onCancel()) {
	    wizardDialog.dispose();
	    return true;
	} else {
	    return false;
	}
    }

    protected JDialog getWizardDialog() {
	return wizardDialog;
    }

    abstract protected boolean onFinish();

    abstract protected boolean onCancel();
}
