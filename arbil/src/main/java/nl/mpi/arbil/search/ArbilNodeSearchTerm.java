package nl.mpi.arbil.search;

/**
 * Search term that can be combined into a local or remote search
 * @see nl.mpi.arbil.search.ArbilSearch
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface ArbilNodeSearchTerm {

    static final String[] NODE_TYPES = new String[]{"All",
	"Corpus", "Session", "Catalogue", "Actor", "Language", "MediaFile", "Source", "WrittenResource"
    };
    static final String[] BOOLEAN_TYPES = new String[]{"and", "or"};
    String columnFieldMessage = "<column (optional)>";
    String valueFieldMessage = "<value (optional)>";
    
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
