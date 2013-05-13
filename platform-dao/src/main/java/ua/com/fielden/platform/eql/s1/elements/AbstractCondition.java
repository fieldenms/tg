package ua.com.fielden.platform.eql.s1.elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public abstract class AbstractCondition implements ICondition {

    protected abstract List<IElement> getCollection();

    @Override
    public List<EntQuery> getLocalSubQueries() {
	if (ignore()) {
	    return Collections.emptyList();
	} else {
	    final List<EntQuery> result = new ArrayList<EntQuery>();

	    for (final IElement item : getCollection()) {
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

	    for (final IElement item : getCollection()) {
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

	    for (final IElement item : getCollection()) {
		result.addAll(item.getAllValues());
	    }

	    return result;
	}
    }
}