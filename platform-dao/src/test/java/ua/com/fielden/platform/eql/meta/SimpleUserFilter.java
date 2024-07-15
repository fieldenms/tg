package ua.com.fielden.platform.eql.meta;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.sample.domain.TeVehicle;
import ua.com.fielden.platform.sample.domain.TeVehicleModel;
import ua.com.fielden.platform.sample.domain.TgAuthor;

public class SimpleUserFilter implements IFilter {

    @Override
    public <ET extends AbstractEntity<?>> ConditionModel enhance(final Class<ET> entityType, final String typeAlias, final String username) {
        if (entityType.equals(TgAuthor.class)) {
            return cond().prop("key").isNotNull().model();    
        } else if (entityType.equals(TeVehicle.class)){
            return cond().prop("key").isNotNull().or().prop("desc").isNotNull().model();
        } else if (entityType.equals(TeVehicleModel.class)){
            return cond().prop("makeKey2").isNotNull().or().prop("desc").isNotNull().model();
        } else {
            return null;
        }
    }
}