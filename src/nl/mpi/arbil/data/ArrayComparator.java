package nl.mpi.arbil.data;

import java.util.Comparator;

/**
 * Compares two arrays by comparing their first elements
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArrayComparator<T> implements Comparator<T[]> {

    private final Comparator<T> itemComparator;
    private final int compareIndex;

    public ArrayComparator(Comparator<T> itemComparator, int compareIndex) {
        this.itemComparator = itemComparator;
        this.compareIndex = compareIndex;
    }

    public int compare(T[] o1, T[] o2) {
        // Check if both arrays are long enough
        if (o1.length >= compareIndex && o2.length >= compareIndex) {
            // Call comparator for array elements
            return itemComparator.compare(o1[compareIndex], o2[compareIndex]);
        } else {
            return 0;
        }
    }
}
