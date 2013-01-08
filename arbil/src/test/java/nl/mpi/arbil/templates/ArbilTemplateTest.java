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
package nl.mpi.arbil.templates;

import java.io.File;
import nl.mpi.arbil.userstorage.SessionStorage;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilTemplateTest {

    private final Mockery context = new JUnit4Mockery();
    private SessionStorage sessionStorage;
    private ArbilTemplate instance;

    @Before
    public void setUp() {
	sessionStorage = context.mock(SessionStorage.class);
	instance = new ArbilTemplate(sessionStorage);
    }

    private void readDefaultTemplate() {
	instance.readTemplate(new File("/tmp/Default"), "Default");
    }

    @Test
    public void testReadDefaultTemplate() {
	readDefaultTemplate();
	assertEquals("Default", instance.getTemplateName());
    }

    @Test
    public void testPathIsEditableField() {
	readDefaultTemplate();
	// Node, not field
	assertFalse(instance.pathIsEditableField(".METATRANSCRIPT.Corpus"));
	// Editable field
	assertTrue(instance.pathIsEditableField(".METATRANSCRIPT.Corpus.Name"));
	// Multiple fields
	assertTrue(instance.pathIsEditableField(".METATRANSCRIPT.Session.Resources.MediaFile(x).Description"));
    }

    @Test
    public void testPathIsDeletableField() {
	readDefaultTemplate();
	// Node, not field
	assertFalse(instance.pathIsDeleteableField(".METATRANSCRIPT.Corpus"));
	// Non-deletable field
	assertFalse(instance.pathIsDeleteableField(".METATRANSCRIPT.Corpus.Name"));
	// Deletable field
	assertTrue(instance.pathIsDeleteableField(".METATRANSCRIPT.Session.Resources.MediaFile(x).Description"));
    }

    @Test
    public void testGetInsertBeforeOfTemplate() {
	readDefaultTemplate();
	{
	    // Session description should come before MDGroup
	    final String result = instance.getInsertBeforeOfTemplate(".METATRANSCRIPT.Session.Description");
	    assertEquals("MDGroup", result);
	}
	{
	    // Media file description should come before keys
	    final String result = instance.getInsertBeforeOfTemplate(".METATRANSCRIPT.Session.Resources.MediaFile(1).Description");
	    assertEquals("Keys", result);
	}
    }

    @Test
    public void testGetParentOfField() {
	readDefaultTemplate();
	{
	    // Session description goes into root node
	    final String result = instance.getParentOfField(".METATRANSCRIPT.Session.Description");
	    assertEquals("", result);
	}
	{
	    // Media file description should go into same media file
	    final String result = instance.getParentOfField(".METATRANSCRIPT.Session.Resources.MediaFile(1).Description");
	    assertEquals(".METATRANSCRIPT.Session.Resources.MediaFile(1)", result);
	}
	{
	    // Truncating of Keys element into Source
	    final String result = instance.getParentOfField(".METATRANSCRIPT.Session.Resources.Source(2).Keys.Key");
	    assertEquals(".METATRANSCRIPT.Session.Resources.Source(2)", result);
	}
    }

    @Test
    public void testGetMaxOccursForTemplate() {
	readDefaultTemplate();
	{
	    // Any number of descriptions
	    final int result = instance.getMaxOccursForTemplate(".METATRANSCRIPT.Session.Description");
	    assertEquals(-1, result);
	}
	{
	    // Any number of actors
	    final int result = instance.getMaxOccursForTemplate(".METATRANSCRIPT.Session.MDGroup.Actors.Actor");
	    assertEquals(-1, result);
	}
	{
	    // Only one project node
	    final int result = instance.getMaxOccursForTemplate(".METATRANSCRIPT.Session.MDGroup.Project");
	    assertEquals(1, result);
	}
    }

    @Test
    public void testGetHelpStringForField() {
	readDefaultTemplate();
	{
	    // Comes directly from template file
	    final String result = instance.getHelpStringForField(".METATRANSCRIPT.Session.Description");
	    assertEquals("The general desciption of this session", result);
	}
    }

    @Test
    public void testPathIsChildNode() {
	readDefaultTemplate();
	{
	    final String result = instance.pathIsChildNode(".METATRANSCRIPT.Session.MDGroup.Project");
	    assertEquals("Project", result);
	}
	{
	    final String result = instance.pathIsChildNode(".METATRANSCRIPT.Catalogue.SubjectLanguages.Language");
	    assertEquals("SubjectLanguages", result);
	}
    }
}
