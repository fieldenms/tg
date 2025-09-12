package ua.com.fielden.platform.entity.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.proxy.StrictProxyException;

import static ua.com.fielden.platform.utils.EntityUtils.splitPropPathToArray;

/**
 * An entity class for ad-hoc construction of entities. Useful when the structure (i.e. properties) becomes known at runtime rather than design time.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@CompanionObject(IEntityAggregates.class)
public class EntityAggregates extends AbstractEntity<String> {

    private final Map<String, Object> aggregates = new HashMap<>();

    @IsProperty(value = String.class, linkProperty = "--stub-link-property--")
    private List<String> groupKeys = new ArrayList<>();
    
    @IsProperty(value = AbstractEntity.class, linkProperty = "--stub-link-property--")
    private List<AbstractEntity<?>> groupValues = new ArrayList<>();

    @IsProperty(value = String.class, linkProperty = "--stub-link-property--")
    private List<String> aggrKeys = new ArrayList<>();
    
    @IsProperty(value = Object.class, linkProperty = "--stub-link-property--")
    private List<Object> aggrValues = new ArrayList<>();

    private Map<String, Object> getAggregates() {
        if (aggregates.isEmpty()) {
            for (int index = 0; index < groupKeys.size(); index++) {
                aggregates.put(groupKeys.get(index), groupValues.get(index));
            }
            for (int index = 0; index < aggrKeys.size(); index++) {
                aggregates.put(aggrKeys.get(index), aggrValues.get(index));
            }
        }
        return aggregates;
    }

    public Object getValueByKey(final String key) {
        final int indexKey = aggrKeys.indexOf(key);
        final int indexAggr = groupKeys.indexOf(key);

        if (indexKey >= 0) {
            return aggrValues.get(indexKey);
        } else if (indexAggr >= 0) {
            return groupValues.get(indexAggr);
        } else {
            return null;
        }
    }

    public void setValueAndKey(final String key, final Object value) {
        removeValueAndKey(key, value);

        if (value instanceof AbstractEntity) {
            if (groupKeys.indexOf(key) >= 0) {
                groupValues.set(groupKeys.indexOf(key), (AbstractEntity) value);
            } else {
                groupKeys.add(key);
                groupValues.add((AbstractEntity) value);
            }
        } else {
            if (aggrKeys.indexOf(key) >= 0) {
                aggrValues.set(aggrKeys.indexOf(key), value);
            } else {
                aggrKeys.add(key);
                aggrValues.add(value);
            }
        }
        aggregates.put(key, value);
    }

    public void removeValueAndKey(final String key, final Object value) {
        if (groupKeys.indexOf(key) >= 0) {
            groupValues.remove(groupKeys.indexOf(key));
            groupKeys.remove(key);
        }
        if (aggrKeys.indexOf(key) >= 0) {
            aggrValues.remove(aggrKeys.indexOf(key));
            aggrKeys.remove(key);
        }
    }

    public List<String> getGroupKeys() {
        return Collections.unmodifiableList(groupKeys);
    }

    public List<AbstractEntity<?>> getGroupValues() {
        return Collections.unmodifiableList(groupValues);
    }

    public List<String> getAggrKeys() {
        return Collections.unmodifiableList(aggrKeys);
    }

    public List<Object> getAggrValues() {
        return Collections.unmodifiableList(aggrValues);
    }

    @Observable
    public void setGroupKeys(final List<String> groupKeys) {
        this.groupKeys = groupKeys;
    }

    @Observable
    public void setGroupValues(final List<AbstractEntity<?>> groupValues) {
        this.groupValues = groupValues;
    }

    @Observable
    public void setAggrKeys(final List<String> aggrKeys) {
        this.aggrKeys = aggrKeys;
    }

    @Observable
    public void setAggrValues(final List<Object> aggrValues) {
        this.aggrValues = aggrValues;
    }

    private AbstractEntity<?> findRootEntity() {
        final List<AbstractEntity<?>> entities = new ArrayList<>();
        for (final Object value : aggregates.values()) {
            if (value instanceof AbstractEntity) {
                entities.add((AbstractEntity<?>) value);
            }
        }

        return entities.size() == 1 ? entities.get(0) : null;
    }

    @Override
    public EntityAggregates set(final String propertyName, final Object value) {
        setValueAndKey(propertyName, value);
        return this;
    }

    @Override
    public <T> T get(final String propertyName) {
        try {
            final String[] parts = splitPropPathToArray(propertyName);

            if (!getAggregates().containsKey(parts[0])) {
                // trying to find in root entity if such discovered
                final AbstractEntity<?> rootEntity = findRootEntity();
                if (rootEntity != null) {
                    try {
                        // TODO alias should become useful and ultimately required in case of many root entities discovered.
                        return (T) rootEntity.get(propertyName);
                    }  catch (final StrictProxyException e) {
                        throw e;
                    }  catch (final Exception e) {
                        try {
                            return (T) rootEntity.get(propertyName.substring(propertyName.indexOf(".") + 1));
                        } catch (final Exception e1) {
                        }
                    }

                }
                // otherwise searching within EntityAggregates properties (those that have annotations and all the stuff) by calling AbstractEntity get() implementation.
                return super.get(propertyName);
            }

            final T root = (T) getAggregates().get(parts[0]);

            // skip going deeper if null is found instead of data
            if (root == null) {
                return null;
            }

            return parts.length == 1 ? root : ((AbstractEntity<?>) root).get(propertyName.substring(propertyName.indexOf(".") + 1));
        } catch (final Exception e) {
            throw new IllegalArgumentException("Could not get the value for property " + propertyName + " for instance " + this, e);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("EntityAggregates:\n");

        int i = 0;
        sb.append(" Group items:\n");
        for (final String entry : groupKeys) {
            sb.append("   " + entry + " = " + groupValues.get(i) + "\n");
            i = i + 1;
        }
        i = 0;
        sb.append(" Aggregated items:\n");
        for (final String entry : aggrKeys) {
            sb.append("   " + entry + " = " + aggrValues.get(i) + "\n");
            i = i + 1;
        }

        return sb.toString();
    }

}
