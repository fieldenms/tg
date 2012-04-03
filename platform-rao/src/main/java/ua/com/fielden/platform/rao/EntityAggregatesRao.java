package ua.com.fielden.platform.rao;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.dao.IEntityAggregatesDao2;
import ua.com.fielden.platform.dao2.QueryExecutionModel;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.pagination.IPage2;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * RAO implementing {@link IEntityAggregatesDao2} contract.
 *
 * @author TG Team
 *
 */
@EntityType(EntityAggregates.class)
public class EntityAggregatesRao implements IEntityAggregatesDao2 {
    private final CommonEntityAggregatesRao rao;

    @Inject
    public EntityAggregatesRao(final CommonEntityAggregatesRao rao) {
	this.rao = rao;
    }

//    public List<EntityAggregates> listAggregates(final IQueryOrderedModel<EntityAggregates> aggregatesQueryModel, final fetch<EntityAggregates> fetchModel) {
//	return rao.getEntities(aggregatesQueryModel, fetchModel);
//    }
//
//    @Override
//    public IPage<EntityAggregates> firstPage(final int pageCapacity) {
//	return rao.firstPage(pageCapacity);
//    }
//
//    @Override
//    public IPage<EntityAggregates> firstPage(final IQueryOrderedModel<EntityAggregates> model, final fetch<EntityAggregates> fetchModel, final int pageCapacity) {
//	return rao.firstPage(model, fetchModel, pageCapacity);
//    }
//
//    @Override
//    public IPage<EntityAggregates> getPage(final int pageNo, final int pageCapacity) {
//	return rao.getPage(pageNo, pageCapacity);
//    }
//
//    @Override
//    public IPage<EntityAggregates> getPage(final IQueryOrderedModel<EntityAggregates> model, final fetch<EntityAggregates> fetchModel, final int pageNo, final int pageCapacity) {
//	return getPage(model, fetchModel, pageNo, 0, pageCapacity);
//    }
//
//    @Override
//    public IPage<EntityAggregates> getPage(final IQueryOrderedModel<EntityAggregates> model, final fetch<EntityAggregates> fetchModel, final int pageNo, final int pageCount, final int pageCapacity) {
//	return rao.getPage(model, fetchModel, pageNo, pageCount, pageCapacity);
//    }
//
//    @Override
//    public byte[] export(final IQueryOrderedModel<EntityAggregates> model, final fetch<EntityAggregates> fetchModel, final String[] propertyNames, final String[] propertyTitles)
//	    throws IOException {
//	return rao.export(model, fetchModel, propertyNames, propertyTitles);
//    }

    @Override
    public void setUsername(final String username) {
	throw new UnsupportedOperationException("Setting username is not required at the client side, and this fact most likely points to a programming mistake.");
    }

    @Override
    public String getUsername() {
	throw new UnsupportedOperationException("Getting username is not required at the client side, and this fact most likely points to a programming mistake.");
    }

    @Override
    public List<EntityAggregates> getAllEntities(final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> aggregatesQueryModel) {
	return rao.getAllEntities(aggregatesQueryModel);
    }

    @Override
    public IPage2<EntityAggregates> firstPage(final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> query, final int pageCapacity) {
	return rao.firstPage(query, pageCapacity);
    }

    @Override
    public IPage2<EntityAggregates> getPage(final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> model, final int pageNo, final int pageCapacity) {
	return rao.getPage(model, pageNo, pageCapacity);
    }

    @Override
    public IPage2<EntityAggregates> getPage(final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> model, final int pageNo, final int pageCount, final int pageCapacity) {
	return rao.getPage(model, pageNo, pageCount, pageCapacity);
    }

    @Override
    public byte[] export(final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> query, final String[] propertyNames, final String[] propertyTitles) throws IOException {
	return rao.export(query, propertyNames, propertyTitles);
    }

    @Override
    public int count(final AggregatedResultQueryModel model, final Map<String, Object> paramValues) {
	return rao.count(model, paramValues);
    }

    @Override
    public int count(final AggregatedResultQueryModel model) {
	return count(model, Collections.<String, Object> emptyMap());
    }
}
