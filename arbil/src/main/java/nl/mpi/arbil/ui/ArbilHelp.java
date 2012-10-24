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

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import nl.mpi.arbil.help.ArbilHelpResources;
import org.xml.sax.SAXException;

/**
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilHelp extends HelpViewerPanel {

    // IMDI resource set
    public final static String IMDI_HELPSET = "IMDI";
    public static final String IMDI_URL_PATTERN = ArbilHelpResources.IMDI_URL_PATTERN;
    private final static HelpResourceSet IMDI_HELP_SET = new HelpResourceSet(IMDI_HELPSET, ArbilHelpResources.class, ArbilHelpResources.IMDI_HELP_RESOURCE_BASE, ArbilHelpResources.IMDI_HELP_INDEX_XML);
    // CMDI resource set
    public final static String CMDI_HELPSET = "CMDI";
    public static final String CMDI_URL_PATTERN = ArbilHelpResources.CMDI_URL_PATTERN;
    private final static HelpResourceSet CMDI_HELP_SET = new HelpResourceSet(CMDI_HELPSET, ArbilHelpResources.class, ArbilHelpResources.CMDI_HELP_RESOURCE_BASE, ArbilHelpResources.CMDI_HELP_INDEX_XML);
    // Singleton
    private static ArbilHelp singleInstance = null;

    public static synchronized ArbilHelp getArbilHelpInstance() throws IOException, SAXException {
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
	if (itemURL.toString().contains(IMDI_URL_PATTERN)) {
	    return showHelpItem(IMDI_HELPSET, itemURL);
	} else if (itemURL.toString().contains(CMDI_URL_PATTERN)) {
	    return showHelpItem(CMDI_HELPSET, itemURL);
	} else {
	    // Refuse to deal with other types of URLS
	    return false;
	}
    }
}
