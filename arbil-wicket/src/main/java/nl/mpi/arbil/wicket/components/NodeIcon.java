package nl.mpi.arbil.wicket.components;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import org.apache.wicket.Resource;
import java.awt.Image;
import org.apache.wicket.markup.html.image.resource.DynamicImageResource;
import org.apache.wicket.markup.html.panel.Panel;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class NodeIcon extends Panel {

    public NodeIcon(String id, Image iconImage) {
	this(id, new IconImageResource(iconImage));
    }

    public NodeIcon(String id, Resource iconResource) {
	super(id);
	add(new org.apache.wicket.markup.html.image.Image("nodeIcon", iconResource));
    }

    private static class IconImageResource extends DynamicImageResource {

	private byte[] data;

	public IconImageResource(java.awt.Image iconImage) {
	    BufferedImage buffImage =
		    new BufferedImage(
		    iconImage.getWidth(null),
		    iconImage.getHeight(null),
		    BufferedImage.TYPE_INT_ARGB);

	    // Draw Image into BufferedImage
	    Graphics g = buffImage.getGraphics();
	    try {
		g.drawImage(iconImage, 0, 0, null);
		data = toImageData(buffImage);
	    } finally {
		g.dispose();
	    }
	}

	@Override
	protected byte[] getImageData() {
	    return data;
	}
    };
}
