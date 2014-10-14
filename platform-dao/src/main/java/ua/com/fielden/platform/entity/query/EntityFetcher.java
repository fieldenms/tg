package ua.com.fielden.platform.entity.query;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.type.Type;
import org.joda.time.DateTime;
import org.joda.time.Period;

import ua.com.fielden.platform.dao.DomainMetadata;
import ua.com.fielden.platform.dao.DomainMetadataAnalyser;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.proxy.ProxyMode;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.generation.EntQueryGenerator;
import ua.com.fielden.platform.entity.query.generation.elements.EntQuery;
import ua.com.fielden.platform.entity.query.generation.elements.ResultQueryYieldDetails;
import ua.com.fielden.platform.entity.query.generation.elements.Yield;
import ua.com.fielden.platform.entity.query.generation.elements.Yields;
import ua.com.fielden.platform.entity.query.model.SingleResultQueryModel;

public class EntityFetcher {
    private final Session session;
    private final EntityFactory entityFactory;
    private final ICompanionObjectFinder coFinder;
    public ICompanionObjectFinder getCoFinder() {
        return coFinder;
    }

    private final DomainMetadata domainMetadata;
    private final IFilter filter;
    private final String username;
    private final Logger logger = Logger.getLogger(this.getClass());

    public EntityFetcher(final Session session, final EntityFactory entityFactory, final ICompanionObjectFinder coFinder, final DomainMetadata domainMetadata, final IFilter filter, final String username) {
        this.session = session;
        this.entityFactory = entityFactory;
        this.coFinder = coFinder;
        this.domainMetadata = domainMetadata;
        this.filter = filter;
        this.username = username;
    }

    private <E extends AbstractEntity<?>> List<E> getEntitiesOnPage(final QueryExecutionModel<E, ?> queryModel, final Integer pageNumber, final Integer pageCapacity, final ProxyMode proxyMode) {
        try {
            final DateTime st = new DateTime();
            final List<E> result = instantiateFromContainers(listContainers(queryModel, pageNumber, pageCapacity), queryModel.isLightweight(), proxyMode);
            final Period pd = new Period(st, new DateTime());
            logger.info("Duration: " + pd.getMinutes() + " m " + pd.getSeconds() + " s " + pd.getMillis() + " ms. Entities count: " + result.size());
            return result;
        } catch (final Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    
    public <E extends AbstractEntity<?>> List<E> getLazyEntitiesOnPage(final QueryExecutionModel<E, ?> queryModel, final Integer pageNumber, final Integer pageCapacity) {
        return getEntitiesOnPage(queryModel, pageNumber, pageCapacity, ProxyMode.LAZY);
    }
    
    public <E extends AbstractEntity<?>> List<E> getEntitiesOnPage(final QueryExecutionModel<E, ?> queryModel, final Integer pageNumber, final Integer pageCapacity) {
        return getEntitiesOnPage(queryModel, pageNumber, pageCapacity, ProxyMode.STRICT);
    }

    public <E extends AbstractEntity<?>> List<E> getEntities(final QueryExecutionModel<E, ?> queryModel) {
        return getEntitiesOnPage(queryModel, null, null);
    }

    private <T extends AbstractEntity<?>> QueryModelResult<T> getModelResult(final QueryExecutionModel<T, ?> qem, final DomainMetadataAnalyser domainMetadataAnalyser, final IFilter filter, final String username) {
        final EntQueryGenerator gen = new EntQueryGenerator(domainMetadataAnalyser, filter, username);
        final EntQuery entQuery = gen.generateEntQueryAsResultQuery(qem);
        final String sql = entQuery.sql();
        return new QueryModelResult<T>(entQuery.type(), sql, getResultPropsInfos(entQuery.getYields()), entQuery.getValuesForSqlParams());
    }

    private SortedSet<ResultQueryYieldDetails> getResultPropsInfos(final Yields model) {
        final SortedSet<ResultQueryYieldDetails> result = new TreeSet<ResultQueryYieldDetails>();
        for (final Yield yield : model.getYields()) {
            result.add(new ResultQueryYieldDetails(yield.getInfo().getName(), yield.getInfo().getJavaType(), yield.getInfo().getHibType(), yield.getInfo().getColumn(), yield.getInfo().getYieldDetailsType()));
        }
        return result;
    }

    protected <E extends AbstractEntity<?>> List<EntityContainer<E>> listContainers(final QueryExecutionModel<E, ?> queryModel, final Integer pageNumber, final Integer pageCapacity)
            throws Exception {
        final DomainMetadataAnalyser domainMetadataAnalyser = new DomainMetadataAnalyser(getDomainMetadata());
        final QueryModelResult<E> modelResult = getModelResult(queryModel, domainMetadataAnalyser, getFilter(), getUsername());
        if (modelResult.idOnlyQuery()) {
            return listContainers(from(select(modelResult.getResultType()).where().prop("id").in().model((SingleResultQueryModel) queryModel.getQueryModel()).model()). //
            lightweight(queryModel.isLightweight()). //
            with(queryModel.getOrderModel()). //
            with(queryModel.getFetchModel()). //
            with(queryModel.getParamValues()).model(), pageNumber, pageCapacity);
        }
        final List<EntityContainer<E>> result = listContainersAsIs(modelResult, pageNumber, pageCapacity);
        final fetch<E> fetchModel = queryModel.getFetchModel() != null ? queryModel.getFetchModel() : fetch(modelResult.getResultType());
        final FetchModel<E> entFetch = new FetchModel<E>(/*queryModel.getFetchModel()*/fetchModel, domainMetadataAnalyser);
        logger.info("Fetch model:\n" + entFetch);
        return new EntityEnhancer<E>(this, domainMetadataAnalyser).enhance(result, entFetch);
    }

    protected Query produceHibernateQuery(final String sql, final SortedSet<HibernateScalar> retrievedColumns, final Map<String, Object> queryParams) {
        final SQLQuery q = session.createSQLQuery(sql);
        logger.info("\nSQL:\n   " + sql + "\n");

        for (final HibernateScalar aliasEntry : retrievedColumns) {
            if (aliasEntry.hasHibType()) {
                logger.debug("adding scalar: alias = [" + aliasEntry.columnName + "] type = [" + aliasEntry.hibType + "]");
                q.addScalar(aliasEntry.columnName, aliasEntry.hibType);
            } else {
                logger.debug("adding scalar: alias = [" + aliasEntry.columnName + "]");
                q.addScalar(aliasEntry.columnName);
            }
        }

        logger.info("\nPARAMS:\n   " + queryParams + "\n");
        for (final Map.Entry<String, Object> paramEntry : queryParams.entrySet()) {
            if (paramEntry.getValue() instanceof Collection) {
                throw new IllegalStateException("Should not have collectional param at this level: [" + paramEntry + "]");
            } else {
                logger.debug("setting param: name = [" + paramEntry.getKey() + "] value = [" + paramEntry.getValue() + "]");
                q.setParameter(paramEntry.getKey(), paramEntry.getValue());
            }
        }

        return q;
    }

    protected SortedSet<HibernateScalar> getScalarFromValueTree(final ValueTree tree) {
        final SortedSet<HibernateScalar> result = new TreeSet<HibernateScalar>();

        for (final Map.Entry<ResultQueryYieldDetails, Integer> single : tree.getSingles().entrySet()) {
            result.add(new HibernateScalar(single.getKey().getColumn(), single.getKey().getHibTypeAsType(), single.getValue()));
        }

        return result;
    }

    protected SortedSet<HibernateScalar> getScalarFromEntityTree(final EntityTree<? extends AbstractEntity<?>> tree) {
        final SortedSet<HibernateScalar> result = new TreeSet<HibernateScalar>();

        for (final Map.Entry<ResultQueryYieldDetails, Integer> single : tree.getSingles().entrySet()) {
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

    protected <E extends AbstractEntity<?>> List<E> instantiateFromContainers(final List<EntityContainer<E>> containers, final boolean userViewOnly,  final ProxyMode proxyMode) {
        final List<E> result = new ArrayList<E>();
        for (final EntityContainer<E> entityContainer : containers) {
            result.add(entityContainer.instantiate(getEntityFactory(), userViewOnly, proxyMode));
        }
        return result;
    }

    protected <E extends AbstractEntity<?>> List<EntityContainer<E>> listContainersAsIs(final QueryModelResult<E> modelResult, final Integer pageNumber, final Integer pageCapacity)
            throws Exception {
        final EntityTree<E> resultTree = new EntityResultTreeBuilder().buildEntityTree(modelResult.getResultType(), modelResult.getYieldedPropsInfo());

        final Query query = produceHibernateQuery(modelResult.getSql(), getScalarFromEntityTree(resultTree), modelResult.getParamValues());
        if (pageNumber != null && pageCapacity != null) {
            query.//
            setFirstResult(pageNumber * pageCapacity).//
            setFetchSize(pageCapacity).//
            setMaxResults(pageCapacity);
        }

        return new EntityRawResultConverter<E>(getEntityFactory(), getCoFinder()).transformFromNativeResult(resultTree, query.list());
    }

    public Session getSession() {
        return session;
    }

    public EntityFactory getEntityFactory() {
        return entityFactory;
    }

    public DomainMetadata getDomainMetadata() {
        return domainMetadata;
    }

    public IFilter getFilter() {
        return filter;
    }

    public String getUsername() {
        return username;
    }

    private static class HibernateScalar implements Comparable<HibernateScalar> {
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