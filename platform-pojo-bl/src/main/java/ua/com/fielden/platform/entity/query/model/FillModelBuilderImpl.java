package ua.com.fielden.platform.entity.query.model;

import com.google.common.collect.ImmutableMap;

import static ua.com.fielden.platform.entity.query.model.FillModels.emptyFillModel;

final class FillModelBuilderImpl implements IFillModel.Builder {

    private final ImmutableMap.Builder<String, Object> valuesBuilder = ImmutableMap.builder();

    @Override
    public IFillModel.Builder set(final CharSequence property, final Object value) {
        if (value == null) {
            throw new FillModelException("Property cannot be filled with null.");
        }
        valuesBuilder.put(property.toString(), value);
        return this;
    }

    @Override
    public IFillModel build() {
        final var values = valuesBuilder.buildOrThrow();
        return values.isEmpty() ? emptyFillModel() : new FillModelImpl(values);
    }

    public IFillModel buildKeepingLast() {
        final var values = valuesBuilder.buildKeepingLast();
        return values.isEmpty() ? emptyFillModel() : new FillModelImpl(values);
    }

}
