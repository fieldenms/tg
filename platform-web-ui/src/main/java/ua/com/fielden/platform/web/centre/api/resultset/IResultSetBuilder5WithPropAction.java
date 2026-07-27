package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.actions.IEntityActionBuilder;
import ua.com.fielden.platform.web.centre.api.actions.multi.EntityMultiActionConfig;
import ua.com.fielden.platform.web.centre.api.actions.multi.EntityMultiActionConfigBuilder;
import ua.com.fielden.platform.web.centre.api.actions.multi.IEntityMultiActionSelector;

import java.util.Optional;
import java.util.function.Supplier;

/// DSL contract for associating one or more property actions with a static column added to an entity centre result set.
/// Cell tap activates the first action group on the column; columns with more than one group additionally expose a triple-dot overflow button that opens a dropdown listing all groups.
///
/// `withAction(...)`, `withMultiAction(...)` and `withActionSupplier(...)` can each be called multiple times on the same property — every call appends a new action group to the column in declaration order.
/// The methods are also mixable: a plain `withAction(a)`, a context-selected `withMultiAction(s, [b, c])` and another `withActionSupplier(sup)` can be chained on the same property to produce three groups.
/// The return type is self-recursive so further DSL calls (`withAction`, `withMultiAction`, `withActionSupplier`, or the inherited `also()`) remain available after each call.
///
/// Note: for static columns the action's `chosen-property` is the column's property name on the row entity, so action producers that resolve a sub-entity from this property path (e.g. to open the referenced entity instead of the row itself) work as expected.
/// For the dynamic-column counterpart, where `chosen-property` is a per-cell binding rather than a row property name, see [IResultSetBuilderDynamicPropsAction].
///
public interface IResultSetBuilder5WithPropAction<T extends AbstractEntity<?>> extends IAlsoProp<T> {

    /// Appends a plain [EntityActionConfig] as a new property-action group on the current column.
    /// An action configuration can be conveniently built by using implementations of [IEntityActionBuilder], which is designed specifically for fluent construction of such configurations.
    /// The contract includes support for entity centre context definition that is specific for an action configuration being constructed.
    ///
    /// Repeatable — call multiple times to add several groups to the same column; the first group is the one invoked on cell tap.
    ///
    IResultSetBuilder5WithPropAction<T> withAction(final EntityActionConfig actionConfig);

    /// Appends an [EntityMultiActionConfig] as a new property-action group on the current column.
    /// A multi-action group runs a [IEntityMultiActionSelector] at runtime to pick which of its sub-actions to invoke for each entity.
    /// A multi-action configuration can be conveniently built by using [EntityMultiActionConfigBuilder].
    ///
    /// Repeatable — call multiple times to add several groups to the same column; freely mixable with [#withAction] and [#withActionSupplier] on the same column.
    ///
    IResultSetBuilder5WithPropAction<T> withMultiAction(final EntityMultiActionConfig multiActionConfig);

    /// A variation of [#withAction] with delayed instantiation of the action configuration until the moment it is used for Web UI rendering.
    /// The main objective for such a delay is to allow all possible registrations, such as registrations of open actions with their corresponding entity types, to complete.
    /// This is necessary to resolve any possible circular references and out-of-order creation of action configurations.
    /// For example, an entity centre that needs to reference some open action defined before that action configuration is registered.
    ///
    /// Repeatable — call multiple times to add several groups to the same column.
    ///
    IResultSetBuilder5WithPropAction<T> withActionSupplier(final Supplier<Optional<EntityActionConfig>> actionConfigSupplier);
}
