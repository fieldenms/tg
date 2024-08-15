package ua.com.fielden.platform.entity.query.fetching;

import org.junit.Test;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.sample.domain.TgPersonName;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import static java.util.stream.IntStream.rangeClosed;
import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.dao.QueryExecutionModel.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

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

}
