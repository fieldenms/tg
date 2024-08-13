package ua.com.fielden.platform.entity.query;

import com.google.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.joda.time.DateTime;
import org.joda.time.Period;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.exceptions.EntityFetcherException;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.FillModel;
import ua.com.fielden.platform.entity.query.model.FillModelApplier;
import ua.com.fielden.platform.eql.retrieval.IEntityContainerFetcher;
import ua.com.fielden.platform.meta.IDomainMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.utils.DefinersExecutor.definersExecutor;

@Singleton
final class EntityFetcher implements IEntityFetcher {

    private static final Logger LOGGER = getLogger(EntityFetcher.class);

    private final IEntityContainerFetcher entityContainerFetcher;
    private final IDomainMetadata domainMetadata;
    private final EntityFactory entityFactory;
    private final FillModelApplier fillModelApplier;

    @Inject
    EntityFetcher(final IEntityContainerFetcher entityContainerFetcher,
                  final IDomainMetadata domainMetadata,
                  final EntityFactory entityFactory,
                  final FillModelApplier fillModelApplier) {
        this.entityContainerFetcher = entityContainerFetcher;
        this.domainMetadata = domainMetadata;
        this.entityFactory = entityFactory;
        this.fillModelApplier = fillModelApplier;
    }

    @Override
    public <E extends AbstractEntity<?>> List<E> getEntities(final Session session, final QueryExecutionModel<E, ?> queryModel) {
        return getEntitiesOnPage(session, queryModel, null, null);
    }

    @Override
    public <E extends AbstractEntity<?>> List<E>
    getEntitiesOnPage(final Session session, final QueryExecutionModel<E, ?> queryModel,
                      final Integer pageNumber, final Integer pageCapacity) {
        try {
            final DateTime st = new DateTime();
            final List<EntityContainer<E>> containers = getContainers(session, queryModel, pageNumber, pageCapacity);
            
            if (!queryModel.isLightweight()) {
                setContainersToBeInstrumented(containers);
            }

            final List<E> result = instantiateFromContainers(containers, queryModel.getFillModel());
            final Period pd = new Period(st, new DateTime());

            final String entityTypeName = queryModel.getQueryModel().getResultType() != null ? queryModel.getQueryModel().getResultType().getSimpleName() : "?";
            LOGGER.debug(format("Duration: %s m %s s %s ms. Entities (%s) count: %s.", pd.getMinutes(), pd.getSeconds(), pd.getMillis(), entityTypeName, result.size()));

            return result;
        } catch (final Exception e) {
            LOGGER.error(e);
            throw new IllegalStateException(e);
        }
    }
    
    private <E extends AbstractEntity<?>> List<EntityContainer<E>>
    getContainers(final Session session, final QueryExecutionModel<E, ?> queryModel,
                  final Integer pageNumber, final Integer pageCapacity) {
        final IRetrievalModel<E> fm = produceRetrievalModel(queryModel.getFetchModel(), queryModel.getQueryModel().getResultType());
        final var qpm = new QueryProcessingModel<>(queryModel.getQueryModel(), queryModel.getOrderModel(),
                                                   fm, queryModel.getParamValues(), queryModel.isLightweight());
        return entityContainerFetcher.listAndEnhanceContainers(session, qpm, pageNumber, pageCapacity);
    }

    private <E extends AbstractEntity<?>> IRetrievalModel<E>
    produceRetrievalModel(final fetch<E> fetchModel, final Class<E> resultType) {
        return fetchModel == null
                ? (resultType.equals(EntityAggregates.class) ? null : new EntityRetrievalModel<E>(fetch(resultType), domainMetadata))
                : (resultType.equals(EntityAggregates.class)
                    ? new EntityAggregatesRetrievalModel<E>(fetchModel, domainMetadata)
                    : new EntityRetrievalModel<E>(fetchModel, domainMetadata));
    }

    @Override
    public <E extends AbstractEntity<?>> Stream<E>
    streamEntities(final Session session, final QueryExecutionModel<E, ?> queryModel, final Optional<Integer> fetchSize) {
        try {
            final IRetrievalModel<E> fm = produceRetrievalModel(queryModel.getFetchModel(), queryModel.getQueryModel().getResultType());
            final QueryProcessingModel<E, ?> qpm = new QueryProcessingModel<>(queryModel.getQueryModel(), queryModel.getOrderModel(), fm, queryModel.getParamValues(), queryModel.isLightweight());
            return entityContainerFetcher
                        .streamAndEnhanceContainers(session, qpm, fetchSize)
                        .map(c -> !queryModel.isLightweight() ? setContainersToBeInstrumented(c) : c)
                        .map(c -> instantiateFromContainers(c, queryModel.getFillModel()))
                        .flatMap(List::stream);
        } catch (final Exception e) {
            LOGGER.error(e);
            throw new EntityFetcherException("Could not stream entities.", e);
        }
    }

    private <E extends AbstractEntity<?>> List<EntityContainer<E>> setContainersToBeInstrumented(final List<EntityContainer<E>> containers) {
        for (final EntityContainer<E> entityContainer : containers) {
            entityContainer.mkInstrumented();
        }
        return containers;
    }

    private <E extends AbstractEntity<?>> List<E> instantiateFromContainers(final List<EntityContainer<E>> containers,
                                                                            final FillModel fillModel) {
        final List<E> result = new ArrayList<>();
        final var instantiator = new EntityFromContainerInstantiator(entityFactory);
        for (final EntityContainer<E> entityContainer : containers) {
            result.add(fillModelApplier.apply(fillModel, instantiator.instantiate(entityContainer)));
        }
        return definersExecutor(fillModel.properties()).execute(result);
    }
}
