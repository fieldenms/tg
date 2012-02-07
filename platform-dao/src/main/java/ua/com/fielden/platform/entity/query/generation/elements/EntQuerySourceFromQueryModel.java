package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;

public class EntQuerySourceFromQueryModel extends AbstractEntQuerySource {
    private final List<EntQuery> models;

    private EntQuery model() {
	return models.get(0);
    }

    public EntQuerySourceFromQueryModel(final String alias, final EntQuery... models) {
	super(alias);
	this.models = Arrays.asList(models);

	for (final YieldModel yield : this.models.get(0).getYields().getYields().values()) {
	    sourceColumns.put(yield.getAlias(), yield.getSqlAlias());
	}
    }

    @Override
    public Class sourceType() {
	return model().getResultType();
    }

    @Override
    public boolean generated() {
	return false;
    }

    @Override
    protected Pair<String, Class> lookForProp(final String dotNotatedPropName) {
	for (final Pair<String, String> candidate : prepareCandidates(dotNotatedPropName)) {
	    final Pair<String, Class> candidateResult = validateCandidate(candidate.getKey(), candidate.getValue());

	    if (candidateResult != null) {
		return candidateResult;
	    }
	}
	return null;

//	if (EntityUtils.isPersistedEntityType(sourceType())) {
//	    return lookForPropOnPropTypeLevel("id", sourceType(), dotNotatedPropName);
//	}
//
//	if (EntityUtils.isPersistedEntityType(sourceType())) {
//	    return lookForPropOnPropTypeLevel(sourceType(), dotNotatedPropName);
//	} else if (isEntityAggregates(sourceType())) {
//	    return lookForPropInEntAggregatesType(dotNotatedPropName);
//	} else {
//	    throw new RuntimeException("Not yet implemented the case of IQueryModelProvider");
//	}
    }

    private Pair<String, Class> validateCandidate(final String first, final String rest) {
	final YieldModel firstLevelPropYield = model().getYield(first);
	if (firstLevelPropYield == null) { // there are no such first level prop at all within source query yields
	    return null;
	} else if (firstLevelPropYield.getType() == null) { //such property is present, but its type is definitely not entity, that's why it can't have subproperties
	    return StringUtils.isEmpty(rest) ? new Pair<String, Class>(first, null) : null;
	} else if (!StringUtils.isEmpty(rest)) {
	    try {
		return new Pair<String, Class>(first, determinePropertyType(firstLevelPropYield.getType(), rest));
	    } catch (final Exception e) {
		return null;
	    }
	} else {
	    return new Pair<String, Class>(first, firstLevelPropYield.getType());
	}
    }

    /**
     * Generates one dot.notated string from list of strings (subproperties).
     * @param parts
     * @return
     */
    private static String joinWithDot(final List<String> parts) {
	final StringBuffer sb = new StringBuffer();
	for (final Iterator<String> iterator = parts.iterator(); iterator.hasNext();) {
	    sb.append(iterator.next());
	    if (iterator.hasNext()) {
		sb.append(".");
	    }
	}
	return sb.toString();
    }

    private static List<Pair<String, String>> prepareCandidates(final String dotNotatedPropName) {
	final List<Pair<String, String>> result =  new ArrayList<Pair<String,String>>();
	final List<String> parts = Arrays.asList(dotNotatedPropName.split("\\."));

	for (int i = parts.size(); i >=1 ; i--) {
	    result.add(new Pair<String, String>(joinWithDot(parts.subList(0, i)), joinWithDot(parts.subList(i, parts.size()))));
	}

	return result;
    }

    @Override
    public Class propType(final String propSimpleName) {
	if (EntityUtils.isPersistedEntityType(sourceType())) {
	    return super.propType(propSimpleName);
	} else if (isEntityAggregates(sourceType())) {
	    return model().getYield(propSimpleName).getType();
	} else {
	    throw new RuntimeException("Not yet supported");
	}
    }

    @Override
    public List<EntValue> getValues() {
	final List<EntValue> result = new ArrayList<EntValue>();
	for (final EntQuery entQry : models) {
	    result.addAll(entQry.getAllValues());
	}
	return result;
    }

    @Override
    public String sql() {
	return "(" + models.get(0).sql() + ") AS " + sqlAlias + "/*" + alias + "*/";
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
	if (!(obj instanceof EntQuerySourceFromQueryModel)) {
	    return false;
	}
	final EntQuerySourceFromQueryModel other = (EntQuerySourceFromQueryModel) obj;
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
    public List<EntQueryCompoundSourceModel> generateMissingSources(final boolean parentLeftJoinLegacy) {
	// TODO Auto-generated method stub
	return null;
    }
}