package nl.mpi.arbil.wicket.components;

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
public class NodesPanel extends Panel implements ArbilDataNodeContainer {

    private transient IPushEventHandler handler;
    private transient IPushService pushService;
    private transient IPushNode<ArbilDataNode> pushNode;
    private ArbilTable table;

    public NodesPanel(String id, DataNodeDataProvider dataNodes) {
	super(id);
	table = new ArbilTable("datatable", dataNodes);
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

	for (ArbilDataNode node : dataNodes.getDataNodes()) {
	    node.registerContainer(this);
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
