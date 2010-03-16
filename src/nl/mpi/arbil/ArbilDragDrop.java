package nl.mpi.arbil;

import nl.mpi.arbil.importexport.ImportExportDialog;
import nl.mpi.arbil.data.ImdiTreeObject;
import java.awt.Container;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import nl.mpi.arbil.data.ImdiSchema;

/**
 * Document   :  ArbilDragDrop
 * Created on :  Tue Sep 09 15:02:56 CEST 2008
 * @author Peter.Withers@mpi.nl
 */
public class ArbilDragDrop {

    // There are numerous limitations of drag and drop in 1.5 and to overcome the resulting issues we need to share the same transferable object on both the drag source and the drop target
    public DataFlavor imdiObjectFlavour = new DataFlavor(ImdiTreeObject.class, "ImdiTreeObject");
    public ImdiObjectSelection imdiObjectSelection = new ImdiObjectSelection();

    public void addDrag(JTable tableSource) {
        tableSource.setDragEnabled(true);
        tableSource.setTransferHandler(imdiObjectSelection);
    }

    public void addDrag(JTree treeSource) {
        treeSource.setDragEnabled(true);
        treeSource.setTransferHandler(imdiObjectSelection);
        treeSource.addTreeSelectionListener(imdiObjectSelection);
        DropTarget target = treeSource.getDropTarget();
        try {
            target.addDropTargetListener(new DropTargetAdapter() {

                public void dragOver(DropTargetDragEvent dtdEvent) {
                    if (imdiObjectSelection.dropAllowed) {
                        dtdEvent.acceptDrag(dtdEvent.getDropAction());
                    } else {
                        dtdEvent.rejectDrag();
                    }
                }

                public void drop(DropTargetDropEvent e) {
                    // handled by the TransferHandler
                }
            });
        } catch (java.util.TooManyListenersException ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
        }
    }

    public void addDrag(JList listSource) {
        listSource.setDragEnabled(true);
        listSource.setTransferHandler(imdiObjectSelection);
    }

    public void addTransferHandler(JComponent targetComponent) {
        //targetComponent.setDragEnabled(true);
        targetComponent.setTransferHandler(imdiObjectSelection);
    }

    public class ImdiObjectSelection extends TransferHandler implements Transferable, javax.swing.event.TreeSelectionListener {

        long dragStartMilliSeconds;
        DataFlavor flavors[] = {imdiObjectFlavour};
        ImdiTreeObject[] draggedImdiObjects;
        DefaultMutableTreeNode[] draggedTreeNodes;
        public boolean selectionContainsArchivableLocalFile = false;
        public boolean selectionContainsLocalFile = false;
        public boolean selectionContainsLocalDirectory = false;
        public boolean selectionContainsImdiResource = false;
        public boolean selectionContainsImdiCorpus = false;
        public boolean selectionContainsImdiInCache = false;
        public boolean selectionContainsImdiCatalogue = false;
        public boolean selectionContainsImdiSession = false;
        public boolean selectionContainsImdiChild = false;
        public boolean selectionContainsLocal = false;
        public boolean selectionContainsRemote = false;
        public boolean selectionContainsFavourite = false;
        private JComponent currentDropTarget = null;
        public boolean dropAllowed = false;

        public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
            if (evt.getSource() == currentDropTarget) {
                System.out.println("Drag target selection change: " + evt.getSource().toString());
                if (evt.getSource() instanceof ImdiTree) {
                    dropAllowed = canDropToTarget((ImdiTree) evt.getSource());
//                    DropTarget dropTarget = dropTree.getDropTarget();                    
                }
            }
        }

        private boolean canDropToTarget(ImdiTree dropTree) {
            ImdiTreeObject currentLeadSelection = dropTree.getLeadSelectionNode();
            if (currentLeadSelection == null) {
                if (TreeHelper.getSingleInstance().componentIsTheFavouritesTree(currentDropTarget)) {
                    // allow drop to the favourites tree even when no selection is made
                    // allow drop to only toe root node of the favourites tree
                    return !selectionContainsFavourite;
                } else if (TreeHelper.getSingleInstance().componentIsTheLocalCorpusTree(currentDropTarget)) {
                    //if (dropTree.getSelectionPath().getPathCount() == 1) {
                    // allow import to local tree if no nodes are selected
                    // allow drop to the root node if it is an import
                    return (selectionContainsRemote && (selectionContainsImdiCorpus || selectionContainsImdiCatalogue || selectionContainsImdiSession));
                }
                return false;
            } else {
                System.out.println("currentLeadSelection: " + currentLeadSelection.toString());
//                todo: prevent dragging to self but allow dragging to other branch of parent session
//                todo: look for error dragging actor from favourites
//                todo: look for error in field triggers when merging from favourite (suppress trtiggeres when merging)
                if (TreeHelper.getSingleInstance().componentIsTheLocalCorpusTree(currentDropTarget)) {
                    if (currentLeadSelection.isDirectory) {
                        return false; // nothing can be dropped to a directory
                    } else if (currentLeadSelection.isCorpus()) {
                        if (selectionContainsImdiCorpus || selectionContainsImdiCatalogue || selectionContainsImdiSession) {
                            return true;
                        }
                    } else if (currentLeadSelection.isCatalogue()) {
                        return false; // nothing can be dropped to a catalogue
                    } else if (currentLeadSelection.isSession()) {
                        if (selectionContainsArchivableLocalFile || (selectionContainsImdiChild && selectionContainsFavourite)) {
                            return true;
                        }
                    } else if (currentLeadSelection.isImdiChild()) {
                        // TODO: in this case we should loop over the dragged nodes and check each one for compatability
                        if (selectionContainsLocalFile || (selectionContainsImdiChild && selectionContainsFavourite)) { // TODO: allow drag drop of appropriate imdi child nodes to sessions and compatable subnodes
                            return true;
                        }
                    }
//        public boolean selectionContainsArchivableLocalFile = false;
//        public boolean selectionContainsLocalFile = false;
//        public boolean selectionContainsLocalDirectory = false;
//        public boolean selectionContainsImdiResource = false;
//        public boolean selectionContainsImdiCorpus = false;
//        public boolean selectionContainsImdiInCache = false;
//        public boolean selectionContainsImdiCatalogue = false;
//        public boolean selectionContainsImdiSession = false;
//        public boolean selectionContainsImdiChild = false;
//        public boolean selectionContainsLocal = false;
//        public boolean selectionContainsRemote = false;
                    return false;
                } else {
                    return false;
                }
            }
        }

        @Override
        public void exportToClipboard(JComponent comp, Clipboard clip, int action) throws IllegalStateException {
            System.out.println("exportToClipboard: " + comp);
            createTransferable(null); // clear the transfer objects
            if (comp instanceof ImdiTree) {
                ImdiTree sourceTree = (ImdiTree) comp;
                ImdiTreeObject[] selectedImdiNodes = sourceTree.getSelectedNodes();
                if (selectedImdiNodes != null) {
                    sourceTree.copyNodeUrlToClipboard(selectedImdiNodes);
                }
            } else if (comp instanceof ImdiTable) {
                ImdiTable sourceTable = (ImdiTable) comp;
                sourceTable.copySelectedTableRowsToClipBoard();
            } else {
                super.exportToClipboard(comp, clip, action);
            }
        }

        @Override
        public int getSourceActions(JComponent c) {
            System.out.println("getSourceActions");
            if ((c instanceof JTree)) {
                JTree jTree = (JTree) c;
                // allow drag providing that the root node is not the only node selected
                if (jTree.getSelectionCount() > 1 || (jTree.getSelectionCount() == 1 && jTree.getSelectionPath().getPathCount() > 1)) {
                    // must have a selection and not be the root node which is never an imdi node
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
            currentDropTarget = null;
            dropAllowed = false;
            if (comp instanceof JTree) {
                if (TreeHelper.getSingleInstance().componentIsTheLocalCorpusTree(comp)) {
                    System.out.println("localcorpustree so can drop here");
                    if (selectionContainsArchivableLocalFile ||
                            //selectionContainsLocalFile ||
                            //selectionContainsLocalDirectory ||
                            //selectionContainsImdiResource ||
                            //selectionContainsLocal ||
                            //selectionContainsRemote ||
                            selectionContainsImdiCorpus ||
                            selectionContainsImdiCatalogue ||
                            selectionContainsImdiSession ||
                            selectionContainsImdiChild) {
                        currentDropTarget = comp; // store the source component for the tree node sensitive drop
                        dropAllowed = canDropToTarget((ImdiTree) comp);
                        return true;
                    }
                }
                if (TreeHelper.getSingleInstance().componentIsTheFavouritesTree(comp)) {
                    System.out.println("favourites tree so can drop here");
                    if (//selectionContainsArchivableLocalFile &&
                            //selectionContainsLocalFile ||
                            //selectionContainsLocalDirectory &&
                            //selectionContainsImdiResource ||
                            //selectionContainsLocal ||
                            //selectionContainsRemote ||
                            //selectionContainsImdiCorpus ||
                            selectionContainsImdiCatalogue ||
                            selectionContainsImdiSession ||
                            selectionContainsImdiChild) {
                        currentDropTarget = comp; // store the source component for the tree node sensitive drop
                        dropAllowed = canDropToTarget((ImdiTree) comp);
                        return true;
                    }
                }
            } else {
                // search through al the parent nodes to see if we can find a drop target
                dropAllowed = (null != findImdiDropableTarget(comp));
                return dropAllowed;
            }
            System.out.println("canImport false");
            return false;
        }

        private Container findImdiDropableTarget(Container tempCom) {
            while (tempCom != null) {
                if (tempCom instanceof LinorgSplitPanel || tempCom instanceof JDesktopPane) {
                    System.out.println("canImport true");
                    return tempCom;
                }
                tempCom = tempCom.getParent();
            }
            return null;
        }

        @Override
        public Transferable createTransferable(JComponent comp) {
            dragStartMilliSeconds = System.currentTimeMillis();
            draggedImdiObjects = null;
            draggedTreeNodes = null;
            selectionContainsArchivableLocalFile = false;
            selectionContainsLocalFile = false;
            selectionContainsLocalDirectory = false;
            selectionContainsImdiResource = false;
            selectionContainsImdiCorpus = false;
            selectionContainsImdiCatalogue = false;
            selectionContainsImdiSession = false;
            selectionContainsImdiChild = false;
            selectionContainsLocal = false;
            selectionContainsRemote = false;
            selectionContainsFavourite = false;
//         if (comp != null)  { System.out.println("createTransferable: " + comp.toString()); }
            if (comp instanceof ImdiTree) {
                ImdiTree draggedTree = (ImdiTree) comp;
                //System.out.println("selectedCount: " + draggedTree.getSelectionCount());
                draggedImdiObjects = new ImdiTreeObject[draggedTree.getSelectionCount()];
                draggedTreeNodes = new DefaultMutableTreeNode[draggedTree.getSelectionCount()];
                for (int selectedCount = 0; selectedCount < draggedTree.getSelectionCount(); selectedCount++) {
                    DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) draggedTree.getSelectionPaths()[selectedCount].getLastPathComponent();
                    //System.out.println("parentNode: " + parentNode.toString());
                    if (parentNode.getUserObject() instanceof ImdiTreeObject) {
                        //System.out.println("DraggedImdi: " + parentNode.getUserObject().toString());
                        draggedImdiObjects[selectedCount] = (ImdiTreeObject) (parentNode.getUserObject());
                        draggedTreeNodes[selectedCount] = parentNode;
                    } else {
                        draggedImdiObjects[selectedCount] = null;
                        draggedTreeNodes[selectedCount] = null;
                    }
                }
                classifyTransferableContents();
                return this;
            } else if (comp instanceof ImdiTable) {

                draggedImdiObjects = ((ImdiTable) comp).getSelectedRowsFromTable();
                classifyTransferableContents();
                return this;
            } else if (comp instanceof JList) {
                Object[] selectedValues = ((JList) comp).getSelectedValues();
                //System.out.println("selectedValues: " + selectedValues);
                draggedImdiObjects = new ImdiTreeObject[selectedValues.length];
                for (int selectedNodeCounter = 0; selectedNodeCounter < selectedValues.length; selectedNodeCounter++) {
                    if (selectedValues[selectedNodeCounter] instanceof ImdiTreeObject) {
                        draggedImdiObjects[selectedNodeCounter] = (ImdiTreeObject) selectedValues[selectedNodeCounter];
                    }
                }
                classifyTransferableContents();
                return this;
            }
            return null;
        }

        private void classifyTransferableContents() {
            System.out.println("classifyTransferableContents");
            // classify the draggable bundle to help matching drop targets
            for (ImdiTreeObject currentDraggedObject : draggedImdiObjects) {
                if (currentDraggedObject.isLocal()) {
                    selectionContainsLocal = true;
                    System.out.println("selectionContainsLocal");
                    if (currentDraggedObject.isDirectory()) {
                        selectionContainsLocalDirectory = true;
                        System.out.println("selectionContainsLocalDirectory");
                    } else {
                        if (!currentDraggedObject.isMetaDataNode()) {
                            selectionContainsLocalFile = true;
                            System.out.println("selectionContainsLocalFile");
                            if (currentDraggedObject.isArchivableFile()) {
                                selectionContainsArchivableLocalFile = true;
                                System.out.println("selectionContainsArchivableLocalFile");
                            }

                        }
                    }
                } else {
                    selectionContainsRemote = true;
                    System.out.println("selectionContainsRemote");
                }
                if (currentDraggedObject.isMetaDataNode()) {
                    if (currentDraggedObject.isLocal() && LinorgSessionStorage.getSingleInstance().pathIsInsideCache(currentDraggedObject.getFile())) {
                        selectionContainsImdiInCache = true;
                        System.out.println("selectionContainsImdiInCache");
                    }
                    if (currentDraggedObject.isImdiChild()) {
                        selectionContainsImdiChild = true;
                        System.out.println("selectionContainsImdiChild");
                        // only an imdichild will contain a resource
                        if (currentDraggedObject.hasResource()) {
                            selectionContainsImdiResource = true;
                            System.out.println("selectionContainsImdiResource");
                        }
                    } else if (currentDraggedObject.isSession()) {
                        selectionContainsImdiSession = true;
                        System.out.println("selectionContainsImdiSession");
                    } else if (currentDraggedObject.isCatalogue()) {
                        selectionContainsImdiCatalogue = true;
                        System.out.println("selectionContainsImdiCatalogue");
                    } else if (currentDraggedObject.isCorpus()) {
                        selectionContainsImdiCorpus = true;
                        System.out.println("selectionContainsImdiCorpus");
                    }
                    if (currentDraggedObject.isFavorite()) {
                        selectionContainsFavourite = true;
                        System.out.println("selectionContainsFavourite");
                    }
                }
            }
        }

        @Override
        public boolean importData(JComponent comp, Transferable t) {
            // due to the swing api being far to keen to do a drag drop action on the windows platform users frequently loose nodes by dragging them into random locations
            // so to avoid this we check the date time from when the transferable was created and if less than x seconds reject the drop
            if (System.currentTimeMillis() - dragStartMilliSeconds < (100 * 3)){
                return false;
            }
            try {
                System.out.println("importData: " + comp.toString());
                if (comp instanceof ImdiTable && draggedImdiObjects == null) {
                    ((ImdiTable) comp).pasteIntoSelectedTableRowsFromClipBoard();
                } else {
                    //System.out.println("draggedImdiObjects: " + draggedImdiObjects);
                    if (draggedImdiObjects != null) {
                        if (comp instanceof JTree && canDropToTarget((ImdiTree) comp)) {
                            System.out.println("comp: " + comp.getName());
                            for (int draggedCounter = 0; draggedCounter < draggedImdiObjects.length; draggedCounter++) {
                                System.out.println("dragged: " + draggedImdiObjects[draggedCounter].toString());
                            }
                            if (TreeHelper.getSingleInstance().componentIsTheFavouritesTree(currentDropTarget)) {
                                boolean resultValue = LinorgFavourites.getSingleInstance().toggleFavouritesList(draggedImdiObjects, true);
                                createTransferable(null); // clear the transfer objects
                                return resultValue;
                            } else {
                                JTree dropTree = (JTree) comp;
                                DefaultMutableTreeNode targetNode = TreeHelper.getSingleInstance().getLocalCorpusTreeSingleSelection();
                                TreeHelper.getSingleInstance().addToSortQueue(targetNode);
                                Object dropTargetUserObject = targetNode.getUserObject();
                                Vector<ImdiTreeObject> importNodeList = new Vector<ImdiTreeObject>();
                                Hashtable<ImdiTreeObject, Vector> imdiNodesDeleteList = new Hashtable<ImdiTreeObject, Vector>();
                                System.out.println("to: " + dropTargetUserObject.toString());
//                     TODO: add drag to local corpus tree
//                     TODO: consider adding a are you sure you want to move that node into this node ...
//                     TODO: must prevent parent nodes being dragged into lower branches of itself
                                if (dropTargetUserObject instanceof ImdiTreeObject) {
                                    //TODO: this should also allow drop to the root node
//                        if (((ImdiTreeObject) dropTargetUserObject).isImdiChild()) {
//                            dropTargetUserObject = ((ImdiTreeObject) dropTargetUserObject).getParentDomNode();
//                        }
                                    if (((ImdiTreeObject) dropTargetUserObject).getParentDomNode().isSession()/* || ((ImdiTreeObject) dropTargetUserObject).isImdiChild()*/) {
                                        //TODO: for now we do not allow drag on to imdi child nodes
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
                                                ((ImdiTreeObject) dropTargetUserObject).requestAddNode("Resource", draggedImdiObjects[draggedCounter]);
                                            }
                                            createTransferable(null); // clear the transfer objects
                                            return true; // we have achieved the drag so return true
                                        }
                                    }
                                }
                                // allow drop to the root node wich will not be an imditreeobject
//                    if (!(dropTargetUserObject instanceof ImdiTreeObject) || ((ImdiTreeObject) dropTargetUserObject).isCorpus()) {
                                if (selectionContainsArchivableLocalFile == false &&
                                        //                                    selectionContainsLocalFile == true &&
                                        selectionContainsLocalDirectory == false &&
                                        selectionContainsImdiResource == false &&
                                        (selectionContainsImdiCorpus == false || selectionContainsImdiSession == false) //&&
                                        //(selectionContainsImdiChild == false || GuiHelper.imdiSchema.nodeCanExistInNode((ImdiTreeObject) dropTargetUserObject, (ImdiTreeObject) draggedImdiObjects[draggedCounter]))// &&
                                        //                                    selectionContainsLocal == true &&
                                        //                                    selectionContainsRemote == false
                                        ) {
                                    System.out.println("ok to move local IMDI");
                                    for (int draggedCounter = 0; draggedCounter < draggedImdiObjects.length; draggedCounter++) {
                                        System.out.println("dragged: " + draggedImdiObjects[draggedCounter].toString());
                                        if (!((ImdiTreeObject) draggedImdiObjects[draggedCounter]).isImdiChild() || ImdiSchema.getSingleInstance().nodeCanExistInNode((ImdiTreeObject) dropTargetUserObject, (ImdiTreeObject) draggedImdiObjects[draggedCounter])) {
                                            //((ImdiTreeObject) dropTargetUserObject).requestAddNode(GuiHelper.imdiSchema.getNodeTypeFromMimeType(draggedImdiObjects[draggedCounter].mpiMimeType), "Resource", null, draggedImdiObjects[draggedCounter].getUrlString(), draggedImdiObjects[draggedCounter].mpiMimeType);

                                            // check that the node has not been dragged into itself
                                            boolean draggedIntoSelf = false;
                                            DefaultMutableTreeNode ancestorNode = targetNode;
                                            while (ancestorNode != null) {
                                                if (draggedTreeNodes[draggedCounter].equals(ancestorNode)) {
                                                    draggedIntoSelf = true;
                                                    System.out.println("found ancestor: " + draggedTreeNodes[draggedCounter] + ":" + ancestorNode);
                                                }
//                                        System.out.println("checking: " + draggedTreeNodes[draggedCounter] + ":" + ancestorNode);
                                                ancestorNode = (DefaultMutableTreeNode) ancestorNode.getParent();
                                            }
                                            // todo: test for dragged to parent session
                                            //System.out.println("found ancestor: " + draggedTreeNodes[draggedCounter] + ":" + ancestorNode);

                                            if (!draggedIntoSelf) {
                                                if (((ImdiTreeObject) draggedImdiObjects[draggedCounter]).isFavorite()) {
                                                    //  continue here
                                                    ((ImdiTreeObject) dropTargetUserObject).requestAddNode(((ImdiTreeObject) draggedImdiObjects[draggedCounter]).toString(), ((ImdiTreeObject) draggedImdiObjects[draggedCounter]));
                                                } else if (!(((ImdiTreeObject) draggedImdiObjects[draggedCounter]).isLocal() && LinorgSessionStorage.getSingleInstance().pathIsInsideCache(((ImdiTreeObject) draggedImdiObjects[draggedCounter]).getFile()))) {
                                                    importNodeList.add((ImdiTreeObject) draggedImdiObjects[draggedCounter]);
                                                } else {
                                                    String targetNodeName;
                                                    if (dropTargetUserObject instanceof ImdiTreeObject) {
                                                        targetNodeName = targetNode.getUserObject().toString();
                                                    } else {
                                                        targetNodeName = ((JLabel) targetNode.getUserObject()).getText();
                                                    }
//                                        if (draggedTreeNodes[draggedCounter].getUserObject())
                                                    int detailsOption = JOptionPane.showOptionDialog(LinorgWindowManager.getSingleInstance().linorgFrame,
                                                            "Move " + draggedTreeNodes[draggedCounter].getUserObject().toString() +
                                                            /*" from " + ((DefaultMutableTreeNode) ancestorNode.getParent()).getUserObject().toString() +*/ " to " + targetNodeName,
                                                            "Arbil",
                                                            JOptionPane.YES_NO_OPTION,
                                                            JOptionPane.PLAIN_MESSAGE,
                                                            null,
                                                            new Object[]{"Move", "Cancel"},
                                                            "Cancel");
                                                    if (detailsOption == 0) {
                                                        boolean addNodeResult = true;
                                                        if (dropTargetUserObject instanceof ImdiTreeObject) {
                                                            addNodeResult = ((ImdiTreeObject) dropTargetUserObject).addCorpusLink(draggedImdiObjects[draggedCounter]);
                                                        } else {
                                                            addNodeResult = TreeHelper.getSingleInstance().addLocation(draggedImdiObjects[draggedCounter].getURI());
                                                        }
                                                        if (addNodeResult) {
                                                            if (draggedTreeNodes[draggedCounter] != null) {
                                                                if (draggedTreeNodes[draggedCounter].getParent().equals(draggedTreeNodes[draggedCounter].getRoot())) {
                                                                    System.out.println("dragged from root");
                                                                    TreeHelper.getSingleInstance().removeLocation(draggedImdiObjects[draggedCounter]);
                                                                    TreeHelper.getSingleInstance().applyRootLocations();
                                                                } else {
                                                                    ImdiTreeObject parentImdi = (ImdiTreeObject) ((DefaultMutableTreeNode) draggedTreeNodes[draggedCounter].getParent()).getUserObject();
                                                                    System.out.println("removeing from parent: " + parentImdi);
                                                                    // add the parent and the child node to the deletelist
                                                                    if (!imdiNodesDeleteList.containsKey(parentImdi)) {
                                                                        imdiNodesDeleteList.put(parentImdi, new Vector());
                                                                    }
                                                                    imdiNodesDeleteList.get(parentImdi).add(draggedImdiObjects[draggedCounter]);
//                                                            System.out.println("delete list: " + imdiNodesDeleteList.get(parentImdi).size());
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (importNodeList.size() > 0) {
//                                  TODO: finish this import code
                                        try {
                                            ImportExportDialog importExportDialog = new ImportExportDialog(dropTree);
                                            if (dropTargetUserObject instanceof ImdiTreeObject) {
                                                importExportDialog.setDestinationNode(((ImdiTreeObject) dropTargetUserObject));
                                            }
                                            importExportDialog.copyToCache(importNodeList);
                                        } catch (Exception e) {
                                            System.out.println(e.getMessage());
                                        }
                                    }
                                    for (ImdiTreeObject currentParent : imdiNodesDeleteList.keySet()) {
                                        System.out.println("deleting by corpus link");
                                        currentParent.deleteCorpusLink(((Vector<ImdiTreeObject>) imdiNodesDeleteList.get(currentParent)).toArray(new ImdiTreeObject[]{}));
                                    }
                                    if (dropTargetUserObject instanceof ImdiTreeObject) {
                                        // TODO: this save is required to prevent user data loss, but the save and reload process may not really be required here
                                        ((ImdiTreeObject) dropTargetUserObject).saveChangesToCache(false);
                                        ((ImdiTreeObject) dropTargetUserObject).reloadNode();
                                    } else {
                                        TreeHelper.getSingleInstance().applyRootLocations();
                                    }
                                    createTransferable(null); // clear the transfer objects
                                    return true; // we have achieved the drag so return true
                                }
                            }
                        } else {
                            Container imdiSplitPanel = findImdiDropableTarget(comp);
                            if (imdiSplitPanel instanceof LinorgSplitPanel) {
                                LinorgSplitPanel targetPanel = (LinorgSplitPanel) imdiSplitPanel;
                                ImdiTableModel dropTableModel = (ImdiTableModel) targetPanel.imdiTable.getModel();
                                dropTableModel.addImdiObjects(draggedImdiObjects);
                                createTransferable(null); // clear the transfer objects
                                return true; // we have achieved the drag so return true
                            } else if (imdiSplitPanel instanceof JDesktopPane) {
                                LinorgWindowManager.getSingleInstance().openFloatingTableOnce(draggedImdiObjects, null);
                                createTransferable(null); // clear the transfer objects
                                return true; // we have achieved the drag so return true
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                GuiHelper.linorgBugCatcher.logError(ex);
            }
            createTransferable(null); // clear the transfer objects
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