package ua.com.fielden.platform.entity.query.generation;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import static ua.com.fielden.platform.entity.query.fluent.query.select;

public class TestUserFilter implements IFilter {

    @Override
    public <T extends AbstractEntity> EntityResultQueryModel<T> enhance(final Class<T> entityType, final String username) {
	return (EntityResultQueryModel<T>) (entityType.equals(TgVehicle.class) ? select(entityType).where().prop("key").notLike().val("A%").model() : null);
    }
}
