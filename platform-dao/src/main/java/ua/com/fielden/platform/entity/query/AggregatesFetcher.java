package ua.com.fielden.platform.entity.query;

import java.util.List;

import org.hibernate.Session;

import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao2.AggregatesQueryExecutionModel;
import ua.com.fielden.platform.dao2.MappingsGenerator;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.generation.DbVersion;


/**
 * This class contains the Hibernate driven implementation of getting results from the provided IQueryOrderedModel.
 *
 * @author TG Team
 *
 * @param <E>
 */
public final class AggregatesFetcher {
    private final EntityFetcher fetcher;

    public AggregatesFetcher(final Session session, final EntityFactory entityFactory, final MappingsGenerator mappingsGenerator, final DbVersion dbVersion, final IFilter filter, final String username) {
	this.fetcher = new EntityFetcher(session, entityFactory, mappingsGenerator, dbVersion, filter, username);
    }

    @SessionRequired
    public List<EntityAggregates> list(final AggregatesQueryExecutionModel queryModel, final Integer pageNumber, final Integer pageCapacity) {
	try {
	    return fetcher.instantiateFromContainers(listContainers(queryModel, pageNumber, pageCapacity), queryModel.isLightweight());
	} catch (final Exception e) {
	    e.printStackTrace();
	    throw new IllegalStateException(e);
	}
    }

    public List<EntityAggregates> list(final AggregatesQueryExecutionModel queryModel) {
	return list(queryModel, null, null);
    }

    @SessionRequired
    protected List<EntityContainer<EntityAggregates>> listContainers(final AggregatesQueryExecutionModel queryModel, final Integer pageNumber, final Integer pageCapacity) throws Exception {
	final QueryModelResult<EntityAggregates> modelResult = new ModelResultProducer().getModelResult(queryModel, fetcher.getDbVersion(), fetcher.getMappingsGenerator(), fetcher.getFilter(), fetcher.getUsername());
	final List<EntityContainer<EntityAggregates>> result = fetcher.listContainersAsIs(modelResult, pageNumber, pageCapacity);
	return new EntityEnhancer<EntityAggregates>(fetcher).enhance(result, queryModel.getFetchModel());
    }
}