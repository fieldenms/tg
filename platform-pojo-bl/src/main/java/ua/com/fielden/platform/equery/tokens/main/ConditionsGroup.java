package ua.com.fielden.platform.equery.tokens.main;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ua.com.fielden.platform.equery.LogicalOperator;
import ua.com.fielden.platform.equery.RootEntityMapper;
import ua.com.fielden.platform.equery.interfaces.IClon;
import ua.com.fielden.platform.equery.interfaces.IQueryToken;
import ua.com.fielden.platform.equery.tokens.conditions.ComparisonOperation;
import ua.com.fielden.platform.equery.tokens.conditions.Condition;
import ua.com.fielden.platform.equery.tokens.conditions.GroupCondition;
import ua.com.fielden.platform.equery.tokens.properties.SearchProperty;

/**
 * Represents list of conditions on the arbitrary nested level. Parent property provides reference to parent in nested hierarchy.
 *
 * @author TG Team
 *
 */
public final class ConditionsGroup implements IQueryToken, IClon<ConditionsGroup> {
    private GroupCondition currGroupCondition;
    private SearchProperty currProperty;
    private LogicalOperator currLogicalOperator;
    private ComparisonOperation currOperation;

    private final ArrayList<Condition> conditions = new ArrayList<Condition>();
    private ConditionsGroup parentGroupReference;
    private boolean isCurrent = false;
    private boolean currNegated = false;

    public ConditionsGroup() {
	isCurrent = true;
    }

    public ConditionsGroup(final ConditionsGroup parentGroupReference) {
	this.parentGroupReference = parentGroupReference;
	if (parentGroupReference == null) {
	    isCurrent = true;
	}
    }

    private ConditionsGroup(final boolean isCurrent, final LogicalOperator currLogicalOperator, final SearchProperty currProperty, final boolean negated, final ComparisonOperation operation) {
	this.isCurrent = isCurrent;
	this.currLogicalOperator = currLogicalOperator;
	this.currProperty = currProperty;
	this.currOperation = operation;
	this.currNegated = negated;
    }


    public boolean isCurrNegated() {
	return currNegated;
    }

    public void setCurrNegated(final boolean currNegated) {
	this.currNegated = currNegated;
    }

    @Override
    public String toString() {
	final StringBuffer sb = new StringBuffer();
	sb.append("WHERE\n");
	if (conditions.size() > 0) {
	    sb.append(" calc props:\n ");
	    for (final Condition condition : conditions) {
		sb.append("  " + condition);
	    }
	}

	// TODO complete

	return sb.toString();
    }

    public SearchProperty getCurrProperty() {
	return currProperty;
    }

    public void setCurrProperty(final SearchProperty currProperty) {
	this.currProperty = currProperty;
    }

    public LogicalOperator getCurrLogicalOperator() {
	return currLogicalOperator;
    }

    public void setCurrLogicalOperator(final LogicalOperator currLogicalOperator) {
	this.currLogicalOperator = currLogicalOperator;
    }

    public ComparisonOperation getCurrOperation() {
	return currOperation;
    }

    public void setCurrOperation(final ComparisonOperation currOperation) {
	this.currOperation = currOperation;
    }

    public List<Condition> getConditions() {
	return conditions;
    }

    public boolean isCurrent() {
	return isCurrent;
    }

    public void setCurrent(final boolean current) {
	this.isCurrent = current;
    }

    public GroupCondition getCurrGroupCondition() {
	return currGroupCondition;
    }

    public void setCurrGroupCondition(final GroupCondition currGroupCondition) {
	this.currGroupCondition = currGroupCondition;
    }

    public ConditionsGroup getParentGroupReference() {
	return parentGroupReference;
    }

    public void setParentGroupReference(final ConditionsGroup parentGroupReference) {
	this.parentGroupReference = parentGroupReference;
    }

    public ConditionsGroup clon() {
	final ConditionsGroup clon = new ConditionsGroup(isCurrent, currLogicalOperator, (currProperty != null ? currProperty.clon() : null), currNegated, currOperation);
	final ArrayList<Condition> clonedConditions = new ArrayList<Condition>();
	for (final Condition condition : conditions) {
	    final Condition clonedCondition = (Condition) condition.clon();
	    if (condition instanceof GroupCondition) {
		((GroupCondition) clonedCondition).getGroup().parentGroupReference = clon;
	    }
	    clonedConditions.add(clonedCondition);
	}
	clon.conditions.clear();
	clon.conditions.addAll(clonedConditions);

	if (currGroupCondition != null) {
	    clon.currGroupCondition = currGroupCondition.clon();
	    // TODO ??? check
	    clon.currGroupCondition.getGroup().parentGroupReference = clon;
	}

	return clon;
    }

    /**
     * Pushes current condition out of the buffer. Ignores canceled conditions.
     */
    public void pushCurrCondition(final Condition condition) {
	if (conditions.size() == 0 && condition.getLogicalOperator() != null) {
	    condition.setLogicalOperator(null);
	}
	conditions.add(condition);
	resetCurrentValues();
    }

    public void resetCurrentValues() {
	currGroupCondition = null;
	currProperty = null;
	currLogicalOperator = null;
	currOperation = null;
	currNegated = false;
    }

    @Override
    public String getSql(final RootEntityMapper alias) {
	final StringBuffer sb = new StringBuffer();
	if (conditions.size() > 0) {
	    //sb.append(parentGroupReference == null && !onCondition ? "\n   WHERE " : "");

	    for (final Iterator<Condition> iterator = conditions.iterator(); iterator.hasNext();) {
		final Condition condition = iterator.next();
		sb.append(condition.logicalOperatorSql());
		sb.append(condition.getSql(alias));
		if (iterator.hasNext()) {
		    sb.append(" ");
		}
	    }
	}
	return sb.toString();
    }
}