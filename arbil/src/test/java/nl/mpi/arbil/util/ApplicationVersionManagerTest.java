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
package nl.mpi.arbil.util;

import org.junit.Test;
import static org.junit.Assert.*;

import static nl.mpi.arbil.util.ApplicationVersionManager.compareVersions;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ApplicationVersionManagerTest {

    /**
     * Test of versionGreaterThan method, of class ApplicationVersionManager.
     */
    @Test
    public void testCompareVersions() {
        assertTrue("2.6.3 >= 1.0.0", compareVersions(
                2, 6, 3,
                1, 0, 0));
        assertTrue("2.6.3 >= 2.0.0", compareVersions(
                2, 6, 3,
                2, 0, 0));
        assertTrue("2.6.3 >= 2.6.0", compareVersions(
                2, 6, 3,
                2, 6, 0));
        assertTrue("2.6.3 >= 2.6.0", compareVersions(
                2, 6, 3,
                2, 6, 0));
        assertTrue("2.6.3 >= 2.6.3", compareVersions(
                2, 6, 3,
                2, 6, 3));
        assertFalse("2.6.3 < 2.6.4", compareVersions(
                2, 6, 3,
                2, 6, 4));
        assertFalse("2.6.3 < 2.7.0", compareVersions(
                2, 6, 3,
                2, 7, 0));
        assertFalse("2.6.3 < 2.7.4", compareVersions(
                2, 6, 3,
                2, 7, 4));
        assertFalse("2.6.3 < 3.0.0", compareVersions(
                2, 6, 3,
                3, 0, 0));
        assertFalse("2.6.3 < 3.6.3", compareVersions(
                2, 6, 3,
                3, 6, 3));
    }

}
