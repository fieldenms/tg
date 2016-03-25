package ua.com.fielden.platform.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;

import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.sample.domain.ITgPerson;
import ua.com.fielden.platform.sample.domain.TgPerson;
import ua.com.fielden.platform.sample.domain.TgSubSystem;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test.AbstractDomainDrivenTestCase;
import ua.com.fielden.platform.test.PlatformTestDomainTypes;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;
import ua.com.fielden.platform.utils.IUniversalConstants;

public class AssignPropertiesBeforeTestTest extends AbstractDomainDrivenTestCase {
    private final String loggedInUser = "LOGGED IN USER";
    private final String otherUser = "OTHER USER";

    @Test
    public void assigned_before_save_user_should_be_the_logged_in_user_and_explanation_should_match_default_value() {
        final ITgPerson coPerson = (ITgPerson) ao(TgPerson.class);
        final User user = coPerson.getUser();

        final TgSubSystem ss1 = ao(TgSubSystem.class).findByKeyAndFetch(fetchAll(TgSubSystem.class), "SS1");

        assertEquals(user, ss1.getUser());
        assertEquals("Default explanation", ss1.getExplanation());
    }

    @Test
    public void nulling_out_non_required_prop_explanation_should_be_permitted_and_it_should_not_get_auto_assigned_before_save_for_already_persisted_entity() {
        final TgSubSystem ss1 = save(ao(TgSubSystem.class).findByKeyAndFetch(fetchAll(TgSubSystem.class), "SS1").setExplanation(null));

        assertNull(ss1.getExplanation());
    }

    @Test
    public void nulling_out_required_prop_user_should_be_permitted_for_persistent_entity_even_though_it_is_declared_as_assigned_before_save_but_saving_of_such_entity_should_not() {
        final TgSubSystem ss1 = ao(TgSubSystem.class).findByKeyAndFetch(fetchAll(TgSubSystem.class), "SS1");

        // property value removal should be permitted, despite its declaration as @Required, but...
        ss1.setUser(null);
        final MetaProperty<User> propUser = ss1.getProperty("user");
        assertTrue(propUser.isValid());

        // ... saving of such entity should be prevent specifically due to property requiredness
        try {
            save(ss1);
            fail();
        } catch (final EntityCompanionException ex) {
            assertEquals("Property user@ua.com.fielden.platform.sample.domain.TgSubSystem is marked as assignable before save, but had its value removed.", ex.getMessage());
        }

    }

    @Test
    public void nulling_out_and_saving_of_non_required_prop_explanation_should_be_permitted_for_persistent_entity_even_though_it_is_declared_as_assigned_before_save() {
        final TgSubSystem ss1 = ao(TgSubSystem.class).findByKeyAndFetch(fetchAll(TgSubSystem.class), "SS1");

        ss1.setExplanation(null);
        final MetaProperty<User> propUser = ss1.getProperty("explanation");
        assertTrue(propUser.isValid());

        final TgSubSystem savedSs1 = save(ss1);
        assertNull(savedSs1.getExplanation());
    }


    @Override
    protected void populateDomain() {
        final UniversalConstantsForTesting constants = (UniversalConstantsForTesting) getInstance(IUniversalConstants.class);
        constants.setNow(dateTime("2014-11-23 02:47:00"));

        save(new_(TgPerson.class, loggedInUser).setUsername(loggedInUser).setBase(true));
        save(new_(TgPerson.class, otherUser).setUsername(otherUser).setBase(true));

        final IUserProvider up = getInstance(IUserProvider.class);
        up.setUsername(loggedInUser, getInstance(IUser.class));

        save(new_(TgSubSystem.class, "SS1"));
    }

    @Override
    protected List<Class<? extends AbstractEntity<?>>> domainEntityTypes() {
        return PlatformTestDomainTypes.entityTypes;
    }
}