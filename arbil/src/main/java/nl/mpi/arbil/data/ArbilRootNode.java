/**
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.arbil.data;

import java.net.URI;
import java.util.List;
import java.util.Vector;
import javax.swing.ImageIcon;
import nl.mpi.flap.model.FieldGroup;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class ArbilRootNode extends ArbilNode {

    final private String name;
    final private ImageIcon icon;
    final private boolean local;
    final private URI nodeUri;

    protected ArbilRootNode(URI nodeUri, String name, ImageIcon icon, boolean local) {
        this.nodeUri = nodeUri;
        this.name = name;
        this.icon = icon;
        this.local = local;
    }

    @Override
    public String toString() {
        return name;
    }

    public ImageIcon getIcon() {
        return icon;
    }

    public ArbilDataNode[] getAllChildren() {
        throw new UnsupportedOperationException();
    }

    public void getAllChildren(Vector<ArbilDataNode> allChildren) {
        throw new UnsupportedOperationException();
    }

    public int getChildCount() {
        return getChildArray().length;
    }

    @Override
    public void setID(String id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getID() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setLabel(String label) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getLabel() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setFieldGroups(List<FieldGroup> fieldGroups) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<FieldGroup> getFieldGroups() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setChildIds(List<String> idString) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<String> getChildIds() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean hasCatalogue() {
        return false;
    }

    public boolean hasHistory() {
        return false;
    }

    public boolean hasLocalResource() {
        return false;
    }

    public boolean hasResource() {
        return false;
    }

    public boolean isArchivableFile() {
        return false;
    }

    public boolean isCatalogue() {
        return false;
    }

    public boolean isChildNode() {
        return false;
    }

    public boolean isCmdiMetaDataNode() {
        return false;
    }

    public boolean isCorpus() {
        return false;
    }

    public boolean isDirectory() {
        return false;
    }

    public boolean isEditable() {
        return false;
    }

    public boolean isEmptyMetaNode() {
        return false;
    }

    public boolean isFavorite() {
        return false;
    }

    public boolean isLocal() {
        return this.local;
    }

    public boolean isMetaDataNode() {
        return false;
    }

    public boolean isResourceSet() {
        return false;
    }

    public boolean isSession() {
        return false;
    }

    public boolean isLoading() {
        return false;
    }

    public boolean isDataLoaded() {
        return true;
    }

    @Override
    public boolean isDataPartiallyLoaded() {
        return true;
    }
//    @Override
//    public List<FieldGroup> getFieldGroups() {
//        return Collections.emptyList();
//    }
}
