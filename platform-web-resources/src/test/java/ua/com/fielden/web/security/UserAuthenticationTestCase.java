package ua.com.fielden.web.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.restlet.Restlet;
import org.restlet.Router;

import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.ClientAuthenticationModel;
import ua.com.fielden.platform.security.provider.IUserController;
import ua.com.fielden.platform.security.user.IAuthenticationModel;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test.DbDrivenTestCase2;
import ua.com.fielden.platform.web.UserAuthResourceFactory;
import ua.com.fielden.platform.web.test.WebBasedTestCase;

/**
 * Provides a unit test for user authentication process.
 *
 * @author TG Team
 *
 */
public class UserAuthenticationTestCase extends WebBasedTestCase {
    private static final String authenticationUri = "/login";
    public static final String appWidePrivateKey = "30820154020100300D06092A864886F70D01010105000482013E3082013A020100024100A6011E18C0C863215BB57F18EFA4B6FFA5B1BA2E3711BC682C7645972460D71F85504D50DE43A36601903CE5400AB0243C39D68434938F16AC5E1C58DD08B6670203010001024036EC39BBF0D49BCFA69B07010610962740F7EB646CFDE63B0532E0556048D6036401AC48175BACDC32D5639915C6F41640FB21EB1D005D09C4694FA99EDC0521022100DD8421FE3095C62B4F38EE7B3845FED13B6124C6BF65965A389412572629F691022100BFD8BA5978AEBF410D59AC9FD3FC111286FCB791CF6325C6BE04020FCE1C0977022100CA629FC022D9A47E0B5AA3A0D6D034B92B7C5CE26D1A3E9D5D96038FB6219ED102200D9A6984449967784E61086B80D583C4638BF1DC45EF4AD36FCFCAF1A1F4BCFB022065E1253643375F7704A405FA2C8FEF4E5C43D9CA67D635FC02DB191ADF291FF1";
    public static final String appWidePublicKey = "305C300D06092A864886F70D0101010500034B003048024100A6011E18C0C863215BB57F18EFA4B6FFA5B1BA2E3711BC682C7645972460D71F85504D50DE43A36601903CE5400AB0243C39D68434938F16AC5E1C58DD08B6670203010001";

    private static UserControllerForTestPurposes controller = new UserControllerForTestPurposes();

    @Test
    public void test_user_athentication_with_correct_cedentials() {
	final IAuthenticationModel auth = new ClientAuthenticationModel(authenticationUri, 512, config.restClientUtil(), appWidePrivateKey);
	final Result result = auth.authenticate(UserControllerForTestPurposes.USER_NAME, UserControllerForTestPurposes.PASSWORD);
	assertNotNull("User authentication result should be present.", result);
	assertTrue("A successful user authentication result is expected.", result.isSuccessful());
	assertNotNull("User specific private key should have been associated with client REST util.", config.restClientUtil().getPrivateKey());
	assertEquals("Username should have been associated with client REST util.", UserControllerForTestPurposes.USER_NAME, config.restClientUtil().getUsername());
	assertNotNull("User should have been associated with REST client utility.", config.restClientUtil().getUser());
	assertEquals("A correct user should have been associated with client REST utility.", UserControllerForTestPurposes.USER_NAME, config.restClientUtil().getUser().getKey());

	final User user = (User) result.getInstance();
	assertNotNull("User was not provided.", user);
	assertEquals("Incorrect username in the returned user entity instance.", "user", user.getKey());
	assertNotNull("User must have a public key.", user.getPublicKey());
    }

    @Test
    public void test_user_athentication_with_incorrect_username() {
	final IAuthenticationModel auth = new ClientAuthenticationModel(authenticationUri, 512, config.restClientUtil(), appWidePrivateKey);

	final Result result = auth.authenticate("incorrect user", UserControllerForTestPurposes.PASSWORD);
	assertNotNull("User authentication result should be present.", result);
	assertFalse("An unsuccessful user authentication result is expected.", result.isSuccessful());
	assertNull("User should have been associated with REST client utility.", config.restClientUtil().getUser());

	final User user = (User) result.getInstance();
	assertNull("User instance should not be provided.", user);
    }

    @Test
    public void test_user_athentication_with_incorrect_password() {
	final IAuthenticationModel auth = new ClientAuthenticationModel(authenticationUri, 512, config.restClientUtil(), appWidePrivateKey);

	final Result result = auth.authenticate(UserControllerForTestPurposes.USER_NAME, "incorrect password");
	assertNotNull("User authentication result should be present.", result);
	assertFalse("An unsuccessful user authentication result is expected.", result.isSuccessful());
	assertNull("User should not have been associated with REST client utility.", config.restClientUtil().getUser());


	final User user = (User) result.getInstance();
	assertNull("User instance should not be provided.", user);
    }

    @Override
    public synchronized Restlet getRoot() {
	final Router router = new Router(getContext());

	config.restServerUtil().setAppWidePrivateKey(appWidePrivateKey);
	config.restServerUtil().setAppWidePublicKey(appWidePublicKey);
	router.attach(authenticationUri, new UserAuthResourceFactory(DbDrivenTestCase2.injector, config.restServerUtil()) {
	    @Override
	    protected IUserController getController() {
		return controller;
	    }
	});
	return router;
    }

    @Override
    public void setUp() {
	super.setUp();
	try {
	    controller.initUser(config.entityFactory());
	    config.restClientUtil().setUserController(controller);
	    config.restClientUtil().setUsername(UserControllerForTestPurposes.USER_NAME);
	} catch (final Exception e) {
	    e.printStackTrace();
	    throw new IllegalStateException("Setup failed -- cannot proceed with the test.");
	}
    }

    @Override
    protected String[] getDataSetPaths() {
	return new String[] { "src/test/resources/data-files/web-test-case.flat.xml" };
    }

}
