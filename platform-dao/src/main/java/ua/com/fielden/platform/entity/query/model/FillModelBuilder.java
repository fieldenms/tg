package ua.com.fielden.platform.entity.query.model;

import com.google.common.collect.ImmutableMap;
import jakarta.inject.Inject;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.meta.IDomainMetadata;

import static ua.com.fielden.platform.entity.query.model.IFillModel.EMPTY_FILL_MODEL;

/**
 * A builder for instances of {@link IFillModel}.
 */
public class FillModelBuilder {

    public static final String ERR_NON_PLAIN_PROPS = "Could not build a fill model for entity [%s] due to non-plain properties used: %s.";
    public static final String ERR_NULL_VALUES = "Properties cannot be filled with null.";

    private final IDomainMetadata domainMetadata;
    private final ImmutableMap.Builder<String, Object> valuesBuilder = ImmutableMap.builder();

    @Inject
    public FillModelBuilder(final IDomainMetadata domainMetadata) {
        this.domainMetadata = domainMetadata;
    }

    /**
     * Captures a fact that property with name {@code propName} should be filled with {@code value}.
     *
     * @param propName  a property name.
     * @param value  a "fill" value; cannot be {@code null}.
     * @return
     */
    public FillModelBuilder set(final CharSequence propName, final Object value) {
        if (value == null) {
            throw new FillModelException(ERR_NULL_VALUES);
        }
        valuesBuilder.put(propName.toString(), value);
        return this;
    }

    public <T extends AbstractEntity<?>> IFillModel<T> build(final Class<T> entityType) {
        final var values = valuesBuilder.buildOrThrow();
        if (values.isEmpty()) {
            return EMPTY_FILL_MODEL;
        }
        else {
            final var entityMetadata = domainMetadata.forEntity(entityType);
            final var nonPlainProps = values.keySet().stream().filter(pn -> !entityMetadata.property(pn).isPlain()).toList();
            if (!nonPlainProps.isEmpty()) {
                throw new FillModelException(ERR_NON_PLAIN_PROPS.formatted(entityType.getSimpleName(), nonPlainProps.toString()));
            }
            return new FillModelImpl<T>(values);
        }
    }

}
