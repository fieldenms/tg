package ua.com.fielden.platform.entity.query;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Period;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.proxy.ProxyMode;

public class EntityFetcher {
    private final QueryExecutionContext executionContext;

    private final Logger logger = Logger.getLogger(this.getClass());

    public EntityFetcher(final QueryExecutionContext executionContext) {
        this.executionContext = executionContext;
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

    private <E extends AbstractEntity<?>> List<E> getEntitiesOnPage(final QueryExecutionModel<E, ?> queryModel, final Integer pageNumber, final Integer pageCapacity, final ProxyMode proxyMode) {
        try {
            final DateTime st = new DateTime();
            final EntityContainerFetcher entityContainerFetcher = new EntityContainerFetcher(executionContext);
            final List<EntityContainer<E>> containers = entityContainerFetcher.listAndEnhanceContainers(queryModel, pageNumber, pageCapacity);
            final List<E> result = instantiateFromContainers(containers, queryModel.isLightweight(), proxyMode);
            final Period pd = new Period(st, new DateTime());
            logger.debug(format("Duration: %s m %s s %s ms. Entities count: %s", pd.getMinutes(), pd.getSeconds(), pd.getMillis(), result.size()));
            return result;
        } catch (final Exception e) {
            logger.error(e);
            throw new IllegalStateException(e);
        }
    }

    private <E extends AbstractEntity<?>> List<E> instantiateFromContainers(final List<EntityContainer<E>> containers, final boolean lightweight, final ProxyMode proxyMode) {
        final List<E> result = new ArrayList<E>();
        final ProxyCache cache = new ProxyCache();
        final EntityFromContainerInstantiator instantiator = new EntityFromContainerInstantiator(executionContext.getEntityFactory(), lightweight, proxyMode, cache, executionContext.getCoFinder());
        for (final EntityContainer<E> entityContainer : containers) {
            result.add(instantiator.instantiate(entityContainer));
        }
        return result;
    }
}