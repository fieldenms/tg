package ua.com.fielden.platform.entity.query.model;

import com.google.common.collect.ImmutableMap;

import java.util.function.Consumer;

public final class FillModels {

    private static final FillModelImpl EMPTY_FILL_MODEL = new FillModelImpl(ImmutableMap.of());

    /**
     * Builds a fill model. A builder instance provided to the given function will be <b>mutable</b>.
     */
    public static FillModel fill(final Consumer<FillModel.Builder> fn) {
        final var builder = new FillModelBuilderImpl();
        fn.accept(builder);
        return builder.build();
    }

    public static FillModel emptyFillModel() {
        return EMPTY_FILL_MODEL;
    }

    /**
     * Produces a new fill model by merging two fill models, giving preference to entries from the right one.
     */
    public static FillModel mergeRight(final FillModel left, final FillModel right) {
        if (left.isEmpty()) {
            return right;
        } else if (right.isEmpty()) {
            return left;
        } else {
            return new FillModelBuilderImpl().include(left).include(right).buildKeepingLast();
        }
    }

    private FillModels() {}

}
