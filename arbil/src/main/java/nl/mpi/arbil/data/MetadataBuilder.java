package nl.mpi.arbil.data;

import java.io.File;
import java.net.URI;
import nl.mpi.arbil.ArbilMetadataException;

/**
 *
 * @author Peter Withers
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface MetadataBuilder {

    /**
     * Add a new node based on a template and optionally attach a resource
     *
     * @return String path to the added node
     */
    URI addChildNode(ArbilDataNode destinationNode, String nodeType, String targetXmlPath, URI resourceUri, String mimeType) throws ArbilMetadataException;

    URI addFromTemplate(File destinationFile, String templateType);

    void addNode(final ArbilDataNode destinationNode, final String nodeTypeDisplayNameLocal, final ArbilDataNode addableNode) throws ArbilMetadataException;

    /**
     * Checks whether the destinationNode in its current state supports adding a node of the specified type
     *
     * @param destinationNode Proposed destination node
     * @param nodeType Full type name of the node to add
     * @return Whether the node can be added
     */
    boolean canAddChildNode(final ArbilDataNode destinationNode, final String nodeType);

    /**
     * Requests to add a new node of given type to given destination node
     *
     * @param destinationNode Node to add new node to
     * @param nodeType Name of node type
     * @param nodeTypeDisplayName Name to display as node type
     */
    void requestAddNode(final ArbilDataNode destinationNode, final String nodeType, final String nodeTypeDisplayName);

    /**
     * Requests to add a node on basis of a given existing node to the given destination node
     *
     * @param destinationNode Node to add new node to
     * @param nodeType Name of node type
     * @param addableNode Node to base new node on
     */
    void requestAddNode(final ArbilDataNode destinationNode, final String nodeTypeDisplayNameLocal, final ArbilDataNode addableNode);

    /**
     * Requests to add a node on basis of a given existing node to the local corpus
     *
     * @param nodeType Name of node type
     * @param addableNode Node to base new node on
     */
    void requestAddRootNode(final ArbilDataNode addableNode, final String nodeTypeDisplayNameLocal);
    
}
