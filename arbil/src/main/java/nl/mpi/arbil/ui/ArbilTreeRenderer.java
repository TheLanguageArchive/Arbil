package nl.mpi.arbil.ui;

import java.awt.Color;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilNode;

/**
 * Document : ArbilTreeRenderer
 * Created on :
 *
 * @author Peter.Withers@mpi.nl
 */
public class ArbilTreeRenderer extends DefaultTreeCellRenderer {

    @Override
    synchronized public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        DefaultTreeCellRenderer returnComponent = this;
        returnComponent.setTextNonSelectionColor(tree.getForeground());
        returnComponent.setBackgroundNonSelectionColor(tree.getBackground());
//        returnComponent.setIcon(null); // setting the icon to null to avoid a null pointer later in the set icon where it tries to testthe icon width/hight
//        returnComponent.setText(null);
        String valueString = "";
        Icon nodeIcon = null;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        if (node.getUserObject() instanceof ArbilNode) {
            ArbilNode arbilNode = (ArbilNode) node.getUserObject();
            // create the object with parameters so the jvm has a chance to reused objects in memory
//            returnComponent = new JLabel(arbilNode.toString(), arbilNode.getIcon(), JLabel.LEFT);
            valueString = (arbilNode.toString());
            nodeIcon = (arbilNode.getIcon());
            returnComponent.setHorizontalAlignment(JLabel.LEFT);
            if (arbilNode instanceof ArbilDataNode) {
                if (((ArbilDataNode) arbilNode).isContainerNode()) {
                    returnComponent.setTextNonSelectionColor(Color.DARK_GRAY);
                    valueString = ("<u>" + arbilNode.toString() + "</u>");
                }
                if (/* !sel && */((ArbilDataNode) arbilNode).getNeedsSaveToDisk(true)) {
                    returnComponent.setTextNonSelectionColor(Color.BLUE);
                }
                if (/* !sel && */((ArbilDataNode) arbilNode).hasSchemaError) {
                    returnComponent.setTextNonSelectionColor(Color.RED);
                }
            }
        } else if (node.getUserObject() instanceof JLabel) {
            // create the object with parameters so the jvm has a chance to reused objects in memory
            valueString = (((JLabel) node.getUserObject()).getText());
            nodeIcon = ((JLabel) node.getUserObject()).getIcon();
        }
//        returnComponent.setBackgroundSelectionColor(tree.getBackground().darker());
        returnComponent.setClosedIcon(nodeIcon);
        returnComponent.setOpenIcon(nodeIcon);
        returnComponent.setLeafIcon(nodeIcon);
        return super.getTreeCellRendererComponent(tree, "<html>" + valueString + "</html>", selected, expanded, leaf, row, hasFocus);
    }
}
