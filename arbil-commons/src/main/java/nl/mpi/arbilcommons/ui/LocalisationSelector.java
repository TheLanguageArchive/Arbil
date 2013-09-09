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
import nl.mpi.arbil.userstorage.CommonsSessionStorage;

/**
 * Created on : Sep 9, 2013, 10:34:40 AM
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public class LocalisationSelector {

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
    }

    public LocalisationSelector(CommonsSessionStorage sessionStorage, List<Locale> knownLocales) {
        this.sessionStorage = sessionStorage;
        this.knownLocales = knownLocales;
    }

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
        class LocaleOption {

            private final Locale locale;
            private final String displayName;

            public LocaleOption(Locale locale) {
                this.locale = locale;
                this.displayName = locale.getDisplayLanguage();
            }

            public LocaleOption(String displayName) {
                this.locale = null;
                this.displayName = displayName;
            }

            public Locale getLocale() {
                return locale;
            }

            @Override
            public String toString() {
                return displayName;
            }

            @Override
            public int hashCode() {
                int hash = 5;
                hash = 37 * hash + (this.locale != null ? this.locale.hashCode() : 0);
                return hash;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == null) {
                    return false;
                }
                if (getClass() != obj.getClass()) {
                    return false;
                }
                final LocaleOption other = (LocaleOption) obj;
                if (other.getLocale() == null) {
                    return locale == null;
                }
                return other.getLocale().equals(locale);
            }
        }
        LocaleOption[] possibilities = new LocaleOption[knownLocales.size() + 1];
        possibilities[0] = new LocaleOption(SYSTEM_DEFAULT);
        int localeIndex = 1;
        // loop the locales and add them to posibilities
        for (Locale locale : knownLocales) {
            possibilities[localeIndex] = new LocaleOption(locale);
            localeIndex++;
        }
        final Locale savedLocale = getSavedLocale();
        final LocaleOption defaultValue = (savedLocale == null) ? new LocaleOption(SYSTEM_DEFAULT) : new LocaleOption(savedLocale);
        LocaleOption userSelection = (LocaleOption) JOptionPane.showInputDialog(
                jFrame,
                "Please select your preferred language",
                "Language Selection",
                JOptionPane.PLAIN_MESSAGE,
                icon,
                possibilities, defaultValue);

        if ((userSelection != null)) {
            if (userSelection.getLocale() == null) {
                setSavedLocale(null);
            } else {
                setSavedLocale(userSelection.getLocale());
            }
        }
    }

    public boolean hasSavedLocal() {
        final String selectedLocaleString = sessionStorage.loadString(SELECTED_LOCALE_KEY);
        return selectedLocaleString != null && selectedLocaleString.length() > 0;
    }

    public void setLanguageFromSaved() {
        final Locale savedLocale = getSavedLocale();
        if (savedLocale != null) {
            Locale.setDefault(savedLocale);
        }
    }
}
