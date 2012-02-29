package ua.com.fielden.platform.entity.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.type.Type;
import org.joda.time.DateTime;
import org.joda.time.Period;

import ua.com.fielden.platform.dao.MappingsGenerator;
import ua.com.fielden.platform.dao.PropertyPersistenceInfo;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao2.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.generation.DbVersion;
import ua.com.fielden.platform.utils.Pair;


/**
 * This class contains the Hibernate driven implementation of getting results from the provided IQueryOrderedModel.
 *
 * @author TG Team
 *
 * @param <E>
 */
public class EntityFetcher<E extends AbstractEntity> extends AbstractFetcher<E> {
    private final EntityEnhancer<E> entityEnhancer;

    public EntityFetcher(final Session session, final EntityFactory entityFactory, final MappingsGenerator mappingsGenerator, final DbVersion dbVersion, final IFilter filter, final String username) {
	super(session, entityFactory, mappingsGenerator, dbVersion, filter, username);
	this.entityEnhancer = new EntityEnhancer<E>(session, entityFactory, mappingsGenerator, dbVersion, filter, username);
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
    public List<E> list(final QueryExecutionModel queryModel, final Integer pageNumber, final Integer pageCapacity) {
	try {
	    return instantiateFromContainers(listContainers(queryModel, pageNumber, pageCapacity), queryModel.isLightweight());
	} catch (final Exception e) {
	    e.printStackTrace();
	    throw new IllegalStateException(e);
	}
    }

    public List<E> list(final QueryExecutionModel queryModel) {
	return list(queryModel, null, null);
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
	final EntityTree resultTree = new EntityResultTreeBuilder(getMappingsGenerator()).buildEntityTree(modelResult.getResultType(), modelResult.getYieldedPropsInfo());

	final Query query = produceHibernateQuery(modelResult.getSql(), getScalarInfo(resultTree), modelResult.getParamValues());
	getLogger().info("query:\n   " + query.getQueryString() + "\n");
	if (pageNumber != null && pageCapacity != null) {
	    query.//
	    setFirstResult(pageNumber * pageCapacity).//
	    setFetchSize(pageCapacity).//
	    setMaxResults(pageCapacity);
	}

	final DateTime st = new DateTime();
	@SuppressWarnings("unchecked")
	final List<EntityContainer<E>> list = new EntityRawResultConverter(getEntityFactory()).transformFromNativeResult(resultTree, query.list());
	final Period pd = new Period(st, new DateTime());
	getLogger().info("Duration: " + pd.getMinutes() + " m " + pd.getSeconds() + " s " + pd.getMillis() + " ms. Entities count: " + list.size());

	return list;
    }

    protected List<Pair<String, Type>> getScalarInfo(final EntityTree tree) {
	final List<Pair<String, Type>> result = new ArrayList<Pair<String, Type>>();

	for (final Map.Entry<PropertyPersistenceInfo, Integer> single : tree.getSingles().entrySet()) {
	    result.add(new Pair<String, Type>(single.getKey().getColumn(), single.getKey().getHibTypeAsType()));
	}

	for (final Map.Entry<String, ValueTree> composite : tree.getCompositeValues().entrySet()) {
	    result.addAll(getScalarInfo(composite.getValue()));
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
	getLogger().info("Instantiating from containers -- entity type is " + (containers.size() > 0 ? containers.get(0).getResultType().getName() : "?"));
	final DateTime st = new DateTime();
	final List<E> result = new ArrayList<E>();
	for (final EntityContainer<E> entityContainer : containers) {
	    result.add(entityContainer.instantiate(getEntityFactory(), userViewOnly));
	}
	final Period pd = new Period(st, new DateTime());
	getLogger().info("Done. Instantiating from containers -- entity type is " + (containers.size() > 0 ? containers.get(0).getResultType().getName() : "?") + "\n Duration: " + pd.getMinutes() + " m " + pd.getSeconds() + " s " + pd.getMillis() + " ms");
	return result;
    }

    @SessionRequired
    protected List<EntityContainer<E>> listContainers(final QueryExecutionModel queryModel, final Integer pageNumber, final Integer pageCapacity) throws Exception {
	final QueryModelResult modelResult = new ModelResultProducer().getModelResult(queryModel, getDbVersion(), getMappingsGenerator(), getFilter(), getUsername());
	final List<EntityContainer<E>> result = listContainersAsIs(modelResult, pageNumber, pageCapacity);
	return entityEnhancer.enhance(result, entityEnhancer.enhanceFetchModelWithKeyProperties(queryModel.getFetchModel(), modelResult.getResultType()), modelResult.getResultType());
    }
}