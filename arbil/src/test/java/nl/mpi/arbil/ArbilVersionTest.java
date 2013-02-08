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
package nl.mpi.arbil;

import java.util.Properties;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilVersionTest {

    public final String ERROR_MESSAGE = "The maven version does not match either the snapshot nor the current svn build number.\n The pom.xml must be updated, please use either the correct build number or a snapshot version.";

    @Test
    public void testGetArtifactVersion() throws Exception {
	Properties properties = new Properties();
	properties.load(getClass().getResourceAsStream("/nl/mpi/arbil/version.properties"));
	final int majorVersionNumber = Integer.parseInt(properties.getProperty("application.majorVersion"));
	final int minorVersionNumber = Integer.parseInt(properties.getProperty("application.minorVersion"));
	final int buildVersionNumber = Integer.parseInt(properties.getProperty("application.revision"));
	final String mavenBuildVersion = properties.getProperty("application.projectVersion");

	String svnVersion = majorVersionNumber + "." + minorVersionNumber + "." + buildVersionNumber + "-";
	System.out.println("svnVersion: " + svnVersion + " ... ");
	String snapshotVersion = majorVersionNumber + "." + minorVersionNumber + "-";
	System.out.println("snapshotVersion: " + snapshotVersion + " ... " + "-SNAPSHOT");
	System.out.println("mavenBuildVersion: " + mavenBuildVersion);
	if (mavenBuildVersion.endsWith("-SNAPSHOT")) {
	    if (!mavenBuildVersion.startsWith(snapshotVersion)) {
		fail(ERROR_MESSAGE);
	    }
	} else {
	    if (!mavenBuildVersion.startsWith(svnVersion)) {
		fail(ERROR_MESSAGE);
	    }
	}
    }
}
