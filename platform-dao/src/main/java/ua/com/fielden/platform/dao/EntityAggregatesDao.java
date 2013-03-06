package ua.com.fielden.platform.dao;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

@EntityType(EntityAggregates.class)
public class EntityAggregatesDao implements IEntityAggregatesDao {

    private final CommonEntityAggregatesDao dao;

    @Inject
    private IUserProvider up;

    @Inject
    protected EntityAggregatesDao(final CommonEntityAggregatesDao dao) {
	this.dao = dao;
    }

    @Override
    public List<EntityAggregates> getAllEntities(final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> aggregatesQueryModel) {
	return dao.getAllEntities(aggregatesQueryModel);
    }

    @Override
    public EntityAggregates getEntity(final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> model) {
        return dao.getEntity(model);
    }

    @Override
    public List<EntityAggregates> getFirstEntities(final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> aggregatesQueryModel, final int numberOfEntities) {
	return dao.getFirstEntities(aggregatesQueryModel, numberOfEntities);
    }

    @Override
    public IPage<EntityAggregates> firstPage(final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> query, final int pageCapacity) {
	return dao.firstPage(query, pageCapacity);
    }

    @Override
    public IPage<EntityAggregates> getPage(final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> query, final int pageNo, final int pageCapacity) {
	return getPage(query, pageNo, 0, pageCapacity);
    }

    @Override
    public IPage<EntityAggregates> getPage(final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> model, final int pageNo, final int pageCount, final int pageCapacity) {
	return dao.getPage(model, pageNo, pageCount, pageCapacity);
    }

    @Override
    public byte[] export(final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> query, final String[] propertyNames, final String[] propertyTitles) throws IOException {
	return dao.export(query, propertyNames, propertyTitles);
    }

    @Override
    public final String getUsername() {
	return up.getUser().getKey();
    }

    @Override
    public int count(final AggregatedResultQueryModel model, final Map<String, Object> paramValues) {
	return dao.count(model, paramValues);
    }

    @Override
    public int count(final AggregatedResultQueryModel model) {
	return dao.count(model);
    }

}