package ua.com.fielden.platform.serialisation.jackson.deserialisers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.serialisation.jackson.deserialisers.ResultDeserialiser.ResultProperty;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ResultDeserialiser extends JsonDeserializer<ResultProperty> {

    private final ObjectMapper mapper;

    public ResultDeserialiser(final ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public ResultProperty deserialize(final JsonParser parser, final DeserializationContext context) throws IOException, JsonProcessingException {
        final JsonNode node = parser.readValueAsTree();
        final List<String> summary = node.get("summary").traverse(mapper).readValueAs(new TypeReference<List<String>>() {
        });
        final Ordering ordering = node.get("ordering").traverse(mapper).readValueAs(Ordering.class);
        return new ResultProperty(summary, ordering);
    }

    public static class ResultProperty {

        private final List<String> summary;
        private final Ordering ordering;

        public ResultProperty(final List<String> summary, final Ordering ordering) {
            this.summary = summary;
            this.ordering = ordering;
        }

        public List<String> getSummaryProps() {
            return new ArrayList<>(summary);
        }

        public Ordering getOrdering() {
            return ordering;
        }
    }
}
