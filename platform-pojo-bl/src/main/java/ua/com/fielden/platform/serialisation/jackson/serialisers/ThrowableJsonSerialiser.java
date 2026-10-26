package ua.com.fielden.platform.serialisation.jackson.serialisers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.ThrowableDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import ua.com.fielden.platform.error.Result;

import java.io.IOException;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/// Serialiser for [Throwable], which applies to throwables other than [Result] and its subtypes, as they have serialisers of their own.
///
/// A throwable is written with the standard properties of [Throwable] (`message`, `localizedMessage`, `cause` and `suppressed`),
/// and with [#TYPE_STR], the name of its class.
/// The standard properties are read by the web client and by Jackson's [ThrowableDeserializer], which [ua.com.fielden.platform.error.ResultJsonDeserialiser] uses.
/// [#TYPE_STR] lets the web client identify the type of a throwable, e.g. to handle exceptions of specific application types.
/// Stack traces and properties specific to subtypes of [Throwable] (e.g. SQL statements of JDBC failures) are not written,
/// as they disclose internal details of the application to clients.
/// The server logs failures with their stack traces.
///
/// By using [Scope], each throwable is written at most once per serialised [Result] (or per throwable serialised on its own),
/// cutting cycles between throwables.
/// A throwable that has been written already is written as `null` if it is a cause, and is omitted if it is a suppressed throwable.
/// The number of throwables written is bounded by [#MAX_THROWABLES], so that an unexpectedly long chain cannot produce an oversized response.
///
public class ThrowableJsonSerialiser extends StdSerializer<Throwable> {

    public static final String
            TYPE_STR = "typeStr",
            MESSAGE = "message",
            LOCALIZED_MESSAGE = "localizedMessage",
            CAUSE = "cause",
            SUPPRESSED = "suppressed";

    /// The maximum number of throwables written per serialised [Result] or throwable.
    /// A serialised [Result] counts towards it, as [Result] is a throwable.
    ///
    public static final int MAX_THROWABLES = 100;

    public ThrowableJsonSerialiser() {
        super(Throwable.class);
    }

    @Override
    public void serialize(final Throwable throwable, final JsonGenerator generator, final SerializerProvider provider) throws IOException {
        final var scope = Scope.of(provider);
        if (scope.isOpen()) {
            if (scope.register(throwable)) {
                writeThrowable(throwable, generator, provider, scope);
            } else {
                generator.writeNull();
            }
        } else {
            scope.open(throwable);
            try {
                writeThrowable(throwable, generator, provider, scope);
            } finally {
                scope.close();
            }
        }
    }

    private static void writeThrowable(
            final Throwable throwable,
            final JsonGenerator generator,
            final SerializerProvider provider,
            final Scope scope)
            throws IOException
    {
        generator.writeStartObject();
        generator.writeStringField(TYPE_STR, throwable.getClass().getName());
        generator.writeStringField(MESSAGE, throwable.getMessage());
        generator.writeStringField(LOCALIZED_MESSAGE, throwable.getLocalizedMessage());

        generator.writeFieldName(CAUSE);
        final var cause = throwable.getCause();
        if (cause != null && scope.canWrite(cause)) {
            provider.defaultSerializeValue(cause, generator);
        } else {
            generator.writeNull();
        }

        generator.writeArrayFieldStart(SUPPRESSED);
        for (final var suppressed : throwable.getSuppressed()) {
            // Checked right before writing, as writing a preceding suppressed throwable registers the throwables it refers to.
            if (scope.canWrite(suppressed)) {
                provider.defaultSerializeValue(suppressed, generator);
            }
        }
        generator.writeEndArray();
        generator.writeEndObject();
    }

    /// The throwables written while serialising a [Result] or a throwable, held as a per-call attribute of [SerializerProvider].
    ///
    /// The scope is open while the exception of a [Result], or a throwable serialised on its own, is being written.
    /// It is shared by [ThrowableJsonSerialiser] and [ResultJsonSerialiser], as a [Result] can be a cause or a suppressed throwable of another throwable.
    ///
    /// To serialise a throwable within another while propagating a [Scope] object, call [SerializerProvider#defaultSerializeValue]
    /// rather than [JsonGenerator#writeObject].
    ///
    static final class Scope {
        private static final Object ATTRIBUTE_KEY = Scope.class;

        private Set<Throwable> written;

        static Scope of(final SerializerProvider provider) {
            final Scope existing = (Scope) provider.getAttribute(ATTRIBUTE_KEY);
            if (existing != null) {
                return existing;
            }
            final Scope scope = new Scope();
            provider.setAttribute(ATTRIBUTE_KEY, scope);
            return scope;
        }

        boolean isOpen() {
            return written != null;
        }

        void open(final Throwable root) {
            written = Collections.newSetFromMap(new IdentityHashMap<>());
            written.add(root);
        }

        void close() {
            written = null;
        }

        boolean canWrite(final Throwable throwable) {
            return written.size() < MAX_THROWABLES && !written.contains(throwable);
        }

        /// Registers `throwable` as written, returning `false` if it cannot be written.
        ///
        boolean register(final Throwable throwable) {
            return canWrite(throwable) && written.add(throwable);
        }
    }

}
