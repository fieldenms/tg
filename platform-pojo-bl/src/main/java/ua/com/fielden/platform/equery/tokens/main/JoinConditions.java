package ua.com.fielden.platform.equery.tokens.main;

import ua.com.fielden.platform.equery.RootEntityMapper;
import ua.com.fielden.platform.equery.interfaces.IClon;
import ua.com.fielden.platform.equery.interfaces.IQueryToken;

public final class JoinConditions implements IQueryToken, IClon<JoinConditions> {
    private ConditionsGroup onGroup;
    private QuerySource querySource;
    private String alias;
    private boolean left = false;

    /**
     * This constructor is required mainly for serialisation.
     * @param expression
     */
    protected JoinConditions() {
    }

    public JoinConditions(final QuerySource querySource, final String alias, final boolean left, final ConditionsGroup onGroup) {
	this.alias = alias;
	this.querySource = querySource.clon();
	this.left = left;
	this.onGroup = onGroup;// == null ? null : onGroup.clon();
    }

    public JoinConditions(final QuerySource querySource, final String alias) {
	this.alias = alias;
	this.querySource = querySource;
    }

    public JoinConditions clon() {
	//	final JoinConditions clon = new JoinConditions(isCurrent, currLogicalOperator, (currProperty != null ? currProperty.clon() : null));
	//	final List<Condition<? extends Condition>> clonedConditions = new ArrayList<Condition<? extends Condition>>();
	//	for (final Condition<? extends Condition> condition : conditions) {
	//	    final Condition<? extends Condition> clonedCondition = condition.clon();
	//	    if (condition instanceof GroupCondition) {
	//		((GroupCondition) clonedCondition).getGroup().parentGroupReference = clon;
	//	    }
	//	    clonedConditions.add(clonedCondition);
	//	}
	//	clon.conditions = clonedConditions;
	//
	//	if (currGroupCondition != null) {
	//	    clon.currGroupCondition = currGroupCondition.clon();
	//	    // TODO ??? check
	//	    clon.currGroupCondition.getGroup().parentGroupReference = clon;
	//	}

	return new JoinConditions(querySource.clon(), alias, left, onGroup); // == null ? null : onGroup.clon()
    }

    @Override
    public String getSql(final RootEntityMapper alias) {
	final StringBuffer sb = new StringBuffer();
	//	    sb.append(parentGroupReference == null ? "\n   WHERE " : "");
	//
	//	    for (final Iterator<Condition<? extends Condition>> iterator = conditions.iterator(); iterator.hasNext();) {
	//		final Condition<? extends Condition> condition = iterator.next();
	//		sb.append(condition.logicalOperatorSql());
	//		sb.append(condition.getSql(alias));
	//		if (iterator.hasNext()) {
	//		    sb.append(" ");
	//		}
	//	    }

	sb.append("   AAAA   ");
	return sb.toString();
    }

    public ConditionsGroup getOnGroup() {
	return onGroup;
    }

    public String getAlias() {
	return alias;
    }

    public void setOnGroup(final ConditionsGroup onGroup) {
	this.onGroup = onGroup;
    }

    public void setAlias(final String alias) {
	this.alias = alias;
    }

    public boolean isLeft() {
	return left;
    }

    public void setLeft(final boolean left) {
	this.left = left;
    }

    public QuerySource getQuerySource() {
	return querySource;
    }

    public void setQuerySource(final QuerySource querySource) {
	this.querySource = querySource;
    }
}