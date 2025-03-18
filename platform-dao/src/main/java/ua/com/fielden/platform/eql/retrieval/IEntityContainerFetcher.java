package ua.com.fielden.platform.eql.retrieval;

import com.google.inject.ImplementedBy;
import org.hibernate.Session;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityContainer;
import ua.com.fielden.platform.entity.query.QueryProcessingModel;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@ImplementedBy(EntityContainerFetcherImpl.class)
public interface IEntityContainerFetcher {

    <E extends AbstractEntity<?>> List<EntityContainer<E>> listAndEnhanceContainers(
            final Session session,
            final QueryProcessingModel<E, ?> queryModel,
            final Integer pageNumber,
            final Integer pageCapacity);


    <E extends AbstractEntity<?>> Stream<List<EntityContainer<E>>> streamAndEnhanceContainers(
            final Session session,
            final QueryProcessingModel<E, ?> queryModel,
            final Optional<Integer> fetchSize);

}
