/**
 * Copyright (C) 2014 The Language Archive, Max Planck Institute for Psycholinguistics
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
package nl.mpi.arbil.ui.wizard;

import java.util.HashMap;
import java.util.ResourceBundle;

/**
 * Model for the ArbilWizard. Hold the wizard's contents and keeps track of the wizard state
 * @see ArbilWizard
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilWizardModel {
    private static final ResourceBundle widgets = ResourceBundle.getBundle("nl/mpi/arbil/localisation/Widgets");

    private Object currentId = null;
    private final HashMap<Object, ArbilWizardContent> contents = new HashMap<Object, ArbilWizardContent>();

    public void addContent(Object id, ArbilWizardContent content) {
	contents.put(id, content);
    }

    public void setCurrent(Object id) {
	currentId = id;
    }

    public Object getCurrentId() {
	return currentId;
    }

    public ArbilWizardContent getCurrent() {
	return contents.get(currentId);
    }

    public ArbilWizardContent next() {
	final ArbilWizardContent current = getCurrent();
	if (current != null) {
	    currentId = current.getNext();
	    return getCurrent();
	} else {
	    return null;
	}
    }

    public ArbilWizardContent previous() {
	final ArbilWizardContent current = getCurrent();
	if (current != null) {
	    currentId = current.getPrevious();
	    return getCurrent();
	} else {
	    return null;
	}
    }
}
