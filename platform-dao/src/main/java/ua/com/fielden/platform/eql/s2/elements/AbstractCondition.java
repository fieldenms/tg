package ua.com.fielden.platform.eql.s2.elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public abstract class AbstractCondition implements ICondition2 {

    protected abstract List<IElement2> getCollection();

    @Override
    public List<EntQuery> getLocalSubQueries() {
	if (ignore()) {
	    return Collections.emptyList();
	} else {
	    final List<EntQuery> result = new ArrayList<EntQuery>();

	    for (final IElement2 item : getCollection()) {
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

	    for (final IElement2 item : getCollection()) {
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

	    for (final IElement2 item : getCollection()) {
		result.addAll(item.getAllValues());
	    }

	    return result;
	}
    }
}