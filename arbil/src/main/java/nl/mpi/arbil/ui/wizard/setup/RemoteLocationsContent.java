/**
 * Copyright (C) 2016 The Language Archive, Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.arbil.ui.wizard.setup;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.arbil.util.TreeHelper;

/**
 * ArbilWizard content that lets the user specify remote locations
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class RemoteLocationsContent extends TextInstructionWizardContent {

    private static TreeHelper treeHelper;

    public static void setTreeHelper(TreeHelper treeHelperInstance) {
	treeHelper = treeHelperInstance;
    }
    private ArbilSetupWizardModel model;
    private JTextArea locationsTextArea;
    public final static String imdiDefaultsResource = "/nl/mpi/arbil/defaults/imdiLocations";
    public final static String cmdiDefaultsResource = "/nl/mpi/arbil/defaults/cmdiLocations";
    private static final ResourceBundle widgets = ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets");

    public RemoteLocationsContent(ArbilSetupWizardModel model) {
	super("/nl/mpi/arbil/resources/html/wizard/RemoteLocations.html");
	this.model = model;

	locationsTextArea = new JTextArea();
	locationsTextArea.setEditable(true);
	locationsTextArea.setRows(6);

	JScrollPane locationsScrollPane = new JScrollPane(locationsTextArea);
	locationsScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	locationsScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	add(locationsScrollPane, BorderLayout.CENTER);
    }

    @Override
    public void beforeShow() {
	if (model.getRemoteLocations() == null) {
	    model.setRemoteLocations(loadRemoteLocations());
	}
    }

    @Override
    public void refresh() {
	locationsTextArea.setText(model.getRemoteLocations());
	locationsTextArea.requestFocusInWindow();
    }

    @Override
    public boolean beforeNext() {
	model.setRemoteLocations(locationsTextArea.getText());
	if (model.getRemoteLocations().trim().length() > 0) {
	    return true;
	} else {
	    return JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(this,
		    widgets.getString("SETUP_YOU HAVE NOT YET SPECIFIED ANY REMOTE LOCATIONS. ARE YOU SURE YOU WANT TO CONTINUE?"),
		    widgets.getString("SETUP_NO LOCATIONS SPECIFIED"),
		    JOptionPane.OK_CANCEL_OPTION,
		    JOptionPane.WARNING_MESSAGE);
	}
    }

    @Override
    public boolean beforePrevious() {
	if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(this,
		widgets.getString("DOING THIS WILL RESET THE LIST OF REMOTE LOCATIONS. DO YOU WANT TO GO BACK A STEP IN THE WIZARD?"),
		widgets.getString("DISCARD CHANGES"),
		JOptionPane.OK_CANCEL_OPTION,
		JOptionPane.WARNING_MESSAGE)) {
	    model.setRemoteLocations(null);
	    return true;
	} else {
	    return false;
	}
    }

    public Object getNext() {
	return ArbilSetupWizard.CONFIRMATION;
    }

    public Object getPrevious() {
	if (model.isCmdiSelected()) {
	    return ArbilSetupWizard.CMDI_PROFILE_SELECT;
	} else {
	    return ArbilSetupWizard.METADATA_FORMAT_SELECT;
	}
    }

    /**
     * Combines the current remote locations and the deafult remote locations from the selected
     * metadata format(s)
     * @return Newline separated concatenation of remote resources
     */
    private String loadRemoteLocations() {
	final String newLine = System.getProperty("line.separator");

	final List<String> locationsList = new LinkedList<String>();

	// Load current locations
	for (ArbilDataNode remoteNode : treeHelper.getRemoteCorpusNodes()) {
	    locationsList.add(remoteNode.getUrlString());
	}

	if (model.isImdiSelected()) {
	    //Load imdi locations
	    try {
		addLocationsFromResource(imdiDefaultsResource, locationsList);
	    } catch (IOException ex) {
		BugCatcherManager.getBugCatcher().logError("Error while reading default IMDI locations", ex);
	    }
	}

	if (model.isCmdiSelected()) {
	    // Load cmdi locations
	    try {
		addLocationsFromResource(cmdiDefaultsResource, locationsList);
	    } catch (IOException ex) {
		BugCatcherManager.getBugCatcher().logError("Error while reading default CMDI locations", ex);
	    }
	}

	final StringBuilder locations = new StringBuilder();
	for (String location : locationsList) {
	    locations.append(location).append(newLine);
	}
	return locations.toString();
    }

    /**
     * Reads locations from a resource and adds them to a list of resource location strings
     * @param resourceLocation Resource location to read from
     * @param locations The list of resource locations to add to
     * @throws IOException In case of failure to read locations from remote resource
     */
    private void addLocationsFromResource(String resourceLocation, List<String> locations) throws IOException {
	final InputStream is = getClass().getResourceAsStream(resourceLocation);
	if (is == null) {
	    throw new IOException("Resource not found: " + resourceLocation);
	}
	final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	String location = reader.readLine();
	while (location != null) {
	    location = location.trim();
	    if (location.length() > 0) {
		if (!locations.contains(location)) {
		    locations.add(location);
		}
	    }
	    location = reader.readLine();
	}
    }
}
