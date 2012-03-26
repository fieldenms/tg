package ua.com.fielden.platform.dao;

import java.io.IOException;
import java.util.List;

import ua.com.fielden.platform.equery.EntityAggregates;
import ua.com.fielden.platform.equery.fetch;
import ua.com.fielden.platform.equery.interfaces.IQueryOrderedModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

@EntityType(EntityAggregates.class)
public class EntityAggregatesDao implements IEntityAggregatesDao {

    private final CommonEntityAggregatesDao dao;

    private String username;

    @Inject
    protected EntityAggregatesDao(final CommonEntityAggregatesDao dao) {
	this.dao = dao;
    }

    public List<EntityAggregates> listAggregates(final IQueryOrderedModel<EntityAggregates> aggregatesQueryModel, final fetch<EntityAggregates> fetchModel) {
	return dao.getEntities(aggregatesQueryModel, fetchModel);
    }

    @Override
    public IPage<EntityAggregates> firstPage(final int pageCapacity) {
	return dao.firstPage(pageCapacity);
    }

    @Override
    public IPage<EntityAggregates> firstPage(final IQueryOrderedModel<EntityAggregates> query, final fetch<EntityAggregates> fetchModel, final int pageCapacity) {
	return dao.firstPage(query, fetchModel, pageCapacity);
    }

    @Override
    public IPage<EntityAggregates> getPage(final int pageNo, final int pageCapacity) {
	return dao.getPage(pageNo, pageCapacity);
    }

    @Override
    public IPage<EntityAggregates> getPage(final IQueryOrderedModel<EntityAggregates> query, final fetch<EntityAggregates> fetchModel, final int pageNo, final int pageCapacity) {
	return getPage(query, fetchModel, pageNo, 0, pageCapacity);
    }

    @Override
    public IPage<EntityAggregates> getPage(final IQueryOrderedModel<EntityAggregates> model, final fetch<EntityAggregates> fetchModel, final int pageNo, final int pageCount, final int pageCapacity) {
	return dao.getPage(model, fetchModel, pageNo, pageCount, pageCapacity);
    }


    @Override
    public byte[] export(final IQueryOrderedModel<EntityAggregates> query, final fetch<EntityAggregates> fetchModel, final String[] propertyNames, final String[] propertyTitles) throws IOException {
	return dao.export(query, fetchModel, propertyNames, propertyTitles);
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
