package ua.com.fielden.platform.dao;

import java.io.IOException;
import java.util.List;

import ua.com.fielden.platform.dao2.QueryExecutionModel;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.pagination.IPage2;
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

    public List<EntityAggregates> getAllEntities(final QueryExecutionModel<EntityAggregates> aggregatesQueryModel) {
	return dao.getAllEntities(aggregatesQueryModel);
    }

    @Override
    public IPage2<EntityAggregates> firstPage(final QueryExecutionModel<EntityAggregates> query, final int pageCapacity) {
	return dao.firstPage(query, pageCapacity);
    }

    @Override
    public IPage2<EntityAggregates> getPage(final QueryExecutionModel<EntityAggregates> query, final int pageNo, final int pageCapacity) {
	return getPage(query, pageNo, 0, pageCapacity);
    }

    @Override
    public IPage2<EntityAggregates> getPage(final QueryExecutionModel<EntityAggregates> model, final int pageNo, final int pageCount, final int pageCapacity) {
	return dao.getPage(model, pageNo, pageCount, pageCapacity);
    }

    @Override
    public byte[] export(final QueryExecutionModel<EntityAggregates> query, final String[] propertyNames, final String[] propertyTitles) throws IOException {
	return dao.export(query, propertyNames, propertyTitles);
    }

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
}