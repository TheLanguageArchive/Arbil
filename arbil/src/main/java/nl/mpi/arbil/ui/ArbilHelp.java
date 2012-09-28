package nl.mpi.arbil.ui;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import org.xml.sax.SAXException;

/**
 * Document : ArbilHelp.java
 * Created on : March 9, 2009, 1:38 PM
 *
 * @author Peter Withers <Peter.Withers@mpi.nl>
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilHelp extends HelpViewerPanel {

    public final static String IMDI_HELPSET = "IMDI";
    public final static String CMDI_HELPSET = "CMDI";
    private final static String IMDI_HELP_RESOURCE_BASE = "/nl/mpi/arbil/resources/html/help/arbil-imdi/";
    private final static HelpResourceSet IMDI_HELP_SET = new HelpResourceSet(IMDI_HELPSET, ArbilHelp.class, IMDI_HELP_RESOURCE_BASE, IMDI_HELP_RESOURCE_BASE + "arbil-imdi.xml");
    private final static String CMDI_HELP_RESOURCE_BASE = "/nl/mpi/arbil/resources/html/help/arbil-cmdi/";
    private final static HelpResourceSet CMDI_HELP_SET = new HelpResourceSet(CMDI_HELPSET, ArbilHelp.class, CMDI_HELP_RESOURCE_BASE, CMDI_HELP_RESOURCE_BASE + "arbil-cmdi.xml");
    private static ArbilHelp singleInstance = null;

    public static synchronized ArbilHelp getArbilHelpInstance() throws IOException, SAXException {
	//TODO: This should not be a singleton...
	if (singleInstance == null) {

	    singleInstance = new ArbilHelp();
	}
	return singleInstance;
    }

    public ArbilHelp() throws IOException, SAXException {
	super(Arrays.asList(IMDI_HELP_SET, CMDI_HELP_SET));
    }

    @Override
    public boolean showHelpItem(URL itemURL) {
	if (itemURL.toString().contains("arbil-imdi")) {
	    return showHelpItem(IMDI_HELPSET, itemURL);
	} else if (itemURL.toString().contains("arbil-cmdi")) {
	    return showHelpItem(CMDI_HELPSET, itemURL);
	} else {
	    // Refuse to deal with other types of URLS
	    return false;
	}
    }
}
