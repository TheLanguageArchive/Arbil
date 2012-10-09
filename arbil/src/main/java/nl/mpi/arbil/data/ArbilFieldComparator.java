package nl.mpi.arbil.data;

import java.util.Comparator;

/**
 * @author Peter Withers <peter.withers@mpi.nl>
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilFieldComparator implements Comparator<ArbilField>, java.io.Serializable {
// NOTE: Comparators without state can be Serializable, makes them more useful.

    public int compare(ArbilField firstColumn, ArbilField secondColumn) {
        try {
            int baseIntA = ((ArbilField) firstColumn).getFieldOrder();
            int comparedIntA = ((ArbilField) secondColumn).getFieldOrder();
            int returnValue = baseIntA - comparedIntA;
            if (returnValue == 0) {
                // if the xml node order is the same then also sort on the strings
                String baseStrA = firstColumn.getFieldValue();
                String comparedStrA = secondColumn.getFieldValue();
                returnValue = baseStrA.compareToIgnoreCase(comparedStrA);
            }
            return returnValue;
        } catch (Exception ex) {
            //bugCatcher.logError(ex);
            return 1;
        }
    }
}
