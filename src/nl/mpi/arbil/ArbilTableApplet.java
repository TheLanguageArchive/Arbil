package nl.mpi.arbil;

import nl.mpi.arbil.data.ImdiLoader;

/*
 * ArbilTableApplet.java
 * Created on 28 September 2009, 13:10
 * @author Peter.Withers@mpi.nl
 */
public class ArbilTableApplet extends javax.swing.JApplet {

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
                imdiTableModel.addSingleImdiObject(ImdiLoader.getSingleInstance().getImdiObject(rootPane, currentUrlString));
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
        LinorgSplitPanel imdiSplitPanel = new LinorgSplitPanel(imdiTable);
        imdiTableModel.hideContextMenuAndStatusBar = true;
        imdiSplitPanel.setSplitDisplay();
        getContentPane().add(imdiSplitPanel, java.awt.BorderLayout.CENTER);
    }
    private String tableTitle = "Arbil Table Demo";
    private ImdiTableModel imdiTableModel;
}
