package ua.com.fielden.platform.web.centre.api.query_enhancer;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;

/**
 *
 * This contract is part of Entity Centre DSL, which provides a way to specify an EQL enhancer.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IQueryEnhancerSetter<T extends AbstractEntity<?>> {

    void setQueryEnhancer(final Class<? extends IQueryEnhancer<T>> type, final CentreContextConfig contextConfig);
}
