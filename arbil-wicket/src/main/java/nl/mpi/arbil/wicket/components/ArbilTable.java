package nl.mpi.arbil.wicket.components;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import nl.mpi.arbil.data.ArbilDataNode;
import org.apache.wicket.Resource;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.resource.DynamicImageResource;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilTable extends DataTable<ArbilDataNode> {

    public ArbilTable(String id, IDataProvider<ArbilDataNode> dataNodes) {
	super(id, createColumns(), dataNodes, 100);
    }

    private static IColumn<ArbilDataNode>[] createColumns() {
	IColumn<ArbilDataNode> iconColumn = new AbstractColumn<ArbilDataNode>(new Model<String>("Icon")) {

	    public void populateItem(Item<ICellPopulator<ArbilDataNode>> cellItem, String componentId, final IModel<ArbilDataNode> rowModel) {
		Resource iconResource = new DynamicImageResource() {

		    @Override
		    protected byte[] getImageData() {
			java.awt.Image image = rowModel.getObject().getIcon().getImage();

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
		cellItem.add(new NodeIcon(componentId, iconResource));
	    }
	};

	IColumn<ArbilDataNode> nodeNameColumn = new AbstractColumn<ArbilDataNode>(new Model<String>("Data node")) {

	    public void populateItem(Item<ICellPopulator<ArbilDataNode>> cellItem, String componentId, IModel<ArbilDataNode> rowModel) {
		cellItem.add(new Label(componentId, rowModel.getObject().toString()));
	    }
	};

	return (IColumn<ArbilDataNode>[]) new IColumn<?>[]{iconColumn, nodeNameColumn};
    }
}
