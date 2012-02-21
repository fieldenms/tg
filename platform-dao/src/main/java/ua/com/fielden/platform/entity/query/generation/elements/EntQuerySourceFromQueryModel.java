package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.dao.MappingsGenerator;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.utils.Pair;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;

public class EntQuerySourceFromQueryModel extends AbstractEntQuerySource {
    private final List<EntQuery> models;

    private EntQuery model() {
	return models.get(0);
    }

    public EntQuerySourceFromQueryModel(final String alias, final MappingsGenerator mappingsGenerator, final EntQuery... models) {
	super(alias, mappingsGenerator);
	this.models = Arrays.asList(models);

	for (final YieldModel yield : model().getYields().getYields().values()) {
	    sourceItems.put(yield.getAlias(), yield.getInfo());
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
    protected Pair<PurePropInfo, PurePropInfo> lookForProp(final String dotNotatedPropName) {
	for (final Pair<String, String> candidate : prepareCandidates(dotNotatedPropName)) {
	    final Pair<PurePropInfo, Class> candidateResult = validateCandidate(candidate.getKey(), candidate.getValue());

	    if (candidateResult != null) {
		return new Pair<PurePropInfo, PurePropInfo>(candidateResult.getKey(), new PurePropInfo(dotNotatedPropName, candidateResult.getValue(), null)) ;
	    }
	}
	return null;
    }

    private Pair<PurePropInfo, Class> validateCandidate(final String first, final String rest) {
	final YieldModel firstLevelPropYield = model().getYield(first);
	if (firstLevelPropYield == null) { // there are no such first level prop at all within source query yields
	    return null;
	} else if (firstLevelPropYield.getInfo().getJavaType() == null) { //such property is present, but its type is definitely not entity, that's why it can't have subproperties
	    return StringUtils.isEmpty(rest) ? new Pair<PurePropInfo, Class>(new PurePropInfo(first, null, null), null) : null;
	} else if (!StringUtils.isEmpty(rest)) {
	    try {
		return new Pair<PurePropInfo, Class>(new PurePropInfo(first, firstLevelPropYield.getInfo().getJavaType(), null), determinePropertyType(firstLevelPropYield.getInfo().getJavaType(), rest));
	    } catch (final Exception e) {
		return null;
	    }
	} else {
	    return new Pair<PurePropInfo, Class>(new PurePropInfo(first, firstLevelPropYield.getInfo().getJavaType(), null), firstLevelPropYield.getInfo().getJavaType());
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
    protected boolean isRequired(final String propName) {
	if (sourceType().equals(EntityAggregates.class)) {
	    return false;
	} else {
	    return super.isRequired(propName);
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
	final StringBuffer sb = new StringBuffer();
	sb.append("(");
	for (final Iterator<EntQuery> iterator = models.iterator(); iterator.hasNext();) {
	    sb.append(iterator.next().sql());
	    sb.append(iterator.hasNext() ? "\nUNION ALL\n" : "");
	}
	sb.append(") AS " + sqlAlias + "/*" + alias + "*/");
	return sb.toString();
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
}