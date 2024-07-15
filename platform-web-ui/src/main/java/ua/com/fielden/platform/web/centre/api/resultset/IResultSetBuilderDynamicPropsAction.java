package ua.com.fielden.platform.web.centre.api.resultset;

import java.util.Optional;
import java.util.function.Supplier;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.actions.IEntityActionBuilder;

public interface IResultSetBuilderDynamicPropsAction<T extends AbstractEntity<?>> extends IResultSetBuilderAlsoDynamicProps<T>{

    /**
     * Adds an instance of {@link EntityActionConfig} to the dynamic property definition.
     * <p>
     * Please note that an action configuration can be conveniently built by using implementations of {@link IEntityActionBuilder}, which is designed specifically for fluent construction of such
     * configurations. This contract includes support for entity centre context definition that is specific for an action configuration being constructed.
     *
     * @param actionConfig
     * @return
     */
    IResultSetBuilderAlsoDynamicProps<T> withAction(final EntityActionConfig actionConfig);

    /**
     * A variation of {@link #withAction(EntityActionConfig)} with delayed instantiation of the action configuration until the moment it is used for Web UI rendering.
     * The main objective for such a delay is to allow all possible registrations, such as registrations of open actions with their corresponding entity types, to complete.
     * This is necessary to resolve any possible circular references and out-of-order creation of action configurations.
     * For example, an entity centre that needs to reference some open action defined before that action configuration is registered.
     *
     * @param actionConfigSupplier
     * @return
     */
    IResultSetBuilderAlsoDynamicProps<T> withActionSupplier(final Supplier<Optional<EntityActionConfig>> actionConfigSupplier);
}
