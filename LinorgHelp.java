/*
 * LinorgHelp.java
 *
 * Created on March 9, 2009, 1:38 PM
 */
package mpi.linorg;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 *
 * @author  petwit
 */
public class LinorgHelp extends javax.swing.JPanel {

    static public String ShorCutKeysPage = "Short Cut Keys";
    DefaultMutableTreeNode rootContentsNode;
    DefaultTreeModel helpTreeModel;
    static public LinorgHelp singleInstance = null;

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
        jTree1.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        File helpDirectory = new File(this.getClass().getResource("/mpi/linorg/resources/html/help").getFile());
        rootContentsNode = new DefaultMutableTreeNode("Contents");
        helpTreeModel = new DefaultTreeModel(rootContentsNode, true);
        jTree1.setModel(helpTreeModel);
        populateContentsPane(helpDirectory, helpTreeModel, rootContentsNode);
        jTree1.setSelectionRow(1);
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

    private void populateContentsPane(File currentDirectory, DefaultTreeModel helpTreeModel, DefaultMutableTreeNode currentNode) {
        System.out.println("populateContentsPane: " + currentDirectory);
        System.out.println("populateContentsPane: " + currentDirectory.exists());
        File[] directoryListingArray = currentDirectory.listFiles();
        System.out.println("populateContentsPane: " + directoryListingArray);
        if (directoryListingArray != null) {
//            Sort the directory listing
            Arrays.sort(directoryListingArray, new Comparator() {

                public int compare(Object object1, Object object2) {
                    if (!(object1 instanceof File && object2 instanceof File)) {
                        throw new IllegalArgumentException("not a File object");
                    }
                    String string1 = ((File) object1).getName();
                    String string2 = ((File) object2).getName();
                    return string2.compareToIgnoreCase(string1);
                }
            });
            for (File currentFile : directoryListingArray) {
                String currentName = currentFile.getName().replaceFirst("\\.html$", "");
                if (currentFile.isDirectory()) {
                    DefaultMutableTreeNode currentDirectoryNode = new DefaultMutableTreeNode(currentName, true);
                    helpTreeModel.insertNodeInto(currentDirectoryNode, currentNode, currentDirectoryNode.getChildCount());
                    populateContentsPane(currentFile, helpTreeModel, currentDirectoryNode);
                } else {
                    DefaultMutableTreeNode currentItemNode = new DefaultMutableTreeNode(new HelpNodeUserObject(currentName, currentFile), false);
                    helpTreeModel.insertNodeInto(currentItemNode, currentNode, 0);
                }
            }
        }
    }

    class HelpNodeUserObject {

        String nameString;
        File helpFile;

        public HelpNodeUserObject(String localNameString, File localHelpFile) {
            nameString = localNameString;
            helpFile = localHelpFile;
        }

        public String toString() {
            return nameString;
        }

        public File getHelpFile() {
            return helpFile;
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
                    jTextPane1.setPage(((HelpNodeUserObject) nodeInfo).getHelpFile().toURL());
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
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
