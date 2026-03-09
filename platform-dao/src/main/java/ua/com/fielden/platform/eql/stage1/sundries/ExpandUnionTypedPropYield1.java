package ua.com.fielden.platform.eql.stage1.sundries;

import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForUnionType;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.operands.Prop1;

import java.util.Optional;
import java.util.stream.Stream;

import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;

/// A transformation on individual yields [Yield1].
///
/// If the yielded expression is a union-typed property, then the yield is transformed into a set of yields, one for each union member.
///
public final class ExpandUnionTypedPropYield1 {

    public static final ExpandUnionTypedPropYield1 INSTANCE = new ExpandUnionTypedPropYield1();

    private ExpandUnionTypedPropYield1() {}

    /// If the transformation is applicable, returns its result.
    /// Otherwise, returns an empty optional.
    ///
    public Optional<Stream<Yield1>> apply(final Yield1 yield, final TransformationContextFromStage1To2 context) {
        if (yield.operand() instanceof Prop1 prop1) {
            final var resolution = Prop1.resolveProp(prop1, context);
            if (resolution.getPath().getLast() instanceof QuerySourceItemForUnionType<?> item) {
                final var yields = item.getProps().values().stream()
                        .filter(subItem -> isEntityType(subItem.javaType()) && !subItem.hasExpression())
                        .map(subItem -> new Yield1(new Prop1("%s.%s".formatted(prop1.propPath(), subItem.name), prop1.external()),
                                                   "%s.%s".formatted(yield.alias(), subItem.name),
                                                   false));
                return Optional.of(yields);
            }
            else {
                return Optional.empty();
            }
        }
        else {
            return Optional.empty();
        }
    }


}
