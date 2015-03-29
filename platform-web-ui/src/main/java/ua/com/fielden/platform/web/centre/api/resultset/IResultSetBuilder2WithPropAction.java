package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.actions.IEntityActionBuilder;

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
public interface IResultSetBuilder2WithPropAction<T extends AbstractEntity<?>> extends IAlsoProp<T> {

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
}
