package nl.mpi.arbil.ui.applet;

import java.net.URI;
import nl.mpi.arbil.ui.GuiHelper;
import nl.mpi.arbil.ui.ArbilTable;
import nl.mpi.arbil.data.ArbilTableModel;
import nl.mpi.arbil.util.ArbilBugCatcher;
import nl.mpi.arbil.ui.ArbilSplitPanel;
import nl.mpi.arbil.data.ArbilDataNodeLoader;

/*
 * ArbilTableApplet.java
 * Created on 28 September 2009, 13:10
 * @author Peter.Withers@mpi.nl
 */
public class ArbilTableApplet extends javax.swing.JApplet {

    @Override
    public void init() {
        try {
            java.awt.EventQueue.invokeAndWait(new Runnable() {

                public void run() {
                    initComponents();
                    addNodesToTable(getParameter("ImdiFileList"));
                    addShowOnlyColumnsToTable(getParameter("ShowOnlyColumns"));
                    addChildNodesToTable(getParameter("ChildNodeColumns"));
                    addHighlightToTable(getParameter("HighlightText"));
                }
            });
        } catch (Exception ex) {
            new ArbilBugCatcher().logError(ex);
        }
    }

    private void addNodesToTable(String nodeURLsString) {
        if (nodeURLsString != null) {
            for (String currentUrlString : nodeURLsString.split(",")) {
                try {
                    arbilTableModel.addSingleArbilDataNode(ArbilDataNodeLoader.getSingleInstance().getArbilDataNode(rootPane, new URI(currentUrlString)));
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        }
    }

    private void addShowOnlyColumnsToTable(String showColumnsString) {
        if (showColumnsString != null) {
            for (String currentshowColumns : showColumnsString.split(",")) {
                arbilTableModel.getFieldView().addShowOnlyColumn(currentshowColumns);
            }
        }
    }

    private void addChildNodesToTable(String childNodesString) {
        if (childNodesString != null) {
            for (String currentChildNode : childNodesString.split(",")) {
                arbilTableModel.addChildTypeToDisplay(currentChildNode);
            }
        }
    }

    private void addHighlightToTable(String highlightableTextString) {
        if (highlightableTextString != null) {
            for (String highlightText : highlightableTextString.split(",")) {
                arbilTableModel.highlightMatchingText(highlightText);
            }
        }
    }

    private void initComponents() {
        arbilTableModel = new ArbilTableModel();
        ArbilTable arbilTable = new ArbilTable(arbilTableModel, tableTitle);
        ArbilSplitPanel arbilSplitPanel = new ArbilSplitPanel(arbilTable);
        arbilTableModel.hideContextMenuAndStatusBar = true;
        arbilSplitPanel.setSplitDisplay();
        getContentPane().add(arbilSplitPanel, java.awt.BorderLayout.CENTER);
    }
    private String tableTitle = "Arbil Table Demo";
    private ArbilTableModel arbilTableModel;
}
