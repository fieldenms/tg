package ua.com.fielden.platform.entity.query.fetching;

import org.junit.Test;
import ua.com.fielden.platform.entity.query.fluent.Limit;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.eql.stage0.OrderingModelConflictException;
import ua.com.fielden.platform.sample.domain.TgPersonName;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import java.util.List;

import static java.util.stream.IntStream.rangeClosed;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.dao.QueryExecutionModel.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;

public class OrderByTest extends AbstractDaoTestCase {

    private static final String TEST_DATA_KEY_PREFIX = "TEST_ORDER_BY_";
    private final ConditionModel testDataCond = cond().prop("key").like().val(TEST_DATA_KEY_PREFIX + "%").model();

    @Test
    public void orderBy_can_be_used_in_a_top_level_query() {
        final var keys = rangeClosed(1, 3).mapToObj(i -> TEST_DATA_KEY_PREFIX + i).toList();
        keys.stream()
                .map(key -> new_(TgPersonName.class, key))
                .forEach(this::save);

        // ascending order
        {
            final var qem = from(
                    select(TgPersonName.class).where().condition(testDataCond)
                            .orderBy().prop("key").asc()
                            .model())
                    .model();
            final var entities = co(TgPersonName.class).getAllEntities(qem);
            assertEquals(keys, entities.stream().map(TgPersonName::getKey).toList());
        }

        // descending order
        final var qem = from(
                select(TgPersonName.class).where().condition(testDataCond)
                        .orderBy().prop("key").desc()
                        .model())
                .model();
        final var entities = co(TgPersonName.class).getAllEntities(qem);
        assertEquals(keys.reversed(), entities.stream().map(TgPersonName::getKey).toList());
    }

    @Test
    public void orderBy_can_be_used_in_a_subquery() {
        final var keys = rangeClosed(1, 3).mapToObj(i -> TEST_DATA_KEY_PREFIX + i).toList();
        keys.stream()
                .map(key -> new_(TgPersonName.class, key))
                .forEach(this::save);

        // ascending order
        {
            final var qem = from(
                    select(select(TgPersonName.class).where().condition(testDataCond)
                                   .orderBy().prop("key").asc()
                                   .model())
                            .model())
                    .model();
            final var entities = co(TgPersonName.class).getAllEntities(qem);
            assertEquals(keys, entities.stream().map(TgPersonName::getKey).toList());
        }

        // descending order
        final var qem = from(
                select(select(TgPersonName.class).where().condition(testDataCond)
                               .orderBy().prop("key").desc()
                               .model())
                        .model())
                .model();
        final var entities = co(TgPersonName.class).getAllEntities(qem);
        assertEquals(keys.reversed(), entities.stream().map(TgPersonName::getKey).toList());
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

        // ascending order
        {
            final var qem = from(select(TgPersonName.class).where().condition(testDataCond)
                                         .orderBy().prop("key").asc()
                                         .limit(limit).model())
                    .model();

            final var entities = co(TgPersonName.class).getAllEntities(qem);
            assertEquals(keys.subList(0, limit), entities.stream().map(TgPersonName::getKey).toList());

        }

        // descending order
        {
            final var qem = from(select(TgPersonName.class).where().condition(testDataCond)
                                         .orderBy().prop("key").desc()
                                         .limit(limit).model())
                    .model();

            final var entities = co(TgPersonName.class).getAllEntities(qem);
            assertEquals(keys.reversed().subList(0, limit), entities.stream().map(TgPersonName::getKey).toList());
        }
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

        // ascending order
        {
            final var qem = from(select(TgPersonName.class).where().condition(testDataCond)
                                         .orderBy().prop("key").asc()
                                         .limit(limit).model())
                    .model();

            final var entities = co(TgPersonName.class).getAllEntities(qem);
            assertEquals(keys.subList(0, total), entities.stream().map(TgPersonName::getKey).toList());

        }

        // descending order
        {
            final var qem = from(select(TgPersonName.class).where().condition(testDataCond)
                                         .orderBy().prop("key").desc()
                                         .limit(limit).model())
                    .model();

            final var entities = co(TgPersonName.class).getAllEntities(qem);
            assertEquals(keys.reversed().subList(0, total), entities.stream().map(TgPersonName::getKey).toList());
        }
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

        // ascending order
        {
            final var qem = from(select(TgPersonName.class).where().condition(testDataCond)
                                         .orderBy().prop("key").asc()
                                         .limit(limit).model())
                    .model();

            final var entities = co(TgPersonName.class).getAllEntities(qem);
            assertEquals(keys.subList(0, total), entities.stream().map(TgPersonName::getKey).toList());

        }

        // descending order
        {
            final var qem = from(select(TgPersonName.class).where().condition(testDataCond)
                                         .orderBy().prop("key").desc()
                                         .limit(limit).model())
                    .model();

            final var entities = co(TgPersonName.class).getAllEntities(qem);
            assertEquals(keys.reversed().subList(0, total), entities.stream().map(TgPersonName::getKey).toList());
        }
    }

    @Test
    public void query_with_limit_all_returns_all_rows() {
        final var keys = rangeClosed(1, 3).mapToObj(i -> TEST_DATA_KEY_PREFIX + i).toList();
        keys.stream()
                .map(key -> new_(TgPersonName.class, key))
                .forEach(this::save);

        // ascending order
        {
            final var qem = from(select(TgPersonName.class).where().condition(testDataCond)
                                         .orderBy().prop("key").asc()
                                         .limit(Limit.all()).model())
                    .model();

            final var entities = co(TgPersonName.class).getAllEntities(qem);
            assertEquals(keys, entities.stream().map(TgPersonName::getKey).toList());

        }

        // descending order
        {
            final var qem = from(select(TgPersonName.class).where().condition(testDataCond)
                                         .orderBy().prop("key").desc()
                                         .limit(Limit.all()).model())
                    .model();

            final var entities = co(TgPersonName.class).getAllEntities(qem);
            assertEquals(keys.reversed(), entities.stream().map(TgPersonName::getKey).toList());
        }
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
        {
            final var qem = from(select(TgPersonName.class).where().condition(testDataCond)
                                         .orderBy().prop("key").asc()
                                         .offset(offset).model())
                    .model();

            final var entities = co(TgPersonName.class).getAllEntities(qem);
            assertEquals(keys.subList(1, total), entities.stream().map(TgPersonName::getKey).toList());

        }

        // descending order
        {
            final var qem = from(select(TgPersonName.class).where().condition(testDataCond)
                                         .orderBy().prop("key").desc()
                                         .offset(offset).model())
                    .model();

            final var entities = co(TgPersonName.class).getAllEntities(qem);
            assertEquals(keys.reversed().subList(1, total), entities.stream().map(TgPersonName::getKey).toList());
        }
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

        // ascending order
        {
            final var qem = from(select(TgPersonName.class).where().condition(testDataCond)
                                         .orderBy().prop("key").asc()
                                         .offset(offset).model())
                    .model();

            final var entities = co(TgPersonName.class).getAllEntities(qem);
            assertEquals(keys, entities.stream().map(TgPersonName::getKey).toList());

        }

        // descending order
        {
            final var qem = from(select(TgPersonName.class).where().condition(testDataCond)
                                         .orderBy().prop("key").desc()
                                         .offset(offset).model())
                    .model();

            final var entities = co(TgPersonName.class).getAllEntities(qem);
            assertEquals(keys.reversed(), entities.stream().map(TgPersonName::getKey).toList());
        }
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

        // ascending order
        {
            final var qem = from(select(TgPersonName.class).where().condition(testDataCond)
                                         .orderBy().prop("key").asc()
                                         .offset(offset).model())
                    .model();

            final var entities = co(TgPersonName.class).getAllEntities(qem);
            assertEquals(List.of(), entities.stream().map(TgPersonName::getKey).toList());

        }

        // descending order
        {
            final var qem = from(select(TgPersonName.class).where().condition(testDataCond)
                                         .orderBy().prop("key").desc()
                                         .offset(offset).model())
                    .model();

            final var entities = co(TgPersonName.class).getAllEntities(qem);
            assertEquals(List.of(), entities.stream().map(TgPersonName::getKey).toList());
        }
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
        {
            final var qem = from(select(TgPersonName.class).where().condition(testDataCond)
                                         .orderBy().prop("key").asc()
                                         .limit(limit)
                                         .offset(offset)
                                         .model())
                    .model();

            final var entities = co(TgPersonName.class).getAllEntities(qem);
            assertEquals(keys.subList(offset, offset + limit), entities.stream().map(TgPersonName::getKey).toList());

        }

        // descending order
        {
            final var qem = from(select(TgPersonName.class).where().condition(testDataCond)
                                         .orderBy().prop("key").desc()
                                         .limit(limit)
                                         .offset(offset)
                                         .model())
                    .model();

            final var entities = co(TgPersonName.class).getAllEntities(qem);
            assertEquals(keys.subList(offset, offset + limit).reversed(), entities.stream().map(TgPersonName::getKey).toList());
        }
    }

}
