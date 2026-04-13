package ua.com.fielden.platform.eql.stage1.operands;

import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForUnionType;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;

import java.util.Optional;

import static ua.com.fielden.platform.entity.AbstractEntity.ID;

/// A transformation on [Prop1].
///
/// If a property path ends with a union-typed property, appends `id` to the path.
///
public final class AppendIdToUnionTypedProp1 {

    public static final AppendIdToUnionTypedProp1 INSTANCE = new AppendIdToUnionTypedProp1();

    private AppendIdToUnionTypedProp1() {}

    /// If the transformation is applicable, returns its result.
    /// Otherwise, returns an empty optional.
    ///
    public Optional<Prop1> apply(final Prop1 prop1, final TransformationContextFromStage1To2 context) {
        final var resolution = Prop1.resolveProp(prop1, context);
        if (resolution.getPath().getLast() instanceof QuerySourceItemForUnionType<?>) {
            return Optional.of(new Prop1("%s.%s".formatted(prop1.propPath(), ID), prop1.external()));
        }
        else {
            return Optional.empty();
        }
    }

}
