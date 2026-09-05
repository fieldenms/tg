package ua.com.fielden.platform.eql.stage2;

import com.google.inject.ImplementedBy;
import ua.com.fielden.platform.entity.query.fluent.enums.JoinType;
import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;
import ua.com.fielden.platform.eql.stage2.operands.Expression2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.types.tuples.T2;

import java.util.List;
import java.util.Map;
import java.util.Set;

/// Performs resolution of properties used in a query.
/// Applies to the stage 2 EQL AST: [Prop2], [ISource2], [Expression2], etc.
///
/// Input is a set of [Prop2] instances.
/// Output is a data structure that contains resolutions for all properties from the input set and all properties that are
/// referenced by the expressions of calculated properties in the input set.
///
/// Calculated properties are analysed by _fully_ expanding them so that all reachable properties are analysed.
/// Therefore, a later transformation of the AST must use [Result#resolutions] when expanding calculated properties rather
/// than compiling expressions into fresh [Expression2] instances itself.
/// It must use [Expression2] instances from [Resolution.Expr#expr], as they are the ones that will have been analysed by this facility.
///
/// Output also includes join nodes ([Result#joins]) which have to be incorporated into the query.
/// These join nodes are the result of processing property paths that traverse the entity graph.
/// E.g., `person.user` produces a join node that connects `Person` via property `person` and resolves `person.user` to `Person.user`.
///
/// Outputs from separate invocations (different instances of [Result]) cannot be meaningfully combined.
/// Therefore, for any top-level query, the input set of properties must include _all_ properties used in that query.
///
@ImplementedBy(PropPathResolver.class)
public interface IPropPathResolver {

    Result resolve(Set<Prop2> props, QueryModelToStage1Transformer gen);

    /// The result type.
    ///
    /// [#resolutions] contains entries for _all_ properties in a query, including those that would appear after expanding
    /// all calculated properties into their corresponding expressions.
    /// This is illustrated by the following example.
    ///
    /// ```
    /// Vehicle.lastReading = expr().model(
    ///     select(Reading.class)
    ///       .where().prop("vehicle").eq().extProp("id")
    ///       .orderBy().prop("date").desc().limit(1)
    ///       .model())
    ///     .model();
    ///
    /// q = select(Vehicle.class).where().prop("lastReading").isNotNull().model()
    ///
    /// resolutions for q:
    ///   (Vehicle, "lastReading") -> <Expr>
    ///   (Reading, "vehicle") -> <Column>
    ///   (Vehicle, "id") -> <Column>
    /// ```
    ///
    /// If the expression for `lastReading` itself referenced other calculated properties, the resolutions map would contain
    /// entries for them as well.
    ///
    /// This property of [#resolutions] is necessary because an entry for a calculated property contains a [Resolution.Expr]
    /// with an expression that is NOT fully expanded, but expanded only once.
    /// Since [#resolutions] is total, it can be used to fully expand the expressions.
    ///
    /// @param joins  source ID -> joins whose left side is that source.
    ///     The keys contain both explicit sources and implicit ones that were created by [IPropPathResolver].
    ///
    /// @param resolutions  (source ID, property path) -> property resolution.
    ///     The keys correspond to [Prop2] instances in the query.
    ///
    record Result (Map<Integer, List<JoinNode>> joins,
                   Map<T2<Integer, String>, Resolution> resolutions) {}

    /// Resolution of a property path.
    ///
    /// * A property path ending in a persistent property resolves to [Column].
    ///
    ///   E.g., `person.user` resolves to `Column(id(Person), "user")`.
    ///
    /// * A property path ending in a calculated property resolves to [Expr].
    ///
    ///   [Expr#expr] represents the calculated property's expression.
    ///   It is not fully expanded (i.e., any references to calculated properties are not expanded, but remain as [Prop2]).
    ///   It is up to consumers to expand them.
    ///
    sealed interface Resolution {
        record Column (Integer sourceId, String prop) implements Resolution {}
        record Expr (Expression2 expr) implements Resolution {}
    }

    record JoinNode (ISource2<?> left, ISource2<?> right, Resolution leftOn, JoinType joinType) {}

}
