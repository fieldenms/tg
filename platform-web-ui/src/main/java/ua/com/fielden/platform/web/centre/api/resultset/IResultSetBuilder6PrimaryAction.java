package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.actions.multi.EntityMultiActionConfig;

/**
 * This contract serves for result set DSL transition to start adding entity specific primary and secondary actions.
 * There could be no secondary actions without a primary one. This follows a simple logic where if there is a need to have one or more
 * actions associated with an entity then the first of them can and should be treated as primary.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IResultSetBuilder6PrimaryAction<T extends AbstractEntity<?>> extends IResultSetBuilder8CustomPropAssignment<T> {

    IAlsoSecondaryAction<T> addPrimaryAction(final EntityActionConfig actionConfig);

    IAlsoSecondaryAction<T> addPrimaryAction(final EntityMultiActionConfig actionConfig);
}
