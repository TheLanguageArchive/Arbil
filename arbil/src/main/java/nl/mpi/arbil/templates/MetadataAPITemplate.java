/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.arbil.templates;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nl.mpi.arbil.ArbilDesktopInjector;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilVocabularies;
import nl.mpi.arbil.data.ArbilVocabulary;
import nl.mpi.arbil.util.BugCatcherManager;
import nl.mpi.metadata.api.MetadataAPI;
import nl.mpi.metadata.api.MetadataException;
import nl.mpi.metadata.api.model.MetadataElement;
import nl.mpi.metadata.api.type.ContainedMetadataElementType;
import nl.mpi.metadata.api.type.ControlledVocabularyItem;
import nl.mpi.metadata.api.type.ControlledVocabularyMetadataType;
import nl.mpi.metadata.api.type.MetadataContainerElementType;
import nl.mpi.metadata.api.type.MetadataDocumentType;
import nl.mpi.metadata.api.type.MetadataElementAttributeType;
import nl.mpi.metadata.api.type.MetadataElementType;
import nl.mpi.metadata.cmdi.api.CMDIApi;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class MetadataAPITemplate implements ArbilTemplate {

    private MetadataAPI metadataAPI;
    private URI templateURI;
    private MetadataDocumentType documentType;
    private Map<String, MetadataElementType> elementPathMap;

    public MetadataAPITemplate(MetadataAPI metadataAPI, URI templateURI) {
	this.metadataAPI = metadataAPI;
	this.templateURI = templateURI;

	this.elementPathMap = new HashMap<String, MetadataElementType>();
    }

    public boolean readTemplate() {
	try {
	    documentType = metadataAPI.getMetadataDocumentType(templateURI);
	    if (documentType != null) {
		readPaths(documentType);
		return true;
	    } else {
		BugCatcherManager.getBugCatcher().logError("Could not find template " + templateURI, null);
	    }
	} catch (IOException ioEx) {
	    BugCatcherManager.getBugCatcher().logError("Error while reading template in metadata API", ioEx);
	} catch (MetadataException mdEx) {
	    BugCatcherManager.getBugCatcher().logError("Error while reading template in metadata API", mdEx);
	}
	return false;
    }

    private void readPaths(MetadataElementType type) {
	elementPathMap.put(type.getPathString(), type);
	if (type instanceof MetadataContainerElementType<?>) {
	    for (MetadataElementType child : ((MetadataContainerElementType<?>) type).getContainableTypes()) {
		readPaths(child);
	    }
	}
    }

    private MetadataElementType getMetadataElement(final String path) {
	final String xPath = path == null ? "/" : path.replace(".", "/:");
	return elementPathMap.get(xPath);
    }

    public String[][] getAutoFieldsArray() {
	// No auto fields
	return new String[][]{};
    }

    public String[][] getFieldConstraints() {
	// TODO
	return new String[][]{};
    }

    public String[][] getFieldTriggersArray() {
	// No field triggers
	return new String[][]{};
    }

    public ArbilVocabulary getFieldVocabulary(String nodePath) {
	MetadataElementType metadataElement = getMetadataElement(nodePath);
	if (metadataElement instanceof ControlledVocabularyMetadataType) {
	    ArbilVocabulary vocabulary = ArbilVocabularies.getSingleInstance().getEmptyVocabulary(templateURI.toString() + "#" + metadataElement.getPathString());

	    for (ControlledVocabularyItem item : ((ControlledVocabularyMetadataType) metadataElement).getItems()) {
		String entryCode = item.getValue();
		String description = item.getDescription();

		if (description == null || description.length() == 0) {
		    vocabulary.addEntry(entryCode, null);
		} else {
		    vocabulary.addEntry(description, entryCode);
		}
	    }
	}
	return null;
    }

    public String[][] getGenreSubgenreArray() {
	// No genre/subgenre
	return new String[][]{};
    }

    public String getHelpStringForField(String fieldName) {
	MetadataElementType metadataElement = getMetadataElement(fieldName);
	return metadataElement.getDescription();
    }

    public String getInsertBeforeOfTemplate(String templatPath) {
	getMetadataElement(templatPath);
	return "";
    }

    public int getMaxOccursForTemplate(String templatPath) {
	MetadataElementType metadataElement = getMetadataElement(templatPath);
	if (metadataElement instanceof ContainedMetadataElementType) {
	    return ((ContainedMetadataElementType) metadataElement).getMaxOccurences();
	}
	return 1;
    }

    public String getParentOfField(String targetFieldPath) {
	getMetadataElement(targetFieldPath);
	return "";
    }

    public String[] getPreferredNameFields() {
	return new String[]{};
    }

    public String[] getRequiredFields() {
	return new String[]{};
    }

    public String[][] getTemplatesArray() {
	return new String[][]{};
    }

    public String[][] getRootTemplatesArray() {
	return new String[][]{};
    }

    public File getTemplateComponentDirectory() {
	return null;
    }

    public File getTemplateDirectory() {
	return null;
    }

    public URL getTemplateFile() {
	try {
	    return documentType.getSchemaLocation().toURL();
	} catch (MalformedURLException e) {
	    BugCatcherManager.getBugCatcher().logError(e);
	    return null;
	}
    }

    public String getTemplateName() {
	return documentType.getName();
    }

    public boolean isArbilChildNode(String childType) {
	MetadataElementType metadataElement = getMetadataElement(childType);
	if (metadataElement != null) {
	    return !(metadataElement instanceof MetadataDocumentType);
	} else {
	    return false;
	}
    }

    public List<String> listAllTemplates() {
	return Collections.EMPTY_LIST;
    }

    public Enumeration<String[]> listTypesFor(Object targetNodeUserObject) {
	//TODO: Pre-process or cache this
	//TODO: Make GUI names unique (see CmdiTemplate#makeGuiNamesUnique)

	// List to collect types in
	final List<String[]> types = new ArrayList<String[]>();

	if (targetNodeUserObject instanceof ArbilDataNode) {
	    // Get root MD element and its type
	    final MetadataElement metadataElement = ((ArbilDataNode) targetNodeUserObject).getMetadataElement();
	    if (metadataElement != null) {
		if (metadataElement.getType() instanceof MetadataContainerElementType) {
		    addChildTypes((MetadataContainerElementType) metadataElement.getType(), types, true);
		} // else no container so no children, skip
	    } else {
		// node must have metadataElement, otherwise not support
		throw new UnsupportedOperationException("Specified ArbilDataNode has no metadataElement. Probably not a MetadataAPI node.");
	    }
	}

	// Sort by display name
	Collections.sort(types, new Comparator<String[]>() {

	    public int compare(String[] o1, String[] o2) {
		return o1[0].compareTo(o2[0]);
	    }
	});
	return Collections.enumeration(types);
    }

    private void addChildTypes(MetadataContainerElementType<MetadataElementType> elementType, List<String[]> types, final boolean addFields) {
	for (MetadataElementType child : elementType.getContainableTypes()) {
	    if (child instanceof ContainedMetadataElementType) {
		final ContainedMetadataElementType type = (ContainedMetadataElementType) child;
		final int minOccurences = type.getMinOccurences();
		final int maxOccurences = type.getMaxOccurences();

		if (addFields && (minOccurences != 1 || maxOccurences != 1)) {
		    // If optional or multiple, add to array
		    types.add(new String[]{type.getName(), type.getPathString().replaceAll("/:", ".")});
		}

		if (type instanceof MetadataContainerElementType) {
		    // If 0 or 1, also process container's children
		    if (maxOccurences == 1) {
			// Child should only add fields if its mandatory
			final boolean childAddFields = addFields && minOccurences == 1;
			addChildTypes((MetadataContainerElementType) type, types, childAddFields);
		    }
		}
	    }
	}
    }

    public Enumeration listTypesFor(Object targetNodeUserObject, boolean includeCorpusNodeEntries) {
	return Collections.enumeration(Collections.EMPTY_SET);
    }

    public boolean pathCanHaveResource(String nodePath) {
	getMetadataElement(nodePath);
	return false;
    }

    public String pathIsChildNode(String nodePath) {
	MetadataElementType elementType = getMetadataElement(nodePath);
	// Must have children
	if (elementType instanceof MetadataContainerElementType
		&& ((MetadataContainerElementType) elementType).getContainableTypes().size() > 0) {
	    // Must be child with valid parent
	    if (elementType instanceof ContainedMetadataElementType) {
		final ContainedMetadataElementType containedType = (ContainedMetadataElementType) elementType;
		final MetadataContainerElementType parent = containedType.getParent();
		if (parent != null) {
		    final int maxOccurs = containedType.getMaxOccurences();
		    if (maxOccurs > 1 || maxOccurs == -1 || maxOccurs != containedType.getMinOccurences()) {
			return parent.getName();
		    }
		}
	    }
	}
	return null;
    }

    public boolean pathIsDeleteableField(String nodePath) {
	getMetadataElement(nodePath);
	return false;
    }

    public boolean pathIsEditableField(String nodePath) {
	MetadataElementType metadataElement = getMetadataElement(nodePath);
	return !(metadataElement instanceof MetadataContainerElementType);
    }

    public List<String[]> getEditableAttributesForPath(String path) {
	MetadataElementType metadataElement = getMetadataElement(path);
	if (metadataElement != null) {
	    Collection<MetadataElementAttributeType> attributes = metadataElement.getAttributes();
	    if (attributes.size() > 0) {
		List attributesList = new ArrayList<String>(attributes.size());
		for (MetadataElementAttributeType attribute : attributes) {
		    //TODO: get attribute path directly from attribute
		    attributesList.add(String.format("%1$s/@%2$s", metadataElement.getPathString(), attribute.getName()));
		}
		return attributesList;
	    }
	}
	return Collections.emptyList();
    }

    public boolean pathAllowsLanguageId(String path) {
	//TODO
	return false;
    }

    public static void main(String args[]) throws URISyntaxException {
	new ArbilDesktopInjector().injectHandlers();
	MetadataAPITemplate template = new MetadataAPITemplate(
		new CMDIApi(),
		new URI(
		"file:///Users/twagoo/svn/metadata-api/trunk/src/test/resources/xsd/TextCorpusProfile.xsd"));
	template.readTemplate();
    }
}
