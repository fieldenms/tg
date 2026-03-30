package ua.com.fielden.platform.entity.query.metadata;

import org.junit.Test;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.metadata.test_entities.*;
import ua.com.fielden.platform.sample.domain.TgPerson;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.Money;

import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

public class CompositeKeyEqlExpressionExecutionTest extends AbstractDaoTestCase {

    @Test
    public void single_required_non_string_key_member() {
        final var entity = save(new_(CompositeKeyEqlExpression_Entity1.class).setDate(date("2026-01-01")));
        assertKeyEquals("01/01/2026", entity);
    }

    @Test
    public void single_optional_non_string_key_member_when_set() {
        final var entity = save(new_(CompositeKeyEqlExpression_Entity2.class).setDate(date("2026-01-01")));
        assertKeyEquals("01/01/2026", entity);
    }

    @Test
    public void single_required_string_key_member() {
        final var entity = save(new_(CompositeKeyEqlExpression_Entity3.class).setName("hello"));
        assertKeyEquals("hello", entity);
    }

    @Test
    public void single_optional_string_key_member_when_set() {
        final var entity = save(new_(CompositeKeyEqlExpression_Entity4.class).setName("hello"));
        assertKeyEquals("hello", entity);
    }

    @Test
    public void single_required_entity_key_member() {
        final var alice = save(new_(TgPerson.class, "Alice").setActive(true));
        final var entity = save(new_(CompositeKeyEqlExpression_Entity5.class).setPerson(alice));
        assertKeyEquals("Alice", entity);
    }

    @Test
    public void single_optional_entity_key_member_when_set() {
        final var alice = save(new_(TgPerson.class, "Alice").setActive(true));
        final var entity = save(new_(CompositeKeyEqlExpression_Entity6.class).setPerson(alice));
        assertKeyEquals("Alice", entity);
    }

    @Test
    public void two_optional_key_members_when_both_are_set() {
        final var entity = save(new_(CompositeKeyEqlExpression_Entity7.class).setDate1(date("2026-01-01")).setDate2(date("2026-06-15")));
        assertKeyEquals("01/01/2026 15/06/2026", entity);
    }

    @Test
    public void two_optional_key_members_when_only_first_is_set() {
        final var entity = save(new_(CompositeKeyEqlExpression_Entity7.class).setDate1(date("2026-01-01")).setDate2(null));
        assertKeyEquals("01/01/2026", entity);
    }

    /// Regression test for a bug where only the second optional member was present
    /// and the generated key expression returned the raw (non-string) value rather than its string representation.
    ///
    @Test
    public void two_optional_key_members_when_only_second_is_set() {
        final var entity = save(new_(CompositeKeyEqlExpression_Entity7.class).setDate1(null).setDate2(date("2026-06-15")));
        assertKeyEquals("15/06/2026", entity);
    }

    @Test
    public void two_required_key_members() {
        final var entity = save(new_(CompositeKeyEqlExpression_Entity8.class).setDate(date("2026-01-01")).setName("hello"));
        assertKeyEquals("01/01/2026|hello", entity);
    }

    @Test
    public void one_required_and_one_optional_key_member_when_optional_is_set() {
        final var entity = save(new_(CompositeKeyEqlExpression_Entity9.class).setName("hello").setDate(date("2026-01-01")));
        assertKeyEquals("hello 01/01/2026", entity);
    }

    @Test
    public void one_required_and_one_optional_key_member_when_optional_is_null() {
        final var entity = save(new_(CompositeKeyEqlExpression_Entity9.class).setName("hello").setDate(null));
        assertKeyEquals("hello", entity);
    }

    @Test
    public void one_optional_and_one_required_key_member_when_optional_is_set() {
        final var entity = save(new_(CompositeKeyEqlExpression_Entity10.class).setDate(date("2026-01-01")).setName("hello"));
        assertKeyEquals("01/01/2026 hello", entity);
    }

    @Test
    public void one_optional_and_one_required_key_member_when_optional_is_null() {
        final var entity = save(new_(CompositeKeyEqlExpression_Entity10.class).setDate(null).setName("hello"));
        assertKeyEquals("hello", entity);
    }

    @Test
    public void required_optional_optional_and_required_key_members_when_all_are_set() {
        final var entity = save(new_(CompositeKeyEqlExpression_Entity11.class).setName("alpha").setDate(date("2026-01-01")).setCount(42).setDescription("omega"));
        assertKeyEquals("alpha 01/01/2026 42 omega", entity);
    }

    @Test
    public void required_optional_optional_and_required_key_members_when_first_optional_is_null() {
        final var entity = save(new_(CompositeKeyEqlExpression_Entity11.class).setName("alpha").setDate(null).setCount(42).setDescription("omega"));
        assertKeyEquals("alpha 42 omega", entity);
    }

    @Test
    public void required_optional_optional_and_required_key_members_when_second_optional_is_null() {
        final var entity = save(new_(CompositeKeyEqlExpression_Entity11.class).setName("alpha").setDate(date("2026-01-01")).setCount(null).setDescription("omega"));
        assertKeyEquals("alpha 01/01/2026 omega", entity);
    }

    @Test
    public void required_optional_optional_and_required_key_members_when_both_optionals_are_null() {
        final var entity = save(new_(CompositeKeyEqlExpression_Entity11.class).setName("alpha").setDate(null).setCount(null).setDescription("omega"));
        assertKeyEquals("alpha omega", entity);
    }

    @Test
    public void negative_money_key_member_should_be_formatted_well() {
        final var entity = save(new_(CompositeKeyEqlExpression_Entity12.class)
                .setName("alpha")
                .setPrice(Money.of("-100").withCurrency(Currency.getInstance("UAH"))));
        assertKeyEquals("alpha -₴ 100.00", entity);
    }

    private void assertKeyEquals(final String expected, final Class<? extends AbstractEntity<?>> entityType, final Long id) {
        final var agg = co(EntityAggregates.class).getEntity(
                from(select(entityType)
                             .where().prop(ID).eq().val(id)
                             .yield().prop(KEY).as("x")
                             .modelAsAggregate())
                        .model());
        assertThat(agg.<Object>get("x")).isInstanceOf(String.class);
        assertEquals(expected, agg.get("x"));
    }

    private void assertKeyEquals(final String expected, final AbstractEntity<?> entity) {
        assertNotNull(entity);
        assertNotNull(entity.getId());
        assertKeyEquals(expected, entity.getType(), entity.getId());
    }

}
