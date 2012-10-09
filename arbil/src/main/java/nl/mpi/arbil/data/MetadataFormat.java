package nl.mpi.arbil.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.swing.ImageIcon;
import nl.mpi.arbil.ArbilIcons;

/**
 *  Document   : MetadataFormat
 *  Created on : Aug 26, 2011, 10:19:34 AM
 *  Author     : Peter Withers
 */
public class MetadataFormat {

    static class FormatType {

	private boolean isImdi;
	private String suffixString;
	private String metadataStartXpath;
	private ImageIcon imageIcon;

	public FormatType(String suffixString, String metadataStartXpath, ImageIcon imageIcon, boolean isImdi) {
	    this.isImdi = isImdi;
	    this.suffixString = suffixString;
	    this.metadataStartXpath = metadataStartXpath;
	    this.imageIcon = imageIcon;
	}
    }
    private final static Collection<FormatType> knownFormats = new CopyOnWriteArraySet<FormatType>(Arrays.asList(new FormatType[]{
		new FormatType(".imdi", "", null, true),
		new FormatType(".cmdi", "", ArbilIcons.getSingleInstance().clarinIcon, false),
		// Generic XML
		new FormatType(".xml", "", ArbilIcons.getSingleInstance().clarinIcon, false), // Clarin icon is not really appropriate
		// KMDI, Kinship metadata
		new FormatType(".kmdi", ".Kinnate.Metadata", ArbilIcons.getSingleInstance().kinOathIcon, false),
		// TLA test results
		new FormatType(".trx", "", ArbilIcons.getSingleInstance().clarinIcon, false)})); // Clarin icon is not really appropriate

//    private static MetadataFormat singleInstance = null;
//    static synchronized public MetadataFormat getSingleInstance() {
////        System.out.println("LinorgWindowManager getSingleInstance");
//        if (singleInstance == null) {
//            singleInstance = new MetadataFormat();
//        }
//        return singleInstance;
//    }
    public static boolean isPathImdi(String urlString) {
	for (FormatType formatType : knownFormats) {
	    if (urlString.endsWith(formatType.suffixString)) {
		return formatType.isImdi;
	    }
	}
	return false;
    }

    public static boolean isPathCmdi(String urlString) {
	for (FormatType formatType : knownFormats) {
	    if (urlString.endsWith(formatType.suffixString)) {
		return !formatType.isImdi;
	    }
	}
	return false;
    }

    public static ImageIcon getFormatIcon(String urlString) {
	for (FormatType formatType : knownFormats) {
	    if (urlString.endsWith(formatType.suffixString)) {
		return formatType.imageIcon;
	    }
	}
	return null;
    }

    public static String getMetadataStartPath(String urlString) {
	for (FormatType formatType : knownFormats) {
	    if (urlString.endsWith(formatType.suffixString)) {
		return formatType.metadataStartXpath;
	    }
	}
	return null;
    }

    static public boolean isPathMetadata(String urlString) {
	return isPathImdi(urlString) || isPathCmdi(urlString); // change made for clarin
    }
}
