package nl.mpi.arbil;

import java.io.IOException;
import java.util.Properties;
import nl.mpi.arbil.util.ApplicationVersion;

public class ArbilVersion extends ApplicationVersion {

    public ArbilVersion() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getResourceAsStream("/nl/mpi/arbil/version.properties"));
            applicationTitle = properties.getProperty("application.title");
            applicationIconName = properties.getProperty("application.iconName");
            currentMajor = properties.getProperty("application.majorVersion");
            currentMinor = properties.getProperty("application.minorVersion");
            currentRevision = properties.getProperty("application.revision");
            lastCommitDate = properties.getProperty("application.lastCommitDate");
            compileDate = properties.getProperty("application.compileDate");
            currentVersionFile = properties.getProperty("application.currentVersionFile");
            artifactVersion = properties.getProperty("application.projectVersion");
        } catch (IOException ex) {
            System.err.println("Version properties could not be read!");
        }
    }
}
