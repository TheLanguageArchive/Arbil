package nl.mpi.arbil.clarin;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import nl.mpi.arbil.util.BugCatcherManager;
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
	CmdiComponentLinkReader cmdiComponentLinkReader = new CmdiComponentLinkReader();
	try {//http://www.clarin.eu/cmd/example/example-md-instance.xml
	    cmdiComponentLinkReader.readLinks(new URI("http://www.clarin.eu/cmd/example/example-md-instance.cmdi"));
	} catch (URISyntaxException exception) {
	    System.err.println(exception.getMessage());
	}
    }

    public static class CmdiResourceLink {

	public static final String HANDLE_SERVER_URI = "http://hdl.handle.net/";

	// NOTE: should the fields be final?
	public CmdiResourceLink(String proxyId, String type, String ref) {
	    resourceProxyId = proxyId;
	    resourceType = type;
	    resourceRef = ref;
	}
	public final String resourceProxyId;
	public final String resourceType;
	public final String resourceRef;
	private int referencingNodes;

	/**
	 * To be called whenever a reference of this resource link is found
	 */
	public synchronized void addReferencingNode() {
	    referencingNodes++;
	}

	/**
	 * To be called whenever a reference of this resource link is removed (without reloading)
	 */
	public synchronized void removeReferencingNode() {
	    referencingNodes--;
	}

	/**
	 * 
	 * @return Number of references to the resource registered
	 */
	public synchronized int getReferencingNodesCount() {
	    return referencingNodes;
	}

	public URI getLinkUri() throws URISyntaxException {
	    if (resourceRef != null && resourceRef.length() > 0) {
		if (resourceRef.startsWith("hdl://")) { // IS THIS VALID? TG 4/10/2011
		    return new URI(resourceRef.replace("hdl://", HANDLE_SERVER_URI));
		} else if (resourceRef.startsWith("hdl:")) {
		    return new URI(resourceRef.replace("hdl:", HANDLE_SERVER_URI));
		} else {
		    return new URI(resourceRef);
		}
	    }
	    return null;
	}
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

    public URI getLinkUrlString(String resourceId) {
	CmdiResourceLink cmdiResourceLink = getResourceLink(resourceId);
	if (cmdiResourceLink != null) {
	    try {
		return cmdiResourceLink.getLinkUri();
	    } catch (URISyntaxException urise) {
		BugCatcherManager.getBugCatcher().logError(urise);
	    }
	}
	return null;
    }

    public CmdiResourceLink getResourceLink(String resourceId) {
	for (CmdiResourceLink cmdiResourceLink : cmdiResourceLinkArray) {
	    if (cmdiResourceLink.resourceProxyId.equals(resourceId)) {
		return cmdiResourceLink;
	    }
	}
	return null;
    }

    /**
     * 
     * @param resourceRef Resource reference (i.e. content of ResourceRef)
     * @return Id of proxy with the given resource reference. Null if no such ref can be found
     */
    public String getProxyId(String resourceRef) {
	for (CmdiResourceLink resourceLink : cmdiResourceLinkArray) {
	    if (resourceLink.resourceRef.equals(resourceRef)) {
		return resourceLink.resourceProxyId;
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
	    BugCatcherManager.getBugCatcher().logError(e);
	}
	return cmdiResourceLinkArray;
    }

    public void addResourceProxy(
	    String resourceProxyId,
	    String resourceType,
	    String resourceRef) {
	CmdiResourceLink cmdiProfile = new CmdiResourceLink(resourceProxyId, resourceType, resourceRef);

	cmdiResourceLinkArray.add(cmdiProfile);
    }

    public void addResourceRelation(
	    String RelationType,
	    String Res1,
	    String Res2) {
	ResourceRelation resourceRelation = new ResourceRelation(RelationType, Res1, Res2);

	cmdiResourceRelationArray.add(resourceRelation);
    }
}
