package mpi.linorg;

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
                    initComponents();
                    PreviewSplitPanel previewSplitPanel = new PreviewSplitPanel();
                    mainSplitPane.setRightComponent(previewSplitPanel);
                    ArbilTreePanels arbilTreePanels = new ArbilTreePanels();
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

    private void initComponents() {
        mainSplitPane = new javax.swing.JSplitPane();
        getContentPane().add(mainSplitPane, java.awt.BorderLayout.CENTER);
    }
    private javax.swing.JSplitPane mainSplitPane;
}
