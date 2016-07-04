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
package nl.mpi.arbil.localisation;

import java.io.InputStream;
import java.util.Locale;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class LocalisationUtils {

    private final Class referenceClass;

    public LocalisationUtils(Class referenceClass) {
	this.referenceClass = referenceClass;
    }

    public LocalisationUtils() {
	this(LocalisationUtils.class);
    }

    public InputStream getLocalizedResourceStream(String resourceLocation) {
	// Try to get variant matching current locale
	final Locale locale = Locale.getDefault();
	final int extensionLocation = resourceLocation.lastIndexOf(".");
	if (extensionLocation >= 0) {
	    // Try with full locale (e.g. 'en_US')
	    {
		final InputStream resourceStream = tryLocalizedResourceStream(resourceLocation, extensionLocation, locale.toString());
		if (resourceStream != null) {
		    return resourceStream;
		}
	    }
	    // Try with just language (e.g. 'en')
	    {
		final InputStream resourceStream = tryLocalizedResourceStream(resourceLocation, extensionLocation, locale.getLanguage());
		if (resourceStream != null) {
		    return resourceStream;
		}
	    }
	}
	// Fall back to default resource location
	return referenceClass.getResourceAsStream(resourceLocation);
    }

    private InputStream tryLocalizedResourceStream(String resourceLocation, final int extensionLocation, final String localeString) {
	// Insert locale string before final extension (e.g. myresource_en_US.html)
	final String localizedResourceLocation = String.format("%s_%s%s",
		resourceLocation.substring(0, extensionLocation),
		localeString,
		resourceLocation.substring(extensionLocation));
	return referenceClass.getResourceAsStream(localizedResourceLocation);
    }
}
