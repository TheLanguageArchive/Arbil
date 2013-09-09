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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import nl.mpi.arbil.userstorage.CommonsSessionStorage;

/**
 * Created on : Sep 9, 2013, 10:34:40 AM
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public class LocalisationSelector extends JPanel {

    private static final String SELECTED_LOCALE_KEY = "selectedLocale";
    private static final String SYSTEM_DEFAULT = "<system default>";
    private final CommonsSessionStorage sessionStorage;
    // list of known locales which might differ between applications that use this local selection widget
    private final List<Locale> knownLocales;

    public LocalisationSelector(CommonsSessionStorage sessionStorage, String[] availableLocales) {
        this.sessionStorage = sessionStorage;
        knownLocales = new ArrayList<Locale>();
        for (String locale : availableLocales) {
            knownLocales.add(new Locale(locale));
        }
//        initUI();
    }

    public LocalisationSelector(CommonsSessionStorage sessionStorage, List<Locale> knownLocales) {
        this.sessionStorage = sessionStorage;
        this.knownLocales = knownLocales;
//        initUI();
    }

//    private void initUI() {
////        JComboBox<Locale> comboBox = new JComboBox<Locale>(knownLocales.toArray(new Locale[knownLocales.size()]));
//        JComboBox<Locale> comboBox = new JComboBox<Locale>(new ComboBoxModel<Locale>() {
//            int selected = 0;
//
//            public void setSelectedItem(Object anItem) {
//                if (anItem instanceof Locale) {
//                    selected = knownLocales.indexOf(anItem) + 1;
//                } else {
//                    selected = 0;
//                }
//            }
//
//            public Object getSelectedItem() {
//                if (selected == 0) {
//                    return SYSTEM_DEFAULT;
//                } else {
//                    return knownLocales.get(selected - 1);
//                }
//            }
//
//            public int getSize() {
//                return knownLocales.size() + 1;
//            }
//
//            public Locale getElementAt(int index) {
//                if (selected == 0) {
//                    return null;
//                } else {
//                    return knownLocales.get(index - 1);
//                }
//            }
//
//            public void addListDataListener(ListDataListener l) {
//            }
//
//            public void removeListDataListener(ListDataListener l) {
//            }
//        });
//        this.add(comboBox);
//    }
    private Locale getSavedLocale() {
        final String selectedLocaleString = sessionStorage.loadString(SELECTED_LOCALE_KEY);
        if (selectedLocaleString == null || selectedLocaleString.equals(SYSTEM_DEFAULT)) {
            return null;
        } else {
            return new Locale(selectedLocaleString);
        }
    }

    private void setSavedLocale(Locale locale) {
        if (locale == null) {
            sessionStorage.saveString(SELECTED_LOCALE_KEY, SYSTEM_DEFAULT);
        } else {
            sessionStorage.saveString(SELECTED_LOCALE_KEY, locale.toString());
        }
    }

    public void askUser(JFrame jFrame, Icon icon) {
        String[] possibilities = new String[knownLocales.size() + 1];
        possibilities[0] = SYSTEM_DEFAULT;
        int localeIndex = 1;
        // loop the locales and add them to posibilities
        for (Locale locale : knownLocales) {
            possibilities[localeIndex] = locale.getDisplayLanguage();
            localeIndex++;
        }
        final Locale savedLocale = getSavedLocale();
        final String defaultValue = (savedLocale == null) ? SYSTEM_DEFAULT : savedLocale.getDisplayLanguage();
        Object userSelection = JOptionPane.showInputDialog(
                jFrame,
                "Complete the sentence:\n"
                + "\"Green eggs and...\"",
                "Customized Dialog",
                JOptionPane.PLAIN_MESSAGE,
                icon,
                possibilities, defaultValue);

        if ((userSelection != null)) {
            if (userSelection == SYSTEM_DEFAULT) {
                setSavedLocale(null);
            } else if (userSelection instanceof Locale) {
                setSavedLocale((Locale) userSelection);
            }
        }
    }

    public void setLanguageFromSaved() {
//        AccessController.doPrivileged(
//            new SetPropertyAction("user.language", "en");
//            Locale.setDefault()
//		System.setProperty("user.language", "sv");
//		System.setProperty("user.country", "SE");
//);
        Locale.setDefault(Locale.ENGLISH);
    }
}
