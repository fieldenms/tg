package ua.com.fielden.platform.serialisation.jackson.deserialisers;

import java.io.IOException;

import org.apache.poi.ss.formula.functions.T;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.error.Warning;
import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.web.utils.PropertyConflict;

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
    public Result deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final JsonNode node = jp.readValueAsTree();

        final Class<T> resultType = (Class<T>) ClassesRetriever.findClass(node.get("@resultType").asText());

        final String message = node.get("message").isNull() ? null : node.get("message").asText();
        final Object instance;
        if (node.get("instance") == null) {
            instance = null;
        } else {
            final JsonNode typeNameObj = node.get("@instanceType");
            final Class<?> instanceType = ClassesRetriever.findClass(typeNameObj.asText());

            instance = mapper.readValue(node.get("instance").traverse(mapper), instanceType);
        }
        final Exception ex;
        if (node.get("ex") == null) {
            ex = null;
        } else {
            ex = mapper.readValue(node.get("ex").traverse(mapper), Exception.class);
        }

        // instantiate the result; warning type checking is required only when instance and message are not null
        if (ex != null) {
            return PropertyConflict.class.equals(resultType) ? new PropertyConflict(instance, ex.getMessage()) : new Result(instance, ex);
        } else {
            return Warning.class.equals(resultType) ? new Warning(instance, message) : new Result(instance, message);
        }
    }
}
