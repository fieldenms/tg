package ua.com.fielden.platform.web.centre.api.impl;

import ua.com.fielden.platform.basic.autocompleter.AbstractSearchEntityByKeyWithCentreContext;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.web.centre.CentreContext;

import com.google.inject.Inject;

/**
 * A stub value matcher for testing purposes
 *
 * @author TG Team
 *
 */
public class CustomVehicleMatcher extends AbstractSearchEntityByKeyWithCentreContext<TgVehicle> {

    @Inject
    public CustomVehicleMatcher(final IEntityDao<TgVehicle> dao) {
        super(dao);
    }

    @Override
    protected EntityResultQueryModel<TgVehicle> completeEqlBasedOnContext(final CentreContext<TgVehicle, ?> context, final String searchString, final ICompoundCondition0<TgVehicle> incompleteEql) {
        return null;
    }

}
