/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.arbil.ui.menu;

import java.awt.Component;
import java.io.File;
import java.net.URI;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.importexport.ArbilToHtmlConverter;
import nl.mpi.arbil.ui.ArbilWindowManager;
import nl.mpi.arbil.ui.GuiHelper;

/**
 * Abstract base class for context menus
 * 
 * @author Twan Goosen
 */
public abstract class ArbilContextMenu extends JPopupMenu {

    public void show(int posX, int posY) {
        // Set common and concrete invisible
        setCommonInvisible();
        setAllInvisible();

        // Set up concrete menu
        setUpMenu();

        // Set up common items & actions
        setUpCommonMenuItems();
        setUpCommonActions();

        // Configure separators
        configureMenuSeparators();

        // Show menu
        super.show(getInvoker(), posX, posY);
        requestFocusInWindow();
    }

    protected abstract void setUpMenu();

    protected abstract void setAllInvisible();

    private void setUpCommonActions() {
        browseForResourceFileMenuItem.setText("Browse For Resource File");
        browseForResourceFileMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    File[] selectedFiles = ArbilWindowManager.getSingleInstance().showFileSelectBox("Select Resource File", false, false, false);
                    if (selectedFiles != null && selectedFiles.length > 0) {
                        leadSelectedTreeNode.resourceUrlField.setFieldValue(selectedFiles[0].toURL().toExternalForm(), true, false);
                    }
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        add(browseForResourceFileMenuItem);


        add(new JSeparator());

        overrideTypeCheckerDecision.setText("Override Type Checker Decision");
        overrideTypeCheckerDecision.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    String titleString = "Override Type Checker Decision";
                    String messageString = "The type checker does not recognise the selected file/s, which means that they\nare not an archivable type. This action will override that decision and allow you\nto add the file/s to a session, as either media or written resources,\nhowever it might not be possible to import the result to the copus server.";
                    String[] optionStrings = {"WrittenResource", "MediaFile", "Cancel"};
                    int userSelection = JOptionPane.showOptionDialog(ArbilWindowManager.getSingleInstance().linorgFrame.getContentPane(), messageString, titleString, JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, optionStrings, optionStrings[2]);
                    if (optionStrings[userSelection].equals("WrittenResource") || optionStrings[userSelection].equals("MediaFile")) {
                        for (ArbilDataNode currentNode : selectedTreeNodes) {
                            if (currentNode.mpiMimeType == null) {
                                currentNode.mpiMimeType = "Manual/" + optionStrings[userSelection];
                                currentNode.typeCheckerMessage = "Manually overridden (might not be compatible with the archive)";
                                currentNode.clearIcon();
                            }
                        }
                    }
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        add(overrideTypeCheckerDecision);

        viewInBrowserMenuItem.setText("Open in External Application");
        // todo: add custom applicaitons menu with dialogue to enter them: suffix, switches, applicaiton file
        viewInBrowserMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    openFileInExternalApplication(selectedTreeNodes);
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        add(viewInBrowserMenuItem);

        viewXmlMenuItem.setText("View XML");

        viewXmlMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    for (ArbilDataNode currentNode : selectedTreeNodes) {
                        GuiHelper.getSingleInstance().openImdiXmlWindow(currentNode, false, false);
                    }
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        add(viewXmlMenuItem);
        viewXmlMenuItemFormatted.setText("View IMDI Formatted");

        viewXmlMenuItemFormatted.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    for (ArbilDataNode currentNode : selectedTreeNodes) {
                        GuiHelper.getSingleInstance().openImdiXmlWindow(currentNode, true, false);
                    }
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });

        add(viewXmlMenuItemFormatted);
        openXmlMenuItemFormatted.setText("Open IMDI Formatted");
        openXmlMenuItemFormatted.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    for (ArbilDataNode currentNode : selectedTreeNodes) {
                        GuiHelper.getSingleInstance().openImdiXmlWindow(currentNode, true, true);
                    }
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        add(openXmlMenuItemFormatted);

        exportHtmlMenuItemFormatted.setText("Export IMDI to HTML");
        exportHtmlMenuItemFormatted.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    new ArbilToHtmlConverter().exportImdiToHtml(selectedTreeNodes);
                } catch (Exception ex) {
                    GuiHelper.linorgBugCatcher.logError(ex);
                }
            }
        });
        add(exportHtmlMenuItemFormatted);

    }

    protected void setUpCommonMenuItems() {
//        todo: continue moving common menu items here
        if (leadSelectedTreeNode != null) {
            // TODO: test that the node is editable
            //if (leadSelectedTreeNode.is)
            if (leadSelectedTreeNode.hasResource()) {
                browseForResourceFileMenuItem.setVisible(true);
            }
            if (!leadSelectedTreeNode.isChildNode() && leadSelectedTreeNode.isMetaDataNode()) {
                viewXmlMenuItem.setVisible(true);
                viewXmlMenuItemFormatted.setVisible(true);
                openXmlMenuItemFormatted.setVisible(true);
                exportHtmlMenuItemFormatted.setVisible(true);
            }
            viewInBrowserMenuItem.setVisible(true);
            overrideTypeCheckerDecision.setVisible(!leadSelectedTreeNode.isMetaDataNode() && leadSelectedTreeNode.mpiMimeType == null);
        }
    }

    private void configureMenuSeparators() {
        // hide and show the separators so that no two separators are displayed without a menu item inbetween
        boolean lastWasSeparator = true;
        Component lastVisibleComponent = null;
        for (Component currentComponent : getComponents()) {
            if (currentComponent instanceof JSeparator) {
//                if (lastWasSeparator == true) {
                currentComponent.setVisible(!lastWasSeparator);
//                }
                lastWasSeparator = true;
            } else if (currentComponent.isVisible()) {
                lastWasSeparator = false;
            }
            if (currentComponent.isVisible()) {
                lastVisibleComponent = currentComponent;
            }
        }
        if (lastVisibleComponent != null && lastVisibleComponent instanceof JSeparator) {
            lastVisibleComponent.setVisible(false);
        }
    }

    private void openFileInExternalApplication(ArbilDataNode[] selectedNodes) {
        for (ArbilDataNode currentNode : selectedNodes) {
            URI targetUri = null;
            if (currentNode.hasResource()) {
                targetUri = currentNode.getFullResourceURI();
            } else {
                targetUri = currentNode.getURI();
            }
            GuiHelper.getSingleInstance().openFileInExternalApplication(targetUri);
        }
    }

    protected void setCommonInvisible() {
        viewXmlMenuItem.setVisible(false);
        viewXmlMenuItemFormatted.setVisible(false);
        openXmlMenuItemFormatted.setVisible(false);
        exportHtmlMenuItemFormatted.setVisible(false);
        overrideTypeCheckerDecision.setVisible(false);
        viewInBrowserMenuItem.setVisible(false);
        browseForResourceFileMenuItem.setVisible(false);
    }

    protected ArbilDataNode[] selectedTreeNodes = null;
    protected ArbilDataNode leadSelectedTreeNode = null;
    
    private JMenuItem browseForResourceFileMenuItem = new JMenuItem();
    private JMenuItem viewXmlMenuItem = new JMenuItem();
    private JMenuItem viewXmlMenuItemFormatted = new JMenuItem();
    private JMenuItem viewInBrowserMenuItem = new JMenuItem();
    private JMenuItem openXmlMenuItemFormatted = new JMenuItem();
    private JMenuItem exportHtmlMenuItemFormatted = new JMenuItem();
    private JMenuItem overrideTypeCheckerDecision = new JMenuItem();
}
