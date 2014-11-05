/**
 * Copyright (C) 2014 The Language Archive, Max Planck Institute for Psycholinguistics
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
package nl.mpi.arbil.util;

import java.io.File;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilNode;

/**
 * Document : BulkResourcesAdder
 * Created on : Aug 1, 2012, 2:07:07 PM
 * Author : Peter Withers
 */
public class BulkResourcesAdder implements Runnable {

    final MessageDialogHandler dialogHandler;
    final ArbilNode targetNode;
    final ArbilDataNode favouriteNode;
    final File[] sourceFiles;
    boolean metadataFilePerResource;
    boolean copyDirectoryStructure;

    public BulkResourcesAdder(MessageDialogHandler dialogHandler, ArbilNode targetNode, ArbilDataNode favouriteNode, File[] sourceFiles) {
        this.dialogHandler = dialogHandler;
        this.targetNode = targetNode;
        this.favouriteNode = favouriteNode;
        this.sourceFiles = sourceFiles;
    }

    public void setCopyDirectoryStructure(boolean copyDirectoryStructure) {
        this.copyDirectoryStructure = copyDirectoryStructure;
    }

    public void setMetadataFilePerResource(boolean metadataFilePerResource) {
        this.metadataFilePerResource = metadataFilePerResource;
    }

    public void doBulkAdd() {
//        dialogHandler.showConfirmDialogBox(null, null);
        new Thread(this, "doBulkAdd").start();
    }

    public void run() {
//        if (targetNode.getNeedsSaveToDisk(false)) {
//            targetNode.saveChangesToCache(true);
//        }
//        try {
////            if (destinationNode != null) {
////			destinationNode.updateLoadingState(1);
////			addNode(destinationNode, nodeTypeDisplayNameLocal, addableNode);
////		    } else {
////			addNodeToRoot(nodeTypeDisplayNameLocal, addableNode);
////		    }
////            if (targetNode != null) {
////                new MetadataBuilder().requestAddNode(targetNode, ((JMenuItem) evt.getSource()).getText(), favouriteNode);
////            } else {
////                new MetadataBuilder().requestAddRootNode(favouriteNode, ((JMenuItem) evt.getSource()).getText());
////            }
//        } catch (Exception ex) {
//            dialogHandler.addMessageDialogToQueue("Failed to add from favourites, see error log for details.", "Error");
//            BugCatcherManager.getBugCatcher().logError(ex);
//        }
    }
}
