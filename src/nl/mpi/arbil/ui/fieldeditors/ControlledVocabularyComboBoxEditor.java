/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.arbil.ui.fieldeditors;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.StringTokenizer;
import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.Timer;
import nl.mpi.arbil.data.ArbilField;
import nl.mpi.arbil.data.ArbilVocabulary;

/**
 * Text editor intended for use with ControlledVocabularyComboBox
 * It has typeahead and can deal with open and closed vocabularies, and both
 * single valued vocabularies and lists.
 *
 * @see ControlledVocabularyComboBox
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ControlledVocabularyComboBoxEditor extends ArbilFieldEditor implements ComboBoxEditor, KeyListener, FocusListener {

    public ControlledVocabularyComboBoxEditor(String initialValue, ArbilField arbilField, JComboBox comboBox) {
        super(initialValue);
        this.initialValue = initialValue;

        if (comboBox != null) {
            setComboBox(comboBox);
            // Also set combobox item to initial value because it will feed its selected item
            // back into the setItem method of this object
            comboBox.setSelectedItem(initialValue);
        }

        this.targetField = arbilField;
        this.vocabulary = arbilField.getVocabulary();

        addKeyListener(this);
        addFocusListener(this);

        initTypeaheadTimer();
    }

    /**
     * Implements getEditorComponent
     */
    public Component getEditorComponent() {
        return this;
    }

    /**
     * Implements ComboBoxEditor setItem
     * @param item
     */
    public void setItem(Object item) {
        if (!typingAhead) {
            String itemString = item.toString();

            // Decide what to do, depending on whether there are multiple values
            if (itemString.indexOf(SEPARATOR) >= 0) {
                // Set item value for entire text
                setText(itemString);
            } else {
                // Set only value currently being edited
                setEditorValue(itemString);
            }
        }
    }

    /**
     * Implements ComboBoxEditor getItem
     * @param anObject
     */
    public Object getItem() {
        return getText();
    }

    /**
     * Local convenience method. Gets item from vocabulary
     * @param index
     * @return
     */
    private String getItemAt(int index) {
        return vocabulary.getVocabularyItems().get(index).itemDisplayName;
    }

    private int getItemsCount() {
        return vocabulary.getVocabularyItems().size();
    }

    public final void setComboBox(JComboBox comboBox) {
        this.comboBox = comboBox;
        comboBox.setEditable(true);
    }

    public String getCurrentValue() {
        return getCurrentValueString();
    }

    // FOCUS LISTENERS
    public void focusGained(FocusEvent e) {
        startTypeaheadTimer(TYPEAHEAD_DELAY_SHORT);
    }

    public void focusLost(FocusEvent e) {
    }

    // EDITOR KEY LISTENERS
    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        if (!comboBox.isPopupVisible()) {
            comboBox.setPopupVisible(true);
        } else {
            if (e.getKeyCode() == KeyEvent.VK_ENTER
                    || (targetField.isVocabularyList() && e.getKeyChar() == SEPARATOR)) {
                // ENTER pressed or SEPARATOR in list field.
                // Autocomplete current item
                handleAutocompleteKey(e);
            } else if (e.isActionKey()) {
                // Navigate combo items
                handleNavigateComboKey(e);
            } else {
                // Probably text entry
                handleTextEntryKey(e);
            }
        }
    }

    private void handleTextEntryKey(KeyEvent e) {
        if (!typingAhead) {
            if (!e.isActionKey()
                    && e.getKeyChar() != KeyEvent.CHAR_UNDEFINED
                    && e.getKeyCode() != KeyEvent.VK_BACK_SPACE
                    && e.getKeyCode() != KeyEvent.VK_DELETE) {
                // Text is being typed. Start (or restart) timer, so that typeahead
                // is executed after last keystroke within delay
                startTypeaheadTimer(TYPEAHEAD_DELAY_SHORT);
            } else if (!targetField.isVocabularyOpen()
                    && (e.getKeyCode() == KeyEvent.VK_BACK_SPACE
                    || e.getKeyCode() == KeyEvent.VK_DELETE)) {
                // In closed list, also autocomplete on backspace/delete but use
                // a longer delay so as not to make it impossible to remove characters
                // or even submit the (non-existent) entry
                startTypeaheadTimer(TYPEAHEAD_DELAY_LONG);
            }
        }
    }

    private void handleNavigateComboKey(KeyEvent e) {
        if (e.getModifiers() == 0) {
            if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                moveSelectedIndex(+1);
                e.consume();
            } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                moveSelectedIndex(-1);
                e.consume();
            } else if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
                moveSelectedIndex(+5);
                e.consume();
            } else if (e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
                moveSelectedIndex(-5);
                e.consume();
            }
        }
    }

    private synchronized void handleAutocompleteKey(KeyEvent e) {
        typingAhead = true;

        if (typeaheadTimer.isRunning()) {
            typeaheadTimer.stop();
        }

        if (autoComplete()) {
            // Completed current item, do not perform any more actions
            // on this key event
            e.consume();
        }
        typingAhead = false;
    }

    // TYPE-AHEAD AND AUTO-COMPLETE METHODS
    /**
     * Types ahead current item. If a match is found, remaining part of the
     * target string is selected.
     */
    private synchronized void typeAhead() {
        typingAhead = true;
        String currentEditorValue = getEditorValue();
        int matchIndex = getMatchingItem(currentEditorValue);
        if (matchIndex >= 0) {
            // Match found
            String match = getItemAt(matchIndex);
            if (comboBox != null) {
                // Make combobox jump to the selected item
                comboBox.setSelectedItem(match);
            }

            int position = getCaretPosition();
            // Insert match into editor
            setEditorValue(match);
            // Set caret back to original position
            setCaretPosition(position);
            // Select remaining part of match
            setSelectionStart(position);
            setSelectionEnd(position + match.length() - currentEditorValue.length());
        }
        typingAhead = false;
    }

    /**
     * Performs auto complete on current item. In effect, current selection is
     * skipped. If nothing is selected, nothing is skipped and false is returned
     *
     * @return Whether any action was taken
     */
    private boolean autoComplete() {

        if (getSelectionEnd() > getSelectionStart()) {
            setCaretPosition(getSelectionEnd());
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
    private int getMatchingItem(String text) {
        if (null != text && !text.isEmpty()) {
            int itemsCount = getItemsCount();

            // Try previous match first, in many cases it will match and there's
            // no need to iterate over all items
            if (previousMatch >= 0
                    && previousMatch < itemsCount
                    && ((String) getItemAt(previousMatch)).toLowerCase().startsWith(text.toLowerCase())) {
                return previousMatch;
            }

            for (int i = 0; i < itemsCount; i++) {
                String item = getItemAt(i);
                if (item instanceof String) {
                    if (item.regionMatches(true, 0, text, 0, text.length())) {
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
        StringTokenizer st = new StringTokenizer(getText(), Character.toString(SEPARATOR));
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
        String value = getText();
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
        String text = getText();
        setText(
                text.substring(0, startEnd[0]) // everything before
                .concat(value) // insert value
                .concat(text.substring(startEnd[1]))); // everything after
        setCaretPosition(startEnd[0] + value.length());

    }

    /**
     * @return Array containing indices of [start, end] of current editor value
     */
    private int[] getEditorCurrentStartEnd() {

        String value = getText();

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
                } while (current < currentIndex);
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
        String value = getText();
        int position = getCaretPosition();
        int index = 0;
        for (int i = 0; i < position; i++) {
            if (value.charAt(i) == SEPARATOR) {
                index++;
            }
        }
        return index;
    }

    private void moveSelectedIndex(int delta) {
        int target = comboBox.getSelectedIndex() + delta;
        // Don't move up to the first item, as it contains the previous value of the field
        target = Math.max(target, 0);
        // Don't try to mobe below final item
        target = Math.min(target, comboBox.getItemCount() - 1);

        // Target should be in list and not equal to current target
        if (target >= 0 && target != comboBox.getSelectedIndex()) {
            // Target should not be multi-valued
            if (comboBox.getItemAt(target).toString().indexOf(SEPARATOR) >= 0) {
                target = Math.min(target + 1, comboBox.getItemCount() - 1);
            }
            comboBox.setSelectedIndex(target);
        }
    }

    private void startTypeaheadTimer(int delay) {
        if (typeaheadTimer.isRunning()) {
            typeaheadTimer.stop();
        }
        //typeaheadTimer.setDelay(delay);
        //typeaheadTimer.restart();
        typeaheadTimer.setInitialDelay(delay);
        typeaheadTimer.start();

    }

    private void initTypeaheadTimer() {
        typeaheadTimer = new Timer(TYPEAHEAD_DELAY_SHORT, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (!typingAhead) {
                    typeaheadTimer.stop();
                    typeAhead();
                }
            }
        });
        typeaheadTimer.setRepeats(false);
    }
    // Private members
    private String initialValue;
    private JComboBox comboBox;
    private ArbilVocabulary vocabulary;
    private ArbilField targetField;
    private Timer typeaheadTimer;
    /**
     * Character that separates items in a list-type field
     */
    private final static char SEPARATOR = ',';
    /**
     * Flag indicating whether type ahead is in process
     */
    private boolean typingAhead = false;
    /**
     * Response rate limit in milliseconds
     */
    private final static int TYPEAHEAD_DELAY_SHORT = 200;
    private final static int TYPEAHEAD_DELAY_LONG = 1000;
}
