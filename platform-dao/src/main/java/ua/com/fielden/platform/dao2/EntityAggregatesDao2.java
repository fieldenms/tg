package ua.com.fielden.platform.dao2;

import java.io.IOException;
import java.util.List;

import ua.com.fielden.platform.dao.UsernameSetterMixin;
import ua.com.fielden.platform.entity.query.AggregatesFetcher;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

@EntityType(EntityAggregates.class)
public class EntityAggregatesDao2 implements IEntityAggregatesDao2 {

    private final CommonEntityAggregatesDao2 dao;

    private String username;

    @Inject
    protected EntityAggregatesDao2(final CommonEntityAggregatesDao2 dao) {
	this.dao = dao;
    }

//    @SessionRequired
//    public List<EntityAggregates> listAggregates(final AggregatesQueryExecutionModel aggregatesQueryModel) {
//	return dao.getEntities(aggregatesQueryModel);
//    }
//
//    @Override
//    public IPage<EntityAggregates> firstPage(final int pageCapacity) {
//	return dao.firstPage(pageCapacity);
//    }
//
//    @Override
//    public IPage<EntityAggregates> firstPage(final IQueryOrderedModel<EntityAggregates> query, final fetch<EntityAggregates> fetchModel, final int pageCapacity) {
//	return dao.firstPage(query, fetchModel, pageCapacity);
//    }
//
//    @Override
//    public IPage<EntityAggregates> getPage(final int pageNo, final int pageCapacity) {
//	return dao.getPage(pageNo, pageCapacity);
//    }
//
//    @Override
//    public IPage<EntityAggregates> getPage(final IQueryOrderedModel<EntityAggregates> query, final fetch<EntityAggregates> fetchModel, final int pageNo, final int pageCapacity) {
//	return getPage(query, fetchModel, pageNo, 0, pageCapacity);
//    }
//
//    @Override
//    public IPage<EntityAggregates> getPage(final IQueryOrderedModel<EntityAggregates> model, final fetch<EntityAggregates> fetchModel, final int pageNo, final int pageCount, final int pageCapacity) {
//	return dao.getPage(model, fetchModel, pageNo, pageCount, pageCapacity);
//    }
//
//
//    @Override
//    public byte[] export(final IQueryOrderedModel<EntityAggregates> query, final fetch<EntityAggregates> fetchModel, final String[] propertyNames, final String[] propertyTitles) throws IOException {
//	return dao.export(query, fetchModel, propertyNames, propertyTitles);
//    }

    @Override
    public final void setUsername(final String username) {
	try {
	    UsernameSetterMixin.setUsername(username, this, Finder.findFieldByName(getClass(), "username"));
	} catch (final Exception e) {
	    throw new IllegalStateException(e);
	}
    }

    @Override
    public final String getUsername() {
	return username;
    }

    @Override
    public List<EntityAggregates> listAggregates(final AggregatesQueryExecutionModel aggregatesQueryModel) {
	return new AggregatesFetcher(dao.getSession(), dao.getEntityFactory(), dao.getMappingsGenerator(), null, null, getUsername()).list(aggregatesQueryModel, null, null);
    }

    @Override
    public IPage<EntityAggregates> firstPage(final int pageCapacity) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IPage<EntityAggregates> firstPage(final AggregatesQueryExecutionModel query, final int pageCapacity) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IPage<EntityAggregates> getPage(final int pageNo, final int pageCapacity) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IPage<EntityAggregates> getPage(final AggregatesQueryExecutionModel model, final int pageNo, final int pageCapacity) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IPage<EntityAggregates> getPage(final AggregatesQueryExecutionModel model, final int pageNo, final int pageCount, final int pageCapacity) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public byte[] export(final AggregatesQueryExecutionModel query, final String[] propertyNames, final String[] propertyTitles) throws IOException {
	// TODO Auto-generated method stub
	return null;
    }
}