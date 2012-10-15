package nl.mpi.arbil.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.html.HTMLDocument;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class HtmlViewPane extends JTextPane {
    
    public HtmlViewPane() {
	setContentType("text/html;charset=UTF-8");
	setEditable(false);
    }
    
    public HtmlViewPane(URL url) throws IOException {
	this();
	setContents(url);
    }
    
    public JScrollPane createScrollPane() {
	JScrollPane scrollPane = new JScrollPane();
	scrollPane.setViewportView(this);
	return scrollPane;
    }
    
    public void setDocumentBase(Class resourceRefClass, String location) {
	((HTMLDocument) getDocument()).setBase(resourceRefClass.getResource(location));
    }
    
    public final void setContents(URL url) throws IOException {
	setContents(url.openStream());
    }
    
    public final void setContents(final InputStream itemStream) throws IOException {
	if (itemStream == null) {
	    setText("Page not found");
	} else {
	    final StringBuilder completeHelpText = new StringBuilder();
	    BufferedReader bufferedHelpReader = new BufferedReader(new InputStreamReader(itemStream, "UTF-8"));
	    try {
		for (String helpLine = bufferedHelpReader.readLine(); helpLine != null; helpLine = bufferedHelpReader.readLine()) {
		    completeHelpText.append(helpLine);
		}
	    } finally {
		bufferedHelpReader.close();
	    }
	    setText(completeHelpText.toString());
	}
	// Scroll to top
	setCaretPosition(0);
    }
}
