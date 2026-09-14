package ua.com.fielden.platform.web.centre.api.resultset;

import java.util.Optional;
import java.util.function.Supplier;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.actions.IEntityActionBuilder;
import ua.com.fielden.platform.web.centre.api.actions.multi.EntityMultiActionConfig;
import ua.com.fielden.platform.web.centre.api.actions.multi.EntityMultiActionConfigBuilder;
import ua.com.fielden.platform.web.centre.api.actions.multi.IEntityMultiActionSelector;

/// DSL contract for associating one or more property actions with a dynamic-column group added via `addProps(...)`.
///
/// Mirrors [IResultSetBuilder5WithPropAction] for static columns:
/// `withAction(...)`, `withMultiAction(...)` and `withActionSupplier(...)` can each be called multiple times on the same dynamic-column group, each call appending a new action group in declaration order.
/// The methods are also mixable on the same column.
/// Cell tap activates the first group's currently-selected sub-action; columns with more than one group additionally expose a triple-dot overflow button that opens a dropdown listing all groups.
///
/// Note: for dynamic columns the action's `chosen-property` is bound to the dynamic per-cell value (`[[item.GROUP_PROP_VALUE]]`, the column's group key), not to a property name on the row entity.
/// Action producers that rely on a property path will not resolve to an entity through this binding; such producers typically fall back to using the row entity itself.
///
public interface IResultSetBuilderDynamicPropsAction<T extends AbstractEntity<?>> extends IResultSetBuilderAlsoDynamicProps<T>{

    /// Adds an instance of [EntityActionConfig] to the dynamic property definition.
    /// Multiple calls to this method (or to [#withMultiAction] / [#withActionSupplier]) on the same property accumulate into an ordered list of action groups.
    /// The first group becomes the default action invoked on cell tap; subsequent groups are reachable through the EGI cell's overflow dropdown.
    ///
    /// An action configuration can be conveniently built by using implementations of [IEntityActionBuilder], which is designed specifically for fluent construction of such configurations.
    /// This contract includes support for entity centre context definition that is specific for an action configuration being constructed.
    ///
    IResultSetBuilderDynamicPropsAction<T> withAction(final EntityActionConfig actionConfig);

    /// Adds an instance of [EntityMultiActionConfig] to the dynamic property definition.
    /// Multiple calls accumulate into an ordered list of action groups, mixing freely with [#withAction] and [#withActionSupplier] on the same property.
    ///
    /// A multi-action configuration can be conveniently built using [EntityMultiActionConfigBuilder]; selection between sub-actions at runtime is driven by the configured [IEntityMultiActionSelector].
    ///
    IResultSetBuilderDynamicPropsAction<T> withMultiAction(final EntityMultiActionConfig multiActionConfig);

    /// A variation of [#withAction] with delayed instantiation of the action configuration until the moment it is used for Web UI rendering.
    /// The main objective for such a delay is to allow all possible registrations, such as registrations of open actions with their corresponding entity types, to complete.
    /// This is necessary to resolve any possible circular references and out-of-order creation of action configurations.
    /// For example, an entity centre that needs to reference some open action defined before that action configuration is registered.
    ///
    IResultSetBuilderDynamicPropsAction<T> withActionSupplier(final Supplier<Optional<EntityActionConfig>> actionConfigSupplier);
}
