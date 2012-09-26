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
    public void testGetNodeTypeFromMimeType() {
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
