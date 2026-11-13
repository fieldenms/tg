package ua.com.fielden.platform.web.resources.webui;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.widgets.component.ComponentRefreshContext;
import ua.com.fielden.platform.web.view.master.api.widgets.component.IComponentRefreshCondition;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toUnmodifiableSet;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.getOriginalType;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.web.factories.webui.ResourceFactoryUtils.getEntityMaster;

/// Utilities for determining the components of an entity master to refresh after a validation of the master entity,
/// according to the conditions declared for them with `refreshWhen`, which are [IComponentRefreshCondition]s.
///
/// The keys of the components to refresh are returned to the client in the custom object of the validation result, under `refreshedComponents`,
/// as the indices of property actions are returned under `propertyActionIndices` by [MultiActionUtils].
/// The server does not keep the state of the entity between validations, so the client provides the names of the properties whose values the user changed
/// in the modification holder of the validation, under `@@changedProps`.
///
public class ComponentRefreshUtils {
    private static final String CHANGED_PROPS = "@@changedProps";
    private static final String REFRESHED_COMPONENTS = "refreshedComponents";

    /// Returns the keys of the components of the master for `entity`, produced by a validation with modification holder `modifHolder`, whose refresh conditions hold.
    ///
    static <T extends AbstractEntity<?>> T2<String, Object> createRefreshedComponentsForMaster(final T entity, final Map<String, Object> modifHolder, final IWebUiConfig webUiConfig) {
        final Class<T> entityType = getOriginalType(entity.getType());
        final EntityMaster<T> master = (EntityMaster<T>) getEntityMaster(entityType, webUiConfig);
        return t2(REFRESHED_COMPONENTS, master == null ? List.of() : componentsToRefresh(master.getComponentRefreshConditions(), entity, modifHolder));
    }

    /// Returns the keys of `conditions` that hold for `entity`, produced by a validation with modification holder `modifHolder`.
    ///
    static <T extends AbstractEntity<?>> List<String> componentsToRefresh(final Map<String, ? extends IComponentRefreshCondition<T>> conditions, final T entity, final Map<String, Object> modifHolder) {
        final var context = new ComponentRefreshContext<>(entity, changedProps(modifHolder));
        return conditions.entrySet().stream()
                .filter(entry -> entry.getValue().shouldRefresh(context))
                .map(Map.Entry::getKey)
                .toList();
    }

    /// Returns the names of the properties whose values the user changed, which the client provides in `modifHolder` under `@@changedProps`,
    /// or no names if `modifHolder` does not have them.
    ///
    private static Set<String> changedProps(final Map<String, Object> modifHolder) {
        return modifHolder.get(CHANGED_PROPS) instanceof List<?> names
               ? names.stream().map(String.class::cast).collect(toUnmodifiableSet())
               : Set.of();
    }

}
