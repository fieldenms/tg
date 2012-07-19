package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.dao.DomainMetadataAnalyser;
import ua.com.fielden.platform.dao.PropertyMetadata;
import ua.com.fielden.platform.utils.Pair;

public class QueryBasedSource extends AbstractSource {
    private final List<EntQuery> models;
    private final Map<String, List<Yield>> yieldsMatrix = new HashMap<String, List<Yield>>();

    private EntQuery model() {
	return models.get(0);
    }

    public QueryBasedSource(final String alias, final DomainMetadataAnalyser domainMetadataAnalyser, final EntQuery... models) {
	super(alias, domainMetadataAnalyser);
	this.models = Arrays.asList(models);
	populateYieldMatrixFromQueryModels(models);
	validateYieldsMatrix();
    }

    private void populateYieldMatrixFromQueryModels(final EntQuery... models) {
	for (final EntQuery entQuery : models) {
	    for (final Yield yield : entQuery.getYields().getYields()) {
		final List<Yield> foundYields = yieldsMatrix.get(yield.getAlias());
		if (foundYields != null) {
		    foundYields.add(yield);
		} else {
		    final List<Yield> newList = new ArrayList<Yield>();
		    newList.add(yield);
		    yieldsMatrix.put(yield.getAlias(), newList);
		}
	    }
	}
    }

    private boolean getYieldNullability(final String yieldAlias) {
	final boolean result = false;
	for (final Yield yield : yieldsMatrix.get(yieldAlias)) {
	    if (yield.getInfo().isNullable()) {
		return true;
	    }
	}
	return result;
    }

    private void validateYieldsMatrix() {
	for (final Map.Entry<String, List<Yield>> entry : yieldsMatrix.entrySet()) {
	    if (entry.getValue().size() != models.size()) {
		throw new IllegalStateException("Incorrect models used as query source - their result types are different!");
	    }
	}
    }

    @Override
    public void populateSourceItems(final boolean parentLeftJoinLegacy) {
	for (final Yield yield : model().getYields().getYields()) {
	    sourceItems.put(yield.getAlias(), new ResultQueryYieldDetails(yield.getInfo().getName(), yield.getInfo().getJavaType(), yield.getInfo().getHibType(), yield.getInfo().getColumn(), //
		    getYieldNullability(yield.getInfo().getName())/*yield.getInfo().isNullable()*/ || parentLeftJoinLegacy, yield.getInfo().getYieldDetailsType()));
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
	    final Pair<PurePropInfo, PurePropInfo> candidateResult = validateCandidate(dotNotatedPropName, candidate.getKey(), candidate.getValue());
	    if (candidateResult != null) {
		return candidateResult;
	    }
	}

	return null;
    }

    private Pair<PurePropInfo, PurePropInfo> validateCandidate(final String dotNotatedPropName, final String first, final String rest) {
	final Yield firstLevelPropYield = model().getYields().getYieldByAlias(first);
	if (firstLevelPropYield == null) { // there are no such first level prop at all within source query yields
	    final PropertyMetadata explicitPropMetadata = getDomainMetadataAnalyser().getInfoForDotNotatedProp(sourceType(), first);
	    if (explicitPropMetadata == null) {
		return null;
	    } else {
		if (explicitPropMetadata.isCalculated()) {
		    if (explicitPropMetadata.getJavaType() == null) {
			return StringUtils.isEmpty(rest) ? new Pair<PurePropInfo, PurePropInfo>(new PurePropInfo(first, null, null, true), new PurePropInfo(first, null, null, true)) : null;
		    } else if (!StringUtils.isEmpty(rest)) {
			    final PropertyMetadata propInfo = getDomainMetadataAnalyser().getInfoForDotNotatedProp(explicitPropMetadata.getJavaType(), rest);
			    if (propInfo == null) {

				return null;
			    } else {
				final boolean propNullability = getDomainMetadataAnalyser().isNullable(explicitPropMetadata.getJavaType(), rest);
				final boolean explicitPartNullability = explicitPropMetadata.isNullable() || isNullable();

				return new Pair<PurePropInfo, PurePropInfo>(
					new PurePropInfo(first, explicitPropMetadata.getJavaType(), explicitPropMetadata.getHibType(), explicitPartNullability),
					new PurePropInfo(dotNotatedPropName, propInfo.getJavaType(), propInfo.getHibType(), propNullability || explicitPartNullability));
			    }
		    } else {

			    final PurePropInfo ppi = new PurePropInfo(first, explicitPropMetadata.getJavaType(), explicitPropMetadata.getHibType(), explicitPropMetadata.isNullable() || isNullable());
			    ppi.setExpressionModel(explicitPropMetadata.getExpressionModel());
			    return new Pair<PurePropInfo, PurePropInfo>(ppi, ppi);
		    }
//		    throw new RuntimeException("Implementation pending! Additional info: " + dotNotatedPropName + " " + explicitPropMetadata);
		} else {
		    return null;
		}
	    }
	} else if (firstLevelPropYield.getInfo().getJavaType() == null) { //such property is present, but its type is definitely not entity, that's why it can't have subproperties
	    return StringUtils.isEmpty(rest) ? new Pair<PurePropInfo, PurePropInfo>(new PurePropInfo(first, null, null, true), new PurePropInfo(first, null, null, true)) : null;
	} else if (!StringUtils.isEmpty(rest)) {
	    final PropertyMetadata propInfo = getDomainMetadataAnalyser().getInfoForDotNotatedProp(firstLevelPropYield.getInfo().getJavaType(), rest);
	    if (propInfo == null) {
		return null;
	    } else {
		final boolean propNullability = getDomainMetadataAnalyser().isNullable(firstLevelPropYield.getInfo().getJavaType(), rest);
		final boolean explicitPartNullability = getYieldNullability(firstLevelPropYield.getAlias())/*firstLevelPropYield.getInfo().isNullable()*/ || isNullable();
		return new Pair<PurePropInfo, PurePropInfo>(
			new PurePropInfo(first, firstLevelPropYield.getInfo().getJavaType(), firstLevelPropYield.getInfo().getHibType(), explicitPartNullability),
			new PurePropInfo(dotNotatedPropName, propInfo.getJavaType(), propInfo.getHibType(), propNullability || explicitPartNullability));
	    }
	} else {
	    final PurePropInfo ppi = new PurePropInfo(first, firstLevelPropYield.getInfo().getJavaType(), firstLevelPropYield.getInfo().getHibType(), getYieldNullability(firstLevelPropYield.getAlias())/*firstLevelPropYield.getInfo().isNullable()*/ || isNullable());

	    return new Pair<PurePropInfo, PurePropInfo>(ppi, ppi);
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
	int result = super.hashCode();
	result = prime * result + ((models == null) ? 0 : models.hashCode());
	return result;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (!super.equals(obj)) {
	    return false;
	}
	if (!(obj instanceof QueryBasedSource)) {
	    return false;
	}
	final QueryBasedSource other = (QueryBasedSource) obj;
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