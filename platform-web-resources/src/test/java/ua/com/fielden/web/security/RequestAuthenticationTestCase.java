package ua.com.fielden.web.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;

import org.junit.Test;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.ClientAuthenticationModel;
import ua.com.fielden.platform.security.provider.IUserController;
import ua.com.fielden.platform.security.user.IAuthenticationModel;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.test.DbDrivenTestCase;
import ua.com.fielden.platform.web.factories.EntityInstanceResourceFactory;
import ua.com.fielden.platform.web.factories.ResourceGuard;
import ua.com.fielden.platform.web.factories.UserAuthResourceFactory;
import ua.com.fielden.platform.web.resources.UserAuthResource;
import ua.com.fielden.platform.web.test.WebBasedTestCase;
import ua.com.fielden.web.entities.IInspectedEntityDao;
import ua.com.fielden.web.entities.InspectedEntity;
import ua.com.fielden.web.rao.InspectedEntityRao;

/**
 * Provides a unit test for request authentication process.
 * 
 * @author TG Team
 * 
 */
public class RequestAuthenticationTestCase extends WebBasedTestCase {
    private static final String authenticationUri = "/login";
    public static final String appWidePrivateKey = "30820154020100300D06092A864886F70D01010105000482013E3082013A020100024100A6011E18C0C863215BB57F18EFA4B6FFA5B1BA2E3711BC682C7645972460D71F85504D50DE43A36601903CE5400AB0243C39D68434938F16AC5E1C58DD08B6670203010001024036EC39BBF0D49BCFA69B07010610962740F7EB646CFDE63B0532E0556048D6036401AC48175BACDC32D5639915C6F41640FB21EB1D005D09C4694FA99EDC0521022100DD8421FE3095C62B4F38EE7B3845FED13B6124C6BF65965A389412572629F691022100BFD8BA5978AEBF410D59AC9FD3FC111286FCB791CF6325C6BE04020FCE1C0977022100CA629FC022D9A47E0B5AA3A0D6D034B92B7C5CE26D1A3E9D5D96038FB6219ED102200D9A6984449967784E61086B80D583C4638BF1DC45EF4AD36FCFCAF1A1F4BCFB022065E1253643375F7704A405FA2C8FEF4E5C43D9CA67D635FC02DB191ADF291FF1";
    public static final String appWidePublicKey = "305C300D06092A864886F70D0101010500034B003048024100A6011E18C0C863215BB57F18EFA4B6FFA5B1BA2E3711BC682C7645972460D71F85504D50DE43A36601903CE5400AB0243C39D68434938F16AC5E1C58DD08B6670203010001";

    private static UserControllerForTestPurposes controller = new UserControllerForTestPurposes();
    private final IInspectedEntityDao rao = new InspectedEntityRao(config.restClientUtil());
    private final IInspectedEntityDao dao = DbDrivenTestCase.injector.getInstance(IInspectedEntityDao.class);

    private final fetch<User> fetchModel = fetch(User.class).with("roles", fetch(UserAndRoleAssociation.class).with("userRole"));

    @Test
    public void test_request_athentication_with_correctly_authenticated_user() {
        // since this test does not use any persistent storage for saving public key of the authenticated user, need to authenticate user for each test
        final IAuthenticationModel auth = new ClientAuthenticationModel(authenticationUri, 512, config.restClientUtil(), appWidePrivateKey);
        final Result result = auth.authenticate(UserControllerForTestPurposes.USER_NAME, UserControllerForTestPurposes.PASSWORD);
        assertNotNull("User authentication result should be present.", result);
        assertTrue("A successful user authentication result is expected.", result.isSuccessful());
        assertNotNull("User specific private key should have been associated with client REST util.", config.restClientUtil().getPrivateKey());
        assertEquals("Username should have been associated with client REST util.", UserControllerForTestPurposes.USER_NAME, config.restClientUtil().getUsername());
        assertNotNull("User should have been associated with REST client utility.", config.restClientUtil().getUser());
        assertEquals("A correct user should have been associated with client REST utility.", UserControllerForTestPurposes.USER_NAME, config.restClientUtil().getUser().getKey());

        // this RAO call will actually be validated for authenticity
        assertTrue("Entity should exist.", rao.entityExists(dao.findById(1L)));
    }

    @Test
    public void test_request_athentication_with_no_private_key_and_wrong_user() {
        config.restClientUtil().setPrivateKey(null);
        config.restClientUtil().setUsername("incorrect user");
        // this RAO call will actually be validated for authenticity
        try {
            rao.entityExists(dao.findById(1L));
            fail("There should have been an authentication exception preventing access to the resource.");
        } catch (final Throwable ex) {
        }
    }

    @Test
    public void test_request_athentication_with_no_private_key_and_correct_user() {
        config.restClientUtil().setPrivateKey(null);
        config.restClientUtil().setUsername(UserControllerForTestPurposes.USER_NAME);
        // this RAO call will actually be validated for authenticity
        try {
            rao.entityExists(dao.findById(1L));
            fail("There should have been an authentication exception preventing access to the resource.");
        } catch (final Throwable ex) {
        }
    }

    @Test
    public void test_request_athentication_with_incorrect_user() {
        config.restClientUtil().setPrivateKey(appWidePrivateKey);
        config.restClientUtil().setUsername("incorrect user");
        // this RAO call will actually be validated for authenticity
        try {
            rao.entityExists(dao.findById(1L));
            fail("There should have been an authentication exception preventing access to the resource.");
        } catch (final Throwable ex) {
        }
    }

    @Test
    public void test_request_athentication_with_incorrect_private_key() {
        config.restClientUtil().setPrivateKey(appWidePrivateKey);
        config.restClientUtil().setUsername(UserControllerForTestPurposes.USER_NAME);
        // this RAO call will actually be validated for authenticity
        try {
            rao.entityExists(dao.findById(1L));
            fail("There should have been an authentication exception preventing access to the resource.");
        } catch (final Throwable ex) {
        }
    }

    @Test
    public void test_request_athentication_with_authorised_user_but_incorrect_private_key() {
        // since this test does not use any persistent storage for saving public key of the authenticated user, need to authenticate user for each test
        final IAuthenticationModel auth = new ClientAuthenticationModel(authenticationUri, 512, config.restClientUtil(), appWidePrivateKey);
        final Result result = auth.authenticate(UserControllerForTestPurposes.USER_NAME, UserControllerForTestPurposes.PASSWORD);
        assertNotNull("User authentication result should be present.", result);
        assertTrue("A successful user authentication result is expected.", result.isSuccessful());
        assertNotNull("User specific private key should have been associated with client REST util.", config.restClientUtil().getPrivateKey());
        assertEquals("Username should have been associated with client REST util.", UserControllerForTestPurposes.USER_NAME, config.restClientUtil().getUsername());
        assertNotNull("User should have been associated with REST client utility.", config.restClientUtil().getUser());
        assertEquals("A correct user should have been associated with client REST utility.", UserControllerForTestPurposes.USER_NAME, config.restClientUtil().getUser().getKey());
        // substitute the key
        config.restClientUtil().setPrivateKey(appWidePrivateKey);
        // this RAO call will actually be validated for authenticity
        try {
            rao.entityExists(dao.findById(1L));
            fail("There should have been an authentication exception preventing access to the resource.");
        } catch (final Throwable ex) {
        }
    }

    @Test
    public void test_credential_validation_with_valid_credentials() {
        // set user public key for testing purposes
        controller.findById(1L, fetchModel).setPublicKey(appWidePublicKey);

        final ClientAuthenticationModel auth = new ClientAuthenticationModel(authenticationUri, 512, config.restClientUtil(), appWidePrivateKey);
        final Result result = auth.validateCredentials(UserControllerForTestPurposes.USER_NAME, appWidePrivateKey);

        assertNotNull("Validation result should be present.", result);
        assertTrue("Validation should be successful.", result.isSuccessful());
        assertEquals("Incorrect result message.", UserAuthResource.CREDENTIALS_ARE_VALID, result.getMessage());
    }

    @Test
    public void test_credential_validation_with_reset_publickey() {
        controller.findById(1L, fetchModel).setPublicKey(appWidePublicKey);

        final ClientAuthenticationModel auth = new ClientAuthenticationModel(authenticationUri, 512, config.restClientUtil(), appWidePrivateKey);
        final String privateKey = "30820156020100300D06092A864886F70D0101010500048201403082013C020100024100D548B1B7E5FF10F106FDBE193C828B1C60EF68A0949EDD8842539FEC794738A7A74524F5290692F29A12E20D780BE9AC72EBAD6DFEB46BB0C649390CC570FD4502030100010241008C9DC8CDBDCD5496144DE6AF2CBDC489893F4E73D0888CF5F104C1435F121DBEF670D9ECE40844B1FD253102DDD72C39C32A2A32FE093B834C9556EB21A7CA61022100F69D5999B0D442AEBDC025B8D89525CFDA6F18BF81789D1A012B4BE985378943022100DD669E7BF31E36531CB20490D372B9556B1A56CBBB95CECC911DE7D385E212D7022100F3DB3BD2418C6233AFE5E46717A2C3B4F8658AAF3333350595CA5E52172C874902205695B1594C24FE9DD65510500B418C9589434DBA736C0CEA17BE985132C4CD15022100C21F3D6CE7D8A8C7F746FD6C48DBFB7F00E825479E40B636D6E16BC1B78CBB85";
        final Result result = auth.validateCredentials(UserControllerForTestPurposes.USER_NAME, privateKey);

        assertNotNull("Validation result should be present.", result);
        assertFalse("Validation should not be successful.", result.isSuccessful());
        assertEquals("Incorrect result message.", UserAuthResource.PASSWORD_RESET, result.getMessage());
    }

    @Test
    public void test_credential_validation_with_user_missing_publickey() {
        final ClientAuthenticationModel auth = new ClientAuthenticationModel(authenticationUri, 512, config.restClientUtil(), appWidePrivateKey);
        final Result result = auth.validateCredentials(UserControllerForTestPurposes.USER_NAME, appWidePrivateKey);

        assertNotNull("Validation result should be present.", result);
        assertFalse("Validation should not be successful.", result.isSuccessful());
        assertEquals("Incorrect result message.", UserAuthResource.INVALID_CREDENTIALS, result.getMessage());
    }

    @Test
    public void test_credential_validation_with_another_user_logged_in_from_diff_location() {
        final String anotherUserPublicKey = "205C300D06092A864886F70D0101010500034B003048024100D548B1B7E5FF10F106FDBE193C828B1C60EF68A0949EDD8842539FEC794738A7A74524F5290692F29A12E20D780BE9AC72EBAD6DFEB46BB0C649390CC570FD450203010001";
        controller.findById(1L, fetchModel).setPublicKey(anotherUserPublicKey);

        final ClientAuthenticationModel auth = new ClientAuthenticationModel(authenticationUri, 512, config.restClientUtil(), appWidePrivateKey);
        final String privateKey = "30820156020100300D06092A864886F70D0101010500048201403082013C020100024100D548B1B7E5FF10F106FDBE193C828B1C60EF68A0949EDD8842539FEC794738A7A74524F5290692F29A12E20D780BE9AC72EBAD6DFEB46BB0C649390CC570FD4502030100010241008C9DC8CDBDCD5496144DE6AF2CBDC489893F4E73D0888CF5F104C1435F121DBEF670D9ECE40844B1FD253102DDD72C39C32A2A32FE093B834C9556EB21A7CA61022100F69D5999B0D442AEBDC025B8D89525CFDA6F18BF81789D1A012B4BE985378943022100DD669E7BF31E36531CB20490D372B9556B1A56CBBB95CECC911DE7D385E212D7022100F3DB3BD2418C6233AFE5E46717A2C3B4F8658AAF3333350595CA5E52172C874902205695B1594C24FE9DD65510500B418C9589434DBA736C0CEA17BE985132C4CD15022100C21F3D6CE7D8A8C7F746FD6C48DBFB7F00E825479E40B636D6E16BC1B78CBB85";
        final Result result = auth.validateCredentials(UserControllerForTestPurposes.USER_NAME, privateKey);

        assertNotNull("Validation result should be present.", result);
        assertFalse("Validation should not be successful.", result.isSuccessful());
        assertEquals("Incorrect result message.", UserAuthResource.ANOTHER_USER_LOGGED_IN, result.getMessage());
    }

    @Override
    public synchronized Restlet getInboundRoot() {
        final Router router = new Router(getContext());
        // add some other resource to be accessed

        final Restlet inspectedEntityInstanceResource = new EntityInstanceResourceFactory<InspectedEntity, IInspectedEntityDao>(IInspectedEntityDao.class, DbDrivenTestCase.injector, DbDrivenTestCase.entityFactory);
        router.attach("/users/{username}/" + InspectedEntity.class.getSimpleName() + "/{entity-id}", inspectedEntityInstanceResource);
        // setup resource guard for the whole router
        final ResourceGuard guard = new ResourceGuard(getContext(), "Test", config.restServerUtil(), config.injector()) {
            @Override
            protected IUserController getController() {
                return controller;
            }
        };
        guard.setNext(router);

        // create the main router and attache user authentication resource as well as the guarded router
        final Router mainRouter = new Router(getContext());
        config.restServerUtil().setAppWidePrivateKey(appWidePrivateKey);
        config.restServerUtil().setAppWidePublicKey(appWidePublicKey);
        mainRouter.attach(authenticationUri, new UserAuthResourceFactory(DbDrivenTestCase.injector, config.restServerUtil()) {
            @Override
            protected IUserController getController() {
                return controller;
            }
        });
        mainRouter.attach(guard);

        return mainRouter;
    }

    @Override
    public void setUp() {
        super.setUp();
        try {
            controller.initUser(config.entityFactory());
            config.restClientUtil().setUserController(controller);
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
