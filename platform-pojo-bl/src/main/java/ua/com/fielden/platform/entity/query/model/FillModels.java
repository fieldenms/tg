package ua.com.fielden.platform.entity.query.model;

import com.google.common.collect.ImmutableMap;

import java.util.function.Consumer;

public final class FillModels {

    private static final FillModelImpl EMPTY_FILL_MODEL = new FillModelImpl(ImmutableMap.of());

    /**
     * Builds a fill model. A builder instance provided to the given function will be <b>mutable</b>.
     */
    public static IFillModel fill(final Consumer<IFillModel.Builder> fn) {
        final var builder = new FillModelBuilderImpl();
        fn.accept(builder);
        return builder.build();
    }

    public static IFillModel emptyFillModel() {
        return EMPTY_FILL_MODEL;
    }

    private FillModels() {}

}
