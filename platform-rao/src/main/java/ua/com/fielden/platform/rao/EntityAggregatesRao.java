package ua.com.fielden.platform.rao;

import java.io.IOException;
import java.util.List;

import ua.com.fielden.platform.dao.IEntityAggregatesDao;
import ua.com.fielden.platform.equery.EntityAggregates;
import ua.com.fielden.platform.equery.fetch;
import ua.com.fielden.platform.equery.interfaces.IQueryOrderedModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * RAO implementing {@link IEntityAggregatesDao} contract.
 *
 * @author TG Team
 *
 */
@EntityType(EntityAggregates.class)
public class EntityAggregatesRao implements IEntityAggregatesDao {
    private final CommonEntityAggregatesRao rao;

    @Inject
    public EntityAggregatesRao(final CommonEntityAggregatesRao rao) {
	this.rao = rao;
    }

    public List<EntityAggregates> listAggregates(final IQueryOrderedModel<EntityAggregates> aggregatesQueryModel, final fetch<EntityAggregates> fetchModel) {
	return rao.getEntities(aggregatesQueryModel, fetchModel);
    }

    @Override
    public IPage<EntityAggregates> firstPage(final int pageCapacity) {
	return rao.firstPage(pageCapacity);
    }

    @Override
    public IPage<EntityAggregates> firstPage(final IQueryOrderedModel<EntityAggregates> model, final fetch<EntityAggregates> fetchModel, final int pageCapacity) {
	return rao.firstPage(model, fetchModel, pageCapacity);
    }

    @Override
    public IPage<EntityAggregates> getPage(final int pageNo, final int pageCapacity) {
	return rao.getPage(pageNo, pageCapacity);
    }

    @Override
    public IPage<EntityAggregates> getPage(final IQueryOrderedModel<EntityAggregates> model, final fetch<EntityAggregates> fetchModel, final int pageNo, final int pageCapacity) {
	return getPage(model, fetchModel, pageNo, 0, pageCapacity);
    }

    @Override
    public IPage<EntityAggregates> getPage(final IQueryOrderedModel<EntityAggregates> model, final fetch<EntityAggregates> fetchModel, final int pageNo, final int pageCount, final int pageCapacity) {
	return rao.getPage(model, fetchModel, pageNo, pageCount, pageCapacity);
    }

    @Override
    public byte[] export(final IQueryOrderedModel<EntityAggregates> model, final fetch<EntityAggregates> fetchModel, final String[] propertyNames, final String[] propertyTitles)
	    throws IOException {
	return rao.export(model, fetchModel, propertyNames, propertyTitles);
    }

    @Override
    public void setUsername(final String username) {
	throw new UnsupportedOperationException("Setting username is not required at the client side, and this fact most likely points to a programming mistake.");
    }

    @Override
    public String getUsername() {
	throw new UnsupportedOperationException("Getting username is not required at the client side, and this fact most likely points to a programming mistake.");
    }
}
