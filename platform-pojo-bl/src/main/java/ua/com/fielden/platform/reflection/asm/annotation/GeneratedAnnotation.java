package ua.com.fielden.platform.reflection.asm.annotation;

import ua.com.fielden.platform.entity.annotation.Generated;

/**
 * A factory for convenient instantiation of {@link Generated} annotations.
 *
 * @author TG Team
 */
public class GeneratedAnnotation {

    public static Generated newInstance() {
        return new Generated() {
            @Override
            public Class<Generated> annotationType() {
                return Generated.class;
            }

            @Override
            public boolean equals(final Object other) {
                if (this == other) {
                    return true;
                }
                return other instanceof Generated;
            }
        };
    }

}
