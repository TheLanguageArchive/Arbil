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
package nl.mpi.arbil.ui.applet;

import java.net.URI;
import nl.mpi.arbil.ArbilDesktopInjector;
import nl.mpi.arbil.data.DataNodeLoader;
import nl.mpi.arbil.ui.ArbilSplitPanel;
import nl.mpi.arbil.ui.ArbilTable;
import nl.mpi.arbil.ui.ArbilTableModel;
import nl.mpi.arbil.ui.ImageBoxRenderer;
import nl.mpi.arbil.util.BugCatcherManager;

/*
 * ArbilTableApplet.java
 * Created on 28 September 2009, 13:10
 * @author Peter.Withers@mpi.nl
 */
public class ArbilTableApplet extends javax.swing.JApplet {

    private DataNodeLoader dataNodeLoader;

    @Override
    public void init() {
        final ArbilDesktopInjector injector = new ArbilDesktopInjector();
        // TODO: test if this suffices
        injector.injectHandlers();
        dataNodeLoader = injector.getDataNodeLoader();
        try {
            java.awt.EventQueue.invokeAndWait(new Runnable() {

                public void run() {
                    initComponents(injector.getImageBoxRenderer());
                    addNodesToTable(getParameter("ImdiFileList"));
                    addShowOnlyColumnsToTable(getParameter("ShowOnlyColumns"));
                    addChildNodesToTable(getParameter("ChildNodeColumns"));
                    addHighlightToTable(getParameter("HighlightText"));
                }
            });
        } catch (Exception ex) {
            BugCatcherManager.getBugCatcher().logError(ex);
        }
    }

    private void addNodesToTable(String nodeURLsString) {
        if (nodeURLsString != null) {
            for (String currentUrlString : nodeURLsString.split(",")) {
                try {
                    arbilTableModel.addSingleArbilDataNode(dataNodeLoader.getArbilDataNode(rootPane, new URI(currentUrlString)));
                } catch (Exception ex) {
                    BugCatcherManager.getBugCatcher().logError(ex);
                }
            }
        }
    }

    private void addShowOnlyColumnsToTable(String showColumnsString) {
        if (showColumnsString != null && showColumnsString.trim().length() > 0) {
            for (String currentshowColumns : showColumnsString.split(",")) {
                arbilTableModel.getFieldView().addShowOnlyColumn(currentshowColumns.trim());
            }
        }
    }

    private void addChildNodesToTable(String childNodesString) {
        if (childNodesString != null && childNodesString.trim().length() > 0) {
            for (String currentChildNode : childNodesString.split(",")) {
                arbilTableModel.addChildTypeToDisplay(currentChildNode.trim());
            }
        }
    }

    private void addHighlightToTable(String highlightableTextString) {
        if (highlightableTextString != null && highlightableTextString.length() > 0) {
            for (String highlightText : highlightableTextString.split(",")) {
                arbilTableModel.highlightMatchingText(highlightText);
            }
        }
    }

    private void initComponents(ImageBoxRenderer imageBoxRenderer) {
        arbilTableModel = new ArbilTableModel(imageBoxRenderer);
        ArbilTable arbilTable = new ArbilTable(arbilTableModel, tableTitle);
        ArbilSplitPanel arbilSplitPanel = new ArbilSplitPanel(arbilTable);
        arbilTableModel.hideContextMenuAndStatusBar = true;
        arbilSplitPanel.setSplitDisplay();
        getContentPane().add(arbilSplitPanel, java.awt.BorderLayout.CENTER);
    }
    private String tableTitle = "Arbil Table Demo";
    private ArbilTableModel arbilTableModel;
}
