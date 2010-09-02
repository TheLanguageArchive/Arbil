package nl.mpi.arbil;

import javax.swing.JApplet;
import nl.mpi.arbil.data.ImdiLoader;

/*
 * ArbilApplet2.java
 * Created on 1 September 2010, 17:23
 * @author Peter.Withers@mpi.nl
 */
public class ArbilApplet2 extends JApplet {

    private javax.swing.JSplitPane mainSplitPane;
    private ArbilMenuBar arbilMenuBar;

    private void initComponents() {
        mainSplitPane = new javax.swing.JSplitPane();
        mainSplitPane.setName("mainSplitPane");
        PreviewSplitPanel previewSplitPanel = new PreviewSplitPanel();
        ArbilTreePanels arbilTreePanels = new ArbilTreePanels();
        mainSplitPane.setLeftComponent(arbilTreePanels);
        mainSplitPane.setRightComponent(previewSplitPanel);
        arbilMenuBar = new ArbilMenuBar(previewSplitPanel);
        add(mainSplitPane, java.awt.BorderLayout.CENTER);
        setJMenuBar(arbilMenuBar);

//        mainSplitPane.setDividerLocation(100);
        mainSplitPane.setDividerSize(5);
        mainSplitPane.setDividerLocation(0.25);

//        LinorgWindowManager.getSingleInstance().loadGuiState(this);
//        setTitle(new LinorgVersion().applicationTitle + " " + new LinorgVersion().compileDate);
//        setIconImage(ImdiIcons.getSingleInstance().linorgIcon.getImage());
        // load the templates and populate the templates menu
        setVisible(true);
        LinorgWindowManager.getSingleInstance().openIntroductionPage();

//        if (arbilMenuBar.checkNewVersionAtStartCheckBoxMenuItem.isSelected()) {
//            new LinorgVersionChecker().checkForUpdate();
//        }
    }

    /**
     * Initialization method that will be called after the applet is loaded
     * into the browser.
     */
    public void init() {
        // TODO start asynchronous download of heavy resources
        System.setProperty("sun.swing.enableImprovedDragGesture", "true");
        System.setProperty("apple.awt.graphics.UseQuartz", "true");
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        initComponents();
    }
    // TODO overwrite start(), stop() and destroy() methods

    public void start() {
        ImdiLoader.getSingleInstance().startLoaderThreads();
    }

    public void stop() {
        // it would seem that any dialogue box on applet stop will kill the web browser in a very bad way
        //arbilMenuBar.performCleanExit();
        //arbilMenuBar.saveApplicationState();
    }
}
