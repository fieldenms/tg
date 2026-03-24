package ua.com.fielden.platform.entity.query.metadata;

import org.junit.Test;
import ua.com.fielden.platform.entity.query.metadata.test_entities.*;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import static org.assertj.core.api.Assertions.assertThat;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;

public class CompositeKeyEqlExpressionGeneratorTest extends AbstractDaoTestCase {

    private final CompositeKeyEqlExpressionGenerator generator = getInstance(CompositeKeyEqlExpressionGenerator.class);

    @Test
    public void single_required_non_string_key_member() {
        final var exp = expr().concat().expr(concatEmptyWith("date")).end().model();
        assertThat(generator.getKeyExpression(CompositeKeyEqlExpression_Entity1.class)).isEqualTo(exp);
    }

    @Test
    public void single_optional_non_string_key_member() {
        final var exp = expr()
                .caseWhen().condition(cond().prop("date").isNull().model())
                .then().val(null)
                .otherwise().expr(
                        expr().caseWhen().prop("date").isNotNull()
                                .then().concat()
                                .expr(expr().caseWhen().condition(alwaysFalse())
                                              .then().val(" ")
                                              .otherwise().val("")
                                              .end().model())
                                .with().expr(concatEmptyWith("date"))
                                .end()
                                .otherwise().val("")
                                .end().model())
                .end().model();
        assertThat(generator.getKeyExpression(CompositeKeyEqlExpression_Entity2.class)).isEqualTo(exp);
    }

    @Test
    public void single_required_string_key_member() {
        final var exp = expr().concat().expr(concatEmptyWith("name")).end().model();
        assertThat(generator.getKeyExpression(CompositeKeyEqlExpression_Entity3.class)).isEqualTo(exp);
    }

    @Test
    public void single_optional_string_key_member() {
        final var exp = expr()
                .caseWhen().condition(cond().prop("name").isNull().model())
                .then().val(null)
                .otherwise().expr(
                        expr().caseWhen().prop("name").isNotNull()
                                .then().concat()
                                .expr(expr().caseWhen().condition(alwaysFalse())
                                              .then().val(" ")
                                              .otherwise().val("")
                                              .end().model())
                                .with().expr(concatEmptyWith("name"))
                                .end()
                                .otherwise().val("")
                                .end().model())
                .end().model();
        assertThat(generator.getKeyExpression(CompositeKeyEqlExpression_Entity4.class)).isEqualTo(exp);
    }

    @Test
    public void single_required_entity_key_member() {
        final var exp = expr().concat().expr(concatEmptyWith("person.key")).end().model();
        assertThat(generator.getKeyExpression(CompositeKeyEqlExpression_Entity5.class)).isEqualTo(exp);
    }

    @Test
    public void single_optional_entity_key_member() {
        final var exp = expr()
                .caseWhen().condition(cond().prop("person").isNull().model())
                .then().val(null)
                .otherwise().expr(expr().caseWhen().prop("person").isNotNull()
                                          .then().concat()
                                          .expr(expr().caseWhen().condition(alwaysFalse())
                                                        .then().val(" ")
                                                        .otherwise().val("")
                                                        .end().model())
                                          .with().expr(concatEmptyWith("person.key"))
                                          .end()
                                          .otherwise().val("")
                                          .end().model())
                .end().model();
        assertThat(generator.getKeyExpression(CompositeKeyEqlExpression_Entity6.class)).isEqualTo(exp);
    }

    @Test
    public void two_optional_key_members() {
        final var exp = expr()
                .caseWhen().condition(cond().condition(cond().prop("date1").isNull().model())
                                              .and().condition(cond().prop("date2").isNull().model()).model())
                .then().val(null)
                .otherwise().expr(
                        expr().concat()
                              .expr(expr().caseWhen().prop("date1").isNotNull()
                                              .then().concat()
                                              .expr(expr().caseWhen().condition(alwaysFalse())
                                                            .then().val(" ")
                                                            .otherwise().val("")
                                                            .end().model())
                                              .with().expr(concatEmptyWith("date1"))
                                              .end()
                                              .otherwise().val("")
                                              .end().model())
                              .with().expr(
                                      expr().caseWhen().prop("date2").isNotNull()
                                              .then().concat()
                                              .expr(expr().caseWhen().condition(cond().condition(alwaysFalse()).or().prop("date1").isNotNull().model())
                                                            .then().val(" ")
                                                            .otherwise().val("")
                                                            .end().model())
                                              .with().expr(concatEmptyWith("date2"))
                                              .end()
                                              .otherwise().val("")
                                              .end().model())
                              .end().model())
                .end().model();
        assertThat(generator.getKeyExpression(CompositeKeyEqlExpression_Entity7.class)).isEqualTo(exp);
    }

    @Test
    public void two_required_key_members() {
        final var exp = expr().concat().expr(concatEmptyWith("date")).with().val("|").with().expr(concatEmptyWith("name")).end().model();
        assertThat(generator.getKeyExpression(CompositeKeyEqlExpression_Entity8.class)).isEqualTo(exp);
    }

    @Test
    public void one_required_and_one_optional_key_member() {
        final var exp = expr().concat()
                .expr(expr().caseWhen().prop("name").isNotNull()
                            .then().concat()
                                   .expr(expr().caseWhen().condition(alwaysFalse())
                                                 .then().val(" ")
                                                 .otherwise().val("")
                                                 .end().model())
                                   .with()
                                   .expr(concatEmptyWith("name"))
                                   .end()
                            .otherwise().val("")
                            .end().model())
                .with().expr(
                        expr().caseWhen().prop("date").isNotNull()
                              .then().concat()
                                     .expr(expr().caseWhen().condition(cond().condition(alwaysFalse()).or().prop("name").isNotNull().model())
                                                 .then().val(" ")
                                                 .otherwise().val("")
                                                 .end().model())
                                     .with().expr(concatEmptyWith("date"))
                                     .end()
                              .otherwise().val("")
                              .end().model())
                .end().model();
        assertThat(generator.getKeyExpression(CompositeKeyEqlExpression_Entity9.class)).isEqualTo(exp);
    }

    @Test
    public void one_optional_and_one_required_key_member() {
        final var exp = expr().concat()
                .expr(expr().caseWhen().prop("date").isNotNull()
                              .then().concat()
                              .expr(expr().caseWhen().condition(alwaysFalse())
                                            .then().val(" ")
                                            .otherwise().val("")
                                            .end().model())
                              .with()
                              .expr(concatEmptyWith("date"))
                              .end()
                              .otherwise().val("")
                              .end().model())
                .with().expr(
                        expr().caseWhen().prop("name").isNotNull()
                                .then().concat()
                                       .expr(expr().caseWhen().condition(cond().condition(alwaysFalse()).or().prop("date").isNotNull().model())
                                                   .then().val(" ")
                                                   .otherwise().val("")
                                                   .end().model())
                                       .with().expr(concatEmptyWith("name"))
                                       .end()
                                .otherwise().val("")
                                .end().model())
                .end().model();
        assertThat(generator.getKeyExpression(CompositeKeyEqlExpression_Entity10.class)).isEqualTo(exp);
    }

    @Test
    public void required_optional_optional_and_required_key_members() {
        final var exp = expr().concat()
                .expr(expr().concat().expr(
                                expr().concat()
                                        .expr(expr().caseWhen().prop("name").isNotNull()
                                                      .then().concat()
                                                      .expr(expr().caseWhen().condition(alwaysFalse())
                                                                    .then().val(" ")
                                                                    .otherwise().val("")
                                                                    .end().model())
                                                      .with().expr(concatEmptyWith("name"))
                                                      .end()
                                                      .otherwise().val("")
                                                      .end().model())
                                        .with().expr(
                                                expr().caseWhen().prop("date").isNotNull()
                                                        .then().concat()
                                                        .expr(expr().caseWhen().condition(cond().condition(alwaysFalse()).or().prop("name").isNotNull().model())
                                                                      .then().val(" ")
                                                                      .otherwise().val("")
                                                                      .end().model())
                                                        .with().expr(concatEmptyWith("date"))
                                                        .end()
                                                        .otherwise().val("")
                                                        .end().model())
                                        .end().model())
                              .with()
                              .expr(expr().caseWhen().prop("count").isNotNull()
                                            .then().concat()
                                            .expr(expr().caseWhen().condition(cond().condition(cond().condition(alwaysFalse()).or().prop("name").isNotNull().model()).or().prop("date").isNotNull().model())
                                                          .then().val(" ")
                                                          .otherwise().val("")
                                                          .end().model())
                                            .with().expr(concatEmptyWith("count"))
                                            .end()
                                            .otherwise().val("")
                                            .end().model())
                              .end().model())
                .with().expr(
                        expr().caseWhen().prop("description").isNotNull()
                                .then().concat()
                                .expr(expr().caseWhen().condition(cond().condition(cond().condition(cond().condition(alwaysFalse()).or().prop("name").isNotNull().model()).or().prop("date").isNotNull().model()).or().prop("count").isNotNull().model())
                                              .then().val(" ")
                                              .otherwise().val("")
                                              .end().model())
                                .with().expr(concatEmptyWith("description"))
                                .end()
                                .otherwise().val("")
                                .end().model())
                .end().model();
        assertThat(generator.getKeyExpression(CompositeKeyEqlExpression_Entity11.class)).isEqualTo(exp);
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Helpers
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    private static ConditionModel alwaysFalse() {
        return cond().val(1).eq().val(2).model();
    }

    private static ExpressionModel concatEmptyWith(final String prop) {
        return expr().concat().val("").with().prop(prop).end().model();
    }

}
