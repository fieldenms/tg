package ua.com.fielden.platform.equery.tokens.conditions;

import ua.com.fielden.platform.equery.LogicalOperator;
import ua.com.fielden.platform.equery.RootEntityMapper;
import ua.com.fielden.platform.equery.tokens.main.ConditionsGroup;

public final class GroupCondition extends Condition<GroupCondition> {
    private ConditionsGroup group;
    private boolean negated;

    /**
     * This constructor is required mainly for serialisation.
     * @param expression
     */
    public GroupCondition() {
    }

    public GroupCondition(final LogicalOperator logicalOperator, final boolean negated, final ConditionsGroup parent) {
	setLogicalOperator(logicalOperator);
	this.negated = negated;
	this.group = new ConditionsGroup(parent);
    }

    private GroupCondition(final ConditionsGroup group, final boolean negated, final LogicalOperator postOperator) {
	this.negated = negated;
	this.group = group;
	this.setLogicalOperator(postOperator);
    }

    public ConditionsGroup getGroup() {
	return group;
    }

    @Override
    public String getSql(final RootEntityMapper alias) {
	final StringBuffer sb = new StringBuffer();

	sb.append(negated ? "NOT" : "");
	sb.append(" (");
	sb.append(group.getSql(alias));
	sb.append(")");
	return sb.toString();
    }

    @Override
    public GroupCondition clon() {
	return new GroupCondition(group.clon(), negated, getLogicalOperator());
    }
}
