package nl.mpi.arbil.search;

import nl.mpi.arbil.ArbilTest;
import nl.mpi.arbil.util.TreeHelper;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilSearchTest extends ArbilTest{
    
    @Before
    public void setUp() throws Exception{
	inject();
	addToLocalTreeFromResource("/nl/mpi/arbil/data/testfiles/test_session_1.imdi");
    }
    
    @Test
    public void testSearch(){
    }

    @Override
    protected TreeHelper newTreeHelper() {
	return super.newTreeHelper();
    }
}
