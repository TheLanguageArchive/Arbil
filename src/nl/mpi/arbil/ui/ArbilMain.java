package nl.mpi.arbil.ui;

import nl.mpi.arbil.ui.menu.ArbilMenuBar;
import nl.mpi.arbil.util.ArbilVersionChecker;
import nl.mpi.arbil.util.ArbilBugCatcher;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import nl.mpi.arbil.ArbilIcons;
import nl.mpi.arbil.ArbilVersion;

/*
 * LinorgView.java
 * This version uses only a JFrame and does not require additional dependencies
 * Created on 23 September 2008, 17:23
 * @author Peter.Withers@mpi.nl
 */
public class ArbilMain extends javax.swing.JFrame {

    private javax.swing.JSplitPane mainSplitPane;
    private ArbilMenuBar arbilMenuBar;
//    static boolean updateViaJavaws = false;

    public ArbilMain() {
        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                arbilMenuBar.performCleanExit();
                //super.windowClosing(e);
            }
        });

        initComponents();
        PreviewSplitPanel previewSplitPanel = new PreviewSplitPanel();
        mainSplitPane.setRightComponent(previewSplitPanel);
        ArbilTreePanels arbilTreePanels = new ArbilTreePanels();
        mainSplitPane.setLeftComponent(arbilTreePanels);
        arbilMenuBar = new ArbilMenuBar(previewSplitPanel, null);
        setJMenuBar(arbilMenuBar);

        mainSplitPane.setDividerLocation(0.25);

        ArbilWindowManager.getSingleInstance().loadGuiState(this);
        setTitle(new ArbilVersion().applicationTitle + " " + new ArbilVersion().compileDate);
        setIconImage(ArbilIcons.getSingleInstance().linorgIcon.getImage());
        // load the templates and populate the templates menu
        setVisible(true);
        ArbilWindowManager.getSingleInstance().openIntroductionPage();

        if (arbilMenuBar.checkNewVersionAtStartCheckBoxMenuItem.isSelected()) {
            new ArbilVersionChecker().checkForUpdate();
        }
    }

    private void initComponents() {

        mainSplitPane = new javax.swing.JSplitPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Arbil");

        mainSplitPane.setDividerLocation(100);
        mainSplitPane.setDividerSize(5);
        mainSplitPane.setName("mainSplitPane");
        getContentPane().add(mainSplitPane, java.awt.BorderLayout.CENTER);

        pack();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        System.setProperty("sun.swing.enableImprovedDragGesture", "true");
        System.setProperty("apple.awt.graphics.UseQuartz", "true");
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                try {
                    new ArbilMain();
                } catch (Exception ex) {
                    new ArbilBugCatcher().logError(ex);
                }
            }
        });
    }
}
