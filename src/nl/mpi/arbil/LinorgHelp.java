package mpi.linorg;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * Document   : LinorgHelp.java
 * Created on : March 9, 2009, 1:38 PM
 * @author Peter.Withers@mpi.nl
 */
public class LinorgHelp extends javax.swing.JPanel {

    static public String ShorCutKeysPage = "Short Cut Keys";
    static public String IntroductionPage = "2. Where do I start";
    DefaultMutableTreeNode rootContentsNode;
    DefaultTreeModel helpTreeModel;
    static private LinorgHelp singleInstance = null;

    static synchronized public LinorgHelp getSingleInstance() {
        System.out.println("LinorgHelp getSingleInstance");
        if (singleInstance == null) {
            singleInstance = new LinorgHelp();
        }
        return singleInstance;
    }

    /** Creates new form LinorgHelp */
    private LinorgHelp() {
        initComponents();
        Vector<String> availablePages = new Vector();
        jTree1.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
//        try {
//            //FileWriter fileWriter = new FileWriter(this.getClass().getResource("/mpi/linorg/resources/html/help/index.txt").getFile());
//            FileWriter fileWriter = new FileWriter("/data1/repos/DesktopApplication1/WithoutApplictionFramework/src/mpi/linorg/resources/html/help/index.txt");
//            BufferedWriter outFile = new BufferedWriter(fileWriter);
//            File helpDirectory = new File(this.getClass().getResource("/mpi/linorg/resources/html/help").getFile());
//            scanDirectory(helpDirectory, outFile);
//            outFile.close();
//        } catch (Exception ex) {
//            GuiHelper.linorgBugCatcher.logError(ex);
//        }
        try {
            InputStream fileReader = (this.getClass().getResourceAsStream("/mpi/linorg/resources/html/help/index.txt"));
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileReader));
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                availablePages.add(line);
            }
            bufferedReader.close();
        } catch (Exception ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
        }

        rootContentsNode = new DefaultMutableTreeNode("Contents");
        helpTreeModel = new DefaultTreeModel(rootContentsNode, true);
        jTree1.setModel(helpTreeModel);
        populateContentsPane(availablePages, helpTreeModel, rootContentsNode);
        jTree1.setSelectionRow(1);
    }

    private void scanDirectory(File helpDirectory, BufferedWriter outFile) throws IOException {
        File[] directoryListingArray = helpDirectory.listFiles();
        for (File currentFile : directoryListingArray) {
            if (currentFile.isDirectory()) {
                scanDirectory(currentFile, outFile);
            } else if (!currentFile.getName().endsWith("index.txt")) {
                System.out.println("currentFile: " + currentFile.getPath());
                outFile.write(currentFile.getPath().split("/mpi/linorg/resources/html/help/")[1] + "\n");
            }
        }
    }

    private DefaultMutableTreeNode findNode(DefaultMutableTreeNode currentNode, String helpPage) {
        System.out.println("currentNode: " + currentNode);
        if (currentNode.getUserObject().toString().equals(helpPage)) {
            return currentNode;
        }
        if (currentNode.getChildCount() >= 0) {
            for (Enumeration e = currentNode.children(); e.hasMoreElements();) {
                DefaultMutableTreeNode nextNode = (DefaultMutableTreeNode) e.nextElement();
                DefaultMutableTreeNode foundNode = findNode(nextNode, helpPage);
                if (foundNode != null) {
                    return foundNode;
                }
            }
        }
        return null;
    }

    public void setCurrentPage(String helpPage) {
        DefaultMutableTreeNode foundNode = findNode(rootContentsNode, helpPage);
        if (foundNode != null) {
            if (foundNode instanceof DefaultMutableTreeNode) {
                final TreePath targetTreePath = new TreePath(((DefaultMutableTreeNode) foundNode).getPath());
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        jTree1.scrollPathToVisible(targetTreePath);
                        jTree1.setSelectionPath(targetTreePath);
                    }
                });
            }
        }
    }

    private void populateContentsPane(Vector<String> availablePages, DefaultTreeModel helpTreeModel, DefaultMutableTreeNode currentNode) {
        System.out.println("populateContentsPane: " + availablePages);
        if (availablePages != null) {
//        Sort the directory listing
            Collections.sort(availablePages, new Comparator() {

                public int compare(Object object1, Object object2) {
                    String string1 = object1.toString();
                    String string2 = object2.toString();
                    return string2.compareToIgnoreCase(string1);
                }
            });

            for (String currentFileName : availablePages) {
                DefaultMutableTreeNode currentSearchNode = rootContentsNode;
                System.out.println("currentFileName: " + currentFileName);
                for (String pathPartString : currentFileName.split("/")) {
                    boolean foundNode = false;
                    for (Enumeration childEnum = currentSearchNode.children(); childEnum.hasMoreElements();) {
                        DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) childEnum.nextElement();
                        if (pathPartString.equals(childNode.getUserObject().toString())) {
                            currentSearchNode = childNode;
                            foundNode = true;
                            break;
                        }
                    }
                    if (!foundNode) {
                        currentSearchNode.setAllowsChildren(true);
                        DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(pathPartString);
                        helpTreeModel.insertNodeInto(childNode, currentSearchNode, 0);
                        currentSearchNode = childNode;
                    }
                }
                URL currentURL = this.getClass().getResource("/mpi/linorg/resources/html/help/" + currentFileName);
                String currentName = currentSearchNode.getUserObject().toString().replaceFirst("\\.html$", "");
                currentSearchNode.setUserObject(new HelpNodeUserObject(currentName, currentURL));
                currentSearchNode.setAllowsChildren(false);
            }
        }
    }

    class HelpNodeUserObject {

        String nameString;
        URL helpURL;

        public HelpNodeUserObject(String localNameString, URL localHelpFile) {
            nameString = localNameString;
            helpURL = localHelpFile;
        }

        public String toString() {
            return nameString;
        }

        public URL getHelpURL() {
            return helpURL;
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();

        setLayout(new java.awt.BorderLayout());

        jSplitPane1.setDividerLocation(200);

        jTree1.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                jTree1ValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(jTree1);

        jSplitPane1.setLeftComponent(jScrollPane1);

        jTextPane1.setEditable(false);
        jScrollPane2.setViewportView(jTextPane1);

        jSplitPane1.setRightComponent(jScrollPane2);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void jTree1ValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_jTree1ValueChanged
// TODO add your handling code here:
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) jTree1.getLastSelectedPathComponent();

        if (node != null) {
        Object nodeInfo = node.getUserObject();
        if (node.isLeaf()) {
            try {
                if (((HelpNodeUserObject) nodeInfo).getHelpURL() != null) {
                    jTextPane1.setPage(((HelpNodeUserObject) nodeInfo).getHelpURL());
//                    jTextPane1.setPage(this.getClass().getResource("/mpi/linorg/resources/html/help/Searching.html"));
                } else {
                    jTextPane1.setText("Page not found");
                }
            } catch (Exception ex) {
                jTextPane1.setText("Page not found error");
//                GuiHelper.linorgBugCatcher.logError(ex);
            }
        }
    }
}//GEN-LAST:event_jTree1ValueChanged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JTree jTree1;
    // End of variables declaration//GEN-END:variables
}
