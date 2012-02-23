package ua.com.fielden.platform.entity.query;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.joda.time.DateTime;
import org.joda.time.Period;

import ua.com.fielden.platform.dao.MappingsGenerator;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao2.QueryExecutionModel;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.generation.DbVersion;


/**
 * This class contains the Hibernate driven implementation of getting results from the provided IQueryOrderedModel.
 *
 * @author TG Team
 *
 * @param <E>
 */
public class ValueFetcher extends AbstractFetcher {

    public ValueFetcher(final Session session, final EntityFactory entityFactory, final MappingsGenerator mappingsGenerator, final DbVersion dbVersion) {
	super(session, entityFactory, mappingsGenerator, dbVersion);
    }

    /**
     * Executes query and produces its result in the form of entity containers; no additional fetching to match provided fetch model is performed.
     * @param modelResult
     * @param pageNumber
     * @param pageCapacity
     * @return
     * @throws Exception
     */
    @SessionRequired
    private List<ValueContainer> listContainersAsIs(final QueryModelResult modelResult, final Integer pageNumber, final Integer pageCapacity) throws Exception {
//	final EntityTree resultTree = new EntityResultTreeBuilder(getMappingsGenerator()).buildTree(modelResult.getResultType(), null/*need instance of eg SimpleMoneyType*/, modelResult.getYieldedPropsInfo());
//
//	final Query query = produceHibernateQuery(modelResult.getSql(), getScalarInfo(resultTree), modelResult.getParamValues());
//	getLogger().info("query:\n   " + query.getQueryString() + "\n");
//	if (pageNumber != null && pageCapacity != null) {
//	    query.//
//	    setFirstResult(pageNumber * pageCapacity).//
//	    setFetchSize(pageCapacity).//
//	    setMaxResults(pageCapacity);
//	}
//
//	final DateTime st = new DateTime();
//	@SuppressWarnings("unchecked")
//	final List<ValueContainer<E>> list = new EntityRawResultConverter(getEntityFactory()).transformFromNativeResult(resultTree, query.list());
//	final Period pd = new Period(st, new DateTime());
//	getLogger().info("Duration: " + pd.getMinutes() + " m " + pd.getSeconds() + " s " + pd.getMillis() + " ms. Entities count: " + list.size());
//
//	return list;
    return null;
    }

    /**
     * Instantiates data from containers into respective entities.
     * @param containers
     * @param userViewOnly
     * @return
     */
    private List instantiateFromContainers(final List<ValueContainer> containers, final boolean userViewOnly) {
	final DateTime st = new DateTime();
	final List result = new ArrayList();
	for (final ValueContainer valueContainer : containers) {
	    result.add(valueContainer.instantiate());
	}
	final Period pd = new Period(st, new DateTime());
	//logger.info("Done. Instantiating from containers -- entity type is " + (containers.size() > 0 ? containers.get(0).resultType.getName() : "?") + "\n Duration: " + pd.getMinutes() + " m " + pd.getSeconds() + " s " + pd.getMillis() + " ms");
	return result;
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
    public List list(final QueryExecutionModel queryModel, final Integer pageNumber, final Integer pageCapacity, final boolean lightweight) {
	try {
	    return instantiateFromContainers(listContainers(queryModel, pageNumber, pageCapacity), lightweight);
	} catch (final Exception e) {
	    e.printStackTrace();
	    throw new IllegalStateException(e);
	}
    }

    public List list(final QueryExecutionModel queryModel, final boolean lightweight) {
	return list(queryModel, null, null, lightweight);
    }

    @SessionRequired
    protected List<ValueContainer> listContainers(final QueryExecutionModel queryModel, final Integer pageNumber, final Integer pageCapacity) throws Exception {
	final QueryModelResult modelResult = new ModelResultProducer().getModelResult(queryModel, getDbVersion(), getMappingsGenerator());
	return listContainersAsIs(modelResult, pageNumber, pageCapacity);
    }

}