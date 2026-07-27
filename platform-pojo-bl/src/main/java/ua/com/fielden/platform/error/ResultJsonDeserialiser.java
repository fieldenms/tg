package ua.com.fielden.platform.error;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.web.utils.PropertyConflict;

import java.io.IOException;

/**
 * Deserialiser for {@link Result} type.
 * <p>
 * Deserialises into {@link Result} instance of concrete subtype defined in '@resultType'; deserialises message, exception and 'instance' using its type information in '@instanceType'.
 *
 * @author TG Team
 *
 */
public class ResultJsonDeserialiser extends StdDeserializer<Result> {
    private static final long serialVersionUID = 1L;
    private final ObjectMapper mapper;

    public ResultJsonDeserialiser(final ObjectMapper mapper) {
        super(Result.class);
        this.mapper = mapper;
    }

    @Override
    public Result deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
        final JsonNode node = jp.readValueAsTree();

        final Class<? extends Result> resultType = (Class<? extends Result>) ClassesRetriever.findClass(node.get("@resultType").asText());

        final String message = node.get(Result.MESSAGE).isNull() ? null : node.get(Result.MESSAGE).asText();
        final Object instance;
        if (node.get(Result.INSTANCE) == null) {
            instance = null;
        } else {
            final JsonNode typeNameObj = node.get("@instanceType");
            final Class<?> instanceType = ClassesRetriever.findClass(typeNameObj.asText());

            instance = mapper.readValue(node.get(Result.INSTANCE).traverse(mapper), instanceType);
        }
        final Exception ex;
        if (node.get(Result.EX) == null) {
            ex = null;
        } else {
            ex = mapper.readValue(node.get(Result.EX).traverse(mapper), Exception.class);
        }

        // instantiate the result; warning type checking is required only when instance and message are not null
        if (ex != null) {
            // Capturing a stack trace during deserialisation is not meaningful, hence disable it.
            return PropertyConflict.class.equals(resultType) ? new PropertyConflict(instance, ex.getMessage()) : new Result(instance, ex, false);
        } else if (Warning.class.equals(resultType)) {
            return new Warning(instance, message);
        } else if (Informative.class.equals(resultType)){
            return new Informative(instance, message);
        } else {
            return new Result(instance, message, null, false);
        }
    }
}
