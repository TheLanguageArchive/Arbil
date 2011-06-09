package nl.mpi.arbil.wicket.components;

import nl.mpi.arbil.data.ArbilDataNode;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilWicketDataTable extends DataTable<ArbilDataNode> {

    public ArbilWicketDataTable(String id, IDataProvider<ArbilDataNode> dataNodes) {
	super(id, createColumns(), dataNodes, 100);
    }

    private static IColumn<ArbilDataNode>[] createColumns() {
	IColumn<ArbilDataNode> iconColumn = new AbstractColumn<ArbilDataNode>(new Model<String>("Icon")) {

	    public void populateItem(Item<ICellPopulator<ArbilDataNode>> cellItem, String componentId, IModel<ArbilDataNode> rowModel) {
		cellItem.add(new NodeIcon(componentId, rowModel.getObject().getIcon().getImage()));
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
