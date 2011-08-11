package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IOrder;

public class Order<T> extends AbstractQueryLink implements IOrder<T> {
    T parent;
    protected Order(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    public T asc() {
	getTokens().asc();
	return parent;
    }

    @Override
    public T desc() {
	getTokens().desc();
	return parent;
    }

}
