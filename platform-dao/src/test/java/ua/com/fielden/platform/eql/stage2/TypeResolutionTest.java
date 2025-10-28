package ua.com.fielden.platform.eql.stage2;

import org.junit.Test;
import ua.com.fielden.platform.eql.exceptions.EqlStage2ProcessingException;
import ua.com.fielden.platform.eql.meta.EqlStage2TestCase;
import ua.com.fielden.platform.sample.domain.TgBogieLocation;
import ua.com.fielden.platform.sample.domain.TgWagon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.eql.meta.PropType.*;
import static ua.com.fielden.platform.persistence.HibernateConstants.H_ENTITY;

public class TypeResolutionTest extends EqlStage2TestCase {

    @Test
    public void caseWhen_with_null_and_not_null_result_types_has_the_not_null_type() {
        final var actQry = qry(select(BOGIE)
                                       .yield().caseWhen().prop("id").isNotNull().then().val("hello")
                                               .otherwise().val(null)
                                               .end()
                                       .as("x")
                                       .modelAsEntity(BOGIE));

        assertThat(actQry.yields.getYieldsMap())
                .hasEntrySatisfying("x", yield -> assertThat(yield.operand().type()).isEqualTo(STRING_PROP_TYPE));
    }

    @Test
    public void caseWhen_with_multiple_distinct_result_types_and_null_type_has_one_of_the_not_null_types() {
        final var actQry = qry(select(BOGIE)
                                       .yield().caseWhen().prop("id").isNotNull().then().val("hello")
                                               .when().prop("version").gt().val(0).then().val(42)
                                               .when().prop("active").eq().val(true).then().now()
                                               .when().prop("refCount").isNull().then().val(null)
                                               .when().prop("location").isNotNull().then().model(select(TgBogieLocation.class).model())
                                               .otherwise().val(null)
                                               .end()
                                       .as("x")
                                       .modelAsEntity(BOGIE));

        assertThat(actQry.yields.getYieldsMap())
                .hasEntrySatisfying("x", yield -> assertThat(yield.operand().type()).isIn(STRING_PROP_TYPE, INTEGER_PROP_TYPE, DATETIME_PROP_TYPE));
    }

    @Test
    public void type_of_caseWhen_with_multiple_equal_entity_result_types_resolves_to_a_single_entity_type() {
        final var actQry = qry(select(BOGIE)
                                       .yield().caseWhen().prop("id").isNotNull().then().model(select(TgBogieLocation.class).model())
                                               .otherwise().model(select(TgBogieLocation.class).model())
                                               .end()
                                       .as("x")
                                       .modelAsEntity(BOGIE));

        assertThat(actQry.yields.getYieldsMap())
                .hasEntrySatisfying("x", yield -> assertThat(yield.operand().type()).isEqualTo(propType(TgBogieLocation.class, H_ENTITY)));
    }

    @Test
    public void type_of_caseWhen_with_multiple_equal_entity_result_types_and_null_resolves_to_a_single_entity_type() {
        final var actQry = qry(select(BOGIE)
                                       .yield().caseWhen().prop("id").isNotNull().then().model(select(TgBogieLocation.class).model())
                                               .when().prop("version").isNotNull().then().model(select(TgBogieLocation.class).model())
                                               .otherwise().val(null)
                                       .end()
                                       .as("x")
                                       .modelAsEntity(BOGIE));

        assertThat(actQry.yields.getYieldsMap())
                .hasEntrySatisfying("x", yield -> assertThat(yield.operand().type()).isEqualTo(propType(TgBogieLocation.class, H_ENTITY)));
    }

    @Test
    public void type_of_caseWhen_with_distinct_entity_result_types_cannot_be_resolved() {
        final var query = select(BOGIE)
                .yield().caseWhen().prop("id").isNotNull().then().model(select(TgBogieLocation.class).model())
                        .when().prop("version").isNotNull().then().model(select(TgWagon.class).model())
                        .otherwise().val(null)
                        .end()
                .as("x")
                .modelAsEntity(BOGIE);

        assertThatThrownBy(() -> qry(query))
                .isInstanceOf(EqlStage2ProcessingException.class)
                .hasMessageStartingWith("Can't determine type with highest precedence");
    }

}
