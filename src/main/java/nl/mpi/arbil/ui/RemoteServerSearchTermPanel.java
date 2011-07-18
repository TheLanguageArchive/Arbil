package nl.mpi.arbil.ui;

import nl.mpi.arbil.search.ArbilRemoteSearch;
import nl.mpi.arbil.search.RemoteServerSearchTerm;
import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.net.URI;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import nl.mpi.arbil.data.ArbilDataNode;

/**
 * Document   : RemoteServerSearchTermPanel
 * Created on : Sept 22, 2010, 15:31:54 PM
 * @author Peter.Withers@mpi.nl
 */
public class RemoteServerSearchTermPanel extends JPanel implements RemoteServerSearchTerm {

    private ArbilNodeSearchPanel parentPanel;
    private JTextField searchField;
    private JLabel resultCountLabel;
    private ArbilRemoteSearch remoteSearch = new ArbilRemoteSearch();

    public RemoteServerSearchTermPanel(ArbilNodeSearchPanel parentPanelLocal) {
	parentPanel = parentPanelLocal;
	searchField = new javax.swing.JTextField(valueFieldMessage);
	searchField.setForeground(Color.lightGray);

	this.setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

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

	searchField.addKeyListener(new java.awt.event.KeyAdapter() {

	    @Override
	    public void keyReleased(java.awt.event.KeyEvent evt) {
		parentPanel.stopSearch();
	    }
	});
	resultCountLabel = new JLabel();
	this.add(searchField);
	this.add(resultCountLabel);
    }

    @Override
    public URI[] getServerSearchResults(ArbilDataNode[] arbilDataNodeArray) {
	final String searchFieldText = searchField.getText();
	if (searchFieldText.equals(valueFieldMessage) || searchFieldText.equals("")) {
	    ArbilWindowManager.getSingleInstance().addMessageDialogToQueue("No remote search term provided, cannot search remotely", "Remote Search");
	}

	URI[] searchResult = remoteSearch.getServerSearchResults(searchFieldText, arbilDataNodeArray);
	resultCountLabel.setText(searchResult.length + " found on server ");
	return searchResult;
    }
}
