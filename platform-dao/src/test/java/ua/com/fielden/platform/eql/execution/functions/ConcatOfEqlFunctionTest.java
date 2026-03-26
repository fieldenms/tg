package ua.com.fielden.platform.eql.execution.functions;

import org.junit.Test;
import ua.com.fielden.platform.eql.execution.AbstractEqlExecutionTestCase;
import ua.com.fielden.platform.sample.domain.TgPersonName;
import ua.com.fielden.platform.types.Money;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

public class ConcatOfEqlFunctionTest extends AbstractEqlExecutionTestCase {

    @Test
    public void concatOf_prop_without_orderBy() {
        final var qry = select(TgPersonName.class)
                .yield().concatOf().prop("key").separator().val(", ").as(RESULT)
                .modelAsAggregate();
        // Without intra-aggregate orderBy the result order is nondeterministic.
        assertThat(retrieveResult(qry))
                .isIn("Alan Turing, John Conway", "John Conway, Alan Turing");
    }

    @Test
    public void concatOf_prop_with_orderBy_asc() {
        final var qry = select(TgPersonName.class)
                .yield().concatOf().prop("key").orderBy().prop("key").asc().separator().val(", ").as(RESULT)
                .modelAsAggregate();
        assertThat(retrieveResult(qry))
                .isEqualTo("Alan Turing, John Conway");
    }

    @Test
    public void concatOf_prop_with_orderBy_desc() {
        final var qry = select(TgPersonName.class)
                .yield().concatOf().prop("key").orderBy().prop("key").desc().separator().val(", ").as(RESULT)
                .modelAsAggregate();
        assertThat(retrieveResult(qry))
                .isEqualTo("John Conway, Alan Turing");
    }

    @Test
    public void concatOf_prop_inside_a_function_with_orderBy() {
        final var qry = select(TgPersonName.class)
                .yield().concatOf().upperCase().prop("key").orderBy().prop("key").asc().separator().val(", ").as(RESULT)
                .modelAsAggregate();
        assertThat(retrieveResult(qry))
                .isEqualTo("ALAN TURING, JOHN CONWAY");
    }

    @Test
    public void concatOf_val() {
        final var qry = select(TgPersonName.class)
                .yield().concatOf().val("word").separator().val(", ").as(RESULT)
                .modelAsAggregate();
        assertThat(retrieveResult(qry))
                .isEqualTo("word, word");
    }

    @Test
    public void concatOf_separated_by_param() {
        final var qry = select(TgPersonName.class)
                .yield().concatOf().val("word").separator().param("sep").as(RESULT)
                .modelAsAggregate();
        assertThat(retrieveResult(qry, Map.of("sep", ":")))
                .isEqualTo("word:word");
    }

    @Test
    public void concatOf_skips_null_values() {
        final var qry = select(select(TgPersonName.class).yield().prop("key").as("x").modelAsAggregate(),
                               select().yield().val(null).as("x").modelAsAggregate())
                .yield().concatOf().prop("x").orderBy().prop("x").asc().separator().val(", ").as(RESULT)
                .modelAsAggregate();
        assertThat(retrieveResult(qry))
                .isEqualTo("Alan Turing, John Conway");
    }

    @Test
    public void concatOf_of_concat_expression_with_orderBy_rank() {
        // Concatenates a computed expression (name + rank suffix), ordered by rank.
        final var qry = select(
                select().yield().val("Alice").as("name").yield().val(1).as("rank").modelAsAggregate(),
                select().yield().val("Bob").as("name").yield().val(2).as("rank").modelAsAggregate())
                .yield().concatOf().concat().prop("name").with().val("-").with().prop("rank").end()
                    .orderBy().prop("rank").desc()
                    .separator().val("; ").as(RESULT)
                .modelAsAggregate();
        assertThat(retrieveResult(qry))
                .isEqualTo("Bob-2; Alice-1");
    }

    @Test
    public void concatOf_of_concat_expression_with_orderBy_name() {
        // Concatenates a computed expression (name + rank suffix), ordered by name.
        final var qry = select(
                select().yield().val("Alice").as("name").yield().val(1).as("rank").modelAsAggregate(),
                select().yield().val("Bob").as("name").yield().val(2).as("rank").modelAsAggregate())
                .yield().concatOf().concat().prop("name").with().val("-").with().prop("rank").end()
                .orderBy().prop("name").asc()
                .separator().val("; ").as(RESULT)
                .modelAsAggregate();
        assertThat(retrieveResult(qry))
                .isEqualTo("Alice-1; Bob-2");
    }

    @Test
    public void concatOf_with_orderBy_using_function_expression() {
        // upperCase() reversal test: ordering by upperCase DESC should reverse alphabetical order.
        final var qry = select(TgPersonName.class)
                .yield().concatOf().prop("key").orderBy().upperCase().prop("key").desc().separator().val(", ").as(RESULT)
                .modelAsAggregate();
        assertThat(retrieveResult(qry))
                .isEqualTo("John Conway, Alan Turing");
    }

    @Test
    public void concatOf_with_orderBy_prop_which_is_not_referenced_elsewhere() {
        final var qry1 = select(
                select().yield().val("Alice").as("name").yield().val(1).as("rank").modelAsAggregate(),
                select().yield().val("Bob").as("name").yield().val(2).as("rank").modelAsAggregate())
                .yield().concatOf().prop("name").orderBy().prop("rank").asc().separator().val(", ").as(RESULT)
                .modelAsAggregate();
        assertThat(retrieveResult(qry1))
                .isEqualTo("Alice, Bob");

        final var qry2 = select(
                select().yield().val("Alice").as("name").yield().val(1).as("rank").modelAsAggregate(),
                select().yield().val("Bob").as("name").yield().val(2).as("rank").modelAsAggregate())
                .yield().concatOf().prop("name").orderBy().prop("rank").desc().separator().val(", ").as(RESULT)
                .modelAsAggregate();
        assertThat(retrieveResult(qry2))
                .isEqualTo("Bob, Alice");
    }

    @Test
    public void concatOf_with_orderBy_using_OrderingModel() {
        final var orderByModel = orderBy().prop("key").asc().model();
        final var qry = select(TgPersonName.class)
                .yield().concatOf().prop("key").orderBy().order(orderByModel).separator().val(", ").as(RESULT)
                .modelAsAggregate();
        assertThat(retrieveResult(qry))
                .isEqualTo("Alan Turing, John Conway");
    }

    @Test
    public void concatOf_with_orderBy_using_OrderingModel_desc() {
        final var orderByModel = orderBy().prop("key").desc().model();
        final var qry = select(TgPersonName.class)
                .yield().concatOf().prop("key").orderBy().order(orderByModel).separator().val(", ").as(RESULT)
                .modelAsAggregate();
        assertThat(retrieveResult(qry))
                .isEqualTo("John Conway, Alan Turing");
    }

    @Test
    public void concatOf_of_Integer_expression() {
        final var qry = select(
                select().yield().val(1).as("num").modelAsAggregate(),
                select().yield().val(2).as("num").modelAsAggregate())
                .yield().concatOf().prop("num").orderBy().prop("num").asc().separator().val(", ").as(RESULT)
                .modelAsAggregate();
        assertThat(retrieveResult(qry))
                .isInstanceOf(String.class)
                .isEqualTo("1, 2");
    }

    @Test
    public void concatOf_of_Decimal_expression() {
        final var qry = select(
                select().yield().val(new BigDecimal("1.50")).as("amount").modelAsAggregate(),
                select().yield().val(new BigDecimal("2.75")).as("amount").modelAsAggregate())
                .yield().concatOf().prop("amount").orderBy().prop("amount").asc().separator().val("; ").as(RESULT)
                .modelAsAggregate();
        assertThat(retrieveResult(qry))
                .isInstanceOf(String.class)
                .isIn("1.50; 2.75", "1.5; 2.75");
    }

    @Test
    public void concatOf_of_Money_expression() {
        final var qry = select(
                select().yield().val(new Money("1.50")).as("cost").modelAsAggregate(),
                select().yield().val(new Money("2.75")).as("cost").modelAsAggregate())
                .yield().concatOf().prop("cost").orderBy().prop("cost").desc().separator().val("; ").as(RESULT)
                .modelAsAggregate();
        assertThat(retrieveResult(qry))
                .isInstanceOf(String.class)
                .isEqualTo("2.7500; 1.5000");
    }

    @Test
    public void concatOf_of_Date_expression() {
        final var qry = select(
                select().yield().val(date("2026-03-26 10:00:00")).as("date").modelAsAggregate(),
                select().yield().val(date("2026-03-26 11:15:00")).as("date").modelAsAggregate())
                .yield().concatOf().prop("date").orderBy().prop("date").desc().separator().val("; ").as(RESULT)
                .modelAsAggregate();
        assertThat(retrieveResult(qry))
                .isInstanceOf(String.class)
                .isEqualTo("26/03/2026 11:15; 26/03/2026 10:00");
    }


    @Override
    protected void populateDomain() {
        super.populateDomain();

        save(new_(TgPersonName.class, "John Conway"));
        save(new_(TgPersonName.class, "Alan Turing"));
    }

}
