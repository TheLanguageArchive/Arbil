package nl.mpi.arbil;

import java.awt.Component;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Document   : ContextMenu
 * Created on : Apr 24, 2009, 3:09:47 PM
 * code moved from LinorgFrame
 * @author Peter.Withers@mpi.nl
 */
public class ContextMenu {

    private javax.swing.JMenu addFromFavouritesMenu;
    private javax.swing.JMenuItem addLocalDirectoryMenuItem;
    private javax.swing.JCheckBoxMenuItem showHiddenFilesMenuItem;
    private javax.swing.JMenuItem addDefaultLocationsMenuItem;
    private javax.swing.JMenu addMenu;
    private javax.swing.JMenuItem addRemoteCorpusMenuItem;
    private javax.swing.JMenuItem addToFavouritesMenuItem;
    private javax.swing.JMenuItem copyBranchMenuItem;
    private javax.swing.JMenuItem copyImdiUrlMenuItem;
    private javax.swing.JMenuItem deleteMenuItem;
    private javax.swing.JMenuItem exportMenuItem;
    private javax.swing.JMenu favouritesMenu;
    private javax.swing.JMenu mergeWithFavouritesMenu;
    private javax.swing.JMenuItem pasteMenuItem1;
    private javax.swing.JMenuItem reloadSubnodesMenuItem;
    private javax.swing.JMenuItem removeCachedCopyMenuItem;
    private javax.swing.JMenuItem removeLocalDirectoryMenuItem;
    private javax.swing.JMenuItem removeRemoteCorpusMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JMenuItem searchSubnodesMenuItem;
    private javax.swing.JMenuItem sendToServerMenuItem;
    private javax.swing.JPopupMenu treePopupMenu;
    private javax.swing.JSeparator treePopupMenuSeparator1;
    private javax.swing.JSeparator treePopupMenuSeparator2;
    private javax.swing.JMenuItem validateMenuItem;
    private javax.swing.JMenuItem viewChangesMenuItem;
    private javax.swing.JMenuItem viewSelectedNodesMenuItem;
    private javax.swing.JMenuItem viewXmlMenuItem;
    private javax.swing.JMenuItem viewInBrrowserMenuItem;
    private javax.swing.JMenuItem viewXmlMenuItemFormatted;
    private javax.swing.JMenuItem openXmlMenuItemFormatted;
    static private ContextMenu singleInstance = null;

    static synchronized public ContextMenu getSingleInstance() {
        System.out.println("ContextMenu getSingleInstance");
        if (singleInstance == null) {
            singleInstance = new ContextMenu();
        }
        return singleInstance;
    }

    private ContextMenu() {
        treePopupMenu = new javax.swing.JPopupMenu();
        viewSelectedNodesMenuItem = new javax.swing.JMenuItem();
        copyImdiUrlMenuItem = new javax.swing.JMenuItem();
        pasteMenuItem1 = new javax.swing.JMenuItem();
        copyBranchMenuItem = new javax.swing.JMenuItem();
        searchSubnodesMenuItem = new javax.swing.JMenuItem();
        reloadSubnodesMenuItem = new javax.swing.JMenuItem();
        addMenu = new javax.swing.JMenu();
        favouritesMenu = new javax.swing.JMenu();
        addToFavouritesMenuItem = new javax.swing.JMenuItem();
        addFromFavouritesMenu = new javax.swing.JMenu();
        mergeWithFavouritesMenu = new javax.swing.JMenu();
        deleteMenuItem = new javax.swing.JMenuItem();
        treePopupMenuSeparator1 = new javax.swing.JSeparator();
        viewXmlMenuItem = new javax.swing.JMenuItem();
        viewXmlMenuItemFormatted = new javax.swing.JMenuItem();
        openXmlMenuItemFormatted = new javax.swing.JMenuItem();
        viewInBrrowserMenuItem = new javax.swing.JMenuItem();
        validateMenuItem = new javax.swing.JMenuItem();
        treePopupMenuSeparator2 = new javax.swing.JSeparator();
        addRemoteCorpusMenuItem = new javax.swing.JMenuItem();
        addDefaultLocationsMenuItem = new javax.swing.JMenuItem();
        removeRemoteCorpusMenuItem = new javax.swing.JMenuItem();
        removeCachedCopyMenuItem = new javax.swing.JMenuItem();
        addLocalDirectoryMenuItem = new javax.swing.JMenuItem();
        showHiddenFilesMenuItem = new javax.swing.JCheckBoxMenuItem();
        removeLocalDirectoryMenuItem = new javax.swing.JMenuItem();
        saveMenuItem = new javax.swing.JMenuItem();
        viewChangesMenuItem = new javax.swing.JMenuItem();
        sendToServerMenuItem = new javax.swing.JMenuItem();
        exportMenuItem = new javax.swing.JMenuItem();

        viewSelectedNodesMenuItem.setText("View Selected");

        viewSelectedNodesMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    viewSelectedNodesMenuItemActionPerformed(evt);
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(viewSelectedNodesMenuItem);
        copyImdiUrlMenuItem.setText("Copy");

        copyImdiUrlMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    copyImdiUrlMenuItemActionPerformed(evt);
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(copyImdiUrlMenuItem);
        pasteMenuItem1.setText("Paste");

        pasteMenuItem1.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    pasteMenuItem1ActionPerformed(evt);
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(pasteMenuItem1);
        copyBranchMenuItem.setText("Import to Local Corpus");

        copyBranchMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    copyBranchMenuItemActionPerformed(evt);
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(copyBranchMenuItem);
        searchSubnodesMenuItem.setText("Search");

        searchSubnodesMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    searchSubnodesMenuItemActionPerformed(evt);
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(searchSubnodesMenuItem);
        reloadSubnodesMenuItem.setText("Reload");

        reloadSubnodesMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    reloadSubnodesMenuItemActionPerformed(evt);
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(reloadSubnodesMenuItem);
        addMenu.setText("Add");

        addMenu.addMenuListener(new javax.swing.event.MenuListener() {

            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }

            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }

            public void menuSelected(javax.swing.event.MenuEvent evt) {
                if (addMenu.getActionCommand().equals("favourites")) {
                    GuiHelper.getSingleInstance().initAddMenu(addMenu, ((ImdiTree) TreeHelper.getSingleInstance().arbilTreePanel.favouritesTree).getSingleSelectedNode());
                } else {
                    GuiHelper.getSingleInstance().initAddMenu(addMenu, ((ImdiTree) TreeHelper.getSingleInstance().arbilTreePanel.localCorpusTree).getSingleSelectedNode());
                }
            }
        });

        treePopupMenu.add(addMenu);
        favouritesMenu.setText("Favourites");
        addToFavouritesMenuItem.setText("Set As Favourite");

        addToFavouritesMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    LinorgFavourites.getSingleInstance().toggleFavouritesList(((ImdiTree) treePopupMenu.getInvoker()).getSelectedNodes(), addToFavouritesMenuItem.getActionCommand().equals("true"));
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(addToFavouritesMenuItem);
        addFromFavouritesMenu.setText("Add From Favourites");

        addFromFavouritesMenu.addMenuListener(new javax.swing.event.MenuListener() {

            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }

            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }

            public void menuSelected(javax.swing.event.MenuEvent evt) {
                GuiHelper.getSingleInstance().initAddFromFavouritesMenu(addFromFavouritesMenu, ((ImdiTree) TreeHelper.getSingleInstance().arbilTreePanel.localCorpusTree).getSingleSelectedNode());
            }
        });

        favouritesMenu.add(addFromFavouritesMenu);
        mergeWithFavouritesMenu.setText("Merge With Favourite");

        mergeWithFavouritesMenu.setActionCommand("Merge With Favouurite");

        favouritesMenu.add(mergeWithFavouritesMenu);
        treePopupMenu.add(favouritesMenu);
        deleteMenuItem.setText("Delete");

        deleteMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    TreeHelper.getSingleInstance().deleteNode(treePopupMenu.getInvoker());
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(deleteMenuItem);

        treePopupMenu.add(treePopupMenuSeparator1);

        viewInBrrowserMenuItem.setText("Open in External Application");
        viewInBrrowserMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    openFileInBrowser(((ImdiTree) treePopupMenu.getInvoker()).getSelectedNodes());
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        treePopupMenu.add(viewInBrrowserMenuItem);

        viewXmlMenuItem.setText("View IMDI XML");

        viewXmlMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    GuiHelper.getSingleInstance().openImdiXmlWindow(((ImdiTree) treePopupMenu.getInvoker()).getSingleSelectedNode(), false, false);
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(viewXmlMenuItem);
        viewXmlMenuItemFormatted.setText("View IMDI Formatted");

        viewXmlMenuItemFormatted.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    GuiHelper.getSingleInstance().openImdiXmlWindow(((ImdiTree) treePopupMenu.getInvoker()).getSingleSelectedNode(), true, false);
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(viewXmlMenuItemFormatted);
        openXmlMenuItemFormatted.setText("Open IMDI Formatted");
        openXmlMenuItemFormatted.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    GuiHelper.getSingleInstance().openImdiXmlWindow(((ImdiTree) treePopupMenu.getInvoker()).getSingleSelectedNode(), true, true);
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        treePopupMenu.add(openXmlMenuItemFormatted);

        validateMenuItem.setText("Check IMDI format");

        validateMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    validateMenuItemActionPerformed(evt);
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(validateMenuItem);

        treePopupMenu.add(treePopupMenuSeparator2);
        addRemoteCorpusMenuItem.setText("Add Remote Location");

        addRemoteCorpusMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    addRemoteCorpusMenuItemActionPerformed(evt);
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(addRemoteCorpusMenuItem);
        addDefaultLocationsMenuItem.setText("Add Default Remote Locations");

        addDefaultLocationsMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    addDefaultLocationsMenuItemActionPerformed(evt);
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(addDefaultLocationsMenuItem);
        removeRemoteCorpusMenuItem.setText("Remove Remote Location");

        removeRemoteCorpusMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    removeRemoteCorpusMenuItemActionPerformed(evt);
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(removeRemoteCorpusMenuItem);
        removeCachedCopyMenuItem.setText("Remove Cache Link");

        removeCachedCopyMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    removeCachedCopyMenuItemActionPerformed(evt);
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(removeCachedCopyMenuItem);
        addLocalDirectoryMenuItem.setText("Add Working Directory");

        addLocalDirectoryMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    addLocalDirectoryMenuItemActionPerformed(evt);
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(addLocalDirectoryMenuItem);

        showHiddenFilesMenuItem.setText("Show Hidden Files");
        showHiddenFilesMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    TreeHelper.getSingleInstance().setShowHiddenFilesInTree(showHiddenFilesMenuItem.getState());
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        treePopupMenu.add(showHiddenFilesMenuItem);

        removeLocalDirectoryMenuItem.setText("Remove Link to Directory");

        removeLocalDirectoryMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    removeLocalDirectoryMenuItemActionPerformed(evt);
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(removeLocalDirectoryMenuItem);
        saveMenuItem.setText("Save Changes to Disk");

        saveMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    for (ImdiTreeObject selectedNode : TreeHelper.getSingleInstance().arbilTreePanel.localCorpusTree.getSelectedNodes()) {
                        System.out.println("userObject: " + selectedNode);
                        // reloading will first check if a save is required then save and reload
                        GuiHelper.imdiLoader.requestReload((ImdiTreeObject) selectedNode);
                    }

                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(saveMenuItem);
        viewChangesMenuItem.setText("View Changes");
        viewChangesMenuItem.setEnabled(false);

        viewChangesMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
//                    LinorgWindowManager.getSingleInstance().openDiffWindow(((ImdiTree) treePopupMenu.getInvoker()).getSingleSelectedNode());
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(viewChangesMenuItem);
        sendToServerMenuItem.setText("Send to Server");

        sendToServerMenuItem.setEnabled(false);

        sendToServerMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    sendToServerMenuItemActionPerformed(evt);

                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(sendToServerMenuItem);
        exportMenuItem.setText("Export");

        exportMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    exportMenuItemActionPerformed(evt);
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(exportMenuItem);
    }

    private void copyBranchMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:    
        if (treePopupMenu.getInvoker() instanceof JTree) {
            try {
                ImportExportDialog importExportDialog = new ImportExportDialog(treePopupMenu.getInvoker());
                importExportDialog.copyToCache(((ImdiTree) treePopupMenu.getInvoker()).getSelectedNodes());
            } catch (Exception ex) {
                GuiHelper.linorgBugCatcher.logError(ex);
            }
        }
    }

    private void addLocalDirectoryMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        //GEN-FIRST:event_addLocalDirectoryMenuItemActionPerformed
        // TODO add your handling code here:    
        JFileChooser fc = new JFileChooser();
        //fc.setDialogTitle(getResourceMap().getString(name + ".dialogTitle"));
        //String textFilesDesc = getResourceMap().getString("txtFileExtensionDescription");   
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setDialogTitle("Add Working Directory");
        int option = fc.showOpenDialog(LinorgWindowManager.getSingleInstance().linorgFrame);

        if (JFileChooser.APPROVE_OPTION == option) {
            try {
                TreeHelper.getSingleInstance().addLocationGui(fc.getSelectedFile().getCanonicalPath());

            } catch (IOException ex) {
                GuiHelper.linorgBugCatcher.logError(ex);
//            System.out.println("Error adding location: " + ex.getMessage());
            }
        }
    }

    private void copyImdiUrlMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        //GEN-FIRST:event_copyImdiUrlMenuItemActionPerformed
        // TODO add your handling code here:    
        //DefaultMutableTreeNode selectedTreeNode = null;   
        ImdiTree sourceTree = (ImdiTree) treePopupMenu.getInvoker();
        ImdiTreeObject[] selectedImdiNodes = sourceTree.getSelectedNodes();
        if (selectedImdiNodes == null) {
            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("No node selected", "Copy");
        } else {
            sourceTree.copyNodeUrlToClipboard(selectedImdiNodes);
        }
    }//GEN-LAST:event_copyImdiUrlMenuItemActionPerformed

    private void addRemoteCorpusMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        //GEN-FIRST:event_addRemoteCorpusMenuItemActionPerformed
        // TODO add your handling code here:    
        String addableLocation = (String) JOptionPane.showInputDialog(LinorgWindowManager.getSingleInstance().linorgFrame, "Enter the URL", "Add Location", JOptionPane.PLAIN_MESSAGE);

        if ((addableLocation != null) && (addableLocation.length() > 0)) {
            TreeHelper.getSingleInstance().addLocationGui(addableLocation);
        }
    }//GEN-LAST:event_addRemoteCorpusMenuItemActionPerformed

    private void addDefaultLocationsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        //GEN-FIRST:event_addDefaultLocationsMenuItemActionPerformed
        // TODO add your handling code here:    
        if (0 < TreeHelper.getSingleInstance().addDefaultCorpusLocations()) {
            TreeHelper.getSingleInstance().applyRootLocations();

        } else {
            // alert the user when the node already exists and cannot be added again       
            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("The defalut locations already exists and will not be added again", "Add Default Locations");

        }
    }//GEN-LAST:event_addDefaultLocationsMenuItemActionPerformed

    private void removeRemoteCorpusMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        //GEN-FIRST:event_removeRemoteCorpusMenuItemActionPerformed// TODO add your handling code here:   
        DefaultMutableTreeNode selectedTreeNode = null;

        if (TreeHelper.getSingleInstance().arbilTreePanel.remoteCorpusTree.getSelectionPath() != null) {
            selectedTreeNode = (DefaultMutableTreeNode) TreeHelper.getSingleInstance().arbilTreePanel.remoteCorpusTree.getSelectionPath().getLastPathComponent();

        }
        TreeHelper.getSingleInstance().removeSelectedLocation(selectedTreeNode);

    }//GEN-LAST:event_removeRemoteCorpusMenuItemActionPerformed

    private void removeCachedCopyMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        //GEN-FIRST:event_removeCachedCopyMenuItemActionPerformed
        // TODO add your handling code here://    
        DefaultMutableTreeNode selectedTreeNode = null;
        //    if (localCorpusTree.getSelectionPath() != null) {
        //        selectedTreeNode = (DefaultMutableTreeNode) localCorpusTree.getSelectionPath().getLastPathComponent();
        //    }
        //    GuiHelper.treeHelper.removeSelectedLocation(selectedTreeNode);
    }
    //GEN-LAST:event_removeCachedCopyMenuItemActionPerformed

    private void removeLocalDirectoryMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        //GEN-FIRST:event_removeLocalDirectoryMenuItemActionPerformed
        // TODO add your handling code here: 
        DefaultMutableTreeNode selectedTreeNode = null;

        if (TreeHelper.getSingleInstance().arbilTreePanel.localDirectoryTree.getSelectionPath() != null) {
            selectedTreeNode = (DefaultMutableTreeNode) TreeHelper.getSingleInstance().arbilTreePanel.localDirectoryTree.getSelectionPath().getLastPathComponent();

        }
        TreeHelper.getSingleInstance().removeSelectedLocation(selectedTreeNode);

    }//GEN-LAST:event_removeLocalDirectoryMenuItemActionPerformed

    private void searchSubnodesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        //GEN-FIRST:event_searchSubnodesMenuItemActionPerformed
        // TODO add your handling code here:    
        LinorgWindowManager.getSingleInstance().openSearchTable(((ImdiTree) TreeHelper.getSingleInstance().arbilTreePanel.localCorpusTree).getSelectedNodes(), "Search");

    }//GEN-LAST:event_searchSubnodesMenuItemActionPerformed

    private void viewSelectedNodesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        //GEN-FIRST:event_viewSelectedNodesMenuItemActionPerformed
        // TODO add your handling code here:   
        LinorgWindowManager.getSingleInstance().openFloatingTableOnce(((ImdiTree) treePopupMenu.getInvoker()).getSelectedNodes(), null);

    }//GEN-LAST:event_viewSelectedNodesMenuItemActionPerformed

    private void sendToServerMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        //GEN-FIRST:event_sendToServerMenuItemActionPerformed
        // TODO add your handling code here:
    }
    //GEN-LAST:event_sendToServerMenuItemActionPerformed

    private void validateMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        //GEN-FIRST:event_validateMenuItemActionPerformed
        // TODO add your handling code here:  
        XsdChecker xsdChecker = new XsdChecker();

        LinorgWindowManager.getSingleInstance().createWindow("XsdChecker", xsdChecker);
// TODO: check the node type before passing
        xsdChecker.checkXML((ImdiTreeObject) ((ImdiTree) treePopupMenu.getInvoker()).getSingleSelectedNode());

        xsdChecker.setDividerLocation(0.5);

    }//GEN-LAST:event_validateMenuItemActionPerformed

    private void reloadSubnodesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        //GEN-FIRST:event_reloadSubnodesMenuItemActionPerformed
        // TODO add your handling code here:    
        // this reload will first clear the save is required flag then reload   
        ((ImdiTreeObject) ((ImdiTree) treePopupMenu.getInvoker()).getSingleSelectedNode()).reloadNode();

    }//GEN-LAST:event_reloadSubnodesMenuItemActionPerformed

    private void openFileInBrowser(ImdiTreeObject[] selectedNodes) {
        for (ImdiTreeObject currentNode : selectedNodes) {
            try {
                URI targetUri = null;
                if (currentNode.hasResource()) {
                    targetUri = new URI(currentNode.getFullResourcePath());
                } else {
                    targetUri = currentNode.getURL().toURI();
                }
                GuiHelper.getSingleInstance().openFileInExternalApplication(targetUri);
            } catch (URISyntaxException usE) {
                GuiHelper.linorgBugCatcher.logError(usE);
            }
        }
    }

    private void exportMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        //GEN-FIRST:event_exportMenuItemActionPerformed
        // TODO add your handling code here:   
        // directory selection dialog   
        // make sure the chosen directory is empty   
        // export the tree, maybe adjusting resource links so that resource files do not need to be copied
        try {
            ImportExportDialog importExportDialog = new ImportExportDialog(TreeHelper.getSingleInstance().arbilTreePanel.remoteCorpusTree);
            importExportDialog.exportImdiBranch(((ImdiTree) treePopupMenu.getInvoker()).getSelectedNodes());
        } catch (Exception ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
        }
    }//GEN-LAST:event_exportMenuItemActionPerformed

    private void pasteMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {
        //GEN-FIRST:event_pasteMenuItem1ActionPerformed// TODO add your handling code here:  
        Object targetNode = ((ImdiTree) treePopupMenu.getInvoker()).getSingleSelectedNode();

        if (targetNode instanceof ImdiTreeObject && targetNode != null) {
            ((ImdiTreeObject) targetNode).pasteIntoNode();

        }
    }//GEN-LAST:event_pasteMenuItem1ActionPerformed  

    public void showTreePopup(Object eventSource, int posX, int posY) {

        boolean showContextMenu = true;
        int selectionCount = ((javax.swing.JTree) eventSource).getSelectionCount();
        int nodeLevel = -1;
        if (selectionCount > 0) {
            nodeLevel = ((javax.swing.JTree) eventSource).getSelectionPath().getPathCount();
        }
        Object leadSelectedTreeObject = ((ImdiTree) eventSource).getSingleSelectedNode();
        boolean showRemoveLocationsTasks = selectionCount == 1 && nodeLevel == 2;
        boolean showAddLocationsTasks = selectionCount == 1 && nodeLevel == 1;
        //System.out.println("path count: " + ((JTree) evt.getSource()).getSelectionPath().getPathCount());
        // set up the contect menu
        removeCachedCopyMenuItem.setVisible(false);
        removeLocalDirectoryMenuItem.setVisible(false);
        addLocalDirectoryMenuItem.setVisible(false);
        showHiddenFilesMenuItem.setVisible(false);
        removeRemoteCorpusMenuItem.setVisible(false);
        addRemoteCorpusMenuItem.setVisible(false);
        copyBranchMenuItem.setVisible(false);
        copyImdiUrlMenuItem.setVisible(false);
        pasteMenuItem1.setVisible(false);
        viewXmlMenuItem.setVisible(false);
        viewXmlMenuItemFormatted.setVisible(false);
        openXmlMenuItemFormatted.setVisible(false);
        viewInBrrowserMenuItem.setVisible(false);
        searchSubnodesMenuItem.setVisible(false);
        reloadSubnodesMenuItem.setVisible(false);
        addDefaultLocationsMenuItem.setVisible(false);
        addMenu.setVisible(false);
        deleteMenuItem.setVisible(false);
        deleteMenuItem.setEnabled(true);
        viewSelectedNodesMenuItem.setVisible(false);
        viewSelectedNodesMenuItem.setText("View Selected");
        favouritesMenu.setVisible(false);
        mergeWithFavouritesMenu.setEnabled(false);
        saveMenuItem.setVisible(false);
        viewChangesMenuItem.setVisible(false);
        sendToServerMenuItem.setVisible(false);
        validateMenuItem.setVisible(false);
        exportMenuItem.setVisible(false);

        if (eventSource == TreeHelper.getSingleInstance().arbilTreePanel.remoteCorpusTree) {
            removeRemoteCorpusMenuItem.setVisible(showRemoveLocationsTasks);
            addRemoteCorpusMenuItem.setVisible(showAddLocationsTasks);
            copyBranchMenuItem.setVisible(selectionCount > 0 && nodeLevel > 1);
            addDefaultLocationsMenuItem.setVisible(showAddLocationsTasks);
        }
        if (eventSource == TreeHelper.getSingleInstance().arbilTreePanel.favouritesTree) {
            addMenu.setActionCommand("favourites");
        } else if (eventSource == TreeHelper.getSingleInstance().arbilTreePanel.localCorpusTree) {
            addMenu.setActionCommand("LocalCorpusTree");
            viewSelectedNodesMenuItem.setText("View/Edit Selected");
            //removeCachedCopyMenuItem.setVisible(showRemoveLocationsTasks);
            pasteMenuItem1.setVisible(selectionCount > 0 && nodeLevel > 1);
            searchSubnodesMenuItem.setVisible(selectionCount > 0 && nodeLevel > 1);
            // a corpus can be added even at the root node
            addMenu.setVisible(selectionCount > 0); // && /*nodeLevel > 1 &&*/ TreeHelper.getSingleInstance().arbilTreePanel.localCorpusTree.getSelectionCount() > 0/* && ((DefaultMutableTreeNode)localCorpusTree.getSelectionPath().getLastPathComponent()).getUserObject() instanceof */); // could check for imdi childnodes
//            addMenu.setEnabled(nodeLevel > 1); // not yet functional so lets dissable it for now
//            addMenu.setToolTipText("test balloon on dissabled menu item");
            deleteMenuItem.setVisible(nodeLevel > 1);
            boolean nodeIsImdiChild = false;
            if (leadSelectedTreeObject != null && leadSelectedTreeObject instanceof ImdiTreeObject) {
                nodeIsImdiChild = ((ImdiTreeObject) leadSelectedTreeObject).isImdiChild();
                if (((ImdiTreeObject) leadSelectedTreeObject).needsSaveToDisk) {
                    saveMenuItem.setVisible(true);
                } else if (((ImdiTreeObject) leadSelectedTreeObject).needsChangesSentToServer()) {
                    viewChangesMenuItem.setVisible(true);
                    sendToServerMenuItem.setVisible(true);
                }
                viewXmlMenuItem.setVisible(!nodeIsImdiChild);
                viewXmlMenuItemFormatted.setVisible(!nodeIsImdiChild);
                openXmlMenuItemFormatted.setVisible(!nodeIsImdiChild);
                validateMenuItem.setVisible(!nodeIsImdiChild);
                exportMenuItem.setVisible(!nodeIsImdiChild);
                // set up the favourites menu
                favouritesMenu.setVisible(true);
            }
            //deleteMenuItem.setEnabled(!nodeIsImdiChild && selectionCount == 1);
//            addMenu.setEnabled(!nodeIsImdiChild);
            showContextMenu = true; //nodeLevel != 1;
        }
        if (eventSource == TreeHelper.getSingleInstance().arbilTreePanel.localDirectoryTree) {
            removeLocalDirectoryMenuItem.setVisible(showRemoveLocationsTasks);
            if (showAddLocationsTasks) {
                showHiddenFilesMenuItem.setState(TreeHelper.getSingleInstance().showHiddenFilesInTree);
                showHiddenFilesMenuItem.setVisible(true);
            }
            addLocalDirectoryMenuItem.setVisible(showAddLocationsTasks);
            if (leadSelectedTreeObject instanceof ImdiTreeObject) {
                copyBranchMenuItem.setVisible(((ImdiTreeObject) leadSelectedTreeObject).isCorpus() || ((ImdiTreeObject) leadSelectedTreeObject).isSession());
            }
        }
        if (leadSelectedTreeObject != null && leadSelectedTreeObject instanceof ImdiTreeObject) {
            addToFavouritesMenuItem.setVisible(((ImdiTreeObject) leadSelectedTreeObject).isImdi());
            addToFavouritesMenuItem.setEnabled(!((ImdiTreeObject) leadSelectedTreeObject).isCorpus() && ((ImdiTreeObject) leadSelectedTreeObject).isImdi());
            if (((ImdiTreeObject) leadSelectedTreeObject).isFavorite()) {
                //addMenu.setVisible(true);// TODO; resolve the issues adding to a favourites session
                saveMenuItem.setVisible(((ImdiTreeObject) leadSelectedTreeObject).needsSaveToDisk);
                addToFavouritesMenuItem.setText("Remove From Favourites List");
                addToFavouritesMenuItem.setActionCommand("false");
                deleteMenuItem.setEnabled(false);
            } else {
                addToFavouritesMenuItem.setText("Add To Favourites List");
                addToFavouritesMenuItem.setActionCommand("true");
            }
        } else {
            addMenu.setActionCommand(null);
            addToFavouritesMenuItem.setVisible(false);
        }
        viewInBrrowserMenuItem.setVisible(nodeLevel > 1);
        copyImdiUrlMenuItem.setVisible((selectionCount == 1 && nodeLevel > 1) || selectionCount > 1); // show the copy menu providing some nodes are selected and the root node is not the only one selected

        viewSelectedNodesMenuItem.setVisible(selectionCount >= 1 && nodeLevel > 1);
        reloadSubnodesMenuItem.setVisible(selectionCount > 0 && nodeLevel > 1);

        // hide show the separators
        treePopupMenuSeparator2.setVisible(nodeLevel != 1 && showRemoveLocationsTasks && eventSource != TreeHelper.getSingleInstance().arbilTreePanel.localDirectoryTree);
        treePopupMenuSeparator1.setVisible(nodeLevel != 1 && eventSource == TreeHelper.getSingleInstance().arbilTreePanel.localCorpusTree);

        // store the event source
        treePopupMenu.setInvoker((javax.swing.JTree) eventSource);

        // show the context menu
        if (showContextMenu) {
            if (eventSource instanceof Component) {
                treePopupMenu.setInvoker((Component) eventSource);
            }
            treePopupMenu.show((java.awt.Component) eventSource, posX, posY);
            treePopupMenu.requestFocusInWindow();
        }
    }
}
