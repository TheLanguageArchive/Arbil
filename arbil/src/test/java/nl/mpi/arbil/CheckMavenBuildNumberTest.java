/**
 * Copyright (C) 2014 The Language Archive, Max Planck Institute for Psycholinguistics
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
package nl.mpi.arbil;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Document : CheckMavenBuildNumberTest <br> Created on : Sep 25, 2012, 13:20:24
 *
 * @author : Peter Withers
 */
public class CheckMavenBuildNumberTest {

    @Test
    public void testMavenBuildNumber() {
        // this tests that the correct build number is specified in the pom.xml based on the current svn version
        // either the correct build number or a snapshot version are valid
        String errorMessage = "The maven version does not match either the snapshot nor the current svn build number.\n The pom.xml must be updated, please use either the correct build number or a snapshot version.";
        ArbilVersion arbilVersion = new ArbilVersion();
        String svnVersion = arbilVersion.currentMajor + "." + arbilVersion.currentMinor + "." + arbilVersion.currentRevision + "-";
        System.out.println("svnVersion: " + svnVersion + " ... ");
        String snapshotVersion = arbilVersion.currentMajor + "." + arbilVersion.currentMinor + "-";
        System.out.println("snapshotVersion: " + snapshotVersion + " ... " + "-SNAPSHOT");
        String mavenBuildVersion = arbilVersion.artifactVersion;
        System.out.println("mavenBuildVersion: " + mavenBuildVersion);
        if (mavenBuildVersion.endsWith("-SNAPSHOT")) {
            assertTrue(errorMessage, mavenBuildVersion.startsWith(snapshotVersion));
        } else {
            assertTrue(errorMessage, mavenBuildVersion.startsWith(svnVersion));
        }

    }
}
