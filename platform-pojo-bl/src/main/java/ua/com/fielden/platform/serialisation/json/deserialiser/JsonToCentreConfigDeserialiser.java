package ua.com.fielden.platform.serialisation.json.deserialiser;

import static ua.com.fielden.platform.reflection.ClassesRetriever.findClass;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.serialisation.json.TgObjectMapper;
import ua.com.fielden.platform.serialisation.json.deserialiser.JsonToCentreConfigDeserialiser.LightweightCentre;
import ua.com.fielden.platform.serialisation.json.deserialiser.JsonToCriteriaDeserialiser.CritProp;
import ua.com.fielden.platform.serialisation.json.deserialiser.JsonToResultDeserialiser.ResultProperty;
import ua.com.fielden.platform.swing.review.DynamicQueryBuilder.QueryProperty;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteriaUtils;
import ua.com.fielden.platform.utils.Pair;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class JsonToCentreConfigDeserialiser extends JsonDeserializer<LightweightCentre> {

    private final TgObjectMapper mapper;

    public JsonToCentreConfigDeserialiser(final TgObjectMapper mapper) {
        this.mapper = mapper;
    }

    @SuppressWarnings("unchecked")
    @Override
    public LightweightCentre deserialize(final JsonParser parser, final DeserializationContext context) throws IOException, JsonProcessingException {
	final JsonNode node = parser.readValueAsTree();
	final Class<AbstractEntity<?>> entityType = (Class<AbstractEntity<?>>) findClass(node.get("entityType").asText());
	final Map<String, CritProp> criteriaProperties = node//
		.path("criteria")//
		.traverse(mapper).readValueAs(
			new TypeReference<Map<String, CritProp>>() {
		});
	final Map<String, ResultProperty> resultProperties = node//
		.path("fetch").traverse(mapper).readValueAs(
			new TypeReference<Map<String, ResultProperty>>() {
		});
	return new LightweightCentre(entityType, criteriaProperties, resultProperties);
    }

    public static class LightweightCentre {

        private final Class<AbstractEntity<?>> entityType;
        private final Map<String, CritProp> criteriaProperties;
        private final Map<String, ResultProperty> resultProperties;

        public LightweightCentre(//
        final Class<AbstractEntity<?>> entityType, //
                final Map<String, CritProp> criteriaProperties,//
                final Map<String, ResultProperty> resultProperties) {
            this.entityType = entityType;
            this.criteriaProperties = criteriaProperties;
            this.resultProperties = resultProperties;
        }

        public Class<AbstractEntity<?>> getEntityType() {
            return entityType;
        }

        public List<QueryProperty> getQueryProperties(final Class<AbstractEntity<?>> managedType) {
            final List<QueryProperty> queryProps = new ArrayList<>();
            for (final Map.Entry<String, CritProp> prop : criteriaProperties.entrySet()) {
                queryProps.add(createQueryProperty(managedType, prop));
            }
            return queryProps;
        }

        private QueryProperty createQueryProperty(final Class<AbstractEntity<?>> managedType, final Entry<String, CritProp> prop) {
            final QueryProperty queryProp = EntityQueryCriteriaUtils.createNotInitialisedQueryProperty(managedType, prop.getKey());
            queryProp.setValue(prop.getValue().getValue1());
            queryProp.setValue2(prop.getValue().getValue2());
            return queryProp;
        }

        public Set<String> createFetchProps() {
            return resultProperties.keySet();
        }

        public Set<String> createSummaryProps() {
            final Set<String> summaryProps = new HashSet<>();
            for (final ResultProperty resProp : resultProperties.values()) {
                summaryProps.addAll(resProp.getSummaryProps());
            }
            return summaryProps;
        }

        public List<Pair<String, Ordering>> createOrderingProps() {
            final List<Pair<String, Ordering>> orderingList = new ArrayList<>();
            for (final Map.Entry<String, ResultProperty> resProp : resultProperties.entrySet()) {
                if (resProp.getValue().getOrdering() != null) {
                    orderingList.add(new Pair<String, Ordering>(resProp.getKey(), resProp.getValue().getOrdering()));
                }
            }
            return orderingList;
        }

        public Map<String, Pair<Object, Object>> createParamMap() {
            final Map<String, Pair<Object, Object>> params = new HashMap<>();
            for (final Map.Entry<String, CritProp> critProp : criteriaProperties.entrySet()) {
                if (critProp != null) {
                    params.put(critProp.getKey(), new Pair<>(critProp.getValue().getValue1(), critProp.getValue().getValue2()));
                }
            }
            return params;
        }

    }
}
