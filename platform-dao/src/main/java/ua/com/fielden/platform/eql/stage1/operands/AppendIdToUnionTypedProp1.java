package ua.com.fielden.platform.eql.stage1.operands;

import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForUnionType;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;

import java.util.Optional;

import static ua.com.fielden.platform.entity.AbstractEntity.ID;

public final class AppendIdToUnionTypedProp1 {

    public static final AppendIdToUnionTypedProp1 INSTANCE = new AppendIdToUnionTypedProp1();

    private AppendIdToUnionTypedProp1() {}

    /// If a property path ends with a union-typed property, appends `id` to the path.
    /// Otherwise, returns an empty optional.
    ///
    /// @param prop1  stage 1 representation of the property to transform
    /// @param prop2  stage 2 representation of the property to transform
    ///
    public Optional<Prop2> apply(final Prop1 prop1, final Prop2 prop2, final TransformationContextFromStage1To2 context) {
        if (prop2.getPath().getLast() instanceof QuerySourceItemForUnionType<?>) {
            final var result = new Prop1("%s.%s".formatted(prop1.propPath(), ID), prop1.external())
                    .transform(context);
            return Optional.of(result);
        }
        else {
            return Optional.empty();
        }
    }


}
