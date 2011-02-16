package mpi.metadata.tools.mdeditor.app.gui;

/**
 * Title:        IMDI Editior Description:  Represents aliases for standard
 * text to be added to a LookAheadTextField. If text is entered in a
 * LookAheadTextField it is checked wheter an alias is provided for the text
 * or if the text itself is an alias. The content of the text will change to
 * the alias. 2001 Company:      Max Planck Institute for Psycholinguistics
 *
 * @author Don Willems
 * @version 1.0
 *
 * @since IMDI 0.5
 */
public class LookAheadAlias implements java.io.Serializable {
    private String source;
    private String alias;

    /**
     * Takes a source and an alias (for that source) string and constructs a
     * new LookAheadAlias.
     *
     * @param source The source string for which alias is the alias.
     * @param alias The alias for the source string.
     */
    public LookAheadAlias(String source, String alias) {
        this.source = source;
        this.alias = alias;
    }

    /**
     * Returns the alias for the source.
     *
     * @return The alias.
     */
    public String getSource() {
        return source;
    }

    /**
     * Returns the source.
     *
     * @return The source.
     */
    public String getAlias() {
        return alias;
    }
}
