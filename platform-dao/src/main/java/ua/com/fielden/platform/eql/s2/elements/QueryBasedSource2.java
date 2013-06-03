package ua.com.fielden.platform.eql.s2.elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.dao.DomainMetadataAnalyser;
import ua.com.fielden.platform.utils.Pair;

public class QueryBasedSource2 extends AbstractSource2 {
    private final List<EntQuery2> models;
    private final Map<String, List<Yield2>> yieldsMatrix = new HashMap<String, List<Yield2>>();

    private EntQuery2 firstModel() {
	return models.get(0);
    }

    public QueryBasedSource2(final String alias, final DomainMetadataAnalyser domainMetadataAnalyser, final EntQuery2... models) {
	super(alias, domainMetadataAnalyser, checkWhetherResultTypeIsPersisted(models));
	this.models = Arrays.asList(models);
//	populateYieldMatrixFromQueryModels(models);
//	validateYieldsMatrix();
    }

    private static boolean checkWhetherResultTypeIsPersisted(final EntQuery2... models) {
	if (models == null || models.length == 0) {
	    throw new IllegalArgumentException("Couldn't produce instance of QueryBasedSource due to zero models passed to constructor!");
	}
	return models[0].isPersistedType();
    }

//    private void populateYieldMatrixFromQueryModels(final EntQuery... models) {
//	for (final EntQuery entQuery : models) {
//	    for (final Yield yield : entQuery.getYields().getYields()) {
//		final List<Yield> foundYields = yieldsMatrix.get(yield.getAlias());
//		if (foundYields != null) {
//		    foundYields.add(yield);
//		} else {
//		    final List<Yield> newList = new ArrayList<Yield>();
//		    newList.add(yield);
//		    yieldsMatrix.put(yield.getAlias(), newList);
//		}
//	    }
//	}
//    }

    private boolean getYieldNullability(final String yieldAlias) {
	final boolean result = false;
	for (final Yield2 yield : yieldsMatrix.get(yieldAlias)) {
	    if (yield.getInfo().isNullable()) {
		return true;
	    }
	}
	return result;
    }

    private void validateYieldsMatrix() {
	for (final Map.Entry<String, List<Yield2>> entry : yieldsMatrix.entrySet()) {
	    if (entry.getValue().size() != models.size()) {
		throw new IllegalStateException("Incorrect models used as query source - their result types are different!");
	    }
	}
    }

    @Override
    public Class sourceType() {
	return firstModel().type();
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
    public List<EntValue2> getValues() {
	final List<EntValue2> result = new ArrayList<EntValue2>();
	for (final EntQuery2 entQry : models) {
	    result.addAll(entQry.getAllValues());
	}
	return result;
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
	if (!(obj instanceof QueryBasedSource2)) {
	    return false;
	}
	final QueryBasedSource2 other = (QueryBasedSource2) obj;
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