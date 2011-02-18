/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.arbil.ui.menu;

import javax.swing.JList;
import nl.mpi.arbil.data.ArbilDataNode;

/**
 *
 * @author twagoo
 */
public class ImagePreviewContextMenu extends ArbilContextMenu {

    public ImagePreviewContextMenu(JList imagePreview) {
        super();
        setInvoker(imagePreview);

        Object[] selectedObjects = imagePreview.getSelectedValues();
        selectedTreeNodes = new ArbilDataNode[selectedObjects.length];
        for (int objectCounter = 0; objectCounter < selectedObjects.length; objectCounter++) {
            selectedTreeNodes[objectCounter] = (ArbilDataNode) selectedObjects[objectCounter];
        }
        leadSelectedTreeNode = (ArbilDataNode) imagePreview.getSelectedValue();
    }

    @Override
    protected void setUpMenu() {
        // Nothing to do
    }

    @Override
    protected void setAllInvisible() {
        // Nothing to do
    }
}
