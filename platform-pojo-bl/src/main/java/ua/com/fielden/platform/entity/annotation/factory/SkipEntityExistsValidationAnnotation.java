package ua.com.fielden.platform.entity.annotation.factory;

import ua.com.fielden.platform.entity.annotation.SkipEntityExistsValidation;

/**
 * A factory for convenient instantiation of {@link SkipEntityExistsValidation} annotations, which mainly should be used for dynamic class generation.
 *
 * @author TG Team
 *
 */
public class SkipEntityExistsValidationAnnotation {
    private final boolean skipActiveOnly;
    private final boolean skipNew;

    public SkipEntityExistsValidationAnnotation(final boolean skipActiveOnly, final boolean skipNew) {
        this.skipActiveOnly = skipActiveOnly;
        this.skipNew = skipNew;
    }

    public SkipEntityExistsValidation newInstance() {
        return new SkipEntityExistsValidation() {

            @Override
            public Class<SkipEntityExistsValidation> annotationType() {
                return SkipEntityExistsValidation.class;
            }

            @Override
            public boolean skipActiveOnly() {
                return skipActiveOnly;
            }

            @Override
            public boolean skipNew() {
                return skipNew;
            }
        };
    }

}
