package ua.com.fielden.platform.utils;

import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.types.tuples.T2;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

/**
 * A utility that assists with implementation of the {@link Object#toString()} method.
 * <p>
 * To use this utility, one should choose a {@linkplain Format format}, either a pre-defined one or create one's own via {@link #formatBuilder()}.
 * Then, one of {@link Format#toString(Object)} methods should be used, which serve as entries to the fluent API provided by {@link ToString}.
 * Finally, {@link ToString#$()} or {@link ToString#toString()} should be used to get the result.
 * <p>
 * {@link ToString} instances contain a mutable container which accumulates the string being built.
 * Thus, instances are mutable and should not be shared.
 *
 * <h3> Usage pattern for superclasses </h3>
 * When there is a superclass that declares fields that should be included in the string representation,
 * the following pattern is recommended:
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
        // always call `super` to take hierarchy into account
        return super.addToString(toString).add("y", y);
    }
}
 * }
 *
 * Another version, with {@link IFormattable}:
 * {@snippet :
class Super implements IFormattable {
    private final Object x;

    public String toString(final IFormat format) {
        return format.toString(this).add("x", x).pipe(this::addToString).$();
    }

    public String toString() {
        return toString(ToString.separateLines);
    }

    // override to include additional fields
    protected ToString addToString(final ToString toString) {
        return toString;
    }
}
 *}
 *
 * @see Format
 */
public final class ToString {

    /**
     * Standard format with all fields on a single line.
     */
    public static final IFormat standard = formatBuilder()
            .beforeFields("(")
            .afterFields(")")
            .fieldDelimiter(" ")
            .fieldFormatter((name, value) -> '[' + name + ": " + value + ']')
            .build();

    /**
     * @see SeparateLinesFormat
     */
    public static final SeparateLinesFormat separateLines = new SeparateLinesFormat();

    private final IFormat format;
    private final StringJoiner stringJoiner;

    ToString(final IFormat format, final String prefix) {
        this.format = format;
        this.stringJoiner = new StringJoiner(format.fieldDelimiter(), prefix + format.beforeFields(), format.afterFields());
    }

    public ToString add(final String name, final Object value) {
        if (applyValueFilter(value)) {
            stringJoiner.add(format.fieldFormatter().apply(name, value));
        }
        return this;
    }

    public <X> ToString addIf(final String name, final X value, final Predicate<? super X> test) {
        if (applyValueFilter(value) && test.test(value)) {
            stringJoiner.add(format.fieldFormatter().apply(name, value));
        }
        return this;
    }

    public <X> ToString addIfNot(final String name, final X value, final Predicate<? super X> test) {
        if (applyValueFilter(value) && !test.test(value)) {
            stringJoiner.add(format.fieldFormatter().apply(name, value));
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
        return $();
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
        return format.valueFilter() == null || format.valueFilter().test(value);
    }

    /**
     * This interface should be implemented by any class whose instances can be formatted from another class,
     * which uses such an {@link IFormat} that recommends its users to implement {@link IFormattable}.
     * <p>
     * For example, if class {@code B} uses {@link IFormat} for its string representation and has a field of type {@code A},
     * then, depending on the {@link IFormat} used, if {@code A} implements {@link IFormattable},
     * it will know that it is being formatted in a context (in the context of {@code B} in this example),
     * and thus can contribute to a prettier format.
     * <p>
     * One use case of this facility is multi-line formats that use increasing levels of indentation.
     */
    public interface IFormattable {
        String toString(IFormat format);
    }

    public interface IFormat {

        default ToString toString(final Object object) {
            requireNonNull(object, "object");
            return new ToString(this, object.getClass().getSimpleName());
        }

        default ToString toString(final Class<?> type) {
            requireNonNull(type, "type");
            return new ToString(this, type.getSimpleName());
        }

        default ToString toString(final CharSequence prefix) {
            requireNonNull(prefix, "prefix");
            return new ToString(this, prefix.toString());
        }

        /**
         * Sequence of characters that is prepended before formatting fields.
         * Typically, this is an opening delimiter.
         */
        CharSequence beforeFields();

        /**
         * Sequence of characters that is appended after formatting fields.
         * Typically, this is a closing delimiter.
         */
        CharSequence afterFields();

        /**
         * Sequence of characters that is inserted between formatted fields.
         * It is used only if there are 2 or more fields.
         */
        CharSequence fieldDelimiter();

        /**
         * A predicate that determines whether a field will be formatted.
         * It is applied to a field's value, which may be null.
         */
        Predicate<Object> valueFilter();

        /**
         * A function that formats a field.
         * It is applied to a field's name and its value, which may be null.
         */
        BiFunction<String, Object, String> fieldFormatter();

    }

    /**
     * The most general format implementation that can be used to create arbitrary simple formats.
     */
    public record Format (CharSequence beforeFields,
                          CharSequence afterFields,
                          CharSequence fieldDelimiter,
                          @Nullable Predicate<Object> valueFilter,
                          BiFunction<String, Object, String> fieldFormatter)
            implements IFormat
    {
        public Format {
            requireNonNull(beforeFields, "beforeFields");
            requireNonNull(afterFields, "afterFields");
            requireNonNull(fieldDelimiter, "fieldDelimiter");
            requireNonNull(fieldFormatter, "fieldFormatter");
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

            private Builder() {}

            private Builder(final CharSequence beforeFields,
                            final CharSequence afterFields,
                            final CharSequence fieldDelimiter,
                            final Predicate<Object> valueFilter,
                            final BiFunction<String, Object, String> fieldFormatter) {
                this.beforeFields = beforeFields;
                this.afterFields = afterFields;
                this.fieldDelimiter = fieldDelimiter;
                this.valueFilter = valueFilter;
                this.fieldFormatter = fieldFormatter;
            }

            public IFormat build() {
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

    /**
     * A format that puts each field on a separate line.
     * <p>
     * Additional capabilities:
     * <ul>
     *   <li> Special support for {@link Map} values: entries are formatted as if they were fields in a class.
     * </ul>
     * <p>
     * It is highly recommended to implement {@link IFormattable}, which will enable control over levels of indentation.
     */
    public static final class SeparateLinesFormat implements IFormat {

        private final CharSequence beforeFields;
        private final CharSequence afterFields;
        private final CharSequence fieldDelimiter;
        private final @Nullable Predicate<Object> valueFilter;
        /** Track the current depth to determine the indentation correctly. Starts from 0. */
        private final int depth;

        public SeparateLinesFormat(final @Nullable Predicate<Object> valueFilter) {
            this(0, valueFilter);
        }

        public SeparateLinesFormat() {
            this(0, null);
        }

        private SeparateLinesFormat(final int depth, final @Nullable Predicate<Object> valueFilter) {
            if (depth < 0) {
                throw new InvalidArgumentException("Depth must be a non-negative integer, but was: %s".formatted(depth));
            }
            final String indentString = indent(depth);
            this.beforeFields = " {\n" + indentString;
            this.afterFields = '\n' + indent(depth - 1) + '}';
            this.fieldDelimiter = "\n" + indentString;
            this.valueFilter = valueFilter;
            this.depth = depth;
        }

        private static String indent(final int depth) {
            return "  ".repeat(depth + 1);
        }

        /**
         * Returns a new instance with the specified depth.
         */
        public SeparateLinesFormat setDepth(final int newDepth) {
            return new SeparateLinesFormat(newDepth, valueFilter);
        }

        @Override
        public ToString toString(final Object object) {
            return IFormat.super.toString(object);
        }

        @Override
        public ToString toString(final Class<?> type) {
            return IFormat.super.toString(type);
        }

        @Override
        public ToString toString(final CharSequence prefix) {
            return IFormat.super.toString(prefix);
        }

        @Override
        public CharSequence beforeFields() {
            return beforeFields;
        }

        @Override
        public CharSequence afterFields() {
            return afterFields;
        }

        @Override
        public CharSequence fieldDelimiter() {
            return fieldDelimiter;
        }

        @Override
        public Predicate<Object> valueFilter() {
            return valueFilter;
        }

        @Override
        public BiFunction<String, Object, String> fieldFormatter() {
            return (name, value) -> name + ": " + formatValue(value);
        }

        private String formatValue(final Object value) {
            // IMPORTANT: update `isFormattable` by testing against all types used in the switch
            return switch (value) {
                case IFormattable it -> it.toString(setDepth(depth + 1));
                case Map<?, ?> it -> setDepth(depth + 1).formatMap(it);
                case Collection<?> it -> setDepth(depth + 1).formatCollection(it);
                case T2<?, ?> it -> setDepth(depth + 1).formatPair(it);
                case null, default -> Objects.toString(value);
            };
        }

        private static boolean isFormattable(final Object object) {
            // IMPORTANT: keep this method in sync with `formatValue` by testing against all types used in the switch
            return object instanceof IFormattable
                    || object instanceof Map<?, ?>
                    || object instanceof Collection<?>
                    || object instanceof T2<?, ?>;
        }

        private String formatMap(final Map<?, ?> map) {
            if (map.isEmpty()) {
                return "{}";
            } else {
                // use the same name for all maps to keep it simple
                final var toString = this.toString("Map");
                map.forEach((key, value) -> toString.add(key instanceof CharSequence csq ? quote(csq.toString()) : Objects.toString(key),
                                                         formatValue(value)));
                return toString.$();
            }
        }

        private static String quote(final String string) {
            // enclose in double-quotes and escape double-quotes inside
            return '"' + string.replace("\"", "\\\"") + '"';
        }

        private String formatCollection(final Collection<?> collection) {
            if (collection.isEmpty()) {
                return "[]";
            }
            else {
                return collection.stream()
                        .map(this::formatValue)
                        .collect(joining(",\n" + indent(depth),
                                         "[\n" + indent(depth),
                                         '\n' + indent(depth - 1) + ']'));
            }
        }

        private String formatPair(final T2<?, ?> pair) {
            if (isFormattable(pair._1) || isFormattable(pair._2)) {
                return "("
                       + formatValue(pair._1)
                       + ",\n"
                       + indent(depth)
                       + formatValue(pair._2)
                       + ')';
            } else {
                return '(' + formatValue(pair._1) + ", " + formatValue(pair._2) + ')';
            }
        }

    }

}
