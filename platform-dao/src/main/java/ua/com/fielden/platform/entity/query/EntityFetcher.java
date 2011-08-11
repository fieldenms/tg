package ua.com.fielden.platform.entity.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.type.Type;
import org.joda.time.DateTime;
import org.joda.time.Period;

import ua.com.fielden.platform.dao.MappingsGenerator;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.entity.query.model.structure.QueryModelResult;
import ua.com.fielden.platform.entity.query.model.structure.QueryModelResult.ResultPropertyInfo;
import ua.com.fielden.platform.utils.Pair;


/**
 * This class contains the Hibernate driven implementation of getting results from the provided IQueryOrderedModel.
 *
 * @author TG Team
 *
 * @param <E>
 */
public class EntityFetcher<E extends AbstractEntity> {
    private Session session;
    private EntityFactory entityFactory;
    private Logger logger = Logger.getLogger(this.getClass());
    private MappingsGenerator mappingsGenerator;
    private EntityEnhancer<E> entityEnhancer;

    public EntityFetcher() {
    }

    protected EntityFetcher(final Session session, final EntityFactory entityFactory, final MappingsGenerator mappingsGenerator) {
	this.session = session;
	this.entityFactory = entityFactory;
	this.mappingsGenerator = mappingsGenerator;
	this.entityEnhancer = new EntityEnhancer<E>(session, entityFactory, mappingsGenerator);
    }

    /**
     * Produces native sql hibernate query from scalars, assigns values to parameters.
     * @param sql
     * @param retrievedColumns
     * @param queryParams
     * @return
     */
    private Query produceHibernateQuery(final String sql, final List<Pair<String, Type>> retrievedColumns, final Map<String, Object> queryParams) {
	final SQLQuery q = session.createSQLQuery(sql);

	for (final Pair<String, Type> aliasEntry : retrievedColumns) {
	    logger.info("adding scalar: alias = [" + aliasEntry.getKey() + "] type = [" + (aliasEntry.getValue() != null ? aliasEntry.getValue().getClass().getName() : "") + "]");
	    if (aliasEntry.getValue() != null) {
		q.addScalar(aliasEntry.getKey(), aliasEntry.getValue());
	    } else {
		q.addScalar(aliasEntry.getKey());
	    }
	}

	for (final Map.Entry<String, Object> paramEntry : queryParams.entrySet()) {
	    logger.info("about to set param: name = [" + paramEntry.getKey() + "] value = [" + paramEntry.getValue() + "]");
	    if (paramEntry.getValue() instanceof Collection) {
		q.setParameterList(paramEntry.getKey(), (Collection) paramEntry.getValue());
	    } else {
		q.setParameter(paramEntry.getKey(), paramEntry.getValue());
	    }
	    logger.debug("setting param: name = [" + paramEntry.getKey() + "] value = [" + paramEntry.getValue() + "]");
	}

	return q;
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
    private List<EntityContainer<E>> listContainersAsIs(final QueryModelResult modelResult, final Integer pageNumber, final Integer pageCapacity) throws Exception {
	final SortedMap<String, ResultPropertyInfo> getYieldedPropsInfo = modelResult.getYieldedPropsInfo();
	final EntityTree resultTree = new EntityResultTreeBuilder(mappingsGenerator).buildTree(modelResult.getResultType(), getYieldedPropsInfo.values());

	final Query query = produceHibernateQuery(modelResult.getSql(), getScalarInfo(resultTree), modelResult.getParamValues());
	logger.info("query:\n   " + query.getQueryString() + "\n");
	if (pageNumber != null && pageCapacity != null) {
	    query.//
	    setFirstResult(pageNumber * pageCapacity).//
	    setFetchSize(pageCapacity).//
	    setMaxResults(pageCapacity);
	}

	final DateTime st = new DateTime();
	@SuppressWarnings("unchecked")
	final List<EntityContainer<E>> list = new EntityRawResultConverter(entityFactory).transformFromNativeResult(resultTree, query.list());
	final Period pd = new Period(st, new DateTime());
	logger.info("Duration: " + pd.getMinutes() + " m " + pd.getSeconds() + " s " + pd.getMillis() + " ms. Entities count: " + list.size());

	return list;
    }

    /*DONE*/
    protected List<Pair<String, Type>> getScalarInfo(final EntityTree tree) {
	final List<Pair<String, Type>> result = new ArrayList<Pair<String, Type>>();

	for (final Map.Entry<PropColumn, Integer> single : tree.getSingles().entrySet()) {
	    result.add(new Pair<String, Type>(single.getKey().getSqlAlias(), single.getKey().getHibType()));
	}

	for (final Map.Entry<String, EntityTree> composite : tree.getComposites().entrySet()) {
	    result.addAll(getScalarInfo(composite.getValue()));
	}

	return result;
    }

    /**
     * Instantiates data from containers into respective entities.
     * @param containers
     * @param userViewOnly
     * @return
     */
    private List<E> instantiateFromContainers(final List<EntityContainer<E>> containers, final boolean userViewOnly) {
	logger.info("Instantiating from containers -- entity type is " + (containers.size() > 0 ? containers.get(0).resultType.getName() : "?"));
	final DateTime st = new DateTime();
	final List<E> result = new ArrayList<E>();
	for (final EntityContainer<E> entityContainer : containers) {
	    result.add(entityContainer.instantiate(entityFactory, userViewOnly));
	}
	final Period pd = new Period(st, new DateTime());
	logger.info("Done. Instantiating from containers -- entity type is " + (containers.size() > 0 ? containers.get(0).resultType.getName() : "?") + "\n Duration: " + pd.getMinutes() + " m " + pd.getSeconds() + " s " + pd.getMillis() + " ms");
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
    public List<E> list(final Session session, final EntityFactory entityFactory, final QueryModel queryModel, final Integer pageNumber, final Integer pageCapacity, final fetch<E> fetchModel) {
	this.session = session;
	this.entityFactory = entityFactory;
	try {
	    return instantiateFromContainers(listContainers(queryModel, pageNumber, pageCapacity, fetchModel), queryModel.isLightweight());
	} catch (final Exception e) {
	    e.printStackTrace();
	    throw new IllegalStateException(e);
	}
    }


    public List<E> list(final Session session, final EntityFactory entityFactory, final QueryModel queryModel) {
	return list(session, entityFactory, queryModel, null, null, null);
    }


    @SessionRequired
    protected List<EntityContainer<E>> listContainers(final QueryModel queryModel, final Integer pageNumber, final Integer pageCapacity, final fetch<E> fetchModel) throws Exception {
	final QueryModelResult modelResult = new ModelResultProducer().getModelResult(queryModel, fetchModel);
	final List<EntityContainer<E>> result = listContainersAsIs(modelResult, pageNumber, pageCapacity);
	return entityEnhancer.enhance(result, entityEnhancer.enhanceFetchModelWithKeyProperties(fetchModel, modelResult.getResultType()), modelResult.getResultType());
    }

}