package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public abstract class AbstractCondition implements ICondition {

    public abstract List<EntProp> getLocalProps();

    @Override
    public Set<ISource> getInvolvedSources() {
	final Set<ISource> result = new HashSet<>();
	for (final EntProp prop : getLocalProps()) {
	    result.add(prop.getSource());
	}
	return result;
    }
}