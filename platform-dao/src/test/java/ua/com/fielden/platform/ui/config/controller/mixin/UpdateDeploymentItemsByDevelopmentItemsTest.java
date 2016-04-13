package ua.com.fielden.platform.ui.config.controller.mixin;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;

import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.domain.PlatformDomainTypes;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.ui.config.EntityCentreAnalysisConfig;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.IEntityCentreAnalysisConfig;
import ua.com.fielden.platform.ui.config.IMainMenu;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.MainMenuItemInvisibility;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfig;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemController;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemInvisibility;
import ua.com.fielden.platform.ui.config.api.IMainMenuStructureBuilder;

/**
 * A test case for "development" => "deployment" menu items migration utility. See
 * {@link MainMenuItemMixin#updateMenuItemsWithDevelopmentOnes(ua.com.fielden.platform.ui.config.api.IMainMenuStructureBuilder, String)} method.
 * 
 * @author TG Team
 * 
 */
public class UpdateDeploymentItemsByDevelopmentItemsTest extends AbstractDaoTestCase {
    private final IUser userDao = getInstance(IUser.class);
    private final MainMenuItemMixin mixin = new MainMenuItemMixin(getInstance(IMainMenu.class), getInstance(IMainMenuItemController.class), getInstance(IEntityCentreConfig.class), getInstance(IEntityCentreAnalysisConfig.class), getInstance(IMainMenuItemInvisibility.class), getInstance(EntityFactory.class));
    private final EntityFactory factory = getInstance(EntityFactory.class);

    private User getBaseUser() {
        return userDao.findByKeyAndFetch(fetchAll(User.class), "BUSER");
    }

    private User getBaseUserOther() {
        return userDao.findByKeyAndFetch(fetchAll(User.class), "BUSEROTHER");
    }

    private User getDescendantUser() {
        return userDao.findByKeyAndFetch(fetchAll(User.class), "DUSER");
    }

    /**
     * Base class for testing structure builders.
     * 
     * @author TG Team
     * 
     */
    private static class MainMenu implements IMainMenuStructureBuilder {
        private final MainMenuStructureFactory structureFactory;

        protected MainMenu(final EntityFactory factory) {
            structureFactory = new MainMenuStructureFactory(factory);
        }

        @Override
        public List<MainMenuItem> build() {
            return structureFactory.build();
        }

        protected MainMenuStructureFactory structureFactory() {
            return structureFactory;
        }
    }

    /**
     * A builder that is fully synchronised with original testing menu items / entity centres.
     * 
     * @author TG Team
     * 
     */
    private static class OriginalCheckingBuilder extends MainMenu {
        protected OriginalCheckingBuilder(final EntityFactory factory) {
            super(factory);
        }

        @Override
        public List<MainMenuItem> build() {
            structureFactory().push("type1").push("type2").push("type3").push("type3").pop().pop().pop() // non-persistent "saveAs"-related menu item
            .push("type4").push("type5").push("type5").pop().pop().pop().pop() // non-persistent "saveAs"-related menu item
            .push("type6").push("type7").push("type7").pop().pop().pop(); // non-persistent "saveAs"-related menu item
            return super.build();
        }
    }

    /**
     * Traverses the trees of items to check them for equality.
     * 
     * @param expected
     * @param actual
     * @return
     */
    private boolean itemsEquals(final List<MainMenuItem> expected, final List<MainMenuItem> actual) {
        if (!expected.equals(actual)) {
            return false;
        }
        for (int i = 0; i < expected.size(); i++) {
            if (!itemsEquals(expected.get(i).getChildren(), actual.get(i).getChildren())) {
                System.err.println("The " + i + "-th element in list " + expected + " is not equal to appropriate actual element due to different children => expected = "
                        + expected.get(i).getChildren() + ", actual = " + actual.get(i).getChildren() + ".");
                return false;
            }
        }
        return true;
    }

    //////////////////////////////////////////////////////////////////////////////
    ////////////////////////// NON_BASE USER IS NOT PERMITTED ////////////////////////
    //////////////////////////////////////////////////////////////////////////////

    private static class UpdatingBuilder0 extends MainMenu {
        protected UpdatingBuilder0(final EntityFactory factory) {
            super(factory);
        }

        @Override
        public List<MainMenuItem> build() {
            structureFactory().push("type1").pop();
            return super.build();
        }
    }

    @Test
    public void test_update_is_not_permitted_for_non_base_user() {
        mixin.setUser(getDescendantUser());
        try {
            mixin.updateMenuItemsWithDevelopmentOnes(new UpdatingBuilder0(factory));
            fail("Should be failed.");
        } catch (final IllegalArgumentException e) {
        }
    }

    //////////////////////////////////////////////////////////////////////////////
    ////////////////////////// ADDITION //////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////

    private static class UpdatingBuilder1 extends MainMenu {
        protected UpdatingBuilder1(final EntityFactory factory) {
            super(factory);
        }

        @Override
        public List<MainMenuItem> build() {
            structureFactory().push("type1").push("type2").push("type3").pop().pop().push("type4").push("type5").pop().pop().pop().push("type6").push("type7").pop().pop();
            return super.build();
        }
    }

    @Test
    public void test_update_with_exactly_the_same_menu_hierarchy() {
        final User baseUser = getBaseUser();
        mixin.setUser(baseUser);
        assertTrue("Incorrect menu before updating.", itemsEquals(new OriginalCheckingBuilder(factory).build(), mixin.loadMenuSkeletonStructure()));
        mixin.updateMenuItemsWithDevelopmentOnes(new UpdatingBuilder1(factory));
        assertTrue("Incorrect menu after updating.", itemsEquals(new OriginalCheckingBuilder(factory).build(), mixin.loadMenuSkeletonStructure()));
    }

    /**
     * A builder that is fully synchronised with original testing menu items / entity centres.
     * 
     * @author TG Team
     * 
     */
    private static class OriginalCheckingBuilderOther extends MainMenu {
        protected OriginalCheckingBuilderOther(final EntityFactory factory) {
            super(factory);
        }

        @Override
        public List<MainMenuItem> build() {
            structureFactory().push("type1").push("type2").push("type3").pop().pop().push("type4").push("type5").pop().pop().pop().push("type6").push("type7").pop().pop();
            return super.build();
        }
    }

    @Test
    public void test_update_with_exactly_the_same_menu_hierarchy_for_menu_items_that_contain_centres_for_different_base_users() {
        final User baseUserOther = getBaseUserOther();
        mixin.setUser(baseUserOther);
        assertTrue("Incorrect menu before updating.", itemsEquals(new OriginalCheckingBuilderOther(factory).build(), mixin.loadMenuSkeletonStructure()));
        mixin.updateMenuItemsWithDevelopmentOnes(new UpdatingBuilder1(factory));
        assertTrue("Incorrect menu after updating.", itemsEquals(new OriginalCheckingBuilderOther(factory).build(), mixin.loadMenuSkeletonStructure()));
        final User baseUser = getBaseUser();
        mixin.setUser(baseUser);
        assertTrue("Incorrect menu after updating.", itemsEquals(new OriginalCheckingBuilder(factory).build(), mixin.loadMenuSkeletonStructure()));
    }

    private static class UpdatingBuilder2 extends MainMenu {
        protected UpdatingBuilder2(final EntityFactory factory) {
            super(factory);
        }

        @Override
        public List<MainMenuItem> build() {
            structureFactory().push("type1").push("type2").push("type3").pop().pop().push("type4").push("type5").pop().pop().pop().push("type6").push("type7").pop().pop().push("type_added").pop();
            return super.build();
        }
    }

    private static class CheckingBuilder2 extends MainMenu {
        protected CheckingBuilder2(final EntityFactory factory) {
            super(factory);
        }

        @Override
        public List<MainMenuItem> build() {
            structureFactory().push("type1").push("type2").push("type3").push("type3").pop().pop().pop() // non-persistent "saveAs"-related menu item
            .push("type4").push("type5").push("type5").pop().pop().pop().pop() // non-persistent "saveAs"-related menu item
            .push("type6").push("type7").push("type7").pop().pop().pop() // non-persistent "saveAs"-related menu item
            .push("type_added").pop();
            return super.build();
        }
    }

    @Test
    public void test_update_with_ADDING_first_level_item_into_the_end_of_list() {
        final User baseUser = getBaseUser();
        mixin.setUser(baseUser);
        assertTrue("Incorrect menu before updating.", itemsEquals(new OriginalCheckingBuilder(factory).build(), mixin.loadMenuSkeletonStructure()));
        mixin.updateMenuItemsWithDevelopmentOnes(new UpdatingBuilder2(factory));
        assertTrue("Incorrect menu after updating.", itemsEquals(new CheckingBuilder2(factory).build(), mixin.loadMenuSkeletonStructure()));
    }

    private static class UpdatingBuilder3 extends MainMenu {
        protected UpdatingBuilder3(final EntityFactory factory) {
            super(factory);
        }

        @Override
        public List<MainMenuItem> build() {
            structureFactory().push("type1").push("type2").push("type3").pop().pop().push("type4").push("type5").pop().pop().pop().push("type_added").pop().push("type6").push("type7").pop().pop();

            return super.build();
        }
    }

    private static class CheckingBuilder3 extends MainMenu {
        protected CheckingBuilder3(final EntityFactory factory) {
            super(factory);
        }

        @Override
        public List<MainMenuItem> build() {
            structureFactory().push("type1").push("type2").push("type3").push("type3").pop().pop().pop() // non-persistent "saveAs"-related menu item
            .push("type4").push("type5").push("type5").pop().pop().pop().pop() // non-persistent "saveAs"-related menu item
            .push("type_added").pop().push("type6").push("type7").push("type7").pop().pop().pop(); // non-persistent "saveAs"-related menu item
            return super.build();
        }
    }

    @Test
    public void test_update_with_ADDING_first_level_item_into_the_middle_of_list() {
        final User baseUser = getBaseUser();
        mixin.setUser(baseUser);
        assertTrue("Incorrect menu before updating.", itemsEquals(new OriginalCheckingBuilder(factory).build(), mixin.loadMenuSkeletonStructure()));
        mixin.updateMenuItemsWithDevelopmentOnes(new UpdatingBuilder3(factory));
        assertTrue("Incorrect menu after updating.", itemsEquals(new CheckingBuilder3(factory).build(), mixin.loadMenuSkeletonStructure()));
    }

    private static class UpdatingBuilder4 extends MainMenu {
        protected UpdatingBuilder4(final EntityFactory factory) {
            super(factory);
        }

        @Override
        public List<MainMenuItem> build() {
            structureFactory().push("type_added").pop().push("type1").push("type2").push("type3").pop().pop().push("type4").push("type5").pop().pop().pop().push("type6").push("type7").pop().pop();

            return super.build();
        }
    }

    private static class CheckingBuilder4 extends MainMenu {
        protected CheckingBuilder4(final EntityFactory factory) {
            super(factory);
        }

        @Override
        public List<MainMenuItem> build() {
            structureFactory().push("type_added").pop().push("type1").push("type2").push("type3").push("type3").pop().pop().pop() // non-persistent "saveAs"-related menu item
            .push("type4").push("type5").push("type5").pop().pop().pop().pop() // non-persistent "saveAs"-related menu item
            .push("type6").push("type7").push("type7").pop().pop().pop(); // non-persistent "saveAs"-related menu item
            return super.build();
        }
    }

    @Test
    public void test_update_with_ADDING_first_level_item_into_the_beginning_of_list() {
        final User baseUser = getBaseUser();
        mixin.setUser(baseUser);
        assertTrue("Incorrect menu before updating.", itemsEquals(new OriginalCheckingBuilder(factory).build(), mixin.loadMenuSkeletonStructure()));
        mixin.updateMenuItemsWithDevelopmentOnes(new UpdatingBuilder4(factory));
        assertTrue("Incorrect menu after updating.", itemsEquals(new CheckingBuilder4(factory).build(), mixin.loadMenuSkeletonStructure()));
    }

    private static class UpdatingBuilder5 extends MainMenu {
        protected UpdatingBuilder5(final EntityFactory factory) {
            super(factory);
        }

        @Override
        public List<MainMenuItem> build() {
            structureFactory().push("type1").push("type2").push("type3").pop().pop().push("type4").push("type5").pop().pop().push("type_added").pop().pop().push("type6").push("type7").pop().pop();
            return super.build();
        }
    }

    private static class CheckingBuilder5 extends MainMenu {
        protected CheckingBuilder5(final EntityFactory factory) {
            super(factory);
        }

        @Override
        public List<MainMenuItem> build() {
            structureFactory().push("type1").push("type2").push("type3").push("type3").pop().pop().pop() // non-persistent "saveAs"-related menu item
            .push("type4").push("type5").push("type5").pop().pop().pop() // non-persistent "saveAs"-related menu item
            .push("type_added").pop().pop().push("type6").push("type7").push("type7").pop().pop().pop(); // non-persistent "saveAs"-related menu item
            return super.build();
        }
    }

    @Test
    public void test_update_with_ADDING_second_level_item_into_the_end_of_list() {
        final User baseUser = getBaseUser();
        mixin.setUser(baseUser);
        assertTrue("Incorrect menu before updating.", itemsEquals(new OriginalCheckingBuilder(factory).build(), mixin.loadMenuSkeletonStructure()));
        mixin.updateMenuItemsWithDevelopmentOnes(new UpdatingBuilder5(factory));
        assertTrue("Incorrect menu after updating.", itemsEquals(new CheckingBuilder5(factory).build(), mixin.loadMenuSkeletonStructure()));
    }

    private static class UpdatingBuilder6 extends MainMenu {
        protected UpdatingBuilder6(final EntityFactory factory) {
            super(factory);
        }

        @Override
        public List<MainMenuItem> build() {
            structureFactory().push("type1").push("type2").push("type3").pop().pop().push("type_added").pop().push("type4").push("type5").pop().pop().pop().push("type6").push("type7").pop().pop();
            return super.build();
        }
    }

    private static class CheckingBuilder6 extends MainMenu {
        protected CheckingBuilder6(final EntityFactory factory) {
            super(factory);
        }

        @Override
        public List<MainMenuItem> build() {
            structureFactory().push("type1").push("type2").push("type3").push("type3").pop().pop().pop() // non-persistent "saveAs"-related menu item
            .push("type_added").pop().push("type4").push("type5").push("type5").pop().pop().pop().pop() // non-persistent "saveAs"-related menu item
            .push("type6").push("type7").push("type7").pop().pop().pop(); // non-persistent "saveAs"-related menu item
            return super.build();
        }
    }

    @Test
    public void test_update_with_ADDING_second_level_item_into_the_middle_of_list() {
        final User baseUser = getBaseUser();
        mixin.setUser(baseUser);
        assertTrue("Incorrect menu before updating.", itemsEquals(new OriginalCheckingBuilder(factory).build(), mixin.loadMenuSkeletonStructure()));
        mixin.updateMenuItemsWithDevelopmentOnes(new UpdatingBuilder6(factory));
        assertTrue("Incorrect menu after updating.", itemsEquals(new CheckingBuilder6(factory).build(), mixin.loadMenuSkeletonStructure()));
    }

    private static class UpdatingBuilder7 extends MainMenu {
        protected UpdatingBuilder7(final EntityFactory factory) {
            super(factory);
        }

        @Override
        public List<MainMenuItem> build() {
            structureFactory().push("type1").push("type_added").pop().push("type2").push("type3").pop().pop().push("type4").push("type5").pop().pop().pop().push("type6").push("type7").pop().pop();
            return super.build();
        }
    }

    private static class CheckingBuilder7 extends MainMenu {
        protected CheckingBuilder7(final EntityFactory factory) {
            super(factory);
        }

        @Override
        public List<MainMenuItem> build() {
            structureFactory().push("type1").push("type_added").pop().push("type2").push("type3").push("type3").pop().pop().pop() // non-persistent "saveAs"-related menu item
            .push("type4").push("type5").push("type5").pop().pop().pop().pop() // non-persistent "saveAs"-related menu item
            .push("type6").push("type7").push("type7").pop().pop().pop(); // non-persistent "saveAs"-related menu item
            return super.build();
        }
    }

    @Test
    public void test_update_with_ADDING_second_level_item_into_the_beginning_of_list() {
        final User baseUser = getBaseUser();
        mixin.setUser(baseUser);
        assertTrue("Incorrect menu before updating.", itemsEquals(new OriginalCheckingBuilder(factory).build(), mixin.loadMenuSkeletonStructure()));
        mixin.updateMenuItemsWithDevelopmentOnes(new UpdatingBuilder7(factory));
        assertTrue("Incorrect menu after updating.", itemsEquals(new CheckingBuilder7(factory).build(), mixin.loadMenuSkeletonStructure()));
    }

    private static class UpdatingBuilder8 extends MainMenu {
        protected UpdatingBuilder8(final EntityFactory factory) {
            super(factory);
        }

        @Override
        public List<MainMenuItem> build() {
            structureFactory().push("type1").push("type2").push("type_added").pop().push("type3").pop().pop().push("type4").push("type5").pop().pop().pop().push("type6").push("type7").pop().pop();
            return super.build();
        }
    }

    private static class CheckingBuilder8 extends MainMenu {
        protected CheckingBuilder8(final EntityFactory factory) {
            super(factory);
        }

        @Override
        public List<MainMenuItem> build() {
            structureFactory().push("type1").push("type2").push("type_added").pop().push("type3").push("type3").pop().pop().pop() // non-persistent "saveAs"-related menu item
            .push("type4").push("type5").push("type5").pop().pop().pop().pop() // non-persistent "saveAs"-related menu item
            .push("type6").push("type7").push("type7").pop().pop().pop(); // non-persistent "saveAs"-related menu item
            return super.build();
        }
    }

    @Test
    public void test_update_with_ADDING_third_level_item_into_the_beginning_of_list() {
        final User baseUser = getBaseUser();
        mixin.setUser(baseUser);
        assertTrue("Incorrect menu before updating.", itemsEquals(new OriginalCheckingBuilder(factory).build(), mixin.loadMenuSkeletonStructure()));
        mixin.updateMenuItemsWithDevelopmentOnes(new UpdatingBuilder8(factory));
        assertTrue("Incorrect menu after updating.", itemsEquals(new CheckingBuilder8(factory).build(), mixin.loadMenuSkeletonStructure()));
    }

    private static class UpdatingBuilder9 extends MainMenu {
        protected UpdatingBuilder9(final EntityFactory factory) {
            super(factory);
        }

        @Override
        public List<MainMenuItem> build() {
            structureFactory().push("type1").push("type2").push("type3").pop().pop().push("type4").push("type5").pop().push("type_added").pop().pop().pop().push("type6").push("type7").pop().pop();
            return super.build();
        }
    }

    private static class CheckingBuilder9 extends MainMenu {
        protected CheckingBuilder9(final EntityFactory factory) {
            super(factory);
        }

        @Override
        public List<MainMenuItem> build() {
            structureFactory().push("type1").push("type2").push("type3").push("type3").pop().pop().pop() // non-persistent "saveAs"-related menu item
            .push("type4").push("type5").push("type5").pop().pop().push("type_added").pop().pop().pop() // non-persistent "saveAs"-related menu item
            .push("type6").push("type7").push("type7").pop().pop().pop(); // non-persistent "saveAs"-related menu item
            return super.build();
        }
    }

    @Test
    public void test_update_with_ADDING_third_level_item_into_the_end_of_list() {
        final User baseUser = getBaseUser();
        mixin.setUser(baseUser);
        assertTrue("Incorrect menu before updating.", itemsEquals(new OriginalCheckingBuilder(factory).build(), mixin.loadMenuSkeletonStructure()));
        mixin.updateMenuItemsWithDevelopmentOnes(new UpdatingBuilder9(factory));
        assertTrue("Incorrect menu after updating.", itemsEquals(new CheckingBuilder9(factory).build(), mixin.loadMenuSkeletonStructure()));
    }

    //////////////////////////////////////////////////////////////////////////////
    ////////////////////////// MODIFICATION //////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////

    private static class UpdatingBuilder10 extends MainMenu {
        protected UpdatingBuilder10(final EntityFactory factory) {
            super(factory);
        }

        @Override
        public List<MainMenuItem> build() {
            structureFactory().push("type_modified").push("type2").push("type3").pop().pop().push("type4").push("type5").pop().pop().pop().push("type6").push("type7").pop().pop();
            return super.build();
        }
    }

    private static class CheckingBuilder10 extends MainMenu {
        protected CheckingBuilder10(final EntityFactory factory) {
            super(factory);
        }

        @Override
        public List<MainMenuItem> build() {
            structureFactory().push("type_modified").push("type2").push("type3").push("type3").pop().pop().pop() // non-persistent "saveAs"-related menu item
            .push("type4").push("type5").push("type5").pop().pop().pop().pop() // non-persistent "saveAs"-related menu item
            .push("type6").push("type7").push("type7").pop().pop().pop(); // non-persistent "saveAs"-related menu item
            return super.build();
        }
    }

    @Test
    public void test_update_with_MODIFYING_first_level_item_in_the_beginning_of_list() {
        final User baseUser = getBaseUser();
        mixin.setUser(baseUser);
        assertTrue("Incorrect menu before updating.", itemsEquals(new OriginalCheckingBuilder(factory).build(), mixin.loadMenuSkeletonStructure()));
        mixin.updateMenuItemsWithDevelopmentOnes(new UpdatingBuilder10(factory));
        assertTrue("Incorrect menu after updating.", itemsEquals(new CheckingBuilder10(factory).build(), mixin.loadMenuSkeletonStructure()));
    }

    private static class UpdatingBuilder11 extends MainMenu {
        protected UpdatingBuilder11(final EntityFactory factory) {
            super(factory);
        }

        @Override
        public List<MainMenuItem> build() {
            structureFactory().push("type1").push("type2").push("type3").pop().pop().push("type4").push("type5").pop().pop().pop().push("type_modified").push("type7").pop().pop();
            return super.build();
        }
    }

    private static class CheckingBuilder11 extends MainMenu {
        protected CheckingBuilder11(final EntityFactory factory) {
            super(factory);
        }

        @Override
        public List<MainMenuItem> build() {
            structureFactory().push("type1").push("type2").push("type3").push("type3").pop().pop().pop() // non-persistent "saveAs"-related menu item
            .push("type4").push("type5").push("type5").pop().pop().pop().pop() // non-persistent "saveAs"-related menu item
            .push("type_modified").push("type7").push("type7").pop().pop().pop(); // non-persistent "saveAs"-related menu item
            return super.build();
        }
    }

    @Test
    public void test_update_with_MODIFYING_first_level_item_in_the_end_of_list() {
        final User baseUser = getBaseUser();
        mixin.setUser(baseUser);
        assertTrue("Incorrect menu before updating.", itemsEquals(new OriginalCheckingBuilder(factory).build(), mixin.loadMenuSkeletonStructure()));
        mixin.updateMenuItemsWithDevelopmentOnes(new UpdatingBuilder11(factory));
        assertTrue("Incorrect menu after updating.", itemsEquals(new CheckingBuilder11(factory).build(), mixin.loadMenuSkeletonStructure()));
    }

    private static class UpdatingBuilder12 extends MainMenu {
        protected UpdatingBuilder12(final EntityFactory factory) {
            super(factory);
        }

        @Override
        public List<MainMenuItem> build() {
            structureFactory().push("type1").push("type_modified").push("type3").pop().pop().push("type4").push("type5").pop().pop().pop().push("type6").push("type7").pop().pop();
            return super.build();
        }
    }

    private static class CheckingBuilder12 extends MainMenu {
        protected CheckingBuilder12(final EntityFactory factory) {
            super(factory);
        }

        @Override
        public List<MainMenuItem> build() {
            structureFactory().push("type1").push("type_modified").push("type3").push("type3").pop().pop().pop() // non-persistent "saveAs"-related menu item
            .push("type4").push("type5").push("type5").pop().pop().pop().pop() // non-persistent "saveAs"-related menu item
            .push("type6").push("type7").push("type7").pop().pop().pop(); // non-persistent "saveAs"-related menu item
            return super.build();
        }
    }

    @Test
    public void test_update_with_MODIFYING_second_level_item_in_the_beginning_of_list() {
        final User baseUser = getBaseUser();
        mixin.setUser(baseUser);
        assertTrue("Incorrect menu before updating.", itemsEquals(new OriginalCheckingBuilder(factory).build(), mixin.loadMenuSkeletonStructure()));
        mixin.updateMenuItemsWithDevelopmentOnes(new UpdatingBuilder12(factory));
        assertTrue("Incorrect menu after updating.", itemsEquals(new CheckingBuilder12(factory).build(), mixin.loadMenuSkeletonStructure()));
    }

    private static class UpdatingBuilder13 extends MainMenu {
        protected UpdatingBuilder13(final EntityFactory factory) {
            super(factory);
        }

        @Override
        public List<MainMenuItem> build() {
            structureFactory().push("type1").push("type2").push("type3").pop().pop().push("type_modified").push("type5").pop().pop().pop().push("type6").push("type7").pop().pop();
            return super.build();
        }
    }

    private static class CheckingBuilder13 extends MainMenu {
        protected CheckingBuilder13(final EntityFactory factory) {
            super(factory);
        }

        @Override
        public List<MainMenuItem> build() {
            structureFactory().push("type1").push("type2").push("type3").push("type3").pop().pop().pop() // non-persistent "saveAs"-related menu item
            .push("type_modified").push("type5").push("type5").pop().pop().pop().pop() // non-persistent "saveAs"-related menu item
            .push("type6").push("type7").push("type7").pop().pop().pop(); // non-persistent "saveAs"-related menu item
            return super.build();
        }
    }

    @Test
    public void test_update_with_MODIFYING_second_level_item_in_the_end_of_list() {
        final User baseUser = getBaseUser();
        mixin.setUser(baseUser);
        assertTrue("Incorrect menu before updating.", itemsEquals(new OriginalCheckingBuilder(factory).build(), mixin.loadMenuSkeletonStructure()));
        mixin.updateMenuItemsWithDevelopmentOnes(new UpdatingBuilder13(factory));
        assertTrue("Incorrect menu after updating.", itemsEquals(new CheckingBuilder13(factory).build(), mixin.loadMenuSkeletonStructure()));
    }

    private static class UpdatingBuilder14 extends MainMenu {
        protected UpdatingBuilder14(final EntityFactory factory) {
            super(factory);
        }

        @Override
        public List<MainMenuItem> build() {
            structureFactory().push("type1").push("type2").push("type_modified").pop().pop().push("type4").push("type5").pop().pop().pop().push("type6").push("type7").pop().pop();
            return super.build();
        }
    }

    private static class CheckingBuilder14 extends MainMenu {
        protected CheckingBuilder14(final EntityFactory factory) {
            super(factory);
        }

        @Override
        public List<MainMenuItem> build() {
            structureFactory().push("type1").push("type2").push("type_modified").pop().pop().push("type4").push("type5").push("type5").pop().pop().pop().pop() // non-persistent "saveAs"-related menu item
            .push("type6").push("type7").push("type7").pop().pop().pop(); // non-persistent "saveAs"-related menu item
            return super.build();
        }
    }

    @Test
    public void test_update_with_MODIFYING_third_level_item_in_the_beginning_of_list() {
        final User baseUser = getBaseUser();
        mixin.setUser(baseUser);
        assertTrue("Incorrect menu before updating.", itemsEquals(new OriginalCheckingBuilder(factory).build(), mixin.loadMenuSkeletonStructure()));
        mixin.updateMenuItemsWithDevelopmentOnes(new UpdatingBuilder14(factory));
        assertTrue("Incorrect menu after updating.", itemsEquals(new CheckingBuilder14(factory).build(), mixin.loadMenuSkeletonStructure()));
    }

    //////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// REMOVAL //////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////

    private static class UpdatingBuilder15 extends MainMenu {
        protected UpdatingBuilder15(final EntityFactory factory) {
            super(factory);
        }

        @Override
        public List<MainMenuItem> build() {
            structureFactory().push("type1").push("type2").pop().push("type4").push("type5").pop().pop().pop().push("type6").push("type7").pop().pop();
            return super.build();
        }
    }

    private static class CheckingBuilder15 extends MainMenu {
        protected CheckingBuilder15(final EntityFactory factory) {
            super(factory);
        }

        @Override
        public List<MainMenuItem> build() {
            structureFactory().push("type1").push("type2").pop().push("type4").push("type5").push("type5").pop().pop().pop().pop() // non-persistent "saveAs"-related menu item
            .push("type6").push("type7").push("type7").pop().pop().pop(); // non-persistent "saveAs"-related menu item
            return super.build();
        }
    }

    @Test
    public void test_update_with_REMOVING_third_level_item() {
        final User baseUser = getBaseUser();
        mixin.setUser(baseUser);
        assertTrue("Incorrect menu before updating.", itemsEquals(new OriginalCheckingBuilder(factory).build(), mixin.loadMenuSkeletonStructure()));
        mixin.updateMenuItemsWithDevelopmentOnes(new UpdatingBuilder15(factory));
        assertTrue("Incorrect menu after updating.", itemsEquals(new CheckingBuilder15(factory).build(), mixin.loadMenuSkeletonStructure()));
    }

    private static class UpdatingBuilder16 extends MainMenu {
        protected UpdatingBuilder16(final EntityFactory factory) {
            super(factory);
        }

        @Override
        public List<MainMenuItem> build() {
            structureFactory().push("type1").push("type2").push("type3").pop().pop().pop().push("type6").push("type7").pop().pop();
            return super.build();
        }
    }

    private static class CheckingBuilder16 extends MainMenu {
        protected CheckingBuilder16(final EntityFactory factory) {
            super(factory);
        }

        @Override
        public List<MainMenuItem> build() {
            structureFactory().push("type1").push("type2").push("type3").push("type3").pop().pop().pop().pop() // non-persistent "saveAs"-related menu item
            .push("type6").push("type7").push("type7").pop().pop().pop(); // non-persistent "saveAs"-related menu item
            return super.build();
        }
    }

    @Test
    public void test_update_with_REMOVING_second_level_item() {
        final User baseUser = getBaseUser();
        mixin.setUser(baseUser);
        assertTrue("Incorrect menu before updating.", itemsEquals(new OriginalCheckingBuilder(factory).build(), mixin.loadMenuSkeletonStructure()));
        mixin.updateMenuItemsWithDevelopmentOnes(new UpdatingBuilder16(factory));
        assertTrue("Incorrect menu after updating.", itemsEquals(new CheckingBuilder16(factory).build(), mixin.loadMenuSkeletonStructure()));
    }

    //////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// ORDER CHANGE /////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////

    private static class UpdatingBuilder17 extends MainMenu {
        protected UpdatingBuilder17(final EntityFactory factory) {
            super(factory);
        }

        @Override
        public List<MainMenuItem> build() {
            structureFactory().push("type6").push("type7").pop().pop().push("type1").push("type2").push("type3").pop().pop().push("type4").push("type5").pop().pop().pop();
            return super.build();
        }
    }

    private static class CheckingBuilder17 extends MainMenu {
        protected CheckingBuilder17(final EntityFactory factory) {
            super(factory);
        }

        @Override
        public List<MainMenuItem> build() {
            structureFactory().push("type6").push("type7").push("type7").pop().pop().pop() // non-persistent "saveAs"-related menu item
            .push("type1").push("type2").push("type3").push("type3").pop().pop().pop() // non-persistent "saveAs"-related menu item
            .push("type4").push("type5").push("type5").pop().pop().pop().pop(); // non-persistent "saveAs"-related menu item
            return super.build();
        }
    }

    @Test
    public void test_update_with_ORDERING_CHANGE_for_first_level_items() {
        final User baseUser = getBaseUser();
        mixin.setUser(baseUser);
        assertTrue("Incorrect menu before updating.", itemsEquals(new OriginalCheckingBuilder(factory).build(), mixin.loadMenuSkeletonStructure()));
        mixin.updateMenuItemsWithDevelopmentOnes(new UpdatingBuilder17(factory));
        assertTrue("Incorrect menu after updating.", itemsEquals(new CheckingBuilder17(factory).build(), mixin.loadMenuSkeletonStructure()));
    }

    private static class UpdatingBuilder18 extends MainMenu {
        protected UpdatingBuilder18(final EntityFactory factory) {
            super(factory);
        }

        @Override
        public List<MainMenuItem> build() {
            structureFactory().push("type6").push("type7").pop().pop().push("type1").push("type4").push("type5").pop().pop().push("type2").push("type3").pop().pop().pop();
            return super.build();
        }
    }

    private static class CheckingBuilder18 extends MainMenu {
        protected CheckingBuilder18(final EntityFactory factory) {
            super(factory);
        }

        @Override
        public List<MainMenuItem> build() {
            structureFactory().push("type6").push("type7").push("type7").pop().pop().pop() // non-persistent "saveAs"-related menu item
            .push("type1").push("type4").push("type5").push("type5").pop().pop().pop() // non-persistent "saveAs"-related menu item
            .push("type2").push("type3").push("type3").pop().pop().pop().pop(); // non-persistent "saveAs"-related menu item
            return super.build();
        }
    }

    @Test
    public void test_update_with_ORDERING_CHANGE_for_first_and_second_level_items() {
        final User baseUser = getBaseUser();
        mixin.setUser(baseUser);
        assertTrue("Incorrect menu before updating.", itemsEquals(new OriginalCheckingBuilder(factory).build(), mixin.loadMenuSkeletonStructure()));
        mixin.updateMenuItemsWithDevelopmentOnes(new UpdatingBuilder18(factory));
        assertTrue("Incorrect menu after updating.", itemsEquals(new CheckingBuilder18(factory).build(), mixin.loadMenuSkeletonStructure()));
    }

    //////////////////////////////////////////////////////////////////////////////
    ///////////// ADDITION, MODIFICATION, REMOVAL AND ORDER CHANGE ///////////////
    //////////////////////////////////////////////////////////////////////////////

    private static class UpdatingBuilder19 extends MainMenu {
        protected UpdatingBuilder19(final EntityFactory factory) {
            super(factory);
        }

        @Override
        public List<MainMenuItem> build() {
            structureFactory().push("type_modified_1").push("type7").pop().pop().push("type_modified_2").push("type4").pop().push("type_modified_3").push("type3").pop().push("type_added").pop().pop().pop();
            return super.build();
        }
    }

    private static class CheckingBuilder19 extends MainMenu {
        protected CheckingBuilder19(final EntityFactory factory) {
            super(factory);
        }

        @Override
        public List<MainMenuItem> build() {
            structureFactory().push("type_modified_1").push("type7").push("type7").pop().pop().pop() // non-persistent "saveAs"-related menu item
            .push("type_modified_2").push("type4").pop().push("type_modified_3").push("type3").push("type3").pop().pop() // non-persistent "saveAs"-related menu item
            .push("type_added").pop().pop().pop();
            return super.build();
        }
    }

    @Test
    public void test_update_with_ADDITION_REMOVAL_MODIFICATION_and_ORDERING_CHANGE() {
        final User baseUser = getBaseUser();
        mixin.setUser(baseUser);
        assertTrue("Incorrect menu before updating.", itemsEquals(new OriginalCheckingBuilder(factory).build(), mixin.loadMenuSkeletonStructure()));
        mixin.updateMenuItemsWithDevelopmentOnes(new UpdatingBuilder19(factory));
        assertTrue("Incorrect menu after updating.", itemsEquals(new CheckingBuilder19(factory).build(), mixin.loadMenuSkeletonStructure()));
    }

    private static int n = 100;

    private static class CheckingAndUpdatingBuilder20 extends MainMenu {
        protected CheckingAndUpdatingBuilder20(final EntityFactory factory) {
            super(factory);
        }

        @Override
        public List<MainMenuItem> build() {
            for (int i = 0; i < n; i++) {
                structureFactory().push("type_neew_" + i).pop();
            }
            return super.build();
        }
    }

    @Test
    public void test_update_with_a_lot_of_items() {
        final User baseUser = getBaseUser();
        mixin.setUser(baseUser);
        assertTrue("Incorrect menu before updating.", itemsEquals(new OriginalCheckingBuilder(factory).build(), mixin.loadMenuSkeletonStructure()));
        mixin.updateMenuItemsWithDevelopmentOnes(new CheckingAndUpdatingBuilder20(factory));
        assertTrue("Incorrect menu after updating.", itemsEquals(new CheckingAndUpdatingBuilder20(factory).build(), mixin.loadMenuSkeletonStructure()));
    }

    //////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// INITIAL SETUP ////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////

    @Override
    protected List<Class<? extends AbstractEntity<?>>> domainEntityTypes() {
        return PlatformDomainTypes.types;
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();
        
        final User baseUser = save(new_(User.class, "BUSER").setBase(true)); // base user
        final User baseUserOther = save(new_(User.class, "BUSEROTHER").setBase(true)); // base user
        save(new_(User.class, "DUSER").setBase(false).setBasedOnUser(baseUser)); // descendant user

        // populate main menu items
        final MainMenuItem root_1 = save(new_(MainMenuItem.class, "type1").setTitle("Root 1").setOrder(1));
        /**/final MainMenuItem item_1_1 = save(new_(MainMenuItem.class, "type2").setParent(root_1).setTitle("Item 1-1").setOrder(1));
        /*    */final MainMenuItem item_1_1_1 = save(new_(MainMenuItem.class, "type3").setParent(item_1_1).setTitle("Item 1-1-1").setOrder(1));
        /*    */save(new_composite(EntityCentreConfig.class, baseUser, "principal for item 1-1-1", item_1_1_1).setPrincipal(true));
        /*        */final EntityCentreConfig item_1_1_1_saveAs = save(new_composite(EntityCentreConfig.class, baseUser, "save as for item 1-1-1", item_1_1_1).setPrincipal(false));
        /*            */save(new_composite(EntityCentreAnalysisConfig.class, item_1_1_1_saveAs, "analysis for save as for item 1-1-1"));
        /**/final MainMenuItem item_1_2 = save(new_(MainMenuItem.class, "type4").setParent(root_1).setTitle("Item 1-2").setOrder(2));
        /*    */final MainMenuItem item_1_2_1 = save(new_(MainMenuItem.class, "type5").setParent(item_1_2).setTitle("Item 1-2-1").setOrder(1));
        /*    */save(new_composite(EntityCentreConfig.class, baseUser, "principal for item 1-2-1", item_1_2_1).setPrincipal(true));
        /*        */save(new_composite(EntityCentreConfig.class, baseUser, "save as for item 1-2-1", item_1_2_1).setPrincipal(false));
        final MainMenuItem root_2 = save(new_(MainMenuItem.class, "type6").setTitle("Root 2").setOrder(2)); // should be recognized as invisible
        /**/final MainMenuItem item_2_1 = save(new_(MainMenuItem.class, "type7").setParent(root_2).setTitle("Item 2-1").setOrder(1)); // should be recognized as invisible
        /**/save(new_composite(EntityCentreConfig.class, baseUser, "principal for item 2-1", item_2_1).setPrincipal(true));
        /*    */save(new_composite(EntityCentreConfig.class, baseUser, "save as for item 2-1", item_2_1).setPrincipal(false));

        //	// entity-centre for different base user!
        //	/*    */save(new_composite(EntityCentreConfig.class, baseUserOther, "principal for item 1-1-1 (for OTHER base user)", item_1_1_1).setPrincipal(true));

        // populate invisibility
        save(new_composite(MainMenuItemInvisibility.class, baseUser, root_2)); // should make principal items 5, 6 and "save as" item 0 not visible
    }
}
