package ua.com.fielden.platform.equery.tokens.conditions;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.LogicalOperator;
import ua.com.fielden.platform.equery.QueryParameter;
import ua.com.fielden.platform.equery.interfaces.IClon;
import ua.com.fielden.platform.equery.interfaces.IQueryModel;
import ua.com.fielden.platform.equery.interfaces.IQueryToken;
import ua.com.fielden.platform.equery.tokens.properties.SearchProperty;

public abstract class Condition<T> implements IQueryToken, IClon<T> {
    private LogicalOperator logicalOperator;

    protected Condition() {
    }

    public LogicalOperator getLogicalOperator() {
	return logicalOperator;
    }

    public void setLogicalOperator(final LogicalOperator logicalOperator) {
	this.logicalOperator = logicalOperator;
    }

    public String logicalOperatorSql() {
	return getLogicalOperator() != null ? getLogicalOperator().getValue() + " " : "";
    }

    public List<SearchProperty> getProps() {
	return new ArrayList<SearchProperty>();
    }

    public List<QueryParameter> getPropsParams(){
	final List<QueryParameter> result = new ArrayList<QueryParameter>();
	for (final SearchProperty prop : getProps()) {
	    result.addAll(prop.getParams());
	}
	return result;
    }

    public List<IQueryModel<? extends AbstractEntity>> getPropsModels(){
	final List<IQueryModel<? extends AbstractEntity>> result = new ArrayList<IQueryModel<? extends AbstractEntity>>();
	for (final SearchProperty prop : getProps()) {
	    result.addAll(prop.getModels());
	}
	return result;
    }
}
