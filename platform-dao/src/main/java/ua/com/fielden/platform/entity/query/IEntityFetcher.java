package ua.com.fielden.platform.entity.query;

import com.google.inject.ImplementedBy;
import org.hibernate.Session;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@ImplementedBy(EntityFetcher.class)
public interface IEntityFetcher {

    <E extends AbstractEntity<?>> List<E> getEntities(Session session, QueryExecutionModel<E, ?> queryModel);

    <E extends AbstractEntity<?>> List<E> getEntitiesOnPage(Session session,
                                                            QueryExecutionModel<E, ?> queryModel,
                                                            Integer pageNumber,
                                                            Integer pageCapacity);

    <E extends AbstractEntity<?>> Stream<E> streamEntities(Session session,
                                                           QueryExecutionModel<E, ?> queryModel,
                                                           Optional<Integer> fetchSize);

}
