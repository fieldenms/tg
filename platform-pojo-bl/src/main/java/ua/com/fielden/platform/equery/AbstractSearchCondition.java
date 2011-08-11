package ua.com.fielden.platform.equery;

import ua.com.fielden.platform.equery.interfaces.IAbstract.IAbstractSearchCondition;

abstract class AbstractSearchCondition<T1, T2> extends AbstractQueryLink implements IAbstractSearchCondition<T1, T2> {

    AbstractSearchCondition(final QueryTokens queryTokens) {
	super(queryTokens);
    }

    abstract T1 createCompoundCondition(final QueryTokens queryTokens);

    abstract T2 createConditionSubject(final QueryTokens queryTokens);

    @Override
    public T2 eq() {
	return createConditionSubject(this.getTokens().eq());
    }

    @Override
    public T2 ne() {
	return createConditionSubject(this.getTokens().ne());
    }

    @Override
    public T2 ge() {
	return createConditionSubject(this.getTokens().ge());
    }

    @Override
    public T2 le() {
	return createConditionSubject(this.getTokens().le());
    }

    @Override
    public T2 gt() {
	return createConditionSubject(this.getTokens().gt());
    }

    @Override
    public T2 lt() {
	return createConditionSubject(this.getTokens().lt());
    }

    @Override
    public T2 in() {
	return createConditionSubject(this.getTokens().in(false));
    }

    @Override
    public T2 notIn() {
	return createConditionSubject(this.getTokens().in(true));
    }

    @Override
    public T2 like() {
	return createConditionSubject(this.getTokens().like(false));
    }

    @Override
    public T2 notLike() {
	return createConditionSubject(this.getTokens().like(true));
    }

    @Override
    public T1 isFalse() {
	return createCompoundCondition(this.getTokens().isFalse());
    }

    @Override
    public T1 isTrue() {
	return createCompoundCondition(this.getTokens().isTrue());
    }

    @Override
    public T1 isNull() {
	return createCompoundCondition(this.getTokens().isNull());
    }

    @Override
    public T1 isNotNull() {
	return createCompoundCondition(this.getTokens().isNotNull());
    }

    @Override
    public T1 between(final Object value1, final Object value2) {
	return createCompoundCondition(this.getTokens().between(value1, value2));
    }
}
