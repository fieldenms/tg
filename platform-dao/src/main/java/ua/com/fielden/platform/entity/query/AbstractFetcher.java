package ua.com.fielden.platform.entity.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.type.Type;
import org.joda.time.DateTime;
import org.joda.time.Period;

import ua.com.fielden.platform.dao.MappingsGenerator;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao2.PropertyPersistenceInfo;
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
public class AbstractFetcher<E extends AbstractEntity<?>> {
    private Session session;
    private EntityFactory entityFactory;
    private Logger logger = Logger.getLogger(this.getClass());
    private MappingsGenerator mappingsGenerator;
    private DbVersion dbVersion;
    private final IFilter filter;
    private final String username;
    private final EntityEnhancer<E> entityEnhancer;

    public AbstractFetcher(final Session session, final EntityFactory entityFactory, final MappingsGenerator mappingsGenerator, final DbVersion dbVersion, final IFilter filter, final String username) {
	this.session = session;
	this.entityFactory = entityFactory;
	this.mappingsGenerator = mappingsGenerator;
	this.dbVersion = dbVersion;
	this.filter = filter;
	this.username = username;
	this.entityEnhancer = new EntityEnhancer<E>(session, entityFactory, mappingsGenerator, dbVersion, filter, username);
    }

    /**
     * Produces native sql hibernate query from scalars, assigns values to parameters.
     * @param sql
     * @param retrievedColumns
     * @param queryParams
     * @return
     */
    protected Query produceHibernateQuery(final String sql, final List<Pair<String, Type>> retrievedColumns, final Map<String, Object> queryParams) {
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

    public Session getSession() {
        return session;
    }

    public EntityFactory getEntityFactory() {
        return entityFactory;
    }

    public Logger getLogger() {
        return logger;
    }

    public MappingsGenerator getMappingsGenerator() {
        return mappingsGenerator;
    }

    public DbVersion getDbVersion() {
        return dbVersion;
    }

    protected List<Pair<String, Type>> getScalarFromValueTree(final ValueTree tree) {
	final List<Pair<String, Type>> result = new ArrayList<Pair<String, Type>>();

	for (final Map.Entry<PropertyPersistenceInfo, Integer> single : tree.getSingles().entrySet()) {
	    result.add(new Pair<String, Type>(single.getKey().getColumn(), single.getKey().getHibTypeAsType()));
	}

	return result;
    }

    public IFilter getFilter() {
        return filter;
    }

    public String getUsername() {
        return username;
    }

    protected List<Pair<String, Type>> getScalarFromEntityTree(final EntityTree<? extends AbstractEntity<?>> tree) {
	final List<Pair<String, Type>> result = new ArrayList<Pair<String, Type>>();

	for (final Map.Entry<PropertyPersistenceInfo, Integer> single : tree.getSingles().entrySet()) {
	    result.add(new Pair<String, Type>(single.getKey().getColumn(), single.getKey().getHibTypeAsType()));
	}

	for (final Map.Entry<String, ValueTree> composite : tree.getCompositeValues().entrySet()) {
	    result.addAll(getScalarFromValueTree(composite.getValue()));
	}

	for (final Map.Entry<String, EntityTree<? extends AbstractEntity<?>>> composite : tree.getComposites().entrySet()) {
	    result.addAll(getScalarFromEntityTree(composite.getValue()));
	}

	return result;
    }

    /**
     * Instantiates data from containers into respective entities.
     * @param containers
     * @param userViewOnly
     * @return
     */
    protected List<E> instantiateFromContainers(final List<EntityContainer<E>> containers, final boolean userViewOnly) {
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

    public EntityEnhancer<E> getEntityEnhancer() {
        return entityEnhancer;
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
    protected List<EntityContainer<E>> listContainersAsIs(final QueryModelResult<E> modelResult, final Integer pageNumber, final Integer pageCapacity) throws Exception {
	final Class<E> resultType = modelResult.getResultType();
	final EntityTree<E> resultTree = new EntityResultTreeBuilder(getMappingsGenerator()).buildEntityTree(resultType, modelResult.getYieldedPropsInfo());

	final Query query = produceHibernateQuery(modelResult.getSql(), getScalarFromEntityTree(resultTree), modelResult.getParamValues());
	getLogger().info("query:\n   " + query.getQueryString() + "\n");
	if (pageNumber != null && pageCapacity != null) {
	    query.//
	    setFirstResult(pageNumber * pageCapacity).//
	    setFetchSize(pageCapacity).//
	    setMaxResults(pageCapacity);
	}

	final DateTime st = new DateTime();
	@SuppressWarnings("unchecked")
	final List<EntityContainer<E>> list = new EntityRawResultConverter<E>(getEntityFactory()).transformFromNativeResult(resultTree, query.list());
	final Period pd = new Period(st, new DateTime());
	getLogger().info("Duration: " + pd.getMinutes() + " m " + pd.getSeconds() + " s " + pd.getMillis() + " ms. Entities count: " + list.size());

	return list;
    }
}