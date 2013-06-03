package ua.com.fielden.platform.eql.s2.elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public abstract class AbstractCondition2 implements ICondition2 {

    protected abstract List<IElement2> getCollection();

    @Override
    public List<EntQuery2> getLocalSubQueries() {
	if (ignore()) {
	    return Collections.emptyList();
	} else {
	    final List<EntQuery2> result = new ArrayList<EntQuery2>();

	    for (final IElement2 item : getCollection()) {
		result.addAll(item.getLocalSubQueries());
	    }

	    return result;
	}
    }

    @Override
    public List<EntProp2> getLocalProps() {
	if (ignore()) {
	    return Collections.emptyList();
	} else {
	    final List<EntProp2> result = new ArrayList<EntProp2>();

	    for (final IElement2 item : getCollection()) {
		result.addAll(item.getLocalProps());
	    }

	    return result;
	}
    }

    @Override
    public List<EntValue2> getAllValues() {
	if (ignore()) {
	    return Collections.emptyList();
	} else {
	    final List<EntValue2> result = new ArrayList<EntValue2>();

	    for (final IElement2 item : getCollection()) {
		result.addAll(item.getAllValues());
	    }

	    return result;
	}
    }
}