package ua.com.fielden.platform.serialisation.jackson.deserialisers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.serialisation.jackson.EntityTypeInfoGetter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * Deserialiser for list of unknown objects (should determine their type ad-hoc).
 *
 * @author TG Team
 *
 */
public class ArrayListJsonDeserialiser extends StdDeserializer<ArrayList> {
    private final ObjectMapper mapper;
    private final EntityTypeInfoGetter entityTypeInfoGetter;

    public ArrayListJsonDeserialiser(final ObjectMapper mapper, final EntityTypeInfoGetter entityTypeInfoGetter) {
        super(ArrayList.class);
        this.mapper = mapper;
        this.entityTypeInfoGetter = entityTypeInfoGetter;
    }

    @Override
    public ArrayList deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final JsonNode node = jp.readValueAsTree();
        if (!node.isArray()) {
            throw new IllegalStateException("ListJsonDeserialiser has got non-array.");
        }

        final Iterator<JsonNode> elements = node.elements();
        final ArrayList list = new ArrayList();
        while (elements.hasNext()) {
            final JsonNode el = elements.next();
            if (el.isNull()) {
                list.add(null);
            } else if (el.isObject() && (el.get("@id_ref") != null || el.get("@id") != null)) {
                final String typeNumber = el.get("@id_ref") != null ? el.get("@id_ref").asText().substring(0, el.get("@id_ref").asText().indexOf("#")) : el.get("@id").asText().substring(0, el.get("@id").asText().indexOf("#"));
                final Class<?> instanceType = ClassesRetriever.findClass(entityTypeInfoGetter.get(Long.parseLong(typeNumber)).getKey());
                list.add(mapper.readValue(el.traverse(mapper), instanceType));
            } else {
                throw new UnsupportedOperationException("ListJsonDeserialiser does not support node [" + el + "] with type [" + el.getNodeType() + "] at this stage."); // not supported
            }
        }
        return list;
    }
}
