package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractCondition implements ICondition {

    @Override
    public Set<ISource> getInvolvedSources() {
        final Set<ISource> result = new HashSet<>();
        for (final EntProp prop : getLocalProps()) {
            result.add(prop.getSource());
        }
        return result;
    }

    protected abstract List<IPropertyCollector> getCollection();

    @Override
    public List<EntQuery> getLocalSubQueries() {
        if (ignore()) {
            return Collections.emptyList();
        } else {
            final List<EntQuery> result = new ArrayList<EntQuery>();

            for (final IPropertyCollector item : getCollection()) {
                result.addAll(item.getLocalSubQueries());
            }

            return result;
        }
    }

    @Override
    public List<EntProp> getLocalProps() {
        if (ignore()) {
            return Collections.emptyList();
        } else {
            final List<EntProp> result = new ArrayList<EntProp>();

            for (final IPropertyCollector item : getCollection()) {
                result.addAll(item.getLocalProps());
            }

            return result;
        }
    }

    @Override
    public List<EntValue> getAllValues() {
        if (ignore()) {
            return Collections.emptyList();
        } else {
            final List<EntValue> result = new ArrayList<EntValue>();

            for (final IPropertyCollector item : getCollection()) {
                result.addAll(item.getAllValues());
            }

            return result;
        }
    }
}