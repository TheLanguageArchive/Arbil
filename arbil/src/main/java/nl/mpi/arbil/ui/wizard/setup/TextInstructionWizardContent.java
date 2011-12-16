package nl.mpi.arbil.ui.wizard.setup;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import nl.mpi.arbil.ui.wizard.ArbilWizardContent;
import nl.mpi.arbil.util.BugCatcher;

/**
 * Abstract Wizard content that has vertical box layout with JTextPane on top (BorderLayout.NORTH)
 * Contents of JTextPane come from resource with specified location
 * @see BorderLayout
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class TextInstructionWizardContent extends JPanel implements ArbilWizardContent {

    private static BugCatcher bugCatcher;

    public static void setBugCatcher(BugCatcher bugCatherInstance) {
	bugCatcher = bugCatherInstance;
    }

    /**
     * @param resourceLocation Location of text (HTML formatted) resource to show as introduction
     */
    public TextInstructionWizardContent(String resourceLocation) {
	super();
	setLayout(new BorderLayout());
	setBackground(Color.WHITE);

	final JTextPane textPane = new JTextPane();
	textPane.setEditable(false);
	textPane.setContentType("text/html");
	textPane.addHyperlinkListener(hyperLinkListener);
	textPane.setMargin(new Insets(5, 10, 5, 10));
	textPane.setBackground(Color.WHITE);
	textPane.setText(loadContentFromResource(resourceLocation));
	add(textPane, BorderLayout.NORTH);
    }

    private String loadContentFromResource(String resourceLocation) {
	try {
	    final InputStream resourceStream = getClass().getResourceAsStream(resourceLocation);
	    if (resourceStream == null) {
		bugCatcher.logError("Cannot load wizard text. Location: " + resourceLocation, null);
	    } else {
		try {
		    final BufferedReader contentReader = new BufferedReader(new InputStreamReader(resourceStream));
		    final StringBuilder contentStringBuilder = new StringBuilder();

		    for (String line = contentReader.readLine(); line != null; line = contentReader.readLine()) {
			contentStringBuilder.append(line);
		    }
		    return contentStringBuilder.toString();
		} finally {
		    resourceStream.close();
		}
	    }
	} catch (IOException ex) {
	    bugCatcher.logError("I/O exception while getting wizard text. Location: " + resourceLocation, ex);
	}
	return "Error while getting wizard text. Please check the error log.";
    }

    public JComponent getContent() {
	return this;
    }

    public void beforeShow() {
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
