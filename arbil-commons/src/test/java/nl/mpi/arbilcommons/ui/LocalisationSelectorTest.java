/*
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
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
package nl.mpi.arbilcommons.ui;

import java.io.File;
import javax.swing.Icon;
import javax.swing.JFrame;
import nl.mpi.flap.plugin.PluginSessionStorage;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public class LocalisationSelectorTest {

    public LocalisationSelectorTest() {
    }

    /**
     * Test of askUser method, of class LocalisationSelector. This is an
     * integration test which includes GUI interaction so it should not be run
     * in automated tests.
     */
    @Ignore
    @Test
    public void testAskUser() {
        System.out.println("askUser");
        JFrame jFrame = null;
        Icon icon = null;
        LocalisationSelector instance = new LocalisationSelector(new PluginSessionStorage() {
            String savedValue = null;

            public File getApplicationSettingsDirectory() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public File getProjectDirectory() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public File getProjectWorkingDirectory() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public String loadString(String filename) {
                return savedValue;
            }

            @Override
            public void saveString(String filename, String storableValue) {
                savedValue = storableValue;
            }
        }, new String[]{"en", "pt", "fr", "ee", "it", "ko", "zh"});
        instance.askUser(jFrame, icon);
        instance.askUser(jFrame, icon);
        instance.askUser(jFrame, icon);
        instance.askUser(jFrame, icon);
        instance.askUser(jFrame, icon);
        instance.askUser(jFrame, icon);
    }
}