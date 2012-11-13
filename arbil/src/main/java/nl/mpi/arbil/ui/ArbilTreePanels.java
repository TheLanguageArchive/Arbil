/**
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.arbil.ui;

import nl.mpi.arbil.data.ArbilTreeHelper;
import javax.swing.JTabbedPane;
import nl.mpi.arbil.util.MessageDialogHandler;

/**
 * ArbilTreePanels.java
 * Created on Jul 14, 2009, 2:30:03 PM
 *
 * @author Peter.Withers@mpi.nl
 */
public class ArbilTreePanels extends javax.swing.JSplitPane {

    public ArbilTreePanels(ArbilTreeHelper treeHelper, ArbilTreeController treeController, MessageDialogHandler dialogHandler) {
	leftLocalSplitPane = new javax.swing.JSplitPane();
	localDirectoryScrollPane = new javax.swing.JScrollPane();
	localCorpusScrollPane = new javax.swing.JScrollPane();
	remoteCorpusScrollPane = new javax.swing.JScrollPane();
	favouritesScrollPane = new javax.swing.JScrollPane();

	this.setDividerSize(5);
	this.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
	this.setName("ArbilTreePanels"); // NOI18N

	leftLocalSplitPane.setDividerSize(5);
	leftLocalSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
	leftLocalSplitPane.setName("ArbilTreePanelsInner"); // NOI18N

	remoteCorpusTree = new ArbilTree(treeController, treeHelper, dialogHandler);
	localDirectoryTree = new ArbilTree(treeController, treeHelper, dialogHandler);
	localCorpusTree = new ArbilTree(treeController, treeHelper, dialogHandler);
	favouritesTree = new ArbilTree(treeController, treeHelper, dialogHandler);
	// enable drag and drop
	ArbilDragDrop.getSingleInstance().addDrag(remoteCorpusTree);
	ArbilDragDrop.getSingleInstance().addDrag(localDirectoryTree);
	ArbilDragDrop.getSingleInstance().addDrag(localCorpusTree);
	ArbilDragDrop.getSingleInstance().addDrag(favouritesTree);

	remoteCorpusTree.setModel(treeHelper.getRemoteCorpusTreeModel());
	remoteCorpusScrollPane.setViewportView(remoteCorpusTree);

	localCorpusTree.setModel(treeHelper.getLocalCorpusTreeModel());
	localCorpusScrollPane.setViewportView(localCorpusTree);

	localDirectoryTree.setModel(treeHelper.getLocalDirectoryTreeModel());
	localDirectoryScrollPane.setViewportView(localDirectoryTree);

	favouritesTree.setModel(treeHelper.getFavouritesTreeModel());
	favouritesScrollPane.setViewportView(favouritesTree);

	JTabbedPane treeTabPane = new JTabbedPane();
	treeTabPane.add(java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets").getString("FILES"), localDirectoryScrollPane);
	treeTabPane.add(java.util.ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets").getString("FAVOURITES"), favouritesScrollPane);

	leftLocalSplitPane.setBottomComponent(treeTabPane);
	leftLocalSplitPane.setLeftComponent(localCorpusScrollPane);

	this.setBottomComponent(leftLocalSplitPane);
	this.setLeftComponent(remoteCorpusScrollPane);

	treeHelper.setTrees(this);
	setDefaultTreePaneSize();
    }

    public final void setDefaultTreePaneSize() {
	setDividerLocation(0.33);
	leftLocalSplitPane.setDividerLocation(0.5);
    }

    public ArbilTree[] getTreeArray() {
	return new ArbilTree[]{localCorpusTree, localDirectoryTree, remoteCorpusTree, favouritesTree};
    }
    private javax.swing.JScrollPane localDirectoryScrollPane;
    private javax.swing.JScrollPane remoteCorpusScrollPane;
    private javax.swing.JScrollPane localCorpusScrollPane;
    private javax.swing.JScrollPane favouritesScrollPane;
    private javax.swing.JSplitPane leftLocalSplitPane;
    public ArbilTree localCorpusTree;
    public ArbilTree localDirectoryTree;
    public ArbilTree remoteCorpusTree;
    public ArbilTree favouritesTree;
}
