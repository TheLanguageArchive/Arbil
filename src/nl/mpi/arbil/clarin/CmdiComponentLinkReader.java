package nl.mpi.arbil.clarin;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import org.apache.commons.digester.Digester;

/**
 * CmdiComponentLinkReader.java
 * Created on March 12, 2010, 17:04:03
 * @author Peter.Withers@mpi.nl
 */
@Deprecated
public class CmdiComponentLinkReader {

    public ArrayList<CmdiComponentLink> cmdiComponentLinkArray = null;
 
    public static void main(String args[]) {
        CmdiComponentLinkReader cmdiComponentLinkReader = new CmdiComponentLinkReader();
        try {
            cmdiComponentLinkReader.readLinks(new URI("file:/Users/petwit/.arbil/imdicache/20100309150110/20100309150110/20100312161347/20100312161400.cmdi"));
        } catch (URISyntaxException exception) {
            System.err.println(exception.getMessage());
        }
    }

    public class CmdiComponentLink {

        public String componentId;
        public String filename;
        public String name;
        public String CardinalityMax;
        public String CardinalityMin;
        public String href;
    }

    public CmdiComponentLinkReader() {
    }

    public ArrayList<CmdiComponentLink> readLinks(URI targetCmdiNode) {
        try {
            Digester digester = new Digester();
            // This method pushes this (SampleDigester) class to the Digesters
            // object stack making its methods available to processing rules.
            digester.push(this);
            // This set of rules calls the addProfile method and passes
            // in five parameters to the method.
            digester.addCallMethod("CMD_ComponentSpec/CMD_Component", "addProfile", 2);
            digester.addCallParam("CMD_ComponentSpec/CMD_Component", 0, "ComponentId");
            digester.addCallParam("CMD_ComponentSpec/CMD_Component", 1, "filename");

            cmdiComponentLinkArray = new ArrayList<CmdiComponentLink>();
            digester.parse(targetCmdiNode.toURL());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cmdiComponentLinkArray;
    }  // Example method called by Digester.

    public void addProfile(
            String componentId,
            String filename //String id,
            //String description,
            //String name,
            //String registrationDate,
            //String creatorName,
            //String href
            ) {
        System.out.println(componentId + " : " + filename);

        CmdiComponentLink cmdiProfile = new CmdiComponentLink();
        cmdiProfile.componentId = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/components/" + componentId;
        cmdiProfile.filename = filename;
//        cmdiProfile.name = name;
//        cmdiProfile.registrationDate = registrationDate;
//        cmdiProfile.creatorName = creatorName;
//        cmdiProfile.href = href;

        // todo: read the sub component for the other values like CardinalityMin CardinalityMax name etc
        cmdiComponentLinkArray.add(cmdiProfile);
    }
}
