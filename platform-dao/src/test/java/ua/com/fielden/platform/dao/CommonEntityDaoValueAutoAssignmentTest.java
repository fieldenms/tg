package ua.com.fielden.platform.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.sample.domain.TgSubSystemDao.DEFAULT_VALUE_FOR_PROPERTY_EXPLANATION;

import org.junit.Test;

import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.persistence.types.EntityWithAutoAssignableProperties;
import ua.com.fielden.platform.sample.domain.ITgPerson;
import ua.com.fielden.platform.sample.domain.TgPerson;
import ua.com.fielden.platform.sample.domain.TgSubSystem;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.utils.IUniversalConstants;

public class CommonEntityDaoValueAutoAssignmentTest extends AbstractDaoTestCase {
    private final String loggedInUser = "LOGGED_IN_USER";
    private final String otherUser = "OTHER_USER";

    @Test
    public void auto_assignment_of_system_user_is_supported() {
        final EntityWithAutoAssignableProperties entity = new_(EntityWithAutoAssignableProperties.class, "VALUE_1");
        assertNull(entity.getUser());
        final EntityWithAutoAssignableProperties savedEntity = save(entity);
        assertNotNull(savedEntity.getUser());
    }

    @Test
    public void auto_assignment_skips_already_assigned_properties() {
        final EntityWithAutoAssignableProperties entity = new_(EntityWithAutoAssignableProperties.class, "VALUE_1");
        final User user = co(User.class).findByKey("USER_1");
        entity.setUser(user);
        final EntityWithAutoAssignableProperties savedEntity = save(entity);
        assertEquals(user, savedEntity.getUser());
    }
    
    @Test
    public void assigned_before_save_user_is_assigned_the_value_of_the_logged_in_user_and_explanation_matces_the_default_value() {
        final ITgPerson coPerson = (ITgPerson) co(TgPerson.class);
        final User user = coPerson.getUser();

        final TgSubSystem ss1 = co(TgSubSystem.class).findByKeyAndFetch(fetchAll(TgSubSystem.class), "SS1");

        assertEquals(user, ss1.getUser());
        assertEquals(DEFAULT_VALUE_FOR_PROPERTY_EXPLANATION, ss1.getExplanation());
    }

    @Test
    public void nulling_out_non_required_properties_is_permitted_and_they_do_not_get_auto_assigned_before_save_for_already_persisted_entities() {
        final TgSubSystem ss1 = save(co(TgSubSystem.class).findByKeyAndFetch(fetchAll(TgSubSystem.class), "SS1").setExplanation(null));

        assertNull(ss1.getExplanation());
    }

    @Test
    public void nulling_out_required_prop_user_is_permitted_for_persistent_entity_due_to_its_declaration_as_assigned_before_save_but_saving_of_such_entity_fails() {
        final TgSubSystem ss1 = co(TgSubSystem.class).findByKeyAndFetch(fetchAll(TgSubSystem.class), "SS1");

        // property value removal should be permitted, despite its declaration as @Required, but...
        assertNotNull(ss1.getUser());
        ss1.setUser(null);
        final MetaProperty<User> propUser = ss1.getProperty("user");
        assertTrue(propUser.isValid());
        assertNull(ss1.getUser());

        // ... saving of such entity should be prevent specifically due to property requiredness
        try {
            save(ss1);
            fail();
        } catch (final EntityCompanionException ex) {
            assertEquals("Property user@ua.com.fielden.platform.sample.domain.TgSubSystem is marked as assignable before save, but had its value removed.", ex.getMessage());
        }

    }

    @Test
    public void nulling_out_and_saving_of_non_required_prop_explanation_is_permitted_for_persistent_entity_even_though_it_is_declared_as_assigned_before_save() {
        final TgSubSystem ss1 = co(TgSubSystem.class).findByKeyAndFetch(fetchAll(TgSubSystem.class), "SS1");

        ss1.setExplanation(null);
        final MetaProperty<User> propUser = ss1.getProperty("explanation");
        assertTrue(propUser.isValid());

        final TgSubSystem savedSs1 = save(ss1);
        assertNull(savedSs1.getExplanation());
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();
        final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
        constants.setNow(dateTime("2014-11-23 02:47:00"));

        save(new_(User.class, "USER_1").setBase(true).setEmail("USER1@unit-test.software").setActive(true));

        final IUser coUser = co(User.class);
        final User lUser = coUser.save(new_(User.class, loggedInUser).setBase(true).setEmail(loggedInUser + "@unit-test.software").setActive(true));
        save(new_(TgPerson.class, loggedInUser).setUser(lUser));
        final User oUser = coUser.save(new_(User.class, otherUser).setBase(true).setEmail(otherUser + "@unit-test.software").setActive(true));
        save(new_(TgPerson.class, otherUser).setUser(oUser));

        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUsername(loggedInUser, getInstance(IUser.class));

        save(new_(TgSubSystem.class, "SS1"));
    }

}