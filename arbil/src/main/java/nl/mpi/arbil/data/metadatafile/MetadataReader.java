/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.arbil.data.metadatafile;

import java.net.URI;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;
import nl.mpi.arbil.data.ArbilDataNode;
import org.w3c.dom.Node;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface MetadataReader {

    /**
     * Add all unreferenced resources in a document to the parent node
     *
     * @param parentNode Parent node, to which resources will be added
     * @param parentChildTree Parent-child tree that is constructed
     * @param childLinks Child links collection that is constructed
     */
    void addUnreferencedResources(ArbilDataNode parentNode, Hashtable<ArbilDataNode, HashSet<ArbilDataNode>> parentChildTree, Vector<String[]> childLinks);

    URI correctLinkPath(URI parentPath, String linkString);

    /**
     * loop all nodes;
     * each end node becomes a field;
     * any node that passes pathIsChildNode becomes a subnode in a node named by the result string of pathIsChildNode;
     * the id of the node that passes pathIsChildNode is stored in the subnode to allow for deletion from the dom if needed
     *
     * @param parentNode
     * @param childLinks
     * @param startNode
     * @param nodePath
     * @param fullNodePath
     * @param parentChildTree
     * @param siblingNodePathCounter
     * @param nodeOrderCounter
     * @return
     */
    int iterateChildNodes(ArbilDataNode parentNode, Vector<String[]> childLinks, Node startNode, final String nodePath, String fullNodePath, Hashtable<ArbilDataNode, HashSet<ArbilDataNode>> parentChildTree, Hashtable<String, Integer> siblingNodePathCounter, int nodeOrderCounter);
    
}
