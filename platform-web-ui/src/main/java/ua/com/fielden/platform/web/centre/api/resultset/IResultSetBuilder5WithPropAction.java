package ua.com.fielden.platform.web.centre.api.resultset;

import java.util.Optional;
import java.util.function.Supplier;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.actions.IEntityActionBuilder;
import ua.com.fielden.platform.web.centre.api.actions.multi.EntityMultiActionConfig;
import ua.com.fielden.platform.web.centre.api.actions.multi.EntityMultiActionConfigBuilder;
import ua.com.fielden.platform.web.centre.api.actions.multi.IEntityMultiActionSelector;

/**
 * This contract is responsible for providing a way to associate actions with properties that are added to an entity centre result set.
 * <p>
 * By default, actions for properties that are constitute a result set are activated by tapping their representation.
 * However, this is behaviour could be changed by specific implementations of the actual UI mechanism.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IResultSetBuilder5WithPropAction<T extends AbstractEntity<?>> extends IAlsoProp<T> {

    /**
     * Adds an instance of {@link EntityActionConfig} to the result set definition.
     * <p>
     * Please note that an action configuration can be conveniently built by using implementations of {@link IEntityActionBuilder}, which is designed specifically for fluent construction of such
     * configurations. This contract includes support for entity centre context definition that is specific for an action configuration being constructed.
     *
     * @param actionConfig
     * @return
     */
    IAlsoProp<T> withAction(final EntityActionConfig actionConfig);

    /**
     * Adds an instance of {@link EntityMultiActionConfig} to the result set definition.
     * <p>
     * Please note that a multi action configuration can be conveniently built by using {@link EntityMultiActionConfigBuilder}, which is designed specifically for fluent construction of such
     * configurations. This contract includes support for adding new {@link EntityActionConfig} and for {@link IEntityMultiActionSelector} type definition.
     *
     * @param multiActionConfig
     * @return
     */
    IAlsoProp<T> withAction(final EntityMultiActionConfig multiActionConfig);

    /**
     * A variation of {@link #withAction(EntityActionConfig)} with delayed instantiation of the action configuration until the moment it is used for Web UI rendering.
     * The main objective for such a delay is to allow all possible registrations, such as registrations of open actions with their corresponding entity types, to complete.
     * This is necessary to resolve any possible circular references and out-of-order creation of action configurations.
     * For example, an entity centre that needs to reference some open action defined before that action configuration is registered.
     *
     * @param actionConfigSupplier
     * @return
     */
    IAlsoProp<T> withActionSupplier(final Supplier<Optional<EntityActionConfig>> actionConfigSupplier);
}
