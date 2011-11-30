package nl.mpi.arbil.ui.wizard.setup;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Insets;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import nl.mpi.arbil.ui.GuiHelper;
import nl.mpi.arbil.ui.wizard.ArbilWizardContent;

/**
 * Abstract Wizard content that has vertical box layout with JTextPane on top (BorderLayout.NORTH)
 * Contents of JTextPane come from resource with specified location
 * @see BorderLayout
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class TextInstructionWizardContent extends JPanel implements ArbilWizardContent {

    /**
     * @param resourceLocation Location of text (optionally HTML) resource to show as introduction
     */
    public TextInstructionWizardContent(String resourceLocation) {
	super();
	setLayout(new BorderLayout());
	setBackground(Color.WHITE);

	JTextPane textPane = new JTextPane();
	textPane.setEditable(false);
	textPane.addHyperlinkListener(hyperLinkListener);
	textPane.setMargin(new Insets(5, 10, 5, 10));
	textPane.setBackground(Color.WHITE);
	try {
	    textPane.setPage(JTextPane.class.getResource(resourceLocation));
	} catch (IOException ex) {
	    textPane.setText("Error while getting wizard text. Please check the error log.");
	    GuiHelper.linorgBugCatcher.logError("I/O exception while getting wizard text from " + JTextPane.class.getResource(resourceLocation), ex);
	}
	add(textPane, BorderLayout.NORTH);
    }

    public JComponent getContent() {
	return this;
    }
    
    public void beforeShow(){
    }

    public boolean beforeNext() {
	return true;
    }

    public boolean beforePrevious() {
	return true;
    }

    public void refresh() {
    }
    private static HyperlinkListener hyperLinkListener = new HyperlinkListener() {

	public void hyperlinkUpdate(HyperlinkEvent e) {
	    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
		if (Desktop.isDesktopSupported()) {
		    try {
			Desktop.getDesktop().browse(e.getURL().toURI());
		    } catch (IOException ex) {
			// ignore
		    } catch (URISyntaxException ex) {
			// ignore
		    }
		}
	    }
	}
    };
}
