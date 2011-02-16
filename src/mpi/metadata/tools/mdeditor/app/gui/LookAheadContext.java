/*
 * LookAheadContext.java
 *
 * Created on December 18, 2000, 9:22 AM
 */
package mpi.metadata.tools.mdeditor.app.gui;

/**
 * This interface defines the methods that classes need to provide a context
 * for a lookahead text field.
 *
 * @author Don Willems
 * @version 0.2
 */
public interface LookAheadContext {
    /**
     * Returns the context for this input, containing the first string that
     * starts with the provided input, the input string itself and the
     * suggested selection interval for a textfield. The suggested selection
     * is the context string minus the input string.
     *
     * @param pos The curent cursor position.
     * @param input The input to the text field.
     *
     * @return The context.
     */
    public Context getContext(String input, int pos);

    /**
     * Returns true if a context string is available and false if not.
     *
     * @param input The string.
     * @param pos The curent cursor position.
     *
     * @return true if a context is avaliable.
     */
    public boolean hasContext(String input, int pos);

    /**
     * Returns the selection interval that selects one item of the context.
     * Generally this will return the selection of the complete text, but in
     * certain cases (for instance with ControlledVocabularyList) only parts
     * of the text will be selected.
     *
     * @param content The content.
     * @param pos The position of the cursor that is to be included in the
     *        selection.
     *
     * @return The selection.
     */
    public SelectionInterval getItemSelectionAtPosition(String content, int pos);
}
