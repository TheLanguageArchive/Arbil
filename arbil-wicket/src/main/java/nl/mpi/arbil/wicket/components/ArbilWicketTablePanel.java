package nl.mpi.arbil.wicket.components;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import nl.mpi.arbil.ui.AbstractArbilTableModel;
import nl.mpi.arbil.wicket.model.ArbilWicketTableModel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.wicketstuff.push.AbstractPushEventHandler;
import org.wicketstuff.push.IPushEventContext;
import org.wicketstuff.push.IPushEventHandler;
import org.wicketstuff.push.IPushNode;
import org.wicketstuff.push.IPushService;
import org.wicketstuff.push.timer.TimerPushService;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilWicketTablePanel extends Panel implements TableModelListener {

    private transient IPushEventHandler handler;
    private transient IPushService pushService;
    private transient IPushNode<AbstractArbilTableModel> pushNode;
    private ArbilWicketDataTable table;
    private ArbilWicketTableModel tableModel;

    public ArbilWicketTablePanel(String id, ArbilWicketTableModel model) {
	super(id);
	this.tableModel = model;
	model.addTableModelListener(this);

	table = new ArbilWicketDataTable("datatable", model);
	add(table);
	table.setOutputMarkupId(true);

	/**
	 * Push handler
	 */
	handler = new AbstractPushEventHandler<AbstractArbilTableModel>() {

	    /**
	     * DataNode event (could be made more specific)
	     */
	    public void onEvent(AjaxRequestTarget target, AbstractArbilTableModel event, IPushNode node, IPushEventContext ctx) {
		if (target != null) {
		    target.addComponent(table);
		}
	    }
	};

	pushService = TimerPushService.get();
	pushNode = pushService.installNode(this, handler);
    }

    public void tableChanged(TableModelEvent e) {
	if (pushService.isConnected(pushNode)) {
	    pushService.publish(pushNode, tableModel);
	} else {
	    tableModel.removeTableModelListener(this);
	}
    }
}
