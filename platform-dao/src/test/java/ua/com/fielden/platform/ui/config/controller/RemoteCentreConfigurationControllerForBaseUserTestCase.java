package ua.com.fielden.platform.ui.config.controller;

import static ua.com.fielden.platform.ui.config.impl.interaction.RemoteCentreConfigurationController.KEY_SEPARATOR;

import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.equery.fetchAll;
import ua.com.fielden.platform.security.user.IUserDao;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test.DbDrivenTestCase;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfigController;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemController;
import ua.com.fielden.platform.ui.config.api.interaction.ICenterConfigurationController;
import ua.com.fielden.platform.ui.config.controller.menu_items.PrincipleMenuItem1;
import ua.com.fielden.platform.ui.config.controller.menu_items.PrincipleMenuItem1_2_1;
import ua.com.fielden.platform.ui.config.controller.menu_items.PrincipleMenuItem2_1;
import ua.com.fielden.platform.ui.config.impl.interaction.RemoteCentreConfigurationController;

/**
 * A test case for {@link RemoteCentreConfigurationController} as applicable to a base user.
 * 
 * @author TG Team
 * 
 */
public class RemoteCentreConfigurationControllerForBaseUserTestCase extends DbDrivenTestCase {
    private final IUserDao userDao = injector.getInstance(IUserDao.class);
    private final IEntityCentreConfigController eccController = injector.getInstance(IEntityCentreConfigController.class);
    private final IMainMenuItemController mmiController = injector.getInstance(IMainMenuItemController.class);
    private List<MainMenuItem> tree;

    private final IUserProvider baseUserProvider = new IUserProvider() {
	@Override
	public User getUser() {
	    return userDao.findById(0L, new fetchAll<User>(User.class));
	}

    };

    private MainMenuItem principle1_2_1;
    private MainMenuItem principle2_1;
    private ICenterConfigurationController ccController1_2_1;
    private ICenterConfigurationController ccController2_1;

    private String principleCentreKey1_2_1;
    private String principleCentreKey2_1;

    @Override
    public void setUp() throws Exception {
	super.setUp();
	if (tree == null) { // minor instantiation optimisation
	    mmiController.setUsername("B-USER");
	    tree = mmiController.loadMenuSkeletonStructure();
	    principle1_2_1 = tree.get(0).getChildren().get(1).getChildren().get(0);
	    ccController1_2_1 = new RemoteCentreConfigurationController(eccController, principle1_2_1, baseUserProvider);
	    principleCentreKey1_2_1 = ccController1_2_1.generateKeyForPrincipleCenter(PrincipleMenuItem1_2_1.class);

	    principle2_1 = tree.get(1).getChildren().get(0);
	    ccController2_1 = new RemoteCentreConfigurationController(eccController, principle2_1, baseUserProvider);
	    principleCentreKey2_1 = ccController2_1.generateKeyForPrincipleCenter(PrincipleMenuItem2_1.class);
	}
    }

    @Test
    public void test_key_generation_for_principle_menu_item() {
	final String generatedKey = ccController1_2_1.generateKeyForPrincipleCenter(PrincipleMenuItem1_2_1.class);
	assertEquals("Incorrectly generated key", PrincipleMenuItem1_2_1.class.getName(), generatedKey);
    }

    @Test
    public void test_key_generation_for_principle_menu_item_out_of_context() {
	try {
	    ccController1_2_1.generateKeyForPrincipleCenter(PrincipleMenuItem1.class);
	    fail("Should have not generated a key out of the context");
	} catch (final Exception ex) {

	}
    }

    @Test
    public void test_key_generation_for_non_principle_menu_item() {
	final String title = "correct desc";
	final String nonPrincipleCentreKey = ccController1_2_1.generateKeyForNonPrincipleCenter(principleCentreKey1_2_1, title);
	assertEquals("Incorrectly generate non principle key", principleCentreKey1_2_1 + KEY_SEPARATOR + title, nonPrincipleCentreKey);
    }

    @Test
    public void test_non_principle_center_name_validation() {
	assertFalse("Should have been recognized as invalid", ccController1_2_1.isNonPrincipleCenterNameValid(principleCentreKey1_2_1, KEY_SEPARATOR + "second part of title"));
    }

    @Test
    public void test_creation_of_a_title_list_for_non_principle_centers() {
	assertEquals("Incorrect number of titles.", 0, ccController1_2_1.getNonPrincipleCenters(principleCentreKey1_2_1).size());
	assertEquals("Incorrect number of titles.", 1, ccController2_1.getNonPrincipleCenters(principleCentreKey2_1).size());
    }

    @Test
    public void test_saving_and_load_of_principle_configuration_for_menu_with_no_configuration() {
	final byte[] configBody = new byte[] { 1, 2, 4 };
	ccController1_2_1.save(principleCentreKey1_2_1, configBody);
	final byte[] savedConfigBody = ccController1_2_1.load(principleCentreKey1_2_1);
	assertEquals("Incorrectly saved configuration", configBody, savedConfigBody);
    }

    @Test
    public void test_saving_and_load_of_principle_configuration_for_menu_that_already_has_a_configuration() {
	final byte[] configBody = new byte[] { 1, 2, 3 };
	ccController2_1.save(principleCentreKey2_1, configBody);
	final byte[] savedConfigBody = ccController2_1.load(principleCentreKey2_1);
	assertEquals("Incorrectly saved configuration", configBody, savedConfigBody);
    }

    @Override
    protected String[] getDataSetPathsForInsert() {
	return new String[] { "src/test/resources/data-files/remote-centre-configuration-controller-test-case.flat.xml" };
    }

}
