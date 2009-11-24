package nl.mpi.arbil;

import nl.mpi.arbil.importexport.ImportExportDialog;
import nl.mpi.arbil.data.ImdiTreeObject;
import java.awt.Component;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import nl.mpi.arbil.data.ImdiLoader;
import nl.mpi.arbil.importexport.ArbilCsvImporter;

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
    private javax.swing.JMenuItem importCsvMenuItem;
//    private javax.swing.JMenu favouritesMenu;
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
    private javax.swing.JMenu historyMenu;
    private javax.swing.JMenuItem viewChangesMenuItem;
    private javax.swing.JMenuItem viewSelectedNodesMenuItem;
    private javax.swing.JMenuItem viewXmlMenuItem;
    private javax.swing.JMenuItem viewInBrrowserMenuItem;
    private javax.swing.JMenuItem viewXmlMenuItemFormatted;
    private javax.swing.JMenuItem openXmlMenuItemFormatted;
    static private ContextMenu singleInstance = null;
    ImdiTreeObject[] selectedTreeNodes = null;
    ImdiTreeObject leadSelectedTreeNode = null;

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
//        favouritesMenu = new javax.swing.JMenu();
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
        historyMenu = new javax.swing.JMenu();
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
        importCsvMenuItem = new javax.swing.JMenuItem();

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
                    if (selectedTreeNodes == null) {
                        LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("No node selected", "Copy");
                    } else {
                        ImdiTree sourceTree = (ImdiTree) treePopupMenu.getInvoker();
                        sourceTree.copyNodeUrlToClipboard(selectedTreeNodes);
                    }
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
                    for (ImdiTreeObject currentNode : selectedTreeNodes) {
                        currentNode.pasteIntoNode();
                    }
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
                    for (ImdiTreeObject currentNode : selectedTreeNodes) {
                        // this reload will first clear the save is required flag then reload
                        currentNode.reloadNode();
                    }
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
                try {
                    initAddMenu(addMenu, leadSelectedTreeNode);
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(addMenu);
//        favouritesMenu.setText("Favourites");
        addFromFavouritesMenu.setText("Add From Favourites");

        addFromFavouritesMenu.addMenuListener(new javax.swing.event.MenuListener() {

            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }

            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }

            public void menuSelected(javax.swing.event.MenuEvent evt) {
                initAddFromFavouritesMenu();
            }
        });
        treePopupMenu.add(addFromFavouritesMenu);
//        favouritesMenu.add(addFromFavouritesMenu);

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

        mergeWithFavouritesMenu.setText("Merge With Favourite");

        mergeWithFavouritesMenu.setActionCommand("Merge With Favouurite");

//        favouritesMenu.add(mergeWithFavouritesMenu);
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
                    for (ImdiTreeObject currentNode : selectedTreeNodes) {
                        GuiHelper.getSingleInstance().openImdiXmlWindow(currentNode, false, false);
                    }
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
                    for (ImdiTreeObject currentNode : selectedTreeNodes) {
                        GuiHelper.getSingleInstance().openImdiXmlWindow(currentNode, true, false);
                    }
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
                    for (ImdiTreeObject currentNode : selectedTreeNodes) {
                        GuiHelper.getSingleInstance().openImdiXmlWindow(currentNode, true, true);
                    }
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

        historyMenu.setText("History");
        historyMenu.addMenuListener(new javax.swing.event.MenuListener() {

            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }

            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }

            public void menuSelected(javax.swing.event.MenuEvent evt) {
                try {
                    initHistoryMenu();
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        treePopupMenu.add(historyMenu);


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
                    for (ImdiTreeObject selectedNode : selectedTreeNodes) {
                        System.out.println("userObject: " + selectedNode);
                        // reloading will first check if a save is required then save and reload
                        ImdiLoader.getSingleInstance().requestReload((ImdiTreeObject) selectedNode.getParentDomNode());
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
                    for (ImdiTreeObject currentNode : selectedTreeNodes) {
//                        LinorgWindowManager.getSingleInstance().openDiffWindow(currentNode);
                    }
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
                    ImportExportDialog importExportDialog = new ImportExportDialog(TreeHelper.getSingleInstance().arbilTreePanel.remoteCorpusTree);
                    importExportDialog.selectExportDirectoryAndExport(((ImdiTree) treePopupMenu.getInvoker()).getSelectedNodes());
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(exportMenuItem);
        importCsvMenuItem.setText("Import CSV");

        importCsvMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    ArbilCsvImporter csvImporter = new ArbilCsvImporter(leadSelectedTreeNode);
                    csvImporter.doImport();
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        treePopupMenu.add(importCsvMenuItem);
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
        TreeHelper.getSingleInstance().removeLocation((ImdiTreeObject) selectedTreeNode.getUserObject());
        TreeHelper.getSingleInstance().applyRootLocations();

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
        TreeHelper.getSingleInstance().removeLocation((ImdiTreeObject) selectedTreeNode.getUserObject());
        TreeHelper.getSingleInstance().applyRootLocations();

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
        for (ImdiTreeObject currentNode : selectedTreeNodes) {
            XsdChecker xsdChecker = new XsdChecker();
            LinorgWindowManager.getSingleInstance().createWindow("XsdChecker", xsdChecker);
            xsdChecker.checkXML(currentNode);
            xsdChecker.setDividerLocation(0.5);
        }
    }

    private void openFileInBrowser(ImdiTreeObject[] selectedNodes) {
        for (ImdiTreeObject currentNode : selectedNodes) {
            try {
                URI targetUri = null;
                if (currentNode.hasResource()) {
                    targetUri = new URI(currentNode.getFullResourcePath());
                } else {
                    if (currentNode.isLocal()) {
                        targetUri = currentNode.getFile().toURI(); // file to uri is the only way to reliably get the uri on 1.4 otherwise the white space will cause an error
                    } else {
                        targetUri = currentNode.getURL().toURI();
                    }
                }
                GuiHelper.getSingleInstance().openFileInExternalApplication(targetUri);
            } catch (URISyntaxException usE) {
                GuiHelper.linorgBugCatcher.logError(usE);
            }
        }
    }

    public void initAddMenu(javax.swing.JMenu addMenu, Object targetNodeUserObject) {
        addMenu.removeAll();
//        System.out.println("initAddMenu: " + targetNodeUserObject);
        ArbilTemplate currentTemplate;
        if (targetNodeUserObject instanceof ImdiTreeObject) {
            currentTemplate = ((ImdiTreeObject) targetNodeUserObject).currentTemplate;
        } else {
            currentTemplate = ArbilTemplateManager.getSingleInstance().getCurrentTemplate();
        }
        for (Enumeration menuItemName = currentTemplate.listTypesFor(targetNodeUserObject); menuItemName.hasMoreElements();) {
            String[] currentField = (String[]) menuItemName.nextElement();
//            System.out.println("MenuText: " + currentField[0]);
//            System.out.println("ActionCommand: " + currentField[1]);

            JMenuItem addMenuItem;
            addMenuItem = new javax.swing.JMenuItem();
            addMenuItem.setText(currentField[0]);
            addMenuItem.setName(currentField[0]);
            addMenuItem.setActionCommand(currentField[1]);
            addMenuItem.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    try {
//                        boolean nodesFoundToAddTo = false;
//                        for (ImdiTreeObject currentNode : selectedTreeNodes) {
//                            if (currentNode != null) {
//                                currentNode.requestAddNode(evt.getActionCommand(), ((JMenuItem) evt.getSource()).getText());
//                                nodesFoundToAddTo = true;
//                            }
//                        }
//                        if (!nodesFoundToAddTo) {
//                            // no nodes found that were valid imdi tree objects so we can assume that tis is the tree root
//                            ImdiTreeObject imdiTreeObject;
//                            imdiTreeObject = new ImdiTreeObject(LinorgSessionStorage.getSingleInstance().getSaveLocation(LinorgSessionStorage.getSingleInstance().getNewImdiFileName()));
//                            imdiTreeObject.requestAddNode(evt.getActionCommand(), ((JMenuItem) evt.getSource()).getText());
//                        }
                        if (leadSelectedTreeNode != null) {
                            leadSelectedTreeNode.requestAddNode(evt.getActionCommand(), ((JMenuItem) evt.getSource()).getText());
                        } else {
                            // no nodes found that were valid imdi tree objects so we can assume that tis is the tree root
                            ImdiTreeObject.requestRootAddNode(evt.getActionCommand(), ((JMenuItem) evt.getSource()).getText());
                        }
                    } catch (Exception ex) {
                        GuiHelper.linorgBugCatcher.logError(ex);
                    }
                }
            });
            addMenu.add(addMenuItem);
        }
    }

    public void initHistoryMenu() {
        historyMenu.removeAll();
        for (String[] currentHistory : leadSelectedTreeNode.getHistoryList()) {
            JMenuItem revertHistoryMenuItem;
            revertHistoryMenuItem = new javax.swing.JMenuItem();
            revertHistoryMenuItem.setText(currentHistory[0]);
            revertHistoryMenuItem.setName(currentHistory[0]);
            revertHistoryMenuItem.setActionCommand(currentHistory[1]);
            revertHistoryMenuItem.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    try {
                           if (!leadSelectedTreeNode.resurrectHistory(evt.getActionCommand())){
                               LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Could not revert version, no changes made", "History");
                           }
                    } catch (Exception ex) {
                        GuiHelper.linorgBugCatcher.logError(ex);
                    }
                }
            });
            historyMenu.add(revertHistoryMenuItem);
        }
    }

    public void initAddFromFavouritesMenu() {
        addFromFavouritesMenu.removeAll();
        for (Enumeration menuItemName = LinorgFavourites.getSingleInstance().listFavouritesFor(leadSelectedTreeNode); menuItemName.hasMoreElements();) {
            String[] currentField = (String[]) menuItemName.nextElement();
//            System.out.println("MenuText: " + currentField[0]);
//            System.out.println("ActionCommand: " + currentField[1]);

            JMenuItem addFavouriteMenuItem;
            addFavouriteMenuItem = new javax.swing.JMenuItem();
            addFavouriteMenuItem.setText(currentField[0]);
            addFavouriteMenuItem.setName(currentField[0]);
            addFavouriteMenuItem.setActionCommand(currentField[1]);
            addFavouriteMenuItem.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    try {
                        String imdiFavouriteUrlString = evt.getActionCommand();
                        ImdiTreeObject templateImdiObject = ImdiLoader.getSingleInstance().getImdiObject(null, imdiFavouriteUrlString);
                        if (leadSelectedTreeNode != null) {
                            leadSelectedTreeNode.requestAddNode(((JMenuItem) evt.getSource()).getText(), templateImdiObject);
                        }
//                    treeHelper.getImdiChildNodes(targetNode);
//                    String addedNodeUrlString = treeHelper.addImdiChildNode(targetNode, linorgFavourites.getNodeType(imdiTemplateUrlString), ((JMenuItem) evt.getSource()).getText());
//                    imdiLoader.getImdiObject("", addedNodeUrlString).requestMerge(imdiLoader.getImdiObject("", imdiTemplateUrlString));
//                    loop child nodes and insert them into the new node
//                    ImdiTreeObject templateImdiObject = GuiHelper.imdiLoader.getImdiObject("", imdiTemplateUrlString);
//                    ImdiTreeObject targetImdiObject = GuiHelper.imdiLoader.getImdiObject("", addedNodeUrl);
//
//                    for (Enumeration<ImdiTreeObject> childTemplateEnum = templateImdiObject.getChildEnum(); childTemplateEnum.hasMoreElements();) {
//                        ImdiTreeObject currentTemplateChild = childTemplateEnum.nextElement();
//                        String addedNodeUrl = treeHelper.addImdiChildNode(targetNode, linorgFavourites.getNodeType(currentTemplateChild.getUrlString()), currentTemplateChild.toString());
//                        linorgFavourites.mergeFromFavourite(addedNodeUrl, imdiTemplateUrlString, true);
//                    }
//                    treeHelper.reloadLocalCorpusTree(targetNode);
                    } catch (Exception ex) {
                        GuiHelper.linorgBugCatcher.logError(ex);
                    }
                }
            });
            addFromFavouritesMenu.add(addFavouriteMenuItem);
        }
    }

    public void showTreePopup(Object eventSource, int posX, int posY) {

        boolean showContextMenu = true;
        int selectionCount = ((javax.swing.JTree) eventSource).getSelectionCount();
        int nodeLevel = -1;
        if (selectionCount > 0) {
            nodeLevel = ((javax.swing.JTree) eventSource).getSelectionPath().getPathCount();
        }
//        Object leadSelectedTreeObject = ((ImdiTree) eventSource).getSingleSelectedNode();
        selectedTreeNodes = ((ImdiTree) eventSource).getSelectedNodes();
        leadSelectedTreeNode = ((ImdiTree) eventSource).getLeadSelectionNode();
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
        addFromFavouritesMenu.setVisible(false);
        mergeWithFavouritesMenu.setEnabled(false);
        saveMenuItem.setVisible(false);
        viewChangesMenuItem.setVisible(false);
        sendToServerMenuItem.setVisible(false);
        validateMenuItem.setVisible(false);
        historyMenu.setVisible(false);
        exportMenuItem.setVisible(false);
        importCsvMenuItem.setVisible(false);

        if (eventSource == TreeHelper.getSingleInstance().arbilTreePanel.remoteCorpusTree) {
            removeRemoteCorpusMenuItem.setVisible(showRemoveLocationsTasks);
            addRemoteCorpusMenuItem.setVisible(showAddLocationsTasks);
            copyBranchMenuItem.setVisible(selectionCount > 0 && nodeLevel > 1);
            addDefaultLocationsMenuItem.setVisible(showAddLocationsTasks);
        }
        if (eventSource == TreeHelper.getSingleInstance().arbilTreePanel.localCorpusTree) {
            viewSelectedNodesMenuItem.setText("View/Edit Selected");
            //removeCachedCopyMenuItem.setVisible(showRemoveLocationsTasks);
            pasteMenuItem1.setVisible(selectionCount > 0 && nodeLevel > 1);
            searchSubnodesMenuItem.setVisible(selectionCount > 0 && nodeLevel > 1);
            // a corpus can be added even at the root node
            addMenu.setVisible(selectionCount == 1); // && /*nodeLevel > 1 &&*/ TreeHelper.getSingleInstance().arbilTreePanel.localCorpusTree.getSelectionCount() > 0/* && ((DefaultMutableTreeNode)localCorpusTree.getSelectionPath().getLastPathComponent()).getUserObject() instanceof */); // could check for imdi childnodes
//            addMenu.setEnabled(nodeLevel > 1); // not yet functional so lets dissable it for now
//            addMenu.setToolTipText("test balloon on dissabled menu item");
            deleteMenuItem.setVisible(nodeLevel > 1);
            boolean nodeIsImdiChild = false;
            if (leadSelectedTreeNode != null) {
                nodeIsImdiChild = leadSelectedTreeNode.isImdiChild();
                //if (leadSelectedTreeNode.getNeedsSaveToDisk()) {
                    // saveMenuItem.setVisible(true);
                //} else if (leadSelectedTreeNode.hasHistory()) {
                    //viewChangesMenuItem.setVisible(true);
                    //sendToServerMenuItem.setVisible(true);
                //}
                viewXmlMenuItem.setVisible(!nodeIsImdiChild);
                viewXmlMenuItemFormatted.setVisible(!nodeIsImdiChild);
                openXmlMenuItemFormatted.setVisible(!nodeIsImdiChild);
                validateMenuItem.setVisible(!nodeIsImdiChild);
                historyMenu.setVisible(leadSelectedTreeNode.hasHistory());
                exportMenuItem.setVisible(!nodeIsImdiChild);
                importCsvMenuItem.setVisible(leadSelectedTreeNode.isCorpus());
                // set up the favourites menu
                addFromFavouritesMenu.setVisible(true);
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
            if (leadSelectedTreeNode != null) {
                copyBranchMenuItem.setVisible(leadSelectedTreeNode.isCorpus() || leadSelectedTreeNode.isSession());
            }
        }
        if (leadSelectedTreeNode != null) {
            saveMenuItem.setVisible(leadSelectedTreeNode.getNeedsSaveToDisk());// save sould always be available if the node has been edited
            if (leadSelectedTreeNode.isFavorite()) {
                addToFavouritesMenuItem.setVisible(true);
                addToFavouritesMenuItem.setEnabled(true);
                addMenu.setVisible(selectedTreeNodes.length == 1);// for now adding is limited to single node selections
                viewSelectedNodesMenuItem.setText("View/Edit Selected");
                addToFavouritesMenuItem.setText("Remove From Favourites List");
                addToFavouritesMenuItem.setActionCommand("false");
                deleteMenuItem.setEnabled(false);
            } else {
                addToFavouritesMenuItem.setVisible(leadSelectedTreeNode.isImdi());
                addToFavouritesMenuItem.setEnabled(!leadSelectedTreeNode.isCorpus() && leadSelectedTreeNode.isImdi());
                addToFavouritesMenuItem.setText("Add To Favourites List");
                addToFavouritesMenuItem.setActionCommand("true");
            }
        } else {
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
