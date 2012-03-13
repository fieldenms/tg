package ua.com.fielden.platform.entity.query;

import java.util.List;

import org.hibernate.Session;

import ua.com.fielden.platform.dao.MappingsGenerator;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao2.AggregatesQueryExecutionModel;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.generation.DbVersion;


/**
 * This class contains the Hibernate driven implementation of getting results from the provided IQueryOrderedModel.
 *
 * @author TG Team
 *
 * @param <E>
 */
public class AggregatesFetcher extends AbstractFetcher<EntityAggregates> {


    public AggregatesFetcher(final Session session, final EntityFactory entityFactory, final MappingsGenerator mappingsGenerator, final DbVersion dbVersion, final IFilter filter, final String username) {
	super(session, entityFactory, mappingsGenerator, dbVersion, filter, username);
    }

    /**
     * Fetches the results of the specified page based on the request of the given instance of IQueryOrderedModel.
     *
     * @param queryModel
     * @param pageNumber
     * @param pageCapacity
     * @return
     */
    @SessionRequired
    public List<EntityAggregates> list(final AggregatesQueryExecutionModel queryModel, final Integer pageNumber, final Integer pageCapacity) {
	try {
	    return instantiateFromContainers(listContainers(queryModel, pageNumber, pageCapacity), queryModel.isLightweight());
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
	final QueryModelResult<EntityAggregates> modelResult = new ModelResultProducer().getModelResult(queryModel, getDbVersion(), getMappingsGenerator(), getFilter(), getUsername());
	final List<EntityContainer<EntityAggregates>> result = listContainersAsIs(modelResult, pageNumber, pageCapacity);
	return getEntityEnhancer().enhance(result, queryModel.getFetchModel(), modelResult.getResultType());
    }
}