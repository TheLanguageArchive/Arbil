package nl.mpi.arbil.wicket.components;

import java.util.Iterator;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilDataNodeContainer;
import nl.mpi.arbil.wicket.model.DataNodeDataProvider;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.data.IDataProvider;
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
public class ArbilWicketTablePanel extends Panel implements ArbilDataNodeContainer {

    private transient IPushEventHandler handler;
    private transient IPushService pushService;
    private transient IPushNode<ArbilDataNode> pushNode;
    private ArbilWicketDataTable table;

    public ArbilWicketTablePanel(String id, IDataProvider<ArbilDataNode> dataNodes) {
	super(id);
	table = new ArbilWicketDataTable("datatable", dataNodes);
	add(table);
	table.setOutputMarkupId(true);

	/**
	 * Push handler
	 */
	handler = new AbstractPushEventHandler<ArbilDataNode>() {

	    /**
	     * DataNode event (could be made more specific)
	     */
	    public void onEvent(AjaxRequestTarget target, ArbilDataNode event, IPushNode node, IPushEventContext ctx) {
		target.addComponent(table);
	    }
	};

	pushService = TimerPushService.get();
	pushNode = pushService.installNode(this, handler);
	for (Iterator<? extends ArbilDataNode> it = dataNodes.iterator(0, dataNodes.size()); it.hasNext();) {
	    it.next().registerContainer(this);
	}
    }

    public void dataNodeRemoved(ArbilDataNode dataNode) {
	if (pushService.isConnected(pushNode)) {
	    pushService.publish(pushNode, dataNode);
	} else {
	    dataNode.removeContainer(this);
	}
    }

    public void dataNodeIconCleared(ArbilDataNode dataNode) {
	if (pushService.isConnected(pushNode)) {
	    pushService.publish(pushNode, dataNode);
	} else {
	    dataNode.removeContainer(this);
	}
    }
}
