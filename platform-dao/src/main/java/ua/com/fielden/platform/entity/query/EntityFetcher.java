package ua.com.fielden.platform.entity.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.type.Type;

import ua.com.fielden.platform.dao2.DomainPersistenceMetadata;
import ua.com.fielden.platform.dao2.PropertyPersistenceInfo;
import ua.com.fielden.platform.dao2.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.generation.DbVersion;


public class EntityFetcher {
    private Session session;
    private EntityFactory entityFactory;
    private DomainPersistenceMetadata domainPersistenceMetadata;
    private DbVersion dbVersion;
    private final IFilter filter;
    private final String username;

    public EntityFetcher(final Session session, final EntityFactory entityFactory, final DomainPersistenceMetadata domainPersistenceMetadata, final DbVersion dbVersion, final IFilter filter, final String username) {
	this.session = session;
	this.entityFactory = entityFactory;
	this.domainPersistenceMetadata = domainPersistenceMetadata;
	this.dbVersion = dbVersion;
	this.filter = filter;
	this.username = username;
    }

    public <E extends AbstractEntity<?>> List<E> getEntitiesOnPage(final QueryExecutionModel<E> queryModel, final Integer pageNumber, final Integer pageCapacity) {
	try {
	    return instantiateFromContainers(listContainers(queryModel, pageNumber, pageCapacity), queryModel.isLightweight());
	} catch (final Exception e) {
	    e.printStackTrace();
	    throw new IllegalStateException(e);
	}
    }

    public <E extends AbstractEntity<?>> List<E> getEntities(final QueryExecutionModel<E> queryModel) {
	return getEntitiesOnPage(queryModel, null, null);
    }

    protected <E extends AbstractEntity<?>> List<EntityContainer<E>> listContainers(final QueryExecutionModel<E> queryModel, final Integer pageNumber, final Integer pageCapacity) throws Exception {
	final QueryModelResult<E> modelResult = new ModelResultProducer().getModelResult(queryModel, getDbVersion(), getDomainPersistenceMetadata(), getFilter(), getUsername());
	final List<EntityContainer<E>> result = listContainersAsIs(modelResult, pageNumber, pageCapacity);
	final fetch<E> fetchModel = queryModel.getFetchModel() != null ? queryModel.getFetchModel() : new fetch<E>(modelResult.getResultType());
	return new EntityEnhancer<E>(this).enhance(result, fetchModel);
    }

    protected Query produceHibernateQuery(final String sql, final SortedSet<HibernateScalar> retrievedColumns, final Map<String, Object> queryParams) {
	final SQLQuery q = session.createSQLQuery(sql);
	System.out.println("   SQL: " + sql);

	for (final HibernateScalar aliasEntry : retrievedColumns) {
	    if (aliasEntry.hasHibType()) {
		q.addScalar(aliasEntry.columnName, aliasEntry.hibType);
	    } else {
		q.addScalar(aliasEntry.columnName);
	    }
	}

	System.out.println("   PARAMS: " + queryParams);
	for (final Map.Entry<String, Object> paramEntry : queryParams.entrySet()) {
	    if (paramEntry.getValue() instanceof Collection) {
		q.setParameterList(paramEntry.getKey(), (Collection<?>) paramEntry.getValue());
	    } else {
		q.setParameter(paramEntry.getKey(), paramEntry.getValue());
	    }
	}

	return q;
    }

    protected SortedSet<HibernateScalar> getScalarFromValueTree(final ValueTree tree) {
	final SortedSet<HibernateScalar> result = new TreeSet<HibernateScalar>();

	for (final Map.Entry<PropertyPersistenceInfo, Integer> single : tree.getSingles().entrySet()) {
	    result.add(new HibernateScalar(single.getKey().getColumn(), single.getKey().getHibTypeAsType(), single.getValue()));
	}

	return result;
    }

    protected SortedSet<HibernateScalar> getScalarFromEntityTree(final EntityTree<? extends AbstractEntity<?>> tree) {
	final SortedSet<HibernateScalar> result = new TreeSet<HibernateScalar>();

	for (final Map.Entry<PropertyPersistenceInfo, Integer> single : tree.getSingles().entrySet()) {
	    result.add(new HibernateScalar(single.getKey().getColumn(), single.getKey().getHibTypeAsType(), single.getValue()));
	}

	for (final Map.Entry<String, ValueTree> composite : tree.getCompositeValues().entrySet()) {
	    result.addAll(getScalarFromValueTree(composite.getValue()));
	}

	for (final Map.Entry<String, EntityTree<? extends AbstractEntity<?>>> composite : tree.getComposites().entrySet()) {
	    result.addAll(getScalarFromEntityTree(composite.getValue()));
	}

	return result;
    }

    protected <E extends AbstractEntity<?>> List<E> instantiateFromContainers(final List<EntityContainer<E>> containers, final boolean userViewOnly) {
	final List<E> result = new ArrayList<E>();
	for (final EntityContainer<E> entityContainer : containers) {
	    result.add(entityContainer.instantiate(getEntityFactory(), userViewOnly));
	}
	return result;
    }

    protected <E extends AbstractEntity<?>>List<EntityContainer<E>> listContainersAsIs(final QueryModelResult<E> modelResult, final Integer pageNumber, final Integer pageCapacity) throws Exception {
	final EntityTree<E> resultTree = new EntityResultTreeBuilder(getDomainPersistenceMetadata()).buildEntityTree(modelResult.getResultType(), modelResult.getYieldedPropsInfo());

	final Query query = produceHibernateQuery(modelResult.getSql(), getScalarFromEntityTree(resultTree), modelResult.getParamValues());
	if (pageNumber != null && pageCapacity != null) {
	    query.//
	    setFirstResult(pageNumber * pageCapacity).//
	    setFetchSize(pageCapacity).//
	    setMaxResults(pageCapacity);
	}

	return new EntityRawResultConverter<E>(getEntityFactory()).transformFromNativeResult(resultTree, query.list());
    }

    public Session getSession() {
        return session;
    }

    public EntityFactory getEntityFactory() {
        return entityFactory;
    }

    public DomainPersistenceMetadata getDomainPersistenceMetadata() {
        return domainPersistenceMetadata;
    }

    public DbVersion getDbVersion() {
        return dbVersion;
    }

    public IFilter getFilter() {
        return filter;
    }

    public String getUsername() {
        return username;
    }

    private static class HibernateScalar implements Comparable<HibernateScalar>{
	private String columnName;
	private Type hibType;
	private Integer positionInResultList;

	public HibernateScalar(final String columnName, final Type hibType, final Integer positionInResultList) {
	    this.columnName = columnName;
	    this.hibType = hibType;
	    this.positionInResultList = positionInResultList;
	}

	public boolean hasHibType() {
	    return hibType != null;
	}

	@Override
	public int compareTo(final HibernateScalar o) {
	    return positionInResultList.compareTo(o.positionInResultList);
	}
    }
}