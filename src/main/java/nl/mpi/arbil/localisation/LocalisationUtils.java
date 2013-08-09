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
