package ua.com.fielden.platform.serialisation.jackson.deserialisers;

import java.io.IOException;

import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.error.Warning;
import ua.com.fielden.platform.reflection.ClassesRetriever;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class ResultJsonDeserialiser<T extends Result> extends StdDeserializer<T> {
    private final ObjectMapper mapper;

    public ResultJsonDeserialiser(final ObjectMapper mapper) {
        super(Result.class);
        this.mapper = mapper;
    }

    @Override
    public T deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final JsonNode node = jp.readValueAsTree();

        final Class<T> resultType = (Class<T>) ClassesRetriever.findClass(node.get("@resultType").asText());

        final String message = node.get("message").isNull() ? null : node.get("message").asText();
        final Object instance;
        if (node.get("instance") == null) {
            instance = null;
        } else {
            final Class<?> instanceType = ClassesRetriever.findClass(node.get("@instanceType").asText());

            instance = mapper.readValue(node.get("instance").traverse(mapper), instanceType);
        }
        final Exception ex;
        if (node.get("ex") == null) {
            ex = null;
        } else {
            ex = mapper.readValue(node.get("ex").traverse(mapper), Exception.class);
        }

        // instantiate the result; warning type checking is required only when instance and message are not null
        if (ex != null && message == null) {
            return (T) new Result(instance, ex);
        } else if (ex != null && message != null) {
            return (T) new Result(instance, message, ex);
        } else {
            return Warning.class.equals(resultType) ? (T) new Warning(instance, message) : (T) new Result(instance, message);
        }
    }
}
