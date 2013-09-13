/*
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
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
package nl.mpi.arbil.userstorage;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;

import static org.jmock.Expectations.returnValue;
import static org.junit.Assert.*;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilConfigurationManagerTest {

    private final Mockery context = new JUnit4Mockery();
    private SessionStorage sessionStorage;
    private ArbilConfigurationManager instance;

    @Before
    public void setUp() {
	sessionStorage = context.mock(SessionStorage.class);
	instance = new ArbilConfigurationManager(sessionStorage);
    }

    /**
     * Test of read method, of class ArbilConfigurationManager.
     */
    @Test
    public void testRead() {
	// read config set 1
	context.checking(new Expectations() {
	    {
		oneOf(sessionStorage).loadBoolean(ArbilConfigurationManager.VERBATIM_XML_TREE_STRUCTURE, false);
		will(returnValue(true));
	    }
	});
	ArbilConfiguration result = instance.read();
	assertTrue(result.isVerbatimXmlTreeStructure());

	// read config set 2
	context.checking(new Expectations() {
	    {
		oneOf(sessionStorage).loadBoolean(ArbilConfigurationManager.VERBATIM_XML_TREE_STRUCTURE, false);
		will(returnValue(false));
	    }
	});
	result = instance.read();
	assertFalse(result.isVerbatimXmlTreeStructure());
    }

    /**
     * Test of write method, of class ArbilConfigurationManager.
     */
    @Test
    public void testWrite() {
	final ArbilConfiguration config = new ArbilConfiguration();

	// write config set 1
	config.setVerbatimXmlTreeStructure(true);
	context.checking(new Expectations() {
	    {
		oneOf(sessionStorage).saveBoolean(ArbilConfigurationManager.VERBATIM_XML_TREE_STRUCTURE, true);
	    }
	});
	instance.write(config);

	// write config set 2
	config.setVerbatimXmlTreeStructure(false);
	context.checking(new Expectations() {
	    {
		oneOf(sessionStorage).saveBoolean(ArbilConfigurationManager.VERBATIM_XML_TREE_STRUCTURE, false);
	    }
	});
	instance.write(config);
    }
}