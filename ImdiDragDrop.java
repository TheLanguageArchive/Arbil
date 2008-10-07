/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author petwit
 */
public class ImdiDragDrop {

    public DataFlavor imdiObjectFlavour = new DataFlavor(ImdiHelper.ImdiTreeObject.class, "ImdiTreeObject");
    private ImdiObjectSelection imdiObjectSelection = new ImdiObjectSelection();

    public void addDrag(JTable tableSource) {
        tableSource.setDragEnabled(true);
        tableSource.setTransferHandler(imdiObjectSelection);
    }

    public void addDrag(JTree treeSource) {
        treeSource.setDragEnabled(true);
        treeSource.setTransferHandler(imdiObjectSelection);
    }

//    public void addDrop(JTree treeTarget) {
//        treeTarget.setDropTarget(new DropTarget() {
//
//            @Override
//            public synchronized void drop(DropTargetDropEvent dtde) {
//                System.out.println("drop");
//                //ImdiObjectSelection imdiObjectSelection = (ImdiObjectSelection)dtde.getTransferable();
//                try {
//                    ImdiHelper.ImdiTreeObject[] draggedImdiObjects = (ImdiHelper.ImdiTreeObject[]) dtde.getTransferable().getTransferData(imdiObjectFlavour);
//                    System.out.println("dropt-jTree.getName: " + draggedImdiObjects.length);
//                    for (int objectCounter = 0; objectCounter < draggedImdiObjects.length; objectCounter++) {
//                        System.out.println(draggedImdiObjects[objectCounter]);
//                    }
//                } catch (Exception ex) {
//                    System.out.println("drop: " + ex.getMessage());
//                }
//                dtde.dropComplete(true);
//                super.drop(dtde);
//            }
//        });
//
////        treeTarget.setTransferHandler(new TransferHandler(null) {
////
//////            @Override
//////            public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
//////                return super.canImport(comp, transferFlavors);
//////            }
////
//////            @Override
//////            public boolean importData(JComponent comp, Transferable t) {
//////                System.out.println("importData");
//////                ImdiObjectSelection imdiObjectTransferable = (ImdiObjectSelection) t;
//////                JTree jTree = (JTree) imdiObjectTransferable.getTransferData(imdiObjectFlavour);
//////                System.out.println("jTree: " + jTree.getName());
//////                return super.importData(comp, t);
//////            }
////        });
//    }
//
    class ImdiObjectSelection extends TransferHandler implements Transferable {

        DataFlavor flavors[] = {imdiObjectFlavour};
        ImdiHelper.ImdiTreeObject[] draggedImdiObjects;

        public int getSourceActions(JComponent c) {
            System.out.println("getSourceActions");
            if ((c instanceof JTree)) {
                JTree jTree = (JTree) c;
                if (jTree.getSelectionCount() > 0 && jTree.getSelectionPath().getPathCount() > 1) {
                    // must have a selection and not be the rood node which is never an imdi node
                    // no selection will only occur on some java runtimes but must be handled here
                    return TransferHandler.COPY;
                }
            } else if (c instanceof JTable) {
                return TransferHandler.COPY;
            }
            return TransferHandler.NONE;
        }

        public boolean canImport(JComponent comp, DataFlavor flavor[]) {
            System.out.println("canImport");
            if (comp instanceof JTree) {
                JTree jTree = (JTree) comp;
                System.out.println("JTree: " + jTree.getName());
                System.out.println("canImport true");
                return true;
            } else if (comp instanceof JTable) {
                JTable jTable = (JTable) comp;
                System.out.println("JTable: " + jTable.getName());
                System.out.println("canImport true");
                return true;
            }
            System.out.println("canImport false");
            return false;
        }

        public Transferable createTransferable(JComponent comp) {
            draggedImdiObjects = null;
            System.out.println("createTransferable: " + comp.toString());
            if (comp instanceof JTree) {
                JTree draggedTree = (JTree) comp;
                System.out.println("selectedCount: " + draggedTree.getSelectionCount());
                //TreePath draggedPath[] = draggedTree.getSelectionPaths();
                draggedImdiObjects = new ImdiHelper.ImdiTreeObject[draggedTree.getSelectionCount()];
                for (int selectedCount = 0; selectedCount < draggedTree.getSelectionCount(); selectedCount++) {
                    DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) draggedTree.getSelectionPaths()[selectedCount].getLastPathComponent();
                    System.out.println("parentNode: " + parentNode.toString());
                    if (parentNode.getUserObject() instanceof ImdiHelper.ImdiTreeObject) {
                        System.out.println("DraggedImdi: " + parentNode.getUserObject().toString());
                        draggedImdiObjects[selectedCount] = (ImdiHelper.ImdiTreeObject) (parentNode.getUserObject());
                    } else {
                        draggedImdiObjects[selectedCount] = null;
                    }
                }
                return this;
            } else if (comp instanceof ImdiTable) {

                draggedImdiObjects = ((ImdiTable) comp).getSelectedRowsFromTable();
                return this;
            }
            System.out.println("createTransferable false");
            return null;
        }

        public boolean importData(JComponent comp, Transferable t) {
            System.out.println("importData: " + comp.toString());
            if (comp instanceof JTree) {
                System.out.println("comp: " + comp.getName());
                for (int draggedCounter = 0; draggedCounter < draggedImdiObjects.length; draggedCounter++) {
                    System.out.println("dragged: " + draggedImdiObjects[draggedCounter].toString());
                }
                JTree dropTree = (JTree) comp;
                for (int selectedCount = 0; selectedCount < dropTree.getSelectionCount(); selectedCount++) {
                    System.out.println("to: " + ((DefaultMutableTreeNode) dropTree.getSelectionPaths()[selectedCount].getLastPathComponent()).getUserObject().toString());
                }
            } else if (comp instanceof JTable) {
                ImdiTableModel dropTableModel = (ImdiTableModel) ((JTable) comp).getModel();
                dropTableModel.addImdiObjects(draggedImdiObjects);
            }
            System.out.println("importData false");
            return false;
        }

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