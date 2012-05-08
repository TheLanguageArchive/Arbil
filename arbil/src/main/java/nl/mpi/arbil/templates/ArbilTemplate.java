package nl.mpi.arbil.templates;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
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
    URL getTemplateFile();

    String getTemplateName();

    boolean isArbilChildNode(String childType);

    List<String> listAllTemplates();

    Enumeration<String[]> listTypesFor(Object targetNodeUserObject);

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

    boolean readTemplate();
    
    List<String[]> getEditableAttributesForPath(final String path);
    
    public boolean pathAllowsLanguageId(String path);
}
