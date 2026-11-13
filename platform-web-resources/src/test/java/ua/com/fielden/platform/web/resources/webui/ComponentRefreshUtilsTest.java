package ua.com.fielden.platform.web.resources.webui;

import org.junit.Test;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;
import ua.com.fielden.platform.web.view.master.api.widgets.component.IComponentRefreshCondition;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.web.resources.webui.ComponentRefreshUtils.componentsToRefresh;

/// Tests for [ComponentRefreshUtils], which determines the components of an entity master to refresh after a validation of the master entity.
///
/// Conditions are evaluated as they are declared for the components of a master, keyed by property names and component keys.
///
public class ComponentRefreshUtilsTest {

    @Test
    public void components_to_refresh_are_those_whose_conditions_hold_for_the_validated_entity_and_the_properties_changed_by_the_user() {
        final var entity = new TgPersistentEntityWithProperties();
        final Map<String, Object> modifHolder = Map.of(
                "@@changedProps", List.of("integerProp"),
                "@@touchedProps", List.of("bigDecimalProp", "integerProp"));
        final Map<String, IComponentRefreshCondition<TgPersistentEntityWithProperties>> conditions = Map.of(
                "integerProp", context -> context.isChanged("integerProp"),
                "component-0", context -> context.isChanged("bigDecimalProp"),
                "component-1", context -> context.entity() == entity);

        assertEquals(Set.of("integerProp", "component-1"), Set.copyOf(componentsToRefresh(conditions, entity, modifHolder)));
    }

    @Test
    public void no_properties_are_changed_by_the_user_if_the_modification_holder_does_not_have_them() {
        final var entity = new TgPersistentEntityWithProperties();
        final Map<String, Object> modifHolder = Map.of(
                "@@touchedProps", List.of("integerProp"),
                "integerProp", Map.of("val", 5));
        final Map<String, IComponentRefreshCondition<TgPersistentEntityWithProperties>> conditions = Map.of(
                "integerProp", context -> context.isChanged("integerProp"),
                "component-0", context -> context.changedProps().isEmpty());

        assertEquals(List.of("component-0"), componentsToRefresh(conditions, entity, modifHolder));
    }

}
