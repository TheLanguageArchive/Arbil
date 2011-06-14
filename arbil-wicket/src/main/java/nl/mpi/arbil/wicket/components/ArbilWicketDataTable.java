package nl.mpi.arbil.wicket.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeTableCell;
import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.data.ArbilTableCell;
import nl.mpi.arbil.data.DefaultArbilTableCell;
import nl.mpi.arbil.ui.ArbilFieldPlaceHolder;
import nl.mpi.arbil.wicket.model.ArbilDataNodeModel;
import nl.mpi.arbil.wicket.model.ArbilWicketTableModel;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.ajax.markup.html.AjaxEditableLabel;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.IConverter;

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
		return model.isHorizontalView() ? "horizontal" : "non-horizontal";
	    }
	}, " "));
    }

    private static boolean isEditable(Object content) {
	if (content instanceof Object[] || content instanceof ArbilField) {
	    return true;
	} else if (content instanceof ArbilField[]) {
	    ArbilDataNode parentObject = ((ArbilField[]) content)[0].getParentDataNode();
	    // check that the field id exists and that the file is in the local cache or in the favourites not loose on a drive, as the determinator of editability
	    return !parentObject.isLoading() && parentObject.isEditable() && parentObject.isMetaDataNode(); // todo: consider limiting editing to files withing the cache only
	} else {
	    return false;
	}
    }

    private static IColumn<ArrayList<ArbilTableCell>>[] createColumns(ArbilWicketTableModel model) {
	IColumn<?>[] columns = new IColumn<?>[model.getColumnCount()];

	// Actual data columns
	for (int i = 0; i < model.getColumnCount(); i++) {
	    final int column = i;
	    columns[i] = new AbstractColumn<ArrayList<ArbilTableCell>>(new Model<String>(model.getColumnName(column))) {

		public void populateItem(Item<ICellPopulator<ArrayList<ArbilTableCell>>> cellItem, String componentId, IModel<ArrayList<ArbilTableCell>> rowModel) {
		    ArbilTableCell cell = rowModel.getObject().get(column);
		    cellItem.add(createComponentForCell(componentId, cell));
		}
	    };
	}

	return (IColumn<ArrayList<ArbilTableCell>>[]) columns;
    }

    private static Component createComponentForCell(String componentId, final ArbilTableCell cell) {
	if (cell instanceof ArbilDataNodeTableCell) {
	    return new NodePanel(componentId, new ArbilDataNodeModel(((ArbilDataNodeTableCell) cell).getContent()));
	} else {
	    //return new Label(componentId, getText(cell));
	    Object content = cell.getContent();

	    if (isEditable(content)) {
		AjaxEditableLabel component = new AjaxEditableLabel(componentId, new Model<ArbilTableCell>(cell)) {

		    @Override
		    public IConverter getConverter(final Class type) {
			return new IConverter() {

			    public Object convertToObject(String value, Locale locale) {
				//throw new UnsupportedOperationException("Not supported yet.");
				return new DefaultArbilTableCell(value);
			    }

			    public String convertToString(Object value, Locale locale) {
				return getText((ArbilTableCell) value);
			    }
			};
		    }
		};
		component.add(new CellValueStyleAppender(cell, true));
		component.setType(ArbilTableCell.class);
		component.setRequired(
			true);


		return component;
	    } else {
		Label component = new Label(componentId, getText(cell));
		component.add(new CellValueStyleAppender(cell, false));
		return component;
	    }
	}
    }

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
	    return "<required field>";
	}
	if (cellObject != null) {
	    return cellObject.toString();
	} else {
	    return "";
	}
    }

    private static class CellValueStyleAppender extends AttributeAppender {

	private CellValueStyleAppender(ArbilTableCell cell, boolean editable) {
	    super("class", new Model<String>(getStyle(cell, editable)), " ");
	}

	private static String getStyle(ArbilTableCell cell, boolean editable) {
	    StringBuilder style = new StringBuilder();

	    if (editable) {
		style.append("editable ");
	    }

	    Object cellObject = cell.getContent();
	    if (cellObject instanceof ArbilField && ((ArbilField) cellObject).isRequiredField() && ((ArbilField) cellObject).toString().length() == 0) {
		style.append("required ");
	    } else if (cellObject instanceof ArbilField && !((ArbilField) cellObject).fieldValueValidates()) {
		style.append("invalid ");
	    }

	    if (cellObject instanceof ArbilFieldPlaceHolder || cellObject instanceof String && "".equals(cellObject)) {
		// Field does not exist in node OR childs column and node has no children of this type
		style.append(" nosuchfield ");
	    } else if (cellObject instanceof ArbilField && ((ArbilField) cellObject).fieldNeedsSaveToDisk()) {
		// Value has changed since last save
		style.append(" needssave ");
	    }

	    return style.toString().trim();
	}
    };
}
