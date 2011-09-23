package ua.com.fielden.platform.criteria.generator.impl;

import java.io.IOException;
import java.util.List;

import ua.com.fielden.platform.dao.IEntityAggregatesDao;
import ua.com.fielden.platform.equery.EntityAggregates;
import ua.com.fielden.platform.equery.fetch;
import ua.com.fielden.platform.equery.interfaces.IQueryOrderedModel;
import ua.com.fielden.platform.pagination.IPage;

/**
 * {@link IEntityAggregatesDao} stub instance that is used only for testing purposes.
 * 
 * @author TG Team
 *
 */
public class StubEntityAggregatesDao implements IEntityAggregatesDao {

    @Override
    public void setUsername(final String username) {
	// TODO Auto-generated method stub

    }

    @Override
    public String getUsername() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public List<EntityAggregates> listAggregates(final IQueryOrderedModel<EntityAggregates> aggregatesQueryModel, final fetch<EntityAggregates> fetchModel) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IPage<EntityAggregates> firstPage(final int pageCapacity) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IPage<EntityAggregates> firstPage(final IQueryOrderedModel<EntityAggregates> query, final fetch<EntityAggregates> fetchModel, final int pageCapacity) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IPage<EntityAggregates> getPage(final int pageNo, final int pageCapacity) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IPage<EntityAggregates> getPage(final IQueryOrderedModel<EntityAggregates> model, final fetch<EntityAggregates> fetchModel, final int pageNo, final int pageCapacity) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IPage<EntityAggregates> getPage(final IQueryOrderedModel<EntityAggregates> model, final fetch<EntityAggregates> fetchModel, final int pageNo, final int pageCount, final int pageCapacity) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public byte[] export(final IQueryOrderedModel<EntityAggregates> query, final fetch<EntityAggregates> fetchModel, final String[] propertyNames, final String[] propertyTitles) throws IOException {
	// TODO Auto-generated method stub
	return null;
    }

}
