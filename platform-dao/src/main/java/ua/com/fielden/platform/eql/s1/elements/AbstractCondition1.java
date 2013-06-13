package ua.com.fielden.platform.eql.s1.elements;

import java.util.List;

import ua.com.fielden.platform.eql.s2.elements.ICondition2;

public abstract class AbstractCondition1<S2 extends ICondition2> implements ICondition1<S2> {
    protected abstract List<IElement1> getCollection();
}