package nl.mpi.arbil.ui.applet;

import java.net.URI;
import nl.mpi.arbil.ui.GuiHelper;
import nl.mpi.arbil.ui.ImdiTable;
import nl.mpi.arbil.ui.ImdiTableModel;
import nl.mpi.arbil.util.LinorgBugCatcher;
import nl.mpi.arbil.ui.ArbilSplitPanel;
import nl.mpi.arbil.data.ImdiLoader;

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
            new LinorgBugCatcher().logError(ex);
        }
    }

    private void addNodesToTable(String nodeURLsString) {
        if (nodeURLsString != null) {
            for (String currentUrlString : nodeURLsString.split(",")) {
                try {
                    imdiTableModel.addSingleImdiObject(ImdiLoader.getSingleInstance().getImdiObject(rootPane, new URI(currentUrlString)));
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        }
    }

    private void addShowOnlyColumnsToTable(String showColumnsString) {
        if (showColumnsString != null) {
            for (String currentshowColumns : showColumnsString.split(",")) {
                imdiTableModel.getFieldView().addShowOnlyColumn(currentshowColumns);
            }
        }
    }

    private void addChildNodesToTable(String childNodesString) {
        if (childNodesString != null) {
            for (String currentChildNode : childNodesString.split(",")) {
                imdiTableModel.addChildTypeToDisplay(currentChildNode);
            }
        }
    }

    private void addHighlightToTable(String highlightableTextString) {
        if (highlightableTextString != null) {
            for (String highlightText : highlightableTextString.split(",")) {
                imdiTableModel.highlightMatchingText(highlightText);
            }
        }
    }

    private void initComponents() {
        imdiTableModel = new ImdiTableModel();
        ImdiTable imdiTable = new ImdiTable(imdiTableModel, tableTitle);
        ArbilSplitPanel imdiSplitPanel = new ArbilSplitPanel(imdiTable);
        imdiTableModel.hideContextMenuAndStatusBar = true;
        imdiSplitPanel.setSplitDisplay();
        getContentPane().add(imdiSplitPanel, java.awt.BorderLayout.CENTER);
    }
    private String tableTitle = "Arbil Table Demo";
    private ImdiTableModel imdiTableModel;
}
