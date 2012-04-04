package ua.com.fielden.platform.migration;

import java.util.List;

import ua.com.fielden.platform.cypher.Cypher;
import ua.com.fielden.platform.security.provider.IUserController;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test.DbDrivenTestCase2;

/**
 * This is a test for user password reset utility.
 *
 * @author TG Team
 *
 */
public class UserPasswordResetTest extends DbDrivenTestCase2 {
    private final IUserController controller = injector.getInstance(IUserController.class);
    private final ResetUserPassword2 passwordReset = new ResetUserPassword2(controller);

    private final String privateKey = "30820153020100300D06092A864886F70D01010105000482013D30820139020100024100D05F15495909B7F30D6CA85E277E5B2F6F2E8D93666714EF6A85D5DD2513244FD4A84CFAFBEAC063E253E8888ACD84AE33853C46050AA74AC4AEC811334A08C70203010001024071644CE12D62EC8787B6A9A91334DFCA6066A504A1A556EF32B8C2392603D4ADE0941A88E06AFB9DA8A13057F80A9605F497AF28AB78437140C6F101C2766E81022100F9EA9DFBCAAEF81974FE0286657F1D1D3F7ED48640D8E5D46B10DF3F5C3BCA6B022100D57192C3F75252472E4A942B6E727B339E7A9B32FFCB6EB3C3ADDC5A7C7ECA1502201BE2215E466EDF1F77FE1F29FF88FF26943F02573844FADF88528995F82473A102206288518661B0010797191A998A1D133EDB14227D42947900477F0550485487990220597F8FCC2F4ABABF3A9DD4E766C55BFBD5B94B33D0DB77E20FE7E1D93EAE0C7F";
    private final String publicKey = "305C300D06092A864886F70D0101010500034B003048024100D05F15495909B7F30D6CA85E277E5B2F6F2E8D93666714EF6A85D5DD2513244FD4A84CFAFBEAC063E253E8888ACD84AE33853C46050AA74AC4AEC811334A08C70203010001";

    private final Cypher cypher;

    public UserPasswordResetTest() throws Exception {
	super();
	cypher = new Cypher();
    }

    public void test_that_utility_resets_password_for_all_users() throws Exception {
	passwordReset.resetAll(privateKey);

	hibernateUtil.getSessionFactory().getCurrentSession().close();

	final List<User> users = controller.findAllUsers();
	for (final User user : users) {
	    assertEquals("Incorrect password.", user.getKey(), cypher.decrypt(user.getPassword(), publicKey));
	}
    }

    public void test_that_utility_resets_password_for_an_individual_user() throws Exception {
	passwordReset.reset("USER-1", privateKey);

	hibernateUtil.getSessionFactory().getCurrentSession().close();

	final User user = controller.findByKey("USER-1");
	assertEquals("Incorrect password.", user.getKey(), cypher.decrypt(user.getPassword(), publicKey));
    }

    @Override
    protected String[] getDataSetPathsForInsert() {
	return new String[] { "src/test/resources/data-files/user-password-reset-test-data.flat.xml" };
    }
}