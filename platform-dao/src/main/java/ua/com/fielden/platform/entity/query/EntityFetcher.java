package ua.com.fielden.platform.entity.query;

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
            EntityContainerFetcher entityContainerFetcher = new EntityContainerFetcher(executionContext);
            List<EntityContainer<E>> containers = entityContainerFetcher.listAndEnhanceContainers(queryModel, pageNumber, pageCapacity);
            final List<E> result = instantiateFromContainers(containers, queryModel.isLightweight(), proxyMode);
            final Period pd = new Period(st, new DateTime());
            logger.info("Duration: " + pd.getMinutes() + " m " + pd.getSeconds() + " s " + pd.getMillis() + " ms. Entities count: " + result.size());
            return result;
        } catch (final Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

    private <E extends AbstractEntity<?>> List<E> instantiateFromContainers(final List<EntityContainer<E>> containers, final boolean userViewOnly, final ProxyMode proxyMode) {
        final List<E> result = new ArrayList<E>();
        for (final EntityContainer<E> entityContainer : containers) {
            result.add(entityContainer.instantiate(executionContext.getEntityFactory(), userViewOnly, proxyMode));
        }
        return result;
    }
}