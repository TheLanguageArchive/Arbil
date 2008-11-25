/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import java.awt.Container;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.JList;
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
    public ImdiObjectSelection imdiObjectSelection = new ImdiObjectSelection();

    public void addDrag(JTable tableSource) {
        tableSource.setDragEnabled(true);
        tableSource.setTransferHandler(imdiObjectSelection);
    }

    public void addDrag(JTree treeSource) {
        treeSource.setDragEnabled(true);
        treeSource.setTransferHandler(imdiObjectSelection);
    }

    public void addDrag(JList listSource) {
        listSource.setDragEnabled(true);
        listSource.setTransferHandler(imdiObjectSelection);
    }

    public void addTransferHandler(JComponent targetComponent) {
        //targetComponent.setDragEnabled(true);
        targetComponent.setTransferHandler(imdiObjectSelection);
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
    public class ImdiObjectSelection extends TransferHandler implements Transferable {

        DataFlavor flavors[] = {imdiObjectFlavour};
        ImdiHelper.ImdiTreeObject[] draggedImdiObjects;
        public boolean selectionContainsArchivableLocalFile = false;
        public boolean selectionContainsLocalFile = false;
        public boolean selectionContainsLocalDirectory = false;
        public boolean selectionContainsImdiResource = false;
        public boolean selectionContainsImdiCorpus = false;
        public boolean selectionContainsImdiSession = false;
        public boolean selectionContainsImdiChild = false;
        public boolean selectionContainsLocal = false;
        public boolean selectionContainsRemote = false;

        @Override
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
            } else if (c instanceof JList) {
                return TransferHandler.COPY;
            }
            return TransferHandler.NONE;
        }

        @Override
        public boolean canImport(JComponent comp, DataFlavor flavor[]) {
            System.out.println("canImport: " + comp);
            if (comp instanceof JTree) {
                if (!GuiHelper.treeHelper.componentIsTheLocalCorpusTree(comp)) {
                    System.out.println("not the localcorpustree so cannot drop here");
                    return false;
                }
                System.out.println("target is the localcorpustree");
                System.out.println("selectionContainsArchivableLocalFile: " + selectionContainsArchivableLocalFile);
                System.out.println("selectionContainsLocalFile: " + selectionContainsLocalFile);
                System.out.println("selectionContainsLocalDirectory: " + selectionContainsLocalDirectory);
                System.out.println("selectionContainsImdiResource: " + selectionContainsImdiResource);
                System.out.println("selectionContainsImdiCorpus: " + selectionContainsImdiCorpus);
                System.out.println("selectionContainsImdiSession: " + selectionContainsImdiSession);
                System.out.println("selectionContainsImdiChild: " + selectionContainsImdiChild);
                System.out.println("selectionContainsLocal: " + selectionContainsLocal);
                System.out.println("selectionContainsRemote: " + selectionContainsRemote);
                if (selectionContainsLocalFile && selectionContainsArchivableLocalFile && // local files can be dropped to a tree
                        //but nothing else
                        !selectionContainsLocalDirectory &&
                        !selectionContainsImdiResource &&
                        !selectionContainsImdiCorpus &&
                        !selectionContainsImdiSession &&
                        !selectionContainsImdiChild &&
                        selectionContainsLocal &&
                        !selectionContainsRemote) {

                    return true;
//                    JTree jTree = (JTree) comp;
//                    DefaultMutableTreeNode targetTreeNode = (DefaultMutableTreeNode) jTree.getLastSelectedPathComponent();
//                    if (targetTreeNode != null) {
//                        Object userObject = targetTreeNode.getUserObject();
//                        if (userObject instanceof ImdiHelper.ImdiTreeObject) {
////                        if (!((ImdiHelper.ImdiTreeObject) userObject).isLocal()) {
////                            System.out.println("cannot drop to remote imdi");
////                            return false;
////                        }
////                        if (!((ImdiHelper.ImdiTreeObject) userObject).isImdi()) {
////                            System.out.println("cannot drop to non imdi");
////                            return false;
////                        }
//                            if (((ImdiHelper.ImdiTreeObject) userObject).isSession()) {
//                                System.out.println("can drop files to sessions");
//                                return true;
//                            }
//                        }
//                    }
//                    System.out.println("JTree: " + jTree.getName());
//                    System.out.println("cannot drop to non ImdiTreeObject");
//                    return false;
                }
            //} else if ((comp instanceof JTable)|| (comp instanceof JList)|| (comp instanceof JScrollPane)||(comp instanceof LinorgWindowManager.ImdiSplitPanel)) {
            //} else if ((comp.getParent().getParent().getParent() instanceof LinorgWindowManager.ImdiSplitPanel) ||(comp.getParent().getParent() instanceof LinorgWindowManager.ImdiSplitPanel) ||(comp.getParent() instanceof LinorgWindowManager.ImdiSplitPanel) ||(comp instanceof LinorgWindowManager.ImdiSplitPanel)) {
            } else {
                // search through al the parent nodes to see if we can find a drop target
                return (null != findImdiSplitPanel(comp));

            }
            System.out.println("canImport false");
            return false;
        }

        private Container findImdiSplitPanel(Container tempCom) {
            while (tempCom != null) {
                if (tempCom instanceof LinorgWindowManager.ImdiSplitPanel) {
                    System.out.println("canImport true");
                    return tempCom;
                }
                tempCom = tempCom.getParent();
//                LinorgWindowManager.ImdiSplitPanel targetPanel = (LinorgWindowManager.ImdiSplitPanel) comp;
//                JTable jTable = targetPanel.imdiTable;
//                System.out.println("JTable: " + jTable.getName());
            }
            return null;
        }

        @Override
        public Transferable createTransferable(JComponent comp) {
            draggedImdiObjects = null;
            selectionContainsArchivableLocalFile = false;
            selectionContainsLocalFile = false;
            selectionContainsLocalDirectory = false;
            selectionContainsImdiResource = false;
            selectionContainsImdiCorpus = false;
            selectionContainsImdiSession = false;
            selectionContainsImdiChild = false;
            selectionContainsLocal = false;
            selectionContainsRemote = false;
            System.out.println("createTransferable: " + comp.toString());
            if (comp instanceof JTree) {
                JTree draggedTree = (JTree) comp;
                //System.out.println("selectedCount: " + draggedTree.getSelectionCount());
                //TreePath draggedPath[] = draggedTree.getSelectionPaths();
                draggedImdiObjects = new ImdiHelper.ImdiTreeObject[draggedTree.getSelectionCount()];
                for (int selectedCount = 0; selectedCount < draggedTree.getSelectionCount(); selectedCount++) {
                    DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) draggedTree.getSelectionPaths()[selectedCount].getLastPathComponent();
                    //System.out.println("parentNode: " + parentNode.toString());
                    if (parentNode.getUserObject() instanceof ImdiHelper.ImdiTreeObject) {
                        //System.out.println("DraggedImdi: " + parentNode.getUserObject().toString());
                        draggedImdiObjects[selectedCount] = (ImdiHelper.ImdiTreeObject) (parentNode.getUserObject());
                        // classify the draggable bundle to help matching drop targets
                        selectionContainsArchivableLocalFile = draggedImdiObjects[selectedCount].isArchivableFile();
                        if (draggedImdiObjects[selectedCount].isLocal()) {
                            selectionContainsLocal = true;
                            if (draggedImdiObjects[selectedCount].isDirectory()) {
                                selectionContainsLocalDirectory = true;
                            } else {
                                selectionContainsLocalFile = true;
                            }
                        } else {
                            selectionContainsRemote = true;
                        }
                        if (draggedImdiObjects[selectedCount].isImdi()) {
                            if (draggedImdiObjects[selectedCount].isImdiChild()) {
                                selectionContainsImdiChild = true;
                                // only an imdichild will contain a resource
                                if (draggedImdiObjects[selectedCount].hasResource()) {
                                    selectionContainsImdiResource = true;
                                }
                            } else if (draggedImdiObjects[selectedCount].isSession()) {
                                selectionContainsImdiSession = true;
                            } else {
                                selectionContainsImdiCorpus = true;
                            }
                        }
                    } else {
                        draggedImdiObjects[selectedCount] = null;
                    }
                }
                return this;
            } else if (comp instanceof ImdiTable) {

                draggedImdiObjects = ((ImdiTable) comp).getSelectedRowsFromTable();
                return this;
            } else if (comp instanceof JList) {
                Object[] selectedValues = ((JList) comp).getSelectedValues();
                //System.out.println("selectedValues: " + selectedValues);
                draggedImdiObjects = new ImdiHelper.ImdiTreeObject[selectedValues.length];
                for (int selectedNodeCounter = 0; selectedNodeCounter < selectedValues.length; selectedNodeCounter++) {
                    if (selectedValues[selectedNodeCounter] instanceof ImdiHelper.ImdiTreeObject) {
                        draggedImdiObjects[selectedNodeCounter] = (ImdiHelper.ImdiTreeObject) selectedValues[selectedNodeCounter];
                    }
                }
                return this;
            }
            //System.out.println("createTransferable false");
            return null;
        }

        @Override
        public boolean importData(JComponent comp, Transferable t) {
            //System.out.println("importData: " + comp.toString());
            //System.out.println("draggedImdiObjects: " + draggedImdiObjects);
            if (draggedImdiObjects != null) {
                if (comp instanceof JTree) {
                    System.out.println("comp: " + comp.getName());
                    for (int draggedCounter = 0; draggedCounter < draggedImdiObjects.length; draggedCounter++) {
                        System.out.println("dragged: " + draggedImdiObjects[draggedCounter].toString());
                    }
                    JTree dropTree = (JTree) comp;
                    DefaultMutableTreeNode targetNode = GuiHelper.treeHelper.getLocalCorpusTreeSingleSelection();
                    // TODO: getImdiChildNodes really shouldnt be called here, instead it would be better for the targetNode to get its childeren before adding anything
                    GuiHelper.treeHelper.getImdiChildNodes(targetNode);
                    Object dropTargetUserObject = targetNode.getUserObject();
                    System.out.println("to: " + dropTargetUserObject.toString());
                    if (dropTargetUserObject instanceof ImdiHelper.ImdiTreeObject) {
                        if (((ImdiHelper.ImdiTreeObject) dropTargetUserObject).isSession()) {
                            if (selectionContainsArchivableLocalFile == true &&
                                    selectionContainsLocalFile == true &&
                                    selectionContainsLocalDirectory == false &&
                                    selectionContainsImdiResource == false &&
                                    selectionContainsImdiCorpus == false &&
                                    selectionContainsImdiSession == false &&
                                    selectionContainsImdiChild == false &&
                                    selectionContainsLocal == true &&
                                    selectionContainsRemote == false) {
                                System.out.println("ok to add local file");
                                for (int draggedCounter = 0; draggedCounter < draggedImdiObjects.length; draggedCounter++) {
                                    System.out.println("dragged: " + draggedImdiObjects[draggedCounter].toString());
                                    Vector tempVector = ((ImdiHelper.ImdiTreeObject) dropTargetUserObject).addChildNode(draggedImdiObjects[draggedCounter]);
                                    GuiHelper.treeHelper.refreshChildNodes(targetNode);
                                    GuiHelper.linorgWindowManager.openFloatingTable(tempVector.elements(), "new Resource(s) in " + dropTargetUserObject);
                                }
                            }
                        }
                        GuiHelper.treeHelper.reloadLocalCorpusTree();
                    }
//                    }
                } else {
                    Container imdiSplitPanel = findImdiSplitPanel(comp);
                    if (imdiSplitPanel instanceof LinorgWindowManager.ImdiSplitPanel) {
                        LinorgWindowManager.ImdiSplitPanel targetPanel = (LinorgWindowManager.ImdiSplitPanel) imdiSplitPanel;
                        ImdiTableModel dropTableModel = (ImdiTableModel) targetPanel.imdiTable.getModel();
                        dropTableModel.addImdiObjects(draggedImdiObjects);
                    }
                }
            }
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