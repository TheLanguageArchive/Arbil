/**
 * Copyright (C) 2014 The Language Archive, Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.arbil.clarin.profiles;

import java.io.File;
import java.net.URISyntaxException;
import nl.mpi.arbil.data.ArbilEntityResolver;
import nl.mpi.arbil.userstorage.SessionStorage;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class CmdiTemplateTest {

    private static final Mockery context = new JUnit4Mockery();
    private static CmdiTemplate instance;

    @BeforeClass
    public static void setUpClass() throws Exception {
	SessionStorage sessionStorage = context.mock(SessionStorage.class);
	instance = new CmdiTemplate(sessionStorage, false); // false -> most tests assume non-verbatim mode

	readTestProfile(instance, context, sessionStorage);
    }

    private static void readTestProfile(CmdiTemplate template, Mockery context, final SessionStorage sessionStorage) throws URISyntaxException {
	final String testProfileUri = CmdiTemplateTest.class.getResource("/nl/mpi/arbil/data/Example_Profile_Instance.xsd").toString();

	final CmdiProfileProvider profileReader = context.mock(CmdiProfileProvider.class);
	context.checking(new Expectations() {
	    {
		oneOf(profileReader).getProfile(testProfileUri);
		will(returnValue(null));
	    }
	});

	ArbilEntityResolver.setSessionStorage(sessionStorage);
	context.checking(new Expectations() {
	    {
		oneOf(sessionStorage).updateCache("http://www.w3.org/2001/xml.xsd", ArbilEntityResolver.EXPIRE_CACHE_DAYS, false);
		will(returnValue(new File(getClass().getResource("/nl/mpi/arbil/resources/xsd/http/www.w3.org/2001/xml.xsd").toURI())));
	    }
	});

	template.loadTemplate(testProfileUri, profileReader);
    }

    @Test
    public void testPathIsEditableField() {
	// Node, not field
	assertFalse(instance.pathIsEditableField(".CMD.Components.Example_Profile_Instance"));
	// Editable field
	assertTrue(instance.pathIsEditableField(".CMD.Components.Example_Profile_Instance.example-component-actor.title"));

	// Attribute fields
	assertFalse(instance.pathIsEditableField(".CMD.Components.Example_Profile_Instance.example-component-actor.title.@test"));
	// Reserved attribute
	assertFalse(instance.pathIsEditableField(".CMD.Components.Example_Profile_Instance.example-component-actor.title.@ref"));
    }

    @Test
    public void testPathIsDeletableField() {
	// Node, not field
	assertFalse(instance.pathIsDeleteableField(".CMD.Components.Example_Profile_Instance.example-component-actor.firstname"));
	// Deletable field with min occurences = 1
	assertTrue(instance.pathIsDeleteableField(".CMD.Components.Example_Profile_Instance.example-component-actor.title"));
	// Deletable field with min occurences = 0
	assertTrue(instance.pathIsDeleteableField(".CMD.Components.Example_Profile_Instance.example-component-text.example-component-texttype.TextTypeDescription"));
    }

    @Test
    public void testGetInsertBeforeOfTemplate() {
	{
	    // Optional title should come before first name
	    final String result = instance.getInsertBeforeOfTemplate(".CMD.Components.Example_Profile_Instance.example-component-actor.title");
	    assertEquals("firstName,lastName,sex,age,ActorLanguage", result);
	}
	{
	    // With index should work
	    final String result = instance.getInsertBeforeOfTemplate(".CMD.Components.Example_Profile_Instance.example-component-actor(1).title");
	    assertEquals("firstName,lastName,sex,age,ActorLanguage", result);
	}
	{
	    // No insert before info for mandatory fields, e.g. first name
	    final String result = instance.getInsertBeforeOfTemplate(".CMD.Components.Example_Profile_Instance.example-component-actor.firstName");
	    assertEquals("", result);
	}
    }

    @Test
    public void testGetParentOfField() {
	{
	    // text component gets merged with root
	    final String result = instance.getParentOfField(".CMD.Components.Example_Profile_Instance.example-component-text.Format");
	    assertEquals("", result);
	}
	{
	    // so does actor
	    final String result = instance.getParentOfField(".CMD.Components.Example_Profile_Instance.example-component-actor.title");
	    assertEquals("", result);
	}
	{
	    // languages has unbounded max so becomes node of its own
	    final String result = instance.getParentOfField(".CMD.Components.Example_Profile_Instance.example-component-actor.ActorLanguage.ActorLanguageName");
	    assertEquals(".CMD.Components.Example_Profile_Instance.example-component-actor.ActorLanguage", result);
	}
    }

    @Test
    public void testGetMaxOccursForTemplate() {
	{
	    // titles: 1-unbounded
	    final int result = instance.getMaxOccursForTemplate(".CMD.Components.Example_Profile_Instance.example-component-actor.title");
	    assertEquals(-1, result);
	}
	{
	    // titles: 1-unbounded (with indices)
	    final int result = instance.getMaxOccursForTemplate(".CMD.Components.Example_Profile_Instance.example-component-actor(1).title");
	    assertEquals(-1, result);
	}
	{
	    // format: 0-1
	    final int result = instance.getMaxOccursForTemplate(".CMD.Components.Example_Profile_Instance.example-component-text.Format");
	    assertEquals(1, result);
	}
	{
	    // format: 0-1 (with indices)
	    final int result = instance.getMaxOccursForTemplate(".CMD.Components.Example_Profile_Instance.example-component-text(1).Format(1)");
	    assertEquals(1, result);
	}
	{
	    // ActorLanguage: 0-unbounded
	    final int result = instance.getMaxOccursForTemplate(".CMD.Components.Example_Profile_Instance.example-component-actor.ActorLanguage");
	    assertEquals(-1, result);
	}
	{
	    // Take care! Mandatory fields will not be included in the template array and therefore always return -1
	    // Example of this: firstName 1-1
	    final int result = instance.getMaxOccursForTemplate(".CMD.Components.Example_Profile_Instance.example-component-actor.firstName");
	    assertEquals(-1, result);
	}
    }

    @Test
    public void testGetHelpStringForField() {
	{
	    // Comes directly from profile schema
	    final String result = instance.getHelpStringForField(".CMD.Components.Example_Profile_Instance.example-component-actor.firstName");
	    assertEquals("This is the firstname of a person", result);
	}
    }

    @Test
    public void testPathIsChildNode() {
	{
	    // this should always be a child node
	    final String result = instance.pathIsChildNode(".CMD.Components.Example_Profile_Instance.example-component-actor.ActorLanguage");
	    assertEquals("example-component-actor", result);
	}
	{
	    // this should be a child node in verbatim mode (so not in this case!)
	    final String result = instance.pathIsChildNode(".CMD.Components.Example_Profile_Instance.example-component-actor");
	    assertNull(result);
	}
	{
	    // this should never be a child node
	    final String result = instance.pathIsChildNode(".CMD.Components.Example_Profile_Instance.example-component-actor.title");
	    assertNull(result);
	}
    }

    @Test
    public void testPathIsChildNodeXmlVerbatim() throws Exception {
	// testing in verbatim XML mode, cannot share static instance of CmdiTemplate so some code duplication is required here..
	final Mockery xmlVerbatimContext = new JUnit4Mockery();
	final SessionStorage xmlVerbatimSessionStorage = xmlVerbatimContext.mock(SessionStorage.class);
	final CmdiTemplate xmlVerbatimInstance = new CmdiTemplate(xmlVerbatimSessionStorage, true);
	readTestProfile(xmlVerbatimInstance, xmlVerbatimContext, xmlVerbatimSessionStorage);

	{
	    // this should always be a child node
	    final String result = xmlVerbatimInstance.pathIsChildNode(".CMD.Components.Example_Profile_Instance.example-component-actor.ActorLanguage");
	    assertEquals("example-component-actor", result);
	}
	{
	    // this should be a child node in verbatim mode
	    final String result = xmlVerbatimInstance.pathIsChildNode(".CMD.Components.Example_Profile_Instance.example-component-actor");
	    assertEquals("Example_Profile_Instance", result);
	}
	{
	    // this should never be a child node
	    final String result = xmlVerbatimInstance.pathIsChildNode(".CMD.Components.Example_Profile_Instance.example-component-actor.title");
	    assertNull(result);
	}
    }
}
