package nl.mpi.arbil.clarin;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import nl.mpi.arbil.util.BugCatcher;
import org.apache.commons.digester.Digester;

/**
 * CmdiResourceLinkReader.java
 * Created on March 12, 2010, 17:04:03
 * @author Peter.Withers@mpi.nl
 */
public class CmdiComponentLinkReader {

    private static BugCatcher bugCatcher;

    public static void setBugCatcher(BugCatcher bugCatcherInstance){
        bugCatcher = bugCatcherInstance;
    }
    
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

    public static class CmdiResourceLink {

        // NOTE: should the fields be final?
        public CmdiResourceLink(String proxyId, String type, String ref) {
            resourceProxyId = proxyId;
            resourceType = type;
            resourceRef = ref;
        }

        public final String resourceProxyId;
        public final  String resourceType;
        public final  String resourceRef;
    }

    public static class ResourceRelation {

        // NOTE: should the fields be final?
        public ResourceRelation(String type, String resource1, String resource2) {
            relationType = type;
            res1 = resource1;
            res2 = resource2;
        }

        public final String relationType;
        public final String res1;
        public final String res2;
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
                    bugCatcher.logError(urise);
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
            bugCatcher.logError(e);
        }
        return cmdiResourceLinkArray;
    }

    public void addResourceProxy(
            String resourceProxyId,
            String resourceType,
            String resourceRef) {
        System.out.println("addResourceProxy: " + resourceProxyId + " : " + resourceType + " : " + resourceRef);

        CmdiResourceLink cmdiProfile = new CmdiResourceLink(resourceProxyId, resourceType, resourceRef);

        cmdiResourceLinkArray.add(cmdiProfile);
    }

    public void addResourceRelation(
            String RelationType,
            String Res1,
            String Res2) {
        System.out.println("addResourceRelation: " + RelationType + " : " + Res1 + " : " + Res2);

        ResourceRelation resourceRelation = new ResourceRelation(RelationType, Res1, Res2);

        cmdiResourceRelationArray.add(resourceRelation);
    }
}
