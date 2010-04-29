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
public class CmdiComponentLinkReader {

    public ArrayList<CmdiComponentLink> cmdiComponentLinkArray = null;

    public static void main(String args[]) {
        System.out.println("CmdiComponentLinkReader");
        CmdiComponentLinkReader cmdiComponentLinkReader = new CmdiComponentLinkReader();
        try {
            cmdiComponentLinkReader.readLinks(new URI("http://www.clarin.eu/cmd/example/example-md-instance.xml"));
        } catch (URISyntaxException exception) {
            System.err.println(exception.getMessage());
        }
    }

    public class CmdiComponentLink {

        public String resourceProxyId;
        public String resourceType;
        public String resourceRef;
    }

    private CmdiComponentLinkReader() {
    }

    public ArrayList<CmdiComponentLink> readLinks(URI targetCmdiNode) {
        try {
            Digester digester = new Digester();
            digester.push(this);
            digester.addCallMethod("CMD/Resources/ResourceProxyList/ResourceProxy", "addResourceProxy", 3);
            digester.addCallParam("CMD/Resources/ResourceProxyList/ResourceProxy", 0, "id");
            digester.addCallParam("CMD/Resources/ResourceProxyList/ResourceProxy/ResourceType", 1);
            digester.addCallParam("CMD/Resources/ResourceProxyList/ResourceProxy/ResourceRef", 2);

            cmdiComponentLinkArray = new ArrayList<CmdiComponentLink>();
            digester.parse(targetCmdiNode.toURL());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cmdiComponentLinkArray;
    }

    public void addResourceProxy(
            String resourceProxyId,
            String resourceType,
            String resourceRef) {
        System.out.println(resourceProxyId + " : " + resourceType + " : " + resourceRef);

        CmdiComponentLink cmdiProfile = new CmdiComponentLink();
        //cmdiProfile.componentId = "http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/components/" + componentId;
        cmdiProfile.resourceProxyId = resourceProxyId;
        cmdiProfile.resourceType = resourceType;
        cmdiProfile.resourceRef = resourceRef;

        cmdiComponentLinkArray.add(cmdiProfile);
    }
}
