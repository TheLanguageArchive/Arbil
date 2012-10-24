/**
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package nl.mpi.arbil.util;

import java.net.CookieHandler;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 * @author Peter.Withers@mpi.nl
 */
public class ArbilMimeHashQueue extends DefaultMimeHashQueue {

    private static boolean allowCookies = false; // this is a silly place for this and should find a better home, but the cookies are only dissabled for the permissions test in this class
    private static ArbilMimeHashQueue singleInstance = null;

    static synchronized public ArbilMimeHashQueue getSingleInstance() {
//        System.out.println("DefaultMimeHashQueue getSingleInstance");
	if (singleInstance == null) {
	    if (!allowCookies) {
		CookieHandler.setDefault(new ShibCookieHandler());
	    }
	    singleInstance = new ArbilMimeHashQueue();
	    singleInstance.startMimeHashQueueThread();
//            System.out.println("CookieHandler: " + java.net.CookieHandler.class.getResource("/META-INF/MANIFEST.MF"));
//            System.out.println("CookieHandler: " + java.net.CookieHandler.class.getResource("/java/net/CookieHandler.class"));
	}
	return singleInstance;
    }
    
    private ArbilMimeHashQueue(){
	super();
    }
    
    /**
     * @param aAllowCookies the allowCookies to set
     */
    public static void setAllowCookies(boolean aAllowCookies) {
	allowCookies = aAllowCookies;
    }
}
