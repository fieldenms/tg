package ua.com.fielden.platform.serialisation.json.serialiser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.WordUtils;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToResultTickManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.CritOnly.Type;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.development.EntityDescriptor;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteriaUtils;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class CentreMangerToJsonSerialiser extends JsonSerializer<ICentreDomainTreeManagerAndEnhancer> {

    @Override
    public void serialize(final ICentreDomainTreeManagerAndEnhancer cdtme, final JsonGenerator generator, final SerializerProvider provider) throws IOException,
            JsonProcessingException {
        final Set<Class<?>> roots = cdtme.getRepresentation().rootTypes();
        if (roots.size() == 1) {
            final Class<?> root = roots.toArray(new Class[0])[0];
            final Pair<Map<String, Map<String, Object>>, List<Map<String, Object>>> crit = createCriterias(root, cdtme);
            final Pair<Map<String, Map<String, Object>>, List<Map<String, Object>>> result = createFetches(root, cdtme);
            generator.writeStartObject();
            generator.writeFieldName("query");
            generator.writeStartObject();
            generator.writeFieldName("entityType");
            generator.writeObject(root.getName());
            generator.writeFieldName("criteria");
            generator.writeObject(crit.getKey());
            generator.writeFieldName("fetch");
            generator.writeObject(result.getKey());
            generator.writeEndObject();
            generator.writeFieldName("centreConfig");
            generator.writeStartObject();
            generator.writeFieldName("criteria");
            generator.writeStartObject();
            generator.writeFieldName("columns");
            generator.writeNumber(cdtme.getFirstTick().getColumnsNumber());
            generator.writeFieldName("criteriaProperties");
            generator.writeObject(crit.getValue());
            generator.writeEndObject();
            generator.writeFieldName("resultProperties");
            generator.writeObject(result.getValue());
            generator.writeEndObject();
            generator.writeEndObject();
        } else {
            throw new IllegalStateException("The root type must be the only one!");
        }
    }

    private Pair<Map<String, Map<String, Object>>, List<Map<String, Object>>> createCriterias(final Class<?> root, final ICentreDomainTreeManagerAndEnhancer cdtme) {
        final Map<String, Map<String, Object>> centre = new HashMap<>();
        final List<Map<String, Object>> centreConfig = new ArrayList<>();
        final IAddToCriteriaTickManager tick = cdtme.getFirstTick();
        final List<String> checkedProps = tick.checkedProperties(root);
        final Class<?> managedType = cdtme.getEnhancer().getManagedType(root);
        final EntityDescriptor ed = new EntityDescriptor(managedType, checkedProps);

        for (final String prop : tick.checkedProperties(root)) {
            if (!AbstractDomainTree.isPlaceholder(prop)) {
                centre.put(prop, generateCritProp(root, prop, cdtme));
                centreConfig.add(generateCritConfigProp(prop, ed));
            } else {
                centreConfig.add(null);
            }
        }
        return new Pair<>(centre, centreConfig);
    }

    private Pair<Map<String, Map<String, Object>>, List<Map<String, Object>>> createFetches(final Class<?> root, final ICentreDomainTreeManagerAndEnhancer cdtme) {
        final Map<String, Map<String, Object>> fetch = new HashMap<>();
        final List<Map<String, Object>> fetchConfig = new ArrayList<>();
        final IAddToResultTickManager tick = cdtme.getSecondTick();
        final Pair<List<Pair<String, Integer>>, Map<String, List<String>>> fetchTotProps = EntityQueryCriteriaUtils.getMappedFetchAndTotals(root, tick, cdtme.getEnhancer());
        final List<Pair<String, Ordering>> orderingList = tick.orderedProperties(root);
        final List<String> checkedProps = tick.checkedProperties(root);
        final Class<?> managedType = cdtme.getEnhancer().getManagedType(root);
        final EntityDescriptor ed = new EntityDescriptor(managedType, checkedProps);

        for (final String prop : tick.checkedProperties(root)) {
            final Integer width = findWidth(prop, fetchTotProps.getKey());
            final Ordering order = findOrdering(prop, orderingList);
            final List<String> total = findTotals(prop, fetchTotProps.getValue());
            fetch.put(prop, generateFetchProp(order, total));
            fetchConfig.add(generateFetchConfigProp(root, prop, width, cdtme, ed));
        }

        return new Pair<Map<String, Map<String, Object>>, List<Map<String, Object>>>(fetch, fetchConfig);
    }

    private Map<String, Object> generateFetchProp(final Ordering order, final List<String> total) {
        final Map<String, Object> resultProp = new HashMap<>();
        resultProp.put("ordering", order);
        resultProp.put("summary", total);
        return resultProp;
    }

    private Map<String, Object> generateFetchConfigProp(final Class<?> root, final String prop, final Integer width, final ICentreDomainTreeManagerAndEnhancer cdtme, final EntityDescriptor ed) {
        final Class<?> managedType = cdtme.getEnhancer().getManagedType(root);
        final Class<?> type = prop.isEmpty() ? managedType : PropertyTypeDeterminator.determinePropertyType(managedType, prop);
        final Map<String, Object> resultProp = new HashMap<>();
        resultProp.put("propertyName", prop);
        resultProp.put("type", EntityUtils.isEntityType(type) ? "Entity" : WordUtils.capitalize(type.getSimpleName()));
        resultProp.put("title", ed.getTitle(prop));
        resultProp.put("description", ed.getDesc(prop));
        resultProp.put("width", width);
        return resultProp;
    }

    private List<String> findTotals(final String prop, final Map<String, List<String>> totMap) {
        final List<String> summary = totMap.get(prop);
        return summary == null ? new ArrayList<String>() : summary;
    }

    private Ordering findOrdering(final String prop, final List<Pair<String, Ordering>> orderingList) {
        for (final Pair<String, Ordering> ordProp : orderingList) {
            if (prop.equals(ordProp.getKey())) {
                return ordProp.getValue();
            }
        }
        return null;
    }

    private Integer findWidth(final String prop, final List<Pair<String, Integer>> propWidth) {
        for (final Pair<String, Integer> widthProp : propWidth) {
            if (prop.equals(widthProp.getKey())) {
                return widthProp.getValue();
            }
        }
        return 0;
    }

    private Map<String, Object> generateCritProp(final Class<?> root, final String prop, final ICentreDomainTreeManagerAndEnhancer cdtme) {
        final IAddToCriteriaTickManager tick = cdtme.getFirstTick();
        final Class<?> managedType = cdtme.getEnhancer().getManagedType(root);
        final Map<String, Object> centreProp = new HashMap<>();
        final Class<?> type = prop.isEmpty() ? managedType : PropertyTypeDeterminator.determinePropertyType(managedType, prop);
        final CritOnly critOnlyAnnotation = prop.isEmpty() ? null : AnnotationReflector.getPropertyAnnotation(CritOnly.class, managedType, prop);
        final boolean isSingle = (critOnlyAnnotation != null && Type.SINGLE.equals(critOnlyAnnotation.value()))
                || (!EntityUtils.isEntityType(type) && !(EntityUtils.isBoolean(type) || EntityUtils.isRangeType(type)));
        centreProp.put("type", EntityUtils.isEntityType(type) ? "Entity" : WordUtils.capitalize(type.getSimpleName()));
        centreProp.put("isSingle", isSingle);
        centreProp.put("value1", tick.getValue(root, prop));
        centreProp.put("value2", AbstractDomainTree.isDoubleCriterionOrBoolean(root, prop) ? tick.getValue2(root, prop) : null);
        return centreProp;

    }

    private Map<String, Object> generateCritConfigProp(final String prop, final EntityDescriptor ed) {
        final Map<String, Object> centreProp = new HashMap<>();
        centreProp.put("propertyName", prop);
        centreProp.put("title", ed.getTitle(prop));
        centreProp.put("description", ed.getDesc(prop));
        return centreProp;
    }

}
