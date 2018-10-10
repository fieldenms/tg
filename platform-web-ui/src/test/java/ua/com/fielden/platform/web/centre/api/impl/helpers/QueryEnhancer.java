package ua.com.fielden.platform.web.centre.api.impl.helpers;

import java.util.Optional;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompleted;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;
import ua.com.fielden.platform.sample.domain.TgWorkOrder;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.IQueryEnhancer;

/**
 * A stub implementation for testing purposes.
 *
 * @author TG Team
 *
 */
public class QueryEnhancer implements IQueryEnhancer<TgWorkOrder> {

    @Override
    public ICompleted<TgWorkOrder> enhanceQuery(final IWhere0<TgWorkOrder> where, final Optional<CentreContext<TgWorkOrder, ?>> context) {
        return where.prop("vehicle").eq().val(context.get().getMasterEntity());
    }

}
