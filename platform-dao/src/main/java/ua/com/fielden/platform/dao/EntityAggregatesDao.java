package ua.com.fielden.platform.dao;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

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
        try(final Stream<EntityAggregates> stream = stream(aggregatesQueryModel)) {
            return stream.collect(toList());
        }
    }

    @Override
    public EntityAggregates getEntity(final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> model) {
        return coFinder.findAsReader(EntityAggregates.class, true).getEntity(model);
    }

    @Override
    public List<EntityAggregates> getFirstEntities(final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> aggregatesQueryModel, final int numberOfEntities) {
        try(final Stream<EntityAggregates> stream = stream(aggregatesQueryModel, numberOfEntities).limit(numberOfEntities)) {
            return stream.collect(toList());
        }
    }

    @Override
    public IPage<EntityAggregates> firstPage(final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> query, final int pageCapacity) {
        return coFinder.findAsReader(EntityAggregates.class, true).firstPage(query, pageCapacity);
    }

    @Override
    public IPage<EntityAggregates> getPage(final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> query, final int pageNo, final int pageCapacity) {
        return getPage(query, pageNo, 0, pageCapacity);
    }

    @Override
    public IPage<EntityAggregates> getPage(final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> model, final int pageNo, final int pageCount, final int pageCapacity) {
        return coFinder.findAsReader(EntityAggregates.class, true).getPage(model, pageNo, pageCount, pageCapacity);
    }

    @Override
    public byte[] export(final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> query, final String[] propertyNames, final String[] propertyTitles)
            throws IOException {
        return coFinder.find(EntityAggregates.class, true).export(query, propertyNames, propertyTitles);
    }

    @Override
    public final String getUsername() {
        return coFinder.find(EntityAggregates.class, true).getUser().getKey();
    }

    @Override
    public int count(final AggregatedResultQueryModel model, final Map<String, Object> paramValues) {
        return ((CommonEntityAggregatesDao) coFinder.find(EntityAggregates.class, true)).count(model, paramValues);
    }

    @Override
    public int count(final AggregatedResultQueryModel model) {
        return ((CommonEntityAggregatesDao) coFinder.find(EntityAggregates.class, true)).count(model);
    }

    @Override
    public boolean stop() {
        return coFinder.find(EntityAggregates.class, true).stop();
    }

    @Override
    public Optional<Integer> progress() {
        return coFinder.find(EntityAggregates.class, true).progress();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Stream<EntityAggregates> stream(final QueryExecutionModel<EntityAggregates, ?> qem, final int fetchSize) {
        return coFinder.findAsReader(EntityAggregates.class, true).stream(qem, fetchSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Stream<EntityAggregates> stream(final QueryExecutionModel<EntityAggregates, ?> qem) {
        return coFinder.findAsReader(EntityAggregates.class, true).stream(qem);
    }
}