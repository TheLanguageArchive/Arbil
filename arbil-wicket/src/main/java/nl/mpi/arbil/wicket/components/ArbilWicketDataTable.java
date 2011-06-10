package nl.mpi.arbil.wicket.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.data.ArbilTableCell;
import nl.mpi.arbil.wicket.model.ArbilWicketTableModel;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilWicketDataTable extends DefaultDataTable<ArrayList<ArbilTableCell>> {

    public ArbilWicketDataTable(String id, final ArbilWicketTableModel model) {
	super(id, createColumns(model), model, model.getRowCount());

	add(new AttributeAppender("class", true, new Model<String>() {

	    @Override
	    public String getObject() {
		return model.isHorizontalView()?"horizontal":"non-horizontal";
	    }
	}, " "));
    }

    private static IColumn<ArrayList<ArbilTableCell>>[] createColumns(ArbilWicketTableModel model) {
	IColumn<?>[] columns = new IColumn<?>[model.getColumnCount()];

	// Actual data columns
	for (int i = 0; i < model.getColumnCount(); i++) {
	    final int column = i;
	    columns[i] = new AbstractColumn<ArrayList<ArbilTableCell>>(new Model<String>(model.getColumnName(column))) {

		public void populateItem(Item<ICellPopulator<ArrayList<ArbilTableCell>>> cellItem, String componentId, IModel<ArrayList<ArbilTableCell>> rowModel) {
		    ArbilTableCell cell = rowModel.getObject().get(column);
		    cellItem.add(new Label(componentId, getText(cell)));
		}
	    };
	}

	return (IColumn<ArrayList<ArbilTableCell>>[]) columns;
    }

    // CODE FOR SHOWING ICON
    //	IColumn<ArbilDataNode> iconColumn = new AbstractColumn<ArbilDataNode>(new Model<String>("Icon")) {
    //
    //	    public void populateItem(Item<ICellPopulator<ArbilDataNode>> cellItem, String componentId, IModel<ArbilDataNode> rowModel) {
    //		cellItem.add(new NodeIcon(componentId, rowModel.getObject().getIcon().getImage()));
    //	    }
    //	};
    private static String getText(ArbilTableCell cell) {
	Object cellObject = cell.getContent();
	if (cellObject instanceof ArbilDataNode) {
	    return (((ArbilDataNode) cellObject).toString());
	} else if (cellObject instanceof ArbilDataNode[]) {
	    String cellText = "";
	    Arrays.sort((ArbilDataNode[]) cellObject, new Comparator() {

		public int compare(Object o1, Object o2) {
		    String value1 = o1.toString();
		    String value2 = o2.toString();
		    return value1.compareToIgnoreCase(value2);
		}
	    });
	    boolean hasAddedValues = false;
	    for (ArbilDataNode currentArbilDataNode : (ArbilDataNode[]) cellObject) {
		cellText = cellText + "[" + currentArbilDataNode.toString() + "],";
		hasAddedValues = true;
	    }
	    if (hasAddedValues) {
		cellText = cellText.substring(0, cellText.length() - 1);
	    }
	    return (cellText);
	} else if (cellObject instanceof ArbilField[]) {
	    return "<multiple values>";
	} else if (cellObject instanceof ArbilField && ((ArbilField) cellObject).isRequiredField() && ((ArbilField) cellObject).toString().length() == 0) {
	    //super.setForeground(Color.RED);
	    return "<required field>";
	} else if (cellObject instanceof ArbilField && !((ArbilField) cellObject).fieldValueValidates()) {
	    //super.setForeground(Color.RED);
	}
	if (cellObject != null) {
	    return cellObject.toString();
	} else {
	    return "";
	}
    }
}
