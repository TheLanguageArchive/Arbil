/**
 * Copyright (C) 2016 The Language Archive, Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.arbil.data;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import nl.mpi.arbil.ArbilTest;
import nl.mpi.arbil.MockSessionStorage;
import nl.mpi.arbil.userstorage.SessionStorage;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilComponentBuilderTest extends ArbilTest {

    private final static Logger logger = LoggerFactory.getLogger(ArbilComponentBuilderTest.class);
    private ArbilComponentBuilder componentBuilder;

    @Before
    public synchronized void setUp() throws Exception {
	inject();
	componentBuilder = new ArbilComponentBuilder();
    }

    @Test
    public void testInsertResourceProxies() throws Exception {
	logger.info("testInsertResourceProxies");
	ArbilDataNode node = getMdInstanceNode();
	ArbilDataNode childNode = getMdChildNode(node);

	int resourceLinks = node.getCmdiComponentLinkReader().cmdiResourceLinkArray.size();

	addResourceToNode(node, childNode, resourceLinks);
    }

    @Test
    public void testRemoveResourceProxies() throws Exception {
	logger.info("testRemoveResourceProxies");
	ArbilDataNode node = getMdInstanceNode();
	ArbilDataNode childNode = getMdChildNode(node);

	int resourceLinks = node.getCmdiComponentLinkReader().cmdiResourceLinkArray.size();
	ArbilDataNode resourceNode = addResourceToNode(node, childNode, resourceLinks);

	// Remove one link ref
	removeResourceProxyFromNode(node, resourceNode);
	// Proxy should still be there
	assertEquals(resourceLinks + 1, node.getCmdiComponentLinkReader().cmdiResourceLinkArray.size());

	// Remove final link ref
	removeResourceProxyFromNode(childNode, resourceNode);
	// Proxy should be removed
	assertEquals(resourceLinks, node.getCmdiComponentLinkReader().cmdiResourceLinkArray.size());
    }

    private ArbilDataNode getMdChildNode(ArbilDataNode node) {
	ArbilDataNode childNode = node.getChildByPath(".CMD.Components.Example_Profile_Instance.example-component-actor.ActorLanguage");
	assertNotNull(childNode);
	childNode.waitTillLoaded();
	return childNode;
    }

    private ArbilDataNode getMdInstanceNode() throws Exception {
	final URI mdFileCopy = copyOfResource(uriFromResource("/nl/mpi/arbil/data/example-md-instance.cmdi"));
	logger.debug("add to tree {}", mdFileCopy);
	addToLocalTreeFromURI(mdFileCopy);
	final ArbilDataNode[] localCorpusNodes = getTreeHelper().getLocalCorpusNodes();
	assertTrue(localCorpusNodes.length > 0);
	final ArbilDataNode node = (ArbilDataNode) localCorpusNodes[0];
	assertNotNull(node);
	assertTrue(node.isCmdiMetaDataNode());
	assertTrue(node.isLocal());
	assertTrue(node.isEditable());
	return node;
    }

    private void addResourceProxyToNode(ArbilDataNode node, ArbilDataNode resourceNode) {
	logger.debug("inserting resource proxy for {} to node {}", resourceNode, node);
	final URI inserted = componentBuilder.insertResourceProxy(node, resourceNode);
	assertNotNull(inserted);
	logger.debug("reload node: {}", node);
	node.reloadNode();
	logger.debug("waiting for node to load: {}", node);
	waitForNodeToLoad(node);
    }

    private void removeResourceProxyFromNode(ArbilDataNode node, ArbilDataNode resourceNode) {
	assertTrue(componentBuilder.removeResourceProxyReferences(node, Collections.singleton(resourceNode.getUrlString())));
	node.reloadNode();
	waitForNodeToLoad(node);
    }

    private ArbilDataNode addResourceToNode(ArbilDataNode node, ArbilDataNode childNode, int resourceLinks) throws URISyntaxException {
	// Add new resource to file
	ArbilDataNode resourceNode = getDataNodeLoader().getArbilDataNodeWithoutLoading(uriFromResource("/nl/mpi/arbil/data/resources/arbil.jpg"));
	addResourceProxyToNode(node, resourceNode);
	// A resource link should have been added
	assertEquals(resourceLinks + 1, node.getCmdiComponentLinkReader().cmdiResourceLinkArray.size());
	// One reference occurences should be present
	assertEquals(1, node.getCmdiComponentLinkReader().getResourceLink(node.getCmdiComponentLinkReader().getProxyId(resourceNode.getUrlString())).getReferencingNodesCount());

	// Add existing resource to child node
	addResourceProxyToNode(childNode, resourceNode);
	// One resource link should have been added
	assertEquals(resourceLinks + 1, node.getCmdiComponentLinkReader().cmdiResourceLinkArray.size());
	// Two reference occurences should be present
	assertEquals(2, node.getCmdiComponentLinkReader().getResourceLink(node.getCmdiComponentLinkReader().getProxyId(resourceNode.getUrlString())).getReferencingNodesCount());
	return resourceNode;
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
