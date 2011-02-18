package nl.mpi.arbil.ui;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import nl.mpi.arbil.data.ArbilDataNode;

/**
 * Document   : ArbilTreeRenderer
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
public class ArbilTreeRenderer implements TreeCellRenderer {

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        JLabel returnComponent;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        if (node.getUserObject() instanceof ArbilDataNode) {
            ArbilDataNode arbilDataNode = (ArbilDataNode) node.getUserObject();
            // create the object with parameters so the jvm has a chance to reused objects in memory
            returnComponent = new JLabel(arbilDataNode.toString(), arbilDataNode.getIcon(), JLabel.LEFT);
            if (/*!sel && */arbilDataNode.hasSchemaError) {
                returnComponent.setForeground(Color.RED);
            }
            if (/*!sel && */arbilDataNode.getNeedsSaveToDisk(true)) {
                returnComponent.setForeground(Color.BLUE);
            }
        } else if (node.getUserObject() instanceof JLabel) {
            // create the object with parameters so the jvm has a chance to reused objects in memory
            returnComponent = new JLabel(((JLabel) node.getUserObject()).getText(), ((JLabel) node.getUserObject()).getIcon(), JLabel.LEFT);
        } else {
            return new JLabel();
        }
        if (selected) {
            returnComponent.setOpaque(true);
            returnComponent.setBackground(tree.getBackground().darker());
        }
        return returnComponent;
    }
}
