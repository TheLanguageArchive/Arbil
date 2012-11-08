/**
 * Copyright (C) ${year} Max Planck Institute for Psycholinguistics
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
package nl.mpi.arbilcommons;

import nl.mpi.arbil.plugin.PluginException;
import nl.mpi.kinnate.plugin.AbstractBasePlugin;

/**
 * Document : ArbilCommons Created on : Nov 08, 2012, 16:06 PM
 *
 * @author Peter Withers
 */
public class ArbilCommons extends AbstractBasePlugin {

    public ArbilCommons() throws PluginException {
        super("ArbilCommons", "Exports CSV files.", "nl.mpi.arbilcommons");
    }
}
