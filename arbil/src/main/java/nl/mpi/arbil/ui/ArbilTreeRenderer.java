package nl.mpi.arbil.ui;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilNode;

/**
 * Document : ArbilTreeRenderer
 * Created on :
 *
 * @author Peter.Withers@mpi.nl
 */
public class ArbilTreeRenderer extends JLabel implements TreeCellRenderer {

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        JLabel returnComponent = this;
        this.setForeground(tree.getForeground());
        this.setBackground(tree.getBackground());
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        if (node.getUserObject() instanceof ArbilNode) {
            ArbilNode arbilNode = (ArbilNode) node.getUserObject();
            // create the object with parameters so the jvm has a chance to reused objects in memory
//            returnComponent = new JLabel(arbilNode.toString(), arbilNode.getIcon(), JLabel.LEFT);
            returnComponent.setText(arbilNode.toString());
            returnComponent.setIcon(arbilNode.getIcon());
            returnComponent.setHorizontalAlignment(JLabel.LEFT);
            if (arbilNode instanceof ArbilDataNode) {
                if (((ArbilDataNode) arbilNode).isContainerNode()) {
                    returnComponent.setForeground(Color.DARK_GRAY);
                    ((JLabel) returnComponent).setText("<html><u>" + arbilNode.toString() + "</u></html>");
                }
                if (/* !sel && */((ArbilDataNode) arbilNode).hasSchemaError) {
                    returnComponent.setForeground(Color.RED);
                }
                if (/* !sel && */((ArbilDataNode) arbilNode).getNeedsSaveToDisk(true)) {
                    returnComponent.setForeground(Color.BLUE);
                }
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
        } else {
            returnComponent.setOpaque(false);
        }
        return returnComponent;
    }
}
