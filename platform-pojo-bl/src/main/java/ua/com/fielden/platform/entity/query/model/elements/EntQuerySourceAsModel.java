package ua.com.fielden.platform.entity.query.model.elements;

import java.util.Arrays;
import java.util.List;

import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

public class EntQuerySourceAsModel extends AbstractEntQuerySource {
    private final List<EntQuery> models;

    public EntQuerySourceAsModel(final String alias, final EntQuery... models) {
	super(alias);
	this.models = Arrays.asList(models);

	for (final YieldModel yield : this.models.get(0).getYields().getYields().values()) {
	    sourceColumns.put(yield.getAlias(), yield.getSqlAlias());
	}
    }

    @Override
    public Class sourceType() {
	return models.get(0).getResultType();
    }

    @Override
    public boolean generated() {
	return false;
    }

    @Override
    protected Pair<Boolean, Class> lookForPropInEntAggregatesType(final Class parentType, final String dotNotatedPropName) {
	final Pair<String, String> splitByDot = EntityUtils.splitPropByFirstDot(dotNotatedPropName);
	final String first = splitByDot.getKey();
	final String rest = splitByDot.getValue();

	final YieldModel firstLevelPropYield = models.get(0).getYield(first);
	if (firstLevelPropYield == null) { // there are no such first level prop at all within source query props
	    return new Pair<Boolean, Class>(false, null);
	} else if (firstLevelPropYield.getOperand().type() == null) { //such property is present, but its type is definitely not entity, that's why it can't have subproperties
	    return new Pair<Boolean, Class>(rest == null, null);
	} else if (rest != null) {
	    return lookForProp(firstLevelPropYield.getOperand().type(), rest); //continue recursively to subproperties
	} else {
	    return new Pair<Boolean, Class>(true, firstLevelPropYield.getOperand().type());
	}
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((getAlias() == null) ? 0 : getAlias().hashCode());
	result = prime * result + ((models == null) ? 0 : models.hashCode());
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
	if (!(obj instanceof EntQuerySourceAsModel)) {
	    return false;
	}
	final EntQuerySourceAsModel other = (EntQuerySourceAsModel) obj;
	if (getAlias() == null) {
	    if (other.getAlias() != null) {
		return false;
	    }
	} else if (!getAlias().equals(other.getAlias())) {
	    return false;
	}
	if (models == null) {
	    if (other.models != null) {
		return false;
	    }
	} else if (!models.equals(other.models)) {
	    return false;
	}
	return true;
    }

    @Override
    public Class propType(final String propSimpleName) {
	if (EntityUtils.isPersistedEntityType(sourceType())) {
	    return super.propType(propSimpleName);
	} else if (isEntityAggregates(sourceType())) {
	    return models.get(0).getYield(propSimpleName).getOperand().type();
	} else {
	    throw new RuntimeException("Not yet supported");
	}
    }

    @Override
    public String sql() {
	return "(" + models.get(0).sql() + ") AS " + sqlAlias + "/*" + alias + "*/";
    }
}