package ua.com.fielden.platform.eql.stage1.sundries;

import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForUnionType;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.operands.AppendIdToUnionTypedProp1;
import ua.com.fielden.platform.eql.stage1.operands.Prop1;
import ua.com.fielden.platform.eql.stage2.sundries.Yield2;

import java.util.Optional;
import java.util.stream.Stream;

import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;

/// This transformation must be applied before [AppendIdToUnionTypedProp1].
/// The order is ensured by this implementation.
///
public final class ExpandUnionTypedPropYield1 {

    public static final ExpandUnionTypedPropYield1 INSTANCE = new ExpandUnionTypedPropYield1();

    private ExpandUnionTypedPropYield1() {}

    /// If a union-typed property is yielded by `yield`, the result is a stream of yields that yield all corresponding union members.
    /// Otherwise, the result is an empty optional.
    ///
    public Optional<Stream<Yield2>> apply(final Yield1 yield, final TransformationContextFromStage1To2 context) {
        if (yield.operand() instanceof Prop1 prop1) {
            final var prop2 = prop1.transformBase(context);
            if (prop2.getPath().getLast() instanceof QuerySourceItemForUnionType<?> item) {
                final var yields = item.getProps().values().stream()
                        .filter(subItem -> isEntityType(subItem.javaType()) && !subItem.hasExpression())
                        // Although we could create Yield2 directly, let's follow a simpler approach -- create Yield1 and transform it.
                        .map(subItem -> new Yield1(new Prop1("%s.%s".formatted(prop1.propPath(), subItem.name), false),
                                                   "%s.%s".formatted(yield.alias(), subItem.name),
                                                   false))
                        .map(y -> y.transform(context));
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
