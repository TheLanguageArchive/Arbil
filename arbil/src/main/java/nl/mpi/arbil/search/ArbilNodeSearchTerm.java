package nl.mpi.arbil.search;

/**
 * Search term that can be combined into a local or remote search
 * @see nl.mpi.arbil.search.ArbilSearch
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface ArbilNodeSearchTerm {
    public static final String BOOLEAN_AND = "and";
    public static final String BOOLEAN_OR = "or";

    public static final String NODE_TYPE_ACTOR = "Actor";
    public static final String NODE_TYPE_ALL = "All";
    public static final String NODE_TYPE_CATALOGUE = "Catalogue";
    public static final String NODE_TYPE_CORPUS = "Corpus";
    public static final String NODE_TYPE_LANGUAGE = "Language";
    public static final String NODE_TYPE_MEDIAFILE = "MediaFile";
    public static final String NODE_TYPE_SESSION = "Session";
    public static final String NODE_TYPE_SOURCE = "Source";
    public static final String NODE_TYPE_WRITTENRESOURCE = "WrittenResource";

    public static final String[] NODE_TYPES = new String[]{NODE_TYPE_ALL, NODE_TYPE_CORPUS, NODE_TYPE_SESSION, NODE_TYPE_CATALOGUE, NODE_TYPE_ACTOR, NODE_TYPE_LANGUAGE, NODE_TYPE_MEDIAFILE, NODE_TYPE_SOURCE, NODE_TYPE_WRITTENRESOURCE};
    public static final String[] BOOLEAN_TYPES = new String[]{BOOLEAN_AND, BOOLEAN_OR};
    public static String COLUMN_FIELD_MESSAGE = "<column (optional)>";
    public static String VALUE_FIELD_MESSAGE = "<value (optional)>";

    /**
     * @return the nodeType
     */
    String getNodeType();

    /**
     * @return the searchFieldName
     */
    String getSearchFieldName();

    /**
     * @return the searchString
     */
    String getSearchString();

    /**
     * @return the booleanAnd
     */
    boolean isBooleanAnd();

    /**
     * @return the notEqual
     */
    boolean isNotEqual();

    /**
     * @param booleanAnd the booleanAnd to set
     */
    void setBooleanAnd(boolean booleanAnd);

    /**
     * @param nodeType the nodeType to set
     */
    void setNodeType(String nodeType);

    /**
     * @param notEqual the notEqual to set
     */
    void setNotEqual(boolean notEqual);

    /**
     * @param searchFieldName the searchFieldName to set
     */
    void setSearchFieldName(String searchFieldName);

    /**
     * @param searchString the searchString to set
     */
    void setSearchString(String searchString);
}
