package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.actions.multi.EntityMultiActionConfig;

/**
 * This contract serves for result set DSL transition to start adding entity specific primary and secondary actions.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IResultSetBuilder7SecondaryAction<T extends AbstractEntity<?>> {


    IAlsoSecondaryAction<T> addSecondaryAction(final EntityActionConfig actionConfig);

    IAlsoSecondaryAction<T> addSecondaryAction(final EntityMultiActionConfig actionConfig);
}
