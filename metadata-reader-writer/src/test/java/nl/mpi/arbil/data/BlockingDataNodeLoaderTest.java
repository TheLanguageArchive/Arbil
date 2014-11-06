/*
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package nl.mpi.arbil.data;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import nl.mpi.flap.model.ModelException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public class BlockingDataNodeLoaderTest {

    /**
     * Test of problematic URLs which have 303 redirects
     */
    @Test
    @Ignore("Dependent on existing handle, needs to be changed")
    public void testProblematicUrls() throws URISyntaxException, ModelException, IOException {

        // https://trac.mpi.nl/ticket/4143
//        https://lux16.mpi.nl/cmdi_test/Root/Metadata/aCollection.cmdi
//        hdl:11142/00-BAB83179-F270-4477-88FE-DFD10EA68CCA
        System.out.println("getBlockingDataNodeLoader");
        final String cacheDirectory = File.createTempFile("arbil-unit-test", "-tmp").getParent();
        final BlockingDataNodeLoader blockingDataNodeLoader = BlockingDataNodeLoader.getBlockingDataNodeLoader(cacheDirectory);
//        ArbilDataNode dataNode = (ArbilDataNode) blockingDataNodeLoader.getPluginArbilDataNode(null, new URI("http://hdl.handle.net/11142/00-74BB450B-4E5E-4EC7-B043-F444C62DB5C0"));
//        ArbilDataNode dataNode = (ArbilDataNode) blockingDataNodeLoader.getPluginArbilDataNode(null, new URI("hdl:11142/00-50AD419C-056C-4A9B-AB38-08BDAC854236"));
        ArbilDataNode dataNode = (ArbilDataNode) blockingDataNodeLoader.getPluginArbilDataNode(null, new URI("hdl:11142/00-BAB83179-F270-4477-88FE-DFD10EA68CCA"));
//        ArbilDataNode dataNode = (ArbilDataNode) blockingDataNodeLoader.getPluginArbilDataNode(null, new URI("hdl:11142/00-F776D856-F005-4C41-9E8F-C16056624E34"));
//        ArbilDataNode dataNode = (ArbilDataNode) blockingDataNodeLoader.getPluginArbilDataNode(null, new URI("http://hdl.handle.net/11142/00-F776D856-F005-4C41-9E8F-C16056624E34"));
//        if (dataNode.getLoadingState() != ArbilDataNode.LoadingState.LOADED && dataNode.isMetaDataNode()) {
//            dataNode.loadArbilDom();
//            dataNode.loadFullArbilDom();// todo: this has changed undo if can
//        }
        System.out.println("getLabel:" + dataNode.getLabel());
        System.out.println("getChildCount:" + dataNode.getChildCount());
        System.out.println("links:" + dataNode.getChildLinks().size());
        System.out.println("field groups:" + dataNode.getFieldGroups().size());
        assertEquals("aCollection", dataNode.getLabel());

//        assertEquals(5, dataNode.getChildLinks().get(0));
//        assertEquals(5, dataNode.getChildIds().get(0).getNodeUriString());
//        assertEquals(5, dataNode.getAllChildren()[0].getURI());
//        assertEquals(5, dataNode.getChildArray()[0].getURI());
//        assertEquals(5, dataNode.getChildArray()[0].getURI());
        assertEquals(5, dataNode.getChildCount());
        assertEquals(5, dataNode.getChildLinks().size());
        assertEquals(1, dataNode.getFieldGroups().size());
    }

}
