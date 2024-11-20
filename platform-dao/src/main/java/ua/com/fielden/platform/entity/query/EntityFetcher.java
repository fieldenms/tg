package ua.com.fielden.platform.entity.query;

import com.google.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.exceptions.EntityFetcherException;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.IFillModel;
import ua.com.fielden.platform.eql.meta.QuerySourceInfoProvider;
import ua.com.fielden.platform.eql.retrieval.IEntityContainerFetcher;
import ua.com.fielden.platform.meta.IDomainMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.entity.query.IRetrievalModel.createRetrievalModel;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.utils.DefinersExecutor.definersExecutor;

@Singleton
final class EntityFetcher implements IEntityFetcher {

    private static final Logger LOGGER = getLogger(EntityFetcher.class);

    public static final String ERR_COULD_NOT_STREAM_ENTITIES = "Could not stream entities.";

    private final IEntityContainerFetcher entityContainerFetcher;
    private final IDomainMetadata domainMetadata;
    private final QuerySourceInfoProvider qsip;
    private final EntityFactory entityFactory;

    @Inject
    EntityFetcher(final IEntityContainerFetcher entityContainerFetcher,
                  final IDomainMetadata domainMetadata,
                  final QuerySourceInfoProvider qsip,
                  final EntityFactory entityFactory)
    {
        this.entityContainerFetcher = entityContainerFetcher;
        this.domainMetadata = domainMetadata;
        this.qsip = qsip;
        this.entityFactory = entityFactory;
    }

    @Override
    public <E extends AbstractEntity<?>> List<E> getEntities(final Session session, final QueryExecutionModel<E, ?> queryModel) {
        return getEntitiesOnPage(session, queryModel, null, null);
    }

    @Override
    public <E extends AbstractEntity<?>> List<E> getEntitiesOnPage(
            final Session session,
            final QueryExecutionModel<E, ?> queryModel,
            final Integer pageNumber,
            final Integer pageCapacity)
    {
        final List<EntityContainer<E>> containers = getContainers(session, queryModel, pageNumber, pageCapacity);

        if (!queryModel.isLightweight()) {
            setContainersToBeInstrumented(containers);
        }

        return instantiateFromContainers(containers, queryModel.getFillModel());
    }

    private <E extends AbstractEntity<?>> List<EntityContainer<E>> getContainers(
            final Session session,
            final QueryExecutionModel<E, ?> queryModel,
            final Integer pageNumber,
            final Integer pageCapacity)
    {
        final var fm = produceRetrievalModel(queryModel.getFetchModel(), queryModel.getQueryModel().getResultType(), domainMetadata, qsip);
        final var qpm = new QueryProcessingModel<>(queryModel.getQueryModel(), queryModel.getOrderModel(), fm, queryModel.getParamValues(), queryModel.isLightweight());
        return entityContainerFetcher.listAndEnhanceContainers(session, qpm, pageNumber, pageCapacity);
    }

    private <E extends AbstractEntity<?>> IRetrievalModel<E> produceRetrievalModel(
            final fetch<E> fetchModel,
            final Class<E> resultType,
            final IDomainMetadata domainMetadata,
            final QuerySourceInfoProvider qsip)
    {
        if (fetchModel == null) {
            return resultType == EntityAggregates.class ? null : new EntityRetrievalModel<>(fetch(resultType), domainMetadata, qsip);
        } else {
            return createRetrievalModel(fetchModel, domainMetadata, qsip);
        }
    }

    @Override
    public <E extends AbstractEntity<?>> Stream<E> streamEntities(
            final Session session,
            final QueryExecutionModel<E, ?> queryModel,
            final Optional<Integer> fetchSize)
    {
        try {
            final IRetrievalModel<E> fm = produceRetrievalModel(queryModel.getFetchModel(), queryModel.getQueryModel().getResultType(), domainMetadata, qsip);
            final QueryProcessingModel<E, ?> qpm = new QueryProcessingModel<>(queryModel.getQueryModel(), queryModel.getOrderModel(), fm, queryModel.getParamValues(), queryModel.isLightweight());
            return entityContainerFetcher
                        .streamAndEnhanceContainers(session, qpm, fetchSize)
                        .map(c -> !queryModel.isLightweight() ? setContainersToBeInstrumented(c) : c)
                        .map(c -> instantiateFromContainers(c, queryModel.getFillModel()))
                        .flatMap(List::stream);
        } catch (final Exception ex) {
            LOGGER.error(ex);
            throw new EntityFetcherException(ERR_COULD_NOT_STREAM_ENTITIES, ex);
        }
    }

    private <E extends AbstractEntity<?>> List<EntityContainer<E>> setContainersToBeInstrumented(final List<EntityContainer<E>> containers) {
        for (final EntityContainer<E> entityContainer : containers) {
            entityContainer.mkInstrumented();
        }
        return containers;
    }

    private <E extends AbstractEntity<?>> List<E> instantiateFromContainers(
            final List<EntityContainer<E>> containers,
            final IFillModel<E> fillModel)
    {
        final var result = new ArrayList<E>();
        final var instantiator = new EntityFromContainerInstantiator(entityFactory);
        for (final EntityContainer<E> entityContainer : containers) {
            result.add(fillModel.fill(instantiator.instantiate(entityContainer)));
        }
        return definersExecutor(fillModel.properties()).execute(result);
    }
}
