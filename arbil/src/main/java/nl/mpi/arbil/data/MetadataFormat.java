/**
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
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
package nl.mpi.arbil.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.swing.ImageIcon;
import nl.mpi.arbil.ArbilIcons;

/**
 * Document : MetadataFormat
 * Created on : Aug 26, 2011, 10:19:34 AM
 * Author : Peter Withers
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
		new FormatType(".imdi", ".METATRANSCRIPT", null, true),
		// todo: the filter strings used by the cmdi templates and metadata loading process should be reading the metadataStartXpath from here instead
		new FormatType(".cmdi", ".CMD.Components", ArbilIcons.clarinIcon, false),
		// Generic XML
		new FormatType(".xml", "", ArbilIcons.clarinIcon, false), // Clarin icon is not really appropriate
		// KMDI, Kinship metadata
		new FormatType(".kmdi", ".Kinnate.CustomData", ArbilIcons.kinOathIcon, false),
		// TLA test results
		new FormatType(".trx", "", ArbilIcons.clarinIcon, false)})); // Clarin icon is not really appropriate

//    private static MetadataFormat singleInstance = null;
//    static synchronized public MetadataFormat getSingleInstance() {
////        logger.debug("LinorgWindowManager getSingleInstance");
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
