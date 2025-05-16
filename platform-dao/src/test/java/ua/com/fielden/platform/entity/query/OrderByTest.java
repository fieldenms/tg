package ua.com.fielden.platform.entity.query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.entity.query.exceptions.EqlValidationException;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompleted;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IOrderingItem1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.StandaloneOrderBy;
import ua.com.fielden.platform.entity.query.fluent.Limit;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.eql.retrieval.exceptions.EntityRetrievalException;
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

    private static final Logger logger = LogManager.getLogger();

    private static final String TEST_DATA_KEY_PREFIX = "TEST_ORDER_BY_";
    private static final ConditionModel testDataCond = cond().prop("key").like().val(TEST_DATA_KEY_PREFIX + "%").model();
    public static final int MAX_ENTITY_KEY_INDEX = 5;

    @Test
    public void limit_must_not_be_null() {
        try {
            select(TgPersonName.class).where().condition(testDataCond)
                    .orderBy().prop("key").desc().limit(null)
                    .model();
            fail();
        } catch (final EqlException ex) {
            assertEquals(EqlException.ERR_NULL_ARGUMENT.formatted("limit"), ex.getMessage());
        }
    }

    @Test
    public void limit_cannot_be_negative() {
        final var negativeLimit = -1;
        try {
            select(TgPersonName.class).where().condition(testDataCond)
                    .orderBy().prop("key").desc().limit(negativeLimit)
                    .model();
            fail();
        } catch (final EqlValidationException ex) {
            assertEquals(EqlValidationException.ERR_LIMIT_GREATER_THAN_ZERO.formatted(negativeLimit), ex.getMessage());
        }

        try {
            select(TgPersonName.class).where().condition(testDataCond)
                    .orderBy().prop("key").desc().limit(Limit.count(negativeLimit))
                    .model();
            fail();
        } catch (final EqlValidationException ex) {
            assertEquals(EqlValidationException.ERR_LIMIT_GREATER_THAN_ZERO.formatted(negativeLimit), ex.getMessage());
        }

        try {
            orderBy().prop("key").asc().limit(negativeLimit).model();
            fail();
        } catch (final EqlValidationException ex) {
            assertEquals(EqlValidationException.ERR_LIMIT_GREATER_THAN_ZERO.formatted(negativeLimit), ex.getMessage());
        }

        try {
            orderBy().prop("key").asc().limit(Limit.count(negativeLimit)).model();
            fail();
        } catch (final EqlValidationException ex) {
            assertEquals(EqlValidationException.ERR_LIMIT_GREATER_THAN_ZERO.formatted(negativeLimit), ex.getMessage());
        }
    }

    @Test
    public void limit_cannot_be_zero() {
        final var zeroLimit = 0;
        try {
            select(TgPersonName.class).where().condition(testDataCond)
                    .orderBy().prop("key").desc().limit(zeroLimit)
                    .model();
            fail();
        } catch (final EqlValidationException ex) {
            assertEquals(EqlValidationException.ERR_LIMIT_GREATER_THAN_ZERO.formatted(zeroLimit), ex.getMessage());
        }

        try {
            select(TgPersonName.class).where().condition(testDataCond)
                    .orderBy().prop("key").desc().limit(Limit.count(zeroLimit))
                    .model();
            fail();
        } catch (final EqlValidationException ex) {
            assertEquals(EqlValidationException.ERR_LIMIT_GREATER_THAN_ZERO.formatted(zeroLimit), ex.getMessage());
        }

        try {
            orderBy().prop("key").asc().limit(zeroLimit).model();
            fail();
        } catch (final EqlValidationException ex) {
            assertEquals(EqlValidationException.ERR_LIMIT_GREATER_THAN_ZERO.formatted(zeroLimit), ex.getMessage());
        }

        try {
            orderBy().prop("key").asc().limit(Limit.count(zeroLimit)).model();
            fail();
        } catch (final EqlValidationException ex) {
            assertEquals(EqlValidationException.ERR_LIMIT_GREATER_THAN_ZERO.formatted(zeroLimit), ex.getMessage());
        }
    }

    @Test
    public void offset_must_be_non_negative() {
        final var negativeOffset = -1;
        try {
            select(TgPersonName.class).where().condition(testDataCond)
                    .orderBy().prop("key").desc().offset(negativeOffset)
                    .model();
            fail();
        } catch (final EqlValidationException ex) {
            assertEquals(EqlValidationException.ERR_OFFSET_NON_NEGATIVE.formatted(negativeOffset), ex.getMessage());
        }

        try {
            orderBy().prop("key").asc().offset(negativeOffset).model();
            fail();
        } catch (final EqlValidationException ex) {
            assertEquals(EqlValidationException.ERR_OFFSET_NON_NEGATIVE.formatted(negativeOffset), ex.getMessage());
        }
    }

    @Test
    public void orderBy_can_be_used_in_a_top_level_query() {
        final var entities = allEntities();

        withQem(select(TgPersonName.class).where().condition(testDataCond),
                $ -> $.prop("key").desc().model(),
                $ -> $.prop("key").desc().model(),
                qem -> assertEquals(entities.reversed(), co(TgPersonName.class).getAllEntities(qem)));
    }

    @Test
    public void orderBy_can_be_used_in_a_subquery() {
        final var entities = allEntities();

        final var query = select(select(TgPersonName.class).where().condition(testDataCond)
                                 .orderBy().prop("key").desc()
                                 .model())
                          .model();
        assertEquals(entities.reversed(), co(TgPersonName.class).getAllEntities(from(query).model()));
    }

    @Test
    public void ordering_model_cannot_be_specified_both_as_standalone_and_as_part_of_a_query() {
        final var qem = from(
                // an ordered query
                select(TgPersonName.class).where().condition(testDataCond)
                        .orderBy().prop("key").desc()
                        .model())
                // an orderBy model that conflicts with the ordered query
                .with(orderBy().prop("id").asc().model())
                .model();

        assertThrows(EntityRetrievalException.class,
                     () -> co(TgPersonName.class).getAllEntities(qem));
    }

    @Test
    public void ordering_model_can_be_used_together_with_ordering_in_subqueries() {
        final var entities = allEntities();

        // select data ordered by key descending
        final var orderedSubquery = select(TgPersonName.class).where().condition(testDataCond).orderBy().prop("key").desc().model();
        final var qem = from(select(orderedSubquery).model())
                        // ordering of the result by key ascending, which is the reversal of the ordering in subquery
                        .with(orderBy().prop("key").asc().model())
                        .model();
        assertEquals(entities, co(TgPersonName.class).getAllEntities(qem));
    }

    @Test
    public void query_with_limit_returns_the_specified_number_of_rows_if_the_total_number_of_rows_is_greater_than_limit() {
        final var entities = allEntities();
        final var total = entities.size();
        final var limit = total - 1;
        assertTrue(limit < total);

        withQem(select(TgPersonName.class).where().condition(testDataCond),
                $ -> $.prop("key").desc().limit(limit).model(),
                $ -> $.prop("key").desc().limit(limit).model(),
                qem -> assertEquals(entities.reversed().subList(0, limit), co(TgPersonName.class).getAllEntities(qem)));
    }

    @Test
    public void query_with_limit_returns_all_rows_if_the_total_number_of_rows_is_less_than_limit() {
        final var entities = allEntities();
        final var total = entities.size();
        final var limit = total + 1;
        assertTrue(limit > total);

        withQem(select(TgPersonName.class).where().condition(testDataCond),
                $ -> $.prop("key").asc().limit(limit).model(),
                $ -> $.prop("key").asc().limit(limit).model(),
                qem -> assertEquals(entities, co(TgPersonName.class).getAllEntities(qem)));
    }

    @Test
    public void query_with_limit_returns_all_rows_if_the_total_number_of_rows_is_equal_to_limit() {
        final var entities = allEntities();
        final var total = entities.size();
        final var limit = total;
        assertTrue(limit == total && limit > 0);

        withQem(select(TgPersonName.class).where().condition(testDataCond),
                $ -> $.prop("key").asc().limit(limit).model(),
                $ -> $.prop("key").asc().limit(limit).model(),
                qem -> assertEquals(entities, co(TgPersonName.class).getAllEntities(qem)));
    }

    @Test
    public void query_with_limit_all_returns_all_rows() {
        final var entities = allEntities();

        withQem(select(TgPersonName.class).where().condition(testDataCond),
                $ -> $.prop("key").desc().limit(Limit.all()).model(),
                $ -> $.prop("key").desc().limit(Limit.all()).model(),
                qem -> assertEquals(entities.reversed(), co(TgPersonName.class).getAllEntities(qem)));
    }

    @Test
    public void query_with_offset_skips_specified_number_of_rows() {
        final var entities = allEntities();
        final var total = entities.size();
        final var offset = 1;

        // ascending order
        withQem(select(TgPersonName.class).where().condition(testDataCond),
                $ -> $.prop("key").asc().offset(offset).model(),
                $ -> $.prop("key").asc().offset(offset).model(),
                qem -> assertEquals(entities.subList(1, total), co(TgPersonName.class).getAllEntities(qem)));

        // descending order
        withQem(select(TgPersonName.class).where().condition(testDataCond),
                $ -> $.prop("key").desc().offset(offset).model(),
                $ -> $.prop("key").desc().offset(offset).model(),
                qem -> assertEquals(entities.reversed().subList(1, total), co(TgPersonName.class).getAllEntities(qem)));
   }

    @Test
    public void query_with_zero_offset_does_not_skip_anything() {
        final var entities = allEntities();
        final var offset = 0;
        assertTrue(offset == 0);

        withQem(select(TgPersonName.class).where().condition(testDataCond),
                $ -> $.prop("key").asc().offset(offset).model(),
                $ -> $.prop("key").asc().offset(offset).model(),
                qem -> assertEquals(entities, co(TgPersonName.class).getAllEntities(qem)));
    }

    @Test
    public void query_with_offset_greater_than_total_number_of_rows_returns_nothing() {
        final var entities = allEntities();
        final var total = entities.size();
        final var offset = total + 1;
        assertTrue(offset > total);

        withQem(select(TgPersonName.class).where().condition(testDataCond),
                $ -> $.prop("key").desc().offset(offset).model(),
                $ -> $.prop("key").desc().offset(offset).model(),
                qem -> assertEquals(List.of(), co(TgPersonName.class).getAllEntities(qem)));
    }

    @Test
    public void query_with_limit_and_offset_applies_both() {
        final var entities = allEntities();
        final var total = entities.size();
        final var limit = total - 2;
        final var offset = 1;

        // ascending order
        withQem(select(TgPersonName.class).where().condition(testDataCond),
                $ -> $.prop("key").asc().limit(limit).offset(offset).model(),
                $ -> $.prop("key").asc().limit(limit).offset(offset).model(),
                qem -> assertEquals(entities.subList(offset, offset + limit), co(TgPersonName.class).getAllEntities(qem)));

        // descending order
        withQem(select(TgPersonName.class).where().condition(testDataCond),
                $ -> $.prop("key").desc().limit(limit).offset(offset).model(),
                $ -> $.prop("key").desc().limit(limit).offset(offset).model(),
                qem -> assertEquals(entities.reversed().subList(offset, offset + limit), co(TgPersonName.class).getAllEntities(qem)));
    }

    @Test
    public void orderBy_can_be_applied_to_multiple_subqueries() {
        final var entities = allEntities();

        // first 2 + last 3
        withQem(select(select(TgPersonName.class).where().condition(testDataCond)
                               .orderBy().prop("key").asc()
                               .limit(2)
                               .model(),
                       select(TgPersonName.class).where().condition(testDataCond)
                               .orderBy().prop("key").asc()
                               .limit(3)
                               .offset(2)
                               .model()),
                $ -> $.prop("key").asc().model(),
                $ -> $.prop("key").asc().model(),
                qem -> assertEquals(entities, co(TgPersonName.class).getAllEntities(qem)));
    }

    @Test
    public void sanity_check_for_the_order_of_allEntities() {
        final var entities = allEntities();
        assertEquals(MAX_ENTITY_KEY_INDEX, entities.size());
        for (int index = 0; index < entities.size(); index++) {
            assertEquals(TEST_DATA_KEY_PREFIX + (index + 1), entities.get(index).getKey());
        }
    }

    @Test
    public void an_order_by_list_may_contain_duplicates_01() {
        final var query = select(TgPersonName.class)
                .orderBy().prop("key").asc()
                .val(1).asc()
                .prop("key").desc()
                .model();

        final var expected = allEntities();
        final var actual = co$(TgPersonName.class).getAllEntities(QueryExecutionModel.from(query).model());
        assertEquals(expected, actual);
    }

    @Test
    public void an_order_by_list_may_contain_duplicates_02() {
        final var query = select(TgPersonName.class)
                .orderBy().prop("key").asc()
                .val(1).asc()
                .val(1).desc()
                .model();

        final var expected = allEntities();
        final var actual = co$(TgPersonName.class).getAllEntities(QueryExecutionModel.from(query).model());
        assertEquals(expected, actual);
    }

    @Test
    public void an_order_by_list_may_contain_duplicates_03() {
        final var expr = expr().val(1).add().prop("id").model();
        final var query = select(TgPersonName.class)
                .orderBy().prop("key").asc()
                .expr(expr).asc()
                .expr(expr).asc()
                .model();

        final var expected = allEntities();
        final var actual = co$(TgPersonName.class).getAllEntities(from(query).model());
        assertEquals(expected, actual);
    }

    @Test
    public void an_order_by_list_may_contain_duplicates_04() {
        final var query = select(TgPersonName.class)
                .orderBy().prop("key").asc()
                .concat().prop("key").with().val("!").end().asc()
                .concat().prop("key").with().val("!").end().asc()
                .model();

        final var expected = allEntities();
        final var actual = co$(TgPersonName.class).getAllEntities(from(query).model());
        assertEquals(expected, actual);
    }

    @Test
    public void an_order_by_list_may_contain_duplicates_05() {
        final var query = select(TgPersonName.class)
                .orderBy().prop("key").asc()
                .model(select().yield().val(1).modelAsPrimitive()).asc()
                .model(select().yield().val(1).modelAsPrimitive()).desc()
                .model(select().yield().val(1).modelAsPrimitive()).asc()
                .prop("key").desc()
                .model();

        final var expected = allEntities();
        final var actual = co$(TgPersonName.class).getAllEntities(from(query).model());
        assertEquals(expected, actual);
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Utilities
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    /**
     * A helper method to retrieve all test entities in the specific order.
     *
     * @return
     */
    private List<TgPersonName> allEntities() {
        final var query = select(TgPersonName.class).where().condition(testDataCond).model();
        final var orderBy = orderBy().prop("key").asc().model();
        final var qem = from(query).with(orderBy).model();
        return co(TgPersonName.class).getAllEntities(qem);
    }

    /**
     * Runs an action twice: once with a {@linkplain QueryExecutionModel QEM} built with an ordering model inside a query,
     * and once with a QEM built with a standalone ordering model.
     * This doubles test coverage while reducing the number of tests.
     *
     * @param query  base query to which an ordering model is applied
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
        // Do not wrap caught exceptions to avoid messing with tools that parse JUnit assertion failures, instead use a logger.
        final var queryWithOrderBy = from(inQuery.apply(query.orderBy())).model();
        try {
            action.accept(queryWithOrderBy);
        } catch (final Throwable ex) {
            logger.error("Failure while testing with an order by inside a query.");
            throw ex;
        }

        final var queryWithStandaloneOrderBy = from(query.model()).with(standalone.apply(orderBy())).model();
        try {
            action.accept(queryWithStandaloneOrderBy);
        } catch (final Throwable ex) {
            logger.error("Failure while testing with a standalone order by.");
            throw ex;
        }
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();

        if (useSavedDataPopulationScript()) {
            return;
        }

        rangeClosed(1, MAX_ENTITY_KEY_INDEX)
        .mapToObj(i -> TEST_DATA_KEY_PREFIX + i)
        .map(key -> new_(TgPersonName.class, key))
        .forEach(this::save);

    }
}
