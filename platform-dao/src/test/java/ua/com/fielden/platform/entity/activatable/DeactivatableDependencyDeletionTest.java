package ua.com.fielden.platform.entity.activatable;

import org.junit.Test;
import ua.com.fielden.platform.entity.annotation.DeactivatableDependencies;
import ua.com.fielden.platform.sample.domain.TgAuthoriser;
import ua.com.fielden.platform.sample.domain.TgPerson;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import static org.junit.Assert.assertTrue;

/// This test covers the deletion of [deactivatable dependencies][DeactivatableDependencies].
///
public class DeactivatableDependencyDeletionTest extends AbstractDaoTestCase implements WithActivatabilityTestUtils {

    @Test
    public void deletion_of_A_which_is_a_deactivatable_dependency_of_B_when_B_is_active_does_not_affect_refCount_of_B() {
        final var person = save(new_(TgPerson.class, "JD").setActive(true).setRefCount(10));
        final var authoriser = save(new_composite(TgAuthoriser.class, person).setActive(true));

        assertTrue(person.isActive());
        assertTrue(authoriser.isActive());
        assertRefCount(10, person);
        co$(TgAuthoriser.class).delete(authoriser);
        assertRefCount(10, person);
    }

}
