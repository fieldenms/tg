package ua.com.fielden.platform.web.centre.api.context;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract that allows one to specify context for parent centre. This will work only if the view for which context is being build is in the insertion point.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IEntityCentreContextSelector6<T extends AbstractEntity<?>> extends IExtendedEntityCentreContextWithFunctionSelector<T> {

    /**
     * Extends this view context with the parent centre context if this view is in the insertion point.
     *
     * @param parentCentreContext
     * @return
     */
    IExtendedEntityCentreContextWithFunctionSelector<T> extendWithParentCentreContext(CentreContextConfig parentCentreContext);
}
