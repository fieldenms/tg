package ua.com.fielden.platform.utils;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.utils.ResourceLoader.getText;

import org.junit.Test;

public class ResourceLoaderTest {

    @Test
    public void testWhetherGetTextWorks() {
	assertEquals("The get text from ResourceLoader class doesn't work!", "<h1>Test Resources</h1>", getText("testResource.html"));
    }

    @Test
    public void testWhetherGetTextFails() {
	assertEquals("The getText from ResourceLoader class didn't failed to found resource", null, getText("notExistingResource.html"));
    }
}
