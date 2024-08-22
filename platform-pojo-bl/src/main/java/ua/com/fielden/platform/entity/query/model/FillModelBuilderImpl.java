package ua.com.fielden.platform.entity.query.model;

import com.google.common.collect.ImmutableMap;

import static ua.com.fielden.platform.entity.query.model.FillModels.emptyFillModel;

final class FillModelBuilderImpl implements FillModel.Builder {

    private final ImmutableMap.Builder<String, Object> valuesBuilder = ImmutableMap.builder();

    @Override
    public FillModel.Builder set(final CharSequence property, final Object value) {
        if (value == null) {
            throw new FillModelException("Property cannot be filled with null.");
        }
        valuesBuilder.put(property.toString(), value);
        return this;
    }

    @Override
    public FillModel.Builder include(final FillModel fillModel) {
        valuesBuilder.putAll(fillModel.asMap());
        return this;
    }

    @Override
    public FillModel build() {
        final var values = valuesBuilder.buildOrThrow();
        return values.isEmpty() ? emptyFillModel() : new FillModelImpl(values);
    }

    public FillModel buildKeepingLast() {
        final var values = valuesBuilder.buildKeepingLast();
        return values.isEmpty() ? emptyFillModel() : new FillModelImpl(values);
    }

}
