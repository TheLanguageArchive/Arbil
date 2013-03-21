/**
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
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
package nl.mpi.arbil;

import java.net.URI;
import java.net.URL;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.ProgressMonitor;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilNode;
import nl.mpi.arbil.ui.AbstractArbilTableModel;
import nl.mpi.arbil.util.WindowManager;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class MockWindowManager implements WindowManager {

    public JFrame getMainFrame() {
	return null;
    }

    public void closeAllWindows() {
    }

    public void openFloatingTable(ArbilDataNode[] rowNodesArray, String frameTitle) {
    }

    public void openFloatingTableOnce(URI[] rowNodesArray, String frameTitle) {
    }

    public void openFloatingTableOnce(ArbilDataNode[] rowNodesArray, String frameTitle) {
    }

    public void openSearchTable(ArbilNode[] selectedNodes, String frameTitle) {
    }

    public void openFloatingSubnodesWindows(ArbilDataNode[] arbilDataNodes) {
    }

    public void openUrlWindowOnce(String frameTitle, URL locationUrl) {
    }

    public ProgressMonitor newProgressMonitor(Object message, String note, int min, int max) {
	return null;
    }

    public AbstractArbilTableModel openFloatingTableOnceGetModel(URI[] rowNodesArray, String frameTitle) {
	return null;
    }

    public AbstractArbilTableModel openAllChildNodesInFloatingTableOnce(URI[] rowNodesArray, String frameTitle) {
	return null;
    }

    public void saveWindowStates() {
    }

    public boolean openFileInExternalApplication(URI targetUri) {
	return true;
    }

    public void openImdiXmlWindow(Object userObject, boolean formatXml, boolean launchInBrowser) {
    }
    
}
