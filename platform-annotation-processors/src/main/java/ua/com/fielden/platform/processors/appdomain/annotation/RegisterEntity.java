package ua.com.fielden.platform.processors.appdomain.annotation;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.processors.appdomain.ApplicationDomainProcessor;
import ua.com.fielden.platform.processors.metamodel.utils.ElementFinder;

import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Objects;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * An annotation that should be used in conjunction with {@link ExtendApplicationDomain} to register additional domain entity types.
 *
 * @see ApplicationDomainProcessor
 *
 * @author TG Team
 */
@Retention(SOURCE)
@Target(TYPE)
public @interface RegisterEntity {

    Class<? extends AbstractEntity<?>> value();

    /**
     * A helper class that represents instances of {@link RegisterEntity} on the level of {@link TypeMirror}.
     */
    static class Mirror {
        private final TypeMirror value;

        private Mirror(final TypeMirror value) {
            this.value = value;
        }

        public static Mirror from(final RegisterEntity annot, final ElementFinder finder) {
            final TypeMirror entityType = finder.getAnnotationElementValueOfClassType(annot, RegisterEntity::value);
            return new Mirror(entityType);
        }

        public TypeMirror value() {
            return value;
        }
    }

    static class Builder {

        private Class<? extends AbstractEntity<?>> value;

        private Builder(final Class<? extends AbstractEntity<?>> value) {
            this.value = value;
        }

        public static Builder builder(final Class<? extends AbstractEntity<?>> value) {
            return new Builder(value);
        }

        public Builder setValue(final Class<? extends AbstractEntity<?>> value) {
            this.value = value;
            return this;
        }

        public RegisterEntity build() {
            return new RegisterEntity() {
                @Override public Class<RegisterEntity> annotationType() { return RegisterEntity.class; }

                @Override
                public Class<? extends AbstractEntity<?>> value() { return value; }

                @Override
                public boolean equals(final Object other) {
                    return this == other || (other instanceof final RegisterEntity atOther) &&
                            Objects.equals(this.value(), atOther.value());
                }
            };
        }

    }

}
