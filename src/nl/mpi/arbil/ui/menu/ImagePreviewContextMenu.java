/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.arbil.ui.menu;

import javax.swing.JList;
import nl.mpi.arbil.data.ArbilNodeObject;

/**
 *
 * @author twagoo
 */
public class ImagePreviewContextMenu extends ArbilContextMenu {

    public ImagePreviewContextMenu(JList imagePreview) {
        super();
        setInvoker(imagePreview);

        Object[] selectedObjects = imagePreview.getSelectedValues();
        selectedTreeNodes = new ArbilNodeObject[selectedObjects.length];
        for (int objectCounter = 0; objectCounter < selectedObjects.length; objectCounter++) {
            selectedTreeNodes[objectCounter] = (ArbilNodeObject) selectedObjects[objectCounter];
        }
        leadSelectedTreeNode = (ArbilNodeObject) imagePreview.getSelectedValue();
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
