package ua.com.fielden.platform.eql.execution.functions;

import org.junit.Test;
import ua.com.fielden.platform.eql.execution.AbstractEqlExecutionTestCase;
import ua.com.fielden.platform.sample.domain.TgPersonName;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

public class ConcatOfEqlFunctionTest extends AbstractEqlExecutionTestCase {

    @Test
    public void concatOf_prop() {
        final var qry = select(TgPersonName.class)
                .yield().concatOf().prop("key").separator().val(", ").as(RESULT)
                .modelAsAggregate();
        // Cannot use orderBy because PostgreSQL doesn't like it.
        assertThat(retrieveResult(qry))
                .isIn("Alan Turing, John Conway", "John Conway, Alan Turing");
    }

    @Test
    public void concatOf_prop_inside_a_function() {
        final var qry = select(TgPersonName.class)
                .yield().concatOf().upperCase().prop("key").separator().val(", ").as(RESULT)
                .modelAsAggregate();
        // Cannot use orderBy because PostgreSQL doesn't like it.
        assertThat(retrieveResult(qry))
                .isIn("ALAN TURING, JOHN CONWAY", "JOHN CONWAY, ALAN TURING");
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
                .yield().concatOf().prop("x").separator().val(", ").as(RESULT)
                .modelAsAggregate();
        // Cannot use orderBy because PostgreSQL doesn't like it.
        assertThat(retrieveResult(qry))
                .isIn("Alan Turing, John Conway", "John Conway, Alan Turing");
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();

        save(new_(TgPersonName.class, "John Conway"));
        save(new_(TgPersonName.class, "Alan Turing"));
    }

}
