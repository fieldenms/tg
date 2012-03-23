package ua.com.fielden.platform.criteria.generator.impl;

import java.io.IOException;
import java.util.List;

import ua.com.fielden.platform.dao.IEntityAggregatesDao;
import ua.com.fielden.platform.dao2.AggregatesQueryExecutionModel;
import ua.com.fielden.platform.dao2.IEntityAggregatesDao2;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.pagination.IPage2;

/**
 * {@link IEntityAggregatesDao} stub instance that is used only for testing purposes.
 *
 * @author TG Team
 *
 */
public class StubEntityAggregatesDao implements IEntityAggregatesDao2 {

    @Override
    public void setUsername(final String username) {
    }

    @Override
    public String getUsername() {
	return null;
    }

    @Override
    public List<EntityAggregates> getAggregates(final AggregatesQueryExecutionModel aggregatesQueryModel) {
	return null;
    }

    @Override
    public IPage2<EntityAggregates> firstPage(final AggregatesQueryExecutionModel query, final int pageCapacity) {
	return null;
    }

    @Override
    public IPage2<EntityAggregates> firstPage(final AggregatesQueryExecutionModel model, final AggregatesQueryExecutionModel summaryModel, final int pageCapacity) {
	return null;
    }

    @Override
    public IPage2<EntityAggregates> getPage(final AggregatesQueryExecutionModel model, final int pageNo, final int pageCapacity) {
	return null;
    }

    @Override
    public IPage2<EntityAggregates> getPage(final AggregatesQueryExecutionModel model, final int pageNo, final int pageCount, final int pageCapacity) {
	return null;
    }

    @Override
    public byte[] export(final AggregatesQueryExecutionModel query, final String[] propertyNames, final String[] propertyTitles) throws IOException {
	return null;
    }
}