package ua.com.fielden.platform.web.view.master.api.widgets.component;

import ua.com.fielden.platform.entity.AbstractEntity;

import java.util.Set;

/// The context in which an [IComponentRefreshCondition] decides whether to refresh a component after a validation of the master entity.
///
/// The properties whose values the user changed are those changed since the previous validation, which the dirtiness of properties does not tell:
/// each validation applies all the changes that are not yet saved to the saved entity, so a property stays dirty from its first change until the entity is saved,
/// and every property of a new entity is dirty.
///
/// @param entity the entity produced by the validation, with the changes of the user applied, and the values that definers set in turn
/// @param changedProps the names of the properties whose values the user changed
/// @param <T> the entity type of the master
///
public record ComponentRefreshContext<T extends AbstractEntity<?>>(T entity, Set<String> changedProps) {

    public ComponentRefreshContext {
        changedProps = Set.copyOf(changedProps);
    }

    /// Determines whether the user changed the value of property `propPath`, which is a property name or a metamodel reference, such as `Vehicle_.model()`.
    ///
    public boolean isChanged(final CharSequence propPath) {
        return changedProps.contains(propPath.toString());
    }

}
