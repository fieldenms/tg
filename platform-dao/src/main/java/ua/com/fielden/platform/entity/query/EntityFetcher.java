package ua.com.fielden.platform.entity.query;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Period;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;

public class EntityFetcher {
    private final QueryExecutionContext executionContext;

    private final Logger logger = Logger.getLogger(this.getClass());

    public EntityFetcher(final QueryExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    public <E extends AbstractEntity<?>> List<E> getEntities(final QueryExecutionModel<E, ?> queryModel) {
        return getEntitiesOnPage(queryModel, null, null);
    }

    public <E extends AbstractEntity<?>> List<E> getEntitiesOnPage(final QueryExecutionModel<E, ?> queryModel, final Integer pageNumber, final Integer pageCapacity) {
        try {
            final DateTime st = new DateTime();
            final EntityContainerFetcher<E> entityContainerFetcher = new EntityContainerFetcher<E>(executionContext);
            final List<EntityContainer<E>> containers = entityContainerFetcher.listAndEnhanceContainers(queryModel, pageNumber, pageCapacity);

            if (!queryModel.isLightweight()) {
                setContainersToBeInstrumented(containers);
            }

            final List<E> result = instantiateFromContainers(containers);
            final Period pd = new Period(st, new DateTime());

            final String entityTypeName = queryModel.getQueryModel().getResultType() != null ? queryModel.getQueryModel().getResultType().getSimpleName() : "?";
            logger.debug(format("Duration: %s m %s s %s ms. Entities (%s) count: %s.", pd.getMinutes(), pd.getSeconds(), pd.getMillis(), entityTypeName, result.size()));

            return result;
        } catch (final Exception e) {
            logger.error(e);
            throw new IllegalStateException(e);
        }
    }

    private <E extends AbstractEntity<?>> List<EntityContainer<E>> setContainersToBeInstrumented(final List<EntityContainer<E>> containers) {
        for (final EntityContainer<E> entityContainer : containers) {
            entityContainer.setInstrumented();
        }
        return containers;
    }

    private <E extends AbstractEntity<?>> List<E> instantiateFromContainers(final List<EntityContainer<E>> containers) {
        final List<E> result = new ArrayList<E>();
        final EntityFromContainerInstantiator instantiator = new EntityFromContainerInstantiator(executionContext.getEntityFactory());
        for (final EntityContainer<E> entityContainer : containers) {
            result.add(instantiator.instantiate(entityContainer));
        }
        return result;
    }
}