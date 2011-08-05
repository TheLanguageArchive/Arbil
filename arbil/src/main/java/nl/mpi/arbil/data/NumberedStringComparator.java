package nl.mpi.arbil.data;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public abstract class NumberedStringComparator implements Comparator {

    private final static Pattern PATTERN = Pattern.compile("[0-9]+");

    public abstract int compare(Object object1, Object object2);

    /**
     * 
     * @param string1 First string of pair
     * @param string2 Second string of pair
     * @return 
     */
    protected Integer compareNumberedStrings(String string1, String string2) {
	// If both strings end in an integer, check if prefix is identical, then compare on basis of int values
	final Matcher match1 = getPattern().matcher(string1);
	if (match1.find()) {
	    // See if prefix matches same area in string 2
	    if (string1.regionMatches(0, string2, 0, match1.start())) {
		// See if string 2 ends in integer as well, and check whether prefixes match completely
		final Matcher match2 = getPattern().matcher(string2);
		if (match2.find() && match1.start() == match2.start()) {
		    // Get integer values and compare
		    try {
			return Integer.parseInt(match1.group()) - Integer.parseInt(match2.group());
		    } catch (NumberFormatException ex) {
			// something gone wrong with the regex, revert to string comparison below
		    }
		}
	    }
	}
	return null;
    }
    
    protected Pattern getPattern(){
	return PATTERN;
    }
}
