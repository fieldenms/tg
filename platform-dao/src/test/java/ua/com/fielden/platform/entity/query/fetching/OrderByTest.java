package ua.com.fielden.platform.entity.query.fetching;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompleted;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IOrderingItem1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.StandaloneOrderBy;
import ua.com.fielden.platform.entity.query.fluent.Limit;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.eql.stage0.OrderingModelConflictException;
import ua.com.fielden.platform.sample.domain.TgPersonName;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.stream.IntStream.rangeClosed;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.dao.QueryExecutionModel.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;

public class OrderByTest extends AbstractDaoTestCase {

    private final Logger logger = LogManager.getLogger();

    private static final String TEST_DATA_KEY_PREFIX = "TEST_ORDER_BY_";
    private final ConditionModel testDataCond = cond().prop("key").like().val(TEST_DATA_KEY_PREFIX + "%").model();

    @Test
    public void orderBy_can_be_used_in_a_top_level_query() {
        final var keys = rangeClosed(1, 3).mapToObj(i -> TEST_DATA_KEY_PREFIX + i).toList();
        keys.stream()
                .map(key -> new_(TgPersonName.class, key))
                .forEach(this::save);

        withQem(select(TgPersonName.class).where().condition(testDataCond),
                $ -> $.prop("key").asc().model(),
                $ -> $.prop("key").asc().model(),
                qem -> {
                    final var entities = co(TgPersonName.class).getAllEntities(qem);
                    assertEquals(keys, entities.stream().map(TgPersonName::getKey).toList());
                });
    }

    @Test
    public void orderBy_can_be_used_in_a_subquery() {
        final var keys = rangeClosed(1, 3).mapToObj(i -> TEST_DATA_KEY_PREFIX + i).toList();
        keys.stream()
                .map(key -> new_(TgPersonName.class, key))
                .forEach(this::save);

        final var qem = from(
                select(select(TgPersonName.class).where().condition(testDataCond)
                               .orderBy().prop("key").asc()
                               .model())
                        .model())
                .model();
        final var entities = co(TgPersonName.class).getAllEntities(qem);
        assertEquals(keys, entities.stream().map(TgPersonName::getKey).toList());

    }

    @Test
    public void ordering_model_cannot_be_specified_both_as_standalone_and_as_part_of_a_query() {
        final var qem = from(
                select(TgPersonName.class).where().condition(testDataCond)
                        .orderBy().prop("key").desc()
                        .model())
                .with(orderBy().prop("id").asc().model())
                .model();

        assertThrows(OrderingModelConflictException.class,
                     () -> co(TgPersonName.class).getAllEntities(qem));
    }

    @Test
    public void query_with_limit_returns_the_specified_number_of_rows_if_the_total_number_of_rows_is_greater_than_limit() {
        final var total = 3;
        final var limit = 2;
        assertTrue(limit < total);
        final var keys = rangeClosed(1, total).mapToObj(i -> TEST_DATA_KEY_PREFIX + i).toList();
        keys.stream()
                .map(key -> new_(TgPersonName.class, key))
                .forEach(this::save);

        withQem(select(TgPersonName.class).where().condition(testDataCond),
                $ -> $.prop("key").desc().limit(limit).model(),
                $ -> $.prop("key").desc().limit(limit).model(),
                qem -> {
                    final var entities = co(TgPersonName.class).getAllEntities(qem);
                    assertEquals(keys.reversed().subList(0, limit), entities.stream().map(TgPersonName::getKey).toList());
                });
    }

    @Test
    public void query_with_limit_returns_all_rows_if_the_total_number_of_rows_is_less_than_limit() {
        final var total = 1;
        final var limit = 2;
        assertTrue(limit > total);
        final var keys = rangeClosed(1, total).mapToObj(i -> TEST_DATA_KEY_PREFIX + i).toList();
        keys.stream()
                .map(key -> new_(TgPersonName.class, key))
                .forEach(this::save);

        withQem(select(TgPersonName.class).where().condition(testDataCond),
                $ -> $.prop("key").asc().limit(limit).model(),
                $ -> $.prop("key").asc().limit(limit).model(),
                qem -> {
                    final var entities = co(TgPersonName.class).getAllEntities(qem);
                    assertEquals(keys.subList(0, total), entities.stream().map(TgPersonName::getKey).toList());
                });
    }

    @Test
    public void query_with_limit_returns_all_rows_if_the_total_number_of_rows_is_equal_to_limit() {
        final var total = 2;
        final var limit = 2;
        assertTrue(limit == total);
        final var keys = rangeClosed(1, total).mapToObj(i -> TEST_DATA_KEY_PREFIX + i).toList();
        keys.stream()
                .map(key -> new_(TgPersonName.class, key))
                .forEach(this::save);

        withQem(select(TgPersonName.class).where().condition(testDataCond),
                $ -> $.prop("key").asc().limit(limit).model(),
                $ -> $.prop("key").asc().limit(limit).model(),
                qem -> {
                    final var entities = co(TgPersonName.class).getAllEntities(qem);
                    assertEquals(keys, entities.stream().map(TgPersonName::getKey).toList());
                });
    }

    @Test
    public void query_with_limit_all_returns_all_rows() {
        final var keys = rangeClosed(1, 3).mapToObj(i -> TEST_DATA_KEY_PREFIX + i).toList();
        keys.stream()
                .map(key -> new_(TgPersonName.class, key))
                .forEach(this::save);

        withQem(select(TgPersonName.class).where().condition(testDataCond),
                $ -> $.prop("key").desc().limit(Limit.all()).model(),
                $ -> $.prop("key").desc().limit(Limit.all()).model(),
                qem -> {
                    final var entities = co(TgPersonName.class).getAllEntities(qem);
                    assertEquals(keys.reversed(), entities.stream().map(TgPersonName::getKey).toList());
                });
    }

    @Test
    public void query_with_offset_skips_specified_number_of_rows_from_start() {
        final var offset = 1;
        final var total = 3;
        final var keys = rangeClosed(1, total).mapToObj(i -> TEST_DATA_KEY_PREFIX + i).toList();
        keys.stream()
                .map(key -> new_(TgPersonName.class, key))
                .forEach(this::save);

        // ascending order
        withQem(select(TgPersonName.class).where().condition(testDataCond),
                $ -> $.prop("key").asc().offset(offset).model(),
                $ -> $.prop("key").asc().offset(offset).model(),
                qem -> {
                    final var entities = co(TgPersonName.class).getAllEntities(qem);
                    assertEquals(keys.subList(1, total), entities.stream().map(TgPersonName::getKey).toList());
                });

        // descending order
        withQem(select(TgPersonName.class).where().condition(testDataCond),
                $ -> $.prop("key").desc().offset(offset).model(),
                $ -> $.prop("key").desc().offset(offset).model(),
                qem -> {
                    final var entities = co(TgPersonName.class).getAllEntities(qem);
                    assertEquals(keys.reversed().subList(1, total), entities.stream().map(TgPersonName::getKey).toList());
                });
   }

    @Test
    public void query_with_zero_offset_doesnt_skip_anything() {
        final var offset = 0;
        final var total = 3;
        assertTrue(offset == 0);
        final var keys = rangeClosed(1, total).mapToObj(i -> TEST_DATA_KEY_PREFIX + i).toList();
        keys.stream()
                .map(key -> new_(TgPersonName.class, key))
                .forEach(this::save);

        withQem(select(TgPersonName.class).where().condition(testDataCond),
                $ -> $.prop("key").asc().offset(offset).model(),
                $ -> $.prop("key").asc().offset(offset).model(),
                qem -> {
                    final var entities = co(TgPersonName.class).getAllEntities(qem);
                    assertEquals(keys, entities.stream().map(TgPersonName::getKey).toList());
                });
    }

    @Test
    public void query_with_offset_greater_than_total_number_of_rows_returns_nothing() {
        final var offset = 2;
        final var total = 1;
        assertTrue(offset > total);
        final var keys = rangeClosed(1, total).mapToObj(i -> TEST_DATA_KEY_PREFIX + i).toList();
        keys.stream()
                .map(key -> new_(TgPersonName.class, key))
                .forEach(this::save);

        withQem(select(TgPersonName.class).where().condition(testDataCond),
                $ -> $.prop("key").desc().offset(offset).model(),
                $ -> $.prop("key").desc().offset(offset).model(),
                qem -> {
                    final var entities = co(TgPersonName.class).getAllEntities(qem);
                    assertEquals(List.of(), entities.stream().map(TgPersonName::getKey).toList());
                });
    }

    @Test
    public void query_with_limit_and_offset_applies_both() {
        final var limit = 3;
        final var offset = 1;
        final var total = 5;
        final var keys = rangeClosed(1, total).mapToObj(i -> TEST_DATA_KEY_PREFIX + i).toList();
        keys.stream()
                .map(key -> new_(TgPersonName.class, key))
                .forEach(this::save);

        // ascending order
        withQem(select(TgPersonName.class).where().condition(testDataCond),
                $ -> $.prop("key").asc().limit(limit).offset(offset).model(),
                $ -> $.prop("key").asc().limit(limit).offset(offset).model(),
                qem -> {
                    final var entities = co(TgPersonName.class).getAllEntities(qem);
                    assertEquals(keys.subList(offset, offset + limit),
                                 entities.stream().map(TgPersonName::getKey).toList());
                });
        // descending order
        withQem(select(TgPersonName.class).where().condition(testDataCond),
                $ -> $.prop("key").desc().limit(limit).offset(offset).model(),
                $ -> $.prop("key").desc().limit(limit).offset(offset).model(),
                qem -> {
                    final var entities = co(TgPersonName.class).getAllEntities(qem);
                    assertEquals(keys.reversed().subList(offset, offset + limit),
                                 entities.stream().map(TgPersonName::getKey).toList());
                });
    }

    /**
     * Runs an action twice: once with a {@linkplain QueryExecutionModel QEM} built with an odering model inside a query,
     * and once with a QEM built with a standalone ordering model. This doubles test coverage while reducing the amount
     * of tests.
     *
     * @param query  base query to which an ordering model will be applied
     * @param inQuery  builds an ordering model inside a query
     * @param standalone  builds a standalone ordering model
     * @param action  action to execute
     */
    private <E extends AbstractEntity<?>> void withQem(
            final ICompleted<E> query,
            final Function<IOrderingItem1<E>, EntityResultQueryModel<E>> inQuery,
            final Function<StandaloneOrderBy.IOrderingItem, OrderingModel> standalone,
            final Consumer<QueryExecutionModel<E, ?>> action)
    {
        // don't wrap caught exceptions to avoid messing with tools that parse JUnit assertion failures, instead use a logger
        final var queryWithOrderBy = from(inQuery.apply(query.orderBy())).model();
        try {
            action.accept(queryWithOrderBy);
        } catch (final Throwable e) {
            logger.error("Failure while testing with an order by inside a query.");
            throw e;
        }

        final var queryWithStandaloneOrderBy = from(query.model()).with(standalone.apply(orderBy())).model();
        try {
            action.accept(queryWithStandaloneOrderBy);
        } catch (final Throwable e) {
            logger.error("Failure while testing with a standalone order by.");
            throw e;
        }
    }

}
