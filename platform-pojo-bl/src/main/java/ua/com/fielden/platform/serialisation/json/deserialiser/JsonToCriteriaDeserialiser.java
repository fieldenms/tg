package ua.com.fielden.platform.serialisation.json.deserialiser;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.activation.UnsupportedDataTypeException;

import ua.com.fielden.platform.serialisation.json.TgObjectMapper;
import ua.com.fielden.platform.serialisation.json.deserialiser.JsonToCriteriaDeserialiser.CritProp;
import ua.com.fielden.platform.types.Money;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class JsonToCriteriaDeserialiser extends JsonDeserializer<CritProp> {

    private final TgObjectMapper mapper;

    public JsonToCriteriaDeserialiser(final TgObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public CritProp deserialize(final JsonParser parser, final DeserializationContext context) throws IOException, JsonProcessingException {
        final JsonNode node = parser.readValueAsTree();
        final boolean isSingle = node.get("isSingle").asBoolean();
        final String type = node.get("type").asText();
        final Object value1 = getValue(type, isSingle, node.get("value1"));
        final Object value2 = getValue(type, isSingle, node.get("value2"));
        return new CritProp(value1, value2);
    }

    private Object getValue(final String type, final boolean isSingle, final JsonNode node) throws JsonProcessingException, IOException {
        if (node.isNull()) {
            return null;
        } else if ("Entity".equals(type)) {
            if (isSingle) {
                throw new UnsupportedDataTypeException("The single type " + type + " is not supported yet");
            } else {
                return node.traverse(mapper).readValueAs(new TypeReference<List<String>>() {
                });
            }
        } else if ("String".equals(type)) {
            return node.asText();
        } else if ("Long".equals(type)) {
            return node.asText().isEmpty() ? null : node.asLong();
        } else if ("Integer".equals(type)) {
            return node.asText().isEmpty() ? null : node.asInt();
        } else if ("BigDecimal".equals(type)) {
            return node.asText().isEmpty() ? null : node.decimalValue();
        } else if ("Money".equals(type)) {
            return node.asText().isEmpty() ? null : new Money(node.asText());
        } else if ("Date".equals(type)) {
            return node.traverse(mapper).readValueAs(Date.class);
        } else if ("Boolean".equals(type)) {
            return node.asBoolean();
        } else {
            throw new UnsupportedDataTypeException("The type " + type + " is not supported yet");
        }
    }

    public static class CritProp {

        private final Object value1;
        private final Object value2;

        public CritProp(final Object value1, final Object value2) {
            this.value1 = value1;
            this.value2 = value2;
        }

        public Object getValue1() {
            return value1;
        }

        public Object getValue2() {
            return value2;
        }
    }
}
