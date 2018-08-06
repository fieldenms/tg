package ua.com.fielden.platform.dao;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityToOpenCompoundMaster;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;

public class CommonOpenCompoundMasterDao<T extends AbstractFunctionalEntityToOpenCompoundMaster<?>> extends CommonEntityDao<T> implements IOpenCompoundMasterAction<T> {

    protected CommonOpenCompoundMasterDao(final IFilter filter) {
        super(filter);
    }

    @Override
    public T save(final T entity) {
        if (!entity.isCalculated()) {
            final AggregatedResultQueryModel workActivityPresentQuery = select()
                .yield().caseWhen().exists(enhnaceEmbededCentreQuery(select(WorkActivity.class), "project", entity.getKey()).model()
                .then().val(1).otherwise().val(0).endAsInt().as("workActivityNumber").modelAsAggregate();
            final EntityAggregates existEntity = coAggregates.getEntity(from(workActivityPresentQuery).model());
            entity.setWorkActivityPresent(((Integer)existEntity.get("workActivityNumber")) > 0);
            entity.setCalculated(true);
        }
        return super.save(entity);
    }
}
