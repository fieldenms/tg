package ua.com.fielden.platform.client.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test case to ensure that application properties are store and retrieved correctly.
 *
 * @author TG Team
 *
 */
public class AppConfigTest {
    private final String path = "src/test/resources/file.properties";

    @After
    @Before
    public void removePropertyFile() {
	final File file = new File(path);
	if (file.exists()) {
	   file.delete();
	}
    }

    @Test
    @Ignore // this test sometimes fails under Windows for no apparent reason
    public void test_saving_to_a_new_file() throws Exception {
	// save properties
	AppSessionController config = new AppSessionController(path, null);
	assertNull("Property should not have been set yet.", config.getUsername());
	assertNull("Property should not have been set yet.", config.getPrivateKey());
	config.persist("username", "some private key");
	// load properties and assert
	config = new AppSessionController(path, null);
	assertTrue("Missing username.", !StringUtils.isEmpty(config.getUsername()));
	assertEquals("Incorrect username.", "username", config.getUsername());
	assertTrue("Missing private key.", !StringUtils.isEmpty(config.getPrivateKey()));
	assertEquals("Incorrect private key.", "some private key", config.getPrivateKey());
    }

    @Test
    public void test_chaning_properties() throws Exception {
	// save properties
	AppSessionController config = new AppSessionController(path, null);
	config.persist("username", "some private key");
	// load properties and assert
	config = new AppSessionController(path, null);
	config.persist("another username", "some other private key");

	assertEquals("Incorrect username.", "another username", config.getUsername());
	assertEquals("Incorrect private key.", "some other private key", config.getPrivateKey());
    }

}
