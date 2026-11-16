package ua.com.fielden.platform.web.view.master.api.widgets.component;

import org.junit.Test;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;

import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/// Tests for [ComponentRefreshContext], with which conditions for refreshing components in entity masters determine the properties changed by the user.
///
/// The entity of a context is irrelevant to these tests, so contexts are created without one.
///
public class ComponentRefreshContextTest {

    @Test
    public void property_is_changed_only_if_it_is_among_the_changed_properties() {
        final var context = new ComponentRefreshContext<TgPersistentEntityWithProperties>(null, Set.of("integerProp", "entityProp"));

        assertTrue(context.isChanged("integerProp"));
        assertTrue(context.isChanged("entityProp"));
        assertFalse(context.isChanged("bigDecimalProp"));
    }

    @Test
    public void changed_property_can_be_identified_by_a_char_sequence_other_than_a_string_as_a_metamodel_reference_is() {
        final var context = new ComponentRefreshContext<TgPersistentEntityWithProperties>(null, Set.of("integerProp"));

        assertTrue(context.isChanged(new StringBuilder("integerProp")));
    }

}
