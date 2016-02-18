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

            if (!queryModel.isLightweight()) {
                setContainersToBeInstrumentalised(containers);
            }

            final List<E> result = instantiateFromContainers(containers, proxyMode);
            final Period pd = new Period(st, new DateTime());

            final String entityTypeName = queryModel.getQueryModel().getResultType() != null ? queryModel.getQueryModel().getResultType().getSimpleName() : "?";
            logger.debug(format("Duration: %s m %s s %s ms. Entities (%s) count: %s.", pd.getMinutes(), pd.getSeconds(), pd.getMillis(), entityTypeName, result.size()));

            return result;
        } catch (final Exception e) {
            logger.error(e);
            throw new IllegalStateException(e);
        }
    }

    private <E extends AbstractEntity<?>> List<EntityContainer<E>> setContainersToBeInstrumentalised(final List<EntityContainer<E>> containers) {
        for (final EntityContainer<E> entityContainer : containers) {
            entityContainer.setInstrumentalised();
        }
        return containers;
    }

    private <E extends AbstractEntity<?>> List<E> instantiateFromContainers(final List<EntityContainer<E>> containers, final ProxyMode proxyMode) {
        final List<E> result = new ArrayList<E>();
        final EntityFromContainerInstantiator instantiator = new EntityFromContainerInstantiator(executionContext.getEntityFactory(), proxyMode, executionContext.getCoFinder());
        for (final EntityContainer<E> entityContainer : containers) {
            result.add(instantiator.instantiate(entityContainer));
        }
        return result;
    }
}