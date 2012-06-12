/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.arbil.data.service;

import java.net.URI;
import nl.mpi.arbil.data.ArbilDataNodeService;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public interface DataNodeServiceLocator {

    ArbilDataNodeService getDataNodeServiceForUri(URI uri);
}
