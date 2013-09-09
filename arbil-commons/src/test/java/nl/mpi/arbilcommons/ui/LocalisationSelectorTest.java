/*
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.Icon;
import javax.swing.JFrame;
import nl.mpi.arbil.userstorage.CommonsSessionStorage;
import org.junit.Test;

/**
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public class LocalisationSelectorTest {

    public LocalisationSelectorTest() {
    }

    /**
     * Test of askUser method, of class LocalisationSelector.
     */
    @Test
    public void testAskUser() {
        System.out.println("askUser");
        JFrame jFrame = null;
        Icon icon = null;
//        final List<Locale> localeList = new ArrayList<Locale>();
//        localeList.add(Locale.UK);
//        localeList.add(Locale.FRENCH);
//        localeList.add(Locale.JAPANESE);
//        localeList.add(Locale.ITALIAN);
//        localeList.add(new Locale("cn", "CHINESE"));
//        localeList.add(new Locale("en", "English"));
//        localeList.add(new Locale("it", "ITALIAN"));
//        localeList.add(new Locale("fr", "FRENCH"));
//        localeList.add(new Locale("it", "ITALIAN"));
//        localeList.add(new Locale("it", "ITALIAN"));
//        localeList.add(new Locale("it", "ITALIAN"));
//        for (Locale locale : Locale.getAvailableLocales()) {
//            System.out.println(locale.getDisplayLanguage());
//        }
        LocalisationSelector instance = new LocalisationSelector(new CommonsSessionStorage() {
            String savedValue = null;

            @Override
            protected String[] getAppDirectoryAlternatives() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            protected String getProjectDirectoryName() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            protected void logError(Exception exception) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            protected void logError(String message, Exception exception) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public Object loadObject(String filename) throws Exception {
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
        }, /*localeList*/ new String[]{"en", "cn", "cz", "ee", "it"});
        instance.askUser(jFrame, icon);
        instance.askUser(jFrame, icon);
        instance.askUser(jFrame, icon);
        instance.askUser(jFrame, icon);
        instance.askUser(jFrame, icon);
        instance.askUser(jFrame, icon);
    }
}