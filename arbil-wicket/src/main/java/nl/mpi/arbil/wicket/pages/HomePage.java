package nl.mpi.arbil.wicket.pages;

import nl.mpi.arbil.wicket.model.DataNodeDataProvider;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeLoader;
import nl.mpi.arbil.data.TreeHelper;
import nl.mpi.arbil.wicket.components.NodeIcon;
import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.resource.DynamicImageResource;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

/**
 * Homepage
 */
public class HomePage extends WebPage {

    private static final long serialVersionUID = 1L;

    // TODO Add any page properties or variables here
    /**
     * Constructor that is invoked when page is invoked without a session.
     * 
     */
    public HomePage(final PageParameters parameters) {
	this(TreeHelper.getSingleInstance().localCorpusNodes);
    }

    public HomePage(ArbilDataNode[] dataNodes) {
	add(createTable("datatable", dataNodes));
    }

    private Component createTable(String name, ArbilDataNode[] dataNodes) {
	IColumn<?>[] columns = new IColumn<?>[]{
	    new AbstractColumn<ArbilDataNode>(new Model<String>("Icon")) {

		public void populateItem(Item<ICellPopulator<ArbilDataNode>> cellItem, String componentId, final IModel<ArbilDataNode> rowModel) {
		    cellItem.add(new NodeIcon(componentId, new DynamicImageResource() {

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
		    }));
		}
	    },
	    new AbstractColumn<ArbilDataNode>(new Model<String>("Data node")) {

		public void populateItem(Item<ICellPopulator<ArbilDataNode>> cellItem, String componentId, IModel<ArbilDataNode> rowModel) {
		    cellItem.add(new Label(componentId, rowModel.getObject().toString()));
		}
	    }
	};

	DataTable<ArbilDataNode> table = new DataTable(name, columns, new DataNodeDataProvider(dataNodes), 100);
	return table;
    }
}
