package ua.com.fielden.platform.eql.stage2;

import org.assertj.core.groups.Tuple;
import org.junit.Test;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityRetrievalModel;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.eql.meta.EqlTestCase;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage2.IPropPathResolver.JoinNode;
import ua.com.fielden.platform.eql.stage2.IPropPathResolver.Resolution;
import ua.com.fielden.platform.sample.domain.TeVehicle;
import ua.com.fielden.platform.sample.domain.TeVehicleMake;
import ua.com.fielden.platform.sample.domain.TeVehicleModel;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

/// Tests for the ordering guarantee of [IPropPathResolver], on which the correctness of generated SQL depends.
///
/// Implicit joins are one-to-one FK joins and so commute, *except* where an intermediate calculated property is
/// involved: a join whose ON side is that property's expression must follow the joins that expression itself needs.
/// [PropPathResolver] never orders joins explicitly -- the order falls out of the depth-first traversal, carried
/// through by the insertion order of the `LinkedHashMap` behind [PropPathResolver.State].
///
public class PropPathResolverTest extends EqlTestCase {

    /// `TeVehicle` carries a three-link chain of entity-typed calculated properties:
    ///
    /// * `replacedByTwice          = replacedBy.replacedBy`
    /// * `replacedByTwiceModel     = replacedByTwice.model`
    /// * `replacedByTwiceModelMake = replacedByTwiceModel.make`
    ///
    /// Navigating into the last therefore requires all four joins, and each must follow the one it is defined in
    /// terms of.
    /// Note that the sources are *allocated* in the reverse of the order in which they are emitted, so this cannot
    /// pass by accident through any ordering by source ID.
    ///
    @Test
    public void joins_for_a_chain_of_dependent_calculated_properties_are_emitted_in_dependency_order() {
        final var joins = joinsForSingleSourceOf(
                select(TeVehicle.class).where().prop("replacedByTwiceModelMake.key").isNotNull().model(),
                TeVehicle.class);

        assertThat(joins).hasSize(4);

        // `replacedBy` is persistent, so its ON side is a column; the three calculated properties yield expressions.
        assertThat(joins).extracting(join -> join.right().sourceType(), PropPathResolverTest::kindOf)
                .containsExactly(Tuple.tuple(TeVehicle.class, "column"),            // replacedBy
                                 Tuple.tuple(TeVehicle.class, "expression"),        // replacedByTwice
                                 Tuple.tuple(TeVehicleModel.class, "expression"),   // replacedByTwiceModel
                                 Tuple.tuple(TeVehicleMake.class, "expression"));   // replacedByTwiceModelMake
    }

    // ------------------------------------------------------------------------------------------------
    // Helpers

    private static String kindOf(final JoinNode join) {
        return join.leftOn() instanceof Resolution.Column ? "column" : "expression";
    }

    /// Resolves `query` and returns the joins hanging off its single explicit source.
    /// A minimal fetch model keeps the result type's own property surface out of the way.
    ///
    private static <T extends AbstractEntity<?>> List<JoinNode> joinsForSingleSourceOf(
            final QueryModel<T> query,
            final Class<T> resultType)
    {
        final var gen = qb();
        final var context = TransformationContextFromStage1To2.mkContext(querySourceInfoProvider(), metadata());
        final var fetchModel = new EntityRetrievalModel<>(fetchOnly(resultType), metadata(), querySourceInfoProvider());
        final var props = gen.generateAsResultQuery(query, null, fetchModel).transform(context).collectProps();

        final var sourceIds = props.stream().map(prop -> prop.source.id()).distinct().toList();
        assertThat(sourceIds).as("the query is expected to have exactly one explicit source").hasSize(1);

        final var result = new PropPathResolver(querySourceInfoProvider(), metadata()).resolve(props, gen);
        return result.joins().getOrDefault(sourceIds.getFirst(), List.of());
    }

}
