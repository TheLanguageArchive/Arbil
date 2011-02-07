package nl.mpi.arbil.FieldEditors;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.StringTokenizer;
import javax.swing.JComboBox;
import javax.swing.text.JTextComponent;
import nl.mpi.arbil.ImdiField;
import nl.mpi.arbil.ImdiVocabularies;

/**
 * Editable combo box that has the items of a controlled vocabulary in it. The
 * vocabulary can be open or closed, as well as a list or single valued. The
 * combo box reflects these properties.
 *
 * Type-ahead is currently implemented. However, selecting an item from the combo
 * will have the (possibly) undesired effect of having an entire list replaced
 * with that single value.
 *
 * Document   : ControlledVocabularyComboBox
 * Created on : Wed Oct 07 11:07:30 CET 2009
 * @author Peter.Withers@mpi.nl
 * @author Twan.Goosen@mpi.nl
 */
public class ControlledVocabularyComboBox extends JComboBox implements KeyListener, ActionListener {

    ImdiField targetField;
    private final static char SEPARATOR = ',';
    private String currentValue;
    // Flag indicating whether autocompletion is in process
    private boolean typingAhead = false;

    public ControlledVocabularyComboBox(ImdiField targetField) {
        this.targetField = targetField;
        //currentValue = targetField.getFieldValue();

        ImdiVocabularies.Vocabulary fieldsVocabulary = targetField.getVocabulary();
        if (null == fieldsVocabulary || null == fieldsVocabulary.findVocabularyItem(targetField.getFieldValue())) {
            this.addItem(targetField.getFieldValue());
        }
        if (null != fieldsVocabulary) {
            for (ImdiVocabularies.VocabularyItem vocabularyListItem : fieldsVocabulary.getVocabularyItems()) {
                this.addItem(vocabularyListItem.languageName);
            }
        }

        this.setSelectedItem(targetField.toString());

        this.setEditable(true);

        if (targetField.isVocabularyOpen()) {
            this.getEditor().getEditorComponent().requestFocusInWindow();
        } else {
            this.requestFocusInWindow();
        }

        this.getEditor().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                System.out.println("editor action performed");
            }
        });

        this.setUI(new javax.swing.plaf.basic.BasicComboBoxUI());

        this.addActionListener(this);
        getEditorComponent().addKeyListener(this);

    }

    public String getCurrentValue() {
        if (currentValue == null) {
            currentValue = getCurrentValueString();
        }
        return currentValue;
    }

    /**
     * Listener callback for combo box actionPerformed event
     * @param actionEvent
     */
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        // handle item selections
        if (!typingAhead) {
            currentValue = getCurrentValueString();

//            if (actionEvent.getSource() instanceof JComboBox) {
//                JComboBox cb = (JComboBox) actionEvent.getSource();
//                currentValue = (String) cb.getSelectedItem();
//            }

            System.out.println("currentValue: " + currentValue);
        }
    }

    // EDITOR KEY LISTENERS
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            // ENTER pressed
            synchronized (this) {
                typingAhead = true;

                if (autoComplete()) {
                    // Completed current item, do not perform any more actions
                    // on this key event
                    e.consume();
                }
                typingAhead = false;

            }
        }
    }

    public void keyReleased(KeyEvent e) {
        if (!typingAhead) {
            if (!e.isActionKey()
                    && e.getKeyCode() != KeyEvent.VK_BACK_SPACE
                    && e.getKeyCode() != KeyEvent.VK_DELETE) {
                // Handle character: update auto complete match
                handleCharacter(e);
            }
        }

        System.out.println("keyReleased: " + e.getKeyChar());
    }

    public void keyTyped(KeyEvent e) {
    }

    private void handleCharacter(KeyEvent e) {
        typeAhead();
    }

    // TYPE-AHEAD AND AUTO-COMPLETE METHODS
    private void typeAhead() {
        int matchIndex = getMatchingItem(getEditorValue());
        if (matchIndex >= 0) {
            String match = (String) getItemAt(matchIndex);
            typeAhead(match);
            System.out.println("Match: " + match);
        }
    }

    /**
     * Types ahead current item. Remaining part of the target string is
     * selected
     * @param value Type-ahead target string
     */
    private synchronized void typeAhead(String value) {
        typingAhead = true;
        String currentEditorValue = getEditorValue();

        JTextComponent textComponent = getEditorComponent();
        int position = textComponent.getCaretPosition();
        setEditorValue(value);
        textComponent.setSelectionStart(position);
        textComponent.setSelectionEnd(position + value.length() - currentEditorValue.length());
        typingAhead = false;
    }

    /**
     * Performs auto complete on current item. In effect, current selection is
     * skipped. If nothing is selected, nothing is skipped and false is returned
     *
     * @return Whether any action was taken
     */
    private boolean autoComplete() {
        JTextComponent editorComponent = getEditorComponent();
        if (editorComponent.getSelectionEnd() > editorComponent.getSelectionStart()) {
            editorComponent.setCaretPosition(editorComponent.getSelectionEnd());
            return true;
        }
        return false;
    }
    private int previousMatch = -1;

    /**
     * Tries to find an item that matches the given text
     * @param text Text to find match for
     * @return Index of matching item, guaranteed to be a String; -1 if no match
     */
    private synchronized int getMatchingItem(String text) {
        if (null != text && !text.isEmpty()) {
            // Little optimization: try previous match first, in many cases
            // it will match and there's need to iterate over all items 
            if (previousMatch >= 0
                    && ((String) getItemAt(previousMatch)).toLowerCase().startsWith(text.toLowerCase())) {
                return previousMatch;
            }

            for (int i = 0; i < getItemCount(); i++) {
                if (getItemAt(i) instanceof String) {
                    if (((String) getItemAt(i)).regionMatches(true, 0, text, 0, text.length())) {
//                    if (((String) getItemAt(i)).toLowerCase()
//                            .startsWith(text.toLowerCase())) {
                        previousMatch = i;
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    // EDITOR MANIPULATION & ACCESS HELPER METHODS
    /**
     * Creates a sanitized string of the current value(s) of the editor
     * @return
     */
    private String getCurrentValueString() {
        StringTokenizer st = new StringTokenizer(getEditorComponent().getText(), Character.toString(SEPARATOR));
        StringBuilder sb = new StringBuilder(st.countTokens());
        while (st.hasMoreTokens()) {
            String token = st.nextToken().trim();
            if (!token.isEmpty()) {
                sb.append(token);
                if (st.hasMoreTokens()) {
                    sb.append(SEPARATOR);
                }
            }
        }
        return sb.toString();
    }

    /**
     * Gets value of the current (determined by caret position) text
     * @return
     */
    private String getEditorValue() {
        String value = getEditorComponent().getText();
        int lastIndex = value.length();
        String separator = Character.toString(SEPARATOR);

        if (!value.contains(separator)) {
            return value;
        } else {
            int[] startEnd = getEditorCurrentStartEnd();
            return value.substring(Math.min(startEnd[0], lastIndex), Math.min(startEnd[1], lastIndex));
        }
    }

    /**
     * Set the value of the CURRENT editor sub-item (i.e. whatever is between
     * the separators closest to the caret position
     * @param value
     */
    private void setEditorValue(String value) {
        int[] startEnd = getEditorCurrentStartEnd();
        String text = getEditorComponent().getText();
        getEditorComponent().setText(
                text.substring(0, startEnd[0]) // everything before
                .concat(value) // insert value
                .concat(text.substring(startEnd[1]))); // everything after
    }

    /**
     * @return Array containing indices of [start, end] of current editor value
     */
    private int[] getEditorCurrentStartEnd() {

        String value = getEditorComponent().getText();

        if (!targetField.isVocabularyList()) {
            // Single valued vocabulary is treated as single entry
            return new int[]{0, value.length()};
        } else {
            // Find bounds of item of current caret position
            int lastIndex = value.length();
            String separator = Character.toString(SEPARATOR);

            int currentIndex = getEditorIndex();
            int start = 0;

            // Traverse editor value to find current start position
            if (currentIndex > 0) {
                int current = 0;
                do {
                    start = value.indexOf(separator, Math.min(start, lastIndex)) + 1;
                    current++;
                }
                while (current < currentIndex);
            }
            
            // Find first bound after start position
            int end = value.indexOf(separator, Math.min(start, lastIndex));
            if (end <= 0) {
                end = lastIndex;
            }
            return new int[]{start, end};
        }
    }

    /**
     * Gets index for currentValues array that corresponds with current caret position in editor
     * @return
     */
    private int getEditorIndex() {
        String value = getEditorComponent().getText();
        int position = getEditorComponent().getCaretPosition();
        int index = 0;
        for (int i = 0; i < position; i++) {
            if (value.charAt(i) == SEPARATOR) {
                index++;
            }
        }
        return index;
    }

    private JTextComponent getEditorComponent() {
        return ((JTextComponent) this.getEditor().getEditorComponent());
    }
}
