package ua.com.fielden.platform.parser;

import com.google.common.collect.Iterables;
import jakarta.annotation.Nullable;
import ua.com.fielden.platform.parser.exceptions.ValueParsingException;

import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static ua.com.fielden.platform.parser.IValueParser.Result.error;
import static ua.com.fielden.platform.parser.IValueParser.Result.ok;
import static ua.com.fielden.platform.utils.MiscUtilities.checkType;

/**
 * Parses arbitrary values.
 * <p>
 * Parsers can be built using various combinators defined in this interface as static methods.
 *
 * @param <I>  parser input
 * @param <O>  parser output
 */
public interface IValueParser<I, O> extends Function<I, IValueParser.Result<O>> {

    /**
     * If this parser succeeds, feeds its output to the given parser.
     */
    default <U> IValueParser<I, U> and(final IValueParser<O, U> parser) {
        return value -> this.apply(value).flatMap(parser);
    }

    static <I, O> IValueParser<Object, O> typeChecking(final Class<I> type, final IValueParser<I, O> parser) {
        return value -> {
            final var either = checkType(value, type);
            if (either.isRight()) {
                return parser.apply(either.asRight().value());
            }
            return error(either.asLeft().value());
        };
    }

    static IValueParser<Object, Integer> intParser() {
        return value -> {
            final int i;
            try {
                i = Integer.parseInt(requireNonNull(value).toString());
            } catch (final NumberFormatException e) {
                return error(e);
            }
            return ok(i);
        };
    }

    static IValueParser<Object, Long> longParser() {
        return value -> {
            final long l;
            try {
                l = Long.parseLong(requireNonNull(value).toString());
            } catch (final NumberFormatException e) {
                return error(e);
            }
            return ok(l);
        };
    }

    /**
     * Parses an enum constant. The name must strictly match (by {@link String#equals(Object)}).
     */
    static <E extends Enum<E>> IValueParser<Object, E> enumParser(final Class<E> enumType) {
        return value -> {
            requireNonNull(value);
            if (enumType.isInstance(value)) {
                return ok((E) value);
            }
            try {
                return ok(Enum.valueOf(enumType, value.toString()));
            } catch (final Exception e) {
                return error(e);
            }
        };
    }

    /**
     * Parses one of the given enum constants ignoring case.
     */
    static <E extends Enum<E>> IValueParser<Object, E> enumIgnoreCaseParser(final E[] enumValues) {
        return oneOfParser(Arrays.asList(enumValues),
                           (e, value) -> e.name().equalsIgnoreCase(value.toString()));
    }

    /**
     * Parses the first item that satisfies a predicate.
     */
    static <O> IValueParser<Object, O> oneOfParser(final Iterable<O> items, final BiPredicate<? super O, Object> test) {
        return value -> {
            requireNonNull(value);
            return Iterables.tryFind(items, item -> test.test(item, value))
                    .transform(Result::ok)
                    .or(() -> error("Value is not one of %s".formatted(Iterables.toString(items))));
        };
    }

    /**
     * Parses the named property, fails if it is absent.
     */
    static <O> IValueParser<Properties, O> propertyParser(final String property, final IValueParser<Object, O> parser) {
        return properties -> {
            final var value = properties.getProperty(property);
            return value == null
                    ? error("Missing property [%s]".formatted(property))
                    : parser.apply(value).refineError(() -> "Failed to parse property [%s]".formatted(property));
        };
    }

    /**
     * Parses the named property if it's present, otherwise the result is an empty optional.
     */
    static <O> IValueParser<Properties, Optional<O>> optPropertyParser(final String property, final IValueParser<Object, O> parser) {
        return properties -> {
            final var value = properties.getProperty(property);
            return value == null
                    ? ok(Optional.empty())
                    : parser.apply(value).map(Optional::of)
                            .refineError(() -> "Failed to parse property [%s]".formatted(property));
        };
    }

    /**
     * Equivalent to {@link #propertyParser(String, IValueParser)} but returns a default value instead of failing.
     */
    static <O> IValueParser<Properties, O> propertyParser(
            final String property,
            final IValueParser<Object, O> parser,
            final O defaultValue)
    {
        return properties -> {
            final var value = properties.getProperty(property);
            return value == null
                    ? ok(defaultValue)
                    : parser.apply(value).refineError(() -> "Failed to parse property [%s]".formatted(property));
        };
    }

    /**
     * Equivalent to {@link #propertyParser(String, IValueParser)} but returns a default value instead of failing.
     */
    static <O> IValueParser<Properties, O> propertyParser(
            final String property,
            final IValueParser<Object, O> parser,
            final Supplier<O> defaultValue)
    {
        return properties -> {
            final var value = properties.getProperty(property);
            return value == null
                    ? ok(defaultValue.get())
                    : parser.apply(value).refineError(() -> "Failed to parse property [%s]".formatted(property));
        };
    }

    sealed interface Result<T> {

        static <T> Result<T> ok(final T value) {
            return new Result.Ok<>(value);
        }

        static <T> Result<T> error(final String message) {
            return new Result.Error(() -> message, null);
        }

        static <T> Result<T> error(final String message, final Throwable cause) {
            return new Result.Error(() -> message, cause);
        }

        static <T> Result<T> error(final Throwable cause) {
            return new Result.Error(null, cause);
        }

        <U> Result<U> map(final Function<? super T, U> fn);

        <U> Result<U> flatMap(final Function<? super T, Result<U>> fn);

        T getOrThrow();

        Optional<T> asOptional();

        Result<T> refineError(Supplier<String> message);

        record Ok<T>(T value) implements Result<T> {
            @Override
            public <U> Result<U> map(final Function<? super T, U> fn) {
                return ok(fn.apply(value));
            }

            @Override
            public <U> Result<U> flatMap(final Function<? super T, Result<U>> fn) {
                return fn.apply(value);
            }

            @Override
            public T getOrThrow() {
                return value;
            }

            @Override
            public Optional<T> asOptional() {
                requireNonNull(value);
                return Optional.of(value);
            }

            @Override
            public Result<T> refineError(final Supplier<String> message) {
                return this;
            }
        }

        record Error(@Nullable Supplier<String> message, @Nullable Throwable cause) implements Result {
            @Override
            public Result map(final Function fn) {
                return this;
            }

            @Override
            public Result flatMap(final Function fn) {
                return this;
            }

            @Override
            public Object getOrThrow() {
                throw new ValueParsingException(message != null ? message.get() : null, cause);
            }

            @Override
            public Optional asOptional() {
                return Optional.empty();
            }

            @Override
            public Result refineError(final Supplier newMessage) {
                requireNonNull(newMessage);
                return new Error(message == null ? newMessage : () -> newMessage.get() + "; " + message.get(),
                                 cause);
            }
        }

    }

}
