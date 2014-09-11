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
import nl.mpi.arbil.ArbilMetadataException;
import nl.mpi.arbil.data.ArbilDataNode.LoadingState;
import nl.mpi.arbil.util.PathUtility;
import nl.mpi.arbil.util.TableManager;

/**
 * @since Jul 15, 2014 11:35:33 AM (creation date)
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public interface DataNodeService {

    boolean addCorpusLink(ArbilDataNode dataNode, ArbilDataNode targetNode);

    void addField(ArbilDataNode dataNode, ArbilField fieldToAdd);

    void bumpHistory(File dataNodeFile) throws IOException;

    void copyLastHistoryToCurrent(ArbilDataNode dataNode);

    void deleteCorpusLink(ArbilDataNode dataNode, ArbilDataNode[] targetImdiNodes);

    /**
     * Retrieves the direct ancestor of the specified child node
     *
     * @param node child node to find direct ancestor for
     * @return the direct ancestor of the specified node
     */
    ArbilDataNode getParentOfNode(ArbilDataNode node);

    boolean isEditable(ArbilDataNode dataNode);

    boolean isFavorite(ArbilDataNode dataNode);

    ArbilDataNode loadArbilDataNode(Object registeringObject, URI localUri);

    void loadArbilDom(ArbilDataNode dataNode);

    /**
     * Sets requested loading state to {@link LoadingState#LOADED} and performs
     * a {@link #loadArbilDom() }
     */
    void loadFullArbilDom(ArbilDataNode dataNode);

    void reloadNode(ArbilDataNode dataNode);

    void reloadNodeShallowly(ArbilDataNode dataNode);

    boolean resurrectHistory(ArbilDataNode dataNode, String historyVersion);

    /**
     * Saves the current changes from memory into a new imdi file on disk.
     * Previous imdi files are renamed and kept as a history. the caller is
     * responsible for reloading the node if that is required
     */
    void saveChangesToCache(ArbilDataNode datanode);

    void setDataNodeNeedsSaveToDisk(ArbilDataNode dataNode, ArbilField originatingField, boolean updateUI);

    // TODO: this is not used yet but may be required for unicode paths
    String urlEncodePath(String inputPath);

    // todo: this should not be in the no UI project
    void pasteIntoNode(final PathUtility pathUtility, final TableManager tableManager, ArbilDataNode dataNode);

    void insertResourceLocation(final PathUtility pathUtility, final TableManager tableManager, ArbilDataNode dataNode, URI location) throws ArbilMetadataException;
}
