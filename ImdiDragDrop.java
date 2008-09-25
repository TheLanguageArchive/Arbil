/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author petwit
 */
public class ImdiDragDrop {

    public DataFlavor imdiObjectFlavour = new DataFlavor(ImdiHelper.ImdiTreeObject.class, "ImdiTreeObject");

    public void addDrag(JTree treeSource) {
        treeSource.setDragEnabled(true);
        treeSource.setTransferHandler(new ImdiObjectSelection());
    }

    public void addDrop(JTree treeTarget) {
        treeTarget.setDropTarget(new DropTarget() {

            @Override
            public synchronized void drop(DropTargetDropEvent dtde) {
                System.out.println("drop");
                //ImdiObjectSelection imdiObjectSelection = (ImdiObjectSelection)dtde.getTransferable();
                try {
                    ImdiHelper.ImdiTreeObject[] draggedImdiObjects = (ImdiHelper.ImdiTreeObject[]) dtde.getTransferable().getTransferData(imdiObjectFlavour);
                    System.out.println("dropt-jTree.getName: " + draggedImdiObjects.length);
                    for (int objectCounter = 0; objectCounter < draggedImdiObjects.length; objectCounter++) {
                        System.out.println(draggedImdiObjects[objectCounter]);
                    }
                } catch (Exception ex) {
                    System.out.println("drop: " + ex.getMessage());
                }
                dtde.dropComplete(true);
                super.drop(dtde);
            }
        });

//        treeTarget.setTransferHandler(new TransferHandler(null) {
//
////            @Override
////            public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
////                return super.canImport(comp, transferFlavors);
////            }
//
////            @Override
////            public boolean importData(JComponent comp, Transferable t) {
////                System.out.println("importData");
////                ImdiObjectSelection imdiObjectTransferable = (ImdiObjectSelection) t;
////                JTree jTree = (JTree) imdiObjectTransferable.getTransferData(imdiObjectFlavour);
////                System.out.println("jTree: " + jTree.getName());
////                return super.importData(comp, t);
////            }
//        });
    }

    class ImdiObjectSelection extends TransferHandler implements Transferable {

        DataFlavor flavors[] = {imdiObjectFlavour};
        ImdiHelper.ImdiTreeObject[] draggedImdiObjects;
//        String localCorpusTreeName = 
//localDirectoryTree
//                remoteCorpusTree
        public int getSourceActions(JComponent c) {
            System.out.println("getSourceActions");
            if ((c instanceof JTree)) {
                JTree jTree = (JTree) c;
                System.out.println("getPathCount: " + jTree.getSelectionPath().getPathCount());
                if (jTree.getSelectionPath().getPathCount() == 1) {
                    System.out.println("not a jtree cannot import");
                    return TransferHandler.NONE;
                }
            }
            
            return TransferHandler.COPY;
        }

        public boolean canImport(JComponent comp, DataFlavor flavor[]) {
            System.out.println("canImport");
            if ((comp instanceof JTree)) {
                JTree jTree = (JTree) comp;
                System.out.println("getName: " + jTree.getName());
//                System.out.println("getPathCount: " + jTree.getSelectionPath().getPathCount());
//                if (jTree.getSelectionPath().getPathCount() == 1) {
//                    System.out.println("not a jtree cannot import");
//                    return false;
//                }
            }
            for (int i = 0, n = flavor.length; i < n; i++) {
                //System.out.println(flavor[i] + " : " + imdiObjectFlavour);
                if (flavor[i].equals(imdiObjectFlavour)) {
                    return true;
                }
            }
            System.out.println("canImport false");
            return false;
        }

        public Transferable createTransferable(JComponent comp) {
            draggedImdiObjects = null;
            System.out.println("createTransferable");
            if (comp instanceof JTree) {
                JTree draggedTree = (JTree) comp;
                //TreePath draggedPath[] = draggedTree.getSelectionPaths();
                draggedImdiObjects = new ImdiHelper.ImdiTreeObject[draggedTree.getSelectionCount()];
                for (int selectedCount = 0; selectedCount < draggedTree.getSelectionCount(); selectedCount++) {
                    DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) draggedTree.getSelectionPaths()[selectedCount].getLastPathComponent();
                    if (parentNode.getUserObject() instanceof ImdiHelper.ImdiTreeObject) {
                        draggedImdiObjects[selectedCount] = (ImdiHelper.ImdiTreeObject) (parentNode.getUserObject());
                    } else {
                        draggedImdiObjects[selectedCount] = null;
                    }
                }
                return this;
            }
            System.out.println("createTransferable false");
            return null;
        }

//        public boolean importData(JComponent comp, Transferable t) {
//            System.out.println("importData");
//            if (comp instanceof JTree) {
//                if (t.isDataFlavorSupported(flavors[0])) {
////                    try {
////                        draggedTree = (JTree) t.getTransferData(flavors[0]);
////                        return true;
////                    } catch (UnsupportedFlavorException ignored) {
////                    } catch (IOException ignored) {
////                    }
//                }
//            }
//            System.out.println("importData false");
//            return false;
//        }
        public Object getTransferData(DataFlavor flavor) {
            System.out.println("getTransferData");
            if (isDataFlavorSupported(flavor)) {
                return draggedImdiObjects;
            }
            return null;
        }

        public DataFlavor[] getTransferDataFlavors() {
            System.out.println("getTransferDataFlavors");
            return flavors;
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            System.out.println("isDataFlavorSupported");
            return flavors[0].equals(flavor);
        }
    }
}