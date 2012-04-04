package nl.mpi.arbil.templates;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import nl.mpi.arbil.data.ArbilDataNode;
import nl.mpi.arbil.data.ArbilVocabulary;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface ArbilTemplate {

    /**
     * @return the autoFieldsArray
     */
    String[][] getAutoFieldsArray();

    /**
     * @return the fieldConstraints
     */
    String[][] getFieldConstraints();

    /**
     * @return the fieldTriggersArray
     */
    String[][] getFieldTriggersArray();

    ArbilVocabulary getFieldVocabulary(String nodePath);

    /**
     * @return the genreSubgenreArray
     */
    String[][] getGenreSubgenreArray();

    String getHelpStringForField(String fieldName);

    String getInsertBeforeOfTemplate(String templatPath);

    int getMaxOccursForTemplate(String templatPath);

    String getParentOfField(String targetFieldPath);

    /**
     * @return the preferredNameFields
     */
    String[] getPreferredNameFields();

    /**
     * @return the requiredFields
     */
    String[] getRequiredFields();

    String[][] getTemplatesArray();
    
    /**
     * @return the rootTemplatesArray
     */
    String[][] getRootTemplatesArray();

    File getTemplateComponentDirectory();

    File getTemplateDirectory();

    /**
     * @return the templateFile
     */
    File getTemplateFile();

    String getTemplateName();

    boolean isArbilChildNode(String childType);

    ArrayList<String> listAllTemplates();

    Enumeration listTypesFor(Object targetNodeUserObject);

    /**
     * This function is only a place holder and will be replaced.
     * @param targetNodeUserObject The imdi node that will receive the new child.
     * @return An enumeration of Strings for the available child types, one of which will be passed to "listFieldsFor()".
     */
    Enumeration listTypesFor(Object targetNodeUserObject, boolean includeCorpusNodeEntries);

    /**
     * @param dataNode Node that has to be checked
     * @param type XML path of candidate type for containment
     * @return Whether any of the possible types matches the provided type
     */
    boolean nodeCanContainType(ArbilDataNode dataNode, String type);

    boolean pathCanHaveResource(String nodePath);

    String pathIsChildNode(String nodePath);

    boolean pathIsDeleteableField(String nodePath);

    boolean pathIsEditableField(String nodePath);

    boolean readTemplate(File templateConfigFile, String templateName);
    
}
