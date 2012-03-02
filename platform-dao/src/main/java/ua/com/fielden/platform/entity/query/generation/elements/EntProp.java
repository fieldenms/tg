package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EntProp implements ISingleOperand {
    private final String name;
    private Class propType;
    private Object hibType;
    private boolean nullable;

    private EntQuery holder; // query, where given property resides (not to be mixed with the query, which source given property is resolved to - in case of correlated subqueries these two may be different queries (subquery and one of its master).
    private String sql;
    private IEntQuerySource source;

    @Override
    public String toString() {
        return name + "[" + (propType != null ? propType.getSimpleName() : "") + "]";
    }

    @Override
    public String sql() {
	return (source != null ? source.getSqlAlias() : "?") +  "." + sql;
    }

    public EntProp(final String name) {
	super();
	this.name = name;
    }

    public EntProp(final String name, final Class propType, final Object hibType, final EntQuery holder) {
	this(name);
	this.propType = propType;
	this.holder = holder;
	this.hibType = hibType;
    }

    @Override
    public List<EntProp> getLocalProps() {
	return Arrays.asList(new EntProp[]{this});
    }

    @Override
    public List<EntQuery> getLocalSubQueries() {
	return Collections.emptyList();
    }

    @Override
    public List<EntValue> getAllValues() {
	return Collections.emptyList();
    }

    public Class getPropType() {
        return propType;
    }

    public Object getHibType() {
	return hibType;
    }

    public void setHibType(final Object hibType) {
	this.hibType = hibType;
    }

    public void setPropType(final Class propType) {
        this.propType = propType;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean ignore() {
	return false;
    }

    @Override
    public Class type() {
	return propType;
    }

    public EntQuery getHolder() {
        return holder;
    }

    public void assignHolderIfNotAssigned(final EntQuery holder) {
        if (this.holder == null) {
            this.holder = holder;
        }
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((name == null) ? 0 : name.hashCode());
	return result;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (!(obj instanceof EntProp)) {
	    return false;
	}
	final EntProp other = (EntProp) obj;
	if (name == null) {
	    if (other.name != null) {
		return false;
	    }
	} else if (!name.equals(other.name)) {
	    return false;
	}
	return true;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(final String sql) {
        this.sql = sql;
    }

    @Override
    public Object hibType() {
	return hibType;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(final boolean nullable) {
        this.nullable = nullable;
    }

    public void setSource(final IEntQuerySource source) {
	this.source = source;
    }
}