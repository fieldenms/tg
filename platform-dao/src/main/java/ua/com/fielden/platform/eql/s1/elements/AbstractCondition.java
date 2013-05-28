package ua.com.fielden.platform.eql.s1.elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.com.fielden.platform.eql.s2.elements.ICondition2;


public abstract class AbstractCondition<S2 extends ICondition2> implements ICondition<S2> {

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
}