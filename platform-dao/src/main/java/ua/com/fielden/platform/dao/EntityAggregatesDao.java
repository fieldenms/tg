package ua.com.fielden.platform.dao;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;

@EntityType(EntityAggregates.class)
public class EntityAggregatesDao implements IEntityAggregatesOperations {

    private final ICompanionObjectFinder coFinder;

    @Inject
    protected EntityAggregatesDao(final ICompanionObjectFinder coFinder) {
        this.coFinder = coFinder;
    }

    @Override
    public List<EntityAggregates> getAllEntities(final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> aggregatesQueryModel) {
        return coFinder.find(EntityAggregates.class).getAllEntities(aggregatesQueryModel);
    }

    @Override
    public EntityAggregates getEntity(final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> model) {
        return coFinder.find(EntityAggregates.class).getEntity(model);
    }

    @Override
    public List<EntityAggregates> getFirstEntities(final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> aggregatesQueryModel, final int numberOfEntities) {
        return coFinder.find(EntityAggregates.class).getFirstEntities(aggregatesQueryModel, numberOfEntities);
    }

    @Override
    public IPage<EntityAggregates> firstPage(final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> query, final int pageCapacity) {
        return coFinder.find(EntityAggregates.class).firstPage(query, pageCapacity);
    }

    @Override
    public IPage<EntityAggregates> getPage(final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> query, final int pageNo, final int pageCapacity) {
        return getPage(query, pageNo, 0, pageCapacity);
    }

    @Override
    public IPage<EntityAggregates> getPage(final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> model, final int pageNo, final int pageCount, final int pageCapacity) {
        return coFinder.find(EntityAggregates.class).getPage(model, pageNo, pageCount, pageCapacity);
    }

    @Override
    public byte[] export(final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> query, final String[] propertyNames, final String[] propertyTitles)
            throws IOException {
        return coFinder.find(EntityAggregates.class).export(query, propertyNames, propertyTitles);
    }

    @Override
    public final String getUsername() {
        return coFinder.find(EntityAggregates.class).getUser().getKey();
    }

    @Override
    public int count(final AggregatedResultQueryModel model, final Map<String, Object> paramValues) {
        return ((CommonEntityAggregatesDao) coFinder.find(EntityAggregates.class)).count(model, paramValues);
    }

    @Override
    public int count(final AggregatedResultQueryModel model) {
        return ((CommonEntityAggregatesDao) coFinder.find(EntityAggregates.class)).count(model);
    }

    @Override
    public boolean stop() {
        return coFinder.find(EntityAggregates.class).stop();
    }

    @Override
    public Integer progress() {
        return coFinder.find(EntityAggregates.class).progress();
    }
}