package ua.com.fielden.platform.entity.annotation;

import static ua.com.fielden.platform.entity.annotation.CritOnly.Mnemonics.WITH;
import static ua.com.fielden.platform.entity.annotation.CritOnly.Mnemonics.WITHOUT;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that an entity property should only be used as a criterion for dynamic entity reviews (i.e. it cannot be added to the result set). {@link #value()} indicates range or
 * single selection to be used for property. See {@link Type} for more details.
 *
 * @author TG Team
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface CritOnly {

    /** Defines how associated property should be represented. */
    Type value() default Type.RANGE;

    /** Only applicable to criteria only properties of BigDecimal type. */
    long precision() default -1;

    /** Only applicable to criteria only properties of BigDecimal type. */
    long scale() default -1;

    /** Attribute to specify applicability of mnemonics for a {@code CritOnly} property. */
    Mnemonics mnemonics() default Mnemonics.DEFAULT;

    /**
     * Mnemonic options for overriding default value deduced from critonly {@link Type}.
     */
    public enum Mnemonics {
        /** The value for mnemonics should be deduced from the value of {@code CritOnly.Type}. */
        DEFAULT,
        /** Critonly property should be with mnemonics. */
        WITH,
        /** Critonly property should be without mnemonics. */
        WITHOUT
    }

    /**
     * Enumeration for specifying the type of a crit-only selection criterion.
     */
    public enum Type {
        /** Indicates that property should be selected by left and right boundary (if it is range property) and by single boundary (if it is single property). */
        RANGE(WITH),
        /** Indicates that property should be selected only by single boundary (even if it is range property). */
        SINGLE(WITHOUT),
        /** Specifies whether associated crit-only property should represent a multi valued selection criterion. */
        MULTI(WITH);

        public final Mnemonics defaultMnemonics;

        private Type (final Mnemonics mnemonics) {
            this.defaultMnemonics = mnemonics;
        }
    }
}
