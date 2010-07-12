package nl.mpi.arbil;

import java.awt.Color;
import nl.mpi.arbil.data.ImdiTreeObject;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * Document   : ImdiTreeRenderer
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
public class ImdiTreeRenderer extends DefaultTreeCellRenderer {

    public ImdiTreeRenderer() {
    }

    @Override
    public Component getTreeCellRendererComponent(
            JTree tree,
            Object value,
            boolean sel,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus) {

        super.getTreeCellRendererComponent(
                tree, value, sel,
                expanded, leaf, row,
                hasFocus);
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        if (node.getUserObject() instanceof ImdiTreeObject) {
            ImdiTreeObject imdiTreeObject = (ImdiTreeObject) node.getUserObject();
            if (/*!sel && */imdiTreeObject.hasSchemaError) {
                setForeground(Color.RED);
            }
            setIcon(imdiTreeObject.getIcon());
//            setToolTipText(imdiTreeObject.toString());
            //listToolTip.setTartgetObject(imdiTreeObject);
            setEnabled(imdiTreeObject.getNodeEnabled());
            //setVisible(imdiTreeObject.getNodeEnabled());
        } else if (node.getUserObject() instanceof JLabel) {
            setIcon(((JLabel) node.getUserObject()).getIcon());
            setText(((JLabel) node.getUserObject()).getText());
        }
        return this;
    }
}
