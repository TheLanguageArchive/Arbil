package nl.mpi.arbil.clarin;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import nl.mpi.arbil.ui.GuiHelper;
import org.apache.commons.digester.Digester;

/**
 * CmdiResourceLinkReader.java
 * Created on March 12, 2010, 17:04:03
 * @author Peter.Withers@mpi.nl
 */
public class CmdiComponentLinkReader {

    public ArrayList<CmdiResourceLink> cmdiResourceLinkArray = null;
    public ArrayList<ResourceRelation> cmdiResourceRelationArray = null;

    public static void main(String args[]) {
        System.out.println("CmdiComponentLinkReader");
        CmdiComponentLinkReader cmdiComponentLinkReader = new CmdiComponentLinkReader();
        try {//http://www.clarin.eu/cmd/example/example-md-instance.xml
            cmdiComponentLinkReader.readLinks(new URI("http://www.clarin.eu/cmd/example/example-md-instance.cmdi"));
        } catch (URISyntaxException exception) {
            System.err.println(exception.getMessage());
        }
    }

    public class CmdiResourceLink {

        public String resourceProxyId;
        public String resourceType;
        public String resourceRef;
    }

    public class ResourceRelation {

        String relationType;
        String res1;
        String res2;
    }

    public CmdiComponentLinkReader() {
    }

    public URI getLinkUrlString(String resourceRef) {
        for (CmdiResourceLink cmdiResourceLink : cmdiResourceLinkArray) {
            if (cmdiResourceLink.resourceProxyId.equals(resourceRef)) {
                try {
                    if (cmdiResourceLink.resourceRef != null && cmdiResourceLink.resourceRef.length() > 0) {
                        if (cmdiResourceLink.resourceRef.startsWith("hdl://")) {
                            return new URI(cmdiResourceLink.resourceRef.replace("hdl://", "http://hdl.handle.net/"));
                        } else {
                            return new URI(cmdiResourceLink.resourceRef);
                        }
                    }
                } catch (URISyntaxException urise) {
                    GuiHelper.linorgBugCatcher.logError(urise);
                }
            }
        }
        return null;
    }

    public ArrayList<CmdiResourceLink> readLinks(URI targetCmdiNode) {
//        ArrayList<URI> returnUriList = new ArrayList<URI>();
        try {
            Digester digester = new Digester();
            digester.push(this);
            digester.addCallMethod("CMD/Resources/ResourceProxyList/ResourceProxy", "addResourceProxy", 3);
            digester.addCallParam("CMD/Resources/ResourceProxyList/ResourceProxy", 0, "id");
            digester.addCallParam("CMD/Resources/ResourceProxyList/ResourceProxy/ResourceType", 1);
            digester.addCallParam("CMD/Resources/ResourceProxyList/ResourceProxy/ResourceRef", 2);

            digester.addCallMethod("CMD/Resources/ResourceRelationList/ResourceRelation", "addResourceRelation", 3);
            digester.addCallParam("CMD/Resources/ResourceRelationList/ResourceRelation/RelationType", 0);
            digester.addCallParam("CMD/Resources/ResourceRelationList/ResourceRelation/Res1", 1, "ref");
            digester.addCallParam("CMD/Resources/ResourceRelationList/ResourceRelation/Res2", 2, "ref");

            cmdiResourceLinkArray = new ArrayList<CmdiResourceLink>();
            cmdiResourceRelationArray = new ArrayList<ResourceRelation>();
            digester.parse(targetCmdiNode.toURL());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cmdiResourceLinkArray;
    }

    public void addResourceProxy(
            String resourceProxyId,
            String resourceType,
            String resourceRef) {
        System.out.println("addResourceProxy: " + resourceProxyId + " : " + resourceType + " : " + resourceRef);

        CmdiResourceLink cmdiProfile = new CmdiResourceLink();
        cmdiProfile.resourceProxyId = resourceProxyId;
        cmdiProfile.resourceType = resourceType;
        cmdiProfile.resourceRef = resourceRef;

        cmdiResourceLinkArray.add(cmdiProfile);
    }

    public void addResourceRelation(
            String RelationType,
            String Res1,
            String Res2) {
        System.out.println("addResourceRelation: " + RelationType + " : " + Res1 + " : " + Res2);

        ResourceRelation resourceRelation = new ResourceRelation();
        resourceRelation.relationType = RelationType;
        resourceRelation.res1 = Res1;
        resourceRelation.res2 = Res2;

        cmdiResourceRelationArray.add(resourceRelation);
    }
}
