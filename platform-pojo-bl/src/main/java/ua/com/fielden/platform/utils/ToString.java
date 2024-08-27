package ua.com.fielden.platform.utils;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

/**
 * A utility that assists with implementation of the {@link Object#toString()} method.
 * <p>
 * To use this utility, one should choose a {@linkplain Format format}, either a pre-defined one or create one's own via {@link #formatBuilder()}.
 * Then, one of {@link Format#toString(Object)} methods should be used, which serve as entries to the fluent API provided
 * by {@link ToString}. Finally, {@link ToString#$()} or {@link ToString#toString()} should be used to obtain the result.
 * <p>
 * {@link ToString} instances contain a mutable container which accumulates the string being built. Thus, instances are
 * mutable and should not be shared.
 *
 * <h3> Usage pattern for superclasses </h3>
 * When there is a superclass that declares fields that should be included in the string representation, the following
 * pattern is recommended:
 * {@snippet :
 class Super {
    private final Object x;

    public String toString() {
        return ToString.standard.toString(this).add("x", x).pipe(this::addToString).$();
    }

    // override to include more fields
    protected ToString addToString(final ToString toString) {
        return toString;
    }
}

class Sub extends Super {
    private final Object y;

    @Override
    protected ToString addToString(final ToString toString) {
        // always call super to take deep hierarchies into account
        return super.addToString(toString).add("y", y);
    }
}
 * }
 *
 * @see Format
 */
public final class ToString {

    /**
     * Standard format with all fields on a single line.
     */
    public static final Format standard = formatBuilder()
            .beforeFields("(")
            .afterFields(")")
            .fieldDelimiter(" ")
            .fieldFormatter((name, value) -> '[' + name + ": " + value + ']')
            .build();

    /**
     * A format that puts each field on a separate line.
     */
    public static final Format separateLines = formatBuilder()
            .beforeFields(" {\n  ")
            .afterFields("\n}")
            .fieldDelimiter("\n  ")
            .fieldFormatter((name, value) -> name + ": " + value)
            .build();

    private final Format format;
    private final StringJoiner stringJoiner;

    private ToString(final Format format, final String prefix) {
        this.format = format;
        this.stringJoiner = new StringJoiner(format.fieldDelimiter, prefix + format.beforeFields, format.afterFields);
    }

    public ToString add(final String name, final Object value) {
        if (applyValueFilter(value)) {
            stringJoiner.add(format.fieldFormatter.apply(name, value));
        }
        return this;
    }

    public <X> ToString addIf(final String name, final X value, final Predicate<? super X> test) {
        if (applyValueFilter(value) && test.test(value)) {
            stringJoiner.add(format.fieldFormatter.apply(name, value));
        }
        return this;
    }

    public <X> ToString addIfNot(final String name, final X value, final Predicate<? super X> test) {
        if (applyValueFilter(value) && !test.test(value)) {
            stringJoiner.add(format.fieldFormatter.apply(name, value));
        }
        return this;
    }

    public ToString addIfNotNull(final String name, final Object value) {
        return addIfNot(name, value, Objects::isNull);
    }

    public ToString addIfPresent(final String name, final Optional<?> maybeValue) {
        return maybeValue.map(v -> add(name, v)).orElse(this);
    }

    public ToString addIfNotEmpty(final String name, final Collection<?> value) {
        return addIfNot(name, value, Collection::isEmpty);
    }

    public ToString pipe(final Function<? super ToString, ToString> fn) {
        return fn.apply(this);
    }

    /**
     * Builds the string and returns it.
     */
    public String toString() {
        return stringJoiner.toString();
    }

    /**
     * Builds the string and returns it.
     */
    public String $() {
        return stringJoiner.toString();
    }

    public static Format.Builder formatBuilder() {
        return new Format.Builder();
    }

    private boolean applyValueFilter(final Object value) {
        return format.valueFilter == null || format.valueFilter.test(value);
    }

    /**
     * @param beforeFields  sequence of characters that is prepended before fields
     * @param afterFields  sequence of characters that is appended after fields
     * @param fieldDelimiter  delimier of field entries
     * @param valueFilter  filter of field values; if a value does not satisfy the predicate, the field is not included
     *                     in the string; {@code null} represents the absence of a filter
     * @param fieldFormatter  formats a field entry
     */
    public record Format(CharSequence beforeFields, CharSequence afterFields, CharSequence fieldDelimiter,
                         @Nullable Predicate<Object> valueFilter,
                         BiFunction<String, Object, String> fieldFormatter)
    {
        public Format {
            requireNonNull(beforeFields, "beforeFields");
            requireNonNull(afterFields, "afterFields");
            requireNonNull(fieldDelimiter, "fieldDelimiter");
            requireNonNull(fieldFormatter, "fieldFormatter");
        }

        public ToString toString(final Object object) {
            requireNonNull(object, "object");
            return new ToString(this, object.getClass().getSimpleName());
        }

        public ToString toString(final Class<?> type) {
            requireNonNull(type, "type");
            return new ToString(this, type.getSimpleName());
        }

        public ToString toString(final CharSequence prefix) {
            requireNonNull(prefix, "prefix");
            return new ToString(this, prefix.toString());
        }

        public Builder toBuilder() {
            return new Builder(beforeFields, afterFields, fieldDelimiter, valueFilter, fieldFormatter);
        }

        public static final class Builder {

            private CharSequence beforeFields;
            private CharSequence afterFields;
            private CharSequence fieldDelimiter;
            private @Nullable Predicate<Object> valueFilter;
            private BiFunction<String, Object, String> fieldFormatter;

            private Builder () {}

            private Builder(final CharSequence beforeFields,
                            final CharSequence afterFields,
                            final CharSequence fieldDelimiter,
                            final Predicate<Object> valueFilter,
                            final BiFunction<String, Object, String> fieldFormatter)
            {
                this.beforeFields = beforeFields;
                this.afterFields = afterFields;
                this.fieldDelimiter = fieldDelimiter;
                this.valueFilter = valueFilter;
                this.fieldFormatter = fieldFormatter;
            }

            public Format build() {
                return new Format(beforeFields, afterFields, fieldDelimiter, valueFilter, fieldFormatter);
            }

            public CharSequence getBeforeFields() {
                return beforeFields;
            }

            public Builder beforeFields(final CharSequence beforeFields) {
                this.beforeFields = beforeFields;
                return this;
            }

            public CharSequence getAfterFields() {
                return afterFields;
            }

            public Builder afterFields(final CharSequence afterFields) {
                this.afterFields = afterFields;
                return this;
            }

            public CharSequence getFieldDelimiter() {
                return fieldDelimiter;
            }

            public Builder fieldDelimiter(final CharSequence fieldDelimiter) {
                this.fieldDelimiter = fieldDelimiter;
                return this;
            }

            @Nullable
            public Predicate<Object> getValueFilter() {
                return valueFilter;
            }

            public Builder valueFilter(@Nullable final Predicate<Object> valueFilter) {
                this.valueFilter = valueFilter;
                return this;
            }

            public BiFunction<String, Object, String> getFieldFormatter() {
                return fieldFormatter;
            }

            public Builder fieldFormatter(final BiFunction<String, Object, String> fieldFormatter) {
                this.fieldFormatter = fieldFormatter;
                return this;
            }
        }
    }

}
