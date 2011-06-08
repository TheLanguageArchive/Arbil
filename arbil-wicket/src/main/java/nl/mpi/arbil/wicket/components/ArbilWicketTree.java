package nl.mpi.arbil.wicket.components;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.wicket.model.ArbilWicketTreeModel;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Resource;
import org.apache.wicket.extensions.markup.html.tree.Tree;
import org.apache.wicket.markup.html.image.resource.DynamicImageResource;
import org.apache.wicket.markup.html.tree.DefaultTreeState;
import org.apache.wicket.markup.html.tree.ITreeState;
import org.apache.wicket.model.IModel;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilWicketTree extends Tree {

    public ArbilWicketTree(String id, IModel<TreeModel> model) {
	super(id, model);
    }
    private ITreeState state;

    @Override
    protected ITreeState newTreeState() {
	return new DefaultTreeState() {
	};
    }

    /**
     * Returns the TreeState of this tree.
     * 
     * @return Tree state instance
     */
    @Override
    public ITreeState getTreeState() {
	if (state == null) {
	    state = newTreeState();

	    // add this object as listener of the state
	    state.addTreeStateListener(this);
	    // FIXME: Where should we remove the listener?
	    if (getModel().getObject() instanceof ArbilWicketTreeModel) {
		state.addTreeStateListener((ArbilWicketTreeModel) this.getModel().getObject());
	    }
	}
	return state;
    }

    @Override
    protected Component newNodeIcon(MarkupContainer parent, String id, final TreeNode node) {

	if (((DefaultMutableTreeNode) node).getUserObject() instanceof ArbilDataNode) {
	    Resource iconResource = new DynamicImageResource() {

		@Override
		protected byte[] getImageData() {
		    Object object = ((DefaultMutableTreeNode) node).getUserObject();

		    java.awt.Image image = ((ArbilDataNode) object).getIcon().getImage();


		    // Create empty BufferedImage, sized to Image
		    BufferedImage buffImage =
			    new BufferedImage(
			    image.getWidth(null),
			    image.getHeight(null),
			    BufferedImage.TYPE_INT_ARGB);

		    // Draw Image into BufferedImage
		    Graphics g = buffImage.getGraphics();
		    g.drawImage(image, 0, 0, null);
		    return toImageData(buffImage);
		}
	    };
	    return new NodeIcon(id, iconResource);
	    //cellItem.add(new NodeIcon(componentId, iconResource));
	} else {
	    return super.newNodeIcon(parent, id, node);
	}
    }
}
