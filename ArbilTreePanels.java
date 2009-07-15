package mpi.linorg;

/**
 * ArbilTreePanels.java
 * Created on Jul 14, 2009, 2:30:03 PM
 * @author petwit
 */
public class ArbilTreePanels extends javax.swing.JSplitPane {

    public ArbilTreePanels() {
        leftLocalSplitPane = new javax.swing.JSplitPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        jScrollPane4 = new javax.swing.JScrollPane();
        jScrollPane3 = new javax.swing.JScrollPane();

        this.setDividerSize(5);
        this.setDividerLocation(0.15);
        this.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        this.setName("ArbilTreePanels"); // NOI18N

        leftLocalSplitPane.setDividerSize(5);
        leftLocalSplitPane.setDividerLocation(0.2);
        leftLocalSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        leftLocalSplitPane.setName("ArbilTreePanelsInner"); // NOI18N

        remoteCorpusTree = new ImdiTree();
        localDirectoryTree = new ImdiTree();
        localCorpusTree = new ImdiTree();

        remoteCorpusTree.setModel(TreeHelper.getSingleInstance().remoteCorpusTreeModel);
        jScrollPane3.setViewportView(remoteCorpusTree);

        localCorpusTree.setModel(TreeHelper.getSingleInstance().localCorpusTreeModel);
        jScrollPane4.setViewportView(localCorpusTree);

        localDirectoryTree.setModel(TreeHelper.getSingleInstance().localDirectoryTreeModel);
        jScrollPane2.setViewportView(localDirectoryTree);

        leftLocalSplitPane.setBottomComponent(jScrollPane2);
        leftLocalSplitPane.setLeftComponent(jScrollPane4);

        this.setBottomComponent(leftLocalSplitPane);
        this.setLeftComponent(jScrollPane3);

        TreeHelper.getSingleInstance().setTrees(remoteCorpusTree, localCorpusTree, localDirectoryTree);
    }
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSplitPane leftLocalSplitPane;
    private ImdiTree localCorpusTree;
    private ImdiTree localDirectoryTree;
    private ImdiTree remoteCorpusTree;
}
