package nl.mpi.arbil;

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
                    addNodesToTable(getParameter("NodeList"));
                }
            });
        } catch (Exception ex) {
            new LinorgBugCatcher().logError(ex);
        }
    }

    private void addNodesToTable(String nodeURLsString) {
        if (nodeURLsString != null) {
            for (String currentUrlString : nodeURLsString.split(",")) {
                imdiTableModel.addSingleImdiObject(GuiHelper.imdiLoader.getImdiObject(null, currentUrlString));
            }
        }
    }

    private void initComponents() {
        imdiTableModel = new ImdiTableModel();
        ImdiTable imdiTable = new ImdiTable(imdiTableModel, tableTitle);
        LinorgSplitPanel imdiSplitPanel = new LinorgSplitPanel(imdiTable);
        imdiSplitPanel.setSplitDisplay();
        getContentPane().add(imdiSplitPanel, java.awt.BorderLayout.CENTER);
    }
    private String tableTitle = "Arbil Table Demo";
    private ImdiTableModel imdiTableModel;
}
