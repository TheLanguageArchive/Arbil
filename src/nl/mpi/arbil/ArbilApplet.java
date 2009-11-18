package nl.mpi.arbil;

/*
 * ArbilApplet.java
 * Created on 8 July 2009, 14:03
 * @author Peter.Withers@mpi.nl
 */
public class ArbilApplet extends javax.swing.JApplet {

    public void init() {
        try {
            java.awt.EventQueue.invokeAndWait(new Runnable() {

                public void run() {
                    mainSplitPane = new javax.swing.JSplitPane();
                    getContentPane().add(mainSplitPane, java.awt.BorderLayout.CENTER);
                    previewSplitPanel = new PreviewSplitPanel();
                    mainSplitPane.setRightComponent(previewSplitPanel);
                    arbilTreePanels = new ArbilTreePanels();
                    mainSplitPane.setLeftComponent(arbilTreePanels);
                    previewSplitPanel.setPreviewPanel(true);
                    ArbilMenuBar arbilMenuBar = new ArbilMenuBar(previewSplitPanel);
                    setJMenuBar(arbilMenuBar);
//                    LinorgWindowManager.getSingleInstance().setComponents(this);
                    LinorgWindowManager.getSingleInstance().openIntroductionPage();
                }
            });
        } catch (Exception ex) {
            new LinorgBugCatcher().logError(ex);
        }
    }

    public void start() {
        arbilTreePanels.setDefaultTreePaneSize();
        previewSplitPanel.setDividerLocation(0.3);
    }
    private javax.swing.JSplitPane mainSplitPane;
    private nl.mpi.arbil.ArbilTreePanels arbilTreePanels;
    private nl.mpi.arbil.PreviewSplitPanel previewSplitPanel;
}
