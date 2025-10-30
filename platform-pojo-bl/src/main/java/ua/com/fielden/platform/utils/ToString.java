package ua.com.fielden.platform.utils;

import jakarta.annotation.Nullable;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.types.tuples.T2;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.reflection.Reflector.isBoxedType;

/**
 * A utility that assists with implementation of the {@link Object#toString()} method.
 * <p>
 * If {@link ToString} is used to implement method {@link Object#toString()} of some type T, then T should implement {@link IFormattable}.
 * This invariant should be honoured to guarantee soundness.
 * <p>
 * To use this utility, one should choose a {@linkplain IFormat format}, either a pre-defined one or implement one's own.
 * Then, one of {@link IFormat#toString(Object)} methods should be used, which serve as entries to the fluent API provided by {@link ToString}.
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
 * @see IFormat
 * @see IFormattable
 */
public final class ToString {

    /**
     * Standard format with all fields on a single line.
     */
    public static final IFormat standard = new StandardFormat();

    /**
     * @see SeparateLinesWithLabelsFormat
     */
    public static SeparateLinesWithLabelsFormat separateLines() {
        return new SeparateLinesWithLabelsFormat();
    }

    private static final Logger LOGGER = getLogger();

    /**
     * The maximum length of a string that can be built.
     */
    private static final int MAX_SAFE_LENGTH = 1024 * 1024 * 100;

    private static final String WARN_MAX_LENGTH_REACHED =
            """
            Maximum length %s of the underlying buffer reached. The resulting string will be truncated. \
            This is a safety measure to prevent out-of-memory errors."""
            .formatted(MAX_SAFE_LENGTH);

    private final IFormat format;
    private final StringJoiner stringJoiner;

    ToString(final IFormat format, final String prefix) {
        this.format = format;
        this.stringJoiner = new StringJoiner(format.fieldDelimiter(), prefix + format.beforeFields(), format.afterFields());
    }

    public ToString add(final String name, final Object value) {
        if (!format.isIgnored(value)) {
            _add(() -> format.formatField(name, value));
        }
        return this;
    }

    /**
     * Appends the string without applying formatting.
     */
    public ToString addLiteral(final String string) {
        _add(() -> string);
        return this;
    }

    public <X> ToString addIf(final String name, final X value, final Predicate<? super X> test) {
        if (!format.isIgnored(value) && test.test(value)) {
            _add(() -> format.formatField(name, value));
        }
        return this;
    }

    public <X> ToString addIfNot(final String name, final X value, final Predicate<? super X> test) {
        if (!format.isIgnored(value) && !test.test(value)) {
            _add(() -> format.formatField(name, value));
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

    public ToString addIfNotEmpty(final String name, final Map<?, ?> value) {
        return addIfNot(name, value, Map::isEmpty);
    }

    public ToString pipe(final Function<? super ToString, ToString> fn) {
        return fn.apply(this);
    }

    /**
     * Builds a string and returns it.
     */
    public String toString() {
        return $();
    }

    /**
     * Builds a string and returns it.
     */
    public String $() {
        return stringJoiner.toString();
    }

    private void _add(final Supplier<String> stringSupplier) {
        // We may be off by the length of the field delimiter, which is unlikely to be significant
        final int remains = MAX_SAFE_LENGTH - stringJoiner.length();
        if (remains <= 0) {
            LOGGER.warn(WARN_MAX_LENGTH_REACHED);
        }
        else {
            final var string = stringSupplier.get();
            if (remains < string.length()) {
                LOGGER.warn(WARN_MAX_LENGTH_REACHED);
            }
            stringJoiner.add(string.substring(0, Integer.min(remains, string.length())));
        }
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
        boolean isIgnored(@Nullable Object value);

        /**
         * Formats a field.
         */
        String formatField(String name, @Nullable Object value);

        /**
         * Formats a value.
         */
        String formatValue(@Nullable Object value);

    }

    private static final class StandardFormat implements IFormat {

        @Override
        public CharSequence beforeFields() {
            return "(";
        }

        @Override
        public CharSequence afterFields() {
            return ")";
        }

        @Override
        public CharSequence fieldDelimiter() {
            return " ";
        }

        @Override
        public boolean isIgnored(@Nullable final Object value) {
            return false;
        }

        @Override
        public String formatField(final String name, @Nullable final Object value) {
            return '[' + name + ": " + value + ']';
        }

        @Override
        public String formatValue(@Nullable final Object value) {
            return Objects.toString(value);
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
    public static class SeparateLinesFormat implements IFormat {

        public static final String ERR_NEGATIVE_DEPTH = "Depth must be a non-negative integer, but was [%s].";

        /**
         * Enables compact representation of map keys.
         * Exists for debugging purposes.
         * Deliberately non-final so that the value can be changed in debug mode.
         */
        private static boolean COMPACT_MAP_KEYS = true;

        private final CharSequence beforeFields;
        private final CharSequence afterFields;
        private final CharSequence fieldDelimiter;
        /** Track the current depth to determine the indentation correctly. Starts from 0. */
        private final int depth;

        public SeparateLinesFormat() {
            this(0);
        }

        protected SeparateLinesFormat(final int depth) {
            if (depth < 0) {
                throw new InvalidArgumentException(ERR_NEGATIVE_DEPTH.formatted(depth));
            }
            final String indentString = indent(depth);
            this.beforeFields = " {\n" + indentString;
            this.afterFields = '\n' + indent(depth - 1) + '}';
            this.fieldDelimiter = '\n' + indentString;
            this.depth = depth;
        }

        private static String indent(final int depth) {
            return "  ".repeat(depth + 1);
        }

        /**
         * Returns a new instance with the specified depth.
         */
        public SeparateLinesFormat setDepth(final int newDepth) {
            return new SeparateLinesFormat(newDepth);
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
        public boolean isIgnored(@Nullable final Object value) {
            return false;
        }

        @Override
        public String formatField(final String name, @Nullable final Object value) {
            return name + ": " + formatValue(value);
        }

        @Override
        public String formatValue(final Object value) {
            // IMPORTANT: update `isFormattable` by testing against all types used in the switch
            return switch (value) {
                case IFormattable it -> it.toString(setDepth(depth + 1));
                case Map<?, ?> it -> setDepth(depth + 1).formatMap(it);
                case Collection<?> it -> setDepth(depth + 1).formatCollection(it);
                case T2<?, ?> it -> setDepth(depth + 1).formatPair(it);
                case String s -> formatString(s);
                case Type type -> formatType(type);
                case null, default -> Objects.toString(value);
            };
        }

        private static boolean isFormattable(final Object object) {
            // IMPORTANT: keep this method in sync with `formatValue` by testing against all types used in the switch
            return object instanceof IFormattable
                    || object instanceof Map<?, ?>
                    || object instanceof Collection<?>
                    || object instanceof T2<?, ?>
                    || object instanceof String
                    || object instanceof Class<?>;
        }

        private String formatMap(final Map<?, ?> map) {
            if (map.isEmpty()) {
                return "{}";
            } else {
                // use the same name for all maps to keep it simple
                final var toString = this.toString("Map");
                map.forEach((key, value) -> toString.add(formatMapKey(key), value));
                return toString.$();
            }
        }

        private String formatMapKey(final Object key) {
            if (key == null) {
                return formatValue(key);
            }
            else if (COMPACT_MAP_KEYS) {
                return switch (key) {
                    case Enum<?> e -> e.name();
                    case String $ -> formatValue(key);
                    case Type $ -> formatValue(key);
                    case Object x when x.getClass().isPrimitive() || isBoxedType(x.getClass()) -> Objects.toString(x);
                    default -> Objects.toIdentityString(key);
                };
            }
            else {
                return formatValue(key);
            }
        }

        private static String formatString(final String s) {
            // Enclose in double-quotes and escape double-quotes inside
            return '"' + s.replace("\"", "\\\"") + '"';
        }

        private static String formatType(final Type type) {
            return type instanceof Class<?> cls ? cls.getCanonicalName() : type.getTypeName();
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
                return '('
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

    /**
     * Extends {@link SeparateLinesFormat} with the ability to use labels for objects that occur more than once in a structure (shared objects)
     * and for circular structures.
     * <p>
     * Labels are used as follows.
     * For each object `O`, the first occurrence of `O` is represented in its entirety.
     * If `O` {@linkplain #requiresLabel(Object) requires a label}, then all subsequent occurrences are represented by a generated label.
     *
     * <h3>Circular structures</h3>
     * Circular structures are supported, but not in the general case.
     * <p>
     * Circular structure in types that implement {@link IFormattable} and a few standard container types are supported.
     * <p>
     * Circular structure in types that neither implement {@link IFormattable} nor use {@link ToString} cannot be supported,
     * as their {@link Object#toString()} is essentially a black box.
     * <p>
     * Circular structure in types that do not implement {@link IFormattable} but use {@link ToString} is supported only
     * in the case of immediate circularity, where an object directly references itself through one of its fields.
     * Transitive circularity for such structures is not supported.
     * This is why the invariant stated in the documentation of {@link ToString} should be honoured.
     */
    public static class SeparateLinesWithLabelsFormat extends SeparateLinesFormat {

        static final int MAX_UNLABELED_VALUE_LENGTH = 300;

        private final IdentityHashMap<Object, String> labels;

        protected SeparateLinesWithLabelsFormat() {
            this.labels = new IdentityHashMap<>();
        }

        protected SeparateLinesWithLabelsFormat(final int depth, final IdentityHashMap<Object, String> labels) {
            super(depth);
            this.labels = labels;
        }

        @Override
        public ToString toString(final Object object) {
            requireNonNull(object, "object");

            // Always label the root object, unless it is inside some other structure being formatted.
            // In that case, there should already be a label for it.
            final var label = labels.get(object);
            if (label == null) {
                final var newLabel = makeLabel(object);
                labels.put(object, newLabel);
                return super.toString(formatFirstOccurrence(newLabel, object.getClass().getSimpleName()));
            }
            else {
                return super.toString(object);
            }
        }

        @Override
        public String formatValue(final Object value) {
            if (value == null) {
                return super.formatValue(value);
            }

            final var label = labels.get(value);
            if (label != null) {
                return label + " (" + value.getClass().getTypeName() + ")";
            }
            else if (requiresLabel(value)) {
                // Creating a label before formatting the value ensures that if the value is circular, the label will be used accordingly.
                final var newLabel = makeLabel(value);
                labels.put(value, newLabel);
                return formatFirstOccurrence(newLabel, super.formatValue(value));
            }
            else {
                final var valueString = super.formatValue(value);
                // Let's also use labels for values represented by large strings, as a defence against unexpected situations.
                if (valueString.length() > MAX_UNLABELED_VALUE_LENGTH) {
                    final var newLabel = makeLabel(value);
                    labels.put(value, newLabel);
                    return formatFirstOccurrence(newLabel, valueString);
                }
                return valueString;
            }
        }

        private String formatFirstOccurrence(final String label, final String valueString) {
            return label + "=" + valueString;
        }

        protected boolean requiresLabel(final Object object) {
            return object instanceof IFormattable
                   || object instanceof Map<?, ?>
                   || object instanceof Collection<?>
                   || object instanceof T2<?, ?>;
        }

        /**
         * This method is overridden so that this format will be used for nested objects as well.
         */
        @Override
        public SeparateLinesWithLabelsFormat setDepth(final int newDepth) {
            return new SeparateLinesWithLabelsFormat(newDepth, labels);
        }

        protected String makeLabel(final Object value) {
            return "#" + System.identityHashCode(value);
        }

    }

}
