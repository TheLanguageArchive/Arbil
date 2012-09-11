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
