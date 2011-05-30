
package nl.mpi.arbil.data;

import java.util.List;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface ArbilVocabularyFilter {

    /**
     *
     * @param items List of vocabulary items to filter
     * @return Filtered version of provided item list
     */
    List<ArbilVocabularyItem> filterVocabularyItems(List<ArbilVocabularyItem> items);
}
