package nl.mpi.arbil.ui.wizard;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dialog.ModalityType;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
    private final ArbilWizardModel model;
    private final static String NEXT_ACTION = "wizard_action_next";
    private final static String PREVIOUS_ACTION = "wizard_action_previous";
    private final static String CANCEL_ACTION = "wizard_action_cancel";
    private final static String FINISH_ACTION = "wizard_action_finish";

    public ArbilWizard() {
	initDialog();
	model = new ArbilWizardModel();
    }

    public void showDialog(ModalityType modalityType) {
	wizardDialog.setModalityType(modalityType);
	refreshContent();
	wizardDialog.setVisible(true);
    }

    public void refreshContent() {
	Object id = model.getCurrentId();
	if (id != null) {
	    wizardContentPanelLayout.show(wizardContentPanel, id.toString());
	    final ArbilWizardContent content = model.getCurrent();
	    previousButton.setEnabled(content.getPrevious() != null);
	    nextButton.setVisible(content.getNext() != null);
	    finishButton.setVisible(content.getNext() == null);
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

	JPanel buttonsPanel = new JPanel(new FlowLayout(SwingConstants.RIGHT));

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

	wizardDialog = new JDialog();
	wizardDialog.setTitle("Arbil wizard");
	wizardDialog.getContentPane().setLayout(new BorderLayout());
	wizardDialog.getContentPane().add(wizardContentPanel, BorderLayout.CENTER);
	wizardDialog.getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

	wizardDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	wizardDialog.addWindowListener(new WindowAdapter() {

	    @Override
	    public void windowClosing(WindowEvent e) {
		if (doCancel()) {
		    e.getWindow().dispose();
		}
	    }
	});
    }
    private ActionListener buttonListener = new ActionListener() {

	public void actionPerformed(ActionEvent e) {
	    if (e.getActionCommand().equals(PREVIOUS_ACTION)) {
		model.previous();
		refreshContent();
	    } else if (e.getActionCommand().equals(NEXT_ACTION)) {
		model.next();
		refreshContent();
	    } else if (e.getActionCommand().equals(FINISH_ACTION)) {
		doFinish();
	    } else if (e.getActionCommand().equals(CANCEL_ACTION)) {
		doCancel();
	    }
	}
    };

    protected boolean doFinish() {
	if (onFinish()) {
	    wizardDialog.setVisible(false);
	    return true;
	} else {
	    return false;
	}
    }

    protected boolean doCancel() {
	if (onCancel()) {
	    wizardDialog.setVisible(false);
	    return true;
	} else {
	    return false;
	}
    }

    abstract protected boolean onFinish();

    abstract protected boolean onCancel();
}
