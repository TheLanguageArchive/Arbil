package nl.mpi.arbil;

import java.io.IOException;
import java.util.Properties;
import nl.mpi.arbil.util.ApplicationVersion;

public class ArbilVersion extends ApplicationVersion {

    public ArbilVersion() {
	Properties properties = new Properties();
	try {
	    properties.load(getClass().getResourceAsStream("/version.properties"));
	    applicationTitle = properties.getProperty("applicationTitle");
	    applicationIconName = properties.getProperty("applicationIconName");
	    currentMajor = properties.getProperty("currentMajor");
	    currentMinor = properties.getProperty("currentMinor");
	    currentRevision = properties.getProperty("currentRevision");
	    lastCommitDate = properties.getProperty("lastCommitDate");
	    compileDate = properties.getProperty("compileDate");
	    currentVersionFile = properties.getProperty("currentVersionFile");
	} catch (IOException ex) {
	    System.err.println("Version properties could not be read!");
	}
    }
}
