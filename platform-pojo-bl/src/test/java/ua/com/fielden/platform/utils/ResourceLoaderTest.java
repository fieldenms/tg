package ua.com.fielden.platform.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.utils.ResourceLoader.exist;
import static ua.com.fielden.platform.utils.ResourceLoader.getStream;
import static ua.com.fielden.platform.utils.ResourceLoader.getText;

import org.junit.Test;

public class ResourceLoaderTest {

    @Test
    public void testWhetherGetTextWorks() {
	assertEquals("The get text from ResourceLoader class doesn't work!", "<h1>Test Resources</h1>", getText("testResource.html"));
    }

    @Test
    public void testWhetherGetTextFails() {
	assertNull("The getText from ResourceLoader class didn't failed to found resource", getText("notExistingResource.html"));
    }

    @Test
    public void testWhetherExistFindsResource() {
	assertTrue("The exist method didn't resource", exist("testResource.html"));
    }

    @Test
    public void testWhetherExistFailsToFindResource() {
	assertFalse("The exist method found non existing resource", exist("notExistingResource.html"));
    }

    @Test
    public void testWhetherGetStreamWorks() {
	assertNotNull("The getStream from ResourceLoader class doesn't work!",  getStream("testResource.html"));
    }

    @Test
    public void testWhetherGetStreamFails() {
	assertNull("The getStream from ResourceLoader class didn't failed to found resource", getStream("notExistingResource.html"));
    }
}
