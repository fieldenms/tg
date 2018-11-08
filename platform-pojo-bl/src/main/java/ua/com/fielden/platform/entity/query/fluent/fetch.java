package ua.com.fielden.platform.entity.query.fluent;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.VERSION;
import static ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory.ALL;
import static ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory.DEFAULT;
import static ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory.ID_AND_VERSION;
import static ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory.ID_ONLY;
import static ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory.KEY_AND_DESC;
import static ua.com.fielden.platform.reflection.Finder.isPropertyPresent;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;

public class fetch<T extends AbstractEntity<?>> {
    public static final String MSG_MISMATCH_BETWEEN_PROPERTY_AND_FETCH_MODEL_TYPES = "Mismatch between actual type [%s] of property [%s] in entity type [%s] and its fetch model type [%s]!";

    public enum FetchCategory {
        ALL, ALL_INCL_CALC, DEFAULT, KEY_AND_DESC, ID_ONLY, ID_AND_VERSION, NONE
    }

    private final Class<T> entityType;
    private final Map<String, fetch<? extends AbstractEntity<?>>> includedPropsWithModels = new HashMap<>();
    private final Set<String> includedProps = new HashSet<>();
    private final Set<String> excludedProps = new HashSet<>();
    private final FetchCategory fetchCategory;
    private final boolean instrumented;

    /**
     * Used mainly for serialisation.
     */
    protected fetch() {
        this(null, ID_AND_VERSION);
    }

    public fetch(final Class<T> entityType, final FetchCategory fetchCategory, final boolean instrumented) {
        this.entityType = entityType;
        this.fetchCategory = fetchCategory;
        this.instrumented = instrumented;
    }

    public fetch(final Class<T> entityType, final FetchCategory fetchCategory) {
        this(entityType, fetchCategory, false);
    }
    
    private void validate(final String propName) {
        checkForExistence(propName);
        checkForDuplicate(propName);
    }
    
    private void checkForDuplicate(final String propName) {
        if (includedPropsWithModels.containsKey(propName) || includedProps.contains(propName) || excludedProps.contains(propName)) {
            throw new IllegalArgumentException("Property [" + propName + "] is already present within fetch model!");
        }
    }
    
    private void checkForExistence(final String propName) {
        if (entityType != EntityAggregates.class && // 
                !ID.equals(propName) && //
                !VERSION.equals(propName) && //
                !isPropertyPresent(entityType, propName)) {
            throw new IllegalArgumentException("Property [" + propName + "] is not present within [" + entityType.getSimpleName() + "] entity!");
        }
    }

    private static <T extends AbstractEntity<?>> fetch<T> copy(final fetch<T> fromFetch) {
        final fetch<T> result = new fetch<>(fromFetch.entityType, fromFetch.fetchCategory, fromFetch.isInstrumented());
        result.includedPropsWithModels.putAll(fromFetch.includedPropsWithModels);
        result.includedProps.addAll(fromFetch.includedProps);
        result.excludedProps.addAll(fromFetch.excludedProps);
        return result;
    }

    /**
     * Should be used to indicate a name of the first level property that should be initialised in the retrieved entity instances.
     *
     * @param propName
     *            - Could be name of the primitive property (e.g. "desc", "numberOfPages"), entity property ("station"), composite type property ("cost", "cost.amount"), union
     *            entity property ("location", "location.workshop"), collectional property ("slots"), one-to-one association property ("financialDetails").
     * @return
     */
    public fetch<T> with(final String propName) {
        validate(propName);
        final fetch<T> result = copy(this);
        result.includedProps.add(propName);
        return result;
    }

    /**
     * Should be used to indicate a name of the first level property that should not be initialised in the retrieved entity instances.
     *
     * @param propName
     *            - Could be name of the primitive property (e.g. "desc", "numberOfPages"), entity property ("station"), composite type property ("cost", "cost.amount"), union
     *            entity property ("location", "location.workshop"), collectional property ("slots"), one-to-one association property ("financialDetails").
     * @return
     */
    public fetch<T> without(final String propName) {
        validate(propName);
        final fetch<T> result = copy(this);
        result.excludedProps.add(propName);
        return result;
    }

    /**
     * Should be used to indicate a name of the first level entity property that should be initialised in the retrieved entity instances and the model to indicate which
     * subproperties of the given property should be initialised as well.
     *
     * @param propName
     * @param fetchModel
     * @return
     */
    public fetch<T> with(final String propName, final fetch<? extends AbstractEntity<?>> fetchModel) {
        validate(propName);
        // if the entityType is not an aggregate entity then we must validate that the type of propName and the type of fetchModel match
        if (entityType != EntityAggregates.class) {
            final Class<?> propType = determinePropertyType(entityType, propName);
            if (propType != fetchModel.entityType) {
                throw new EqlException(format(MSG_MISMATCH_BETWEEN_PROPERTY_AND_FETCH_MODEL_TYPES, propType, propName, entityType, fetchModel.getEntityType()));
            }
        }

        final fetch<T> result = copy(this);
        result.includedPropsWithModels.put(propName, fetchModel);
        return result;
    }

    public Class<T> getEntityType() {
        return entityType;
    }

    public boolean isInstrumented() {
        return instrumented;
    }

    public Map<String, fetch<? extends AbstractEntity<?>>> getIncludedPropsWithModels() {
        return unmodifiableMap(includedPropsWithModels);
    }

    public Set<String> getIncludedProps() {
        return unmodifiableSet(includedProps);
    }

    public Set<String> getExcludedProps() {
        return unmodifiableSet(excludedProps);
    }

    public FetchCategory getFetchCategory() {
        return fetchCategory;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((entityType == null) ? 0 : entityType.hashCode());
        result = prime * result + ((excludedProps == null) ? 0 : excludedProps.hashCode());
        result = prime * result + ((fetchCategory == null) ? 0 : fetchCategory.hashCode());
        result = prime * result + ((includedProps == null) ? 0 : includedProps.hashCode());
        result = prime * result + ((includedPropsWithModels == null) ? 0 : includedPropsWithModels.hashCode());
        result = prime * result + (instrumented ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof fetch)) {
            return false;
        }
        final fetch other = (fetch) obj;
        if (entityType == null) {
            if (other.entityType != null) {
                return false;
            }
        } else if (!entityType.equals(other.entityType)) {
            return false;
        }
        if (excludedProps == null) {
            if (other.excludedProps != null) {
                return false;
            }
        } else if (!excludedProps.equals(other.excludedProps)) {
            return false;
        }
        if (fetchCategory != other.fetchCategory) {
            return false;
        }
        if (includedProps == null) {
            if (other.includedProps != null) {
                return false;
            }
        } else if (!includedProps.equals(other.includedProps)) {
            return false;
        }
        if (includedPropsWithModels == null) {
            if (other.includedPropsWithModels != null) {
                return false;
            }
        } else if (!includedPropsWithModels.equals(other.includedPropsWithModels)) {
            return false;
        }
        if (instrumented != other.instrumented) {
            return false;
        }
        return true;
    }

    private static String offset = "    ";

    @Override
    public String toString() {
        return getString(offset);
    }

    private String getString(final String currOffset) {
        final StringBuffer sb = new StringBuffer();
        sb.append("\n" + currOffset + entityType.getSimpleName() + " [" + fetchCategory + "]" + (isInstrumented() ? " instrumented" : ""));
        if (includedProps.size() > 0) {
            sb.append("\n" + currOffset + "+ " + includedProps);
        }
        if (excludedProps.size() > 0) {
            sb.append("\n" + currOffset + "- " + excludedProps);
        }
        if (includedPropsWithModels.size() > 0) {
            for (final Map.Entry<String, fetch<?>> fetchModel : includedPropsWithModels.entrySet()) {
                sb.append("\n" + currOffset + "+ " + fetchModel.getKey() + fetchModel.getValue().getString(currOffset + offset));
            }
        }
        return sb.toString();
    }

    private FetchCategory getMergedFetchCategory(final fetch<?> second) {
        if (fetchCategory == ALL || second.fetchCategory == ALL) {
            return ALL;
        }

        if (fetchCategory == DEFAULT || second.fetchCategory == DEFAULT) {
            return DEFAULT;
        }

        if (fetchCategory == KEY_AND_DESC || second.fetchCategory == KEY_AND_DESC) {
            return KEY_AND_DESC;
        }

        if (fetchCategory == ID_AND_VERSION || second.fetchCategory == ID_AND_VERSION) {
            return ID_AND_VERSION;
        }

        return ID_ONLY;
    }

    public fetch<?> unionWith(final fetch<?> second) {
        if (second == null) {
            return this;
        }

        final FetchCategory resultCategory = getMergedFetchCategory(second);
        final fetch<T> result = new fetch<>(getEntityType(), resultCategory, (isInstrumented() || second.isInstrumented()));
        result.includedProps.addAll(includedProps);
        result.includedProps.addAll(second.includedProps);
        result.excludedProps.addAll(excludedProps);
        result.excludedProps.addAll(second.excludedProps);
        for (final Entry<String, fetch<? extends AbstractEntity<?>>> iterable_element : includedPropsWithModels.entrySet()) {
            result.includedPropsWithModels.put(iterable_element.getKey(), iterable_element.getValue().unionWith(second.getIncludedPropsWithModels().get(iterable_element.getKey())));
        }

        for (final Entry<String, fetch<? extends AbstractEntity<?>>> iterable_element : second.includedPropsWithModels.entrySet()) {
            if (!result.includedPropsWithModels.containsKey(iterable_element.getKey())) {
                result.includedPropsWithModels.put(iterable_element.getKey(), iterable_element.getValue().unionWith(getIncludedPropsWithModels().get(iterable_element.getKey())));
            }
        }

        return result;
    }
}