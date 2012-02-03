package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.Arrays;
import java.util.List;


public class EntSetFromQryModel implements ISetOperand {
    private final EntQuery model;

    @Override
    public String sql() {
	return model.sql();
    }

    public EntSetFromQryModel(final EntQuery model) {
	super();
	this.model = model;
    }

    @Override
    public List<EntProp> getLocalProps() {
	return model.getLocalProps();
    }

    @Override
    public List<EntValue> getAllValues() {
	return model.getAllValues();
    }

    @Override
    public List<EntQuery> getLocalSubQueries() {
	return Arrays.asList(new EntQuery[]{model});
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((model == null) ? 0 : model.hashCode());
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
	if (!(obj instanceof EntSetFromQryModel)) {
	    return false;
	}
	final EntSetFromQryModel other = (EntSetFromQryModel) obj;
	if (model == null) {
	    if (other.model != null) {
		return false;
	    }
	} else if (!model.equals(other.model)) {
	    return false;
	}
	return true;
    }
}