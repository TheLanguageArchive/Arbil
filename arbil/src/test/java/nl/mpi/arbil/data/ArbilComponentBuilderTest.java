package nl.mpi.arbil.data;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import nl.mpi.arbil.ArbilTest;
import nl.mpi.arbil.MockSessionStorage;
import nl.mpi.arbil.userstorage.SessionStorage;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilComponentBuilderTest extends ArbilTest {

    ArbilComponentBuilder componentBuilder;
    boolean firstrun = true;

    @Before
    public synchronized void setUp() throws Exception {
	if (firstrun) {
	    inject();
	    componentBuilder = new ArbilComponentBuilder();
	    firstrun = false;
	}
    }

    @Override
    public void cleanUp() {
    }

    private ArbilDataNode getMdChildNode(ArbilDataNode node) {
	ArbilDataNode childNode = node.getChildByPath(".CMD.Components.Example_Profile_Instance.example-component-actor.ActorLanguage");
	childNode.waitTillLoaded();
	return childNode;
    }

    private ArbilDataNode getMdInstanceNode() throws InterruptedException, IOException, URISyntaxException {
	addToLocalTreeFromURI(copyOfResource(uriFromResource("/nl/mpi/arbil/data/example-md-instance.cmdi")));
	ArbilDataNode node = getTreeHelper().getLocalCorpusNodes()[0];
	assertNotNull(node);
	assertTrue(node.isCmdiMetaDataNode());
	assertTrue(node.isLocal());
	assertTrue(node.isEditable());
	return node;
    }

    private void addResourceProxyToNode(ArbilDataNode node, ArbilDataNode resourceNode) {
	assertNotNull(componentBuilder.insertResourceProxy(node, resourceNode));
	node.reloadNode();
	waitForNodeToLoad(node);
    }

    private void removeResourceProxyFromNode(ArbilDataNode node, ArbilDataNode resourceNode) {
	assertTrue(componentBuilder.removeResourceProxyReferences(node, Collections.singleton(resourceNode.getUrlString())));
	node.reloadNode();
	waitForNodeToLoad(node);
    }

    @Test
    public void testInsertResourceProxies() throws Exception {
	ArbilDataNode node = getMdInstanceNode();
	ArbilDataNode childNode = getMdChildNode(node);

	int resourceLinks = node.getCmdiComponentLinkReader().cmdiResourceLinkArray.size();

	// Add new resource to file
	ArbilDataNode resourceNode = getDataNodeLoader().getArbilDataNodeWithoutLoading(uriFromResource("/nl/mpi/arbil/data/resources/arbil.jpg"));
	addResourceProxyToNode(node, resourceNode);
	// A resource link should have been added
	assertEquals(resourceLinks + 1, node.getCmdiComponentLinkReader().cmdiResourceLinkArray.size());

	// Add existing resource to child node
	addResourceProxyToNode(childNode, resourceNode);
	// No resource link should have been added, only a reference
	assertEquals(resourceLinks + 1, node.getCmdiComponentLinkReader().cmdiResourceLinkArray.size());
    }

    @Test
    public void testRemoveResourceProxies() throws Exception {
	ArbilDataNode node = getMdInstanceNode();
	ArbilDataNode childNode = getMdChildNode(node);

	int resourceLinks = node.getCmdiComponentLinkReader().cmdiResourceLinkArray.size();

	// Add new resource to file
	ArbilDataNode resourceNode = getDataNodeLoader().getArbilDataNodeWithoutLoading(uriFromResource("/nl/mpi/arbil/data/resources/arbil.jpg"));
	addResourceProxyToNode(node, resourceNode);
	// Add existing resource to child node
	addResourceProxyToNode(childNode, resourceNode);
	// One resource link should have been added
	assertEquals(resourceLinks + 1, node.getCmdiComponentLinkReader().cmdiResourceLinkArray.size());

	// Remove one link ref
	removeResourceProxyFromNode(node, resourceNode);
	// Proxy should still be there
	assertEquals(resourceLinks + 1, node.getCmdiComponentLinkReader().cmdiResourceLinkArray.size());
	
	// TODO: not implemented yet
//	removeResourceProxyFromNode(childNode, resourceNode);
//	assertEquals(resourceLinks, node.getCmdiComponentLinkReader().cmdiResourceLinkArray.size());
    }

    @Override
    protected SessionStorage newSessionStorage() {
	return new MockSessionStorage() {

	    @Override
	    public boolean pathIsInsideCache(File fullTestFile) {
		return true;
	    }
	};
    }
}
