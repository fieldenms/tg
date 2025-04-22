package ua.com.fielden.platform.processors.verify.annotation;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.lang.model.element.Element;

/**
 * Indicates whether the verification of a program element should be skipped.
 *
 * @author TG Team
 */
@Retention(SOURCE)
@Target({FIELD, TYPE, METHOD})
public @interface SkipVerification {

    public static record Factory() {

        public static boolean shouldSkipVerification(final Element element) {
            return element.getAnnotation(SkipVerification.class) != null;
        }

        public static SkipVerification create() {
            return new Factory().newInstance();
        }

        public SkipVerification newInstance() {
            return new SkipVerification() {
                @Override public Class<SkipVerification> annotationType() { return SkipVerification.class; }

                @Override
                public boolean equals(final Object other) {
                    return this == other || other instanceof SkipVerification;
                }
            };
        }

    }

}
