package ua.com.fielden.platform.entity.annotation.factory;

import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.CritOnly.Type;

public class CritOnlyAnnotation {

    private final long scale;
    private final long precision;
    private final Type value;

    public CritOnlyAnnotation(final Type value, final long scale, final long precision) {
        this.value = value;
        this.scale = scale;
        this.precision = precision;
    }

    public CritOnly newInstance() {
        return new CritOnly() {

            @Override
            public Class<CritOnly> annotationType() {
                return CritOnly.class;
            }

            @Override
            public long precision() {
                return precision;
            }

            @Override
            public long scale() {
                return scale;
            }

            @Override
            public Type value() {
                return value;
            }

        };
    }
}
