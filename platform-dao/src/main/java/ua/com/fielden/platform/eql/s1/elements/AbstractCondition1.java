package ua.com.fielden.platform.eql.s1.elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.com.fielden.platform.eql.s2.elements.ICondition2;


public abstract class AbstractCondition1<S2 extends ICondition2> implements ICondition1<S2> {

    protected abstract List<IElement1> getCollection();

    @Override
    public List<EntQuery1> getLocalSubQueries() {
	if (ignore()) {
	    return Collections.emptyList();
	} else {
	    final List<EntQuery1> result = new ArrayList<EntQuery1>();

	    for (final IElement1 item : getCollection()) {
		result.addAll(item.getLocalSubQueries());
	    }

	    return result;
	}
    }

    @Override
    public List<EntProp1> getLocalProps() {
	if (ignore()) {
	    return Collections.emptyList();
	} else {
	    final List<EntProp1> result = new ArrayList<EntProp1>();

	    for (final IElement1 item : getCollection()) {
		result.addAll(item.getLocalProps());
	    }

	    return result;
	}
    }
}