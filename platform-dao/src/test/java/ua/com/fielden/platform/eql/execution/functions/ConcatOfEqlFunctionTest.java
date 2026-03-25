package ua.com.fielden.platform.eql.execution.functions;

import org.junit.Test;
import ua.com.fielden.platform.eql.execution.AbstractEqlExecutionTestCase;
import ua.com.fielden.platform.sample.domain.TgPersonName;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Override
    protected void populateDomain() {
        super.populateDomain();

        save(new_(TgPersonName.class, "John Conway"));
        save(new_(TgPersonName.class, "Alan Turing"));
    }

}
